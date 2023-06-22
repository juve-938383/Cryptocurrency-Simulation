package com.mygdx.cryptocurrencysim;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class User {
    public String Username = "";
    public int Balance = 30;
    public String PublicKey = "";
    private String privateKey = "";
    public LinkedList<Block> BlockChain = new LinkedList<>();

    private final int KEY_LENGTH = 10;

    //region Initialization
    public User(){
        GenerateKeys();
    }

    //Generate "silly" public/private keys
    private void GenerateKeys(){
        Random random = new Random();
        StringBuilder _tempPublicKey = new StringBuilder();
        for( int i = 0; i < KEY_LENGTH; i++){
            _tempPublicKey.append(MerkleTree.ALPHANUMERIC.charAt(random.nextInt(62)));
        }
        PublicKey = _tempPublicKey.toString();
        privateKey = new StringBuilder(PublicKey).reverse().toString();
    }
    //endregion

    //region Signatures

    //Generates "silly" digital signature
    //Used to verify the signature using PublicKey
    public String GenerateSignature(User _receiver, int _quantity, long _timestamp, String _key){
        char[] _senderKeyChars = _key.toCharArray();
        Arrays.sort(_senderKeyChars);
        char[] _receiverKeyChars = _receiver.PublicKey.toCharArray();
        char[] _timestampStr = ("" + _timestamp).toCharArray();
        for (int i = 0; i < _senderKeyChars.length; i++) {
            _senderKeyChars[i] = (char) Math.abs(_senderKeyChars[i] - _receiverKeyChars[i] + _timestampStr[12 - i]);
            _senderKeyChars[i] = (char) (((_senderKeyChars[i] * _quantity) % 75) + 48);
        }
        return new String(_senderKeyChars);
    }

    //Generates "silly" digital signature
    //Used to initially generate the signature, using privateKey
    public String GenerateSignature(User _receiver, int _quantity, long _timestamp){
        return GenerateSignature(_receiver, _quantity, _timestamp, privateKey);
    }
    //endregion

    //Creates a random transaction
    public Transaction CreateTransaction(){
        Random random = new Random();
        User _receiver = Network.RandomUser();
        if (_receiver == this)
            return CreateTransaction();
        return new Transaction(this, _receiver, random.nextInt(1, (int) (1 + Math.ceil(Balance / (float)random.nextInt(1, 4)))));
    }

    @Override
    public String toString(){
        return(Username + ": " + Balance);
    }
}
