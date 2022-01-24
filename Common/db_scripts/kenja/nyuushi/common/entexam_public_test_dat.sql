-- $Id: 13e341cda468fbb157ccc89651519cf7358b1ffe $

drop table ENTEXAM_PUBLIC_TEST_DAT
create table ENTEXAM_PUBLIC_TEST_DAT( \
    ENTEXAMYEAR         varchar(4)  NOT NULL, \
    APPLICANTDIV        varchar(1)  NOT NULL, \
    EXAMNO              varchar(10) NOT NULL, \
    SCORE1              smallint, \
    SCORE2              smallint, \
    AVG                 decimal(4,1), \
    VALUE               smallint, \
    KAKUYAKU_FLG        varchar(1), \
    REGISTERCD          varchar(10), \
    UPDATED             timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_PUBLIC_TEST_DAT add constraint PK_ENTEXAM_PUBTEST primary key (ENTEXAMYEAR, APPLICANTDIV, EXAMNO)