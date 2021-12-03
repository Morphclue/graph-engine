package uks.gmde2122;

import java.util.ArrayList;

public class JNode {
    private static int lastNodeNumber = 1;
    private int id = lastNodeNumber++;

    private ArrayList<Object> attributesList;

    public int getId() {
        return id;
    }

    public ArrayList<Object> getAttributesList() {
        return attributesList;
    }

    public Object getAttributeValues(String attributeName) {
        for (int i = 0; i < attributesList.size(); i += 2) {
            if (attributesList.get(i).equals(attributeName)) {
                return attributesList.get(i + 1);
            }
        }
        return null;
    }

    public JNode putAttribute(String attribute, Object value) {
        if (attributesList == null) {
            this.attributesList = new ArrayList<>();
        }
        int i = getIndex(attribute);
        if (i >= 0) {
            this.attributesList.set(i + 1, value);
        } else {
            this.attributesList.add(attribute);
            this.attributesList.add(value);
        }
        return this;
    }

    private int getIndex(String attribute) {
        if (attributesList == null) {
            return -1;
        }
        for (int i = 0; i < attributesList.size(); i += 2) {
            if (attributesList.get(i).equals(attribute)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    // no fulib
    public String toString() {
        String label = (String) getAttributeValues("label");
        if(label == null){
            label = "JNode";
        }

        return String.format("%d:%s", getId(), label);
    }
}
