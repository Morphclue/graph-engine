package uks.gmde2122;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class JGraph {

    private static final STGroup TEMPLATE_GROUP = new STGroupFile("uks/gmde2122/graph.stg");
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
        stringTemplate.add("objects", drawObjects());
        stringTemplate.add("edges", drawEdges());
        String dotString = stringTemplate.render();

        try {
            Graphviz.fromString(dotString).render(Format.SVG).toFile(new File("tmp/" + name + ".svg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String drawObjects() {
        StringBuilder objects = new StringBuilder();
        for (JNode node : this.nodeList) {
            String label = (String) node.getAttributeValues("label");
            if (label == null) {
                label = "noLabel";
            }

            ST objectST = TEMPLATE_GROUP.getInstanceOf("object");
            objectST.add("objectId", "" + node.getId());
            objectST.add("label", "" + label);
            objectST.add("attributeList", new String[]{"attr1 = Hello", "attr2 = World"});
            objects.append(objectST.render());
        }
        return objects.toString();
    }

    public String drawEdges() {
        StringBuilder edges = new StringBuilder();
        for (int i = 0; i < edgeList.size(); i += 3) {
            JNode source = (JNode) edgeList.get(i);
            String label = (String) edgeList.get(i + 1);
            JNode target = (JNode) edgeList.get(i + 2);

            ST edgeST = TEMPLATE_GROUP.getInstanceOf("edge");
            edgeST.add("source", source.getId());
            edgeST.add("label", label);
            edgeST.add("target", target.getId());
            edges.append(edgeST.render());
        }
        return edges.toString();
    }
}
