package curriculum.web.page

import xml.NodeSeq

trait Layout {
  def render(page: NodeSeq) =
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="/resources/jquery/smoothness/jquery-ui-1.8.11.custom.css"></link>
        <link rel="stylesheet" type="text/css" href="/resources/curriculum.css"></link>
        <script src="/resources/jquery/jquery-1.5.1.min.js" type="text/javascript"></script>
        {page \ "head" \ "_"}
      </head>
      <body>
        <!-- Retrieve page's body content except script -->
        {(page \ "body" \ "_").filter(_.label != "script")}
        <!-- Load js once the page is loaded to have at least something to read in the
             meanwhile, otherwise put script in the head part to bypass this beahvior -->
        <script src="/resources/jquery/jquery-ui-1.8.11.custom.min.js" type="text/javascript"></script>
        <script src="/resources/curriculum.js" type="text/javascript"></script>
        {(page \ "body" \ "_").filter(_.label == "script")}
      </body>
    </html>
}



