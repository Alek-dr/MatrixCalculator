package com.barmin.matrixcalculator;

import com.barmin.matrixcalculator.matrixLib.Matrix;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Barmin on 03.01.2017.
 * Singleton class, provides a storage
 * of matrix and their names
 */

public class Storage {
    private static volatile Storage instance;

    private static char[] liters = new char[] { 'A', 'B', 'C', 'D', 'E', 'F','G','H','I','K',
            'L','M','N','O','P','Q','R','S','T','V','X','Y','Z' };

    public static Queue<Character> StandartName = new LinkedList<>();

    public static ArrayList<Matrix> matrixCollection = new ArrayList<Matrix>();

    public static Storage getInstance() {
        Storage localInstance = instance;
        if (localInstance == null) {
            synchronized (Storage.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new Storage();
                }
            }
        }
        //initialize queue
        for(Character c : liters){
            StandartName.add(c);
        }
        return localInstance;
    }

    public static void addMatrix(Matrix m){
        for(int i=0; i<matrixCollection.size(); i++){
            if(matrixCollection.get(i).name.equals(m.name)){
                matrixCollection.remove(i);
            }
        }
        matrixCollection.add(m);
    }

    public static void delete(Matrix M){
        matrixCollection.remove(M);
    }

    public static Matrix getMatrix(String name){
        for(Matrix M : matrixCollection){
            if(M.name.equals(name))
                return M;
        }
        return null;
    }

}