package com.jil.filexplorer.utils;

import android.content.Context;
import android.os.Environment;

import com.jil.filexplorer.api.FileInfo;
import com.jil.filexplorer.api.FileOperation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import static com.jil.filexplorer.utils.FileUtils.closeAnyThing;

/***
 * 升级邮件主题格式
 * 邮件主题：Filexplorer Update Message=1.0.0
 * 正文：{ "name":"filexplorer.apk", "length":10000, "description":"1.这是一个全新的版本，你没有见过的版本。\n2.apk本体使用方舟编译器编译。" }
 */
public class EmailHelper {
    private Properties properties;
    private Session session;
    private Message message;
    private MimeMultipart multipart;

    public String getUpdateMessage() {
        return bodyText.toString();
    }

    String host = "pop.qq.com";
    String username = "1521983309@qq.com";
    String password = "zqgmopnpdadkhfcf";
    private StringBuilder bodyText=new StringBuilder();

    public EmailHelper() {
        super();
        this.properties = new Properties();
    }

    public String getUpdateInfo(){
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(getUpdateMessage());
            return (String) jsonObject.get("description");
        } catch (JSONException e) {
            e.printStackTrace();
        }
       return "";
    }

    public static void sendEmail(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    EmailHelper sender = new EmailHelper();
                    //设置服务器地址和端口，网上搜的到
                    sender.setProperties("smtp.163.com", "25");
                    //分别设置发件人，邮件标题和文本内容
                    sender.setMessage("jinhao_li@163.com", "用户日志",
                            "Java Mail ！");
                    //设置收件人
                    sender.setReceiver(new String[]{"q1521983309@163.com"});
                    //添加附件，我这里注释掉，因为有人说这行报错...
                    //这个附件的路径是我手机里的啊，要发你得换成你手机里正确的路径

                    sender.addAttachment(Environment.getExternalStorageDirectory()+File.separator+"fileExplorer.log");
                    //发送邮件
                    sender.sendEmail("smtp.163.com", "jinhao_li@163.com", "1521souquan");
                } catch (AddressException e) {
                    e.printStackTrace();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setProperties(String host, String post) {
        // 地址
        this.properties.put("mail.smtp.host", host);
        // 端口号
        this.properties.put("mail.smtp.post", post);
        // 是否验证
        this.properties.put("mail.smtp.auth", true);
        this.session = Session.getInstance(properties);
        this.message = new MimeMessage(session);
        this.multipart = new MimeMultipart("mixed");
    }

    /**
     * 设置收件人
     *
     * @param receiver
     * @throws MessagingException
     */
    public void setReceiver(String[] receiver) throws MessagingException {
        Address[] address = new InternetAddress[receiver.length];
        for (int i = 0; i < receiver.length; i++) {
            address[i] = new InternetAddress(receiver[i]);
        }
        this.message.setRecipients(Message.RecipientType.TO, address);
    }

    /**
     * 设置邮件
     *
     * @param from
     *            来源
     * @param title
     *            标题
     * @param content
     *            内容
     * @throws AddressException
     * @throws MessagingException
     */
    public void setMessage(String from, String title, String content)
            throws AddressException, MessagingException {
        this.message.setFrom(new InternetAddress(from));
        this.message.setSubject(title);
        // 纯文本的话用setText()就行，不过有附件就显示不出来内容了
        MimeBodyPart textBody = new MimeBodyPart();
        textBody.setContent(content, "text/html;charset=gbk");
        this.multipart.addBodyPart(textBody);
    }

    /**
     * 添加附件
     *
     * @param filePath
     *            文件路径
     * @throws MessagingException
     */
    public void addAttachment(String filePath) throws MessagingException {
        FileDataSource fileDataSource = new FileDataSource(new File(filePath));
        DataHandler dataHandler = new DataHandler(fileDataSource);
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setDataHandler(dataHandler);
        mimeBodyPart.setFileName(fileDataSource.getName());
        this.multipart.addBodyPart(mimeBodyPart);
    }

    /**
     * 发送邮件
     *
     * @param host
     *            地址
     * @param account
     *            账户名
     * @param pwd
     *            密码
     * @throws MessagingException
     */
    public void sendEmail(String host, String account, String pwd)
            throws MessagingException {
        // 发送时间
        this.message.setSentDate(new Date());
        // 发送的内容，文本和附件
        this.message.setContent(this.multipart);
        this.message.saveChanges();
        // 创建邮件发送对象，并指定其使用SMTP协议发送邮件
        Transport transport = session.getTransport("smtp");
        // 登录邮箱
        transport.connect(host, account, pwd);

        // 发送邮件
        transport.sendMessage(message, message.getAllRecipients());
        // 关闭连接
        transport.close();
    }

    public Message[] readMailMessage() throws MessagingException {
        Store store=loginIn();
        store.connect(host, username, password);
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        Message[] message = folder.getMessages();
        LogUtils.i("邮件数量:　" , message.length+"");
        return message;
    }

    public Message findUpdateMessage() throws MessagingException {
        Message[] message = readMailMessage();
        ArrayList<Message> updateList =new ArrayList<>(Arrays.asList(message));
        for(int i=updateList.size()-1;i>0;i--){
            Message temp=updateList.get(i);
            if (temp.getSubject().startsWith("Filexplorer Update Message") && getFrom(temp).contains("1521983309")) {
                this.message=temp;
                return temp;
            }
        }
        return null;
    }

    /**
     * 获取邮件主题
     * @return
     */
    public String getSubject() {
        try {
            findUpdateMessage();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        if(this.message!=null) {
            try {
                getMailContent(message);
                return this.message.getSubject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 　　*　解析邮件，把得到的邮件内容保存到一个StringBuffer对象中，解析邮件
     * 　　*　主要是根据MimeType类型的不同执行不同的操作，一步一步的解析
     */

    public void getMailContent(Part part) throws Exception {
        String contentType = part.getContentType();
        // 获得邮件的MimeType类型
        System.out.println("邮件的MimeType类型: " + contentType);
        int nameIndex = contentType.indexOf("name");
        boolean conName = false;
        if (nameIndex != -1) {
            conName = true;
        }
        System.out.println("邮件内容的类型:　" + contentType);
        if (part.isMimeType("text/plain") && conName == false) {
            // text/plain 类型
            bodyText.append((String) part.getContent());
        } else if (part.isMimeType("text/html") && conName == false) {
            // text/html 类型
            bodyText.append((String) part.getContent());
        } else if (part.isMimeType("multipart/*")) {
            // multipart/*
            Multipart multipart = (Multipart) part.getContent();
            int counts = multipart.getCount();
            for (int i = 0; i < counts; i++) {
                getMailContent(multipart.getBodyPart(i));
            }
        } else if (part.isMimeType("message/rfc822")) {
            // message/rfc822
            getMailContent((Part) part.getContent());
        } else {

        }
    }

    /**
     * 获取升级文件大小
     * @return
     * @throws IOException
     * @throws MessagingException
     * @throws JSONException
     */
    public Object getUpdateLength() throws IOException, MessagingException, JSONException {
        JSONObject jsonObject =new JSONObject(getUpdateMessage());
        return jsonObject.get("length");
    }




    /**
     * 保存附件
     * @throws Exception
     */
    public File saveAttachMent(Context context) throws Exception {
        File dowloadDir= new File(Environment.getExternalStorageDirectory()+File.separator+"Download");
        FileInfo todir =FileUtils.getFileInfoFromFile(dowloadDir);
        String fileName;
        File updateApk=null;
        if (message.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) message.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart mPart = mp.getBodyPart(i);
                String disposition = mPart.getDisposition();
                if ((disposition != null)
                        && ((disposition.equals(Part.ATTACHMENT)) || (disposition
                        .equals(Part.INLINE)))) {
                    fileName = mPart.getFileName();
                    if (fileName.toLowerCase().contains("gb2312")) {
                        fileName = MimeUtility.decodeText(fileName);
                    }
                    updateApk=saveFile(fileName, mPart.getInputStream(),context,todir);
                } else if (mPart.isMimeType("multipart/*")) {
                    saveAttachMent(context);
                } else {
                    fileName = mPart.getFileName();
                    if ((fileName != null)
                            && (fileName.toLowerCase().contains("GB2312"))) {
                        fileName = MimeUtility.decodeText(fileName);
                        updateApk=saveFile(fileName, mPart.getInputStream(),context,todir);
                    }
                }
            }
        } else if (message.isMimeType("message/rfc822")) {
            saveAttachMent(context);
        }
        return updateApk;
    }

    /**
     * 真正的保存附件到指定目录里
     */
    private File saveFile(String fileName,InputStream inputStream,Context context,FileInfo to) throws JSONException, MessagingException, IOException {
        ArrayList<InputStream> inLists =new ArrayList<>(Collections.singleton(inputStream));
        FileInfo in =new FileInfo();
        in.setFileName(fileName);
        ArrayList<FileInfo> fi =new ArrayList<>(Arrays.asList(in));
        FileOperation fileOperation=FileOperation.with(context).download(inLists,fi,2076672).to(to);
        fileOperation.run();
        return new File(to.getFilePath()+File.separator+fileName);
    }

    /**
     * 真正的保存附件到指定目录里
     */
    private static File saveFile(InputStream inputStream){
        String fileName="名字";
        File dowloadDir= new File(Environment.getExternalStorageDirectory()+File.separator+"Download");
        if(!dowloadDir.exists()) dowloadDir.mkdirs();
        File storeFile = new File(dowloadDir , fileName);
        FileOutputStream fis = null;
        int sum=0;
        try {
            fis = new FileOutputStream(storeFile);
            byte[] b =new byte[1024];
            int len;
            while ((len = inputStream.read(b)) != -1) {
                fis.write(b,0,len);
                fis.flush();
                sum+=len;
            }
            System.out.println("数据大小"+sum);
        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            closeAnyThing(fis, inputStream);
        }

        return storeFile;
    }

    public Store loginIn() throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        Store store = session.getStore("pop3");
        return store;
    }

    /**
     * *　获得发件人的地址和姓名 　
     * */
    public static String getFrom(Message mimeMessage) {
        InternetAddress address[] = new InternetAddress[0];
        try {
            address = (InternetAddress[]) mimeMessage.getFrom();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        String from = address[0].getAddress();
        if (from == null) {
            from = "";
        }
        return from;
    }

}
