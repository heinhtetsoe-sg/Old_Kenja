-- $Id: f464fd549b2c8bb6cdd137d81157aa4555b23ee1 $
drop table ENTEXAM_VISIT_DAT

create table ENTEXAM_VISIT_DAT \
( \
    ENTEXAMYEAR         varchar(4)   not null, \
    VISIT_NO            varchar(3)   not null, \
    VISIT_DATE          date         not null, \
    NAME                varchar(60)  , \
    NAME_KANA           varchar(120) , \
    ERACD               varchar(1)   , \
    BIRTH_Y             varchar(2)   , \
    BIRTH_M             varchar(2)   , \
    BIRTH_D             varchar(2)   , \
    SEX                 varchar(1)   , \
    ZIPCD               varchar(8),   \
    ADDRESS1            varchar(150), \
    ADDRESS2            varchar(150), \
    TELNO               varchar(14),  \
    FS_CD               varchar(12),  \
    FS_ERACD            varchar(1),   \
    FS_Y                varchar(2),   \
    FS_M                varchar(2),   \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_VISIT_DAT add constraint \
PK_ENTEXAM_VISIT primary key (ENTEXAMYEAR, VISIT_NO)
