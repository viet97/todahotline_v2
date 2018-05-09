package org.linphone.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListView;

/**
 * Created by QuocVietDang1 on 5/9/2018.
 */

public class ExpandedListView extends ListView {
    private static String TAG = "ExpandedListView";

    public ExpandedListView(Context context) {
        super(context);
    }

    public ExpandedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setListViewHeightBasedOnChildren(this);
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        Adapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int totalHeight = 0;
        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem != null) {
                listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
                totalHeight += listItem.getMeasuredHeight();
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        Log.d(TAG, "setListViewHeightBasedOnChildren: " + params.height);
        ;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
