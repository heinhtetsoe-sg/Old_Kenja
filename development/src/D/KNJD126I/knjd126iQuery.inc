<?php

require_once('for_php7.php');

class knjd126iquery extends Query {
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

    //名称マスタ(D065)件数取得
    function getNameMstD065($model) {
        $chaircd = $subclasscd = "";
        if ($model->field["CHAIRCD_SUBCLASS"]) {
            list($chaircd, $subclasscd) = preg_split("/:/", $model->field["CHAIRCD_SUBCLASS"]);
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'D065' AND ";
        $query .= "     NAME1   = '".$subclasscd."' ";

        return $query;
    }

    //名称マスタの取得
    function getKantenHyouka($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     ABBV1, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= "     NAMECD1 = 'D028' ";
        } else {
            $query .= "     NAMECD1 = 'D029' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //編集可能項目の判別1
    function selectContolCodeQuery($model){

        $query  = " SELECT ";
        $query .= "    CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '3' ";
        return $query;
    }

    //編集可能項目の判別2
    function selectViewcdQuery($model, $execute_date){
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
        }
        $query  = "WITH MAX_GRADE AS ( ";
        $query .= "    SELECT ";
        $query .= "        max(t2.grade) as grade ";
        $query .= "    FROM ";
        $query .= "        schreg_regd_hdat t1, ";
        $query .= "        schreg_regd_dat t2, ";
        $query .= "        schreg_base_mst t3, ";
        $query .= "        chair_std_dat t4 ";
        $query .= "    WHERE ";
        $query .= "        t1.year      = t2.year AND ";
        $query .= "        t1.semester  = t2.semester AND ";
        $query .= "        t1.grade     = t2.grade AND ";
        $query .= "        t1.hr_class  = t2.hr_class AND ";
        $query .= "        t2.year      = '".CTRL_YEAR."' AND ";
        $query .= "        t2.semester  = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "        t2.schregno  = t3.schregno AND ";
        $query .= "        t4.year      = t2.year AND ";
        $query .= "        t4.chaircd   = '".$chaircd_subclass_array[0]."' AND ";
        $query .= "        t4.schregno  = t2.schregno AND ";
        $query .= "        '".$execute_date."' BETWEEN t4.appdate AND t4.appenddate ";
        $query .= "    ) ";

        $query .= "SELECT ";
        $query .= "    t1.viewcd, t2.viewname ";
        $query .= "FROM ";
        $query .= "    jviewname_grade_ydat t1, jviewname_grade_mst t2 ";
        $query .= "WHERE ";
        $query .= "    t1.year = '".CTRL_YEAR."' AND ";
        $query .= "    t1.grade = t2.grade AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    t1.CLASSCD        = '".$subclass_array[0]."' AND ";
            $query .= "    t1.SCHOOL_KIND    = '".$subclass_array[1]."' AND ";
            $query .= "    t1.CURRICULUM_CD  = '".$subclass_array[2]."' AND ";
            $query .= "    t1.SUBCLASSCD     = '".$subclass_array[3]."' AND ";
            $query .= "    t1.CLASSCD       = t2.CLASSCD AND ";
            $query .= "    t1.SCHOOL_KIND   = t2.SCHOOL_KIND AND ";
            $query .= "    t1.CURRICULUM_CD = t2.CURRICULUM_CD AND ";
            $query .= "    t1.SUBCLASSCD    = t2.SUBCLASSCD AND ";
        } else {
            $query .= "    t1.SUBCLASSCD = '".$chaircd_subclass_array[1]."' AND ";
            $query .= "    t1.SUBCLASSCD = t2.SUBCLASSCD AND ";
        }
        $query .= "    t1.viewcd = t2.viewcd AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(t1.VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "    substr(t1.viewcd,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= "    AND t1.grade in (SELECT w1.grade FROM MAX_GRADE w1) ";
        $query .= "ORDER BY ";
        $query .= "    t1.viewcd ";
        return $query;
    }

    //データ取得
    function getJviewstatRecordDat($model, $view_key) {
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
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "     AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "     AND CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "     AND SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND SUBCLASSCD    = '".$chaircd_subclass_array[1]."' ";
        }
        //観点コード
        if (get_count($view_key) > 0) {
            $query .= "     AND viewcd in('".implode("','", $view_key)."') ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND substr(VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "     AND substr(viewcd,1,2) = '".$model->field["CLASSCD"]."' ";
        }

        return $query;
    }

    //学年取得
    function getGrade($model, $execute_date)
    {
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
        }

        $query  = " SELECT ";
        $query .= "     GDAT.GRADE, ";
        $query .= "     GDAT.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT C_DAT ";
        $query .= "     INNER JOIN CHAIR_STD_DAT C_STD ON C_DAT.YEAR = C_STD.YEAR ";
        $query .= "           AND C_DAT.SEMESTER = C_STD.SEMESTER ";
        $query .= "           AND C_DAT.CHAIRCD = C_STD.CHAIRCD ";
        $query .= "           AND C_DAT.YEAR = C_STD.YEAR ";
        $query .= "           AND '".$execute_date."' BETWEEN C_STD.APPDATE AND C_STD.APPENDDATE ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON C_STD.YEAR = REGD.YEAR ";
        $query .= "           AND C_STD.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND C_STD.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ";
        $query .= "           AND REGD.GRADE = GDAT.GRADE ";
        $query .= "           AND GDAT.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= " WHERE ";
        $query .= "     C_DAT.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND C_DAT.SEMESTER = '".$model->field["SEMESTER"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        AND C_DAT.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "        AND C_DAT.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "        AND C_DAT.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "        AND C_DAT.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "        AND C_DAT.SUBCLASSCD = '".$chaircd_subclass_array[1]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GDAT.GRADE ";

        return $query;
    }

    //合算
    function selectTotalQuery($model, $execute_date, $view_key, $grade, $mark)
    {
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
        }
        $query  = " WITH VIEWSTAT AS ( ";
        $query .= "    SELECT ";
        $query .= "        J_REC.SCHREGNO ";
        // "1" => "1101"
        foreach ($view_key as $code => $col) {
            $query .= "    ,max(case when J_REC.VIEWCD = '".$col."' then J_REC.STATUS else null end) as STATUS".$code;
        }
        $query .= "    FROM ";
        $query .= "     JVIEWSTAT_RECORD_DAT J_REC ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON J_REC.YEAR = REGD.YEAR ";
        $query .= "           AND J_REC.SEMESTER = REGD.SEMESTER ";
        $query .= "           AND J_REC.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ";
        $query .= "           AND REGD.GRADE = GDAT.GRADE ";
        $query .= "           AND GDAT.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}' ";
        $query .= "    WHERE ";
        $query .= "        J_REC.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        J_REC.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        J_REC.CLASSCD        = '".$subclass_array[0]."' AND ";
            $query .= "        J_REC.SCHOOL_KIND    = '".$subclass_array[1]."' AND ";
            $query .= "        J_REC.CURRICULUM_CD  = '".$subclass_array[2]."' AND ";
            $query .= "        J_REC.SUBCLASSCD     = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "        J_REC.SUBCLASSCD = '".$chaircd_subclass_array[1]."' AND ";
        }
        //観点コード
        if (get_count($view_key) > 0) 
            $query .= "    J_REC.VIEWCD in('".implode("','", $view_key)."') AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        SUBSTR(J_REC.VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "        substr(J_REC.VIEWCD,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= "    GROUP BY ";
        $query .= "         J_REC.SCHREGNO ";
        $query .= "    ) ";

        //-----メイン-----//
        $query .= " SELECT ";
        $query .= "    TBL1.YEAR ";
        // "1" => "1101"
        foreach ($view_key as $code => $col) {
            if (substr($col,2) != "99") {
                $query .= ", SUM(CASE WHEN VALUE(tbl2.status{$code}, '') = '{$mark}' THEN 1 ELSE 0 END) AS STATUS{$code}";
            }
        }
        $query .= " FROM ";
        $query .= "    (SELECT DISTINCT ";
        $query .= "        REGD.YEAR, ";
        $query .= "        REGD.SCHREGNO ";
        $query .= "    FROM ";
        $query .= "        SCHREG_REGD_DAT REGD ";
        if ($grade == "CLASS") {
            $query .= "        , CHAIR_STD_DAT C_STD ";
        }
        $query .= "    WHERE ";
        $query .= "        REGD.YEAR      = '".CTRL_YEAR."' AND ";
        $query .= "        REGD.SEMESTER  = '".$model->field["SEMESTER2"]."' ";
        if ($grade != "CLASS") {
            $query .= "        AND REGD.GRADE     = '".$grade."' ";
        }
        if ($grade == "CLASS") {
            $query .= "        AND C_STD.YEAR      = REGD.YEAR ";
            $query .= "        AND C_STD.CHAIRCD   = '".$chaircd_subclass_array[0]."' ";
            $query .= "        AND C_STD.SCHREGNO  = REGD.SCHREGNO ";
            $query .= "        AND '".$execute_date."' BETWEEN C_STD.APPDATE AND C_STD.APPENDDATE ";
        }
        $query .= "    ) TBL1 ";
        $query .= "    LEFT JOIN VIEWSTAT TBL2 ON TBL2.SCHREGNO = TBL1.SCHREGNO ";
        $query .= " GROUP BY ";
        $query .= "    TBL1.YEAR ";
        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $execute_date, $view_key)
    {
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
        }
        $query  = "WITH VIEWSTAT AS ( ";
        $query .= "    SELECT ";
        $query .= "        schregno ";
        // "1" => "1101"
        foreach ($view_key as $code => $col)
        {
            $query .= "    ,max(case when viewcd = '".$col."' then status else null end) as status".$code;
        }
        $query .= "    FROM ";
        $query .= "        JVIEWSTAT_RECORD_DAT ";
        $query .= "    WHERE ";
        $query .= "        year = '".CTRL_YEAR."' AND ";
        $query .= "        semester = '".$model->field["SEMESTER"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        CLASSCD        = '".$subclass_array[0]."' AND ";
            $query .= "        SCHOOL_KIND    = '".$subclass_array[1]."' AND ";
            $query .= "        CURRICULUM_CD  = '".$subclass_array[2]."' AND ";
            $query .= "        SUBCLASSCD     = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "        SUBCLASSCD = '".$chaircd_subclass_array[1]."' AND ";
        }
        //観点コード
        if (get_count($view_key) > 0) 
            $query .= "    viewcd in('".implode("','", $view_key)."') AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        SUBSTR(VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "        substr(viewcd,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= "    GROUP BY schregno ";
        $query .= "    ) ";

        //-----メイン-----//
        $query .= "SELECT ";
        $query .= "    tbl1.hr_name, tbl1.grade, tbl1.hr_class, tbl1.attendno, tbl1.schregno, tbl1.name_show ";
        // "1" => "1101"
        foreach ($view_key as $code => $col)
        {
            if (substr($col,2) != "99") {
                $query .= ",tbl2.status".$code;
            } else {
                if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                    $query .= ",T3.SCORE AS STATUS".$code;
                } else {
                    $query .= ",T3.VALUE AS STATUS".$code;
                }
            }
        }
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "        t1.hr_name, t2.grade, t2.hr_class, t2.attendno, t4.schregno, t3.name_show ";
        $query .= "    FROM ";
        $query .= "        schreg_regd_hdat t1, ";
        $query .= "        schreg_regd_dat t2, ";
        $query .= "        schreg_regd_gdat gdat, ";
        $query .= "        schreg_base_mst t3, ";
        $query .= "        chair_std_dat t4 ";
        $query .= "    WHERE ";
        $query .= "        t1.year      = t2.year AND ";
        $query .= "        t1.semester  = t2.semester AND ";
        $query .= "        t1.grade     = t2.grade AND ";
        $query .= "        t1.hr_class  = t2.hr_class AND ";
        $query .= "        t2.year      = '".CTRL_YEAR."' AND ";
        $query .= "        t2.semester  = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "        t2.schregno  = t3.schregno AND ";
        $query .= "        t1.year      = gdat.year AND ";
        $query .= "        t1.grade     = gdat.grade AND ";
        $query .= "        gdat.SCHOOL_KIND  = '{$model->field["SCHOOL_KIND"]}' AND ";
        $query .= "        t4.year      = t2.year AND ";
        $query .= "        t4.chaircd   = '".$chaircd_subclass_array[0]."' AND ";
        $query .= "        t4.schregno  = t2.schregno AND ";
        $query .= "        '".$execute_date."' BETWEEN t4.appdate AND t4.appenddate ";
        $query .= "    ) tbl1 ";
        $query .= "    left join VIEWSTAT tbl2 on tbl2.schregno = tbl1.schregno ";
        $query .= "     LEFT JOIN RECORD_SCORE_DAT T3 ";
        $query .= "              ON T3.YEAR         = '".CTRL_YEAR."' ";
        $query .= "             AND T3.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "             AND T3.TESTKINDCD   = '99' ";
        $query .= "             AND T3.TESTITEMCD   = '00' ";
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            if ($model->field["SEMESTER"] == '9') {
                $query .= "             AND T3.SCORE_DIV    = '09' ";
            } else {
                $query .= "             AND T3.SCORE_DIV    = '08' ";
            }
        } else {
            $query .= "             AND T3.SCORE_DIV    = '00' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T3.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "             AND T3.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "             AND T3.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "             AND T3.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "             AND T3.SUBCLASSCD   = '".$chaircd_subclass_array[1]."' ";
        }
        $query .= "             AND T3.SCHREGNO     = tbl1.SCHREGNO ";
        $query .= "ORDER BY ";
        $query .= "    tbl1.grade, tbl1.hr_class, tbl1.attendno ";
        return $query;
    }

    function getTransfer($model, $schregNo) {
        $sdate = str_replace("/", "-", $model->control["学期開始日付"][$model->field["SEMESTER2"]]);
        $edate = str_replace("/", "-", $model->control["学期終了日付"][$model->field["SEMESTER2"]]);

        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregNo}' ";
        $query .= "     AND (TRANSFER_SDATE BETWEEN '{$sdate}' AND '{$edate}' ";
        $query .= "          OR ";
        $query .= "          TRANSFER_EDATE BETWEEN '{$sdate}' AND '{$edate}' ";
        $query .= "          OR ";
        $query .= "          '{$sdate}' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
        $query .= "          OR ";
        $query .= "          '{$edate}' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
        $query .= "         ) ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregNo}' ";
        $query .= "     AND '{$sdate}' >= GRD_DATE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SUM(CNT) AS CNT ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";

        return $query;
    }

    /* データ更新処理 */
    function update($model)
    {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
        }
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {

            //削除
            $query  = "DELETE FROM ";
            $query .= "    JVIEWSTAT_RECORD_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "    SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $query .= "    SCHREGNO = '".$model->data["SCHREGNO"][$i]."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD        = '".$subclass_array[0]."' AND ";
                $query .= "    SCHOOL_KIND    = '".$subclass_array[1]."' AND ";
                $query .= "    CURRICULUM_CD  = '".$subclass_array[2]."' AND ";
                $query .= "    SUBCLASSCD     = '".$subclass_array[3]."' ";
            } else {
                $query .= "    SUBCLASSCD = '".$chaircd_subclass_array[1]."' ";
            }
            $db->query($query);

            foreach ($model->data["STATUS"] as $code => $col) {

                //評定は更新しない
                if (substr($col,2) == "99") continue;

                //追加
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]              = $subclass_array[0];
                    $data["SCHOOL_KIND"][TEXT]          = $subclass_array[1];
                    $data["CURRICULUM_CD"][TEXT]        = $subclass_array[2];
                    $data["SUBCLASSCD"][TEXT]           = $subclass_array[3];
                } else {
                    $data["SUBCLASSCD"][TEXT]   = $chaircd_subclass_array[1];
                }
                $data["VIEWCD"][TEXT]       = $col;
                if (($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"] && $model->fields["STATUS".$code][$i] == "") ||
                    ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"] && $model->fields["STATUS".$code][$i] == "")) {
                    $data["STATUS"][TEXT]       = $model->nonVisualViewCd;
                } else if (($model->field["SEMESTER"] == "9" && $model->Properties["useJviewStatus_NotHyoji_D028"] && $model->fields["STATUS".$code][$i] == "F") ||
                           ($model->field["SEMESTER"] != "9" && $model->Properties["useJviewStatus_NotHyoji_D029"] && $model->fields["STATUS".$code][$i] == "F")) {
                    $data["STATUS"][TEXT]       = "";
                } else {
                    $data["STATUS"][TEXT]       = $model->fields["STATUS".$code][$i];
                }
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "JVIEWSTAT_RECORD_DAT");
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
    }

    /* 成績データ更新処理 */
    function updateRecord($model)
    {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
        }
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {

            foreach ($model->data["STATUS"] as $code => $col) {

                //評定のみ更新
                if (substr($col,2) != "99") continue;

                //RECORD_SCORE_DATの追加
                knjd126iQuery::insertRecord($db, $model, $i);

                //数字に変換
                $status = $model->fields["STATUS".$code][$i];
                if ($status == "A") $status = "11";
                if ($status == "B") $status = "22";
                if ($status == "C") $status = "33";

                //評定をセット
                $data = array();
                if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                    $data["SCORE"][NUMBER]  = $status;
                } else {
                    $data["VALUE"][NUMBER]  = $status;
                }
                $data["VALUE_DI"][TEXT] = "";

                //条件
                $where  = "WHERE   YEAR         = '".CTRL_YEAR."' ";
                $where .= "  AND   SEMESTER     = '".$model->field["SEMESTER"]."' ";
                $where .= "  AND   TESTKINDCD   = '99' ";
                $where .= "  AND   TESTITEMCD   = '00' ";
                if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                    if ($model->field["SEMESTER"] == '9') {
                        $where .= "  AND   SCORE_DIV    = '09' ";
                    } else {
                        $where .= "  AND   SCORE_DIV    = '08' ";
                    }
                } else {
                    $where .= "  AND   SCORE_DIV    = '00' ";
                }
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "  AND   CLASSCD       = '".$subclass_array[0]."' ";
                    $where .= "  AND   SCHOOL_KIND   = '".$subclass_array[1]."' ";
                    $where .= "  AND   CURRICULUM_CD = '".$subclass_array[2]."' ";
                    $where .= "  AND   SUBCLASSCD    = '".$subclass_array[3]."' ";
                } else {
                    $where .= "  AND   SUBCLASSCD   = '".$chaircd_subclass_array[1]."' "; //科目更新
                }
                $where .= "  AND   SCHREGNO     = '".$model->data["SCHREGNO"][$i]."' ";

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
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
        }
        // RECORD_SCORE_DATの存在チェック
        $query  = "SELECT  COUNT(*) ";
        $query .= "FROM    RECORD_SCORE_DAT ";
        $query .= "WHERE   YEAR         = '".CTRL_YEAR."' ";
        $query .= "  AND   SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "  AND   TESTKINDCD   = '99' ";
        $query .= "  AND   TESTITEMCD   = '00' ";
        
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            if ($model->field["SEMESTER"] == '9') {
                $query .= "  AND   SCORE_DIV    = '09' ";
            } else {
                $query .= "  AND   SCORE_DIV    = '08' ";
            }
        } else {
            $query .= "  AND   SCORE_DIV    = '00' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND   CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "  AND   SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "  AND   CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "  AND   SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND   SUBCLASSCD   = '".$chaircd_subclass_array[1]."' ";
        }
        $query .= "  AND   SCHREGNO     = '".$model->data["SCHREGNO"][$i]."' ";
        $record_cnt = $db->getOne($query);
        // RECORD_SCORE_DATがなかったら追加する
        if ($record_cnt == 0) {
            $data = array();
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT]   = "99";
            $data["TESTITEMCD"][TEXT]   = "00";
            if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                if ($model->field["SEMESTER"] == '9') {
                    $data["SCORE_DIV"][TEXT]    = "09";
                } else {
                    $data["SCORE_DIV"][TEXT]    = "08";
                }
            } else {
                $data["SCORE_DIV"][TEXT]    = "00";
            }
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]              = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]          = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]        = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]           = $subclass_array[3];
            } else {
                $data["SUBCLASSCD"][TEXT]   = $chaircd_subclass_array[1];
            }
            $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
            $data["CHAIRCD"][TEXT]      = $chaircd_subclass_array[0];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
            $db->query($query);
        }
    }

    function setRecordScoreSemeVal($db, $model) {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
        }
        $regdSeme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            $setScoreField = "SCORE";
        } else {
            $setScoreField = "VALUE";
        }

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregNo = $model->data["SCHREGNO"][$i];
            if ($model->fields[$schregNo]) {
                continue;
            }
            $query = knjd126iQuery::getRecordScoreData($model, $schregNo, $regdSeme, $chaircd_subclass_array, $subclass_array);
            $scoreData = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $query = knjd126iQuery::getRecordScore($model, $schregNo);
            $dataCnt = $db->getOne($query);

            $data = array();
            $data[$setScoreField][NUMBER]   = $scoreData["VALUE"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            if ($dataCnt > 0) {
                $where  = " WHERE ";
                $where .= "     YEAR = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
                $where .= "     AND TESTKINDCD = '99' ";
                $where .= "     AND TESTITEMCD = '00' ";
                if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
                    if ($model->field["SEMESTER"] == '9') {
                        $where .= "     AND SCORE_DIV = '09' ";
                    } else {
                        $where .= "     AND SCORE_DIV = '08' ";
                    }
                } else {
                    $where .= "     AND SCORE_DIV = '00' ";
                }
                $where .= "     AND SCHREGNO = '{$schregNo}' ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "     AND   CLASSCD       = '".$subclass_array[0]."' ";
                    $where .= "     AND   SCHOOL_KIND   = '".$subclass_array[1]."' ";
                    $where .= "     AND   CURRICULUM_CD = '".$subclass_array[2]."' ";
                    $where .= "     AND   SUBCLASSCD    = '".$subclass_array[3]."' ";
                } else {
                    $where .= "     AND   SUBCLASSCD   = '".$chaircd_subclass_array[1]."' ";
                }

                $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
                $db->query($query);
            } else {
                $data["YEAR"][TEXT]         = $scoreData["YEAR"];
                $data["SEMESTER"][TEXT]     = $scoreData["SEMESTER"];
                $data["TESTKINDCD"][TEXT]   = $scoreData["TESTKINDCD"];
                $data["TESTITEMCD"][TEXT]   = $scoreData["TESTITEMCD"];
                $data["SCORE_DIV"][TEXT]    = $scoreData["SCORE_DIV"];
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]              = $scoreData["CLASSCD"];
                    $data["SCHOOL_KIND"][TEXT]          = $scoreData["SCHOOL_KIND"];
                    $data["CURRICULUM_CD"][TEXT]        = $scoreData["CURRICULUM_CD"];
                    $data["SUBCLASSCD"][TEXT]           = $scoreData["SUBCLASSCD"];
                } else {
                    $data["SUBCLASSCD"][TEXT]   = $scoreData["SUBCLASSCD"];
                }
                $data["SCHREGNO"][TEXT]     = $scoreData["SCHREGNO"];
                $data["CHAIRCD"][TEXT]      = $scoreData["CHAIRCD"];
                $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
                $db->query($query);
            }
        }
    }

    function getRecordScore($model, $schregNo) {
        //講座コード、科目コードを分ける
        $chaircd_subclass_array = array();
        $chaircd_subclass_array = explode(":", $model->field["CHAIRCD_SUBCLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            //科目対応
            $subclass_array = array();
            $subclass_array = explode("-", $chaircd_subclass_array[1]);
        }

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND TESTKINDCD   = '99' ";
        $query .= "     AND TESTITEMCD   = '00' ";
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            if ($model->field["SEMESTER"] == '9') {
                $query .= "     AND   SCORE_DIV    = '09' ";
            } else {
                $query .= "     AND   SCORE_DIV    = '08' ";
            }
        } else {
            $query .= "     AND   SCORE_DIV    = '00' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND   CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "  AND   SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "  AND   CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "  AND   SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND   SUBCLASSCD   = '".$chaircd_subclass_array[1]."' ";
        }
        $query .= "  AND   SCHREGNO     = '{$schregNo}' ";
        return $query;
    }

    function getRecordScoreData($model, $schregNo, $regdSeme, $chaircd_subclass_array, $subclass_array) {
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     '{$model->field["SEMESTER"]}' AS SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.CLASSCD, ";
            $query .= "         L1.SCHOOL_KIND, ";
            $query .= "         L1.CURRICULUM_CD, ";
        }
        $query .= "     L1.SUBCLASSCD, ";
        $query .= "     SUM(L3.ASSESSLEVEL) AS TOTAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN JVIEWSTAT_RECORD_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND L1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND   L1.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "          AND   L1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "          AND   L1.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "          AND   L1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND   L1.SUBCLASSCD   = '".$chaircd_subclass_array[1]."' ";
        }
        $query .= "     INNER JOIN JVIEWSTAT_INPUTSEQ_DAT L2 ON L1.YEAR = L2.YEAR ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND L1.CLASSCD = L2.CLASSCD ";
            $query .= "         AND L1.SCHOOL_KIND = L2.SCHOOL_KIND ";
            $query .= "         AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ";
        }
        $query .= "          AND L1.SUBCLASSCD = L2.SUBCLASSCD ";
        $query .= "          AND L1.VIEWCD = L2.VIEWCD ";
        $query .= "          AND T1.GRADE = L2.GRADE ";
        $query .= "          AND L1.SEMESTER = L2.SEMESTER ";
        $query .= "          AND L2.VIEWFLG = '1' ";
        $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_MST L3 ON L3.YEAR = L2.YEAR ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND L3.CLASSCD = L2.CLASSCD ";
            $query .= "                                  AND L3.SCHOOL_KIND = L2.SCHOOL_KIND ";
            $query .= "                                  AND L3.CURRICULUM_CD = L2.CURRICULUM_CD ";
        }
        $query .= "                                      AND L3.SUBCLASSCD  = L2.SUBCLASSCD ";
        $query .= "                                      AND L3.VIEWCD = L2.VIEWCD ";
        $query .= "                                      AND L3.DIV = '1' ";
        $query .= "                                      AND L3.GRADE = L2.GRADE ";
        $query .= "                                      AND L3.ASSESSMARK = L1.STATUS ";
        
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$regdSeme}' ";
        $query .= "     AND T1.SCHREGNO = '{$schregNo}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         L1.CLASSCD, ";
            $query .= "         L1.SCHOOL_KIND, ";
            $query .= "         L1.CURRICULUM_CD, ";
        }
        $query .= "     L1.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     '99' AS TESTKINDCD, ";
        $query .= "     '00' AS TESTITEMCD, ";
        if ($model->Properties["useTestCountflg"] == 'TESTITEM_MST_COUNTFLG_NEW_SDIV') {
            if ($model->field["SEMESTER"] == '9') {
                $query .= "     '09' AS SCORE_DIV, ";
            } else {
                $query .= "     '08' AS SCORE_DIV, ";
            }
        } else {
            $query .= "     '00' AS SCORE_DIV, ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '{$chaircd_subclass_array[0]}' AS CHAIRCD, ";
        $query .= "     L1.ASSESSLEVEL AS VALUE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= "     INNER JOIN JVIEWSTAT_LEVEL_MST L1 ON L1.YEAR = T1.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "         AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "          AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "          AND L1.VIEWCD = '9999' ";
        $query .= "          AND L1.DIV = '2' ";
        $query .= "          AND L1.GRADE = T1.GRADE ";
        $query .= "          AND T1.TOTAL BETWEEN L1.ASSESSLOW AND L1.ASSESSHIGH ";
        return $query;
    }
}
?>
