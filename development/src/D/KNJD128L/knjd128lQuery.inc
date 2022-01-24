<?php

require_once('for_php7.php');


class knjd128lquery extends Query {

    //「評価にリンクを付けるか？」のフラグを取得
    function getNameMstD048()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D048' AND ";
        $query .= "     NAMECD2 = '01' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
    function getCalcMethod() {
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
    function getTestSubCnt($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
//        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') "; //この画面では学年評定を参照しない
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' ";
        }

        return $query;
    }

    //名称マスタA023に登録されているの学校校種を取得
    function getSchoolkindQuery() {
    
        $query  = " SELECT ";
        $query .= "     MIN(NAME1) AS SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";

        return $query;
    }

    //テスト名取得
    function getTestName($model, $testSubCnt) {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T2.SEMESTERNAME, ";
        $query .= "     T2.SDATE, ";
        $query .= "     T2.EDATE, ";
        $query .= "     T3.TESTITEMABBV1 AS TESTITEMNAME, ";
        $query .= "     T3.SIDOU_INPUT, ";
        $query .= "     T3.SIDOU_INPUT_INF, ";
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
            $query .= "          AND T1.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND T1.SUBCLASSCD     = '".$subclasscd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     TESTCD ";

        return $query;
    }

    //算出元科目別設定があるか
    function getTestMotoSubCnt($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_CALC_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
//        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') "; //この画面では学年評定を参照しない
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' ";
        }

        return $query;
    }

    //算出元テスト取得
    function getTestMoto($model, $testSubCnt) {
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
            $query .= "          AND T1.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND T1.SUBCLASSCD     = '".$subclasscd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     TESTCD, ";
        $query .= "     MOTO_TESTCD ";

        return $query;
    }

    //科目データの取得
    function getSubclassMst($model) {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T4, ";
        $query .= "     SUBCLASS_MST T3 ";
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T3.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "     AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     AND (T3.SUBCLASSCD NOT LIKE '9%' OR T3.SUBCLASSCD LIKE '90%' ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //講座データの取得
    function selectChairQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
    
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
        if(AUTHORITY != DEF_UPDATABLE){
          $query .= "       t3.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     T1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "     T1.subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //V学校マスタ
    function getVSchoolMst() {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //累積最大日取得
    function GetMax($subclasscd, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "          AND semester   = (SELECT MAX(semester) FROM attend_subclass_dat ";
        $query .= "                             WHERE COPYCD     = '0' ";
        $query .= "                               AND year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                               AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "                               AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "                               AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "                               AND SUBCLASSCD     = '".$subclass_array[3]."') ";
        } else {
            $query .= "                               AND subclasscd = '".$model->field["SUBCLASSCD"]."') ";
        }
        $query .= "ORDER BY tmp_month DESC ";
        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $execute_date) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= " SELECT W1.schregno";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.inoutcd, W2.name_show, W2.grd_date, W2.COURSE";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "       ,W4.PROV_FLG ";
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
        /* 異動情報 */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.schregno, MIN(T1.transfer_sdate) AS transfer_sdate, MAX(T1.transfer_edate) AS transfer_edate";
        $query .= "       FROM schreg_transfer_dat T1 ";
        $query .= "      WHERE FISCALYEAR(T1.transfer_sdate) = '".CTRL_YEAR."'";
        $query .= "      GROUP BY T1.schregno ) W3 ";
        $query .= "   ON W3.schregno = W1.schregno ";
        /* 仮評定情報 */
        //仮評定フラグ対応
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= " LEFT JOIN ";
            $query .= "    (SELECT  SCHREGNO, PROV_FLG ";
            $query .= "     FROM   RECORD_PROV_FLG_DAT ";
            $query .= "     WHERE  YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "       AND  CLASSCD          = '".$subclass_array[0]."' ";
                $query .= "       AND  SCHOOL_KIND      = '".$subclass_array[1]."' ";
                $query .= "       AND  CURRICULUM_CD    = '".$subclass_array[2]."' ";
                $query .= "       AND  SUBCLASSCD       = '".$subclass_array[3]."' ";
            } else {
                $query .= "       AND  SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
            $query .= "    ) W4 ";
            $query .= "   ON W4.SCHREGNO = W1.SCHREGNO ";
        }

        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";
        return $query;
    }

    //成績データ
    function getScore($year, $testcd, $schregno, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= "SELECT ";
        $query .= "    SCORE, ";
        $query .= "    COMP_CREDIT, ";
        $query .= "    GET_CREDIT, ";
        $query .= "    VALUE_DI ";
        $query .= "FROM ";
        $query .= "    RECORD_SCORE_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        $query .= "    SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '{$subclass_array[0]}' AND ";
            $query .= "     SCHOOL_KIND     = '{$subclass_array[1]}' AND ";
            $query .= "     CURRICULUM_CD   = '{$subclass_array[2]}' AND ";
            $query .= "     SUBCLASSCD      = '{$subclass_array[3]}' AND ";
        } else {
            $query .= "     SUBCLASSCD      = '{$model->field["SUBCLASSCD"]}' AND ";
        }
        $query .= "    SCHREGNO = '{$schregno}' ";
        return $query;
    }

    //追指導データ
    function getRecordSlump($year, $testcd, $schregno, $grade, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        //相対評定マスタ
        $query .= "WITH RELA_CNT AS ( ";
        $query .= "    SELECT ";
        $query .= "        'CNT' AS KEY, ";
        $query .= "        COUNT(*) AS CNT ";
        $query .= "    FROM ";
        $query .= "        RELATIVEASSESS_MST RELA ";
        $query .= "    WHERE ";
        $query .= "        RELA.ASSESSCD = '3' ";
        $query .= "        AND RELA.GRADE = '{$grade}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND RELA.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "    AND RELA.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "    AND RELA.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "    AND RELA.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "    AND RELA.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "    ) ";
        //メイン
        $query .= "SELECT ";
        $query .= "    CASE WHEN 0 < RELA_CNT.CNT THEN RELA.ASSESSLEVEL ELSE ASSESS.ASSESSLEVEL END AS ASSESSLEVEL, ";
        $query .= "    CASE WHEN 0 < RELA_CNT.CNT THEN RELA2.ASSESSLOW ELSE ASSESS2.ASSESSLOW END AS ASSESSLOW2, ";
        $query .= "    SLUMP.SCORE, ";
        $query .= "    SLUMP.MARK ";
        $query .= "FROM   RECORD_SLUMP_SDIV_DAT SLUMP ";
        $query .= "       LEFT JOIN ASSESS_MST ASSESS ";
        $query .= "             ON  ASSESS.ASSESSCD = '3' ";
        $query .= "             AND SLUMP.SCORE BETWEEN ASSESS.ASSESSLOW AND ASSESS.ASSESSHIGH ";
        $query .= "       LEFT JOIN RELA_CNT RELA_CNT ";
        $query .= "             ON  RELA_CNT.KEY = 'CNT' ";
        $query .= "       LEFT JOIN RELATIVEASSESS_MST RELA ";
        $query .= "             ON  RELA.ASSESSCD = '3' ";
        $query .= "             AND SLUMP.SCORE BETWEEN RELA.ASSESSLOW AND RELA.ASSESSHIGH ";
        $query .= "             AND RELA.GRADE = '{$grade}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND RELA.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "         AND RELA.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "         AND RELA.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "         AND RELA.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND RELA.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "       LEFT JOIN ASSESS_MST ASSESS2 ";
        $query .= "             ON  ASSESS2.ASSESSCD = '3' ";
        $query .= "             AND ASSESS2.ASSESSLEVEL = 2 ";
        $query .= "       LEFT JOIN RELATIVEASSESS_MST RELA2 ";
        $query .= "             ON  RELA2.ASSESSCD = '3' ";
        $query .= "             AND RELA2.ASSESSLEVEL = 2 ";
        $query .= "             AND RELA2.GRADE = '{$grade}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND RELA2.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "         AND RELA2.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "         AND RELA2.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "         AND RELA2.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND RELA2.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "WHERE  SLUMP.YEAR          = '{$year}' ";
        $query .= "  AND  SLUMP.SEMESTER || SLUMP.TESTKINDCD || SLUMP.TESTITEMCD || SLUMP.SCORE_DIV = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND SLUMP.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "  AND SLUMP.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "  AND SLUMP.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "  AND SLUMP.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND SLUMP.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "  AND  SLUMP.SCHREGNO      = '{$schregno}' ";
        return $query;
    }

    //考査満点マスタ
    function getPerfect($year, $subclasscd, $testcd, $grade, $course, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = "";
        $query .= "SELECT ";
        $query .= "    case when DIV IS NULL then 100 else PERFECT end as PERFECT ";
        $query .= "FROM ";
        $query .= "    PERFECT_RECORD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '{$year}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "    SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "    CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "    SUBCLASSCD       = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "    SUBCLASSCD = '{$subclasscd}' AND ";
        }
        $testcd2 = substr($testcd, 0, 5);
        $query .= "    SEMESTER || TESTKINDCD || TESTITEMCD = '{$testcd2}' AND ";
        $query .= "    GRADE = CASE WHEN DIV = '01' THEN '00' ELSE '{$grade}' END AND ";
        $query .= "    COURSECD || MAJORCD || COURSECODE = CASE WHEN DIV IN ('01','02') THEN '00000000' ELSE '{$course}' END ";
        return $query;
    }

    //欠課数上限値
    function getKeekaOver($model, $schno, $year, $grade, $course, $schoolRow) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }

        $query  = "";
        //法定時数(1)
        if ($schoolRow["JUGYOU_JISU_FLG"] == "1") {
            $query .= " SELECT ";
            $query .= "     ABSENCE_HIGH AS COMP_ABSENCE_HIGH ";
            $query .= " FROM ";
            $query .= "     CREDIT_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$year}' ";
            $query .= "     AND GRADE = '{$grade}' ";
            $query .= "     AND COURSECD || MAJORCD || COURSECODE = '{$course}' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND CLASSCD       = '".$subclass_array[0]."' ";
                $query .= "     AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
                $query .= "     AND CURRICULUM_CD = '".$subclass_array[2]."' ";
                $query .= "     AND SUBCLASSCD    = '".$subclass_array[3]."' ";
            } else {
                $query .= "     AND SUBCLASSCD    = '{$model->field["SUBCLASSCD"]}' ";
            }

        //実時数(2)
        } else {
            $query .= " SELECT ";
            $query .= "     COMP_ABSENCE_HIGH ";
            $query .= " FROM ";
            $query .= "     SCHREG_ABSENCE_HIGH_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$year}' ";
            $query .= "     AND DIV = '1' "; //年間
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND CLASSCD       = '".$subclass_array[0]."' ";
                $query .= "     AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
                $query .= "     AND CURRICULUM_CD = '".$subclass_array[2]."' ";
                $query .= "     AND SUBCLASSCD    = '".$subclass_array[3]."' ";
            } else {
                $query .= "     AND SUBCLASSCD    = '{$model->field["SUBCLASSCD"]}' ";
            }
            $query .= "     AND SCHREGNO = '{$schno}' ";

        }
        return $query;
    }

    //(年間ごとに集計）する場合
    function getAttendData($schno, $absent_cov, $absent_cov_late, $schoolRow, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($schoolRow["SUB_OFFDAYS"]  == "1") $notice .= " + SUM(value(OFFDAYS,0))";
        if ($schoolRow["SUB_ABSENT"]   == "1") $notice .= " + SUM(value(ABSENT,0))";
        if ($schoolRow["SUB_SUSPEND"]  == "1") $notice .= " + SUM(value(SUSPEND,0))";
        if ($schoolRow["SUB_MOURNING"] == "1") $notice .= " + SUM(value(MOURNING,0))";
        if ($schoolRow["SUB_VIRUS"]    == "1") $notice .= " + SUM(value(VIRUS,0))";
        if ($schoolRow["SUB_KOUDOME"] == "1")  $notice .= " + SUM(value(KOUDOME,0))";
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        /* 出欠累積情報 */
        $query  = "SELECT schregno ";
        $query .= "      ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0)) - SUM(VALUE(SUSPEND,0)) - SUM(VALUE(MOURNING,0))";
        if ($model->Properties["useVirus"] == 'true') $query .= " - SUM(VALUE(VIRUS,0))";
        if ($model->Properties["useKoudome"] == 'true') $query .= " - SUM(VALUE(KOUDOME,0))";
        if ($schoolRow["SUB_OFFDAYS"]  == "1") $query .= " + SUM(VALUE(OFFDAYS,0))";
        if ($schoolRow["SUB_SUSPEND"]  == "1") $query .= " + SUM(VALUE(SUSPEND,0))";
        if ($schoolRow["SUB_MOURNING"] == "1") $query .= " + SUM(VALUE(MOURNING,0))";
        if ($schoolRow["SUB_VIRUS"]    == "1") $query .= " + SUM(VALUE(VIRUS,0))";
        if ($schoolRow["SUB_KOUDOME"]  == "1") $query .= " + SUM(VALUE(KOUDOME,0))";
        $query .= "       AS LESSON ";
        $query .= "       ,".$notice."  AS T_NOTICE ";
        //小数点ありAdd
        if ($absent_cov == "4" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "   ,DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS NOTICE_LATE ";

        } elseif ($absent_cov == "5" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {

            $query .= "   ,CASE WHEN MOD({$late_early} , {$absent_cov_late}) >= {$schoolRow["AMARI_KURIAGE"]} ";
            $query .= "         THEN 0 ";
            $query .= "         ELSE MOD({$late_early},{$absent_cov_late}) ";
            $query .= "    END AS T_LATEEARLY ";
            $query .= "   ,(({$late_early}) / {$absent_cov_late}) + ({$notice}) + (CASE WHEN MOD({$late_early} , {$absent_cov_late}) >= {$schoolRow["AMARI_KURIAGE"]} ";
            $query .= "                                                  THEN 1 ";
            $query .= "                                                  ELSE 0 ";
            $query .= "                                             END) AS NOTICE_LATE ";

        } elseif ($absent_cov == "2" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "   ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";

        } else {
            $query .= "   ,".$late_early."     AS T_LATEEARLY ";
            $query .= "   ,".$notice."  AS NOTICE_LATE ";
        }
        $query .= "       ,".$late_early."     AS LATE_EARLY ";
        $query .= "  FROM attend_subclass_dat  ";
        $query .= " WHERE COPYCD     = '0' ";
        $query .= "   AND year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "   AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "   AND CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "   AND SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "   AND SUBCLASSCD    = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "   AND schregno  = '{$schno}' ";
        $query .= " GROUP BY schregno";

        return $query;
    }
    //(学期ごとに集計）する場合
    function getAttendData2($schno, $absent_cov, $absent_cov_late, $schoolRow, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($schoolRow["SUB_OFFDAYS"]  == "1") $notice .= " + SUM(value(OFFDAYS,0))";
        if ($schoolRow["SUB_ABSENT"]   == "1") $notice .= " + SUM(value(ABSENT,0))";
        if ($schoolRow["SUB_SUSPEND"]  == "1") $notice .= " + SUM(value(SUSPEND,0))";
        if ($schoolRow["SUB_MOURNING"] == "1") $notice .= " + SUM(value(MOURNING,0))";
        if ($schoolRow["SUB_VIRUS"]    == "1") $notice .= " + SUM(value(VIRUS,0))";
        if ($schoolRow["SUB_KOUDOME"] == "1")  $notice .= " + SUM(value(KOUDOME,0))";
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        $query  = "SELECT schregno ";
        $query .= "       ,SUM(LESSON)      AS LESSON";
        $query .= "       ,SUM(T_NOTICE)    AS T_NOTICE";
        $query .= "       ,SUM(T_LATEEARLY) AS T_LATEEARLY";
        $query .= "       ,SUM(NOTICE_LATE) AS NOTICE_LATE";
        $query .= "       ,SUM(LATE_EARLY)  AS LATE_EARLY";
        $query .= "  FROM ";
        $query .= "       (SELECT  T1.schregno ";
        $query .= "               ,T1.semester ";

        $query .= "               ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0)) - SUM(VALUE(SUSPEND,0)) - SUM(VALUE(MOURNING,0))";
        if ($model->Properties["useVirus"] == 'true') $query .= " - SUM(VALUE(VIRUS,0))";
        if ($model->Properties["useKoudome"] == 'true') $query .= " - SUM(VALUE(KOUDOME,0))";
        if ($schoolRow["SUB_OFFDAYS"]  == "1") $query .= " + SUM(VALUE(OFFDAYS,0))";
        if ($schoolRow["SUB_SUSPEND"]  == "1") $query .= " + SUM(VALUE(SUSPEND,0))";
        if ($schoolRow["SUB_MOURNING"] == "1") $query .= " + SUM(VALUE(MOURNING,0))";
        if ($schoolRow["SUB_VIRUS"]    == "1") $query .= " + SUM(VALUE(VIRUS,0))";
        if ($schoolRow["SUB_KOUDOME"]  == "1") $query .= " + SUM(VALUE(KOUDOME,0))";
        $query .= "                AS LESSON ";

        $query .= "               ,".$notice." AS T_NOTICE ";
        //小数点ありAdd
        if ($absent_cov == "3" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "           ,DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS NOTICE_LATE ";

        } elseif ($absent_cov == "1" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "           ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
            $query .= "           ,".$late_early."    AS T_LATEEARLY ";
            $query .= "           ,".$notice." AS NOTICE_LATE ";
        }
        $query .= "               ,".$late_early."    AS LATE_EARLY ";
        $query .= "          FROM attend_subclass_dat T1   ";
        $query .= "         WHERE COPYCD        = '0' ";
        $query .= "           AND T1.year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           AND T1.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "           AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "           AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "           AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "           AND T1.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "           AND T1.schregno   = '{$schno}' ";
        $query .= "         GROUP BY T1.schregno, T1.semester) T1 ";
        $query .= " GROUP BY schregno ";

        return $query;
    }

    //成績入力完了チェック
    function getRecordChkfinDat($model, $testcd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECORD_CHKFIN_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD || RECORD_DIV = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD         = '".$subclass_array[0]."' ";
            $query .= "     AND SCHOOL_KIND     = '".$subclass_array[1]."' ";
            $query .= "     AND CURRICULUM_CD   = '".$subclass_array[2]."' ";
            $query .= "     AND SUBCLASSCD      = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND SUBCLASSCD      = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";

        return $query;
    }

    //成績入力完了更新
    function updateRecordChkfin($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();

        //初期化
        foreach ($model->testcdArray as $key => $codeArray) {
            $testcd = $codeArray["TESTCD"];
            $controlFlg = $codeArray["CONTROL_FLG"];

            $query = knjd128lQuery::getRecordChkfinDat($model, $testcd);
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
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "     AND CLASSCD          = '".$subclass_array[0]."' ";
                    $where .= "     AND SCHOOL_KIND      = '".$subclass_array[1]."' ";
                    $where .= "     AND CURRICULUM_CD    = '".$subclass_array[2]."' ";
                    $where .= "     AND SUBCLASSCD       = '".$subclass_array[3]."' ";
                } else {
                    $where .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
                }
                $where .= "     AND CHAIRCD    = '{$model->field["CHAIRCD"]}' ";

                $query = Query::updateSQL($data, "RECORD_CHKFIN_SDIV_DAT", $where);
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    /* 成績データ更新処理 */
    function update($model) {
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $year = CTRL_YEAR;

        //合併先科目か
        $query = knjd128lQuery::getCombined($model, $year);
        $rowCombined = $db->getRow($query, DB_FETCHMODE_ASSOC);

        for ($i = 0; $i < $model->field["COUNTER"]; $i++) {
            $schno = $model->fields["SCHREGNO"][$i];

            foreach ($model->testcdArray as $key => $codeArray) {
                $testcd = $codeArray["TESTCD"];
                $controlFlg = $codeArray["CONTROL_FLG"];
                $score = $model->fields["SCORE".$testcd][$i];

                //管理者コントロール
                if ($controlFlg == "1") {
                    knjd128lQuery::delInsRecordScore($year, $testcd, $model, $schno, $score, $db, $rowCombined, $i);
                }
            }
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //更新
    function delInsRecordScore($year, $testcd, $model, $schno, $score, $db, $rowCombined, $i) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }

        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = substr($testcd, 0, 1);
        $data["TESTKINDCD"][TEXT]   = substr($testcd, 1, 2);
        $data["TESTITEMCD"][TEXT]   = substr($testcd, 3, 2);
        $data["SCORE_DIV"][TEXT]    = substr($testcd, 5, 2);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]          = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        $data["SCORE"][NUMBER]      = $score == "*" ? "" : $score;
        $data["VALUE_DI"][TEXT]     = $score == "*" ? $score : "";
        $data["CHAIRCD"][TEXT]      = $model->field["CHAIRCD"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        //履修単位・修得単位
        if ($testcd == "9990009") {
            //単位の自動計算
            //履修単位／修得単位が入力可で、学年評定が入力されていて、履修単位／修得単位がNULLの時
            //仕様追加。プロパティnotOpenCreditが'1'の時は、履修単位／修得単位の窓をあけない。また、単位の自動計算は、常に上書きする。
            if ((strlen($score) && $score != "*") && (!strlen($model->fields["COMP_CREDIT"][$i]) && !strlen($model->fields["GET_CREDIT"][$i]) || $model->Properties["notOpenCredit"] == '1')) {
                //合併先科目か
                if ($rowCombined["FLG"] == "1" || $rowCombined["FLG"] == "2") {
                    $query = knjd128lQuery::getCreditCombined($model, $year, $schno, $score, $testcd, $rowCombined["FLG"]);
                } else {
                    $query = knjd128lQuery::getCredit($model, $year, $schno, $score);
                }
                $rowCredit = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $data["COMP_CREDIT"][NUMBER]    = $rowCredit["COMP_CREDIT"];
                $data["GET_CREDIT"][NUMBER]     = $rowCredit["GET_CREDIT"];
            } else {
                $data["COMP_CREDIT"][NUMBER]    = ($model->Properties["notOpenCredit"] == '1') ? "" : $model->fields["COMP_CREDIT"][$i];
                $data["GET_CREDIT"][NUMBER]     = ($model->Properties["notOpenCredit"] == '1') ? "" : $model->fields["GET_CREDIT"][$i];
            }
        }

        //削除
        $query = knjd128lQuery::deleteRecordScore($year, $testcd, $schno, $model);
        $db->query($query);
        //成績が1件も入力されていない場合、RECORD_SCORE_DATのレコードは作成しない（考査ごと）
        //testInsArray：成績が１つでも入力された考査コードを配列に保管
        if (in_array($testcd, $model->testInsArray)) {
            //追加
            $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            $db->query($query);
        }

        //評定フラグ・・・仮評定(1)、本評定(NULL)
        //学年評定を更新する場合、評定フラグは本評定(NULL)とする・・・レコードを削除またはNULLで更新(どちらでもよい)
        if ($testcd == "9990009") {
            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                $query = knjd128lQuery::delRecordProvFlgDat($year, $model->field["SUBCLASSCD"], $schno, $model);
                $db->query($query);
                if ($model->fields["PROV_FLG"][$i] == '1') {
                    $query = knjd128lQuery::insRecordProvFlgDat($year, $model->field["SUBCLASSCD"], $schno, $model);
                    $db->query($query);
                }
            }
        }
    }

    //テーブルRECORD_SCORE_DATのレコードを削除
    function deleteRecordScore($year, $testcd, $schno, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_SCORE_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        $query .= "  AND  SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "  AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "  AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "  AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "  AND  SCHREGNO      = '{$schno}' ";
        return $query;
    }

    //単位マスタから履修単位・修得単位を取得
    function getCredit($model, $year, $schno, $score) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     case when 1 <= {$score} then T4.CREDITS ";
        $query .= "          when 0 = {$score} then 0 ";
        $query .= "          else NULL end AS COMP_CREDIT, ";
        $query .= "     case when 1 < {$score} then T4.CREDITS ";
        $query .= "          when 1 = {$score} then 0 ";
        $query .= "          when 0 = {$score} then 0 ";
        $query .= "          else NULL end AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '{$year}' ";
        $query .= "                            AND T4.COURSECD = T1.COURSECD ";
        $query .= "                            AND T4.MAJORCD = T1.MAJORCD ";
        $query .= "                            AND T4.GRADE = T1.GRADE ";
        $query .= "                            AND T4.COURSECODE = T1.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T4.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "                        AND T4.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "                        AND T4.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "                        AND T4.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "                        AND T4.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '{$schno}' ";
        return $query;
    }

    //合併先科目か
    function getCombined($model, $year) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= " SELECT ";
        $query .= "     CALCULATE_CREDIT_FLG AS FLG ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND COMBINED_CLASSCD        = '".$subclass_array[0]."' ";
            $query .= " AND COMBINED_SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= " AND COMBINED_CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= " AND COMBINED_SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND COMBINED_SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        return $query;
    }

    //合併先科目の履修単位・修得単位を取得
    //(固定)合併先科目の単位マスタから
    //(加算)合併元科目の成績データから
    function getCreditCombined($model, $year, $schno, $score, $testcd, $flg) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     case when 1 <= {$score} and '{$flg}' = '1' then T4.CREDITS ";
        $query .= "          when 1 <= {$score} and '{$flg}' = '2' then T5.COMP_CREDIT ";
        $query .= "          when 0 = {$score} then 0 ";
        $query .= "          else NULL end AS COMP_CREDIT, ";
        $query .= "     case when 1 < {$score} and '{$flg}' = '1' then T4.CREDITS ";
        $query .= "          when 1 < {$score} and '{$flg}' = '2' and D015.NAMESPARE1 = 'Y' then T5.GET_CREDIT_Y ";
        $query .= "          when 1 < {$score} and '{$flg}' = '2' then T5.GET_CREDIT ";
        $query .= "          when 1 = {$score} then 0 ";
        $query .= "          when 0 = {$score} then 0 ";
        $query .= "          else NULL end AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        //(固定)合併先科目の単位マスタから
        $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '{$year}' ";
        $query .= "                            AND T4.COURSECD = T1.COURSECD ";
        $query .= "                            AND T4.MAJORCD = T1.MAJORCD ";
        $query .= "                            AND T4.GRADE = T1.GRADE ";
        $query .= "                            AND T4.COURSECODE = T1.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T4.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "                        AND T4.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "                        AND T4.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "                        AND T4.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "                        AND T4.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        //(加算)合併元科目の成績データから
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             SUM(T2.COMP_CREDIT) AS COMP_CREDIT, ";
        $query .= "             SUM(T2.GET_CREDIT) AS GET_CREDIT, ";
        $query .= "             SUM(case when 1 < T2.SCORE then T2.GET_CREDIT ";
        $query .= "                      when 1 = T2.SCORE then T2.COMP_CREDIT end) AS GET_CREDIT_Y ";
        $query .= "         FROM ";
        $query .= "             SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "             RECORD_SCORE_DAT T2 ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T1.COMBINED_CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "         AND T1.COMBINED_SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "         AND T1.COMBINED_CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "         AND T1.COMBINED_SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND T1.COMBINED_SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "             AND T2.YEAR = '{$year}' ";
        $query .= "             AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '{$testcd}' ";
        $query .= "             AND T2.SCHREGNO = '{$schno}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.ATTEND_CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ";
        }
        $query .= "             AND T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ";
        $query .= "         GROUP BY ";
        $query .= "             T2.SCHREGNO ";
        $query .= "     ) T5 ON T5.SCHREGNO = T1.SCHREGNO ";
        //元科目が評定１の場合、履修単位を修得単位に加算・・・但し、名称マスタの予備１がYの時
        $query .= "     LEFT JOIN V_NAME_MST D015 ON D015.YEAR = T1.YEAR ";
        $query .= "                              AND D015.NAMECD1 = 'D015' ";
        $query .= "                              AND D015.NAMECD2 = '01' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '{$schno}' ";
        return $query;
    }

    //テーブルRECORD_PROV_FLG_DATのレコードを削除
    function delRecordProvFlgDat($year, $subclass, $schno, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclass);
        }
        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_PROV_FLG_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "  AND  SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "  AND  CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "  AND  SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND  SUBCLASSCD     = '{$subclass}' ";
        }
        $query .= "  AND  SCHREGNO      = '{$schno}' ";
        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを追加
    function insRecordProvFlgDat($year, $subclass, $schno, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclass);
        }
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]   = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT] = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]    = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]    = $subclass;
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        //評定フラグ・・・仮評定(1)、本評定(NULL)
        $data["PROV_FLG"][TEXT]     = "1";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_PROV_FLG_DAT");
        return $query;
    }
}
?>
