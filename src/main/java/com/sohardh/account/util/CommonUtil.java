package com.sohardh.account.util;

import static java.util.Objects.isNull;

import com.sohardh.account.dto.Statement;
import java.util.UUID;

public final class CommonUtil {

  private CommonUtil() {
  }

  public static String getInternalReference(Statement s) {
    if (isNull(s.refNo()) || s.refNo().equals("-")){
      return s.valueDate() + "-" + UUID.randomUUID();
    }
    return s.valueDate() + "-" + s.refNo();
  }
}
