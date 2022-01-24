<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd125bQuery.inc 56581 2017-10-22 12:37:16Z maeshiro $

class knjd125bquery extends Query
{

    //編集可能学期の判別
    public function selectContolCodeQuery()
    {
        $query  = " SELECT ";
        $query .= "    CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     CONTROL_FLG = '1' ";
        return $query;
    }

    //時間割講座テストより試験日を抽出
    public function selectExecuteDateQuery($model, $semester, $testkindcd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "    EXECUTEDATE ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     TESTKINDCD = '".$testkindcd."' AND ";
        $query .= "     CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= " ORDER BY EXECUTEDATE ";
        return $query;
    }

    //科目データの取得
    public function selectSubclassQuery($gen_ed)
    {
        $query  = " SELECT ";
        $query .= "     T3.subclasscd, ";
        $query .= "     T3.subclassname ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     subclass_mst T3, ";
        $query .= "     chair_stf_dat T4 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T1.year       = T4.year AND ";
        $query .= "     T1.semester   = T4.semester AND ";
        $query .= "     T1.chaircd    = T4.chaircd AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "     T3.subclasscd NOT LIKE '9%' AND ";
        $query .= "     T3.subclasscd not in (SELECT NAME1 FROM V_NAME_MST ";
        $query .= "                            WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 in('D002','D003')) AND ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= " t4.STAFFCD = '".STAFFCD."' AND ";
        }
        $query .= "     T3.subclasscd = T1.subclasscd ";
        $query .= " GROUP BY ";
        $query .= "     T3.subclasscd, ";
        $query .= "     T3.subclassname ";
        $query .= " ORDER BY ";
        $query .= "     T3.subclasscd ";
        return $query;
    }

    //講座データの取得
    public function selectChairQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.chaircd, ";
        $query .= "     T1.chairname ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     chair_stf_dat T3 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".CTRL_SEMESTER."' AND ";
        $query .= "     T2.year       = T1.year AND ";
        $query .= "     T2.semester   = T1.semester AND ";
        $query .= "     T2.chaircd    = T1.chaircd AND ";
        $query .= "     T1.year       = T3.year AND ";
        $query .= "     T1.semester   = T3.semester AND ";
        $query .= "     T1.chaircd    = T3.chaircd AND ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= " t3.STAFFCD = '".STAFFCD."' AND ";
        }
        $query .= "     T1.subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.chaircd ";
        return $query;
    }

    //遅刻何回で欠課とするかの指数取得
    public function getScAbsentCov()
    {
        return "SELECT absent_cov,absent_cov_late FROM school_mst WHERE year = '".CTRL_YEAR."'";
    }

    //累積最大日取得
    public function GetMax($subclasscd)
    {
        $query  = "SELECT DISTINCT year ";
        $query .= "              ,month ";
        $query .= "              ,CASE MONTH WHEN '01' THEN CHAR(SMALLINT(month) + 12)  ";
        $query .= "                          WHEN '02' THEN CHAR(SMALLINT(month) + 12)  ";
        $query .= "                          WHEN '03' THEN CHAR(SMALLINT(month) + 12) ELSE month END AS tmp_month ";
        $query .= "              ,semester ";
        $query .= "              ,appointed_day ";
        $query .= "         FROM attend_subclass_dat ";
        $query .= "        WHERE COPYCD     = '0' ";
        $query .= "          AND year       = '".CTRL_YEAR."' ";
        $query .= "          AND subclasscd = '".$subclasscd."' ";
        $query .= "          AND semester   = (SELECT MAX(semester) FROM attend_subclass_dat ";
        $query .= "                             WHERE COPYCD     = '0' ";
        $query .= "                               AND year       = '".CTRL_YEAR."' ";
        $query .= "                               AND subclasscd = '".$subclasscd."') ";
        $query .= "ORDER BY tmp_month DESC ";
        return $query;
    }

    //評定マスタ
    public function getAssessMark()
    {
        $query  = "SELECT  ASSESSCD, ASSESSLEVEL, ASSESSMARK ";
        $query .= "FROM    ASSESS_MST ";
        $query .= "WHERE   ASSESSCD in('2','3') ";
        $query .= "ORDER BY  ASSESSCD, ASSESSLEVEL ";
        return $query;
    }

    //合併先科目の単位固定／加算フラグ
    public function getCalculateCreditFlg($subclasscd)
    {
        $query  = "SELECT DISTINCT CALCULATE_CREDIT_FLG ";
        $query .= "FROM   SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND  COMBINED_SUBCLASSCD = '".$subclasscd."' ";
        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $execute_date)
    {
        //定期考査の出欠の表１
        $query  = " WITH TEST_ATTEND_A AS ( ";
        $query .= "     SELECT  '1' AS SEMES, '01' AS TESTKINDCD, T2.SCHREGNO, ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '1' AND T1.TESTKINDCD = '01' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "     UNION  ";
        $query .= "     SELECT  '1' AS SEMES, '02' AS TESTKINDCD, T2.SCHREGNO, ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '1' AND T1.TESTKINDCD = '02' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "     UNION  ";
        $query .= "     SELECT  '2' AS SEMES, '01' AS TESTKINDCD, T2.SCHREGNO, ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '2' AND T1.TESTKINDCD = '01' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "     UNION  ";
        $query .= "     SELECT  '2' AS SEMES, '02' AS TESTKINDCD, T2.SCHREGNO, ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '2' AND T1.TESTKINDCD = '02' AND T1.TESTITEMCD = '01' AND  ";
        $query .= "             T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "        )  ";
        //定期考査の出欠の表２
        $query .= " ,TEST_ATTEND_B AS ( ";
        $query .= "     SELECT  SCHREGNO,  ";
        $query .= "             MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM1_INTR_SCORE1_ATTEND,  ";
        $query .= "             MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM1_TERM_SCORE1_ATTEND,  ";
        $query .= "             MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SEM2_INTR_SCORE1_ATTEND,  ";
        $query .= "             MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SEM2_TERM_SCORE1_ATTEND  ";
        $query .= "     FROM    TEST_ATTEND_A  ";
        $query .= "     GROUP BY SCHREGNO  ";
        $query .= "     ) ";
        //成績データの表
        $query .= " ,RECORD_SCORE AS ( ";
        $query .= "     SELECT YEAR,SUBCLASSCD,SCHREGNO AS REC_SCHNO ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '10101' THEN SCORE END) AS SEM1_INTR_SCORE1 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '10102' THEN SCORE END) AS SEM1_INTR_SCORE2 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '10201' THEN SCORE END) AS SEM1_TERM_SCORE1 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '10202' THEN SCORE END) AS SEM1_TERM_SCORE2 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '20101' THEN SCORE END) AS SEM2_INTR_SCORE1 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '20102' THEN SCORE END) AS SEM2_INTR_SCORE2 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '20201' THEN SCORE END) AS SEM2_TERM_SCORE1 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||SCORE_DIV = '20202' THEN SCORE END) AS SEM2_TERM_SCORE2 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '199' THEN SCORE END) AS SEM1_SCORE ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '299' THEN SCORE END) AS SEM2_SCORE ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '999' THEN SCORE END) AS GRAD_SCORE ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '199' THEN VALUE END) AS SEM1_VALUE ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '299' THEN VALUE END) AS SEM2_VALUE ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '999' THEN VALUE END) AS GRAD_VALUE ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '999' THEN GET_CREDIT END) AS GET_CREDIT ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '999' THEN COMP_CREDIT END) AS COMP_CREDIT ";
        $query .= "     FROM   RECORD_SCORE_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "            SUBCLASSCD='".$model->field["SUBCLASSCD"]."' ";
        $query .= "     GROUP BY YEAR,SUBCLASSCD,SCHREGNO ";
        $query .= "     ) ";
        //追試・再試データの表
        $query .= " ,SUPP_EXA AS ( ";
        $query .= "     SELECT SCHREGNO AS EXA_SCHNO ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '10101' THEN SCORE_PASS END) AS SEM1_INTR_SCORE1_PASS ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '10201' THEN SCORE_PASS END) AS SEM1_TERM_SCORE1_PASS ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '20101' THEN SCORE_PASS END) AS SEM2_INTR_SCORE1_PASS ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '20201' THEN SCORE_PASS END) AS SEM2_TERM_SCORE1_PASS ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '10101' THEN SCORE_FLG END) AS SEM1_INTR_SCORE1_FLG ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '10201' THEN SCORE_FLG END) AS SEM1_TERM_SCORE1_FLG ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '20101' THEN SCORE_FLG END) AS SEM2_INTR_SCORE1_FLG ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD = '20201' THEN SCORE_FLG END) AS SEM2_TERM_SCORE1_FLG ";
        $query .= "     FROM   SUPP_EXA_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "            SUBCLASSCD='".$model->field["SUBCLASSCD"]."' ";
        $query .= "     GROUP BY SCHREGNO ";
        $query .= "     ) ";

        $query .= " SELECT W0.*";
        $query .= "       ,W4.*";
        $query .= "       ,W1.schregno ";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.inoutcd, W2.name_show, W2.grd_date, W2.COURSE";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
        $query .= "       ,W5.chair_sem1 ";
        $query .= "       ,W6.chair_sem2 ";
        $query .= "       ,W7.SEM1_INTR_SCORE1_ATTEND,W7.SEM1_TERM_SCORE1_ATTEND ";
        $query .= "       ,W7.SEM2_INTR_SCORE1_ATTEND,W7.SEM2_TERM_SCORE1_ATTEND ";
        if ($model->isJitu) {
            $query .= "   ,value(L1.COMP_ABSENCE_HIGH,99) as ABSENCE_HIGH ";
        } else {
            $query .= "   ,value(W8.ABSENCE_HIGH,99) as ABSENCE_HIGH ";
        }
        $query .= "       ,W8.CREDITS,W8.AUTHORIZE_FLG ";
        $query .= "       ,W9.COMP_CREDIT_SUM,W9.GET_CREDIT_SUM,W9.GET_CREDIT_SUM_Y ";//合併先科目の単位を抽出（2:加算タイプ）
        $query .= "   FROM ";
        $query .= "    (SELECT schregno ";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year = '".CTRL_YEAR."'  ";
        $query .= "        AND chaircd = '".$model->field["CHAIRCD"]."'  ";
        $query .= "        AND '".$execute_date."' between appdate AND appenddate ";
        $query .= "    ) W1 ";
        /* 成績情報 */
        $query .= " LEFT OUTER JOIN RECORD_SCORE W0 ON W0.REC_SCHNO = W1.schregno ";
        /* 追試・再試情報 */
        $query .= " LEFT OUTER JOIN SUPP_EXA W4 ON W4.EXA_SCHNO = W1.schregno ";
        /* 基礎情報 */
        $query .= " INNER JOIN ";
        $query .= "    (SELECT T1.year,T1.grade,T0.hr_name, T1.hr_class,T1.attendno,T2.inoutcd,T2.name_show,T2.schregno, T2.grd_date, T1.COURSECD || T1.MAJORCD || T1.COURSECODE as COURSE ";
        $query .= "       FROM schreg_regd_hdat T0, schreg_regd_dat T1, schreg_base_mst T2 ";
        $query .= "      WHERE T0.year     = T1.year";
        $query .= "        AND T0.semester = T1.semester";
        $query .= "        AND T0.grade    = T1.grade";
        $query .= "        AND T0.hr_class = T1.hr_class";
        $query .= "        AND T1.year     = '".CTRL_YEAR."' ";
        $query .= "        AND T1.semester = '".CTRL_SEMESTER."' ";
        $query .= "        AND T1.schregno = T2.schregno ) W2 ";
        $query .= "   ON W1.schregno = W2.schregno ";
        /* 異動情報 */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.schregno, MIN(T1.transfer_sdate) AS transfer_sdate, MAX(T1.transfer_edate) AS transfer_edate";
        $query .= "       FROM schreg_transfer_dat T1 ";
        $query .= "      WHERE FISCALYEAR(T1.transfer_sdate) = '".CTRL_YEAR."'";
        $query .= "      GROUP BY T1.schregno ) W3 ";
        $query .= "   ON W3.schregno = W1.schregno ";
        /* 在籍情報(前期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem1";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  = '".$model->field["CHAIRCD"]."'";
        $query .= "        AND semester = '1') W5";
        $query .= "   ON W5.schregno = W1.schregno ";
        /* 在籍情報(後期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem2";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."'";
        $query .= "        AND chaircd  = '".$model->field["CHAIRCD"]."'";
        $query .= "        AND semester = '2') W6";
        $query .= "   ON W6.schregno = W1.schregno ";
        /* 出欠情報 */
        $query .= " LEFT OUTER JOIN TEST_ATTEND_B W7 ON W7.schregno = W1.schregno ";

        /* 単位マスタ情報(単位数、欠課数上限値、半期認定フラグ) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.SCHREGNO, T2.CREDITS, T2.ABSENCE_HIGH, T2.AUTHORIZE_FLG ";
        $query .= "     FROM   CREDIT_MST T2, SCHREG_REGD_DAT T1 ";
        $query .= "     WHERE  T1.YEAR='".CTRL_YEAR."' AND T1.SEMESTER='".CTRL_SEMESTER."' AND ";
        $query .= "            T2.SUBCLASSCD='".$model->field["SUBCLASSCD"]."' AND ";
        $query .= "            T2.CREDITS IS NOT NULL AND ";
        $query .= "            T2.YEAR=T1.YEAR AND ";
        $query .= "            T2.COURSECD=T1.COURSECD AND ";
        $query .= "            T2.MAJORCD=T1.MAJORCD AND ";
        $query .= "            T2.COURSECODE=T1.COURSECODE AND ";
        $query .= "            T2.GRADE=T1.GRADE) W8 ";
        $query .= "   ON W8.schregno = W1.schregno ";
        if ($model->isJitu) {
            $query .= "        LEFT JOIN SCHREG_ABSENCE_HIGH_DAT L1 ON L1.YEAR = '".CTRL_YEAR."' ";
            $query .= "             AND L1.DIV = '".$model->absenceDiv."' ";
            $query .= "             AND L1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            $query .= "             AND L1.SCHREGNO = W1.SCHREGNO ";
        }

        /* 合併先科目の単位を抽出（2:加算タイプ）*/
        /* 合併先科目の単位は、生徒が選択した合併元科目の単位数を加算した値である */
        /* 「名称予備１＝Y」の場合、合併元科目の「評定＝１（履修のみ）」を合併先科目では「修得」として扱う */
        $query .= " LEFT JOIN (";
        $query .= "     SELECT T2.SCHREGNO, ";
        $query .= "            SUM(T2.COMP_CREDIT) AS COMP_CREDIT_SUM, ";
        $query .= "            SUM(T2.GET_CREDIT) AS GET_CREDIT_SUM, ";
        $query .= "            SUM(case when 1 < T2.VALUE then T2.GET_CREDIT ";
        $query .= "                     when 1 = T2.VALUE then T2.COMP_CREDIT end) AS GET_CREDIT_SUM_Y ";
        $query .= "     FROM   SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "            RECORD_SCORE_DAT T2 ";
        $query .= "     WHERE  T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND  T1.COMBINED_SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= "       AND  T1.CALCULATE_CREDIT_FLG = '2' ";
        $query .= "       AND  T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND  T2.SEMESTER = '9' ";//学年末
        $query .= "       AND  T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ";
        $query .= "       AND (T2.COMP_CREDIT IS NOT NULL OR T2.GET_CREDIT IS NOT NULL) ";
        $query .= "     GROUP BY T2.SCHREGNO ";
        $query .= ") W9 ON W9.schregno = W1.schregno ";

        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";
        return $query;
    }

    // 学校詳細マスタ
    public function getSchoolMst()
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     JUGYOU_JISU_FLG ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR='".CTRL_YEAR."' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //考査満点マスタ
    public function getPerfect($year, $subclasscd, $testcd, $grade, $course)
    {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    case when DIV IS NULL then 100 else PERFECT end as PERFECT ";
        $query .= "FROM ";
        $query .= "    PERFECT_RECORD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        $query .= "    SUBCLASSCD = '{$subclasscd}' AND ";
        $query .= "    SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd}' AND ";
        $query .= "    GRADE = CASE WHEN DIV = '01' THEN '00' ELSE '{$grade}' END AND ";
        $query .= "    COURSECD || MAJORCD || COURSECODE = CASE WHEN DIV IN ('01','02') THEN '00000000' ELSE '{$course}' END ";
        return $query;
    }

    // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
    // 名称区分：D015　名称コード：01　名称予備１：「Y」もしくは「NULL」
    public function getNameMst()
    {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAMESPARE1 "; // Y:, null:
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR='".CTRL_YEAR."' AND ";
        $query .= "    NAMECD1='D015' AND ";
        $query .= "    NAMECD2='01' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //休学時の欠課をカウントするかどうかのフラグ(1 or NULL)を取得。1:欠課をカウントする
    public function getOffdaysFlg($year)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SUB_OFFDAYS,SUB_ABSENT,SUB_SUSPEND,SUB_MOURNING,SUB_VIRUS ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR='{$year}' ";
        return $query;
    }

    public function getAttendData($chaircd, $subclasscd, $absent_cov, $absent_cov_late, $offdaysFlg)
    {
        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($offdaysFlg["SUB_OFFDAYS"] == "1") {
            $notice .= " + SUM(value(OFFDAYS,0))";
        }
        if ($offdaysFlg["SUB_ABSENT"] == "1") {
            $notice .= " + SUM(value(ABSENT,0))";
        }
        if ($offdaysFlg["SUB_SUSPEND"] == "1") {
            $notice .= " + SUM(value(SUSPEND,0))";
        }
        if ($offdaysFlg["SUB_MOURNING"] == "1") {
            $notice .= " + SUM(value(MOURNING,0))";
        }
        if ($offdaysFlg["SUB_VIRUS"] == "1") {
            $notice .= " + SUM(value(VIRUS,0))";
        }
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        /* 出欠累積情報 */
        $query  = "SELECT schregno ";
        $query .= "       ,".$notice."  AS T_NOTICE ";
        $query .= "       ,".$late_early."     AS T_LATEEARLY ";

        if ($absent_cov == "2" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
            $query .= "   ,".$notice."  AS NOTICE_LATE ";
        }
        $query .= "  FROM attend_subclass_dat T1 ";
        $query .= " WHERE COPYCD        = '0' ";
        $query .= "   AND T1.year       = '".CTRL_YEAR."' ";
        $query .= "   AND T1.subclasscd = '".$subclasscd."' ";
        $query .= "   AND EXISTS (SELECT 'X' FROM chair_std_dat T2 ";
        $query .= "                WHERE T2.year    = '".CTRL_YEAR."'";
        $query .= "                  AND T2.chaircd = '".$chaircd."'";
        $query .= "                  AND T2.schregno = T1.schregno) ";
        $query .= " GROUP BY schregno";
        return $query;
    }

    //ABSENT_COVが１(学期ごとに集計）する場合
    public function getAttendData2($chaircd, $subclasscd, $absent_cov_late, $offdaysFlg)
    {
        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($offdaysFlg["SUB_OFFDAYS"] == "1") {
            $notice .= " + SUM(value(OFFDAYS,0))";
        }
        if ($offdaysFlg["SUB_ABSENT"] == "1") {
            $notice .= " + SUM(value(ABSENT,0))";
        }
        if ($offdaysFlg["SUB_SUSPEND"] == "1") {
            $notice .= " + SUM(value(SUSPEND,0))";
        }
        if ($offdaysFlg["SUB_MOURNING"] == "1") {
            $notice .= " + SUM(value(MOURNING,0))";
        }
        if ($offdaysFlg["SUB_VIRUS"] == "1") {
            $notice .= " + SUM(value(VIRUS,0))";
        }
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        $query  = "SELECT schregno ";
        $query .= "       ,SUM(T_NOTICE)    AS T_NOTICE";
        $query .= "       ,SUM(T_LATEEARLY) AS T_LATEEARLY";
        $query .= "       ,SUM(NOTICE_LATE) AS NOTICE_LATE";
        $query .= "  FROM ";
        $query .= "       (SELECT  T1.schregno";
        $query .= "               ,T1.semester ";
        $query .= "               ,".$notice." AS T_NOTICE ";
        $query .= "               ,".$late_early."    AS T_LATEEARLY ";
        if ((is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
            $query .= "           ,".$notice." AS NOTICE_LATE ";
        }
        $query .= "          FROM attend_subclass_dat T1   ";
        $query .= "         WHERE COPYCD        = '0' ";
        $query .= "           AND T1.year       = '".CTRL_YEAR."' ";
        $query .= "           AND T1.subclasscd = '".$subclasscd."' ";
        $query .= "           AND EXISTS (SELECT 'X' FROM chair_std_dat T2 ";
        $query .= "                        WHERE T1.year     = T2.year ";
        $query .= "                          AND T1.schregno = T2.schregno ";
        $query .= "                          AND T2.chaircd  = '".$chaircd."')";
        $query .= "         GROUP BY T1.schregno, T1.semester) T1 ";
        $query .= " GROUP BY schregno ";
        return $query;
    }

    //テーブルSchChrTestのEXECUTEDデータ更新SQL
    public function updateExecuted($model)
    {
        $db = Query::dbCheckOut();

        $chkDbValue = array( "1" => array("TESTKINDCD" => "01", "TESTITEMCD" => "01","SEMETER" => "1") ,
                             "2" => array("TESTKINDCD" => "02", "TESTITEMCD" => "01","SEMETER" => "1") ,
                             "3" => array("TESTKINDCD" => "01", "TESTITEMCD" => "01","SEMETER" => "2") ,
                             "4" => array("TESTKINDCD" => "02", "TESTITEMCD" => "01","SEMETER" => "2") );

        //管理者コントロール
        $query = knjd125bQuery::selectContolCodeQuery();
        $results = $db->query($query);
        while ($row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key[$row_ct["CONTROL_CODE"]]=$row_ct["CONTROL_CODE"];
        }
        $results->free();

        //disable flag
        $ableflg[1] = (isset($admin_key["0111"]))? "" : "disabled" ;
        $ableflg[2] = (isset($admin_key["0121"]))? "" : "disabled" ;
        $ableflg[3] = (isset($admin_key["0211"]))? "" : "disabled" ;
        $ableflg[4] = (isset($admin_key["0221"]))? "" : "disabled" ;

        for ($i=1; $i<=4; $i++) {
            if ($ableflg[$i] == "disabled") {
                continue;
            }

            $chkfg = ($model->field["CHK_COMP".$i] == "on")? "1" : "0" ;

            $data = array();
            $data["EXECUTED"][TEXT]     = $chkfg ;       //成績入力区分
            $data["REGISTERCD"][TEXT]   = STAFFCD ;      //登録者コード
            $data["UPDATED"][FUNC]      = "sysdate()";   //更新日付

            $where  = "";
            $where  = " WHERE YEAR       = '".CTRL_YEAR."' AND ";
            $where .= "       TESTKINDCD = '".$chkDbValue[$i]["TESTKINDCD"]."' AND ";
            $where .= "       TESTITEMCD = '".$chkDbValue[$i]["TESTITEMCD"]."' AND ";
            $where .= "       CHAIRCD    = '".$model->field["CHAIRCD"]."' AND ";
            $where .= "       SEMESTER   = '".$chkDbValue[$i]["SEMETER"]."' ";

            $query = "";
            $query = Query::updateSQL($data, "SCH_CHR_TEST", $where);

            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

    //テーブルSchChrTestのデータ抽出SQL
    public function getSchChrTestData($charcd)
    {
        $query = "
            SELECT SEMESTER || TESTKINDCD || TESTITEMCD as TESTKIND, EXECUTED FROM SCH_CHR_TEST
               WHERE CHAIRCD = '".$charcd."'
               AND YEAR = '".CTRL_YEAR."'
               AND ((TESTKINDCD = '01' AND TESTITEMCD = '01') OR (TESTKINDCD = '02' AND TESTITEMCD = '01') )";

        return $query;
    }


    //テーブルRECORD_SCORE_DATの存在チェック用SQL
    public function existRecordNew($year, $sem, $kind, $item, $div, $subclass, $schno)
    {
        $query  = "";
        $query .= "SELECT COUNT(*) ";
        $query .= "FROM   RECORD_SCORE_DAT ";
        $query .= "WHERE  YEAR          = '$year' ";
        $query .= "  AND  SEMESTER      = '$sem' ";
        $query .= "  AND  TESTKINDCD    = '$kind' ";
        $query .= "  AND  TESTITEMCD    = '$item' ";
        $query .= "  AND  SCORE_DIV     = '$div' ";
        $query .= "  AND  SUBCLASSCD    = '$subclass' ";
        $query .= "  AND  SCHREGNO      = '$schno' ";

        return $query;
    }


    //評定マスタ
    public function getAssessLevel($assesscd, $score)
    {
        $query  = "SELECT  ASSESSLEVEL ";
        $query .= "FROM    ASSESS_MST ";
        $query .= "WHERE   ASSESSCD = '$assesscd' ";
        $query .= "  AND   $score BETWEEN ASSESSLOW AND ASSESSHIGH ";
        return $query;
    }


    //前期課程（中学）かどうかを判断
    public function getIsJunior($schno)
    {
        $query  = "";
        $query .= " SELECT  COUNT(*) ";
        $query .= " FROM    SCHREG_REGD_DAT ";
        $query .= " WHERE   YEAR = '".CTRL_YEAR."' ";
        $query .= " AND     SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " AND     GRADE IN ('01','02','03') ";
        $query .= " AND     SCHREGNO = '$schno' ";
        return $query;
    }

    //評定マスタ・・・前期課程（中学）の場合、固定で学年評定をセット
    public function getAssessLevelJunior($assesscd, $score)
    {
        $query  = "";
        $query .= " WITH ASSESS3_1(ASSESSCD,ASSESSLEVEL,ASSESSLOW,ASSESSHIGH) AS (VALUES('3', 1,  0.0,   0.0)) ";
        $query .= "    , ASSESS3_2(ASSESSCD,ASSESSLEVEL,ASSESSLOW,ASSESSHIGH) AS (VALUES('3', 2,  1.0,  34.0)) ";
        $query .= "    , ASSESS3_3(ASSESSCD,ASSESSLEVEL,ASSESSLOW,ASSESSHIGH) AS (VALUES('3', 3, 35.0,  59.0)) ";
        $query .= "    , ASSESS3_4(ASSESSCD,ASSESSLEVEL,ASSESSLOW,ASSESSHIGH) AS (VALUES('3', 4, 60.0,  84.0)) ";
        $query .= "    , ASSESS3_5(ASSESSCD,ASSESSLEVEL,ASSESSLOW,ASSESSHIGH) AS (VALUES('3', 5, 85.0, 100.0)) ";
        $query .= "    , ASSESS3  (ASSESSCD,ASSESSLEVEL,ASSESSLOW,ASSESSHIGH) ";
        $query .= "           AS (SELECT * FROM ASSESS3_1 ";
        $query .= "     UNION ALL SELECT * FROM ASSESS3_2 ";
        $query .= "     UNION ALL SELECT * FROM ASSESS3_3 ";
        $query .= "     UNION ALL SELECT * FROM ASSESS3_4 ";
        $query .= "     UNION ALL SELECT * FROM ASSESS3_5) ";
        $query .= " SELECT  ASSESSLEVEL ";
        $query .= " FROM    ASSESS3 ";
        $query .= " WHERE   ASSESSCD = '$assesscd' ";
        $query .= "   AND   $score BETWEEN ASSESSLOW AND ASSESSHIGH ";
        return $query;
    }

    //追加および更新
    public function insertUpdateRecordNew($year, $sem, $kind, $item, $div, $model, $schno, $score, $db)
    {
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = $sem;
        $data["TESTKINDCD"][TEXT]   = $kind;
        $data["TESTITEMCD"][TEXT]   = $item;
        $data["SCORE_DIV"][TEXT]    = $div;
        $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
        $data["SCHREGNO"][TEXT]     = $schno;
        if ($model->testcd == $sem.$kind.$item) {
            $data["CHAIRCD"][TEXT]  = $model->field["CHAIRCD"];
        } elseif ($div == "00") {
            $data["CHAIRCD"][TEXT]  = "";
        }
        $data["SCORE"][NUMBER]      = $score;
        if ($kind == "99") {
            $assesscd = ($sem < 9) ? "2" : "3" ;
            $query = knjd125bQuery::getAssessLevel($assesscd, $score);
            $data["VALUE"][NUMBER]  = (strlen($score)) ? $db->getOne($query) : "" ;
            //①点数がある場合
            //②学年評定の場合
            //③2007年度以前の場合
            if (strlen($score) && $assesscd == "3" && CTRL_YEAR <= "2007") {
                //④前期課程（中学）の場合
                $isJunior = $db->getOne(knjd125bQuery::getIsJunior($schno));
                if ($isJunior) {
                    //※固定で学年評定をセット
                    $query = knjd125bQuery::getAssessLevelJunior($assesscd, $score);
                    $data["VALUE"][NUMBER]  = $db->getOne($query);
                }
            }
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD ;      //登録者コード
        $data["UPDATED"][FUNC]      = "sysdate()";   //更新日付
        //追加および更新を判定
        $query = knjd125bQuery::existRecordNew($year, $sem, $kind, $item, $div, $model->field["SUBCLASSCD"], $schno);
        $existflg = $db->getOne($query);
        if (0 < $existflg) {
            $where  = "WHERE  YEAR          = '$year' ";
            $where .= "  AND  SEMESTER      = '$sem' ";
            $where .= "  AND  TESTKINDCD    = '$kind' ";
            $where .= "  AND  TESTITEMCD    = '$item' ";
            $where .= "  AND  SCORE_DIV     = '$div' ";
            $where .= "  AND  SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' ";
            $where .= "  AND  SCHREGNO      = '$schno' ";
            $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
        } else {
            $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
        }
        $db->query($query);
    }


    /* データ更新処理 */
    public function updateRecord($model)
    {
        $db = Query::dbCheckOut();

        //管理者コントロール
        $admin_key_sem = $admin_key_kind = array();
        $results = $db->query(knjd125bquery::selectContolCodeQuery());
        while ($row_ct = $results->fetchRow(DB_FETCHMODE_ASSOC)) {
            $admin_key_sem[substr($row_ct["CONTROL_CODE"], 1, 1)] = $row_ct["CONTROL_CODE"];
            $admin_key_kind[substr($row_ct["CONTROL_CODE"], 2, 1)] = $row_ct["CONTROL_CODE"];
        }
        $results->free();
        
        $isValueAllNull = array();
        $isValueAllNull[1] = false;
        $isValueAllNull[2] = false;
        $isValueAllNull[9] = false;

        for ($i=0; $i<get_count($model->data["SCHREGNO"]); $i++) {
            //素点および学期末および学年末
            $schno = $model->data["SCHREGNO"][$i];
            $score = $score3 = array();
            $term_data = array();
            $term_data9 = array();
            $item = "01";
            for ($sem =1; $sem <= 2; $sem++) {
                for ($kind = 1; $kind <= 2; $kind++) {
                    $kind = sprintf("%02d", $kind);
                    for ($div = 1; $div <= 2; $div++) {
                        $div = sprintf("%02d", $div);

                        //★素点---------->
                        $score_no = $sem.sprintf("%d", $kind).sprintf("%d", $item).sprintf("%d", $div);
                        $score[$score_no]  = $model->fields2["SCORE".$score_no][$i];
                        $score3[$score_no] = $model->fields3["SCORE".$score_no][$i];
                        $isJunior = $db->getOne(knjd125bQuery::getIsJunior($schno));
                        if ("2007" < CTRL_YEAR && $isJunior &&
                           ($model->field["SUBCLASSCD"] == "010100" ||
                            $model->field["SUBCLASSCD"] == "030100" ||
                            $model->field["SUBCLASSCD"] == "090100")) {
                            $divide = 0.7;
                        } else {
                            $divide = 0.9;
                        }
                        if (strlen($score3[$score_no])) {
                            $term_data[$sem][] = ($div == "01") ? $score3[$score_no] * $divide : $score3[$score_no];
                        } elseif (strlen($score[$score_no])) {
                            $term_data[$sem][] = ($div == "01") ? $score[$score_no] * $divide : $score[$score_no];
                        }
                        //ここで、管理者コントロールを見る-->(0111,0121,0211,0221)
                        if (isset($admin_key_sem[$sem]) && isset($admin_key_kind[sprintf("%d", $kind)])) {
                            //NULLデータ削除（素点）
                            if (!$model->isScoreAllNull["SCORE".$score_no]) {
                                knjd125bQuery::deleteRecordScore(CTRL_YEAR, $sem, $kind, $item, $div, $model->field["SUBCLASSCD"], $schno, $db);
                            } else {
                                knjd125bQuery::insertUpdateRecordNew(CTRL_YEAR, $sem, $kind, $item, $div, $model, $schno, $score[$score_no], $db);
                            }
                        }
                    }//div
                }//kind

                //★学期末---------->
                $score[$sem."900"] = $model->fields2["SCORE".$sem."900"][$i];
                if (get_count($term_data[$sem]) == 0) {
                    $score_ceil = $score[$sem."900"];
                } elseif (get_count($term_data[$sem]) == 4) {
                    $score_ceil = ceil(array_sum($term_data[$sem]) / 2);
                } else {
                    $score_ceil = "";
                }
                if (strlen($score_ceil)) {
                    $term_data9[] = $score_ceil;
                }
                //ここで、管理者コントロールを見る-->(0111,0121,0182または0211,0221,0282)
                if (isset($admin_key_sem["1"]) || isset($admin_key_sem["2"])) {
                    knjd125bQuery::insertUpdateRecordNew(CTRL_YEAR, $sem, "99", "00", "00", $model, $schno, $score_ceil, $db);
                    if (strlen($score_ceil)) {
                        $isValueAllNull[$sem] = true;
                    }
                }
            }//sem

            //★学年末---------->
            if (get_count($term_data9) == 2) {
                $score_ceil9 = ceil(array_sum($term_data9) / 2);
            } else {
                $score_ceil9 = "";
            }
            //ここで、管理者コントロールを見る-->(0111,0121,0182,0211,0221,0282)
            if (isset($admin_key_sem["1"]) || isset($admin_key_sem["2"])) {
                knjd125bQuery::insertUpdateRecordNew(CTRL_YEAR, "9", "99", "00", "00", $model, $schno, $score_ceil9, $db);
                if (strlen($score_ceil9)) {
                    $isValueAllNull[9] = true;
                }
            }
            //ここで、管理者コントロールを見る-->(0885)
//          if (isset($admin_key_sem["8"])) {
            knjd125bQuery::updateCredit($model, $schno, $i, $db);
//          }
        }//schno

        //NULLデータ削除（学期末・学年末）
        foreach ($isValueAllNull as $key => $value) {
            if (isset($admin_key_sem["1"]) || isset($admin_key_sem["2"])) {
                if (!$value) {
                    for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
                        knjd125bQuery::deleteRecordScore(CTRL_YEAR, $key, "99", "00", "00", $model->field["SUBCLASSCD"], $model->data["SCHREGNO"][$i], $db);
                    }
                }
            }
        }//foreach

        Query::dbCheckIn($db);
    }

    //テーブルRECORD_SCORE_DATのレコードを削除
    public function deleteRecordScore($year, $sem, $kind, $item, $div, $subclass, $schno, $db)
    {
        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_SCORE_DAT ";
        $query .= "WHERE  YEAR          = '$year' ";
        $query .= "  AND  SEMESTER      = '$sem' ";
        $query .= "  AND  TESTKINDCD    = '$kind' ";
        $query .= "  AND  TESTITEMCD    = '$item' ";
        $query .= "  AND  SCORE_DIV     = '$div' ";
        $query .= "  AND  SUBCLASSCD    = '$subclass' ";
        $query .= "  AND  SCHREGNO      = '$schno' ";
        $db->query($query);
    }

    //テーブルRECORD_SCORE_DATの取得SQL（前期評点・後期評点・学年評定）
    public function getValueRecordNew($sem, $subclass, $schno)
    {
        $query  = "";
        $query .= "SELECT VALUE ";
        $query .= "FROM   RECORD_SCORE_DAT ";
        $query .= "WHERE  YEAR          = '".CTRL_YEAR."' ";
        $query .= "  AND  SEMESTER      = '$sem' ";
        $query .= "  AND  TESTKINDCD    = '99' ";
        $query .= "  AND  TESTITEMCD    = '00' ";
        $query .= "  AND  SCORE_DIV     = '00' ";
        $query .= "  AND  SUBCLASSCD    = '$subclass' ";
        $query .= "  AND  SCHREGNO      = '$schno' ";

        return $query;
    }


    /* データ更新処理 */
    public function updateCredit($model, $schno, $i, $db)
    {
        $data = array();

        //履修／修得単位を一旦セットする
        $data["COMP_CREDIT"][NUMBER] = $model->fields["COMP_CREDIT"][$i];
        $data["GET_CREDIT"][NUMBER]  = $model->fields["GET_CREDIT"][$i];

        /* 履修単位・修得単位の自動計算処理 始め */

        //$notice_val[0]:欠課数(画面表示)
        //$notice_val[1]:単位数
        //$notice_val[2]:欠課数上限値
        //$notice_val[3]:半期認定フラグ
        $notice_val = explode("-", $model->data["NOTICE_LATE"][$i]);

        //$combined_val[0]:合併元科目の履修単位の合計
        //$combined_val[1]:合併元科目の修得単位の合計
        //$combined_val[2]:合併元科目の修得単位の合計「名称予備１＝Y」の場合
        $combined_val = explode("-", $model->data["COMBINED_CREDIT_SUM"][$i]);

        //学年評定を取得
        $query = knjd125bQuery::getValueRecordNew("9", $model->field["SUBCLASSCD"], $schno);
        $model->fields["GRAD_VALUE"][$i] = $db->getOne($query);

        if (($model->fields["GRAD_VALUE"][$i] != "") &&
            ($model->fields["COMP_CREDIT"][$i] == "" && $model->fields["GET_CREDIT"][$i] == "")) {
            // 履修単位・修得単位が入力可で、学年評定が入力されていて、履修単位・修得単位がNULLの時
            if (isset($model->creditflg)) {
                // 合併先科目である
                // $model->creditflg ---> 1:単位固定タイプ 2:単位加算タイプ
                if ($model->fields["GRAD_VALUE"][$i] > "1") {
                    // 学年評定:２以上、履修・修得に登録
                    $getCredit = ($model->repGetCredit == "Y") ? $combined_val[2] : $combined_val[1];
                    $data["COMP_CREDIT"][NUMBER] = ($model->creditflg == "2") ? $combined_val[0] : $notice_val[1];
                    $data["GET_CREDIT"][NUMBER]  = ($model->creditflg == "2") ? $getCredit : $notice_val[1];
                } elseif ($model->fields["GRAD_VALUE"][$i] == "1") {
                    // 学年評定:１、履修のみに登録
                    $data["COMP_CREDIT"][NUMBER] = ($model->creditflg == "2") ? $combined_val[0] : $notice_val[1];
                    $data["GET_CREDIT"][NUMBER]  = 0;
                }
            } else {
                // 合併先科目でない--->今までと同じ計算処理をする
                if ($notice_val[1] != "" && ($model->fields["GRAD_VALUE"][$i] != "")) {
                    //単位数・学年評定がある場合のみ処理
                    $hanki_credit = $notice_val[1] / 2;
                    if ($model->fields["GRAD_VALUE"][$i] > "1" && $notice_val[0] > $notice_val[2]) {
                        //①学年評定:２以上、欠課数上限値オーバー
                        if ($notice_val[3] == "1") {
                            //半期認定科目
                            $data["COMP_CREDIT"][NUMBER] = floor($hanki_credit);
                            $data["GET_CREDIT"][NUMBER]  = floor($hanki_credit);
                            //前期評点・後期評点を取得
                            $query = knjd125bQuery::getValueRecordNew("1", $model->field["SUBCLASSCD"], $schno);
                            $model->fields["SEM1_VALUE"][$i] = $db->getOne($query);
                            $query = knjd125bQuery::getValueRecordNew("2", $model->field["SUBCLASSCD"], $schno);
                            $model->fields["SEM2_VALUE"][$i] = $db->getOne($query);
                            if ($model->fields["SEM1_VALUE"][$i] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "1";
                            }
                            if ($model->fields["SEM2_VALUE"][$i] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "2";
                            }
                        } else {
                            $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                            $data["GET_CREDIT"][NUMBER]  = $notice_val[1];
                            $data["COMP_TAKESEMES"][TEXT] = "";
                        }
                    } elseif ($model->fields["GRAD_VALUE"][$i] > "1" && $notice_val[0] <= $notice_val[2]) {
                        //②学年評定:２以上、欠課数上限値内
                        $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                        $data["GET_CREDIT"][NUMBER]  = $notice_val[1];
                        $data["COMP_TAKESEMES"][TEXT] = "";
                    } elseif ($model->fields["GRAD_VALUE"][$i] == "1" && $notice_val[0] > $notice_val[2]) {
                        //③学年評定:１、欠課数上限値オーバー
                        if ($notice_val[3] == "1") {
                            //半期認定科目
                            $data["COMP_CREDIT"][NUMBER] = floor($hanki_credit);
                            $data["GET_CREDIT"][NUMBER]  = 0;
                            //前期評点・後期評点を取得
                            $query = knjd125bQuery::getValueRecordNew("1", $model->field["SUBCLASSCD"], $schno);
                            $model->fields["SEM1_VALUE"][$i] = $db->getOne($query);
                            $query = knjd125bQuery::getValueRecordNew("2", $model->field["SUBCLASSCD"], $schno);
                            $model->fields["SEM2_VALUE"][$i] = $db->getOne($query);
                            if ($model->fields["SEM1_VALUE"][$i] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "1";
                            }
                            if ($model->fields["SEM2_VALUE"][$i] != "") {
                                $data["COMP_TAKESEMES"][TEXT] = "2";
                            }
                        } else {
                            $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                            $data["GET_CREDIT"][NUMBER]  = 0;
                            $data["COMP_TAKESEMES"][TEXT] = "";
                        }
                    } elseif ($model->fields["GRAD_VALUE"][$i] == "1" && $notice_val[0] <= $notice_val[2]) {
                        //④学年評定:１、欠課数上限値内
                        $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                        $data["GET_CREDIT"][NUMBER]  = 0;
                        $data["COMP_TAKESEMES"][TEXT] = "";
                    } else {
                        //⑥その他---NO006
                        $data["COMP_CREDIT"][NUMBER] = "";
                        $data["GET_CREDIT"][NUMBER]  = "";
                        $data["COMP_TAKESEMES"][TEXT] = "";
                    }
                }//if3
            }//if2
        }//if1

        /* 履修単位・修得単位の自動計算処理 終わり */

        $data["REGISTERCD"][TEXT]        = STAFFCD ;
        $data["UPDATED"][FUNC]           = "sysdate()";

        $where  = "WHERE  YEAR          = '".CTRL_YEAR."' ";
        $where .= "  AND  SEMESTER      = '9' ";
        $where .= "  AND  TESTKINDCD    = '99' ";
        $where .= "  AND  TESTITEMCD    = '00' ";
        $where .= "  AND  SCORE_DIV     = '00' ";
        $where .= "  AND  SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' ";
        $where .= "  AND  SCHREGNO      = '".$schno."' ";
        $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);

        $db->query($query);
    }
}
