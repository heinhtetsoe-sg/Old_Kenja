-- $Id: 35ea35785151a9696d7498a104158392b9caf022 $

drop table HREPORT_GUIDANCE_SCHREG_REMARK_HDAT

create table HREPORT_GUIDANCE_SCHREG_REMARK_HDAT( \
    YEAR           varchar(4)     not null, \
    SEMESTER       varchar(1)     not null, \
    RECORD_DATE    date           not null, \
    SCHREGNO       varchar(8)     not null, \
    RECORD_STAFFCD varchar(10)   , \
    REGISTERCD     varchar(10)   , \
    UPDATED        timestamp      default current timestamp \
 ) in usr1dms index in idx1dms

alter table HREPORT_GUIDANCE_SCHREG_REMARK_HDAT add constraint PK_HR_GU_SC_R_HD \
primary key (YEAR, SEMESTER, RECORD_DATE, SCHREGNO)
