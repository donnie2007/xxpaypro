package me.zohar.runscore.gatheringcode.param;

import java.util.Date;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.BeanUtils;

import lombok.Data;
import me.zohar.runscore.common.utils.IdUtils;
import me.zohar.runscore.constants.Constant;
import me.zohar.runscore.gatheringcode.domain.GatheringCode;

@Data
public class GatheringCodeParam {

	/**
	 * 主键id
	 */
	private String id;

	/**
	 * 所属账号
	 */
	private String userName;

	/**
	 * 收款通道id
	 */
	@NotBlank
	private String gatheringChannelId;

	/**
	 * 收款金额
	 */
	private Double gatheringAmount;

	private Boolean fixedGatheringAmount;

	/**
	 * 收款人
	 */
	@NotBlank
	private String payee;



	private String storageId;

	/**
	 * 银行卡号
	 */
	private String bankCode;

	/**
	 * 开户行
	 */
	private String bankAddress;

	/**
	 * 卡户主
	 */

	private String bankUsername;

	public GatheringCode convertToPo(String userAccountId) {
		GatheringCode po = new GatheringCode();
		BeanUtils.copyProperties(this, po);
		po.setId(IdUtils.getId());
		po.setCreateTime(new Date());
		po.setUserAccountId(userAccountId);
		po.setInUse(true);
		po.setState(Constant.收款码状态_正常);
		return po;
	}

}
