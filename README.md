#OLD, NOT MAINTAINED

[example_project_repo]:https://github.com/weefbellington/droidvalidate-examples

##About

DroidValidate is an annotation-based input validation library for Android.

##Code sample

```java
@DroidValidate
public class DVExampleActivity extends Activity {

    @TextViewConstraint.NotEmpty
    private EditText usernameField;

    @TextViewConstraint.NotEmpty
    @TextViewConstraint.Length(minLength = 6, maxLength = 48)
    @TextViewConstraint.Matches(R.id.confirm_password_field)
    private EditText passwordField;

    @TextViewConstraint.NotEmpty
    @TextViewConstraint.Length(minLength = 6, maxLength = 48)
    private EditText confirmPasswordField;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        usernameField = (EditText)findViewById(R.id.user_name_field);
        passwordField = (EditText)findViewById(R.id.password_field);
        confirmPasswordField = (EditText)findViewById(R.id.confirm_password_field);
    }

    public void submitButtonPressed(View v) {

        DVProcessor validationProcessor = new DVProcessor();
        List<Exception> validationExceptions = validationProcessor.validate(this);

        if (!validationExceptions.isEmpty()) {
            AlertDialog.Builder alertBuilder = validationProcessor.getValidationAlert(this, validationExceptions);
            alertBuilder.show();
        } else {
            Toast.makeText(this, getString(R.string.validation_passed), Toast.LENGTH_LONG).show();
        }
    }
}
```

##Example project


Check out the [example project repo][example_project_repo] from GitHub.
