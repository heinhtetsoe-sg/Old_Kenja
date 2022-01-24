DROP INDEX IX_MENU 
DROP INDEX IX_schregbase_m1 
DROP INDEX IX_schregbase_m2 
DROP INDEX IX_schregregd_hd1 
DROP INDEX IX_schregregd_d1 
DROP INDEX IX_schregregd_d2 
DROP INDEX IX_schregregd_d3 
DROP INDEX IX_schregregd_d4 
DROP INDEX IX_schregregd_d5 
DROP INDEX IX_bschedule_d1 
DROP INDEX IX_schedule_d1 
DROP INDEX IX_schedule_d2 
DROP INDEX IX_schedule_d3 
DROP INDEX IX_schedule_d4 
DROP INDEX IX_schedule_d5 
DROP INDEX IX_schedule_d6 
DROP INDEX IX_schedule_d7 
DROP INDEX IX_attendclass_c1 
DROP INDEX IX_attendclass_c2 
DROP INDEX IX_attendclass_c3 
DROP INDEX IX_attendclass_d1 
DROP INDEX IX_attend_d1 
DROP INDEX IX_applicant_m1 
DROP INDEX IX_applicant_m2 
DROP INDEX IX_applicant_m3 
DROP INDEX IX_applicant_m4 
DROP INDEX IX_zipcd_m1 
DROP INDEX IX_zipcd_m2 


CREATE INDEX IX_MENU \
ON menu_mst \
(exename) \

CREATE INDEX IX_schregbase_m1 \
ON schreg_base_mst \
(lname) \

CREATE INDEX IX_schregbase_m2 \
ON schreg_base_mst \
(lkana) \

CREATE INDEX IX_schregregd_hd1 \
ON schreg_regd_hdat \
(tr_cd1) \

CREATE INDEX IX_schregregd_d1 \
ON schreg_regd_dat \
(coursecode1) \
 
//CREATE UNIQUE INDEX IX_schregregd_d2 \
//ON schreg_regd_dat \
//(year, semester, grade, hr_class, attendno) \


CREATE UNIQUE INDEX IX_schregregd_d3 \
ON schreg_regd_dat \
(year, semester, grade, hr_class, seat_row, seat_col) \

CREATE INDEX IX_schregregd_d4 \
ON schreg_regd_dat \
(coursecd,majorcd) \

//CREATE INDEX IX_schregregd_d5 \
//ON schreg_regd_dat \
//(majorcd) \

CREATE INDEX IX_bschedule_d1 \
ON bschedule_dat \
(year, SEQ, daycd) \

CREATE INDEX IX_schedule_d1 \
ON schedule_dat \
(year,semester) \

CREATE INDEX IX_schedule_d2 \
ON schedule_dat \
(classcd) \
 
CREATE INDEX IX_schedule_d3 \
ON schedule_dat \
(subclasscd) \
 
CREATE INDEX IX_schedule_d4 \
ON schedule_dat \
(year, staffcd, datacd) \
 
CREATE INDEX IX_schedule_d5 \
ON schedule_dat \
(executedate) \

CREATE INDEX IX_schedule_d6 \
ON schedule_dat \
(attendclasscd) \
 
CREATE INDEX IX_schedule_d7 \
ON schedule_dat \
(periodcd) \
 
CREATE INDEX IX_attendclass_c1 \
ON attendclasscd_cre \
(year) \

CREATE INDEX IX_attendclass_c2 \
ON attendclasscd_cre \
(attendclasscd) \
 
CREATE UNIQUE INDEX IX_attendclass_c3 \
ON attendclasscd_cre \
(year, attendclasscd) \
 
CREATE INDEX IX_attendclass_d1 \
ON attendclass_dat \
(schregno, year, attendclasscd) \
 
CREATE UNIQUE INDEX IX_attend_d1 \
ON attend_dat \
(schregno, attenddate, periodcd) \

CREATE INDEX IX_applicant_m1 \
ON applicant_mst \
(d1_coursecd, d1_majorcd) \
 
CREATE INDEX IX_applicant_m2 \
ON applicant_mst \
(d2_coursecd, d2_majorcd) \

CREATE INDEX IX_applicant_m3 \
ON applicant_mst \
(acceptcd) \

CREATE INDEX IX_applicant_m4 \
ON applicant_mst \
(attendcd) \

CREATE INDEX IX_zipcd_m1 \
ON zipcd_mst \
(new_zipcd) \

CREATE INDEX IX_zipcd_m2 \
ON zipcd_mst \
(old_zipcd) \

CREATE INDEX IX_zipcd_m3 \
ON zipcd_mst \
(pref) \

