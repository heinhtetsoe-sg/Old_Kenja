-- $Id: 32ddd2c6e5720872c9684007e9885124b1196929 $

drop table EVENT_MST

create table EVENT_MST \
    (SCHOOL_KIND         VARCHAR(2)  NOT NULL, \
     DATA_DIV            VARCHAR(1)  NOT NULL, \
     GRADE               VARCHAR(2)  NOT NULL, \
     COURSECD            VARCHAR(1)  NOT NULL, \
     MAJORCD             VARCHAR(3)  NOT NULL, \
     EXECUTEDATE         DATE  NOT NULL, \
     HR_CLASS            VARCHAR(3)  NOT NULL, \
     HR_CLASS_DIV        VARCHAR(1)  NOT NULL, \
     HOLIDAY_FLG         VARCHAR(1), \
     EVENT_FLG           VARCHAR(1), \
     REMARK1             VARCHAR(150), \
     REMARK2             VARCHAR(150), \
     REGISTERCD          VARCHAR(8), \
    ) in usr1dms index in idx1dms

alter table EVENT_MST add constraint pk_event_mst primary key (SCHOOL_KIND,DATA_DIV,GRADE,EXECUTEDATE,HR_CLASS,HR_CLASS_DIV)
