package com.barmin.matrix;

import android.app.Notification;
import android.app.usage.UsageEvents;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.ListPopupWindow;

import com.example.barmin.matrix.Storage;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * Created by Barmin on 31.12.2016.
 */

public class Matrix {

    public Matrix(int row, int col){
        matr = new double[row][col];
        this.col = col;
        this.row = row;
        round = 3;
    }

    public Matrix(double[][] matr){
        this.matr = matr;
        this.row = matr.length;
        this.col = matr[0].length;
        round = 3;
    }

    public static NumberFormat nf = new DecimalFormat("#.###");

    private double[][] matr;

    private int col;

    private int row;

    public StringBuilder message;

    public int columns(){
        return matr[0].length;
    }

    public int rows(){
        return matr.length;
    }

    public String name;

    public int round;

    public volatile static boolean resume;

    private boolean zero(){
        for (double[] z : matr)
        {
            for(double d: z)
                if (d != 0) { return false; }
        }
        return true;
    }

    @Override
    public String toString(){
        return this.name;
    }

    //region Operations
    public static Matrix addMatrix(Matrix A, Matrix B) throws SumExceptiom{
        boolean canAdd = (A.col == B.col) && (A.row == B.row) ? true : false;
        if (!canAdd) { throw new SumExceptiom("Invalid summation"); }
        else{
            Matrix X = new Matrix(A.row, A.col);
            if (A.round > B.round) { X.round = A.round; } else { X.round = B.round; }
            for (int i = 0; i < A.row; i++){
                for (int j = 0; j < A.col; j++){
                    X.matr[i][j] = A.matr[i][j] + B.matr[i][j];
                }
            }
            //X.rounding();
            return X;
        }
    }

    public static Matrix subtract(Matrix A, Matrix B) throws SumExceptiom{
        boolean canAdd = (A.col == B.col) && (A.row == B.row) ? true : false;
        if (!canAdd) { throw new SumExceptiom("Invalid subtraction"); }
        else{
            Matrix X = new Matrix(A.row, A.col);
            if (A.round > B.round) { X.round = A.round; } else { X.round = B.round; }
            for (int i = 0; i < A.row; i++){
                for (int j = 0; j < A.col; j++){
                    X.matr[i][j] = A.matr[i][j] - B.matr[i][j];
                }
            }
            //X.rounding();
            return X;
        }
    }

    public static Matrix multiblyByNumber(Matrix M, double n){
        Matrix X = new Matrix(M.row, M.col);
        X.round = M.round;
        for (int i = 0; i < M.row; i++){
            for (int j = 0; j < M.col; j++){
                X.matr[i][j] = M.matr[i][j] * n;
            }
        }
        //X.rounding();
        return X;
    }

    public static Matrix multiplyByMatrix(Matrix A, Matrix B) throws MultException{
        boolean canMultiply = A.col == B.row ? true : false;
        if (!canMultiply) { throw new MultException("Invalid multiplication"); }
        else{
            Matrix X = new Matrix(A.row, B.col);
            if (A.round > B.round) { X.round = A.round; } else { X.round = B.round; }
            for (int i = 0; i < A.row; i++){
                for (int j = 0; j < B.col; j++){
                    for (int k = 0; k < B.row; k++){
                        X.matr[i][j] = X.matr[i][j] + A.matr[i][k] * B.matr[k][j];
                    }
                }
            }
            //X.rounding();
            return X;
        }
    }

    public Matrix transpose(){
        Matrix X = new Matrix(col, row);
        X.round = round;
        for (int i = 0; i < row; i++){
            for (int j = 0; j < col; j++){
                X.matr[j][i] = matr[i][j];
            }
        }
        //X.rounding();
        return X;
    }

    public Matrix minor(int row, int col){
        //Разложение по строке
        boolean flagRow = false;
        boolean flagCol = false;
        Matrix X = new Matrix(this.row - 1, this.col - 1);
        for (int i = 0; i < this.row; i++){
            int z = i;
            //Разложение идет по строке, поэтому при просмотре каждой новой строки
            //флаг столбца надо сбрасывать
            flagCol = false;
            if (i == row){
                flagRow = true;
                continue;
            }
            if (flagRow) { z = i - 1; }

            for (int j = 0; j < this.col; j++){
                int s = j;
                if (j == col){
                    flagCol = true;
                    continue;
                }
                if (flagCol) { s = j - 1; }
                X.matr[z][s] = matr[i][j];
            }
        }
        //X.rounding();
        return X;
    }

    private void rounding(){
        for(int i=0; i<row; i++){
            for(int j=0; j<col;j++){
                //matr[i][j] = (double)Math.round(matr[i][j]*10*round)/(10*round);
                matr[i][j] = round(matr[i][j],round);
            }
        }
    }

    private double round(double number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        double tmp = number * pow;
        return (double) (int) ((tmp - (int) tmp) >= 0.5 ? tmp + 1 : tmp) / pow;
    }

    public int rang() throws Exception {
        if(zero()){
            return 0;
        }
        Matrix X = getStepMatrix();
        X.excludeZeroColRow();
        return X.rows();
    }

    //endregion

    //region Determinant
    public double determinant() throws IncompabilityOfColumnsAndRows{
        if (col != row) { throw new IncompabilityOfColumnsAndRows("It is not square matrix"); }
        else { return determinant(this); }
    }

    public double determinant(boolean write) throws IncompabilityOfColumnsAndRows{
        if (col != row) { throw new IncompabilityOfColumnsAndRows("It is not square matrix"); }
        else { return determinant(this, true); }
    }

    private double determinant(Matrix X){
        resume = false;
        double res = 0;
        if ((X.col == 2) && (X.row == 2)){
            return X.matr[0][0] * X.matr[1][1] - X.matr[0][1] * X.matr[1][0];
        }
        else if ((X.col == 1) && (X.row == 1)) { return X.matr[0][0]; }
        else{
            for (int j = 0; j < X.col; j++){
                Matrix J;
                if(!zero()){
                    J = X.minor(0, j);
                }else{
                    J = new Matrix(1,1);
                }
                double minor = determinant(J);
                res = res + Math.pow(-1, j + 2) * X.matr[0][j] * minor;
            }
            return res;
        }
    }

    private double determinant(Matrix X, boolean write){
        resume = false;
        double res = 0;
        String dot = String.valueOf((char)183);
        if (X.col == 3){
            X.message = new StringBuilder(nf.format(matr[0][0]) + dot + nf.format(matr[1][1]) + dot + nf.format(matr[2][2]) + " + "
                    + nf.format(matr[0][1]) + dot + nf.format(matr[1][2]) + dot + nf.format(matr[2][0]) + " + "
                    + nf.format(matr[1][0]) + dot + nf.format(matr[2][1]) + dot + nf.format(matr[0][2]) + " - "
                    + nf.format(matr[0][2]) + dot + nf.format(matr[1][1]) + dot + nf.format(matr[2][0]) + " - "
                    + nf.format(matr[1][0]) + dot + nf.format(matr[0][1]) + dot + nf.format(matr[2][2]) + " - "
                    + nf.format(matr[2][1]) + dot + nf.format(matr[1][2]) + dot + nf.format(matr[0][0]));
        }
        if (X.col == 2){
            X.message = new StringBuilder(nf.format(matr[0][0]) + dot + nf.format(matr[1][1]) +
                    " - " + nf.format(matr[0][1]) + dot + nf.format(matr[1][0]));
        }
        if ((X.col == 2) && (X.row == 2)){
            return X.matr[0][0] * X.matr[1][1] - X.matr[0][1] * X.matr[1][0];
        }
        else if ((X.col == 1) && (X.row == 1)) { return X.matr[0][0]; }
        else{
            for (int j = 0; j < X.col; j++){
                Matrix J = X.minor(0, j);
                double minor = determinant(J);
                res = res + Math.pow(-1, j + 2) * X.matr[0][j] * minor;
            }
            return res;
        }
    }

    //endregion

    //region Rows operations
    public void swapRows(int r1, int r2) throws IncompabilityOfColumnsAndRows{
        if ((row < r1) | (row < r2) | (r1 < 0) | (r2 < 0)) { throw new IncompabilityOfColumnsAndRows("Invalid index"); }
        if (r1 == r2) { return; }
        else{
            double[][] Matr;
            Matr = copy(this);
            for (int j = 0; j < col; j++){
                double x1 = Matr[r1][j];
                double x2 = Matr[r2][j];
                Matr[r1][j] = x2;
                Matr[r2][j] = x1;
            }
            matr = Matr;
        }
    }

    public void addRowMultiplyedByNumber(int row1, double n, int row2){
        for (int j = 0; j < col; j++){
            matr[row2][j] = matr[row2][j] + matr[row1][j] * n;
        }
    }

    public void multiplyRowByNumber(int r, double n) throws IncompabilityOfColumnsAndRows{
        if ((row < r) | (r < 0)) { throw new IncompabilityOfColumnsAndRows("Invalid index"); }
        else{
            for (int j = 0; j < col; j++){
                matr[r][j] = matr[r][j] * n;
            }
        }
    }

    public void divRowByNumber(int r, double n) throws IncompabilityOfColumnsAndRows{
        if ((row < r) | (r < 0)) { throw new IncompabilityOfColumnsAndRows("Invalid index"); }
        else{
            for (int j = 0; j < col; j++){
                matr[r][j] = (matr[r][j]) * (1 / n);
            }
        }
    }

    public void addNumberToRow(int r, double n) throws IncompabilityOfColumnsAndRows{
        if ((row < r) | (r < 0)) { throw new IncompabilityOfColumnsAndRows("Invalid index"); }
        else{
            for (int j = 0; j < col; j++){
                matr[r][j] = matr[r][j] + n;
            }
        }
    }

    public void subNumberFromRow(int r, double n) throws IncompabilityOfColumnsAndRows{
        if ((row < r) | (r < 0)) { throw new IncompabilityOfColumnsAndRows("Invalid index"); }
        else{
            for (int j = 0; j < col; j++){
                matr[r][j] = matr[r][j] - n;
            }
        }
    }

    public void deleteRow(int r) throws IncompabilityOfColumnsAndRows{
        if ((r > row) | (r < 0)) { throw new IncompabilityOfColumnsAndRows("Row number is negative or more then number of rows in matrix"); }
        Matrix X = new Matrix(row - 1, col);
        boolean flag = false;
        for (int i = 0; i < row; i++){
            int z = i;
            if (i == r) { flag = true; continue; }
            if (flag) { z = z - 1; }
            for (int j = 0; j < col; j++){
                X.matr[z][j] = matr[i][j];
            }
        }
        matr = X.matr;
        col = X.col;
        row = X.row;
    }

    private void deleteRows(List<Integer> dRows) throws IncompabilityOfColumnsAndRows{
        if (dRows.size() > row) throw new IncompabilityOfColumnsAndRows("Row number is negative or more then number of rows in matrix");
        Matrix X = new Matrix(row - dRows.size(), col);
        int z = 0;
        int n = 0;
        for (int i = 0; i < row; i++){
            z = i - n;
            if (dRows.contains(i)) { n++; continue; }
            for (int j = 0; j < col; j++)
                X.matr[z][j] = matr[i][j];
        }
        matr = X.matr;
        col = X.col;
        row = X.row;
    }

    public void excludeZeroRow() throws Exception{
        if (zero()) { matr = new double[1][1]; return; }
        List<Integer> RowToDelete = new ArrayList<Integer>();
        Matrix V = new Matrix(col, 1);
        for (int i = 0; i < V.row; i++){
            V.matr[i][0] = 1;
        }
        Matrix zRow = multiplyByMatrix(this,V);
        for (int i = 0; i < zRow.row; i++){
            if (zRow.matr[i][0] == 0) { RowToDelete.add(i); }
        }
        deleteRows(RowToDelete);
    }

    public List<Integer> excludeZeroRow2() throws Exception{
        if (zero()) { matr = new double[1][1]; return new ArrayList<>(); }
        List<Integer> RowToDelete = new ArrayList<Integer>();
        Matrix V = new Matrix(col, 1);
        for (int i = 0; i < V.row; i++)
            V.matr[i][0] = 1;
        Matrix zRow = multiplyByMatrix(this,V);
        for (int i = 0; i < zRow.row; i++)
            if (zRow.matr[i][0] == 0) { RowToDelete.add(i); }
        deleteRows(RowToDelete);
        return RowToDelete;
    }

    //endregion

    //region Column operations

    public void swapColumns(int col1, int col2) throws IncompabilityOfColumnsAndRows{
        if ((col < col1) | (col < col2) | (col1 < 0) | (col2 < 0) | (col1 == col2)) { throw new IncompabilityOfColumnsAndRows("Invalid index"); }
        else{
            for (int i = 0; i < row; i++){
                double x1 = matr[i][col1];
                double x2 = matr[i][col2];
                matr[i][col1] = x2;
                matr[i][col2] = x1;
            }
        }
    }

    public void multiplyColByNumber(int col, double n) throws MultException{
        if ((this.col < col) | (col < 0)) { throw new MultException("Invalid multiplication"); }
        else{
            for (int i = 0; i < row; i++){
                matr[i][col] = matr[i][col] * n;
            }
        }
    }

    public void divColByNumber(int col, double n) throws IncompabilityOfColumnsAndRows{
        if ((this.col < col) | (col < 0)) { throw new IncompabilityOfColumnsAndRows("Invalid index"); }
        else{
            for (int i = 0; i < row; i++){
                matr[i][col] = matr[i][col] / n;
            }
        }
    }

    public void addNumberToCol(int col, double n) throws IncompabilityOfColumnsAndRows{
        if ((this.col < col) | (col < 0)) { throw new IncompabilityOfColumnsAndRows("Invalid index"); }
        else{
            for (int i = 0; i < row; i++){
                matr[i][col] = matr[i][col] + n;
            }
        }
    }

    public void subNumberFromCol(int col, double n) throws IncompabilityOfColumnsAndRows{
        if ((this.col < col) | (col < 0)) { throw new IncompabilityOfColumnsAndRows("Invalid index"); }
        else{
            for (int i = 0; i < row; i++){
                matr[i][col] = matr[i][col] - n;
            }
        }
    }

    public void deleteCol(int col) throws IncompabilityOfColumnsAndRows{
        if ((col > this.col) | (col < 0)) { throw new IncompabilityOfColumnsAndRows("Col number is negative or more then number of columns in matrix"); }
        Matrix X = new Matrix(this.row, this.col - 1);
        boolean flag = false;
        for (int j = 0; j < this.col; j++){
            int z = j;
            if (j == col) { flag = true; continue; }
            if (flag) { z = z - 1; }
            for (int i = 0; i < this.row; i++){
                X.matr[i][z] = this.matr[i][j];
            }
        }
        this.matr = X.matr;
        this.col = X.col;
        this.row = X.row;
    }

    public void addColMultiplyedByNumber(int col1, double n, int col2){
        for (int i = 0; i < row; i++){
            matr[i][col2] = matr[i][col2] + matr[i][col1] * n;
        }
    }

    public void excludeZeroCol() throws Exception{
        if (zero()) { matr = new double[1][1]; return; }
        List<Integer> ColToDelete = new ArrayList<Integer>();
        Matrix Trans = this.transpose();
        Matrix V = new Matrix(Trans.col, 1);
        for (int i = 0; i < V.row; i++){
            V.matr[i][0] = 1;
        }
        Matrix zCol = multiplyByMatrix(Trans,V);
        for (int i = 0; i < zCol.row; i++){
            if (zCol.matr[i][0] == 0) { ColToDelete.add(i); }
        }
        for (int j : ColToDelete){
            this.deleteCol(j);
        }
    }

    //endregion

    //Power
    public Matrix power(int n, Handler h) throws Exception{
        Message msg;
        if(col!=row)throw new Exception("Матрица должна быть квадратной");
        if(n==0){
            Matrix E = new Matrix(eMatrix(row,col));
            msg = h.obtainMessage(0,E);
            h.sendMessage(msg);
            while(!resume)
            {
                if(resume==true)
                    resume = true;
            }
            resume = false;
            msg = h.obtainMessage(2,"\n");
            h.sendMessage(msg);
            return E;
        }
        if(zero()){
            this.message = new StringBuilder("Нулевая матрица");
            msg = h.obtainMessage(0,this);
            h.sendMessage(msg);
            while(!resume)
            {
                if(resume==true)
                    resume = true;
            }
            resume = false;
            msg = h.obtainMessage(2,"\n");
            h.sendMessage(msg);
            return null;
        }
        if(n<0){
            //в отрицательную степень тоже надо возводить
            Matrix X1 = new Matrix(1, 1);
            X1.round = round;
            X1 = inverseByGaussGordan();
            Matrix X2 = new Matrix(row, col);
            X2.matr = copy(this);
            X2.round = round;
            while (n<-1){
                X2 = multiplyByMatrix(X1,X2);
                n++;
            }
            msg = h.obtainMessage(0,X2);
            h.sendMessage(msg);
            while(!resume)
            {
                if(resume==true)
                    resume = true;
            }
            resume = false;
            msg = h.obtainMessage(2,"\n");
            h.sendMessage(msg);
            return X2;
        }
        else{
            Matrix X = new Matrix(row, col);
            X.matr = copy(this);
            X.round = round;
            while (n > 1){
                X = multiplyByMatrix(this, X);
                if (X == null) { return null; }
                n--;
            }
            msg = h.obtainMessage(0,X);
            h.sendMessage(msg);
            while(!resume)
            {
                if(resume==true)
                    resume = true;
            }
            resume = false;
            msg = h.obtainMessage(2,"\n");
            h.sendMessage(msg);
            return X;
        }
    }

    //region Row Echelon Form

    public Matrix getStepMatrix() throws Exception {
        resume = false;
        if (zero()) throw new Exception("Матрица нулевая");
        if ((col == 1) & (row == 1)) {
            Matrix Res = new Matrix(row, col);
            Res.matr[0][0] = 1;
            return Res;
        }
        else if(row==1){
            Matrix Res = new Matrix(row, col);
            Res.matr = copy(this);
            Res.divRowByNumber(0,matr[0][0]);
            return Res;
        }
        int row = 0;
        double leadElement = 0;
        double[] RowElem;
        Matrix X = new Matrix(this.row, this.col);
        X.name = this.name;
        X.round = this.round;
        X.matr = copy(this);
        X.excludeZeroRow();
        if (!(X.col >= X.row)) {
            for (int z = 0; z < col; z++) {
                RowElem = X.findLeadElem(z, z);
                leadElement = RowElem[1];
                row = (int) RowElem[0];
                if (leadElement == 0)
                    continue;
                if (row != 0)
                    X.swapRows(z, row);
                if (leadElement != 1)
                    X.divRowByNumber(z, leadElement);
                for (int i = z + 1; i < row; i++) {
                    if (X.matr[i][z] == 0)
                        continue;
                    X.addRowMultiplyedByNumber(z, -X.matr[i][z], i);
                }
            }
            return X;
        } else {
            for (int z = 0; z < X.row; z++) {
                RowElem = X.findLeadElem(z, z);
                leadElement = RowElem[1];
                row = (int) RowElem[0];
                if (leadElement == 0)
                    continue;
                if (row != 0)
                    X.swapRows(z, row);
                if (leadElement != 1)
                    X.divRowByNumber(z, leadElement);
                for (int i = z + 1; i < X.row; i++) {
                    if (X.matr[i][z] == 0)
                        continue;
                    X.addRowMultiplyedByNumber(z, -X.matr[i][z], i);
                }
            }
            return X;
        }
    }

    public Matrix getStepMatrix(Handler h) throws Exception{
        resume = false;
        if (zero()) throw new Exception("Матрица нулевая");
        if ((col == 1) & (row == 1)) {
            Matrix Res = new Matrix(row, col);
            Res.matr[0][0] = 1;
            return Res;
        }
        int row = 0;
        int strI = 0;
        int strZ = 0;
        double leadElement = 0;
        double[] RowElem;
        double divBy = 0;
        Message msg;
        Matrix X = new Matrix(this.row, this.col);
        X.name = this.name;
        X.matr = copy(this);
        msg = h.obtainMessage(0,X);
        h.sendMessage(msg);
        while(!resume)
        {
            if(resume==true)
                resume = true;
        }
        resume = false;
        List<Integer> delRows =  X.excludeZeroRow2();
        if(delRows.size()>0){
            X.message = new StringBuilder("Исключим строки:");
            for(int i: delRows){
                strI = i+1;
                X.message.append(" " + strI);
            }
            msg = h.obtainMessage(0,X);
            h.sendMessage(msg);
            while(!resume)
            {
                if(resume==true)
                    resume = true;
            }
            resume = false;
        }
        if (!(X.col >= X.row)) {
            for (int z = 0; z < col; z++) {
                strZ = z + 1;
                RowElem = X.findLeadElem(z, z);
                leadElement = RowElem[1];
                row = (int) RowElem[0];
                strI = row+1;
                if (leadElement == 0)
                    continue;
                if (row != 0) {
                    X.swapRows(z, row);
                    if(z != 0){
                        X.message = new StringBuilder("Поменяли местами строки " + strZ + " и " + strI);
                        msg = h.obtainMessage(0,X);
                        h.sendMessage(msg);
                        while(!resume)
                        {
                            if(resume==true)
                                resume = true;
                        }
                        resume = false;
                    }
                }
                if (leadElement != 1) {
                    X.divRowByNumber(z, leadElement);
                    X.message  = new StringBuilder("Поделили строку " + strZ + " на " + nf.format(leadElement));
                    msg = h.obtainMessage(0,X);
                    h.sendMessage(msg);
                    while(!resume)
                    {
                        if(resume==true)
                            resume = true;
                    }
                    resume = false;
                }
                for (int i = z + 1; i < row; i++) {
                    if (X.matr[i][z] == 0)
                        continue;
                    strI = i + 1;
                    divBy = -X.matr[i][z];
                    X.addRowMultiplyedByNumber(z, -X.matr[i][z], i);
                    X.message = new StringBuilder("Добавили к строке " + strI + " строку " + strZ + " умноженную на " + nf.format(divBy));
                    msg = h.obtainMessage(0,X);
                    h.sendMessage(msg);
                    while(!resume)
                    {
                        if(resume==true)
                            resume = true;
                    }
                    resume = false;
                }
            }
            resume = false;
            msg = h.obtainMessage(2,"\n");
            h.sendMessage(msg);
            return X;
        } else {
            for (int z = 0; z < X.row; z++) {
                strZ = z + 1;
                RowElem = X.findLeadElem(z, z);
                leadElement = RowElem[1];
                row = (int) RowElem[0];
                strI = row+1;
                if (leadElement == 0)
                    continue;
                if (row != 0) {
                    X.swapRows(z, row);
                    if(z!=row){
                        X.message = new StringBuilder("Поменяли местами строки "+strZ+" и " + strI);
                        msg = h.obtainMessage(0,X);
                        h.sendMessage(msg);
                        while(!resume)
                        {
                            if(resume==true)
                                resume = true;
                        }
                        resume = false;
                    }
                }
                if (leadElement != 1) {
                    X.divRowByNumber(z, leadElement);
                    X.message = new StringBuilder("Поделили строку " + strZ + " на " + nf.format(leadElement));
                    msg = h.obtainMessage(0,X);
                    h.sendMessage(msg);
                    while(!resume)
                    {
                        if(resume==true)
                            resume = true;
                    }
                    resume = false;
                }
                for (int i = z + 1; i < X.row; i++) {
                    if (X.matr[i][z] == 0)
                    continue;
                    strI = i + 1;
                    divBy = -X.matr[i][z];
                    X.addRowMultiplyedByNumber(z, -X.matr[i][z], i);
                    X.message = new StringBuilder("Добавили к строке " + strI + " строку " + strZ + " умноженную на " + nf.format(divBy));
                    msg = h.obtainMessage(0,X);
                    h.sendMessage(msg);
                    while(!resume)
                    {
                        if(resume==true)
                            resume = true;
                    }
                    resume = false;
                }
            }
            resume = false;
            msg = h.obtainMessage(2,"\n");
            h.sendMessage(msg);
            return X;
        }
    }

    //endregion

    // region Gauss-Jordan
    public Matrix gaussJordan() throws Exception{
        resume = false;
        if (zero()) throw new Exception("Матрица нулевая");
        if ((col == 1) | (row == 1)){
            Matrix Res = new Matrix(row, col);
            Res.matr[0][0] = 1;
            return Res;
        }
        excludeZeroColRow();
        int row = 0;
        double leadElement = 0;
        double[] RowElem;
        Matrix X = new Matrix(this.row, this.col);
        X.name = this.name;
        X.round = round;
        X.matr = copy(this);
        int key = (this.col >= this.row)? 1 : 0;
        switch (key)
        {
            case (0):
            {
                for (int z = 0; z < this.col; z++)
                {
                    RowElem = X.findLeadElem(z, z);
                    leadElement = RowElem[1];
                    row = (int) RowElem[0];
                    if (leadElement == 0) { continue; }
                    if (row != 0){
                        X.swapRows(z, row); //X.rounding();
                    }
                    if (leadElement != 1){
                        X.divRowByNumber(z, leadElement);
                        //X.rounding();
                    }
                    for (int i = 0; i < this.row; i++){
                        if (i == z) { continue; }
                        if (X.matr[i][z] == 0) { continue; }
                        X.addRowMultiplyedByNumber(z, -X.matr[i][z], i);
                        //X.rounding();
                    }
                }
                //X.rounding();
                return X;
            }
            case (1):
            {
                for (int z = 0; z < this.row; z++){
                    RowElem = X.findLeadElem(z, z);
                    leadElement = RowElem[1];
                    row = (int) RowElem[0];
                    if (leadElement == 0) { continue; }
                    if (row != 0){
                        X.swapRows(z, row); //X.rounding();
                    }
                    if (leadElement != 1){
                        X.divRowByNumber(z, leadElement);
                        //X.rounding();
                    }
                    for (int i = 0; i < this.row; i++){
                        if (i == z) { continue; }
                        if (X.matr[i][z] == 0) { continue; }
                        X.addRowMultiplyedByNumber(z, -X.matr[i][z], i);
                        //X.rounding();
                    }
                }
                //X.rounding();
                return X;
            }
        }
        return X;
    }

    public Matrix gaussJordan(Handler h) throws Exception{
        resume = false;
        if (zero()) throw new Exception("Матрица нулевая");
        if ((col == 1) | (row == 1)){
            Matrix Res = new Matrix(row, col);
            Res.matr[0][0] = 1;
            return Res;
        }
        Message msg;
        //excludeZeroColRow();
        excludeZeroRow();
        int row = 0;
        double divBy = 0;
        double leadElement = 0;
        double[] RowElem;
        int strI = 0;
        int strZ = 0;
        Matrix X = new Matrix(this.row, this.col);
        X.name = this.name;
        X.round = round;
        X.matr = copy(this);
        msg = h.obtainMessage(0,X);
        h.sendMessage(msg);
        while(!resume)
        {
            if(resume==true)
                resume = true;
        }
        resume = false;
        int key = (this.col >= this.row)? 1 : 0;
        switch (key)
        {
            case (0):
            {
                for (int z = 0; z < this.col; z++)
                {
                    strZ = z + 1;
                    RowElem = X.findLeadElem(z, z);
                    leadElement = RowElem[1];
                    row = (int) RowElem[0];
                    strI = row+1;
                    if (leadElement == 0) { continue; }
                    if (row != 0){
                        X.swapRows(z, row); //X.rounding();
                        if(z!=row){
                            X.message = new StringBuilder("Поменяли местами строки " + strZ + " и " + strI);
                            msg = h.obtainMessage(0,X);
                            h.sendMessage(msg);
                            while(!resume)
                            {
                                if(resume==true)
                                    resume = true;
                            }
                            resume = false;
                        }
                    }
                    if (leadElement != 1){
                        X.divRowByNumber(z, leadElement);
                        X.message = new StringBuilder("Поделили строку " + strZ + " на " + nf.format(leadElement));
                        //X.rounding();
                        msg = h.obtainMessage(0,X);
                        h.sendMessage(msg);
                        while(!resume)
                        {
                            if(resume==true)
                                resume = true;
                        }
                        resume = false;
                    }
                    for (int i = 0; i < this.row; i++){
                        if (i == z) { continue; }
                        if (X.matr[i][z] == 0) { continue; }
                        strI = i + 1;
                        divBy = -X.matr[i][z];
                        X.addRowMultiplyedByNumber(z, -X.matr[i][z], i);
                        X.message = new StringBuilder("Добавили к строке " + strI + " строку " + strZ + " умноженную на " +nf.format(divBy));
                        //X.rounding();
                        msg = h.obtainMessage(0,X);
                        h.sendMessage(msg);
                        while(!resume)
                        {
                            if(resume==true)
                                resume = true;
                        }
                        resume = false;
                    }
                }
                //X.rounding();
                return X;
            }
            case (1):
            {
                for (int z = 0; z < this.row; z++){
                    strZ = z + 1;
                    RowElem = X.findLeadElem(z, z);
                    leadElement = RowElem[1];
                    row = (int) RowElem[0];
                    strI = row+1;
                    if (leadElement == 0) { continue; }
                    if (row != 0){
                        X.swapRows(z, row); //X.rounding();
                        if(z!=row){
                            X.message = new StringBuilder("Поменяли местами строки " + strZ + " и " + strI);
                            msg = h.obtainMessage(0,X);
                            h.sendMessage(msg);
                            while(!resume)
                            {
                                if(resume==true)
                                    resume = true;
                            }
                            resume = false;
                        }
                    }
                    if (leadElement != 1){
                        X.divRowByNumber(z, leadElement);
                        X.message = new StringBuilder("Поделили строку " + strZ + " на " + nf.format(leadElement));
                        //X.rounding();
                        msg = h.obtainMessage(0,X);
                        h.sendMessage(msg);
                        while(!resume)
                        {
                            if(resume==true)
                                resume = true;
                        }
                        resume = false;
                    }
                    for (int i = 0; i < this.row; i++){
                        if (i == z) { continue; }
                        if (X.matr[i][z] == 0) { continue; }
                        strI = i + 1;
                        divBy = -X.matr[i][z];
                        X.addRowMultiplyedByNumber(z, -X.matr[i][z], i);
                        X.message = new StringBuilder("Добавили к строке " + strI + " строку " + strZ + " умноженную на " +nf.format(divBy));
                        //X.rounding();
                        msg = h.obtainMessage(0,X);
                        h.sendMessage(msg);
                        while(!resume)
                        {
                            if(resume==true)
                                resume = true;
                        }
                        resume = false;
                    }
                }
                //X.rounding();
                return X;
            }
        }
        return X;
    }

    //endregion

    //region Inverse Matrix
    public Matrix inverseByGaussGordan() throws IncompabilityOfColumnsAndRows{
        resume = false;
        double det = 0;
        if (col != row){ throw new IncompabilityOfColumnsAndRows("Матрица не является квадратной");}
        det = determinant();
        if (det == 0){ throw new IncompabilityOfColumnsAndRows("Матрица вырожденная - определитель равен 0");}
        else{
            int c = col;
            Matrix X = specialMatrix(this);
            X.name = name + "_E";
            X.round = round;
            try{
                X = X.gaussJordan();
            }catch (Exception ex){

            }
            onlyInverse(X, c);
            return X;
        }
    }

    public Matrix inverseByGaussGordan(Handler h) throws Exception{
        resume = false;
        double det = 0;
        if (col != row){ throw new IncompabilityOfColumnsAndRows("Матрица не является квадратной");}
        det = determinant();
        if (det == 0){ throw new IncompabilityOfColumnsAndRows("Матрица вырожденная - определитель равен 0");}
        else{
            int c = col;
            Matrix X = specialMatrix(this);
            X.round = round;
            X.name = name + "_E";
            X = X.gaussJordan(h);
            onlyInverse(X, c);
            Message msg;
            X.message = new StringBuilder("Res= ");
            msg = h.obtainMessage(0,X);
            h.sendMessage(msg);
            while(!resume)
            {
                if(resume==true)
                    resume = true;
            }
            resume = false;
            return X;
        }
    }

    private Matrix specialMatrix(Matrix This){
        Matrix X = new Matrix(row, col * 2);
        for(int i=0; i<This.row; i++)
            for(int j=0; j<This.col; j++)
                X.matr[i][j] = This.matr[i][j];
        int n = X.col - This.col;
        for(int i = 0; i<This.row; i++)
            X.matr[i][i+n] = 1;
        return X;
    }

    private void onlyInverse(Matrix G, int n){
        double[][] inv = new double [G.row][n];
        for(int i=0; i<n; i++)
            for(int j=0; j<n; j++)
                inv[i][j] = G.matr[i][n + j];
        G.matr = inv;
        G.col = n;
        G.row = n;
    }

    //endregion

    //region Help methods
    public void excludeZeroColRow() throws Exception{
        if (zero()) { matr = new double[1][1]; return; }
        List<Integer> RowToDelete = new ArrayList<Integer>();
        List<Integer> ColToDelete = new ArrayList<Integer>();
        Matrix V = new Matrix(col, 1);
        for (int i = 0; i < V.row; i++){
            V.matr[i][0] = 1;
        }
        Matrix zRow = multiplyByMatrix(this,V);
        for (int i = 0; i < zRow.row; i++){
            if (zRow.matr[i][0] == 0) { RowToDelete.add(i); }
        }
        Matrix Trans = this.transpose();
        V = new Matrix(Trans.col, 1);
        for (int i = 0; i < V.row; i++){
            V.matr[i][0] = 1;
        }
        Matrix zCol = multiplyByMatrix(Trans,V);
        for (int i = 0; i < zCol.row; i++){
            if (zCol.matr[i][0] == 0) { ColToDelete.add(i); }
        }
        deleteColArray(ColToDelete);
        deleteRowArray(RowToDelete);
    }

    private void deleteRowArray(List<Integer> rows) throws IncompabilityOfColumnsAndRows{
        Matrix X = new Matrix(row - rows.size(), col);
        int xi = 0;
        int n = 0;
        for (int i = 0; i < row; i++)
        {
            xi = i - n;
            if (rows.contains(i)){
                n++;
                continue;
            }
            for (int j = 0; j < col; j++){
                X.matr[xi][j] = matr[i][j];
            }
        }
        matr = X.matr;
        col = X.col;
        row = X.row;
    }

    private void deleteColArray(List<Integer> cols) throws IncompabilityOfColumnsAndRows{
        Matrix X = new Matrix(row, col - cols.size());
        int xj = 0;
        int n = 0;
        for (int j = 0; j < col; j++){
            xj = j - n;
            if (cols.contains(j)){
                n++;
                continue;
            }
            for (int i = 0; i < row; i++){
                X.matr[i][xj] = matr[i][j];
            }
        }
        matr = X.matr;
        col = X.col;
        row = X.row;
    }

    private double[] findLeadElem(int col, int sRow){
        double[] res = new double[2];
        //res[0] - row, res[1] - elem
        for (int i = sRow; i < row; i++){
            if (matr[i][col] != 0) { res[0] = i; res[1] = matr[i][sRow]; return res; }
        }
        res[0] = 0;
        res[1] = matr[0][0];
        return res;
    }

    public double getElem(int row, int col){
        return matr[row][col];
    }

    public String getSElem(int row,int col){
        return nf.format(matr[row][col]);
    }

    private double[][]copy(Matrix X){
        double[][] matrCopy = new double [X.row][X.col];
        for(int i=0; i<X.row;i++)
            for(int j=0;j<X.col;j++)
                matrCopy[i][j] = X.matr[i][j];
        return matrCopy;
    }

    private double[][]eMatrix(int col,int row)throws Exception{
        if(col!=row) throw new Exception("Матрица должна быть квадратной");
        double[][] E = new double[row][col];
        for(int i=0; i<row;i++)
            E[i][i] = 1;
        return E;
    }

    //endregion
}
