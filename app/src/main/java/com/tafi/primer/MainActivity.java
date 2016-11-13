package com.tafi.primer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;

import com.tafi.primer.utils.AnimationHelper;
import com.tafi.primer.utils.PrimeHelper;

public class MainActivity extends AppCompatActivity {

    private static final int MAX_PROGRESS = 100;
    private static final int PROGRESS_ERROR_PENALTY_PERCENT = 13;
    private static final int LEVEL_TIME_MSECS = 10000;
    private static final int LEVEL_INTERVAL_TIME_MSECS = 5;

    private ViewHolder mViewHolder;
    private CountDownTimer mCountDownTimer;
    private Animation.AnimationListener mErrorListener;
    private Animation.AnimationListener mLevelWinListener;
    private long mTimeRemaining = -1;
    private boolean mGameActive = false;
    private int mCurrentLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewHolder = new ViewHolder(this);

        mLevelWinListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mViewHolder.setHighScoreTextVisibility(mCurrentLevel > getSavedHighestLevel());
                mViewHolder.setBalloonsVisibility(true);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewHolder.setBalloonsVisibility(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        };

        mErrorListener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                ((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(250);
                mViewHolder.mError.setVisibility(View.VISIBLE);
                mViewHolder.mToFactor.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewHolder.mError.setVisibility(View.GONE);
                mViewHolder.mToFactor.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }
        };

        mViewHolder.disablePrimeButtons();
        mViewHolder.setHighScoreValue(getSavedHighestLevel());

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share: {
                shareGame();
                return true;
            }

            case R.id.action_help: {
                View helpBubble = findViewById(R.id.help_bubble);
                setHelpVisibility(!(helpBubble.getVisibility() == View.VISIBLE));
                return true;
            }

            case R.id.action_reset_high_score: {
                mViewHolder.setHighScoreValue(0);
                setSavedHighestLevel(0);
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void setSavedHighestLevel(int level) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.saved_high_score), level);
        editor.commit();
    }

    private int getSavedHighestLevel() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getInt(getString(R.string.saved_high_score), 0);
    }

    public void onPrimeClick(View view) {
        if (!mGameActive) {
            return;
        }
        CharSequence currentNumberStr = mViewHolder.mToFactor.getText();
        CharSequence buttonValueStr = ((Button)view).getText();
        int currentNumber = Integer.parseInt(currentNumberStr.toString());
        int buttonValue = Integer.parseInt(buttonValueStr.toString());
        double result = ((double)currentNumber) / ((double)buttonValue);

        if (result == (int)result) { // Correct prime
            mViewHolder.mToFactor.setText(Integer.toString((int)result));
            if (result == 1) {
                winLevel();
            }
        }
        else { // Incorrect prime
            int currentProgress = mViewHolder.mProgressBar.getProgress();
            int progressPenalty = (int)((double)MAX_PROGRESS * ((double)PROGRESS_ERROR_PENALTY_PERCENT/100.));
            int timePenalty = (int)((double)LEVEL_TIME_MSECS * ((double)PROGRESS_ERROR_PENALTY_PERCENT/100.));
            if (currentProgress > progressPenalty) {
                mCountDownTimer.cancel();
                int newProgress = currentProgress - progressPenalty;
                mViewHolder.mProgressBar.setProgress(newProgress);
                long newTime = mTimeRemaining - timePenalty;
                startClock(newTime);
                animateError();
            }
            else {
                loseGame();
            }
        }
    }

    public void onStartClick(View view) {
        mGameActive = true;
        mViewHolder.mToFactor.clearAnimation();

        // Visibilities
        setHelpVisibility(false);
        mViewHolder.mLoseFrame.setVisibility(View.INVISIBLE);
        mViewHolder.mStart.setVisibility(View.INVISIBLE);
        mViewHolder.mProgressBar.setVisibility(View.VISIBLE);

        // Texts
        mViewHolder.mLevel.setText(getString(R.string.level_number, mCurrentLevel));

        // Problem set
        PrimeHelper.ProblemSet problemSet = PrimeHelper.randomizeProblemSet(mCurrentLevel);
        mViewHolder.showProblemSet(problemSet);

        // Clock
        startClock(LEVEL_TIME_MSECS);
    }

    private void shareGame() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void onHideHelp(View view) {
        setHelpVisibility(false);
    }

    private void setHelpVisibility(final boolean visible) {
        if ((visible && mViewHolder.mHelpBubble.getVisibility() == View.VISIBLE) ||
                (!visible && mViewHolder.mHelpBubble.getVisibility() == View.INVISIBLE)) {
            return;
        }

        if (visible) {
            mViewHolder.mLoseFrame.setVisibility(View.INVISIBLE);
        }

        float fromX = visible ? 0 : 1;
        float toX = visible ? 1 : 0;
        float fromY = visible ? 0 : 1;
        float toY = visible ? 1 : 0;

        Animation helpAnimation = new ScaleAnimation(fromX, toX, fromY, toY, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        helpAnimation.setDuration(300);
        helpAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (visible) {
                    mViewHolder.mHelpBubble.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (!visible) {
                    mViewHolder.mHelpBubble.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mViewHolder.mHelpBubble.startAnimation(helpAnimation);
    }

    private void finishLevel(boolean setStartButtonText) {
        setHelpVisibility(false);
        mGameActive = false;
        mCurrentLevel++;
        mViewHolder.mLevel.setText("");
        if (setStartButtonText) {
            mViewHolder.mStart.setText(getString(R.string.start_level_number, mCurrentLevel));
        }
        mViewHolder.mStart.setVisibility(View.VISIBLE);
        mViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
        mViewHolder.mProgressBar.setProgress(MAX_PROGRESS);
        mViewHolder.mToFactor.setText("");
        mViewHolder.disablePrimeButtons();
    }

    private void startClock(final long startTime) {

        mCountDownTimer = new CountDownTimer(startTime, LEVEL_INTERVAL_TIME_MSECS) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeRemaining = millisUntilFinished;
                int currentProgress = (int)(MAX_PROGRESS * (millisUntilFinished / (double)LEVEL_TIME_MSECS));
                mViewHolder.mProgressBar.setProgress(currentProgress);
            }

            @Override
            public void onFinish() {
                mTimeRemaining = -1;
                loseGame();
            }
        };
        mCountDownTimer.start();
    }

    private void animateError() {
        Animation scaleUp = new ScaleAnimation(1, 2, 1, 2, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleUp.setDuration(250);
        scaleUp.setAnimationListener(mErrorListener);
        mViewHolder.mError.startAnimation(scaleUp);
    }

    private void loseGame() {
        mCurrentLevel = 0;
        mCountDownTimer.cancel();
        finishLevel(false);
        animateLose();
    }

    private void animateLose() {
        int totalDuration = 1500;
        Animation grumpyAnimation = AnimationHelper.getScaleAnimation(0, 1, totalDuration);
        grumpyAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mViewHolder.mLoseFrame.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewHolder.mStart.setText(getString(R.string.restart));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mViewHolder.mLoseFrame.startAnimation(grumpyAnimation);
    }

    private void winLevel() {
        mCountDownTimer.cancel();
        animateWin();
        if (mCurrentLevel > getSavedHighestLevel()) {
            setSavedHighestLevel(mCurrentLevel);
            mViewHolder.setHighScoreValue(mCurrentLevel);

            float scale = 2f;
            int duration = 1000;
            final ScaleAnimation getSmallAnimation = new ScaleAnimation(scale, 1, scale, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            getSmallAnimation.setDuration(duration);

            ScaleAnimation growAnimation = new ScaleAnimation(1, scale, 1, scale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            growAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mViewHolder.mHighScoreValue.startAnimation(getSmallAnimation);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            growAnimation.setDuration(duration);
//            growAnimation.setRepeatCount(3);

            mViewHolder.mHighScoreValue.startAnimation(growAnimation);

        }
        finishLevel(true);
    }

    private void animateWin() {
        int totalDuration = 3000;

        Animation numberAnimationSize = new ScaleAnimation(1, 5, 1, 5, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        numberAnimationSize.setDuration(totalDuration);
        Animation numberAnimationOpacity = new AlphaAnimation(1, 0);
        numberAnimationOpacity.setDuration(totalDuration);
        AnimationSet numberAnimation = new AnimationSet(true);
        numberAnimation.addAnimation(numberAnimationOpacity);
        numberAnimation.addAnimation(numberAnimationSize);
        mViewHolder.mToFactor.startAnimation(numberAnimation);

        Animation balloon1Animation = new TranslateAnimation(0, 0, 700, -1500);
        balloon1Animation.setDuration((int) (totalDuration * 0.7));
        balloon1Animation.setAnimationListener(mLevelWinListener); // TODO there must be a better way
        mViewHolder.mBalloon1.startAnimation(balloon1Animation);

        Animation balloon2Animation = new TranslateAnimation(0, 0, 700, -1500);
        balloon2Animation.setDuration(totalDuration);
        mViewHolder.mBalloon2.startAnimation(balloon2Animation);

        Animation balloon3Animation = new TranslateAnimation(0, 0, 700, -1500);
        balloon3Animation.setDuration((int) (totalDuration * 0.8));
        mViewHolder.mBalloon3.startAnimation(balloon3Animation);
    }

}
