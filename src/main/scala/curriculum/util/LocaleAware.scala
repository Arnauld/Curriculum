package curriculum.util

import java.util.Locale

trait LocaleAware {
  def adaptTo(locale:Locale):Any
}