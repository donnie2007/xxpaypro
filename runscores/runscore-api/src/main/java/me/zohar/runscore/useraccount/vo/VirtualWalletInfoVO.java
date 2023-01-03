package me.zohar.runscore.useraccount.vo;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import me.zohar.runscore.useraccount.domain.UserAccount;

@Data
public class VirtualWalletInfoVO {

	private String virtualWalletAddr;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date virtualWalletLatelyModifyTime;

	public static VirtualWalletInfoVO convertFor(UserAccount userAccount) {
		if (userAccount == null) {
			return null;
		}
		VirtualWalletInfoVO vo = new VirtualWalletInfoVO();
		BeanUtils.copyProperties(userAccount, vo);
		return vo;
	}

}
