package com.barmin.matrixcalculator;

import com.barmin.matrixcalculator.matrixLib.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Barmin on 15.01.2017.
 */

public class Parser {

    private String inputString;

    private Matrix oneMatrix;

    static final Pattern justNumb =  Pattern.compile("^-?d+[,.]?d*$");

    private List<Matrix> WorkingMatrix = new ArrayList<>();

    public Parser(String str){
        inputString = str;
        ownMatrixCollection();
    }

    private void ownMatrixCollection(){
        for (int i = 0; i < Storage.matrixCollection.size(); i++)
            WorkingMatrix.add(Storage.matrixCollection.get(i));
    }

    public boolean valid(){
        //предполагается, что во вненем коде проверяется,
        //можно ли получить результат
        boolean flag = false;
        for (int i = 0; i < WorkingMatrix.size(); i++){
            if (inputString.contains(WorkingMatrix.get(i).name)){
                flag = true;
            }
        }
        if (!flag) { return false; }

        int br1 = countOfChar(inputString, '(');
        int br2 = countOfChar(inputString, ')');
        return br1 == br2 ? true : false;
    }

    public Matrix getMatrix(){
        if(oneMatrix!=null)
            return oneMatrix;
        return new Matrix(1,1);
    }

    public boolean oneMatrix(){
        Pattern p= Pattern.compile("^[+|-]*\\(*[+|-]*\\w+\\)*[+|-]*$");
        Matcher m = p.matcher(inputString);
        if(m.find()){
            String res = m.group();
            if(bracketExpression(res))
                res = deleteBracket(res).toString();
            String part1 = getFirstPart(res);
            boolean s1 = getSign(part1);
            StringBuilder M1 = getFirstName(res);
            int m1 = getID(M1);
            if(s1) oneMatrix = WorkingMatrix.get(m1);
            else oneMatrix = Matrix.multiblyByNumber(WorkingMatrix.get(m1),-1);
            return true;
        }
        return false;
    }

    private int countOfChar(String str, char ch){
        int n = 0;
        for (int i = 0; i < str.length(); i++){
            if (str.charAt(i) == ch) { n++; }
        }
        return n;
    }

    private boolean bracketExpression(String str){
        if (str.contains("(")) { return true; }
        return false;
    }

    private StringBuilder bracket(StringBuilder str){
        int n = 0;
        int ind = str.lastIndexOf("(");
        for (int i = ind + 1; i < str.length(); i++){
            n++;
            if (str.charAt(i) == ')'){
                return new StringBuilder(str.substring(ind+1, i));
                //return new StringBuilder(str.substring(ind+1, n));
            }
        }
        return new StringBuilder();
    }

    private StringBuilder multiplyString(String str) throws Exception{
        //часть 1 - матрица на матрицу
        Matcher m;
        String multiplication = new String();
        Pattern mult = Pattern.compile("\\-?_??[A-z]+[0-9]*\\*\\-?_??[A-z]+[0-9]*");
        m = mult.matcher(str);
        if (m.find())
        {
            multiplication = m.group();
            //проверка каждой матрицы с каждой, включая усножение на себя
            for (int i = 0; i < WorkingMatrix.size(); i++){
                for (int j = 0; j < WorkingMatrix.size(); j++){
                    if (multiplication.equals(WorkingMatrix.get(i).name + "*" + WorkingMatrix.get(j).name)){
                        Matrix X = Matrix.multiplyByMatrix(WorkingMatrix.get(i),WorkingMatrix.get(j));
                        X.name = "_w" + String.valueOf(WorkingMatrix.size());
                        WorkingMatrix.add(X);
                        str = str.replace(multiplication, X.name);
                        return new StringBuilder(str);
                    }
                    if (multiplication.equals("-" + WorkingMatrix.get(i).name + "*" + WorkingMatrix.get(j).name)){
                        Matrix X = Matrix.multiplyByMatrix(WorkingMatrix.get(i),WorkingMatrix.get(j));
                        X = Matrix.multiblyByNumber(X,-1);
                        X.name = "_w" + String.valueOf(WorkingMatrix.size());
                        WorkingMatrix.add(X);
                        str = str.replace(multiplication, X.name);
                        return new StringBuilder(str);
                    }
                    if (multiplication.equals(WorkingMatrix.get(i).name + "*" + "-" + WorkingMatrix.get(j).name)){
                        Matrix X = Matrix.multiplyByMatrix(WorkingMatrix.get(i),WorkingMatrix.get(j));
                        X = Matrix.multiblyByNumber(X,-1);
                        X.name = "_w" + String.valueOf(WorkingMatrix.size());
                        WorkingMatrix.add(X);
                        str = str.replace(multiplication, X.name);
                        return new StringBuilder(str);
                    }
                    if (multiplication.equals("-" + WorkingMatrix.get(i).name + "*" + "-" + WorkingMatrix.get(j).name)){
                        Matrix X = Matrix.multiplyByMatrix(WorkingMatrix.get(i),WorkingMatrix.get(j));
                        X.name = "_w" + String.valueOf(WorkingMatrix.size());
                        WorkingMatrix.add(X);
                        str = str.replace(multiplication, X.name);
                        return new StringBuilder(str);
                    }
                }
            }
        }
        //часть 2 - матрица на число
        Pattern mult1 = Pattern.compile("\\-?_??[A-z]+[0-9]*\\*\\-?\\d+[,.]?\\d*");
        Pattern numb = Pattern.compile("\\-?\\d+[,.]?\\d*$");
        m = mult1.matcher(str);
        if (m.matches()){
            multiplication = m.group();
            String sn = numb.matcher(multiplication).group();
            String N = sn.replace('.', ',');
            multiplication = multiplication.replace(sn, N);
            str = str.replace(sn, N);
            double n = Double.valueOf(N);
            for (int i = 0; i < WorkingMatrix.size(); i++){
                if (multiplication.equals(WorkingMatrix.get(i).name + "*" + n)){
                    Matrix X = Matrix.multiblyByNumber(WorkingMatrix.get(i),n);
                    X.name = "_w" + String.valueOf(WorkingMatrix.size());
                    WorkingMatrix.add(X);
                    str = str.replace(multiplication, X.name);
                    return new StringBuilder(str);
                }
                if (multiplication.equals("-" + WorkingMatrix.get(i).name + "*" + n)){
                    Matrix X = Matrix.multiblyByNumber(WorkingMatrix.get(i),n);
                    X = Matrix.multiblyByNumber(X,-1);
                    X.name = "_w" + String.valueOf(WorkingMatrix.size());
                    WorkingMatrix.add(X);
                    str = str.replace(multiplication, X.name);
                    return new StringBuilder(str);
                }
            }
        }
        //часть 3 - число на матрицу
        Pattern mult2 = Pattern.compile("-?\\d+[,.]?\\d*\\*\\-?_??[A-z]+[0-9]*");
        m = mult2.matcher(str);
        Pattern numb2 = Pattern.compile("^\\-?\\d+[,.]?\\d*");
        if (m.matches()){
            multiplication = m.group();
            String sn1 = numb2.matcher(multiplication).group();
            String N1 = sn1.replace('.', ',');
            multiplication = multiplication.replace(sn1, N1);
            str = str.replace(sn1, N1);
            double n1 = Double.valueOf(N1);

            for (int i = 0; i < WorkingMatrix.size(); i++){
                if (multiplication.equals(n1 + "*" + WorkingMatrix.get(i).name)){
                    Matrix X = Matrix.multiblyByNumber(WorkingMatrix.get(i),n1);
                    X.name = "_w" + String.valueOf(WorkingMatrix.size());
                    WorkingMatrix.add(X);
                    str = str.replace(multiplication, X.name);
                    return new StringBuilder(str);
                }
                if (multiplication.equals(n1 + "*" + "-" + WorkingMatrix.get(i).name)){
                    Matrix X = Matrix.multiblyByNumber(WorkingMatrix.get(i),n1);
                    X = Matrix.multiblyByNumber(X,-1);
                    X.name = "_w" + String.valueOf(WorkingMatrix.size());
                    WorkingMatrix.add(X);
                    str = str.replace(multiplication, X.name);
                    return new StringBuilder(str);
                }
            }
        }
        //часть 4 число на число
        Pattern mult3 = Pattern.compile("\\-?\\d+[,.]?\\d*\\*\\-?\\d+[,.]?\\d*");
        m = mult3.matcher(str);
        if (m.matches()){
            String[] numbersString = m.group().split("\\*");
            String N1 = numbersString[0].replace('.', ',');
            String N2 = numbersString[1].replace('.', ',');
            double k1 = Double.valueOf(N1);
            double k2 = Double.valueOf(N2);
            double k = k1 * k2;
            str = str.replace(m.group(), String.valueOf(k));
            return new StringBuilder(str);
        }
        return new StringBuilder();
    }

    private StringBuilder addOrSub(String str)throws Exception{
        Pattern p= Pattern.compile("[+|-]+\\w+[+|-]+-?\\w+|[+|-]*\\w+[+|-]+-?\\w+");
        Matcher m = p.matcher(str);
        if(m.find()){
            String res = m.group();
            String part1 = getFirstPart(res);
            String part2 = getSecondPart(res);
            boolean s1 = getSign(part1);
            boolean s2 = getSign(part2);
            StringBuilder M1 = getFirstName(res);
            StringBuilder M2 = getSecondName(res);
            int m1 = getID(M1);
            int m2 = getID(M2);
            Matrix result = getResMatrix(s1,s2,m1,m2);
            WorkingMatrix.add(result);
            str = str.replace(res, result.name);
            return new StringBuilder(str);
        }
        Pattern p2= Pattern.compile("^[+|-]*\\w+");
        Matcher m2 = p2.matcher(str);
        if(m2.find()){
            return normalizeStart(m2.group());
        }
        return new StringBuilder();
    }

    private StringBuilder filter(StringBuilder str){
        Pattern p= Pattern.compile("\\w+[+|-]+$");
        Matcher m = p.matcher(str);
        if(m.find()){
            return normalizeEnd(m.group());
        }
        return str;
    }

    private StringBuilder normalizeEnd(String s){
        int n = 0;
        for(int i=s.length()-1;i>=0;i--){
            n++;
            //if(Character.isAlphabetic(s.charAt(i))|Character.isDigit(s.charAt(i)))
            if(isFit(s.charAt(i)))
                return  new StringBuilder(s.substring(0,n+1));
        }
        return new StringBuilder();
    }

    private StringBuilder normalizeStart(String s){
        String part1 = getFirstPart(s);
        boolean s1 = getSign(part1);
        StringBuilder M1 = getFirstName(s);
        int m1 = getID(M1);
        if(s1) return new StringBuilder(s.replace(s,M1));
        else{
            Matrix X = Matrix.multiblyByNumber(WorkingMatrix.get(m1),-1);
            X.name = "_w" + String.valueOf(WorkingMatrix.size());
            WorkingMatrix.add(X);
            return new StringBuilder(s.replace(s,X.name));
        }
    }

    private Matrix getResMatrix(boolean s1, boolean s2, int id1, int id2) throws Exception {
        if(s1&s2){
            Matrix X = Matrix.addMatrix(WorkingMatrix.get(id1),WorkingMatrix.get(id2));
            X.name = "_w" + String.valueOf(WorkingMatrix.size());
            return X;
        }
        else if(s1&!s2){
            Matrix X = Matrix.subtract(WorkingMatrix.get(id1),WorkingMatrix.get(id2));
            X.name = "_w" + String.valueOf(WorkingMatrix.size());
            return X;
        }
        else if(!s1&s2){
            Matrix X = Matrix.subtract(WorkingMatrix.get(id2),WorkingMatrix.get(id1));
            X.name = "_w" + String.valueOf(WorkingMatrix.size());
            return X;
        }
        else if(!s1&!s2){
            Matrix X = Matrix.addMatrix(WorkingMatrix.get(id2),WorkingMatrix.get(id1));
            X = Matrix.multiblyByNumber(X,-1);
            X.name = "_w" + String.valueOf(WorkingMatrix.size());
            return X;
        }
        return new Matrix(1,1);
    }

    private int getID(StringBuilder name) {
        int i;
        for (i = 0; i < WorkingMatrix.size(); i++)
            if (WorkingMatrix.get(i).name.equals(name.toString()))
                return i;
        return 0;
    }

    private String getFirstPart(String str){
        for(int i=0; i<str.length();i++)
            //if(Character.isAlphabetic(str.charAt(i))|str.charAt(i)=='_')
            if(isFit(str.charAt(i)))
                return str.substring(0,i);
        return new String();
    }

    private String getSecondPart(String str){
        int s1 = 0;
        int s2 = 0;
        for(int i=str.length()-1; i>=0;i--)
            if(str.charAt(i)=='+'|str.charAt(i)=='-'){
                s1 = i;
                break;
            }
        for(int i=s1; i>=0;i--)
            // if(Character.isAlphabetic(str.charAt(i))|Character.isDigit(str.charAt(i))){
            if(isFit(str.charAt(i))){
                s2 = i;
                break;
            }
        return str.substring(s2+1,s1+1);
    }

    private boolean getSign(String str){
        int count = 0;
        if(str.equals("")) return true;
        for(int i=0;i<str.length();i++)
            if(str.charAt(i)=='-') count++;
        if(count%2==0) return true;
        else return false;
    }

    private StringBuilder getFirstName(String str){
        int s1 = 0;
        int s2 = str.length();
        for(int i=0; i<str.length();i++)
            //if(Character.isAlphabetic(str.charAt(i))|str.charAt(i)=='_'){
            if(isFit(str.charAt(i))){
                s1 = i;
                break;
            }
        for(int i=s1; i<str.length();i++)
            if((str.charAt(i)=='+')|(str.charAt(i)=='-')){
                s2 = i;
                break;
            }
        return new StringBuilder(str.substring(s1,s2));
    }

    private StringBuilder getSecondName(String str){
        for(int i=str.length()-1;i>=0;i--)
            if((str.charAt(i)=='+')|(str.charAt(i)=='-'))
                return new StringBuilder(str.substring(i+1));
        return new StringBuilder();
    }

    private StringBuilder deleteBracket(String str){
        Pattern b = Pattern.compile("\\(|\\)");
        //часть 1
        boolean yes = false;
        Pattern brackExpr = Pattern.compile("\\(\\w+[0-9]*\\)");
        Matcher m = brackExpr.matcher(str);
        //удаляет скобки вокруг переменной
        while (m.find()){
            yes = true;
            String s = m.group().replaceAll(b.pattern(),"");
            str = str.replace(m.group(),s);
            m = brackExpr.matcher(str);
        }
        if(yes)
            return new StringBuilder(str);

        Pattern p= Pattern.compile("^\\(*[+|-]*\\w+\\)*$");
        m = p.matcher(str);
        while (m.find()){
            yes = true;
            String s = m.group().replaceAll(b.pattern(),"");
            str = str.replace(m.group(),s);
            m = brackExpr.matcher(str);
        }
        if(yes)
            return new StringBuilder(str);


        Pattern brackExpr1 = Pattern.compile("\\(\\-?\\d+[,.]?\\d*\\)");
        Matcher m1 = brackExpr1.matcher(str);
        //удаляет скобки вокруг числа
        while (m1.find()){
            yes = true;
            String s = m1.group().replaceAll(b.pattern(),"");
            str = str.replace(m1.group(),s);
            m1 = brackExpr.matcher(str);
        }
        if(yes)
            return new StringBuilder(str);

        return new StringBuilder();
    }

    private Matrix getMatrixFromString(String str) throws Exception {
        for (int i = WorkingMatrix.size() - 1; i >= 0; i--){
            if (WorkingMatrix.get(i).name.equals(str)) { return WorkingMatrix.get(i); }
        }
        throw new Exception("При выполнении операции произошла ошибка");
    }

    public Matrix getResult() throws Exception{
        //где то уже вызвали метод Valid и он вернул true
        StringBuilder control = new StringBuilder();
        if (bracketExpression(inputString)){
            StringBuilder s1 = new StringBuilder(inputString);
            StringBuilder s2 = new StringBuilder();
            StringBuilder s3 = new StringBuilder();
            boolean flag = true;
                while (contains(s1,'(')){
                    control.delete(0,control.length());
                    control.append(s1);
                    flag = true;
                    s2 = bracket(s1);
                    s3 = s2;
                    if(control.equals(s2)) throw new Exception();
                    while (contains(s2,'*')){
                        control.delete(0,control.length());
                        control.append(s2);
                        Matcher number = justNumb.matcher(s2);
                        if (number.matches()) { break; }
                        flag = false;
                        s2 = multiplyString(s2.toString());
                        if(strEquals(control,s2)) throw new Exception();
                    }
                    while (contains(s2,'-')|contains(s2,'+')){
                        control.delete(0,control.length());
                        control.append(s2);
                        Matcher number = justNumb.matcher(s2);
                        if (number.matches()) { break; }
                        flag = false;
                        s2 = addOrSub(s2.toString());
                        if(strEquals(control,s2)) throw new Exception();
                    }
                    if (flag){
                        control.delete(0,control.length());
                        control.append(s1);
                        s1 = deleteBracket(s1.toString());
                        if(strEquals(control,s1)) throw new Exception();
                    }
                    String repl = s1.toString().replace(s3.toString(),s2.toString());
                    s1 = new StringBuilder(repl);
                }
                s1 = filter(s1);
                while (contains(s1,'*'))
                {
                    control.delete(0,control.length());
                    control.append(s1);
                    s1 = multiplyString(s1.toString());
                    if(strEquals(control,s1)) throw new Exception();
                }
                while (contains(s1,'-')|contains(s1,'+')){
                    control.delete(0,control.length());
                    control.append(s1);
                    Matcher number = justNumb.matcher(s1);
                    if (number.matches()) { break; }
                    s1 = addOrSub(s1.toString());
                    if(strEquals(control,s1)) throw new Exception();
                }
                return getMatrixFromString(s1.toString());
        }
        else
        {
            //если выражение не содержит скобки
            StringBuilder s1 = new StringBuilder(inputString);

                while (contains(s1,'*'))
                {
                    control.delete(0,control.length());
                    control.append(s1);
                    s1 = multiplyString(s1.toString());
                    if(strEquals(control,s1)) throw new Exception();
                }
                while (contains(s1,'-')|contains(s1,'+')){
                    control.delete(0,control.length());
                    control.append(s1);
                    Matcher number = justNumb.matcher(s1);
                    if (number.matches()) { break; }
                    s1 = addOrSub(s1.toString());
                    s1 = filter(s1);
                    if(strEquals(control,s1)) throw new Exception();
                }
                s1 = filter(s1);
                return getMatrixFromString(s1.toString());        }
    }

    private boolean contains(StringBuilder s, char ch){
        for(int i=0; i<s.length();i++)
            if(s.charAt(i)==ch) return true;
        return false;
    }

    private boolean strEquals(StringBuilder s1, StringBuilder s2){
        if(s1.length()!=s2.length())
            return false;
        for(int i=0; i<s1.length(); i++)
            if(s1.charAt(i)!=s2.charAt(i))
                return false;
        return true;
    }

    private static boolean isFit(char ch){
        Pattern p = Pattern.compile("\\w");
        Matcher m = p.matcher(String.valueOf(ch));
        if(m.matches()) return true;
        else return false;
    }
}