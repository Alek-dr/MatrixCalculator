package com.barmin.matrixcalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.barmin.matrixcalculator.matrixLib.Matrix;

import layout.TheMatrix;

public class CreateMatrix extends AppCompatActivity {

    private static View.OnClickListener buttonListener;
    private static TextWatcher textWatcher;
    private TheMatrix frag;
    private static final int max = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_matrix);
        frag = (TheMatrix)getSupportFragmentManager().findFragmentById(R.id.f_the_matrix);
        frag.setMatrix(3,3);
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int row = 3;
                int col = 3;
                EditText colEdit = (EditText)findViewById(R.id.edit_cols);
                EditText rowEdit = (EditText)findViewById(R.id.edit_rows);
                String srow = rowEdit.getText().toString();
                String scol = colEdit.getText().toString();
                //TODO if n>max make popup
                if (srow.length()>0) row = Integer.valueOf(srow);
                    else row = 1;
                if (scol.length()>0) col = Integer.valueOf(scol);
                    else col = 1;
                if (row>max){
                    row = max;
                    rowEdit.setText(String.valueOf(row));
                }
                if (col>max){
                    col = max;
                    colEdit.setText(String.valueOf(col));
                }
                frag.setMatrix(row,col);
            }
        };
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
            }
        };
        EditText name = (EditText)findViewById(R.id.edit_matrix_name);
        String sname = Storage.StandartName.peek().toString();
        name.setText(sname);
        setListeners();
    }

    private void setListeners(){
        Button ok = (Button)findViewById(R.id.btn_ok);
        ok.setOnClickListener(buttonListener);
        EditText rows = (EditText)findViewById(R.id.edit_rows);
        EditText cols = (EditText)findViewById(R.id.edit_cols);
        rows.addTextChangedListener(textWatcher);
        cols.addTextChangedListener(textWatcher);
    }
}
