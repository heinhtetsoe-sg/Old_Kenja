<?php

require_once('for_php7.php');

class knje151fquery extends Query {

    //名称マスタの取得
    function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科取得
    function getClassMst($model, $classcd="")
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND  AS VALUE, ";
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND  || ' ' || T2.CLASSNAME AS LABEL, ";
        } else {
            $query .= "     T2.CLASSCD AS VALUE, ";
            $query .= "     T2.CLASSCD || ' ' || T2.CLASSNAME AS LABEL, ";
        }
        $query .= "     VALUE(T2.ELECTDIV,'0') AS ELECTDIV ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_YDAT T1, ";
        $query .= "     V_CLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD       = T2.CLASSCD  AND ";
            $query .= "     T1.SCHOOL_KIND   = T2.SCHOOL_KIND  ";
        } else {
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = T2.CLASSCD ";
        }
        if($classcd){
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND T2.CLASSCD          = '".$class_array[0]."' ";
                $query .= "     AND T2.SCHOOL_KIND      = '".$class_array[1]."' ";
            } else {
                $query .= "     AND T2.CLASSCD = '".$classcd."' ";
            }
        }
        $query .= "     AND EXISTS (SELECT 'X' ";
        $query .= "             FROM ";
        $query .= "                 JVIEWNAME_SUB_MST T3, ";
        $query .= "                 SCHREG_REGD_GDAT T4 ";
        $query .= "             WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 T1.CLASSCD = T3.CLASSCD AND ";
            $query .= "                 T1.SCHOOL_KIND = T3.SCHOOL_KIND AND ";
            $query .= "                 T1.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
        }
        $query .= "                 T1.SUBCLASSCD = T3.SUBCLASSCD AND ";
        $query .= "                 T1.VIEWCD = T3.VIEWCD AND ";
        $query .= "                 T1.YEAR = T4.YEAR AND ";
        $query .= "                 T3.SCHOOL_KIND = T4.SCHOOL_KIND )";
        /*if($model->field["GRADE_HR_CLASS"]){
            $query .= "     AND EXISTS (SELECT 'X' ";
            $query .= "             FROM ";
            $query .= "                 JVIEWNAME_SUB_MST T3, ";
            $query .= "                 SCHREG_REGD_GDAT T4 ";
            $query .= "             WHERE ";
            $query .= "                 T1.SUBCLASSCD = T3.SUBCLASSCD AND ";
            $query .= "                 T1.VIEWCD = T3.VIEWCD AND ";
            $query .= "                 T1.YEAR = T4.YEAR AND ";
            $query .= "                 T3.SCHOOL_KIND = T4.SCHOOL_KIND AND ";
            //$query .= "                 T4.GRADE = SUBSTR('".$model->field["GRADE_HR_CLASS"]."',1,2)) ";
        }*/
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubclassMst($classcd, $model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
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
        $query .= "     JVIEWNAME_SUB_YDAT T1, ";
        $query .= "     V_SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD = T2.CLASSCD  AND ";
            $query .= "    T1.SCHOOL_KIND = T2.SCHOOL_KIND  AND ";
            $query .= "    T1.CURRICULUM_CD = T2.CURRICULUM_CD  AND ";
        }
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$class_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$class_array[1]."' ";
        } else {
            $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) = '".$classcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座データの取得
    function selectChairQuery($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.chaircd AS VALUE, ";
        $query .= "     T1.chaircd || ' ' || T1.chairname AS LABEL ";
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
        if(AUTHORITY != DEF_UPDATABLE){
          $query .= "       t3.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "    T1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "    T1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "    T1.SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$class_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$class_array[1]."' ";
        } else {
            $query .= "    substr(T1.subclasscd,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.chaircd ";
        return $query;
    }

    //編集可能項目の判別
    function selectViewcdQuery($model){
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_YDAT T1, ";
        $query .= "     JVIEWNAME_SUB_MST T2 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "    T1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "    T1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "    T1.SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD = T2.CLASSCD  AND ";
            $query .= "    T1.SCHOOL_KIND = T2.SCHOOL_KIND  AND ";
            $query .= "    T1.CURRICULUM_CD = T2.CURRICULUM_CD  AND ";
        }
        $query .= "    T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "    T1.VIEWCD = T2.VIEWCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= "    AND T2.SCHOOL_KIND IN ('P','J') ";
        /*if($model->field["GRADE_HR_CLASS"]){
            $query .= "   AND T2.SCHOOL_KIND IN(SELECT ";
            $query .= "                             SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_GDAT ";
            $query .= "                         WHERE ";
            $query .= "                             YEAR = '".CTRL_YEAR."'  AND ";
            $query .= "                             GRADE = SUBSTR('".$model->field["GRADE_HR_CLASS"]."',1,2)) ";
        }*/
        $query .= " ORDER BY ";
        $query .= "    T1.VIEWCD ";

        return $query;
    }
    
    //編集可能項目の判別2(追加)
    function getSchool_kind($grade){
        $query  = "   SELECT ";
        $query .= "       SCHOOL_KIND ";
        $query .= "   FROM ";
        $query .= "       SCHREG_REGD_GDAT ";
        $query .= "   WHERE ";
        $query .= "       YEAR  = '".CTRL_YEAR."'  AND ";
        $query .= "       GRADE = '".$grade."' ";
        
        return $query;
    }

    //編集可能項目の判別2(追加)
    function getJviewName_School_kind($model, $School_kind) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_YDAT T1, ";
        $query .= "     JVIEWNAME_SUB_MST T2 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "    T1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "    T1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "    T1.SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD = T2.CLASSCD  AND ";
            $query .= "    T1.SCHOOL_KIND = T2.SCHOOL_KIND  AND ";
            $query .= "    T1.CURRICULUM_CD = T2.CURRICULUM_CD  AND ";
        }
        $query .= "    T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "    T1.VIEWCD    = T2.VIEWCD AND ";
        $query .= "    T2.SCHOOL_KIND = '".$School_kind."' ";
        
        return $query;
    }

    //編集可能項目の判別3(追加)
    function getJviewName_Viewcd($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_SUB_YDAT T1, ";
        $query .= "     JVIEWNAME_SUB_MST T2 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "    T1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "    T1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "    T1.SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD = T2.CLASSCD  AND ";
            $query .= "    T1.SCHOOL_KIND = T2.SCHOOL_KIND  AND ";
            $query .= "    T1.CURRICULUM_CD = T2.CURRICULUM_CD  AND ";
        }
        $query .= "    T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "    T1.VIEWCD    = T2.VIEWCD ";
        
        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $execute_date, $view_key)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
            //教科対応
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
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
        $query .= "        SCHREG_REGD_FI_HDAT T1, ";
        $query .= "        SCHREG_REGD_FI_DAT T2, ";
        $query .= "        SCHREG_BASE_MST T3 ";
        $query .= "     WHERE ";
        $query .= "        T1.YEAR      = T2.YEAR AND ";
        $query .= "        T1.SEMESTER  = T2.SEMESTER AND ";
        $query .= "        T1.GRADE     = T2.GRADE AND ";
        $query .= "        T1.HR_CLASS  = T2.HR_CLASS AND ";
        $query .= "        T2.YEAR      = '".CTRL_YEAR."' AND ";
        $query .= "        T2.SEMESTER  = '".$model->field["SEMESTER2"]."' AND ";
        $query .= "        T2.SCHREGNO  = T3.SCHREGNO ";
        $query .= " ), VIEWSTAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        // "1" => "1101"
        foreach ($view_key as $code => $col)
        {
            $query .= "    ,MAX(CASE WHEN T2.VIEWCD = '".$col."' THEN T2.STATUS ELSE NULL END) AS STATUS".$code;
        }
        $query .= "     FROM ";
        $query .= "         SCHINFO T1, ";
        $query .= "         JVIEWSTAT_SUB_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "        T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T2.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "        T1.SCHREGNO = T2.SCHREGNO AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T2.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "        T2.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "        T2.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "        T2.SUBCLASSCD       = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "        T2.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //観点コード
        if (get_count($view_key) > 0) 
            $query .= "    T2.VIEWCD IN ('".implode("','", $view_key)."') AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(T2.VIEWCD, 1, 2) = '".$class_array[0]."' ";
        } else {
            $query .= "    SUBSTR(T2.VIEWCD, 1, 2) = '".$model->field["CLASSCD"]."' ";
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
        foreach ($view_key as $code => $col)
        {
            if (substr($col,2) != "99") {
                $query .= ",T2.STATUS".$code;
            } else {
                $query .= ",T3.VALUATION AS STATUS".$code;
            }
        }
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN VIEWSTAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_STUDYREC_DAT T3 ";
        $query .= "              ON T3.SCHOOLCD     = '0' ";
        $query .= "             AND T3.YEAR         = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T3.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "             AND T3.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "             AND T3.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "             AND T3.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "             AND T3.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "             AND T3.SCHREGNO     = T1.SCHREGNO ";
        //追加-------------------
        $query .= "     LEFT JOIN CHAIR_DAT T4 ";
        $query .= "              ON T4.chaircd = '".$model->field["CHAIRCD"]."' ";
        $query .= "             AND T4.YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND T4.CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "             AND T4.SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "             AND T4.CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "             AND T4.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "             AND T4.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "             AND T4.SEMESTER = '".$model->field["SEMESTER2"]."' ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T5 ";
        $query .= "              ON T5.chaircd = T4.chaircd ";
        $query .= "             AND T5.YEAR = T4.YEAR  ";
        $query .= "             AND T5.SEMESTER = T4.SEMESTER ";
        $query .= "             AND T5.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //更新
    function update($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();

        for($i=0; $i < get_count($model->data["SCHREGNO"]); $i++){

            //削除
            $query  = "DELETE FROM ";
            $query .= "    JVIEWSTAT_SUB_DAT ";
            $query .= "WHERE ";
            $query .= "    YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "    SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $query .= "    SCHREGNO = '".$model->data["SCHREGNO"][$i]."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD          = '".$subclass_array[0]."' AND ";
                $query .= "    SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
                $query .= "    CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
                $query .= "    SUBCLASSCD       = '".$subclass_array[3]."' ";
            } else {
                $query .= "    SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
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
                    $data["CLASSCD"][TEXT]        = $subclass_array[0];
                    $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
                    $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
                    $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
                } else {
                    $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
                }
                $data["VIEWCD"][TEXT]       = $col;
                $data["STATUS"][TEXT]       = $model->fields["STATUS".$code][$i];
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "JVIEWSTAT_SUB_DAT");
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }

    /* 成績データ更新処理 */
    function updateRecord($model)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {

            //年次の取得
            $query  = " SELECT ";
            $query .= "     ANNUAL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_FI_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO    = '".$model->data["SCHREGNO"][$i]."' AND ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER    = '".CTRL_SEMESTER."' ";
            $annual = $db->getOne($query);

            foreach ($model->data["STATUS"] as $code => $col) {

                //評定のみ更新
                if (substr($col,2) != "99") continue;

                //数字に変換
                $status = $model->fields["STATUS".$code][$i];
                if ($status == "A") $status = "11";
                if ($status == "B") $status = "22";
                if ($status == "C") $status = "33";

                //削除
                $query  = "DELETE FROM SCHREG_STUDYREC_DAT ";
                $query .= "WHERE   SCHOOLCD     = '0' ";
                $query .= "  AND   YEAR         = '".CTRL_YEAR."' ";
                $query .= "  AND   SCHREGNO     = '".$model->data["SCHREGNO"][$i]."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "  AND   CLASSCD          = '".$subclass_array[0]."' ";
                    $query .= "  AND   SCHOOL_KIND      = '".$subclass_array[1]."' ";
                    $query .= "  AND   CURRICULUM_CD    = '".$subclass_array[2]."' ";
                    $query .= "  AND   SUBCLASSCD       = '".$subclass_array[3]."' ";
                } else {
                    $query .= "  AND   SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
                }
                $db->query($query);

                //追加
                $data = array();
                $data["SCHOOLCD"][TEXT]     = "0";
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SCHREGNO"][TEXT]     = $model->data["SCHREGNO"][$i];
                $data["ANNUAL"][TEXT]       = $annual;
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $data["CLASSCD"][TEXT]        = $subclass_array[0];
                    $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
                    $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
                    $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
                } else {
                    $data["CLASSCD"][TEXT]      = $model->field["CLASSCD"];
                    $data["SUBCLASSCD"][TEXT]   = $model->field["SUBCLASSCD"];
                }
                $data["VALUATION"][NUMBER]  = $status;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::insertSQL($data, "SCHREG_STUDYREC_DAT");
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
    }
}
?>
