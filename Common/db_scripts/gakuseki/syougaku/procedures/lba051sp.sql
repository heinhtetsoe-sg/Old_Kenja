drop procedure lba051sp 

CREATE PROCEDURE LBA051SP ( \
	 IN IN_userid	varchar(6) \
	,IN IN_year	VARCHAR(4) \
	,IN IN_grade	VARCHAR(2) \
	,OUT OUT_RESULT	VARCHAR(1)			-- ������� \
	,OUT OUT_COUNT	integer \
) LANGUAGE SQL \
  BEGIN \
------------------------------------------------------------------------------ \
-- �����ѿ��������� \
------------------------------------------------------------------------------ \
-- ���顼���ơ�������� \
DECLARE  SQLSTATE  char(5) default '00000'; \
DECLARE  at_error  smallint default 0; \
DECLARE  STS_SUCCESS	VARCHAR(1) default '0'; \
DECLARE  STS_LOCKED	VARCHAR(1) default '1'; \
DECLARE  STS_INVALID	VARCHAR(1) default '9'; \
-- �����ѿ���� \
declare WK_year			varchar(20); \
declare WK_remain_val		smallint; \
declare WK_graduate_mmdd	varchar(20); \
declare WK_graduate_date	date; \
declare WK_trans_sdate		date default NULL; \
 \
declare WK_promote		varchar(1)	default '0'; \
declare WK_graduate		varchar(1)	default '1'; \
declare WK_remain		varchar(1)	default '2'; \
 \
declare WK_remaincredits	smallint	     	default  0; \
declare WK_shiftcd		varchar(1)	default  NULL; \
declare WK_outputnum		integer default 0; \
declare WK_RECCNT		integer default 0; \
------------------------------------------------------------------------------ \
-- ���顼���� \
------------------------------------------------------------------------------ \
-- ��å����� \
	DECLARE EXIT HANDLER FOR SQLEXCEPTION \	
        IF SQLSTATE = '40001' THEN \
                SET OUT_RESULT   = STS_LOCKED; \
                SET OUT_COUNT    = 0; \
                ROLLBACK; \
        ELSE \
        -- ����¾ \
                SET OUT_RESULT   = STS_INVALID; \
                SET OUT_COUNT    = 0; \
                ROLLBACK; \
        END IF; \
------------------------------------------------------------------------------ \
-- �������� \
------------------------------------------------------------------------------ \
	-- ������� \
	set OUT_RESULT	= STS_INVALID; \
	set OUT_COUNT	= 0; \
 \
	-- �Խ��ơ��֥�Υ�å� \
	LOCK TABLE progress_dat IN EXCLUSIVE MODE; \
 \
	-- ��α�����ͤμ��� \
	SELECT 	ctrl_value1 INTO WK_remain_val FROM control_mst  \
	 WHERE 	ctrl_cd1   = 'B201' \
	   AND 	ctrl_cd2   = '1201'; \
 \
	-- ´�ȷ����μ��� \
	SELECT 	ctrl_char3 INTO WK_graduate_mmdd \
	  FROM 	control_mst \
	 WHERE 	ctrl_cd1   = 'B201' \
	   AND 	ctrl_cd2   = '1001'; \
 \
	set WK_graduate_date = DATE(CHAR(CHAR(integer(IN_year) + 1), 4) || '-' || WK_graduate_mmdd); \
 \
	-- ��ǯ�ʹԥǡ�������(���ꤷ����ǯ�Τ�) \
	DELETE FROM progress_dat D01 \
	 WHERE EXISTS (SELECT 'X' FROM schreg_regd_dat W1 \
	                WHERE W1.year     = IN_year \
	                  AND W1.grade    = IN_grade \
	                  AND W1.schregno = D01.schregno); \
----------------------------------------------------------------------------------------- \
-- ��������FOR�롼��
----------------------------------------------------------------------------------------- \
 	-- ��ǯ�ʹԥǡ����������� \
	FOR for1 as \
        SELECT D01.schregno \
              ,D01.year \
              ,D01.grade \
              ,M01.coursecd \
              ,M01.majorcd \
              ,D01.coursecode \
          FROM schreg_regd_dat  D01 \
              ,schreg_base_mst  M01 \
         WHERE D01.year     = IN_year \
           AND D01.grade    = IN_grade \
           AND M01.schregno = D01.schregno \
           AND NOT EXISTS (SELECT 'X' FROM schreg_transfer_dat W1 \
                            WHERE W1.transfercd IN ('5','8') \
                              AND W1.schregno = D01.schregno) \
 	DO \
        -- ��ư��ʬ5:ž��/8:��ؤ����̤ϻ����˽������� \
 \	 
		---- ��ǯ�ʹԥǡ����κ����оݳ��ǡ���Ƚ�� \
		BEGIN  \
			DECLARE EXIT HANDLER FOR NOT FOUND \
			SELECT transfer_sdate INTO WK_trans_sdate \
			  FROM schreg_transfer_dat \
			 WHERE transfercd = '9' \
			   AND schregno = for1.schregno; \
			SET WK_RECCNT = 1; \
		END; \
 \
		-- ´�Ȥΰ�ư�ǡ�����̵�����ޤ���ͭ�äƤ������´�Ȥΰ�ư�ǡ����ʤ��оݼ� \
		-- ��´��ǯ�������̾�Ȱ㤦�Τ����´�Ƚ����򤷤Ƥ��ޤä����̤ʤΤ��оݳ� \
 \		
		IF WK_RECCNT = 0 OR WK_trans_sdate = WK_graduate_date THEN \
			---- ��αñ�̿��μ��� \
			SELECT COALESCE(SUM(M01.credit),0) INTO WK_remaincredits \
			  FROM credit_mst     M01 \
			      ,record_dat     D01 \
			 WHERE D01.year	      = IN_year \
			   AND D01.semester   = '4' \
			   AND D01.schregno   = for1.schregno \
			   AND D01.grades     = 1 \
			   AND M01.grade      = for1.grade \
			   AND M01.coursecd   = for1.coursecd \
			   AND M01.majorcd    = for1.majorcd \
			   AND M01.course     = for1.coursecode  \
			   AND M01.subclasscd = D01.subclasscd \
			   AND M01.classcd    = D01.classcd; \
 \
  			---- �ܹԶ�ʬ���Խ� \
			IF WK_remaincredits >= WK_remain_val THEN \
				set WK_shiftcd	= WK_remain; \
			ELSEIF for1.grade        = '3'     THEN \
				set WK_shiftcd	= WK_graduate; \
			ELSE \
				set WK_shiftcd	= WK_promote; \
			END IF; \
 \
			INSERT INTO progress_dat \
				(progressyear, schregno, shiftcd, remaincredits, updated) \
			VALUES 	(IN_year, for1.schregno, WK_shiftcd, WK_remaincredits, SYSDATE() ); \
 \
			SET WK_outputnum = WK_outputnum + 1; \
 \
		END IF; \
 	END FOR; \
	SET OUT_COUNT	= WK_outputnum; \
 \
	-- �»����դι��� \
	UPDATE control_mst \
	   SET ctrl_date3  = SYSDATE() \
	 WHERE ctrl_cd1    = 'B201' \
	   AND ctrl_cd2    = '1001'; \
 \
	-- ���ｪλ \
	set OUT_RESULT	= STS_SUCCESS; \
	COMMIT; \
END
