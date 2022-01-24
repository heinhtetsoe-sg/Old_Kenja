<?php

require_once('for_php7.php');


class knjd129sQuery extends Query
{

    //「ＣＳＶは、非表示とする？」のフラグを取得
    public function getNameMstD058()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 "; // 1:非表示
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D058' AND ";
        $query .= "     NAMECD2 = '01' ";
        return $query;
    }
    //CSV取込不可チェック
    public function getNotusecsvCnt($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                                AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                                                AND L1.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "                                                AND L1.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "                                                AND L1.SCORE_DIV = T1.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.CONTROL_FLG = '1' ";
        $query .= " AND L1.NOT_USE_CSV_FLG = '1' ";
        $query .= " AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') ";
        $query .= " AND T1.CLASSCD          = '".$subclass_array[0]."' ";
        $query .= " AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
        $query .= " AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
        $query .= " AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";

        return $query;
    }
    //科目名の取得(CSV用)
    public function getSubclassName($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     CLASSCD          = '".$subclass_array[0]."' AND ";
        $query .= "     SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
        $query .= "     CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
        $query .= "     SUBCLASSCD       = '".$subclass_array[3]."' ";
        return $query;
    }
    //講座名の取得(CSV用)
    public function getChairName($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        return $query;
    }

    //学校の判別
    public function getNameMstZ010()
    {
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
    public function getCalcMethod()
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_REMARK1  ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_SEQ = '007' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //科目別設定があるか
    public function getTestSubCnt($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
//        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') "; //この画面では学年評定を参照しない
        $query .= " AND T1.CLASSCD          = '".$subclass_array[0]."' ";
        $query .= " AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
        $query .= " AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
        $query .= " AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";

        return $query;
    }

    //テスト名取得
    public function getTestName($model, $testSubCnt)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T2.SEMESTERNAME, ";
        $query .= "     T2.SDATE, ";
        $query .= "     T2.EDATE, ";
        $query .= "     T3.TESTITEMABBV1 AS TESTITEMNAME, ";
        $query .= "     T3.SIDOU_INPUT, ";
        $query .= "     T3.SIDOU_INPUT_INF, ";
        if ($model->Properties["useMikomiFlg"] == '1') {
            $query .= "     T3.MIKOMI_FLG, ";
        }
        if ($model->Properties["useSankouFlg"] == '1') {
            $query .= "     T3.SANKOU_FLG, ";
        }
        if ($model->Properties["useRemarkFlg"] == '1') {
            $query .= "     T3.REMARK_FLG, ";
        }
        $query .= "     T1.CONTROL_FLG ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST T2 ";
        $query .= "         ON  T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.SEMESTER     = T1.SEMESTER ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T3 ";
        $query .= "         ON  T3.YEAR         = T1.YEAR ";
        $query .= "         AND T3.SEMESTER     = T1.SEMESTER ";
        $query .= "         AND T3.TESTKINDCD   = T1.TESTKINDCD ";
        $query .= "         AND T3.TESTITEMCD   = T1.TESTITEMCD ";
        $query .= "         AND T3.SCORE_DIV    = T1.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
//        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') "; //この画面では学年評定を参照しない
        //科目別設定がある場合は、科目別設定。それ以外は基本設定されたパーツのみ表示する。
        $subclasscd = (0 < $testSubCnt) ? $model->field["SUBCLASSCD"] : $model->subclassAll;
        $subclass_array = array();
        $subclass_array = explode("-", $subclasscd);
        $query .= "          AND T1.CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "          AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "          AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "          AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
        $query .= " ORDER BY ";
        $query .= "     TESTCD ";

        return $query;
    }

    //算出元科目別設定があるか
    public function getTestMotoSubCnt($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_CALC_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
//        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') "; //この画面では学年評定を参照しない
        $query .= " AND T1.CLASSCD          = '".$subclass_array[0]."' ";
        $query .= " AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
        $query .= " AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
        $query .= " AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";

        return $query;
    }

    //算出元テスト取得
    public function getTestMoto($model, $testSubCnt)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T2.TESTITEMNAME AS TESTNAME, ";
        $query .= "     T1.MOTO_SEMESTER || T1.MOTO_TESTKINDCD || T1.MOTO_TESTITEMCD || T1.MOTO_SCORE_DIV AS MOTO_TESTCD, ";
        $query .= "     T3.TESTITEMNAME AS MOTO_TESTNAME, ";
        $query .= "     T1.SEMESTER ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_CALC_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
        $query .= "         ON  T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.SEMESTER     = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD   = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD   = T1.TESTITEMCD ";
        $query .= "         AND T2.SCORE_DIV    = T1.SCORE_DIV ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T3 ";
        $query .= "         ON  T3.YEAR         = T1.YEAR ";
        $query .= "         AND T3.SEMESTER     = T1.MOTO_SEMESTER ";
        $query .= "         AND T3.TESTKINDCD   = T1.MOTO_TESTKINDCD ";
        $query .= "         AND T3.TESTITEMCD   = T1.MOTO_TESTITEMCD ";
        $query .= "         AND T3.SCORE_DIV    = T1.MOTO_SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
//        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') "; //この画面では学年評定を参照しない
        //科目別設定がある場合は、科目別設定。それ以外は基本設定されたパーツのみ表示する。
        $subclasscd = (0 < $testSubCnt) ? $model->field["SUBCLASSCD"] : $model->subclassAll;
        $subclass_array = array();
        $subclass_array = explode("-", $subclasscd);
        $query .= "          AND T1.CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "          AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "          AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "          AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
        $query .= " ORDER BY ";
        $query .= "     TESTCD, ";
        $query .= "     MOTO_TESTCD ";

        return $query;
    }

    //科目データの取得
    public function getSubclassMst($model)
    {
        $query  = "";
        $query .= " WITH REPLACE AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         '1' AS DIV, ";
        $query .= "         COMBINED_CLASSCD, ";
        $query .= "         COMBINED_SCHOOL_KIND, ";
        $query .= "         COMBINED_CURRICULUM_CD, ";
        $query .= "         COMBINED_SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
        $query .= "     CASE WHEN L1.COMBINED_SUBCLASSCD IS NOT NULL THEN '●' ELSE '　' END || T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T4, ";
        $query .= "     SUBCLASS_MST T3 ";
        $query .= "     LEFT JOIN REPLACE L1 ON L1.DIV = '1' ";
        $query .= "         AND T3.CLASSCD = L1.COMBINED_CLASSCD ";
        $query .= "         AND T3.SCHOOL_KIND = L1.COMBINED_SCHOOL_KIND ";
        $query .= "         AND T3.CURRICULUM_CD = L1.COMBINED_CURRICULUM_CD ";
        $query .= "         AND T3.SUBCLASSCD = L1.COMBINED_SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
        $query .= "     AND T2.YEAR       = T1.YEAR ";
        $query .= "     AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     AND T4.YEAR       = T1.YEAR ";
        $query .= "     AND T4.SEMESTER   = T1.SEMESTER ";
        $query .= "     AND T4.CHAIRCD    = T1.CHAIRCD ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND T4.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "     AND T3.SCHOOL_KIND = '{$model->school_kind}' ";
        $query .= "     AND T3.CLASSCD = T1.CLASSCD ";
        $query .= "     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "     AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     AND (T3.SUBCLASSCD NOT LIKE '9%' OR T3.SUBCLASSCD LIKE '90%' ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //講座データの取得
    public function selectChairQuery($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || '：' || T1.chairname AS LABEL ";
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
            $query .= "       t3.STAFFCD = '".STAFFCD."' AND ";
        }
        $query .= "     T1.CLASSCD          = '".$subclass_array[0]."' AND ";
        $query .= "     T1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
        $query .= "     T1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
        $query .= "     T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //V学校マスタ
    public function getVSchoolMst()
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $execute_date)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = "";
        $query .= " SELECT W1.schregno";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.inoutcd, W2.name_show, W2.grd_date, G1.GROUP_CD, W2.COURSE";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
        $query .= "       ,W4.transfer_sdate1, W4.transfer_edate1 ";
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "       ,W5.PROV_FLG ";
        }
        $query .= "   FROM ";
        /* 生徒一覧 */
        $query .= "    (SELECT schregno ";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year = '".CTRL_YEAR."'  ";
        $query .= "        AND chaircd = '".$model->field["CHAIRCD"]."'  ";
        $query .= "        AND '".$execute_date."' between appdate AND appenddate ";
        $query .= "    ) W1 ";
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
        $query .= "   ON W2.schregno = W1.schregno ";
        /* 異動情報（留学以外） */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.schregno, MIN(T1.transfer_sdate) AS transfer_sdate, MAX(T1.transfer_edate) AS transfer_edate";
        $query .= "       FROM schreg_transfer_dat T1 ";
        $query .= "      WHERE FISCALYEAR(T1.transfer_sdate) = '".CTRL_YEAR."' and T1.TRANSFERCD NOT IN ('1') ";
        $query .= "      GROUP BY T1.schregno ) W3 ";
        $query .= "   ON W3.schregno = W1.schregno ";
        /* 異動情報（留学のみ） */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.schregno, MIN(T1.transfer_sdate) AS transfer_sdate1, MAX(T1.transfer_edate) AS transfer_edate1 ";
        $query .= "       FROM schreg_transfer_dat T1 ";
        $query .= "      WHERE FISCALYEAR(T1.transfer_sdate) = '".CTRL_YEAR."' and T1.TRANSFERCD = '1' ";
        $query .= "      GROUP BY T1.schregno ) W4 ";
        $query .= "   ON W4.schregno = W1.schregno ";
        /* コースグループ情報 */
        $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT G1 ";
        $query .= "         ON G1.YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND G1.GRADE = W2.grade ";
        $query .= "        AND G1.COURSECD || G1.MAJORCD || G1.COURSECODE = W2.COURSE ";
        /* 仮評定情報 */
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= " LEFT JOIN ";
            $query .= "    (SELECT  SCHREGNO, PROV_FLG ";
            $query .= "     FROM   RECORD_PROV_FLG_DAT ";
            $query .= "     WHERE  YEAR = '".CTRL_YEAR."' ";
            $query .= "       AND  CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "       AND  SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "       AND  CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "       AND  SUBCLASSCD       = '".$subclass_array[3]."' ";
            $query .= "    ) W5 ";
            $query .= "   ON W5.SCHREGNO = W1.SCHREGNO ";
        }

        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";
        return $query;
    }

    //成績データ
    public function getScore($model, $execute_date)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH SELECT_T AS ( ".knjd129sQuery::selectQuery($model, $execute_date).") ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.COMP_CREDIT, ";
        $query .= "     T1.GET_CREDIT, ";
        $query .= "     T1.VALUE_DI, "; //欠試'*'
        if ($model->Properties["useMikomiFlg"] == '1') {
            $query .= "     CASE WHEN SUPP.SCORE_FLG = '2' AND T1.VALUE_DI = '*' "; //2:見込点(欠席者)
            $query .= "          THEN SUPP.SCORE ";
            $query .= "     END AS SCORE_PASS, ";
        } else {
            $query .= "     CASE WHEN SUPP.SCORE_FLG = '2' "; //2:見込点
            $query .= "          THEN SUPP.SCORE ";
            $query .= "     END AS SCORE_PASS, ";
        }
        $query .= "     SUPP.SCORE_FLG ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     LEFT JOIN SUPP_EXA_SDIV_DAT SUPP ";
        $query .= "           ON T1.YEAR = SUPP.YEAR ";
        $query .= "          AND T1.SEMESTER = SUPP.SEMESTER ";
        $query .= "          AND T1.TESTKINDCD = SUPP.TESTKINDCD ";
        $query .= "          AND T1.TESTITEMCD = SUPP.TESTITEMCD ";
        $query .= "          AND T1.SCORE_DIV = SUPP.SCORE_DIV ";
        $query .= "          AND T1.CLASSCD = SUPP.CLASSCD ";
        $query .= "          AND T1.SCHOOL_KIND = SUPP.SCHOOL_KIND ";
        $query .= "          AND T1.CURRICULUM_CD = SUPP.CURRICULUM_CD ";
        $query .= "          AND T1.SUBCLASSCD = SUPP.SUBCLASSCD ";
        $query .= "          AND T1.SCHREGNO = SUPP.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
        $query .= "     AND T1.SCHREGNO IN (SELECT I1.SCHREGNO FROM SELECT_T I1) ";
        return $query;
    }

    //考査満点マスタ
    public function getPerfect($year, $testcd, $grade, $course, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     case when T1.DIV IS NULL then 100 else T1.PERFECT end as PERFECT ";
        $query .= " FROM ";
        $query .= "     PERFECT_RECORD_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT G1 ";
        $query .= "         ON G1.YEAR = T1.YEAR ";
        $query .= "        AND G1.GRADE = '{$grade}' ";
        $query .= "        AND G1.COURSECD || G1.MAJORCD || G1.COURSECODE = '{$course}' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.CLASSCD       = '".$subclass_array[0]."' ";
        $query .= "     AND T1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
        $query .= "     AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
        $query .= "     AND T1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$testcd}' ";
        $query .= "     AND T1.GRADE = CASE WHEN T1.DIV = '01' THEN '00' ELSE '{$grade}' END ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ";
        $query .= "         CASE WHEN T1.DIV IN ('01','02') ";
        $query .= "              THEN '00000000' ";
        $query .= "              WHEN T1.DIV = '04' ";
        $query .= "              THEN '0' || VALUE(G1.GROUP_CD, '000') || '0000' ";
        $query .= "              ELSE '{$course}' END ";

        return $query;
    }

    //割合設定
    public function getPercent($year, $testcd, $grade, $course, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.RATE as PERCENT ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_RATE_GRADE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.CLASSCD       = '".$subclass_array[0]."' ";
        $query .= "     AND T1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
        $query .= "     AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
        $query .= "     AND T1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        $query .= "     AND T1.DIV = '3' "; //3:コース
        $query .= "     AND T1.GRADE = '{$grade}' ";
        $query .= "     AND T1.HR_CLASS = '000' ";
        $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$course}' ";

        return $query;
    }

    //成績入力完了チェック
    public function getRecordChkfinDat($model, $testcd)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECORD_CHKFIN_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD || RECORD_DIV = '{$testcd}' ";
        $query .= "     AND CLASSCD         = '".$subclass_array[0]."' ";
        $query .= "     AND SCHOOL_KIND     = '".$subclass_array[1]."' ";
        $query .= "     AND CURRICULUM_CD   = '".$subclass_array[2]."' ";
        $query .= "     AND SUBCLASSCD      = '".$subclass_array[3]."' ";
        $query .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";

        return $query;
    }

    //成績入力完了更新
    public function updateRecordChkfin($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $db = Query::dbCheckOut();

        //初期化
        foreach ($model->testcdArray as $key => $codeArray) {
            $testcd = $codeArray["TESTCD"];
            $controlFlg = $codeArray["CONTROL_FLG"];

            $query = knjd129sQuery::getRecordChkfinDat($model, $testcd);
            $resultRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($resultRow) && $controlFlg == "1") {
                $chkfg = ($model->field["CHK_COMP".$testcd] == 'on') ? '1' : '0';

                $data = array();
                $data["EXECUTED"][TEXT]   = $chkfg ;     //成績入力区分
                $data["REGISTERCD"][TEXT] = STAFFCD ;    //登録者コード
                $data["UPDATED"][FUNC]    = "sysdate()"; //更新日付

                $where  = " WHERE ";
                $where .= "         YEAR = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD || RECORD_DIV = '{$testcd}' ";
                $where .= "     AND CLASSCD          = '".$subclass_array[0]."' ";
                $where .= "     AND SCHOOL_KIND      = '".$subclass_array[1]."' ";
                $where .= "     AND CURRICULUM_CD    = '".$subclass_array[2]."' ";
                $where .= "     AND SUBCLASSCD       = '".$subclass_array[3]."' ";
                $where .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";

                $query = Query::updateSQL($data, "RECORD_CHKFIN_SDIV_DAT", $where);
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    /* 成績データ更新処理 */
    public function update($model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $year = CTRL_YEAR;
        for ($i = 0; $i < $model->field["COUNTER"]; $i++) {
            $schno = $model->fields["SCHREGNO"][$i];
            foreach ($model->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];
                $controlFlg = $codeArray["CONTROL_FLG"];
                $score = $model->fields["SCORE".$testcd][$i];
                //管理者コントロール
                if ($controlFlg == "1") {
                    knjd129sQuery::delInsRecordScore($year, $testcd, $model, $schno, $score, $db, $i);
                }
            }
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //更新
    public function delInsRecordScore($year, $testcd, $model, $schno, $score, $db, $i)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = substr($testcd, 0, 1);
        $data["TESTKINDCD"][TEXT]   = substr($testcd, 1, 2);
        $data["TESTITEMCD"][TEXT]   = substr($testcd, 3, 2);
        $data["SCORE_DIV"][TEXT]    = substr($testcd, 5, 2);
        $data["CLASSCD"][TEXT]          = $subclass_array[0];
        $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
        $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
        $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
        $data["SCHREGNO"][TEXT]     = $schno;
        $data["SCORE"][NUMBER]      = $score == "*" ? "" : $score;
        $data["VALUE_DI"][TEXT]     = $score == "*" ? $score : "";
        $data["CHAIRCD"][TEXT]      = $model->field["CHAIRCD"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        //学年評定を更新する場合、学年成績を５段階に換算しセット
        if ($testcd == "9990009") {
            $gTestcd = "9990008";
            $gScore = $model->fields["SCORE".$gTestcd][$i];
            //学年成績あり、学年評定なし、の場合
            if (strlen($gScore) && $gScore != "*" && !strlen($score)) {
                $query = knjd129sQuery::getAssessSubclassMst($model, $year, $schno, $gScore);
                $score = $db->getOne($query);
                $data["SCORE"][NUMBER] = $score;
            }
        }

        //追加および更新
        //testInsArray：成績が１つでも入力された考査コードを配列に保管
        if (in_array($testcd, $model->testInsArray) || $testcd == "9990009" && in_array($gTestcd, $model->testInsArray)) {
            $query = knjd129sQuery::existRecordScore($year, $testcd, $schno, $model);
            $existflg = $db->getOne($query);
            if (0 < $existflg) {
                $where .= "WHERE  YEAR          = '{$year}' ";
                $where .= "  AND  SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' ";
                $where .= "  AND  CLASSCD        = '".$subclass_array[0]."' ";
                $where .= "  AND  SCHOOL_KIND    = '".$subclass_array[1]."' ";
                $where .= "  AND  CURRICULUM_CD  = '".$subclass_array[2]."' ";
                $where .= "  AND  SUBCLASSCD     = '".$subclass_array[3]."' ";
                $where .= "  AND  SCHREGNO       = '{$schno}' ";
                $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            }
            $db->query($query);
        //削除
        //成績が1件も入力されていない場合、RECORD_SCORE_DATのレコードは作成しない（考査ごと）
        } else {
            $query = knjd129sQuery::deleteRecordScore($year, $testcd, $schno, $model);
            $db->query($query);
        }

        //評定フラグ・・・仮評定(1)、本評定(NULL)
        //学年評定を更新する場合、評定フラグは本評定(NULL)とする・・・レコードを削除またはNULLで更新(どちらでもよい)
        if ($testcd == "9990009") {
            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                $query = knjd129sQuery::delRecordProvFlgDat($year, $model->field["SUBCLASSCD"], $schno, $model);
                $db->query($query);
                if ($model->fields["PROV_FLG"][$i] == '1') {
                    $query = knjd129sQuery::insRecordProvFlgDat($year, $model->field["SUBCLASSCD"], $schno, $model);
                    $db->query($query);
                }
            }
        }
    }

    //テーブルRECORD_SCORE_DATの存在チェック用SQL
    public function existRecordScore($year, $testcd, $schno, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = "";
        $query .= "SELECT COUNT(*) ";
        $query .= "FROM   RECORD_SCORE_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        $query .= "  AND  SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' ";
        $query .= "  AND  CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "  AND  SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "  AND  CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "  AND  SUBCLASSCD     = '".$subclass_array[3]."' ";
        $query .= "  AND  SCHREGNO       = '{$schno}' ";
        return $query;
    }

    //テーブルRECORD_SCORE_DATのレコードを削除
    public function deleteRecordScore($year, $testcd, $schno, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_SCORE_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        $query .= "  AND  SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' ";
        $query .= "  AND  CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "  AND  SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "  AND  CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "  AND  SUBCLASSCD     = '".$subclass_array[3]."' ";
        $query .= "  AND  SCHREGNO       = '{$schno}' ";
        return $query;
    }

    //テーブルRECORD_PROV_FLG_DATのレコードを削除
    public function delRecordProvFlgDat($year, $subclass, $schno, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $subclass);

        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_PROV_FLG_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        $query .= "  AND  CLASSCD        = '".$subclass_array[0]."' ";
        $query .= "  AND  SCHOOL_KIND    = '".$subclass_array[1]."' ";
        $query .= "  AND  CURRICULUM_CD  = '".$subclass_array[2]."' ";
        $query .= "  AND  SUBCLASSCD     = '".$subclass_array[3]."' ";
        $query .= "  AND  SCHREGNO      = '{$schno}' ";
        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを追加
    public function insRecordProvFlgDat($year, $subclass, $schno, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $subclass);

        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["CLASSCD"][TEXT]       = $subclass_array[0];
        $data["SCHOOL_KIND"][TEXT]   = $subclass_array[1];
        $data["CURRICULUM_CD"][TEXT] = $subclass_array[2];
        $data["SUBCLASSCD"][TEXT]    = $subclass_array[3];
        $data["SCHREGNO"][TEXT]     = $schno;
        //評定フラグ・・・仮評定(1)、本評定(NULL)
        $data["PROV_FLG"][TEXT]     = "1";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_PROV_FLG_DAT");
        return $query;
    }

    //学年成績を５段階に換算し取得
    public function getAssessSubclassMst($model, $year, $schno, $gScore)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     R1.ASSESSLEVEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN ASSESS_SUBCLASS_MST R1 ";
        $query .= "             ON  R1.YEAR         = '{$year}' ";
        $query .= "             AND R1.GRADE        = T1.GRADE ";
        $query .= "             AND R1.COURSECD     = T1.COURSECD ";
        $query .= "             AND R1.MAJORCD      = T1.MAJORCD ";
        $query .= "             AND R1.COURSECODE   = T1.COURSECODE ";
        $query .= "             AND R1.CLASSCD      = '".$subclass_array[0]."' ";
        $query .= "             AND R1.SCHOOL_KIND  = '".$subclass_array[1]."' ";
        $query .= "             AND R1.CURRICULUM_CD= '".$subclass_array[2]."' ";
        $query .= "             AND R1.SUBCLASSCD   = '".$subclass_array[3]."' ";
        $query .= "             AND {$gScore} BETWEEN R1.ASSESSLOW AND R1.ASSESSHIGH ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.SCHREGNO = '{$schno}' ";

        return $query;
    }
}
