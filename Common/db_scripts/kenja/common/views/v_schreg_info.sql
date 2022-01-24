-- $Id: b2a88bda422474da99f7200a4c8f5a246954a100 $

drop view V_SCHREG_INFO

create view V_SCHREG_INFO \
    (schregno, year, semester, grade, hr_class, attendno, annual, hr_name, hr_nameabbv, \
    coursecd, majorcd, coursecode, name, name_show, sex, \
    ent_date, ent_div, ent_reason, grd_date, grd_div, grd_reason) as \
SELECT \
    t1.schregno, \
    t2.year, \
    t2.semester, \
    t2.grade, \
    t2.hr_class, \
    t2.attendno, \
    t2.annual, \
    t3.hr_name, \
    t3.hr_nameabbv, \
    t2.coursecd, \
    t2.majorcd, \
    t2.coursecode, \
    t1.name, \
    t1.name_show, \
    t1.sex, \
    t1.ent_date, \
    t1.ent_div, \
    t1.ent_reason, \
    t1.grd_date, \
    t1.grd_div, \
    t1.grd_reason \
FROM \
    schreg_base_mst t1, \
    schreg_regd_dat t2, \
    schreg_regd_hdat t3 \
WHERE \
    t1.schregno = t2.schregno AND \
    t2.year = t3.year AND \
    t2.semester = t3.semester AND \
    t2.grade = t3.grade AND \
    t2.hr_class = t3.hr_class