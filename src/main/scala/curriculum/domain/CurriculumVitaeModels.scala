package curriculum.domain

object CurriculumVitaeModels {

  val Models = <meta namespace="curriculum">
    <entities>
      {GenderEntity}{CivilityEntity}{ExperienceEntity}{SkillEntity}{InterestEntity}{CurriculumVitaeEntity}
    </entities>
    <instances>
      <instance type="curriculum:gender">
        <attributes>
          <attribute name="value">Male</attribute>
        </attributes>
      </instance>
      <instance type="curriculum:gender">
        <attributes>
          <attribute name="value">Female</attribute>
        </attributes>
      </instance>
    </instances>
  </meta>

  def GenderEntity =
    <entity name="gender">
      <attributes>
        <attribute name="key" type="dt:text">
          <labels>
            <label locale="fr_FR">Genre</label>
          </labels>
        </attribute>
      </attributes>
    </entity>

  def CivilityEntity =
    <entity name="civility">
      <attributes>
        <attribute name="first_name" type="dt:text">
          <labels>
            <label locale="fr_FR">Prénom</label>
          </labels>
        </attribute>
        <attribute name="last_name" type="dt:text">
          <labels>
            <label locale="fr_FR">Nom</label>
          </labels>
        </attribute>
        <attribute name="email" type="dt:text">
          <labels>
            <label locale="fr_FR">Mél</label>
          </labels>
        </attribute>
        <attribute name="twitter" type="dt:link">
          <labels>
            <label locale="fr_FR">Twitter</label>
          </labels>
        </attribute>
        <attribute name="blog" type="dt:link">
          <labels>
            <label locale="fr_FR">Blog</label>
          </labels>
        </attribute>
        <attribute name="github" type="dt:link">
          <labels>
            <label locale="fr_FR">Github</label>
          </labels>
        </attribute>
        <attribute name="librarything" type="dt:link">
          <labels>
            <label locale="fr_FR">LibraryThing</label>
          </labels>
        </attribute>
        <attribute name="date_of_birth" type="dt:date">
          <labels>
            <label locale="fr_FR">Date de naissance</label>
          </labels>
        </attribute>
        <attribute name="gender" type="curriculum:gender">
          <labels>
            <label locale="fr_FR">Genre</label>
          </labels>
        </attribute>
      </attributes>
    </entity>

  def ExperienceEntity =
    <entity name="experience">
      <attributes>
        <attribute name="date_range" type="dt:date_range">
          <labels>
            <label locale="fr_FR">Période</label>
          </labels>
        </attribute>
        <attribute name="company" type="dt:text">
          <labels>
            <label locale="fr_FR">Société</label>
          </labels>
        </attribute>
        <attribute name="company_website" type="dt:link">
          <labels>
            <label locale="fr_FR">Site de la société</label>
          </labels>
        </attribute>
        <attribute name="title" type="dt:text">
          <labels>
            <label locale="fr_FR">Résumé</label>
          </labels>
        </attribute>
        <attribute name="description" type="dt:html">
          <labels>
            <label locale="fr_FR">Description</label>
          </labels>
        </attribute>
      </attributes>
    </entity>

  def SkillEntity =
    <entity name="skill">
      <attributes>
        <attribute name="title" type="dt:text">
          <labels>
            <label locale="fr_FR">Compétence</label>
          </labels>
        </attribute>
        <attribute name="comment" type="dt:text">
          <labels>
            <label locale="fr_FR">Détails</label>
          </labels>
        </attribute>
        <attribute name="level" type="dt:ratio">
          <labels>
            <label locale="fr_FR">Niveau</label>
          </labels>
        </attribute>
        <attribute name="level_comment" type="dt:text">
          <labels>
            <label locale="fr_FR">Notes</label>
          </labels>
        </attribute>
      </attributes>
    </entity>

  def InterestEntity =
    <entity name="interest">
      <attributes>
        <attribute name="title" type="dt:text">
          <labels>
            <label locale="fr_FR">Interêt</label>
          </labels>
        </attribute>
        <attribute name="comment" type="dt:text">
          <labels>
            <label locale="fr_FR">Détails</label>
          </labels>
        </attribute>
        <attribute name="level" type="dt:ratio">
          <labels>
            <label locale="fr_FR">Niveau</label>
          </labels>
        </attribute>
        <attribute name="level_comment" type="dt:text">
          <labels>
            <label locale="fr_FR">Notes</label>
          </labels>
        </attribute>
      </attributes>
    </entity>

  def CurriculumVitaeEntity =
    <entity name="curriculum_vitae">
      <attributes>
        <attribute name="civility" type="curriculum:civility">
          <labels>
            <label locale="fr_FR">Civilité</label>
          </labels>
        </attribute>
        <attribute name="experiences" type="curriculum:experience" upper_bound="-1">
          <labels>
            <label locale="fr_FR">Experiences</label>
          </labels>
        </attribute>
        <attribute name="skills" type="curriculum:skill" upper_bound="-1">
          <labels>
            <label locale="fr_FR">Compétences</label>
          </labels>
          <descriptions type="html">
            <description locale="fr_FR">
              Les niveaux sont donnés à titre indicatif afin de mieux cerner le profil.
            </description>
          </descriptions>
        </attribute>
        <attribute name="interests" type="curriculum:interest" upper_bound="-1">
          <labels>
            <label locale="fr_FR">Centre d'interêts</label>
          </labels>
        </attribute>
      </attributes>
    </entity>
}