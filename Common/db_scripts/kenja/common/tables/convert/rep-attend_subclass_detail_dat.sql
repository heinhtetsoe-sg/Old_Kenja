-- $Id: 0118e43757acbe8ae2a1bdae0a5537881677a199 $

drop table ATTEND_SUBCLASS_DETAIL_DAT_OLD
create table ATTEND_SUBCLASS_DETAIL_DAT_OLD like ATTEND_SUBCLASS_DETAIL_DAT
insert into  ATTEND_SUBCLASS_DETAIL_DAT_OLD select * from ATTEND_SUBCLASS_DETAIL_DAT

drop   table ATTEND_SUBCLASS_DETAIL_DAT
create table ATTEND_SUBCLASS_DETAIL_DAT ( \
        COPYCD          VARCHAR(1) NOT NULL, \
        YEAR            VARCHAR(4) NOT NULL, \
        MONTH           VARCHAR(2) NOT NULL, \
        SEMESTER        VARCHAR(1) NOT NULL, \
        SCHREGNO        VARCHAR(8) NOT NULL, \
        CLASSCD         VARCHAR(2) NOT NULL, \
        SCHOOL_KIND     VARCHAR(2) NOT NULL, \
        CURRICULUM_CD   VARCHAR(2) NOT NULL, \
        SUBCLASSCD      VARCHAR(6) NOT NULL, \
        SEQ             VARCHAR(3) NOT NULL, \
        CNT             SMALLINT, \
        VAL             VARCHAR(2), \
        REGISTERCD      VARCHAR(8), \
        UPDATED         timestamp default current timestamp \
    )  IN USR1DMS INDEX IN IDX1DMS

ALTER TABLE ATTEND_SUBCLASS_DETAIL_DAT add constraint pk_at_sub_det_dat primary key (COPYCD,YEAR,MONTH,SEMESTER,SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,SEQ)

insert into ATTEND_SUBCLASS_DETAIL_DAT \
    SELECT \
        COPYCD, \
        YEAR, \
        MONTH, \
        SEMESTER, \
        SCHREGNO, \
        CLASSCD, \
        'H' AS SCHOOL_KIND, \
        '2' AS CURRICULUM_CD, \
        SUBCLASSCD, \
        SEQ, \
        CNT, \
        VAL, \
        REGISTERCD, \
        UPDATED \
    FROM \
        ATTEND_SUBCLASS_DETAIL_DAT_OLD