package uks.gmde2122;

import java.util.ArrayList;
import java.util.function.Predicate;

public class CTLExists implements Predicate<JGraph> {
    private JGraph ltsGraph;
    private Predicate<JGraph> safeCondition;
    private Predicate<JGraph> goalCondition;
    private ArrayList<JGraph> solution;
    private ArrayList<JGraph> visited;

    @Override
    public boolean test(JGraph jGraph) {
        ArrayList<JGraph> path = new ArrayList<>();
        visited = new ArrayList<>();
        path.add(jGraph);

        solution = findPath(path);
        return solution != null;
    }

    private ArrayList<JGraph> findPath(ArrayList<JGraph> path) {
        JGraph lastGraph = path.get(path.size() - 1);

        if (visited.contains(lastGraph)) {
            return null;
        }

        visited.add(lastGraph);

        if (goalCondition.test(lastGraph)) {
            return path;
        }

        if (safeCondition.test(lastGraph)) {
            ArrayList<Object> edgeList = this.ltsGraph.getEdgeList();
            for (int i = 0; i < edgeList.size(); i += 3) {
                JGraph source = (JGraph) edgeList.get(i);
                JGraph target = (JGraph) edgeList.get(i + 2);
                if (source == lastGraph) {
                    ArrayList<JGraph> clone = (ArrayList<JGraph>) path.clone();
                    clone.add(target);
                    ArrayList<JGraph> solution = findPath(clone);
                    if (solution != null) {
                        return solution;
                    }
                }
            }
        }
        return null;
    }

    public CTLExists setLTSGraph(JGraph ltsGraph) {
        this.ltsGraph = ltsGraph;
        return this;
    }

    public CTLExists setSafeCondition(Predicate<JGraph> safeCondition) {
        this.safeCondition = safeCondition;
        return this;
    }

    public CTLExists setGoalCondition(Predicate<JGraph> goalCondition) {
        this.goalCondition = goalCondition;
        return this;
    }

    public ArrayList<JGraph> getSolution() {
        return this.solution;
    }
}
