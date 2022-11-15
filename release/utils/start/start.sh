#!/bin/bash
trap 'kill -TERM $PID' TERM INT
cd /opt/naikeri/dra/Naikeri-DRA-VERSION/bin/
#rm -rf *.xml
java -Xms1g -Xmx1g -cp naikeri-diameter-routing-agent-VERSION.jar:libs/* -Dlogback.configurationFile=../conf/logback.xml -Dorg.restcomm.sctp.bufferSize=50000000 -DmainConfig.path=../conf com.naikeri.sgw.impl.DiameterRoutingAgent &
PID=$!
wait $PID
trap - TERM INT
wait $PID
EXIT_STATUS=$?

