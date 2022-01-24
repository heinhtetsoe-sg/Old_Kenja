drop trigger attendclasscd_tri

CREATE TRIGGER attendclasscd_TRI \
NO CASCADE BEFORE INSERT  \
ON attendclasscd_cre \
REFERENCING NEW AS NEW \
FOR EACH ROW MODE DB2SQL WHEN (New.attendclasscd IS NULL) \
	SET New.attendclasscd = (SELECT COALESCE(MAX(attendclasscd), 0) + 1 \ 
				 FROM attendclasscd_cre \
			         WHERE year = New.year) \

