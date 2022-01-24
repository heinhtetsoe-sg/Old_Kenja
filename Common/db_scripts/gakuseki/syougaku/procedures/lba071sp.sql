drop procedure LBA071SP 

CREATE PROCEDURE LBA071SP \
( \
     in IN_userid 	varchar(6), \
     in IN_year    	varchar(4), \
     in IN_grade	varchar(1), \
     OUT OUT_RESULT  	varchar(10),            -- 処理結果 \
     OUT OUT_COUNT   	integer              -- 処理件数 \
) LANGUAGE SQL \ 
  BEGIN \
------------------------------------------------------------------------------ \
-- 内部変数宣言・定義 \
------------------------------------------------------------------------------ \
-- エラーステータス定義 \
DECLARE at_error   smallint default 0; \
DECLARE SQLSTATE   char(5) default '00000'; \
DECLARE STS_SUCCESS VARCHAR(1) default '0'; \
DECLARE STS_LOCKED  VARCHAR(1) default '1'; \
DECLARE STS_INVALID VARCHAR(1) default '9'; \
------------------------------------------------------------------------------ \
-- 内部変数宣言 \
DECLARE WK_promote    varchar(1) default '0'; \
DECLARE WK_graduate   varchar(1) default '1'; \
DECLARE WK_remain     varchar(1) default '2'; \
 \
DECLARE WK_graduateday    	varchar(20); \
DECLARE WK_graduatedate    	date default NULL; \
DECLARE WK_nextyear   		varchar(20); \
DECLARE WK_no         		varchar(5) default '0'; \
DECLARE WK_outputs    		integer default 5; \
DECLARE WK_remain_flg         	varchar(1) default NULL; \
-- variables for cursor \
        DECLARE v_progressyear  varchar(4); \
        DECLARE v_schregno      varchar(6); \
        DECLARE v_shiftcd       varchar(1); \
        DECLARE v_grade         varchar(1); \
        DECLARE v_hr_class      varchar(2); \
        DECLARE v_attendno      varchar(2); \
------------------------------------------------------------------------------ \
-- カーソル宣言 \
-- ※事前にクラス編成データのごみデータを削除する為,カーソルでは \
--   クラス編成データに存在しないデータ(学年進行データ)のみを対象とす\
--   卒業生に関しては台帳番号を振る為,学年＋組＋出席番号でソートする \
------------------------------------------------------------------------------ \
DECLARE CUR_progres  CURSOR FOR \
 \
    SELECT T1.progressyear \
          ,T1.schregno \
          ,T1.shiftcd \
          ,T2.grade \
          ,T2.hr_class \
          ,T2.attendno \
      FROM progress_dat T1 \
          ,schreg_regd_dat  T2 \
     WHERE T1.progressyear  =  IN_year \
       AND T2.year              =  T1.progressyear \
       AND T2.schregno          =  T1.schregno \
       AND EXISTS (SELECT 'X' FROM schreg_regd_dat W1 \
                    WHERE W1.year      = IN_year \
                      AND W1.grade     = IN_grade \
                      AND W1.schregno  = T1.schregno) \
       AND NOT EXISTS (SELECT 'X' FROM class_formation_dat W2 \
                        WHERE W2.year     = char(integer(IN_year) + 1) \
                          AND W2.schregno = T1.schregno ) \
     ORDER BY T2.grade \
             ,T2.hr_class \
             ,T2.attendno; \
------------------------------------------------------------------------------ \
-- エラー処理 \
------------------------------------------------------------------------------ \
    BEGIN \
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET at_error = 1; \
        DECLARE EXIT HANDLER FOR SQLEXCEPTION \
        IF SQLSTATE = '40001' THEN \
        -- ロック失敗 \
                SET OUT_RESULT  = STS_LOCKED; \
                SET OUT_COUNT  = 0; \
                ROLLBACK; \
        ELSE \
                SET OUT_RESULT = STS_INVALID; \
                SET OUT_COUNT   = 0; \
                ROLLBACK; \
        END IF; \
    END; \
------------------------------------------------------------------------------ \
------------------------------------------------------------------------------ \
---------- MAIN PROC ---------- \
------------------------------------------------------------------------------ \
     -- 初期処理 \
     set OUT_RESULT = STS_INVALID; \
     set OUT_COUNT  = 0; \
     -- 編集テーブルのロック \
     LOCK TABLE progress_dat IN EXCLUSIVE MODE; \
 \
     -- 卒業月日の取得 \
     SELECT ctrl_char3 INTO WK_graduateday \
       FROM control_mst \
      WHERE ctrl_cd1  = 'B201' \
        AND ctrl_cd2  = '1001'; \
 \
     set WK_graduatedate = DATE(CHAR(char(integer(IN_year) + 1), 4) || '-' || WK_graduateday); \
     set WK_nextyear     = CHAR(char(integer(IN_year) + 1), 4); \
 \
     -- クラス編成データのごみデータ(学年進行データに存在しないデータ)の削除 \
     DELETE FROM class_formation_dat T1 \
      WHERE T1.year     = WK_nextyear \
        AND T1.examineeno IS NULL   -- 新入生は対象外 \
        AND NOT EXISTS (SELECT 'X' FROM progress_dat W1 \
                         WHERE W1.progressyear = IN_year \
                           AND W1.schregno  = T1.schregno); \
 \
     -- クラス編成データのごみデータ(学年進行データで保留なのに次年度で進級に存在するデータ)の削除 \
     DELETE FROM class_formation_dat T1 \ 
      WHERE T1.year     = WK_nextyear \
        AND T1.examineeno IS NULL   -- 新入生は対象外 \
        AND T1.grade     =char(integer(IN_grade) + 1) \
        AND EXISTS (SELECT 'X' FROM progress_dat W1, schreg_regd_dat W2 \
                     WHERE W1.progressyear = IN_year \
                       AND W1.shiftcd         = '2'     --0:進級／1:卒業／2:保留 \
                       AND W1.schregno        = T1.schregno \
                       AND W2.year            = W1.progressyear \
                       AND W2.grade           = IN_grade \
                       AND W2.schregno        = W1.schregno); \
 \
     -- クラス編成データのごみデータ(学年進行データで進級なのに次年度で留年に存在するデータ)の削除 \
     DELETE FROM class_formation_dat T1 \
      WHERE T1.year    = WK_nextyear \
        AND T1.examineeno IS NULL   -- 新入生は対象外 \
        AND T1.grade     = IN_grade \
        AND EXISTS (SELECT 'X' FROM progress_dat W1, schreg_regd_dat W2 \
                     WHERE W1.progressyear = IN_year \
                       AND W1.shiftcd      = '0'     --0:進級／1:卒業／2:保留 \
                       AND W1.schregno     = T1.schregno \
                       AND W2.year         = W1.progressyear \
                       AND W2.grade        = IN_grade \
                       AND W2.schregno     = W1.schregno); \
     -- 3年生のみの処理 \
     IF IN_grade  = '3' THEN \
          -- 卒業生の台帳番号クリア\
          UPDATE schreg_base_mst T1 SET \
                 T1.graduateno = NULL \
           WHERE EXISTS (SELECT 'X' FROM schreg_transfer_dat W1 \
                          WHERE W1.transfercd  = '9' \
                            AND W1.transfer_sdate = WK_graduatedate \
                            AND W1.schregno    = T1.schregno); \
 \
          -- 卒業生の学籍異動データの削除 \
          -- ※既に(別の日付で)卒業処理がされている生徒がいる可能性がある為に卒業日付も抽出条件に指定!! \
          DELETE FROM schreg_transfer_dat \ 
           WHERE transfer_sdate = WK_graduatedate \
             AND transfercd  = '9'; \
     END IF; \
     -- 学籍基礎マスタ／学籍異動データ 追加・更新処理(卒業生のみ)  \
     -- 在校生はクラス編成データへの追加 \
     OPEN CUR_progres; \
     loop1: LOOP \
          FETCH CUR_progres INTO v_progressyear, v_schregno, v_shiftcd, v_grade, v_hr_class, v_attendno; \
          IF at_error = 1 THEN \
               LEAVE loop1; \
          END IF; \
 \
          -- 卒業 \
          IF v_shiftcd = WK_graduate THEN \
               -- 現在の卒業生台帳番号の最大値を取得 \
               SELECT char(MAX(INTEGER(graduateno))) INTO WK_no FROM schreg_base_mst; \
               -- 卒業年月日、台帳番号 \
               UPDATE schreg_base_mst \
                  SET graduateddate = WK_graduatedate \
                     ,graduateno   = char(integer(WK_no) + 1) \
                     ,updated      = SYSDATE() \
                WHERE schregno     = v_schregno; \
               -- 異動データの作成 \
               INSERT INTO schreg_transfer_dat \
                     (schregno, transfercd, transfer_sdate, transferreason, updated) \
               VALUES \
                     (v_schregno,'9',WK_graduatedate,'卒業',SYSDATE()); \
 \
          -- 進級・保留 \
          ELSE \
               IF v_shiftcd = WK_promote THEN \
                    SET WK_remain_flg = '0'; \
               ELSEIF v_shiftcd = WK_remain THEN \
                    SET WK_remain_flg  = '1'; \
               END IF; \
 \
               INSERT INTO class_formation_dat( \
                           schregno \
                          ,year \
                          ,grade, hr_class, attendno \
                          ,t_cd1, t_cd2, t_cd3 \
                          ,seat_row, seat_col \
                          ,coursecode \
                          ,remaingrade_flg \
                          ,old_hr_class, old_attendno \
                          ,coursecd \
                          ,majorcd \
                          ,updated) \
                    SELECT v_schregno \
                          ,WK_nextyear \
                          ,char(integer(T1.grade) + (1 - integer(WK_remain_flg))), CAST(NULL as varchar(2)), CAST(NULL as varchar(2)) \
                          ,'000000','000000','000000' \
                          ,CAST(NULL as varchar(2)), CAST(NULL as varchar(2)) \
                          ,CAST(NULL as varchar(1)) \
                          ,WK_remain_flg \
                          ,T1.hr_class, T1.attendno \
                          ,T2.coursecd \
                          ,T2.majorcd \
                          ,SYSDATE() \
                      FROM schreg_regd_dat T1 \
                          ,schreg_base_mst T2 \
                     WHERE T1.schregno = v_schregno \
                       AND T1.year     = v_progressyear \
                       AND T1.schregno = T2.schregno; \
\
          END IF; \
 \
          SET WK_outputs = WK_outputs + 1; \
 \
     END LOOP loop1; \
 \
     CLOSE CUR_progres; \
     SET OUT_COUNT   = WK_outputs;         -- 処理件数セット \
 \
     -- コントロールマスタの日付更新 \
     UPDATE control_mst \
        SET ctrl_date2  = SYSDATE() \
      WHERE ctrl_cd1  = 'B201' \
        AND ctrl_cd2  = '1001'; \
 \
     -- 正常終了 \
     SET OUT_RESULT  = STS_SUCCESS; \
     COMMIT; \
END
