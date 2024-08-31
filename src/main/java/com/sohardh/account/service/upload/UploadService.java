package com.sohardh.account.service.upload;

import com.sohardh.account.model.FireflyStatement;
import com.sohardh.account.repositories.FireflyStatementRepository;
import com.sohardh.account.util.DateUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UploadService {

  @Value("classpath:import_config.json")
  private Resource fireflyImportConfigPath;

  @Value("${auto.import.secret}")
  private static String autoImportSecret;

  @Value("${auto.import.url}")
  private String autoImportUrl;

  @Value("${auto.import.token}")
  private String autoImportToken;

  private final FireflyStatementRepository fireflyStatementRepository;

  public UploadService(FireflyStatementRepository fireflyStatementRepository) {
    this.fireflyStatementRepository = fireflyStatementRepository;
  }

  public void uploadStatements() throws IOException {
    var statements = fireflyStatementRepository.findAllNotProcessed();
    log.info("Found {} unprocessed statements", statements.size());
    var filePath = getCsvFile();
    try {
      writeToCsv(statements, filePath);
      uploadFiles(filePath.getPath()).subscribe();
      statements.forEach(statement -> {
        statement.setIsProcessed(true);
        statement.setProcessedAt(LocalDate.now());
      });
      fireflyStatementRepository.saveAll(statements);
    } finally {
      Files.deleteIfExists(filePath.toPath());
    }

  }

  private Mono<String> uploadFiles(String statementPath) throws IOException {
    var webClient = WebClient.builder()
        .baseUrl(autoImportUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + autoImportToken)
        .build();

    var bodyBuilder = new MultipartBodyBuilder();
    bodyBuilder.part("importable", new FileSystemResource(Path.of(statementPath)));
    bodyBuilder.part("json", new FileSystemResource(Path.of(fireflyImportConfigPath.getURI())));
    log.info("Uploading statements...");
    var start = Instant.now();
    return webClient.post()
        .uri(uriBuilder -> uriBuilder.queryParam("secret", autoImportSecret).build())
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .accept(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
        .retrieve()
        .bodyToMono(String.class)
        .doOnNext(s -> log.info("Statements uploaded successfully : {}, took : {}s", s,
            Duration.between(start, Instant.now()).toSeconds()));
  }

  private void writeToCsv(List<FireflyStatement> statements, File filePath) throws IOException {
    var headers = List.of("date", "description", "debit", "credit", "ref_no", "opposite_account",
        "asset_account", "tag", "category").toArray(String[]::new);
    var out = new FileWriter(filePath);
    var csvFormat = CSVFormat.DEFAULT.builder().setHeader(headers).build();
    try (var printer = new CSVPrinter(out, csvFormat)) {
      for (FireflyStatement obj : statements) {
        printer.printRecord(getRow(obj));
      }
    }
  }

  private File getCsvFile() throws IOException {
    var fileName = MessageFormat.format("temp-{0}.csv", UUID.randomUUID());
    var resourcesPath = new ClassPathResource(".").getFile().getPath();
    return Paths.get(resourcesPath, fileName).toFile();
  }

  private List<String> getRow(FireflyStatement statement) {
    var tags = statement.getTags();
    var tagsString = tags == null || tags.length == 0 ? "" : String.join(" ", List.of(tags));
    var dateString = DateUtil.convertToString(statement.getDate(), "yyyy-MM-dd");
    return List.of(dateString, statement.getDescription(), String.valueOf(statement.getDebit()),
        String.valueOf(statement.getCredit()), statement.getInternalReference(),
        statement.getOpposingAccount(),
        statement.getAssetAccount(), tagsString, statement.getCategory());
  }

}
