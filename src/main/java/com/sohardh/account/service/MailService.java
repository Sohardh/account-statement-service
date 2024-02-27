package com.sohardh.account.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface MailService {

  List<String> getEmailBody(String label) throws GeneralSecurityException, IOException;

}
