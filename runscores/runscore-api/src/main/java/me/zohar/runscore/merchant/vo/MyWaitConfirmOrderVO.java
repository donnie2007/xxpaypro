package me.zohar.runscore.merchant.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.BeanUtils;

import com.fasterxml.jackson.annotation.JsonFormat;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;
import me.zohar.runscore.dictconfig.DictHolder;
import me.zohar.runscore.merchant.domain.MerchantOrder;

@Data
public class MyWaitConfirmOrderVO {

	/**
	 * 主键id
	 */
	private String id;

	/**
	 * 订单号
	 */
	private String orderNo;

	private String gatheringChannelId;

	private String gatheringChannelName;

	/**
	 * 收款金额
	 */
	private Double gatheringAmount;

	/**
	 * 接单时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date receivedTime;

	/**
	 * 商户自定义的附加信息
	 */
	private String attch;

	/**
	 * 订单状态
	 */
	private String orderState;

	private String orderStateName;

	private String payee;

	public static List<MyWaitConfirmOrderVO> convertFor(List<MerchantOrder> merchantOrders) {
		if (CollectionUtil.isEmpty(merchantOrders)) {
			return new ArrayList<>();
		}
		List<MyWaitConfirmOrderVO> vos = new ArrayList<>();
		for (MerchantOrder merchantOrder : merchantOrders) {
			vos.add(convertFor(merchantOrder));
		}
		return vos;
	}

	public static MyWaitConfirmOrderVO convertFor(MerchantOrder merchantOrder) {
		if (merchantOrder == null) {
			return null;
		}
		MyWaitConfirmOrderVO vo = new MyWaitConfirmOrderVO();
		BeanUtils.copyProperties(merchantOrder, vo);
		vo.setGatheringChannelName(merchantOrder.getGatheringChannel().getChannelName());
		if (merchantOrder.getPayInfo() != null) {
			vo.setAttch(merchantOrder.getPayInfo().getAttch());
		}
		vo.setOrderStateName(DictHolder.getDictItemName("merchantOrderState", vo.getOrderState()));
		return vo;
	}

}
