import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Polynom implements Comparable<Polynom>, Cloneable {
    private final HashMap<Integer, Double> coefficients;

    // Constructor
    public Polynom() {
        coefficients = new HashMap<>();
    }

    // Constructor with initial coefficient
    public Polynom(int degree, double coefficient) {
        coefficients = new HashMap<>();
        if (coefficient != 0) {
            coefficients.put(degree, coefficient);
        }
    }

    // Copy constructor
    public Polynom(Polynom other) {
        coefficients = new HashMap<>(other.coefficients);
    }

    // Add coefficient
    public void addCoefficient(int degree, double coefficient) {
        if (coefficient != 0) {
            coefficients.put(degree, coefficient);
        } else {
            coefficients.remove(degree);
        }
    }

    // Addition
    public Polynom add(Polynom other) {
        Polynom result = new Polynom(this);
        for (Map.Entry<Integer, Double> entry : other.coefficients.entrySet()) {
            int degree = entry.getKey();
            double coef = entry.getValue();
            result.coefficients.merge(degree, coef, Double::sum);
            if (result.coefficients.get(degree) == 0) {
                result.coefficients.remove(degree);
            }
        }
        return result;
    }

    // Subtraction
    public Polynom subtract(Polynom other) {
        Polynom result = new Polynom(this);
        for (Map.Entry<Integer, Double> entry : other.coefficients.entrySet()) {
            int degree = entry.getKey();
            double coef = entry.getValue();
            result.coefficients.merge(degree, -coef, Double::sum);
            if (result.coefficients.get(degree) == 0) {
                result.coefficients.remove(degree);
            }
        }
        return result;
    }

    // Multiplication by polynomial
    public Polynom multiply(Polynom other) {
        Polynom result = new Polynom();
        for (Map.Entry<Integer, Double> entry1 : this.coefficients.entrySet()) {
            for (Map.Entry<Integer, Double> entry2 : other.coefficients.entrySet()) {
                int newDegree = entry1.getKey() + entry2.getKey();
                double newCoef = entry1.getValue() * entry2.getValue();
                result.coefficients.merge(newDegree, newCoef, Double::sum);
                if (result.coefficients.get(newDegree) == 0) {
                    result.coefficients.remove(newDegree);
                }
            }
        }
        return result;
    }

    // Multiplication by number
    public Polynom multiply(double number) {
        Polynom result = new Polynom();
        for (Map.Entry<Integer, Double> entry : coefficients.entrySet()) {
            double newCoef = entry.getValue() * number;
            if (newCoef != 0) {
                result.coefficients.put(entry.getKey(), newCoef);
            }
        }
        return result;
    }

    // Division
    public Polynom divide(Polynom divisor) throws ArithmeticException {
        if (divisor.coefficients.isEmpty()) {
            throw new ArithmeticException("Division by zero polynomial");
        }
        Polynom quotient = new Polynom();
        Polynom remainder = new Polynom(this);

        int divisorDegree = divisor.getDegree();
        double divisorLeadCoef = divisor.coefficients.getOrDefault(divisorDegree, 0.0);

        while (!remainder.coefficients.isEmpty() && remainder.getDegree() >= divisorDegree) {
            int degreeDiff = remainder.getDegree() - divisorDegree;
            double coef = remainder.coefficients.getOrDefault(remainder.getDegree(), 0.0) / divisorLeadCoef;

            Polynom term = new Polynom(degreeDiff, coef);
            quotient = quotient.add(term);
            remainder = remainder.subtract(divisor.multiply(term));
        }
        return quotient;
    }

    // Remainder
    public Polynom remainder(Polynom divisor) throws ArithmeticException {
        if (divisor.coefficients.isEmpty()) {
            throw new ArithmeticException("Division by zero polynomial");
        }
        Polynom remainder = new Polynom(this);

        int divisorDegree = divisor.getDegree();
        double divisorLeadCoef = divisor.coefficients.getOrDefault(divisorDegree, 0.0);

        while (!remainder.coefficients.isEmpty() && remainder.getDegree() >= divisorDegree) {
            int degreeDiff = remainder.getDegree() - divisorDegree;
            double coef = remainder.coefficients.getOrDefault(remainder.getDegree(), 0.0) / divisorLeadCoef;

            Polynom term = new Polynom(degreeDiff, coef);
            remainder = remainder.subtract(divisor.multiply(term));
        }
        return remainder;
    }

    // Get degree
    public int getDegree() {
        return coefficients.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    @Override
    public String toString() {
        if (coefficients.isEmpty()) {
            return "0";
        }
        TreeMap<Integer, Double> sorted = new TreeMap<>(coefficients);
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (Map.Entry<Integer, Double> entry : sorted.descendingMap().entrySet()) {
            double coef = entry.getValue();
            int degree = entry.getKey();

            if (coef == 0) continue;

            if (!first && coef > 0) {
                sb.append(" + ");
            } else if (coef < 0) {
                sb.append(" - ");
            }

            double absCoef = Math.abs(coef);
            if (absCoef != 1 || degree == 0) {
                sb.append(absCoef);
            }

            if (degree > 0) {
                sb.append("x");
                if (degree > 1) {
                    sb.append("^").append(degree);
                }
            }
            first = false;
        }
        return sb.toString();
    }

    @Override
    public int compareTo(Polynom other) {
        int thisDegree = this.getDegree();
        int otherDegree = other.getDegree();

        if (thisDegree != otherDegree) {
            return Integer.compare(thisDegree, otherDegree);
        }

        TreeMap<Integer, Double> thisSorted = new TreeMap<>(this.coefficients);
        TreeMap<Integer, Double> otherSorted = new TreeMap<>(other.coefficients);

        for (Integer degree : thisSorted.descendingKeySet()) {
            double thisCoef = thisSorted.getOrDefault(degree, 0.0);
            double otherCoef = otherSorted.getOrDefault(degree, 0.0);
            if (thisCoef != otherCoef) {
                return Double.compare(thisCoef, otherCoef);
            }
        }
        return 0;
    }

    @Override
    public Polynom clone() {
        try {
            return new Polynom(this);
        } catch (Exception e) {
            throw new AssertionError("Clone not supported", e);
        }
    }

    // Main method for testing
    public static void main(String[] args) {
        // Test polynomial operations
        Polynom p1 = new Polynom();
        p1.addCoefficient(2, 1);
        p1.addCoefficient(1, 2);
        p1.addCoefficient(0, 1);

        Polynom p2 = new Polynom();
        p2.addCoefficient(1, 1);
        p2.addCoefficient(0, 1);

        System.out.println("p1 = " + p1);
        System.out.println("p2 = " + p2);

        System.out.println("p1 + p2 = " + p1.add(p2));
        System.out.println("p1 - p2 = " + p1.subtract(p2));
        System.out.println("p1 * p2 = " + p1.multiply(p2)); 
        System.out.println("p1 * 2 = " + p1.multiply(2));
        System.out.println("p1 / p2 = " + p1.divide(p2));
        System.out.println("p1 % p2 = " + p1.remainder(p2));
    }
}