
create table PV_CBT_USER_DAT( \
    KNJID         VARCHAR(10)  NOT NULL, \
    LOGINID       VARCHAR(10)  NOT NULL, \
    INITIAL_PASS  VARCHAR(10) , \
    USEFLG        VARCHAR(2)  , \
    USER_TYPE     VARCHAR(2)  , \
    START_DATE    DATE        , \
    END_DATE      DATE        , \
    UPDATE_DATE   DATE        , \
    UPDATE_TIME   TIME        , \
    REGISTERCD    VARCHAR(10) , \
    UPDATED       TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table PV_CBT_USER_DAT add constraint PK_PV_CBT_USER_DAT primary key (KNJID,LOGINID)

