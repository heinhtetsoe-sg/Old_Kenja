-- $Id: prischool_mst.sql 56577 2017-10-22 11:35:50Z maeshiro $
drop table prischool_mst

create table prischool_mst \
    (prischoolcd        varchar(7) not null, \
     prischool_name     varchar(75), \
     prischool_kana     varchar(75), \
     princname          varchar(60), \
     princname_show     varchar(30), \
     princkana          varchar(120), \
     districtcd         varchar(2), \
     prischool_zipcd    varchar(8), \
     prischool_addr1    varchar(75), \
     prischool_addr2    varchar(75), \
     prischool_telno    varchar(14), \
     prischool_faxno    varchar(14), \
     registercd         varchar(8), \
     updated            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table prischool_mst add constraint pk_prischool_mst primary key (prischoolcd)
