-- $Id: 7fbffe00b72b2aab5317c03e60262f18214bb765 $

drop table ATTEND_SCORE_CREDIT_MST
create table ATTEND_SCORE_CREDIT_MST ( \
    "YEAR"          VARCHAR(4)      NOT NULL, \
    "SEMESTER"      VARCHAR(1)      NOT NULL, \
    "CREDIT"        SMALLINT        NOT NULL, \
    "ATTEND_SCORE"  SMALLINT        NOT NULL, \
    "KEKKA_LOW"     SMALLINT, \
    "KEKKA_HIGH"    SMALLINT, \
    "REGISTERCD"    VARCHAR(10), \
    "UPDATED"       timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ATTEND_SCORE_CREDIT_MST add constraint PK_ATT_SCORE_CRE_M \
primary key (YEAR, SEMESTER, CREDIT, ATTEND_SCORE)
