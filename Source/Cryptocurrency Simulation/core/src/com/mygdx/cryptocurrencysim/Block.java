package com.mygdx.cryptocurrencysim;

import java.util.Arrays;

public class Block {
    public String PreviousBlockHash;
    public Transaction[] TransactionList;
    public String TransactionsHash;
    public String Nonce;
    public String CurrentHash;
    public long Timestamp;

    private final int MINER_REWARD = 3;

    //Creates a block
    public Block(String _previousBlockHash, Transaction[] _transactionList, String _nonce, long _timestamp, Miner _miner){
        PreviousBlockHash = _previousBlockHash;
        TransactionList = _transactionList;
        TransactionsHash = MerkleTree.GenerateHash(_transactionList, System.currentTimeMillis());
        Nonce = _nonce;
        Timestamp = _timestamp;
        CurrentHash = GenerateHash();
        _miner.Balance += MINER_REWARD;
    }

    //Generates the hash of the block
    private String GenerateHash(){
        char[] _finalHash = Nonce.toCharArray();
        char[] _transactionHashChars = TransactionsHash.toCharArray();
        char[] _timestampStr = ("" + Timestamp).toCharArray();
        for(int i = 0; i < 10; i++){
            _transactionHashChars[i] = (char)(Math.abs((_transactionHashChars[i] + _timestampStr[12 - i]) % 75) + 48);
            _finalHash[30 - 1 - i] = (char)(Math.abs((_finalHash[30 - 1 - i] - _timestampStr[12 - i]) % 75) + 48);
        }

        for (int i = 0; i < _transactionHashChars.length * 2; i++){
            _finalHash[i] = (char)(Math.abs((_finalHash[i] + _timestampStr[(i % _timestampStr.length)]) % 75) + 48);
        }

        return new String(_finalHash);
    }

}
