drop table element_mst

create table element_mst \
        (e_cd                   varchar(6)      not null, \
         e_name                 varchar(20), \
         e_kana                 varchar(20), \
         e_princ_lname          varchar(20), \
         e_princ_fname          varchar(20), \
         e_princ_lname_show     varchar(10), \
         e_princ_fname_show     varchar(10), \
         e_princ_lkana          varchar(40), \
         e_princ_fkana          varchar(40), \
         districtcd             varchar(2), \
         e_zipcd                varchar(8), \
         e_address1             varchar(50), \
         e_address2             varchar(50), \
         e_telno                varchar(13), \
         e_faxno                varchar(13), \
         testcd                 varchar(6), \
         edboardcd              varchar(6), \
         updated                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table element_mst add constraint pk_element_mst primary key (e_cd)

