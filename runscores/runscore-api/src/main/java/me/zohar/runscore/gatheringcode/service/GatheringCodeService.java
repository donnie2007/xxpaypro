package me.zohar.runscore.gatheringcode.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import cn.hutool.core.util.StrUtil;
import me.zohar.runscore.common.exception.BizError;
import me.zohar.runscore.common.exception.BizException;
import me.zohar.runscore.common.valid.ParamValid;
import me.zohar.runscore.common.vo.PageResult;
import me.zohar.runscore.constants.Constant;
import me.zohar.runscore.gatheringcode.domain.GatheringCode;
import me.zohar.runscore.gatheringcode.domain.GatheringCodeUsage;
import me.zohar.runscore.gatheringcode.param.GatheringCodeParam;
import me.zohar.runscore.gatheringcode.param.GatheringCodeQueryCondParam;
import me.zohar.runscore.gatheringcode.repo.GatheringCodeRepo;
import me.zohar.runscore.gatheringcode.repo.GatheringCodeUsageRepo;
import me.zohar.runscore.gatheringcode.vo.GatheringCodeUsageVO;
import me.zohar.runscore.gatheringcode.vo.GatheringCodeVO;
import me.zohar.runscore.mastercontrol.domain.ReceiveOrderSetting;
import me.zohar.runscore.mastercontrol.repo.ReceiveOrderSettingRepo;
import me.zohar.runscore.storage.domain.Storage;
import me.zohar.runscore.storage.repo.StorageRepo;
import me.zohar.runscore.useraccount.domain.AccountReceiveOrderChannel;
import me.zohar.runscore.useraccount.domain.UserAccount;
import me.zohar.runscore.useraccount.repo.AccountReceiveOrderChannelRepo;
import me.zohar.runscore.useraccount.repo.UserAccountRepo;

@Validated
@Service
public class GatheringCodeService {

	@Autowired
	private GatheringCodeRepo gatheringCodeRepo;

	@Autowired
	private GatheringCodeUsageRepo gatheringCodeUsageRepo;

	@Autowired
	private StorageRepo storageRepo;

	@Autowired
	private UserAccountRepo userAccountRepo;

	@Autowired
	private AccountReceiveOrderChannelRepo accountReceiveOrderChannelRepo;

	@Autowired
	private ReceiveOrderSettingRepo receiveOrderSettingRepo;

	@Transactional
	public void switchGatheringCode(List<String> gatheringCodeIds, String userAccountId) {
		Map<String, Boolean> channelMap = new HashMap<>();
		List<GatheringCode> gatheringCodes = gatheringCodeRepo.findByUserAccountId(userAccountId);
		for (GatheringCode gatheringCode : gatheringCodes) {
			boolean inUse = false;
			for (String gatheringCodeId : gatheringCodeIds) {
				if (gatheringCode.getId().equals(gatheringCodeId)) {
					inUse = true;
					break;
				}
			}
			gatheringCode.setInUse(inUse);
			gatheringCodeRepo.save(gatheringCode);
			if (channelMap.get(gatheringCode.getGatheringChannelId()) == null) {
				channelMap.put(gatheringCode.getGatheringChannelId(), inUse);
			} else {
				channelMap.put(gatheringCode.getGatheringChannelId(),
						channelMap.get(gatheringCode.getGatheringChannelId()) || inUse);
			}

		}
		List<AccountReceiveOrderChannel> accountReceiveOrderChannels = accountReceiveOrderChannelRepo
				.findByUserAccountIdAndChannelDeletedFlagFalse(userAccountId);
		for (AccountReceiveOrderChannel accountReceiveOrderChannel : accountReceiveOrderChannels) {
			if (Constant.????????????????????????_?????????.equals(accountReceiveOrderChannel.getState())) {
				continue;
			}
			boolean inUse = channelMap.get(accountReceiveOrderChannel.getChannelId()) != null
					&& channelMap.get(accountReceiveOrderChannel.getChannelId());
			accountReceiveOrderChannel.setState(inUse ? Constant.????????????????????????_????????? : Constant.????????????????????????_?????????);
			accountReceiveOrderChannelRepo.save(accountReceiveOrderChannel);
		}
	}

	@Transactional
	public void delMyGatheringCodeById(String id, String userAccountId) {
		GatheringCode gatheringCode = gatheringCodeRepo.getOne(id);
		if (!userAccountId.equals(gatheringCode.getUserAccountId())) {
			throw new BizException(BizError.??????????????????);
		}

		ReceiveOrderSetting setting = receiveOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting.getAuditGatheringCode()) {
			gatheringCode.initiateAudit(Constant.?????????????????????_??????);
		} else {
			delGatheringCodeById(id);
		}
	}

	@Transactional
	public void delGatheringCodeById(String id) {
		GatheringCode gatheringCode = gatheringCodeRepo.getOne(id);
		disassociationGatheringCodeStorage(gatheringCode.getStorageId());
		gatheringCodeRepo.delete(gatheringCode);
	}

	@Transactional(readOnly = true)
	public GatheringCodeVO findMyGatheringCodeById(String id, String userAccountId) {
		GatheringCodeVO vo = findGatheringCodeById(id);
		if (!userAccountId.equals(vo.getUserAccountId())) {
			throw new BizException(BizError.??????????????????);
		}
		return vo;
	}

	@Transactional(readOnly = true)
	public GatheringCodeVO findGatheringCodeById(String id) {
		GatheringCode gatheringCode = gatheringCodeRepo.getOne(id);
		return GatheringCodeVO.convertFor(gatheringCode);
	}

	@Transactional(readOnly = true)
	public GatheringCodeUsageVO findGatheringCodeUsageById(String id) {
		GatheringCodeUsage gatheringCodeUsage = gatheringCodeUsageRepo.getOne(id);
		return GatheringCodeUsageVO.convertFor(gatheringCodeUsage);
	}

	@Transactional(readOnly = true)
	public List<GatheringCodeUsageVO> findAllGatheringCode(String userAccountId) {
		return GatheringCodeUsageVO.convertFor(gatheringCodeUsageRepo.findByUserAccountId(userAccountId));
	}

	@Transactional(readOnly = true)
	public PageResult<GatheringCodeUsageVO> findMyGatheringCodeUsageByPage(GatheringCodeQueryCondParam param) {
		if (StrUtil.isBlank(param.getUserAccountId())) {
			throw new BizException(BizError.??????????????????);
		}
		return findGatheringCodeUsageByPage(param);
	}

	@Transactional(readOnly = true)
	public PageResult<GatheringCodeUsageVO> findGatheringCodeUsageByPage(GatheringCodeQueryCondParam param) {
		Specification<GatheringCodeUsage> spec = new Specification<GatheringCodeUsage>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<GatheringCodeUsage> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotEmpty(param.getState())) {
					predicates.add(builder.equal(root.get("state"), param.getState()));
				}
				if (StrUtil.isNotEmpty(param.getGatheringChannelId())) {
					predicates.add(builder.equal(root.get("gatheringChannelId"), param.getGatheringChannelId()));
				}
				if (StrUtil.isNotEmpty(param.getPayee())) {
					predicates.add(builder.equal(root.get("payee"), param.getPayee()));
				}
				if (StrUtil.isNotEmpty(param.getUserName())) {
					predicates.add(builder.equal(root.join("userAccount", JoinType.INNER).get("userName"),
							param.getUserName()));
				}
				if (StrUtil.isNotEmpty(param.getUserAccountId())) {
					predicates.add(builder.equal(root.get("userAccountId"), param.getUserAccountId()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<GatheringCodeUsage> result = gatheringCodeUsageRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("createTime"))));
		PageResult<GatheringCodeUsageVO> pageResult = new PageResult<>(
				GatheringCodeUsageVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
		return pageResult;
	}

	@Transactional(readOnly = true)
	public PageResult<GatheringCodeUsageVO> findTop5TodoAuditGatheringCodeByPage() {
		Specification<GatheringCodeUsage> spec = new Specification<GatheringCodeUsage>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<GatheringCodeUsage> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("state"), Constant.???????????????_?????????));
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<GatheringCodeUsage> result = gatheringCodeUsageRepo.findAll(spec,
				PageRequest.of(0, 5, Sort.by(Sort.Order.desc("initiateAuditTime"))));
		PageResult<GatheringCodeUsageVO> pageResult = new PageResult<>(
				GatheringCodeUsageVO.convertFor(result.getContent()), 1, 5, result.getTotalElements());
		return pageResult;
	}

	@Transactional
	public void associateGatheringCodeStorage(String storageId, String gatheringCodeId) {
		Storage storage = storageRepo.getOne(storageId);
		storage.setAssociateId(gatheringCodeId);
		storage.setAssociateBiz("gatheringCode");
		storageRepo.save(storage);
	}

	@Transactional
	public void disassociationGatheringCodeStorage(String storageId) {
		Storage oldStorage = storageRepo.getOne(storageId);
		oldStorage.setAssociateId(null);
		oldStorage.setAssociateBiz(null);
		storageRepo.save(oldStorage);
	}

	@Transactional
	public void updateToNormalState(String id) {
		GatheringCode gatheringCode = gatheringCodeRepo.getOne(id);
		gatheringCode.setAuditType(null);
		gatheringCode.setInitiateAuditTime(null);
		gatheringCode.setState(Constant.???????????????_??????);
		gatheringCodeRepo.save(gatheringCode);
	}

	@Transactional
	public void addOrUpdateGatheringCode(GatheringCodeParam param) {
		String userAccountId = null;
		if (StrUtil.isBlank(param.getId())) {
			if (StrUtil.isBlank(param.getUserName())) {
				throw new BizException(BizError.????????????);
			}
			UserAccount userAccount = userAccountRepo.findByUserNameAndDeletedFlagIsFalse(param.getUserName());
			if (userAccount == null) {
				throw new BizException(BizError.??????????????????????????????????????????);
			}
			userAccountId = userAccount.getId();
			addGatheringCode(param, userAccountId);
		} else {
			GatheringCode gatheringCode = gatheringCodeRepo.getOne(param.getId());
			userAccountId = gatheringCode.getUserAccountId();
			updateGatheringCode(param, gatheringCode.getUserAccountId());
		}
	}

	@ParamValid
	@Transactional
	public void addGatheringCode(GatheringCodeParam param, String userAccountId) {
		ReceiveOrderSetting receiveOrderSetting = receiveOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		param.setFixedGatheringAmount(!receiveOrderSetting.getUnfixedGatheringCodeReceiveOrder());
		if (param.getFixedGatheringAmount()) {
			if (param.getGatheringAmount() == null) {
				throw new BizException(BizError.????????????);
			}
			if (param.getGatheringAmount() <= 0) {
				throw new BizException(BizError.????????????);
			}
		}
		GatheringCode gatheringCode = param.convertToPo(userAccountId);
		ReceiveOrderSetting setting = receiveOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting.getAuditGatheringCode()) {
			gatheringCode.initiateAudit(Constant.?????????????????????_??????);
		}
		gatheringCodeRepo.save(gatheringCode);
		if(StrUtil.isNotBlank(param.getStorageId())){
			associateGatheringCodeStorage(param.getStorageId(), gatheringCode.getId());
		}

	}

	@ParamValid
	@Transactional
	public void updateGatheringCode(GatheringCodeParam param, String userAccountId) {
		ReceiveOrderSetting receiveOrderSetting = receiveOrderSettingRepo.findTopByOrderByLatelyUpdateTime();
		param.setFixedGatheringAmount(!receiveOrderSetting.getUnfixedGatheringCodeReceiveOrder());
		if (param.getFixedGatheringAmount()) {
			if (param.getGatheringAmount() == null) {
				throw new BizException(BizError.????????????);
			}
			if (param.getGatheringAmount() <= 0) {
				throw new BizException(BizError.????????????);
			}
		}
		GatheringCode gatheringCode = gatheringCodeRepo.getOne(param.getId());
		if (!gatheringCode.getUserAccountId().equals(userAccountId)) {
			throw new BizException(BizError.?????????????????????);
		}
		// ?????????????????????????????????
		if (!param.getStorageId().equals(gatheringCode.getStorageId())) {
			disassociationGatheringCodeStorage(gatheringCode.getStorageId());
		}
		BeanUtils.copyProperties(param, gatheringCode);
		gatheringCodeRepo.save(gatheringCode);
		associateGatheringCodeStorage(param.getStorageId(), gatheringCode.getId());
	}

}
