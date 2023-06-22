package com.mygdx.cryptocurrencysim;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

public class Miner extends User implements Runnable{
    public LinkedList<Transaction> TransactionQueue = new LinkedList<>();
    public RunningState State = RunningState.Idle;
    public boolean JustMined = false;

    private final int BLOCK_SIZE = 3;
    private long timeSinceLastAction = System.currentTimeMillis();
    private long lastFrameTime = System.currentTimeMillis();
    private float waitTime = 20.0f;
    private float justMinedTimer = 1000;

    //region Running State Machine
    public void Update(){
        //JustMined signal control
        long _currentTime = System.currentTimeMillis();
        if(JustMined){
            justMinedTimer -= (_currentTime - lastFrameTime);
            if(justMinedTimer < 0){
                JustMined = false;
                justMinedTimer = 1000;
            }
        }
        lastFrameTime = _currentTime;

        //State managements
        if(_currentTime-timeSinceLastAction > waitTime * 1000){
            if(State == RunningState.Idle){
                if(TransactionQueue.size() >= BLOCK_SIZE) {
                    State = RunningState.Mining;
                    System.out.println(Username + " started mining");
                    Thread _thread = new Thread(this);
                    _thread.start();
                }
            }
            else{
                System.out.println(Username + " is resting");
                State = RunningState.Idle;
            }
            waitTime = new Random().nextFloat(7, 15);
            timeSinceLastAction = System.currentTimeMillis();
        }
    }
    //endregion

    //region Mining
    @Override
    public void run() {
        //Wait
        synchronized (Thread.currentThread()) {
            try {
                Thread.currentThread().wait((long) waitTime * 1000);

            } catch (InterruptedException ignored) { }
        }

        //Verify transactions
        LinkedList<Transaction> _selectedTransactions = PickTransactions(BLOCK_SIZE);
        _selectedTransactions = VerifyAllTransactions(_selectedTransactions);
        //Calculate nonce
        Transaction[] _transactions = new Transaction[_selectedTransactions.size()];
        _selectedTransactions.toArray(_transactions);
        String _blockNonce = CalculateNonce(_transactions);

        //Stop if another miner has confirmed these transactions
        for(Transaction _transaction : _selectedTransactions){
            if(!TransactionQueue.contains(_transaction)){
                return;
            }
        }

        //Apply transactions
        for(Transaction _validTransaction : _transactions){
            _validTransaction.Sender.Balance -= _validTransaction.Quantity;
            _validTransaction.Receiver.Balance += _validTransaction.Quantity;
        }
        System.out.println(Username + " finished validating a block");
        Network.BroadcastRemovedTransactions(_selectedTransactions);

        //Create and broadcast next block
        String _previousBlockHash = "";
        if(super.BlockChain.size() > 0){
            _previousBlockHash = super.BlockChain.peekLast().CurrentHash;
        }
        JustMined = true;
        Network.BroadcastBlock(new Block(_previousBlockHash, _transactions, _blockNonce, System.currentTimeMillis(), this));

    }
    //endregion

    //region Transaction validation

    //Try to verify transactions and if any isn't valid, discard it and pick some other transaction
    private  LinkedList<Transaction> VerifyAllTransactions(LinkedList<Transaction> _transactions){
        boolean _transactionsVerified = true;
        LinkedList<Transaction>_transactionsToRemove = new LinkedList<>();
        //Check if all transactions are valid
        for(Transaction _transaction : _transactions){
            if(!VerifyTransaction(_transaction)){
                _transactionsVerified = false;
                _transactionsToRemove.add(_transaction);
            }
        }
        if(_transactionsVerified){
            //Check double spending
            LinkedList<Transaction> _invalidDoubleSpendTransactions = VerifyDoubleSpendTransactions(_transactions);
            if(_invalidDoubleSpendTransactions.size() == 0){
                return _transactions;
            }else{
                _transactionsToRemove.addAll(_invalidDoubleSpendTransactions);
            }

        }
        //Remove invalid transactions
        Network.BroadcastRemovedTransactions(_transactionsToRemove);
        for (Transaction _transaction : _transactionsToRemove) {
            if(_transactions.contains(_transaction)){
                _transactions.remove(_transaction);
            }
        }

        int _missingElementsCount = BLOCK_SIZE - _transactions.size();
        _transactionsToRemove.addAll(_transactions);
        //Refill the list with new transactions and validate them
        for(int i = 0; i < _missingElementsCount; i++){
            _transactions.add(PickTransactions(1, _transactionsToRemove).get(0));
        }
        return VerifyAllTransactions(_transactions);
    }

    //Sometimes, all transactions on the list will be valid on their own but they add up to an invalid sum
    //For example: if user A has 10 units of currency and we are considering a block where A sends B 5 coins and A sends C 6 coins
    //             then both these transactions are valid but the sum is too much for A to afford
    //This function checks for such cases
    private LinkedList<Transaction> VerifyDoubleSpendTransactions(LinkedList<Transaction> _transactions){
        LinkedList<Transaction> _doubleSpendingTransactions = new LinkedList<>();
        //     <PublicKey, Balance>
        HashMap<String,    Integer> _userBalancesDictionary = new HashMap<>();
        for(Transaction _transaction: _transactions){
            if(!_userBalancesDictionary.containsKey(_transaction.Sender.PublicKey)){
                _userBalancesDictionary.put(_transaction.Sender.PublicKey, _transaction.Sender.Balance);
            }
            _userBalancesDictionary.put(_transaction.Sender.PublicKey, _userBalancesDictionary.get(_transaction.Sender.PublicKey) - _transaction.Quantity);
        }
        String _invalidTransactionUserPublicKey = "";
        for(Map.Entry<String, Integer> pair : _userBalancesDictionary.entrySet()){
            if(pair.getValue() <= 0){
                _invalidTransactionUserPublicKey = pair.getKey();
            }
        }

        if(!_invalidTransactionUserPublicKey.equals("")) {
            int i = 0;
            while (_doubleSpendingTransactions.size() < 3 - _userBalancesDictionary.size()) {
                if (_transactions.get(i).Sender.PublicKey.equals(_invalidTransactionUserPublicKey)) {
                    _doubleSpendingTransactions.add(_transactions.get(i));
                }
                i++;
            }
        }
        return  _doubleSpendingTransactions;

    }

    //Checks that the sender can afford the transaction and verifies its signature
    private boolean VerifyTransaction(Transaction _transaction){
        return ( (_transaction.Sender.Balance > _transaction.Quantity) && VerifySignature(_transaction));
    }

    //Verifies sender signature
    private boolean VerifySignature(Transaction _transaction){
        return (_transaction.Signature.contentEquals(_transaction.Sender.GenerateSignature(_transaction.Receiver, _transaction.Quantity, _transaction.Timestamp,_transaction.Sender.PublicKey)));
    }

    //Picks the next transaction in queue
    private LinkedList<Transaction> PickTransactions(int _count){
        LinkedList<Transaction> _transactionsList = new LinkedList<>();
        for(int i = 0; i < _count; i++){
            _transactionsList.add(TransactionQueue.get(i));
        }
        return _transactionsList;
    }

    //Picks the next transaction in queue, skipping the invalid ones
    private LinkedList<Transaction> PickTransactions(int _count, LinkedList<Transaction> _ignoreTransactions){
        LinkedList<Transaction> _transactionsList = new LinkedList<>();
        int i = 0;
        while(i < _count){
            Transaction _transaction = TransactionQueue.get(i);
            if(_ignoreTransactions.contains(_transaction)){
                _count++;
            }else {
                _transactionsList.add(_transaction);
            }
            i++;
        }
        return _transactionsList;
    }


    //endregion

    //Calculates nonce for the block
    private String CalculateNonce(Transaction[] _transactions){
        int[] _mixedSignatures = new int[30];
        char[][] _charSignatures = {_transactions[0].Signature.toCharArray(),
                                    _transactions[1].Signature.toCharArray(),
                                    _transactions[2].Signature.toCharArray()};

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 3; j++) {
                _mixedSignatures[10 * j + i] = _charSignatures[j][i];
            }
        }

        StringBuilder _nonce = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            _nonce.append(Math.abs(_mixedSignatures[i] - _mixedSignatures[29 - i]) + 48);
        }
        return _nonce.toString();

    }

    //Remove invalid or completed transactions
    public void RemoveTransactions(LinkedList<Transaction> _transactions){
        for(Transaction _t: _transactions){
            TransactionQueue.remove(_t);
        }

    }

    //Miner state
    public enum RunningState {
        Idle,
        Mining
    }
}
