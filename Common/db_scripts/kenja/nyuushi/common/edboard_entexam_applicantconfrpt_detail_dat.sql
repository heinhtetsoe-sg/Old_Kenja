-- $Id: 3b00bb891bb9796c65b9d57b9da6802329e58869 $

drop table EDBOARD_ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
create table EDBOARD_ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT( \
    EDBOARD_SCHOOLCD    varchar(12)     not null, \
    ENTEXAMYEAR         varchar(4)      not null, \
    APPLICANTDIV        varchar(1)      not null, \
    EXAMNO              varchar(10)     not null, \
    SEQ                 varchar(3)      not null, \
    REMARK1             varchar(150), \
    REMARK2             varchar(150), \
    REMARK3             varchar(150), \
    REMARK4             varchar(750), \
    REMARK5             varchar(750), \
    REMARK6             varchar(750), \
    REMARK7             varchar(150), \
    REMARK8             varchar(150), \
    REMARK9             varchar(150), \
    REMARK10            varchar(150), \
    REMARK11            varchar(150), \
    REMARK12            varchar(150), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table EDBOARD_ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT add constraint \
PK_ED_EE_APCNRPTDE primary key (EDBOARD_SCHOOLCD, ENTEXAMYEAR, APPLICANTDIV, EXAMNO, SEQ)
