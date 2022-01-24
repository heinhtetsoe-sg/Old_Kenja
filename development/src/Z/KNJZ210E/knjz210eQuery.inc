<?php

require_once('for_php7.php');

class knjz210equery extends Query {

    //段階値の設定数取得
    function getAssesslevel($namecd1)
    {
        $query  = " SELECT ";
        $query .= "      COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    //名称マスタ
    function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //登録学期数の取得
    function getSemester($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        $query .= " AND NAMECD2 <> '9' ";
        
        return $query;
    }

    //学年取得
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

    //観点取得
    function getViewcd($model){
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT DISTINCT";
        $query .= "    T1.VIEWCD AS VALUE, ";
        $query .= "    T1.VIEWNAME AS LABEL, ";
        $query .= "    T1.SHOWORDER ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_MST T1 ";
        $query .= " WHERE ";
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
        $query .= " ORDER BY ";
        $query .= "    T1.SHOWORDER, ";
        $query .= "    T1.VIEWCD ";

        return $query;
    }

    //観点数取得
    function getcountViewcd($model){
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
            $class_array = array();
            $class_array = explode("-", $model->field["CLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST T2 ON T2.GRADE        = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND T2.CLASSCD      = T1.CLASSCD ";
            $query .= "                                 AND T2.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "                                 AND T2.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        }
        $query .= "                                     AND T2.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "                                     AND T2.VIEWCD       = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR          = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE         = '".$model->field["GRADE"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "    T1.SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "    T1.CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "    T1.SUBCLASSCD    = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "    T1.SUBCLASSCD    = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$class_array[0]."' ";
        } else {
            $query .= "    SUBSTR(T1.VIEWCD,1,2) = '".$model->field["CLASSCD"]."' ";
        }

        return $query;
    }

    //段階値のチェック数取得
    function getViewflg($model, $semester) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(VIEWFLG) ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_INPUTSEQ_DAT T1 ";
        $query .= " LEFT JOIN JVIEWNAME_GRADE_MST T2 ON T2.GRADE      = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND T2.CLASSCD      = T1.CLASSCD ";
            $query .= "                                 AND T2.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "                                 AND T2.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        }
        $query .= "                                 AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                 AND T2.VIEWCD     = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."'  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($model->field["DIV"] === '1') {
            $query .= " AND T1.VIEWCD       = '".$model->field["VIEWCD"]."' ";
        }
        $query .= " AND T1.GRADE        = '".$model->field["GRADE"]."' ";
        $query .= " AND T1.SEMESTER     = '".$semester."' ";

        return $query;
    }

    //対象データの段階値数チェック
    function selectCountQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " SELECT  ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "     T1.CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "     T1.SUBCLASSCD    = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "     T1.SUBCLASSCD  = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        if ($model->field["DIV"] === '1') {
            $query .= "     T1.VIEWCD      = '".$model->field["VIEWCD"]."' AND ";
        } else {
            $query .= "     T1.VIEWCD      = '9999' AND ";
        }
        $query .= "     T1.DIV         = '".$model->field["DIV"]."' AND ";
        $query .= "     T1.GRADE       = '".$model->field["GRADE"]."'  ";
        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $query  = " WITH NAMEDATA AS ( ";
        for ($i = 1; $i <= $model->field["MAX_ASSESSLEVEL"]; $i++) {
            if ($i > 1) {
                $query .= "  UNION ";
            }
            $query .= "  SELECT ";
            $query .= "      '".$i."' AS NAMESPARE2 ";
            $query .= "  FROM ";
            $query .= "      SYSIBM.SYSDUMMY1 ";
        }
        $query .= " ), ASSESSDATA AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.ASSESSLOW, ";
        $query .= "     T1.ASSESSHIGH, ";
        $query .= "     T1.ASSESSLEVEL, ";
        $query .= "     T1.ASSESSMARK ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD       = '".$subclass_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND   = '".$subclass_array[1]."' AND ";
            $query .= "     T1.CURRICULUM_CD = '".$subclass_array[2]."' AND ";
            $query .= "     T1.SUBCLASSCD    = '".$subclass_array[3]."' AND ";
        } else {
            $query .= "     T1.SUBCLASSCD  = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        if ($model->field["DIV"] === '1') {
            $query .= "     T1.VIEWCD      = '".$model->field["VIEWCD"]."' AND ";
        } else {
            $query .= "     T1.VIEWCD      = '9999' AND ";
        }
        $query .= "     T1.DIV         = '".$model->field["DIV"]."' AND ";
        $query .= "     T1.GRADE       = '".$model->field["GRADE"]."'  ";
        
        $query .= " ), ASSESSDATA_LIST AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     ROW_NUMBER() OVER(ORDER BY T2.ASSESSLEVEL) AS NAMESPARE2, ";
        $query .= "     T2.ASSESSLEVEL, ";
        $query .= "     T2.ASSESSLOW, ";
        $query .= "     T2.ASSESSHIGH, ";
        $query .= "     T2.ASSESSMARK ";
        $query .= " FROM ";
        $query .= "     ASSESSDATA T2 ";
        $query .= " ORDER BY ";
        $query .= "     T2.ASSESSLEVEL DESC";
        $query .= " ) ";
        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     T1.NAMESPARE2, ";
        $query .= "     T3.NAMESPARE1, ";
        $query .= "     T2.ASSESSLEVEL, ";
        $query .= "     T2.ASSESSLOW, ";
        $query .= "     T2.ASSESSHIGH, ";
        $query .= "     T2.ASSESSMARK ";
        $query .= " FROM ";
        $query .= "     NAMEDATA T1 ";
        $query .= " LEFT JOIN ASSESSDATA_LIST T2 ON int(T1.NAMESPARE2) = T2.NAMESPARE2 ";
        $query .= " LEFT JOIN V_NAME_MST T3 ON T3.YEAR = '".CTRL_YEAR."' ";
        $query .= "                        AND T3.NAMECD1 = 'D029' ";
        $query .= "                        AND T3.NAMESPARE2 = T1.NAMESPARE2 ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMESPARE2 DESC";
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
        $query .= "     JVIEWSTAT_LEVEL_MST ";
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
        if ($model->field["DIV"] === '1') {
            $query .= "     VIEWCD     = '".$model->field["VIEWCD"]."' AND ";
        }
        $query .= "     DIV        = '".$model->field["DIV"]."' AND ";
        $query .= "     GRADE      = '".$model->field["GRADE"]."' ";

        $db->query($query);
        
        return;
    }
        
    /* データ更新処理 */
    function update($model, $db) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        for ($i = 0; $i < get_count($model->data["NAMESPARE2"]); $i++) {
            $assesshigh  = $model->data["ASSESSHIGH"][$i];
            if ($i == 0) {
                $set_assesshigh  = intval($model->fields["ASSESSLEVEL"][$i] * $model->viewflg_check);
            }
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
            if ($model->field["DIV"] === '1') {
                $data["VIEWCD"][TEXT]        = $model->field["VIEWCD"];
            } else {
                $data["VIEWCD"][TEXT]        = '9999';
            }
            $data["DIV"][TEXT]           = $model->field["DIV"];
            $data["GRADE"][TEXT]         = $model->field["GRADE"];
            $data["ASSESSLEVEL"][NUMBER] = $model->fields["ASSESSLEVEL"][$i];
            if ($model->data["NAMESPARE2"][$i] === '1') {
                $data["ASSESSLOW"][NUMBER]   = 1;
            } else {
                $data["ASSESSLOW"][NUMBER]   = $model->fields["ASSESSLOW"][$i];
            }
            
            if ($i == 0) {
                $data["ASSESSHIGH"][NUMBER]  = $set_assesshigh;
            } else {
                $data["ASSESSHIGH"][NUMBER]  = $model->fields["ASSESSHIGHT"][$i];
            }
            $data["ASSESSMARK"][TEXT]    = $model->fields["ASSESSMARK"][$i];
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "JVIEWSTAT_LEVEL_MST");
            $db->query($query);
        }
        return;
    }

}
?>
