package me.zohar.runscore.mastercontrol.param;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class UpdateReceiveOrderSettingParam {

	@NotNull
	private Boolean stopStartAndReceiveOrder;
	
	@NotNull
	private Boolean banReceiveRepeatOrder;

	@NotNull
	@DecimalMin(value = "0", inclusive = true)
	private Integer receiveOrderEffectiveDuration;

	@NotNull
	@DecimalMin(value = "0", inclusive = true)
	private Integer orderPayEffectiveDuration;

	@NotNull
	@DecimalMin(value = "0", inclusive = false)
	private Integer receiveOrderUpperLimit;

	@NotNull
	private Boolean showAllOrder;

	@NotNull
	@DecimalMin(value = "0", inclusive = true)
	private Double cashDepositMinimumRequire;

	@NotNull
	@DecimalMin(value = "0", inclusive = true)
	private Double cashPledge;

	@NotNull
	private Boolean unfixedGatheringCodeReceiveOrder;
	
	@NotNull
	private Boolean receiveOrderSound;
	
	@NotNull
	private Boolean auditGatheringCode;
	
}
