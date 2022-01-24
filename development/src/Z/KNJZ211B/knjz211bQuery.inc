<?php

require_once('for_php7.php');

class knjz211bQuery extends Query {

    //学期の取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= "     AND NAMECD2 <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期の数を取得
    function getSemestercount($namecd1) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= "     AND NAMECD2 <> '9' ";

        return $query;
    }

/*
    //段階値の上限値取得
    function getMaxAssessHight($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
        }
        $query  = "  SELECT ";
        $query .= "      SUM(T1.ASSESSHIGH) ";
        $query .= "  FROM ";
        $query .= "      JVIEWSTAT_LEVEL_MST T1 ";
        $query .= "  WHERE ";
        $query .= "     T1.YEAR        = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD  = '".$model->subclasscd."' ";
        }
        $query .= "  AND T1.DIV = '1' ";
        $query .= "  AND T1.GRADE = '".$model->grade."' ";
        $query .= "  AND T1.ASSESSLEVEL IN ( ";
        $query .= "                          SELECT ";
        $query .= "                              MAX(M1.ASSESSLEVEL) AS MAX_ASSESSLEVEL ";
        $query .= "                          FROM ";
        $query .= "                              JVIEWSTAT_LEVEL_MST M1 ";
        $query .= "                          WHERE ";
        $query .= "                              T1.YEAR = M1.YEAR  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND T1.CLASSCD =M1.CLASSCD  ";
            $query .= "                          AND T1.SCHOOL_KIND = M1.SCHOOL_KIND  ";
            $query .= "                          AND T1.CURRICULUM_CD = M1.CURRICULUM_CD ";
            $query .= "                          AND T1.SUBCLASSCD = M1.SUBCLASSCD  ";
        } else {
            $query .= "                          AND T1.SUBCLASSCD = M1.SUBCLASSCD  ";
        }
        $query .= "                          AND T1.VIEWCD = M1.VIEWCD  ";
        $query .= "                          AND T1.DIV = M1.DIV  ";
        $query .= "                          AND T1.GRADE = M1.GRADE ";
        $query .= "                          )  ";

        return $query;
    }

    //観点コード一覧を取得
    function getViewlist($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
        }
        $query  = "  SELECT ";
        $query .= "      T1.VIEWCD ";
        $query .= "  FROM ";
        $query .= "      JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR  = '".CTRL_YEAR."' ";
        $query .= "  AND T1.GRADE = '".$model->grade."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD  = '".$model->subclasscd."' ";
        }
        $query .= "  ORDER BY ";
        $query .= "      T1.VIEWCD ";

        return $query;
    }

    //JVIEWSTAT_LEVEL_MST登録データおよび段階値のMAXの上限値チェック
    function getCheckViewcd($model, $viewcd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
        }
        $query  = "  SELECT ";
        $query .= "      COUNT(*) AS CNT ";
        $query .= "  FROM ";
        $query .= "      JVIEWSTAT_LEVEL_MST T1 ";
        $query .= "  WHERE ";
        $query .= "      T1.YEAR  = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND T1.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= " AND T1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= " AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= " AND T1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND T1.SUBCLASSCD  = '".$model->subclasscd."' ";
        }
        $query .= "  AND T1.VIEWCD = '".$viewcd."' ";
        $query .= "  AND T1.DIV = '1' ";
        $query .= "  AND T1.GRADE = '".$model->grade."' ";
        $query .= "  AND T1.ASSESSHIGH IS NOT NULL ";
        $query .= "  AND T1.ASSESSLEVEL IN (  SELECT ";
        $query .= "                              MAX(M1.ASSESSLEVEL) AS MAX_ASSESSLEVEL ";
        $query .= "                          FROM ";
        $query .= "                              JVIEWSTAT_LEVEL_MST M1 ";
        $query .= "                          WHERE ";
        $query .= "                              T1.YEAR = M1.YEAR  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND T1.CLASSCD =M1.CLASSCD ";
            $query .= "                          AND T1.SCHOOL_KIND = M1.SCHOOL_KIND  ";
            $query .= "                          AND T1.CURRICULUM_CD = M1.CURRICULUM_CD  ";
            $query .= "                          AND T1.SUBCLASSCD = M1.SUBCLASSCD  ";
        } else {
            $query .= "                          AND T1.SUBCLASSCD = M1.SUBCLASSCD  ";
        }
        $query .= "                          AND T1.VIEWCD = M1.VIEWCD  ";
        $query .= "                          AND T1.DIV = M1.DIV  ";
        $query .= "                          AND T1.GRADE = M1.GRADE ";
        $query .= "                          ) ";
        $query .= "   ";

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
            $query .= "                                 AND T2.CLASSCD       = T1.CLASSCD ";
            $query .= "                                 AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                                 AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
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
            $query .= " AND T1.SUBCLASSCD    = '".$model->subclasscd."' ";
        }
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
        $query .= "     AND G1.SCHOOL_KIND  <> 'H' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
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

    //データの取得（右画面）
    function getRow($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
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
        $query .= "     T1.VIEWCD      = '9999' AND ";
        $query .= "     T1.DIV         = '2' AND ";
        $query .= "     T1.GRADE       = '".$model->grade."'  ";

        $query .= " ), ASSESSDATA_LIST AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     ROW_NUMBER() OVER(ORDER BY T2.ASSESSLEVEL) AS NAMESPARE2, ";
        $query .= "     T2.ASSESSLEVEL, ";
        $query .= "     T2.ASSESSLOW, ";
        $query .= "     T2.ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "     ASSESSDATA T2 ";
        $query .= " ORDER BY ";
        $query .= "     T2.ASSESSLEVEL DESC";
        $query .= " ) ";
        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     T1.NAMESPARE2, ";
        $query .= "     T2.ASSESSLEVEL, ";
        $query .= "     T2.ASSESSLOW, ";
        $query .= "     T2.ASSESSHIGH ";
        //$query .= "     T1.NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAMEDATA T1 ";
        $query .= " LEFT JOIN ASSESSDATA_LIST T2 ON int(T1.NAMESPARE2) = T2.NAMESPARE2 ";
        //$query .= " LEFT JOIN ASSESSDATA T2 ON int(T1.NAMESPARE2) = T2.ASSESSLEVEL ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMESPARE2 DESC";

        return $query;
    }

    //科目が同一のものをカウント
    function getSubclassCnt($model, $row) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
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
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD       = '".$row["CLASSCD"]."' AND ";
            $query .= "    T1.SCHOOL_KIND   = '".$row["SCHOOL_KIND"]."' AND ";
            $query .= "    T1.CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' AND ";
        } 
        $query .= "    T1.SUBCLASSCD = '".$row["SUBCLASSCD"]."' ";

        return $query;
    }    

    //評定設定チェック
    function getjviewStatLevelMst($model, $row) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $row["SUBCLASSCD"]);
        }
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_MST T2  ON T2.GRADE       = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T2.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T2.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                      AND T2.SUBCLASSCD  = T1.SUBCLASSCD ";
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T1.CLASSCD       = '".$row["CLASSCD"]."' AND ";
            $query .= "    T1.SCHOOL_KIND   = '".$row["SCHOOL_KIND"]."' AND ";
            $query .= "    T1.CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' AND ";
            $query .= "    T1.SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";
        } else {
            $query .= "    T1.SUBCLASSCD = '".$row["SUBCLASSCD"]."' ";
        }
        $query .= " AND T2.VIEWCD = '9999' ";
        $query .= " AND T2.DIV = '2' ";

        return $query;
    }    

    //対象データの段階値数チェック
    function selectCountQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
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
            $query .= "     T1.SUBCLASSCD  = '".$model->subclasscd."' AND ";
        }
        $query .= "     T1.VIEWCD      = '9999' AND ";
        $query .= "     T1.DIV         = '2' AND ";
        $query .= "     T1.GRADE       = '".$model->grade."'  ";
        return $query;
    }


    //全体の一覧の取得（左画面）
    function selectQuery($model, $semestercount, $assesslevel) {
    
        $query  = " WITH VIEWNAME AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME, ";
        $query .= "     T2.VIEWABBV, ";
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
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' ";

        $query .= " ), ASSESSHIGH AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T4.SUBCLASSNAME, ";
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
        $query .= "     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T4.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T4.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T4.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND ";
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
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T4.SUBCLASSNAME, ";
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
        $query .= "     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T4.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T4.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T4.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "    T1.SEMESTER = '1' ";
        
        $query .= " ), SEMESTER2 AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T4.SUBCLASSNAME, ";
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
        $query .= "     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T4.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T4.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T4.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "    T1.SEMESTER = '2' ";
        $query .= " ) ";
        
        //3学期がある場合
        if ($semestercount == "3") {
            $query .= " , SEMESTER3 AS ( ";
            $query .= " SELECT DISTINCT";
            $query .= "     T1.GRADE, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T4.SUBCLASSNAME, ";
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
            $query .= "     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                  AND T4.CLASSCD         = T1.CLASSCD ";
                $query .= "                                  AND T4.SCHOOL_KIND     = T1.SCHOOL_KIND ";
                $query .= "                                  AND T4.CURRICULUM_CD   = T1.CURRICULUM_CD ";
            }
            $query .= " WHERE ";
            $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND ";
            $query .= "    T1.SEMESTER = '3' ";
            $query .= " ) ";
        }
        
        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     V1.CLASSCD, ";
            $query .= "     V1.SCHOOL_KIND, ";
            $query .= "     V1.CURRICULUM_CD, ";
        }
        $query .= "     V1.SUBCLASSCD, ";
        $query .= "     V1.SUBCLASSNAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     V1.CLASSCD || '-' || V1.SCHOOL_KIND || '-' || V1.CURRICULUM_CD || '-' || V1.SUBCLASSCD || V1.SUBCLASSNAME AS KEY, ";
        } else {
            $query .= "     V1.SUBCLASSCD || V1.SUBCLASSNAME AS KEY, ";
        }
        $query .= "     V1.VIEWCD, ";
        $query .= "     V1.VIEWNAME, ";
        $query .= "     T1.VIEWFLG AS VIEWFLG1, ";
        $query .= "     T2.VIEWFLG AS VIEWFLG2, ";
        if ($semestercount == "3") {
            $query .= "     T3.VIEWFLG AS VIEWFLG3, ";
        }
        $query .= "     V2.ASSESSHIGH, ";
        $query .= "     V1.SHOWORDER ";
        $query .= " FROM ";
        $query .= "     VIEWNAME V1 ";
        $query .= " LEFT JOIN ASSESSHIGH V2 ON V2.VIEWCD = V1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND V2.CLASSCD         = V1.CLASSCD ";
            $query .= "                                  AND V2.SCHOOL_KIND     = V1.SCHOOL_KIND ";
            $query .= "                                  AND V2.CURRICULUM_CD   = V1.CURRICULUM_CD ";
        }
        $query .= "                        AND V2.SUBCLASSCD = V1.SUBCLASSCD ";
        $query .= " LEFT JOIN SEMESTER1  T1 ON T1.VIEWCD = V1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND T1.CLASSCD       = V1.CLASSCD ";
            $query .= "                    AND T1.SCHOOL_KIND   = V1.SCHOOL_KIND ";
            $query .= "                    AND T1.CURRICULUM_CD     = V1.CURRICULUM_CD ";
        }
        $query .= "                        AND T1.SUBCLASSCD = V1.SUBCLASSCD ";
        $query .= " LEFT JOIN SEMESTER2  T2 ON T2.VIEWCD = V1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND T2.CLASSCD       = V1.CLASSCD ";
            $query .= "                    AND T2.SCHOOL_KIND   = V1.SCHOOL_KIND ";
            $query .= "                    AND T2.CURRICULUM_CD     = V1.CURRICULUM_CD ";
        }
        $query .= "                        AND T2.SUBCLASSCD = V1.SUBCLASSCD ";
        if ($semestercount == "3") {
            $query .= " LEFT JOIN SEMESTER3 T3 ON T3.VIEWCD = V1.VIEWCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                   AND T3.CLASSCD        = V1.CLASSCD ";
                $query .= "                   AND T3.SCHOOL_KIND    = V1.SCHOOL_KIND ";
                $query .= "                   AND T3.CURRICULUM_CD  = V1.CURRICULUM_CD ";
            }
            $query .= "                       AND T3.SUBCLASSCD = V1.SUBCLASSCD ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     V1.CLASSCD, ";
            $query .= "     V1.SCHOOL_KIND, ";
            $query .= "     V1.CURRICULUM_CD, ";
        }
        $query .= "     V1.SUBCLASSCD, ";
        $query .= "     V1.SHOWORDER, ";
        $query .= "     V1.VIEWCD ";

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
        $query .= "     DIV        = '2' AND ";
        $query .= "     GRADE      = '".$model->grade."' ";

        $db->query($query);

        return;
    }
        
    // データ更新処理
    function update($model, $db) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
        }
        for ($i = 0; $i < get_count($model->data["NAMESPARE2"]); $i++) {
            $assesshigh  = $model->data["ASSESSHIGH"][$i];
            if ($i == 0) {
                $set_assesshigh  = $model->max_assesshight;
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
                $data["SUBCLASSCD"][TEXT]    = $model->subclasscd;
            }
            $data["VIEWCD"][TEXT]        = '9999';
            $data["DIV"][TEXT]           = '2';
            $data["GRADE"][TEXT]         = $model->grade;
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
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "JVIEWSTAT_LEVEL_MST");

            $db->query($query);
        }
        return;
    }
*/



//↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
    //段階値の上限値取得
    function getMaxAssessHight($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
        }
        $query  = " SELECT ";
        $query .= "     SUM(T1.ASSESSHIGH) ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_SEMES_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER  = '9' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "     AND T1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "     AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "     AND T1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD    = '".$model->subclasscd."' ";
        }
        $query .= "     AND T1.DIV   = '1' ";
        $query .= "     AND T1.GRADE = '".$model->grade."' ";
        $query .= "     AND T1.ASSESSLEVEL IN ( ";
        $query .= "                            SELECT ";
        $query .= "                                MAX(M1.ASSESSLEVEL) AS MAX_ASSESSLEVEL ";
        $query .= "                            FROM ";
        $query .= "                                JVIEWSTAT_LEVEL_SEMES_MST M1 ";
        $query .= "                            WHERE ";
        $query .= "                                    T1.YEAR      = M1.YEAR  ";
        $query .= "                                AND T1.SEMESTER  = M1.SEMESTER ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                AND T1.CLASSCD       = M1.CLASSCD  ";
            $query .= "                                AND T1.SCHOOL_KIND   = M1.SCHOOL_KIND ";
            $query .= "                                AND T1.CURRICULUM_CD = M1.CURRICULUM_CD ";
            $query .= "                                AND T1.SUBCLASSCD    = M1.SUBCLASSCD ";
        } else {
            $query .= "                                AND T1.SUBCLASSCD    = M1.SUBCLASSCD ";
        }
        $query .= "                                AND T1.VIEWCD = M1.VIEWCD ";
        $query .= "                                AND T1.DIV    = M1.DIV ";
        $query .= "                                AND T1.GRADE  = M1.GRADE ";
        $query .= "                          )  ";

        return $query;
    }

    //観点コード一覧を取得
    function getViewlist($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
        }
        $query  = " SELECT ";
        $query .= "     T1.VIEWCD ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '".$model->grade."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "     AND T1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "     AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "     AND T1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD  = '".$model->subclasscd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.VIEWCD ";

        return $query;
    }

    //JVIEWSTAT_LEVEL_SEMES_MST登録データおよび段階値のMAXの上限値チェック
    function getCheckViewcd($model, $viewcd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_SEMES_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER  = '9' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "     AND T1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "     AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "     AND T1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD  = '".$model->subclasscd."' ";
        }
        $query .= "     AND T1.VIEWCD = '".$viewcd."' ";
        $query .= "     AND T1.DIV    = '1' ";
        $query .= "     AND T1.GRADE  = '".$model->grade."' ";
        $query .= "     AND T1.ASSESSHIGH IS NOT NULL ";
        $query .= "     AND T1.ASSESSLEVEL IN (  SELECT ";
        $query .= "                                  MAX(M1.ASSESSLEVEL) AS MAX_ASSESSLEVEL ";
        $query .= "                              FROM ";
        $query .= "                                  JVIEWSTAT_LEVEL_SEMES_MST M1 ";
        $query .= "                              WHERE ";
        $query .= "                                      T1.YEAR      = M1.YEAR ";
        $query .= "                                  AND T1.SEMESTER  = M1.SEMESTER ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                              AND T1.CLASSCD        = M1.CLASSCD ";
            $query .= "                              AND T1.SCHOOL_KIND    = M1.SCHOOL_KIND ";
            $query .= "                              AND T1.CURRICULUM_CD  = M1.CURRICULUM_CD ";
            $query .= "                              AND T1.SUBCLASSCD     = M1.SUBCLASSCD ";
        } else {
            $query .= "                              AND T1.SUBCLASSCD     = M1.SUBCLASSCD ";
        }
        $query .= "                                  AND T1.VIEWCD    = M1.VIEWCD  ";
        $query .= "                                  AND T1.DIV       = M1.DIV  ";
        $query .= "                                  AND T1.GRADE     = M1.GRADE ";
        $query .= "                          ) ";
        $query .= "   ";

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
            $query .= "                             AND T2.CLASSCD        = T1.CLASSCD ";
            $query .= "                             AND T2.SCHOOL_KIND    = T1.SCHOOL_KIND ";
            $query .= "                             AND T2.CURRICULUM_CD  = T1.CURRICULUM_CD ";
        }
        $query .= "                                 AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                 AND T2.VIEWCD     = T1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "     AND T1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "     AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "     AND T1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD    = '".$model->subclasscd."' ";
        }
        $query .= "     AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "     AND T1.SEMESTER = '".$semester."' ";

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
        $query .= "     AND G1.SCHOOL_KIND  <> 'H' ";
        if(AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
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

    //データの取得（右画面）
    function getRow($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
        }
        $query  = " WITH NAMEDATA AS ( ";
        for ($i = 1; $i <= $model->field["MAX_ASSESSLEVEL"]; $i++) {
            if ($i > 1) {
                $query .= "  UNION ";
            }
            $query .= " SELECT ";
            $query .= "     '".$i."' AS NAMESPARE2 ";
            $query .= " FROM ";
            $query .= "     SYSIBM.SYSDUMMY1 ";
        }
        $query .= " ), ASSESSDATA AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.ASSESSLOW, ";
        $query .= "     T1.ASSESSHIGH, ";
        $query .= "     T1.ASSESSLEVEL ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_SEMES_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '9' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "     AND T1.SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "     AND T1.CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "     AND T1.SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD    = '".$model->subclasscd."' ";
        }
        $query .= "     AND T1.VIEWCD   = '9999' ";
        $query .= "     AND T1.DIV      = '2' ";
        $query .= "     AND T1.GRADE    = '".$model->grade."' ";

        $query .= " ), ASSESSDATA_LIST AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     ROW_NUMBER() OVER(ORDER BY T2.ASSESSLEVEL) AS NAMESPARE2, ";
        $query .= "     T2.ASSESSLEVEL, ";
        $query .= "     T2.ASSESSLOW, ";
        $query .= "     T2.ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "     ASSESSDATA T2 ";
        $query .= " ORDER BY ";
        $query .= "     T2.ASSESSLEVEL DESC";
        $query .= " ) ";
        //メイン処理
        $query .= " SELECT DISTINCT";
        $query .= "     T1.NAMESPARE2, ";
        $query .= "     T2.ASSESSLEVEL, ";
        $query .= "     T2.ASSESSLOW, ";
        $query .= "     T2.ASSESSHIGH ";
        //$query .= "     T1.NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAMEDATA T1 ";
        $query .= "     LEFT JOIN ASSESSDATA_LIST T2 ON int(T1.NAMESPARE2) = T2.NAMESPARE2 ";
        //$query .= "   LEFT JOIN ASSESSDATA T2 ON int(T1.NAMESPARE2) = T2.ASSESSLEVEL ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMESPARE2 DESC";

        return $query;
    }

    //科目が同一のものをカウント
    function getSubclassCnt($model, $row) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
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
        $query .= "     LEFT JOIN SUBCLASS_MST T3         ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "        T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.GRADE = '".$model->field["GRADE"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND T1.CLASSCD       = '".$row["CLASSCD"]."' ";
            $query .= "    AND T1.SCHOOL_KIND   = '".$row["SCHOOL_KIND"]."' ";
            $query .= "    AND T1.CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' ";
        } 
        $query .= "    AND T1.SUBCLASSCD = '".$row["SUBCLASSCD"]."' ";

        return $query;
    }    

    //評定設定チェック
    function getjviewStatLevelMst($model, $row) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $row["SUBCLASSCD"]);
        }
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_SEMES_MST T2 ON T2.GRADE      = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                       AND T2.CLASSCD         = T1.CLASSCD ";
            $query .= "                                       AND T2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                       AND T2.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                           AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     LEFT JOIN SUBCLASS_MST T3              ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                       AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                                       AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                       AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD       = '".$row["CLASSCD"]."' ";
            $query .= "     AND T1.SCHOOL_KIND   = '".$row["SCHOOL_KIND"]."' ";
            $query .= "     AND T1.CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' ";
            $query .= "     AND T1.SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD    = '".$row["SUBCLASSCD"]."' ";
        }
        $query .= "     AND T2.VIEWCD = '9999' ";
        $query .= "     AND T2.DIV    = '2' ";

        return $query;
    }    

    //対象データの段階値数チェック
    function selectCountQuery($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subclasscd);
        }
        $query  = " SELECT  ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER   = '9' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "     AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "     AND CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "     AND SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND SUBCLASSCD  = '".$model->subclasscd."' ";
        }
        $query .= "     AND VIEWCD      = '9999' ";
        $query .= "     AND DIV         = '2' ";
        $query .= "     AND GRADE       = '".$model->grade."' ";

        return $query;
    }


    //全体の一覧の取得（左画面）
    function selectQuery($model, $semestercount, $assesslevel) {
    
        $query  = " WITH VIEWNAME AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME, ";
        $query .= "     T2.VIEWABBV, ";
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
        $query .= "     LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "        T1.YEAR  = '".CTRL_YEAR."' ";
        $query .= "    AND T1.GRADE = '".$model->field["GRADE"]."' ";

        $query .= " ), ASSESSHIGH AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T4.SUBCLASSNAME, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T3.ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST T2        ON T2.GRADE        = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                        AND T2.CLASSCD         = T1.CLASSCD ";
            $query .= "                                        AND T2.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                        AND T2.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND T2.SUBCLASSCD  = T1.SUBCLASSCD ";
        $query .= "                                            AND T2.VIEWCD      = T1.VIEWCD ";
        $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_SEMES_MST T3  ON T3.YEAR        = T1.YEAR ";
        $query .= "                                            AND T3.GRADE       = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                        AND T3.CLASSCD         = T1.CLASSCD ";
            $query .= "                                        AND T3.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                        AND T3.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= "                                            AND T3.SUBCLASSCD  = T1.SUBCLASSCD ";
        $query .= "                                            AND T3.VIEWCD      = T1.VIEWCD ";
        $query .= "     LEFT JOIN SUBCLASS_MST T4               ON T4.SUBCLASSCD  = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                        AND T4.CLASSCD         = T1.CLASSCD ";
            $query .= "                                        AND T4.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                        AND T4.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "     AND T3.DIV   = '1' ";
        $query .= "     AND T3.ASSESSLEVEL IN ";
        $query .= "                         (SELECT ";
        $query .= "                              MAX(ASSESSLEVEL) AS MAX_ASSESSLEVEL ";
        $query .= "                          FROM ";
        $query .= "                              JVIEWSTAT_LEVEL_SEMES_MST M1 ";
        $query .= "                          WHERE ";
        $query .= "                                  M1.YEAR       = T3.YEAR ";
        $query .= "                              AND M1.SEMESTER   = T3.SEMESTER ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                          AND M1.CLASSCD         = T3.CLASSCD ";
            $query .= "                          AND M1.SCHOOL_KIND     = T3.SCHOOL_KIND ";
            $query .= "                          AND M1.CURRICULUM_CD   = T3.CURRICULUM_CD ";
        }
        $query .= "                              AND M1.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "                              AND M1.VIEWCD     = T3.VIEWCD ";
        $query .= "                              AND M1.DIV        = T3.DIV ";
        $query .= "                              AND M1.GRADE      = T3.GRADE ";
        $query .= "                          ) ";

        $query .= " ), SEMESTER1 AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T4.SUBCLASSNAME, ";
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
        $query .= "     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T4.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T4.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T4.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "        T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "    AND T1.GRADE    = '".$model->field["GRADE"]."' ";
        $query .= "    AND T1.SEMESTER = '1' ";
        
        $query .= " ), SEMESTER2 AS ( ";
        $query .= " SELECT DISTINCT";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T4.SUBCLASSNAME, ";
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
        $query .= "     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T4.CLASSCD         = T1.CLASSCD ";
            $query .= "                                  AND T4.SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "                                  AND T4.CURRICULUM_CD   = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "    T1.SEMESTER = '2' ";
        $query .= " ) ";
        
        //3学期がある場合
        if ($semestercount == "3") {
            $query .= " , SEMESTER3 AS ( ";
            $query .= " SELECT DISTINCT";
            $query .= "     T1.GRADE, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T4.SUBCLASSNAME, ";
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
            $query .= "     LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                  AND T4.CLASSCD         = T1.CLASSCD ";
                $query .= "                                  AND T4.SCHOOL_KIND     = T1.SCHOOL_KIND ";
                $query .= "                                  AND T4.CURRICULUM_CD   = T1.CURRICULUM_CD ";
            }
            $query .= " WHERE ";
            $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "    T1.GRADE = '".$model->field["GRADE"]."' AND ";
            $query .= "    T1.SEMESTER = '3' ";
            $query .= " ) ";
        }
        
        //メイン処理
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     V1.CLASSCD, ";
            $query .= "     V1.SCHOOL_KIND, ";
            $query .= "     V1.CURRICULUM_CD, ";
        }
        $query .= "     V1.SUBCLASSCD, ";
        $query .= "     V1.SUBCLASSNAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     V1.CLASSCD || '-' || V1.SCHOOL_KIND || '-' || V1.CURRICULUM_CD || '-' || V1.SUBCLASSCD || V1.SUBCLASSNAME AS KEY, ";
        } else {
            $query .= "     V1.SUBCLASSCD || V1.SUBCLASSNAME AS KEY, ";
        }
        $query .= "     V1.VIEWCD, ";
        $query .= "     V1.VIEWNAME, ";
        $query .= "     T1.VIEWFLG AS VIEWFLG1, ";
        $query .= "     T2.VIEWFLG AS VIEWFLG2, ";
        if ($semestercount == "3") {
            $query .= "     T3.VIEWFLG AS VIEWFLG3, ";
        }
        $query .= "     V2.ASSESSHIGH, ";
        $query .= "     V1.SHOWORDER ";
        $query .= " FROM ";
        $query .= "     VIEWNAME V1 ";
        $query .= " LEFT JOIN ASSESSHIGH V2 ON V2.VIEWCD = V1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND V2.CLASSCD           = V1.CLASSCD ";
            $query .= "                    AND V2.SCHOOL_KIND       = V1.SCHOOL_KIND ";
            $query .= "                    AND V2.CURRICULUM_CD     = V1.CURRICULUM_CD ";
        }
        $query .= "                        AND V2.SUBCLASSCD = V1.SUBCLASSCD ";
        $query .= " LEFT JOIN SEMESTER1  T1 ON T1.VIEWCD     = V1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND T1.CLASSCD           = V1.CLASSCD ";
            $query .= "                    AND T1.SCHOOL_KIND       = V1.SCHOOL_KIND ";
            $query .= "                    AND T1.CURRICULUM_CD     = V1.CURRICULUM_CD ";
        }
        $query .= "                        AND T1.SUBCLASSCD = V1.SUBCLASSCD ";
        $query .= " LEFT JOIN SEMESTER2  T2 ON T2.VIEWCD     = V1.VIEWCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND T2.CLASSCD           = V1.CLASSCD ";
            $query .= "                    AND T2.SCHOOL_KIND       = V1.SCHOOL_KIND ";
            $query .= "                    AND T2.CURRICULUM_CD     = V1.CURRICULUM_CD ";
        }
        $query .= "                        AND T2.SUBCLASSCD = V1.SUBCLASSCD ";
        if ($semestercount == "3") {
            $query .= " LEFT JOIN SEMESTER3 T3 ON T3.VIEWCD     = V1.VIEWCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                   AND T3.CLASSCD        = V1.CLASSCD ";
                $query .= "                   AND T3.SCHOOL_KIND    = V1.SCHOOL_KIND ";
                $query .= "                   AND T3.CURRICULUM_CD  = V1.CURRICULUM_CD ";
            }
            $query .= "                       AND T3.SUBCLASSCD = V1.SUBCLASSCD ";
        }
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     V1.CLASSCD, ";
            $query .= "     V1.SCHOOL_KIND, ";
            $query .= "     V1.CURRICULUM_CD, ";
        }
        $query .= "     V1.SUBCLASSCD, ";
        $query .= "     V1.SHOWORDER, ";
        $query .= "     V1.VIEWCD ";

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
        $query .= "     JVIEWSTAT_LEVEL_SEMES_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER   = '9' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "     AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "     AND CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "     AND SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "     AND SUBCLASSCD    = '".$model->subclasscd."' ";
        }
        $query .= "     AND DIV        = '2' ";
        $query .= "     AND GRADE      = '".$model->grade."' ";

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
            $assesshigh  = $model->data["ASSESSHIGH"][$i];
            if ($i == 0) {
                $set_assesshigh  = $model->max_assesshight;
            }
            $data = array();
            $data["YEAR"][TEXT]          = CTRL_YEAR;
            $data["SEMESTER"][TEXT]      = '9';
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]          = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
            } else {
                $data["SUBCLASSCD"][TEXT]       = $model->subclasscd;
            }
            $data["VIEWCD"][TEXT]        = '9999';
            $data["DIV"][TEXT]           = '2';
            $data["GRADE"][TEXT]         = $model->grade;
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
            $data["REGISTERCD"][TEXT]    = STAFFCD ;
            $data["UPDATED"][FUNC]       = "sysdate()";
            //追加
            $query = Query::insertSQL($data, "JVIEWSTAT_LEVEL_SEMES_MST");

            $db->query($query);
        }
        return;
    }
//↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑



}
?>
