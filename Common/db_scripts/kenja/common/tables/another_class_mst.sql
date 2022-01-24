-- $Id: bd1ec728207488a039caf5737ebe2fb43168ceb7 $

drop   table ANOTHER_CLASS_MST
create table ANOTHER_CLASS_MST ( \
    CLASSCD         varchar(2) not null, \
    SCHOOL_KIND     varchar(2) not null, \
    CLASSNAME       varchar(30), \
    CLASSABBV       varchar(15), \
    CLASSNAME_ENG   varchar(40), \
    CLASSABBV_ENG   varchar(30), \
    CLASSORDERNAME1 varchar(60), \
    CLASSORDERNAME2 varchar(60), \
    CLASSORDERNAME3 varchar(60), \
    SUBCLASSES      smallint, \
    SHOWORDER       smallint, \
    SHOWORDER2      smallint, \
    SHOWORDER3      smallint, \
    SHOWORDER4      smallint, \
    ELECTDIV        varchar(1), \
    SPECIALDIV      varchar(1), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ANOTHER_CLASS_MST add constraint PK_A_CLASS_MST primary key (CLASSCD, SCHOOL_KIND)

