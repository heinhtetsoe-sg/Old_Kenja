drop table ENTEXAM_TOKEI_HIGH_LOW_DAT

create table ENTEXAM_TOKEI_HIGH_LOW_DAT(  \
  ENTEXAMYEAR     VARCHAR(4) not null,  \
  APPLICANTDIV    VARCHAR(1) not null,  \
  TESTDIV         VARCHAR(2) not null,  \
  EXAM_TYPE       VARCHAR(2) not null,  \
  SHDIV           VARCHAR(1) not null,  \
  COURSECD        VARCHAR(1) not null,  \
  MAJORCD         VARCHAR(3) not null,  \
  EXAMCOURSECD    VARCHAR(4) not null,  \
  SEX             VARCHAR(1) not null,  \
  TESTSUBCLASSCD  VARCHAR(2) not null,  \
  HIGHSCORE       SMALLINT,  \
  LOWSCORE        SMALLINT,  \
  AVG             DECIMAL(5, 2),  \
  TOTAL           SMALLINT,  \
  COUNT           SMALLINT,  \
  REGISTERCD      VARCHAR(10),  \
  UPDATED         TIMESTAMP default CURRENT TIMESTAMP  \
) in usr1dms index in idx1dms

alter table ENTEXAM_TOKEI_HIGH_LOW_DAT add constraint PK_ENTEXAM_TOKEI_HIGH_LOW_D primary key (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, SHDIV, COURSECD, MAJORCD, EXAMCOURSECD, SEX, TESTSUBCLASSCD)
