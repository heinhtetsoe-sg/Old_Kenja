-- $Id: ced5627107e50dffc9ce35c2eeec4eaeb4d06fb5 $

drop table IBSUBCLASS_YDAT

create table IBSUBCLASS_YDAT( \
       IBYEAR             VARCHAR(4)      NOT NULL, \
       IBCLASSCD          VARCHAR(2)      NOT NULL, \
       IBPRG_COURSE       VARCHAR(2)      NOT NULL, \
       IBCURRICULUM_CD    VARCHAR(2)      NOT NULL, \
       IBSUBCLASSCD       VARCHAR(6)      NOT NULL, \
       IBSTANDARD_NO      SMALLINT, \
       REGISTERCD         VARCHAR(8), \
       UPDATED            TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table IBSUBCLASS_YDAT add constraint PK_IBSUBCLASS_Y primary key \
      (IBYEAR, IBCLASSCD, IBPRG_COURSE, IBCURRICULUM_CD, IBSUBCLASSCD)


