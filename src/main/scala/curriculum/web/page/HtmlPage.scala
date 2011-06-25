package curriculum.web.page

import xml.NodeSeq

trait HtmlPage {

  import HtmlPage._

  def content: NodeSeq =
    <html>
      <head>
        {headContent}{stylesheets}
      </head>
      <body>
        {bodyContent}{scripts}
      </body>
    </html>

  var headContent: NodeSeq = NodeSeq.Empty
  var bodyContent: NodeSeq = NodeSeq.Empty

  def bodyContent_<<(nodeSeq:NodeSeq) {
    bodyContent = bodyContent ++ nodeSeq
  }

  var stylesheets: NodeSeq = NodeSeq.Empty
  var scripts: NodeSeq = NodeSeq.Empty

  def declareStylesheets(paths: String*) {
    stylesheets = stylesheets ++ (for(path <- paths) yield stylesheet(path))
  }

  def declareScripts(paths: String*) {
    scripts = scripts ++ (for(path <- paths) yield script(path))
  }
}

object HtmlPage {
  def stylesheet(path: String) =
      <link rel="stylesheet" type="text/css" href={path}/>

  def script(path: String) =
    <script src={path} type="text/javascript"></script>

}

