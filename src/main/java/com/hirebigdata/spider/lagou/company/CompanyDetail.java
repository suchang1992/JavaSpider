package com.hirebigdata.spider.lagou.company;

import com.hirebigdata.spider.lagou.config.MongoConfig;
import com.hirebigdata.spider.lagou.utils.Helper;
import com.hirebigdata.spider.lagou.utils.MyMongoClient;
import com.mongodb.MongoClient;
import com.mongodb.ReflectionDBObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/3
 * on average, crawl 5 positions need 1 second
 * 503,451 positions, totally need 100,000 second, which is 27 hour, which is one day.
 */
public class CompanyDetail extends ReflectionDBObject {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CompanyDetail.class);
    String url = "";
    String title = "";
    String fullname = "";
    String vali = "";
    String brief = "";
    String location = "";
    String field = "";
    String homepage = "";
    String introduction = "";
    String logo = "";
    List<String> stage = new ArrayList<>();
    List<String> size = new ArrayList<>();
    List<String> labels = new ArrayList<>();
    List<Job> jobList = new ArrayList<>();//http://www.lagou.com/gongsi/451.html  http://www.lagou.com/gongsi/6296.html
    List<Member> members = new ArrayList<>();//http://www.lagou.com/gongsi/250.html
    List<Product> products = new ArrayList<>();//http://www.lagou.com/gongsi/1575.html

    public CompanyDetail(String url){
        this.url = url;
    }

    public static void main(String[] args) throws Exception{
//        String url = "http://www.lagou.com/gongsi/1575.html";
//        String url = "http://www.lagou.com/gongsi/451.html";
//        String url = "http://www.lagou.com/gongsi/250.html";
        String url = "http://www.lagou.com/gongsi/49408.html";
//        String url = "http://www.lagou.com/gongsi/1914.html";

        CompanyDetail companyDetail = new CompanyDetail(url);
        companyDetail.begin();
    }

    public void begin(){
        String result = Helper.doGet(this.url);
        Document doc = Jsoup.parse(result);

        try{
            this.startParse(doc);
            Helper.saveToMongoDB(MyMongoClient.getMongoClient(), MongoConfig.dbName, MongoConfig.collectionLagouCompanyDetail, this);
        }catch (Exception ue){
            System.out.println(this.url);
            log.error(ue.getMessage() + " at " + this.url);
            ue.printStackTrace();
        }
    }

    public void startParse(Document doc){
        Element content_left = doc.select(".content_l").first();
        if (content_left == null){
            return;
        }
        this.processLeftInfo(content_left);

        Element content_right = doc.select(".content_r").first();
        this.processRightInfo(content_right);

        this.processMemberInfo(content_right);
        this.processProductInfo(content_left);
        this.processJobInfo(content_left);
    }

    public void processRightInfo(Element content_right){
        this.location = content_right.select(".c_tags table tbody tr").first().select("td").get(1).text();
        this.field = content_right.select(".c_tags table tbody tr").get(1).select("td").get(1).text();
        this.size.add(content_right.select(".c_tags table tbody tr").get(2).select("td").get(1).text());
        this.homepage = content_right.select(".c_tags table tbody tr").get(3).select("td").get(1).select("a").attr("href");
        this.stage.add(content_right.select(".c_stages .stageshow .c5").first().text());
    }

    public void processLeftInfo(Element content_left){
        this.title = content_left.select(".c_box h2").first().attr("title");
        this.fullname = content_left.select(".c_box h1").first().attr("title");
        this.vali = content_left.select(".c_box .va ").first().text();
        if (content_left.select(".c_box .oneword").first() != null)
            this.brief = content_left.select(".c_box .oneword").first().text();

        this.labels = Arrays.asList(content_left.select(".c_box #hasLabels").first().select("li").text().split(" "));
        if (content_left.select(".c_section .c_intro").first() != null)
            this.introduction = content_left.select(".c_section .c_intro").first().text();

        this.logo = content_left.select("#logoShow img").first().attr("src");
    }

    public void processMemberInfo(Element content_right){
        Elements members = content_right.select(".c_member .member_info");
        for (Element member : members){
            Member member1 = new Member();
            member1.portrait = member.select(".m_portrait img").first().attr("src");
            member1.name = member.select(".m_name").first().text();
            member1.weibo = member.select(".m_name a").attr("href");
            member1.position = member.select(".m_position").text();
            member1.intro = member.select(".m_intro").text();
            this.members.add(member1);
        }
    }

    public void processProductInfo(Element content_left){
        Elements products = content_left.select(".c_product");
        for (Element product : products){
            Product product1 = new Product();
            product1.name = product.select("dd .cp_intro .cp_h3_c a").first().text();
            product1.url = product.select("dd .cp_intro .cp_h3_c a").first().attr("href");
            product1.intro = product.select("dd .cp_intro .scroll-pane").first().text();
            this.products.add(product1);
        }
    }

    public void processJobInfo(Element content_left){
        Elements jobs = content_left.select("#jobList li");
        Element moreJob = content_left.select(".c_section dd .positions_more").first();
        if (moreJob != null){
            this.getMoreJobs(moreJob.attr("href"));
        }else {
            for (Element job : jobs){
                this.getJobDetail(job.select("a").attr("href"));
            }
        }
    }

    public void getJobDetail(String jobLink){
        try{
            String jobHtml = Helper.doGet(jobLink);
            Document doc = Jsoup.parse(jobHtml);
            Job job1 = new Job(jobLink);
            job1.job_name = doc.select(".content_l .job_detail .join_tc_icon h1").attr("title");
            job1.salary = doc.select(".content_l .job_detail .job_request span").get(0).text();
            job1.location = doc.select(".content_l .job_detail .job_request span").get(1).text();
            job1.experiment = doc.select(".content_l .job_detail .job_request span").get(2).text();
            job1.scholar = doc.select(".content_l .job_detail .job_request span").get(3).text();
            job1.type = doc.select(".content_l .job_detail .job_request span").get(4).text();
            job1.publish_date = doc.select("#container > div.clearfix > div.content_l > dl > dd.job_request > div").first().text();
            job1.jd = doc.select(".content_l .job_bt").text();
            this.jobList.add(job1);
        }catch (Exception e){
            log.error(e.getMessage() + " while processing " + jobLink);
            e.printStackTrace();
        }
    }

    public void getMoreJobs(String jobListLink){
        String jobListIndex = Helper.doGet(jobListLink);
        Document doc = Jsoup.parse(jobListIndex);

        Integer totalPage = (int)Math.ceil(Double.parseDouble(doc.select(".jobsTotalB i").text()) / 10.0);
        for (int i=1; i<=totalPage; i++){
            String jobListPage = Helper.doGet(jobListLink + "?pageNo=" + String.valueOf(i));
            Document jobsAtPage = Jsoup.parse(jobListPage);
            Elements jobs = jobsAtPage.select("#searchForm li");
            for (Element job : jobs){
                this.getJobDetail(job.select("a").attr("href"));
            }
        }
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getVali() {
        return vali;
    }

    public void setVali(String vali) {
        this.vali = vali;
    }

    public String getBrief() {
        return brief;
    }

    public void setBrief(String brief) {
        this.brief = brief;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getSize() {
        return size;
    }

    public void setSize(List<String> size) {
        this.size = size;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public List<String> getStage() {
        return stage;
    }

    public void setStage(List<String> stage) {
        this.stage = stage;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Job> getJobList() {
        return jobList;
    }

    public void setJobList(List<Job> jobList) {
        this.jobList = jobList;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
