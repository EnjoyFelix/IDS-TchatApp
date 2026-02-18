# Compile and run the Registry
# maven compiles to the target
mvn clean compile
export CLASSPATH=$CLASSPATH:target/classes/
rmiregistry 1099 &