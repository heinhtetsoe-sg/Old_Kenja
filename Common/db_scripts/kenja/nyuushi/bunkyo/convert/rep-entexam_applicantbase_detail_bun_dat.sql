-- $Id: 2754f1c707aded936eaf67c9608e061b2940ec77 $
insert into ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT \
( ENTEXAMYEAR, \
  EXAMNO, \
  SEQ, \
  REMARK1, \
  REMARK2, \
  REMARK3, \
  REMARK4, \
  REMARK5, \
  REMARK6, \
  REMARK7, \
  REMARK8, \
  REMARK9, \
  REMARK10, \
  REMARK90, \
  REMARK91, \
  REMARK92, \
  REMARK93, \
  REMARK94, \
  REMARK95, \
  REMARK96, \
  REMARK97, \
  REMARK98, \
  REMARK99, \
  REGISTERCD, \
  UPDATED) \
select \
 ENTEXAMYEAR, \
 EXAMNO, \
 SEQ, \
 REMARK1, \
 REMARK2, \
 REMARK3, \
 REMARK4, \
 REMARK5, \
 REMARK6, \
 REMARK7, \
 REMARK8, \
 REMARK9, \
 REMARK10, \
 REMARK90, \
 REMARK91, \
 REMARK92, \
 REMARK93, \
 REMARK94, \
 REMARK95, \
 REMARK96, \
 REMARK97, \
 REMARK98, \
 REMARK99, \
 REGISTERCD, \
 UPDATED \
from ENTEXAM_APPLICANTBASE_DETAIL_DAT \
where \
    SEQ IN ('010', '011', '012', '013')

