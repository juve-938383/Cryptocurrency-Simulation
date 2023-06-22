package com.mygdx.cryptocurrencysim;

import java.util.Arrays;

public class MerkleTree {
    public static final String ALPHANUMERIC = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    //Generates "silly" hashing for a list of transactions
    //by recursively splitting it down to a binary tree and calculating the hashes of the nodes
    //and returning the hash of the root
    public static String GenerateHash(Transaction[] _transactions, long _timestamp){
        int _transactionsCount = _transactions.length;
        if(_transactionsCount == 1){
            return _transactions[0].Signature;
        }
        int _splitIndex = (int) Math.floor(_transactionsCount/2.0f);
        char[] _leftHash = GenerateHash(Arrays.copyOfRange(_transactions, 0, _splitIndex), _timestamp).toCharArray();
        char[] _rightHash = GenerateHash(Arrays.copyOfRange(_transactions, _splitIndex, _transactionsCount), _timestamp).toCharArray();

        StringBuilder _finalHash = new StringBuilder();
        char[] _timestampStr = ("" + _timestamp).toCharArray();
        for(int i = 0; i < _leftHash.length; i++){
            int _mashedChar = _leftHash[i] - (_rightHash[_rightHash.length-1] * Integer.parseInt("" + _timestampStr[12-i]));
            _finalHash.append((char)(Math.abs(_mashedChar % 75) + 48));
        }
        return _finalHash.toString();
    }
}
