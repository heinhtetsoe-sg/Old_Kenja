-- $Id: 058169869b4ce3488b5c519da9af7cbf7cffa0ca $

drop table RELATIVEASSESS_MST

create table RELATIVEASSESS_MST( \
     GRADE          VARCHAR(2)   NOT NULL, \
     CLASSCD        VARCHAR(2)   NOT NULL, \
     CURRICULUM_CD  VARCHAR(2)   NOT NULL, \
     SCHOOL_KIND    VARCHAR(2)   NOT NULL, \
     SUBCLASSCD     VARCHAR(6)   NOT NULL, \
     ASSESSCD       VARCHAR(1)   NOT NULL, \
     ASSESSLEVEL    SMALLINT     NOT NULL, \
     ASSESSMARK     VARCHAR(6), \
     ASSESSLOW      DECIMAL(4,1), \
     ASSESSHIGH     DECIMAL(4,1), \
     REGISTERCD     VARCHAR(8), \
     UPDATED        TIMESTAMP DEFAULT CURRENT TIMESTAMP \
    ) in usr1dms index in idx1dms

alter table RELATIVEASSESS_MST add constraint pk_relaassess_mst primary key \
    (GRADE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, ASSESSCD, ASSESSLEVEL)
