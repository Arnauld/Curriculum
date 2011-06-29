package curriculum.eav

import org.specs.Specification
import org.joda.time.LocalDate

class LocalDateRangeSpecs extends Specification {

  val refDate1 = new LocalDate(2011, 1, 19)
  val refDate2 = new LocalDate(1664, 2, 13)

  "LocalDateRangeType" should {

    "parse valid string" in {
      val drOpt = LocalDateRangeType.parse("2011/01/19-1664/02/13")
      drOpt must_== Some(LocalDateRange(Some(refDate1), Some(refDate2)))
    }

    "parse valid string without max" in {
      val drOpt = LocalDateRangeType.parse("2011/01/19-")
      drOpt must_== Some(LocalDateRange(Some(refDate1), None))
    }

    "parse valid string without min" in {
      val drOpt = LocalDateRangeType.parse("-2011/01/19-")
      drOpt must_== Some(LocalDateRange(None, Some(refDate1)))
    }

  }
}