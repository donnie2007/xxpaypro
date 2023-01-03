package me.zohar.runscore.mastercontrol.param;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class UpdateRegisterSettingParam {
	
	@NotNull
	private Boolean registerEnabled;

	@NotNull
	@DecimalMin(value = "0", inclusive = true)
	private Integer inviteCodeEffectiveDuration;
	
	@NotNull
	private Boolean inviteRegisterEnabled;
	
	@NotNull
	private Boolean onlyOpenMemberAccount;
	
	@NotBlank
	private String agentExplain;
}
