CREATE TABLE `buch` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `Titel` varchar(255) NOT NULL,
  `Inhaltstext_Link` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `index_titel` (`Titel`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `autor` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `Name` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `index_autor` (`Name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `buch_autor` (
  `ID_Buch` int(10) unsigned NOT NULL,
  `ID_Autor` int(10) unsigned NOT NULL,
  KEY `FK_Buch_Autor_ID_Buch` (`ID_Buch`),
  KEY `FK_Buch_Autor_ID_Autor` (`ID_Autor`),
  CONSTRAINT `FK_Buch_Autor_ID_Autor` FOREIGN KEY (`ID_Autor`) REFERENCES `autor` (`ID`) ON UPDATE CASCADE,
  CONSTRAINT `FK_Buch_Autor_ID_Buch` FOREIGN KEY (`ID_Buch`) REFERENCES `buch` (`ID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `medium` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `ID_Buch` int(10) unsigned NOT NULL,
  `ISBN_13` varchar(17) NOT NULL,
  `Typ` varchar(20) NOT NULL,
  `Verlag` varchar(255) NOT NULL,
  `Verlag_Ort` varchar(255) DEFAULT NULL,
  `Erscheinungsjahr` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_Medium_ID_Buch` (`ID_Buch`),
  CONSTRAINT `FK_Medium_ID_Buch` FOREIGN KEY (`ID_Buch`) REFERENCES `buch` (`ID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `schlagwort` (
  `ID` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `Schlagwort` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `index_schlagwort` (`Schlagwort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `buch_schlagwort` (
  `ID_Buch` int(10) unsigned NOT NULL,
  `ID_Schlagwort` bigint(20) unsigned NOT NULL,
  KEY `FK_Buch_Schlagwort_ID_Buch` (`ID_Buch`),
  KEY `FK_Buch_Schlagwort_ID_Schlagwort` (`ID_Schlagwort`),
  CONSTRAINT `FK_Buch_Schlagwort_ID_Buch` FOREIGN KEY (`ID_Buch`) REFERENCES `buch` (`ID`) ON UPDATE CASCADE,
  CONSTRAINT `FK_Buch_Schlagwort_ID_Schlagwort` FOREIGN KEY (`ID_Schlagwort`) REFERENCES `schlagwort` (`ID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `sachgruppe` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `Sachgruppe` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `index_sachgruppe` (`Sachgruppe`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `buch_sachgruppe` (
  `ID_Buch` int(10) unsigned NOT NULL,
  `ID_Sachgruppe` int(10) unsigned NOT NULL,
  KEY `FK_Buch_Sachgruppe_ID_Buch` (`ID_Buch`),
  KEY `FK_Buch_Sachgruppe_ID_Sachgruppe` (`ID_Sachgruppe`),
  CONSTRAINT `FK_Buch_Sachgruppe_ID_Buch` FOREIGN KEY (`ID_Buch`) REFERENCES `buch` (`ID`) ON UPDATE CASCADE,
  CONSTRAINT `FK_Buch_Sachgruppe_ID_Sachgruppe` FOREIGN KEY (`ID_Sachgruppe`) REFERENCES `sachgruppe` (`ID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `gattung` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `Gattung` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `index_gattung` (`Gattung`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `buch_gattung` (
  `ID_Buch` int(10) unsigned NOT NULL,
  `ID_Gattung` int(10) unsigned NOT NULL,
  KEY `FK_Buch_Gattung_ID_Buch` (`ID_Buch`),
  KEY `FK_Buch_Gattung_ID_Gattung` (`ID_Gattung`),
  CONSTRAINT `FK_Buch_Gattung_ID_Buch` FOREIGN KEY (`ID_Buch`) REFERENCES `buch` (`ID`) ON UPDATE CASCADE,
  CONSTRAINT `FK_Buch_Gattung_ID_Gattung` FOREIGN KEY (`ID_Gattung`) REFERENCES `gattung` (`ID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `inhalt` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `Inhalt` varchar(255) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `index_inhalt` (`Inhalt`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `buch_inhalt` (
  `ID_Buch` int(10) unsigned NOT NULL,
  `ID_Inhalt` int(10) unsigned NOT NULL,
  KEY `FK_Buch_Inhalt_ID_Buch` (`ID_Buch`),
  KEY `FK_Buch_Inhalt_ID_Inhalt` (`ID_Inhalt`),
  CONSTRAINT `FK_Buch_Inhalt_ID_Buch` FOREIGN KEY (`ID_Buch`) REFERENCES `buch` (`ID`) ON UPDATE CASCADE,
  CONSTRAINT `FK_Buch_Inhalt_ID_Inhalt` FOREIGN KEY (`ID_Inhalt`) REFERENCES `inhalt` (`ID`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
