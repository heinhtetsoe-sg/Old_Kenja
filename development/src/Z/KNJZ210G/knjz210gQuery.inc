<?php

require_once('for_php7.php');

class knjz210gQuery extends Query {

    //科目名取得
    function getSubclassName($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
        }

        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD    = '".$model->subclasscd."' ";
        }

        return $query;
    }

    //学期の取得
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
    
    //D029の存在チェック
    function getNamemstCheck($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";

        return $query;
    }
    
    //段階値の上限値取得
    function getAssesslevel($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     MAX(NAMESPARE2) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";

        return $query;
    }
    
    //段階値のチェック数取得
    function getViewflg($model, $semester) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(VIEWFLG) ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_INPUTSEQ_DAT T1 ";
        $query .= " LEFT JOIN JVIEWNAME_GRADE_MST T2 ON T2.GRADE      = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND T2.CLASSCD       = T1.CLASSCD ";
            $query .= "                             AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                             AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                                 AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                 AND T2.VIEWCD     = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."'  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T2.STUDYREC_CLASSCD       = '".$subclass_array[0]."' ";
            $query .= " AND T2.STUDYREC_SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= " AND T2.STUDYREC_CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= " AND T2.STUDYREC_SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T2.STUDYREC_SUBCLASSCD    = '".$model->subclasscd."' ";
        }
        $query .= " AND T2.STUDYREC_VIEWCD  = '".$model->studyrec_viewcd."' ";
        $query .= " AND T1.GRADE        = '".$model->field["GRADE"]."' ";
        $query .= " AND T1.SEMESTER     = '".$semester."' ";

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
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
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

    //データの取得（右画面）
    function getRow($model, $namecd1) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
        }
        $query  = " WITH NAMEDATA AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.NAMESPARE2, ";
        $query .= "     T1.NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     T1.NAMECD1     = '".$namecd1."' AND ";
        $query .= "     T1.NAMESPARE2  <> '' ";
        
        $query .= " ), ASSESSDATA AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.ASSESSLOW, ";
        $query .= "     T1.ASSESSHIGH, ";
        $query .= "     T1.ASSESSLEVEL ";
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
            $query .= "     T1.SUBCLASSCD  = '".$model->subclasscd."' AND ";
        }
        $query .= "     T1.VIEWCD      = '".$model->studyrec_viewcd."' AND ";
        $query .= "     T1.DIV         = '3' AND ";
        $query .= "     T1.GRADE       = '".$model->field["GRADE"]."'  ";
        $query .= " ) ";
        
        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     T1.NAMESPARE2, ";
        $query .= "     T2.ASSESSLOW, ";
        $query .= "     T2.ASSESSHIGH, ";
        $query .= "     T1.NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAMEDATA T1 ";
        $query .= " LEFT JOIN ASSESSDATA T2 ON int(T1.NAMESPARE2) = T2.ASSESSLEVEL ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMESPARE2 DESC";

        return $query;
    }

    //段階値取得
    function getNamespare2($model, $namecd1) {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.NAMESPARE2, ";
        $query .= "     T1.NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     T1.NAMECD1     = '".$namecd1."' AND ";
        $query .= "     T1.NAMESPARE2  <> '' ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMESPARE2 DESC";
        
        return $query;
    }

    //全体の一覧の取得（左画面）
    function selectQuery($model, $semestercount, $assesslevel) {
    
        $query  = " WITH VIEWNAME AS ( ";
        $query .= " SELECT DISTINCT";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME, ";
        $query .= "     T2.VIEWABBV, ";
        $query .= "     T2.SHOWORDER, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "     T1.GRADE, ";
        $query .= "     T2.STUDYREC_SUBCLASSCD, ";
        $query .= "     T2.STUDYREC_VIEWCD ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST T2  ON T2.GRADE       = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T2.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T2.CURRICULUM_CD     = T1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T2.SUBCLASSCD  = T1.SUBCLASSCD ";
        $query .= "                                      AND T2.VIEWCD      = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' ";

        //２件以上あるレコードを表示する
        $query .= " ), VIEWNAME_CNT_KEY2 AS ( ";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "     T2.STUDYREC_SUBCLASSCD, ";
        $query .= "     T2.STUDYREC_VIEWCD, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     VIEWNAME T2 ";
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "     T2.STUDYREC_SUBCLASSCD, ";
        $query .= "     T2.STUDYREC_VIEWCD ";
        $query .= " HAVING ";
        $query .= "     2 <= COUNT(*) ";
        //２件以上あるレコードを表示する
        $query .= " ), VIEWNAME_CNT_KEY1 AS ( ";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "     T2.STUDYREC_SUBCLASSCD, ";
        $query .= "     SUM(T2.CNT) AS CNT ";
        $query .= " FROM ";
        $query .= "     VIEWNAME_CNT_KEY2 T2 ";
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.STUDYREC_CLASSCD, ";
            $query .= "     T2.STUDYREC_SCHOOL_KIND, ";
            $query .= "     T2.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "     T2.STUDYREC_SUBCLASSCD ";

        //リスト
        $query .= " ), VIEWNAME_LIST AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.CNT AS CNT_KEY2, ";
        $query .= "     T3.CNT AS CNT_KEY1 ";
        $query .= " FROM ";
        $query .= "     VIEWNAME T1 ";
        $query .= "     INNER JOIN VIEWNAME_CNT_KEY2 T2  ON T2.STUDYREC_SUBCLASSCD      = T1.STUDYREC_SUBCLASSCD ";
        $query .= "                                     AND T2.STUDYREC_VIEWCD          = T1.STUDYREC_VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND T2.STUDYREC_CLASSCD         = T1.STUDYREC_CLASSCD ";
            $query .= "                                 AND T2.STUDYREC_SCHOOL_KIND     = T1.STUDYREC_SCHOOL_KIND ";
            $query .= "                                 AND T2.STUDYREC_CURRICULUM_CD   = T1.STUDYREC_CURRICULUM_CD ";
        }
        $query .= "     INNER JOIN VIEWNAME_CNT_KEY1 T3  ON T3.STUDYREC_SUBCLASSCD      = T1.STUDYREC_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                 AND T3.STUDYREC_CLASSCD         = T1.STUDYREC_CLASSCD ";
            $query .= "                                 AND T3.STUDYREC_SCHOOL_KIND     = T1.STUDYREC_SCHOOL_KIND ";
            $query .= "                                 AND T3.STUDYREC_CURRICULUM_CD   = T1.STUDYREC_CURRICULUM_CD ";
        }

        $query .= " ), ASSESSHIGH AS ( ";
        $query .= " SELECT DISTINCT";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
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
        $query .= "    T3.DIV = '1' AND";
        if ($assesslevel != "") {
            $query .= "    T3.ASSESSLEVEL = ".$assesslevel." ";
        } else {
            $query .= "    T3.ASSESSLEVEL = 3 ";
        }

        $query .= " ), T_SEMESTER AS ( ";
        $query .= " SELECT DISTINCT";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.VIEWFLG, ";
        $query .= "     T1.SEMESTER, ";
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
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= " ) ";
        
        //メイン処理
        $query .= " SELECT DISTINCT";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     V1.CLASSCD, ";
            $query .= "     V1.SCHOOL_KIND, ";
            $query .= "     V1.CURRICULUM_CD, ";
        }
        $query .= "     V1.SUBCLASSCD, ";
        $query .= "     S1.SUBCLASSNAME, ";
        $query .= "     V1.GRADE, ";

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     V1.STUDYREC_CLASSCD || '-' || V1.STUDYREC_SCHOOL_KIND || '-' || V1.STUDYREC_CURRICULUM_CD || '-' || V1.STUDYREC_SUBCLASSCD AS KEY1, ";
        } else {
            $query .= "     V1.STUDYREC_SUBCLASSCD AS KEY1, ";
        }
        $query .= "     V1.STUDYREC_VIEWCD AS KEY2, ";
        $query .= "     V1.CNT_KEY1, ";
        $query .= "     V1.CNT_KEY2, ";

        $query .= "     V1.VIEWCD, ";
        $query .= "     V1.VIEWNAME, ";
        $query .= "     T1.VIEWFLG AS VIEWFLG1, ";
        $query .= "     T2.VIEWFLG AS VIEWFLG2, ";
        if ($semestercount == "3") {
            $query .= "     T3.VIEWFLG AS VIEWFLG3, ";
        }
        $query .= "     V2.ASSESSHIGH, ";
        $query .= "     V1.SHOWORDER, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     V1.STUDYREC_CLASSCD, ";
            $query .= "     V1.STUDYREC_SCHOOL_KIND, ";
            $query .= "     V1.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "     V1.STUDYREC_SUBCLASSCD, ";
        $query .= "     V1.STUDYREC_VIEWCD ";
        $query .= " FROM ";
        $query .= "     VIEWNAME_LIST V1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST S1 ON S1.SUBCLASSCD      = V1.STUDYREC_SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND S1.CLASSCD         = V1.STUDYREC_CLASSCD ";
            $query .= "                          AND S1.SCHOOL_KIND     = V1.STUDYREC_SCHOOL_KIND ";
            $query .= "                          AND S1.CURRICULUM_CD   = V1.STUDYREC_CURRICULUM_CD ";
        }
        $query .= " LEFT JOIN ASSESSHIGH V2 ON V2.VIEWCD = V1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND V2.CLASSCD         = V1.CLASSCD ";
            $query .= "                                  AND V2.SCHOOL_KIND     = V1.SCHOOL_KIND ";
            $query .= "                                  AND V2.CURRICULUM_CD     = V1.CURRICULUM_CD ";
        }
        $query .= "                        AND V2.SUBCLASSCD = V1.SUBCLASSCD ";
        $query .= " LEFT JOIN T_SEMESTER T1 ON T1.VIEWCD = V1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND T1.CLASSCD         = V1.CLASSCD ";
            $query .= "                    AND T1.SCHOOL_KIND     = V1.SCHOOL_KIND ";
            $query .= "                    AND T1.CURRICULUM_CD   = V1.CURRICULUM_CD ";
        }
        $query .= "                        AND T1.SUBCLASSCD = V1.SUBCLASSCD ";
        $query .= "                        AND T1.SEMESTER = '1' ";
        $query .= " LEFT JOIN T_SEMESTER T2 ON T2.VIEWCD = V1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND T2.CLASSCD         = V1.CLASSCD ";
            $query .= "                    AND T2.SCHOOL_KIND     = V1.SCHOOL_KIND ";
            $query .= "                    AND T2.CURRICULUM_CD   = V1.CURRICULUM_CD ";
        }
        $query .= "                        AND T2.SUBCLASSCD = V1.SUBCLASSCD ";
        $query .= "                        AND T2.SEMESTER = '2' ";
        if ($semestercount == "3") {
            $query .= " LEFT JOIN T_SEMESTER T3 ON T3.VIEWCD = V1.VIEWCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                   AND T3.CLASSCD         = V1.CLASSCD ";
                $query .= "                   AND T3.SCHOOL_KIND     = V1.SCHOOL_KIND ";
                $query .= "                   AND T3.CURRICULUM_CD   = V1.CURRICULUM_CD ";
            }
            $query .= "                       AND T3.SUBCLASSCD = V1.SUBCLASSCD ";
            $query .= "                       AND T3.SEMESTER = '3' ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     V1.STUDYREC_CLASSCD, ";
            $query .= "     V1.STUDYREC_SCHOOL_KIND, ";
            $query .= "     V1.STUDYREC_CURRICULUM_CD, ";
        }
        $query .= "     V1.STUDYREC_SUBCLASSCD, ";
        $query .= "     V1.STUDYREC_VIEWCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     V1.CLASSCD, ";
            $query .= "     V1.SCHOOL_KIND, ";
            $query .= "     V1.CURRICULUM_CD, ";
        }
        $query .= "     V1.SUBCLASSCD, ";
        $query .= "     V1.SHOWORDER, ";
        $query .= "     V1.VIEWCD ";
//echo $query;
        return $query;
    }    

    //データ削除
    function delete($model, $db) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
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
            $query .= "     SUBCLASSCD = '".$model->subclasscd."' AND ";
        }
        $query .= "     VIEWCD      = '".$model->studyrec_viewcd."' AND ";
        $query .= "     DIV         = '3' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' ";

        $db->query($query);
        
        return;
    }
        
    /* データ更新処理 */
    function update($model, $db) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
        }
        for ($i = 0; $i < get_count($model->data["NAMESPARE2"]); $i++) {
            $assesslevel = $model->data["NAMESPARE2"][$i];
            $assesshigh  = $model->data["ASSESSHIGH"][$i];

            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]       = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]   = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT] = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]    = $subclass_array[3];
            } else {
                $data["SUBCLASSCD"][TEXT]    = $model->subclasscd;
            }
            $data["VIEWCD"][TEXT]        = $model->studyrec_viewcd;
            $data["DIV"][TEXT]           = '3';
            $data["GRADE"][TEXT]         = $model->field["GRADE"];
            $data["ASSESSLEVEL"][NUMBER] = $assesslevel;
            if ($model->data["NAMESPARE2"][$i] === '1') {
                $data["ASSESSLOW"][NUMBER]   = 1;
            } else {
                $data["ASSESSLOW"][NUMBER]   = $model->fields["ASSESSLOW"][$i];
            }
            if ($model->data["NAMESPARE2"][$i] === '3') {
                $data["ASSESSHIGH"][NUMBER]  = $assesshigh;
            } else if ($model->data["NAMESPARE2"][$i] === '2') {
                $data["ASSESSHIGH"][NUMBER]  = $model->Assesshightvalue2;
            } else if ($model->data["NAMESPARE2"][$i] === '1') {
                $data["ASSESSHIGH"][NUMBER]  = $model->Assesshightvalue1;
            }
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
