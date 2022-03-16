package com.test.trendview.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ListUtiles {

	public static boolean isEmpty(ArrayList list) {
		if (null == list || list.isEmpty()) {
			return true;
		}
		return false;
	}

	public static boolean isAllEmpty(ArrayList l1, ArrayList l2) {
		if (isEmpty(l1) && isEmpty(l2)) {
			return true;
		}
		return false;
	}

	public static int getListSize(ArrayList list) {
		return (null == list ? 0 : list.size());
	}

	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		params.height += 5;// if without this statement,the listview will be a
							// little short
		listView.setLayoutParams(params);
	}
}
