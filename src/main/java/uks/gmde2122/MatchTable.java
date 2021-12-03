package uks.gmde2122;

import java.util.ArrayList;

public class MatchTable {

    ArrayList<ArrayList<Object>> table = new ArrayList<>();
    ArrayList<String> columnNames = new ArrayList<>();
    private JGraph graph;

    public MatchTable setStartNode(JNode start) {
        ArrayList<Object> row = new ArrayList<>();
        row.add(start);
        table.add(row);
        columnNames.add("start");
        return this;
    }

    public MatchTable setGraph(JGraph graph) {
        this.graph = graph;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("|\t").append(String.join("\t|\t", columnNames)).append("\t|\n");

        for (ArrayList<Object> row : table) {
            stringBuilder.append("| ");

            for (Object cell : row) {
                stringBuilder.append(cell.toString()).append("\t|\t");
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }

    public void expandForward(String sourceColumnName, String edgeLabel, String targetColumnName) {
        int sourceIndex = columnNames.indexOf(sourceColumnName);
        columnNames.add(targetColumnName);

        ArrayList<ArrayList<Object>> resultTable = new ArrayList<>();
        ArrayList<Object> edgeList = graph.getEdgeList();
        for (ArrayList<Object> row : table) {
            JNode source = (JNode) row.get(sourceIndex);
            for (int i = 0; i < edgeList.size(); i += 3) {
                if (source == edgeList.get(i) && edgeLabel.equals(edgeList.get(i + 1))) {
                    JNode target = (JNode) edgeList.get(i + 2);
                    @SuppressWarnings("unchecked")
                    ArrayList<Object> newRow = (ArrayList<Object>) row.clone();
                    newRow.add(target);
                    resultTable.add(newRow);
                }
            }
        }
        table = resultTable;
    }

    public void expandBackward(String sourceColumnName, String edgeLabel, String targetColumnName) {
        int targetIndex = columnNames.indexOf(targetColumnName);
        columnNames.add(sourceColumnName);

        ArrayList<ArrayList<Object>> resultTable = new ArrayList<>();
        ArrayList<Object> edgeList = graph.getEdgeList();
        for (ArrayList<Object> row : table) {
            JNode target = (JNode) row.get(targetIndex);
            for (int i = 0; i < edgeList.size(); i += 3) {
                if (target == edgeList.get(i + 2) && edgeLabel.equals(edgeList.get(i + 1))) {
                    JNode newSource = (JNode) edgeList.get(i);
                    @SuppressWarnings("unchecked")
                    ArrayList<Object> newRow = (ArrayList<Object>) row.clone();
                    newRow.add(newSource);
                    resultTable.add(newRow);
                }
            }
        }
        table = resultTable;
    }

    public void expandAttribute(String sourceColumnName, String attributeName, String targetColumnName) {
        int sourceIndex = columnNames.indexOf(sourceColumnName);
        columnNames.add(targetColumnName);

        ArrayList<ArrayList<Object>> resultTable = new ArrayList<>();

        for (ArrayList<Object> row : table) {
            JNode source = (JNode) row.get(sourceIndex);
            ArrayList<Object> attributeList = source.getAttributesList();
            for (int i = 0; i < attributeList.size(); i += 2) {
                if (attributeName.equals(attributeList.get(i))) {
                    Object value = attributeList.get(i + 1);
                    @SuppressWarnings("unchecked")
                    ArrayList<Object> newRow = (ArrayList<Object>) row.clone();
                    newRow.add(value);
                    resultTable.add(newRow);
                }
            }
        }
        table = resultTable;
    }
}
