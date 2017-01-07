package keyless;

import keyless.index.FullUniqueIndex;
import keyless.index.NonUniqueIndex;

import java.util.Iterator;

/**
 * Created by gcherian on 12/24/2016.
 */


public class TestIndex {

    public void testFullUniqueIndexPerformance() {
        FullUniqueIndex fullUniqueIndex = new FullUniqueIndex(Domain.id());

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            Domain d = new Domain();
            d.name = "name-" + i;
            fullUniqueIndex.put(d);
        }
        long end = System.currentTimeMillis();

        System.out.println("FUI Size = " + fullUniqueIndex.size() + ", Time = " + (end - start));


    }

    public void testNonUniqueIndexPerformance() {

        NonUniqueIndex nonUniqueIndex = new NonUniqueIndex(Domain.id(), Domain.name());


        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            Domain d = new Domain();
            d.name = "name-" + i;
            nonUniqueIndex.put(d);
        }
        long end = System.currentTimeMillis();
        System.out.println("NUI Size = " + nonUniqueIndex.size() + ", Time = " + (end - start));


    }

    public void testFullUniqueIndexIterator() {

        FullUniqueIndex<Domain> fullUniqueIndex = new FullUniqueIndex(Domain.id());

        for (int i = 0; i < 10; i++) {
            Domain d = new Domain();
            d.name = "name-" + i;
            fullUniqueIndex.put(d);
        }

        int i = 0;
        Iterator<Domain> iterator = fullUniqueIndex.iterator();
        while (iterator.hasNext()) {
            Domain next = iterator.next();
            System.out.println("Size " + fullUniqueIndex.size() + " Cursor " + i++ + " Value " + next.name);
        }

    }

    public void testNonUniqueIndexIterator() {

        NonUniqueIndex<Domain> nonUniqueIndex = new NonUniqueIndex<Domain>(Domain.id(), Domain.name());

        for (int i = 0; i < 10; i++) {
            Domain d = new Domain();
            d.name = "name-" + i % 2;
            nonUniqueIndex.put(d);
        }

        int i = 0;
        Iterator<Domain> iterator = nonUniqueIndex.iterator();
        while (iterator.hasNext()) {
            Domain next = iterator.next();
            System.out.println("Size " + nonUniqueIndex.size() + " Cursor " + i++ + " Value " + next.name);
        }

    }

}
