<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd125eQuery.inc 56581 2017-10-22 12:37:16Z maeshiro $

class knjd125eQuery extends Query {

    //時間割講座テストより試験日を抽出
    function selectExecuteDateQuery($model, $opt_chair_cmb){

        $query  = " SELECT DISTINCT ";
        $query .= "    EXECUTEDATE ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_TEST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     TESTKINDCD||TESTITEMCD = '".$model->field["TESTITEM"]."' AND ";
        if ($model->field["CHAIRCD"] != "0") {
            $query .= " CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        } else {
            $query .= " CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') ";
        }
        $query .= " ORDER BY EXECUTEDATE ";
//echo "CHAIRCD = " .$model->field["CHAIRCD"] ."<BR>";
//echo $query;
      return $query;
    }

    //科目データの取得
    function selectSubclassQuery()
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
    function selectChairQuery($model)
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

    //学期
    function getSemesterMst()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER,SEMESTERNAME ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    SEMESTER <> '9' ";
        $query .= "ORDER BY SEMESTER ";
        return $query;
    }

    //テスト名
    function getTestItemName($model)
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER,TESTKINDCD,TESTITEMCD, ";
        $query .= "    TESTITEMNAME ";
        $query .= "FROM ";
        $query .= "    TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "    TESTKINDCD <> '99' ";
        $query .= "ORDER BY SEMESTER,TESTKINDCD,TESTITEMCD ";
        return $query;
    }

    //成績入力対象科目の判別
    function getSubjectFlg($namecd1, $subclass)
    {
        $query  = "SELECT COUNT(*) CNT ";
        $query .= "FROM   V_NAME_MST ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' AND ";
        $query .= "       NAMECD1 = '$namecd1' AND ";
        $query .= "       NAME1 = '$subclass' ";
        return $query;
    }

    //遅刻何回で欠課とするかの指数取得
    function getScAbsentCov()
    {
        return "SELECT absent_cov,absent_cov_late FROM school_mst WHERE year = '".CTRL_YEAR."'";
    }

    //合併先科目の単位固定／加算フラグ
    function getCalculateCreditFlg($subclasscd)
    {
        $query  = "SELECT DISTINCT CALCULATE_CREDIT_FLG ";
        $query .= "FROM   SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "WHERE  YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND  COMBINED_SUBCLASSCD = '".$subclasscd."' ";
        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $execute_date, $opt_chair_cmb)
    {
        //定期考査の出欠の表１
        $query  = " WITH TEST_ATTEND_A AS ( ";
        $query .= "     SELECT  '1' AS SEMES, '01' AS TESTKINDCD, T2.SCHREGNO,  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END AS DI_CD ";
        $query .= "     FROM    SCH_CHR_TEST T1,ATTEND_DAT T2  ";
        $query .= "             LEFT JOIN ATTEND_DI_CD_DAT ADD ON T2.YEAR  = ADD.YEAR ";
        $query .= "                                           AND T2.DI_CD = ADD.DI_CD ";
        $query .= "     WHERE   T1.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T1.SEMESTER = '1' AND T1.TESTKINDCD = '01' AND T1.TESTITEMCD = '01' AND  ";
        if ($model->field["CHAIRCD"] != "0") {
            $query .= "         T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        } else {
            $query .= "         T1.CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') AND  ";
        }
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = T1.CHAIRCD AND  ";
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
        if ($model->field["CHAIRCD"] != "0") {
            $query .= "         T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        } else {
            $query .= "         T1.CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') AND  ";
        }
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = T1.CHAIRCD AND  ";
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
        if ($model->field["CHAIRCD"] != "0") {
            $query .= "         T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        } else {
            $query .= "         T1.CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') AND  ";
        }
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = T1.CHAIRCD AND  ";
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
        if ($model->field["CHAIRCD"] != "0") {
            $query .= "         T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        } else {
            $query .= "         T1.CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') AND  ";
        }
        $query .= "             T2.YEAR = '".CTRL_YEAR."' AND  ";
        $query .= "             T2.CHAIRCD = T1.CHAIRCD AND  ";
        $query .= "             T2.ATTENDDATE = T1.EXECUTEDATE AND T1.PERIODCD = T2.PERIODCD AND  ";
        $query .= "             CASE WHEN ADD.ATSUB_REPL_DI_CD IS NOT NULL THEN ADD.ATSUB_REPL_DI_CD ELSE ADD.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14')  ";
        $query .= "        )  ";
        //定期考査の出欠の表２
        $query .= " ,TEST_ATTEND_B AS ( ";
        $query .= "     SELECT  SCHREGNO,  ";
        $query .= "             MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SCORE101_ATTEND,  ";
        $query .= "             MIN(CASE WHEN SEMES = '1' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SCORE102_ATTEND,  ";
        $query .= "             MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '01' THEN INT(DI_CD) END) AS SCORE201_ATTEND,  ";
        $query .= "             MIN(CASE WHEN SEMES = '2' AND TESTKINDCD = '02' THEN INT(DI_CD) END) AS SCORE202_ATTEND  ";
        $query .= "     FROM    TEST_ATTEND_A  ";
        $query .= "     GROUP BY SCHREGNO  ";
        $query .= "     ) ";
        //成績データの表
        $query .= " ,RECORD_SCORE AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "           ,SUM(CASE WHEN SEMESTER||TESTKINDCD = '101' THEN SCORE END) AS SCORE101 ";
        $query .= "           ,SUM(CASE WHEN SEMESTER||TESTKINDCD = '102' THEN SCORE END) AS SCORE102 ";
        $query .= "           ,SUM(CASE WHEN SEMESTER||TESTKINDCD = '201' THEN SCORE END) AS SCORE201 ";
        $query .= "           ,SUM(CASE WHEN SEMESTER||TESTKINDCD = '202' THEN SCORE END) AS SCORE202 ";
        $query .= "     FROM   RECORD_SCORE_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND ";
        if ($model->subjectflg != "C") {
            $query .= "        SCORE_DIV IN ('01') AND ";       //素点（基本/中学・英語）
        } else {
            $query .= "        SCORE_DIV IN ('01','02') AND ";  //共通・ﾚｯｽﾝ（高校・英語数学）
        }
        $query .= "            SUBCLASSCD='".$model->field["SUBCLASSCD"]."' ";
        $query .= "     GROUP BY SCHREGNO ";
        $query .= "     ) ";
        //成績データの表2
        $query .= " ,RECORD_VALUE AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "           ,MIN(CASE WHEN SEMESTER = '1' THEN SCORE END) AS SCORE199 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER = '2' THEN SCORE END) AS SCORE299 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER = '9' THEN SCORE END) AS SCORE999 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER = '1' THEN VALUE END) AS VALUE199 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER = '2' THEN VALUE END) AS VALUE299 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER = '9' THEN VALUE END) AS VALUE999 ";
        $query .= "           ,MIN(CASE WHEN SEMESTER = '9' THEN GET_CREDIT END) AS GET_CREDIT ";
        $query .= "           ,MIN(CASE WHEN SEMESTER = '9' THEN COMP_CREDIT END) AS COMP_CREDIT ";
        $query .= "     FROM   RECORD_SCORE_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "            TESTKINDCD IN ('99') AND ";
        $query .= "            SUBCLASSCD='".$model->field["SUBCLASSCD"]."' ";
        $query .= "     GROUP BY SCHREGNO ";
        $query .= "     ) ";
        //追試・再試データの表
        $query .= " ,SUPP_EXA AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '101' THEN SCORE END) AS SCORE101_SUPP ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '102' THEN SCORE END) AS SCORE102_SUPP ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '201' THEN SCORE END) AS SCORE201_SUPP ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '202' THEN SCORE END) AS SCORE202_SUPP ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '101' THEN SCORE_FLG END) AS SCORE101_FLG ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '102' THEN SCORE_FLG END) AS SCORE102_FLG ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '201' THEN SCORE_FLG END) AS SCORE201_FLG ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '202' THEN SCORE_FLG END) AS SCORE202_FLG ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '101' THEN SCORE_PASS END) AS SCORE101_PASS ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '102' THEN SCORE_PASS END) AS SCORE102_PASS ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '201' THEN SCORE_PASS END) AS SCORE201_PASS ";
        $query .= "           ,MIN(CASE WHEN SEMESTER||TESTKINDCD = '202' THEN SCORE_PASS END) AS SCORE202_PASS ";
        $query .= "     FROM   SUPP_EXA_DAT ";
        $query .= "     WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "            SUBCLASSCD='".$model->field["SUBCLASSCD"]."' ";
        $query .= "     GROUP BY SCHREGNO ";
        $query .= "     ) ";
        //成績データNULL生徒の表
        $query .= " ,RECORD_SCORE_NULL AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "     FROM   RECORD_SCORE_DAT ";
        $query .= "     WHERE  YEAR = '".CTRL_YEAR."' AND ";
        $query .= "            SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "            TESTKINDCD||TESTITEMCD = '".$model->field["TESTITEM"]."' AND ";
        if ($model->subjectflg != "C") {
            $query .= "        SCORE_DIV IN ('01') AND ";       //素点（基本/中学・英語）
        } else {
            $query .= "        SCORE_DIV IN ('01','02') AND ";  //共通・ﾚｯｽﾝ（高校・英語数学）
        }
        $query .= "            SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= "     GROUP BY SCHREGNO ";
        $query .= "     HAVING SUM(SCORE) IS NULL ";
        $query .= "     ) ";
        //講座生徒データの表
        $query .= " ,CHAIR_STD AS ( ";
        $query .= "     SELECT SCHREGNO ";
        $query .= "       FROM CHAIR_STD_DAT ";
        $query .= "      WHERE YEAR = '".CTRL_YEAR."' AND  ";
        if ($model->field["CHAIRCD"] != "0") {
            $query .= "        CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        } else {
            $query .= "        CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') AND  ";
        }
        $query .= "            '".$execute_date."' BETWEEN APPDATE AND APPENDDATE ";
        $query .= "     GROUP BY SCHREGNO ";
        $query .= "     ) ";

        $query .= " SELECT W1.schregno ";
        $query .= "       ,W4.SCORE101_FLG,W4.SCORE102_FLG,W4.SCORE201_FLG,W4.SCORE202_FLG ";
        $query .= "       ,W4.SCORE101_PASS,W4.SCORE102_PASS,W4.SCORE201_PASS,W4.SCORE202_PASS ";
        $query .= "       ,W4.SCORE101_SUPP,W4.SCORE102_SUPP,W4.SCORE201_SUPP,W4.SCORE202_SUPP ";
        $query .= "       ,W0.SCORE101,W0.SCORE102,W0.SCORE201,W0.SCORE202 ";
        $query .= "       ,W10.SCORE199,W10.SCORE299,W10.SCORE999 ";
        $query .= "       ,W10.VALUE199,W10.VALUE299,W10.VALUE999 ";
        $query .= "       ,W10.GET_CREDIT,W10.COMP_CREDIT ";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.inoutcd, W2.name_show, W2.grd_date";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
        $query .= "       ,W5.chair_sem1 ";
        $query .= "       ,W6.chair_sem2 ";
        $query .= "       ,W7.SCORE101_ATTEND,W7.SCORE102_ATTEND ";
        $query .= "       ,W7.SCORE201_ATTEND,W7.SCORE202_ATTEND ";
        if ($model->isJitu) {
            $query .= "   ,value(L1.COMP_ABSENCE_HIGH,99) as ABSENCE_HIGH ";
        } else {
            $query .= "   ,value(W8.ABSENCE_HIGH,99) as ABSENCE_HIGH ";
        }
        $query .= "       ,W8.CREDITS,W8.AUTHORIZE_FLG ";
        $query .= "       ,W9.COMP_CREDIT_SUM,W9.GET_CREDIT_SUM,W9.GET_CREDIT_SUM_Y ";//合併先科目の単位を抽出（2:加算タイプ）
        $query .= "   FROM ";
        $query .= "    ( ";
        $query .= "     SELECT T1.SCHREGNO ";
        $query .= "     FROM   RECORD_SCORE_NULL T1, CHAIR_STD T2 ";
        $query .= "     WHERE  T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "    ) W1 ";
        /* 成績情報 */
        $query .= " LEFT OUTER JOIN RECORD_SCORE W0 ON W0.schregno = W1.schregno ";
        $query .= " LEFT OUTER JOIN RECORD_VALUE W10 ON W10.schregno = W1.schregno ";
        /* 追試・再試情報 */
        $query .= " LEFT OUTER JOIN SUPP_EXA W4 ON W4.schregno = W1.schregno ";
        /* 基礎情報 */
        $query .= " INNER JOIN ";
        $query .= "    (SELECT T1.year,T1.grade,T0.hr_name, T1.hr_class,T1.attendno,T2.inoutcd,T2.name_show,T2.schregno, T2.grd_date ";
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
        $query .= "      WHERE year     = '".CTRL_YEAR."' AND ";
        if ($model->field["CHAIRCD"] != "0") {
            $query .= "        CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        } else {
            $query .= "        CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') AND  ";
        }
        $query .= "            semester = '1') W5";
        $query .= "   ON W5.schregno = W1.schregno ";
        /* 在籍情報(後期) */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT DISTINCT schregno, semester AS chair_sem2";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year     = '".CTRL_YEAR."' AND ";
        if ($model->field["CHAIRCD"] != "0") {
            $query .= "        CHAIRCD = '".$model->field["CHAIRCD"]."' AND  ";
        } else {
            $query .= "        CHAIRCD IN ('".implode("','", $opt_chair_cmb)."') AND  ";
        }
        $query .= "            semester = '2') W6";
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
//echo $query;
        return $query;
    }

    // 学校詳細マスタ
    function getSchoolMst()
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

    // 合併元科目の「評定＝１（履修のみ）」を合併先科目（単位加算）では「修得」として扱うフラグの取得
    // 名称区分：D015　名称コード：01　名称予備１：「Y」もしくは「NULL」
    function getNameMst()
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
    function getOffdaysFlg($year) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SUB_OFFDAYS,SUB_ABSENT,SUB_SUSPEND,SUB_MOURNING,SUB_VIRUS ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR='{$year}' ";
        return $query;
    }

    function GetAttendData($subclasscd, $absent_cov, $absent_cov_late, $offdaysFlg)
    {
        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($offdaysFlg["SUB_OFFDAYS"] == "1")  $notice .= " + SUM(value(OFFDAYS,0))";
        if ($offdaysFlg["SUB_ABSENT"] == "1")   $notice .= " + SUM(value(ABSENT,0))";
        if ($offdaysFlg["SUB_SUSPEND"] == "1")  $notice .= " + SUM(value(SUSPEND,0))";
        if ($offdaysFlg["SUB_MOURNING"] == "1") $notice .= " + SUM(value(MOURNING,0))";
        if ($offdaysFlg["SUB_VIRUS"] == "1")    $notice .= " + SUM(value(VIRUS,0))";
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        /* 出欠累積情報 */
        $query .= "SELECT schregno ";
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
        $query .= " GROUP BY schregno";
        return $query;
    }

    //ABSENT_COVが１(学期ごとに集計）する場合
    function GetAttendData2($subclasscd, $absent_cov_late, $offdaysFlg)
    {
        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($offdaysFlg["SUB_OFFDAYS"] == "1")  $notice .= " + SUM(value(OFFDAYS,0))";
        if ($offdaysFlg["SUB_ABSENT"] == "1")   $notice .= " + SUM(value(ABSENT,0))";
        if ($offdaysFlg["SUB_SUSPEND"] == "1")  $notice .= " + SUM(value(SUSPEND,0))";
        if ($offdaysFlg["SUB_MOURNING"] == "1") $notice .= " + SUM(value(MOURNING,0))";
        if ($offdaysFlg["SUB_VIRUS"] == "1")    $notice .= " + SUM(value(VIRUS,0))";
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
        $query .= "         GROUP BY T1.schregno, T1.semester) T1 ";
        $query .= " GROUP BY schregno ";
        return $query;
    }

    //追試および見込
    function updateSuppExaDat($model) {

        $db = Query::dbCheckOut();

        $seme_kind = $model->field["SEMESTER"] .substr($model->field["TESTITEM"],0,2);
        for ($i = 0; $i < get_count($model->fields["chk_box"]); $i++) {
            //削除
            $query  = "DELETE ";
            $query .= "FROM   SUPP_EXA_DAT ";
            $query .= "WHERE  YEAR                      = '".CTRL_YEAR."' ";
            $query .= "  AND  SEMESTER                  = '".$model->field["SEMESTER"]."' ";
            $query .= "  AND  TESTKINDCD || TESTITEMCD  = '".$model->field["TESTITEM"]."' ";
            $query .= "  AND  SUBCLASSCD                = '".$model->field["SUBCLASSCD"]."' ";
            $query .= "  AND  SCHREGNO                  = '".$model->fields["SCHREGNO"][$i]."' ";
            $db->query($query);
            //追加
            $score1 = $model->fields["SCORE".$seme_kind."_1"][$i];
            $score2 = $model->fields["SCORE".$seme_kind."_2"][$i];
            if (strlen($score1)) {
                $score = $score1;//追試
                $score_pass = $score1 * 0.9;//合格点
                $score_flg = "1";//フラグ
            } else if (strlen($score2)) {
                $score = $score2;//見込
                $score_pass = $score2;//合格点
                $score_flg = "2";//フラグ
            } else {
                continue;//追加しない
            }
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT]   = substr($model->field["TESTITEM"],0,2);
            $data["TESTITEMCD"][TEXT]   = substr($model->field["TESTITEM"],2,2);
            $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
            $data["SCHREGNO"][TEXT]     = $model->fields["SCHREGNO"][$i];
            $data["SCORE"][NUMBER]      = $score;
            $data["SCORE_PASS"][NUMBER] = ceil($score_pass);
            $data["SCORE_FLG"][TEXT]    = $score_flg;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "SUPP_EXA_DAT");
            $db->query($query);
//var_dump($data);
        }

        Query::dbCheckIn($db);
    }

    //（前期・後期）（総合・評点）
    function updateRecordScoreDat($model) {

        $db = Query::dbCheckOut();

        $seme_kind = $model->field["SEMESTER"] .substr($model->field["TESTITEM"],0,2);
        for ($i = 0; $i < get_count($model->fields["chk_box"]); $i++) {
            //総合・評点
            $isJunior = $db->getOne(knjd125eQuery::getIsJunior($model->fields["SCHREGNO"][$i]));
            if ("2007" < CTRL_YEAR && $isJunior && 
               ($model->field["SUBCLASSCD"] == "010100" || 
                $model->field["SUBCLASSCD"] == "030100" || 
                $model->field["SUBCLASSCD"] == "090100")) {
                $divideFlg = true;
            } else {
                $divideFlg = false;
            }
            $query = knjd125eQuery::getRecordScoreGakki(CTRL_YEAR, $model->field["SEMESTER"], $model->field["SUBCLASSCD"], $model->fields["SCHREGNO"][$i], $model->subjectflg, $divideFlg);
            $score = $db->getOne($query);
            $score_ceil = (strlen($score)) ? ceil($score / 2) : "";
            $data = array();
            $data["SCORE"][NUMBER]      = $score_ceil;
            if (strlen($score_ceil)) {
                $query = knjd125eQuery::GetAssessLevel("2", $score_ceil);
                $data["VALUE"][NUMBER]  = $db->getOne($query);
            } else {
                $data["VALUE"][NUMBER]  = "";
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $where  = "WHERE  YEAR          = '".CTRL_YEAR."' ";
            $where .= "  AND  SEMESTER      = '".$model->field["SEMESTER"]."' ";
            $where .= "  AND  TESTKINDCD    = '99' ";
            $where .= "  AND  TESTITEMCD    = '00' ";
            $where .= "  AND  SCORE_DIV     = '00' ";
            $where .= "  AND  SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' ";
            $where .= "  AND  SCHREGNO      = '".$model->fields["SCHREGNO"][$i]."' ";
            $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
            $db->query($query);
            //学年評定
            $query  = " SELECT  SUM(SCORE) AS SCORE ";
            $query .= " FROM    RECORD_SCORE_DAT ";
            $query .= " WHERE   YEAR='".CTRL_YEAR."' AND ";
            $query .= "         SEMESTER<>'9' AND ";
            $query .= "         TESTKINDCD='99' AND ";
            $query .= "         SUBCLASSCD='".$model->field["SUBCLASSCD"]."' AND ";
            $query .= "         SCHREGNO='".$model->fields["SCHREGNO"][$i]."' ";
            $query .= " GROUP BY YEAR ";
            $query .= " HAVING COUNT(SCORE) = 2 ";
            $score = $db->getOne($query);
            $score_ceil = (strlen($score)) ? ceil($score / 2) : "";
            $data = array();
            $data["SCORE"][NUMBER]      = $score_ceil;
            if (strlen($score_ceil)) {
                $query = knjd125eQuery::GetAssessLevel("3", $score_ceil);
                $data["VALUE"][NUMBER]  = $db->getOne($query);
                //③2007年度以前の場合
                if (CTRL_YEAR <= "2007") {
                    //④前期課程（中学）の場合
                    $isJunior = $db->getOne(knjd125eQuery::getIsJunior($model->fields["SCHREGNO"][$i]));
                    if ($isJunior) {
                        //※固定で学年評定をセット
                        $query = knjd125eQuery::GetAssessLevelJunior("3", $score_ceil);
                        $data["VALUE"][NUMBER]  = $db->getOne($query);
                    }
                }
            } else {
                $data["VALUE"][NUMBER]  = "";
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $where  = "WHERE  YEAR          = '".CTRL_YEAR."' ";
            $where .= "  AND  SEMESTER      = '9' ";
            $where .= "  AND  TESTKINDCD    = '99' ";
            $where .= "  AND  TESTITEMCD    = '00' ";
            $where .= "  AND  SCORE_DIV     = '00' ";
            $where .= "  AND  SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' ";
            $where .= "  AND  SCHREGNO      = '".$model->fields["SCHREGNO"][$i]."' ";
            $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
            $db->query($query);
            //履修・修得単位の自動計算
            $query  = " SELECT  SEMESTER, VALUE, GET_CREDIT, COMP_CREDIT ";
            $query .= " FROM    RECORD_SCORE_DAT ";
            $query .= " WHERE   YEAR='".CTRL_YEAR."' AND ";
            $query .= "         TESTKINDCD='99' AND ";
            $query .= "         TESTITEMCD='00' AND ";
            $query .= "         SCORE_DIV='00' AND ";
            $query .= "         SUBCLASSCD='".$model->field["SUBCLASSCD"]."' AND ";
            $query .= "         SCHREGNO='".$model->fields["SCHREGNO"][$i]."' ";
            $tani = array();
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $tani[$row["SEMESTER"]]["VALUE"]        = $row["VALUE"];
                $tani[$row["SEMESTER"]]["GET_CREDIT"]   = $row["GET_CREDIT"];
                $tani[$row["SEMESTER"]]["COMP_CREDIT"]  = $row["COMP_CREDIT"];
            }
            //学年評定が入力されていて、履修単位・修得単位がNULLの時
            if ($tani["9"]["VALUE"] != "" && $tani["9"]["COMP_CREDIT"] == "" && $tani["9"]["GET_CREDIT"] == "")
            {
//var_dump($tani);
                knjd125eQuery::updateCredit($model, $tani, $i, $db);
            }
        }
    }

    //（前期・後期）（総合・評点）
    function getRecordScoreGakki($year, $sem, $subclass, $schno, $subjectflg, $divideFlg) {

        $query  = "";
        $query .= " WITH SCORE AS ( ";
        $query .= "     SELECT  SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV, SCORE ";
        $query .= "     FROM    RECORD_SCORE_DAT ";
        $query .= "     WHERE   YEAR='$year' AND ";
        $query .= "             SEMESTER='$sem' AND ";
        $query .= "             TESTKINDCD<>'99' AND ";
        if ($subjectflg == "B") {
            $query .= "         SCORE_DIV in('01','02') AND ";
        } else {
            $query .= "         SCORE_DIV in('01','02','03') AND ";
        }
        $query .= "             SUBCLASSCD='$subclass' AND ";
        $query .= "             SCHREGNO='$schno' ";
        $query .= "     ) ";
        $query .= " ,SCORE_PASS AS ( ";
        $query .= "     SELECT  SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_PASS ";
        $query .= "     FROM    SUPP_EXA_DAT ";
        $query .= "     WHERE   YEAR='$year' AND ";
        $query .= "             SEMESTER='$sem' AND ";
        $query .= "             SUBCLASSCD='$subclass' AND ";
        $query .= "             SCHREGNO='$schno' ";
        $query .= "     ) ";
        $query .= " ,SCORE_MAIN AS ( ";
        $query .= "     SELECT  T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ";
        if ($subjectflg == "B") {
            if ($divideFlg) {
                $query .= "    ,CASE WHEN T1.SCORE_DIV IN('01') AND T2.SCORE_PASS IS NOT NULL THEN T2.SCORE_PASS*0.7 ";
                $query .= "          WHEN T1.SCORE_DIV IN('01') AND T2.SCORE_PASS IS NULL     THEN T1.SCORE*0.7 ";
                $query .= "          ELSE T1.SCORE END AS SCORE ";
            } else {
                $query .= "    ,CASE WHEN T1.SCORE_DIV IN('01') AND T2.SCORE_PASS IS NOT NULL THEN T2.SCORE_PASS*0.9 ";
                $query .= "          WHEN T1.SCORE_DIV IN('01') AND T2.SCORE_PASS IS NULL     THEN T1.SCORE*0.9 ";
                $query .= "          ELSE T1.SCORE END AS SCORE ";
            }
        } else if ($subjectflg == "C") {
            $query .= "        ,CASE WHEN T1.SCORE_DIV IN('01') AND T2.SCORE_PASS IS NOT NULL THEN T2.SCORE_PASS*0.9 ";
            $query .= "              WHEN T1.SCORE_DIV IN('02') AND T2.SCORE_PASS IS NOT NULL THEN 0 ";
            $query .= "              WHEN T1.SCORE_DIV IN('01') AND T2.SCORE_PASS IS NULL     THEN T1.SCORE*0.9 ";
            $query .= "              WHEN T1.SCORE_DIV IN('02') AND T2.SCORE_PASS IS NULL     THEN T1.SCORE*0.9 ";
            $query .= "              ELSE T1.SCORE END AS SCORE ";
        } else {
            if ($divideFlg) {
                $query .= "    ,CASE WHEN T1.SCORE_DIV IN('01') AND T2.SCORE_PASS IS NOT NULL THEN T2.SCORE_PASS*0.6 ";
                $query .= "          WHEN T1.SCORE_DIV IN('01') AND T2.SCORE_PASS IS NULL     THEN T1.SCORE*0.6 ";
                $query .= "          ELSE T1.SCORE END AS SCORE ";
            } else {
                $query .= "    ,CASE WHEN T1.SCORE_DIV IN('01') AND T2.SCORE_PASS IS NOT NULL THEN T2.SCORE_PASS*0.8 ";
                $query .= "          WHEN T1.SCORE_DIV IN('01') AND T2.SCORE_PASS IS NULL     THEN T1.SCORE*0.8 ";
                $query .= "          ELSE T1.SCORE END AS SCORE ";
            }
        }
        $query .= "     FROM    SCORE T1 ";
        $query .= "             LEFT JOIN SCORE_PASS T2 ON T2.TESTKINDCD=T1.TESTKINDCD AND T2.TESTITEMCD=T1.TESTITEMCD ";
        $query .= "     ) ";

        $query .= " SELECT  SUM(SCORE) AS SCORE ";
        $query .= " FROM    SCORE_MAIN ";
        $query .= " GROUP BY SEMESTER ";
        if ($subjectflg == "B") {
            $query .= " HAVING COUNT(SCORE) = 4 ";
        } else {
            $query .= " HAVING COUNT(SCORE) = 6 ";
        }

        return $query;
    }


    //評定マスタ
    function GetAssessLevel($assesscd, $score)
    {
        $query  = "SELECT  ASSESSLEVEL ";
        $query .= "FROM    ASSESS_MST ";
        $query .= "WHERE   ASSESSCD = '$assesscd' ";
        $query .= "  AND   $score BETWEEN ASSESSLOW AND ASSESSHIGH ";
        return $query;
    }

    //前期課程（中学）かどうかを判断
    function getIsJunior($schno) {
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
    function GetAssessLevelJunior($assesscd, $score) {
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


    /* 履修単位・修得単位の自動計算処理 */
    function updateCredit($model, $tani, $i, $db)
    {
        $data = array();

        //履修／修得単位を一旦セットする
        $data["COMP_CREDIT"][NUMBER] = $tani["9"]["COMP_CREDIT"];
        $data["GET_CREDIT"][NUMBER]  = $tani["9"]["GET_CREDIT"];

        /* 履修単位・修得単位の自動計算処理 始め */

        //$notice_val[0]:欠課数(画面表示)
        //$notice_val[1]:単位数
        //$notice_val[2]:欠課数上限値
        //$notice_val[3]:半期認定フラグ
        $notice_val = explode("-", $model->fields["NOTICE_LATE"][$i]);

        //$combined_val[0]:合併元科目の履修単位の合計
        //$combined_val[1]:合併元科目の修得単位の合計
        //$combined_val[2]:合併元科目の修得単位の合計「名称予備１＝Y」の場合
        $combined_val = explode("-", $model->fields["COMBINED_CREDIT_SUM"][$i]);

        // 履修単位・修得単位が入力可で、学年評定が入力されていて、履修単位・修得単位がNULLの時
        if (($tani["9"]["VALUE"] != "") && 
            ($tani["9"]["COMP_CREDIT"] == "" && $tani["9"]["GET_CREDIT"] == ""))
        {
            // 合併先科目である
            if (isset($model->creditflg)) {
                // $model->creditflg ---> 1:単位固定タイプ 2:単位加算タイプ
                // 学年評定:２以上、履修・修得に登録
                if ($tani["9"]["VALUE"] > "1") {
                    $getCredit = ($model->repGetCredit == "Y") ? $combined_val[2] : $combined_val[1];
                    $data["COMP_CREDIT"][NUMBER] = ($model->creditflg == "2") ? $combined_val[0] : $notice_val[1];
                    $data["GET_CREDIT"][NUMBER]  = ($model->creditflg == "2") ? $getCredit : $notice_val[1];
                }
                // 学年評定:１、履修のみに登録
                else if ($tani["9"]["VALUE"] == "1") {
                    $data["COMP_CREDIT"][NUMBER] = ($model->creditflg == "2") ? $combined_val[0] : $notice_val[1];
                    $data["GET_CREDIT"][NUMBER]  = 0;
                }

            // 合併先科目でない--->今までと同じ計算処理をする
            } else {
                //単位数・学年評定がある場合のみ処理
                if ($notice_val[1] != "" && ($tani["9"]["VALUE"] != "")) {
                    $hanki_credit = $notice_val[1] / 2;
                    //①学年評定:２以上、欠課数上限値オーバー
                    if ($tani["9"]["VALUE"] > "1" && $notice_val[0] > $notice_val[2]) {
                        //半期認定科目
                        if ($notice_val[3] == "1") {
                            $data["COMP_CREDIT"][NUMBER] = floor($hanki_credit);
                            $data["GET_CREDIT"][NUMBER]  = floor($hanki_credit);
                            if ($tani["1"]["VALUE"] != "") $data["COMP_TAKESEMES"][TEXT] = "1";
                            if ($tani["2"]["VALUE"] != "") $data["COMP_TAKESEMES"][TEXT] = "2";
                        } else {
                            $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                            $data["GET_CREDIT"][NUMBER]  = $notice_val[1];
                            $data["COMP_TAKESEMES"][TEXT] = "";
                        }
                    }
                    //②学年評定:２以上、欠課数上限値内
                    elseif ($tani["9"]["VALUE"] > "1" && $notice_val[0] <= $notice_val[2]) {
                            $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                            $data["GET_CREDIT"][NUMBER]  = $notice_val[1];
                            $data["COMP_TAKESEMES"][TEXT] = "";
                    }
                    //③学年評定:１、欠課数上限値オーバー
                    elseif ($tani["9"]["VALUE"] == "1" && $notice_val[0] > $notice_val[2]) {
                        //半期認定科目
                        if ($notice_val[3] == "1") {
                            $data["COMP_CREDIT"][NUMBER] = floor($hanki_credit);
                            $data["GET_CREDIT"][NUMBER]  = 0;
                            if ($tani["1"]["VALUE"] != "") $data["COMP_TAKESEMES"][TEXT] = "1";
                            if ($tani["2"]["VALUE"] != "") $data["COMP_TAKESEMES"][TEXT] = "2";
                        } else {
                            $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                            $data["GET_CREDIT"][NUMBER]  = 0;
                            $data["COMP_TAKESEMES"][TEXT] = "";
                        }
                    }
                    //④学年評定:１、欠課数上限値内
                    elseif ($tani["9"]["VALUE"] == "1" && $notice_val[0] <= $notice_val[2]) {
                            $data["COMP_CREDIT"][NUMBER] = $notice_val[1];
                            $data["GET_CREDIT"][NUMBER]  = 0;
                            $data["COMP_TAKESEMES"][TEXT] = "";
                    }
                    //⑥その他---NO006
                    else {
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
        $where .= "  AND  SCHREGNO      = '".$model->fields["SCHREGNO"][$i]."' ";
        $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);

//var_dump($data);
        $db->query($query);
    }

}
?>
