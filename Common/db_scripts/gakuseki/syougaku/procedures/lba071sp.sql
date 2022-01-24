drop procedure LBA071SP 

CREATE PROCEDURE LBA071SP \
( \
     in IN_userid 	varchar(6), \
     in IN_year    	varchar(4), \
     in IN_grade	varchar(1), \
     OUT OUT_RESULT  	varchar(10),            -- ������� \
     OUT OUT_COUNT   	integer              -- ������� \
) LANGUAGE SQL \ 
  BEGIN \
------------------------------------------------------------------------------ \
-- �����ѿ��������� \
------------------------------------------------------------------------------ \
-- ���顼���ơ�������� \
DECLARE at_error   smallint default 0; \
DECLARE SQLSTATE   char(5) default '00000'; \
DECLARE STS_SUCCESS VARCHAR(1) default '0'; \
DECLARE STS_LOCKED  VARCHAR(1) default '1'; \
DECLARE STS_INVALID VARCHAR(1) default '9'; \
------------------------------------------------------------------------------ \
-- �����ѿ���� \
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
-- ����������� \
-- �������˥��饹�����ǡ����Τ��ߥǡ������������,��������Ǥ� \
--   ���饹�����ǡ�����¸�ߤ��ʤ��ǡ���(��ǯ�ʹԥǡ���)�Τߤ��оݤȤ�\
--   ´�����˴ؤ��Ƥ���Ģ�ֹ�򿶤��,��ǯ���ȡܽ����ֹ�ǥ����Ȥ��� \
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
-- ���顼���� \
------------------------------------------------------------------------------ \
    BEGIN \
        DECLARE CONTINUE HANDLER FOR NOT FOUND SET at_error = 1; \
        DECLARE EXIT HANDLER FOR SQLEXCEPTION \
        IF SQLSTATE = '40001' THEN \
        -- ��å����� \
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
     -- ������� \
     set OUT_RESULT = STS_INVALID; \
     set OUT_COUNT  = 0; \
     -- �Խ��ơ��֥�Υ�å� \
     LOCK TABLE progress_dat IN EXCLUSIVE MODE; \
 \
     -- ´�ȷ����μ��� \
     SELECT ctrl_char3 INTO WK_graduateday \
       FROM control_mst \
      WHERE ctrl_cd1  = 'B201' \
        AND ctrl_cd2  = '1001'; \
 \
     set WK_graduatedate = DATE(CHAR(char(integer(IN_year) + 1), 4) || '-' || WK_graduateday); \
     set WK_nextyear     = CHAR(char(integer(IN_year) + 1), 4); \
 \
     -- ���饹�����ǡ����Τ��ߥǡ���(��ǯ�ʹԥǡ�����¸�ߤ��ʤ��ǡ���)�κ�� \
     DELETE FROM class_formation_dat T1 \
      WHERE T1.year     = WK_nextyear \
        AND T1.examineeno IS NULL   -- ���������оݳ� \
        AND NOT EXISTS (SELECT 'X' FROM progress_dat W1 \
                         WHERE W1.progressyear = IN_year \
                           AND W1.schregno  = T1.schregno); \
 \
     -- ���饹�����ǡ����Τ��ߥǡ���(��ǯ�ʹԥǡ�������α�ʤΤ˼�ǯ�٤ǿʵ��¸�ߤ���ǡ���)�κ�� \
     DELETE FROM class_formation_dat T1 \ 
      WHERE T1.year     = WK_nextyear \
        AND T1.examineeno IS NULL   -- ���������оݳ� \
        AND T1.grade     =char(integer(IN_grade) + 1) \
        AND EXISTS (SELECT 'X' FROM progress_dat W1, schreg_regd_dat W2 \
                     WHERE W1.progressyear = IN_year \
                       AND W1.shiftcd         = '2'     --0:�ʵ顿1:´�ȡ�2:��α \
                       AND W1.schregno        = T1.schregno \
                       AND W2.year            = W1.progressyear \
                       AND W2.grade           = IN_grade \
                       AND W2.schregno        = W1.schregno); \
 \
     -- ���饹�����ǡ����Τ��ߥǡ���(��ǯ�ʹԥǡ����ǿʵ�ʤΤ˼�ǯ�٤�αǯ��¸�ߤ���ǡ���)�κ�� \
     DELETE FROM class_formation_dat T1 \
      WHERE T1.year    = WK_nextyear \
        AND T1.examineeno IS NULL   -- ���������оݳ� \
        AND T1.grade     = IN_grade \
        AND EXISTS (SELECT 'X' FROM progress_dat W1, schreg_regd_dat W2 \
                     WHERE W1.progressyear = IN_year \
                       AND W1.shiftcd      = '0'     --0:�ʵ顿1:´�ȡ�2:��α \
                       AND W1.schregno     = T1.schregno \
                       AND W2.year         = W1.progressyear \
                       AND W2.grade        = IN_grade \
                       AND W2.schregno     = W1.schregno); \
     -- 3ǯ���Τߤν��� \
     IF IN_grade  = '3' THEN \
          -- ´��������Ģ�ֹ楯�ꥢ\
          UPDATE schreg_base_mst T1 SET \
                 T1.graduateno = NULL \
           WHERE EXISTS (SELECT 'X' FROM schreg_transfer_dat W1 \
                          WHERE W1.transfercd  = '9' \
                            AND W1.transfer_sdate = WK_graduatedate \
                            AND W1.schregno    = T1.schregno); \
 \
          -- ´�����γ��Ұ�ư�ǡ����κ�� \
          -- ������(�̤����դ�)´�Ƚ���������Ƥ������̤������ǽ��������٤�´�����դ���о��˻���!! \
          DELETE FROM schreg_transfer_dat \ 
           WHERE transfer_sdate = WK_graduatedate \
             AND transfercd  = '9'; \
     END IF; \
     -- ���Ҵ��åޥ��������Ұ�ư�ǡ��� �ɲá���������(´�����Τ�)  \
     -- �߹����ϥ��饹�����ǡ����ؤ��ɲ� \
     OPEN CUR_progres; \
     loop1: LOOP \
          FETCH CUR_progres INTO v_progressyear, v_schregno, v_shiftcd, v_grade, v_hr_class, v_attendno; \
          IF at_error = 1 THEN \
               LEAVE loop1; \
          END IF; \
 \
          -- ´�� \
          IF v_shiftcd = WK_graduate THEN \
               -- ���ߤ�´������Ģ�ֹ�κ����ͤ���� \
               SELECT char(MAX(INTEGER(graduateno))) INTO WK_no FROM schreg_base_mst; \
               -- ´��ǯ��������Ģ�ֹ� \
               UPDATE schreg_base_mst \
                  SET graduateddate = WK_graduatedate \
                     ,graduateno   = char(integer(WK_no) + 1) \
                     ,updated      = SYSDATE() \
                WHERE schregno     = v_schregno; \
               -- ��ư�ǡ����κ��� \
               INSERT INTO schreg_transfer_dat \
                     (schregno, transfercd, transfer_sdate, transferreason, updated) \
               VALUES \
                     (v_schregno,'9',WK_graduatedate,'´��',SYSDATE()); \
 \
          -- �ʵ顦��α \
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
     SET OUT_COUNT   = WK_outputs;         -- ����������å� \
 \
     -- ����ȥ���ޥ��������չ��� \
     UPDATE control_mst \
        SET ctrl_date2  = SYSDATE() \
      WHERE ctrl_cd1  = 'B201' \
        AND ctrl_cd2  = '1001'; \
 \
     -- ���ｪλ \
     SET OUT_RESULT  = STS_SUCCESS; \
     COMMIT; \
END
