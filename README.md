# Cryptocurrency-Simulation
 A Bitcoin-based cryptocurrency simulation in Java for a school project.
 
# Goal
 The aim of this project is the creation of a simulation of a network of cryptocurrency exchange based on the blockchain technology and, specifically, the model of Bitcoin, in Java.
 Some specific features of this model have to be: the lack of a central entity for the control and distribution of cryptocurrency; the possibility for entities (“users”) to try to spend more currency than they own; the validation of transactions by the validators (“miners”); the miners work in parallel on separate threads.
 
# Result
 In the end, I created a real-time simulation of such transaction network as described above. In addition, this network was accompanied by a visual representation of the most interesting features: a chart of the live balances of the users; the miners’ working status; the transaction ledger/queue (up to 5 elements); the blockchain (up to 5 elements).
 There is no central entity for the control and distribution of the currency. The users can spend up to their balance, however, there exists the possibility for them to spend multiple times from this balance before their transactions have been validated, making for a double-(or more)-spending scenario.
The miners work in parallel on multiple threads and validate the transactions by checking two factors:
1. the legitimacy of the transaction, that is, that the transaction has been signed by the sender user’s private key, by checking against their public key;
2. whether the sender user can afford the transaction (avoiding in this way double-spending).
In addition, the miners have mechanisms to check that the transactions they are currently validating have not been already validated by other miners (that is, are not already in the blockchain). In this way, we avoid blockchain branching.

# Usage
In order to run the builds of the project:
- donload the build;
- unzip the file inside ```Build-[version]```;
- run ```Cryptocurrency Sim.exe```;
- if you want to run the jar, use ```java -jar dasktop-1.0.jar``` in the directory where the file is located;

![Image of the program running](https://github.com/juve-938383/Cryptocurrency-Simulation/blob/main/Source/Cryptocurrency%20Simulation/assets/Simulation%20Example.png?raw=true)

©juve938383 All rights reserved
