package curriculum.domain

object CurriculumVitaeInstances {

  val GenreMale =
    <instance type="gender">
      <attributes>
        <attribute name="key">male</attribute>
      </attributes>
    </instance>

  val GenreFemale =
    <instance type="gender">
      <attributes>
        <attribute name="key">female</attribute>
      </attributes>
    </instance>

  val Arnauld =
    <instance type="curriculum_vitae">
      <attributes>
        {ArnauldCivility}
        {ArnauldExperiences}
        {ArnauldSkills}
        {ArnauldInterests}
      </attributes>
    </instance>

  def ArnauldCivility =
        <attribute name="civility">
          <instance type="curriculum:civility">
            <attributes>
              <attribute name="first_name">Arnauld</attribute>
              <attribute name="last_name">Loyer</attribute>
              <attribute name="email">
                <![CDATA[
                <prenom> DOT <nom> AT gmail DOT com
                ]]>
              </attribute>
              <attribute name="twitter">aloyer</attribute>
              <attribute name="blog">http://technbolts.tumblr.com/</attribute>
              <attribute name="librarything">http://www.librarything.com/catalog/Arnauld</attribute>
              <attribute name="github">https://github.com/Arnauld/Curriculum</attribute>
              <attribute name="date_of_birth">1976/12/06</attribute>
              <attribute name="gender">
                <instance-ref type="curriculum:gender">
                  <attributes>
                    <attribute name="key">male</attribute>
                  </attributes>
                </instance-ref>
              </attribute>
            </attributes>
          </instance>
        </attribute>

  def ArnauldExperiences =
        <attribute name="experiences">
          <instance type="curriculum:experience">
            <attributes>
              <attribute name="date_range">2000/12/03-2004/09/10</attribute>
              <attribute name="company">Atthis</attribute>
              <attribute name="company_website">http://www.atthis.fr/</attribute>
              <attribute name="title">Ingénieur d'étude et développement</attribute>
              <attribute name="description">
                <article>
                  <section>
                    <h1>Développement logiciel du Système d’Information Géographique (SIG) GIPS pour IBM</h1>
                    <ul>
                      <li>Prototypage de plusieurs version Java de GIPS : modélisation, développement et conduite de projet
                      (architecture 3-Tiers, <em>Java SE</em>, C++, OpenGL, IHM en Java, SGBDR, Eclipse RCP, <em>EMF</em>)</li>
                      <li>Maintenance du logiciel GIPS pour le compte d'IBM</li>
                    </ul>
                  </section>
                  <section>
                    <h1>Adaptation de GIPS aux besoins de la Brigade des Sapeurs Pompiers de Paris</h1>
                    <ul>
                      <li>Modélisation du système d'information, récupération et intégration de données</li>
                      <li>Installation et intégration de GIPS dans le centre opérationnel</li>
                    </ul>
                  </section>
                </article>
              </attribute>
            </attributes>
          </instance>
          <instance type="curriculum:experience">
            <attributes>
              <attribute name="date_range">2004/09/11-2008/01/02</attribute>
              <attribute name="company">Agelid</attribute>
              <attribute name="company_website">http://www.agelid.fr/</attribute>
              <attribute name="title">Ingénieur d'étude et développement</attribute>
              <attribute name="description">
                <!-- DTD would define content of description as #any; -->
                <article>
                  <header>
                    (Poursuite des activités de la branche SIG d'Atthis)
                  </header>
                  <section>
                    <h1>Réécriture du logiciel Logipol pour la gestion des polices municipales (<a href="http://www.logipol.fr">www.logipol.fr</a>)</h1>
                    <p>Conception et réalisation du logiciel <a href="http://www.agelid.com/sig/produits/logipol.jsp">LOGIPOL+V4</a> en Java Swing</p>
                    <ul>
                      <li>Développeur référant (équipe de 3 personnes)</li>
                      <li>Développement du générateur de code pour les couches ORM/DAO/DTO et Model avec <em>EMF</em> et JET</li>
                      <li>Développement d'un framework IOC pour une architecture en plugins</li>
                    </ul>
                  </section>
                  <section>
                    <h1>Conception et réalisation du logiciel d'études géomarketing basées sur l'analyse de grille
                    <a href="http://www.agelid.com/sig/produits/idmatic.jsp">IDmatic</a></h1>
                    <ul>
                      <li>Client <em>Eclipse RCP</em> et composant <em>SWT</em></li>
                      <li>Editeur de calcul et de génération de grille basé sur la technologie <em>GMF</em></li>
                      <li>Integration avec le composant IDsig</li>
                      <li>Calcul géographique avec <a href="http://www.vividsolutions.com/jts/jtshome.htm">JTS</a></li>
                    </ul>
                  </section>
                </article>
              </attribute>
            </attributes>
          </instance>
          <instance type="curriculum:experience">
            <attributes>
              <attribute name="date_range">2008/01/03-</attribute>
              <attribute name="company">Eptica</attribute>
              <attribute name="company_website">http://www.eptica.fr/</attribute>
              <attribute name="title">Artisan développeur, Concepteur</attribute>
              <attribute name="description">
                <article>
                  <section>
                    <h1>Developpement et évolution de la suite logicielle Eptica</h1>
                    <ul>
                      <li>Développement de nouvelles fonctionnalités</li>
                      <li>Réécriture et découpage de code monolitique en composants et modules</li>
                      <li>Technologies utilisées : <em>Java EE</em>, <em>Spring</em>, <em>Hibernate</em>, Struts 1&amp;2, Maven, Groovy, Javascript, XML/XSLT, ...</li>
                    </ul>
                  </section>
                  <section>
                    <h1>Participation à la mise en place d'une méthodologie de développement agile SCRUM</h1>
                    <ul>
                      <li>Amellioration de la couverture de tests</li>
                      <li>Mise en place de tests d'intégrations</li>
                      <li>Mise en place d'une intégration continue</li>
                    </ul>
                  </section>
                </article>
              </attribute>
            </attributes>
          </instance>
        </attribute>

  def ArnauldSkills =
        <attribute name="skills">
          <instance type="curriculum:skill">
            <attributes>
              <attribute name="title">Java</attribute>
              <attribute name="level">100/100</attribute>
              <attribute name="level_comment">SE 11ans/EE 3ans</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:skill">
            <attributes>
              <attribute name="title">[Domain|Test|Behavior] Driven Design</attribute>
              <attribute name="level">85/100</attribute>
              <attribute name="level_comment">~5ans</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:skill">
            <attributes>
              <attribute name="title">Software development</attribute>
              <attribute name="level">85/100</attribute>
              <attribute name="level_comment">~9ans</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:skill">
            <attributes>
              <attribute name="title">Frameworks, Librairies</attribute>
              <attribute name="level">70/100</attribute>
              <attribute name="comment">Hibernate, Spring, Quartz, Struts 1&amp;2, WebServices...</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:skill">
            <attributes>
              <attribute name="title">Agilité</attribute>
              <attribute name="level">65/100</attribute>
              <attribute name="level_comment">~3ans</attribute>
              <attribute name="comment">Sensibilisé ~2004, XP, Scrum, Mingle, post-it...</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:skill">
            <attributes>
              <attribute name="title">Scala</attribute>
              <attribute name="level">40/100</attribute>
              <attribute name="level_comment">~2ans</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:skill">
            <attributes>
              <attribute name="title">Eclipse RCP / EMF</attribute>
              <attribute name="level">45/100</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:skill">
            <attributes>
              <attribute name="title">Outils</attribute>
              <attribute name="level">65/100</attribute>
              <attribute name="comment">Maven, Ant, Hudson, JBehave, SQL...</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:skill">
            <attributes>
              <attribute name="title">Web</attribute>
              <attribute name="level">60/100</attribute>
              <attribute name="comment">javascript, CSS, html...</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:skill">
            <attributes>
              <attribute name="title">XML/XSLT</attribute>
              <attribute name="level">65/100</attribute>
            </attributes>
          </instance>
        </attribute>

  def ArnauldInterests =
        <attribute name="interests">
          <instance type="curriculum:interest">
            <attributes>
              <attribute name="title">Programming</attribute>
              <attribute name="level">100/100</attribute>
              <attribute name="comment">java, scala, erlang...</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:interest">
            <attributes>
              <attribute name="title">Software design and architecture</attribute>
              <attribute name="level">100/100</attribute>
              <attribute name="level_comment">~9ans</attribute>
              <attribute name="comment">Domain driven design, patterns, MDdays 2009, Devoxx2010...</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:interest">
            <attributes>
              <attribute name="title">Model driven design</attribute>
              <attribute name="level">85/100</attribute>
              <attribute name="comment">MDA, MOF, EMF</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:interest">
            <attributes>
              <attribute name="title">Scala</attribute>
              <attribute name="level">90/100</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:interest">
            <attributes>
              <attribute name="title">NoSQL</attribute>
              <attribute name="level">85/100</attribute>
              <attribute name="comment">Théorie, DHT, CouchDB, Cassandra, Voldemort</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:interest">
            <attributes>
              <attribute name="title">Agililité</attribute>
              <attribute name="level">85/100</attribute>
              <attribute name="comment">XP, Scrum</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:interest">
            <attributes>
              <attribute name="title">Veille technologique</attribute>
              <attribute name="level">85/100</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:interest">
            <attributes>
              <attribute name="title">Aquarelle</attribute>
              <attribute name="level">50/100</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:interest">
            <attributes>
              <attribute name="title">Bandes dessinées</attribute>
              <attribute name="level">90/100</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:interest">
            <attributes>
              <attribute name="title">Jeu de rôle, contes et légendes, féérie</attribute>
              <attribute name="level">80/100</attribute>
            </attributes>
          </instance>
          <instance type="curriculum:interest">
            <attributes>
              <attribute name="title">Architecture</attribute>
              <attribute name="level">80/100</attribute>
              <attribute name="comment">Toyo Ito, Tadao Ando, Zaha Hadid...</attribute>
            </attributes>
          </instance>
        </attribute>
}