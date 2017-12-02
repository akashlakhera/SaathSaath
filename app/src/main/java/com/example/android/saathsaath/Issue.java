package com.example.android.saathsaath;

import android.provider.MediaStore;

/**
 * Created by aditya on 12/2/17.
 */

public class Issue {

    private String descriptionText;
    private String issueImageURL;
    private String issueVideoURL;

    public Issue(){}
    public Issue(String issueText, String imageURl, String videoUrl){
        this.issueImageURL = issueText;
        this.issueImageURL = imageURl;
        this.issueVideoURL = videoUrl;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    public String getIssueImageURL() {
        return issueImageURL;
    }

    public void setIssueImageURL(String issueImageURL) {
        this.issueImageURL = issueImageURL;
    }

    public String getIssueVideoURL() {
        return issueVideoURL;
    }

    public void setIssueVideoURL(String issueVideoURL) {
        this.issueVideoURL = issueVideoURL;
    }
}
