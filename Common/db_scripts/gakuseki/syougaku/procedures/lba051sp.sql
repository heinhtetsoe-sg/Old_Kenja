drop procedure lba051sp 

CREATE PROCEDURE LBA051SP ( \
	 IN IN_userid	varchar(6) \
	,IN IN_year	VARCHAR(4) \
	,IN IN_grade	VARCHAR(2) \
	,OUT OUT_RESULT	VARCHAR(1)			-- 処理結果 \
	,OUT OUT_COUNT	integer \
) LANGUAGE SQL \
  BEGIN \
------------------------------------------------------------------------------ \
-- 内部変数宣言・定義 \
------------------------------------------------------------------------------ \
-- エラーステータス定義 \
DECLARE  SQLSTATE  char(5) default '00000'; \
DECLARE  at_error  smallint default 0; \
DECLARE  STS_SUCCESS	VARCHAR(1) default '0'; \
DECLARE  STS_LOCKED	VARCHAR(1) default '1'; \
DECLARE  STS_INVALID	VARCHAR(1) default '9'; \
-- 内部変数宣言 \
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
-- エラー処理 \
------------------------------------------------------------------------------ \
-- ロック失敗 \
	DECLARE EXIT HANDLER FOR SQLEXCEPTION \	
        IF SQLSTATE = '40001' THEN \
                SET OUT_RESULT   = STS_LOCKED; \
                SET OUT_COUNT    = 0; \
                ROLLBACK; \
        ELSE \
        -- その他 \
                SET OUT_RESULT   = STS_INVALID; \
                SET OUT_COUNT    = 0; \
                ROLLBACK; \
        END IF; \
------------------------------------------------------------------------------ \
-- 処理開始 \
------------------------------------------------------------------------------ \
	-- 初期処理 \
	set OUT_RESULT	= STS_INVALID; \
	set OUT_COUNT	= 0; \
 \
	-- 編集テーブルのロック \
	LOCK TABLE progress_dat IN EXCLUSIVE MODE; \
 \
	-- 保留既定値の取得 \
	SELECT 	ctrl_value1 INTO WK_remain_val FROM control_mst  \
	 WHERE 	ctrl_cd1   = 'B201' \
	   AND 	ctrl_cd2   = '1201'; \
 \
	-- 卒業月日の取得 \
	SELECT 	ctrl_char3 INTO WK_graduate_mmdd \
	  FROM 	control_mst \
	 WHERE 	ctrl_cd1   = 'B201' \
	   AND 	ctrl_cd2   = '1001'; \
 \
	set WK_graduate_date = DATE(CHAR(CHAR(integer(IN_year) + 1), 4) || '-' || WK_graduate_mmdd); \
 \
	-- 学年進行データを削除(指定した学年のみ) \
	DELETE FROM progress_dat D01 \
	 WHERE EXISTS (SELECT 'X' FROM schreg_regd_dat W1 \
	                WHERE W1.year     = IN_year \
	                  AND W1.grade    = IN_grade \
	                  AND W1.schregno = D01.schregno); \
----------------------------------------------------------------------------------------- \
-- カーソルFORループ
----------------------------------------------------------------------------------------- \
 	-- 学年進行データ作成処理 \
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
        -- 異動区分5:転学/8:退学の生徒は事前に除外する \
 \	 
		---- 学年進行データの作成対象外データ判定 \
		BEGIN  \
			DECLARE EXIT HANDLER FOR NOT FOUND \
			SELECT transfer_sdate INTO WK_trans_sdate \
			  FROM schreg_transfer_dat \
			 WHERE transfercd = '9' \
			   AND schregno = for1.schregno; \
			SET WK_RECCNT = 1; \
		END; \
 \
		-- 卒業の異動データが無いかまたは有っても正常な卒業の異動データなら対象者 \
		-- ※卒業年月日が通常と違うのは先に卒業処理をしてしまった生徒なので対象外 \
 \		
		IF WK_RECCNT = 0 OR WK_trans_sdate = WK_graduate_date THEN \
			---- 保留単位数の取得 \
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
  			---- 移行区分の編集 \
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
	-- 実施日付の更新 \
	UPDATE control_mst \
	   SET ctrl_date3  = SYSDATE() \
	 WHERE ctrl_cd1    = 'B201' \
	   AND ctrl_cd2    = '1001'; \
 \
	-- 正常終了 \
	set OUT_RESULT	= STS_SUCCESS; \
	COMMIT; \
END
