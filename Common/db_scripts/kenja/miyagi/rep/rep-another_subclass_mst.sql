-- $Id: rep-another_subclass_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $


drop   table ANOTHER_SUBCLASS_MST_OLD

create table ANOTHER_SUBCLASS_MST_OLD like ANOTHER_SUBCLASS_MST

drop table ANOTHER_SUBCLASS_MST
create table ANOTHER_SUBCLASS_MST( \
    CLASSCD            varchar(2)    not null, \
    SCHOOL_KIND        varchar(2)    not null, \
    CURRICULUM_CD      varchar(2)    not null, \
    SUBCLASSCD         varchar(6)    not null, \
    SUBCLASSNAME       varchar(90), \
    SUBCLASSABBV       varchar(90), \
    SUBCLASSNAME_ENG   varchar(90), \
    SUBCLASSABBV_ENG   varchar(90), \
    SUBCLASSORDERNAME1 varchar(60), \
    SUBCLASSORDERNAME2 varchar(60), \
    SUBCLASSORDERNAME3 varchar(60), \
    SHOWORDER          smallint, \
    SHOWORDER2         smallint, \
    SHOWORDER3         smallint, \
    SUBCLASSCD2        varchar(6), \
    SUBCLASSCD3        varchar(6), \
    ELECTDIV           varchar(1), \
    VALUATION          smallint, \
    GET_CREDIT         smallint, \
    REGISTERCD         varchar(8), \
    UPDATED            timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ANOTHER_SUBCLASS_MST add constraint PK_A_SUBCLASS_MST primary key (CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)

insert into ANOTHER_SUBCLASS_MST select * from ANOTHER_SUBCLASS_MST_OLD
