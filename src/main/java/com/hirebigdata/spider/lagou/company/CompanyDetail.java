package com.hirebigdata.spider.lagou.company;

import com.hirebigdata.spider.lagou.config.MongoConfig;
import com.hirebigdata.spider.lagou.utils.Helper;
import com.hirebigdata.spider.lagou.utils.MyMongoClient;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.ReflectionDBObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: shellbye.com@gmail.com
 * Date: 2015/4/3
 * on average, crawl 5 positions need 1 second
 * 503,451 positions, totally need 100,000 second, which is 27 hour, which is one day.
 */
public class CompanyDetail extends ReflectionDBObject {
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("lagou");
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
    String crawled_time = "";
    List<KeyWithCrawledTime> stage = new ArrayList<>();
    List<KeyWithCrawledTime> size = new ArrayList<>();
    List<String> labels = new ArrayList<>();
    List<Job> jobList = new ArrayList<>();//http://www.lagou.com/gongsi/451.html  http://www.lagou.com/gongsi/6296.html
    List<Member> members = new ArrayList<>();//http://www.lagou.com/gongsi/250.html
    List<Product> products = new ArrayList<>();//http://www.lagou.com/gongsi/1575.html

    static String storeCollection = MongoConfig.collectionLagouCompanyDetailV3;
    boolean existedAlready = false;
    boolean newJobAllGot = false;
    Set<String> oldJobUrls = new HashSet<>();
    Set<String> oldStages = new HashSet<>();
    Set<String> oldSizes = new HashSet<>();

    public CompanyDetail(String url){
        url = url.split("\\?")[0];
        this.existedAlready = Helper.isExistInMongoDB(MyMongoClient.getMongoClient(),
                MongoConfig.dbNameLagou, CompanyDetail.storeCollection,"Url", url);
        this.url = url;
        this.crawled_time = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        if (this.existedAlready){
            BasicDBObject cmp = Helper.getDocumentFromMongo(MyMongoClient.getMongoClient(),
                    MongoConfig.dbNameLagou, CompanyDetail.storeCollection, "Url", url);
            this.set_id(cmp.get("_id"));
            BasicDBList jobs = (BasicDBList)cmp.get("JobList");
            for (int i = 0; i<jobs.size(); i++){
                this.oldJobUrls.add(((BasicDBObject) jobs.get(i)).get("Job_link").toString());
                this.jobList.add(Job.getJobFromBasicDBObject((BasicDBObject)jobs.get(i)));
            }

            BasicDBList stages = (BasicDBList)cmp.get("Stage");
            for (int i = 0; i<stages.size(); i++){
                String s = ((BasicDBObject)stages.get(i)).get("Key").toString();
                this.oldStages.add(s);
                this.stage.add(KeyWithCrawledTime
                        .getKeyWithCrawledTimeFromBasicDBObject((BasicDBObject) stages.get(i)));
            }

            BasicDBList sizes = (BasicDBList) cmp.get("Size");
            for (int i = 0; i<sizes.size(); i++){
                String s = ((BasicDBObject)sizes.get(i)).get("Key").toString();
                this.oldSizes.add(s);
                this.size.add(KeyWithCrawledTime
                        .getKeyWithCrawledTimeFromBasicDBObject((BasicDBObject)sizes.get(i)));
            }
        }
    }

    public static void main(String[] args) throws Exception{
//        String url = "http://www.lagou.com/gongsi/1575.html";
//        String url = "http://www.lagou.com/gongsi/451.html";
//        String url = "http://www.lagou.com/gongsi/250.html";
        String url = "http://www.lagou.com/c/28133.html";
//        String url = "http://www.lagou.com/gongsi/1914.html";

        CompanyDetail companyDetail = new CompanyDetail(url);
        companyDetail.begin();
    }

    public void begin(){
        log.info("start company " + this.url);
        String result = Helper.doGet(this.url);
        if (result == null || "".equals(result)){
            log.error("try max time doGet still failed at " + this.url);
        }
        Document doc = Jsoup.parse(result);

        try{
            this.startParse(doc);
            if (this.existedAlready)
                Helper.updateMongoDB(MyMongoClient.getMongoClient(), MongoConfig.dbNameLagou,
                        CompanyDetail.storeCollection, this, "Url", this.url);
            else
                Helper.saveToMongoDB(MyMongoClient.getMongoClient(), MongoConfig.dbNameLagou,
                        CompanyDetail.storeCollection, this);
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
        this.field = content_right.select(".c_tags table tbody tr").get(1).select("td").get(1).attr("title");
        String size = content_right.select(".c_tags table tbody tr").get(2).select("td").get(1).text();
        if (!this.oldSizes.contains(size))
            this.size.add(new KeyWithCrawledTime(size));
        this.homepage = content_right.select(".c_tags table tbody tr")
                .get(3).select("td").get(1).select("a").attr("href");
        String stage = content_right.select(".c_stages .stageshow .c5").first().text();
        if (!this.oldStages.contains(stage))
            this.stage.add(new KeyWithCrawledTime(stage));
    }

    public void processLeftInfo(Element content_left){
        this.title = content_left.select(".c_box h2").first().attr("title");
        this.fullname = content_left.select(".c_box h1").first().attr("title");
        this.vali = content_left.select(".c_box .va ").first().text();
        if (content_left.select(".c_box .oneword").first() != null)
            this.brief = content_left.select(".c_box .oneword").first().text();

        this.labels = Arrays.asList(content_left.select(".c_box #hasLabels")
                .first().select("li").text().split(" "));
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
            try{
                product1.name = product.select("dd .cp_intro .cp_h3_c a").first().text();
                product1.url = product.select("dd .cp_intro .cp_h3_c a").first().attr("href");
                product1.intro = product.select("dd .cp_intro .scroll-pane").first().text();
                this.products.add(product1);
            }catch (NullPointerException en){
                log.error("error when process products " + en.getMessage());
                this.products.add(product1);
            }
        }
    }

    public void processJobInfo(Element content_left){
        Elements jobs = content_left.select("#jobList li");
        Element moreJob = content_left.select(".c_section dd .positions_more").first();
        if (moreJob != null && !this.newJobAllGot){
            this.getMoreJobs(moreJob.attr("href"));
        }else {
            for (int i=0; i<jobs.size() && !this.newJobAllGot; i++){
                this.getJobDetail(jobs.get(i).select("a").attr("href"));
            }
        }
    }

    public void getJobDetail(String jobLink){
        jobLink = jobLink.split("\\?")[0];
        if (this.oldJobUrls.contains(jobLink)){
            this.newJobAllGot = true;
            return;
        }
        try{
            String jobHtml = Helper.doGet(jobLink);
            if (jobHtml == null || "".equals(jobHtml)){
                log.error("try max time doGet still failed at " + jobLink);
                return;
            }
            Document doc = Jsoup.parse(jobHtml);
            Job job1 = new Job(jobLink);
            job1.job_name = doc.select(".content_l .job_detail .join_tc_icon h1").attr("title");
            job1.salary = doc.select(".content_l .job_detail .job_request span").get(0).text();
            job1.location = doc.select(".content_l .job_detail .job_request span").get(1).text();
            job1.experiment = doc.select(".content_l .job_detail .job_request span").get(2).text();
            job1.scholar = doc.select(".content_l .job_detail .job_request span").get(3).text();
            job1.type = doc.select(".content_l .job_detail .job_request span").get(4).text();
            job1.publish_date = doc.select("#container > div.clearfix > div.content_l " +
                    "> dl > dd.job_request > div").first().text();
            job1.jd = doc.select(".content_l .job_bt").text();
            this.jobList.add(job1);
        }catch (Exception e){
            log.error(e.getMessage() + " while processing " + jobLink);
            e.printStackTrace();
        }
    }

    public void getMoreJobs(String jobListLink){
        String jobListIndex = Helper.doGet(jobListLink);
        if (jobListIndex == null){
            log.error("try max time doGet still failed at " + this.url);
        }
        Document doc = Jsoup.parse(jobListIndex);

        String pageString = doc.select(".jobsTotalB i").text();
        if ("".equals(pageString) || pageString == null){
            return;
        }
        Integer totalPage = (int)Math.ceil(Double.parseDouble(pageString) / 10.0);
        for (int i=1; i<=totalPage && !this.newJobAllGot; i++){
            String jobListPage = Helper.doGet(jobListLink + "?pageNo=" + String.valueOf(i));
            Document jobsAtPage = Jsoup.parse(jobListPage);
            Elements jobs = jobsAtPage.select("#searchForm li");
            for (int j = 0; j<jobs.size() && !this.newJobAllGot; j++){
                this.getJobDetail(jobs.get(j).select("a").attr("href"));
            }
        }
    }

    @Override
    public String toString(){
        return this.url + " " + this.existedAlready;
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

    public List<KeyWithCrawledTime> getSize() {
        return size;
    }

    public void setSize(List<KeyWithCrawledTime> size) {
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

    public List<KeyWithCrawledTime> getStage() {
        return stage;
    }

    public void setStage(List<KeyWithCrawledTime> stage) {
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
