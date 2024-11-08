package vn.edu.fpt.bmicalculator;

import java.text.DecimalFormat;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity {
    // formatter for decimal numbers
    private static final DecimalFormat decimalFormat =
            new DecimalFormat("#0.0");

    private double weight = 0.0; // weight entered by the user
    private double height = 0.0; // height entered by the user
    private boolean isMetric = true; // metric or english units

    private EditText weightEditText; // for weight input
    private EditText heightEditText; // for height input
    private TextView bmiTextView; // shows calculated BMI
    private TextView statusTextView; // shows BMI status
    private RadioGroup unitRadioGroup; // metric or english units

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get references to programmatically manipulated TextViews
        bmiTextView = (TextView) findViewById(R.id.bmiTextView);
        statusTextView = (TextView) findViewById(R.id.statusTextView);

        // get references to EditTexts
        weightEditText = (EditText) findViewById(R.id.weightEditText);
        heightEditText = (EditText) findViewById(R.id.heightEditText);

        // get reference to RadioGroup
        unitRadioGroup = (RadioGroup) findViewById(R.id.unitRadioGroup);

        // set TextWatchers for EditTexts
        weightEditText.addTextChangedListener(textWatcher);
        heightEditText.addTextChangedListener(textWatcher);

        // set RadioGroup listener
        unitRadioGroup.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        isMetric = checkedId == R.id.metricRadioButton;
                        calculateBMI(); // recalculate BMI
                    }
                }
        );
    }

    // calculate and display BMI
    private void calculateBMI() {
        double bmi = 0.0;

        // calculate BMI based on user's unit choice
        if (isMetric) {
            bmi = weight / (height * height); // metric formula
        } else {
            bmi = (weight * 703) / (height * height); // english formula
        }

        // display BMI
        bmiTextView.setText(decimalFormat.format(bmi));

        // display BMI status
        if (bmi < 18.5) {
            statusTextView.setText(R.string.underweight);
        } else if (bmi < 25) {
            statusTextView.setText(R.string.normal);
        } else if (bmi < 30) {
            statusTextView.setText(R.string.overweight);
        } else {
            statusTextView.setText(R.string.obese);
        }
    }

    // listener object for EditText's text-changed events
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start,
                                  int before, int count) {
            try {
                weight = Double.parseDouble(
                        weightEditText.getText().toString());
                height = Double.parseDouble(
                        heightEditText.getText().toString());
                calculateBMI(); // update the BMI
            } catch (NumberFormatException e) {
                bmiTextView.setText("");
                statusTextView.setText("");
            }
        }

        @Override
        public void afterTextChanged(Editable s) { }

        @Override
        public void beforeTextChanged(CharSequence s, int start,
                                      int count, int after ) { }
    };
}