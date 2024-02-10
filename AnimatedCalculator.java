
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Stack;

// Class for the Animated Calculator
public class AnimatedCalculator extends JFrame {

    private JTextField expressionField;

    // Constructor to set up the GUI
    public AnimatedCalculator() {
        setTitle("Styled Calculator");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        expressionField = new JTextField();
        expressionField.setFont(new Font("Arial", Font.PLAIN, 25));
        mainPanel.add(expressionField, BorderLayout.NORTH);

        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        add(mainPanel);

        setVisible(true);
    }

    // Creates the button panel with buttons and their actions
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        String[] buttonLabels = {
                "7", "8", "9", "/", "sin",
                "4", "5", "6", "*", "cos",
                "1", "2", "3", "-", "tan",
                "0", ".", "=", "+", "ln",
                "(", ")", "^", "exp", "pi",
                "Delete", "Clear", "!",
                "%", "acot", "log",
        };
        // Adding buttons to the panel
        for (int i = 0; i < buttonLabels.length; i++) {
            JButton button = new JButton(buttonLabels[i]);
            button.addActionListener(new ButtonClickListener());
            button.setPreferredSize(new Dimension(50, 60));
            button.setFont(new Font("MONOSPACED", Font.BOLD, 21));
            // Setting button colors
            if (buttonLabels[i].equals("=") || buttonLabels[i].equals("Clear")) {
                button.setBackground(Color.GREEN);
            } else {
                button.setBackground(Color.LIGHT_GRAY);
            }

            gbc.gridx = i % 5;
            gbc.gridy = i / 5;
            buttonPanel.add(button, gbc);
        }

        return buttonPanel;
    }

    // ActionListener for the calculator buttons
    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton source = (JButton) e.getSource();
            String buttonText = source.getText();
            // Handling button actions
            switch (buttonText) {
                case "=":
                    String expression = expressionField.getText();
                    expression = handleImplicitMultiplication(expression);
                    double result = calculateExp(expression);
                    expressionField.setText(String.valueOf(result));
                    break;
                case "Delete":
                    String text = expressionField.getText();
                    if (!text.isEmpty()) {
                        expressionField.setText(text.substring(0, text.length() - 1));
                    }
                    break;
                case "Clear":
                    expressionField.setText("");
                    break;
                default:
                    expressionField.setText(expressionField.getText() + buttonText);
            }
        }
    }

    // Calculate the expression after replacing constants and handling implicit
    // multiplication
    private double calculateExp(String expression) {
        String modifiedExpression = expression
                .replaceAll("pi", String.valueOf(Math.PI))
                .replaceAll("exp", "2.718282"); // Replace "exp" with numerical value
        String postfix = infixToPostfix(modifiedExpression);
        return calculatePostfix(postfix);
    }

    // Handles implicit multiplication by adding '*' between consecutive digits
    private String handleImplicitMultiplication(String expression) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < expression.length(); i++) {
            char currentChar = expression.charAt(i);
            result.append(currentChar);
            if (Character.isDigit(currentChar) && i + 1 < expression.length() &&
                    (expression.charAt(i + 1) == '(' || Character.isDigit(expression.charAt(i + 1)))) {
                result.append('*');
            }
        }
        return result.toString();
    }

    // Converts infix expression to postfix notation
    private String infixToPostfix(String infix) {
        StringBuilder postfix = new StringBuilder();
        Stack<String> operators = new Stack<>();
        String[] tokens = infix.split("(?<=[-+*/^()!%])|(?=[-+*/^()!%])");

        for (String token : tokens) {
            if (token.trim().isEmpty()) {
                continue;
            }

            if (token.equals("pi")) {
                postfix.append(token).append(" ");
            } else {
                char firstChar = token.charAt(0);

                if (Character.isDigit(firstChar) || firstChar == '.') {
                    postfix.append(token).append(" ");
                } else if (Character.isLetter(firstChar)) {
                    handleFunctionOrConstant(postfix, operators, token);
                } else if (isOperator(String.valueOf(firstChar))) {
                    handleOperator(postfix, operators, String.valueOf(firstChar));
                } else if (firstChar == '(') {
                    operators.push("(");
                } else if (firstChar == ')') {
                    while (!operators.isEmpty() && !operators.peek().equals("(")) {
                        postfix.append(operators.pop()).append(" ");
                    }
                    operators.pop(); // Discard the "("
                }
            }
        }

        while (!operators.isEmpty()) {
            postfix.append(operators.pop()).append(" ");
        }

        return postfix.toString().trim();
    }

    // Handles functions and constants during infix to postfix conversion
    private void handleFunctionOrConstant(StringBuilder postfix, Stack<String> operators, String token) {
        String lowercaseToken = token.toLowerCase();

        if (isOperator(lowercaseToken)) {
            handleOperator(postfix, operators, lowercaseToken);
        } else if (lowercaseToken.equals("pi") || lowercaseToken.equals("e")) {
            postfix.append(lowercaseToken).append(" ");
        } else if (lowercaseToken.equals("exp")) {
            // Replace "exp" with "2.718282"
            postfix.append("2.718282").append(" ");
        } else if (isFunction(lowercaseToken)) {
            operators.push(lowercaseToken);
        } else {
            // Assuming it's an invalid input (no variables)
            JOptionPane.showMessageDialog(null, "Invalid input: " + token);
        }
    }

    // check if the operater is a mathmatical function
    private boolean isFunction(String fun) {
        return fun.equals("sin") || fun.equals("cos") || fun.equals("tan") || fun.equals("cot") ||
                fun.equals("asin") || fun.equals("acos") || fun.equals("atan") || fun.equals("acot") ||
                fun.equals("log") || fun.equals("ln") || fun.equals("sgn") ||
                fun.equals("exp");
    }

    private void handleOperator(StringBuilder postfix, Stack<String> operators, String token) {
        while (!operators.isEmpty() && PriorityOp(operators.peek()) >= PriorityOp(token)) {
            postfix.append(operators.pop()).append(" ");
        }
        operators.push(token);
    }

    private double calculatePostfix(String postfix) {
        Stack<Double> values = new Stack<>();
        String[] tokens = postfix.split("\\s+");

        for (String token : tokens) {
            if (Character.isDigit(token.charAt(0)) || (token.length() > 1 && token.charAt(0) == '-' &&
                    Character.isDigit(token.charAt(1)))) {
                values.push(Double.parseDouble(token));
            } else if (isOperator(token)) {
                executeOperation(values, token);
            }
        }

        return values.pop();
    }

    // define all of the operaters that are recognized by the calculator
    private static boolean isOperator(String operator) {
        return operator.equals("+") || operator.equals("-") || operator.equals("*") || operator.equals("/")
                || operator.equals("^") || operator.equals("%")
                ||
                operator.equals("!") || operator.equals("pi") ||
                operator.equals("ln") || operator.equals("sin") || operator.equals("cos") || operator.equals("tan")
                || operator.equals("cot") ||
                operator.equals("log") || operator.equals("sgn") ||
                operator.equals("asin") || operator.equals("acos") || operator.equals("atan") || operator.equals("acot")
                || operator.equals("exp") ||
                operator.equals("and");
    }

    // Returns the priority level of an operator
    private static int PriorityOp(String op) {
        switch (op) {
            case "+":
            case "-":
            case "and":
                return 1;
            case "*":
            case "/":
            case "%":
                return 2;
            case "^":
                return 3;
            case "sin":
            case "cos":
            case "tan":
            case "cot":
            case "asin":
            case "acos":
            case "atan":
            case "acot":
            case "log":
            case "ln":
            case "sgn":
                return 4;
            case "!":
                return 5;
            case "pi":
            case "exp":
                return 6;
            default:
                return 0;
        }
    }

    // Executes mathematical operations based on the operator
    private static void executeOperation(Stack<Double> values, String operator) {
        switch (operator) {
            case "+":
                values.push(values.pop() + values.pop());
                break;
            case "-":
                double num1 = values.pop();
                double num2 = values.pop();
                values.push(num2 - num1);
                break;
            case "*":
                values.push(values.pop() * values.pop());
                break;
            case "/":
                double denominator = values.pop();
                double numerator = values.pop();
                if (denominator != 0) {
                    values.push(numerator / denominator);
                } else {
                    JOptionPane.showMessageDialog(null, "Zero Division");
                }
                break;
            case "^":
                double expo = values.pop();
                double base = values.pop();
                values.push(Math.pow(base, expo));
                break;
            case "%":
                double mod = values.pop();
                double num = values.pop();
                values.push(num % mod);
                break;
            case "sin":
                values.push(Math.sin(values.pop()));
                break;
            case "cos":
                values.push(Math.cos(values.pop()));
                break;
            case "tan":
                values.push(Math.tan(values.pop()));
                break;
            case "cot":
                values.push(1.0 / Math.tan(values.pop()));
                break;
            case "asin":
                values.push(Math.asin(values.pop()));
                break;
            case "acos":
                values.push(Math.acos(values.pop()));
                break;
            case "atan":
                values.push(Math.atan(values.pop()));
                break;
            case "acot":
                values.push(1.0 / Math.atan(values.pop()));
                break;
            case "ln":
                values.push(Math.log(values.pop()));
                break;
            case "log":
                values.push(Math.log10(values.pop()));
                break;
            case "exp":
                values.push(Math.exp(values.pop()));
                break;
            case "sgn":
                values.push(Math.signum(values.pop()));
                break;
            case "pi":
                values.push(Math.PI);
                break;
            case "!":
                values.push(factorial(values.pop()));
                break;
            case "and":
                Long bits1 = Double.doubleToLongBits(values.pop());
                Long bits2 = Double.doubleToLongBits(values.pop());
                Long resultBits = bits1 & bits2;
                values.push(Double.longBitsToDouble(resultBits));
                break;
        }
    }

    // Calculates the factorial of a number
    private static double factorial(double num) {
        if (num < 0) {
            JOptionPane.showMessageDialog(null, "Factorial of a negative number is undefined");
            return Double.NaN;
        }

        double result = 1;
        for (int i = 2; i <= num; i++) {
            result *= i;
        }

        if (num != (int) num) {
            result *= Math.exp(Math.log(Math.sqrt(2 * Math.PI * num) * Math.pow(num / Math.E, num)));
        }

        return result;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnimatedCalculator());
    }
}
