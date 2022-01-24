-- $Id: d8bbf36f34f968166f1de834e7128247e5973136 $
drop table ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT

create table ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT \
( \
    ENTEXAMYEAR         varchar(4)  not null, \
    EXAMNO              varchar(5)  not null, \
    SEQ                 varchar(3)  not null, \
    REMARK1             varchar(150), \
    REMARK2             varchar(150), \
    REMARK3             varchar(150), \
    REMARK4             varchar(150), \
    REMARK5             varchar(150), \
    REMARK6             varchar(150), \
    REMARK7             varchar(150), \
    REMARK8             varchar(150), \
    REMARK9             varchar(150), \
    REMARK10            varchar(150), \
    REMARK11            varchar(150), \
    REMARK12            varchar(150), \
    REMARK13            varchar(150), \
    REMARK14            varchar(150), \
    REMARK15            varchar(150), \
    REMARK16            varchar(150), \
    REMARK17            varchar(150), \
    REMARK18            varchar(150), \
    REMARK19            varchar(150), \
    REMARK20            varchar(150), \
    REMARK90            varchar(150), \
    REMARK91            varchar(150), \
    REMARK92            varchar(150), \
    REMARK93            varchar(150), \
    REMARK94            varchar(150), \
    REMARK95            varchar(150), \
    REMARK96            varchar(150), \
    REMARK97            varchar(150), \
    REMARK98            varchar(150), \
    REMARK99            varchar(150), \
    REGISTERCD          varchar(10),  \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT add constraint \
PK_ENTEXAM_APP_DEB primary key (ENTEXAMYEAR, EXAMNO, SEQ)

