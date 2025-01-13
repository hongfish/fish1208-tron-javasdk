package com.fish1208.entity;

import java.io.Serializable;

public class GetTransactionSign implements Serializable {

	public static class Param implements Serializable {
		private Object transaction;//所签名的交易
		private String privateKey;//交易发送账户的私钥, HEX 格式
		public Object getTransaction() {
			return transaction;
		}
		public void setTransaction(Object transaction) {
			this.transaction = transaction;
		}
		public String getPrivateKey() {
			return privateKey;
		}
		public void setPrivateKey(String privateKey) {
			this.privateKey = privateKey;
		}
	}

}
