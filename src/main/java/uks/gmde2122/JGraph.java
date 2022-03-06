package uks.gmde2122;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.function.Function;

public class JGraph extends JNode {
    public static Function<JGraph, String> labelFunction = null;
    private final ArrayList<JNode> nodeList = new ArrayList<>();
    private final ArrayList<Object> edgeList = new ArrayList<>();
    private StringBuilder objects;
    private StringBuilder edges;
    private STGroup stGroup;
    private boolean explode;

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

    public JGraph removeEdge(JNode source, String label, JNode target) {
        for (int i = 0; i < edgeList.size(); i += 3) {
            if (edgeList.get(i).equals(source) &&
                    edgeList.get(i + 1).equals(label) &&
                    edgeList.get(i + 2).equals(target)) {
                edgeList.remove(i + 2);
                edgeList.remove(i + 1);
                edgeList.remove(i);
                return this;
            }
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
        draw(name, false);
    }

    public void draw(String name, boolean explode) {
        this.explode = explode;
        stGroup = null;
        URL resource = JGraph.class.getResource("graph.stg");
        if (resource != null) {
            stGroup = new STGroupFile(resource);
        } else {
            stGroup = new STGroupFile("src/main/resources/uks/gmde2122/graph.stg");
        }

        objects = new StringBuilder();
        edges = new StringBuilder();

        fillObjectsAndEdges(this);

        ST stringTemplate = stGroup.getInstanceOf("graph");
        stringTemplate.add("title", name);
        stringTemplate.add("objects", objects.toString());
        stringTemplate.add("edges", edges.toString());
        String dotString = stringTemplate.render();

        try {
            Graphviz.fromString(dotString).render(Format.SVG).toFile(new File("tmp/" + name + ".svg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillObjectsAndEdges(JGraph jGraph) {
        for (JNode node : jGraph.nodeList) {
            String label = (String) node.getAttributeValues("label");
            if (label == null) {
                label = "noLabel";
            }

            ArrayList<String> attributeList = new ArrayList<>();
            for (int a = 0; a < node.getAttributesList().size(); a += 2) {
                attributeList.add(String.format("%s = %s",
                        "" + node.getAttributesList().get(a),
                        "" + node.getAttributesList().get(a + 1)));
            }
            ST objectST = stGroup.getInstanceOf("object");
            objectST.add("objectId", "" + node.getId());
            objectST.add("label", label);
            objectST.add("attributeList", attributeList);
            objects.append(objectST.render());

            if (node instanceof JGraph kidGraph && explode) {
                for (JNode kidNode : kidGraph.getNodeList()) {
                    ST edgeST = stGroup.getInstanceOf("edge");
                    edgeST.add("source", "" + kidGraph.getId());
                    edgeST.add("label", "c");
                    edgeST.add("target", "" + kidNode.getId());
                    edges.append(edgeST.render());
                    break;
                }
                fillObjectsAndEdges(kidGraph);
            }
        }

        for (int i = 0; i < jGraph.getEdgeList().size(); i += 3) {
            JNode source = (JNode) jGraph.getEdgeList().get(i);
            String label = (String) jGraph.getEdgeList().get(i + 1);
            JNode target = (JNode) jGraph.getEdgeList().get(i + 2);

            ST edgeST = stGroup.getInstanceOf("edge");
            edgeST.add("source", "" + source.getId());
            edgeST.add("label", label);
            edgeST.add("target", "" + target.getId());
            edges.append(edgeST.render());
        }
    }

    public ArrayList<Object> getEdgeList() {
        return edgeList;
    }

    public ArrayList<JNode> getNodeList() {
        return nodeList;
    }

    @Override
    // no fulib
    public Object clone() {
        JGraph cloneGraph = new JGraph();
        for (JNode node : this.getNodeList()) {
            JNode cloneNode = new JNode();
            cloneNode.setAttributesList((ArrayList<Object>) node.getAttributesList().clone());
            cloneGraph.getNodeList().add(cloneNode);
        }

        for (int i = 0; i < getEdgeList().size(); i += 3) {
            JNode source = (JNode) getEdgeList().get(i);
            String label = (String) getEdgeList().get(i + 1);
            JNode target = (JNode) getEdgeList().get(i + 2);

            int sourceIndex = getNodeList().indexOf(source);
            int targetIndex = getNodeList().indexOf(target);

            JNode cloneSource = cloneGraph.getNodeList().get(sourceIndex);
            JNode cloneTarget = cloneGraph.getNodeList().get(targetIndex);

            cloneGraph.createEdge(cloneSource, label, cloneTarget);
        }

        return cloneGraph;
    }

    public String computeCertificate() {
        LinkedHashMap<JNode, String> nodeToCertificate = new LinkedHashMap<>();
        for (JNode node : this.getNodeList()) {
            String certificateZero = certificateZero(node);
            nodeToCertificate.put(node, certificateZero);
        }

        TreeSet<String> certificateList = new TreeSet<>(nodeToCertificate.values());
        LinkedHashMap<String, Integer> certificateToNumber = new LinkedHashMap<>();

        while (true) {
            int oldNumberOfCertificates = certificateList.size();
            for (String certificate : certificateList) {
                certificateToNumber.put(certificate, certificateToNumber.size() + 1);
            }

            LinkedHashMap<JNode, ArrayList<String>> nodeToLines = new LinkedHashMap<>();
            for (JNode node : this.getNodeList()) {
                nodeToLines.put(node, new ArrayList<>());
            }

            for (int i = 0; i < this.edgeList.size(); i += 3) {
                JNode source = (JNode) this.getEdgeList().get(i);
                String label = (String) this.getEdgeList().get(i + 1);
                JNode target = (JNode) this.getEdgeList().get(i + 2);

                Integer sourceNumber = certificateToNumber.get(nodeToCertificate.get(source));
                Integer targetNumber = certificateToNumber.get(nodeToCertificate.get(target));
                String sourceLine = String.format("  > %s %d\n", label, targetNumber);
                ArrayList<String> sourceLines = nodeToLines.get(source);
                sourceLines.add(sourceLine);

                String targetLine = String.format("  < %s %d\n", label, sourceNumber);
                ArrayList<String> targetLines = nodeToLines.get(target);
                targetLines.add(targetLine);
            }

            certificateList.clear();
            for (JNode node : this.getNodeList()) {
                String oldCertificate = nodeToCertificate.get(node);
                Integer oldNumber = certificateToNumber.get(oldCertificate);
                ArrayList<String> lines = nodeToLines.get(node);
                lines.sort(String::compareTo);
                String newCertificate = "- " + oldNumber + "\n" + String.join("", lines);
                nodeToCertificate.put(node, newCertificate);
                certificateList.add(newCertificate);
            }

            if (oldNumberOfCertificates == certificateList.size()) {
                break;
            }
        }

        return String.join("", certificateList);
    }

    private String certificateZero(JNode node) {
        ArrayList<String> lines = new ArrayList<>();
        for (int i = 0; i < node.getAttributesList().size(); i += 2) {
            lines.add(String.format("%s: %s", node.getAttributesList().get(i), node.getAttributesList().get(i + 1)));
        }
        lines.sort(String::compareTo);
        return "- " + String.join("  \n", lines) + "\n";
    }

    @Override
    //no fulib
    public String toString() {
        if (labelFunction == null) {
            return super.toString();
        }
        return labelFunction.apply(this);
    }

    public boolean isIsomorphic(JGraph other) {
        JRule isomorphicRule = new JRule().setName("isomorphicRule").setLhs(other);

        return !isomorphicRule.findMatches(this).getTable().isEmpty();
    }
}
