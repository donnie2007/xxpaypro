package me.zohar.runscore.useraccount.param;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class VirtualWalletParam {

	@NotBlank
	private String virtualWalletAddr;
	
	/**
	 * 用户账号id
	 */
	@NotBlank
	private String userAccountId;

}
