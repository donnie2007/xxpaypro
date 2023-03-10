package me.zohar.runscore.rechargewithdraw.service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import me.zohar.runscore.common.exception.BizError;
import me.zohar.runscore.common.exception.BizException;
import me.zohar.runscore.common.utils.SpringUtils;
import me.zohar.runscore.common.utils.ThreadPoolUtils;
import me.zohar.runscore.common.valid.ParamValid;
import me.zohar.runscore.common.vo.PageResult;
import me.zohar.runscore.constants.Constant;
import me.zohar.runscore.mastercontrol.domain.RechargeSetting;
import me.zohar.runscore.mastercontrol.repo.RechargeSettingRepo;
import me.zohar.runscore.rechargewithdraw.domain.PayChannel;
import me.zohar.runscore.rechargewithdraw.domain.RechargeOrder;
import me.zohar.runscore.rechargewithdraw.param.AbcyzfCallbackParam;
import me.zohar.runscore.rechargewithdraw.param.LowerLevelRechargeOrderQueryCondParam;
import me.zohar.runscore.rechargewithdraw.param.RechargeOrderParam;
import me.zohar.runscore.rechargewithdraw.param.RechargeOrderQueryCondParam;
import me.zohar.runscore.rechargewithdraw.repo.PayChannelRepo;
import me.zohar.runscore.rechargewithdraw.repo.RechargeOrderRepo;
import me.zohar.runscore.rechargewithdraw.vo.RechargeOrderVO;
import me.zohar.runscore.useraccount.domain.AccountChangeLog;
import me.zohar.runscore.useraccount.domain.UserAccount;
import me.zohar.runscore.useraccount.repo.AccountChangeLogRepo;
import me.zohar.runscore.useraccount.repo.UserAccountRepo;

@Validated
@Slf4j
@Service
public class RechargeService {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private RechargeOrderRepo rechargeOrderRepo;

	@Autowired
	private UserAccountRepo userAccountRepo;

	@Autowired
	private AccountChangeLogRepo accountChangeLogRepo;

	@Autowired
	private RechargeSettingRepo rechargeSettingRepo;

	@Autowired
	private PayChannelRepo payChannelRepo;

	@ParamValid
	public void checkOrderWithAbcyzf(AbcyzfCallbackParam param) {
		if (!Abcyzf.??????????????????.equals(param.getTrade_status())) {
			return;
		}
		String sign = Abcyzf.generateCallbackSign(param.getOut_trade_no(), param.getMoney(), param.getType(),
				param.getTrade_no(), param.getTrade_status());
		if (!sign.equals(param.getSign())) {
			throw new BizException(BizError.???????????????);
		}
		checkOrder(param.getOut_trade_no(), Double.parseDouble(param.getMoney()), new Date());
	}

	/**
	 * ????????????
	 */
	@Transactional
	public void checkOrder(@NotBlank String orderNo,
			@NotNull @DecimalMin(value = "0", inclusive = false) Double actualPayAmount, @NotNull Date payTime) {
		RechargeOrder order = rechargeOrderRepo.findByOrderNo(orderNo);
		if (order == null) {
			throw new BizException(BizError.?????????????????????);
		}
		if (Constant.??????????????????_?????????.equals(order.getOrderState())) {
			return;
		}

		order.updatePayInfo(actualPayAmount, payTime);
		rechargeOrderRepo.save(order);

		if (order.getRechargeAmount().compareTo(actualPayAmount) != 0) {
			log.warn("??????????????????????????????????????????,????????????????????????;?????????????????????{}", orderNo);
			return;
		}
		ThreadPoolUtils.getRechargeSettlementPool().schedule(() -> {
			redisTemplate.opsForList().leftPush(Constant.????????????_?????????????????????, order.getOrderNo());
		}, 1, TimeUnit.SECONDS);
	}

	/**
	 * ????????????
	 * 
	 * @param orderNo
	 */
	@Transactional(readOnly = true)
	public void manualSettlement(@NotBlank String orderNo) {
		redisTemplate.opsForList().leftPush(Constant.????????????_?????????????????????, orderNo);
	}

	/**
	 * ??????????????????
	 */
	@Transactional
	public void rechargeOrderSettlement(String orderNo) {
		RechargeOrder rechargeOrder = rechargeOrderRepo.findByOrderNo(orderNo);
		if (rechargeOrder == null) {
			throw new BizException(BizError.?????????????????????);
		}
		if (!Constant.??????????????????_?????????.equals(rechargeOrder.getOrderState())) {
			return;
		}

		rechargeOrder.settlement();
		rechargeOrderRepo.save(rechargeOrder);
		UserAccount userAccount = rechargeOrder.getUserAccount();
		double cashDeposit = userAccount.getCashDeposit() + rechargeOrder.getActualPayAmount();
		userAccount.setCashDeposit(NumberUtil.round(cashDeposit, 4).doubleValue());
		userAccountRepo.save(userAccount);
		accountChangeLogRepo.save(AccountChangeLog.buildWithRecharge(userAccount, rechargeOrder));
		updateCashDepositWithRechargePreferential(userAccount, rechargeOrder.getActualPayAmount());
	}

	/**
	 * ??????????????????????????????
	 */
	public void updateCashDepositWithRechargePreferential(UserAccount userAccount, Double actualPayAmount) {
		RechargeSetting setting = rechargeSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null || !setting.getReturnWaterRateEnabled() || setting.getReturnWaterRate() == null) {
			return;
		}

		double returnWater = actualPayAmount * setting.getReturnWaterRate() * 0.01;
		double cashDeposit = userAccount.getCashDeposit() + returnWater;
		userAccount.setCashDeposit(NumberUtil.round(cashDeposit, 4).doubleValue());
		userAccountRepo.save(userAccount);
		accountChangeLogRepo.save(
				AccountChangeLog.buildWithRechargePreferential(userAccount, returnWater, setting.getReturnWaterRate()));
	}

	@Transactional(readOnly = true)
	public void rechargeOrderAutoSettlement() {
		List<RechargeOrder> orders = rechargeOrderRepo.findByPayTimeIsNotNullAndSettlementTimeIsNullOrderBySubmitTime();
		for (RechargeOrder order : orders) {
			// ???????????????????????????????????????????????????,??????????????????
			if (order.getRechargeAmount().compareTo(order.getActualPayAmount()) != 0) {
				log.warn("??????????????????????????????????????????,????????????????????????;?????????????????????{}", order.getOrderNo());
				continue;
			}
			redisTemplate.opsForList().leftPush(Constant.????????????_?????????????????????, order.getOrderNo());
		}
	}

	/**
	 * ??????????????????
	 */
	@Transactional
	public void orderTimeoutDeal() {
		Date now = new Date();
		List<RechargeOrder> orders = rechargeOrderRepo.findByOrderStateAndUsefulTimeLessThan(Constant.??????????????????_?????????, now);
		for (RechargeOrder order : orders) {
			order.setDealTime(now);
			order.setOrderState(Constant.??????????????????_????????????);
			rechargeOrderRepo.save(order);
		}
	}

	@ParamValid
	@Transactional
	public RechargeOrderVO generateRechargeOrder(RechargeOrderParam param) {
		PayChannel payChannel = payChannelRepo.getOne(param.getPayChannelId());
		if (Constant.????????????_???????????????.equals(payChannel.getPayType().getPayCategory())
				|| Constant.????????????_????????????.equals(payChannel.getPayType().getPayCategory())
				|| Constant.????????????_?????????.equals(payChannel.getPayType().getPayCategory())) {
			if (param.getDepositTime() == null) {
				throw new BizException(BizError.????????????????????????);
			}
			if (StrUtil.isBlank(param.getDepositor())) {
				throw new BizException(BizError.???????????????????????????);
			}
		}
		RechargeSetting rechargeSetting = rechargeSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (param.getRechargeAmount() < rechargeSetting.getRechargeLowerLimit()) {
			throw new BizException(BizError.????????????.getCode(),
					"??????????????????????????????" + new DecimalFormat("###################.###########")
							.format(rechargeSetting.getRechargeLowerLimit()));
		}
		if (param.getRechargeAmount() > rechargeSetting.getRechargeUpperLimit()) {
			throw new BizException(BizError.????????????.getCode(),
					"??????????????????????????????" + new DecimalFormat("###################.###########")
							.format(rechargeSetting.getRechargeUpperLimit()));
		}
		Integer orderEffectiveDuration = Constant.??????????????????????????????;
		if (rechargeSetting != null) {
			orderEffectiveDuration = rechargeSetting.getOrderEffectiveDuration();
		}

		RechargeOrder rechargeOrder = param.convertToPo(orderEffectiveDuration);
		rechargeOrder.setPayCategory(payChannel.getPayType().getPayCategory());
		if (Constant.????????????_???????????????.equals(payChannel.getPayType().getPayCategory())
				|| Constant.????????????_????????????.equals(payChannel.getPayType().getPayCategory())
				|| Constant.????????????_?????????.equals(payChannel.getPayType().getPayCategory())) {
			rechargeOrder.setUsefulTime(null);
		} else {
			PayPlatformService payPlatformService = SpringUtils.getBean(payChannel.getPayPlatformCode());
			String payUrl = payPlatformService.startPay(rechargeOrder.getOrderNo(), rechargeOrder.getRechargeAmount(),
					payChannel.getPayPlatformChannelCode());
			rechargeOrder.setPayUrl(payUrl);
		}
		rechargeOrderRepo.save(rechargeOrder);
		return RechargeOrderVO.convertFor(rechargeOrder);
	}

	@Transactional(readOnly = true)
	public PageResult<RechargeOrderVO> findTop5TodoRechargeOrderByPage() {
		Specification<RechargeOrder> spec = new Specification<RechargeOrder>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<RechargeOrder> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("orderState"), Constant.??????????????????_?????????));
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<RechargeOrder> result = rechargeOrderRepo.findAll(spec,
				PageRequest.of(0, 5, Sort.by(Sort.Order.desc("submitTime"))));
		PageResult<RechargeOrderVO> pageResult = new PageResult<>(RechargeOrderVO.convertFor(result.getContent()), 1, 5,
				result.getTotalElements());
		return pageResult;
	}

	@Transactional(readOnly = true)
	public PageResult<RechargeOrderVO> findRechargeOrderByPage(RechargeOrderQueryCondParam param) {
		Specification<RechargeOrder> spec = new Specification<RechargeOrder>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<RechargeOrder> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotBlank(param.getOrderNo())) {
					predicates.add(builder.equal(root.get("orderNo"), param.getOrderNo()));
				}
				if (StrUtil.isNotBlank(param.getRechargeWayCode())) {
					predicates.add(builder.equal(root.get("rechargeWayCode"), param.getRechargeWayCode()));
				}
				if (StrUtil.isNotBlank(param.getOrderState())) {
					predicates.add(builder.equal(root.get("orderState"), param.getOrderState()));
				}
				if (param.getSubmitStartTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("submitTime").as(Date.class),
							DateUtil.beginOfDay(param.getSubmitStartTime())));
				}
				if (param.getSubmitEndTime() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("submitTime").as(Date.class),
							DateUtil.endOfDay(param.getSubmitEndTime())));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<RechargeOrder> result = rechargeOrderRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("submitTime"))));
		PageResult<RechargeOrderVO> pageResult = new PageResult<>(RechargeOrderVO.convertFor(result.getContent()),
				param.getPageNum(), param.getPageSize(), result.getTotalElements());
		return pageResult;
	}

	/**
	 * ????????????
	 * 
	 * @param id
	 */
	@Transactional
	public void cancelOrder(@NotBlank String id) {
		RechargeOrder rechargeOrder = rechargeOrderRepo.getOne(id);
		if (!Constant.??????????????????_?????????.equals(rechargeOrder.getOrderState())) {
			throw new BizException(BizError.????????????????????????????????????????????????);
		}
		rechargeOrder.setOrderState(Constant.??????????????????_????????????);
		rechargeOrder.setDealTime(new Date());
		rechargeOrderRepo.save(rechargeOrder);
	}

	@Transactional(readOnly = true)
	public RechargeOrderVO findRechargeOrderById(@NotBlank String id) {
		RechargeOrder rechargeOrder = rechargeOrderRepo.getOne(id);
		return RechargeOrderVO.convertFor(rechargeOrder);
	}

	@Transactional
	public void approval(@NotBlank String id, Double actualPayAmount, @NotBlank String approvalResult) {
		if (Constant.??????????????????_????????????.equals(approvalResult)) {
			cancelOrder(id);
		} else if (Constant.??????????????????_?????????.equals(approvalResult)) {
			RechargeOrder rechargeOrder = rechargeOrderRepo.getOne(id);
			checkOrder(rechargeOrder.getOrderNo(), actualPayAmount, rechargeOrder.getDepositTime());
		}
	}

	@Transactional(readOnly = true)
	public PageResult<RechargeOrderVO> findLowerLevelRechargeOrderByPage(LowerLevelRechargeOrderQueryCondParam param) {
		UserAccount currentAccount = userAccountRepo.getOne(param.getCurrentAccountId());
		UserAccount lowerLevelAccount = currentAccount;
		if (StrUtil.isNotBlank(param.getUserName())) {
			lowerLevelAccount = userAccountRepo.findByUserNameAndDeletedFlagIsFalse(param.getUserName());
			if (lowerLevelAccount == null) {
				throw new BizException(BizError.??????????????????);
			}
			// ??????????????????????????????????????????????????????????????????
			if (!lowerLevelAccount.getAccountLevelPath().startsWith(currentAccount.getAccountLevelPath())) {
				throw new BizException(BizError.???????????????????????????????????????????????????????????????);
			}
		}
		String lowerLevelAccountLevelPath = lowerLevelAccount.getAccountLevelPath();

		Specification<RechargeOrder> spec = new Specification<RechargeOrder>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<RechargeOrder> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.like(root.join("userAccount", JoinType.INNER).get("accountLevelPath"),
						lowerLevelAccountLevelPath + "%"));
				if (StrUtil.isNotEmpty(param.getAccountType())) {
					predicates.add(builder.equal(root.join("userAccount", JoinType.INNER).get("accountType"),
							param.getAccountType()));
				}
				if (param.getSubmitStartTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("submitTime").as(Date.class),
							DateUtil.beginOfDay(param.getSubmitStartTime())));
				}
				if (param.getSubmitEndTime() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("submitTime").as(Date.class),
							DateUtil.endOfDay(param.getSubmitEndTime())));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<RechargeOrder> result = rechargeOrderRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("submitTime"))));
		PageResult<RechargeOrderVO> pageResult = new PageResult<>(RechargeOrderVO.convertFor(result.getContent()),
				param.getPageNum(), param.getPageSize(), result.getTotalElements());
		return pageResult;
	}

}
