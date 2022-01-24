-- $Id: ff47cfb5a65a79b27c62ed15d0f16a88dba24592 $

alter table ENTEXAM_QUALIFIED_MST alter column PLUS_POINT set data type decimal(4, 1)
reorg table ENTEXAM_QUALIFIED_MST
