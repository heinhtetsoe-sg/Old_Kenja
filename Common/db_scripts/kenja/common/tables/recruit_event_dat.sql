-- $Id: f735956868ff8c7b6a753654db85d7797fd9c653 $

drop table RECRUIT_EVENT_DAT

create table RECRUIT_EVENT_DAT( \
    YEAR                varchar(4) not null, \
    RECRUIT_NO          varchar(8) not null, \
    TOUROKU_DATE        date not null, \
    EVENT_CLASS_CD      varchar(3) not null, \
    EVENT_CD            varchar(3) not null, \
    MEDIA_CD            varchar(2) not null, \
    STATE_CD            varchar(2), \
    HOPE_COURSECD1      varchar(1), \
    HOPE_MAJORCD1       varchar(3), \
    HOPE_COURSECODE1    varchar(4), \
    HOPE_COURSECD2      varchar(1), \
    HOPE_MAJORCD2       varchar(3), \
    HOPE_COURSECODE2    varchar(4), \
    HOPE_COURSECD3      varchar(1), \
    HOPE_MAJORCD3       varchar(3), \
    HOPE_COURSECODE3    varchar(4), \
    HOPE_COURSECD4      varchar(1), \
    HOPE_MAJORCD4       varchar(3), \
    HOPE_COURSECODE4    varchar(4), \
    HOPE_COURSECD5      varchar(1), \
    HOPE_MAJORCD5       varchar(3), \
    HOPE_COURSECODE5    varchar(4), \
    REMARK              varchar(250), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table RECRUIT_EVENT_DAT add constraint PK_RECRUIT_E_DAT primary key (YEAR, RECRUIT_NO, TOUROKU_DATE, EVENT_CLASS_CD, EVENT_CD, MEDIA_CD)
