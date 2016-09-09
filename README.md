Keyless Index
------------------------------

### Problem
Traditionally data that can be looked up is stored in a map with key and value. Imagine we just have a list of objects, we need to convert this back and forth to a map if we have to lookup this based on a particular attribute in the object.
This is not only extra effort for the developer, to write these custom key extractors, it mandates a new compile time structure for every attribute you want to use as a key in the map.

###Solution
We propose the idea of not having a key, instead have a key extractor lambda as a parameter to constructing the data structure.
This uses open hashing with linear probing for efficient lookups using an index using an efficient and deterministic hash function.