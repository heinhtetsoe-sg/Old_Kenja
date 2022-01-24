drop table finschool_mst

create table finschool_mst \
    (finschoolcd        varchar(6) not null, \
     finschool_name     varchar(75), \
     finschool_kana     varchar(75), \
     princname          varchar(60), \
     princname_show     varchar(30), \
     princkana          varchar(120), \
     districtcd         varchar(2), \
     finschool_zipcd    varchar(8), \
     finschool_addr1    varchar(75), \
     finschool_addr2    varchar(75), \
     finschool_telno    varchar(14), \
     finschool_faxno    varchar(14), \
     edboardcd          varchar(6), \
     registercd         varchar(8), \
     updated            timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table finschool_mst add constraint pk_finschool_mst primary key (finschoolcd)
