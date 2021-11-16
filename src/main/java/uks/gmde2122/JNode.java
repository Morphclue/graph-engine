package uks.gmde2122;

public class JNode {
    private static int lastNodeNumber = 1;
    private int id = lastNodeNumber ++;

    public int getId() {
        return id;
    }

    public JNode putAttribute(String label, Object wolf) {
        return this;
    }
}
