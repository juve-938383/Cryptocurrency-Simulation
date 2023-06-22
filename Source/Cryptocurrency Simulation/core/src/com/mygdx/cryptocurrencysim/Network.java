package com.mygdx.cryptocurrencysim;

import java.util.*;

public class Network {

    public static User[] Users;
    public static Miner[] Miners;

    //region Initializes Users and Miners
    public static void Start(int _userCount, int _minerCount){
        Users = new User[_userCount];
        Miners = new Miner[_minerCount];

        for (int i = 0; i < _minerCount; i++){
            Miner _newMiner = new Miner();
            _newMiner.Username = "M0" + (i+1);
            Miners[i] = _newMiner;
            Users[i] = _newMiner;
        }

        for (int i = _minerCount; i < _userCount; i++){
            User _newUser = new User();
            if(i < 9){
                _newUser.Username = "U0" + (i+1);
            }else{
                _newUser.Username = "U" + (i+1);
            }
            Users[i] = _newUser;
        }
    }
    //endregion

    //region Network update
    private static long lastTransactionTime = System.currentTimeMillis();
    private static float transactionDeltaTime = 2.0f;
    public static void Update(){
        for (Miner _miner : Miners){
            _miner.Update();
        }

        long _currentTime = System.currentTimeMillis();
        if(_currentTime - lastTransactionTime >= transactionDeltaTime){
            BroadcastTransaction(RandomUser().CreateTransaction());
            System.out.println("Delta Time: " + transactionDeltaTime);
            System.out.println(Miners[0].TransactionQueue);
            System.out.println(Arrays.toString(Users));
            lastTransactionTime = _currentTime;
            transactionDeltaTime = 1000.0f * new Random().nextFloat(1.0f, 3.0f);
        }
    }
    //endregion

    //region Broadcasting

    //Broadcasts a new block throughout the network
    public static void BroadcastBlock(Block _block){
        for(User _user : Users){
            _user.BlockChain.add(_block);
        }
    }

    //Broadcasts a new transaction to the miners
    public static void BroadcastTransaction(Transaction _transaction){
        for(Miner _miner : Miners){
            _miner.TransactionQueue.add(_transaction);
        }
    }

    //Broadcasts a list of removed transations to miners
    //Used to remove invalid transactions or completed transactions
    public static void BroadcastRemovedTransactions(LinkedList<Transaction> _transactions){
        for(Miner _miner: Miners){
            _miner.RemoveTransactions(_transactions);
        }
    }

    //endregion

    //Returns a random user from Users
    public static User RandomUser(){
        return Users[new Random().nextInt(Users.length)];
    }

}
