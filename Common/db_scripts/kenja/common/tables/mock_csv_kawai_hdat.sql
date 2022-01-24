-- $Id: f9d5dbb1d4745f1bb521f30d6ef10d13ec6ee8ba $

drop table MOCK_CSV_KAWAI_HDAT
create table MOCK_CSV_KAWAI_HDAT( \
    YOUSHI_NO       varchar(15), \
    YEAR            varchar(4)  not null, \
    MOSI_CD         varchar(5)  not null, \
    MOCKCD          varchar(9)  not null, \
    SCHOOL_CD       varchar(12), \
    GRADE           varchar(2)  not null, \
    HR_CLASS        varchar(3)  not null, \
    ATTENDNO        varchar(3)  not null, \
    SCHREGNO        varchar(8), \
    KANA            varchar(120), \
    EXAM_TYPE       varchar(1), \
    BUN_RI_CD       varchar(1), \
    REMARK_A        varchar(60), \
    REMARK_B        varchar(60), \
    REMARK_C        varchar(60), \
    REMARK_D        varchar(60), \
    REMARK_E        varchar(60), \
    REMARK_F        varchar(60), \
    REMARK_G        varchar(60), \
    REMARK_H        varchar(60), \
    REMARK_I        varchar(60), \
    REGISTERCD      varchar(10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table MOCK_CSV_KAWAI_HDAT add constraint PK_KAWAI_H primary key (YEAR, MOSI_CD, GRADE, HR_CLASS, ATTENDNO)
