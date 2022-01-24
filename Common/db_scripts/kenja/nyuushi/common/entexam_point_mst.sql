-- $Id$
drop table ENTEXAM_POINT_MST

create table ENTEXAM_POINT_MST \
( \
    ENTEXAMYEAR      varchar(4)  not null, \
    APPLICANTDIV     varchar(1)  not null, \
    TESTDIV          varchar(2)  not null, \
    POINTCD          varchar(2)  not null, \
    POINTLEVEL       smallint    not null, \
    POINTLOW         smallint    , \
    POINTHIGH        smallint    , \
    PLUS_POINT       varchar(2)  , \
    MINUS_POINT      varchar(2)  , \
    ANDOR            varchar(1)  , \
    POINTLOW2        smallint    , \
    POINTHIGH2       smallint    , \
    REGISTERCD       varchar(10) , \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_POINT_MST add constraint \
PK_ENTEXAM_POINT_M primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, POINTCD, POINTLEVEL)
