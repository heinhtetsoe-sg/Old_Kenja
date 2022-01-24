<?php

require_once('for_php7.php');

class knjd621bQuery extends Query {

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SEMESTERNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER < '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model, $grade="")
    {
        $sem = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE || '学年' AS LABEL, ";
        $query .= "    GRADE AS VALUE ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$sem ."' ";
        if($grade){
            $query .= "    AND GRADE = '".$grade."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    GRADE";

        return $query;
    }

    //テスト種別取得
    function getTestcd($model, $testcd="")
    {
        $query  = "SELECT ";
        $query .= "    TESTITEMNAME AS LABEL, ";
        $query .= "    TESTKINDCD || TESTITEMCD AS VALUE ";
        $query .= "FROM ";
        $query .= "    TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "WHERE ";
        $query .= "    YEAR='".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER = '".$model->field["SEMESTER"] ."' ";
        $query .= "    AND TESTKINDCD || TESTITEMCD <> '9900' ";
        if($testcd){
            $query .= "    AND TESTKINDCD || TESTITEMCD = '".$testcd."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //テスト科目一覧
    function getSubclass($model) {
        $sem = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];

        $query  = " WITH SCH_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$sem}' ";
        $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ), CHR_T AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "                            AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "                            AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$sem}' ";
        $query .= "     AND T1.SCHREGNO IN (SELECT ";
        $query .= "                             W1.SCHREGNO ";
        $query .= "                         FROM ";
        $query .= "                             SCH_T W1 ";
        $query .= "                        ) ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     T1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS LABEL, ";
            $query .= "     T1.SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '{$sem}' AND ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTCD"]}' AND ";
        $query .= "     T1.SUBCLASSCD IN (SELECT ";
        $query .= "                        W1.SUBCLASSCD ";
        $query .= "                    FROM ";
        $query .= "                        CHR_T W1 ";
        $query .= "                   ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目名称取得
    function getSubclassMst($subclasscd)
    {
        $query  = "SELECT ";
        $query .= "    SUBCLASSNAME ";
        $query .= "FROM ";
        $query .= "    SUBCLASS_MST ";
        $query .= "WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' && $subclasscd != '999999') {
            $query .= "    CLASSCD          = '".substr($subclasscd, 0, 2)."' ";
            $query .= "    SCHOOL_KIND      = '".substr($subclasscd, 3, 1)."' ";
            $query .= "    CURRICULUM_CD    = '".substr($subclasscd, 5, 1)."' ";
            $query .= "    SUBCLASSCD       = '".substr($subclasscd, 7, 6)."' ";
        } else {
            $query .= "    SUBCLASSCD = '".$subclasscd."' ";
        }
        return $query;
    }

    //CSVデータ取得
    function selectCsvQuery($model, $subclasscd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd         = substr($subclasscd, 0, 2);
            $substr_school_kind     = substr($subclasscd, 3, 1);
            $substr_curriculum_cd   = substr($subclasscd, 5, 1);
            $substr_subclasscd      = substr($subclasscd, 7, 6);
        }
        $rank = ($model->field["STANDARD"] == "2") ? "T1.GRADE_AVG_RANK" : "T1.GRADE_RANK";

        $query  = " SELECT ";
        $query .= "     {$rank}, ";
        $query .= "     L3.NAME, ";
        $query .= "     L2.HR_NAME, ";
        if ($model->field["STANDARD"] == "2" && $subclasscd == '999999') {
            $query .= "     ROUND(T1.AVG, 1) AS AVG ";
        } elseif($model->field["STANDARD"] == "2") {
            $query .= "     CAST(T1.SCORE AS DECIMAL(5,1)) AS SCORE "; //平均点を選択しているときは小数点第一まで表示する
        } else {
            $query .= "     T1.SCORE ";
        }
        if ($model->field["ASSESS_LEVEL"] == "1") {
            if ($model->field["ASSESS_LEVEL_REP"] == "1") {
                $query .= "     ,CASE WHEN A1.ASSESSLEVEL = 1 OR A1.ASSESSLEVEL = 2 THEN 3 ELSE A1.ASSESSLEVEL END AS ASSESSLEVEL ";
            } else {
                $query .= "     ,A1.ASSESSLEVEL ";
            }
        }
        $query .= " FROM ";
        $query .= "     RECORD_RANK_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                        AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT L2 ON  L2.YEAR = T1.YEAR ";
        $query .= "                         AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "                         AND L2.GRADE = L1.GRADE ";
        $query .= "                         AND L2.HR_CLASS = L1.HR_CLASS ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L3 ON L3.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     ASSESS_LEVEL_MST A1 ON  A1.YEAR = T1.YEAR ";
        $query .= "                         AND A1.SEMESTER = T1.SEMESTER ";
        $query .= "                         AND A1.TESTKINDCD || A1.TESTITEMCD = T1.TESTKINDCD || T1.TESTITEMCD ";
        $query .= "                         AND A1.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND A1.CLASSCD          = T1.CLASSCD ";
            $query .= "                         AND A1.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "                         AND A1.CURRICULUM_CD    = T1.CURRICULUM_CD ";
        }
        $query .= "                         AND A1.DIV = '1' ";
        $query .= "                         AND A1.GRADE = L1.GRADE ";
        $query .= "                         AND A1.HR_CLASS = '000' ";
        $query .= "                         AND A1.COURSECD || A1.MAJORCD || A1.COURSECODE = '00000000' ";
        if ($model->field["STANDARD"] == "2" && $subclasscd == '999999') {
            $query .= "                     AND VALUE(ROUND(T1.AVG, 1),0) BETWEEN A1.ASSESSLOW AND A1.ASSESSHIGH ";
        } elseif($model->field["STANDARD"] == "2") {
            $query .= "                     AND VALUE(CAST(T1.SCORE AS DECIMAL(5,1)),0) BETWEEN A1.ASSESSLOW AND A1.ASSESSHIGH ";
        } else {
            $query .= "                     AND VALUE(T1.SCORE,0) BETWEEN A1.ASSESSLOW AND A1.ASSESSHIGH ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     L1.GRADE = '{$model->field["GRADE"]}' AND ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTCD"]}' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' && $subclasscd != '999999') {
            $query .= "     T1.CLASSCD          = '{$substr_classcd}' AND ";
            $query .= "     T1.SCHOOL_KIND      = '{$substr_school_kind}' AND ";
            $query .= "     T1.CURRICULUM_CD    = '{$substr_curriculum_cd}' AND ";
            $query .= "     T1.SUBCLASSCD       = '{$substr_subclasscd}' AND ";
        } else {
            $query .= "     T1.SUBCLASSCD = '{$subclasscd}' AND ";
        }
        $query .= "     {$rank} <= {$model->field["RANK_RANGE"]} ";
        $query .= " ORDER BY ";
        $query .= "     {$rank}, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.ATTENDNO ";

        return $query;
    }
}
?>
