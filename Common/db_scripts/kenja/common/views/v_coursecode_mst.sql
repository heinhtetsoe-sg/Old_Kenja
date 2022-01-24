-- $Id: a5290d95efdeb6f893078f4d497380004d29ec41 $

drop view v_coursecode_mst

create view v_coursecode_mst (year, coursecode, coursecodename, COURSECODEABBV1, COURSECODEABBV2, COURSECODEABBV3, updated) as \
    SELECT \
        t1.year, \
        t2.coursecode, \
        t2.coursecodename, \
        t2.COURSECODEABBV1, \
        t2.COURSECODEABBV2, \
        t2.COURSECODEABBV3, \
        t2.updated \
    FROM \
        coursecode_ydat t1, \
        coursecode_mst t2 \
    WHERE \
        t1.coursecode = t2.coursecode
