package exactInference;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import util.Assignment;
import util.Inference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ExactInference extends Inference {
    private ArrayList<ExactBNode> exactBN;


    public ExactInference(Document doc) {
        exactBN = new ArrayList<>();
        topologicalOrder = new ArrayList<>();
        readXML(doc);
    }

    public double enumerationNoEvidence(String variable, String varValue) {
        ExactBNode exactBNodeJoint = exactBN.get(0);
        ArrayList<ExactBNode> restBN = new ArrayList<>(exactBN);
        restBN.remove(0);
        ExactBNode[] restBNArray = restBN.toArray(new ExactBNode[restBN.size()]);
        exactBNodeJoint = exactBNodeJoint.join(restBNArray);

        HashSet<String> variables = exactBNodeJoint.getVariables();
        for (String varSumOut : variables) {
            if (!varSumOut.equals(variable)) {
                exactBNodeJoint = exactBNodeJoint.sum_out(varSumOut);
            }
        }
        exactBNodeJoint = exactBNodeJoint.normalise();

        HashMap<String, String> ass = new HashMap<>();
        ass.put(variable, varValue);
        return exactBNodeJoint.evaluate(new Assignment(ass));
    }

    public double variableEliminationNoEvidence(String variable, String varValue, String[] order) {
        for (String varEliminate : order) {
            ArrayList<ExactBNode> exactBNodesContainsVarEliminate = new ArrayList<>();
            ExactBNode[] ExactBNodes = exactBN.toArray(new ExactBNode[exactBN.size()]);
            for (ExactBNode exactBNode : ExactBNodes) {
                if (exactBNode.contains(varEliminate)) {
                    exactBNodesContainsVarEliminate.add(exactBNode);
                    exactBN.remove(exactBNode);
                }
            }

            if(exactBNodesContainsVarEliminate.size() == 0) {
                continue;
            }
            ExactBNode exactBNodeJoint = exactBNodesContainsVarEliminate.get(0);
            exactBNodesContainsVarEliminate.remove(exactBNodeJoint);
            ExactBNode[] restExactBNodes = exactBNodesContainsVarEliminate.toArray(new ExactBNode[exactBNodesContainsVarEliminate.size()]);
            if(restExactBNodes.length != 0) {
                exactBNodeJoint = exactBNodeJoint.join(restExactBNodes);
            }
            exactBNodeJoint = exactBNodeJoint.sum_out(varEliminate);
            exactBN.add(exactBNodeJoint);
        }

        ExactBNode exactBNode = exactBN.get(0);
        exactBN.remove(exactBNode);
        if (exactBN.size() != 0) {
            ExactBNode[] restExactBNodes = exactBN.toArray(new ExactBNode[exactBN.size()]);
            exactBNode = exactBNode.join(restExactBNodes);
        }
        exactBNode = exactBNode.normalise();
        HashMap<String, String> ass = new HashMap<>();
        ass.put(variable, varValue);
        return exactBNode.evaluate(new Assignment(ass));
    }

    public double variableEliminationWithEvidence(String variable, String varValue, ArrayList<String[]> evidence) {
        topologicalOrder.remove(variable);
        String[] order = topologicalOrder.toArray(new String[topologicalOrder.size()]);
        for(String[] evi : evidence) {
            for(int i = 0; i < exactBN.size(); i++) {
                ExactBNode exactBNode = exactBN.get(i);
                if(exactBNode.contains(evi[0])) {
                    exactBN.remove(exactBNode);
                    exactBNode = exactBNode.assign(evi[0], evi[1]);
                    exactBN.add(i, exactBNode);
                }
            }
        }

        return variableEliminationNoEvidence(variable, varValue, order);
    }

    protected void readXML(Document doc) {
        NodeList listVar = doc.getElementsByTagName("VARIABLE");
        HashMap<String, ArrayList<String>> variables_values = getVariablesValues(listVar);

        NodeList listDefinition = doc.getElementsByTagName("DEFINITION");
        for (int i = 0; i < listDefinition.getLength(); i++) {
            Node node = listDefinition.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                String variable = element.getElementsByTagName("FOR").item(0).getTextContent();

                NodeList listGiven = element.getElementsByTagName("GIVEN");
                ArrayList<String> givenVariables = new ArrayList<>();
                for (int j = 0; j < listGiven.getLength(); j++) {
                    String varGiven = listGiven.item(j).getTextContent();
                    givenVariables.add(varGiven);
                }

                updateTopologicalOrder(variable, givenVariables);

                String givenVariablesValueText = element.getElementsByTagName("TABLE").item(0).getTextContent();
                String[] givenVariablesValuesText = givenVariablesValueText.split(" ");
                HashMap<Assignment, Double> data = new HashMap<>();
                for (int x = 0; x < givenVariablesValuesText.length; x++) {

                    HashMap<String, String> ass = new HashMap<>();

                    ArrayList<String> valuesForVariable = variables_values.get(variable);
                    int sizeForVariableValues = valuesForVariable.size();
                    String valueForVariable = valuesForVariable.get(x % sizeForVariableValues);
                    ass.put(variable, valueForVariable);

                    int denominator = givenVariablesValuesText.length;
                    for (int y = 0; y < givenVariables.size(); y++) {
                        String givenVariable = givenVariables.get(y);
                        ArrayList<String> valuesGivenVariable = variables_values.get(givenVariable);
                        int sizeGivenVariableValues = valuesGivenVariable.size();
                        denominator /= sizeGivenVariableValues;
                        String valueGivenVariable = valuesGivenVariable.get((x / denominator) % sizeGivenVariableValues);
                        ass.put(givenVariable, valueGivenVariable);
                    }

                    Assignment assignment = new Assignment(ass);
                    data.put(assignment, Double.valueOf(givenVariablesValuesText[x]));
                }

                ExactBNode exactBNode = new ExactBNode(data);
                exactBN.add(exactBNode);
            }
        }
    }



}
