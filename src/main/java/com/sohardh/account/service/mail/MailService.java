package com.sohardh.account.service.mail;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;

public interface MailService {

  List<String> getEmailBodies(LocalDate lastDate)
      throws GeneralSecurityException, IOException, InterruptedException;

}
