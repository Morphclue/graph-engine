package uks.gmde2122;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class JRule {
    private JGraph lhs;
    private String name;
    private BiConsumer<JRule, MatchTable> filterLambda = (r, t) -> {
    };
    private Consumer<ApplyRuleParams> applyLambda;

    public void apply(ApplyRuleParams params) {
        if (this.applyLambda != null) {
            this.applyLambda.accept(params);
        }
    }

    public JGraph createLhs() {
        lhs = new JGraph();
        return lhs;
    }

    public MatchTable findMatches(JGraph graph) {
        MatchTable matchTable = new MatchTable().setGraph(graph).setRuleName(name);
        JNode maxNode = chooseFirstNode();
        matchTable.setStartNodes(maxNode.toString(), graph.getNodeList().toArray(new JNode[]{}));
        System.out.println(matchTable);

        ArrayList<JNode> todo = new ArrayList<>();
        ArrayList<JNode> tableNodes = new ArrayList<>();
        ArrayList<Integer> doneEdgeIndices = new ArrayList<>();
        todo.add(maxNode);
        tableNodes.add(maxNode);

        while (!todo.isEmpty() && matchTable.getTable().size() > 0) {
            maxNode = todo.remove(0);

            filterAttributes(matchTable, maxNode);
            filterEdges(matchTable, maxNode, tableNodes, doneEdgeIndices);
            filterLambda.accept(this, matchTable);
            expandEdges(matchTable, maxNode, todo, tableNodes, doneEdgeIndices);
        }

        return matchTable;
    }

    private void expandEdges(MatchTable matchTable, JNode maxNode, ArrayList<JNode> todo,
                             ArrayList<JNode> tableNodes, ArrayList<Integer> doneEdgeIndices) {
        if (matchTable.getTable().size() <= 0) {
            return;
        }

        for (int i = 0; i < lhs.getEdgeList().size(); i += 3) {
            if (doneEdgeIndices.contains(i)) {
                continue;
            }
            JNode sourceNode = (JNode) lhs.getEdgeList().get(i);
            String label = (String) lhs.getEdgeList().get(i + 1);
            JNode targetNode = (JNode) lhs.getEdgeList().get(i + 2);
            if (sourceNode == maxNode) {
                matchTable.expandForward(sourceNode.toString(), label, targetNode.toString());
                System.out.println(matchTable);
                todo.add(targetNode);
                tableNodes.add(targetNode);
                doneEdgeIndices.add(i);
            } else if (targetNode == maxNode) {
                matchTable.expandBackward(sourceNode.toString(), label, targetNode.toString());
                System.out.println(matchTable);
                todo.add(sourceNode);
                tableNodes.add(sourceNode);
                doneEdgeIndices.add(i);
            }
        }
    }

    private void filterEdges(MatchTable matchTable, JNode maxNode, ArrayList<JNode> tableNodes,
                             ArrayList<Integer> doneEdgeIndices) {
        for (int i = 0; i < lhs.getEdgeList().size(); i += 3) {
            if (doneEdgeIndices.contains(i)) {
                continue;
            }
            JNode sourceNode = (JNode) lhs.getEdgeList().get(i);
            String label = (String) lhs.getEdgeList().get(i + 1);
            JNode targetNode = (JNode) lhs.getEdgeList().get(i + 2);
            if (sourceNode == maxNode && tableNodes.contains(targetNode)) {
                matchTable.filterEdge(sourceNode.toString(), label, targetNode.toString());
                System.out.println(matchTable);
                doneEdgeIndices.add(i);
            } else if (targetNode == maxNode && tableNodes.contains(sourceNode)) {
                matchTable.filterEdge(sourceNode.toString(), label, targetNode.toString());
                System.out.println(matchTable);
                doneEdgeIndices.add(i);
            }
        }
    }

    private void filterAttributes(MatchTable matchTable, JNode node) {
        for (int i = 0; i < node.getAttributesList().size(); i += 2) {
            String attributeName = (String) node.getAttributesList().get(i);
            Object value = node.getAttributesList().get(i + 1);
            matchTable.expandAttribute(node.toString(), attributeName, node + attributeName);
            matchTable.filterAttribute(node + attributeName, value);
            System.out.println(matchTable);
        }
    }

    private JNode chooseFirstNode() {
        JNode maxNode = null;
        int maxScore = 0;
        for (JNode node : lhs.getNodeList()) {
            int attributes = node.getAttributesList().size() / 2;
            int edges = 0;
            for (int i = 0; i < lhs.getEdgeList().size(); i += 3) {
                if (node == lhs.getEdgeList().get(i)) {
                    edges++;
                }
                if (node == lhs.getEdgeList().get(i + 2)) {
                    edges++;
                }
            }
            int score = attributes + edges;
            if (score > maxScore) {
                maxScore = score;
                maxNode = node;
            }
        }

        return maxNode;
    }

    public JRule setName(String name) {
        this.name = name;
        return this;
    }

    public JRule setLhs(JGraph lhs) {
        this.lhs = lhs;
        return this;
    }

    public JGraph getLhs() {
        return lhs;
    }

    public void setFilterLambda(BiConsumer<JRule, MatchTable> lambda) {
        this.filterLambda = lambda;
    }

    public Consumer<ApplyRuleParams> getApplyLambda() {
        return applyLambda;
    }

    public void setApplyLambda(Consumer<ApplyRuleParams> applyLambda) {
        this.applyLambda = applyLambda;
    }

    public String getName() {
        return name;
    }

    @Override
    // no fulib
    public String toString() {
        return "JRule" + name;
    }
}
