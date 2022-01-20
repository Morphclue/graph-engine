package uks.gmde2122;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FerrymanTest {

    @Test
    public void testFerrymanProblem() {
        JGraph startGraph = generateStartGraph();

        ArrayList<JRule> ruleList = new ArrayList<>();

        JGraph lhs;
        JRule eatRule = new JRule().setName("eatRule");
        lhs = eatRule.createLhs();
        JNode lhsEater = lhs.createNode();
        JNode lhsFood = lhs.createNode();
        JNode lhsBank = lhs.createNode().putAttribute("label", "bank");
        lhs.createEdge(lhsEater, "likes", lhsFood);
        lhs.createEdge(lhsEater, "at", lhsBank);
        lhs.createEdge(lhsFood, "at", lhsBank);
        eatRule.setFilterLambda(this::eatRuleLambdaNoBoat);
        ruleList.add(eatRule);

        JRule loadCargoRule = new JRule().setName("loadCargoRule");
        lhs = loadCargoRule.createLhs();
        JNode lhsBoat = lhs.createNode().putAttribute("label", "boat");
        lhsBank = lhs.createNode().putAttribute("label", "bank");
        JNode lhsCargo = lhs.createNode();
        lhs.createEdge(lhsBoat, "moored", lhsBank);
        lhs.createEdge(lhsCargo, "at", lhsBank);
        loadCargoRule.setApplyLambda(this::loadCargoApply);
        ruleList.add(loadCargoRule);

        lhs.draw("loadCargoLhs");

        int nextGraphNumber = 1;

        LinkedHashMap<String, JGraph> certificateMap = new LinkedHashMap<>();
        String startCertificate = startGraph.computeCertificate();
        System.out.println("Start-Certificate = \n" + startCertificate);
        certificateMap.put(startCertificate, startGraph);

        JGraph ltsGraph = new JGraph();
        ltsGraph.getNodeList().add(startGraph);

        ArrayList<JGraph> todo = new ArrayList<>();
        todo.add(startGraph);
        startGraph.putAttribute("label", "startGraph");

        while (!todo.isEmpty()) {
            startGraph = todo.remove(0);

            for (JRule rule : ruleList) {
                MatchTable ruleMatches = rule.findMatches(startGraph);

                for (ArrayList<Object> row : ruleMatches.getTable()) {
                    JGraph cloneGraph = (JGraph) startGraph.clone();
                    cloneGraph.draw("cloneGraphBefore" + nextGraphNumber);

                    ArrayList<Object> cloneRow = new ArrayList<>();
                    for (Object origin : row) {
                        if (origin instanceof JNode) {
                            int originIndex = startGraph.getNodeList().indexOf(origin);
                            JNode cloneNode = cloneGraph.getNodeList().get(originIndex);
                            cloneRow.add(cloneNode);
                        } else {
                            cloneRow.add(origin);
                        }
                    }

                    rule.apply(new ApplyRuleParams()
                            .setHostGraph(cloneGraph)
                            .setRule(rule)
                            .setColumnNames(ruleMatches.getColumnNames())
                            .setRow(cloneRow));
                    cloneGraph.putAttribute("label", "cloneGraph" + nextGraphNumber);
                    cloneGraph.draw("cloneGraph" + nextGraphNumber++);

                    ltsGraph.getNodeList().add(cloneGraph);
                    ltsGraph.createEdge(startGraph, rule.getName(), cloneGraph);

                    todo.add(cloneGraph);
                }
            }
        }

        ltsGraph.draw("ltsGraph");
        ltsGraph.draw("ltsGraphExplode", true);
    }

    private void loadCargoApply(ApplyRuleParams params) {
        JNode lhsBoat = params.getRule().getLhs().getNodeList().get(0);
        JNode lhsBank = params.getRule().getLhs().getNodeList().get(1);
        JNode lhsCargo = params.getRule().getLhs().getNodeList().get(2);

        int boatIndex = params.getColumnNames().indexOf(lhsBoat.toString());
        int bankIndex = params.getColumnNames().indexOf(lhsBank.toString());
        int cargoIndex = params.getColumnNames().indexOf(lhsCargo.toString());

        JNode hostBoat = (JNode) params.getRow().get(boatIndex);
        JNode hostBank = (JNode) params.getRow().get(bankIndex);
        JNode hostCargo = (JNode) params.getRow().get(cargoIndex);

        params.getHostGraph().removeEdge(hostCargo, "at", hostBank);
        params.getHostGraph().createEdge(hostCargo, "in", hostBoat);
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

    private void eatRuleLambdaNoBoat(JRule rule, MatchTable matchTable) {
        JNode lhsBank = rule.getLhs().getNodeList().get(2);
        int bankIndex = matchTable.getColumnNames().indexOf(lhsBank.toString());
        if (bankIndex < 0) {
            return;
        }

        ArrayList<ArrayList<Object>> resultTable = new ArrayList<>();
        ArrayList<Object> edgeList = matchTable.getGraph().getEdgeList();
        rowLoop:
        for (ArrayList<Object> row : matchTable.getTable()) {
            JNode bank = (JNode) row.get(bankIndex);
            for (int i = 0; i < edgeList.size(); i += 3) {
                JNode source = (JNode) edgeList.get(i);
                String label = (String) edgeList.get(i + 1);
                JNode target = (JNode) edgeList.get(i + 2);

                if (label.equals("moored") && bank == target) {
                    continue rowLoop;
                }
            }
            resultTable.add(row);
        }

        boolean change = matchTable.getTable().size() != resultTable.size();
        matchTable.table = resultTable;
        if (change) {
            System.out.println(matchTable);
        }
    }
}
