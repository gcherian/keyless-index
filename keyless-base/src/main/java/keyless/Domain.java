package keyless;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.Function;

/**
 * Created by georg on 10/17/2016.
 */
public class Domain implements Serializable {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Domain)) return false;

        Domain domain = (Domain) o;

        return id.equals(domain.id);

    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public String id;

    @Override
    public String toString() {
        return "Domain{" +
                "id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", attribute='" + attribute + '\'' +
                ", variability=" + variability +
                ", invariance=" + invariance +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    final long timestamp;
    String attribute;
    long variability;
    long invariance;
    String name;
    String type;

    public Domain() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public Domain(String name) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.name = name;
    }


    static public Function id() {
        return new Function<Domain, String>() {
            @Override
            public String apply(Domain o) {
                return o.id;
            }
        };
    }

    static public Function timestamp() {
        return new Function<Domain, Long>() {
            @Override
            public Long apply(Domain o) {
                return o.timestamp;
            }
        };
    }

    static public Function attribute() {
        return new Function<Domain, String>() {
            @Override
            public String apply(Domain o) {
                return o.attribute;
            }
        };
    }

    static public Function variability() {
        return new Function<Domain, Long>() {
            @Override
            public Long apply(Domain o) {
                return o.variability;
            }
        };
    }

    static public Function invariance() {
        return new Function<Domain, Long>() {
            @Override
            public Long apply(Domain o) {
                return o.invariance;
            }
        };
    }

    static public Function name() {
        return new Function<Domain, String>() {
            @Override
            public String apply(Domain o) {
                return o.name;
            }
        };
    }

    static public Function type() {
        return new Function<Domain, String>() {
            @Override
            public String apply(Domain o) {
                return o.type;
            }
        };
    }

}
