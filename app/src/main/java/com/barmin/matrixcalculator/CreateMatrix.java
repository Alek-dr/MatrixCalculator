package com.barmin.matrixcalculator;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.barmin.matrixcalculator.matrixLib.Matrix;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import layout.TheMatrix;

public class CreateMatrix extends AppCompatActivity implements PropertyChangeListener {

    private static View.OnClickListener buttonListener;
    private TheMatrix frag;
    private int currentRow;
    private int currentCol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_create_matrix);
        if(savedInstanceState!=null){
            frag = (TheMatrix)getSupportFragmentManager().getFragment(savedInstanceState, "frag");
            currentRow = savedInstanceState.getInt("rows");
            currentCol = savedInstanceState.getInt("cols");
        }else {
            currentCol = 3;
            currentRow = 3;
            frag = (TheMatrix)getSupportFragmentManager().findFragmentById(R.id.f_the_matrix);
        }
        frag.setMatrix(currentRow,currentCol);
        EditText name = (EditText)findViewById(R.id.edit_matrix_name);
        String sname = Storage.StandartName.peek().toString();
        name.setText(sname);
        setListeners();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "frag", frag);
        outState.putInt("rows", currentRow);
        outState.putInt("cols", currentCol);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setListeners(){
        buttonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Matrix m = frag.getMatrix();
                EditText nameEdit = (EditText)findViewById(R.id.edit_matrix_name);
                String editName = nameEdit.getText().toString();
                String storName = Storage.StandartName.peek().toString();
                if (editName.equals(storName))
                    m.name = Storage.StandartName.poll().toString();
                else m.name = editName;
                Storage.addMatrix(m);
                CreateMatrix.super.onBackPressed();
                //Always hide keyboard
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        };
        Button ok = (Button)findViewById(R.id.btn_ok);
        ok.setOnClickListener(buttonListener);
        EditText rows = (EditText)findViewById(R.id.edit_rows);
        EditText cols = (EditText)findViewById(R.id.edit_cols);
        DimensionWatcher dwRows = new DimensionWatcher(rows);
        DimensionWatcher dwCols = new DimensionWatcher(cols);
        dwRows.addListener(this);
        dwCols.addListener(this);
        rows.addTextChangedListener(dwRows);
        cols.addTextChangedListener(dwCols);
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String dimension = propertyChangeEvent.getPropertyName();
        switch (dimension){
            case ("colsChanged"):{
                int col = (int)propertyChangeEvent.getNewValue();
                if (col==currentCol) break;
                frag.setMatrix(currentRow, col);
                currentCol = col;
                break;
            }
            case ("rowsChanged"):{
                int row = (int)propertyChangeEvent.getNewValue();
                if (row==currentRow) break;
                frag.setMatrix(row, currentCol);
                currentRow = row;
                break;
            }
        }
    }
}
