<?php

require_once('for_php7.php');

class knjz182Query extends Query {
    //学年コンボ
    function getGrade() {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND (SCHOOL_KIND = 'J' OR SCHOOL_KIND = 'H') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期コンボ
    function getSemester() {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //成績種別コンボ
    function getTestkindcd($semester) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     TESTITEMNAME AS LABEL, ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラスコンボ
    function getHrClass($model) {
        $year = CTRL_YEAR;
        $schregSemester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER : $model->field["SEMESTER"];

        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SEMESTER = '{$schregSemester}' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科コースコンボ
    function getCourse($model) {
        $year = CTRL_YEAR;
        $schregSemester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER : $model->field["SEMESTER"];

        $query  = " SELECT DISTINCT ";
        $query .= "     L3.COURSENAME || L3.MAJORNAME || L4.COURSECODENAME AS LABEL, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L3 ";
        $query .= "         ON  L3.YEAR = T1.YEAR ";
        $query .= "         AND L3.COURSECD = T1.COURSECD ";
        $query .= "         AND L3.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L4 ";
        $query .= "         ON  L4.YEAR = T1.YEAR ";
        $query .= "         AND L4.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$schregSemester}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校校種取得
    function getSchoolKind($model) {
        $query  = "SELECT ";
        $query .= "        SCHOOL_KIND ";
        $query .= "FROM ";
        $query .= "        SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "        GRADE = '".$model->field["GRADE"]."' ";
        $query .= "AND      YEAR = '".CTRL_YEAR."' ";
        
        return $query;
    }

    //科目コンボ
    function getSubclasscd($model, $schoolkind) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        } else {
            $query .= "     SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
            $query .= "     SUBCLASSCD AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND SCHOOL_KIND = '".$schoolkind."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $chairSemester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER : $model->field["SEMESTER"];
            $query .= " AND SUBSTR(SUBCLASSCD, 1, 2) IN ( ";
            $query .= "         SELECT ";
            $query .= "             SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD ";
            $query .= "         FROM ";
            $query .= "             CHAIR_STF_DAT T1, ";
            $query .= "             CHAIR_DAT T2 ";
            $query .= "         WHERE ";
            $query .= "             T1.YEAR = '{$year}' ";
            $query .= "             AND T1.SEMESTER = '{$chairSemester}' ";
            $query .= "             AND T1.STAFFCD = '".STAFFCD."' ";
            $query .= "             AND T2.YEAR = T1.YEAR ";
            $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "             AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "         GROUP BY ";
            $query .= "             SUBSTR(T2.SUBCLASSCD, 1, 2) ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //段階数
    function getAssessLevelCnt($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     COUNT(T1.ASSESSLEVEL) AS ASSESSLEVELCNT ";
        $query .= " FROM ";
        $query .= "     ASSESS_LEVEL_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        $query .= "     AND T1.DIV      = '{$model->field["DIV"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //表示切替(1:学年 2:クラス 3:コース)
        if ($model->field["DIV"] == '2') {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        } else if ($model->field["DIV"] == '3') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }

        return $query;
    }

    //平均点・最低点・最高点を取得(計算用)・・・序列確定後
    function getRecordAverage($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.AVG AS AVG, ";
        $query .= "     T1.LOWSCORE, ";
        $query .= "     T1.HIGHSCORE ";
        $query .= " FROM ";
        $query .= "     RECORD_AVERAGE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        $query .= "     AND T1.AVG_DIV  = '{$model->field["DIV"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //表示切替(1:学年 2:クラス 3:コース)
        if ($model->field["DIV"] == '2') {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        } else if ($model->field["DIV"] == '3') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }

        return $query;
    }

    //考査満点マスタ(DIV = 01:科目, 02:学年, 03:コース)
    function getPerfect($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     case when DIV IS NULL then 100 else PERFECT end as PERFECT ";
        $query .= " FROM ";
        $query .= "     PERFECT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND SEMESTER    = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND TESTKINDCD || TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        $query .= "     AND GRADE       = CASE WHEN DIV = '01' THEN '00' ELSE '{$model->field["GRADE"]}' END ";
        $query .= "     AND COURSECD || MAJORCD || COURSECODE = CASE WHEN DIV IN ('01','02') THEN '00000000' ELSE '{$model->field["COURSE"]}' END ";

        return $query;
    }

    //リスト取得
    function getList($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.ASSESSLEVEL, ";
        $query .= "     T1.ASSESSLOW, ";
        $query .= "     T1.ASSESSHIGH ";
        $query .= " FROM ";
        $query .= "     ASSESS_LEVEL_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        $query .= "     AND T1.DIV      = '{$model->field["DIV"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //表示切替(1:学年 2:クラス 3:コース)
        if ($model->field["DIV"] == '2') {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        } else if ($model->field["DIV"] == '3') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.ASSESSLEVEL DESC ";

        return $query;
    }

    //学年名(照会)
    function getGradeName($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 AS GRADE_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";

        return $query;
    }

    //成績種別名(照会)
    function getTestName($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T2.SEMESTERNAME || '　' || T1.TESTITEMNAME AS TEST_NAME ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";

        return $query;
    }

    //リスト取得(照会)
    function getListInquiry($model) {
        $year = CTRL_YEAR;
        $schregSemester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER : $model->field["SEMESTER"];

        $query  = " WITH T_ASSESS_LEVEL AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.TESTKINDCD, ";
        $query .= "         T1.TESTITEMCD, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.DIV, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         COUNT(T1.ASSESSLEVEL) AS CNT_DANKAI ";
        $query .= "     FROM ";
        $query .= "         ASSESS_LEVEL_MST T1 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$year}' ";
        $query .= "         AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        $query .= "         AND T1.DIV = '{$model->field["DIV"]}' ";
        $query .= "         AND T1.GRADE = '{$model->field["GRADE"]}' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $chairSemester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER : $model->field["SEMESTER"];
            $query .= "     AND SUBSTR(T1.SUBCLASSCD, 1, 2) IN ( ";
            $query .= "         SELECT ";
            $query .= "             SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD ";
            $query .= "         FROM ";
            $query .= "             CHAIR_STF_DAT T1, ";
            $query .= "             CHAIR_DAT T2 ";
            $query .= "         WHERE ";
            $query .= "             T1.YEAR = '{$year}' ";
            $query .= "             AND T1.SEMESTER = '{$chairSemester}' ";
            $query .= "             AND T1.STAFFCD = '".STAFFCD."' ";
            $query .= "             AND T2.YEAR = T1.YEAR ";
            $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "             AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "         GROUP BY ";
            $query .= "             SUBSTR(T2.SUBCLASSCD, 1, 2) ";
            $query .= "     ) ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.TESTKINDCD, ";
        $query .= "         T1.TESTITEMCD, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.DIV, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     S1.SEMESTERNAME, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD AS TESTCD, ";
        $query .= "     S2.TESTITEMNAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     L1.GRADE_NAME1 AS GRADE_NAME, ";
        $query .= "     T1.DIV, ";
        $query .= "     CASE WHEN T1.DIV = '1' THEN '学年' ";
        $query .= "          WHEN T1.DIV = '2' THEN 'クラス' ";
        $query .= "          WHEN T1.DIV = '3' THEN 'コース' ";
        $query .= "          ELSE NULL END AS DIV_NAME, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     L2.HR_NAME, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ";
        $query .= "     L3.COURSENAME || L3.MAJORNAME || L4.COURSECODENAME AS COURSE_NAME, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L5.SUBCLASSNAME, ";
        $query .= "     T1.CNT_DANKAI ";
        $query .= " FROM ";
        $query .= "     T_ASSESS_LEVEL T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST S1 ";
        $query .= "         ON  S1.YEAR = T1.YEAR ";
        $query .= "         AND S1.SEMESTER = T1.SEMESTER ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW S2 ";
        $query .= "         ON  S2.YEAR = T1.YEAR ";
        $query .= "         AND S2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND S2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND S2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ";
        $query .= "         ON  L1.YEAR = T1.YEAR ";
        $query .= "         AND L1.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L2 ";
        $query .= "         ON  L2.YEAR = T1.YEAR ";
        $query .= "         AND L2.SEMESTER = '{$schregSemester}' ";
        $query .= "         AND L2.GRADE = T1.GRADE ";
        $query .= "         AND L2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L3 ";
        $query .= "         ON  L3.YEAR = T1.YEAR ";
        $query .= "         AND L3.COURSECD = T1.COURSECD ";
        $query .= "         AND L3.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L4 ";
        $query .= "         ON  L4.YEAR = T1.YEAR ";
        $query .= "         AND L4.COURSECODE = T1.COURSECODE ";
        $query .= "     LEFT JOIN V_SUBCLASS_MST L5 ";
        $query .= "         ON  L5.YEAR = T1.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND L5.CLASSCD       = T1.CLASSCD ";
            $query .= "     AND L5.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "     AND L5.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "         AND L5.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }


    //Insert作成
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変数
        $year = CTRL_YEAR;

        //削除
        $query  = " DELETE FROM ";
        $query .= "     ASSESS_LEVEL_MST T1 ";
        $query .= " WHERE  ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        $query .= "     AND T1.DIV      = '{$model->field["DIV"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //表示切替(1:学年 2:クラス 3:コース)
        if ($model->field["DIV"] == '2') {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        } else if ($model->field["DIV"] == '3') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $db->query($query);

        //追加
        for ($i = $this->field["ASSESSLEVELCNT"]; $i > 0; $i--) {
            $data = array();
            $data["YEAR"][TEXT]          = $year;
            $data["SEMESTER"][TEXT]      = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT]    = substr($model->field["TESTKINDCD"],0,2);
            $data["TESTITEMCD"][TEXT]    = substr($model->field["TESTKINDCD"],2,2);
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]          = substr($model->field["SUBCLASSCD"], 0, 2);
                $data["SCHOOL_KIND"][TEXT]      = substr($model->field["SUBCLASSCD"], 3, 1);
                $data["CURRICULUM_CD"][TEXT]    = substr($model->field["SUBCLASSCD"], 5, 1);
                $data["SUBCLASSCD"][TEXT]       = substr($model->field["SUBCLASSCD"], 7, 6);
            } else {
                $data["SUBCLASSCD"][TEXT]    = $model->field["SUBCLASSCD"];
            }
            $data["DIV"][TEXT]           = $model->field["DIV"];
            $data["GRADE"][TEXT]         = $model->field["GRADE"];
            $data["HR_CLASS"][TEXT]      = strlen($model->field["HR_CLASS"]) ? $model->field["HR_CLASS"]           : "000";
            $data["COURSECD"][TEXT]      = strlen($model->field["COURSE"])   ? substr($model->field["COURSE"],0,1) : "0";
            $data["MAJORCD"][TEXT]       = strlen($model->field["COURSE"])   ? substr($model->field["COURSE"],1,3) : "000";
            $data["COURSECODE"][TEXT]    = strlen($model->field["COURSE"])   ? substr($model->field["COURSE"],4,4) : "0000";

            $data["ASSESSLEVEL"][NUMBER] = $i;
            $data["ASSESSLOW"][NUMBER]   = $model->field["ASSESSLOW" .$i];
            if ($i == $this->field["ASSESSLEVELCNT"]) {
//                $data["ASSESSHIGH"][NUMBER] = 100;
                $data["ASSESSHIGH"][NUMBER] = $model->field["ASSESSHIGH".$i];
            } else {
                $assesshigh = "ASSESSLOW".($i + 1);
                $data["ASSESSHIGH"][NUMBER] = ($model->field[$assesshigh] - 1);
            }

            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "sysdate()";

            $query = Query::insertSQL($data, "ASSESS_LEVEL_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //コピー時の存在チェック用
    function getCntCopyQuery($model, $copycd) {
        //変数
        $year = CTRL_YEAR;
        //SQL
        $query  = " SELECT COUNT(*) FROM ";
        $query .= "     ASSESS_LEVEL_MST T1 ";
        $query .= " WHERE  ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '{$copycd}' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $chairSemester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER : $model->field["SEMESTER"];
            $query .= "     AND SUBSTR(T1.SUBCLASSCD, 1, 2) IN ( ";
            $query .= "         SELECT ";
            $query .= "             SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD ";
            $query .= "         FROM ";
            $query .= "             CHAIR_STF_DAT T1, ";
            $query .= "             CHAIR_DAT T2 ";
            $query .= "         WHERE ";
            $query .= "             T1.YEAR = '{$year}' ";
            $query .= "             AND T1.SEMESTER = '{$chairSemester}' ";
            $query .= "             AND T1.STAFFCD = '".STAFFCD."' ";
            $query .= "             AND T2.YEAR = T1.YEAR ";
            $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "             AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "         GROUP BY ";
            $query .= "             SUBSTR(T2.SUBCLASSCD, 1, 2) ";
            $query .= "     ) ";
        }
        return $query;
    }

    //コピー
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変数
        $year = CTRL_YEAR;
        $testkindcd = substr($model->field["TESTKINDCD"],0,2);
        $testitemcd = substr($model->field["TESTKINDCD"],2,2);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     ASSESS_LEVEL_MST T1 ";
        $query .= " WHERE  ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $chairSemester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER : $model->field["SEMESTER"];
            $query .= "     AND SUBSTR(T1.SUBCLASSCD, 1, 2) IN ( ";
            $query .= "         SELECT ";
            $query .= "             SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD ";
            $query .= "         FROM ";
            $query .= "             CHAIR_STF_DAT T1, ";
            $query .= "             CHAIR_DAT T2 ";
            $query .= "         WHERE ";
            $query .= "             T1.YEAR = '{$year}' ";
            $query .= "             AND T1.SEMESTER = '{$chairSemester}' ";
            $query .= "             AND T1.STAFFCD = '".STAFFCD."' ";
            $query .= "             AND T2.YEAR = T1.YEAR ";
            $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "             AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "         GROUP BY ";
            $query .= "             SUBSTR(T2.SUBCLASSCD, 1, 2) ";
            $query .= "     ) ";
        }
        $db->query($query);

        //COPY
        $query  = " INSERT INTO ASSESS_LEVEL_MST ";
        $query .= " SELECT ";
        $query .= "     '{$year}', ";
        $query .= "     '{$model->field["SEMESTER"]}', ";
        $query .= "     '{$testkindcd}', ";
        $query .= "     '{$testitemcd}', ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.ASSESSLEVEL, ";
        $query .= "     T1.ASSESSMARK, ";
        $query .= "     T1.ASSESSLOW, ";
        $query .= "     T1.ASSESSHIGH, ";
        $query .= "     CAST(NULL AS DECIMAL(4,1)) AS PERCENT, ";
        $query .= "     CAST(NULL AS DECIMAL) AS STANDARD_ASSESSLOW, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ASSESS_LEVEL_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["PRE_SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["PRE_TESTKINDCD"]}' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $chairSemester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER : $model->field["SEMESTER"];
            $query .= "     AND SUBSTR(T1.SUBCLASSCD, 1, 2) IN ( ";
            $query .= "         SELECT ";
            $query .= "             SUBSTR(T2.SUBCLASSCD, 1, 2) AS CLASSCD ";
            $query .= "         FROM ";
            $query .= "             CHAIR_STF_DAT T1, ";
            $query .= "             CHAIR_DAT T2 ";
            $query .= "         WHERE ";
            $query .= "             T1.YEAR = '{$year}' ";
            $query .= "             AND T1.SEMESTER = '{$chairSemester}' ";
            $query .= "             AND T1.STAFFCD = '".STAFFCD."' ";
            $query .= "             AND T2.YEAR = T1.YEAR ";
            $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "             AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "         GROUP BY ";
            $query .= "             SUBSTR(T2.SUBCLASSCD, 1, 2) ";
            $query .= "     ) ";
        }

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
