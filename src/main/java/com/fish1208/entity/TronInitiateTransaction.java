package com.fish1208.entity;

import java.math.BigDecimal;

public class TronInitiateTransaction {
	private String url;
	private String fromAddress;
	private String toAddress;
	private BigDecimal value;
	private String contract;
	private String privateKey;
	private Integer decimal;
	private String chainId;

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getFromAddress() {
		return fromAddress;
	}
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}
	public String getToAddress() {
		return toAddress;
	}
	public void setToAddress(String toAddress) {
		this.toAddress = toAddress;
	}
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public String getContract() {
		return contract;
	}
	public void setContract(String contract) {
		this.contract = contract;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	public Integer getDecimal() {
		return decimal;
	}
	public void setDecimal(Integer decimal) {
		this.decimal = decimal;
	}
	public String getChainId() {
		return chainId;
	}
	public void setChainId(String chainId) {
		this.chainId = chainId;
	}


}
