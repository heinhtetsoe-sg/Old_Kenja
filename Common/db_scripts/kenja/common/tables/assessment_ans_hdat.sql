-- $Id: 9b7373b01cc60e3ac561330c2919f454c3afc082 $

drop table ASSESSMENT_ANS_HDAT
create table ASSESSMENT_ANS_HDAT ( \
    SCHREGNO        VARCHAR (8) not null, \
    WRITING_DATE    DATE        not null, \
    REGISTERCD      VARCHAR (10), \
    UPDATED         timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ASSESSMENT_ANS_HDAT add constraint PK_ASSESS_ANS_H primary key (SCHREGNO, WRITING_DATE)
