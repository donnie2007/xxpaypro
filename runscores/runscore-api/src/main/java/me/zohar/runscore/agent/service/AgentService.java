package me.zohar.runscore.agent.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import cn.hutool.core.util.IdUtil;
import me.zohar.runscore.agent.domain.InviteCode;
import me.zohar.runscore.agent.domain.InviteCodeChannelRebate;
import me.zohar.runscore.agent.param.ChannelRebateParam;
import me.zohar.runscore.agent.param.GenerateInviteCodeParam;
import me.zohar.runscore.agent.repo.InviteCodeChannelRebateRepo;
import me.zohar.runscore.agent.repo.InviteCodeRepo;
import me.zohar.runscore.agent.vo.InviteCodeDetailsInfoVO;
import me.zohar.runscore.common.exception.BizError;
import me.zohar.runscore.common.exception.BizException;
import me.zohar.runscore.common.valid.ParamValid;
import me.zohar.runscore.constants.Constant;
import me.zohar.runscore.mastercontrol.domain.RegisterSetting;
import me.zohar.runscore.mastercontrol.repo.RegisterSettingRepo;
import me.zohar.runscore.useraccount.domain.AccountReceiveOrderChannel;
import me.zohar.runscore.useraccount.domain.UserAccount;
import me.zohar.runscore.useraccount.repo.AccountReceiveOrderChannelRepo;
import me.zohar.runscore.useraccount.repo.UserAccountRepo;

@Validated
@Service
public class AgentService {

	@Autowired
	private UserAccountRepo userAccountRepo;

	@Autowired
	private InviteCodeRepo inviteCodeRepo;

	@Autowired
	private RegisterSettingRepo inviteRegisterSettingRepo;

	@Autowired
	private AccountReceiveOrderChannelRepo accountReceiveOrderChannelRepo;

	@Autowired
	private InviteCodeChannelRebateRepo inviteCodeChannelRebateRepo;

	/**
	 * 生成邀请码
	 * 
	 * @param param
	 * @return
	 */
	@ParamValid
	@Transactional
	public String generateInviteCode(GenerateInviteCodeParam param) {
		RegisterSetting setting = inviteRegisterSettingRepo.findTopByOrderByLatelyUpdateTime();
		if (setting == null || !setting.getInviteRegisterEnabled()) {
			throw new BizException(BizError.邀请注册功能已关闭);
		}
		UserAccount inviter = userAccountRepo.getOne(param.getInviterId());
		if (!(Constant.账号类型_管理员.equals(inviter.getAccountType())
				|| Constant.账号类型_代理.equals(inviter.getAccountType()))) {
			throw new BizException(BizError.只有管理员或代理才能进行下级开户操作);
		}
		if (!(Constant.账号类型_代理.equals(param.getAccountType()) || Constant.账号类型_会员.equals(param.getAccountType()))) {
			throw new BizException(BizError.开户类型只能是代理或会员);
		}
		if (setting.getOnlyOpenMemberAccount()) {
			if (!Constant.账号类型_会员.equals(param.getAccountType())) {
				throw new BizException(BizError.开户类型只能是会员);
			}
		}

		Map<String, String> channelMap = new HashMap<>();
		for (ChannelRebateParam channelRebateParam : param.getChannelRebates()) {
			if (channelMap.get(channelRebateParam.getChannelId()) != null) {
				throw new BizException(BizError.不能设置重复的通道);
			}
			channelMap.put(channelRebateParam.getChannelId(), channelRebateParam.getChannelId());
			AccountReceiveOrderChannel receiveOrderChannel = accountReceiveOrderChannelRepo
					.findByUserAccountIdAndChannelId(inviter.getId(), channelRebateParam.getChannelId());
			if (receiveOrderChannel == null) {
				throw new BizException(BizError.未知通道不能进行下级开户操作);
			}
			if (channelRebateParam.getRebate() > receiveOrderChannel.getRebate()) {
				throw new BizException(BizError.下级账号的返点不能大于上级账号);
			}
		}

		String code = IdUtil.fastSimpleUUID().substring(0, 6);
		while (inviteCodeRepo.findTopByUsedFalseAndCodeAndPeriodOfValidityGreaterThanEqual(code, new Date()) != null) {
			code = IdUtil.fastSimpleUUID().substring(0, 6);
		}
		InviteCode newInviteCode = param.convertToPo(code, setting.getInviteCodeEffectiveDuration());
		inviteCodeRepo.save(newInviteCode);
		for (ChannelRebateParam channelRebateParam : param.getChannelRebates()) {
			InviteCodeChannelRebate channelRebate = channelRebateParam.convertToPo(newInviteCode.getId());
			inviteCodeChannelRebateRepo.save(channelRebate);
		}
		return newInviteCode.getId();
	}

	@Transactional(readOnly = true)
	public InviteCodeDetailsInfoVO getInviteCodeDetailsInfoById(@NotBlank String id) {
		InviteCode inviteCode = inviteCodeRepo.getOne(id);
		InviteCodeDetailsInfoVO inviteDetailsInfo = InviteCodeDetailsInfoVO.convertFor(inviteCode);
		return inviteDetailsInfo;
	}

}
