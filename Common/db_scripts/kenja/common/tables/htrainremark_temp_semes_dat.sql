-- $Id: 40107a76532b3011de04e8ccc29fc0d163eea350 $

drop table HTRAINREMARK_TEMP_SEMES_DAT
create table HTRAINREMARK_TEMP_SEMES_DAT( \
    YEAR             varchar(4)    not null, \
    SEMESTER         varchar(1)    not null, \
    GRADE            varchar(2)    not null, \
    DATA_DIV         varchar(2)    not null, \
    PATTERN_CD       smallint      not null, \
    PATTERN_NAME     varchar(2), \
    REMARK           varchar(1500), \
    REGISTERCD       varchar(10), \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table HTRAINREMARK_TEMP_SEMES_DAT add constraint PK_HTRAINRE_SEM primary key (YEAR, SEMESTER, GRADE, DATA_DIV, PATTERN_CD)