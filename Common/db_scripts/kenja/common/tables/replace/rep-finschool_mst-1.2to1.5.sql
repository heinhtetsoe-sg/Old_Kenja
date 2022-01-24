-- $Id: 6dcd1a86785169db90ea6fd34e80b5fd459c928d $

DROP TABLE finschool_mst_old

CREATE TABLE finschool_mst_old LIKE finschool_mst

INSERT INTO finschool_mst_old SELECT * from finschool_mst

DROP TABLE finschool_mst

create table finschool_mst \
    (finschoolcd         varchar(7) not null, \
     finschool_type      varchar(1), \
     finschool_distcd    varchar(3), \
     finschool_distcd2   varchar(3), \
     finschool_div       varchar(1), \
     finschool_name      varchar(75), \
     finschool_kana      varchar(75), \
     finschool_name_abbv varchar(30), \
     finschool_kana_abbv varchar(75), \
     princname           varchar(60), \
     princname_show      varchar(30), \
     princkana           varchar(120), \
     districtcd          varchar(2), \
     finschool_zipcd     varchar(8), \
     finschool_addr1     varchar(75), \
     finschool_addr2     varchar(75), \
     finschool_telno     varchar(14), \
     finschool_faxno     varchar(14), \
     edboardcd           varchar(6), \
     registercd          varchar(8), \
     updated             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

insert into finschool_mst \
    select \
        RIGHT(RTRIM('0000000'||finschoolcd),7) as finschoolcd, \
        cast(NULL as varchar(1)) as finschool_type, \
        cast(NULL as varchar(3)) as finschool_distcd, \
        cast(NULL as varchar(3)) as finschool_distcd2, \
        cast(NULL as varchar(1)) as finschool_div, \
        finschool_name, \
        finschool_kana, \
        cast(NULL as varchar(30)) as finschool_name_abbv, \
        cast(NULL as varchar(75)) as finschool_kana_abbv, \
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
