package com.fish1208.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TronSyncData {
	private String txid;
	private String toAddress;
	private String fromAddress;
	private String contract;
	private Integer blockNumber;
	private BigDecimal amount;
	private Boolean status;
	private String remarks;
	private Long timestamp;
	private Long blockTimestamp;






}
