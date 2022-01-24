
drop table GENEVIEWMBR_KIND_DAT

create table GENEVIEWMBR_KIND_DAT( \
 YEAR           VARCHAR(4)   NOT NULL, \
 KINDCD         VARCHAR(2)   NOT NULL, \
 COURSECD       VARCHAR(1)   NOT NULL, \
 MAJORCD        VARCHAR(3)   NOT NULL, \
 GRADE          VARCHAR(2)   NOT NULL, \
 COURSECODE     VARCHAR(4)   NOT NULL, \
 A_MEMBER       SMALLINT    , \
 B_MEMBER       SMALLINT    , \
 C_MEMBER       SMALLINT    , \
 D_MEMBER       SMALLINT    , \
 E_MEMBER       SMALLINT    , \
 COURSE_MEMBER  SMALLINT    , \
 GRADE_MEMBER   SMALLINT    , \
 REGISTERCD     VARCHAR(10) , \
 UPDATED        TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table GENEVIEWMBR_KIND_DAT add constraint PK_GENEVIEWMBR_KIND_DAT primary key (YEAR,KINDCD,COURSECD,MAJORCD,GRADE,COURSECODE)

