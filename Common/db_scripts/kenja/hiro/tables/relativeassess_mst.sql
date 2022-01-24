drop table relativeassess_mst

create table relativeassess_mst \
    (grade          varchar(2)   not null, \
     subclasscd     varchar(6)   not null, \
     assesscd       varchar(1)   not null, \
     assesslevel    smallint     not null, \
     assessmark     varchar(6), \
     assesslow      decimal(4,1), \
     assesshigh     decimal(4,1), \
     registercd     varchar(8), \
     updated        timestamp default current timestamp \
    ) in usr1dms index in idx1dms

alter table relativeassess_mst add constraint pk_relaassess_mst primary key \
    (grade, subclasscd, assesscd, assesslevel)
