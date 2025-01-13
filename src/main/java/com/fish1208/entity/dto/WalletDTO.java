package com.fish1208.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletDTO {

    private String account;

    private String password;

    private String walletFileName;

    private String addressTo; //资金接收方

    private BigDecimal funds; //转账资金

}
