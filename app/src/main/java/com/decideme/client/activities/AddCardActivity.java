package com.decideme.client.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.decideme.client.R;
import com.decideme.client.attributes.Utility;

import java.util.ArrayList;
import java.util.Calendar;

public class AddCardActivity extends Activity {

    private static ArrayList<String> listOfPattern;
    Integer[] imageArray = {R.drawable.visa, R.drawable.master, R.drawable.disnet, R.drawable.ae};
    private Context mContext;
    private EditText et_date, et_cvv, et_name;
    private AutoCompleteTextView autoCompleteTextView;
    private Button btn_pay;
    private String cardtype = "none";
    private String working, cardName, cardNumber, cardMonth, cardYear, cardCVV,
            cardValidity;
    private boolean isValid = true;
    private ImageView img_back;

    private TextWatcher mDateEntryWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            working = s.toString();
            isValid = true;
            if (working.length() == 2 && before == 0) {
                if (Integer.parseInt(working) < 1 || Integer.parseInt(working) > 12) {
                    isValid = false;
                } else {
                    working += "/";
                    et_date.setText(working);
                    et_date.setSelection(working.length());
                }
            } else if (working.length() == 5 && before == 0) {
                String enteredYear = working.substring(3);
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                if (Integer.parseInt("20" + enteredYear) < currentYear) {
                    isValid = false;
                }
            } else if (working.length() != 5) {
                isValid = false;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

    };

    public static ArrayList<String> listOfPattern() {
        listOfPattern = new ArrayList<String>();
        String ptVisa = "^4[0-9]$";
        listOfPattern.add(ptVisa);
        String ptMasterCard = "^5[1-5]$";
        listOfPattern.add(ptMasterCard);
        String ptDiscover = "^6(?:011|5[0-9]{2})$";
        listOfPattern.add(ptDiscover);
        String ptAmeExp = "^3[47]$";
        listOfPattern.add(ptAmeExp);
        return listOfPattern;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_add_card);

        initUI();

        btn_pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cardtype.equalsIgnoreCase("none")) {
                    if (isValid) {
                        if (et_cvv.getText().toString().length() == 3) {
                            if (!et_name.getText().toString().equalsIgnoreCase("")) {
                                saveCard();
                            } else {
                                Utility.toast("Enter a card holder name", mContext);
                            }
                        } else {
                            Utility.toast("Enter a valid CVV number", mContext);
                        }
                    } else {
                        Utility.toast("Enter a valid date: MM/YY", mContext);
                    }
                } else {
                    Utility.toast("Enter a valid card number", mContext);
                }
            }
        });

        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String ccNum = s.toString();
                if (ccNum.length() >= 2) {
                    for (int i = 0; i < listOfPattern.size(); i++) {
                        if (ccNum.substring(0, 2).matches(listOfPattern.get(i))) {
                            autoCompleteTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, imageArray[i], 0);
                            cardtype = String.valueOf(i);
                        }
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!autoCompleteTextView.getText().toString().equalsIgnoreCase("")) {
                    for (int i = 0; i < listOfPattern.size(); i++) {
                        if (autoCompleteTextView.getText().toString().matches(listOfPattern.get(i))) {
                            autoCompleteTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, imageArray[i], 0);
                            cardtype = String.valueOf(i);
                        }
                    }
                } else {
                    cardtype = "none";
                    autoCompleteTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.rect, 0);
                }
            }
        });

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void saveCard() {

        if (cardtype.equalsIgnoreCase("0")) {
            cardName = "Visa";
        } else if (cardtype.equalsIgnoreCase("1")) {
            cardName = "MasterCard";
        } else if (cardtype.equalsIgnoreCase("2")) {
            cardName = "American Express";
        } else if (cardtype.equalsIgnoreCase("3")) {
            cardName = "Discover";
        }

        String input_no = autoCompleteTextView.getText().toString();
        input_no = input_no.replace(" ", "");
        cardNumber = input_no;

        cardValidity = et_date.getText().toString();
        String[] separated = cardValidity.split("/");
        cardMonth = separated[0];
        cardYear = "20" + separated[1];
        cardCVV = et_cvv.getText().toString();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("cc_type", cardName);
        returnIntent.putExtra("cc_number", cardNumber);
        returnIntent.putExtra("cc_month", cardMonth);
        returnIntent.putExtra("cc_year", cardYear);
        returnIntent.putExtra("cc_cvv", cardCVV);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void initUI() {
        mContext = AddCardActivity.this;
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.at_card_number);
        autoCompleteTextView.addTextChangedListener(new FourDigitCardFormatWatcher());
        et_date = (EditText) findViewById(R.id.et_card_month_year);
        et_date.addTextChangedListener(mDateEntryWatcher);
        et_cvv = (EditText) findViewById(R.id.et_card_cvv);
        et_name = (EditText) findViewById(R.id.et_card_holder_name);
        btn_pay = (Button) findViewById(R.id.btn_save_card);
        img_back = (ImageView) findViewById(R.id.img_back);

        listOfPattern();
    }

    public static class FourDigitCardFormatWatcher implements TextWatcher {

        // Change this to what you want... ' ', '-' etc..
        private static final char space = ' ';

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Remove spacing char
            if (s.length() > 0 && (s.length() % 5) == 0) {
                final char c = s.charAt(s.length() - 1);
                if (space == c) {
                    s.delete(s.length() - 1, s.length());
                }
            }
            // Insert char where needed.
            if (s.length() > 0 && (s.length() % 5) == 0) {
                char c = s.charAt(s.length() - 1);
                // Only if its a digit where there should be a space we insert a space
                if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(space)).length <= 3) {
                    s.insert(s.length() - 1, String.valueOf(space));
                }
            }
        }
    }


}