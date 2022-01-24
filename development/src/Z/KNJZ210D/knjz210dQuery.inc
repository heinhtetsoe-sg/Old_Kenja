<?php

require_once('for_php7.php');

class knjz210dquery extends Query {

    //校種の取得
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$grade."' ";
        
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
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        
        return $query;
    }

    //学期の数を取得
    function getSemestercount($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 <> '9' ";

        return $query;
    }

    //年組一覧取得
    function getGrade($model) {
        $query  = " SELECT DISTINCT";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     G1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1";
        $query .= "     LEFT JOIN  SCHREG_REGD_GDAT G1 ON G1.YEAR  = T1.YEAR ";
        $query .= "                                   AND G1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= "     AND G1.SCHOOL_KIND  <> 'H' ";
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
    function getClassMst($classcd, $grade, $model)
    {
        $query  = " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SUBSTR(T1.VIEWCD,1,2) || '-' || T1.SCHOOL_KIND  AS VALUE, ";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) || '-' || T1.SCHOOL_KIND || ':' || T2.CLASSNAME AS LABEL ";
        } else {
            $query .= "     SUBSTR(T1.VIEWCD,1,2) AS VALUE, ";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) || ' ' || T2.CLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= "     LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = SUBSTR(T1.VIEWCD,1,2)";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                       AND T2.CLASSCD = T1.CLASSCD ";
            $query .= "                       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".$grade."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD = T2.CLASSCD  AND ";
            $query .= "     T1.SCHOOL_KIND = T2.SCHOOL_KIND  AND ";
        }
        $query .= "     SUBSTR(T1.VIEWCD,1,2) = T2.CLASSCD ";
        if($classcd){
            $query .= "     AND T2.CLASSCD = '".$classcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubclassMst($classcd, $grade, $model)
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
            $query .= "                          AND T2.CLASSCD         = T1.CLASSCD ";
            $query .= "                          AND T2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                          AND T2.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.GRADE = '".$grade."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$class_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$class_array[1]."' AND ";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "     SUBSTR(T1.SUBCLASSCD,1,2) = '".$classcd."' AND";
            $query .= "     SUBSTR(T1.VIEWCD,1,2) = '".$classcd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model, $semester_count) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " WITH VIEWNAME AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME, ";
        $query .= "     T2.SHOWORDER ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST T2  ON T2.GRADE       = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T2.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T2.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T2.SUBCLASSCD  = T1.SUBCLASSCD ";
        $query .= "                                      AND T2.VIEWCD      = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "    T1.SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "    T1.CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "    T1.SUBCLASSCD    = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$model->field["CLASSCD"]."' ";
        }

        $query .= " ), ASSESSHIGH AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T3.ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST T2  ON T2.GRADE       = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T2.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T2.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T2.SUBCLASSCD  = T1.SUBCLASSCD ";
        $query .= "                                      AND T2.VIEWCD      = T1.VIEWCD ";
        $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_MST T3  ON T3.YEAR        = T1.YEAR ";
        $query .= "                                      AND T3.GRADE       = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T3.SUBCLASSCD  = T1.SUBCLASSCD ";
        $query .= "                                      AND T3.VIEWCD      = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "    T1.SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "    T1.CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "    T1.SUBCLASSCD    = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$subclass_array[0]."' AND";
        } else {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$model->field["CLASSCD"]."' AND";
        }
        $query .= "    T3.DIV = '1' AND";
        $query .= "    T3.ASSESSLEVEL IN ";
        $query .= "                         (SELECT ";
        $query .= "                              MAX(ASSESSLEVEL) AS MAX_ASSESSLEVEL ";
        $query .= "                          FROM ";
        $query .= "                              JVIEWSTAT_LEVEL_MST M1 ";
        $query .= "                          WHERE ";
        $query .= "                              M1.YEAR = T3.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                      AND M1.CLASSCD         = T3.CLASSCD ";
            $query .= "                      AND M1.SCHOOL_KIND     = T3.SCHOOL_KIND ";
            $query .= "                      AND M1.CURRICULUM_CD   = T3.CURRICULUM_CD ";
        }
        $query .= "                          AND M1.SUBCLASSCD      = T3.SUBCLASSCD ";
        $query .= "                          AND M1.VIEWCD          = T3.VIEWCD ";
        $query .= "                          AND M1.DIV             = T3.DIV ";
        $query .= "                          AND M1.GRADE           = T3.GRADE ";
        $query .= "                          ) ";

        $query .= " ), SEMESTER1 AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.VIEWFLG, ";
        $query .= "     T3.SHOWORDER ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_INPUTSEQ_DAT T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR        = T1.YEAR ";
        $query .= "                                      AND T2.GRADE       = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T2.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T2.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T2.SUBCLASSCD  = T1.SUBCLASSCD ";
        $query .= "                                      AND T2.VIEWCD      = T1.VIEWCD ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST T3  ON T3.GRADE       = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T3.SUBCLASSCD  = T1.SUBCLASSCD ";
        $query .= "                                      AND T3.VIEWCD      = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "    T1.SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "    T1.CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "    T1.SUBCLASSCD    = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$subclass_array[0]."' AND";
        } else {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$model->field["CLASSCD"]."' AND";
        }
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "    T1.SEMESTER = '1' ";
        
        $query .= " ), SEMESTER2 AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.VIEWFLG, ";
        $query .= "     T3.SHOWORDER ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_INPUTSEQ_DAT T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR        = T1.YEAR ";
        $query .= "                                      AND T2.GRADE       = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T2.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T2.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T2.SUBCLASSCD  = T1.SUBCLASSCD ";
        $query .= "                                      AND T2.VIEWCD      = T1.VIEWCD ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST T3  ON T3.GRADE       = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T3.SUBCLASSCD  = T1.SUBCLASSCD ";
        $query .= "                                      AND T3.VIEWCD      = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "    T1.SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "    T1.CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "    T1.SUBCLASSCD    = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$subclass_array[0]."' AND";
        } else {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$model->field["CLASSCD"]."' AND";
        }
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "    T1.SEMESTER = '2' ";
        $query .= " ) ";
        
        //3学期がある場合
        if ($semester_count == 3) {
            $query .= " , SEMESTER3 AS ( ";
            $query .= " SELECT DISTINCT";
            $query .= "     T1.VIEWCD, ";
            $query .= "     T1.VIEWFLG, ";
            $query .= "     T3.SHOWORDER ";
            $query .= " FROM ";
            $query .= "     JVIEWSTAT_INPUTSEQ_DAT T1 ";
            $query .= "     LEFT JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR        = T1.YEAR ";
            $query .= "                                      AND T2.GRADE       = T1.GRADE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                  AND T2.CLASSCD         = T1.CLASSCD ";
                $query .= "                                  AND T2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
                $query .= "                                  AND T2.CURRICULUM_CD   = T1.CURRICULUM_CD ";
            }
            $query .= "                                      AND T2.SUBCLASSCD  = T1.SUBCLASSCD ";
            $query .= "                                      AND T2.VIEWCD      = T1.VIEWCD ";
            $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST T3  ON T3.GRADE       = T1.GRADE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                  AND T3.CLASSCD         = T1.CLASSCD ";
                $query .= "                                  AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
                $query .= "                                  AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
            }
            $query .= "                                      AND T3.SUBCLASSCD  = T1.SUBCLASSCD ";
            $query .= "                                      AND T3.VIEWCD      = T1.VIEWCD ";
            $query .= " WHERE ";
            $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    T1.CLASSCD       = '".$subclass_array[0]."' AND ";
                $query .= "    T1.SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
                $query .= "    T1.CURRICULUM_CD = '".$subclass_array[2]."' AND ";
                $query .= "    T1.SUBCLASSCD    = '".$subclass_array[3]."' AND ";
            } else {
                $query .= "    T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
            }
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$subclass_array[0]."' AND";
            } else {
                $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$model->field["CLASSCD"]."' AND";
            }
            $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND ";
            $query .= "    T1.SEMESTER = '3' ";
            $query .= " ) ";
        }
        
        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     V1.VIEWCD, ";
        $query .= "     V1.VIEWNAME, ";
        $query .= "     T1.VIEWFLG AS VIEWFLG1, ";
        $query .= "     T2.VIEWFLG AS VIEWFLG2, ";
        if ($semester_count == 3) {
            $query .= "     T3.VIEWFLG AS VIEWFLG3, ";
        }
        $query .= "     V2.ASSESSHIGH, ";
        $query .= "     V1.SHOWORDER ";
        $query .= " FROM ";
        $query .= "     VIEWNAME V1 ";
        $query .= " LEFT JOIN ASSESSHIGH V2 ON V2.VIEWCD = V1.VIEWCD ";
        $query .= " LEFT JOIN SEMESTER1  T1 ON T1.VIEWCD = V1.VIEWCD ";
        $query .= " LEFT JOIN SEMESTER2  T2 ON T2.VIEWCD = V1.VIEWCD ";
        if ($semester_count == 3) {
            $query .= " LEFT JOIN SEMESTER3 T3 ON T3.VIEWCD = V1.VIEWCD ";
        }
        $query .= " ORDER BY ";
        $query .= "     V1.SHOWORDER, ";
        $query .= "     V1.VIEWCD ";

        return $query;
    }

    //データ削除
    function delete($model, $db) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " DELETE FROM ";
        $query .= "     JVIEWSTAT_INPUTSEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "     SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "     CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "     SUBCLASSCD    = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     GRADE      = '".$model->field["GRADE"]."' ";

        $db->query($query);
        
        return;
    }
    
    //データカウント
    function count($model, $viewcd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_INPUTSEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "     SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "     CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "     SUBCLASSCD    = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     VIEWCD     = '".$viewcd."' AND ";
        $query .= "     GRADE      = '".$model->field["GRADE"]."' AND ";
        $query .= "     VIEWFLG    = '1' ";

        return $query;
    }
    
    //科目選択時のデータ有無のチェック
    function setdatacount($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_INPUTSEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "     SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "     CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "     SUBCLASSCD    = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     GRADE      = '".$model->field["GRADE"]."' ";

        return $query;
    }
    
    //ASSESSHIGHT nullカウント
    function AssessHighcount($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST T2  ON T2.GRADE       = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T2.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T2.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T2.SUBCLASSCD  = T1.SUBCLASSCD ";
        $query .= "                                      AND T2.VIEWCD      = T1.VIEWCD ";
        $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_MST T3  ON T3.YEAR        = T1.YEAR ";
        $query .= "                                      AND T3.GRADE       = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T3.SUBCLASSCD  = T1.SUBCLASSCD ";
        $query .= "                                      AND T3.VIEWCD      = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "    T3.ASSESSHIGH IS NULL AND";
        $query .= "    T3.DIV = '1' AND";
        $query .= "    T3.ASSESSLEVEL IN ";
        $query .= "                         (SELECT ";
        $query .= "                              MAX(ASSESSLEVEL) AS MAX_ASSESSLEVEL ";
        $query .= "                          FROM ";
        $query .= "                              JVIEWSTAT_LEVEL_MST M1 ";
        $query .= "                          WHERE ";
        $query .= "                              M1.YEAR = T3.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                      AND M1.CLASSCD         = T3.CLASSCD ";
            $query .= "                      AND M1.SCHOOL_KIND     = T3.SCHOOL_KIND ";
            $query .= "                      AND M1.CURRICULUM_CD   = T3.CURRICULUM_CD ";
        }
        $query .= "                          AND M1.SUBCLASSCD      = T3.SUBCLASSCD ";
        $query .= "                          AND M1.VIEWCD          = T3.VIEWCD ";
        $query .= "                          AND M1.DIV             = T3.DIV ";
        $query .= "                          AND M1.GRADE           = T3.GRADE ";
        $query .= "                          ) ";

        return $query;
    }

    
    /* データ更新処理 */
    //学期ごとに追加または更新する
    function insJviewstatInputSeqDat($model, $db, $seme) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        for ($i = 0; $i < get_count($model->data["VIEWCD"]); $i++) {
            $viewcd = $model->data["VIEWCD"][$i];

            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]          = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
            } else {
                $data["SUBCLASSCD"][TEXT]    = $model->field["SUBCLASSCD"];
            }
            $data["VIEWCD"][TEXT]        = $viewcd;
            $data["GRADE"][TEXT]         = $model->field["GRADE"];
            $data["SEMESTER"][TEXT]      = $seme;
            $data["VIEWFLG"][TEXT]       = $model->fields["VIEWFLG".$seme][$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";

            //追加
            $query = Query::insertSQL($data, "JVIEWSTAT_INPUTSEQ_DAT");
            $db->query($query);
        }
        return;
    }

    //VIEWFLGが一致していない場合、JVIEWSTAT_LEVEL_MSTの観点の上限値（ASSESSHIGH）をNULLにする
    function deleteManten($model, $viewcd, $db) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $data = array();
        $data["ASSESSHIGH"][NUMBER]  = "";
        $data["ASSESSLOW"][NUMBER]  = "";
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][NUMBER]     = "sysdate()";
        
        $where  = " WHERE ";
        $where .= "      YEAR        = '" .CTRL_YEAR." '";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= " AND  CLASSCD       = '".$subclass_array[0]."' ";
            $where .= " AND  SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $where .= " AND  CURRICULUM_CD = '".$subclass_array[2]."' ";
            $where .= " AND  SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $where .= " AND  SUBCLASSCD  = '" .$model->field["SUBCLASSCD"]."'";
        }
        $where .= " AND  VIEWCD      = '" .$viewcd. "' ";
        $where .= " AND  DIV         = '1' ";
        $where .= " AND  GRADE       = '" .$model->field["GRADE"]."'";
        
        $query = Query::updateSQL($data, "JVIEWSTAT_LEVEL_MST", $where);        //追加
        $db->query($query);
        
        return;
    }
    
}
?>
