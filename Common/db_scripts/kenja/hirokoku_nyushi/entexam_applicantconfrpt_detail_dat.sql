-- $Id: entexam_applicantconfrpt_detail_dat.sql 63896 2018-12-11 05:55:24Z tawada $

drop table ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
create table ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT( \
    ENTEXAMYEAR        varchar(4)   not null, \
    APPLICANTDIV       varchar(1)   not null, \
    EXAMNO             varchar(10)  not null, \
    SEQ                varchar(3)   not null, \
    REMARK1            varchar(150), \
    REMARK2            varchar(150), \
    REMARK3            varchar(150), \
    REMARK4            varchar(150), \
    REMARK5            varchar(150), \
    REMARK6            varchar(150), \
    REMARK7            varchar(150), \
    REMARK8            varchar(150), \
    REMARK9            varchar(150), \
    REMARK10           varchar(150), \
    REMARK11           varchar(150), \
    REMARK12           varchar(150), \
    REGISTERCD         varchar(10), \
    UPDATED            timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT add constraint PK_EEXAM_APCNRPTDE primary key (ENTEXAMYEAR, APPLICANTDIV, EXAMNO, SEQ)