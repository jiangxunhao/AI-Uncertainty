package exactInference;

import util.Assignment;
import util.Factor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ExactBNode implements Factor {
    private HashSet<String> variables;
    private HashMap<Assignment, Double> dataTable;

    public ExactBNode(HashMap<Assignment, Double> dataTable) {
        this.dataTable = dataTable;
        for(Assignment assignment : dataTable.keySet()) {
            HashMap<String, String> ass = assignment.getAssignment();
            variables = new HashSet<>(ass.keySet());
            break;
        }
    }

    public HashSet<String> getVariables() {
        return variables;
    }

    public HashMap<Assignment, Double> getDataTable() {
        return dataTable;
    }

    @Override
    public ExactBNode assign(Assignment assignment) {
        ExactBNode exactBNode = this;

        HashMap<String, String> ass = assignment.getAssignment();
        for(Map.Entry<String, String> set : ass.entrySet()) {
            String variable = set.getKey();
            String varValue = set.getValue();

            exactBNode = exactBNode.assign(variable, varValue);
        }

        return exactBNode;
    }

    @Override
    public ExactBNode assign(String variable, String varValue) {
        HashMap<Assignment, Double> newDataTable = new HashMap<>();

        for(Map.Entry<Assignment, Double> data : dataTable.entrySet()) {
            Assignment assignment = data.getKey();
            HashMap<String, String> ass = assignment.getAssignment();

            Double value = data.getValue();

            if(varValue.equals(ass.remove(variable))) {
                Assignment newAssignment = new Assignment(ass);
                newDataTable.put(newAssignment, value);
            }
        }

        return new ExactBNode(newDataTable);
    }

    @Override
    public ExactBNode normalise() {
        double sum = 0.0;
        HashMap<Assignment, Double> newDataTable = new HashMap<>();

        for(Double value : dataTable.values()) {
            sum += value.doubleValue();
        }

        for(Map.Entry<Assignment, Double> data : dataTable.entrySet()) {
            newDataTable.put(data.getKey(), Double.valueOf(data.getValue().doubleValue() / sum));
        }

        return new ExactBNode(newDataTable);
    }

    @Override
    public ExactBNode sum_out(String variable) {
        HashMap<Assignment, Double> newDataTable = new HashMap<>();

        for(Map.Entry<Assignment, Double> data : dataTable.entrySet()) {
            Assignment assignment = data.getKey();
            HashMap<String, String> ass = assignment.getAssignment();
            double value = data.getValue().doubleValue();

            ass.remove(variable);
            Assignment newAssignment = new Assignment(ass);
            if(newDataTable.containsKey(newAssignment)) {
                value += newDataTable.get(newAssignment).doubleValue();
            }
            newDataTable.put(newAssignment, Double.valueOf(value));
        }

        return new ExactBNode(newDataTable);
    }

    @Override
    public ExactBNode join(Factor factor) {
        HashMap<Assignment, Double> newDataTable = new HashMap<>();

        HashSet<String> intersection = new HashSet<>();

        ExactBNode exactBNode2 = (ExactBNode) factor;

        HashSet<String> variables2 = exactBNode2.getVariables();
        for(String variable1 : variables) {
            if(variables2.contains(variable1)) {
                intersection.add(variable1);
            }
        }

        HashMap<Assignment, Double> dataTable2 = exactBNode2.getDataTable();

        for(Map.Entry<Assignment, Double> data1 : dataTable.entrySet()) {
            Assignment assignment1 = data1.getKey();
            HashMap<String, String> ass1 = assignment1.getAssignment();
            double value1 = data1.getValue().doubleValue();

            for(Map.Entry<Assignment, Double> data2 : dataTable2.entrySet()) {
                Assignment assignment2 = data2.getKey();
                HashMap<String, String> ass2 = assignment2.getAssignment();
                double value2 = data2.getValue().doubleValue();

                boolean isPut = true;
                for(String variable : intersection) {
                    String varValue1 = ass1.get(variable);
                    String varValue2 = ass2.get(variable);
                    if(!varValue1.equals(varValue2)) {
                        isPut = false;
                        break;
                    }
                }
                if(isPut) {
                    HashMap<String, String> newAss = new HashMap<>(ass1);
                    for(Map.Entry<String, String> set : ass2.entrySet()) {
                        String variable = set.getKey();
                        String varValue = set.getValue();
                        if(newAss.containsKey(variable)) {
                            continue;
                        }
                        newAss.put(variable, varValue);
                    }
                    Assignment newAssignment = new Assignment(newAss);
                    newDataTable.put(newAssignment, Double.valueOf(value1 * value2));
                }
            }
        }

        return new ExactBNode(newDataTable);
    }

    @Override
    public ExactBNode join(Factor[] factors) {
        ExactBNode newExactBNode = this;
        for(Factor factor : factors) {
            ExactBNode exactBNode2 = (ExactBNode) factor;
            newExactBNode = newExactBNode.join(exactBNode2);
        }
        return newExactBNode;
    }

    @Override
    public boolean contains(String variable) {
        return variables.contains(variable);
    }

    @Override
    public double evaluate(Assignment assignment) {
        return dataTable.get(assignment);
    }
}
