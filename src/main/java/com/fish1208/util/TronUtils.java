package com.fish1208.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fish1208.entity.TronAddress;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.crypto.*;
import org.bitcoinj.wallet.DeterministicSeed;
import org.tron.common.crypto.Hash;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.services.http.JsonFormat;
import org.tron.core.services.http.Util;
import org.tron.protos.Contract;
import org.tron.protos.Protocol;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TronUtils {
    static int ADDRESS_SIZE = 21;
    private static final String A="j@f6D2wY1JvtfTT@";
    private static final String B="7LRkXl2%KL^mpEef";
    public static final String RANDOM="%z#ZCBTOGb!IQPve";
    private static byte addressPreFixByte = (byte) 0x41; // 41 + address (byte) 0xa0; //a0 + address

    public static List<String> createMnemonicCode() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] entropy = new byte[DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS / 8];
        secureRandom.nextBytes(entropy);
        List<String> mnemonicCode = new ArrayList<String>();
        // 生成12位助记词
        try {
            mnemonicCode = MnemonicCode.INSTANCE.toMnemonic(entropy);
        } catch (MnemonicException.MnemonicLengthException e) {

            e.printStackTrace();
            log.error("生成助记词错误", e);
        }
        return mnemonicCode;
    }


    /**
     * 离线创建地址
     */
    public static TronAddress createAddress() {
        ImmutableList<ChildNumber> BIP44_ACCOUNT_ZERO_PATH = ImmutableList.of(new ChildNumber(44, true),
                new ChildNumber(195, true), ChildNumber.ZERO_HARDENED, ChildNumber.ZERO);
        List<String> mnemonicCode = createMnemonicCode();
        byte[] seed = MnemonicCode.toSeed(mnemonicCode, "");
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(masterPrivateKey);
        DeterministicKey deterministicKey = deterministicHierarchy.deriveChild(BIP44_ACCOUNT_ZERO_PATH, false, true,
                new ChildNumber(0));
        byte[] bytes = deterministicKey.getPrivKeyBytes();
        ECKeyPair keyPair = ECKeyPair.create(bytes);
        String privateKey = keyPair.getPrivateKey().toString(16);
        String address= TronUtils.fromHexAddress(TronUtils.getPrivateKeyToAddress(privateKey));
        String mnemonic=StringUtils.join(mnemonicCode, " ");
        TronAddress tronAddress=TronAddress.builder()
                .pritateKey(privateKey).mnemonic(mnemonic).address(address).build();
        return tronAddress;
    }
    public static String toHexAddress(String tAddress) {

        return ByteArray.toHexString(decodeFromBase58Check(tAddress));
    }

    public static BigDecimal amout(String date, int t) {
        long dec_num = Long.parseLong(date, 16);
        BigDecimal val = new BigDecimal(dec_num);
        return val.divide(new BigDecimal(Math.pow(10, t)), 6, BigDecimal.ROUND_DOWN);
    }

    private static byte[] decodeFromBase58Check(String addressBase58) {
        if (StringUtils.isEmpty(addressBase58)) {
            return null;
        }
        byte[] address = decode58Check(addressBase58);
        if (!addressValid(address)) {
            return null;
        }
        return address;
    }

    private static byte[] decode58Check(String input) {
        byte[] decodeCheck = Base58.decode(input);
        if (decodeCheck.length <= 4) {
            return null;
        }
        byte[] decodeData = new byte[decodeCheck.length - 4];
        System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
        byte[] hash0 = Sha256Hash.hash(true, decodeData);
        byte[] hash1 = Sha256Hash.hash(true, hash0);
        if (hash1[0] == decodeCheck[decodeData.length] && hash1[1] == decodeCheck[decodeData.length + 1]
                && hash1[2] == decodeCheck[decodeData.length + 2] && hash1[3] == decodeCheck[decodeData.length + 3]) {
            return decodeData;
        }
        return null;
    }

    public static String encode58Check(byte[] input) {
        byte[] hash0 = hash(input);
        byte[] hash1 = hash(hash0);
        byte[] inputCheck = new byte[input.length + 4];
        System.arraycopy(input, 0, inputCheck, 0, input.length);
        System.arraycopy(hash1, 0, inputCheck, input.length, 4);
        return Base58.encode(inputCheck);
    }

    private static byte[] hash(byte[] input) {
        return hash(input, 0, input.length);
    }

    private static byte[] hash(byte[] input, int offset, int length) {

        MessageDigest digest = newDigest();
        digest.update(input, offset, length);
        return digest.digest();

    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e); // Can't happen.
        }
    }

    private static boolean addressValid(byte[] address) {
        if (ArrayUtils.isEmpty(address)) {
            return false;
        }
        if (address.length != ADDRESS_SIZE) {
            return false;
        }
        byte preFixbyte = address[0];
        return preFixbyte == getAddressPreFixByte();
        // Other rule;
    }

    public static String fromHexAddress(String address) {
        return encode58Check(ByteArray.fromHexString(address));
    }



    public static String topics(String data) {
        String hexAddress = "41" + data.substring(data.length() - 40, data.length());
        return fromHexAddress(hexAddress);

    }

    private static byte getAddressPreFixByte() {
        return addressPreFixByte;
    }

    /**
     * 补充0到64个字节
     *
     * @param dt
     * @return
     */
    public static String addZero(String dt, int length) {
        StringBuilder builder = new StringBuilder();
        final int count = length;
        int zeroAmount = count - dt.length();
        for (int i = 0; i < zeroAmount; i++) {
            builder.append("0");
        }
        builder.append(dt);
        return builder.toString();
    }

    public static String castHexAddress(String address) {
        if (address.startsWith("T")) {
            return TronUtils.toHexAddress(address);
        }
        return address;
    }
    public static String getPrivateKeyToAddress(String privateKey) {
        String address = Keys.toChecksumAddress(Keys.getAddress(ECKeyPair.create(Numeric.toBigInt(privateKey))));
        //System.out.println("======" + address);
        return address.replace("0x", "41");
    }

    public static boolean isError(JSONObject json) {
        if (json == null || (StringUtils.isNotEmpty(json.getString("error")) && json.get("error") != "null")) {
            return true;
        }
        return false;
    }
    public static BigInteger decodeHex(String hex_num) {
        hex_num = hex_num.replace("0x", "");
        if ("".equals(hex_num)) {
            return new BigInteger("0");
        }
        BigInteger bigInterger = new BigInteger(hex_num, 16);
        return bigInterger;
    }
    public static  String signTransaction(String trans, String privateKey) throws Exception {
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
    }

    public static TransactionCapsule getTransactionSign(Protocol.TransactionSign transactionSign) {
        byte[] privateKey = transactionSign.getPrivateKey().toByteArray();
        TransactionCapsule trx = new TransactionCapsule(transactionSign.getTransaction());

        trx.sign(privateKey);
        return trx;
    }

    public static Protocol.Transaction packTransaction(String strTransaction) {
        JSONObject jsonTransaction = JSONObject.parseObject(strTransaction);
        JSONObject rawData = jsonTransaction.getJSONObject("raw_data");
        JSONArray contracts = new JSONArray();
        JSONArray rawContractArray = rawData.getJSONArray("contract");

        for(int i = 0; i < rawContractArray.size(); ++i) {
            try {
                JSONObject contract = rawContractArray.getJSONObject(i);
                JSONObject parameter = contract.getJSONObject("parameter");
                String contractType = contract.getString("type");
                Any any = null;
                switch (contractType) {
                    case "AccountCreateContract":
                        Contract.AccountCreateContract.Builder accountCreateContractBuilder = Contract.AccountCreateContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), accountCreateContractBuilder);
                        any = Any.pack(accountCreateContractBuilder.build());
                        break;
                    case "TransferContract":
                        Contract.TransferContract.Builder transferContractBuilder = Contract.TransferContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), transferContractBuilder);
                        any = Any.pack(transferContractBuilder.build());
                        break;
                    case "TransferAssetContract":
                        Contract.TransferAssetContract.Builder transferAssetContractBuilder = Contract.TransferAssetContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), transferAssetContractBuilder);
                        any = Any.pack(transferAssetContractBuilder.build());
                        break;
                    case "VoteAssetContract":
                        Contract.VoteAssetContract.Builder voteAssetContractBuilder = Contract.VoteAssetContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), voteAssetContractBuilder);
                        any = Any.pack(voteAssetContractBuilder.build());
                        break;
                    case "VoteWitnessContract":
                        Contract.VoteWitnessContract.Builder voteWitnessContractBuilder = Contract.VoteWitnessContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), voteWitnessContractBuilder);
                        any = Any.pack(voteWitnessContractBuilder.build());
                        break;
                    case "WitnessCreateContract":
                        Contract.WitnessCreateContract.Builder witnessCreateContractBuilder = Contract.WitnessCreateContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), witnessCreateContractBuilder);
                        any = Any.pack(witnessCreateContractBuilder.build());
                        break;
                    case "AssetIssueContract":
                        Contract.AssetIssueContract.Builder assetIssueContractBuilder = Contract.AssetIssueContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), assetIssueContractBuilder);
                        any = Any.pack(assetIssueContractBuilder.build());
                        break;
                    case "WitnessUpdateContract":
                        Contract.WitnessUpdateContract.Builder witnessUpdateContractBuilder = Contract.WitnessUpdateContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), witnessUpdateContractBuilder);
                        any = Any.pack(witnessUpdateContractBuilder.build());
                        break;
                    case "ParticipateAssetIssueContract":
                        Contract.ParticipateAssetIssueContract.Builder participateAssetIssueContractBuilder = Contract.ParticipateAssetIssueContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), participateAssetIssueContractBuilder);
                        any = Any.pack(participateAssetIssueContractBuilder.build());
                        break;
                    case "AccountUpdateContract":
                        Contract.AccountUpdateContract.Builder accountUpdateContractBuilder = Contract.AccountUpdateContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), accountUpdateContractBuilder);
                        any = Any.pack(accountUpdateContractBuilder.build());
                        break;
                    case "FreezeBalanceContract":
                        Contract.FreezeBalanceContract.Builder freezeBalanceContractBuilder = Contract.FreezeBalanceContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), freezeBalanceContractBuilder);
                        any = Any.pack(freezeBalanceContractBuilder.build());
                        break;
                    case "UnfreezeBalanceContract":
                        Contract.UnfreezeBalanceContract.Builder unfreezeBalanceContractBuilder = Contract.UnfreezeBalanceContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), unfreezeBalanceContractBuilder);
                        any = Any.pack(unfreezeBalanceContractBuilder.build());
                        break;
                    case "UnfreezeAssetContract":
                        Contract.UnfreezeAssetContract.Builder unfreezeAssetContractBuilder = Contract.UnfreezeAssetContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), unfreezeAssetContractBuilder);
                        any = Any.pack(unfreezeAssetContractBuilder.build());
                        break;
                    case "WithdrawBalanceContract":
                        Contract.WithdrawBalanceContract.Builder withdrawBalanceContractBuilder = Contract.WithdrawBalanceContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), withdrawBalanceContractBuilder);
                        any = Any.pack(withdrawBalanceContractBuilder.build());
                        break;
                    case "UpdateAssetContract":
                        Contract.UpdateAssetContract.Builder updateAssetContractBuilder = Contract.UpdateAssetContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), updateAssetContractBuilder);
                        any = Any.pack(updateAssetContractBuilder.build());
                        break;
                    case "SmartContract":
                        Protocol.SmartContract.Builder smartContractBuilder = Protocol.SmartContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), smartContractBuilder);
                        any = Any.pack(smartContractBuilder.build());
                        break;
                    case "TriggerSmartContract":
                        Contract.TriggerSmartContract.Builder triggerSmartContractBuilder = Contract.TriggerSmartContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), triggerSmartContractBuilder);
                        any = Any.pack(triggerSmartContractBuilder.build());
                        break;
                    case "CreateSmartContract":
                        Contract.CreateSmartContract.Builder createSmartContractBuilder = Contract.CreateSmartContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), createSmartContractBuilder);
                        any = Any.pack(createSmartContractBuilder.build());
                        break;
                    case "ExchangeCreateContract":
                        Contract.ExchangeCreateContract.Builder exchangeCreateContractBuilder = Contract.ExchangeCreateContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), exchangeCreateContractBuilder);
                        any = Any.pack(exchangeCreateContractBuilder.build());
                        break;
                    case "ExchangeInjectContract":
                        Contract.ExchangeInjectContract.Builder exchangeInjectContractBuilder = Contract.ExchangeInjectContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), exchangeInjectContractBuilder);
                        any = Any.pack(exchangeInjectContractBuilder.build());
                        break;
                    case "ExchangeTransactionContract":
                        Contract.ExchangeTransactionContract.Builder exchangeTransactionContractBuilder = Contract.ExchangeTransactionContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), exchangeTransactionContractBuilder);
                        any = Any.pack(exchangeTransactionContractBuilder.build());
                        break;
                    case "ExchangeWithdrawContract":
                        Contract.ExchangeWithdrawContract.Builder exchangeWithdrawContractBuilder = Contract.ExchangeWithdrawContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), exchangeWithdrawContractBuilder);
                        any = Any.pack(exchangeWithdrawContractBuilder.build());
                        break;
                    case "ProposalCreateContract":
                        Contract.ProposalCreateContract.Builder ProposalCreateContractBuilder = Contract.ProposalCreateContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), ProposalCreateContractBuilder);
                        any = Any.pack(ProposalCreateContractBuilder.build());
                        break;
                    case "ProposalApproveContract":
                        Contract.ProposalApproveContract.Builder ProposalApproveContractBuilder = Contract.ProposalApproveContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), ProposalApproveContractBuilder);
                        any = Any.pack(ProposalApproveContractBuilder.build());
                        break;
                    case "ProposalDeleteContract":
                        Contract.ProposalDeleteContract.Builder ProposalDeleteContractBuilder = Contract.ProposalDeleteContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), ProposalDeleteContractBuilder);
                        any = Any.pack(ProposalDeleteContractBuilder.build());
                        break;
                    case "UpdateSettingContract":
                        Contract.UpdateSettingContract.Builder UpdateSettingContractBuilder = Contract.UpdateSettingContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), UpdateSettingContractBuilder);
                        any = Any.pack(UpdateSettingContractBuilder.build());
                        break;
                    case "UpdateEnergyLimitContract":
                        Contract.UpdateEnergyLimitContract.Builder UpdateEnergyLimitContractBuilder = Contract.UpdateEnergyLimitContract.newBuilder();
                        JsonFormat.merge(parameter.getJSONObject("value").toJSONString(), UpdateEnergyLimitContractBuilder);
                        any = Any.pack(UpdateEnergyLimitContractBuilder.build());
                }

                if (any != null) {
                    String value = org.tron.common.utils.ByteArray.toHexString(any.getValue().toByteArray());
                    parameter.put("value", value);
                    contract.put("parameter", parameter);
                    contracts.add(contract);
                }
            } catch (JsonFormat.ParseException var40) {
            }
        }

        rawData.put("contract", contracts);
        jsonTransaction.put("raw_data", rawData);
        Protocol.Transaction.Builder transactionBuilder = Protocol.Transaction.newBuilder();

        try {
            JsonFormat.merge(new ObjectMapper().writeValueAsString(jsonTransaction), transactionBuilder);
            return transactionBuilder.build();
        } catch (Exception var39) {
            return null;
        }
    }

    public static byte[] generateContractAddress(Protocol.Transaction trx, byte[] ownerAddress) {
        byte[] txRawDataHash = org.tron.common.utils.Sha256Hash.of(trx.getRawData().toByteArray()).getBytes();
        byte[] combined = new byte[txRawDataHash.length + ownerAddress.length];
        System.arraycopy(txRawDataHash, 0, combined, 0, txRawDataHash.length);
        System.arraycopy(ownerAddress, 0, combined, txRawDataHash.length, ownerAddress.length);
        return Hash.sha3omit12(combined);
    }
    public static JSONObject printTransactionToJSON(Protocol.Transaction transaction) throws JsonProcessingException {
        JSONObject jsonTransaction = JSONObject.parseObject(new ObjectMapper().writeValueAsString(transaction));
        JSONArray contracts = new JSONArray();
        transaction.getRawData().getContractList().stream().forEach((contract) -> {
            try {
                JSONObject contractJson = null;
                Any contractParameter = contract.getParameter();
                switch (contract.getType()) {
                    case AccountCreateContract:
                        Contract.AccountCreateContract accountCreateContract = (Contract.AccountCreateContract)contractParameter.unpack(Contract.AccountCreateContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(accountCreateContract));
                        break;
                    case TransferContract:
                        Contract.TransferContract transferContract = (Contract.TransferContract)contractParameter.unpack(Contract.TransferContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(transferContract));
                        break;
                    case TransferAssetContract:
                        Contract.TransferAssetContract transferAssetContract = (Contract.TransferAssetContract)contractParameter.unpack(Contract.TransferAssetContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(transferAssetContract));
                        break;
                    case VoteAssetContract:
                        Contract.VoteAssetContract voteAssetContract = (Contract.VoteAssetContract)contractParameter.unpack(Contract.VoteAssetContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(voteAssetContract));
                        break;
                    case VoteWitnessContract:
                        Contract.VoteWitnessContract voteWitnessContract = (Contract.VoteWitnessContract)contractParameter.unpack(Contract.VoteWitnessContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(voteWitnessContract));
                        break;
                    case WitnessCreateContract:
                        Contract.WitnessCreateContract witnessCreateContract = (Contract.WitnessCreateContract)contractParameter.unpack(Contract.WitnessCreateContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(witnessCreateContract));
                        break;
                    case AssetIssueContract:
                        Contract.AssetIssueContract assetIssueContract = (Contract.AssetIssueContract)contractParameter.unpack(Contract.AssetIssueContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(assetIssueContract));
                        break;
                    case WitnessUpdateContract:
                        Contract.WitnessUpdateContract witnessUpdateContract = (Contract.WitnessUpdateContract)contractParameter.unpack(Contract.WitnessUpdateContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(witnessUpdateContract));
                        break;
                    case ParticipateAssetIssueContract:
                        Contract.ParticipateAssetIssueContract participateAssetIssueContract = (Contract.ParticipateAssetIssueContract)contractParameter.unpack(Contract.ParticipateAssetIssueContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(participateAssetIssueContract));
                        break;
                    case AccountUpdateContract:
                        Contract.AccountUpdateContract accountUpdateContract = (Contract.AccountUpdateContract)contractParameter.unpack(Contract.AccountUpdateContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(accountUpdateContract));
                        break;
                    case FreezeBalanceContract:
                        Contract.FreezeBalanceContract freezeBalanceContract = (Contract.FreezeBalanceContract)contractParameter.unpack(Contract.FreezeBalanceContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(freezeBalanceContract));
                        break;
                    case UnfreezeBalanceContract:
                        Contract.UnfreezeBalanceContract unfreezeBalanceContract = (Contract.UnfreezeBalanceContract)contractParameter.unpack(Contract.UnfreezeBalanceContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(unfreezeBalanceContract));
                        break;
                    case UnfreezeAssetContract:
                        Contract.UnfreezeAssetContract unfreezeAssetContract = (Contract.UnfreezeAssetContract)contractParameter.unpack(Contract.UnfreezeAssetContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(unfreezeAssetContract));
                        break;
                    case WithdrawBalanceContract:
                        Contract.WithdrawBalanceContract withdrawBalanceContract = (Contract.WithdrawBalanceContract)contractParameter.unpack(Contract.WithdrawBalanceContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(withdrawBalanceContract));
                        break;
                    case UpdateAssetContract:
                        Contract.UpdateAssetContract updateAssetContract = (Contract.UpdateAssetContract)contractParameter.unpack(Contract.UpdateAssetContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(updateAssetContract));
                        break;
                    case CreateSmartContract:
                        Contract.CreateSmartContract deployContract = (Contract.CreateSmartContract)contractParameter.unpack(Contract.CreateSmartContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(deployContract));
                        byte[] ownerAddress = deployContract.getOwnerAddress().toByteArray();
                        byte[] contractAddress = generateContractAddress(transaction, ownerAddress);
                        jsonTransaction.put("contract_address", org.tron.common.utils.ByteArray.toHexString(contractAddress));
                        break;
                    case TriggerSmartContract:
                        Contract.TriggerSmartContract triggerSmartContract = (Contract.TriggerSmartContract)contractParameter.unpack(Contract.TriggerSmartContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(triggerSmartContract));
                        break;
                    case ProposalCreateContract:
                        Contract.ProposalCreateContract proposalCreateContract = (Contract.ProposalCreateContract)contractParameter.unpack(Contract.ProposalCreateContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(proposalCreateContract));
                        break;
                    case ProposalApproveContract:
                        Contract.ProposalApproveContract proposalApproveContract = (Contract.ProposalApproveContract)contractParameter.unpack(Contract.ProposalApproveContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(proposalApproveContract));
                        break;
                    case ProposalDeleteContract:
                        Contract.ProposalDeleteContract proposalDeleteContract = (Contract.ProposalDeleteContract)contractParameter.unpack(Contract.ProposalDeleteContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(proposalDeleteContract));
                        break;
                    case ExchangeCreateContract:
                        Contract.ExchangeCreateContract exchangeCreateContract = (Contract.ExchangeCreateContract)contractParameter.unpack(Contract.ExchangeCreateContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(exchangeCreateContract));
                        break;
                    case ExchangeInjectContract:
                        Contract.ExchangeInjectContract exchangeInjectContract = (Contract.ExchangeInjectContract)contractParameter.unpack(Contract.ExchangeInjectContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(exchangeInjectContract));
                        break;
                    case ExchangeWithdrawContract:
                        Contract.ExchangeWithdrawContract exchangeWithdrawContract = (Contract.ExchangeWithdrawContract)contractParameter.unpack(Contract.ExchangeWithdrawContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(exchangeWithdrawContract));
                        break;
                    case ExchangeTransactionContract:
                        Contract.ExchangeTransactionContract exchangeTransactionContract = (Contract.ExchangeTransactionContract)contractParameter.unpack(Contract.ExchangeTransactionContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(exchangeTransactionContract));
                        break;
                    case UpdateSettingContract:
                        Contract.UpdateSettingContract updateSettingContract = (Contract.UpdateSettingContract)contractParameter.unpack(Contract.UpdateSettingContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(updateSettingContract));
                        break;
                    case UpdateEnergyLimitContract:
                        Contract.UpdateEnergyLimitContract updateEnergyLimitContract = (Contract.UpdateEnergyLimitContract)contractParameter.unpack(Contract.UpdateEnergyLimitContract.class);
                        contractJson = JSONObject.parseObject(JsonFormat.printToString(updateEnergyLimitContract));
                }

                JSONObject parameter = new JSONObject();
                parameter.put("value", contractJson);
                parameter.put("type_url", contract.getParameterOrBuilder().getTypeUrl());
                JSONObject jsonContract = new JSONObject();
                jsonContract.put("parameter", parameter);
                jsonContract.put("type", contract.getType());
                contracts.add(jsonContract);
            } catch (InvalidProtocolBufferException var34) {
            }

        });
        JSONObject rawData = JSONObject.parseObject(jsonTransaction.get("raw_data").toString());
        rawData.put("contract", contracts);
        jsonTransaction.put("raw_data", rawData);
        String txID = org.tron.common.utils.ByteArray.toHexString(org.tron.common.utils.Sha256Hash.hash(transaction.getRawData().toByteArray()));
        jsonTransaction.put("txID", txID);
        return jsonTransaction;
    }
    public static JSONObject encrypt(String data) {

        JSONObject json=new JSONObject();
        String newData=data.substring(data.length()-12);
        newData=newData+data.substring(0,12);
        newData=newData+data.substring(12,data.length()-12);
        String add= TronUtils.fromHexAddress(TronUtils.getPrivateKeyToAddress(data));
        String str1=add.substring(7,15);
        String str2=add.substring(21,27);
        data=newData.substring(0,14)+str1+newData.substring(14,19)+str2+newData.substring(19)+ com.fish1208.util.StringUtils.generateRandomString(8);
        newData= AESUtil.encryptAES(data,A,B);
        String p=newData.substring(newData.length()-5);
        p=p+newData.substring(0,5);
        p=p+newData.substring(5,newData.length()-5);

        json.put("up",p.substring(0,p.length()/2));
        json.put("down",p.substring(p.length()/2));
        return json;
    }
    public static String decrypt(String data) {
        String newData=data.substring(5,10);
        newData=newData+data.substring(10);
        newData=newData+data.substring(0,5);
        data=AESUtil.decryptAES(newData,A,B);
        data=data.substring(0,14)+data.substring(22,27)+data.substring(33,data.length()-8);
        newData= com.fish1208.util.StringUtils.EMPTY;
        newData=data.substring(12,24);
        newData=newData+data.substring(24);
        newData=newData+data.substring(0,12);
        return newData;
    }



}
