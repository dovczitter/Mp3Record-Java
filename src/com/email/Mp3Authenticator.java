package com.email;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class Mp3Authenticator extends Authenticator
{
	String user;
    String pw;

    public Mp3Authenticator (String username, String password)
    {
    	super();
    	this.user = username;
    	this.pw = password;
    }

    public PasswordAuthentication getPasswordAuthentication()
    {
       return new PasswordAuthentication(user, pw);
    }
}
