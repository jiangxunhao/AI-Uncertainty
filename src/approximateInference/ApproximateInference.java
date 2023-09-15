package approximateInference;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import util.Assignment;
import util.Inference;

import java.util.ArrayList;
import java.util.HashMap;

public class ApproximateInference extends Inference {
    static public final int TIMES = 10000;

    private HashMap<String, ApproximateBNode> approximateBN;
    public boolean isValidDAG;
    public boolean isValidCondition;

    public ApproximateInference(Document doc) {
        approximateBN = new HashMap<String, ApproximateBNode>();
        topologicalOrder = new ArrayList<>();
        isValidDAG = true;
        isValidCondition = true;
        readXML(doc);
    }

    public double rejection(String variable, String varValue, ArrayList<String[]> evidence) {
        String[][] samples = getSamples();
        int n = 0, m = 0;
        for(int i = 0; i < TIMES; i++) {
            if (evidence.isEmpty() || isValid(samples[i], evidence)) {
                m++;
                if (samples[i][topologicalOrder.indexOf(variable)].equals(varValue)) {
                    n++;
                }
            }
        }
        return (double)n / (double)m;
    }

    public HashMap<String, ApproximateBNode> getApproximateBN() {
        return approximateBN;
    }

    private boolean isValid(String[] sample, ArrayList<String[]> evidence) {
        if(evidence.isEmpty()) {
            return true;
        }

        for(int i = 0; i < sample.length; i++) {
            for(int j = 0; j < evidence.size(); j++) {
                String var = evidence.get(j)[0];
                String varValue = evidence.get(j)[1];
                if (!sample[topologicalOrder.indexOf(var)].equals(varValue)) {
                    return false;
                }
            }
        }
        return true;
    }

    private String[][] getSamples() {
        String[][] samples = new String[TIMES][approximateBN.size()];
        for (int i = 0; i < TIMES; i++) {
            for (int j = 0; j < approximateBN.size(); j++) {
                String var = topologicalOrder.get(j);
                ApproximateBNode approximateBNode = approximateBN.get(var);
                ArrayList<ApproximateBNode> parents = approximateBNode.getParents();
                HashMap<String, String> ass = new HashMap<>();
                for (int x = 0; x < parents.size(); x++) {
                    String parentVar = parents.get(x).getVariable();
                    String parentValue = samples[i][topologicalOrder.indexOf(parentVar)];
                    ass.put(parentVar, parentValue);
                }
                Assignment assignment = new Assignment(ass);
                samples[i][j] = approximateBNode.sample(assignment);
            }
        }
        return samples;
    }

    @Override
    protected void readXML(Document doc) {
        NodeList listVar = doc.getElementsByTagName("VARIABLE");

        // get all variables and their possible values
        HashMap<String, ArrayList<String>> variables_values = getVariablesValues(listVar);

        NodeList listDefinition = doc.getElementsByTagName("DEFINITION");
        for (int i = 0; i < listDefinition.getLength(); i++) {
            Node node = listDefinition.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                String variable = element.getElementsByTagName("FOR").item(0).getTextContent();

                ArrayList<ApproximateBNode> parents = new ArrayList<>();

                // get all the parents of current node
                NodeList listGiven = element.getElementsByTagName("GIVEN");
                ArrayList<String> parentsString = new ArrayList<>();
                for (int j = 0; j < listGiven.getLength(); j++) {
                    String parentString = listGiven.item(j).getTextContent();
                    parentsString.add(parentString);
                    if (!approximateBN.containsKey(parentString)) {
                        isValidDAG = false;
                    }
                    parents.add(approximateBN.get(parentString));
                }

                updateTopologicalOrder(variable, parentsString);

                // get all probability values of the table
                String givenValueText = element.getElementsByTagName("TABLE").item(0).getTextContent();
                String[] givenValuesText = givenValueText.split(" ");

                // get the possible values of current variable
                ArrayList<String> variableValues = variables_values.get(variable);
                int varValuesSize = variableValues.size();

                HashMap<Assignment, Double> table = new HashMap<>();
                HashMap<Assignment, Double> conTable = new HashMap<>();
                for (int x = 0; x < givenValuesText.length ; x++) {

                    HashMap<String, String> ass = new HashMap<>();
                    HashMap<String, String> con = new HashMap<>();

                    ass.put(variable, variableValues.get(x % varValuesSize));

                    int denominator = givenValuesText.length;
                    for(int y = 0; y < parentsString.size(); y++) {
                        String parentVar = parentsString.get(y);
                        ArrayList<String> parentValues = variables_values.get(parentVar);
                        int parentValuesSize = parentValues.size();
                        denominator /= parentValuesSize;
                        String parentValue = parentValues.get( (x / denominator) % parentValuesSize );

                        ass.put(parentVar, parentValue);
                        con.put(parentVar, parentValue);
                    }

                    Assignment assignment = new Assignment(ass);
                    Assignment conAssignment = new Assignment(con);

                    Double value = Double.valueOf(givenValuesText[x]);
                    table.put(assignment, value);

                    if (conTable.containsKey(conAssignment)) {
                        conTable.put(conAssignment, value + conTable.get(conAssignment));
                    } else {
                        conTable.put(conAssignment, value);
                    }
                }

                for(Double value : conTable.values()) {
                    if(value != 1.0) {
                        isValidCondition = false;
                    }
                }

                ApproximateBNode approximateBNode = new ApproximateBNode(variable, variableValues, table, parents);
                approximateBN.put(variable, approximateBNode);
                for(ApproximateBNode parent : parents) {
                    if(parent != null) {
                        parent.addChild(approximateBNode);
                    }

                }
            }
        }
    }

}
