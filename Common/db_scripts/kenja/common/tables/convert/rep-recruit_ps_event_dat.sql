-- $Id: fc8cf75601645f4b5135fc63048d6cf62cdbaf93 $

drop table RECRUIT_PS_EVENT_DAT_OLD

rename table RECRUIT_PS_EVENT_DAT to RECRUIT_PS_EVENT_DAT_OLD

create table RECRUIT_PS_EVENT_DAT( \
    YEAR                varchar(4)  not null, \
    RECRUIT_NO          varchar(14) not null, \
    TOUROKU_DATE        date        not null, \
    EVENT_CLASS_CD      varchar(3)  not null, \
    EVENT_CD            varchar(3)  not null, \
    MEDIA_CD            varchar(2)  not null, \
    PRISCHOOLCD         varchar(7)  not null, \
    PRISCHOOL_CLASS_CD  varchar(7)  not null, \
    STATE_CD            varchar(2), \
    REMARK              varchar(250), \
    ATTEND_MEETING_FLG  varchar(1), \
    DOC_REQ_NUMBER      varchar(3), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

INSERT INTO RECRUIT_PS_EVENT_DAT \
    SELECT \
        YEAR, \
        RECRUIT_NO, \
        TOUROKU_DATE, \
        EVENT_CLASS_CD, \
        EVENT_CD, \
        MEDIA_CD, \
        PRISCHOOLCD, \
        PRISCHOOL_CLASS_CD, \
        STATE_CD, \
        REMARK, \
        CAST(NULL AS VARCHAR(1)) AS ATTEND_MEETING_FLG, \
        CAST(NULL AS VARCHAR(3)) AS DOC_REQ_NUMBER, \
        REGISTERCD, \
        UPDATED \
    FROM \
        RECRUIT_PS_EVENT_DAT_OLD

alter table RECRUIT_PS_EVENT_DAT add constraint PK_RECRUIT_PS_E_D primary key (YEAR, RECRUIT_NO, TOUROKU_DATE, EVENT_CLASS_CD, EVENT_CD, MEDIA_CD)
