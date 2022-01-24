-- $Id: f536189b2fb4ecc7a4b6c336084e1efe12f74a40 $

drop table TMP_RECORD_DAT

create table TMP_RECORD_DAT  \
(  \
        "YEAR"                  varchar(4)      not null, \
        "SUBCLASSCD"            varchar(6)      not null, \
        "TAKESEMES"             varchar(1)      not null, \
        "SCHREGNO"              varchar(8)      not null, \
        "CHAIRCD"               varchar(7),  \
        "SEM1_INTR_CHAIRCD"     varchar(7),  \
        "SEM1_TERM_CHAIRCD"     varchar(7),  \
        "SEM1_TERM2_CHAIRCD"    varchar(7),  \
        "SEM2_INTR_CHAIRCD"     varchar(7),  \
        "SEM2_TERM_CHAIRCD"     varchar(7),  \
        "SEM2_TERM2_CHAIRCD"    varchar(7),  \
        "SEM3_INTR_CHAIRCD"     varchar(7),  \
        "SEM3_TERM_CHAIRCD"     varchar(7),  \
        "SEM1_INTR_SCORE"       smallint,  \
        "SEM1_TERM_SCORE"       smallint,  \
        "SEM1_TERM2_SCORE"      smallint,  \
        "SEM2_INTR_SCORE"       smallint,  \
        "SEM2_TERM_SCORE"       smallint,  \
        "SEM2_TERM2_SCORE"      smallint,  \
        "SEM3_INTR_SCORE"       smallint,  \
        "SEM3_TERM_SCORE"       smallint,  \
        "SEM1_INTR_VALUE"       smallint,  \
        "SEM1_TERM_VALUE"       smallint,  \
        "SEM1_TERM2_VALUE"      smallint,  \
        "SEM1_VALUE"            smallint,  \
        "SEM2_INTR_VALUE"       smallint,  \
        "SEM2_TERM_VALUE"       smallint,  \
        "SEM2_TERM2_VALUE"      smallint,  \
        "SEM2_VALUE"            smallint,  \
        "SEM3_INTR_VALUE"       smallint,  \
        "SEM3_TERM_VALUE"       smallint,  \
        "SEM3_VALUE"            smallint,  \
        "GRAD_VALUE"            smallint,  \
        "GRAD_VALUE2"           smallint,  \
        "GET_CREDIT"            smallint,  \
        "ADD_CREDIT"            smallint,  \
        "COMP_TAKESEMES"        varchar(1),  \
        "COMP_CREDIT"           smallint,  \
        "SEM1_INTR_SCORE_DI"    varchar(2),  \
        "SEM1_TERM_SCORE_DI"    varchar(2),  \
        "SEM2_INTR_SCORE_DI"    varchar(2),  \
        "SEM2_TERM_SCORE_DI"    varchar(2),  \
        "SEM3_INTR_SCORE_DI"    varchar(2),  \
        "SEM3_TERM_SCORE_DI"    varchar(2),  \
        "SEM1_INTR_VALUE_DI"    varchar(2),  \
        "SEM1_TERM_VALUE_DI"    varchar(2),  \
        "SEM1_TERM2_VALUE_DI"   varchar(2),  \
        "SEM1_VALUE_DI"         varchar(2),  \
        "SEM2_INTR_VALUE_DI"    varchar(2),  \
        "SEM2_TERM_VALUE_DI"    varchar(2),  \
        "SEM2_TERM2_VALUE_DI"   varchar(2),  \
        "SEM2_VALUE_DI"         varchar(2),  \
        "SEM3_INTR_VALUE_DI"    varchar(2),  \
        "SEM3_TERM_VALUE_DI"    varchar(2),  \
        "SEM3_VALUE_DI"         varchar(2),  \
        "GRAD_VALUE_DI"         varchar(2),  \
        "REGISTERCD"            varchar(8),  \
        "UPDATED"               timestamp default current timestamp  \
) in usr1dms index in idx1dms

insert into TMP_RECORD_DAT \
SELECT \
YEAR, \
SUBCLASSCD, \
TAKESEMES, \
SCHREGNO, \
CHAIRCD, \
cast(null as varchar(7)), \
cast(null as varchar(7)), \
cast(null as varchar(7)), \
cast(null as varchar(7)), \
cast(null as varchar(7)), \
cast(null as varchar(7)), \
cast(null as varchar(7)), \
cast(null as varchar(7)), \
SEM1_INTR_SCORE, \
SEM1_TERM_SCORE, \
cast(null as smallint), \
SEM2_INTR_SCORE, \
SEM2_TERM_SCORE, \
cast(null as smallint), \
SEM3_INTR_SCORE, \
SEM3_TERM_SCORE, \
SEM1_INTR_VALUE, \
SEM1_TERM_VALUE, \
cast(null as smallint), \
SEM1_VALUE, \
SEM2_INTR_VALUE, \
SEM2_TERM_VALUE, \
cast(null as smallint), \
SEM2_VALUE, \
SEM3_INTR_VALUE, \
SEM3_TERM_VALUE, \
SEM3_VALUE, \
GRAD_VALUE, \
cast(null as smallint), \
GET_CREDIT, \
ADD_CREDIT, \
cast(null as varchar(1)), \
cast(null as smallint), \
SEM1_INTR_SCORE_DI, \
SEM1_TERM_SCORE_DI, \
SEM2_INTR_SCORE_DI, \
SEM2_TERM_SCORE_DI, \
SEM3_INTR_SCORE_DI, \
SEM3_TERM_SCORE_DI, \
SEM1_INTR_VALUE_DI,  \
SEM1_TERM_VALUE_DI,  \
cast(null as varchar(2)), \
SEM1_VALUE_DI,  \
SEM2_INTR_VALUE_DI,  \
SEM2_TERM_VALUE_DI,  \
cast(null as varchar(2)), \
SEM2_VALUE_DI,  \
SEM3_INTR_VALUE_DI,  \
SEM3_TERM_VALUE_DI,  \
SEM3_VALUE_DI,  \
GRAD_VALUE_DI, \
REGISTERCD, \
UPDATED \
FROM \
RECORD_DAT 

drop table RECORD_DAT_OLD

rename table RECORD_DAT TO RECORD_DAT_OLD

rename table TMP_RECORD_DAT TO RECORD_DAT

alter table RECORD_DAT  \
add constraint PK_RECORD_DAT  \
primary key  \
( \
YEAR, \
SUBCLASSCD, \
TAKESEMES, \
SCHREGNO \
)

