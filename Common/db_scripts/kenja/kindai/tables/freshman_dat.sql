
drop table freshman_dat

create table freshman_dat \
      (enteryear           varchar(4)      not null, \
       schregno            varchar(8)      not null, \
       ent_div             varchar(1), \
       hr_class            varchar(3), \
       attendno            varchar(3), \
       inoutcd             varchar(1), \
       coursecd            varchar(1), \
       majorcd             varchar(3), \
       name                varchar(60), \
       name_kana           varchar(120), \
       birthday            date, \
       sex                 varchar(1), \
       finschoolcd         varchar(6), \
       finschoolgraddate   date, \
       zipcd               varchar(8), \
       addr1               varchar(75), \
       addr2               varchar(75), \
       telno               varchar(14), \
       faxno               varchar(14), \
       email               varchar(20), \
       emergencycall       varchar(60), \
       emergencytelno      varchar(14), \
       registercd          varchar(8), \
       updated             timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table freshman_dat add constraint pk_freshman_dat primary key \
      (enteryear, schregno) 

