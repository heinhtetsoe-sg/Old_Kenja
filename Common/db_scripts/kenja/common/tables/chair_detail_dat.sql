-- $Id: c312e8894d6cc99ada5dfab09dabf7797dd34f0d $

drop table CHAIR_DETAIL_DAT
create table CHAIR_DETAIL_DAT ( \
       YEAR             VARCHAR(4)      NOT NULL, \
       SEMESTER         VARCHAR(1)      NOT NULL, \
       CHAIRCD          VARCHAR(7)      NOT NULL, \
       SEQ              VARCHAR(3)      NOT NULL, \
       REMARK1          VARCHAR(150), \
       REMARK2          VARCHAR(150), \
       REMARK3          VARCHAR(150), \
       REMARK4          VARCHAR(150), \
       REMARK5          VARCHAR(150), \
       REMARK6          VARCHAR(150), \
       REMARK7          VARCHAR(150), \
       REMARK8          VARCHAR(150), \
       REMARK9          VARCHAR(150), \
       REMARK10         VARCHAR(150), \
       REGISTERCD       VARCHAR(10), \
       UPDATED          TIMESTAMP DEFAULT CURRENT TIMESTAMP \
      ) in usr1dms index in idx1dms

alter table CHAIR_DETAIL_DAT add constraint PK_CHAIR_DETAIL \
primary key (YEAR, SEMESTER, CHAIRCD, SEQ)
