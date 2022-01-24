
create table PV_MBT_CLASS_MST( \
    CLASSCD      VARCHAR(5)   NOT NULL, \
    SITE_ID      VARCHAR(30)  NOT NULL, \
    CLASSNAME    VARCHAR(60) , \
    RECORD_DATE  TIMESTAMP   , \
    UPDATE_DATE  TIMESTAMP   , \
    DELETE_DATE  TIMESTAMP   , \
    REGISTERCD   VARCHAR(10) , \
    UPDATED      TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table PV_MBT_CLASS_MST add constraint PK_PV_MBT_CLASS_MST primary key (CLASSCD,SITE_ID)

