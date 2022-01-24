-- $Id$

alter table HEXAM_EMPREMARK_SEQ_DAT alter column JOBHUNT_REC set data type varchar(1300)
alter table HEXAM_EMPREMARK_SEQ_DAT alter column JOBHUNT_RECOMMEND set data type varchar(3100)
alter table HEXAM_EMPREMARK_SEQ_DAT alter column JOBHUNT_ABSENCE set data type varchar(550)
alter table HEXAM_EMPREMARK_SEQ_DAT alter column JOBHUNT_HEALTHREMARK set data type varchar(130)

reorg table HEXAM_EMPREMARK_SEQ_DAT
