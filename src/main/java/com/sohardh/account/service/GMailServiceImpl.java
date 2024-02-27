package com.sohardh.account.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Value;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Label;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service("GmailServiceImpl")
public class GMailServiceImpl implements MailService {

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_LABELS);
  private static final String TOKENS_DIRECTORY_PATH = "google_tokens.json";

  private static final String CREDENTIALS_FILE_PATH = "google_client_secret.json";
  @Value("${spring.application.name}")
  private String appName;

  @Override
  public List<String> getEmailBody(String label) throws GeneralSecurityException, IOException {
    getEmails();
    return null;
  }


  /**
   * Creates an authorized Credential object.
   *
   * @param HTTP_TRANSPORT The network HTTP Transport.
   * @return An authorized Credential object.
   * @throws IOException If the credentials.json file cannot be found.
   */
  private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
    // Load client secrets.
    GoogleClientSecrets clientSecrets;

    try (var in = new ClassPathResource(CREDENTIALS_FILE_PATH).getInputStream()) {
      clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
    }

    // Build flow and trigger user authorization request.
    var flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
        SCOPES).setDataStoreFactory(
            new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH))).setAccessType("offline")
        .build();
    var receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    //returns an authorized Credential object.
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  private List<Label> getEmails() throws GeneralSecurityException, IOException {
    final var HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    var service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY,
        getCredentials(HTTP_TRANSPORT)).setApplicationName(appName).build();

    // Print the labels in the user's account.
    String user = "me";
    var listResponse = service.users().labels().list(user).execute();
    var _labels = listResponse.getLabels();
    if (_labels.isEmpty()) {
      System.out.println("No labels found.");
    } else {
      System.out.println("Labels:");
      for (Label label : _labels) {
        System.out.printf("- %s\n", label.getName());
      }
    }
    return _labels;
  }

}
