/* Copyright (C) 2013 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations
 * under the License. */

package com.lbconsulting.alist.classes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.lbconsulting.alist.database.ItemsTable;
import com.lbconsulting.alist.utilities.MyLog;

/**
 * The dynamic ListView is an extension of ListView that supports cell dragging and swapping. This layout is in charge of positioning the hover cell
 * in the correct location on the screen in response to user touch events. It uses the position of the hover cell to determine when two cells should
 * be swapped. If two cells should be swapped, all the corresponding data set and layout changes are handled here. If no cell is selected, all the
 * touch events are passed down to the ListView and behave normally. If one of the items in the ListView experiences a long press event, the contents
 * of its current visible state are captured as a bitmap and its visibility is set to INVISIBLE. A hover cell is then created and added to this layout
 * as an overlaying BitmapDrawable above the ListView. Once the hover cell is translated some distance to signify an item swap, a data set change
 * accompanied by animation takes place. When the user releases the hover cell, it animates into its corresponding position in the ListView. When the
 * hover cell is either above or below the bounds of the ListView, this ListView also scrolls on its own so as to reveal additional content. Note: It
 * is required that your adapter have stable IDs [see hasStableIds()], and implement the SwappableListAdapter interface
 */
public class DynamicListView extends ListView {

	private final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
	private final int MOVE_DURATION = 150;
	private final int LINE_THICKNESS = 15;

	private int mLastEventY = -1;

	private int mDownY = -1;
	private int mDownX = -1;

	private int mTotalOffset = 0;

	private boolean mCellIsMobile = false;
	private boolean mIsMobileScrolling = false;
	private int mSmoothScrollAmountAtEdge = 0;

	private final int INVALID_ID = -1;
	private long mAboveItemId = INVALID_ID;
	private long mMobileItemId = INVALID_ID;
	private long mBelowItemId = INVALID_ID;
	private long mSwitchedItemId = INVALID_ID;

	private BitmapDrawable mHoverCell;
	private Rect mHoverCellCurrentBounds;
	private Rect mHoverCellOriginalBounds;

	private final int INVALID_POINTER_ID = -1;
	private int mActivePointerId = INVALID_POINTER_ID;

	private boolean mIsWaitingForScrollFinish = false;
	private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
	private static boolean isManualSort = false;

	public DynamicListView(Context context) {
		super(context);
		init(context);
	}

	public DynamicListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public DynamicListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void init(Context context) {
		setOnItemLongClickListener(mOnItemLongClickListener);
		setOnScrollListener(mScrollListener);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		mSmoothScrollAmountAtEdge = (int) (SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);
	}

	public static void setManualSort(boolean manualSort) {
		isManualSort = manualSort;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		if (null == adapter) {
			return;
		}

		if (!adapter.hasStableIds()) {
			throw new IllegalArgumentException("adapter must have stable ids");
		}

		if (!(adapter instanceof SwappableListAdapter)) {
			throw new IllegalArgumentException("adapter must implement SwappableListAdapter");
		}
	}

	/**
	 * Listens for long clicks on any items in the ListView. When a cell has been selected, the hover cell is created and set up.
	 */
	private AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View v, int pos, long itemID) {
			MyLog.i("DynamicListView", "onItemLongClick");
			if (isManualSort) {
				mTotalOffset = 0;
				int position = pointToPosition(mDownX, mDownY);
				mMobileItemId = itemID;
				MyLog.i("DynamicListView", "onItemLongClick() itemID:" + itemID + "; listViewPosition:" + pos);

				int itemNum = position - getFirstVisiblePosition();
				View selectedView = getChildAt(itemNum);
				mHoverCell = getAndAddHoverView(selectedView);

				ItemsTable.setItemInvisible(getContext(), itemID);

				mCellIsMobile = true;

				updateNeighborViewsForID(mMobileItemId);
			}
			return true;
		}
	};

	public void ManualSort(int pos, long itemID) {
		MyLog.i("DynamicListView", "onItemLongClick");

		// DoManualSort(pos, itemID);

	}

	/*	private static void DoManualSort(int pos, long itemID) {
			mTotalOffset = 0;
			int position = pointToPosition(mDownX, mDownY);
			mMobileItemId = itemID;
			MyLog.i("DynamicListView", "onItemLongClick() itemID:" + itemID + "; listViewPosition:" + pos);

			int itemNum = position - getFirstVisiblePosition();
			View selectedView = getChildAt(itemNum);
			mHoverCell = getAndAddHoverView(selectedView);

			ItemsTable.setItemInvisible(getContext(), itemID);

			mCellIsMobile = true;

			updateNeighborViewsForID(mMobileItemId);
		}*/

	/**
	 * Creates the hover cell with the appropriate bitmap and of appropriate size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
	 * single time an invalidate call is made.
	 */
	private BitmapDrawable getAndAddHoverView(View v) {

		int w = v.getWidth();
		int h = v.getHeight();
		int top = v.getTop();
		int left = v.getLeft();

		Bitmap b = getBitmapWithBorder(v);

		BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

		mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
		mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);

		drawable.setBounds(mHoverCellCurrentBounds);

		return drawable;
	}

	/** Draws a black border over the screenshot of the view passed in. */
	private Bitmap getBitmapWithBorder(View v) {
		Bitmap bitmap = getBitmapFromView(v);
		Canvas can = new Canvas(bitmap);

		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(LINE_THICKNESS);
		paint.setColor(Color.BLACK);

		can.drawBitmap(bitmap, 0, 0, null);
		can.drawRect(rect, paint);

		return bitmap;
	}

	/** Returns a bitmap showing a screenshot of the view passed in. */
	private Bitmap getBitmapFromView(View v) {
		Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		v.draw(canvas);
		return bitmap;
	}

	/**
	 * Stores a reference to the views above and below the item currently corresponding to the hover cell. It is important to note that if this item
	 * is either at the top or bottom of the list, mAboveItemId or mBelowItemId may be invalid.
	 */
	private void updateNeighborViewsForID(long itemID) {
		int position = getPositionForID(itemID);
		ListAdapter adapter = getAdapter();
		mAboveItemId = adapter.getItemId(position - 1);
		mBelowItemId = adapter.getItemId(position + 1);
	}

	/** Retrieves the view in the list corresponding to itemID */
	public View getViewForID(long itemID) {
		int firstVisiblePosition = getFirstVisiblePosition();
		ListAdapter adapter = getAdapter();
		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			int position = firstVisiblePosition + i;
			long id = adapter.getItemId(position);
			if (id == itemID) {
				return v;
			}
		}
		return null;
	}

	/** Retrieves the position in the list corresponding to itemID */
	public int getPositionForID(long itemID) {
		View v = getViewForID(itemID);
		if (v == null) {
			return -1;
		}
		return getPositionForView(v);
	}

	/**
	 * dispatchDraw gets invoked when all the child views are about to be drawn. By overriding this method, the hover cell (BitmapDrawable) can be
	 * drawn over the listview's items whenever the listview is redrawn.
	 */
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mHoverCell != null) {
			mHoverCell.draw(canvas);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				mDownX = (int) event.getX();
				mDownY = (int) event.getY();
				mActivePointerId = event.getPointerId(0);
				break;

			case MotionEvent.ACTION_MOVE:
				if (mActivePointerId == INVALID_POINTER_ID) {
					break;
				}

				int pointerIndex = event.findPointerIndex(mActivePointerId);

				mLastEventY = (int) event.getY(pointerIndex);
				int deltaY = mLastEventY - mDownY;

				if (mCellIsMobile) {
					mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mHoverCellOriginalBounds.top
							+ deltaY + mTotalOffset);
					mHoverCell.setBounds(mHoverCellCurrentBounds);
					invalidate();

					handleCellSwitch();

					mIsMobileScrolling = false;
					handleMobileCellScroll();

					return false;
				}
				break;

			case MotionEvent.ACTION_UP:
				touchEventsEnded();
				break;

			case MotionEvent.ACTION_CANCEL:
				touchEventsCancelled();
				break;

			case MotionEvent.ACTION_POINTER_UP:
				/* If a multi-touch event took place and the original touch dictating
				 * the movement of the hover cell has ended, then the dragging event
				 * ends and the hover cell is animated to its corresponding position
				 * in the ListView. */
				pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
						MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				final int pointerId = event.getPointerId(pointerIndex);
				if (pointerId == mActivePointerId) {
					touchEventsEnded();
				}
				break;

			default:
				break;
		}

		return super.onTouchEvent(event);
	}

	/**
	 * This method determines whether the hover cell has been shifted far enough to invoke a cell swap. If so, then the respective cell swap candidate
	 * is determined and the data set is changed. Upon posting a notification of the data set change, a layout is invoked to place the cells in the
	 * right place. Using a ViewTreeObserver and a corresponding OnPreDrawListener, we can offset the cell being swapped to where it previously was
	 * and then animate it to its new position.
	 */
	private void handleCellSwitch() {
		final int deltaY = mLastEventY - mDownY;
		int deltaYTotal = mHoverCellOriginalBounds.top + mTotalOffset + deltaY;

		View aboveView = getViewForID(mAboveItemId);
		View mobileView = getViewForID(mMobileItemId);
		View belowView = getViewForID(mBelowItemId);

		boolean isBelow = (belowView != null) && (deltaYTotal > belowView.getTop());
		boolean isAbove = (aboveView != null) && (deltaYTotal < aboveView.getTop());

		if (isBelow || isAbove) {
			final long switchItemID = isBelow ? mBelowItemId : mAboveItemId;
			View switchView = isBelow ? belowView : aboveView;

			// Check to see if a manual sort order switch is required
			if (switchItemID != mSwitchedItemId) {
				// switch the items manual sort order
				final SwappableListAdapter adapter = (SwappableListAdapter) getAdapter();
				adapter.swap(mMobileItemId, switchItemID, mSwitchedItemId);
				mSwitchedItemId = switchItemID;
				if (switchView != null) {
					MyLog.i("DynamicListView: handleCellSwitch", "Swap " + getViewItemName(mobileView) + " with "
							+ getViewItemName(switchView));
				}
			}

			if (switchView == null) {
				updateNeighborViewsForID(mMobileItemId);
				return;
			}

			mDownY = mLastEventY;

			final int switchViewStartTop = switchView.getTop();

			mobileView.setVisibility(View.VISIBLE);
			switchView.setVisibility(View.INVISIBLE);
			MyLog.i("DynamicListView: handleCellSwitch", "mobileView:VISIBLE switchView:INVISIBLE");

			updateNeighborViewsForID(mMobileItemId);

			final ViewTreeObserver observer = getViewTreeObserver();
			observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

				@Override
				public boolean onPreDraw() {
					observer.removeOnPreDrawListener(this);

					View switchedView = getViewForID(switchItemID);

					mTotalOffset += deltaY;

					int switchViewNewTop = switchedView.getTop();
					int delta = switchViewStartTop - switchViewNewTop;

					switchedView.setTranslationY(delta);

					ObjectAnimator animator = ObjectAnimator.ofFloat(switchedView, View.TRANSLATION_Y, 0);
					animator.setDuration(MOVE_DURATION);
					animator.start();

					return true;
				}
			});
		}
	}

	private String getViewItemName(View listItemRow) {
		View ll = ((ViewGroup) listItemRow).getChildAt(1);
		TextView tv = (TextView) ((ViewGroup) ll).getChildAt(0);
		return tv.getText().toString();
	}

	/**
	 * Resets all the appropriate fields to a default state while also animating the hover cell back to its correct location.
	 */
	private void touchEventsEnded() {
		final View mobileView = getViewForID(mMobileItemId);
		if (mCellIsMobile || mIsWaitingForScrollFinish) {
			mCellIsMobile = false;
			mIsWaitingForScrollFinish = false;
			mIsMobileScrolling = false;
			mActivePointerId = INVALID_POINTER_ID;

			ItemsTable.setItemVisible(getContext(), mMobileItemId);
			ItemsTable.setItemVisible(getContext(), mSwitchedItemId);
			mSwitchedItemId = INVALID_POINTER_ID;

			// If the auto scroller has not completed scrolling, we need to wait for it to
			// finish in order to determine the final location of where the hover cell
			// should be animated to.
			if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
				mIsWaitingForScrollFinish = true;
				return;
			}

			mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mobileView.getTop());

			ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(mHoverCell, "bounds",
					sBoundEvaluator, mHoverCellCurrentBounds);
			hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator valueAnimator) {
					invalidate();
				}
			});
			hoverViewAnimator.addListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationStart(Animator animation) {
					setEnabled(false);
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					mAboveItemId = INVALID_ID;
					mMobileItemId = INVALID_ID;
					mBelowItemId = INVALID_ID;
					mobileView.setVisibility(VISIBLE);
					mHoverCell = null;
					setEnabled(true);
					invalidate();
				}
			});
			hoverViewAnimator.start();
		} else {
			touchEventsCancelled();
		}
	}

	/**
	 * Resets all the appropriate fields to a default state.
	 */
	private void touchEventsCancelled() {
		View mobileView = getViewForID(mMobileItemId);
		ItemsTable.setItemVisible(getContext(), mMobileItemId);
		ItemsTable.setItemVisible(getContext(), mSwitchedItemId);
		mSwitchedItemId = INVALID_POINTER_ID;

		if (mCellIsMobile) {
			mAboveItemId = INVALID_ID;
			mMobileItemId = INVALID_ID;
			mBelowItemId = INVALID_ID;
			mobileView.setVisibility(VISIBLE);
			mHoverCell = null;
			invalidate();
		}
		mCellIsMobile = false;
		mIsMobileScrolling = false;
		mActivePointerId = INVALID_POINTER_ID;
	}

	/**
	 * This TypeEvaluator is used to animate the BitmapDrawable back to its final location when the user lifts his finger by modifying the
	 * BitmapDrawable's bounds.
	 */
	private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {

		@Override
		public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
			return new Rect(interpolate(startValue.left, endValue.left, fraction),
					interpolate(startValue.top, endValue.top, fraction),
					interpolate(startValue.right, endValue.right, fraction),
					interpolate(startValue.bottom, endValue.bottom, fraction));
		}

		public int interpolate(int start, int end, float fraction) {
			return (int) (start + fraction * (end - start));
		}
	};

	/**
	 * Determines whether this ListView is in a scrolling state invoked by the fact that the hover cell is out of the bounds of the listview;
	 */
	private void handleMobileCellScroll() {
		mIsMobileScrolling = handleMobileCellScroll(mHoverCellCurrentBounds);
	}

	/**
	 * This method is in charge of determining if the hover cell is above or below the bounds of the ListView. If so, the ListView does an appropriate
	 * upward or downward smooth scroll so as to reveal new items.
	 */
	public boolean handleMobileCellScroll(Rect r) {
		int offset = computeVerticalScrollOffset();
		int height = getHeight();
		int extent = computeVerticalScrollExtent();
		int range = computeVerticalScrollRange();
		int hoverViewTop = r.top;
		int hoverHeight = r.height();

		if (hoverViewTop <= 0 && offset > 0) {
			smoothScrollBy(-mSmoothScrollAmountAtEdge, 0);
			return true;
		}

		if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
			smoothScrollBy(mSmoothScrollAmountAtEdge, 0);
			return true;
		}

		return false;
	}

	/**
	 * This scroll listener is added to the ListView in order to handle cell swapping when the cell is either at the top or bottom edge of the
	 * ListView. If the hover cell is at either edge of the ListView, the ListView will begin scrolling. As scrolling takes place, the ListView
	 * continuously checks if new cells became visible and determines whether they are potential candidates for a cell swap.
	 */
	private AbsListView.OnScrollListener mScrollListener = new AbsListView.OnScrollListener() {

		private int mPreviousFirstVisibleItem = -1;
		private int mPreviousVisibleItemCount = -1;
		private int mCurrentFirstVisibleItem;
		private int mCurrentVisibleItemCount;
		private int mCurrentScrollState;

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
				int totalItemCount) {
			mCurrentFirstVisibleItem = firstVisibleItem;
			mCurrentVisibleItemCount = visibleItemCount;

			mPreviousFirstVisibleItem = (mPreviousFirstVisibleItem == -1) ? mCurrentFirstVisibleItem
					: mPreviousFirstVisibleItem;
			mPreviousVisibleItemCount = (mPreviousVisibleItemCount == -1) ? mCurrentVisibleItemCount
					: mPreviousVisibleItemCount;

			checkAndHandleFirstVisibleCellChange();
			checkAndHandleLastVisibleCellChange();

			mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
			mPreviousVisibleItemCount = mCurrentVisibleItemCount;
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			mCurrentScrollState = scrollState;
			mScrollState = scrollState;
			isScrollCompleted();
		}

		/**
		 * This method is in charge of invoking 1 of 2 actions. Firstly, if the listview is in a state of scrolling invoked by the hover cell being
		 * outside the bounds of the listview, then this scrolling event is continued. Secondly, if the hover cell has already been released, this
		 * invokes the animation for the hover cell to return to its correct position after the listview has entered an idle scroll state.
		 */
		private void isScrollCompleted() {
			if (mCurrentVisibleItemCount > 0 && mCurrentScrollState == SCROLL_STATE_IDLE) {
				if (mCellIsMobile && mIsMobileScrolling) {
					handleMobileCellScroll();
				} else if (mIsWaitingForScrollFinish) {
					touchEventsEnded();
				}
			}
		}

		/**
		 * Determines if the ListView scrolled up enough to reveal a new cell at the top of the list. If so, then the appropriate parameters are
		 * updated.
		 */
		public void checkAndHandleFirstVisibleCellChange() {
			if (mCurrentFirstVisibleItem != mPreviousFirstVisibleItem) {
				if (mCellIsMobile && mMobileItemId != INVALID_ID) {
					updateNeighborViewsForID(mMobileItemId);
					handleCellSwitch();
				}
			}
		}

		/**
		 * Determines if the ListView scrolled down enough to reveal a new cell at the bottom of the list. If so, then the appropriate parameters are
		 * updated.
		 */
		public void checkAndHandleLastVisibleCellChange() {
			int currentLastVisibleItem = mCurrentFirstVisibleItem + mCurrentVisibleItemCount;
			int previousLastVisibleItem = mPreviousFirstVisibleItem + mPreviousVisibleItemCount;
			if (currentLastVisibleItem != previousLastVisibleItem) {
				if (mCellIsMobile && mMobileItemId != INVALID_ID) {
					updateNeighborViewsForID(mMobileItemId);
					handleCellSwitch();
				}
			}
		}
	};

	/**
	 * SwappableListAdapter is an interface that adds the ability to swap elements
	 */
	public interface SwappableListAdapter {

		/**
		 * swaps the items in the adapter, calling notifyDataSetChanged when finished
		 */
		public void swap(long mobileItemID, long switchItemID, long previousSwitchItemID);
	}
}
