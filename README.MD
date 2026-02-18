# IDS TchatApp - Group F 
> This repository was previously on the im2ag's gitlab, but it unfortunately went readonly on 17/02/26. 

## Members
- Chemin Elisée <elisee.chemin@etu.univ-grenoble-alpes.fr>
- Magieu Pierre <magieup@etu.univ-grenoble-alpes.fr>

## How to run
> This section is a summary of the report's.

```bash
# You can manually compile the sources using
mvn clean compile

# To run the rmi registry :
# It compiles the sources and runs the registry on port 1099 
./rmi.sh

# To run the server :
# It checks if the rmi registry is running 
# before running the server using port 1099
./server.sh

# To run a client :
# It uses localhost:1099 as the registry's address.
./client.sh
```