-- $Id: 6b3e97fd761b2355d05ee7566f8e302a98ee067c $

DROP TABLE FINSCHOOL_YDAT_OLD
CREATE TABLE FINSCHOOL_YDAT_OLD LIKE FINSCHOOL_YDAT
INSERT INTO FINSCHOOL_YDAT_OLD SELECT * from FINSCHOOL_YDAT

drop   table FINSCHOOL_YDAT
create table FINSCHOOL_YDAT \
    (year           varchar(4) not null, \
     finschoolcd    varchar(12) not null, \
     registercd     varchar(10), \
     updated        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

insert into FINSCHOOL_YDAT \
    SELECT \
        * \
    FROM \
        FINSCHOOL_YDAT_OLD

alter table FINSCHOOL_YDAT add constraint pk_finschool_y primary key (year, finschoolcd)
