<?php

require_once('for_php7.php');

class knjd320aQuery extends Query {

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
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //学年取得
    function getSelectGrade($model, $grade="")
    {
        $sem = ($model->field["SEMESTER"] == 9) ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $query  = "SELECT DISTINCT ";
        $query .= "    CAST(SMALLINT(GRADE) AS CHAR(1)) || '学年' AS LABEL, ";
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
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
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
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "     AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '{$sem}' AND ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTCD"]}' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "     T1.SUBCLASSCD IN (SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ";
        }
        $query .= "                        W1.SUBCLASSCD ";
        $query .= "                    FROM ";
        $query .= "                        CHR_T W1 ";
        $query .= "                   ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目名称取得
    function getSubclassMst($model, $subclasscd)
    {
        $query  = "SELECT ";
        $query .= "    SUBCLASSNAME ";
        $query .= "FROM ";
        $query .= "    SUBCLASS_MST ";
        $query .= "WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' && $subclasscd != '999999') {
            $query .= "    CLASSCD          = '".substr($subclasscd, 0, 2)."' ";
            $query .= "    AND SCHOOL_KIND      = '".substr($subclasscd, 3, 1)."' ";
            $query .= "    AND CURRICULUM_CD    = '".substr($subclasscd, 5, 1)."' ";
            $query .= "    AND SUBCLASSCD       = '".substr($subclasscd, 7, 6)."' ";
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
        $setrank = ($model->field["OUTPUT_RANK"] == "1") ? "T1.GRADE_RANK" : "T1.COURSE_RANK";
        $hikakurank = ($model->field["OUTPUT_RANK"] == "1") ? "M1.GRADE_RANK" : "M1.COURSE_RANK";
        
        $query  = " SELECT DISTINCT";
        //コース別の場合、コース毎にCSVを出力する
        if ($model->field["OUTPUT_RANK"] == "2") {
            $query .= "     L1.COURSECODE, ";
            $query .= "     C3.COURSECODENAME, ";
        }
        $query .= "     T1.SCHREGNO, ";
        $query .= "     {$setrank} AS RANK, ";
        $query .= "     L3.NAME, ";
        $query .= "     L2.HR_NAME, ";
        $query .= "     L1.HR_CLASS, ";//ORDER用
        $query .= "     L1.ATTENDNO, ";
        $query .= "     T1.SCORE, ";
        $query .= "     L6.STAFFNAME, ";
        $query .= "     S1.CHAIRCD, ";
        $query .= "     L5.STAFFCD ";
        $query .= " FROM ";
        $query .= "     RECORD_RANK_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                        AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND L1.SCHREGNO = T1.SCHREGNO ";
        //コース別の場合
        if ($model->field["OUTPUT_RANK"] == "2") {
            $query .= " LEFT JOIN COURSE_MST C1 ON C1.COURSECD   = L1.COURSECD ";
            $query .= " LEFT JOIN MAJOR_MST  C2 ON C2.COURSECD   = L1.COURSECD ";
            $query .= "                        AND C2.MAJORCD    = L1.MAJORCD ";
            $query .= " LEFT JOIN V_COURSECODE_MST C3 ON C3.YEAR = L1.YEAR ";
            $query .= "                              AND VALUE(C3.COURSECODE,'0000') = VALUE(L1.COURSECODE,'0000') ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT L2 ON  L2.YEAR = T1.YEAR ";
        $query .= "                         AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "                         AND L2.GRADE = L1.GRADE ";
        $query .= "                         AND L2.HR_CLASS = L1.HR_CLASS ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L3 ON L3.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN CHAIR_STD_DAT S1 ON S1.YEAR = T1.YEAR ";
        $query .= "                           AND S1.SEMESTER = T1.SEMESTER ";
        $query .= "                           AND S1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN CHAIR_DAT L4 ON L4.YEAR = T1.YEAR ";
        $query .= "                        AND L4.SEMESTER = T1.SEMESTER ";
        if ($model->Properties["useCurriculumcd"] == '1' && $subclasscd != '999999') {
            $query .= "                        AND L4.CLASSCD = T1.CLASSCD ";
            $query .= "                        AND L4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                        AND L4.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                        AND L4.SUBCLASSCD = T1.SUBCLASSCD ";
        }
        $query .= " LEFT JOIN CHAIR_STF_DAT L5 ON L5.YEAR = T1.YEAR ";
        $query .= "                            AND L5.SEMESTER = T1.SEMESTER ";
        $query .= "                            AND L5.CHARGEDIV = 1 ";
        $query .= " LEFT JOIN STAFF_MST L6 ON L6.STAFFCD = L5.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     L1.GRADE = '{$model->field["GRADE"]}' AND ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTCD"]}' AND ";
        $query .= "     S1.CHAIRCD = L4.CHAIRCD AND ";
        $query .= "     S1.CHAIRCD = L5.CHAIRCD AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' && $subclasscd != '999999') {
            $query .= "     T1.CLASSCD          = '{$substr_classcd}' AND ";
            $query .= "     T1.SCHOOL_KIND      = '{$substr_school_kind}' AND ";
            $query .= "     T1.CURRICULUM_CD    = '{$substr_curriculum_cd}' AND ";
            $query .= "     T1.SUBCLASSCD       = '{$substr_subclasscd}' AND ";
        } else {
            $query .= "     T1.SUBCLASSCD = '{$subclasscd}' AND ";
        }
        //得点の場合
        if ($model->field["OUTPUT_JYOUKEN"] === '1') {
            $query .= "     T1.SCORE <= ".$model->field["SCORE_RANGE"]." ";
        //順位の場合(学年別のときのみ)
        } else {
            $query .= "     {$setrank} >  ( SELECT ";
            $query .= "                             MAX({$hikakurank}) - ".$model->field["RANK_RANGE"]." AS RANK ";
            $query .= "                        FROM  ";
            $query .= "                             RECORD_RANK_DAT M1  ";
            $query .= "                        LEFT JOIN ";
            $query .= "                             SCHREG_REGD_DAT M2 ON  M2.YEAR = M1.YEAR ";
            $query .= "                                                AND M2.SEMESTER = M1.SEMESTER ";
            $query .= "                                                AND M2.SCHREGNO = M1.SCHREGNO ";
            $query .= "                        WHERE ";
            $query .= "                             M1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "                             M1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
            $query .= "                             M2.GRADE = '{$model->field["GRADE"]}' AND ";
            $query .= "                             M1.TESTKINDCD || M1.TESTITEMCD = '{$model->field["TESTCD"]}' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1' && $subclasscd != '999999') {
                $query .= "                         M1.CLASSCD          = '{$substr_classcd}' AND ";
                $query .= "                         M1.SCHOOL_KIND      = '{$substr_school_kind}' AND ";
                $query .= "                         M1.CURRICULUM_CD    = '{$substr_curriculum_cd}' AND ";
                $query .= "                         M1.SUBCLASSCD       = '{$substr_subclasscd}' ";
            } else {
                $query .= "                         M1.SUBCLASSCD = '{$subclasscd}' ";
            }        
            $query .= "                       ) ";
        }
        $query .= " ORDER BY ";
        if ($model->field["OUTPUT_RANK"] == "2") {
            $query .= "     L1.COURSECODE, ";
        }
        $query .= "     {$setrank}, ";
        $query .= "     L1.HR_CLASS, ";
        $query .= "     L1.ATTENDNO, ";
        $query .= "     S1.CHAIRCD DESC, ";
        $query .= "     L5.STAFFCD DESC";
        
        return $query;
    }
    
    //CSVデータ取得(コース別で順位指定の場合)
    function selectCsv2Query($model, $subclasscd, $getcource, $cource_count) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd         = substr($subclasscd, 0, 2);
            $substr_school_kind     = substr($subclasscd, 3, 1);
            $substr_curriculum_cd   = substr($subclasscd, 5, 1);
            $substr_subclasscd      = substr($subclasscd, 7, 6);
        }
        $setrank = ($model->field["OUTPUT_RANK"] == "1") ? "T1.GRADE_RANK" : "T1.COURSE_RANK";
        $hikakurank = ($model->field["OUTPUT_RANK"] == "1") ? "M1.GRADE_RANK" : "M1.COURSE_RANK";
        //コース別にデータ取得
        for ($i = 0; $i < $cource_count; $i++) {
            if ($i == 0) {
                $query  = " SELECT DISTINCT";
            } else {
                $query .= " UNION ";
                $query .= " SELECT DISTINCT ";
            }
            //コース別の場合、コース毎にCSVを出力する
            $query .= "     L1.COURSECODE, ";
            $query .= "     C3.COURSECODENAME, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     {$setrank} AS RANK, ";
            $query .= "     L3.NAME, ";
            $query .= "     L2.HR_NAME, ";
            $query .= "     L1.HR_CLASS, ";
            $query .= "     L1.ATTENDNO, ";
            $query .= "     T1.SCORE, ";
            $query .= "     L6.STAFFNAME, ";
            $query .= "     S1.CHAIRCD, ";
            $query .= "     L5.STAFFCD ";
            $query .= " FROM ";
            $query .= "     RECORD_RANK_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_DAT L1 ON  L1.YEAR = T1.YEAR ";
            $query .= "                        AND L1.SEMESTER   = T1.SEMESTER ";
            $query .= "                        AND L1.SCHREGNO   = T1.SCHREGNO ";
            //コース別の場合
            $query .= " LEFT JOIN COURSE_MST C1 ON C1.COURSECD = L1.COURSECD ";
            $query .= " LEFT JOIN MAJOR_MST C2 ON C2.COURSECD = L1.COURSECD ";
            $query .= "                       AND C2.MAJORCD = L1.MAJORCD ";
            $query .= " LEFT JOIN V_COURSECODE_MST C3 ON C3.YEAR = L1.YEAR ";
            $query .= "                              AND VALUE(C3.COURSECODE,'0000') = VALUE(L1.COURSECODE,'0000') ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_HDAT L2 ON  L2.YEAR = T1.YEAR ";
            $query .= "                         AND L2.SEMESTER = T1.SEMESTER ";
            $query .= "                         AND L2.GRADE = L1.GRADE ";
            $query .= "                         AND L2.HR_CLASS = L1.HR_CLASS ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_BASE_MST L3 ON L3.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN CHAIR_STD_DAT S1 ON S1.YEAR = T1.YEAR ";
            $query .= "                           AND S1.SEMESTER = T1.SEMESTER ";
            $query .= "                           AND S1.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN CHAIR_DAT L4 ON L4.YEAR = T1.YEAR ";
            $query .= "                        AND L4.SEMESTER = T1.SEMESTER ";
            if ($model->Properties["useCurriculumcd"] == '1' && $subclasscd != '999999') {
                $query .= "                        AND L4.CLASSCD = T1.CLASSCD ";
                $query .= "                        AND L4.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "                        AND L4.CURRICULUM_CD = T1.CURRICULUM_CD ";
                $query .= "                        AND L4.SUBCLASSCD = T1.SUBCLASSCD ";
            }
            $query .= " LEFT JOIN CHAIR_STF_DAT L5 ON L5.YEAR = T1.YEAR ";
            $query .= "                            AND L5.SEMESTER = T1.SEMESTER ";
            $query .= "                            AND L5.CHARGEDIV = 1 ";
            $query .= " LEFT JOIN STAFF_MST L6 ON L6.STAFFCD = L5.STAFFCD ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
            $query .= "     L1.GRADE = '{$model->field["GRADE"]}' AND ";
            $query .= "     L1.COURSECD   = '{$getcource[$i]["COURSECD"]}' AND ";
            $query .= "     L1.MAJORCD    = '{$getcource[$i]["MAJORCD"]}' AND ";
            $query .= "     L1.COURSECODE = '{$getcource[$i]["COURSECODE"]}' AND ";
            $query .= "     T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTCD"]}' AND ";
            $query .= "     S1.CHAIRCD = L4.CHAIRCD AND ";
            $query .= "     S1.CHAIRCD = L5.CHAIRCD AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1' && $subclasscd != '999999') {
                $query .= "     T1.CLASSCD          = '{$substr_classcd}' AND ";
                $query .= "     T1.SCHOOL_KIND      = '{$substr_school_kind}' AND ";
                $query .= "     T1.CURRICULUM_CD    = '{$substr_curriculum_cd}' AND ";
                $query .= "     T1.SUBCLASSCD       = '{$substr_subclasscd}' AND ";
            } else {
                $query .= "     T1.SUBCLASSCD = '{$subclasscd}' AND ";
            }
            //得点の場合
            if ($model->field["OUTPUT_JYOUKEN"] === '1') {
                $query .= "     T1.SCORE <= ".$model->field["SCORE_RANGE"]." ";
            //順位の場合
            } else {
                $query .= "     {$setrank} >  ( SELECT ";
                $query .= "                             MAX({$hikakurank}) - ".$model->field["RANK_RANGE"]." AS RANK ";
                $query .= "                        FROM  ";
                $query .= "                             RECORD_RANK_DAT M1  ";
                $query .= "                        LEFT JOIN ";
                $query .= "                             SCHREG_REGD_DAT M2 ON  M2.YEAR = M1.YEAR ";
                $query .= "                                                AND M2.SEMESTER = M1.SEMESTER ";
                $query .= "                                                AND M2.SCHREGNO = M1.SCHREGNO ";
                //コース別の場合
                $query .= "                        LEFT JOIN COURSE_MST CM1 ON CM1.COURSECD = M2.COURSECD ";
                $query .= "                        LEFT JOIN MAJOR_MST  CM2 ON CM2.COURSECD = M2.COURSECD ";
                $query .= "                                                AND CM2.MAJORCD  = M2.MAJORCD ";
                $query .= "                        LEFT JOIN V_COURSECODE_MST CM3 ON CM3.YEAR = M2.YEAR ";
                $query .= "                                                      AND VALUE(CM3.COURSECODE,'0000') = VALUE(M2.COURSECODE,'0000') ";
                $query .= "                        WHERE ";
                $query .= "                             M1.YEAR = '".CTRL_YEAR."' AND ";
                $query .= "                             M1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
                $query .= "                             M2.GRADE = '{$model->field["GRADE"]}' AND ";
                $query .= "                             M2.COURSECD   = '{$getcource[$i]["COURSECD"]}' AND ";
                $query .= "                             M2.MAJORCD    = '{$getcource[$i]["MAJORCD"]}' AND ";
                $query .= "                             M2.COURSECODE = '{$getcource[$i]["COURSECODE"]}' AND ";
                $query .= "                             M1.TESTKINDCD || M1.TESTITEMCD = '{$model->field["TESTCD"]}' AND ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1' && $subclasscd != '999999') {
                    $query .= "                         M1.CLASSCD          = '{$substr_classcd}' AND ";
                    $query .= "                         M1.SCHOOL_KIND      = '{$substr_school_kind}' AND ";
                    $query .= "                         M1.CURRICULUM_CD    = '{$substr_curriculum_cd}' AND ";
                    $query .= "                         M1.SUBCLASSCD       = '{$substr_subclasscd}' ";
                } else {
                    $query .= "                         M1.SUBCLASSCD = '{$subclasscd}' ";
                }        
                $query .= "                       ) ";
            }
        }
        $query .= " ORDER BY ";
        if ($model->field["OUTPUT_RANK"] == "2") {
            $query .= "     COURSECODE, ";
        }
        $query .= "     RANK, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     CHAIRCD DESC, ";
        $query .= "     STAFFCD DESC ";

        return $query;
    }
    
    //テスト科目に含まれる該当コースを取得する
    function getCourseQuery($model, $subclasscd) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $substr_classcd         = substr($subclasscd, 0, 2);
            $substr_school_kind     = substr($subclasscd, 3, 1);
            $substr_curriculum_cd   = substr($subclasscd, 5, 1);
            $substr_subclasscd      = substr($subclasscd, 7, 6);
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     L1.COURSECD, ";
        $query .= "     L1.MAJORCD, ";
        $query .= "     L1.COURSECODE, ";
        $query .= "     C3.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     RECORD_RANK_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_DAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                        AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND L1.SCHREGNO = T1.SCHREGNO ";
        //コース別の場合
        if ($model->field["OUTPUT_RANK"] == "2") {
            $query .= " LEFT JOIN COURSE_MST C1 ON C1.COURSECD = L1.COURSECD ";
            $query .= " LEFT JOIN MAJOR_MST C2 ON C2.COURSECD = L1.COURSECD ";
            $query .= "                       AND C2.MAJORCD = L1.MAJORCD ";
            $query .= " LEFT JOIN V_COURSECODE_MST C3 ON C3.YEAR = L1.YEAR ";
            $query .= "                              AND VALUE(C3.COURSECODE,'0000') = VALUE(L1.COURSECODE,'0000') ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT L2 ON  L2.YEAR = T1.YEAR ";
        $query .= "                         AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "                         AND L2.GRADE = L1.GRADE ";
        $query .= "                         AND L2.HR_CLASS = L1.HR_CLASS ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L3 ON L3.SCHREGNO = T1.SCHREGNO ";
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
            $query .= "     T1.SUBCLASSCD       = '{$substr_subclasscd}' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '{$subclasscd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     L1.COURSECD, ";
        $query .= "     L1.MAJORCD, ";
        $query .= "     L1.COURSECODE ";

        return $query;
    }
}
?>
