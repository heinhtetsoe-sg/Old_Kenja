-- $Id: schreg_textbook_tmp.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table SCHREG_TEXTBOOK_TMP_OLD
create table SCHREG_TEXTBOOK_TMP_OLD like SCHREG_TEXTBOOK_TMP
insert into  SCHREG_TEXTBOOK_TMP_OLD select * from SCHREG_TEXTBOOK_TMP

drop   table SCHREG_TEXTBOOK_TMP
create table SCHREG_TEXTBOOK_TMP ( \
    YEAR           varchar(4) not null, \
    SCHREGNO       varchar(8) not null, \
    CLASSCD        varchar(2) not null, \
    CURRICULUM_CD  varchar(1) not null, \
    SUBCLASSCD     varchar(6) not null, \
    TEXTBOOKCD     varchar(8) not null, \
    REGISTERCD     varchar(8), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_TEXTBOOK_TMP add constraint PK_TEXTBOOK_TMP primary key (YEAR,SCHREGNO,CLASSCD,CURRICULUM_CD,SUBCLASSCD,TEXTBOOKCD)

