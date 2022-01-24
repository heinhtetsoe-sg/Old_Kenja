-- kanji=´Á»ú
-- $Id: 35801c62212227ebc8c2562f3829ce2d7e23c3f8 $

drop table ENTEXAM_SEAT_EXEC_HIST_DAT

create table ENTEXAM_SEAT_EXEC_HIST_DAT \
(  \
    ENTEXAMYEAR   VARCHAR(4)  not null, \
    APPLICANTDIV  VARCHAR(1)  not null, \
    TESTDIV       VARCHAR(2)  not null, \
    EXAMNO        VARCHAR(10) not null, \
    EXEC_TIME     TIMESTAMP   not null, \
    NAME          VARCHAR(60), \
    EXAMHALLNO    VARCHAR(2), \
    SEATNO        VARCHAR(2), \
    REGISTERCD    VARCHAR(10), \
    UPDATED       timestamp default current timestamp \
) in usr1dms index in idx1dms

alter table ENTEXAM_SEAT_EXEC_HIST_DAT add constraint PK_SEAT_EXEC_HIST_D \
primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAMNO, EXEC_TIME)
