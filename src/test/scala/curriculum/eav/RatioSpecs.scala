package curriculum.eav

import org.specs.Specification


class RatioSpecs extends Specification{

  "RatioType" should {
    "parse valid string" in {
      val ratioOpt = RatioType.parse("12/7")
      ratioOpt must_== Some(Ratio(12,7))
    }

    "parse valid string with negative numerator" in {
      val ratioOpt = RatioType.parse("-12/7")
      ratioOpt must_== Some(Ratio(-12,7))
    }

    "parse valid string without denominator" in {
      val ratioOpt = RatioType.parse("12")
      ratioOpt must_== Some(Ratio(12,1))
    }

    "parse valid string without denominator and negative value" in {
      val ratioOpt = RatioType.parse("-12")
      ratioOpt must_== Some(Ratio(-12,1))
    }

    "parse invalid string" in {
      val ratioOpt = RatioType.parse("/7")
      ratioOpt must_== None
    }
  }
}