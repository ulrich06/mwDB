package org.mwg.ml.common.mathexp.impl;


import org.mwg.Node;
import org.mwg.ml.common.mathexp.MathVariableResolver;

import java.util.*;

public class MathExpressionEngine implements org.mwg.ml.common.mathexp.MathExpressionEngine {

    private MathVariableResolver varResolver;

    public static final char decimalSeparator = '.';
    public static final char minusSign = '-';
    private final MathToken[] _cacheAST;
    private HashMap<String, Double> vars = new HashMap<String, Double>();

    private MathExpressionEngine(String expression) {

        vars.put("PI", Math.PI);
        vars.put("TRUE", 1.0);
        vars.put("FALSE", 0.0);

        varResolver = new MathVariableResolver() {
            @Override
            public Double resolve(String potentialVarName) {
                return vars.get(potentialVarName);
            }
        };

        _cacheAST = buildAST(shuntingYard(expression));
    }


    static class LRUCache extends LinkedHashMap<String, org.mwg.ml.common.mathexp.MathExpressionEngine> {

        int cacheSize;

        public LRUCache(int cacheSize) {
            super(16, 0.75f, true);
            this.cacheSize = cacheSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, org.mwg.ml.common.mathexp.MathExpressionEngine> eldest) {
            return size() >= cacheSize;
        }

    }

    private static LinkedHashMap<String, org.mwg.ml.common.mathexp.MathExpressionEngine> cached = new LRUCache(100);


    public static synchronized org.mwg.ml.common.mathexp.MathExpressionEngine parse(String p_expression) {
        org.mwg.ml.common.mathexp.MathExpressionEngine cachedEngine = cached.get(p_expression);
        if (cachedEngine != null) {
            return cachedEngine;
        }
        org.mwg.ml.common.mathexp.MathExpressionEngine newEngine = new MathExpressionEngine(p_expression);
        cached.put(p_expression, newEngine);
        return newEngine;
    }


    /**
     * @native ts
     * return !isNaN(+st);
     */
    static boolean isNumber(String st) {
        if (st.charAt(0) == minusSign && st.length() == 1)
            return false;
        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (!isDigit(ch) && ch != minusSign && ch != decimalSeparator) {
                return false;
            }
        }
        return true;
    }

    /**
     * @native ts
     * var cc = c.charCodeAt(0);
     * if ( cc >= 0x30 && cc <= 0x39 ){
     * return true ;
     * }
     * return false ;
     */
    static boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    /**
     * @native ts
     * var cc = c.charCodeAt(0);
     * if ( ( cc >= 0x41 && cc <= 0x5A ) || ( cc >= 0x61 && cc <= 0x7A ) ){
     * return true ;
     * }
     * return false ;
     */
    static boolean isLetter(char c) {
        return Character.isLetter(c);
    }

    /**
     * @native ts
     * var cc = c.charCodeAt(0);
     * if ( ( cc >= 0x0009 && cc <= 0x000D ) || ( cc == 0x0020 ) || ( cc == 0x0085 ) || ( cc == 0x00A0 ) ){
     * return true ;
     * }
     * return false ;
     */
    static boolean isWhitespace(char c) {
        return Character.isWhitespace(c);
    }


    /**
     * Implementation of the <i>Shunting Yard</i> algorithm to transform an
     * infix mathexp to a RPN mathexp.
     *
     * @param expression The input mathexp in infx.
     * @return A RPN representation of the mathexp, with each token as a list
     * member.
     */
    private List<String> shuntingYard(String expression) {
        List<String> outputQueue = new ArrayList<String>();
        Stack<String> stack = new Stack<String>();
        MathExpressionTokenizer tokenizer = new MathExpressionTokenizer(expression);
        String lastFunction = null;
        String previousToken = null;
        while (tokenizer.hasNext()) {
            String token = tokenizer.next();
            if (MathEntities.getINSTANCE().functions.keySet().contains(token.toUpperCase())) {
                stack.push(token);
                lastFunction = token;
            } else if (PrimitiveHelper.equals(",", token)) {
                while (!stack.isEmpty() && !PrimitiveHelper.equals("(", stack.peek())) {
                    outputQueue.add(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new RuntimeException("Parse error for function '"
                            + lastFunction + "'");
                }
            } else if (MathEntities.getINSTANCE().operators.keySet().contains(token)) {
                MathOperation o1 = MathEntities.getINSTANCE().operators.get(token);
                String token2 = stack.isEmpty() ? null : stack.peek();
                while (MathEntities.getINSTANCE().operators.keySet().contains(token2)
                        && ((o1.isLeftAssoc() && o1.getPrecedence() <= MathEntities.getINSTANCE().operators
                        .get(token2).getPrecedence()) || (o1
                        .getPrecedence() < MathEntities.getINSTANCE().operators.get(token2)
                        .getPrecedence()))) {
                    outputQueue.add(stack.pop());
                    token2 = stack.isEmpty() ? null : stack.peek();
                }
                stack.push(token);
            } else if (PrimitiveHelper.equals("(", token)) {
                if (previousToken != null) {
                    if (isNumber(previousToken)) {
                        throw new RuntimeException("Missing operator at character position " + tokenizer.getPos());
                    }
                }
                stack.push(token);
            } else if (PrimitiveHelper.equals(")", token)) {
                while (!stack.isEmpty() && !PrimitiveHelper.equals("(", stack.peek())) {
                    outputQueue.add(stack.pop());
                }
                if (stack.isEmpty()) {
                    throw new RuntimeException("Mismatched parentheses");
                }
                stack.pop();
                if (!stack.isEmpty()
                        && MathEntities.getINSTANCE().functions.keySet().contains(stack.peek().toUpperCase())) {
                    outputQueue.add(stack.pop());
                }
            } else {
                outputQueue.add(token);
            }
            previousToken = token;
        }
        while (!stack.isEmpty()) {
            String element = stack.pop();
            if (PrimitiveHelper.equals("(", element) || PrimitiveHelper.equals(")", element)) {
                throw new RuntimeException("Mismatched parentheses");
            }
            outputQueue.add(element);
        }
        return outputQueue;
    }

    @Override
    public final double eval(Node context) {
        if (this._cacheAST == null) {
            throw new RuntimeException("Call parse before");
        }
        Stack<Double> stack = new Stack<Double>();
        for (int ii = 0; ii < _cacheAST.length; ii++) {
            MathToken mathToken = _cacheAST[ii];
            switch (mathToken.type()) {
                case 0:
                    double v1 = stack.pop();
                    double v2 = stack.pop();
                    MathOperation castedOp = (MathOperation) mathToken;
                    stack.push(castedOp.eval(v2, v1));
                    break;
                case 1:
                    MathFunction castedFunction = (MathFunction) mathToken;
                    double[] p = new double[castedFunction.getNumParams()];
                    for (int i = castedFunction.getNumParams() - 1; i >= 0; i--) {
                        p[i] = stack.pop();
                    }
                    stack.push(castedFunction.eval(p));
                    break;
                case 2:
                    MathDoubleToken castedDouble = (MathDoubleToken) mathToken;
                    stack.push(castedDouble.content());
                    break;
                case 3:
                    MathFreeToken castedFreeToken = (MathFreeToken) mathToken;
                    if (varResolver.resolve(castedFreeToken.content()) != null) {
                        stack.push(varResolver.resolve(castedFreeToken.content()));
                    } else {
                        if (context != null) {
                            if (PrimitiveHelper.equals("TIME", castedFreeToken.content())) {
                                stack.push((double) context.time());
                            } else {
                                String tokenName = castedFreeToken.content().trim();
                                Object resolved;
                                String cleanName;
                                if (tokenName.startsWith("{") && tokenName.endsWith("}")) {
                                    resolved = context.get(castedFreeToken.content().substring(1, tokenName.length() - 1));
                                    cleanName = castedFreeToken.content().substring(1, tokenName.length() - 1);
                                } else {
                                    resolved = context.get(castedFreeToken.content());
                                    cleanName = castedFreeToken.content();
                                }
                                if (cleanName.startsWith("$")) {
                                    cleanName = cleanName.substring(1);
                                }
                                if (resolved != null) {
                                    double resultAsDouble = PrimitiveHelper.parseDouble(resolved.toString());
                                    //ToDo uncomment and unvalidate cache
                                    //vars.put(cleanName, resultAsDouble);
                                    String valueString = resolved.toString();
                                    if (PrimitiveHelper.equals(valueString, "true")) {
                                        stack.push(1.0);
                                    } else if (PrimitiveHelper.equals(valueString, "false")) {
                                        stack.push(0.0);
                                    } else {
                                        try {
                                            stack.push(resultAsDouble);
                                        } catch (Exception e) {
                                            //noop
                                        }
                                    }

                                } else {
                                    throw new RuntimeException("Unknow variable for name " + castedFreeToken.content());
                                }
                            }
                        } else {
                            throw new RuntimeException("Unknow variable for name " + castedFreeToken.content());
                        }
                    }
                    break;
            }
        }
        Double result = stack.pop();
        if (result == null) {
            return 0;
        } else {
            return result;
        }
    }

    public MathToken[] buildAST(List<String> rpn) {
        MathToken[] result = new MathToken[rpn.size()];
        for (int ii = 0; ii < rpn.size(); ii++) {
            String token = rpn.get(ii);
            if (MathEntities.getINSTANCE().operators.keySet().contains(token)) {
                result[ii] = MathEntities.getINSTANCE().operators.get(token);
            } else if (MathEntities.getINSTANCE().functions.keySet().contains(token.toUpperCase())) {
                result[ii] = MathEntities.getINSTANCE().functions.get(token.toUpperCase());
            } else {

                if (token.length() > 0 && isLetter(token.charAt(0))) {
                    result[ii] = new MathFreeToken(token);
                } else {
                    try {
                        double parsed = PrimitiveHelper.parseDouble(token);
                        result[ii] = new MathDoubleToken(parsed);
                    } catch (Exception e) {
                        result[ii] = new MathFreeToken(token);
                    }
                }
            }
        }
        return result;
    }

    /*
    @Override
    public void setVarResolver(MathVariableResolver p_resolver) {
        this.varResolver = p_resolver;
    }*/

}
