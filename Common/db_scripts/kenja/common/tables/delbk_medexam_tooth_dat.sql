-- $Id: b3ab1842e3d1491efbb7b39417312d1957564f17 $

DROP TABLE DELBK_MEDEXAM_TOOTH_DAT

CREATE TABLE DELBK_MEDEXAM_TOOTH_DAT ( \
     DEL_SEQ            SMALLINT      NOT NULL, \
     YEAR               VARCHAR(4)    NOT NULL, \
     SCHREGNO           VARCHAR(8)    NOT NULL, \
     JAWS_JOINTCD       VARCHAR(2), \
     JAWS_JOINTCD2      VARCHAR(2), \
     JAWS_JOINTCD3      VARCHAR(2), \
     PLAQUECD           VARCHAR(2), \
     GUMCD              VARCHAR(2), \
     CALCULUS           VARCHAR(2), \
     ORTHODONTICS       VARCHAR(2), \
     UP_R_BABY5         VARCHAR(2), \
     UP_R_BABY4         VARCHAR(2), \
     UP_R_BABY3         VARCHAR(2), \
     UP_R_BABY2         VARCHAR(2), \
     UP_R_BABY1         VARCHAR(2), \
     UP_L_BABY1         VARCHAR(2), \
     UP_L_BABY2         VARCHAR(2), \
     UP_L_BABY3         VARCHAR(2), \
     UP_L_BABY4         VARCHAR(2), \
     UP_L_BABY5         VARCHAR(2), \
     LW_R_BABY5         VARCHAR(2), \
     LW_R_BABY4         VARCHAR(2), \
     LW_R_BABY3         VARCHAR(2), \
     LW_R_BABY2         VARCHAR(2), \
     LW_R_BABY1         VARCHAR(2), \
     LW_L_BABY1         VARCHAR(2), \
     LW_L_BABY2         VARCHAR(2), \
     LW_L_BABY3         VARCHAR(2), \
     LW_L_BABY4         VARCHAR(2), \
     LW_L_BABY5         VARCHAR(2), \
     BABYTOOTH          SMALLINT, \
     REMAINBABYTOOTH    SMALLINT, \
     TREATEDBABYTOOTH   SMALLINT, \
     BRACK_BABYTOOTH    SMALLINT, \
     UP_R_ADULT8        VARCHAR(2), \
     UP_R_ADULT7        VARCHAR(2), \
     UP_R_ADULT6        VARCHAR(2), \
     UP_R_ADULT5        VARCHAR(2), \
     UP_R_ADULT4        VARCHAR(2), \
     UP_R_ADULT3        VARCHAR(2), \
     UP_R_ADULT2        VARCHAR(2), \
     UP_R_ADULT1        VARCHAR(2), \
     UP_L_ADULT1        VARCHAR(2), \
     UP_L_ADULT2        VARCHAR(2), \
     UP_L_ADULT3        VARCHAR(2), \
     UP_L_ADULT4        VARCHAR(2), \
     UP_L_ADULT5        VARCHAR(2), \
     UP_L_ADULT6        VARCHAR(2), \
     UP_L_ADULT7        VARCHAR(2), \
     UP_L_ADULT8        VARCHAR(2), \
     LW_R_ADULT8        VARCHAR(2), \
     LW_R_ADULT7        VARCHAR(2), \
     LW_R_ADULT6        VARCHAR(2), \
     LW_R_ADULT5        VARCHAR(2), \
     LW_R_ADULT4        VARCHAR(2), \
     LW_R_ADULT3        VARCHAR(2), \
     LW_R_ADULT2        VARCHAR(2), \
     LW_R_ADULT1        VARCHAR(2), \
     LW_L_ADULT1        VARCHAR(2), \
     LW_L_ADULT2        VARCHAR(2), \
     LW_L_ADULT3        VARCHAR(2), \
     LW_L_ADULT4        VARCHAR(2), \
     LW_L_ADULT5        VARCHAR(2), \
     LW_L_ADULT6        VARCHAR(2), \
     LW_L_ADULT7        VARCHAR(2), \
     LW_L_ADULT8        VARCHAR(2), \
     ADULTTOOTH         SMALLINT, \
     REMAINADULTTOOTH   SMALLINT, \
     TREATEDADULTTOOTH  SMALLINT, \
     LOSTADULTTOOTH     SMALLINT, \
     BRACK_ADULTTOOTH   SMALLINT, \
     CHECKADULTTOOTH    SMALLINT, \
     OTHERDISEASECD     VARCHAR(2), \
     OTHERDISEASE       VARCHAR(60), \
     DENTISTREMARKCD    VARCHAR(2), \
     DENTISTREMARK      VARCHAR(30), \
     DENTISTREMARKDATE  DATE, \
     DENTISTREMARK_CO   SMALLINT, \
     DENTISTREMARK_GO   VARCHAR(1), \
     DENTISTREMARK_G    VARCHAR(1), \
     DENTISTTREATCD     VARCHAR(2), \
     DENTISTTREAT       VARCHAR(30), \
     REGISTERCD         VARCHAR(8), \
     UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP, \
     DEL_REGISTERCD     VARCHAR(8), \
     DEL_UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    )

ALTER TABLE DELBK_MEDEXAM_TOOTH_DAT ADD CONSTRAINT PK_DLBK_MDXM_TOOTH PRIMARY KEY \
    (DEL_SEQ, YEAR,SCHREGNO)

