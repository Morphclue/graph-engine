package uks.gmde2122;

import org.junit.Test;

public class FerrymanTest {
    @Test
    public void testFerrymanProblem() {
        JGraph startGraph = new JGraph();

        JNode wolf = startGraph.createNode().putAttribute("label", "wolf");
        JNode goat = new JNode().putAttribute("label", "goat");
        JNode cabbage = new JNode().putAttribute("label", "cabbage");
        JNode boat = new JNode().putAttribute("label", "boat");
        JNode leftBank = new JNode().putAttribute("label", "bank").putAttribute("side", "left");
        JNode rightBank = new JNode().putAttribute("label", "bank").putAttribute("side", "right");
    }
}
