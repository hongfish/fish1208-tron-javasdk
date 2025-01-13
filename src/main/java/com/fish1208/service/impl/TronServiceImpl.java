package com.fish1208.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fish1208.entity.TriggerSmartContract;
import com.fish1208.entity.TronSyncData;
import com.fish1208.service.ITronService;
import com.fish1208.util.ByteArray;
import com.fish1208.util.TronUtils;
import com.fish1208.util.http.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.LinkedMap;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.services.http.JsonFormat;
import org.tron.core.services.http.Util;
import org.tron.protos.Protocol;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
@Service
public class TronServiceImpl implements ITronService {
	private final static String METHOD_TRON_GETTRANSACTIONBYID = "/wallet/gettransactionbyid";
	private final static String METHOD_TRON_GETTRANSACTIONINFO = "/wallet/gettransactioninfobyid";
	private final static String METHOD_TRON_GETNOWBLOCK = "/wallet/getnowblock";
	private final static String METHOD_TRON_GETBLOCKBYLIMITNEXT = "/wallet/getblockbylimitnext";
	private final static String METHOD_TRON_GETBLOCKBYNUM = "/wallet/getblockbynum";
	private final static String METHOD_TRON_GETACCOUNT = "/wallet/getaccount";// 获取tron账户
	private final static String METHOD_TRON_TRIGGERSMARTCONTRACT = "/wallet/triggersmartcontract";
	private final static String METHOD_TRON_BROADCASTTRANSACTION = "/wallet/broadcasttransaction";
	private final static String METHOD_TRON_CREATETRANSACTION = "/wallet/createtransaction";
	public  final static String RANDOM="sLbWkvHhJRU2h8mw";
	public  final static String USDT_CONTRACT="TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";
	private final static String METHOD_TRON_GETNODEINFO = "/wallet/getnodeinfo";

	public int getBlockNum(String url) {
		url =url + METHOD_TRON_GETNODEINFO;

		String tran = HttpUtils.sendGet(url,null);
		JSONObject param = JSONObject.parseObject(tran);
		String blockStr = param.getString("block");

		return Integer.valueOf(blockStr.split(",")[0].split(":")[1]);
	}

	/**
	 * 根据HASH 获取
	 *
	 * @return
	 */
	public JSONObject getTransactionInfoById(String url, String hash) {
		log.info("=============== 根据HASH 获取交易================");
		url = url + METHOD_TRON_GETTRANSACTIONINFO;
		JSONObject param = new JSONObject();
		param.put("value", hash);
		String tran = HttpUtils.jsonPost(url, param.toJSONString());
		return JSONObject.parseObject(tran);
	}

	public JSONObject getTransactionById(String url, String hash) {
		log.info("=============== 根据HASH 获取交易================");
		url = url + METHOD_TRON_GETTRANSACTIONBYID;
		JSONObject param = new JSONObject();
		param.put("value", hash);
		String tran = HttpUtils.jsonPost(url, param.toJSONString());
		return JSONObject.parseObject(tran);
	}

	public Long getNowBlock(String url) {
		log.info("=============== 获取最新高度================");
		url = url + METHOD_TRON_GETNOWBLOCK;
		String httpRequest = HttpUtils.sendGet(url, null);
		JSONObject jsonObject = JSONObject.parseObject(httpRequest);
		if(TronUtils.isError(jsonObject)) {
			return 0l;
		}
		return jsonObject.getJSONObject("block_header").getJSONObject("raw_data").getLong("number");
	}

	/**
	 * 查询指定范围的区块
	 */
	public JSONObject getblockbylimitnext(String url, Long startNum, Long endNum) {
		log.info("=============== 获取指定范围的区块数据================");
		url = url + METHOD_TRON_GETBLOCKBYLIMITNEXT;
		JSONObject param = new JSONObject();
		param.put("startNum", startNum);
		param.put("endNum", endNum);
		String tran = HttpUtils.jsonPost(url, param.toJSONString());
		return JSONObject.parseObject(tran);
	}

	public TronSyncData getTransactionToken(String url, String hash, int unit) {
		JSONObject json = getTransactionInfoById(url, hash);
		TronSyncData tronSyncData = new TronSyncData();
		tronSyncData.setTxid(hash);
		tronSyncData.setStatus(false);
		if (json == null || json.isEmpty()) {

			tronSyncData.setRemarks("交易不存在");
			return tronSyncData;
		}
		JSONObject receipt = json.getJSONObject("receipt");
		tronSyncData.setRemarks(receipt.getString("result"));
		if (!"SUCCESS".equals(receipt.getString("result"))) {
			// tronSyncData.setRemarks("交易失败");
			return tronSyncData;
		}

		JSONArray jsonArray = json.getJSONArray("log");
		if (CollectionUtils.isEmpty(jsonArray)) {
			tronSyncData.setRemarks("交易数据不存在");
			return tronSyncData;
		}
		tronSyncData.setBlockNumber(json.getIntValue("blockNumber"));
		JSONObject data = jsonArray.getJSONObject(0);
		tronSyncData.setContract(TronUtils.fromHexAddress("41" + data.getString("address")));
		JSONArray topics = data.getJSONArray("topics");
		tronSyncData.setFromAddress(TronUtils.topics(topics.getString(1)));
		tronSyncData.setToAddress(TronUtils.topics(topics.getString(2)));
		tronSyncData.setAmount(TronUtils.amout(data.getString("data"), unit));
		tronSyncData.setStatus(true);
		return tronSyncData;
	}

	public JSONObject getblockbynum(String url, Integer num) {
		url = url + METHOD_TRON_GETBLOCKBYNUM;
		JSONObject param = new JSONObject();
		param.put("num", num);
		String tran = HttpUtils.jsonPost(url, param.toJSONString());
		return JSONObject.parseObject(tran);
	}

	/**
	 * 查询tron币数量
	 *
	 * @param address
	 * @return
	 */
	public BigDecimal balanceOfTron(String url, String address) {
		final BigDecimal decimal = new BigDecimal("1000000");
		final int accuracy = 6;// 六位小数;
		// 通过http接口查询
		url = url + METHOD_TRON_GETACCOUNT;
		JSONObject param = new JSONObject();
		param.put("address", TronUtils.castHexAddress(address));
		JSONObject obj = JSONObject.parseObject(HttpUtils.jsonPost(url, param.toJSONString()));

		if (ObjectUtil.isNotNull(obj)) {
			BigInteger balance = obj.getBigInteger("balance");
			if(ObjectUtil.isNotNull(balance))
				return new BigDecimal(balance).divide(decimal, accuracy, RoundingMode.FLOOR);
		}
		return BigDecimal.ZERO;
	}

	/**
	 * 查询合约余额
	 *
	 * @param contract 合约地址
	 * @param address  查询地址
	 * @param accuracy 代币合约精度
	 * @return
	 */
	public BigDecimal balanceOfTrc(String url, String contract, String address, int accuracy) {
		// 按照精度补位
		StringBuffer wei = new StringBuffer();
		wei.append("1");
		for (int i = 0; i < accuracy; i++) {
			wei.append("0");
		}
		final BigDecimal decimal = new BigDecimal(wei.toString());
		String hexAddress = address;
		if (address.startsWith("T")) {
			hexAddress = TronUtils.toHexAddress(address);
		}
		String hexContract = contract;
		if (contract.startsWith("T")) {
			hexContract = TronUtils.toHexAddress(contract);
		}
		TriggerSmartContract.Param param = new TriggerSmartContract.Param();
		param.setContract_address(hexContract);
		param.setOwner_address(hexAddress);
		param.setFunction_selector("balanceOf(address)");
		String addressParam = TronUtils.addZero(hexAddress.substring(2), 64);
		param.setParameter(addressParam);
		// 通过http查询合约余额
		url = url + METHOD_TRON_TRIGGERSMARTCONTRACT;
		String data = HttpUtils.jsonPost(url, JSONObject.toJSONString(param));
		TriggerSmartContract.Result result = JSON.parseObject(data, TriggerSmartContract.Result.class);
		if (result != null && result.isSuccess()) {
			String value = result.getConstantResult(0);
			// System.out.println(value);
			if (value != null) {
				return new BigDecimal(new BigInteger(value, 16)).divide(decimal, accuracy, RoundingMode.FLOOR);

			}
		}
		return BigDecimal.ZERO;
	}

	public TriggerSmartContract.Result triggerSmartContract(String url, TriggerSmartContract.Param param) {
		url = url + METHOD_TRON_TRIGGERSMARTCONTRACT;

		String tran = HttpUtils.jsonPost(url, JSON.toJSONString(param));
		return JSON.parseObject(tran, TriggerSmartContract.Result.class);
	}

	public JSONObject broadcastTransaction(String url, String dt) {
		url = url + METHOD_TRON_BROADCASTTRANSACTION;

		String tran = HttpUtils.jsonPost(url, dt);
		return JSONObject.parseObject(tran);
	}

	/**
	 * 代币转账 trc20
	 *
	 * @param contract
	 * @param fromAddress
	 * @param privateKey  fromAddress的私钥
	 * @param amount
	 * @param toAddress
	 * @param remark
	 * @return
	 */
	public String sendTokenTransaction(String url, String contract, String fromAddress, String privateKey,
									   String amount, String toAddress, String remark) {
		try {
			String hexFromAddress = TronUtils.castHexAddress(fromAddress);
			String hexToAddress = TronUtils.castHexAddress(toAddress);
			String hexContract = TronUtils.castHexAddress(contract);

			if (remark == null) {
				remark = "";
			}
			TriggerSmartContract.Param param = new TriggerSmartContract.Param();
			param.setOwner_address(hexFromAddress);
			param.setContract_address(hexContract);
			param.setFee_limit(6000000L);
			param.setFunction_selector("transfer(address,uint256)");
			String addressParam = TronUtils.addZero(hexToAddress, 64);
			String amountParam = TronUtils.addZero(
					new BigDecimal(amount).multiply(new BigDecimal("10").pow(6)).toBigInteger().toString(16), 64);
			remark = TronUtils.addZero(remark,64);
			param.setParameter(addressParam + amountParam+remark);
			param.setParameter(addressParam + amountParam);
			// System.out.println("创建交易参数:" + JSONObject.toJSONString(param));
			TriggerSmartContract.Result obj = triggerSmartContract(url, param);
			obj.getTransaction().getRaw_data().put("data", ByteArray.toHexString(remark.getBytes()));
			// System.out.println("创建交易结果:" + JSONObject.toJSONString(obj));
			if (!obj.isSuccess()) {
				log.error("创建交易失败");
				return null;
			}

			// System.out.println(JSONObject.toJSONString(obj.getTransaction()));
			String dt = TronUtils.signTransaction(JSONObject.toJSONString(obj.getTransaction()), privateKey);

			// System.out.println("签名交易结果:" + dt);

			// 广播交易
			if (dt != null) {

				JSONObject rea = broadcastTransaction(url, dt);
				// System.out.println("广播交易结果:" + JSONObject.toJSONString(rea));
				// System.out.println(JSONObject.toJSONString(rea));
				if (rea != null) {
					Object result = rea.get("result");
					if (result instanceof Boolean) {
						if ((boolean) result) {
							return (String) rea.get("txid");
						}
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			log.error(t.getMessage(), t);
		}
		return null;
	}

	private String signTransaction(String trans, String privateKey) {
		try {
			String transactionStr = "{\"transaction\":" + trans + ",\"privateKey\":\"" + privateKey + "\"}";
			JSONObject input = JSONObject.parseObject(transactionStr);
			String strTransaction = input.getJSONObject("transaction").toJSONString();
			Protocol.Transaction transaction = Util.packTransaction(strTransaction);
			JSONObject jsonTransaction = JSONObject
					.parseObject(JsonFormat.printToString(transaction));
			input.put("transaction", jsonTransaction);
			Protocol.TransactionSign.Builder build = Protocol.TransactionSign.newBuilder();
			JsonFormat.merge(input.toJSONString(), build);
			TransactionCapsule reply = getTransactionSign(build.build());
			return Util.printTransaction(reply.getInstance());
		} catch (Exception e) {
			log.error("signTransaction is error, e = {}", e.getMessage());
			return null;
		}
	}

	public static TransactionCapsule getTransactionSign(Protocol.TransactionSign transactionSign) {
		byte[] privateKey = transactionSign.getPrivateKey().toByteArray();
		TransactionCapsule trx = new TransactionCapsule(transactionSign.getTransaction());

		trx.sign(privateKey);
		return trx;
	}

	/**
	 *	 转账trx
	 * @param fromAddress 从那个地址转出
	 * @param privateKey 转出地址私钥
	 * @param amount 转账金额
	 * @param toAddress 转到那个地址
	 * @return
	 * @throws Throwable
	 */
	public String transferTrx(String url,String fromAddress, String privateKey, String amount, String toAddress) {
		String hexFromAddress = TronUtils.toHexAddress(fromAddress);
		String hexToAddress = TronUtils.toHexAddress(toAddress);
		BigInteger a = new BigDecimal(amount).multiply(new BigDecimal("10").pow(6)).toBigInteger();
		Map<String, Object> paramMap=new LinkedMap<>();
		paramMap.put("to_address", hexToAddress);
		paramMap.put("owner_address", hexFromAddress);
		paramMap.put("amount", a);
		// System.out.println(JSONObject.toJSONString(paramMap));
		String transaction= HttpUtils.jsonPost(url+"/wallet/createtransaction", JSONObject.toJSONString(paramMap));


		String dt = signTransaction(transaction,privateKey);
		log.info("签名交易结果:" + dt);
		// System.out.println("签名交易结果:" + dt);
		//广播交易
		if (dt != null) {
			log.info("广播交易参数:" + dt);
			//通过http广播交易
			JSONObject rea = JSON.parseObject(HttpUtils.jsonPost(url+"/wallet/broadcasttransaction", dt));
			// System.out.println(JSONObject.toJSONString(rea));
			log.info("广播交易结果:" + JSONObject.toJSONString(rea));
			if (rea != null) {
				Object result = rea.get("result");
				if (result instanceof Boolean) {
					if ((boolean) result) {
						return (String) rea.get("txid");
					}
				}
			}
		}

		return null;
	}

	public Integer hashGetLastNum(String hash){
		return Integer.parseInt(hash.replaceAll(".*(\\d+)\\D*", "$1"));
	}

}
