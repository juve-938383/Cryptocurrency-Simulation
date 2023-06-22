package com.mygdx.cryptocurrencysim;

public class Transaction {
    public User Sender;
    public User Receiver;
    public int Quantity;
    public String Signature;
    public long Timestamp;

    //Creates a transaction
    public Transaction(User _sender, User _receiver, int _quantity){
        Sender = _sender;
        Receiver = _receiver;
        Quantity = _quantity;
        Timestamp = System.currentTimeMillis();
        Signature = Sender.GenerateSignature(Receiver, Quantity, Timestamp);

    }

    //Returns transaction data
    @Override
    public String toString() {
        return (Sender.Username + "->" + Receiver.Username + ":" + Quantity);
    }
}
