package uks.gmde2122;

import org.junit.Test;

import java.util.ArrayList;
import java.util.TreeMap;

public class FerrymanTest {

    @Test
    public void testFerrymanProblem() {
        JGraph startGraph = generateStartGraph();

        int nextGraphNumber = 1;

        TreeMap<String, JGraph> certificateMap = new TreeMap<>();
        String startCertificate = startGraph.computeCertificate();
        System.out.println("Start-Certificate = \n" + startCertificate);
        certificateMap.put(startCertificate, startGraph);

        JGraph ltsGraph = new JGraph();
        ltsGraph.getNodeList().add(startGraph);

        ArrayList<JGraph> todo = new ArrayList<>();
        todo.add(startGraph);
        startGraph.putAttribute("label", fmpGraphLabel(startGraph));

        while (!todo.isEmpty()) {
            startGraph = todo.remove(0);

            for (JRule rule : buildRules()) {
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
                    cloneGraph.putAttribute("label", cloneGraph.toString());
                    cloneGraph.draw("cloneGraph" + nextGraphNumber++);

                    String newCertificate = cloneGraph.computeCertificate();
                    JGraph oldGraph = certificateMap.get(newCertificate);
                    if (oldGraph != null) {
                        ltsGraph.createEdge(startGraph, rule.getName(), oldGraph);
                    } else {
                        todo.add(cloneGraph);
                        certificateMap.put(newCertificate, cloneGraph);
                        ltsGraph.getNodeList().add(cloneGraph);
                        ltsGraph.createEdge(startGraph, rule.getName(), cloneGraph);
                    }

                    ltsGraph.getNodeList().add(cloneGraph);
                    ltsGraph.createEdge(startGraph, rule.getName(), cloneGraph);

                    todo.add(cloneGraph);
                }
            }
        }

        ltsGraph.draw("ltsGraph");
        ltsGraph.draw("ltsGraphExplode", true);
    }

    private ArrayList<JRule> buildRules() {
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
        lhs.draw("eatRuleLhs");

        JRule loadCargoRule = new JRule().setName("loadCargoRule");
        lhs = loadCargoRule.createLhs();
        JNode lhsBoat = lhs.createNode().putAttribute("label", "boat");
        lhsBank = lhs.createNode().putAttribute("label", "bank");
        JNode lhsCargo = lhs.createNode();
        lhs.createEdge(lhsBoat, "moored", lhsBank);
        lhs.createEdge(lhsCargo, "at", lhsBank);
        loadCargoRule.setFilterLambda(this::loadCargoRuleLambdaBoatEmpty);
        loadCargoRule.setApplyLambda(this::loadCargoApply);
        ruleList.add(loadCargoRule);
        lhs.draw("loadCargoLhs");

        JRule moveBoatRule = new JRule().setName("moveBoatRule");
        lhs = moveBoatRule.createLhs();
        lhsBoat = lhs.createNode().putAttribute("label", "boat");
        JNode lhsOldBank = lhs.createNode().putAttribute("label", "bank");
        JNode lhsNewBank = lhs.createNode().putAttribute("label", "bank");
        lhs.createEdge(lhsBoat, "moored", lhsOldBank);
        lhs.createEdge(lhsOldBank, "os", lhsNewBank);
        moveBoatRule.setApplyLambda(this::moveBoatApply);
        ruleList.add(moveBoatRule);
        lhs.draw("moveBoatLhs");

        JRule unloadRule = new JRule().setName("unloadRule");
        lhs = unloadRule.createLhs();
        lhsBoat = lhs.createNode().putAttribute("label", "boat");
        lhsBank = lhs.createNode().putAttribute("label", "bank");
        lhsCargo = lhs.createNode();
        lhs.createEdge(lhsBoat, "moored", lhsBank);
        lhs.createEdge(lhsCargo, "moored", lhsBoat);
        unloadRule.setApplyLambda(this::unloadApply);
        ruleList.add(unloadRule);
        lhs.draw("loadCargoLhs");

        return ruleList;
    }

    private void unloadApply(ApplyRuleParams params) {
        JNode lhsBoat = params.getRule().getLhs().getNodeList().get(0);
        JNode lhsBank = params.getRule().getLhs().getNodeList().get(1);
        JNode lhsCargo = params.getRule().getLhs().getNodeList().get(2);

        int boatIndex = params.getColumnNames().indexOf(lhsBoat.toString());
        int bankIndex = params.getColumnNames().indexOf(lhsBank.toString());
        int cargoIndex = params.getColumnNames().indexOf(lhsCargo.toString());

        JNode hostBoat = (JNode) params.getRow().get(boatIndex);
        JNode hostBank = (JNode) params.getRow().get(bankIndex);
        JNode hostCargo = (JNode) params.getRow().get(cargoIndex);

        params.getHostGraph().removeEdge(hostBoat, "in", hostBank);
        params.getHostGraph().createEdge(hostBoat, "at", hostCargo);
    }

    private void moveBoatApply(ApplyRuleParams params) {
        JNode lhsBoat = params.getRule().getLhs().getNodeList().get(0);
        JNode lhsOldBank = params.getRule().getLhs().getNodeList().get(1);
        JNode lhsNewBank = params.getRule().getLhs().getNodeList().get(2);

        int boatIndex = params.getColumnNames().indexOf(lhsBoat.toString());
        int oldBankIndex = params.getColumnNames().indexOf(lhsOldBank.toString());
        int newBankIndex = params.getColumnNames().indexOf(lhsNewBank.toString());

        JNode hostBoat = (JNode) params.getRow().get(boatIndex);
        JNode hostOldBank = (JNode) params.getRow().get(oldBankIndex);
        JNode hostNewBank = (JNode) params.getRow().get(newBankIndex);

        params.getHostGraph().removeEdge(hostBoat, "moored", hostOldBank);
        params.getHostGraph().createEdge(hostBoat, "moored", hostNewBank);
    }

    private String fmpGraphLabel(JGraph jGraph) {
        String leftThings = "";
        String rightThings = "";
        String boatContent = "";
        String boatSide = null;
        for (int i = 0; i < jGraph.getEdgeList().size(); i += 3) {
            JNode source = (JNode) jGraph.getEdgeList().get(i);
            JNode target = (JNode) jGraph.getEdgeList().get(i + 2);
            String sourceLabel = (String) source.getAttributeValues("label");
            String targetLabel = (String) target.getAttributeValues("label");
            String targetSide = (String) target.getAttributeValues("side");

            if (targetLabel.equals("boat")) {
                boatContent += sourceLabel.substring(0, 1);
                continue;
            }
            if (sourceLabel.equals("bank") || targetSide == null) {
                continue;
            }
            if (sourceLabel.equals("boat")) {
                boatSide = targetSide;
                continue;
            }
            if (targetSide.equals("left")) {
                leftThings += sourceLabel.substring(0, 1).toUpperCase();
            } else {
                rightThings += sourceLabel.substring(0, 1).toUpperCase();
            }
        }

        if (boatSide.equals("left")) {
            leftThings += "b" + boatContent;
        } else {
            rightThings += "b" + boatContent;
        }

        return leftThings + "-" + rightThings;
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
        JGraph.labelFunction = this::fmpGraphLabel;
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

    private void loadCargoRuleLambdaBoatEmpty(JRule jRule, MatchTable matchTable) {
        ArrayList<Object> edgeList = matchTable.getGraph().getEdgeList();
        for (int i = 0; i < edgeList.size(); i += 3) {
            JNode target = (JNode) edgeList.get(i + 2);
            String targetLabel = (String) target.getAttributeValues("label");

            if (targetLabel.equals("boat")) {
                matchTable.table.clear();
            }
        }
    }
}
