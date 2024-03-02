package com.sohardh.account.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;

public interface MailService {

  Optional<String> getEmailBody() throws GeneralSecurityException, IOException;

}
