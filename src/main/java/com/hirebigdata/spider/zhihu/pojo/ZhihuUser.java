package com.hirebigdata.spider.zhihu.pojo;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/1/9.
 */
public class ZhihuUser {
    String user_data_id;// class:zm-profile-header-op-btns clearfix tag:button  data-id 唯一标识
    String user__xsrf_value;//请求中的xsrf ????
    String name; // name/about   class:title-section ellipsis
    String url_name;//连接上的后部分
    String bio = "";// name/about   class:bio
    ArrayList<String> locations = new ArrayList<String>();// name/about 居住信息  class:zm-profile-icon zm-profile-icon-location
    String business = ""; //class:business item
    String business_topic_url = "";//class:business item
    String gender;//class:item gender
    ArrayList<String> employments = new ArrayList<String>();  // name/about 职业经历  class:zm-profile-icon zm-profile-icon-company
    ArrayList<String> educations = new ArrayList<>();   // name/about 教育经历  class:zm-profile-icon zm-profile-icon-edu
    String description = "";//class:fold-item
    String vote_count = "0";//class:zm-profile-module-desc
    String thank_count = "0";//class:zm-profile-module-desc
    String fav_count = "0";//class:zm-profile-module-desc
    String share_count = "0";//class:zm-profile-module-desc
    String weibo_url = "";//class:zm-profile-header-user-weibo
    String followee_count = "0";//  href:/people/name/followees
    String follower_count = "0";//  href:/people/name/followers
    String questions_count = "0";//  href:/people/name/asks
    String answers_count = "0";// href:/people/name/answers
    String posts_count = "0";// href:/people/name/posts
    String collections_count = "0";// href:/people/name/collections
    String logs_count = "0";// href:/people/name/logs
    String personal_page_view_count = "0";//class:zm-profile-side-section
    String follow_columns_count = "0 个专栏";//class:zm-profile-side-section
    String follow_topics_count = "0 个话题";//class:zm-profile-side-section
    String avatar = "";//第一个页面class:zm-profile-header-avatar-container

    ArrayList<Column> columns = new ArrayList<Column>();
    ArrayList<Topic> topics = new ArrayList<Topic>();
    ArrayList<Question> questions = new ArrayList<Question>();
    ArrayList<Answer> answers = new ArrayList<Answer>();
    ArrayList<String> followees= new ArrayList<String>();//填data-id
    ArrayList<String> followers = new ArrayList<String>();//填data-id
    ArrayList<Skilled_topic> skilled_topics = new ArrayList<Skilled_topic>();


    public String getUser_data_id() {
        return user_data_id;
    }

    public void setUser_data_id(String user_data_id) {
        this.user_data_id = user_data_id;
    }

    public String getUser__xsrf_value() {
        return user__xsrf_value;
    }

    public void setUser__xsrf_value(String user__xsrf_value) {
        this.user__xsrf_value = user__xsrf_value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl_name() {
        return url_name;
    }

    public void setUrl_name(String url_name) {
        this.url_name = url_name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public ArrayList<String> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<String> locations) {
        this.locations = locations;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getBusiness_topic_url() {
        return business_topic_url;
    }

    public void setBusiness_topic_url(String business_topic_url) {
        this.business_topic_url = business_topic_url;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public ArrayList<String> getEmployments() {
        return employments;
    }

    public void setEmployments(ArrayList<String> employments) {
        this.employments = employments;
    }

    public ArrayList<String> getEducations() {
        return educations;
    }

    public void setEducations(ArrayList<String> educations) {
        this.educations = educations;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVote_count() {
        return vote_count;
    }

    public void setVote_count(String vote_count) {
        this.vote_count = vote_count;
    }

    public String getThank_count() {
        return thank_count;
    }

    public void setThank_count(String thank_count) {
        this.thank_count = thank_count;
    }

    public String getFav_count() {
        return fav_count;
    }

    public void setFav_count(String fav_count) {
        this.fav_count = fav_count;
    }

    public String getShare_count() {
        return share_count;
    }

    public void setShare_count(String share_count) {
        this.share_count = share_count;
    }

    public String getWeibo_url() {
        return weibo_url;
    }

    public void setWeibo_url(String weibo_url) {
        this.weibo_url = weibo_url;
    }

    public String getFollowee_count() {
        return followee_count;
    }

    public void setFollowee_count(String followee_count) {
        this.followee_count = followee_count;
    }

    public String getFollower_count() {
        return follower_count;
    }

    public void setFollower_count(String follower_count) {
        this.follower_count = follower_count;
    }

    public String getQuestions_count() {
        return questions_count;
    }

    public void setQuestions_count(String questions_count) {
        this.questions_count = questions_count;
    }

    public String getAnswers_count() {
        return answers_count;
    }

    public void setAnswers_count(String answers_count) {
        this.answers_count = answers_count;
    }

    public String getPosts_count() {
        return posts_count;
    }

    public void setPosts_count(String posts_count) {
        this.posts_count = posts_count;
    }

    public String getCollections_count() {
        return collections_count;
    }

    public void setCollections_count(String collections_count) {
        this.collections_count = collections_count;
    }

    public String getLogs_count() {
        return logs_count;
    }

    public void setLogs_count(String logs_count) {
        this.logs_count = logs_count;
    }

    public String getPersonal_page_view_count() {
        return personal_page_view_count;
    }

    public void setPersonal_page_view_count(String personal_page_view_count) {
        this.personal_page_view_count = personal_page_view_count;
    }

    public String getFollow_columns_count() {
        return follow_columns_count;
    }

    public void setFollow_columns_count(String follow_columns_count) {
        this.follow_columns_count = follow_columns_count;
    }

    public String getFollow_topics_count() {
        return follow_topics_count;
    }

    public void setFollow_topics_count(String follow_topics_count) {
        this.follow_topics_count = follow_topics_count;
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }

    public void setColumns(ArrayList<Column> columns) {
        this.columns = columns;
    }

    public ArrayList<Topic> getTopics() {
        return topics;
    }

    public void setTopics(ArrayList<Topic> topics) {
        this.topics = topics;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(ArrayList<Question> questions) {
        this.questions = questions;
    }

    public ArrayList<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(ArrayList<Answer> answers) {
        this.answers = answers;
    }

    public ArrayList<String> getFollowees() {
        return followees;
    }

    public void setFollowees(ArrayList<String> followees) {
        this.followees = followees;
    }

    public ArrayList<String> getFollowers() {
        return followers;
    }

    public void setFollowers(ArrayList<String> followers) {
        this.followers = followers;
    }

    public ArrayList<Skilled_topic> getSkilled_topics() {
        return skilled_topics;
    }

    public void setSkilled_topics(ArrayList<Skilled_topic> skilled_topics) {
        this.skilled_topics = skilled_topics;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
