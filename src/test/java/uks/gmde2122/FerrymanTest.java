package uks.gmde2122;

import org.junit.Test;

import java.util.ArrayList;

public class FerrymanTest {

    @Test
    public void testFerrymanProblem() {
        JGraph startGraph = generateStartGraph();

        ArrayList<JRule> ruleList = new ArrayList<>();
        JRule loadCargoRule = new JRule();
        JGraph lhs = loadCargoRule.createLhs();
        JNode lhsBoat = lhs.createNode().putAttribute("label", "boat");
        JNode lhsBank = lhs.createNode().putAttribute("label", "bank");
        JNode lhsCargo = lhs.createNode();
        lhs.createEdge(lhsBoat, "moored", lhsBank);
        lhs.createEdge(lhsCargo, "at", lhsBank);
        ruleList.add(loadCargoRule);

        lhs.draw("loadCargoLhs");

        for (JRule rule : ruleList) {
            MatchTable ruleMatches = rule.findMatches(startGraph);
            System.out.println(ruleMatches);

            MatchTable loadCargoMatches = new MatchTable().setGraph(startGraph).setStartNodes("boat",
                    startGraph.getNodeList().toArray(new JNode[]{}));
            loadCargoMatches.expandForward("boat", "moored", "bank");
            loadCargoMatches.expandBackward("cargo", "at", "bank");
            loadCargoMatches.expandForward("cargo", "likes", "food");
            loadCargoMatches.filterEdge("food", "at", "bank");
            loadCargoMatches.expandAttribute("cargo", "label", "label");
            loadCargoMatches.filterAttribute("label", "goat");
            System.out.println(loadCargoMatches);

            MatchTable rowBoatMatches = new MatchTable().setGraph(startGraph).setStartNodes("boat",
                    startGraph.getNodeList().toArray(new JNode[]{}));
            rowBoatMatches.expandForward("boat", "moored", "bank");
            rowBoatMatches.expandForward("bank", "os", "other");
            System.out.println(rowBoatMatches);

            MatchTable factor1 = new MatchTable().setGraph(startGraph).setStartNodes("eater",
                    startGraph.getNodeList().toArray(new JNode[]{}));

            MatchTable factor2 = new MatchTable().setGraph(startGraph).setStartNodes("food",
                    startGraph.getNodeList().toArray(new JNode[]{}));
            factor1.crossProduct(factor2);
            factor1.filterIso("eater", "food");
            factor1.filterEdge("eater", "likes", "food");
            System.out.println(factor1);
        }
    }

    private JGraph generateStartGraph() {
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
        return startGraph;
    }
}
