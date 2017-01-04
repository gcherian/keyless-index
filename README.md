Keyless Index
------------------------------

This is a list data structure, which has all the inherent features of a map, or a multimap.

### Problem
Traditionally data that can be looked up is stored in a map with key and value. Imagine we just have a list of objects, we need to convert this back and forth to a map if we have to lookup this based on a particular attribute in the object.
This is not only extra effort for the developer, to write these custom key extractors, it mandates a new compile time structure for every attribute you want to use as a key in the map.

###Solution
We propose the idea of not having a key, instead have a key extractor lambda as a parameter to constructing the data structure. This way the key is derived from the value.
This uses open hashing with linear probing for efficient lookups using an index using an efficient and deterministic hash function.

###Elegance
This makes the storage as simple as possible for every kind of objects, there is no map of map of maps, instead, there is only lists, each element can be an object or a list.

A non unique index is like a multimap, which takes two functions (an index extractor and a pk (primary key) extractor.
A full unique index is like a map, which takes only one function a pk extractor. This way it allows to build a hierarchy of objects.



###Usage

    NonUniqueIndex nui = new NonUniqueIndex(Domain::getId,Domain::getName)
    Domain audioAmplitude =  new Domain(1,"Audio","Pitch")
    Domain audioFrequency =  new Domain(2,"Audio","Spectral")
    Domain visualColor = new Domain(3,"Visual","Color")
    
    nui.put(audioAmplitude)
    nui.put(audioFrequency)
    nui.put(visualColor)

    +--------------------------------+
    | audioAmplitude | audioFrequency|
    +--------------------------------+
    |            visualColor         |
    +--------------------------------+

    FullUniqueIndex fui = nui.get(george1)
    Person george2 = fui.get(george2)

###Storage

The Non Unique Index  is just another list of Full Unique Indices.In the above example:
- on index 0 we have a Full Unique Index with two george objects.
- on index 1 we have an single object cherian, as it is unique by index key.

###Extension

The datastructure is extended to distribuite and  scale by using Spark RDD in the keyless-rdd module. This module uses the keyless-base module which has the code for single JVM.




