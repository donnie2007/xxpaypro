package me.zohar.runscore.useraccount.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "login_log")
@DynamicInsert(true)
@DynamicUpdate(true)
public class LoginLog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 主键id
	 */
	@Id
	@Column(name = "id", length = 32)
	private String id;

	private String sessionId;

	/**
	 * 登录系统
	 */
	private String system;

	/**
	 * 登录状态
	 */
	private String state;

	/**
	 * ip地址
	 */
	private String ipAddr;

	/**
	 * 登录地点
	 */
	private String loginLocation;

	/**
	 * 登录时间
	 */
	private Date loginTime;

	/**
	 * 浏览器
	 */
	private String browser;

	/**
	 * 操作系统
	 */
	private String os;

	private String msg;

	/**
	 * 用户名
	 */
	private String userName;

	/**
	 * 最后访问时间
	 */
	private Date lastAccessTime;

}
