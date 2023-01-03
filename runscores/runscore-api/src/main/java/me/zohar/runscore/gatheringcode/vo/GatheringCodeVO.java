package me.zohar.runscore.gatheringcode.vo;

import java.io.Serializable;
import java.util.Date;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import me.zohar.runscore.dictconfig.DictHolder;
import me.zohar.runscore.gatheringcode.domain.GatheringCode;

@Data
public class GatheringCodeVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 主键id
	 */
	private String id;

	private String gatheringChannelName;

	/**
	 * 状态,启用:1;禁用:0
	 */
	private String state;

	private String stateName;

	private Boolean fixedGatheringAmount;

	/**
	 * 收款金额
	 */
	private Double gatheringAmount;

	/**
	 * 收款人
	 */
	private String payee;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;

	private String storageId;

	/**
	 * 投注人用户账号id
	 */
	private String userAccountId;

	private String userName;

	private String gatheringChannelId;

	public static GatheringCodeVO convertFor(GatheringCode gatheringCode) {
		if (gatheringCode == null) {
			return null;
		}
		GatheringCodeVO vo = new GatheringCodeVO();
		BeanUtils.copyProperties(gatheringCode, vo);
		vo.setStateName(DictHolder.getDictItemName("gatheringCodeState", vo.getState()));
		if (gatheringCode.getUserAccount() != null) {
			vo.setUserName(gatheringCode.getUserAccount().getUserName());
		}
		if (gatheringCode.getGatheringChannel() != null) {
			vo.setGatheringChannelName(gatheringCode.getGatheringChannel().getChannelName());
		}
		return vo;
	}

}
