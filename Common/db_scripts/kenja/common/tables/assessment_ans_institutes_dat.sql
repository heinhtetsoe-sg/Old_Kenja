-- $Id: 6b4b41c4cce586907440089e267f6a86a9d98a88 $

drop table ASSESSMENT_ANS_INSTITUTES_DAT
create table ASSESSMENT_ANS_INSTITUTES_DAT( \
    SCHREGNO            varchar(8)  not null, \
    WRITING_DATE        DATE        not null, \
    HANDICAP            varchar(150), \
    DIAGNOSIS_DATE      varchar(7), \
    INSTITUTES_CD       varchar(5), \
    ATTENDING_DOCTOR    varchar(30), \
    REMARK              varchar(150), \
    MEDICINE_FLG        varchar(1), \
    MEDICINE_NAME       varchar(150), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ASSESSMENT_ANS_INSTITUTES_DAT add constraint PK_ASSESS_ANS_I primary key (SCHREGNO, WRITING_DATE)
