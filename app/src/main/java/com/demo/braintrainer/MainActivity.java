package com.demo.braintrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView textViewOpinion0;
    private TextView textViewOpinion1;
    private TextView textViewOpinion2;
    private TextView textViewOpinion3;
    private TextView textViewTimer;
    private TextView textViewScore;
    private TextView textViewQuestion;
    private ImageView imageViewAnswer;
    private ArrayList<TextView> opinions = new ArrayList<>();
    private CountDownTimer timer;

    private String question;
    private int rightAnswer;
    private int rightAnswerPosition;
    private boolean isPositive;
    private int min = 5;
    private int max = 30;
    private int countOfQuestions = 0;
    private int countOfRightAnswers = 0;
    private boolean gameOver = false;
    private long countdownPeriod;
    private long currentMillisLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViewOpinion0 = findViewById(R.id.textViewOpinion0);
        textViewOpinion1 = findViewById(R.id.textViewOpinion1);
        textViewOpinion2 = findViewById(R.id.textViewOpinion2);
        textViewOpinion3 = findViewById(R.id.textViewOpinion3);
        textViewTimer = findViewById(R.id.textViewTimer);
        textViewScore = findViewById(R.id.textViewScore);
        imageViewAnswer = findViewById(R.id.imageViewAnswer);
        textViewQuestion = findViewById(R.id.textViewQuestion);
        opinions.add(textViewOpinion0);
        opinions.add(textViewOpinion1);
        opinions.add(textViewOpinion2);
        opinions.add(textViewOpinion3);
        countdownPeriod = 25000;
        imageViewAnswer.setVisibility(View.INVISIBLE);
        playNext();
        createDownTownTimer(countdownPeriod);
    }

    private void createDownTownTimer(long millis) {
        timer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                currentMillisLeft = millisUntilFinished;
                textViewTimer.setText(getTime(millisUntilFinished));
                if (millisUntilFinished < 10000) {
                    textViewTimer.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                } else {
                    textViewTimer.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                }
            }

            @Override
            public void onFinish() {
                gameOver = true;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                int maxRightAnswers = preferences.getInt("maxRightAnswers", 0);
                if (countOfRightAnswers >= maxRightAnswers) {
                    preferences.edit().putInt("maxRightAnswers", countOfRightAnswers).apply();
                }
                Intent intent = new Intent(MainActivity.this, ScoreActivity.class);
                intent.putExtra("result", countOfRightAnswers);
                startActivity(intent);
            }
        };
        timer.start();
    }

    private void playNext() {
        generateQuestion();
        for (int i = 0; i < opinions.size(); i++) {
            if (i == rightAnswerPosition) {
                opinions.get(i).setText(Integer.toString(rightAnswer));
            } else {
                opinions.get(i).setText(Integer.toString(generateWrongAnswer()));
            }
            String score = String.format("%s / %s", countOfRightAnswers, countOfQuestions);
            textViewScore.setText(score);
        }
    }

    private void generateQuestion() {
        int a = (int) (Math.random() * (max - min + 1) + min);
        int b = (int) (Math.random() * (max - min + 1) + min);
        int mark = (int) (Math.random() * 2);
        isPositive = mark == 1;
        if (isPositive) {
            rightAnswer = a + b;
            question = String.format("%s + %s = ?", a, b);
        } else {
            rightAnswer = a - b;
            question = String.format("%s - %s = ?", a, b);
        }
        textViewQuestion.setText(question);
        rightAnswerPosition = (int) (Math.random() * 4);
    }

    private int generateWrongAnswer() {
        int result;
        do {
            result = (int) (Math.random() * max * 2 + 1) - (max - min);
        } while (result == rightAnswer);
        return result;
    }

    private String getTime(long milliseconds) {
        int seconds = (int) (milliseconds / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void showAnswerHint(int resId) {
        imageViewAnswer.setImageResource(resId);
        imageViewAnswer.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                imageViewAnswer.setVisibility(View.INVISIBLE);
            }
        }, 1000);
    }

    public void onClickAnswer(View view) {
        if (!gameOver) {
            TextView textView = (TextView) view;
            String answer = textView.getText().toString();
            int chosenAnswer = Integer.parseInt(answer);
            if (chosenAnswer == rightAnswer) {
                countOfRightAnswers++;
                if (timer != null) {
                    timer.cancel();
                    createDownTownTimer(currentMillisLeft + 2000);
                    showAnswerHint(R.drawable.ok);
                }
            } else {
                showAnswerHint(R.drawable.wrong);
            }
            countOfQuestions++;
            playNext();
        }
    }
}
