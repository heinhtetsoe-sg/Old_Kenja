-- $Id: 2a9e2d3fb8dc9dc4ba7165626caa363ac30ccc0b $

DROP TABLE finschool_mst_old

CREATE TABLE finschool_mst_old LIKE finschool_mst

INSERT INTO finschool_mst_old SELECT * from finschool_mst

DROP TABLE finschool_mst

create table finschool_mst \
    (finschoolcd      varchar(6) not null, \
     finschool_name   varchar(75)  , \
     finschool_kana   varchar(75)  , \
     princname        varchar(60)  , \
     princname_show   varchar(30)  , \
     princkana        varchar(120) , \
     districtcd       varchar(2)   , \
     finschool_zipcd  varchar(8)   , \
     finschool_addr1  varchar(75)  , \
     finschool_addr2  varchar(75)  , \
     finschool_telno  varchar(14)  , \
     finschool_faxno  varchar(14)  , \
     edboardcd        varchar(6)   , \
     registercd       varchar(8)   , \
     updated          timestamp default current timestamp \
    ) in usr1dms index in idx1dms

insert into finschool_mst \
    select \
        char(int(finschoolcd)) as finschoolcd, \
        finschool_name, \
        finschool_kana, \
        princname, \
        princname_show, \
        princkana, \
        districtcd, \
        finschool_zipcd, \
        finschool_addr1, \
        finschool_addr2, \
        finschool_telno, \
        finschool_faxno, \
        edboardcd, \
        registercd, \
        updated \
    from finschool_mst_old

alter table finschool_mst add constraint pk_finschool_mst primary key (finschoolcd)
