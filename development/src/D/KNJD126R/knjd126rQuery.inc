<?php

require_once('for_php7.php');

class knjd126rquery extends Query {

    //校種一覧取得
    function getSchoolKindList($model, $flg) {
        $query  = " SELECT ";
        if ($flg == "cnt") {
            $query .= "     NAME1 AS VALUE  ";
        } else {
            $query .= "     NAME1 AS VALUE, ";
            $query .= "     ABBV1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->selectSchoolKind) {
            $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタの取得
    function selectNamemstQuery($model)
    {
        $setNameCd = "Z009";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $setNameCd = "Z".$model->field["SCHOOL_KIND"]."09";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."09";
        }
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = '{$setNameCd}' ";
        $query .= " ORDER BY ";
        $query .= "    NAMECD2 ";

        return $query;
    }

    //教科データの取得
    function selectSubclassQuery($model)
    {
        $query  = "SELECT ";
        $query .= "    T3.classcd, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.SCHOOL_KIND, ";
        }
        $query .= "    T3.classname, ";
        $query .= "    value(T3.electdiv,'0') as electdiv ";
        $query .= "FROM ";
        $query .= "    chair_dat T1, ";
        $query .= "    chair_std_dat T2, ";
        $query .= "    class_mst T3, ";
        $query .= "    chair_stf_dat T4 ";
        $query .= "WHERE ";
        $query .= "    T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "    T1.semester   = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "    T2.year       = T1.year AND ";
        $query .= "    T1.year       = T4.year AND ";
        $query .= "    T1.semester   = T4.semester AND ";
        $query .= "    T1.chaircd    = T4.chaircd AND ";
        $query .= "    T2.semester   = T1.semester AND ";
        $query .= "    T2.chaircd    = T1.chaircd AND ";
        $query .= "    T3.classcd IN (SELECT substr(J1.viewcd,1,2) FROM jviewname_grade_ydat J1 WHERE J1.year = '".CTRL_YEAR."' GROUP BY substr(J1.viewcd,1,2)) AND ";
        //更新可能(制限付)
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     t4.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.CLASSCD       = T1.CLASSCD  AND ";
            $query .= "    T3.SCHOOL_KIND   = T1.SCHOOL_KIND  ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= "    AND T3.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "    AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        } else {
            $query .= "    T3.classcd = substr(T1.subclasscd,1,2) ";
        }
        $query .= "GROUP BY ";
        $query .= "    T3.classcd, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.SCHOOL_KIND, ";
        }
        $query .= "    T3.classname, ";
        $query .= "    value(T3.electdiv,'0') ";
        $query .= "ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T3.CLASSCD, ";
            $query .= "    T3.SCHOOL_KIND ";
        } else {
            $query .= "    T3.classcd ";
        }
        return $query;
    }

    //講座データの取得
    function selectChairQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASS_VALUE, ";
        } else {
            $query .= "    T1.SUBCLASSCD AS SUBCLASS_VALUE, ";
        }
        $query .= "     T1.chaircd, ";
        $query .= "     T1.chairname ";
        $query .= " FROM ";
        $query .= "     chair_dat T1, ";
        $query .= "     chair_std_dat T2, ";
        $query .= "     chair_stf_dat T3 ";
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".$model->field["SEMESTER2"]."' AND ";
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
            $query .= "    substr(T1.subclasscd,1,2) = '".$class_array[0]."' AND ";
            $query .= "    T1.SCHOOL_KIND            = '".$class_array[1]."'  ";
        } else {
            $query .= "    substr(T1.subclasscd,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.chaircd ";
        return $query;
    }


    //名称マスタの取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getFirstStudent($model, $execute_date) {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
            $substr_classcd          = $subclass_array[0];
            $substr_school_kind      = $subclass_array[1];
            $substr_curriculum_cd    = $subclass_array[2];
            $substr_subclasscd       = $subclass_array[3];
        }

        $query  = "     SELECT ";
        $query .= "        REGD.GRADE ";
        $query .= "     FROM ";
        $query .= "        SCHREG_REGD_HDAT REGDH, ";
        $query .= "        SCHREG_REGD_DAT REGD, ";
        $query .= "        SCHREG_BASE_MST BASE, ";
        $query .= "        CHAIR_STD_DAT CSTD ";
        $query .= "     WHERE ";
        $query .= "        REGDH.YEAR      = REGD.YEAR ";
        $query .= "        AND REGDH.SEMESTER  = REGD.SEMESTER ";
        $query .= "        AND REGDH.GRADE     = REGD.GRADE ";
        $query .= "        AND REGDH.HR_CLASS  = REGD.HR_CLASS ";
        $query .= "        AND REGD.YEAR      = '".CTRL_YEAR."' ";
        $query .= "        AND REGD.SEMESTER  = '".$model->field["SEMESTER2"]."' ";
        $query .= "        AND REGD.SCHREGNO  = BASE.SCHREGNO ";
        $query .= "        AND CSTD.YEAR      = REGD.year ";
        $query .= "        AND CSTD.CHAIRCD   = '".$chaircd_subclass_array[0]."' ";
        $query .= "        AND CSTD.SCHREGNO  = REGD.SCHREGNO ";
        $query .= "        AND '".$execute_date."' BETWEEN CSTD.APPDATE AND CSTD.APPENDDATE ";
        $query .= "     ORDER BY ";
        $query .= "        REGD.GRADE ";
        $query .= "     FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //管理者コントロール取得
    function getAdminContol($model) {
        $query  = " SELECT ";
        $query .= "     CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '3' ";

        return $query;
    }

    //編集可能項目の判別
    function selectPatternData($model) {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        $query  = " SELECT ";
        $query .= "     SUB_PD.VIEWCD, ";
        $query .= "     SUB_PD.PATTERN_CD, ";
        $query .= "     PTRY.PATTERN_NAME, ";
        $query .= "     PTRY.PERFECT, ";
        $query .= "     PTRD.ASSESSLEVEL, ";
        $query .= "     PTRD.ASSESSMARK, ";
        $query .= "     PTRD.ASSESSLOW, ";
        $query .= "     PTRD.ASSESSHIGH, ";
        $query .= "     PTRD.ASSESS_SHOW1, ";
        $query .= "     PTRD.ASSESS_SHOW2 ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_SUBCLASS_PATTERN_DAT SUB_PD ";
        $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_PATTERN_YMST PTRY ON SUB_PD.YEAR = PTRY.YEAR ";
        $query .= "          AND PTRY.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "          AND SUB_PD.PATTERN_CD = PTRY.PATTERN_CD ";
        $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_PATTERN_DAT PTRD ON PTRY.YEAR = PTRD.YEAR ";
        $query .= "          AND PTRY.SCHOOL_KIND = PTRD.SCHOOL_KIND ";
        $query .= "          AND PTRY.PATTERN_CD = PTRD.PATTERN_CD ";
        $query .= " WHERE ";
        $query .= "     SUB_PD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SUB_PD.GRADE = '{$model->grade}' ";
        $query .= "     AND SUB_PD.CLASSCD || '-' || SUB_PD.SCHOOL_KIND || '-' || SUB_PD.CURRICULUM_CD || '-' || SUB_PD.SUBCLASSCD = '{$chaircd_subclass_array[1]}' ";
        $query .= " ORDER BY ";
        $query .= "     SUB_PD.VIEWCD ";

        return $query;
    }

    //編集可能項目の判別
    function selectViewcdQuery($model) {
        //教育課程対応
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
            $substr_classcd          = $subclass_array[0];
            $substr_school_kind      = $subclass_array[1];
            $substr_curriculum_cd    = $subclass_array[2];
            $substr_subclasscd       = $subclass_array[3];
        }
        $query  = " SELECT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "     JVIEWNAME_GRADE_MST T2 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '{$model->grade}' AND ";
        $query .= "    T1.GRADE = T2.GRADE AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD       = '".$substr_classcd."' AND ";
            $query .= "    T1.SCHOOL_KIND   = '".$substr_school_kind."' AND ";
            $query .= "    T1.CURRICULUM_CD = '".$substr_curriculum_cd."' AND ";
            $query .= "    T1.SUBCLASSCD    = '".$substr_subclasscd."' AND ";
            $query .= "    T1.CLASSCD       = T2.CLASSCD AND ";
            $query .= "    T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
            $query .= "    T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
            $query .= "    T1.SUBCLASSCD    = T2.SUBCLASSCD AND ";
        } else {
            $query .= "    T1.SUBCLASSCD    = '".$chaircd_subclass_array[1]."' AND ";
            $query .= "    T1.SUBCLASSCD    = T2.SUBCLASSCD AND ";
        }
        $query .= "    T1.VIEWCD = T2.VIEWCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(T1.VIEWCD,1,2)    = '".$substr_classcd."' ";
        } else {
            $query .= "    SUBSTR(T1.VIEWCD,1,2)    = '".substr($chaircd_subclass_array[1], 0, 2)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    T1.VIEWCD ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $execute_date, $view_key) {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
            $substr_classcd          = $subclass_array[0];
            $substr_school_kind      = $subclass_array[1];
            $substr_curriculum_cd    = $subclass_array[2];
            $substr_subclasscd       = $subclass_array[3];
        }

        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "        REGDH.HR_NAME, ";
        $query .= "        REGD.GRADE, ";
        $query .= "        REGD.HR_CLASS, ";
        $query .= "        REGD.ATTENDNO, ";
        $query .= "        REGD.SCHREGNO, ";
        $query .= "        BASE.NAME ";
        $query .= "     FROM ";
        $query .= "        SCHREG_REGD_HDAT REGDH, ";
        $query .= "        SCHREG_REGD_DAT REGD, ";
        $query .= "        SCHREG_BASE_MST BASE, ";
        $query .= "        CHAIR_STD_DAT CSTD ";
        $query .= "     WHERE ";
        $query .= "        REGDH.YEAR      = REGD.YEAR ";
        $query .= "        AND REGDH.SEMESTER  = REGD.SEMESTER ";
        $query .= "        AND REGDH.GRADE     = REGD.GRADE ";
        $query .= "        AND REGDH.HR_CLASS  = REGD.HR_CLASS ";
        $query .= "        AND REGD.YEAR      = '".CTRL_YEAR."' ";
        $query .= "        AND REGD.SEMESTER  = '".$model->field["SEMESTER2"]."' ";
        $query .= "        AND REGD.SCHREGNO  = BASE.SCHREGNO ";
        $query .= "        AND CSTD.YEAR      = REGD.YEAR ";
        $query .= "        AND CSTD.CHAIRCD   = '".$chaircd_subclass_array[0]."' ";
        $query .= "        AND CSTD.SCHREGNO  = REGD.SCHREGNO ";
        $query .= "        AND '".$execute_date."' BETWEEN CSTD.APPDATE AND CSTD.APPENDDATE ";
        $query .= " ), VIEWSTAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        // "1" => "1101"
        foreach ($view_key as $code => $col) {
            $query .= "    ,MAX(CASE WHEN T2.VIEWCD = '".$col."' THEN T2.SCORE ELSE NULL END) AS SCORE".$code;
        }
        $query .= "     FROM ";
        $query .= "         SCHINFO T1, ";
        $query .= "         JVIEWSTAT_RECORD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T2.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "         T2.SEMESTER     = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.SCHREGNO     = T2.SCHREGNO AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T2.CLASSCD          = '".$substr_classcd."' AND ";
            $query .= "         T2.SCHOOL_KIND      = '".$substr_school_kind."' AND ";
            $query .= "         T2.CURRICULUM_CD    = '".$substr_curriculum_cd."' AND ";
            $query .= "         T2.SUBCLASSCD       = '".$substr_subclasscd."' AND ";
        } else {
            $query .= "         T2.SUBCLASSCD       = '".$chaircd_subclass_array[1]."' AND ";
        }
        //観点コード
        if (get_count($view_key) > 0) 
            $query .= "    T2.VIEWCD IN ('".implode("','", $view_key)."') AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        SUBSTR(T2.VIEWCD, 1, 2) = '".$substr_classcd."' ";
        } else {
            $query .= "        SUBSTR(T2.VIEWCD, 1, 2) = '".substr($chaircd_subclass_array[1], 0, 2)."' ";
        }
        $query .= "    GROUP BY T1.SCHREGNO ";
        $query .= "    ) ";

        //-----メイン-----//
        $query .= "SELECT ";
        $query .= "    T1.HR_NAME, ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.HR_CLASS, ";
        $query .= "    T1.ATTENDNO, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.NAME ";
        // "1" => "1101"
        foreach ($view_key as $code => $col) {
            if (substr($col,2) != "99" && $col != "99") {
                $query .= ",T2.SCORE".$code;
            } else {
                $query .= ",T3.SCORE AS SCORE".$code;
            }
        }
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN VIEWSTAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN RECORD_SCORE_DAT T3 ";
        $query .= "              ON T3.YEAR         = '".CTRL_YEAR."' ";
        $query .= "             AND T3.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "             AND T3.TESTKINDCD   = '99' ";
        $query .= "             AND T3.TESTITEMCD   = '00' ";
        $query .= "             AND T3.SCORE_DIV    = '09' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T3.CLASSCD          = '".$substr_classcd."' ";
            $query .= "             AND T3.SCHOOL_KIND      = '".$substr_school_kind."' ";
            $query .= "             AND T3.CURRICULUM_CD    = '".$substr_curriculum_cd."' ";
            $query .= "             AND T3.SUBCLASSCD       = '".$substr_subclasscd."' ";
        } else {
            $query .= "             AND T3.SUBCLASSCD       = '".$chaircd_subclass_array[1]."' ";
        }
        $query .= "             AND T3.SCHREGNO     = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "    T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";

        return $query;
    }

    function getTransfer($model, $schregNo) {
        $sdate = str_replace("/", "-", $model->control["学期開始日付"][$model->field["SEMESTER2"]]);
        $edate = str_replace("/", "-", $model->control["学期終了日付"][$model->field["SEMESTER2"]]);

        $query  = " WITH MAIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_TRANSFER_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '{$schregNo}' ";
        $query .= "         AND (TRANSFER_SDATE BETWEEN '{$sdate}' AND '{$edate}' ";
        $query .= "              OR ";
        $query .= "              TRANSFER_EDATE BETWEEN '{$sdate}' AND '{$edate}' ";
        $query .= "              OR ";
        $query .= "              '{$sdate}' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
        $query .= "              OR ";
        $query .= "              '{$edate}' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
        $query .= "             ) ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '{$schregNo}' ";
        $query .= "         AND '{$sdate}' >= GRD_DATE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SUM(CNT) AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";

        return $query;
    }

    //更新
    function getUpdateQuery($model) {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
            $substr_classcd          = $subclass_array[0];
            $substr_school_kind      = $subclass_array[1];
            $substr_curriculum_cd    = $subclass_array[2];
            $substr_subclasscd       = $subclass_array[3];
        }

        $db = Query::dbCheckOut();

        for ($i=0; $i < get_count($model->data["SCHREGNO"]); $i++) {

            //削除
            $query  = " DELETE FROM ";
            $query .= "     JVIEWSTAT_RECORD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO    = '".$model->data["SCHREGNO"][$i]."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     CLASSCD         = '".$substr_classcd."' AND ";
                $query .= "     SCHOOL_KIND     = '".$substr_school_kind."' AND ";
                $query .= "     CURRICULUM_CD   = '".$substr_curriculum_cd."' AND ";
                $query .= "     SUBCLASSCD      = '".$substr_subclasscd."' ";
            } else {
                $query .= "     SUBCLASSCD      = '".$chaircd_subclass_array[1]."' ";
            }
            $db->query($query);

            foreach ($model->data["SCORE"] as $code => $col) {
                //評定は更新しない
                if (substr($col,2) == "99" || $col == "99") continue;

                //追加
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]              = $substr_classcd;
                    $data["SCHOOL_KIND"][TEXT]          = $substr_school_kind;
                    $data["CURRICULUM_CD"][TEXT]        = $substr_curriculum_cd;
                    $data["SUBCLASSCD"][TEXT]           = $substr_subclasscd;
                } else {
                    $data["SUBCLASSCD"][TEXT]   = $chaircd_subclass_array[1];
                }
                $data["VIEWCD"][TEXT]       = $col;
                $setScore = $model->fields["SCORE".$code][$i];
                $data["SCORE"][NUMBER]      = $setScore;
                if (is_array($model->viewLevel[$col])) {
                    foreach ($model->viewLevel[$col] as $levelKey => $levelRow) {
                        if ($levelRow["ASSESSLOW"] <= $setScore && $levelRow["ASSESSHIGH"] >= $setScore) {
                            $data["STATUS"][TEXT]       = $levelRow["ASSESSMARK"];
                        }
                    }
                }
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "JVIEWSTAT_RECORD_DAT");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    //成績データ更新処理
    function getUpdateRecordQuery($model) {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
            $substr_classcd          = $subclass_array[0];
            $substr_school_kind      = $subclass_array[1];
            $substr_curriculum_cd    = $subclass_array[2];
            $substr_subclasscd       = $subclass_array[3];
        }
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {

            foreach ($model->data["SCORE"] as $code => $col) {

                //評定のみ更新
                if (substr($col,2) != "99" && $col != "99") continue;

                //RECORD_SCORE_DATの追加
                knjd126rQuery::insertRecord($db, $model, $i);

                //数字に変換
                $score = $model->fields["SCORE".$code][$i];

                //評定をセット
                $data = array();
                $data["SCORE"][NUMBER]  = $score;
                $data["VALUE_DI"][TEXT] = "";

                //条件
                $where  = " WHERE ";
                $where .= "     YEAR        = '".CTRL_YEAR."' ";
                $where .= " AND SEMESTER    = '".$model->field["SEMESTER"]."' ";
                $where .= " AND TESTKINDCD  = '99' ";
                $where .= " AND TESTITEMCD  = '00' ";
                $where .= " AND SCORE_DIV   = '09' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= " AND CLASSCD         = '".$substr_classcd."' ";
                    $where .= " AND SCHOOL_KIND     = '".$substr_school_kind."' ";
                    $where .= " AND CURRICULUM_CD   = '".$substr_curriculum_cd."' ";
                    $where .= " AND SUBCLASSCD      = '".$substr_subclasscd."' ";
                } else {
                    $where .= " AND SUBCLASSCD      = '".$chaircd_subclass_array[1]."' ";
                }
                $where .= " AND SCHREGNO    = '".$model->data["SCHREGNO"][$i]."' ";

                //更新
                $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
    }

    // RECORD_SCORE_DATの追加
    function insertRecord($db, $model, $i) {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
            $substr_classcd          = $subclass_array[0];
            $substr_school_kind      = $subclass_array[1];
            $substr_curriculum_cd    = $subclass_array[2];
            $substr_subclasscd       = $subclass_array[3];
        }

        // RECORD_SCORE_DATの存在チェック
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER    = '".$model->field["SEMESTER"]."' ";
        $query .= " AND TESTKINDCD  = '99' ";
        $query .= " AND TESTITEMCD  = '00' ";
        $query .= " AND SCORE_DIV   = '09' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD         = '".$substr_classcd."' ";
            $query .= " AND SCHOOL_KIND     = '".$substr_school_kind."' ";
            $query .= " AND CURRICULUM_CD   = '".$substr_curriculum_cd."' ";
            $query .= " AND SUBCLASSCD      = '".$substr_subclasscd."' ";
        } else {
            $query .= " AND SUBCLASSCD      = '".$chaircd_subclass_array[1]."' ";
        }
        $query .= " AND SCHREGNO    = '".$model->data["SCHREGNO"][$i]."' ";
        $record_cnt = $db->getOne($query);
        // RECORD_SCORE_DATがなかったら追加する
        if ($record_cnt == 0) {
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT]   = "99";
            $data["TESTITEMCD"][TEXT]   = "00";
            $data["SCORE_DIV"][TEXT]    = "09";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]          = $substr_classcd;
                $data["SCHOOL_KIND"][TEXT]      = $substr_school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $substr_curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $substr_subclasscd;
            } else {
                $data["SUBCLASSCD"][TEXT]       = $chaircd_subclass_array[1];
            }
            $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            $db->query($query);
        }
    }
}
?>
