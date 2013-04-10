package com.email;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.mp3record.ConfigType;

// re: http://www.java-tips.org/other-api-tips/javamail/how-to-send-an-email-with-a-file-attachment-4.html

public class Email
{
    public static void send (String appName, String pathName) throws Exception
    {
 	    Session session = Session.getInstance (getProperties(), new Mp3Authenticator(ConfigType.AuthUser.getValue(), ConfigType.AuthPwd.getValue()));
    	String filename = getFilename(pathName);
    	String newPath = getNewPath(pathName);
    	if (newPath.compareTo(pathName) != 0)
    		copyFile (pathName, newPath);
    	
		String msgText1 = "Sent from Android " +appName+ " : "+filename;
		
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(ConfigType.AuthUser.getValue()));
		Address[] address = InternetAddress.parse(ConfigType.SendTo.getValue());
		msg.addRecipients(Message.RecipientType.TO, address);
		msg.setSubject(msgText1);

		// create and fill the first message part
		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setText(msgText1);

		// create the second message part
		MimeBodyPart mbp2 = new MimeBodyPart();

		// attach the file to the message
		FileDataSource fds = new FileDataSource(newPath);
		mbp2.setDataHandler(new DataHandler(fds));
		mbp2.setFileName(fds.getName());

	     // create the Multipart and add its parts to it
		Multipart mp = new MimeMultipart();
		mp.addBodyPart(mbp1);
		mp.addBodyPart(mbp2);
		
		// add the Multipart to the message
		msg.setContent(mp);

	    // set the Date: header
		msg.setSentDate(new Date());
	      
		// send the message
		Transport.send(msg);
    }
 
    private static Properties getProperties()
    {
	    Properties properties = new Properties();
	    properties.put("mail.transport.protocol", "smtp");
	    properties.put("mail.smtp.user", ConfigType.User.getValue());
	    properties.put("mail.smtp.host", ConfigType.Host.getValue());
	    properties.put("mail.smtp.port", ConfigType.Port.getValue());
	    properties.put("mail.smtp.starttls.enable", "true");
	    properties.put("mail.smtp.auth", "true");
        //properties.put("mail.smtp.debug", "true");
	    properties.put("mail.smtp.socketFactory.port", ConfigType.Port.getValue());
	    properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
	    properties.put("mail.smtp.socketFactory.fallback", "false");
	    
	    return properties;
    }
    private static String getFilename(String pathName)
	{
		int start = pathName.lastIndexOf("/") + 1;
		String fn = pathName.substring(start);
    	String cfgFn = ConfigType.Mp3Filename.getValue();
    	if (cfgFn!=null && !cfgFn.isEmpty()) {
    		fn = cfgFn;
    	}
		return fn;
	}
    private static String getNewPath(String pathName)
	{
    	String fn = pathName;
    	String cfgFn = ConfigType.Mp3Filename.getValue();
    	if (cfgFn!=null && !cfgFn.isEmpty()) {
    		int endPos = pathName.lastIndexOf("/") + 1;
    		fn = pathName.substring(0,endPos) + cfgFn;
    	}
		return fn;
	}

    private static void copyFile (String src, String dest) throws IOException
    {
    	File sFile = new File(src);
    	if (!sFile.exists())
    		return;
    	File dFile = new File(dest);
    	if (dFile.exists())
    		dFile.delete();
    	
    	OutputStream os = new BufferedOutputStream(new FileOutputStream(dest));
    	InputStream is = new BufferedInputStream(new FileInputStream(src));
    	int numRead = 0;

    	while((numRead = is.read()) != -1)
    	{
    		os.write(numRead);
    	}
    	os.flush();
    	os.close();
    	is.close();
    }
}
