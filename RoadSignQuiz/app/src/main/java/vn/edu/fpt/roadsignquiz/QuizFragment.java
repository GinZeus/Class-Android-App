package vn.edu.fpt.roadsignquiz;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class QuizFragment extends Fragment {
    private static final String TAG = "RoadSignQuiz Activity";
    private static final int SIGNS_IN_QUIZ = 10;

    private List<String> fileNameList;
    private List<String> quizSignsList;
    private Set<String> signCategoriesSet;
    private String correctAnswer;
    private int totalGuesses;
    private int correctAnswers;
    private int guessRows;
    private SecureRandom random;
    private Handler handler;
    private Animation shakeAnimation;

    private TextView questionNumberTextView;
    private ImageView signImageView;
    private LinearLayout[] guessLinearLayouts;
    private TextView answerTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        fileNameList = new ArrayList<>();
        quizSignsList = new ArrayList<>();
        random = new SecureRandom();
        handler = new Handler();

        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3);

        questionNumberTextView = view.findViewById(R.id.questionNumberTextView);
        signImageView = view.findViewById(R.id.signImageView);
        guessLinearLayouts = new LinearLayout[3];
        guessLinearLayouts[0] = view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] = view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] = view.findViewById(R.id.row3LinearLayout);
        answerTextView = view.findViewById(R.id.answerTextView);

        for (LinearLayout row : guessLinearLayouts) {
            for (int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button) row.getChildAt(column);
                button.setOnClickListener(guessButtonListener);
            }
        }

        questionNumberTextView.setText(
                getString(R.string.question, 1, SIGNS_IN_QUIZ));
        return view;
    }

    public void updateGuessRows(SharedPreferences sharedPreferences) {
        String choices = sharedPreferences.getString(
                MainActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;

        for (LinearLayout layout : guessLinearLayouts)
            layout.setVisibility(View.GONE);

        for (int row = 0; row < guessRows; row++)
            guessLinearLayouts[row].setVisibility(View.VISIBLE);
    }

    public void updateSignCategories(SharedPreferences sharedPreferences) {
        signCategoriesSet = sharedPreferences.getStringSet(MainActivity.SIGN_CATEGORIES, null);
    }

    public void resetQuiz() {
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear();

        try {
            for (String category : signCategoriesSet) {
                String[] paths = assets.list(category);

                for (String path : paths)
                    fileNameList.add(path.replace(".png", ""));
            }
        }
        catch (IOException e) {
            Log.e(TAG, "Error loading image file names", e);
        }

        correctAnswers = 0;
        totalGuesses = 0;
        quizSignsList.clear();

        int signCounter = 1;
        int numberOfSigns = fileNameList.size();

        while (signCounter <= SIGNS_IN_QUIZ) {
            int randomIndex = random.nextInt(numberOfSigns);

            String fileName = fileNameList.get(randomIndex);

            if (!quizSignsList.contains(fileName)) {
                quizSignsList.add(fileName);
                ++signCounter;
            }
        }

        loadNextSign();
    }

    private void loadNextSign() {
        String nextImage = quizSignsList.remove(0);
        correctAnswer = nextImage;
        answerTextView.setText("");

        questionNumberTextView.setText(getString(
                R.string.question, (correctAnswers + 1), SIGNS_IN_QUIZ));

        String category = nextImage.substring(0, nextImage.indexOf('-'));

        AssetManager assets = getActivity().getAssets();

        try (InputStream stream =
                     assets.open(category + "/" + nextImage + ".png")) {
            Drawable sign = Drawable.createFromStream(stream, nextImage);
            signImageView.setImageDrawable(sign);
        }
        catch (IOException e) {
            Log.e(TAG, "Error loading " + nextImage, e);
        }

        Collections.shuffle(fileNameList);

        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        for (int row = 0; row < guessRows; row++) {
            for (int column = 0; column < guessLinearLayouts[row].getChildCount(); column++) {
                Button newGuessButton = (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                String fileName = fileNameList.get((row * 2) + column);
                newGuessButton.setText(getSignName(fileName));
            }
        }

        int row = random.nextInt(guessRows);
        int column = random.nextInt(2);
        LinearLayout randomRow = guessLinearLayouts[row];
        String signName = getSignName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(signName);
    }

    private String getSignName(String name) {
        return name.substring(name.indexOf('-') + 1).replace('_', ' ');
    }

    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button guessButton = ((Button) v);
            String guess = guessButton.getText().toString();
            String answer = getSignName(correctAnswer);
            ++totalGuesses;

            if (guess.equals(answer)) {
                ++correctAnswers;

                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(
                        getResources().getColor(R.color.correct_answer));

                disableButtons();

                if (correctAnswers == SIGNS_IN_QUIZ) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(getString(R.string.results,
                            correctAnswers,
                            totalGuesses,
                            (double) correctAnswers / totalGuesses * 100));

                    builder.setPositiveButton(R.string.reset_quiz,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    resetQuiz();
                                }
                            });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                else {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadNextSign();
                        }
                    }, 1000);
                }
            }
            else {
                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(
                        getResources().getColor(R.color.incorrect_answer));
                signImageView.startAnimation(shakeAnimation);
            }
        }
    };

    private void disableButtons() {
        for (int row = 0; row < guessRows; row++) {
            for (int column = 0; column < guessLinearLayouts[row].getChildCount(); column++) {
                Button button = (Button) guessLinearLayouts[row].getChildAt(column);
                button.setEnabled(false);
            }
        }
    }
}