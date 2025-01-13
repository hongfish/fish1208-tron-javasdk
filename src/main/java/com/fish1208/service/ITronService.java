package com.fish1208.service;

import com.alibaba.fastjson.JSONObject;
import com.fish1208.entity.TronSyncData;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public interface ITronService {

    int getBlockNum(String url);

    /**
     * 根据HASH 获取
     *
     * @return
     */

    JSONObject getTransactionInfoById(String url, String hash);

    JSONObject getTransactionById(String url, String hash);

    Long getNowBlock(String url);

    /**
     * 查询指定范围的区块
     */
    JSONObject getblockbylimitnext(String url, Long startNum, Long endNum);

    TronSyncData getTransactionToken(String url, String hash, int unit);

    JSONObject getblockbynum(String url, Integer num);

    /**
     * 查询tron币数量
     *
     * @param address
     * @return
     */
    BigDecimal balanceOfTron(String url, String address);

    /**
     * 查询合约余额
     *
     * @param contract 合约地址
     * @param address  查询地址
     * @param accuracy 代币合约精度
     * @return
     */
    BigDecimal balanceOfTrc(String url, String contract, String address, int accuracy);

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
    String sendTokenTransaction(String url, String contract, String fromAddress, String privateKey,
                                              String amount, String toAddress, String remark);

    String transferTrx(String url,String fromAddress, String privateKey, String amount, String toAddress);

}

