# Simple DHT
Designed a Simple DHT based on Chord
- Implemented ID space partitioning/re-partitioning
- Implemented Ring-based routing
- Implemented Node joins

1. PA Specification:
https://docs.google.com/document/d/1Y24TKLiBYLHk7hKFgHy2zQJI3UTHkBbNJ7mGYkwyy6E/edit

2. Testing scripts and instructions are provided in the specification document.

#### Chord DHT:
1. Distributed Hash Table using consistent hashing.
2. Orgnaizes nodes in a ring.


#### Algorithm implementing Chord: A Scalable Peer-to-peer Lookup Service for Internet Applications

1. Represent the hash Key space as a virtual ring.
2. Use the SHA-1 hash function that evenly distributes items over hash space.
3. Maps the nodes(5) to buckets in the same ring, i.e a node is responsible for all the hash keys after the previous node's hash.
4. A data item is hashed and stored on the node responsible for the hash (which the succesor node).
5. Each node maintains the successor nodes and any query request is routed from one node to other node in a ring. (Finger tables are not in scope)
6. A node can join the system by requesting the first node of the system which hashes the node to ID space and broadcasts the message to other nodes. (Concurrent node joins are not in scope)
7. The predecessor of the new node and new node updates  the pointer succesor node  and all the keys belonging to the new node are transferred by the new node successor.
8. Node failures are not in scope.
