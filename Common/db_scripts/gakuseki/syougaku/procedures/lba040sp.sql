drop procedure lba040sp 

CREATE PROCEDURE LBA040SP \
( \
	 IN  IN_userid 		varchar(6) \
	,IN  IN_year		varchar(4) \
	,OUT OUT_RESULT		VARCHAR(10)		-- ������� \
	,OUT OUT_COUNT		integer			-- ������� \
	,OUT OUT_MESSAGE	VARCHAR(256)		-- ���顼��å����� \
) LANGUAGE SQL \
  BEGIN \
------------------------------------------------------------------------------ \
-- �����ѿ��������� \
------------------------------------------------------------------------------ \
-- ���顼���ơ�������� \
	DECLARE STS_SUCCESS	VARCHAR(1) default '0'; \
	DECLARE STS_LOCKED	VARCHAR(1) default '1'; \
	DECLARE STS_INVALID	VARCHAR(1) default '9'; \
	DECLARE SQLSTATE 	char(5) default '00000'; \
-- �����ѿ���� \
declare 	WK_schregno			varchar(4); \
declare		WK_OUT_COUNT			INTEGER; \
declare		WK_grade			varchar(1) default '0'; \
declare		WK_hr_class			varchar(2) default '00'; \
declare		WK_ROW				integer	default	0; \
declare		WK_COL				integer default 0; \
------------------------------------------------------------------------------ \
-- ���顼���� \
------------------------------------------------------------------------------ \
        BEGIN \
                DECLARE EXIT HANDLER FOR SQLEXCEPTION \
                -- ��å����� \
                IF SQLSTATE = '40001' THEN \
                        set OUT_RESULT  = STS_LOCKED; \
                        set OUT_COUNT   = 0; \
                        ROLLBACK; \
                ELSE \
                -- ����¾ \
                        set OUT_RESULT  = STS_INVALID; \
                        set OUT_COUNT   = 0; \
                        set OUT_MESSAGE = 'other error'; \
                        ROLLBACK; \
                END IF; \
        END; \
------------------------------------------------------------------------------ \
-- �������� \
------------------------------------------------------------------------------ \
	-- ������� \
	set OUT_RESULT	 = STS_INVALID; \
	set OUT_COUNT	 = 0; \
	set WK_schregno	 = '0'; \
	set WK_OUT_COUNT = 0; \
  -- �������� \
------------------------------------------------------------------------------ \
-- ��������FOR�롼�פ���� \
------------------------------------------------------------------------------ \
  FOR for1 AS \
         SELECT \
                 schregno \
                ,grade \
                ,hr_class \
                ,attendno \
        FROM schreg_regd_dat \
        WHERE year      = IN_year \
        ORDER BY grade, hr_class, attendno \
  DO \
	IF (WK_grade <> for1.grade) OR (WK_hr_class <> for1.hr_class) THEN \
		set WK_grade    = for1.grade; \
		set WK_hr_class = for1.hr_class; \
		set WK_ROW	= 1; \
		set WK_COL	= 1; \
 \		
	ELSEIF 	WK_COL >= 7 THEN \
		set WK_ROW = WK_ROW + 1; \
		set WK_COL = 1; \
	ELSE \
		set WK_COL = WK_COL + 1; \
	END IF; \
 \		 
	UPDATE schreg_regd_dat SET \
		seat_row	=	char(char(WK_ROW), 2) \
		,seat_col	=	char(char(WK_COL), 2) \
		,updated	=	current timestamp \
		WHERE year   = IN_year \
		AND schregno = for1.schregno; \
	SET WK_OUT_COUNT = WK_OUT_COUNT	+ 1; \
  END FOR; \
	set OUT_RESULT = STS_SUCCESS; \
	set OUT_COUNT  = WK_OUT_COUNT; \
	COMMIT; \
END

