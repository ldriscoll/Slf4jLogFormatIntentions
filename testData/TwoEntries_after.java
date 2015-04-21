import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ldriscoll on 4/20/15.
 */
public class Test {
  Logger logger = LoggerFactory.getLogger(Test.class);
  public void test() {
    String poop = "poop";
    String floss = "floss";
    logger.info("This {} is a {}", poop, floss, new RuntimeException());
  }
}

