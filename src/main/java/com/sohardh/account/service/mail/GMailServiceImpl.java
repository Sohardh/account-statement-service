package com.sohardh.account.service.mail;

import static com.google.api.services.gmail.GmailScopes.GMAIL_READONLY;
import static java.text.MessageFormat.format;
import static org.springframework.util.CollectionUtils.isEmpty;

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
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service("GmailServiceImpl")
@Slf4j
public class GMailServiceImpl implements MailService {

  private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
  private static final List<String> SCOPES = List.of(GmailScopes.GMAIL_LABELS, GMAIL_READONLY);
  private static final String TOKENS_DIRECTORY_PATH = "google_tokens.json";

  private static final String CREDENTIALS_FILE_PATH = "google_client_secret.json";
  private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
  @Value("${spring.application.name}")
  private String appName;

  @Override
  public Optional<String> getEmailBody() throws GeneralSecurityException, IOException {
    return getEmail();
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
        SCOPES).setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
        .setAccessType("offline").build();
    var receiver = new LocalServerReceiver.Builder().setPort(8888).build();
    //returns an authorized Credential object.
    return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
  }

  private Optional<String> getEmail() throws GeneralSecurityException, IOException {
    final var HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    var service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY,
        getCredentials(HTTP_TRANSPORT)).setApplicationName(appName).build();

    String user = "me";
    String query = format("from:hdfcbanksmartstatement@hdfcbank.net and after:{0}",
        getFirstDateOfTheMonth());
    var response = service.users().messages().list(user)
        .setQ(query)
        .setMaxResults(5L)
        .execute();

    if (isEmpty(response.getMessages())) {
      log.info("No new statement found on date : {}", dateFormat.format(new Date()));
      return Optional.empty();
    }

    var messageIds = response.getMessages().stream().map(Message::getId).toList();

    if (messageIds.size() > 1) {
      log.info("Found more than 2 emails! Skipping parsing.");
      return Optional.empty();
    }

    log.info("Found account statement email with id : {}", messageIds.get(0));
    return getMessageBody(service, user, messageIds.get(0));
  }

  private Optional<String> getMessageBody(Gmail service, String userId, String messageId)
      throws IOException {
    log.info("Fetching email content for messageId : {}", messageId);
    Message message = service.users().messages().get(userId, messageId).execute();
    // Check if the message has parts (it could be plain text)
    if (message.getPayload().getParts() == null) {
      return Optional.ofNullable(message.getPayload().getBody().getData());
    }

    var data = new ArrayList<String>();
    var parts = new Stack<MessagePart>();
    parts.addAll(message.getPayload() != null && !isEmpty(message.getPayload().getParts())
        ? message.getPayload().getParts() : Collections.emptyList());

    while (!parts.isEmpty()) {
      var part = parts.pop();
      parts.addAll(part.getParts() != null
          ? part.getParts() : Collections.emptyList());
      if (part.getBody().getData() != null) {
        byte[] decodedData = Base64.decodeBase64(part.getBody().getData());
        data.add(new String(decodedData, StandardCharsets.UTF_8));
      }
    }

    if (data.isEmpty()) {
      log.info("Email Content not found for messageId : {}", messageId);
      return Optional.empty();
    }

    return Optional.of(String.join("\n", data));
  }

  private String getFirstDateOfTheMonth() {
    var today = LocalDate.now();

    // Set the day of the month to 1 to get the first day.
    var firstDay = today.withDayOfMonth(1);

    String format = firstDay.format(DateTimeFormatter.ofPattern("yyy-MM-dd"));
    return "2024-03-01";
  }
}
