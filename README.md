Keyless Index
------------------------------

This is a list data structure, which has all the inherent features of a map, or a multimap.


### Problem (Why)
Traditionally data that can be looked up is stored in a map with key and value. Imagine we just have a list of objects, we need to convert this back and forth to a map if we have to lookup this based on a particular attribute in the object.
For example, imagine we have a List of Rooms with certain attributes to each room - id, houseId, numberOfWindows, color,name, area etc.
    Room room1 = new Room(1,1024,2,"blue","living",700);
    Room room1 = new Room(2,1024,3,"green","dining",500);
We now want to group the rooms together into houses, without introducing a new object.
    List<Room> house = new ArrayList<Room>(room1,room2);
Now suppose we want to abstract the floor as a list of houses.
    List<List<Room>> floor = new ArrayList<ArrayList<Room>>(house);
Imagine we have to find all the dining room in the floor, say to do some maintenance. And we can already see nested for loops with conditions to achieve this.

    List<Room> diningRooms = new ArrayList<>();
    for (List<Room> house: floor) {
        for (Room room : house) {
            if (room.name =="dining")
                diningRooms.add(room);
        }
    }
If the maintenance has to be done per floor, we would need to group by floorId (first two digits of houseId), we know how tedious it becomes for this simple grouping with a multimap.
    Map<Int,Room> diningRoomByFloor = new HashMap<Int,List<Room>>();
    for (Room room: diningRooms) {
        int floor = room.houseId/100;
        if (diningRoomByFloor.get(floor) != null) {
            List<Room> existing = diningRoomByFloor.get(floor)
            existing.add(room)   
        }else {
            List<Room> new = new ArrayList<>();
            new.add(room);
            diningRoomByFloor.add(room);
        }
 If we just have to traverse the diningRoomByFloor, we know it is going to be tedious and boring. This is not only extra effort for the developer, to write these custom key extractors, it mandates a new compile time structure for every attribute you want to use as a key in the map.

### Solution (How)
We propose the idea of not having a key, instead have a key extractor lambda as a parameter to constructing the data structure. This way the key is derived from the value. This uses open hashing with linear probing for efficient lookups using an index using an efficient and deterministic hash function.

### Elegance
This makes the storage as simple as possible for every kind of objects, there is no map of map of maps, instead, there is only lists, each element can be an object or a list.

A non unique index is like a multimap, which takes two functions (an index extractor and a pk (primary key) extractor.
A full unique index is like a map, which takes only one function a pk extractor. This way it allows to build a hierarchy of objects.

We have only two types of datastructure FullUniqueIndex (one object per group - like a map) or NonUniqueIndex (one or many objects per group - like a multimap) A NonUniqueIndex will intern keep a FullUniqueIndex if there are more than one objects, this way the programmer do not have special logic to treat one vs many.


### Usage

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

    FullUniqueIndex fui = nui.get(new Domain(1,null,null))
    Domain audioAmplitute = fui.get(new Domain(1,null,null))
    //audioAmplitude = Domain(1,"Audio","Pitch")

### Hierarchy


### Extension

The datastructure is extended to distribuite and  scale by using Spark RDD in the keyless-rdd module. This module uses the keyless-base module which has the code for single JVM.




