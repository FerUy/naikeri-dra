#!/bin/bash
trap 'kill -TERM $PID' TERM INT
cd /opt/naikeri/dra/Naikeri-DRA-VERSION/bin/
#rm -rf *.xml

# Console output goes to the terminal and to a file. Anything logged before log4j2.xml is read, and any
# log4j2 configuration error (an unwritable log directory, for instance), appears only here.
# tee runs in a process substitution so that $! below is still the JVM's PID and the traps keep working.
CONSOLE_LOG=../logs/console.out

java -Xms1g -Xmx1g -cp naikeri-diameter-routing-agent-VERSION.jar:libs/* -Dlog4j2.configurationFile=../conf/log4j2.xml -Dorg.restcomm.sctp.bufferSize=50000000 -DmainConfig.path=../conf com.naikeri.sgw.impl.DiameterRoutingAgent > >(tee -a "$CONSOLE_LOG") 2>&1 &
PID=$!
wait $PID
trap - TERM INT
wait $PID
EXIT_STATUS=$?
