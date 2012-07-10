package com.agiledirigible.droidvalidate;

import android.content.Context;
import android.widget.TextView;

public class DVExceptions {

    public static class ValidationException extends Exception {

        protected String mErrorMsg;
        protected int mResourceId;

        public ValidationException(Context context, int descriptionResourceId) {
            super();
            mResourceId = descriptionResourceId;
            if (mResourceId > -1) {
                mErrorMsg = context.getString(descriptionResourceId);
            }
        }

        @Override
        public String getMessage() {
            return mErrorMsg;
        }
    }

    public static class TextViewValidationException extends ValidationException {

        protected String mContentDescription;

        public TextViewValidationException(TextView textView, int descriptionResourceId) {

            super(textView.getContext(), descriptionResourceId);
            mContentDescription = (String)textView.getContentDescription();
        }

        @Override
        public String getMessage() {
            String msgFormat = super.getMessage();
            return String.format(msgFormat, mContentDescription);
        }
    }

    public static class TextViewLengthException extends TextViewValidationException {

        protected int mMaxLength;
        protected int mMinLength;

        public TextViewLengthException(TextView textView,
                                       int minLength,
                                       int maxLength) {
            super(textView, -1);
            mMinLength = minLength;
            mMaxLength = maxLength;

            if (mMinLength < 0 && mMaxLength < 0) {
                mErrorMsg = "ERROR BAD VALUES FOR MIN AND MAX LENGTH";
            }else if (mMinLength == mMaxLength) {
                mResourceId = R.string.droidvalidate_error_textview_length_exact;
                mErrorMsg = textView.getContext().getString(mResourceId);
                mErrorMsg = String.format(mErrorMsg, mContentDescription, mMinLength);
            } else if (mMinLength > -1 && mMaxLength > -1) {
                mResourceId = R.string.droidvalidate_error_textview_length_between;
                mErrorMsg = textView.getContext().getString(mResourceId);
                mErrorMsg = String.format(mErrorMsg, mContentDescription, mMinLength, mMaxLength);
            } else if (mMaxLength > -1) {
                mResourceId = R.string.droidvalidate_error_textview_too_long;
                mErrorMsg = textView.getContext().getString(mResourceId);
                mErrorMsg = String.format(mErrorMsg, mContentDescription, mMaxLength);
            } else if (mMinLength > -1) {
                mResourceId = R.string.droidvalidate_error_textview_too_short;
                mErrorMsg = textView.getContext().getString(mResourceId);
                mErrorMsg = String.format(mErrorMsg, mContentDescription, mMinLength);
            }

        }

        public String getMessage() {

            return mErrorMsg;
        }
    }

    public static class TextViewDoesNotMatchException extends TextViewValidationException {

        public TextViewDoesNotMatchException(TextView textView, TextView matchView) {
            super(textView, R.string.droidvalidate_error_fields_must_match);
            mErrorMsg = String.format(mErrorMsg, mContentDescription, matchView.getContentDescription());
        }
    }
}
