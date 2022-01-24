-- $Id: ac7c523a46df163f87e0929b133c00dcddb393c8 $
drop table chair_dat

create table chair_dat( \
       YEAR             VARCHAR(4)      NOT NULL, \
       SEMESTER         VARCHAR(1)      NOT NULL, \
       CHAIRCD          VARCHAR(7)      NOT NULL, \
       GROUPCD          VARCHAR(4), \
       CLASSCD          VARCHAR(2), \
       SCHOOL_KIND      VARCHAR(2), \
       CURRICULUM_CD    VARCHAR(2), \
       SUBCLASSCD       VARCHAR(6), \
       CHAIRNAME        VARCHAR(30), \
       CHAIRABBV        VARCHAR(30), \
       TAKESEMES        VARCHAR(1), \
       LESSONCNT        SMALLINT CHECK(LESSONCNT <= 7), \
       FRAMECNT         SMALLINT CHECK(FRAMECNT >= 2), \
       COUNTFLG         VARCHAR(1), \
       REGISTERCD       VARCHAR(8), \
       UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table chair_dat add constraint pk_chair_dat primary key \
      (year,semester,chaircd)
