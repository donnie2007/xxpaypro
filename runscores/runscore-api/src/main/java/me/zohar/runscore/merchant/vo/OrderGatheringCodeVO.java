package me.zohar.runscore.merchant.vo;

import java.util.Date;

import me.zohar.runscore.gatheringcode.domain.GatheringCode;
import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import me.zohar.runscore.dictconfig.DictHolder;
import me.zohar.runscore.merchant.domain.MerchantOrder;

@Data
public class OrderGatheringCodeVO {

	/**
	 * 主键id
	 */
	private String id;

	/**
	 * 订单号
	 */
	private String orderNo;

	/**
	 * 收款渠道
	 */
	private String gatheringChannelCode;

	private String gatheringChannelName;

	/**
	 * 收款金额
	 */
	private Double gatheringAmount;

	/**
	 * 订单状态
	 */
	private String orderState;

	private String orderStateName;

	/**
	 * 提交时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date submitTime;

	/**
	 * 有效时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date usefulTime;

	private String gatheringCodeStorageId;

	private String gatheringCodeUrl;

	private String bankCode;

	private String bankAddress;

	private String bankUsername;

	/**
	 * 同步通知地址
	 */
	private String returnUrl;

	public static OrderGatheringCodeVO convertFor(MerchantOrder merchantOrder) {
		if (merchantOrder == null) {
			return null;
		}
		OrderGatheringCodeVO vo = new OrderGatheringCodeVO();
		BeanUtils.copyProperties(merchantOrder, vo);
		vo.setOrderStateName(DictHolder.getDictItemName("merchantOrderState", vo.getOrderState()));
		vo.setReturnUrl(merchantOrder.getPayInfo().getReturnUrl());
		if (merchantOrder.getGatheringChannel() != null) {
			vo.setGatheringChannelCode(merchantOrder.getGatheringChannel().getChannelCode());
			vo.setGatheringChannelName(merchantOrder.getGatheringChannel().getChannelName());
			if(merchantOrder.getGatheringCode()!=null && "bankcard".equals(merchantOrder.getGatheringChannel().getChannelCode())){
				vo.setBankCode(merchantOrder.getGatheringCode().getBankCode());
				vo.setBankAddress(merchantOrder.getGatheringCode().getBankAddress());
				vo.setBankUsername(merchantOrder.getGatheringCode().getBankUsername());
			}
		}
		return vo;
	}

}
