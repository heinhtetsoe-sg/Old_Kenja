<?php

require_once('for_php7.php');

class knjd219eQuery extends Query {

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
        $query .= "     AND SEMESTER <> '9' ";
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
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE || ':' || L3.COURSENAME || L3.MAJORNAME || L4.COURSECODENAME AS LABEL, ";
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

    //コースグループコンボ
    function getGroupCd($model) {
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     GROUP_CD || ':' || GROUP_NAME AS LABEL, ";
        $query .= "     GROUP_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COURSE_GROUP_CD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND GRADE    = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目コンボ
    function getSubclasscd($model) {
        $year = CTRL_YEAR;
        $staffcd = STAFFCD;

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
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN ( ";
            } else {
	            $query .= "     AND SUBCLASSCD IN ( ";
            }
            $query .= "         SELECT ";
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $query .= "     		T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            } else {
	            $query .= "             T1.SUBCLASSCD ";
            }
            $query .= "         FROM ";
            $query .= "             CHAIR_DAT T1, ";
            $query .= "             CHAIR_STD_DAT T2, ";
            $query .= "             CHAIR_STF_DAT T4 ";
            $query .= "         WHERE ";
            $query .= "                 T1.YEAR       = '{$year}' ";
            $query .= "             AND T1.SEMESTER   = '{$model->field["SEMESTER"]}' ";
            $query .= "             AND SUBSTR(T1.SUBCLASSCD, 1 ,2) <= '90' ";
            $query .= "             AND T2.YEAR       = T1.YEAR ";
            $query .= "             AND T2.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T2.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.YEAR       = T1.YEAR ";
            $query .= "             AND T4.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T4.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.STAFFCD    = '{$staffcd}' "; //ログインした先生
            $query .= "         GROUP BY ";
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $query .= "     	T1.CLASSCD, ";
	            $query .= "     	T1.SCHOOL_KIND, ";
	            $query .= "     	T1.CURRICULUM_CD, ";
	        }
            $query .= "             T1.SUBCLASSCD ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //平均点・最低点・最高点を取得(計算用)・・・序列確定後
    function getRecordAverage($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.COUNT ";
        $query .= " FROM ";
        $query .= "     RECORD_AVERAGE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        $query .= "     AND T1.AVG_DIV  = '{$model->field["DIV"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //表示切替(1:学年 2:クラス 3:コース 5:コースグループ)
        if ($model->field["DIV"] == '2') {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        } else if ($model->field["DIV"] == '3') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        } else if ($model->field["DIV"] == '5') {
            $query .= "     AND T1.MAJORCD = '{$model->field["GROUP_CD"]}' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "     AND T1.CLASSCD 			= '".$subclass_array[0]."' ";
	        $query .= "     AND T1.SCHOOL_KIND 		= '".$subclass_array[1]."' ";
	        $query .= "     AND T1.CURRICULUM_CD 	= '".$subclass_array[2]."' ";
	        $query .= "     AND T1.SUBCLASSCD 		= '".$subclass_array[3]."' ";
        } else {
	        $query .= "     AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }

        return $query;
    }

    //リスト取得
    function getList($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.ASSESSLEVEL, ";
        $query .= "     T1.ASSESSLOW, ";
        $query .= "     T1.ASSESSHIGH, ";
        $query .= "     T1.PERCENT ";
        $query .= " FROM ";
        $query .= "     ASSESS_LEVEL_SEMES_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        $query .= "     AND T1.DIV      = '{$model->field["DIV"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //表示切替(1:学年 2:クラス 3:コース 5:コースグループ)
        if ($model->field["DIV"] == '2') {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        } else if ($model->field["DIV"] == '3') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        } else if ($model->field["DIV"] == '5') {
            $query .= "     AND T1.MAJORCD = '{$model->field["GROUP_CD"]}' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "     AND T1.CLASSCD 			= '".$subclass_array[0]."' ";
	        $query .= "     AND T1.SCHOOL_KIND 		= '".$subclass_array[1]."' ";
	        $query .= "     AND T1.CURRICULUM_CD 	= '".$subclass_array[2]."' ";
	        $query .= "     AND T1.SUBCLASSCD 		= '".$subclass_array[3]."' ";
        } else {
	        $query .= "     AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.ASSESSLEVEL ";

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
            $query .= "     	T1.CLASSCD, ";
            $query .= "     	T1.SCHOOL_KIND, ";
            $query .= "     	T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         COUNT(T1.ASSESSLEVEL) AS CNT_DANKAI ";
        $query .= "     FROM ";
        $query .= "         ASSESS_LEVEL_SEMES_MST T1 ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$year}' ";
        $query .= "         AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        $query .= "         AND T1.DIV = '{$model->field["DIV"]}' ";
        $query .= "         AND T1.GRADE = '{$model->field["GRADE"]}' ";
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
            $query .= "     	T1.CLASSCD, ";
            $query .= "     	T1.SCHOOL_KIND, ";
            $query .= "     	T1.CURRICULUM_CD, ";
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
            $query .= "     	T1.CLASSCD, ";
            $query .= "     	T1.SCHOOL_KIND, ";
            $query .= "     	T1.CURRICULUM_CD, ";
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
            $query .= "     	AND L5.CLASSCD = T1.CLASSCD ";
            $query .= "   		AND L5.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "  		AND L5.CURRICULUM_CD = T1.CURRICULUM_CD ";
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
            $query .= "     	T1.CLASSCD, ";
            $query .= "     	T1.SCHOOL_KIND, ";
            $query .= "     	T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }

    //各評定値の人数割合（％）
    function getCountPercent($model, $assessLow, $assessHigh, $insertFlg = "") {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $year = CTRL_YEAR;
        $schregSemester = ($model->field["SEMESTER"] == "9") ? CTRL_SEMESTER : $model->field["SEMESTER"];

        $query  = "";
        //各学期までの学年成績
        $query .= " WITH T_GAKUNEN_SEISEKI AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         SMALLINT(ROUND(AVG(FLOAT(L1.SCORE)),0)) AS GRAD_SCORE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN RECORD_RANK_DAT L1 ";
        $query .= "             ON  L1.YEAR         = T1.YEAR ";
        //（仕様案①）学年成績は、ここで算出した値を参照する場合の条件・・・TODO:とりあえずこの案にしておく
        $query .= "             AND L1.SEMESTER     = '{$model->field["SEMESTER"]}' ";
        $query .= "             AND L1.TESTKINDCD || L1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        //（仕様案②）学年成績は、別プログラムで算出した値を参照する場合の条件
        //$query .= "             AND L1.SEMESTER || L1.TESTKINDCD || L1.TESTITEMCD = '99901' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "    			AND L1.CLASSCD 			= '".$subclass_array[0]."' ";
	        $query .= "    			AND L1.SCHOOL_KIND 		= '".$subclass_array[1]."' ";
	        $query .= "    			AND L1.CURRICULUM_CD 	= '".$subclass_array[2]."' ";
	        $query .= "    			AND L1.SUBCLASSCD 		= '".$subclass_array[3]."' ";
        } else {
	        $query .= "             AND L1.SUBCLASSCD   = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "             AND L1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '{$year}' ";
        $query .= "         AND T1.SEMESTER = '{$schregSemester}' ";
        $query .= "         AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //表示切替(1:学年 2:クラス 3:コース 5:コースグループ)
        if ($model->field["DIV"] == '2') {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        } else if ($model->field["DIV"] == '3') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        } else if ($model->field["DIV"] == '5') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN ( ";
            $query .= "         SELECT ";
            $query .= "             G1.COURSECD || G1.MAJORCD || G1.COURSECODE AS COURSE ";
            $query .= "         FROM ";
            $query .= "             COURSE_GROUP_CD_DAT G1 ";
            $query .= "         WHERE ";
            $query .= "                 G1.YEAR     = '{$year}' ";
            $query .= "             AND G1.GRADE    = '{$model->field["GRADE"]}' ";
            $query .= "             AND G1.GROUP_CD = '{$model->field["GROUP_CD"]}' ";
            $query .= "     ) ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE ";
        $query .= "     ) ";

        //確定処理（学年成績・単位）
        if ($insertFlg != "") {
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.GRAD_SCORE, ";
            $query .= "     T4.CREDITS ";
            $query .= " FROM ";
            $query .= "     T_GAKUNEN_SEISEKI T1 ";
            $query .= "     LEFT JOIN CREDIT_MST T4 ";
            $query .= "         ON  T4.YEAR = '{$year}' ";
            $query .= "         AND T4.COURSECD = T1.COURSECD ";
            $query .= "         AND T4.MAJORCD = T1.MAJORCD ";
            $query .= "         AND T4.GRADE = T1.GRADE ";
            $query .= "         AND T4.COURSECODE = T1.COURSECODE ";
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
		        $query .= "    		AND T4.CLASSCD 			= '".$subclass_array[0]."' ";
		        $query .= "    		AND T4.SCHOOL_KIND 		= '".$subclass_array[1]."' ";
		        $query .= "    		AND T4.CURRICULUM_CD 	= '".$subclass_array[2]."' ";
		        $query .= "    		AND T4.SUBCLASSCD 		= '".$subclass_array[3]."' ";
	        } else {
	            $query .= "         AND T4.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.GRAD_SCORE BETWEEN {$assessLow} AND {$assessHigh} ";

        //シミュレーション処理（人数）
        } else {
            $query .= " SELECT ";
            $query .= "     COUNT(T1.SCHREGNO) AS CNT ";
            $query .= " FROM ";
            $query .= "     T_GAKUNEN_SEISEKI T1 ";
            $query .= " WHERE ";
            $query .= "     T1.GRAD_SCORE BETWEEN {$assessLow} AND {$assessHigh} ";
        }

        return $query;
    }

    //学年成績・学年評定・履修単位・修得単位を作成
    function insRecordScore($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($gradValue = 1; $gradValue <= $model->field["ASSESSLEVELCNT"]; $gradValue++) {
            //学年成績の下限・上限
            $assessLow = $model->field["ASSESSLOW".$gradValue];
            if ($gradValue == $model->field["ASSESSLEVELCNT"]) {
                $assessHigh = $model->field["ASSESSHIGH".$gradValue];
            } else {
                $assessHigh = ($model->field["ASSESSLOW".($gradValue + 1)] - 1);
            }
            //学年成績・単位を取得
            $query = knjd219eQuery::getCountPercent($model, $assessLow, $assessHigh, "insert");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subCd      = $model->field["SUBCLASSCD"];
                $schno      = $row["SCHREGNO"];
                $gradScore  = $row["GRAD_SCORE"];
                $compCredit = $row["CREDITS"];
                $getCredit  = ($gradValue == 1) ? 0 : $row["CREDITS"];
//echo "schno=" .$schno .", gradScore=" .$gradScore .", gradValue=" .$gradValue .", compCredit=" .$compCredit .", getCredit=" .$getCredit ."<BR>";
                //学年成績を作成
                $query = knjd219eQuery::getDeleteRecordDat(CTRL_YEAR, "9", "99", "01", "00", $subCd, $schno, $model);
                $db->query($query);
                $query = knjd219eQuery::getInsertRecordDat(CTRL_YEAR, "9", "99", "01", "00", $subCd, $schno, $gradScore, "", "", $model);
                $db->query($query);
                //学年評定・履修単位・修得単位を作成
                $query = knjd219eQuery::getDeleteRecordDat(CTRL_YEAR, "9", "99", "00", "00", $subCd, $schno, $model);
                $db->query($query);
                $query = knjd219eQuery::getInsertRecordDat(CTRL_YEAR, "9", "99", "00", "00", $subCd, $schno, $gradValue, $compCredit, $getCredit, $model);
                $db->query($query);
                //履歴：仮評定を保持
                $query = knjd219eQuery::getDeleteProv(CTRL_YEAR, $model->field["SEMESTER"], "99", "00", $subCd, $schno, $model);
                $db->query($query);
                $query = knjd219eQuery::getInsertProv(CTRL_YEAR, $model->field["SEMESTER"], "99", "00", $subCd, $schno, $gradValue, $compCredit, $getCredit, $model);
                $db->query($query);
            }
            $result->free();
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //RECORD_SCORE_DATのレコードを削除。
    function getDeleteRecordDat($year, $sem, $kind, $item, $div, $subCd, $schno, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $subCd);
        }
        $query  = "";
        $query .= " DELETE FROM RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$sem}' ";
        $query .= "     AND TESTKINDCD = '{$kind}' ";
        $query .= "     AND TESTITEMCD = '{$item}' ";
        $query .= "     AND SCORE_DIV = '{$div}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "    	AND CLASSCD 		= '".$subclass_array[0]."' ";
	        $query .= "    	AND SCHOOL_KIND 	= '".$subclass_array[1]."' ";
	        $query .= "    	AND CURRICULUM_CD 	= '".$subclass_array[2]."' ";
	        $query .= "    	AND SUBCLASSCD 		= '".$subclass_array[3]."' ";
        } else {
	        $query .= "     AND SUBCLASSCD = '{$subCd}' ";
        }
        $query .= "     AND SCHREGNO = '{$schno}' ";
        return $query;
    }

    //RECORD_SCORE_DATのレコードを追加
    function getInsertRecordDat($year, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $subCd);
        }
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = $sem;
        $data["TESTKINDCD"][TEXT]   = $kind;
        $data["TESTITEMCD"][TEXT]   = $item;
        $data["SCORE_DIV"][TEXT]    = $div;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
        } else {
	        $data["SUBCLASSCD"][TEXT]   = $subCd;
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        $data["VALUE"][NUMBER]      = $gradValue;
        $data["COMP_CREDIT"][NUMBER]= $compCredit;
        $data["GET_CREDIT"][NUMBER] = $getCredit;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
        return $query;
    }

    //RECORD_PROV_RATE_DATのレコードを削除。
    function getDeleteProv($year, $sem, $kind, $item, $subCd, $schno, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $subCd);
        }
        $query  = "";
        $query .= " DELETE FROM RECORD_PROV_RATE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$sem}' ";
        $query .= "     AND TESTKINDCD = '{$kind}' ";
        $query .= "     AND TESTITEMCD = '{$item}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "    	AND CLASSCD 		= '".$subclass_array[0]."' ";
	        $query .= "    	AND SCHOOL_KIND 	= '".$subclass_array[1]."' ";
	        $query .= "    	AND CURRICULUM_CD 	= '".$subclass_array[2]."' ";
	        $query .= "    	AND SUBCLASSCD 		= '".$subclass_array[3]."' ";
        } else {
	        $query .= "     AND SUBCLASSCD = '{$subCd}' ";
        }
        $query .= "     AND SCHREGNO = '{$schno}' ";
        return $query;
    }

    //RECORD_PROV_RATE_DATのレコードを追加（履歴：仮評定を保持）
    function getInsertProv($year, $sem, $kind, $item, $subCd, $schno, $gradValue, $compCredit, $getCredit, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $subCd);
        }
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = $sem;
        $data["TESTKINDCD"][TEXT]   = $kind;
        $data["TESTITEMCD"][TEXT]   = $item;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
        } else {
	        $data["SUBCLASSCD"][TEXT]   = $subCd;
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        $data["VALUE"][NUMBER]      = $gradValue;
        $data["COMP_CREDIT"][NUMBER]= $compCredit;
        $data["GET_CREDIT"][NUMBER] = $getCredit;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_PROV_RATE_DAT");
        return $query;
    }

    //Insert作成
    function insAssessLevel($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $subclass_array = array();
	        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        }
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変数
        $year = CTRL_YEAR;

        //削除
        $query  = " DELETE FROM ";
        $query .= "     ASSESS_LEVEL_SEMES_MST T1 ";
        $query .= " WHERE  ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        $query .= "     AND T1.DIV      = '{$model->field["DIV"]}' ";
        $query .= "     AND T1.GRADE    = '{$model->field["GRADE"]}' ";
        //表示切替(1:学年 2:クラス 3:コース 5:コースグループ)
        if ($model->field["DIV"] == '2') {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        } else if ($model->field["DIV"] == '3') {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        } else if ($model->field["DIV"] == '5') {
            $query .= "     AND T1.MAJORCD = '{$model->field["GROUP_CD"]}' ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "    	AND T1.CLASSCD 			= '".$subclass_array[0]."' ";
	        $query .= "    	AND T1.SCHOOL_KIND 		= '".$subclass_array[1]."' ";
	        $query .= "    	AND T1.CURRICULUM_CD 	= '".$subclass_array[2]."' ";
	        $query .= "    	AND T1.SUBCLASSCD 		= '".$subclass_array[3]."' ";
        } else {
	        $query .= "     AND T1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $db->query($query);

        //追加
        for ($i = 1; $i <= $model->field["ASSESSLEVELCNT"]; $i++) {
            $data = array();
            $data["YEAR"][TEXT]             = $year;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["TESTKINDCD"][TEXT]       = substr($model->field["TESTKINDCD"],0,2);
            $data["TESTITEMCD"][TEXT]       = substr($model->field["TESTKINDCD"],2,2);
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $data["CLASSCD"][TEXT]        = $subclass_array[0];
	            $data["SCHOOL_KIND"][TEXT]    = $subclass_array[1];
	            $data["CURRICULUM_CD"][TEXT]  = $subclass_array[2];
	            $data["SUBCLASSCD"][TEXT]     = $subclass_array[3];
	        } else {
	            $data["SUBCLASSCD"][TEXT]     = $model->field["SUBCLASSCD"];
            }
            $data["DIV"][TEXT]              = $model->field["DIV"];
            $data["GRADE"][TEXT]            = $model->field["GRADE"];
            $data["HR_CLASS"][TEXT]         = strlen($model->field["HR_CLASS"]) ? $model->field["HR_CLASS"]           : "000";
            $data["COURSECD"][TEXT]         = strlen($model->field["COURSE"])   ? substr($model->field["COURSE"],0,1) : "0";
            $data["MAJORCD"][TEXT]          = strlen($model->field["COURSE"])   ? substr($model->field["COURSE"],1,3) : strlen($model->field["GROUP_CD"]) ? $model->field["GROUP_CD"] : "000";
            $data["COURSECODE"][TEXT]       = strlen($model->field["COURSE"])   ? substr($model->field["COURSE"],4,4) : "0000";

            $data["ASSESSLEVEL"][NUMBER]    = $i;
            $data["ASSESSLOW"][NUMBER]      = $model->field["ASSESSLOW".$i];
            if ($i == $model->field["ASSESSLEVELCNT"]) {
                $data["ASSESSHIGH"][NUMBER] = $model->field["ASSESSHIGH".$i];
            } else {
                $data["ASSESSHIGH"][NUMBER] = ($model->field["ASSESSLOW".($i + 1)] - 1);
            }
            $data["PERCENT"][NUMBER]        = $model->field["PERCENT".$i];

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "ASSESS_LEVEL_SEMES_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
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
        $query .= "     ASSESS_LEVEL_SEMES_MST T1 ";
        $query .= " WHERE  ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["TESTKINDCD"]}' ";
        $db->query($query);

        //COPY
        $query  = " INSERT INTO ASSESS_LEVEL_SEMES_MST ";
        $query .= " SELECT ";
        $query .= "     '{$year}', ";
        $query .= "     '{$model->field["SEMESTER"]}', ";
        $query .= "     '{$testkindcd}', ";
        $query .= "     '{$testitemcd}', ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     	T1.CLASSCD, ";
            $query .= "     	T1.SCHOOL_KIND, ";
            $query .= "     	T1.CURRICULUM_CD, ";
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
        $query .= "     T1.PERCENT, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     ASSESS_LEVEL_SEMES_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->field["PRE_SEMESTER"]}' ";
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$model->field["PRE_TESTKINDCD"]}' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
