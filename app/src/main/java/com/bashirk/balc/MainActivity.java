package com.bashirk.balc;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bashirk.balc.R;


public class MainActivity extends Activity {

    private double weight = 0;
    private boolean male = true;
    private Double bacLevel = 0.0;
    private boolean isSaved = false;

    //UI Components
    private EditText edtTxtWeight;
    private Switch swtGender;
    private RadioGroup rdGrpDrinkSize;
    private SeekBar skBarAlcoholPercentage;
    private TextView txtViewBACLevelValue, txtViewSeekBarValue, txtViewBACStatus;
    private RadioButton rdBtnOneOz;
    private Button btnSave, btnAddDrink;
    private ProgressBar pgBarBAClevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Weight and Gender
        edtTxtWeight = (EditText) findViewById(R.id.txtWeight);
        swtGender = (Switch) findViewById(R.id.switchGender);
        swtGender.setChecked(male);

        //RadioGroup
        rdGrpDrinkSize = (RadioGroup) findViewById(R.id.rdGrpDrinkSize);
        rdBtnOneOz = (RadioButton) findViewById(R.id.rdBtnOneOz);

        //SeekBar
        skBarAlcoholPercentage = (SeekBar) findViewById(R.id.skBarAlcoholPercentage);
        skBarAlcoholPercentage.setProgress(5);
        skBarAlcoholPercentage.setMax(25);
        txtViewSeekBarValue = (TextView) findViewById(R.id.txtViewSeekBarValue);
        txtViewSeekBarValue.setText(String.valueOf(skBarAlcoholPercentage.getProgress()) + " %");
        skBarAlcoholPercentage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress = progress / 5;
                progress = progress * 5;
                seekBar.setProgress(progress);
                txtViewSeekBarValue.setText(String.valueOf(progress) + " %");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //DO Nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //DO Nothing
            }
        });

        //BAC Level
        txtViewBACLevelValue = (TextView) findViewById(R.id.txtViewBACLevelValue);
        pgBarBAClevel = (ProgressBar) findViewById(R.id.prgBarBACLevel);
        pgBarBAClevel.setMax(25);

        //Add Drink and Save
        btnSave = (Button) findViewById(R.id.btnSave);
        btnAddDrink = (Button) findViewById(R.id.btnAddDrink);

        //BAC Level Status
        txtViewBACStatus = (TextView) findViewById(R.id.txtViewStatusValue);

        //Setting BACLevel to 0.0 at startup
        bacLevel = 0.0;
        displayBACLevel();
    }

    /**
     * On Click Handle for Save Button
     *
     * @param view
     */
    public void save(View view) {

        if (bacLevel == 0.0) {
            setWeightAndGender();
        } else {
            double oldWeight = weight;
            boolean oldMale = male;

            setWeightAndGender();

            double genderConstant = 0.0;
            double oldGenderConstant = 0.0;
            if (male) {
                genderConstant = 0.68;
                oldGenderConstant = 0.55;
            } else {
                genderConstant = 0.55;
                oldGenderConstant = 0.68;
            }

            bacLevel = bacLevel * ((oldWeight * oldGenderConstant) / (weight * genderConstant));

            bacLevel = Math.round(bacLevel * 10000.0) / 10000.0;
            displayBACLevel();

            if (bacLevel > 0.25) {
                maxDrinkingCapacityReached();
            }
        }
    }

    /**
     * setting Weight and Gender value
     */
    private void setWeightAndGender() {
        weight = getWeight();

        if (weight > 0.0) {
            male = swtGender.isChecked();
            isSaved = true;
        } else {
            setError(getResources().getString(R.string.weightError));
        }
    }

    /**
     * Extracting value form textField for weight
     */
    private double getWeight() {
        Editable text = edtTxtWeight.getText();
        if (text.length() > 0) {
            return Double.parseDouble(text.toString());
        } else {
            return 0.0;
        }
    }

    /**
     * On Click Handle for Add Drink Button
     *
     * @param view
     */
    public void addDrink(View view) {
        double ounce = 0.0;
        double alcoholPercentage = 0.0;

        if (isSaved) {
            if (rdGrpDrinkSize.getCheckedRadioButtonId() == R.id.rdBtnOneOz) {
                ounce = 1.5;
            } else if (rdGrpDrinkSize.getCheckedRadioButtonId() == R.id.rdBtnFiveOz) {
                ounce = 5.0;
            } else {
                ounce = 12.0;
            }

            alcoholPercentage = skBarAlcoholPercentage.getProgress() / 100.0;

            calculateBACLevel(alcoholPercentage, ounce);

            displayBACLevel();

            if (bacLevel >= 0.25) {
                maxDrinkingCapacityReached();
            }
        } else {
            if (getWeight() > 0.0) {
                setError(getResources().getString(R.string.saveError));
            } else {
                setError(getResources().getString(R.string.weightError));
            }
        }
    }


    /**
     * Disabling Save and Add Drink Buttons when maximum BAC level is reached
     */
    private void maxDrinkingCapacityReached() {
        btnSave.setEnabled(false);
        btnAddDrink.setEnabled(false);
        setError(getResources().getString(R.string.maxCapacity));
    }

    /**
     * Error Message Toast
     */
    private void setError(String errorMessage) {
        Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    /**
     * Displays BAC Level and Status
     */
    private void displayBACLevel() {
        txtViewBACLevelValue.setText(bacLevel.toString());

        Double temp = bacLevel * 100.0;
        pgBarBAClevel.setProgress(temp.intValue());

        if (bacLevel <= 0.08) {
            txtViewBACStatus.setText(getResources().getString(R.string.safe));
            txtViewBACStatus.setBackgroundResource(R.drawable.green_background);
        } else if (bacLevel < 0.20) {
            txtViewBACStatus.setText(getResources().getString(R.string.careful));
            txtViewBACStatus.setBackgroundResource(R.drawable.orange_background);
        } else {
            txtViewBACStatus.setText(getResources().getString(R.string.overLimit));
            txtViewBACStatus.setBackgroundResource(R.drawable.red_bxackground);
        }
    }

    /**
     * Calculated BAC level according to User Input
     *
     * @param alcoholPercentage
     * @param ounce
     */
    private void calculateBACLevel(double alcoholPercentage, double ounce) {

        double genderConstant = 0.0;
        if (male) {
            genderConstant = 0.68;
        } else {
            genderConstant = 0.55;
        }

        bacLevel = bacLevel + ((ounce * alcoholPercentage * 6.24) / (weight * genderConstant));

        bacLevel = Math.round(bacLevel * 10000.0) / 10000.0;
    }

    /**
     * On Click handle for Reset button
     * All values are set to their defaults
     *
     * @param view
     */
    public void resetFields(View view) {

        //Enable buttons
        btnSave.setEnabled(true);
        btnAddDrink.setEnabled(true);

        //Default radio button
        rdBtnOneOz.setChecked(true);

        //Seek bar default value
        skBarAlcoholPercentage.setProgress(5);
        txtViewSeekBarValue.setText("5 %");

        //Gender default Value
        male = true;
        swtGender.setChecked(male);

        //Empty Weight text box
        weight = 0.0;
        edtTxtWeight.setText("");

        //BAC level is set to 0
        bacLevel = 0.0;
        displayBACLevel();

        isSaved = false;
    }
}
