package com.fish1208.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class TriggerSmartContract implements Serializable {

	public static class Param implements Serializable {
		private String contract_address;//默认为hexString格式
		private String function_selector;//函数签名，不能有空格
		private String parameter;//调用参数[1,2]的虚拟机格式，使用remix提供的js工具，将合约调用者调用的参数数组[1,2]转化为虚拟机所需要的参数格式
		private long fee_limit;//最大消耗的SUN（1TRX = 1,000,000SUN）
		private long call_value;//本次调用往合约转账的SUN（1TRX = 1,000,000SUN）
		private String owner_address;//发起triggercontract的账户地址，默认为hexString格式
		private int call_token_value;//本次调用往合约中转账10币的数量，如果不设置token_id，这项设置为0或者不设置
		private int token_id;//本次调用往合约中转账10币的id，如果没有，不需要设置
		private long Permission_id;//可选参数Permission_id，多重签名时使用，设置交易多重签名时使用的permissionId
		public String getContract_address() {
			return contract_address;
		}
		public void setContract_address(String contract_address) {
			this.contract_address = contract_address;
		}
		public String getFunction_selector() {
			return function_selector;
		}
		public void setFunction_selector(String function_selector) {
			this.function_selector = function_selector;
		}
		public String getParameter() {
			return parameter;
		}
		public void setParameter(String parameter) {
			this.parameter = parameter;
		}
		public long getFee_limit() {
			return fee_limit;
		}
		public void setFee_limit(long fee_limit) {
			this.fee_limit = fee_limit;
		}
		public long getCall_value() {
			return call_value;
		}
		public void setCall_value(long call_value) {
			this.call_value = call_value;
		}
		public String getOwner_address() {
			return owner_address;
		}
		public void setOwner_address(String owner_address) {
			this.owner_address = owner_address;
		}
		public int getCall_token_value() {
			return call_token_value;
		}
		public void setCall_token_value(int call_token_value) {
			this.call_token_value = call_token_value;
		}
		public int getToken_id() {
			return token_id;
		}
		public void setToken_id(int token_id) {
			this.token_id = token_id;
		}
		public long getPermission_id() {
			return Permission_id;
		}
		public void setPermission_id(long permission_id) {
			Permission_id = permission_id;
		}

	}


	public static class Result implements Serializable {
		private Map<String, Object> result;
		private List<Object> constant_result;
		private Transaction transaction;



		public Map<String, Object> getResult() {
			return result;
		}


		public void setResult(Map<String, Object> result) {
			this.result = result;
		}


		public List<Object> getConstant_result() {
			return constant_result;
		}


		public void setConstant_result(List<Object> constant_result) {
			this.constant_result = constant_result;
		}


		public Transaction getTransaction() {
			return transaction;
		}


		public void setTransaction(Transaction transaction) {
			this.transaction = transaction;
		}


		/**
		 * 获取结果
		 *
		 * @param index
		 * @param <T>
		 * @return
		 */
		public <T> T getConstantResult(int index) {
			if (constant_result == null || constant_result.size() <= index) {
				return null;
			}
			return (T) constant_result.get(index);
		}


		public boolean isSuccess() {
			if (result != null) {
				Object obj = this.result.get("result");
				if (obj instanceof Boolean) {
					return (boolean) obj;
				}
			}
			return false;
		}
	}


	public static class Transaction implements Serializable {
		private String txID;
		private Map<String, Object> raw_data;
		private String raw_data_hex;
		private boolean visible;
		private String[] signature;
		public String getTxID() {
			return txID;
		}
		public void setTxID(String txID) {
			this.txID = txID;
		}
		public Map<String, Object> getRaw_data() {
			return raw_data;
		}
		public void setRaw_data(Map<String, Object> raw_data) {
			this.raw_data = raw_data;
		}
		public String getRaw_data_hex() {
			return raw_data_hex;
		}
		public void setRaw_data_hex(String raw_data_hex) {
			this.raw_data_hex = raw_data_hex;
		}
		public boolean isVisible() {
			return visible;
		}
		public void setVisible(boolean visible) {
			this.visible = visible;
		}
		public String[] getSignature() {
			return signature;
		}
		public void setSignature(String[] signature) {
			this.signature = signature;
		}

	}

	public static class RawData implements Serializable {
		private List<Contract> contract;
		private String ref_block_bytes;
		private String ref_block_hash;
		private Long expiration;
		private Long timestamp;
		private long fee_limit;
		public List<Contract> getContract() {
			return contract;
		}
		public void setContract(List<Contract> contract) {
			this.contract = contract;
		}
		public String getRef_block_bytes() {
			return ref_block_bytes;
		}
		public void setRef_block_bytes(String ref_block_bytes) {
			this.ref_block_bytes = ref_block_bytes;
		}
		public String getRef_block_hash() {
			return ref_block_hash;
		}
		public void setRef_block_hash(String ref_block_hash) {
			this.ref_block_hash = ref_block_hash;
		}
		public long getExpiration() {
			return expiration;
		}
		public void setExpiration(long expiration) {
			this.expiration = expiration;
		}
		public long getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
		public long getFee_limit() {
			return fee_limit;
		}
		public void setFee_limit(long fee_limit) {
			this.fee_limit = fee_limit;
		}

	}

	public static class Contract implements Serializable {
		private Map<String, Object> parameter;
		private String type;
		public Map<String, Object> getParameter() {
			return parameter;
		}
		public void setParameter(Map<String, Object> parameter) {
			this.parameter = parameter;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}

	}
}
