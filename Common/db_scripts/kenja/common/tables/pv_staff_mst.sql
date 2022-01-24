
create table PV_STAFF_MST( \
    KNJID       VARCHAR(10)  NOT NULL, \
    STAFFCD     VARCHAR(10)  NOT NULL, \
    REGISTERCD  VARCHAR(10) , \
    UPDATED     TIMESTAMP    DEFAULT CURRENT TIMESTAMP, \
    LOGINID     VARCHAR(10)  \
 ) in usr1dms index in idx1dms

alter table PV_STAFF_MST add constraint PK_PV_STAFF_MST primary key (KNJID,STAFFCD)

