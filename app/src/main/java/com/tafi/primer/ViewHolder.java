/*
 * Copyright (c) 2016 PayPal, Inc.
 *
 * All rights reserved.
 *
 * THIS CODE AND INFORMATION ARE PROVIDED "AS IS" WITHOUT WARRANTY OF ANY
 * KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND/OR FITNESS FOR A
 * PARTICULAR PURPOSE.
 */

package com.tafi.primer;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tafi.primer.utils.PrimeHelper;

/**
 * View holder for MainActivity.
 *
 * @author trabinerson
 */
public class ViewHolder {

    public final View mHelpBubble;
    public final TextView mLevel;
    public final Button mStart;
    public final ProgressBar mProgressBar;
    public final FrameLayout mLoseFrame;
    public final Button mPrime1;
    public final Button mPrime2;
    public final Button mPrime3;
    public final Button mPrime4;
    public final Button mPrime5;
    public final TextView mToFactor;
    public final ImageView mError;
    public final View mBalloon1;
    public final View mBalloon2;
    public final View mBalloon3;
    public final TextView mHighScoreText1;
    public final TextView mHighScoreText2;
    public final TextView mHighScoreText3;
    public final View mHighScore;
    public final TextView mHighScoreValue;

    public ViewHolder(Activity activity) {
        mHelpBubble = activity.findViewById(R.id.help_bubble);
        mLevel = (TextView) activity.findViewById(R.id.textview_level);
        mStart = (Button) activity.findViewById(R.id.button_start);
        mProgressBar = (ProgressBar) activity.findViewById(R.id.progress_bar);
        mLoseFrame = (FrameLayout) activity.findViewById(R.id.grumpy);
        mPrime1 = (Button) activity.findViewById(R.id.prime1);
        mPrime2 = (Button) activity.findViewById(R.id.prime2);
        mPrime3 = (Button) activity.findViewById(R.id.prime3);
        mPrime4 = (Button) activity.findViewById(R.id.prime4);
        mPrime5 = (Button) activity.findViewById(R.id.prime5);
        mToFactor = (TextView) activity.findViewById(R.id.to_factor);
        mError = (ImageView) activity.findViewById(R.id.error_view);
        mBalloon1 = activity.findViewById(R.id.ballon1);
        mBalloon2 = activity.findViewById(R.id.ballon2);
        mBalloon3 = activity.findViewById(R.id.ballon3);
        mHighScoreText1 = (TextView) activity.findViewById(R.id.ballon1_text);
        mHighScoreText2 = (TextView) activity.findViewById(R.id.ballon2_text);
        mHighScoreText3 = (TextView) activity.findViewById(R.id.ballon3_text);
        mHighScore = activity.findViewById(R.id.high_score);
        mHighScoreValue = (TextView) activity.findViewById(R.id.high_score_value);
    }

    public void disablePrimeButtons() {
        mPrime1.setText("");
        mPrime2.setText("");
        mPrime3.setText("");
        mPrime4.setText("");
        mPrime5.setText("");

        mPrime1.setEnabled(false);
        mPrime2.setEnabled(false);
        mPrime3.setEnabled(false);
        mPrime4.setEnabled(false);
        mPrime5.setEnabled(false);
    }

    public void showProblemSet(PrimeHelper.ProblemSet problemSet) {
        // Set values on screen
        mPrime1.setText(Integer.toString(problemSet.primes.get(0)));
        mPrime2.setText(Integer.toString(problemSet.primes.get(1)));
        mPrime3.setText(Integer.toString(problemSet.primes.get(2)));
        mPrime4.setText(Integer.toString(problemSet.primes.get(3)));
        mPrime5.setText(Integer.toString(problemSet.primes.get(4)));
        mToFactor.setText(Integer.toString(problemSet.toFactor));

        // Enable buttons
        mPrime1.setEnabled(true);
        mPrime2.setEnabled(true);
        mPrime3.setEnabled(true);
        mPrime4.setEnabled(true);
        mPrime5.setEnabled(true);
    }

    public void setBalloonsVisibility(boolean visible) {
        setViewVisibility(mBalloon1, visible, true);
        setViewVisibility(mBalloon2, visible, true);
        setViewVisibility(mBalloon3, visible, true);
    }

    public void setHighScoreTextVisibility(boolean visible) {
        setViewVisibility(mHighScoreText1, visible, false);
        setViewVisibility(mHighScoreText2, visible, false);
        setViewVisibility(mHighScoreText3, visible, false);
    }

    public void setHighScoreValue(int value) {
        setViewVisibility(mHighScore, value > 0, false);
        mHighScoreValue.setText(String.valueOf(value));
    }

    private void setViewVisibility(View view, boolean visible, boolean gone) {
        int visibility;
        if (visible) {
            visibility = View.VISIBLE;
        } else if (gone) {
            visibility = View.GONE;
        } else {
            visibility = View.INVISIBLE;
        }
        view.setVisibility(visibility);
    }
}
