drop table junior_mst

create table junior_mst \
        (j_cd                   varchar(6)      not null, \
         j_name                 varchar(20), \
         j_kana                 varchar(20), \
         j_princ_lname          varchar(20), \
         j_princ_fname          varchar(20), \
         j_princ_lname_show     varchar(10), \
         j_princ_fname_show     varchar(10), \
         j_princ_lkana          varchar(40), \
         j_princ_fkana          varchar(40), \
         districtcd             varchar(2), \
         j_zipcd                varchar(8), \
         j_address1             varchar(50), \
         j_address2             varchar(50), \
         j_telno                varchar(13), \
         j_faxno                varchar(13), \
         testcd                 varchar(6), \
         edboardcd              varchar(6), \
         updated                timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table junior_mst add constraint pk_junior_mst primary key (j_cd)

