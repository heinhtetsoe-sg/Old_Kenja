-- $Id: f2c04a0a6e4be3a949314b078b2d2ed1835550f1 $

drop table RECRUIT_VISIT_SCORE_DAT

create table RECRUIT_VISIT_SCORE_DAT( \
    YEAR                varchar(4) not null, \
    RECRUIT_NO          varchar(8) not null, \
    SEMESTER            varchar(1) not null, \
    SUBCLASSCD01        smallint, \
    SUBCLASSCD02        smallint, \
    SUBCLASSCD03        smallint, \
    SUBCLASSCD04        smallint, \
    SUBCLASSCD05        smallint, \
    SUBCLASSCD06        smallint, \
    SUBCLASSCD07        smallint, \
    SUBCLASSCD08        smallint, \
    SUBCLASSCD09        smallint, \
    TOTAL3              smallint, \
    TOTAL5              smallint, \
    TOTAL9              smallint, \
    SELECT_DIV          varchar(1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_VISIT_SCORE_DAT add constraint PK_RECRUIT_VIS_SCO primary key (YEAR, RECRUIT_NO, SEMESTER)
