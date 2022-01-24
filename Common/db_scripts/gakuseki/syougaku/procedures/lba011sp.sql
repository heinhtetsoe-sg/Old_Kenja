
drop procedure lba011sp

create procedure lba011sp \
( \
	 IN  IN_userid	varchar(6) \
	,IN  IN_year	varchar(4) \
	,OUT OUT_RESULT	VARCHAR(10)		-- ������\
	,OUT OUT_COUNT	INTEGER			-- ������� \
)  \
\
------------------------------------------------------------------------------ \
-- �����ѿ��������� \
------------------------------------------------------------------------------ \
 \
 LANGUAGE SQL \
BEGIN \
DECLARE SQLSTATE char(5) default '00000'; \
DECLARE WK_ctrl_char1	varchar(20); \
DECLARE  at_error	smallint default 0; \
DECLARE  STS_SUCCESS	VARCHAR(1) default '0'; \
DECLARE  STS_LOCKED	VARCHAR(1) default '1'; \
DECLARE  STS_INVALID	VARCHAR(1) default '9'; \
DECLARE  EXIT HANDLER FOR SQLEXCEPTION \
BEGIN \
	IF SQLSTATE='40001' THEN \
		SET at_error = 1; \
	ELSE \
		SET at_error = 2; \
	END IF; \
------------------------------------------------------------------------------ \
-- ���顼���� \
------------------------------------------------------------------------------ \
        -- ��å����� \
        IF at_error = 1 THEN \
--              DBMS_OUTPUT.PUT_LINE(SQLCODE || SQLERRM);       -- �ǥХå� \
                SET OUT_RESULT  = STS_LOCKED; \
                SET OUT_COUNT   = 0; \
                ROLLBACK; \
        END IF; \
 \
        -- ����¾ \
        IF at_error = 2 THEN \
--              DBMS_OUTPUT.PUT_LINE(SQLCODE || SQLERRM);       -- �ǥХå� \
                SET OUT_RESULT  = STS_INVALID; \
                SET OUT_COUNT   = 0; \
                ROLLBACK; \
        END IF; \
 \
END; \
------------------------------------------------------------------------------ \
-- �������� \
------------------------------------------------------------------------------ \
	-- ������� \
	SET OUT_RESULT	= STS_INVALID; \
	SET OUT_COUNT	= 0; \
 \
	-- �Խ��ơ��֥�Υ�å� \
	LOCK TABLE class_formation_dat IN EXCLUSIVE MODE; \
 \
	-- �������ܹԥǡ�������(����ǯ�٤ο������Τߡ���αǯ���Ͻ���) \
	DELETE FROM class_formation_dat WHERE year = IN_year AND grade = '1' AND remaingrade_flg = '0'; \
\
	-- ���ػִ�Ը����鿷�����ܹԥǡ�������� \
	INSERT INTO class_formation_dat( \
		 examineeno \
		,year \
		,grade,hr_class,attendno \
		,t_cd1,t_cd2,t_cd3 \
		,seat_row,seat_col \
		,coursecode \
		,remaingrade_flg \
		,old_hr_class,old_attendno \
		,coursecd \
		,majorcd \
		,updated \
	) SELECT \
		 A01.examineeno \
		,IN_year \
		,'1',cast(NULL as varchar(2)),cast(NULL as varchar(2)) \
		,'000000','000000','000000' \
		,cast(NULL as varchar(2)),cast(NULL as varchar(2)) \
		,cast(NULL as varchar(3)) \
		,'0' \
		,cast(NULL as varchar(2)), cast(NULL as varchar(2)) \
                ,(CASE A01.pass_failcd \
		 WHEN '1' \
		 THEN A01.d1_coursecd \
		 WHEN '4' \
		 THEN A01.d1_coursecd \
		 WHEN '5' \
		 THEN A01.d1_coursecd \
		 WHEN '3' \
		 THEN A01.d2_coursecd \
		 ELSE NULL END) \
		 as coursecd \ 
                ,(CASE A01.pass_failcd \
		 WHEN '1' \
		 THEN A01.d1_majorcd \
		 WHEN '4' \
		 THEN A01.d1_majorcd \
		 WHEN '5' \
		 THEN A01.d1_majorcd \
		 WHEN '3' \
		 THEN A01.d2_majorcd \
		 ELSE NULL END) \
		 as majorcd \
		,SYSDATE() \
	FROM applicant_mst	A01 \
	WHERE A01.enteryear = IN_year \
	  AND A01.acceptcd    IN ('1', '3') \
	  AND A01.pass_failcd  IN ('1', '3', '4', '5') \
	  AND A01.entercd    = '0'; \
 \
	GET DIAGNOSTICS OUT_COUNT = ROW_COUNT;	-- ����������å� \
 \	
	-- �������ܹ��������μ»����չ��� \
	UPDATE control_mst \
	SET ctrl_date1  = SYSDATE() \
	WHERE ctrl_cd1	= 'B201' \
	  AND ctrl_cd2  = '1002'; \
\
	-- ���ｪλ\
	SET OUT_RESULT	= STS_SUCCESS; \
	COMMIT; \
 \
END 

