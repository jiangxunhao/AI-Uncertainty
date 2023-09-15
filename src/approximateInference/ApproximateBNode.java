package approximateInference;

import util.Assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class ApproximateBNode {
    private String variable;
    private ArrayList<String> varValues;
    private HashMap<Assignment, Double> table;
    private ArrayList<ApproximateBNode> parents;
    private ArrayList<ApproximateBNode> children;

    public ApproximateBNode(String variable, ArrayList<String> varValues, HashMap<Assignment, Double> table, ArrayList<ApproximateBNode> parents) {
        this.variable = variable;
        this.varValues = varValues;
        this.table = table;
        this.parents = parents;
        children = new ArrayList<>();
    }

    public String sample(Assignment parentsVarValues) {
        double[] distribution = new double[varValues.size()];
        double sumDis = 0.0;
        for (int i = 0; i < varValues.size(); i++) {
            HashMap<String, String> ass = new HashMap<>(parentsVarValues.getAssignment());
            ass.put(variable, varValues.get(i));
            Assignment assignment = new Assignment(ass);
            sumDis += evaluate(assignment);
            distribution[i] = sumDis;
        }
        Random random = new Random();
        double r = random.nextDouble();
        for(int i = 0; i < distribution.length; i++ ) {
            if (r < distribution[i]) {
                return varValues.get(i);
            }
        }
        return null;
    }

    public Double evaluate(Assignment assignment) {
        return table.get(assignment);
    }

    public void addChild(ApproximateBNode approximateBNode) {
        children.add(approximateBNode);
    }

    public String getVariable() {
        return variable;
    }

    public ArrayList<ApproximateBNode> getParents() {
        return parents;
    }

    public ArrayList<ApproximateBNode> getChildren() {
        return children;
    }


}
