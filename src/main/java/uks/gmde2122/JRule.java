package uks.gmde2122;

public class JRule {
    private JGraph lhs;

    public JGraph createLhs() {
        lhs = new JGraph();
        return lhs;
    }

    public MatchTable findMatches(JGraph graph) {
        MatchTable matchTable = new MatchTable().setGraph(graph);
        JNode maxNode = chooseFirstNode();
        matchTable.setStartNodes(maxNode.toString(), graph.getNodeList().toArray(new JNode[]{}));

        filterAttributes(matchTable, maxNode);
        tryEdgeExpand(matchTable, maxNode);

        return matchTable;
    }

    private void tryEdgeExpand(MatchTable matchTable, JNode node) {
        for (int i = 0; i < lhs.getEdgeList().size(); i += 3) {
            JNode sourceNode = (JNode) lhs.getEdgeList().get(i);
            String label = (String) lhs.getEdgeList().get(i + 1);
            JNode targetNode = (JNode) lhs.getEdgeList().get(i + 2);
            if (sourceNode == node) {
                matchTable.expandForward(sourceNode.toString(), label, targetNode.toString());
            } else if (targetNode == node) {
                matchTable.expandBackward(sourceNode.toString(), label, targetNode.toString());
            }
        }
    }

    private void filterAttributes(MatchTable matchTable, JNode node) {
        for (int i = 0; i < node.getAttributesList().size(); i += 2) {
            String attributeName = (String) node.getAttributesList().get(i);
            Object value = node.getAttributesList().get(i + 1);
            matchTable.expandAttribute(node.toString(), attributeName, node + attributeName);
            matchTable.filterAttribute(node + attributeName, value);
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
}
