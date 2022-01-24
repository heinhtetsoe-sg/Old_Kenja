-- $Id: 3597a9a4a38f5e25763b3fc3f4166600295e5fb5 $

drop table CHAIR_STF_DETAIL_DAT

create table CHAIR_STF_DETAIL_DAT \
      (YEAR          varchar(4)  not null, \
       SEMESTER      varchar(1)  not null, \
       CHAIRCD       varchar(7)  not null, \
       STAFFCD       varchar(10) not null, \
       SEQ           varchar(3)  not null, \
       REMARK_SINT1  smallint, \
       REMARK_SINT2  smallint, \
       REMARK_INT1   integer, \
       REMARK_INT2   integer, \
       REMARK_INT3   integer, \
       REMARK_CHAR1  varchar(90), \
       REMARK_CHAR2  varchar(90), \
       REMARK_CHAR3  varchar(90), \
       REMARK_CHAR4  varchar(90), \
       REMARK_CHAR5  varchar(90), \
       REGISTERCD    varchar(10), \
       UPDATED       timestamp default current timestamp \
      ) in usr1dms index in idx1dms

alter table CHAIR_STF_DETAIL_DAT add constraint PK_CHR_STF_DETAIL primary key \
      (YEAR, SEMESTER, CHAIRCD, STAFFCD, SEQ)
