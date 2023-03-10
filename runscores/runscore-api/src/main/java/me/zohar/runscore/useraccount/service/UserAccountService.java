package me.zohar.runscore.useraccount.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import me.zohar.runscore.agent.domain.InviteCode;
import me.zohar.runscore.agent.domain.InviteCodeChannelRebate;
import me.zohar.runscore.agent.repo.InviteCodeRepo;
import me.zohar.runscore.common.exception.BizError;
import me.zohar.runscore.common.exception.BizException;
import me.zohar.runscore.common.valid.ParamValid;
import me.zohar.runscore.common.vo.PageResult;
import me.zohar.runscore.constants.Constant;
import me.zohar.runscore.mastercontrol.domain.RegisterSetting;
import me.zohar.runscore.mastercontrol.repo.RegisterSettingRepo;
import me.zohar.runscore.merchant.domain.GatheringChannel;
import me.zohar.runscore.merchant.repo.GatheringChannelRepo;
import me.zohar.runscore.useraccount.domain.AccountChangeLog;
import me.zohar.runscore.useraccount.domain.AccountReceiveOrderChannel;
import me.zohar.runscore.useraccount.domain.UserAccount;
import me.zohar.runscore.useraccount.param.AccountChangeLogQueryCondParam;
import me.zohar.runscore.useraccount.param.AddUserAccountParam;
import me.zohar.runscore.useraccount.param.AdjustCashDepositParam;
import me.zohar.runscore.useraccount.param.BindBankInfoParam;
import me.zohar.runscore.useraccount.param.LowerLevelAccountChangeLogQueryCondParam;
import me.zohar.runscore.useraccount.param.LowerLevelAccountQueryCondParam;
import me.zohar.runscore.useraccount.param.ModifyLoginPwdParam;
import me.zohar.runscore.useraccount.param.ModifyMoneyPwdParam;
import me.zohar.runscore.useraccount.param.UserAccountEditParam;
import me.zohar.runscore.useraccount.param.UserAccountQueryCondParam;
import me.zohar.runscore.useraccount.param.UserAccountRegisterParam;
import me.zohar.runscore.useraccount.param.VirtualWalletParam;
import me.zohar.runscore.useraccount.repo.AccountChangeLogRepo;
import me.zohar.runscore.useraccount.repo.AccountReceiveOrderChannelRepo;
import me.zohar.runscore.useraccount.repo.UserAccountRepo;
import me.zohar.runscore.useraccount.vo.AccountChangeLogVO;
import me.zohar.runscore.useraccount.vo.BankInfoVO;
import me.zohar.runscore.useraccount.vo.LoginAccountInfoVO;
import me.zohar.runscore.useraccount.vo.LowerLevelAccountDetailsInfoVO;
import me.zohar.runscore.useraccount.vo.UserAccountDetailsInfoVO;
import me.zohar.runscore.useraccount.vo.UserAccountInfoVO;
import me.zohar.runscore.useraccount.vo.VirtualWalletInfoVO;

@Validated
@Service
public class UserAccountService {

	@Autowired
	private UserAccountRepo userAccountRepo;

	@Autowired
	private AccountChangeLogRepo accountChangeLogRepo;

	@Autowired
	private InviteCodeRepo inviteCodeRepo;

	@Autowired
	private RegisterSettingRepo registerSettingRepo;

	@Autowired
	private AccountReceiveOrderChannelRepo accountReceiveOrderChannelRepo;

	@Autowired
	private GatheringChannelRepo gatheringChannelRepo;

	/**
	 * ??????????????????
	 * 
	 * @param userAccountId
	 * @param receiveOrderState
	 */
	@Transactional
	public void updateReceiveOrderState(@NotBlank String userAccountId, @NotBlank String receiveOrderState) {
		UserAccount userAccount = userAccountRepo.getOne(userAccountId);
		userAccount.setReceiveOrderState(receiveOrderState);
		userAccountRepo.save(userAccount);
	}

	/**
	 * ????????????????????????
	 */
	@Transactional
	public void updateLatelyLoginTime(String userAccountId) {
		UserAccount userAccount = userAccountRepo.getOne(userAccountId);
		userAccount.setLatelyLoginTime(new Date());
		userAccountRepo.save(userAccount);
	}

	@ParamValid
	@Transactional
	public void updateUserAccount(UserAccountEditParam param) {
		UserAccount existUserAccount = userAccountRepo.findByUserNameAndDeletedFlagIsFalse(param.getUserName());
		if (existUserAccount != null && !existUserAccount.getId().equals(param.getId())) {
			throw new BizException(BizError.??????????????????);
		}
		UserAccount userAccount = userAccountRepo.getOne(param.getId());
		BeanUtils.copyProperties(param, userAccount);
		userAccountRepo.save(userAccount);
	}

	@Transactional(readOnly = true)
	public UserAccountDetailsInfoVO findUserAccountDetailsInfoById(String userAccountId) {
		UserAccount userAccount = userAccountRepo.getOne(userAccountId);
		return UserAccountDetailsInfoVO.convertFor(userAccount);
	}

	@Transactional(readOnly = true)
	public PageResult<UserAccountDetailsInfoVO> findUserAccountDetailsInfoByPage(UserAccountQueryCondParam param) {
		Specification<UserAccount> spec = new Specification<UserAccount>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<UserAccount> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("deletedFlag"), false));
				if (StrUtil.isNotEmpty(param.getUserName())) {
					predicates.add(builder.like(root.get("userName"), "%" + param.getUserName() + "%"));
				}
				if (StrUtil.isNotEmpty(param.getRealName())) {
					predicates.add(builder.like(root.get("realName"), "%" + param.getRealName() + "%"));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<UserAccount> result = userAccountRepo.findAll(spec, PageRequest.of(param.getPageNum() - 1,
				param.getPageSize(), Sort.by(Direction.fromString(param.getDirection()), param.getPropertie())));
		PageResult<UserAccountDetailsInfoVO> pageResult = new PageResult<>(
				UserAccountDetailsInfoVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
		return pageResult;
	}

	@ParamValid
	@Transactional
	public void bindBankInfo(BindBankInfoParam param) {
		UserAccount userAccount = userAccountRepo.getOne(param.getUserAccountId());
		BeanUtils.copyProperties(param, userAccount);
		userAccount.setBankInfoLatelyModifyTime(new Date());
		userAccountRepo.save(userAccount);
	}

	@ParamValid
	@Transactional
	public void bindVirtualWallet(VirtualWalletParam param) {
		UserAccount userAccount = userAccountRepo.getOne(param.getUserAccountId());
		BeanUtils.copyProperties(param, userAccount);
		userAccount.setVirtualWalletLatelyModifyTime(new Date());
		userAccountRepo.save(userAccount);
	}

	@ParamValid
	@Transactional
	public void bindVirtualWallet(BindBankInfoParam param) {
		UserAccount userAccount = userAccountRepo.getOne(param.getUserAccountId());
		BeanUtils.copyProperties(param, userAccount);
		userAccount.setBankInfoLatelyModifyTime(new Date());
		userAccountRepo.save(userAccount);
	}

	@ParamValid
	@Transactional
	public void modifyLoginPwd(ModifyLoginPwdParam param) {
		UserAccount userAccount = userAccountRepo.getOne(param.getUserAccountId());
		BCryptPasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
		if (!pwdEncoder.matches(param.getOldLoginPwd(), userAccount.getLoginPwd())) {
			throw new BizException(BizError.???????????????????????????);
		}
		modifyLoginPwd(param.getUserAccountId(), param.getNewLoginPwd());
	}

	@Transactional
	public void modifyLoginPwd(@NotBlank String userAccountId, @NotBlank String newLoginPwd) {
		UserAccount userAccount = userAccountRepo.getOne(userAccountId);
		userAccount.setLoginPwd(new BCryptPasswordEncoder().encode(newLoginPwd));
		userAccountRepo.save(userAccount);
	}

	@ParamValid
	@Transactional
	public void modifyMoneyPwd(ModifyMoneyPwdParam param) {
		UserAccount userAccount = userAccountRepo.getOne(param.getUserAccountId());
		BCryptPasswordEncoder pwdEncoder = new BCryptPasswordEncoder();
		if (!pwdEncoder.matches(param.getOldMoneyPwd(), userAccount.getMoneyPwd())) {
			throw new BizException(BizError.???????????????????????????);
		}
		String newMoneyPwd = pwdEncoder.encode(param.getNewMoneyPwd());
		userAccount.setMoneyPwd(newMoneyPwd);
		userAccountRepo.save(userAccount);
	}

	@ParamValid
	@Transactional
	public void modifyMoneyPwd(@NotBlank String userAccountId, @NotBlank String newMoneyPwd) {
		UserAccount userAccount = userAccountRepo.getOne(userAccountId);
		userAccount.setMoneyPwd(new BCryptPasswordEncoder().encode(newMoneyPwd));
		userAccountRepo.save(userAccount);
	}

	@Transactional(readOnly = true)
	public LoginAccountInfoVO getLoginAccountInfo(String userName) {
		return LoginAccountInfoVO.convertFor(userAccountRepo.findByUserNameAndDeletedFlagIsFalse(userName));
	}

	@Transactional(readOnly = true)
	public UserAccountInfoVO getUserAccountInfo(String userAccountId) {
		return UserAccountInfoVO.convertFor(userAccountRepo.getOne(userAccountId));
	}

	@Transactional(readOnly = true)
	public BankInfoVO getBankInfo(String userAccountId) {
		return BankInfoVO.convertFor(userAccountRepo.getOne(userAccountId));
	}

	@Transactional(readOnly = true)
	public VirtualWalletInfoVO getVirtualWalletInfo(String userAccountId) {
		return VirtualWalletInfoVO.convertFor(userAccountRepo.getOne(userAccountId));
	}

	@ParamValid
	@Transactional
	public void addUserAccount(AddUserAccountParam param) {
		UserAccount userAccount = userAccountRepo.findByUserNameAndDeletedFlagIsFalse(param.getUserName());
		if (userAccount != null) {
			throw new BizException(BizError.??????????????????);
		}
		String encodePwd = new BCryptPasswordEncoder().encode(param.getLoginPwd());
		param.setLoginPwd(encodePwd);
		UserAccount newUserAccount = param.convertToPo();
		if (StrUtil.isNotBlank(param.getInviterUserName())) {
			UserAccount inviter = userAccountRepo.findByUserNameAndDeletedFlagIsFalse(param.getInviterUserName());
			if (inviter == null) {
				throw new BizException(BizError.??????????????????);
			}
			// ???????????????????????????????????????????????????
			// if (param.getRebate() > inviter.getRebate()) {
			// throw new BizException(BizError.?????????????????????????????????????????????);
			// }
			newUserAccount.setInviterId(inviter.getId());
			newUserAccount.setAccountLevel(inviter.getAccountLevel() + 1);
			newUserAccount.setAccountLevelPath(inviter.getAccountLevelPath() + "." + newUserAccount.getId());
		}
		userAccountRepo.save(newUserAccount);
	}

	/**
	 * ????????????
	 * 
	 * @param param
	 */
	@ParamValid
	@Transactional
	public void register(UserAccountRegisterParam param) {
		RegisterSetting setting = registerSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (!setting.getRegisterEnabled()) {
			throw new BizException(BizError.?????????????????????);
		}
		UserAccount existAccount = userAccountRepo.findByUserNameAndDeletedFlagIsFalse(param.getUserName());
		if (existAccount != null) {
			throw new BizException(BizError.??????????????????);
		}
		param.setLoginPwd(new BCryptPasswordEncoder().encode(param.getLoginPwd()));
		param.setMoneyPwd(new BCryptPasswordEncoder().encode(param.getMoneyPwd()));
		UserAccount newUserAccount = param.convertToPo();
		if (setting.getInviteRegisterEnabled()) {
			InviteCode inviteCode = inviteCodeRepo
					.findTopByUsedFalseAndCodeAndPeriodOfValidityGreaterThanEqual(param.getInviteCode(), new Date());
			if (inviteCode == null) {
				throw new BizException(BizError.??????????????????????????????);
			}
			newUserAccount.updateInviteInfo(inviteCode);
			userAccountRepo.save(newUserAccount);
			Set<InviteCodeChannelRebate> inviteCodeChannelRebates = inviteCode.getInviteCodeChannelRebates();
			for (InviteCodeChannelRebate inviteCodeChannelRebate : inviteCodeChannelRebates) {
				AccountReceiveOrderChannel accountReceiveOrderChannel = AccountReceiveOrderChannel
						.build(inviteCodeChannelRebate, newUserAccount.getId());
				accountReceiveOrderChannelRepo.save(accountReceiveOrderChannel);
			}
			inviteCode.setUsed(true);
			inviteCodeRepo.save(inviteCode);
		} else {
			userAccountRepo.save(newUserAccount);
			List<GatheringChannel> gatheringChannels = gatheringChannelRepo.findByDeletedFlagIsFalse();
			for (GatheringChannel gatheringChannel : gatheringChannels) {
				AccountReceiveOrderChannel accountReceiveOrderChannel = AccountReceiveOrderChannel
						.build(gatheringChannel, newUserAccount.getId());
				accountReceiveOrderChannelRepo.save(accountReceiveOrderChannel);
			}
		}
	}

	@Transactional(readOnly = true)
	public PageResult<AccountChangeLogVO> findAccountChangeLogByPage(AccountChangeLogQueryCondParam param) {
		Specification<AccountChangeLog> spec = new Specification<AccountChangeLog>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<AccountChangeLog> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (param.getStartTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("accountChangeTime").as(Date.class),
							DateUtil.beginOfDay(param.getStartTime())));
				}
				if (param.getEndTime() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("accountChangeTime").as(Date.class),
							DateUtil.endOfDay(param.getEndTime())));
				}
				if (StrUtil.isNotEmpty(param.getAccountChangeTypeCode())) {
					predicates.add(builder.equal(root.get("accountChangeTypeCode"), param.getAccountChangeTypeCode()));
				}
				if (StrUtil.isNotEmpty(param.getUserAccountId())) {
					predicates.add(builder.equal(root.get("userAccountId"), param.getUserAccountId()));
				}
				if (StrUtil.isNotEmpty(param.getUserName())) {
					predicates.add(builder.equal(root.join("userAccount", JoinType.INNER).get("userName"),
							param.getUserName()));
				}

				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<AccountChangeLog> result = accountChangeLogRepo.findAll(spec, PageRequest.of(param.getPageNum() - 1,
				param.getPageSize(), Sort.by(Sort.Order.desc("accountChangeTime"), Sort.Order.desc("id"))));
		PageResult<AccountChangeLogVO> pageResult = new PageResult<>(AccountChangeLogVO.convertFor(result.getContent()),
				param.getPageNum(), param.getPageSize(), result.getTotalElements());
		return pageResult;
	}

	@Transactional
	public void delUserAccount(@NotBlank String userAccountId) {
		UserAccount userAccount = userAccountRepo.getOne(userAccountId);
		userAccount.setDeletedFlag(true);
		userAccountRepo.save(userAccount);
	}

	@ParamValid
	@Transactional
	public void adjustCashDeposit(AdjustCashDepositParam param) {
		UserAccount userAccount = userAccountRepo.getOne(param.getUserAccountId());
		if (Constant.??????????????????_??????????????????.equals(param.getAccountChangeTypeCode())) {
			Double cashDeposit = NumberUtil.round(userAccount.getCashDeposit() + param.getAccountChangeAmount(), 4)
					.doubleValue();
			userAccount.setCashDeposit(cashDeposit);
			userAccountRepo.save(userAccount);
			accountChangeLogRepo.save(
					AccountChangeLog.buildWithHandworkAdjustCashDeposit(userAccount, param.getAccountChangeAmount()));
		} else if (Constant.??????????????????_??????????????????.equals(param.getAccountChangeTypeCode())) {
			Double cashDeposit = NumberUtil.round(userAccount.getCashDeposit() - param.getAccountChangeAmount(), 4)
					.doubleValue();
			if (cashDeposit < 0) {
				throw new BizException(BizError.???????????????????????????????????????);
			}
			userAccount.setCashDeposit(cashDeposit);
			userAccountRepo.save(userAccount);
			accountChangeLogRepo.save(
					AccountChangeLog.buildWithHandworkAdjustCashDeposit(userAccount, -param.getAccountChangeAmount()));
		}
	}

	@ParamValid
	@Transactional(readOnly = true)
	public PageResult<LowerLevelAccountDetailsInfoVO> findLowerLevelAccountDetailsInfoByPage(
			LowerLevelAccountQueryCondParam param) {
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
		String lowerLevelAccountId = lowerLevelAccount.getId();
		String lowerLevelAccountLevelPath = lowerLevelAccount.getAccountLevelPath();

		Specification<UserAccount> spec = new Specification<UserAccount>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<UserAccount> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				predicates.add(builder.equal(root.get("deletedFlag"), false));
				if (Constant.????????????????????????_???????????????????????????.equals(param.getQueryScope())) {
					Predicate predicate1 = builder.equal(root.get("id"), lowerLevelAccountId);
					Predicate predicate2 = builder.equal(root.get("inviterId"), lowerLevelAccountId);
					predicates.add(builder.or(predicate1, predicate2));
				} else {
					predicates.add(builder.like(root.get("accountLevelPath"), lowerLevelAccountLevelPath + "%"));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<UserAccount> result = userAccountRepo.findAll(spec,
				PageRequest.of(param.getPageNum() - 1, param.getPageSize(), Sort.by(Sort.Order.asc("registeredTime"))));
		PageResult<LowerLevelAccountDetailsInfoVO> pageResult = new PageResult<>(
				LowerLevelAccountDetailsInfoVO.convertFor(result.getContent()), param.getPageNum(), param.getPageSize(),
				result.getTotalElements());
		return pageResult;
	}

	@Transactional(readOnly = true)
	public PageResult<AccountChangeLogVO> findLowerLevelAccountChangeLogByPage(
			LowerLevelAccountChangeLogQueryCondParam param) {
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
		String lowerLevelAccountId = lowerLevelAccount.getId();
		String lowerLevelAccountLevelPath = lowerLevelAccount.getAccountLevelPath();

		Specification<AccountChangeLog> spec = new Specification<AccountChangeLog>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Predicate toPredicate(Root<AccountChangeLog> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
				List<Predicate> predicates = new ArrayList<Predicate>();
				if (StrUtil.isNotBlank(param.getUserName())) {
					predicates.add(builder.equal(root.get("userAccountId"), lowerLevelAccountId));
				} else {
					predicates.add(builder.like(root.join("userAccount", JoinType.INNER).get("accountLevelPath"),
							lowerLevelAccountLevelPath + "%"));
				}
				if (param.getStartTime() != null) {
					predicates.add(builder.greaterThanOrEqualTo(root.get("accountChangeTime").as(Date.class),
							DateUtil.beginOfDay(param.getStartTime())));
				}
				if (param.getEndTime() != null) {
					predicates.add(builder.lessThanOrEqualTo(root.get("accountChangeTime").as(Date.class),
							DateUtil.endOfDay(param.getEndTime())));
				}
				if (StrUtil.isNotBlank(param.getAccountChangeTypeCode())) {
					predicates.add(builder.equal(root.get("accountChangeTypeCode"), param.getAccountChangeTypeCode()));
				}
				return predicates.size() > 0 ? builder.and(predicates.toArray(new Predicate[predicates.size()])) : null;
			}
		};
		Page<AccountChangeLog> result = accountChangeLogRepo.findAll(spec, PageRequest.of(param.getPageNum() - 1,
				param.getPageSize(), Sort.by(Sort.Order.desc("accountChangeTime"))));
		PageResult<AccountChangeLogVO> pageResult = new PageResult<>(AccountChangeLogVO.convertFor(result.getContent()),
				param.getPageNum(), param.getPageSize(), result.getTotalElements());
		return pageResult;
	}

}
