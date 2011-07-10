package curriculum.cluster.page

import curriculum.web.page.HtmlPage
import org.slf4j.LoggerFactory
class ClusterAdminPage() extends HtmlPage {

  val log = LoggerFactory.getLogger(classOf[ClusterAdminPage])

  declareStylesheets("/resources/ui-progress-bar/stylesheets/ui.progress-bar.css")
  declareScripts("/resources/ui-progress-bar/javascripts/progress.js")
  declareStylesheets("/resources/bubble/css/style.css")
  declareScripts("/resources/bubble/jquery.easing.1.3.js")
  declareStylesheets("/resources/cluster.css")
  declareScripts("/resources/cluster.js")

  bodyContent =
    <div id="content">
      <a class="back" href="/search"></a>
      <div class="title"></div>

      <div class="navigation" id="nav">
        <div class="item user">
            <img src="/resources/bubble/images/bg_user.png" alt=" " width="199" height="199" class="circle"/>
          <a href="#" class="icon"></a>
          <h2>Cluster</h2>
          <ul>
            <li>
              <a href="#" id="start-node">Start node</a>
            </li>
            <li>
              <a href="#" id="list-nodes">List nodes</a>
            </li>
          </ul>
        </div>
        <div class="item home">
            <img src="/resources/bubble/images/bg_home.png" alt=" " width="199" height="199" class="circle"/>
          <a href="#" class="icon"></a>
          <h2>Home</h2>
          <ul>
            <li>
              <a href="#">Portfolio</a>
            </li>
            <li>
              <a href="#">Services</a>
            </li>
            <li>
              <a href="#">Contact</a>
            </li>
          </ul>
        </div>
        <div class="item shop">
            <img src="/resources/bubble/images/bg_shop.png" alt=" " width="199" height="199" class="circle"/>
          <a href="#" class="icon"></a>
          <h2>Shop</h2>
          <ul>
            <li>
              <a href="#" id="search">Search</a>
            </li>
          </ul>
        </div>
        <!-- div class="item camera">
            <img src="/resources/bubble/images/bg_camera.png" alt=" " width="199" height="199" class="circle"/>
          <a href="#" class="icon"></a>
          <h2>Photos</h2>
          <ul>
            <li>
              <a href="#">Gallery</a>
            </li>
            <li>
              <a href="#">Prints</a>
            </li>
            <li>
              <a href="#">Submit</a>
            </li>
          </ul>
        </div-->
        <div class="item fav">
            <img src="/resources/bubble/images/bg_fav.png" alt=" " width="199" height="199" class="circle"/>
          <a href="#" class="icon"></a>
          <h2>Love</h2>
          <ul>
            <li>
              <a href="https://github.com/Arnauld/Curriculum" target="_blank">Clone me on github</a>
            </li>
          </ul>
        </div>
      </div>
      <section id="logs">
        <!--div class="message">
          <div class="type type-warning">&nbsp;</div>
          <div class="content">
            <b>Aucun noeud disponible</b>. En tant qu'administrateur vous devriez en allumer un!
          </div>
        </div>
        <div class="message">
          <div class="type type-sucess">&nbsp;</div>
          <div class="content">
            <b>Le noeud &lt;Dude&gt; est démarré sur le port 9000</b>.
          </div>
        </div>
        <div class="message">
          <div class="type type-search-started">&nbsp;</div>
          <div class="content">
            <b>Recherche en cours</b>.
          </div>
        </div>
        <div class="message">
          <div class="type type-search-done">&nbsp;</div>
          <div class="content">
            <b>Recherche effectuée: 1 résultat</b>.
          </div>
        </div-->
      </section>
      <section id="display">
      </section>
    </div>

  val old =
    <div id="clusters">
      <form method="post" action="/cluster/start">
        <fieldset id="start-node">
          <legend>Start a new node</legend>
          <div class="entry name">
            <label for="start-node-name" class="start-node-name">
              Node name
            </label>
              <input name="start-node-name"
                     id="start-node-name"
                     type="text"
                     class="start-node-name" value=" " autofocus="autofocus"/>
          </div>
          <div class="entry port">
            <label for="start-node-port" class="start-node-port">
              Port
            </label>
              <input name="start-node-port"
                     type="number"
                     min="9000" max="9080"
                     value="9000"
                     class="start-node-name"/>
          </div>
          <div class="entry buttons">
            <label for="start-node-port" class="start-node-port">
              &nbsp;
            </label>
              <input type="submit" value="Go!"/>
          </div>
        </fieldset>
      </form>
    </div>

}
