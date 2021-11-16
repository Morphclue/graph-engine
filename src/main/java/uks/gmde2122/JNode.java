package uks.gmde2122;

import java.util.ArrayList;

public class JNode {
    private static int lastNodeNumber = 1;
    private int id = lastNodeNumber++;
    private ArrayList<Object> properties;

    public int getId() {
        return id;
    }

    public JNode putAttribute(String attribute, Object value) {
        if (properties == null) {
            this.properties = new ArrayList<>();
        }
        int i = getIndex(attribute);
        if (i >= 0) {
            this.properties.set(i + 1, value);
        } else {
            this.properties.add(attribute);
            this.properties.add(value);
        }
        return this;
    }

    private int getIndex(String attribute) {
        if (properties == null) {
            return -1;
        }
        for (int i = 0; i < properties.size(); i += 2) {
            if (properties.get(i).equals(attribute)) {
                return i;
            }
        }
        return -1;
    }
}
