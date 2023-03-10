package me.zohar.runscore.useraccount.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.useragent.UserAgent;
import lombok.extern.slf4j.Slf4j;
import me.zohar.runscore.common.utils.IdUtils;
import me.zohar.runscore.common.utils.ThreadPoolUtils;
import me.zohar.runscore.common.vo.PageResult;
import me.zohar.runscore.useraccount.domain.LoginLog;
import me.zohar.runscore.useraccount.param.LoginLogQueryCondParam;
import me.zohar.runscore.useraccount.repo.LoginLogRepo;
import me.zohar.runscore.useraccount.vo.LoginLogVO;

@Validated
@Slf4j
@Service
public class LoginLogService {

	public static final String 淘宝IP查询地址 = "http://ip.taobao.com/service/getIpInfo.php";

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private LoginLogRepo loginLogRepo;

	@Autowired
	private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

	@Transactional
	public void logout(@NotBlank String sessionId) {
		sessionRepository.deleteById(sessionId);
	}

	@Transactional
	public List<String> getSessionId() {
		List<String> sessionIds = new ArrayList<>();
		String prefix = "spring:session:index:" + FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME + ":";
		Set<String> keys = redisTemplate.keys(prefix + "*");
		for (String key : keys) {
			String principalName = key.split(prefix)[1];
			Map<String, ? extends Session> sessionMap = sessionRepository.findByPrincipalName(principalName);
			for (Entry<String, ? extends Session> entry : sessionMap.entrySet()) {
				sessionIds.add(entry.getKey());
			}
		}
		return sessionIds;
	}

	@Transactional(readOnly = true)
	public PageResult<LoginLogVO> findOnlineAccountByPage(LoginLogQueryCondParam param) {
		param.setSessionIds(getSessionId());
		return findLoginLogByPage(param);
	}

	@Transactional
	public void updateLastAccessTime(String sessionId) {
		LoginLog loginLog = loginLogRepo.findTopBySessionIdOrderByLoginTime(sessionId);
		if (loginLog != null) {
			loginLog.setLastAccessTime(new Date());
			loginLogRepo.save(loginLog);
		}
	}

	@Transactional(readOnly = true)
	public PageResult<LoginLogVO> findLoginLogByPage(LoginLogQueryCondParam param) {
		Specification<LoginLog> spec = new Specification<LoginLog>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<LoginLog> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotEmpty(param.getIpAddr())) {
					predicates.add(builder.equal(root.get("ipAddr"), param.getIpAddr()));
				}
				if (StrUtil.isNotEmpty(param.getUserName())) {
					predicates.add(builder.equal(root.get("userName"), param.getUserName()));
				}
				if (StrUtil.isNotEmpty(param.getState())) {
					predicates.add(builder.equal(root.get("state"), param.getState()));
				}
				if (param.getStartTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("loginTime").as(Date.class),
							DateUtil.beginOfDay(param.getStartTime())));
				}
				if (param.getEndTime() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("loginTime").as(Date.class),
							DateUtil.endOfDay(param.getEndTime())));
				}
				if (CollectionUtil.isNotEmpty(param.getSessionIds())) {
					predicates.add(root.get("sessionId").in(param.getSessionIds()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<LoginLog> result = loginLogRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("loginTime"))));
		PageResult<LoginLogVO> pageResult = new PageResult<>(LoginLogVO.convertFor(result.getContent()),
				param.getPageNum(), param.getPageSize(), result.getTotalElements());
		return pageResult;
	}

	@Transactional
	public void recordLoginLog(String sessionId, String userName, String system, String state, String msg,
			String ipAddr, UserAgent userAgent) {
		ThreadPoolUtils.getLoginLogPool().schedule(() -> {
			Date now = new Date();
			String loginLocation = null;
			try {
				String respResult = HttpUtil.get(淘宝IP查询地址 + "?ip=" + ipAddr);
				JSONObject jsonObject = JSONObject.parseObject(respResult);
				loginLocation = jsonObject.getJSONObject("data").getString("region")
						+ jsonObject.getJSONObject("data").getString("city");
			} catch (Exception e) {
				log.error("ip查询异常", e);
			}
			LoginLog loginLog = new LoginLog();
			loginLog.setId(IdUtils.getId());
			loginLog.setSessionId(sessionId);
			loginLog.setUserName(userName);
			loginLog.setSystem(system);
			loginLog.setState(state);
			loginLog.setMsg(msg);
			loginLog.setIpAddr(ipAddr);
			loginLog.setLoginLocation(loginLocation);
			loginLog.setLoginTime(now);
			loginLog.setBrowser(userAgent.getBrowser().getName());
			loginLog.setOs(userAgent.getOs().getName());
			loginLogRepo.save(loginLog);

		}, 10, TimeUnit.MILLISECONDS);
	}

}
