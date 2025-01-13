package com.fish1208.controller;

import com.alibaba.fastjson.JSONObject;
import com.fish1208.common.response.Result;
import com.fish1208.config.ChainConfig;
import com.fish1208.service.ITronService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/tron")
public class TronController {

    @Autowired
    private ITronService tronService;

    @Autowired
    private ChainConfig chainConfig;

    /**
     * 获取当前区块高度
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getCurrentBlockNumber")
    public Result<?> getCurrentBlockNumber() throws Exception{
        Long latestBlock = tronService.getNowBlock(chainConfig.getRpcUrl());
        return Result.data(latestBlock);
    }

    /**
     * 查询账户余额
     * @param address
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/balanceOf")
    public Result<?> balanceOf(@RequestParam String address) throws Exception {
        BigDecimal balance = tronService.balanceOfTron(chainConfig.getRpcUrl(), address);
        return Result.data(balance);
    }

    /**
     * 根据hash值获取交易信息
     *
     * @param txHash
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getTransactionInfoById")
    public Result<?> getTransactionInfoById(@RequestParam String txHash) throws Exception {
        JSONObject result = tronService.getTransactionInfoById(chainConfig.getRpcUrl(), txHash);
        return Result.data(result);
    }

    /**
     * 根据hash值获取交易
     *
     * @param txHash
     * @return
     * @throws Exception
     */
    @GetMapping(value = "/getTransactionById")
    public Result<?> getTransactionById(@RequestParam String txHash) throws Exception {
        JSONObject result = tronService.getTransactionById(chainConfig.getRpcUrl(), txHash);
        return Result.data(result);
    }

}
