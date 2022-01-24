drop table entexam_applicantconfrpt_dat

CREATE TABLE entexam_applicantconfrpt_dat \
( \
    entexamyear         varchar(4)  not null, \
    examno              varchar(5)  not null, \
    confidential_rpt01  smallint, \
    confidential_rpt02  smallint, \
    confidential_rpt03  smallint, \
    confidential_rpt04  smallint, \
    confidential_rpt05  smallint, \
    confidential_rpt06  smallint, \
    confidential_rpt07  smallint, \
    confidential_rpt08  smallint, \
    confidential_rpt09  smallint, \
    confidential_rpt10  smallint, \
    confidential_rpt11  smallint, \
    confidential_rpt12  smallint, \
    absence_days       smallint, \
    average5            decimal(4,1), \
    average_all         decimal(4,1), \
    registercd          varchar(8), \
    updated             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table entexam_applicantconfrpt_dat add constraint \
pk_entexam_apcnrpt primary key (entexamyear,examno)
