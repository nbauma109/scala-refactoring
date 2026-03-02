package scala.tools.refactoring.tests.implementations.extraction

import scala.tools.refactoring.tests.util.TestHelper
import scala.tools.refactoring.implementations.extraction.Extractions
import org.junit.Assert._

@scala.tools.refactoring.tests.util.ScalaVersion(matches = "2.12")
class ExtractionsTest extends TestHelper with Extractions {
  @Test
  @ScalaVersion(matches = "2.11")
  def findExtractionTargets() = {
    val s = toSelection("""
      object O{
        def fn = {
          val a = 1
          /*(*/2 * a/*)*/
        }
      }
    """)

    TestCollector.collect(s)
    assertEquals(2, TestCollector.extractionTargets.length)
  }

  @Test
  def noExtractionTargetsForSyntheticScopes() = {
    val s = toSelection("""
      object O{
        def fn =
          1 :: 2 :: /*(*/Nil/*)*/
      }
    """)

    TestCollector.collect(s)
    assertEquals(2, TestCollector.extractionTargets.length)
  }

  @Test
  def noExtractionTargetsForCasesWithSelectedPattern() = {
    val s = toSelection("""
      object O{
        1 match {
          case /*(*/i/*)*/ => i
        }
      }
    """)

    TestCollector.collect(s)
    assertEquals(1, TestCollector.extractionTargets.length)
  }

  object TestCollector extends ExtractionCollector[Extraction] {
    var extractionTargets: List[ExtractionTarget] = Nil

    def isValidExtractionSource(s: Selection) = true

    def createExtractions(source: Selection, targets: List[ExtractionTarget], name: String) = {
      extractionTargets = targets
      Nil
    }
  }
}
