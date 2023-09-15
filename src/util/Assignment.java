package util;

import java.util.HashMap;

public class Assignment {
    private HashMap<String, String> assignment;

    public Assignment(HashMap<String, String> assignment) {
        this.assignment = assignment;
    }

    public HashMap<String, String> getAssignment() {
        return assignment;
    }

    @Override
    public boolean equals(Object obj) {
        Assignment comparedAssignment = (Assignment) obj;
        return this.assignment.equals(comparedAssignment.getAssignment());
    }

    @Override
    public int hashCode() {
        return assignment.hashCode();
    }

    public boolean isEmpty() {
        return assignment.isEmpty();
    }
}
