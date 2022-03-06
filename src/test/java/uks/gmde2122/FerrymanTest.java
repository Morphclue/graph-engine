package uks.gmde2122;

import org.junit.Test;

import java.util.ArrayList;
import java.util.TreeMap;

public class FerrymanTest {

    private final String LABEL = "label";
    private final String BANK = "bank";
    private final String SIDE = "side";
    private final String WOLF = "wolf";
    private final String GOAT = "goat";
    private final String CABBAGE = "cabbage";
    private final String BOAT = "boat";
    private final String LEFT = "left";
    private final String RIGHT = "right";
    private final String MOORED = "moored";
    private final String LIKES = "likes";
    private final String IN = "in";
    private final String AT = "at";
    private final String OS = "os";
    private JGraph ltsGraph;

    @Test
    public void testFerrymanProblem() {
        JGraph startGraph = generateStartGraph();
        ArrayList<JRule> rules = buildRules();

        ltsGraph = explore(startGraph, rules);
        ltsGraph.draw("ltsGraphExplode", true);

        CTLExists eOp = new CTLExists()
                .setLTSGraph(ltsGraph)
                .setSafeCondition(this::noEat)
                .setGoalCondition(this::fmpSolved);

        boolean solvable = eOp.test(startGraph);

        if (solvable) {
            for (JGraph jGraph : eOp.getSolution()) {
                System.out.printf("visit graph %s %s%n", "" + jGraph.getId(), jGraph);
            }
        }

        ltsGraph.draw("ltsGraph");
    }

    private JGraph explore(JGraph startGraph, ArrayList<JRule> rules) {
        int nextGraphNumber = 1;

        TreeMap<String, ArrayList<JGraph>> certificateMap = new TreeMap<>();
        String startCertificate = startGraph.computeCertificate();
        System.out.println("Start-Certificate = \n" + startCertificate);
        ArrayList<JGraph> graphList = new ArrayList<>();
        graphList.add(startGraph);
        certificateMap.put(startCertificate, graphList);

        JGraph ltsGraph = new JGraph();
        ltsGraph.getNodeList().add(startGraph);

        ArrayList<JGraph> todo = new ArrayList<>();
        todo.add(startGraph);
        startGraph.putAttribute(LABEL, fmpGraphLabel(startGraph));

        while (!todo.isEmpty()) {
            startGraph = todo.remove(0);

            for (JRule rule : rules) {
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
                            .setRule(rule).
                            setColumnNames(ruleMatches.getColumnNames()).
                            setRow(cloneRow));
                    cloneGraph.putAttribute(LABEL, cloneGraph.toString());
                    cloneGraph.draw("cloneGraph" + nextGraphNumber++);

                    String newCertificate = cloneGraph.computeCertificate();
                    ArrayList<JGraph> oldGraphs = certificateMap.get(newCertificate);

                    if (oldGraphs != null) {
                        for (JGraph oldGraph : oldGraphs) {
                            if (oldGraph.isIsomorphic(cloneGraph)) {
                                ltsGraph.createEdge(startGraph, rule.getName(), oldGraph);
                            } else {
                                todo.add(cloneGraph);
                                oldGraphs.add(cloneGraph);
                                ltsGraph.getNodeList().add(cloneGraph);
                                ltsGraph.createEdge(startGraph, rule.getName(), cloneGraph);
                            }
                        }

                    } else {
                        todo.add(cloneGraph);
                        ArrayList<JGraph> cloneGraphList = new ArrayList<>();
                        cloneGraphList.add(cloneGraph);
                        certificateMap.put(newCertificate, cloneGraphList);
                        ltsGraph.getNodeList().add(cloneGraph);
                        ltsGraph.createEdge(startGraph, rule.getName(), cloneGraph);
                    }
                }
            }
        }

        return ltsGraph;
    }

    private JGraph generateStartGraph() {
        JGraph.labelFunction = this::fmpGraphLabel;
        JGraph startGraph = new JGraph();

        JNode wolf = startGraph.createNode().putAttribute(LABEL, WOLF);
        JNode goat = startGraph.createNode().putAttribute(LABEL, GOAT);
        JNode cabbage = startGraph.createNode().putAttribute(LABEL, CABBAGE);
        JNode boat = startGraph.createNode().putAttribute(LABEL, BOAT);
        JNode leftBank = startGraph.createNode()
                .putAttribute(LABEL, BANK)
                .putAttribute(SIDE, LEFT);
        JNode rightBank = startGraph.createNode()
                .putAttribute(LABEL, BANK)
                .putAttribute(SIDE, RIGHT);

        startGraph.createEdge(wolf, AT, leftBank);
        startGraph.createEdge(goat, AT, leftBank);
        startGraph.createEdge(cabbage, AT, leftBank);
        startGraph.createEdge(boat, MOORED, leftBank);
        startGraph.createEdge(wolf, LIKES, goat);
        startGraph.createEdge(goat, LIKES, cabbage);
        startGraph.createEdge(leftBank, OS, rightBank);
        startGraph.createEdge(rightBank, OS, leftBank);

        startGraph.draw("start");
        return startGraph;
    }

    private ArrayList<JRule> buildRules() {
        ArrayList<JRule> ruleList = new ArrayList<>();

        JGraph lhs;
        JRule eatRule = new JRule().setName("eatRule");
        lhs = eatRule.createLhs();
        JNode lhsEater = lhs.createNode();
        JNode lhsFood = lhs.createNode();
        JNode lhsBank = lhs.createNode().putAttribute(LABEL, BANK);
        lhs.createEdge(lhsEater, LIKES, lhsFood);
        lhs.createEdge(lhsEater, AT, lhsBank);
        lhs.createEdge(lhsFood, AT, lhsBank);
        eatRule.setFilterLambda(this::eatRuleLambdaNoBoat);
        eatRule.setApplyLambda(this::eatApply);
        ruleList.add(eatRule);
        lhs.draw("eatRuleLhs");

        JRule loadCargoRule = new JRule().setName("loadCargoRule");
        lhs = loadCargoRule.createLhs();
        JNode lhsBoat = lhs.createNode().putAttribute(LABEL, BOAT);
        lhsBank = lhs.createNode().putAttribute(LABEL, BANK);
        JNode lhsCargo = lhs.createNode();
        lhs.createEdge(lhsBoat, MOORED, lhsBank);
        lhs.createEdge(lhsCargo, AT, lhsBank);
        loadCargoRule.setFilterLambda(this::loadCargoRuleLambdaBoatEmpty);
        loadCargoRule.setApplyLambda(this::loadCargoApply);
        ruleList.add(loadCargoRule);
        lhs.draw("loadCargoLhs");

        JRule moveBoatRule = new JRule().setName("moveBoatRule");
        lhs = moveBoatRule.createLhs();
        lhsBoat = lhs.createNode().putAttribute(LABEL, BOAT);
        JNode lhsOldBank = lhs.createNode().putAttribute(LABEL, BANK);
        JNode lhsNewBank = lhs.createNode().putAttribute(LABEL, BANK);
        lhs.createEdge(lhsBoat, MOORED, lhsOldBank);
        lhs.createEdge(lhsOldBank, OS, lhsNewBank);
        moveBoatRule.setApplyLambda(this::moveBoatApply);
        ruleList.add(moveBoatRule);
        lhs.draw("moveBoatLhs");

        JRule unloadRule = new JRule().setName("unloadRule");
        lhs = unloadRule.createLhs();
        lhsBoat = lhs.createNode().putAttribute(LABEL, BOAT);
        lhsBank = lhs.createNode().putAttribute(LABEL, BANK);
        lhsCargo = lhs.createNode();
        lhs.createEdge(lhsBoat, MOORED, lhsBank);
        lhs.createEdge(lhsCargo, IN, lhsBoat);
        unloadRule.setApplyLambda(this::unloadApply);
        ruleList.add(unloadRule);
        lhs.draw("loadCargoLhs");

        JRule solvedRule = new JRule().setName("solvedRule");
        lhs = solvedRule.createLhs();
        JNode lhsWolf = lhs.createNode().putAttribute(LABEL, WOLF);
        JNode lhsGoat = lhs.createNode().putAttribute(LABEL, GOAT);
        JNode lhsCabbage = lhs.createNode().putAttribute(LABEL, CABBAGE);
        lhsBoat = lhs.createNode().putAttribute(LABEL, BOAT);
        lhsBank = lhs.createNode().putAttribute(SIDE, RIGHT);
        lhs.createEdge(lhsWolf, AT, lhsBank);
        lhs.createEdge(lhsGoat, AT, lhsBank);
        lhs.createEdge(lhsCabbage, AT, lhsBank);
        lhs.createEdge(lhsBoat, MOORED, lhsBank);
        ruleList.add(solvedRule);
        lhs.draw("solvedRuleLhs");

        return ruleList;
    }

    private String fmpGraphLabel(JGraph jGraph) {
        String leftThings = "";
        String rightThings = "";
        String boatContent = "";
        String boatSide = null;
        for (int i = 0; i < jGraph.getEdgeList().size(); i += 3) {
            JNode source = (JNode) jGraph.getEdgeList().get(i);
            JNode target = (JNode) jGraph.getEdgeList().get(i + 2);
            String sourceLabel = (String) source.getAttributeValues(LABEL);
            String targetLabel = (String) target.getAttributeValues(LABEL);
            String targetSide = (String) target.getAttributeValues(SIDE);

            if (targetLabel.equals(BOAT)) {
                boatContent += sourceLabel.substring(0, 1);
                continue;
            }
            if (sourceLabel.equals(BANK) || targetSide == null) {
                continue;
            }
            if (sourceLabel.equals(BOAT)) {
                boatSide = targetSide;
                continue;
            }
            if (targetSide.equals(LEFT)) {
                leftThings += sourceLabel.substring(0, 1).toUpperCase();
            } else {
                rightThings += sourceLabel.substring(0, 1).toUpperCase();
            }
        }

        if (boatSide.equals(LEFT)) {
            leftThings += "b" + boatContent;
        } else {
            rightThings += "b" + boatContent;
        }

        return leftThings + "-" + rightThings;
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

        params.getHostGraph().removeEdge(hostCargo, IN, hostBoat);
        params.getHostGraph().createEdge(hostCargo, AT, hostBank);
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

        params.getHostGraph().removeEdge(hostBoat, MOORED, hostOldBank);
        params.getHostGraph().createEdge(hostBoat, MOORED, hostNewBank);
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

        params.getHostGraph().removeEdge(hostCargo, AT, hostBank);
        params.getHostGraph().createEdge(hostCargo, IN, hostBoat);
    }

    private void eatApply(ApplyRuleParams params) {
        JNode lhsFood = params.getRule().getLhs().getNodeList().get(1);
        int foodIndex = params.getColumnNames().indexOf(lhsFood.toString());
        JNode hostFood = (JNode) params.getRow().get(foodIndex);

        params.getHostGraph().removeNode(hostFood);
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

                if (label.equals(MOORED) && bank == target) {
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
            String targetLabel = (String) target.getAttributeValues(LABEL);

            if (targetLabel.equals(BOAT)) {
                matchTable.table.clear();
            }
        }
    }

    private boolean noEat(JGraph jGraph) {
        ArrayList<Object> edgeList = ltsGraph.getEdgeList();

        for (int i = 0; i < edgeList.size(); i += 3) {
            if (edgeList.get(i) == jGraph && "eatRule".equals(edgeList.get(i + 1))) {
                return false;
            }
        }
        return true;
    }

    private boolean fmpSolved(JGraph jGraph) {
        ArrayList<Object> edgeList = ltsGraph.getEdgeList();

        for (int i = 0; i < edgeList.size(); i += 3) {
            if (edgeList.get(i) == jGraph && "solvedRule".equals(edgeList.get(i + 1))) {
                return true;
            }
        }
        return false;
    }
}
