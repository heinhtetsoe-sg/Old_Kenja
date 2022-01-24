-- $Id: b5e57fc4b4ecdab07efd560f7625dbef1f521eff $

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
    REGISTERCD         varchar(10), \
    UPDATED            timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ANOTHER_SUBCLASS_MST add constraint PK_A_SUBCLASS_MST primary key (CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD)