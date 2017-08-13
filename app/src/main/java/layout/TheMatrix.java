package layout;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import com.barmin.matrixcalculator.matrixLib.Matrix;

import com.barmin.matrixcalculator.R;


public class TheMatrix extends Fragment {

    public GridLayout grid;

    public TheMatrix() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_the_matrix, container, false);
    }

    public void initialMatrix(){
        grid = (GridLayout)getView().findViewById(R.id.grid_matrix);
        grid.setRowCount(3);
        grid.setColumnCount(3);
        for (int i = 0; i < 9; i++) {
            EditText cell = new EditText(grid.getContext());
            //cell.setInputType(12290);
            cell.setHint("0");
            //cell.setTextSize(40.0f);
            grid.addView(cell);
        }
    }

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

}