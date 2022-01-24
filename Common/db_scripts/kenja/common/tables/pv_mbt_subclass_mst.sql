
create table PV_MBT_SUBCLASS_MST( \
    SUBCLASSCD    VARCHAR(5)    NOT NULL, \
    SITE_ID       VARCHAR(30)   NOT NULL, \
    SUBCLASSNAME  VARCHAR(120) , \
    CLASSCD       VARCHAR(5)   , \
    STATUS        VARCHAR(30)  , \
    RECORD_DATE   TIMESTAMP    , \
    UPDATE_DATE   TIMESTAMP    , \
    DELETE_DATE   TIMESTAMP    , \
    REGISTERCD    VARCHAR(10)  , \
    UPDATED       TIMESTAMP     DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table PV_MBT_SUBCLASS_MST add constraint PK_PV_MBT_SUBCLASS_MST primary key (SUBCLASSCD,SITE_ID)

