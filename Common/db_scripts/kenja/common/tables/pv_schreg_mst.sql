
create table PV_SCHREG_MST( \
    KNJID       VARCHAR(10)  NOT NULL, \
    SCHREGNO    VARCHAR(8)   NOT NULL, \
    REGISTERCD  VARCHAR(10) , \
    UPDATED     TIMESTAMP    DEFAULT CURRENT TIMESTAMP, \
    LOGINID     VARCHAR(10)  \
 ) in usr1dms index in idx1dms

alter table PV_SCHREG_MST add constraint PK_PV_SCHREG_MST primary key (KNJID,SCHREGNO)

