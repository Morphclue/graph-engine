package uks.gmde2122;

import java.util.ArrayList;

public class ApplyRuleParams {
    private JGraph hostGraph;
    private JRule rule;
    private ArrayList<String> columnNames;
    private ArrayList<Object> row;

    public JGraph getHostGraph() {
        return hostGraph;
    }

    public ApplyRuleParams setHostGraph(JGraph graph) {
        this.hostGraph = graph;
        return this;
    }

    public JRule getRule() {
        return rule;
    }

    public ApplyRuleParams setRule(JRule rule) {
        this.rule = rule;
        return this;
    }

    public ArrayList<String> getColumnNames() {
        return columnNames;
    }

    public ApplyRuleParams setColumnNames(ArrayList<String> columnNames) {
        this.columnNames = columnNames;
        return this;
    }

    public ArrayList<Object> getRow() {
        return row;
    }

    public ApplyRuleParams setRow(ArrayList<Object> row) {
        this.row = row;
        return this;
    }
}
