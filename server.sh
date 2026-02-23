# Make sure the Registry is running
if [ $(pgrep -c rmiregistry) -ne 1 ]; then
  echo "[Info] rmiregistry isn't running, starting it for you !"
  ./rmi.sh
fi

# run the client
export CLASSPATH=$CLASSPATH:target/classes/
java server.TchatServer 1099

# Use this one when running the client and server on different machines
# java -Djava.rmi.server.hostname=SERVER_ADDRESS server.TchatServer 1099




