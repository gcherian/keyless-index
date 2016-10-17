package keyless;

import keyless.index.FullUniqueIndex;
import keyless.index.NonUniqueIndex;

public class Main {

    public static void main(String[] args) {
        FullUniqueIndex fullUniqueIndex = new FullUniqueIndex(Domain.id());
        Domain domain1 = new Domain();
        domain1.name = "Visual";

        Domain domain2 = new Domain();
        domain2.name = "Audio";

        fullUniqueIndex.put(domain1);
        fullUniqueIndex.put(domain2);

        Domain domain10 = (Domain) fullUniqueIndex.get(domain1);
        System.out.println(domain1.id + " == " + domain10.id);

        Domain domain20 = (Domain) fullUniqueIndex.get(domain2);
        System.out.println(domain2.id + " == " + domain20.id);

        Domain domain3 = new Domain();
        domain3.name = domain2.name;

        NonUniqueIndex nonUniqueIndex = new NonUniqueIndex(Domain.id(), Domain.name());
        nonUniqueIndex.put(domain1);
        nonUniqueIndex.put(domain2);
        nonUniqueIndex.put(domain3);

        Domain domain100 = (Domain) nonUniqueIndex.get(domain1);
        System.out.println(domain1.id + " == " + domain100.id);

        FullUniqueIndex index23 = (FullUniqueIndex) nonUniqueIndex.get(domain2);

        Domain domain200 = (Domain) index23.get(domain2);
        System.out.println(domain2.id + " == " + domain200.id);

        Domain domain300 = (Domain) index23.get(domain3);
        System.out.println(domain3.id + " == " + domain300.id);






    }
}

