package com.fish1208.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferFromDTO {

    private String fromEoA;

    private String eoaPwd;

    private String toEoA;

    private BigDecimal ehtValue; //转账资金

    private String gasPrice; //gas燃料

    private String gasLimit; //gas上限
}
