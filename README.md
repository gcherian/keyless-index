Keyless Index
------------------------------

This is a list data structure, which has all the inherent features of a map, or a multimap.


### Problem (Why)
Traditionally data that can be looked up is stored in a map with key and value. Imagine we just have a list of objects, we need to convert this back and forth to a map if we have to lookup this based on a particular attribute in the object.
For example, imagine we have a List of Rooms with certain attributes to each room - id, floorId, houseId, numberOfWindows, color,name, area etc.

    Room room1 = new Room(1,10, 1024,2,"blue","living",700);
    Room room1 = new Room(2,10, 1024,3,"green","dining",500);
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
        int floor = room.getFloorId
        if (diningRoomByFloor.get(floor) != null) {
            List<Room> existing = diningRoomByFloor.get(floor)
            existing.add(room)   
        }else {
            List<Room> new = new ArrayList<>();
            new.add(room);
            diningRoomByFloor.add(room);
        }
        
 If we just have to traverse the diningRoomByFloor, we know it is going to be tedious and boring. This is not only extra effort for the developer, to write these custom key extractors, it is error prone, for example one could get a NullPointerException without the null check above.
 
 
Also considering the types that we used in the list are defined at the compile time, it is not posssible to have different groupings at the runtime without having different compile time structures. For example, if we have to have rooms grouped by name instead of floor, we would need a different data structure and different code to populate this.

Now imagine, we need to have a tower of floors, each floor of houses and houses with rooms. The tower is grouped by floor, the floor by house and house by rooms. We soon see the datastructure geting complicated to navigate and maintain.

    List<Room> house = new ArrayList<>();
    Map<Int,List<Room>> floor = new HashMap<>();
    Map<Int<Map<Int,List<Room>>> tower = new HashMap<>();
    
### Solution (How)
We propose the idea of not having a key, instead have a key extractor lambda as a parameter to constructing the data structure. This way the key is derived from the value. This uses open hashing with linear probing for efficient lookups using an index using an efficient and deterministic hash function.

### Elegance
This makes the storage as simple as possible for every kind of objects, there is no map of map of maps, instead, there is only lists, each element can be an object or a list.

A non unique index is like a multimap, which takes two functions (an index extractor and a pk (primary key) extractor.
A full unique index is like a map, which takes only one function a pk extractor. This way it allows to build a hierarchy of objects.

We have only two types of datastructure FullUniqueIndex (one object per group - like a map) or NonUniqueIndex (one or many objects per group - like a multimap) A NonUniqueIndex will intern keep a FullUniqueIndex if there are more than one objects, this way the programmer do not have special logic to treat one vs many.



### Usage

    FullUniqueIndex rooms = new FullUniqueIndex(Room::getId)
    NonUniqueIndex diningRoomsByFloor = new NonUniqueIndex(Room::getId,Room::getFloorId).filter(r -> r.getName = "dining")
    NonUniqueIndex  tower = new NonUniqueIndex(Room::getId,Room::gethouseId,Room::getFloorId)


### Spark Integration

Spark brings a distributed abstraction of data for inmemory processing, however lacks an API for point lookup. Keyless integrates very well with Spark by providing exactly this feature in Spark. The keyless datastructure is extended to distribuite and  scale by using Spark RDD in the keyless-rdd module. This module uses the keyless-base module which has the code for single JVM. This uses a KeylessRDD which uses a KeylessRDDPartition as a parent/dependency that will hold one index per partition and exposes the same API for the Index. 


### API

    def get(k: T): T
    def getAll(): List[T]
    def multiget(ks: List[T]): List[T] 
    def put(k: T): KeylessRDD[T]
    def multiput(ts: List[T]): KeylessRDD[T]
    
    





