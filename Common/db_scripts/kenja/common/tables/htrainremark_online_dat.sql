-- kanji=漢字

drop table HTRAINREMARK_ONLINE_DAT

create table HTRAINREMARK_ONLINE_DAT ( \
 YEAR                 VARCHAR(4)     NOT NULL, \
 SCHREGNO             VARCHAR(8)     NOT NULL, \
 ANNUAL               VARCHAR(2)     NOT NULL, \
 ABSENCE_REASON       VARCHAR(1200)  , \
 DAYS                 SMALLINT , \
 PARTICIPATION_DAYS   SMALLINT , \
 METHOD               VARCHAR(1000) , \
 OTHER_LEARNING       VARCHAR(1200) , \
 REGISTERCD           VARCHAR(10)   , \
 UPDATED              TIMESTAMP      DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table HTRAINREMARK_ONLINE_DAT add constraint HTRAIN_ONLINE_D primary key (YEAR, SCHREGNO)
