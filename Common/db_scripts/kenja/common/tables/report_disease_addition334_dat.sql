-- $Id: cb273ecbd8a70c76a103afa6225eeda583f94a70 $

drop table REPORT_DISEASE_ADDITION334_DAT

create table REPORT_DISEASE_ADDITION334_DAT( \
    EDBOARD_SCHOOLCD VARCHAR(12) NOT NULL, \
    YEAR             VARCHAR(4)  NOT NULL, \
    EXECUTE_DATE     DATE        NOT NULL, \
    FIXED_DATE       DATE               , \
    REGISTERCD       VARCHAR(10)        , \
    UPDATED          timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table REPORT_DISEASE_ADDITION334_DAT add constraint PK_REP_DIS_ADD334_DAT primary key (EDBOARD_SCHOOLCD, YEAR, EXECUTE_DATE)