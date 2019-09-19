package com.xxjr.xxyun.netty;


public enum CmdEnum {

	CMD_0000("0000", "xxjrCommun", "formatError", "格式错误"),
	CMD_0001("0001", "xxjrCommun", "heartBeat", "心跳连接"),
	CMD_0002("0002", "xxjrCommun", "scanLogin", "扫码登录"),
	CMD_0003("0003", "xxjrCommun", "confLogin", "登录确认"),
	CMD_0004("0004", "xxjrCommun", "callCmd", "拨打电话");


	private String cmdName;
	private String cmdService;
	private String cmdMethod;
	private String busiDesc;

	private CmdEnum() {
	}

	private CmdEnum(String cmdName, String cmdService, String cmdMethod, String busiDesc) {
		this.cmdName = cmdName;
		this.cmdService = cmdService;
		this.cmdMethod = cmdMethod;
		this.busiDesc = busiDesc;
	}
	
	public String getCmdService() {
		return cmdService;
	}

	public void setCmdService(String cmdService) {
		this.cmdService = cmdService;
	}

	public String getCmdName() {
		return cmdName;
	}

	public void setCmdName(String cmdName) {
		this.cmdName = cmdName;
	}

	public String getCmdMethod() {
		return cmdMethod;
	}

	public void setCmdMethod(String cmdMethod) {
		this.cmdMethod = cmdMethod;
	}

	public String getBusiDesc() {
		return busiDesc;
	}

	public void setBusiDesc(String busiDesc) {
		this.busiDesc = busiDesc;
	}

	public String value() {
		return this.cmdName;
	}
}
