-- kanji=漢字
-- $Id:

drop table REPORT_URINALYSIS2_DAT

create table REPORT_URINALYSIS2_DAT \
      ( \
        EDBOARD_SCHOOLCD varchar(12)   not null, \
        YEAR             varchar(4)    not null, \
        EXECUTE_DATE     date          not null, \
        FIXED_DATE       date, \
        REGISTERCD       varchar(10), \
        UPDATED          timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table REPORT_URINALYSIS2_DAT add constraint PK_REPORT_URINALYSIS2_DAT primary key (EDBOARD_SCHOOLCD, YEAR, EXECUTE_DATE)
