package me.zohar.runscore.merchant.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import me.zohar.runscore.common.exception.BizError;
import me.zohar.runscore.common.exception.BizException;
import me.zohar.runscore.common.valid.ParamValid;
import me.zohar.runscore.common.vo.PageResult;
import me.zohar.runscore.constants.Constant;
import me.zohar.runscore.merchant.domain.Merchant;
import me.zohar.runscore.merchant.domain.MerchantBankCard;
import me.zohar.runscore.merchant.domain.MerchantSettlementRecord;
import me.zohar.runscore.merchant.param.AddMerchantParam;
import me.zohar.runscore.merchant.param.AddOrUpdateMerchantBankCardParam;
import me.zohar.runscore.merchant.param.ApplySettlementParam;
import me.zohar.runscore.merchant.param.MerchantEditParam;
import me.zohar.runscore.merchant.param.MerchantQueryCondParam;
import me.zohar.runscore.merchant.param.MerchantSettlementRecordQueryCondParam;
import me.zohar.runscore.merchant.param.ModifyLoginPwdParam;
import me.zohar.runscore.merchant.param.ModifyMoneyPwdParam;
import me.zohar.runscore.merchant.repo.MerchantBankCardRepo;
import me.zohar.runscore.merchant.repo.MerchantRepo;
import me.zohar.runscore.merchant.repo.MerchantSettlementRecordRepo;
import me.zohar.runscore.merchant.vo.LoginMerchantInfoVO;
import me.zohar.runscore.merchant.vo.MerchantBankCardVO;
import me.zohar.runscore.merchant.vo.MerchantSettlementRecordVO;
import me.zohar.runscore.merchant.vo.MerchantVO;

@Validated
@Service
public class MerchantService {

	@Autowired
	private MerchantRepo merchantRepo;

	@Autowired
	private MerchantBankCardRepo merchantBankCardRepo;

	@Autowired
	private MerchantSettlementRecordRepo merchantSettlementRecordRepo;

	/**
	 * ????????????
	 * 
	 * @param id
	 */
	@ParamValid
	@Transactional
	public void settlementConfirmCredited(@NotBlank String id) {
		MerchantSettlementRecord record = merchantSettlementRecordRepo.getOne(id);
		if (!(Constant.??????????????????_????????????.equals(record.getState()))) {
			throw new BizException(BizError.??????????????????????????????????????????????????????????????????);
		}

		record.confirmCredited();
		merchantSettlementRecordRepo.save(record);
	}

	/**
	 * ????????????
	 * 
	 * @param id
	 */
	@ParamValid
	@Transactional
	public void settlementApproved(@NotBlank String id, String note) {
		MerchantSettlementRecord record = merchantSettlementRecordRepo.getOne(id);
		if (!Constant.??????????????????_?????????.equals(record.getState())) {
			throw new BizException(BizError.?????????????????????????????????????????????????????????);
		}

		record.approved(note);
		merchantSettlementRecordRepo.save(record);
	}

	public void settlementNotApproved(@NotBlank String id, String note) {
		MerchantSettlementRecord record = merchantSettlementRecordRepo.getOne(id);
		if (!(Constant.??????????????????_?????????.equals(record.getState()) || Constant.??????????????????_????????????.equals(record.getState()))) {
			throw new BizException(BizError.?????????????????????????????????????????????????????????????????????????????????);
		}
		record.notApproved(note);
		merchantSettlementRecordRepo.save(record);

		Merchant merchant = record.getMerchant();
		double withdrawableAmount = NumberUtil.round(merchant.getWithdrawableAmount() + record.getWithdrawAmount(), 4)
				.doubleValue();
		merchant.setWithdrawableAmount(withdrawableAmount);
		merchantRepo.save(merchant);

	}

	@Transactional(readOnly = true)
	public MerchantSettlementRecordVO findByMerchantSettlementRecordId(@NotBlank String id) {
		return MerchantSettlementRecordVO.convertFor(merchantSettlementRecordRepo.getOne(id));
	}

	@Transactional(readOnly = true)
	public PageResult<MerchantSettlementRecordVO> findTop5TodoSettlementByPage() {
		Specification<MerchantSettlementRecord> spec = new Specification<MerchantSettlementRecord>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<MerchantSettlementRecord> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("state"), Constant.??????????????????_?????????));
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<MerchantSettlementRecord> result = merchantSettlementRecordRepo.findAll(spec,
				PageRequest.of(0, 5, Sort.by(Sort.Order.desc("applyTime"))));
		PageResult<MerchantSettlementRecordVO> pageResult = new PageResult<>(
				MerchantSettlementRecordVO.convertFor(result.getContent()), 1, 5, result.getTotalElements());
		return pageResult;
	}

	@Transactional(readOnly = true)
	public PageResult<MerchantSettlementRecordVO> findMerchantSettlementRecordByPage(
			MerchantSettlementRecordQueryCondParam param) {
		Specification<MerchantSettlementRecord> spec = new Specification<MerchantSettlementRecord>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<MerchantSettlementRecord> root, CriteriaQuery<?> query,
					CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotBlank(param.getOrderNo())) {
					predicates.add(builder.equal(root.get("orderNo"), param.getOrderNo()));
				}
				if (StrUtil.isNotBlank(param.getMerchantId())) {
					predicates.add(builder.equal(root.get("merchantId"), param.getMerchantId()));
				}
				if (StrUtil.isNotBlank(param.getState())) {
					predicates.add(builder.equal(root.get("state"), param.getState()));
				}
				if (param.getApplyStartTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("applyTime").as(Date.class),
							DateUtil.beginOfDay(param.getApplyStartTime())));
				}
				if (param.getApplyEndTime() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("applyTime").as(Date.class),
							DateUtil.endOfDay(param.getApplyEndTime())));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<MerchantSettlementRecord> result = merchantSettlementRecordRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("applyTime"))));
		PageResult<MerchantSettlementRecordVO> pageResult = new PageResult<>(
				MerchantSettlementRecordVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
		return pageResult;
	}

	@ParamValid
	@Transactional
	public void applySettlement(ApplySettlementParam param) {
		Merchant merchant = merchantRepo.getOne(param.getMerchantId());
		BCryptPasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
		if (!pwdEncoder.matches(param.getMoneyPwd(), merchant.getMoneyPwd())) {
			throw new BizException(BizError.?????????????????????);
		}
		MerchantBankCard merchantBankCard = merchantBankCardRepo.getOne(param.getMerchantBankCardId());
		if (!merchant.getId().equals(merchantBankCard.getMerchantId())) {
			throw new BizException(BizError.?????????????????????);
		}
		double withdrawableAmount = merchant.getWithdrawableAmount() - param.getWithdrawAmount();
		if (withdrawableAmount < 0) {
			throw new BizException(BizError.?????????????????????);
		}
		merchant.setWithdrawableAmount(NumberUtil.round(withdrawableAmount, 4).doubleValue());
		merchantRepo.save(merchant);
		MerchantSettlementRecord merchantSettlementRecord = param.convertToPo();
		merchantSettlementRecordRepo.save(merchantSettlementRecord);
	}

	@Transactional(readOnly = true)
	public MerchantBankCardVO findMerchantBankCardById(@NotBlank String id) {
		return MerchantBankCardVO.convertFor(merchantBankCardRepo.getOne(id));
	}

	public void deleteMerchantBankCard(String merchantBankCardId, String merchantId) {
		MerchantBankCard merchantBankCard = merchantBankCardRepo.getOne(merchantBankCardId);
		if (!merchantBankCard.getMerchantId().equals(merchantId)) {
			throw new BizException(BizError.????????????);
		}
		merchantBankCard.delete();
		merchantBankCardRepo.save(merchantBankCard);
	}

	@ParamValid
	@Transactional
	public void addOrUpdateMerchantBankCard(AddOrUpdateMerchantBankCardParam param, String merchantId) {
		// ??????
		if (StrUtil.isBlank(param.getId())) {
			MerchantBankCard merchantBankCard = param.convertToPo(merchantId);
			merchantBankCardRepo.save(merchantBankCard);
		}
		// ??????
		else {
			MerchantBankCard merchantBankCard = merchantBankCardRepo.getOne(param.getId());
			if (!merchantBankCard.getMerchantId().equals(merchantId)) {
				throw new BizException(BizError.?????????????????????????????????);
			}
			BeanUtils.copyProperties(param, merchantBankCard);
			merchantBankCardRepo.save(merchantBankCard);
		}
	}

	@Transactional(readOnly = true)
	public List<MerchantBankCardVO> findMerchantBankCardByMerchantId(@NotBlank String merchantId) {
		return MerchantBankCardVO.convertFor(merchantBankCardRepo.findByMerchantIdAndDeletedFlagIsFalse(merchantId));
	}

	@ParamValid
	@Transactional
	public void modifyLoginPwd(ModifyLoginPwdParam param) {
		Merchant merchant = merchantRepo.getOne(param.getMerchantId());
		BCryptPasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
		if (!pwdEncoder.matches(param.getOldLoginPwd(), merchant.getLoginPwd())) {
			throw new BizException(BizError.???????????????????????????);
		}
		modifyLoginPwd(merchant.getId(), param.getNewLoginPwd());
	}

	@ParamValid
	@Transactional
	public void modifyMoneyPwd(ModifyMoneyPwdParam param) {
		Merchant merchant = merchantRepo.getOne(param.getMerchantId());
		BCryptPasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
		if (!pwdEncoder.matches(param.getOldMoneyPwd(), merchant.getMoneyPwd())) {
			throw new BizException(BizError.???????????????????????????);
		}
		modifyMoneyPwd(merchant.getId(), param.getNewMoneyPwd());
	}

	@Transactional(readOnly = true)
	public List<MerchantVO> findAllMerchant() {
		return MerchantVO.convertFor(merchantRepo.findByDeletedFlagIsFalse());
	}

	@Transactional(readOnly = true)
	public MerchantVO getMerchantInfo(String id) {
		return MerchantVO.convertFor(merchantRepo.getOne(id));
	}

	/**
	 * ????????????????????????
	 */
	@Transactional
	public void updateLatelyLoginTime(String id) {
		Merchant merchant = merchantRepo.getOne(id);
		merchant.setLatelyLoginTime(new Date());
		merchantRepo.save(merchant);
	}

	@Transactional(readOnly = true)
	public LoginMerchantInfoVO getLoginMerchantInfo(String userName) {
		return LoginMerchantInfoVO.convertFor(merchantRepo.findByUserNameAndDeletedFlagIsFalse(userName));
	}

	@Transactional
	public void modifyLoginPwd(@NotBlank String id, @NotBlank String newLoginPwd) {
		Merchant merchant = merchantRepo.getOne(id);
		merchant.setLoginPwd(new BCryptPasswordEncoder().encode(newLoginPwd));
		merchantRepo.save(merchant);
	}

	@Transactional
	public void modifyMoneyPwd(@NotBlank String id, @NotBlank String newMoneyPwd) {
		Merchant merchant = merchantRepo.getOne(id);
		merchant.setMoneyPwd(new BCryptPasswordEncoder().encode(newMoneyPwd));
		merchantRepo.save(merchant);
	}

	@Transactional(readOnly = true)
	public MerchantVO findMerchantById(@NotBlank String id) {
		return MerchantVO.convertFor(merchantRepo.getOne(id));
	}

	@Transactional
	public void delMerchantById(@NotBlank String id) {
		Merchant merchant = merchantRepo.getOne(id);
		merchant.setDeletedFlag(true);
		merchantRepo.save(merchant);
	}

	@ParamValid
	@Transactional
	public void addMerchant(AddMerchantParam param) {
		Merchant merchantWithUserName = merchantRepo.findByUserNameAndDeletedFlagIsFalse(param.getUserName());
		if (merchantWithUserName != null) {
			throw new BizException(BizError.??????????????????);
		}
		Merchant merchantWithMerchantNum = merchantRepo.findByMerchantNumAndDeletedFlagIsFalse(param.getMerchantNum());
		if (merchantWithMerchantNum != null) {
			throw new BizException(BizError.??????????????????);
		}
		Merchant merchantWithName = merchantRepo.findByMerchantNameAndDeletedFlagIsFalse(param.getMerchantName());
		if (merchantWithName != null) {
			throw new BizException(BizError.?????????????????????);
		}
		param.setLoginPwd(new BCryptPasswordEncoder().encode(param.getLoginPwd()));
		Merchant merchant = param.convertToPo();
		merchantRepo.save(merchant);
	}

	@ParamValid
	@Transactional
	public void updateMerchant(MerchantEditParam param) {
		Merchant merchantWithUserName = merchantRepo.findByUserNameAndDeletedFlagIsFalse(param.getUserName());
		if (merchantWithUserName != null && !merchantWithUserName.getId().equals(param.getId())) {
			throw new BizException(BizError.??????????????????);
		}
		Merchant merchantWithMerchantNum = merchantRepo.findByMerchantNumAndDeletedFlagIsFalse(param.getMerchantNum());
		if (merchantWithMerchantNum != null && !merchantWithMerchantNum.getId().equals(param.getId())) {
			throw new BizException(BizError.??????????????????);
		}
		Merchant merchantWithName = merchantRepo.findByMerchantNameAndDeletedFlagIsFalse(param.getMerchantName());
		if (merchantWithName != null && !merchantWithName.getId().equals(param.getId())) {
			throw new BizException(BizError.?????????????????????);
		}
		Merchant merchant = merchantRepo.getOne(param.getId());
		BeanUtils.copyProperties(param, merchant);
		merchantRepo.save(merchant);
	}

	@Transactional(readOnly = true)
	public PageResult<MerchantVO> findMerchantByPage(MerchantQueryCondParam param) {
		Specification<Merchant> spec = new Specification<Merchant>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<Merchant> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("deletedFlag"), false));
				if (StrUtil.isNotBlank(param.getMerchantName())) {
					predicates.add(builder.equal(root.get("merchantName"), param.getMerchantName()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<Merchant> result = merchantRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.desc("createTime"))));
		PageResult<MerchantVO> pageResult = new PageResult<>(MerchantVO.convertFor(result.getContent()),
				param.getPageNum(), param.getPageSize(), result.getTotalElements());
		return pageResult;
	}

}
