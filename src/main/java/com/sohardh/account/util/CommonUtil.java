package com.sohardh.account.util;

import com.sohardh.account.dto.Statement;

public final class CommonUtil {

  private CommonUtil() {
  }

  public static String getInternalReference(Statement s) {
    return s.valueDate() + "-" + s.refNo();
  }
}
