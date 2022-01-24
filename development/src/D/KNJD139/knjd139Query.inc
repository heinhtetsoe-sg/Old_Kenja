<?php

require_once('for_php7.php');

class knjd139query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

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
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    function getGradeHrclass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= "     LEFT JOIN  SCHREG_REGD_GDAT G1 ON G1.YEAR  = T1.YEAR ";
        $query .= "                                   AND G1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        if ($model->Properties["useSchool_KindField"] == "1"){
            $query .= "     AND G1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        if(AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //教科取得
    function getClassMst($model, $classcd, $grade_hr_class)
    {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SUBSTR(T1.VIEWCD,1,2) || '-' || T1.SCHOOL_KIND AS VALUE, ";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) || '-' || T1.SCHOOL_KIND || ' ' || T2.CLASSNAME AS LABEL ";
        } else {
            $query .= "     SUBSTR(T1.VIEWCD,1,2) AS VALUE, ";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) || ' ' || T2.CLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = SUBSTR(T1.VIEWCD,1,2)";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND  T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".substr($grade_hr_class,0,2)."' AND ";
        $query .= "     SUBSTR(T1.VIEWCD,1,2) = T2.CLASSCD ";
        $query .= "     AND SUBSTR(T1.VIEWCD,1,2) < '90' ";
        if ($model->Properties["useSchool_KindField"] == "1"){
            $query .= "     AND T1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        if($classcd){
            $query .= "     AND T2.CLASSCD = '".$classcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubclassMst($classcd, $grade_hr_class, $model)
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
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T2.SUBCLASSCD || ' ' || T2.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND  T2.CLASSCD = T1.CLASSCD ";
            $query .= "                         AND  T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                         AND  T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".substr($grade_hr_class,0,2)."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) = '".$class_array[0]."' AND";
            $query .= "     T1.SCHOOL_KIND = '".$class_array[1]."' AND";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) = '".$classcd."' AND";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = '".$classcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //観点取得
    function selectViewcdQuery($model, $schregno){
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT DISTINCT";
        $query .= "     R1.VIEWCD, ";
        $query .= "     R1.STATUS, ";
        $query .= "     T2.VIEWNAME, ";
        $query .= "     T2.SHOWORDER ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_RECORD_DAT R1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_YDAT T1 ON T1.YEAR        = R1.YEAR";
        $query .= "                                      AND T1.SUBCLASSCD  = R1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND  T1.CLASSCD = R1.CLASSCD ";
            $query .= "                                  AND  T1.SCHOOL_KIND = R1.SCHOOL_KIND ";
            $query .= "                                  AND  T1.CURRICULUM_CD = R1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T1.VIEWCD      = R1.VIEWCD ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST T2  ON T2.SUBCLASSCD  = R1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND  T1.CLASSCD = R1.CLASSCD ";
            $query .= "                                 AND  T1.SCHOOL_KIND = R1.SCHOOL_KIND ";
            $query .= "                                 AND  T1.CURRICULUM_CD = R1.CURRICULUM_CD ";
        }
        $query .= "                                     AND T2.VIEWCD      = R1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "    R1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    R1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "    R1.SCHREGNO = '".$schregno."' AND ";
        $query .= "    T1.GRADE = '".substr($model->field["GRADE_HR_CLASS"],0,2)."' AND ";
        $query .= "    T1.GRADE = T2.GRADE AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    R1.CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "    R1.SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "    R1.CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "    R1.SUBCLASSCD    = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    R1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  T1.CLASSCD = T2.CLASSCD AND ";
            $query .= "  T1.SCHOOL_KIND = T2.SCHOOL_KIND AND ";
            $query .= "  T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "    T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "    T1.VIEWCD = T2.VIEWCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(R1.VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "    SUBSTR(R1.VIEWCD,1,2) = '".$model->field["CLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "    T2.SHOWORDER, ";
        $query .= "    R1.VIEWCD ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " WITH REPORTREMARK AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         REMARK1 ";
        $query .= "     FROM ";
        $query .= "         JVIEWSTAT_REPORTREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '". CTRL_YEAR ."' ";
        $query .= "     AND SEMESTER    = '{$model->field["SEMESTER"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "     AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "     AND CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "     AND SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND SUBCLASSCD  = '{$model->field["SUBCLASSCD"]}' ";
        }

        $query .= " ), SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME, ";
        $query .= "         T3.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2, ";
        $query .= "         SCHREG_REGD_HDAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T1.YEAR = T3.YEAR ";
        $query .= "     AND T1.GRADE = T3.GRADE ";
        $query .= "     AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
        $query .= " ) ";
        
        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.REMARK1";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= " LEFT JOIN REPORTREMARK T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    function update($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            $query  = " DELETE FROM ";
            $query .= "     JVIEWSTAT_REPORTREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR       = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER   = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO   = '".$schregno."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     CLASSCD       = '".$subclass_array[0]."' AND ";
                $query .= "     SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
                $query .= "     CURRICULUM_CD = '".$subclass_array[2]."' AND ";
                $query .= "     SUBCLASSCD    = '".$subclass_array[3]."' ";
            } else {
                $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
            
            $db->query($query);

            if (!strlen($model->fields["REMARK1"][$i])) {
                continue;
            }

            //項目
            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["SEMESTER"][TEXT]      = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]      = $schregno;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]          = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
            } else {
                $data["SUBCLASSCD"][TEXT]    = $model->field["SUBCLASSCD"];
            }
            $data["REMARK1"][TEXT]       = $model->fields["REMARK1"][$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "JVIEWSTAT_REPORTREMARK_DAT");

            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
