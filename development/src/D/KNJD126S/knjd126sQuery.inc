<?php

require_once('for_php7.php');

class knjd126squery extends Query {
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

    //年組取得（権限チェック）
    function getHrClass($model) {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     REGH.GRADE || REGH.HR_CLASS AS VALUE, ";
        $query .= "     REGH.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGH, ";
        $query .= "     SCHREG_REGD_GDAT GDAT ";
        $query .= " WHERE ";
        $query .= "     REGH.YEAR     = GDAT.YEAR AND ";
        $query .= "     REGH.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     REGH.SEMESTER = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "     REGH.GRADE    = GDAT.GRADE ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "  AND (REGH.TR_CD1       = '".STAFFCD."' OR ";
            $query .= "     REGH.TR_CD2       = '".STAFFCD."' OR ";
            $query .= "     REGH.TR_CD3       = '".STAFFCD."' OR ";
            $query .= "     REGH.SUBTR_CD1    = '".STAFFCD."' OR ";
            $query .= "     REGH.SUBTR_CD2    = '".STAFFCD."' OR ";
            $query .= "     REGH.SUBTR_CD3    = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     GDAT.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT GDAT ";
        $query .= " WHERE ";
        $query .= "     GDAT.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     GDAT.GRADE = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";

        return $query;
    }

    //選択教科取得
    function getClassMst($subclasscd, $grade_hr_class, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $subclasscd);
        }
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND AS VALUE, ";
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || ':' || T2.CLASSNAME AS LABEL, ";
        } else {
            $query .= "     T2.CLASSCD AS VALUE, ";
            $query .= "     T2.CLASSCD || ' ' || T2.CLASSNAME AS LABEL, ";
        }
        $query .= "     VALUE(T2.ELECTDIV,'0') AS ELECTDIV ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "     V_CLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE    = '".substr($grade_hr_class,0,2)."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD      = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        } else {
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = T2.CLASSCD ";
        }
        if($subclasscd){
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T2.CLASSCD          = '".$subclass_array[0]."' ";
                $query .= "     AND T2.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            } else {
                $query .= "     AND T2.CLASSCD = '".substr($subclasscd, 0, 2)."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ(D065)件数取得
    function getNameMstD065($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'D065' AND ";
        $query .= "     NAME1   = '".$model->field["SUBCLASSCD"]."' ";

        return $query;
    }

    //科目取得
    function getSubclassMst($grade_hr_class, $model) {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD || '：' || T2.SUBCLASSNAME AS LABEL, ";
        } else {
            $query .= "     T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T2.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL, ";
        }
        $query .= "     VALUE(T2.ELECTDIV,'0') AS ELECTDIV ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "     V_SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE    = '".substr($grade_hr_class,0,2)."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        $query .= "          AND PTRY.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "          AND SUB_PD.PATTERN_CD = PTRY.PATTERN_CD ";
        $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_PATTERN_DAT PTRD ON PTRY.YEAR = PTRD.YEAR ";
        $query .= "          AND PTRY.SCHOOL_KIND = PTRD.SCHOOL_KIND ";
        $query .= "          AND PTRY.PATTERN_CD = PTRD.PATTERN_CD ";
        $query .= " WHERE ";
        $query .= "     SUB_PD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SUB_PD.GRADE = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
        $query .= "     AND SUB_PD.CLASSCD || '-' || SUB_PD.SCHOOL_KIND || '-' || SUB_PD.CURRICULUM_CD || '-' || SUB_PD.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     SUB_PD.VIEWCD ";

        return $query;
    }

    //編集可能項目の判別
    function selectViewcdQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = " SELECT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1, ";
        $query .= "     JVIEWNAME_GRADE_MST T2 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR  = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".substr($model->field["GRADE_HR_CLASS"],0,2)."' AND ";
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
            $query .= "    T1.SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' AND ";
            $query .= "    T1.SUBCLASSCD    = T2.SUBCLASSCD AND ";
        }
        $query .= "    T1.VIEWCD = T2.VIEWCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(T1.VIEWCD,1,2)    = '".$substr_classcd."' ";
        } else {
            $query .= "    SUBSTR(T1.VIEWCD,1,2)    = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    T1.VIEWCD ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $view_key) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "        T1.HR_NAME, ";
        $query .= "        T2.GRADE, ";
        $query .= "        T2.HR_CLASS, ";
        $query .= "        T2.ATTENDNO, ";
        $query .= "        T2.SCHREGNO, ";
        $query .= "        T3.NAME_SHOW ";
        $query .= "     FROM ";
        $query .= "        SCHREG_REGD_HDAT T1, ";
        $query .= "        SCHREG_REGD_DAT T2, ";
        $query .= "        SCHREG_BASE_MST T3 ";
        $query .= "     WHERE ";
        $query .= "        T1.YEAR      = T2.YEAR AND ";
        $query .= "        T1.SEMESTER  = T2.SEMESTER AND ";
        $query .= "        T1.GRADE     = T2.GRADE AND ";
        $query .= "        T1.HR_CLASS  = T2.HR_CLASS AND ";
        $query .= "        T1.GRADE || T1.HR_CLASS  = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "        T2.YEAR      = '".CTRL_YEAR."' AND ";
        $query .= "        T2.SEMESTER  = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "        T2.SCHREGNO  = T3.SCHREGNO ";
        $query .= " ), VIEWSTAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        // "1" => "1101"
        foreach ($view_key as $code => $col) {
            $query .= "    ,MAX(CASE WHEN T2.VIEWCD = '".$col."' THEN T2.STATUS ELSE NULL END) AS STATUS".$code;
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
            $query .= "         T2.SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //観点コード
        if (get_count($view_key) > 0) 
            $query .= "    T2.VIEWCD IN ('".implode("','", $view_key)."') AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        SUBSTR(T2.VIEWCD, 1, 2) = '".$substr_classcd."' ";
        } else {
            $query .= "        SUBSTR(T2.VIEWCD, 1, 2) = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
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
        $query .= "    T1.NAME_SHOW ";
        // "1" => "1101"
        foreach ($view_key as $code => $col) {
            if (substr($col,2) != "99") {
                $query .= ",T2.STATUS".$code;
            } else {
                $query .= ",T3.SCORE AS STATUS".$code;
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
            $query .= "             AND T3.SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "             AND T3.SCHREGNO     = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "    T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";
        return $query;
    }

    function getClickList($model) {
        $query  = " SELECT ";
        $query .= "     SUBPD.VIEWCD, ";
        $query .= "     PATD.ASSESSLEVEL, ";
        $query .= "     PATD.ASSESSMARK ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_SUBCLASS_PATTERN_DAT SUBPD ";
        $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_PATTERN_DAT PATD ON SUBPD.YEAR = PATD.YEAR ";
        $query .= "          AND SUBPD.SCHOOL_KIND = PATD.SCHOOL_KIND ";
        $query .= "          AND SUBPD.PATTERN_CD = PATD.PATTERN_CD ";
        $query .= " WHERE ";
        $query .= "     SUBPD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SUBPD.GRADE = '".substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SUBPD.CLASSCD || '-' || SUBPD.SCHOOL_KIND || '-' || SUBPD.CURRICULUM_CD || '-' || SUBPD.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND SUBPD.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SUBPD.VIEWCD, ";
        $query .= "     SUBPD.PATTERN_CD, ";
        $query .= "     PATD.ASSESSLEVEL DESC ";

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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();

        for ($i=0; $i < get_count($model->data["SCHREGNO"]); $i++) {

            foreach ($model->data["STATUS"] as $code => $col) {

                //評定は更新しない
                if (substr($col,2) == "99") continue;

                $query  = " SELECT ";
                $query .= "     COUNT(*) AS CNT ";
                $query .= " FROM ";
                $query .= "     JVIEWSTAT_RECORD_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR            = '".CTRL_YEAR."' ";
                $query .= "     AND SEMESTER    = '".$model->field["SEMESTER"]."' ";
                $query .= "     AND SCHREGNO    = '".$model->data["SCHREGNO"][$i]."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "     AND CLASSCD         = '".$substr_classcd."' ";
                    $query .= "     AND SCHOOL_KIND     = '".$substr_school_kind."' ";
                    $query .= "     AND CURRICULUM_CD   = '".$substr_curriculum_cd."' ";
                    $query .= "     AND SUBCLASSCD      = '".$substr_subclasscd."' ";
                } else {
                    $query .= "     AND SUBCAND LASSCD      = '".$model->field["SUBCLASSCD"]."' ";
                }
                $query .= "     AND VIEWCD    = '{$col}' ";
                $dataCnt = $db->getOne($query);

                $setStatus = $model->fields["STATUS".$code][$i];
                $data = array();
                $data["STATUS"][TEXT]       = $setStatus;
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";
                if ($dataCnt > 0) {

                    $where  = " WHERE ";
                    $where .= "     YEAR            = '".CTRL_YEAR."' ";
                    $where .= "     AND SEMESTER    = '{$model->field["SEMESTER"]}' ";
                    $where .= "     AND SCHREGNO    = '{$model->data["SCHREGNO"][$i]}' ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $where .= "     AND CLASSCD         = '{$substr_classcd}' ";
                        $where .= "     AND SCHOOL_KIND     = '{$substr_school_kind}' ";
                        $where .= "     AND CURRICULUM_CD   = '{$substr_curriculum_cd}' ";
                        $where .= "     AND SUBCLASSCD      = '{$substr_subclasscd}' ";
                    } else {
                        $where .= "     AND SUBCLASSCD      = '{$model->field["SUBCLASSCD"]}' ";
                    }
                    $where .= "     AND VIEWCD    = '{$col}' ";
                    $query = Query::updateSQL($data, "JVIEWSTAT_RECORD_DAT", $where);
                    $db->query($query);
                } else {
                    //追加
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
                        $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
                    }
                    $data["VIEWCD"][TEXT]       = $col;

                    $query = Query::insertSQL($data, "JVIEWSTAT_RECORD_DAT");
                    $db->query($query);
                }
            }
        }
        Query::dbCheckIn($db);
    }

    //成績データ更新処理
    function getUpdateRecordQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
        }
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {

            foreach ($model->data["STATUS"] as $code => $col) {

                //評定のみ更新
                if (substr($col,2) != "99") continue;

                //RECORD_SCORE_DATの追加
                knjd126sQuery::insertRecord($db, $model, $i);

                //数字に変換
                $score = $model->fields["STATUS".$code][$i];

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
                    $where .= " AND SUBCLASSCD      = '".$model->field["SUBCLASSCD"]."' "; //科目更新
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd          = substr($model->field["SUBCLASSCD"], 0, 2);
            $substr_school_kind      = substr($model->field["SUBCLASSCD"], 3, 1);
            $substr_curriculum_cd    = substr($model->field["SUBCLASSCD"], 5, 1);
            $substr_subclasscd       = substr($model->field["SUBCLASSCD"], 7, 6);
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
            $query .= " AND SUBCLASSCD      = '".$model->field["SUBCLASSCD"]."' ";
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
                $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
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
