# IoT-Cluster-Simulation-and-Analysis
IoT Cluster Simulation and Analysis

## Context

This project goal was to simulate a cluster of IoT devices capable of offloading tasks. There were three different parts: 
* Slave node implementation; 
* Master node implementation; 
* System Integration. 

The **slave** should:

1. Report the availability for receiving and processing tasks.
2. Report the performance index (considered the number of threads).
3. Given a collection of numbers, it should apply operations and return the result and receive requests for reporting the performance index or code execution and be able to reply. 

The **master** node:
1. Has to know which slave nodes (an arbitrary number of slaves) are available from a given list. 
2. Given a simple mathematical problem to solve, it should be able to split that problem in parts which are proportional to the reported performance index of the nodes (load balancing).
3. It has to communicate with the slaves and gather results. If some node stops responding, the master should be able to reschedule the task with maximum priority to another node(s).

The communication between Master and Slaves was made with POST and GET requests, tested with Postman and with a console (simple UI).