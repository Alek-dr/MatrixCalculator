package com.barmin.matrixcalculator;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Created by alexander on 17.08.17.
 */

public class DimensionWatcher implements TextWatcher {

    public static final int max = 10;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);
    EditText editText;

    DimensionWatcher(EditText editText){
        this.editText = editText;
    }

    public void addListener(PropertyChangeListener listener){
        support.addPropertyChangeListener(listener);
    }

    public void removeListener(PropertyChangeListener listener){
        support.removePropertyChangeListener(listener);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        int newDim;
        String sDim = editable.toString();
        //TODO if n>max make popup
        if (sDim.length()>0) newDim = Integer.valueOf(sDim);
        else newDim = 1;
        if (newDim>max) newDim = max;
        switch (editText.getId()){
            case R.id.edit_cols:{
                support.firePropertyChange("colsChanged",null, newDim);
                break;
            }
            case R.id.edit_rows:{
                support.firePropertyChange("rowsChanged",null, newDim);
            }
        }
    }
}
