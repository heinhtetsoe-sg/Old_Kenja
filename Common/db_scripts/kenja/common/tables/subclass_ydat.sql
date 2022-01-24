-- $Id: ee12de8297e7abfd31af6af9110959ddc1b22305 $

drop table SUBCLASS_YDAT

create table SUBCLASS_YDAT( \
       YEAR             VARCHAR(4)      NOT NULL, \
       CLASSCD          VARCHAR(2)      NOT NULL, \
       SCHOOL_KIND      VARCHAR(2)      NOT NULL, \
       CURRICULUM_CD    VARCHAR(2)      NOT NULL, \
       SUBCLASSCD       VARCHAR(6)      NOT NULL, \
       STANDARD_NO      SMALLINT, \
       REGISTERCD       VARCHAR(8), \
       UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table SUBCLASS_YDAT add constraint pk_subclass_ydat primary key \
      (YEAR, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)


