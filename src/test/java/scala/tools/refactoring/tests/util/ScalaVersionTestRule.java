package scala.tools.refactoring.tests.util;

import org.junit.Assume;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import scala.util.Properties;

public class ScalaVersionTestRule implements MethodRule {

  final class EmptyStatement extends Statement {
    @Override
    public void evaluate() throws Throwable {
      Assume.assumeTrue(false);
    }
  }

  private boolean shouldRun(ScalaVersion onlyOn, String versionString) {
    if (onlyOn == null) {
      return true;
    }
    if (!onlyOn.doesNotMatch().isEmpty() && versionString.contains(onlyOn.doesNotMatch())) {
      return false;
    } else {
      return versionString.contains(onlyOn.matches());
    }
  }

  public Statement apply(Statement stmt, FrameworkMethod meth, Object arg2) {
    ScalaVersion onlyOnMethod = meth.getAnnotation(ScalaVersion.class);
    ScalaVersion onlyOnClass = arg2 == null ? null : arg2.getClass().getAnnotation(ScalaVersion.class);
    String versionString = Properties.versionString();

    if (!shouldRun(onlyOnClass, versionString)) {
      return new EmptyStatement();
    }
    if (!shouldRun(onlyOnMethod, versionString)) {
      return new EmptyStatement();
    }
    return stmt;
  }
}
