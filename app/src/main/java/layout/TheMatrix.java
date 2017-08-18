package layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.app.FragmentTransaction;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import com.barmin.matrixcalculator.matrixLib.Matrix;

import com.barmin.matrixcalculator.R;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class TheMatrix extends Fragment {

    public GridLayout grid;
    public static NumberFormat nf = new DecimalFormat("#.###");

    public TheMatrix() {

    }

    // region Lifecycle
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_the_matrix, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            if (grid==null)
                grid = (GridLayout)getView().findViewById(R.id.grid_matrix);
            double[] matrVals = savedInstanceState.getDoubleArray("matr");
            for(int i=2; i<grid.getChildCount()+2; i++){
                EditText cell = (EditText)grid.getChildAt(i-2);
                cell.setInputType(12290);
                cell.setTextSize(20.0f);
                double n = matrVals[i];
                if (n==(int)n){
                    String sN = String.format("%d", Integer.valueOf((int)n));
                    cell.setText(sN);
                }else{
                    cell.setText(String.valueOf(n));
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (outState == null)
            outState = new Bundle();
        double[] vals = ravel(getMatrix().getMatr());
        outState.putDoubleArray("matr",vals);
        super.onSaveInstanceState(outState);
    }

    //endregion

    public Matrix getMatrix(){
        if (grid==null)
            grid = (GridLayout)getView().findViewById(R.id.grid_matrix);
        int r = grid.getRowCount();
        int c = grid.getColumnCount();
        double [][] m = new double[r][c];
        int elem = 0;
        for (int i=0; i<r;i++){
            for(int j=0; j<c;j++){
                double n;
                EditText ed = (EditText)grid.getChildAt(elem++);
                try{
                    n = Double.valueOf(ed.getText().toString());
                }catch (Exception ex){
                    n = 0;
                }
                m[i][j] = n;
            }
        }
        return new Matrix(m);
    }

    public void setMatrix(int row, int col){
        if (grid==null)
            grid = (GridLayout)getView().findViewById(R.id.grid_matrix);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        param.weight = 1;
        grid.removeAllViews();
        grid.setRowCount(row);
        grid.setColumnCount(col);
        for (int i = 0; i < row*col; i++) {
            EditText cell = new EditText(grid.getContext());
            cell.setInputType(12290);
            cell.setHint("0");
            cell.setTextSize(20.0f);
            cell.setLayoutParams(param);
            grid.addView(cell);
        }
    }

    private double[] ravel(double[][] matr){
        double [] rowVals = new double[matr.length*matr[0].length+2];
        rowVals[0] = matr.length;
        rowVals[1] = matr[0].length;
        int k = 2;
        for(int i=0; i<matr.length;i++){
            for (int j=0; j<matr[0].length; j++){
                rowVals[k] = matr[i][j];
                k++;
            }
        }
        return rowVals;
    }

    private double[][] unravel(double[] rowVals){
        int row = (int)rowVals[0];
        int col = (int)rowVals[1];
        int k = 2;
        double[][]matr = new double[row][col];
        for(int i=0; i<row; i++){
            for(int j=0; j<col; j++){
                matr[i][j] = rowVals[k];
                k++;
            }
        }
        return matr;
    }

}
