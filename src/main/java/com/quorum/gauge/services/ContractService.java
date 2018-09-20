package com.quorum.gauge.services;

import com.quorum.gauge.common.QuorumNode;
import com.quorum.gauge.sol.SimpleStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;
import org.web3j.tx.Contract;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.exceptions.ContractCallException;

import java.math.BigInteger;
import java.util.Arrays;

@Service
public class ContractService extends AbstractService {

    @Autowired
    PrivacyService privacyService;

    @Autowired
    AccountService accountService;

    public Contract createSimpleContract(QuorumNode source, QuorumNode target) {
        Quorum client = connectionFactory.getConnection(source);
        ClientTransactionManager clientTransactionManager = new ClientTransactionManager(
                client,
                accountService.getFirstAccountAddress(source),
                Arrays.asList(privacyService.id(target)));

        try {
            SimpleStorage contract = SimpleStorage.deploy(client,
                    clientTransactionManager,
                    BigInteger.valueOf(0),
                    new BigInteger("47b760", 16),
                    BigInteger.valueOf(42)).send();
            return contract;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // Read-only contract
    public int readSimpleContractValue(QuorumNode node, String contractAddress) {
        Quorum client = connectionFactory.getConnection(node);
        String address;
        try {
            address = client.ethCoinbase().send().getAddress();
            ReadonlyTransactionManager txManager = new ReadonlyTransactionManager(client, address);
            return SimpleStorage.load(contractAddress, client, txManager,
                    BigInteger.valueOf(0),
                    new BigInteger("47b760", 16)).get().send().intValue();
        } catch (ContractCallException cce) {
            if (cce.getMessage().contains("Empty value (0x)")) {
                return 0;
            }
            throw new RuntimeException(cce);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TransactionReceipt updateSimpleContract(QuorumNode source, QuorumNode target, String contractAddress, int newValue) {
        Quorum client = connectionFactory.getConnection(source);
        String address;
        try {
            address = accountService.getFirstAccountAddress(source);
            ClientTransactionManager txManager = new ClientTransactionManager(
                    client,
                    address,
                    Arrays.asList(privacyService.id(target)));
            return SimpleStorage.load(contractAddress, client, txManager,
                    BigInteger.valueOf(0),
                    new BigInteger("47b760", 16)).set(BigInteger.valueOf(newValue)).send();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
