package testPlugin;

import com.intellij.testFramework.fixtures.CodeInsightTestUtil;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;

public class ReplaceConcatenationWithSlf4jTest extends LightJavaCodeInsightFixtureTestCase {

  public void testTwoEntries() {
    final String testName = getTestName(false);
    final String intentionName = "Poopers";
    CodeInsightTestUtil.doIntentionTest(myFixture, intentionName, testName + ".java", testName + "_after.java");
  }

}
