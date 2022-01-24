-- $Id: attend_subclass_over_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop   table ATTEND_SUBCLASS_OVER_DAT
create table ATTEND_SUBCLASS_OVER_DAT ( \
    YEAR          VARCHAR(4)    NOT NULL, \
    GRADE         VARCHAR(2)    NOT NULL, \
    SCHREGNO      VARCHAR(8)    NOT NULL, \
    CLASSCD       VARCHAR(2)    NOT NULL, \
    SCHOOL_KIND   VARCHAR(2)    NOT NULL, \
    CURRICULUM_CD VARCHAR(2)    NOT NULL, \
    SUBCLASSCD    VARCHAR(6)    NOT NULL \
) IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ATTEND_SUBCLASS_OVER_DAT ADD CONSTRAINT PK_ATT_SUB_OVR_DAT PRIMARY KEY (YEAR,GRADE,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD)

