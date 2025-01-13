package com.fish1208.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TronAddress {
    private String address;
    private String pritateKey;
    private String mnemonic;
}
