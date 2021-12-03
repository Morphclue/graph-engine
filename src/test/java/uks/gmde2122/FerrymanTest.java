package uks.gmde2122;

import org.junit.Test;

public class FerrymanTest {
    @Test
    public void testFerrymanProblem() {
        JGraph startGraph = new JGraph();

        JNode wolf = startGraph.createNode().putAttribute("label", "wolf");
        JNode goat = startGraph.createNode().putAttribute("label", "goat");
        JNode cabbage = startGraph.createNode().putAttribute("label", "cabbage");
        JNode boat = startGraph.createNode().putAttribute("label", "boat");
        JNode leftBank = startGraph.createNode()
                .putAttribute("label", "bank")
                .putAttribute("side", "left");
        JNode rightBank = startGraph.createNode()
                .putAttribute("label", "bank")
                .putAttribute("side", "right");

        startGraph.createEdge(wolf, "at", leftBank);
        startGraph.createEdge(goat, "at", leftBank);
        startGraph.createEdge(cabbage, "at", leftBank);
        startGraph.createEdge(boat, "moored", leftBank);
        startGraph.createEdge(wolf, "likes", goat);
        startGraph.createEdge(goat, "likes", cabbage);
        startGraph.createEdge(leftBank, "os", rightBank);
        startGraph.createEdge(rightBank, "os", leftBank);

        startGraph.draw("start");

        MatchTable loadCargoMatches = new MatchTable().setGraph(startGraph).setStartNode(boat);
        loadCargoMatches.expandForward("start", "moored", "bank");
        loadCargoMatches.expandBackward("cargo", "at", "bank");
        loadCargoMatches.expandForward("cargo", "likes", "food");
        loadCargoMatches.filterEdge("food", "at", "bank");
        loadCargoMatches.expandAttribute("cargo", "label", "label");
        loadCargoMatches.filterAttribute("label", "goat");
        System.out.println(loadCargoMatches);

        MatchTable rowBoatMatches = new MatchTable().setGraph(startGraph).setStartNode(boat);
        rowBoatMatches.expandForward("start", "moored", "bank");
        rowBoatMatches.expandForward("bank", "os", "other");
        System.out.println(rowBoatMatches);
    }
}
