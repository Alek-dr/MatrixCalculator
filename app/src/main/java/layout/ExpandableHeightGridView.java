package layout;

import android.view.View;
import android.widget.GridLayout;
import android.widget.GridView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
/**
 * Created by alexander on 28.08.17.
 */

public class ExpandableHeightGridView extends GridLayout {

    boolean expanded = false;

    public ExpandableHeightGridView(Context context)
    {
        super(context);
    }

    public ExpandableHeightGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ExpandableHeightGridView(Context context, AttributeSet attrs,
                                    int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public boolean isExpanded()
    {
        return expanded;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // HACK! TAKE THAT ANDROID!
        if (isExpanded())
        {
            // Calculate entire height by providing a very large height hint.
            // View.MEASURED_SIZE_MASK represents the largest height possible.
            int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
                    MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);

            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        }
        else
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public void removeAllViews(){
        super.removeAllViews();
    }

    @Override
    public void addView(View v){
        super.addView(v);
    }

//    @Override
//    public void setRowCount(int row){
//        super.setRowCount(row);
//    }
//
//    @Override
//    public void setColumnCount(int col){
//        super.setColumnCount(col);
//    }

    public void setExpanded(boolean expanded)
    {
        this.expanded = expanded;
    }
}
