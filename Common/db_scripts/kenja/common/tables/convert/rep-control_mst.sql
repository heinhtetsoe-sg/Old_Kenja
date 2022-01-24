-- kanji=漢字
-- $Id: a71639b39f2b0ce9fe2c927d0144ca86f640557e $

drop table CONTROL_MST_OLD

rename table CONTROL_MST to CONTROL_MST_OLD

create table CONTROL_MST( \
   CTRL_NO           VARCHAR(2)    NOT NULL, \
   CTRL_YEAR         VARCHAR(4)    , \
   CTRL_SEMESTER     VARCHAR(1)    , \
   CTRL_DATE         DATE          , \
   ATTEND_CTRL_DATE  DATE          , \
   ATTEND_TERM       SMALLINT      , \
   IMAGEPATH         VARCHAR(60)   , \
   EXTENSION         VARCHAR(4)    , \
   MESSAGE           VARCHAR(2898) , \
   PWDVALIDTERM      SMALLINT      , \
   REGISTERCD        VARCHAR(10)    , \
   UPDATED           TIMESTAMP      DEFAULT CURRENT TIMESTAMP \
) in usr1dms index in idx1dms

alter table CONTROL_MST add constraint PK_CONTROL_MST primary key (CTRL_NO)

insert into CONTROL_MST( \
    CTRL_NO, \
    CTRL_YEAR, \
    CTRL_SEMESTER, \
    CTRL_DATE, \
    ATTEND_CTRL_DATE, \
    ATTEND_TERM, \
    IMAGEPATH, \
    EXTENSION, \
    MESSAGE, \
    PWDVALIDTERM, \
    REGISTERCD, \
    UPDATED \
 ) select  \
    CTRL_NO, \
    CTRL_YEAR, \
    CTRL_SEMESTER, \
    CTRL_DATE, \
    ATTEND_CTRL_DATE, \
    CAST(NULL AS SMALLINT) AS ATTEND_TERM, \
    IMAGEPATH, \
    EXTENSION, \
    MESSAGE, \
    PWDVALIDTERM, \
    REGISTERCD, \
    UPDATED \
 from CONTROL_MST_OLD 

