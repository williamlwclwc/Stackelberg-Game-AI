AI and Games Semester 2 Project - Group 16  

Our group choose the long report option, however, we also created this agent for your reference.
This is a Stackelberg agent using linear regression and moving window approach to estimate the reaction function 
of the follower based on the 100 pairs of historical data. You may find the details in the report.

To run this agent, simply replace SimpleLeader with Group16Leader:

i) run

/usr/java/latest/bin/rmiregistry &

to enable RMI registration;

ii) run

java -classpath poi-3.7-20101029.jar: -Djava.rmi.server.hostname=127.0.0.1 comp34120.ex2.Main &

to run the GUI of the platform;

iii) run

java -Djava.rmi.server.hostname=127.0.0.1 Group16Leader &

to run the Group16Leader.

If you want to compile the source code, run javac Group16Leader.java
