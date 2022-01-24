-- $Id: 69f4bfb6f9388369ba1ceaaa2a53be49b0efcee5 $

drop table FINHIGHSCHOOL_YDAT_OLD
create table FINHIGHSCHOOL_YDAT_OLD like FINHIGHSCHOOL_YDAT
insert into FINHIGHSCHOOL_YDAT_OLD select * from FINHIGHSCHOOL_YDAT


drop table FINHIGHSCHOOL_YDAT

create table FINHIGHSCHOOL_YDAT \
    (YEAR           varchar(4) not null, \
     FINSCHOOLCD    varchar(12) not null, \
     REGISTERCD     varchar(10), \
     UPDATED        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table FINHIGHSCHOOL_YDAT add constraint PK_FINHIGHSCHOOL_Y primary key \
    (YEAR, FINSCHOOLCD)

INSERT INTO FINHIGHSCHOOL_YDAT \
    SELECT \
        * \
    FROM \
        FINHIGHSCHOOL_YDAT_OLD
