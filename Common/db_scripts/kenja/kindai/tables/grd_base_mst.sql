drop table grd_base_mst

create table grd_base_mst \
    (schregno            varchar(8)    not null, \
     inoutcd             varchar(1), \
     name                varchar(60), \
     name_show           varchar(30), \
     name_kana           varchar(120), \
     name_eng            varchar(40), \
     old_name            varchar(60), \
     old_name_show       varchar(30), \
     old_name_kana       varchar(120), \
     old_name_eng        varchar(40), \
     birthday            date, \
     sex                 varchar(1), \
     bloodtype           varchar(2), \
     blood_rh            varchar(1), \
     finschoolcd         varchar(6), \
     finish_date         date, \
     ent_date            date, \
     ent_div             varchar(1), \
     grd_date            date, \
     grd_div             varchar(1), \
     grd_reason          varchar(75), \
     grd_no              varchar(8), \
     grd_term            varchar(4), \
     grd_semester        varchar(1), \
     grd_grade           varchar(2), \
     grd_hr_class        varchar(3), \
     grd_attendno        varchar(3), \
     permanentzipcd      varchar(8), \
     permanentaddr1      varchar(75), \
     permanentaddr2      varchar(75), \
     cur_zipcd           varchar(8), \
     cur_areacd          varchar(1), \
     cur_addr1           varchar(75), \
     cur_addr2           varchar(75), \
     cur_addr1_eng       varchar(50), \
     cur_addr2_eng       varchar(50), \
     cur_telno           varchar(14), \
     cur_faxno           varchar(14), \
     cur_email           varchar(20), \
     cur_emergencycall   varchar(60), \
     cur_emergencytelno  varchar(14), \
     zipcd               varchar(8), \
     areacd              varchar(1), \
     addr1               varchar(75), \
     addr2               varchar(75), \
     telno               varchar(14), \
     faxno               varchar(14), \
     registercd          varchar(8), \
     updated             timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table grd_base_mst add constraint pk_grd_base primary key (schregno)

