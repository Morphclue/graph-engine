package uks.gmde2122;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.util.ArrayList;

public class JGraph {

    private static final STGroup TEMPLATE_GROUP = new STGroupFile(
            JGraph.class.getResource("graph.stg"));
    private ArrayList<JNode> nodeList = new ArrayList<>();
    private ArrayList<Object> edgeList = new ArrayList<>();

    public JNode createNode() {
        JNode node = new JNode();
        nodeList.add(node);
        return node;
    }

    public JGraph createEdge(JNode source, String label, JNode target) {
        if (!this.containsEdge(source, label, target)) {
            edgeList.add(source);
            edgeList.add(label);
            edgeList.add(target);

        }
        return this;
    }

    private boolean containsEdge(JNode source, String label, JNode target) {
        for (int i = 0; i < edgeList.size(); i += 3) {
            if (edgeList.get(i).equals(source) &&
                    edgeList.get(i + 1).equals(label) &&
                    edgeList.get(i + 2).equals(target)
            ) {
                return true;
            }
        }
        return false;
    }

    public void draw(String name) {
        ST stringTemplate = TEMPLATE_GROUP.getInstanceOf("graph");
        stringTemplate.add("title", name);
        stringTemplate.add("objects", "");
        stringTemplate.add("edges", "");
        String dotString = stringTemplate.render();
    }
}
