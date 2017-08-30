package com.barmin.matrixcalculator;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.barmin.matrixcalculator.matrixLib.Matrix;
import com.barmin.matrixcalculator.matrixLib.IncompabilityOfColumnsAndRows;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import layout.ExpandableHeightGridView;

public class WorkActivity extends AppCompatActivity {
    private EditText workSpace;
    private TextView curMatr;
    public boolean stepByStep;
    private boolean flag;
    private final NumberFormat nf = new DecimalFormat("#.###");
    private Matrix currentMatrix;
    private int currID;
    private int editableStart = 0;
    private static final Pattern det = Pattern.compile("^det\\([a-zA-Z]+[0-9]*\\)$");
    private static final Pattern pow = Pattern.compile("^[a-zA-Z]+[0-9]*\\^\\-?\\d+[,.]?\\d*");
    private static final Pattern gauss = Pattern.compile("^Ref\\([a-zA-Z]+[0-9]*\\)$");
    private static final Pattern ident = Pattern.compile("^Ident\\([a-zA-Z]+[0-9]*\\)$");
    private static final Pattern rg = Pattern.compile("^Rg\\([a-zA-Z]+[0-9]*\\)$");
    private static final Pattern tr = Pattern.compile("^Tr\\([a-zA-Z]+[0-9]*\\)$");
    private static final Pattern inverse = Pattern.compile("^Inv\\([a-zA-Z]+[0-9]*\\)$");
    private static View.OnClickListener helpBtnsLisnener;
    private static View.OnClickListener matrixListener;
    private static View.OnClickListener matrixOperListener;
    private static View.OnLongClickListener longClickListener;
    private static View.OnClickListener baseOperListener;
    static Handler h;

    private enum Actions {
        Det, Inverse, Transpose, REF, GaussJordan,
        Rang, Expressoin, Pow, Nothing
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)
            setContentView(R.layout.activity_work_port);
        else setContentView(R.layout.activity_work_land);

        stepByStep = false;
        flag = false;
        curMatr = (TextView) findViewById(R.id.current_matrix);
        workSpace = (EditText) findViewById(R.id.work_field);
        workSpace.setTextIsSelectable(true);
        workSpace.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (flag) {
                    String expr = getExpression();
                    flag = false;
                    Actions key = whatToDo(expr);
                    try {
                        switch (key) {
                            case Det: {
                                determinant(expr);
                                break;
                            }
                            case Inverse: {
                                inverse(expr);
                                break;
                            }
                            case Transpose: {
                                transpose(expr);
                                editableStart = workSpace.length();
                                break;
                            }
                            case REF: {
                                rowEchelonForm(expr);
                                break;
                            }
                            case GaussJordan: {
                                gaussJordan(expr);
                                break;
                            }
                            case Rang: {
                                rang(expr);
                                editableStart = workSpace.length();
                                break;
                            }
                            case Nothing: {
                                break;
                            }
                        }
                    } catch (Exception ex) {
                        if (ex instanceof IncompabilityOfColumnsAndRows) {
                            keyboardCheck();
                            workSpace.append("\n");
                            workSpace.append(ex.getMessage());
                            workSpace.append("\n");
                            workSpace.append("\n");
                            Editable edit = workSpace.getText();
                            Selection.setSelection(edit, workSpace.length());
                            editableStart = workSpace.length();
                        } else {

                        }
                    }
                }
            }
        });
        workSpace.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    String expr = getExpression();
                    if (whatToDo(expr).equals(Actions.Pow)) {
                        try {
                            workSpace.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                            workSpace.setSingleLine(false);
                            power(expr);
                            workSpace.append("\n");
                        } catch (Exception ex) {

                        }
                    } else {
                        keyboardCheck();
                    }
                }
                return false;
            }
        });
        createListeners();
        viewMatrix();
        setListeners();

        h = new Handler() {
            public void handleMessage(Message msg) {
                try {
                    if (msg.what == 0) {
                        //Прислали матрицу
                        if (msg.obj instanceof Matrix) {
                            write((Matrix) msg.obj);
                            h.removeMessages(0, msg.obj);
                            Matrix.resume = true;
                        }
                    } else if (msg.what == 1) {
                        //Надо обновить матрицы
                        viewMatrix();
                    } else if (msg.what == 2) {
                        //Прислали exception :(
                        if (msg.obj instanceof Exception) {
                            writeMessage("\n" + ((Exception) msg.obj).getMessage() + "\n");
                        }
                        //Или сообщение :)
                        else if (msg.obj instanceof String) {
                            writeMessage((String) msg.obj);
                        }
                    }
                } catch (Exception ex) {

                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        //Nothing
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.step);
        checkable.setChecked(stepByStep);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.step:
                stepByStep = !item.isChecked();
                item.setChecked(stepByStep);
                return true;
            default:
                return false;
        }
    }

    //region Listeners

    private void setListeners() {
        Button mult = (Button) findViewById(R.id.mult);
        Button plus = (Button) findViewById(R.id.plus);
        Button subtr = (Button) findViewById(R.id.sub);
        mult.setOnClickListener(baseOperListener);
        plus.setOnClickListener(baseOperListener);
        subtr.setOnClickListener(baseOperListener);
        Button det = (Button) findViewById(R.id.det);
        Button power = (Button) findViewById(R.id.power);
        Button transp = (Button) findViewById(R.id.transpose);
        Button invert = (Button) findViewById(R.id.invert);
        Button step = (Button) findViewById(R.id.step);
        Button rang = (Button) findViewById(R.id.rang);
        Button jordan = (Button) findViewById(R.id.jordan);
        det.setOnClickListener(matrixOperListener);
        power.setOnClickListener(matrixOperListener);
        transp.setOnClickListener(matrixOperListener);
        invert.setOnClickListener(matrixOperListener);
        step.setOnClickListener(matrixOperListener);
        rang.setOnClickListener(matrixOperListener);
        jordan.setOnClickListener(matrixOperListener);
        Button C = (Button) findViewById(R.id.C);
        C.setOnClickListener(helpBtnsLisnener);
        Button show = (Button) findViewById(R.id.show);
        show.setOnClickListener(helpBtnsLisnener);
        Button delete = (Button) findViewById(R.id.del_matrix);
        delete.setOnClickListener(helpBtnsLisnener);
        Button backspace = (Button) findViewById(R.id.backspace);
        backspace.setOnClickListener(helpBtnsLisnener);
        Button equal = (Button) findViewById(R.id.equal);
        equal.setOnClickListener(baseOperListener);
        Button brack = (Button) findViewById(R.id.brack);
        brack.setOnClickListener(helpBtnsLisnener);
    }

    void createListeners() {
        //region Help
        helpBtnsLisnener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardCheck();
                switch (v.getId()) {
                    case R.id.C: {
                        workSpace.setText("");
                        workSpace.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        editableStart = 0;
                        flag = true;
                        System.gc();
                        break;
                    }
                    case R.id.show: {
                        if (currentMatrix != null) {
                            flag = false;
                            String s = getExpression();
                            if (s != "" || s != "\n") {
                                workSpace.append("\n");
                            }
                            workSpace.append(currentMatrix.name + "=");
                            writeMatrix(currentMatrix);
                            workSpace.append("\n");
                            editableStart = workSpace.length();
                        }
                        break;
                    }
                    case R.id.del_matrix: {
                        if (currentMatrix != null) {
                            Storage.delete(currentMatrix);
                            LinearLayout grid = (LinearLayout) findViewById(R.id.grid_matrix);
                            Button matrix = (Button) findViewById(currID);
                            grid.removeView(matrix);
                            curMatr.setText("");
                            curMatr.setHint("Текущая матрица");
                        }
                        break;
                    }
                    case R.id.backspace: {
                        if (canDel()) {
                            int start = workSpace.getSelectionStart();
                            int length = workSpace.getText().length();
                            if (length > 0) {
                                if (start > 0) {
                                    workSpace.getText().delete(start - 1, start);
                                    flag = true;
                                }
                            }
                        }
                        break;
                    }
                    case R.id.brack: {
                        if (canEdit()&oneMoreCheck()) {
                            int start = workSpace.getSelectionStart();
                            workSpace.getText().insert(start, "()");
                            setCursor(start + 1);
                            break;
                        }
                    }
                }
            }
        };
        //endregion

        //region Matrix
        matrixListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (matrixCanWrite() & canEdit() & oneMoreCheck()) {
                    int start = Math.max(workSpace.getSelectionStart(), 0);
                    int end = Math.max(workSpace.getSelectionEnd(), 0);
                    String insert = ((Button) v).getText().toString();
                    flag = true;
                    workSpace.getText().replace(Math.min(start, end), Math.max(start, end), insert, 0, insert.length());
                }
            }
        };
        //endregion

        //region Matrix Operations
        matrixOperListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardCheck();
                String expr = getExpression();
                if (expr.equals("\n") | expr.isEmpty()) {
                    switch (v.getId()) {
                        case R.id.det: {
                            editableStart = workSpace.length();
                            workSpace.append("det()");
                            setCursor();
                            break;
                        }
                        case R.id.step: {
                            editableStart = workSpace.length();
                            workSpace.append("Ref()");
                            setCursor();
                            break;
                        }
                        case R.id.jordan: {
                            editableStart = workSpace.length();
                            workSpace.append("Ident()");
                            setCursor();
                            break;
                        }
                        case R.id.rang: {
                            editableStart = workSpace.length();
                            workSpace.append("Rg()");
                            setCursor();
                            break;
                        }
                        case R.id.transpose: {
                            editableStart = workSpace.length();
                            workSpace.append("Tr()");
                            setCursor();
                            break;
                        }
                        case R.id.invert: {
                            editableStart = workSpace.length();
                            workSpace.append("Inv()");
                            setCursor();
                            break;
                        }
                    }
                }
                if (v == findViewById(R.id.power)) {
                    if (canPower()) {
                        editableStart = workSpace.length();
                        workSpace.append("^");
                        showKeyboard();
                        Editable edit = workSpace.getText();
                        Selection.setSelection(edit, workSpace.length());
                    }
                }
            }
        };
        //endregion

        //region Long Click
        longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String name = ((Button) v).getText().toString();
                currentMatrix = Storage.getMatrix(name);
                currID = v.getId();
                curMatr.setText(currentMatrix.name);
                return true;
            }
        };
        //endregion

        //region Base Operations
        baseOperListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keyboardCheck();
                if (canEdit()&oneMoreCheck()) {
                    int start = workSpace.getSelectionStart();
                    switch (v.getId()) {
                        case R.id.mult: {
                            workSpace.getText().insert(start, "*");
                            break;
                        }
                        case R.id.plus: {
                            workSpace.getText().insert(start, "+");
                            break;
                        }
                        case R.id.sub: {
                            workSpace.getText().insert(start, "-");
                            break;
                        }
                        case R.id.equal: {
                            expression(getExpression());
                            break;
                        }
                    }
                }
            }
        };
        //endregion
    }

    //endregion

    private void keyboardCheck() {
        int start = workSpace.getSelectionStart();
        workSpace.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        workSpace.setSingleLine(false);
        Editable edit = workSpace.getText();
        Selection.setSelection(edit, start);
    }

    private Actions whatToDo(String expr) {
        Matcher match;
        match = det.matcher(expr);
        if (match.matches())
            return Actions.Det;
        match.reset();
        match = gauss.matcher(expr);
        if (match.matches())
            return Actions.REF;
        match.reset();
        match = ident.matcher(expr);
        if (match.matches())
            return Actions.GaussJordan;
        match.reset();
        match = inverse.matcher(expr);
        if (match.matches())
            return Actions.Inverse;
        match.reset();
        match = rg.matcher(expr);
        if (match.matches())
            return Actions.Rang;
        match.reset();
        match = tr.matcher(expr);
        if (match.matches())
            return Actions.Transpose;
        match.reset();
        match = pow.matcher(expr);
        if (match.matches())
            return Actions.Pow;
        match.reset();
        if (expr.contains("*") | expr.contains("+") | expr.contains("-"))
            return Actions.Expressoin;
        return Actions.Nothing;
    }

    private String getExpression() {
        int n = 0;
        int ind = workSpace.length();
        for (int i = ind; i > 0; i--) {
            n++;
            char ch = workSpace.getText().toString().charAt(i - 1);
            String s = String.valueOf(ch);
            if (s.equals("\n"))
                return workSpace.getText().toString().substring(i);
        }
        return workSpace.getText().toString().substring(0, ind);
    }

    private void viewMatrix() {
        int row = Storage.matrixCollection.size() / 3;
        if (row == 0) row = 1;
        LinearLayout grid = (LinearLayout) findViewById(R.id.grid_matrix);
        //grid.setExpanded(true);
        grid.removeAllViews();
        //grid.setRowCount(3);
        //grid.setColumnCount(2);
        int i = 0;
        for (Matrix M : Storage.matrixCollection) {
            Button mname = new Button(grid.getContext());
            mname.setText(M.name);
            mname.setOnClickListener(matrixListener);
            mname.setOnLongClickListener(longClickListener);
            mname.setId(i);
            grid.addView(mname);
            i++;
        }
    }

    public void onNewMatrixClick(View view) {
        Intent intent = new Intent(this, CreateMatrix.class);
        startActivity(intent);
    }

    private void writeMessage(String msg) {
        try {
            if(msg=="\n"){
                workSpace.append("\n");
            }else{
                workSpace.append(msg);
                workSpace.append("\n");
            }
            Editable edit = workSpace.getText();
            Selection.setSelection(edit, workSpace.length());
            editableStart = workSpace.length();
        } catch (Exception ex) {

        }
    }

    //region Matrix operations

    private void determinant(String expr) throws Exception {
        String find;
        for (int i = 0; i < Storage.matrixCollection.size(); i++) {
            find = "det(" + Storage.matrixCollection.get(i).name + ")";
            if (expr.equals(find)) {
                final int I = i;
                if (stepByStep) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            double res = 0;
                            try {
                                res = Storage.matrixCollection.get(I).determinant(true);
                                Message msg = h.obtainMessage(2, " = " + Storage.matrixCollection.get(I).message + " = " + nf.format(res) + "\n");
                                h.sendMessage(msg);
                            } catch (IncompabilityOfColumnsAndRows ex) {
                                Message msg = h.obtainMessage(2, ex);
                                h.sendMessage(msg);
                            }
                        }
                    });
                    t.start();
                    break;
                } else {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            double res = 0;
                            try {
                                res = Storage.matrixCollection.get(I).determinant();
                                Message msg = h.obtainMessage(2, "=" + nf.format(res) + "\n");
                                h.sendMessage(msg);
                            } catch (IncompabilityOfColumnsAndRows ex) {
                                Message msg = h.obtainMessage(2, ex);
                                h.sendMessage(msg);
                            }
                        }
                    });
                    t.run();
                    break;
                }
            }
        }
    }

    private void inverse(String expr) throws Exception {
        String find;
        workSpace.append("\n");
        for (int i = 0; i < Storage.matrixCollection.size(); i++) {
            find = "Inv(" + Storage.matrixCollection.get(i).name + ")";
            if (expr.equals(find)) {
                final Matrix M = Storage.matrixCollection.get(i);
                if (stepByStep) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Matrix H;
                            try {
                                H = M.inverseByGaussGordan(h);
                                H.name = "Res";
                                Storage.addMatrix(H);
                                Message msg = h.obtainMessage(2,"\n");
                                h.sendMessage(msg);
                                msg = h.obtainMessage(1);
                                h.sendMessage(msg);
                            } catch (Exception ex) {
                                Message msg = h.obtainMessage(2, ex);
                                h.sendMessage(msg);
                            }
                        }
                    });
                    t.start();
                    break;
                } else {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Matrix H;
                            try {
                                H = M.inverseByGaussGordan();
                                H.name = "Res";
                                Storage.addMatrix(H);
                                Message msg = h.obtainMessage(0, H);
                                h.sendMessage(msg);
                                msg = h.obtainMessage(2,"\n");
                                h.sendMessage(msg);
                                msg = h.obtainMessage(1);
                                h.sendMessage(msg);
                            } catch (Exception ex) {
                                Message msg = h.obtainMessage(2, ex);
                                h.sendMessage(msg);
                            }
                        }
                    });
                    t.start();
                    break;
                }
            }
        }
    }

    private void transpose(String expr) throws Exception {
        String find;
        for (int i = 0; i < Storage.matrixCollection.size(); i++) {
            find = "Tr(" + Storage.matrixCollection.get(i).name + ")";
            if (expr.equals(find)) {
                Matrix M;
                M = Storage.matrixCollection.get(i).transpose();
                M.name = "Res";
                Storage.addMatrix(M);
                workSpace.append("\n");
                workSpace.append("\n" + M.name + "=");
                writeMatrix(M);
                workSpace.append("\n");
                viewMatrix();
            }
        }
    }

    private void rowEchelonForm(String expr) throws Exception {
        String find;
        workSpace.append("\n");
        for (int i = 0; i < Storage.matrixCollection.size(); i++) {
            find = "Ref(" + Storage.matrixCollection.get(i).name + ")";
            if (expr.equals(find)) {
                final Matrix M = Storage.matrixCollection.get(i);
                try {
                    if (stepByStep) {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Matrix H;
                                try {
                                    H = M.getStepMatrix(h);
                                    H.name = "Res";
                                    Storage.addMatrix(H);
                                    Message msg = h.obtainMessage(2,"\n");
                                    h.sendMessage(msg);
                                    msg = h.obtainMessage(1);
                                    h.sendMessage(msg);
                                } catch (Exception ex) {
                                    Message msg = h.obtainMessage(2, ex);
                                    h.sendMessage(msg);
                                }
                            }
                        });
                        t.start();
                        break;
                    } else {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Matrix H;
                                try {
                                    H = M.getStepMatrix();
                                    H.name = "Res";
                                    Storage.addMatrix(H);
                                    Message msg = h.obtainMessage(0, H);
                                    h.sendMessage(msg);
                                    msg = h.obtainMessage(2,"\n");
                                    h.sendMessage(msg);
                                    msg = h.obtainMessage(1);
                                    h.sendMessage(msg);
                                } catch (Exception ex) {
                                    Message msg = h.obtainMessage(2, ex);
                                    h.sendMessage(msg);
                                }
                            }
                        });
                        t.start();
                        break;
                    }
                } catch (Exception ex) {

                }
            }
        }
    }

    private void gaussJordan(String expr) throws Exception {
        String find;
        workSpace.append("\n");
        for (int i = 0; i < Storage.matrixCollection.size(); i++) {
            find = "Ident(" + Storage.matrixCollection.get(i).name + ")";
            if (expr.equals(find)) {
                final Matrix M = Storage.matrixCollection.get(i);
                if (stepByStep) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Matrix H;
                            try {
                                H = M.gaussJordan(h);
                                H.name = "Res";
                                Storage.addMatrix(H);
                                Message msg = h.obtainMessage(2,"\n");
                                h.sendMessage(msg);
                                msg = h.obtainMessage(1);
                                h.sendMessage(msg);
                            } catch (Exception ex) {
                                Message msg = h.obtainMessage(2, ex);
                                h.sendMessage(msg);
                            }
                        }
                    });
                    t.start();
                    break;
                } else {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Matrix H;
                            try {
                                H = M.gaussJordan();
                                H.name = "Res";
                                Storage.addMatrix(H);
                                Message msg = h.obtainMessage(0, H);
                                h.sendMessage(msg);
                                msg = h.obtainMessage(2,"\n");
                                h.sendMessage(msg);
                                msg = h.obtainMessage(1);
                                h.sendMessage(msg);
                            } catch (Exception ex) {
                                Message msg = h.obtainMessage(2, ex);
                                h.sendMessage(msg);
                            }
                        }
                    });
                    t.start();
                    break;
                }
            }
        }
    }

    private void rang(String expr) throws Exception {
        String find;
        double res;
        for (int i = 0; i < Storage.matrixCollection.size(); i++) {
            find = "Rg(" + Storage.matrixCollection.get(i).name + ")";
            if (expr.equals(find)) {
                res = Storage.matrixCollection.get(i).rang();
                workSpace.append("=" + nf.format(res) + "\n");
                workSpace.append("\n");
                Editable edit = workSpace.getText();
                Selection.setSelection(edit, workSpace.length());
            }
        }
    }

    //Expression

    private void expression(String expr) {
        Parser p = new Parser(expr);
        Matrix res;
        flag = false;
        try {
            if (p.oneMatrix()) {
                res = p.getMatrix();
                res.message = null;
                workSpace.append("=");
                write(res);
                workSpace.append("\n");
            } else {
                if (p.valid()) {
                    res = p.getResult();
                    res.name = "Res";
                    if (res != null) {
                        Storage.addMatrix(res);
                        viewMatrix();
                        workSpace.append("\n" + "\n" + "Res=");
                        write(res);
                        workSpace.append("\n");
                    }
                }
            }
        } catch (Exception ex) {
            workSpace.append("\n" + ex.getMessage() + "\n" + "\n");
        } finally {
            System.gc();
            editableStart = workSpace.length();
        }
    }

    //Power methods

    private void power(String expr) throws Exception {
        final int p = extractNumber(expr);
        String P = String.valueOf(p);
        for (int i = 0; i < Storage.matrixCollection.size(); i++) {
            if (expr.equals(Storage.matrixCollection.get(i).name + "^" + P)) {
                final Matrix M = Storage.matrixCollection.get(i);
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Matrix H;
                            try {
                                H = M.power(p,h);
                                if(H!=null){
                                    H.name = "Res";
                                    Storage.addMatrix(H);
                                    Message msg = h.obtainMessage(1);
                                    h.sendMessage(msg);
                                }
                            } catch (Exception ex) {
                                Message msg = h.obtainMessage(2, ex);
                                h.sendMessage(msg);
                            }
                        }
                    });
                    t.start();
                }
            }
    }

    private int extractNumber(String expr) {
        String numb = "";
        int res = 0;
        for (int i = expr.length() - 1; i > 0; i--) {
            if (expr.charAt(i) == '^') {
                numb = expr.substring(i + 1);
                break;
            }
        }
        try {
            res = Integer.valueOf(numb);
        } catch (Exception ex) {

        }
        return res;
    }

    private boolean canPower() {
        String expr = getExpression();
        for (int i = 0; i < Storage.matrixCollection.size(); i++) {
            if (expr.equals(Storage.matrixCollection.get(i).name)) {
                return true;
            }
        }
        return false;
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(workSpace, 0);
        workSpace.setInputType(InputType.TYPE_CLASS_NUMBER);
    }

    //endregion

    //region Behavior

    private boolean canEdit() {
        int start = workSpace.getSelectionStart();
        if (start >= editableStart - 1) return true;
        return false;
    }

    private boolean oneMoreCheck(){
        int start = workSpace.getSelectionStart();
        if(workSpace.length()==start+1){
            if(workSpace.getText().charAt(start)=='\n')
                return false;
        }
        return true;
    }

    private boolean canDel() {
        int start = workSpace.getSelectionStart();
        if (start >= 1) {
            if (workSpace.getText().charAt(start - 1) == '\n') return false;
            else if (workSpace.getText().charAt(start - 1) == '*' |
                    workSpace.getText().charAt(start - 1) == '+' |
                    workSpace.getText().charAt(start - 1) == '-') {
                return true;
            }
        }
        if ((start >= editableStart)) return true;
        return false;
    }

    private boolean matrixCanWrite() {
        int start = workSpace.getSelectionStart();
        if (start == 0)
            return true;
         else {
            char ch = workSpace.getText().toString().charAt(start - 1);
            if (ch == '+' || ch == '-' || ch == '*' || ch == '(' || ch == '\n') {
                return true;
            }
        }
        return false;
    }

    private void setCursor() {
        Editable edit = workSpace.getText();
        Selection.setSelection(edit, workSpace.length() - 1);
    }

    private void setCursor(int i) {
        Editable edit = workSpace.getText();
        Selection.setSelection(edit, i);
    }

    //endregion

    //region Write

    public void write(Matrix X) {
        if (X.message != null) {
            workSpace.append("\n" + X.message + "\n");
        }
        writeMatrix(X);
    }

    private void writeMatrix(Matrix X) {
        StringBuilder ident = new StringBuilder();
        StringBuilder newLine = new StringBuilder("\n");
        StringBuilder twoSpace = new StringBuilder("  ");
        StringBuilder oneSpace = new StringBuilder(" ");
        int max = 1;
        int n = 0;
        int col = X.columns();
        //столбец - ширина
        Map<Integer, Integer> MaxWidth = new HashMap<Integer, Integer>();
        //в цикле находим максимальную длину строки в каждом столбце
        for (int j = 0; j < col; j++) {
            max = 1;
            MaxWidth.put(j, max);
            for (int i = 0; i < X.rows(); i++) {
                n = X.getSElem(i, j).length();
                if (n > max) {
                    max = n;
                    MaxWidth.remove(j);
                    MaxWidth.put(j, max);
                }
            }
        }
        int totalLength = 0;
        for (int L : MaxWidth.values())
            totalLength += L;
        totalLength += (X.columns() - 1) * 2;
        while (!fit(totalLength)) {
            float size = workSpace.getTextSize();
            size -= 3;
            workSpace.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        }
        final Paint paint = workSpace.getPaint();
        float w = paint.measureText("A");
        int u = (int) Math.floor(w);
        totalLength = totalLength * u;
        int t = (int) Math.floor((workSpace.getWidth() - totalLength) * 0.5);
        t = (int) Math.floor(t / w);
        for (int h = 0; h < t; h++)
            ident.append(oneSpace);
        int width = 0;
        int need = 0;
        for (int i = 0; i < X.rows(); i++) {
            workSpace.append(newLine);
            for (int j = 0; j < X.columns(); j++) {
                if (j != 0) {
                    //отступ в два пробела перед началом нового столбца
                    workSpace.append(twoSpace);
                } else {
                    //отступ шириной в имя для первого столбца
                    workSpace.append(ident);
                }
                width = MaxWidth.get(j);
                need = width - X.getSElem(i, j).length();
                for (int z = 0; z < need; z++)
                    workSpace.append(oneSpace);
                workSpace.append(nf.format(X.getElem(i, j)));
            }
        }
        workSpace.append(newLine);
        Editable edit = workSpace.getText();
        Selection.setSelection(edit, workSpace.length());
        workSpace.computeScroll();
        editableStart = workSpace.length();
    }

    private boolean fit(int n) {
        final Paint paint = workSpace.getPaint();
        float w = paint.measureText("A");
        if (workSpace.getWidth() > n * w)
            return true;
        else
            return false;
    }

    //endregion

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart(){
        viewMatrix();
        super.onRestart();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }
}
