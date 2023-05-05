package math;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class BigDecimalTests {
  @Test
  void precision() {
    String val = "0.0000000000021";
    System.out.println(new BigDecimal(val).precision());
   System.out.println(new BigDecimal(val).scale());

    val = "1.0000000000021";


   System.out.println(new BigDecimal(val).precision());
   System.out.println(new BigDecimal(val).scale());
  }
}
