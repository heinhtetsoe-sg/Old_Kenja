drop table exam_kind_dat


create table exam_kind_dat \
        (userid                   varchar(10)     not null, \
         enteryear                varchar(4)   , \
         exam_kind                varchar(1)   , \
         viewinputcd              varchar(1)   , \
         updated                  timestamp default current timestamp \
        ) in usr1dms index in idx1dms

alter table exam_kind_dat add constraint pk_exmknd_dt primary key \
         (userid)