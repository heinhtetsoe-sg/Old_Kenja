<?php

require_once('for_php7.php');
class knji100c_03Query extends Query {

/*
        性別区分 .........Z002

        使用テーブル
        SCHREG_BASE_MST......基礎情報取得用
        SCHREG_REGD_DAT......学籍情報取得用
        ATTEND_SUBCLASS_DAT..出欠情報（科目別）取得用
        SUBCLASS_MST.........科目名取得用
*/

    //ＣＳＶ書き出し
    function selectQuery($model, $attend_seme, $month, $absent_cov, $absent_cov_late, $amari_kuriage) {

        $query  = " WITH BASE_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ) ";
        //出欠の単純集計
        $query .= " , ATTEND_SUBCLASS AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SEMESTER, ";
        $query .= "         MONTH, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     	CLASSCD, ";
            $query .= "     	SCHOOL_KIND, ";
            $query .= "     	CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SICK, ";
        $query .= "         NOTICE, ";
        $query .= "         NONOTICE, ";
        $query .= "         NURSEOFF, ";
        $query .= "         LATE, ";
        $query .= "         EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') AND ";
        $query .= "         SEMESTER  <= '".$attend_seme."' AND ";
        $query .= "         MONTH IN ('". implode($month, "','") ."') ";
        $query .= " ), SUBCLASS_DATA AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SEMESTER ";
        for($i=0; $i<get_count($model->opt_subclass); $i++){
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
		        $subclass_array = array();
		        $subclass_array = explode("-", $model->opt_subclass[$i]);
	        }
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $query .= "     , '".$subclass_array[0]."'  AS CLASSCD".$i." ";
	            $query .= "     , '".$subclass_array[1]."'  AS SCHOOL_KIND".$i." ";
	            $query .= "     , '".$subclass_array[2]."'  AS CURRICULUM_CD".$i." ";
	            $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$subclass_array[3]."' THEN INT(SUBCLASSCD) ELSE NULL END) AS SUBCLASSCD".$i." ";
	            $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$subclass_array[3]."' THEN (SICK + NOTICE + NONOTICE + NURSEOFF) ELSE NULL END) AS KETSUJI".$i." ";
	            $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$subclass_array[3]."' THEN (LATE + EARLY) ELSE NULL END) AS LATE_EARLY".$i." ";
	            if (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0) {
	                $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$subclass_array[3]."' THEN (LATE + EARLY) ELSE 0 END) / {$absent_cov_late} * {$absent_cov_late} AS CONVERTED_LATE_EARLY".$i." ";
	            }
	            $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$subclass_array[3]."' THEN 0 ELSE NULL END) AS KEKKA".$i." ";
            } else {
	            $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN INT(SUBCLASSCD) ELSE NULL END) AS SUBCLASSCD".$i." ";
	            $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN (SICK + NOTICE + NONOTICE + NURSEOFF) ELSE NULL END) AS KETSUJI".$i." ";
	            $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN (LATE + EARLY) ELSE NULL END) AS LATE_EARLY".$i." ";
	            if (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0) {
	                $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN (LATE + EARLY) ELSE 0 END) / {$absent_cov_late} * {$absent_cov_late} AS CONVERTED_LATE_EARLY".$i." ";
	            }
	            $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN 0 ELSE NULL END) AS KEKKA".$i." ";
        	}
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         SEMESTER ";
        $query .= " ) ";

        //学期ごとに清算
        $query .= " , SUBCLASS_DATA2 AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SEMESTER ";
        for($i=0; $i<get_count($model->opt_subclass); $i++){
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $query .= "         ,CLASSCD".$i." ";
	            $query .= "         ,SCHOOL_KIND".$i." ";
	            $query .= "         ,CURRICULUM_CD".$i." ";
            }
            $query .= "         ,SUBCLASSCD".$i." ";
            $query .= "         ,KETSUJI".$i." ";
            if ($model->Properties["chikokuHyoujiFlg"] == "1") {
                $query .= "     ,LATE_EARLY".$i." ";
            } else {
                if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "     ,0 AS LATE_EARLY".$i." ";
                } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "     ,CASE WHEN CONVERTED_LATE_EARLY".$i." >= LATE_EARLY".$i." THEN 0 ELSE (LATE_EARLY".$i." - CONVERTED_LATE_EARLY".$i.") END AS LATE_EARLY".$i." ";
                } else {
                    $query .= "     ,LATE_EARLY".$i." ";
                }
            }
            if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     ,decimal((float(LATE_EARLY".$i.") / {$absent_cov_late}) + KETSUJI".$i.",4,1) AS KEKKA".$i." ";
            } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     ,(LATE_EARLY".$i." / {$absent_cov_late}) + KETSUJI".$i." AS KEKKA".$i." ";
            } else {
                $query .= "     ,KETSUJI".$i." AS KEKKA".$i." ";
            }
        }
        $query .= "     FROM ";
        $query .= "         SUBCLASS_DATA ";
        $query .= " ) ";

        //年間で清算
        $query .= " , SUBCLASS_DATA3 AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHREGNO ";
        for($i=0; $i<get_count($model->opt_subclass); $i++){
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $query .= "         ,CLASSCD".$i." ";
	            $query .= "         ,SCHOOL_KIND".$i." ";
	            $query .= "         ,CURRICULUM_CD".$i." ";
            }
            $query .= "         ,SUBCLASSCD".$i." ";
            $query .= "         ,SUM(KETSUJI".$i.") AS KETSUJI".$i." ";
            if ($model->Properties["chikokuHyoujiFlg"] == "1") {
                $query .= "     ,SUM(LATE_EARLY".$i.") AS LATE_EARLY".$i." ";
            } else {
                if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "     ,0 AS LATE_EARLY".$i." ";
                } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "     ,CASE WHEN (SUM(LATE_EARLY".$i.") / {$absent_cov_late} * {$absent_cov_late}) >= SUM(LATE_EARLY".$i.") THEN 0 ELSE (SUM(LATE_EARLY".$i.") - (SUM(LATE_EARLY".$i.") / {$absent_cov_late} * {$absent_cov_late})) END AS LATE_EARLY".$i." ";
                } elseif (($absent_cov == "5") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                    $query .= "     ,CASE WHEN MOD(SUM(LATE_EARLY".$i."), {$absent_cov_late}) >= {$amari_kuriage} THEN 0 ELSE MOD(SUM(LATE_EARLY".$i."), {$absent_cov_late}) END AS LATE_EARLY".$i." ";
                } else {
                    $query .= "     ,SUM(LATE_EARLY".$i.") AS LATE_EARLY".$i." ";
                }
            }
            if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     ,decimal((float(SUM(LATE_EARLY".$i.")) / {$absent_cov_late}) + SUM(KETSUJI".$i."),4,1) AS KEKKA".$i." ";
            } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     ,(SUM(LATE_EARLY".$i.") / {$absent_cov_late}) + SUM(KETSUJI".$i.") AS KEKKA".$i." ";
            } elseif (($absent_cov == "5") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                $query .= "     ,(SUM(LATE_EARLY".$i.") / {$absent_cov_late}) + SUM(KETSUJI".$i.") +  (CASE WHEN MOD(SUM(LATE_EARLY".$i."), {$absent_cov_late}) >= {$amari_kuriage} THEN 1 ELSE 0 END) AS KEKKA".$i." ";
            } else {
                $query .= "     ,SUM(KEKKA".$i.") AS KEKKA".$i." ";
            }
        }
        $query .= "     FROM ";
        $query .= "         SUBCLASS_DATA2 ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        for($i=0; $i<get_count($model->opt_subclass); $i++){
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $query .= "         ,CLASSCD".$i." ";
	            $query .= "         ,SCHOOL_KIND".$i." ";
	            $query .= "         ,CURRICULUM_CD".$i." ";
            }
            $query .= "         ,SUBCLASSCD".$i." ";
        }
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        if ($model->output == OUT_CODE_NAME){           //コード＋名称
            $query .= "     VALUE(T1.SEX,'') || ',' || ";
            $query .= "     VALUE(N1.NAME1,'') SEX, ";
        } else if ($model->output == OUT_CODE_ONLY){    //コードのみ
            $query .= "     T1.SEX SEX, ";
        } else if ($model->output == OUT_NAME_ONLY){    //名称のみ
            $query .= "     N1.NAME1 SEX, ";
        }
        $query .= "     '".$model->month_name."まで' AS MONTH ";
        for($i=0; $i<get_count($model->opt_subclass); $i++){
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
		        $subclass_array = array();
		        $subclass_array = explode("-", $model->opt_subclass[$i]);
	        }
	        //教育課程対応
	        if ($model->Properties["useCurriculumcd"] == '1') {
	            $query .= "     ,CASE WHEN L1.SUBCLASSCD" .$i ." IS NOT NULL AND L1.CLASSCD" .$i ." IS NOT NULL THEN '".$subclass_array[0] ."' ELSE NULL END AS CLASSCD" .$i ." ";
	            $query .= "     ,CASE WHEN L1.SUBCLASSCD" .$i ." IS NOT NULL AND L1.SCHOOL_KIND" .$i ." IS NOT NULL THEN '".$subclass_array[1] ."' ELSE NULL END AS SCHOOL_KIND" .$i ." ";
	            $query .= "     ,CASE WHEN L1.SUBCLASSCD" .$i ." IS NOT NULL AND L1.CURRICULUM_CD" .$i ." IS NOT NULL THEN '".$subclass_array[2] ."' ELSE NULL END AS CURRICULUM_CD" .$i ." ";
	            $query .= "     ,CASE WHEN L1.SUBCLASSCD" .$i ." IS NOT NULL THEN '".$subclass_array[3] ."' ELSE NULL END AS SUBCLASSCD" .$i ." ";
	            $query .= "     ,L1.KETSUJI" .$i ." ";
	            $query .= "     ,L1.LATE_EARLY" .$i ." ";
	            $query .= "     ,L1.KEKKA" .$i ." ";
            } else {
	            $query .= "     ,CASE WHEN L1.SUBCLASSCD" .$i ." IS NOT NULL THEN '".$model->opt_subclass[$i] ."' ELSE NULL END AS SUBCLASSCD" .$i ." ";
	            $query .= "     ,L1.KETSUJI" .$i ." ";
	            $query .= "     ,L1.LATE_EARLY" .$i ." ";
	            $query .= "     ,L1.KEKKA" .$i ." ";
            }
        }
        $query .= " FROM ";
        $query .= "     BASE_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD2 = T1.SEX AND N1.NAMECD1='Z002' ";
        $query .= "     LEFT JOIN SUBCLASS_DATA3 L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON  T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                                   AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //科目取得
    function getSubclassList($model, $attend_seme, $month) {
        $query  = "     SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
	        $query .= "         T2.SUBCLASSNAME ";
        } else {
	        $query .= "         T1.SUBCLASSCD, ";
	        $query .= "         T2.SUBCLASSNAME ";
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT T1 ";
        $query .= "         LEFT JOIN V_SUBCLASS_MST T2 ON T1.YEAR = T2.YEAR AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     						   AND T1.CLASSCD = T2.CLASSCD ";
            $query .= "   							   AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= "  							   AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') AND ";
        $query .= "         T1.SEMESTER  <= '".$attend_seme."' AND ";
        $query .= "         T1.MONTH IN ('". implode($month, "','") ."') ";
        $query .= "     ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        $query .= "         SUBCLASSCD ";
        } else {
	        $query .= "         T1.SUBCLASSCD ";
        }

        return $query;
    }

    //遅刻何回で欠課とするかの指数取得
    function getSchoolAbsent() {

        $query  = " SELECT * FROM V_SCHOOL_MST WHERE YEAR = '".CTRL_YEAR."'";

        return $query;
    }

    //課程学科取得
    function getCourseMajor($model) {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS VALUE, ";
        $query .= "     COURSENAME || MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = "SELECT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                      REGD_G.GRADE ";
                $query .= "                   FROM ";
                $query .= "                      SCHREG_REGD_GDAT REGD_G ";
                $query .= "                   WHERE ";
                $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                      AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND GRADE IN (SELECT ";
            $query .= "                      REGD_G.GRADE ";
            $query .= "                   FROM ";
            $query .= "                      SCHREG_REGD_GDAT REGD_G ";
            $query .= "                   WHERE ";
            $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                      AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //性別取得
    function getSex($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'Z002' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || '_' ||  HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model) {
        $date = str_replace("/","-",CTRL_DATE);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
                           //卒業(1)・転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    (S1.GRD_DIV IN('1','2','3') AND S1.GRD_DATE < ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }

    //生徒リストtoリスト右
    function getStudent_right($model) {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' ";
        if ($model->coursemajor) {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '{$model->coursemajor}' ";
        }
        if ($model->grade) {
            $query .= " AND T1.GRADE = '{$model->grade}' ";
        }
        if ($model->sex) {
            $query .= " AND L1.SEX = '{$model->sex}' ";
        }
        if ($model->grade_hr_class) {
            $query .= " AND T1.GRADE || '_' || T1.HR_CLASS = '{$model->grade_hr_class}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒リストtoリスト左
    function getStudent_left($model) {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //対象月取得
    function getSemeMonth() {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "          THEN MONTH(SDATE) + 12 ";
        $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "          THEN MONTH(EDATE) + 12 ";
        $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "        YEAR  = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE = '".substr($model->grade_hr_class, 0, 2)."' ";

        return $query;
    }

    //対象月名称取得
    function getMonthName($month, $model) {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $setNameCd  = "Z".$model->schKind."05";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$setNameCd}' ";
        $query .= "     AND NAMECD2 = '".sprintf('%02d',$month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    //出欠名称取得
    function getAttendName($namecd2) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'C001' ";
        $query .= "     AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //サブシステム取得
    function getSubSystem() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'I001' ";

        return $query;
    }

    //選択フィールド取得
    function getFieldSql($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '03' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //選択フィールド削除
    function getDelFieldSql($model) {
        $query  = " DELETE FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '03' ";

        return $query;
    }

    //選択フィールド保存
    function getFieldInsSql($model, $setSeq, $val) {

        //入金予定項目データ
        $data = array();
        $data["YEAR"][TEXT]            = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]        = "03";
        $data["SEQ"][NUMBER]           = $setSeq;
        $data["FIELD_NAME"][TEXT]      = $val;
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "KNJI100C_KAKIDASHI_LIST");
        return $query;
    }
}
?>
