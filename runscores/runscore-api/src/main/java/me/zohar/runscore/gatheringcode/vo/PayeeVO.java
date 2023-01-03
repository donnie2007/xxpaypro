package me.zohar.runscore.gatheringcode.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class PayeeVO implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	private String payee;

	private Boolean inUse;

	public static PayeeVO build(String payee, Boolean inUse) {
		PayeeVO vo = new PayeeVO();
		vo.setPayee(payee);
		vo.setInUse(inUse);
		return vo;
	}

}
