package com.example.trabinerson.primer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final int[] PRIMES = {2, 3, 5, 7, 11, 13, 17, 19, 23};
    private static final int MAX_PROGRESS = 100;
    private static final int MAX_POSSIBLE_TO_FACTOR = 50000;
    private static final int PROGRESS_ERROR_PENALTY_PERCENT = 13;
    private static final int LEVEL_TIME_MSECS = 10000;
    private static final int LEVEL_INTERVAL_TIME_MSECS = 5;
    private static final int FIRST_LARGE_PRIME = 13;

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
                mViewHolder.mBalloon1.setVisibility(View.VISIBLE);
                mViewHolder.mBalloon2.setVisibility(View.VISIBLE);
                mViewHolder.mBalloon3.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewHolder.mBalloon1.setVisibility(View.GONE);
                mViewHolder.mBalloon2.setVisibility(View.GONE);
                mViewHolder.mBalloon3.setVisibility(View.GONE);
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
                setHelpVisibility(true);
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
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
        mViewHolder.mToFactor.clearAnimation();
        mGameActive = true;
        mViewHolder.mLoseFrame.setVisibility(View.INVISIBLE);
        mViewHolder.mLevel.setText("Level " + mCurrentLevel);
        randomizeValues(mCurrentLevel);
        mViewHolder.mStart.setVisibility(View.INVISIBLE);
        mViewHolder.mProgressBar.setVisibility(View.VISIBLE);
        startClock(LEVEL_TIME_MSECS);
    }

    private void shareGame() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Factor those primes! Check out Primer: http://play.google.com/store/apps/details?id=com.primer.game");
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    public void onHideHelp(View view) {
        setHelpVisibility(false);
    }

    private void setHelpVisibility(boolean visible) {
        findViewById(R.id.help_bubble).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void finishLevel(boolean setStartButtonText) {
        mGameActive = false;
        mCurrentLevel++;
        mViewHolder.mLevel.setText("");
        if (setStartButtonText) {
            mViewHolder.mStart.setText("Start Level " + mCurrentLevel);
        }
        mViewHolder.mStart.setVisibility(View.VISIBLE);
        mViewHolder.mProgressBar.setVisibility(View.INVISIBLE);
        mViewHolder.mProgressBar.setProgress(MAX_PROGRESS);
        mViewHolder.mPrime1.setText("");
        mViewHolder.mPrime2.setText("");
        mViewHolder.mPrime3.setText("");
        mViewHolder.mPrime4.setText("");
        mViewHolder.mPrime5.setText("");
        mViewHolder.mToFactor.setText("");
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

    private void randomizeValues(int level) {

        int numPrimes = 5;

        // Set max possible prime by level
        int maxPrimeIndex = PRIMES.length - 1;
        if (level <= PRIMES.length - numPrimes + 1) {
            maxPrimeIndex = numPrimes + level - 2;
        }

        // Get ArrayList of primes - bah java
        ArrayList<Integer> primes = new ArrayList<>(maxPrimeIndex + 1);
        for (int i = 0; i <= maxPrimeIndex; i++) {
            primes.add(i, PRIMES[i]);
        }

        // Randomize primes
        Collections.shuffle(primes);
        ArrayList<Integer> chosenPrimes = new ArrayList<>(numPrimes);
        for (int k = 0; k < numPrimes; k++) {
            chosenPrimes.add(k, primes.get(k));
        }

        // Make sure either 2 or 3 are chosen
        int smallest;
        Random random = new Random();
        if (chosenPrimes.contains(2)) {
            smallest = 2;
        }
        else if (chosenPrimes.contains(3)) {
            smallest = 3;
        }
        else {
            smallest = 2;
            if (random.nextBoolean()) {
                smallest++;
            }
            chosenPrimes.add(0, smallest);
            chosenPrimes.remove(chosenPrimes.size() - 1);
        }

        // Choose primes to use for number
        int numFactors = (int)(level / 2.) + 2;
        ArrayList<Integer> participatingPrimes = new ArrayList<>(numFactors);
        int numLargeChosen = 0;
        int toFactor = 1;
        for (int k = 0; k < numFactors; k++) {
            int prime = chooseNextPrime(random, numPrimes, chosenPrimes, smallest, toFactor, level, numLargeChosen);
            if (prime >= FIRST_LARGE_PRIME) {
                numLargeChosen++;
            }
            participatingPrimes.add(k, prime);
            toFactor *= prime;
        }

        // Set primes
        Collections.sort(chosenPrimes);
        int prime1 = chosenPrimes.get(0);
        int prime2 = chosenPrimes.get(1);
        int prime3 = chosenPrimes.get(2);
        int prime4 = chosenPrimes.get(3);
        int prime5 = chosenPrimes.get(4);

        // Set values on screen
        mViewHolder.mPrime1.setText(Integer.toString(prime1));
        mViewHolder.mPrime2.setText(Integer.toString(prime2));
        mViewHolder.mPrime3.setText(Integer.toString(prime3));
        mViewHolder.mPrime4.setText(Integer.toString(prime4));
        mViewHolder.mPrime5.setText(Integer.toString(prime5));
        mViewHolder.mToFactor.setText(Integer.toString(toFactor));
    }

    private static int chooseNextPrime(
            Random random, int numPrimes, ArrayList<Integer> chosenPrimes,
            int smallest, int currentProduct, int level, int numLargeChosen) {

        // Try to randomize a good prime
        int maxTrials = 500;
        int numTrials = 0;
        int prime;
        do {
            int r = random.nextInt(numPrimes);
            prime = chosenPrimes.get(r);
            if (isPrimeKosher(prime, level, numLargeChosen, currentProduct)) {
                return prime;
            }
            numTrials++;
        }
        while (numTrials < maxTrials);

        // OK, all primes suck
        return smallest;
    }

    private static boolean isPrimeKosher(int prime, int level, int numLargeChosen, int currentProduct) {
        return !isPrimeTooLarge(prime, level, numLargeChosen) && !doesPrimeExceedMax(prime, currentProduct);
    }

    private static boolean isPrimeTooLarge(int prime, int level, int numLargeChosen) {
        if (prime < FIRST_LARGE_PRIME) {
            return false;
        }
        return (numLargeChosen > 0 && level < 10) || (numLargeChosen > 1 && level < 20);
    }

    private static boolean doesPrimeExceedMax(int prime, int currentProduct) {
        return prime * currentProduct >= MAX_POSSIBLE_TO_FACTOR;
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
        Animation grumpyAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        grumpyAnimation.setDuration(totalDuration);
        grumpyAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mViewHolder.mLoseFrame.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mViewHolder.mStart.setText("Restart");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mViewHolder.mLoseFrame.startAnimation(grumpyAnimation);
    }

    private void winLevel() {
        mCountDownTimer.cancel();
        finishLevel(true);
        animateWin();
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

    public static class ViewHolder {

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
        public final ImageView mBalloon1;
        public final ImageView mBalloon2;
        public final ImageView mBalloon3;

        public ViewHolder(Activity activity) {
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
            mBalloon1 = (ImageView) activity.findViewById(R.id.ballon1);
            mBalloon2 = (ImageView) activity.findViewById(R.id.ballon2);
            mBalloon3 = (ImageView) activity.findViewById(R.id.ballon3);
        }
    }
}
