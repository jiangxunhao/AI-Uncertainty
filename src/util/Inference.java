package util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;

abstract public class Inference {
    protected ArrayList<String> topologicalOrder;

    protected abstract void readXML(Document doc);

    protected HashMap<String, ArrayList<String>> getVariablesValues(NodeList listVar) {
        HashMap<String, ArrayList<String>> variable_values = new HashMap<>();
        for (int i = 0; i < listVar.getLength(); i++) {
            Node node = listVar.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                String variable = element.getElementsByTagName("NAME").item(0).getTextContent();

                NodeList listOutcome = element.getElementsByTagName("OUTCOME");
                ArrayList<String> values = new ArrayList<>();
                for (int j = 0; j < listOutcome.getLength(); j++) {
                    String value = listOutcome.item(j).getTextContent();
                    values.add(value);
                }

                variable_values.put(variable, values);
            }
        }

        return variable_values;
    }

    protected void updateTopologicalOrder(String variable, ArrayList<String> givenVariables) {
        if(givenVariables.size() == 0) {
            topologicalOrder.add(0,variable);
            return;
        }
        int numberAfterGivenVar = 0;
        for(int i = 0; i < topologicalOrder.size(); i++) {
            if(givenVariables.contains(topologicalOrder.get(i))) {
                numberAfterGivenVar++;
            }
            if(numberAfterGivenVar == givenVariables.size()) {
                topologicalOrder.add(i+1, variable);
                break;
            }
        }
    }
}
