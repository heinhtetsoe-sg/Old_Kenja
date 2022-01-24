-- kanji=漢字
-- $Id:

drop table REPORT_ECG_DAT

create table REPORT_ECG_DAT \
      ( \
        EDBOARD_SCHOOLCD varchar(12)   not null, \
        YEAR             varchar(4)    not null, \
        EXECUTE_DATE     date          not null, \
        FIXED_DATE       date, \
        REGISTERCD       varchar(10), \
        UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table REPORT_ECG_DAT add constraint PK_REPORT_ECG_DAT primary key (EDBOARD_SCHOOLCD, YEAR, EXECUTE_DATE)
