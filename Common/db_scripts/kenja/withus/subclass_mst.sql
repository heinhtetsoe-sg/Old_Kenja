-- $Id: subclass_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $

drop   table SUBCLASS_MST

create table SUBCLASS_MST ( \
    CLASSCD             varchar(2) not null, \
    CURRICULUM_CD       varchar(1) not null, \
    SUBCLASSCD          varchar(6) not null, \
    SUBCLASSNAME        varchar(60), \
    SUBCLASSABBV        varchar(15), \
    SUBCLASSNAME_ENG    varchar(40), \
    SUBCLASSABBV_ENG    varchar(20), \
    SUBCLASSORDERNAME1  varchar(60), \
    SUBCLASSORDERNAME2  varchar(60), \
    SUBCLASSORDERNAME3  varchar(60), \
    SHOWORDER           smallint, \
    SHOWORDER2          smallint, \
    SHOWORDER3          smallint, \
    SUBCLASSCD2         varchar(6), \
    SUBCLASSCD3         varchar(6), \
    INOUT_DIV           varchar(1), \
    REGISTERCD          varchar(8), \
    UPDATED             timestamp \
) in usr1dms index in idx1dms

alter table SUBCLASS_MST add constraint PK_SUBCLASS_MST primary key (CLASSCD, SUBCLASSCD, CURRICULUM_CD)

