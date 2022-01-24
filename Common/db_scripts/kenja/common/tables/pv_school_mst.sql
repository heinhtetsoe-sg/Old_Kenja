
create table PV_SCHOOL_MST ( \
    SCHOOL_CD          VARCHAR(10)  NOT NULL, \
    SCHOOL_NAME        VARCHAR(90)  NOT NULL, \
    PREFCD             VARCHAR(2)  , \
    SCHOOL_GROUP       VARCHAR(3)  , \
    SCHOOL_GROUP_NAME  VARCHAR(90) , \
    SCHOOL_KIND        VARCHAR(2)  , \
    SCHOOL_KIND_NAME   VARCHAR(90) , \
    USEFLG             VARCHAR(2)  , \
    STARTDATE          DATE        , \
    ENDDATE            DATE        , \
    UPDATE_FLG         VARCHAR(1)  , \
    REGISTERCD         VARCHAR(10) , \
    UPDATED            TIMESTAMP    \
 ) in usr1dms index in idx1dms

alter table PV_SCHOOL_MST add constraint PK_PV_SCHOOL_MST primary key (SCHOOL_CD)

