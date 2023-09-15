import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.*;
import javax.xml.parsers.*;

import approximateInference.ApproximateBNode;
import approximateInference.ApproximateInference;
import exactInference.ExactInference;
import org.w3c.dom.*;
import util.Assignment;

public class A2main {

    public static void main(String[] args) {
        Document doc = null;

        try {
            File xmlfile = new File(args[1]);
            // read-in and parse the xml file, e.g.
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(xmlfile);

            // construct your BN based on the XML specification
        } catch (Exception e) {
            e.printStackTrace();
        }

        Scanner sc = new Scanner(System.in);

        switch (args[0]) {
            case "P1": {
                // use the network constructed based on the specification in args[1]
                String[] query = getQueriedNode(sc);
                String variable = query[0];
                String value = query[1];

                ExactInference exactInference = new ExactInference(doc);
                double result = exactInference.enumerationNoEvidence(variable, value);
                printResult(result);
            }
            break;

            case "P2": {
                // use the network constructed based on the specification in args[1]
                String[] query = getQueriedNode(sc);
                String variable = query[0];
                String value = query[1];
                String[] order = getOrder(sc);
                // execute query of p(variable=value) with given order of elimination

                ExactInference exactInference = new ExactInference(doc);
                double result = exactInference.variableEliminationNoEvidence(variable, value, order);
                printResult(result);
            }
            break;

            case "P3": {
                // use the network constructed based on the specification in args[1]
                String[] query = getQueriedNode(sc);
                String variable = query[0];
                String value = query[1];
                ArrayList<String[]> evidence = getEvidence(sc);
                // execute query of p(variable=value|evidence) with an order

                ExactInference exactInference = new ExactInference(doc);
                double result = exactInference.variableEliminationWithEvidence(variable, value, evidence);
                printResult(result);
            }
            break;

            case "P4": {
                // use the network constructed based on the specification in args[1]
                String[] query = getQueriedNode(sc);
                String variable = query[0];
                String value = query[1];
                ArrayList<String[]> evidence = getEvidence(sc);

                ApproximateInference approximateInference = new ApproximateInference(doc);

                if (approximateInference.isValidDAG && approximateInference.isValidCondition) {
                    double result = approximateInference.rejection(variable, value, evidence);
                    printResult(result);
                } else {
                    System.out.println("isValidDAG: " + approximateInference.isValidDAG);
                    System.out.println("isValidCondition: " + approximateInference.isValidCondition);
                }

            }
            break;
        }
        sc.close();
    }

    // method to obtain the evidence from the user
    private static ArrayList<String[]> getEvidence(Scanner sc) {

        System.out.println("Evidence:");
        ArrayList<String[]> evidence = new ArrayList<String[]>();
        String[] line = sc.nextLine().split(" ");

        for (String st : line) {
            String[] ev = st.split(":");
            evidence.add(ev);
        }
        return evidence;
    }

    // method to obtain the order from the user
    private static String[] getOrder(Scanner sc) {

        System.out.println("Order:");
        String[] val = sc.nextLine().split(",");
        return val;
    }

    // method to obtain the queried node from the user
    private static String[] getQueriedNode(Scanner sc) {

        System.out.println("Query:");
        String[] val = sc.nextLine().split(":");

        return val;

    }

    // method to format and print the result
    private static void printResult(double result) {

        DecimalFormat dd = new DecimalFormat("#0.00000");
        System.out.println(dd.format(result));
    }

}
