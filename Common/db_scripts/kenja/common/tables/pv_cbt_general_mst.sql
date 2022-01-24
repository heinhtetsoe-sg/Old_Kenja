
create table PV_CBT_GENERAL_MST( \
    ID          VARCHAR(5)   NOT NULL, \
    RECORDCD    VARCHAR(10)  NOT NULL, \
    RECORDNAME  VARCHAR(30) , \
    FIELD1      VARCHAR(30) , \
    FIELD2      VARCHAR(30) , \
    REGISTERCD  VARCHAR(10) , \
    UPDATED     TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table PV_CBT_GENERAL_MST add constraint PK_PV_CBT_GENERAL_MST primary key (ID,RECORDCD)

