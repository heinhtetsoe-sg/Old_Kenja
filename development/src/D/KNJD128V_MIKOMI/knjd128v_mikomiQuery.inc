<?php

require_once('for_php7.php');


class knjd128v_mikomiQuery extends Query {

    //学校名取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= " AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtn = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtn;
    }

    //課程学科コンボ取得
    function getCourseMajor($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV TEST_GCM ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST VCM ON TEST_GCM.YEAR = VCM.YEAR ";
        $query .= "           AND TEST_GCM.COURSECD = VCM.COURSECD ";
        $query .= "           AND TEST_GCM.MAJORCD = VCM.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     TEST_GCM.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND TEST_GCM.SCHOOLCD = '".SCHOOLCD."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND TEST_GCM.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND TEST_GCM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND TEST_GCM.COURSECD || '-' || TEST_GCM.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

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

    //指導（1:マーク）
    function getMarkList() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D054' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目別設定があるか
    function getTestSubCnt($model) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " ADMIN_CONTROL_GCM_SDIV_DAT T1 ";
        } else {
            $query .= " ADMIN_CONTROL_SDIV_DAT T1 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV <= '".$model->field["TESTCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$subclass_array[1]."' ";
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
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

    //管理者コントロールの基本のSCHOOL_KINDについて、混在は、MAXを取得する。
    //例
    //「00-00-00-000000」・・・これはゴミ
    //「00-H-00-000000」・・・これを参照する
    function getAdminSchoolKind($schoolKind) {
        $query  = " SELECT ";
        $query .= "     MAX(SCHOOL_KIND) ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     ADMIN_CONTROL_GCM_SDIV_DAT ";
        } else {
            $query .= "     ADMIN_CONTROL_SDIV_DAT ";
        }
        $query .= " WHERE ";
        $query .= "         YEAR            = '".CTRL_YEAR."' ";
        $query .= "     AND CLASSCD         = '00' ";
        $query .= "     AND SCHOOL_KIND     IN ('00','{$schoolKind}') "; //宮城県は'00'がある(古いデータ)
        $query .= "     AND CURRICULUM_CD   = '00' ";
        $query .= "     AND SUBCLASSCD      = '000000' ";
        return $query;
    }

    //テスト名取得
    function getTestName($model, $testSubCnt) {
        //科目別設定がある場合は、科目別設定。それ以外は基本設定されたパーツのみ表示する。
        $subclasscd = (0 < $testSubCnt) ? $model->field["SUBCLASSCD"] : $model->subclassAll;
        $subclass_array = array();
        $subclass_array = explode("-", $subclasscd);
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
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " ADMIN_CONTROL_GCM_SDIV_DAT T1 ";
        } else {
            $query .= " ADMIN_CONTROL_SDIV_DAT T1 ";
        }
        $query .= "     LEFT JOIN SEMESTER_MST T2 ";
        $query .= "         ON  T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.SEMESTER     = T1.SEMESTER ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T3 ";
        } else {
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T3 ";
        }
        $query .= "         ON  T3.YEAR         = T1.YEAR ";
        $query .= "         AND T3.SEMESTER     = T1.SEMESTER ";
        $query .= "         AND T3.TESTKINDCD   = T1.TESTKINDCD ";
        $query .= "         AND T3.TESTITEMCD   = T1.TESTITEMCD ";
        $query .= "         AND T3.SCORE_DIV    = T1.SCORE_DIV ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     AND T3.SCHOOLCD     = T1.SCHOOLCD ";
            $query .= "     AND T3.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "     AND T3.GRADE        = T1.GRADE ";
            $query .= "     AND T3.COURSECD || T3.MAJORCD = T1.COURSECD || T1.MAJORCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV <= '".$model->field["TESTCD"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND T1.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND T1.SUBCLASSCD     = '".$subclasscd."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "          AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= "          AND T1.SCHOOL_KIND = '".$subclass_array[1]."' ";
            $query .= "          AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     TESTCD ";

        return $query;
    }

    //科目データの取得
    function getSubclassMst($model) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = "";
        $query .= " WITH REPLACE AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         '1' AS DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .= "     COMBINED_SUBCLASSCD AS SUBCLASSCD ";
        }
        $query .= "     FROM ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
            $query .= "     CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN '●' ELSE '　' END || T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN '●' ELSE '　' END || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T4, ";
        $query .= "     SUBCLASS_MST T3 ";
        $query .= "     LEFT JOIN REPLACE L1 ON L1.DIV = '1' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = L1.SUBCLASSCD ";
        } else {
            $query .= "         AND T3.SUBCLASSCD = L1.SUBCLASSCD ";
        }
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
        if ($model->auth != DEF_UPDATABLE) {
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T3.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "     AND T3.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "     AND T3.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "     AND T3.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND T3.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //講座データの取得
    function selectChairQuery($model) {
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
        if($model->auth != DEF_UPDATABLE){
          $query .= "       t3.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     T1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     T1.SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "     T1.subclasscd = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     T1.chaircd = '".$model->field["CHAIRCD"]."' ";
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
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
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
        $query  = "";
        $query .= " SELECT W1.schregno";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.inoutcd, W2.name_show, W2.grd_date, W2.COURSE";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
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
        //指定された課程学科の生徒
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "        AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }
        $query .= "        AND T1.schregno = T2.schregno ) W2 ";
        $query .= "   ON W2.schregno = W1.schregno ";
        /* 異動情報 */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.schregno, MIN(T1.transfer_sdate) AS transfer_sdate, MAX(T1.transfer_edate) AS transfer_edate";
        $query .= "       FROM schreg_transfer_dat T1 ";
        $query .= "      WHERE FISCALYEAR(T1.transfer_sdate) = '".CTRL_YEAR."'";
        $query .= "      GROUP BY T1.schregno ) W3 ";
        $query .= "   ON W3.schregno = W1.schregno ";

        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";
        return $query;
    }

    //成績データ
    function getScore($model, $execute_date) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH SELECT_T AS ( ".knjd128v_mikomiQuery::selectQuery($model, $execute_date).") ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.COMP_CREDIT, ";
        $query .= "     T1.GET_CREDIT, ";
        $query .= "     T1.VALUE_DI "; //欠試'*'
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
        $query .= "     AND T1.SCHREGNO IN (SELECT I1.SCHREGNO FROM SELECT_T I1) ";
        return $query;
    }

    //見込点データ
    function getRecordInfo($model, $execute_date) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH SELECT_T AS ( ".knjd128v_mikomiQuery::selectQuery($model, $execute_date).") ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T1.SCORE AS MIKOMI ";
        $query .= " FROM ";
        $query .= "     SUPP_EXA_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $query .= "     AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
        $query .= "     AND T1.SCHREGNO IN (SELECT I1.SCHREGNO FROM SELECT_T I1) ";
        $query .= "     AND T1.SCORE_FLG = '{$model->info_seq}' ";
        return $query;
    }

    //SUPP_EXA_SDIV_DAT更新
    function updateRecordInfo($model) {
        //DB接続
        $db = Query::dbCheckOut();

        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $year = CTRL_YEAR;
        $testcd = $model->field["TESTCD"];

        for ($i = 0; $i < $model->field["COUNTER"]; $i++) {
            //見込点は、欠席者のみ入力可
            if ($model->fields["MIKOMI_VALUE_DI"][$i] !== "*" && $model->fields["MIKOMI_VALUE_DI"][$i] !== "**") continue;
            $schno = $model->fields["SCHREGNO"][$i];
            //データセット
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
            $data["SCORE_FLG"][TEXT]    = $model->info_seq;
            $data["SCORE"][NUMBER]      = $model->fields["MIKOMI"][$i];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            //カウント
            $query = knjd128v_mikomiQuery::countRecordInfo($year, $testcd, $schno, $model);
            if ($db->getOne($query) == 0) {
                $query = Query::insertSQL($data, "SUPP_EXA_SDIV_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR = '{$year}' ";
                $where .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= " AND CLASSCD        = '".$subclass_array[0]."' ";
                    $where .= " AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
                    $where .= " AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
                    $where .= " AND SUBCLASSCD     = '".$subclass_array[3]."' ";
                } else {
                    $where .= " AND SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
                }
                $where .= "     AND SCHREGNO = '{$schno}' ";
                $query = Query::updateSQL($data, "SUPP_EXA_SDIV_DAT", $where);
            }
            $db->query($query);
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //SUPP_EXA_SDIV_DAT削除
    function countRecordInfo($year, $testcd, $schno, $model) {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SUPP_EXA_SDIV_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= " AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= " AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= " AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND SCHREGNO = '{$schno}' ";
        return $query;
    }
}
?>
