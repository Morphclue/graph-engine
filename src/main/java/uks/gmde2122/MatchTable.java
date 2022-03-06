package uks.gmde2122;

import java.util.ArrayList;

public class MatchTable {
    ArrayList<ArrayList<Object>> table = new ArrayList<>();
    ArrayList<String> columnNames = new ArrayList<>();
    private JGraph graph;
    private String ruleName;

    public MatchTable setStartNodes(String columnName, JNode... start) {
        columnNames.add(columnName);
        for (JNode node : start) {
            ArrayList<Object> row = new ArrayList<>();
            row.add(node);
            table.add(row);
        }
        return this;
    }

    public MatchTable setGraph(JGraph graph) {
        this.graph = graph;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ruleName).append("\n");
        stringBuilder.append("|\t").append(String.join("\t|\t", columnNames)).append("\t|\n");
        stringBuilder.append("|---".repeat(columnNames.size()));
        stringBuilder.append("|\n");

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
                    ArrayList<Object> newRow = (ArrayList<Object>) row.clone();
                    newRow.add(value);
                    resultTable.add(newRow);
                }
            }
        }
        table = resultTable;
    }

    public void filterEdge(String sourceColumnName, String edgeLabel, String targetColumnName) {
        int sourceIndex = columnNames.indexOf(sourceColumnName);
        int targetIndex = columnNames.indexOf(targetColumnName);
        ArrayList<ArrayList<Object>> resultTable = new ArrayList<>();
        ArrayList<Object> edgeList = graph.getEdgeList();

        for (ArrayList<Object> row : table) {
            JNode source = (JNode) row.get(sourceIndex);
            JNode target = (JNode) row.get(targetIndex);
            for (int i = 0; i < edgeList.size(); i += 3) {
                if (source == edgeList.get(i)
                        && edgeLabel.equals(edgeList.get(i + 1))
                        && target == edgeList.get(i + 2)) {
                    resultTable.add(row);
                }
            }
        }
        table = resultTable;
    }

    public void filterAttribute(String sourceColumnName, Object filterValue) {
        int sourceIndex = columnNames.indexOf(sourceColumnName);
        ArrayList<ArrayList<Object>> resultTable = new ArrayList<>();

        for (ArrayList<Object> row : table) {
            Object value = row.get(sourceIndex);

            if (value.equals(filterValue)) {
                resultTable.add(row);
            }
        }
        table = resultTable;
    }

    public void crossProduct(MatchTable factor) {
        columnNames.addAll(factor.getColumnNames());
        ArrayList<ArrayList<Object>> resultTable = new ArrayList<>();
        for (ArrayList<Object> firstRow : table) {
            for (ArrayList<Object> secondRow : factor.getTable()) {
                ArrayList<Object> newRow = (ArrayList<Object>) firstRow.clone();
                newRow.addAll(secondRow);
                resultTable.add(newRow);
            }
        }
        table = resultTable;
    }

    public ArrayList<String> getColumnNames() {
        return columnNames;
    }

    public ArrayList<ArrayList<Object>> getTable() {
        return table;
    }

    public void filterIso(String column1, String column2) {
        int firstColumnIndex = columnNames.indexOf(column1);
        int secondColumnIndex = columnNames.indexOf(column2);
        ArrayList<ArrayList<Object>> resultTable = new ArrayList<>();
        for (ArrayList<Object> row : table) {
            Object node1 = row.get(firstColumnIndex);
            Object node2 = row.get(secondColumnIndex);

            if (node1 != node2) {
                resultTable.add(row);
            }
        }
        table = resultTable;
    }

    public JGraph getGraph() {
        return graph;
    }

    public MatchTable setRuleName(String ruleName) {
        this.ruleName = ruleName;
        return this;
    }
}
