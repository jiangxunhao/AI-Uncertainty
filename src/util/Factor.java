package util;

public interface Factor {
    Factor assign(Assignment assignment);

    Factor assign(String variable, String varValue);

    Factor normalise();

    Factor sum_out(String variable);

    Factor join(Factor factor);

    Factor join(Factor[] factors);

    boolean contains(String variable);

    double evaluate(Assignment assignment);
}
