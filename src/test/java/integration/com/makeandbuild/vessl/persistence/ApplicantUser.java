package com.makeandbuild.vessl.persistence;

import javax.persistence.Column;

import com.makeandbuild.vessl.persistence.jdbc.SaveWhen;


public class ApplicantUser extends User {
    @Column(name = "applicant_id")
    @SaveWhen(insert = true, update = true)
    private String applicantId;

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }
}