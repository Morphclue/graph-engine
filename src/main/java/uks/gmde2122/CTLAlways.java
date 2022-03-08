package uks.gmde2122;

import java.util.ArrayList;
import java.util.function.Predicate;

public class CTLAlways implements Predicate<JGraph> {
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
        return solution == null;
    }

    private ArrayList<JGraph> findPath(ArrayList<JGraph> path) {
        // TODO: path-finding
        return path;
    }

    public CTLAlways setLTSGraph(JGraph ltsGraph) {
        this.ltsGraph = ltsGraph;
        return this;
    }

    public CTLAlways setSafeCondition(Predicate<JGraph> safeCondition) {
        this.safeCondition = safeCondition;
        return this;
    }

    public CTLAlways setGoalCondition(Predicate<JGraph> goalCondition) {
        this.goalCondition = goalCondition;
        return this;
    }

    public ArrayList<JGraph> getSolution() {
        return this.solution;
    }
}
