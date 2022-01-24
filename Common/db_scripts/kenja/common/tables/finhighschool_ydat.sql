-- $Id: d3149bd3d5782a088172836d57fcf14fd4b00746 $
drop table FINHIGHSCHOOL_YDAT

create table FINHIGHSCHOOL_YDAT \
    (YEAR           varchar(4) not null, \
     FINSCHOOLCD    varchar(12) not null, \
     REGISTERCD     varchar(10), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table FINHIGHSCHOOL_YDAT add constraint PK_FINHIGHSCHOOL_Y primary key \
    (YEAR, FINSCHOOLCD)
