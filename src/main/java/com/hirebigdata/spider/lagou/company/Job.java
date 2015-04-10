package com.hirebigdata.spider.lagou.company;

import com.mongodb.BasicDBObject;
import com.mongodb.ReflectionDBObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/3
 */
public class Job extends ReflectionDBObject {
    String job_link = "";
    String job_name = "";
    String jd = "";
    String salary = "";
    String location = "";
    String experiment = "";
    String scholar = "";
    String type = "";
    String publish_date = "";
    String crawled_time = "";

    Job(String job_link){
        this.job_link = job_link.split("\\?")[0];
        this.crawled_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    public static Job getJobFromBasicDBObject(BasicDBObject dbObject){
        Job job = new Job((String)dbObject.get("Job_link"));
        job.setCrawled_time((String)dbObject.get("Crawled_time"));
        job.setExperiment((String)dbObject.get("Experiment"));
        job.setJd((String)dbObject.get("Jd"));
        job.setJob_name((String)dbObject.get("Job_name"));
        job.setLocation((String)dbObject.get("Location"));
        job.setPublish_date((String)dbObject.get("Publish_date"));
        job.setSalary((String)dbObject.get("Salary"));
        job.setScholar((String)dbObject.get("Scholar"));
        job.setType((String)dbObject.get("Type"));
        return job;
    }

    public static void main(String[] args){
    }

    public String getJob_link() {
        return job_link;
    }

    public void setJob_link(String job_link) {
        this.job_link = job_link;
    }

    public String getJob_name() {
        return job_name;
    }

    public void setJob_name(String job_name) {
        this.job_name = job_name;
    }

    public String getJd() {
        return jd;
    }

    public void setJd(String jd) {
        this.jd = jd;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getExperiment() {
        return experiment;
    }

    public void setExperiment(String experiment) {
        this.experiment = experiment;
    }

    public String getScholar() {
        return scholar;
    }

    public void setScholar(String scholar) {
        this.scholar = scholar;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPublish_date() {
        return publish_date;
    }

    public void setPublish_date(String publish_date) {
        this.publish_date = publish_date;
    }

    public String getCrawled_time() {
        return crawled_time;
    }

    public void setCrawled_time(String crawled_time) {
        this.crawled_time = crawled_time;
    }
}
