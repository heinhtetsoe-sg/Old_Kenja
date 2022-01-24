-- $Id: 2480eaaadd23172aab4efce15e131d80dd6a6652 $

drop table ENTEXAM_KANRI_NO_DAT

create table ENTEXAM_KANRI_NO_DAT( \
    ENTEXAMYEAR     VARCHAR(4)   NOT NULL, \
    APPLICANTDIV    VARCHAR(1)   NOT NULL, \
    EXAMNO          VARCHAR(10)  NOT NULL, \
    REGISTERCD      VARCHAR(10), \
    UPDATED         TIMESTAMP    DEFAULT CURRENT TIMESTAMP \
 ) in usr1dms index in idx1dms

alter table ENTEXAM_KANRI_NO_DAT add constraint PK_ENTEXAM_KANRINO primary key (ENTEXAMYEAR, APPLICANTDIV, EXAMNO)
