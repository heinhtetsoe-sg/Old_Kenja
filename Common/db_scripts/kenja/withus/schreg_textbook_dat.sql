-- $Id: schreg_textbook_dat.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop table SCHREG_TEXTBOOK_DAT_OLD
create table SCHREG_TEXTBOOK_DAT_OLD like SCHREG_TEXTBOOK_DAT
insert into  SCHREG_TEXTBOOK_DAT_OLD select * from SCHREG_TEXTBOOK_DAT

drop   table SCHREG_TEXTBOOK_DAT
create table SCHREG_TEXTBOOK_DAT ( \
    YEAR           varchar(4) not null, \
    SCHREGNO       varchar(8) not null, \
    CLASSCD        varchar(2) not null, \
    CURRICULUM_CD  varchar(1) not null, \
    SUBCLASSCD     varchar(6) not null, \
    TEXTBOOKCD     varchar(8) not null, \
    ORDER_SEQ      int not null, \
    GET_FLG        varchar(1), \
    REGISTERCD     varchar(8), \
    UPDATED        timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table SCHREG_TEXTBOOK_DAT add constraint PK_TEXTBOOK_DAT primary key (YEAR,SCHREGNO,CLASSCD,CURRICULUM_CD,SUBCLASSCD,TEXTBOOKCD,ORDER_SEQ)

