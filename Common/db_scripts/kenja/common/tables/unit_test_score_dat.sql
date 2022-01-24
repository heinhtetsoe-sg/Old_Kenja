-- $Id: 891ba63d52cb6473d1fc0e1044bd6dcee805a4ac $

drop table UNIT_TEST_SCORE_DAT

create table UNIT_TEST_SCORE_DAT( \
     YEAR                   varchar(4)    not null, \
     SCHREGNO               varchar(8)    not null, \
     CLASSCD                varchar(2)    not null, \
     SCHOOL_KIND            varchar(2)    not null, \
     CURRICULUM_CD          varchar(2)    not null, \
     SUBCLASSCD             varchar(6)    not null, \
     SEQ                    smallint      not null, \
     VIEWCD                 varchar(4)    not null, \
     SCORE                  smallint, \
     SCORE_WEIGHT           decimal(5, 2),  \
     REGISTERCD             varchar(10), \
     UPDATED                timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table UNIT_TEST_SCORE_DAT add constraint pk_unit_test_score primary key (YEAR, SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SEQ, VIEWCD)