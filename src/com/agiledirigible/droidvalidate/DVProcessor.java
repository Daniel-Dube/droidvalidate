package com.agiledirigible.droidvalidate;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.agiledirigible.droidvalidate.annotations.DVConstraint;
import com.agiledirigible.droidvalidate.annotations.DroidValidate;
import com.agiledirigible.droidvalidate.annotations.TextViewConstraint;
import com.agiledirigible.droidvalidate.helper.ReflectionHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.agiledirigible.droidvalidate.DVExceptions.TextViewDoesNotMatchException;
import static com.agiledirigible.droidvalidate.DVExceptions.TextViewLengthException;
import static com.agiledirigible.droidvalidate.DVExceptions.TextViewValidationException;
import static com.agiledirigible.droidvalidate.DVExceptions.ValidationException;

public class DVProcessor {

    private static final String TAG = "com.agiledirigible.droidvalidate.DroidValidator";

    protected final boolean mShouldAutoSetTextViewErrors;

    public DVProcessor() {
        this(true);
    }

    public DVProcessor(boolean shouldAutoSetTextViewErrors) {
        mShouldAutoSetTextViewErrors = shouldAutoSetTextViewErrors;
    }

    public List<Exception> validate(Object instanceToValidate, View viewToValidate) {

        ArrayList<Exception> exceptions = new ArrayList<Exception>();

        // find all fields on subclasses annotated with @DroidValidate
        List<Field> fieldsToValidate = ReflectionHelper.getInheritedFields(
                instanceToValidate.getClass(),
                DroidValidate.class);

        if (!fieldsToValidate.isEmpty()) {

            boolean breakLoop = false;
            for (Field fieldToValidate : fieldsToValidate) {

                //access private fields
                fieldToValidate.setAccessible(true);

                //check if there is a constraint on this field -- if not, break and scan the next field.
                if (!ReflectionHelper.isMetaAnnotationPresent(fieldToValidate, DVConstraint.class)) {
                    continue;
                }

                try {

                    //see if the annotation view ID matches the target view ID, if present
                    //if a match is found, we validate the field and then break the loop
                    //otherwise, continue the loop
                    View annotatedView = (View) fieldToValidate.get(instanceToValidate);
                    if (viewToValidate != null) {
                        if (annotatedView.getId() != viewToValidate.getId()) {
                            continue;
                        } else {
                            breakLoop = true;
                        }
                    }

                    // check the annotations to see if they are constraints
                    for (Annotation annotation : fieldToValidate.getDeclaredAnnotations()) {

                        // if the constraint causes a validation exception, add it to the response object
                        ValidationException e = validate(annotation, annotatedView);
                        addToListIfNotNull(exceptions, e);
                        // also set errors on TextViews if mShouldAutoSetTextViewErrors is enabled
                        callSetErrorIfAppropriate(annotatedView, e);

                    }
                } catch (IllegalAccessException e) {
                    Log.e(TAG, Log.getStackTraceString(e), e);
                } catch (InvocationTargetException e) {
                    Log.e(TAG, Log.getStackTraceString(e), e);
                }

                if (breakLoop) break;
            }

        }

        Collections.reverse(exceptions);
        return exceptions;
    }

    public List<Exception> validate(Object instanceToValidate) {
        return validate(instanceToValidate, null);
    }

    protected ValidationException validate(Annotation annotation, View viewToValidate) throws IllegalAccessException, InvocationTargetException {

        ValidationException e = null;

        DVConstraint constraint = (annotation.annotationType().getAnnotation(DVConstraint.class));
        if (constraint != null) {

            String methodName = constraint.value();
            Method validationMethod;

            try {
                validationMethod = getClass().getDeclaredMethod(methodName, new Class[]{annotation.annotationType(), TextView.class});
            } catch (NoSuchMethodException noSuchMethod) {
                throw new RuntimeException(noSuchMethod);
            }

            Object[] args = new Object[]{annotation, viewToValidate};
            e = (ValidationException) validationMethod.invoke(this, args);
        }

        return e;
    }

    protected ValidationException validateNotEmpty(TextViewConstraint.NotEmpty annotation, TextView textView) {
        CharSequence text = textView.getText();
        if (text == null || text.length() < 1) {
            return new TextViewValidationException(textView, R.string.droidvalidate_error_textview_cannot_be_empty);
        }
        return null;
    }

    protected ValidationException validateLength(TextViewConstraint.Length annotation, TextView textView) {

        ValidationException e = null;

        CharSequence text = textView.getText();
        int textLength = text.length();
        int errorStringId = -1;

        if (textLength < annotation.minLength() | textLength > annotation.maxLength()) {
            e = new TextViewLengthException(textView, annotation.minLength(), annotation.maxLength());
        }

        return e;
    }

    protected ValidationException validateMatches(TextViewConstraint.Matches annotation, TextView textView) {

        ValidationException e = null;
        View rootView = textView.getRootView();

        if (rootView != null) {
            TextView matchView = (TextView) rootView.findViewById(annotation.value());
            String matchText = matchView.getText().toString();
            String text = textView.getText().toString();
            if (!text.equals(matchText)) {
                e = new TextViewDoesNotMatchException(textView, matchView);
            }
        }

        return e;
    }

    protected <T> void addToListIfNotNull(List<T> list, T obj) {
        if (obj != null) list.add(obj);
    }

    protected void callSetErrorIfAppropriate(Object validatedField, ValidationException exception) {
        if (exception != null && mShouldAutoSetTextViewErrors && validatedField instanceof EditText) {
            EditText editText = (EditText) validatedField;
            editText.setError(exception.getLocalizedMessage());
        }
    }

    public AlertDialog.Builder getValidationAlert(Context context, List<Exception> validationExceptions) {

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setTitle(R.string.droidvalidate_default_alert_title);

        String message = context.getString(R.string.droidvalidate_default_alert_message) + "\n";

        for (Exception e : validationExceptions) {
            //add newline and bullet point
            String errorFormat = "\n\u2022 %s";
            message += String.format(errorFormat, e.getLocalizedMessage());
        }

        alertBuilder.setMessage(message);
        alertBuilder.setPositiveButton(R.string.droidvalidate_text_OK, null);

        return alertBuilder;
    }

}

