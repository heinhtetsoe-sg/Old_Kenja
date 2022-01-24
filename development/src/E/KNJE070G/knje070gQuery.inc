<?php
class knje070gQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($year, $semester, $model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     REG_H.GRADE || REG_H.HR_CLASS AS VALUE, ";
            $query .= "     REG_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REG_H ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                    $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                    $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     REG_H.YEAR     = '".$year."' AND ";
            $query .= "     REG_H.SEMESTER = '".$semester."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     REG_H.GRADE || REG_H.HR_CLASS AS VALUE, ";
            $query .= "     REG_H.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REG_H ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                    $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                    $query .= "           AND REG_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT REG_G ON REG_H.YEAR = REG_G.YEAR ";
                $query .= "           AND REG_H.GRADE = REG_G.GRADE ";
                $query .= "           AND REG_G.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     REG_H.YEAR        = '".$year."' AND ";
            $query .= "     REG_H.SEMESTER    = '".$semester."' AND ";
            $query .= "     (REG_H.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "      REG_H.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "      REG_H.TR_CD3     = '".STAFFCD."' OR "; 
            $query .= "      REG_H.SUBTR_CD1  = '".STAFFCD."' OR "; 
            $query .= "      REG_H.SUBTR_CD2  = '".STAFFCD."' OR "; 
            $query .= "      REG_H.SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種を取得　getSchoolDiv()で使用
    function getSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '".$grade."' ";

        return $query;
    }

    //卒業可能学年チェック
    function checkGrdGrade($grade) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' AND ";
        $query .= "     NAME1   = 'H' AND ";
        $query .= "     '".$grade."' BETWEEN NAMESPARE2 AND NAMESPARE3 ";

        return $query;
    }

    //テーブルのフィールド確認
    function getTableColumn($tabname, $colname) {
        $query  = " SELECT T1.COLUMN_NAME ";
        $query .= " FROM ";
        $query .= "     SYSIBM.COLUMNS T1 ";
        $query .= " WHERE ";
        $query .= "     T1.TABLE_NAME = '".$tabname."' AND T1.COLUMN_NAME = '".$colname."' ";

        return $query;
    }

    //仮評定データ取得
    function getProvData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CASE WHEN T2.PROV_SEMESTER IS NULL THEN '99' ELSE T2.PROV_SEMESTER END AS PROV_SEMESTER, ";
        $query .= "     CASE WHEN T2.PROV_TESTKINDCD IS NULL THEN '999' ELSE T2.PROV_TESTKINDCD END AS PROV_TESTKINDCD, ";
        $query .= "     CASE WHEN T2.PROV_TESTITEMCD IS NULL THEN '999' ELSE T2.PROV_TESTITEMCD END AS PROV_TESTITEMCD, ";
        $query .= "     CASE WHEN T2.PROV_SCORE_DIV IS NULL THEN '999' ELSE T2.PROV_SCORE_DIV END AS PROV_SCORE_DIV, ";
        $query .= "     VALUE(L1.SEMESTERNAME,'　') AS SEMESTERNAME, ";
        $query .= "     VALUE(L2.TESTITEMNAME,'　') AS TESTITEMNAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     STUDYREC_PROV_FLG_DAT T2 ";
        $query .= "     LEFT JOIN SEMESTER_MST L1 ";
        $query .= "              ON T2.YEAR = L1.YEAR ";
        $query .= "             AND T2.PROV_SEMESTER    = L1.SEMESTER ";
        $query .= "     LEFT JOIN ".$model->Properties["useTestCountflg"]." L2 ";
        $query .= "              ON T2.YEAR = L2.YEAR ";
        if ($model->Properties["useTestCountflg"] != "TESTITEM_MST_COUNTFLG") {
            $query .= "             AND T2.PROV_SEMESTER    = L2.SEMESTER ";
        }
        $query .= "             AND T2.PROV_TESTKINDCD  = L2.TESTKINDCD ";
        $query .= "             AND T2.PROV_TESTITEMCD  = L2.TESTITEMCD ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "             AND T2.PROV_SCORE_DIV  = L2.SCORE_DIV ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     PROV_SEMESTER, ";
        $query .= "     PROV_TESTKINDCD, ";
        $query .= "     PROV_TESTITEMCD, ";
        $query .= "     PROV_SCORE_DIV ";

        return $query;
    }

    //評定有の件数チェック
    function getValuationCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_STUDYREC_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.SCHOOLCD = '0' AND ";
        $query .= "     T2.VALUATION IS NOT NULL ";

        return $query;
    }

    //生徒一覧
    function getList($model, $schregno="") {
        $query  = " SELECT ";
        if ($model->Properties["KNJXEXP_SEARCH"] == "SCHREGNO_NORMAL") {
            $query .= "     T2.SCHREGNO || '-0-0-' || T2.SCHREGNO AS VALUE, ";
        } else {
            $query .= "     T2.GRADE || '-' || T2.HR_CLASS || '-' || T2.ATTENDNO || '-' || T2.SCHREGNO AS VALUE, ";
        }
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR     = T2.YEAR ";
        $query .= "                                  AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "                                  AND T3.GRADE    = T2.GRADE ";
        $query .= "                                  AND T3.HR_CLASS = T2.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' AND ";
        if ($schregno) {
            $query .= "     T1.SCHREGNO IN ('".implode("','", $schregno)."') ";
        } else {
            $query .= "     T2.GRADE || T2.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //最高年次
    function getinfo_max_grade($schregno) {
        $query  = " SELECT ";
        $query .= "     MAX(CASE WHEN W1.ANNUAL IS NULL THEN W2.ANNUAL ELSE W1.ANNUAL END) AS ANNUAL_MAX ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         ANNUAL ";
        $query .= "      FROM ";
        $query .= "         SCHREG_REGD_DAT W1 ";
        $query .= "      WHERE ";
        $query .= "         W1.SCHREGNO  = '".$schregno."' AND ";
        $query .= "         W1.YEAR     <= '".CTRL_YEAR."' ";
        $query .= "     ) W1 ";
        $query .= "     FULL JOIN ( SELECT ";
        $query .= "                     SCHREGNO, ";
        $query .= "                     ANNUAL ";
        $query .= "                  FROM ";
        $query .= "                     GRD_REGD_DAT W1 ";
        $query .= "                  WHERE ";
        $query .= "                     W1.SCHREGNO  = '".$schregno."' AND ";
        $query .= "                     W1.YEAR     <= '".CTRL_YEAR."' ) W2 ON W2.SCHREGNO = W1.SCHREGNO ";

        return $query;
    }

    //学習記録データ（進学用）    //2004/04/01 nakamoto 教科コード仕様の変更に伴う修正
    function getinfo_ps1_1($model, $schregno,$hyoutei,$model,$schooldiv)
    {
        $subject_D = "01";            //教科コード
        $subject_U = "89";            //教科コード
        $subject_T = "90";            //総合的な学習の時間

        //    評定１を２と判定
        $h_1_2 = "";
        $h_1_3 = "";

        $chkProvFlg = "1" == $model->Properties["useProvFlg"] ? " PROV_FLG = '1' AND " : "";
        if( $hyoutei=="on" ) {                            //----->評定読み替えのON/OFF
            $h_1_2 = "CASE WHEN ".$chkProvFlg." VALUE(T1.GRADES,0) = 1 THEN 2 ELSE T1.GRADES END";

            $h_1_3 = "(CASE WHEN ".$chkProvFlg." GRADES = 1 AND GET_CREDIT=0 THEN ";
            $h_1_3 .= " (CASE WHEN COMP_CREDIT <> 0 THEN COMP_CREDIT ELSE CREDIT_MST_CREDIT END) ";
            $h_1_3 .= " ELSE GET_CREDIT END) ";
            $h_1_3 .= " + ADD_CREDIT ";
        } else {
            $h_1_2 = "T1.GRADES";

            $h_1_3 = "CREDIT";
        }

        $query .= " WITH";
        if ($schooldiv == "0") {
            $query .= " DROP_YEAR AS(";
            $query .= "        SELECT DISTINCT YEAR";
            $query .= "        FROM SCHREG_REGD_DAT T1";
            $query .= "        WHERE SCHREGNO = '".$schregno."' ";
            $query .= "        AND T1.YEAR NOT IN (SELECT MAX(YEAR) FROM SCHREG_REGD_DAT T2 WHERE SCHREGNO = '".$schregno."' AND YEAR <= '".CTRL_YEAR."' GROUP BY GRADE)";
            $query .= " ),";
        }
        $query .= " MAX_SEMESTER0 AS (SELECT YEAR, SCHREGNO, MAX(SEMESTER) AS SEMESTER ";
        $query .= "   FROM SCHREG_REGD_DAT WHERE SCHREGNO = '".$schregno."' ";
        $query .= "   GROUP BY YEAR, SCHREGNO ";
        $query .= " ), ";

        $query .= " STUDYREC_DAT AS(";
        $query .= "        SELECT  T1.SCHREGNO";
        $query .= "              , CASE WHEN INT(T1.YEAR) = 0 THEN '0' ELSE T1.YEAR END AS YEAR";
        $query .= "              , CASE WHEN INT(T1.ANNUAL) = 0 THEN '0' ELSE T1.ANNUAL END AS ANNUAL";
        $query .= "              , T1.SCHOOLCD";
        $query .= "              , T1.VALUATION AS GRADES";
        $query .= "              , CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0)";
        $query .= "                     ELSE T1.GET_CREDIT END AS CREDIT";
        $query .= "              , VALUE(T1.COMP_CREDIT,0) AS COMP_CREDIT";
        $query .= "              , VALUE(T1.GET_CREDIT,0) AS GET_CREDIT";
        $query .= "              , VALUE(T1.ADD_CREDIT,0) AS ADD_CREDIT";
        $query .= "              , T1.CLASSCD, T1.SUBCLASSCD";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "              , T1.SCHOOL_KIND";
            $query .= "              , T1.CURRICULUM_CD";
        }
        $query .= "              , T1.CLASSNAME, T1.SUBCLASSNAME";
        $query .= "              , T4.CREDITS AS CREDIT_MST_CREDIT ";
        $query .= "        FROM  SCHREG_STUDYREC_DAT T1";
        $query .= "        LEFT JOIN MAX_SEMESTER0 T2 ON T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "        LEFT JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR ";
        $query .= "           AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "           AND T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "        LEFT JOIN CREDIT_MST T4 ON T4.YEAR = T3.YEAR ";
        $query .= "           AND T4.CLASSCD = T1.CLASSCD ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "           AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "           AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "           AND T4.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "           AND T4.COURSECD = T3.COURSECD ";
        $query .= "           AND T4.MAJORCD = T3.MAJORCD ";
        $query .= "           AND T4.COURSECODE = T3.COURSECODE ";
        $query .= "           AND T4.GRADE = T3.GRADE ";
        $query .= "        WHERE  T1.SCHREGNO = '".$schregno."' ";
        $query .= "           AND T1.YEAR <= '".CTRL_YEAR."' ";
        $query .= "       AND (T1.CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' OR T1.CLASSCD = '".$subject_T."')";
        if ("1" == $model->Properties["tyousasyoNotPrintAnotherStudyrec"]) {
            $query .= "       AND T1.SCHOOLCD <> '1' ";
        }
        if ($schooldiv == "0") {
            $query .= "       AND T1.YEAR NOT IN(SELECT YEAR FROM DROP_YEAR)";
        }
        $query .= "           AND value(T1.PRINT_FLG, '0') NOT IN('1')";//印刷有無フラグが１のレコードは印刷しない。
        $query .= " )";

        $query .= ",SUBCLASSGROUP AS(";
        $query .= "        SELECT ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "                T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD,";
        }
        $query .= "                T1.SUBCLASSCD, T2.SUBCLASSCD2";
        $query .= "              , T4.CLASSNAME, T4.CLASSORDERNAME1";
        $query .= "              , T4.SHOWORDER2 AS SHOWORDERCLASS";
        $query .= "              , T3.SUBCLASSNAME, T3.SUBCLASSORDERNAME1";
        $query .= "              , T3.SHOWORDER2 AS SHOWORDERSUBCLASS";
        $query .= "        FROM ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "        (SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM STUDYREC_DAT GROUP BY CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD) T1";
        } else {
            $query .= "        (SELECT SUBCLASSCD FROM STUDYREC_DAT GROUP BY SUBCLASSCD) T1";
        }
        $query .= "              , SUBCLASS_MST T2, SUBCLASS_MST T3, CLASS_MST T4";
        $query .= "        WHERE  T1.SUBCLASSCD = T2.SUBCLASSCD";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "           AND T1.SCHOOL_KIND = T2.SCHOOL_KIND";
            $query .= "           AND T1.CURRICULUM_CD = T2.CURRICULUM_CD";
            $query .= "           AND T1.CLASSCD = T2.CLASSCD";
        }
        $query .= "           AND T2.SUBCLASSCD2 IS NOT NULL";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "           AND T2.SCHOOL_KIND = T3.SCHOOL_KIND";
            $query .= "           AND T2.CURRICULUM_CD = T3.CURRICULUM_CD";
            $query .= "           AND T2.CLASSCD = T3.CLASSCD";
        }
        $query .= "           AND T2.SUBCLASSCD2 = T3.SUBCLASSCD";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "           AND T2.SCHOOL_KIND = T4.SCHOOL_KIND";
            $query .= "           AND T2.CLASSCD = T4.CLASSCD";
        } else {
            $query .= "           AND SUBSTR(T2.SUBCLASSCD,1,2) = T4.CLASSCD";
        }
        $query .= " )";
        
        $query .= ",STUDYREC_SUBCLASSGROUP AS(";
        $query .= "        SELECT ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "    T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD,";
        }
        $query .= "        T2.SUBCLASSCD";
        $query .= "              , MIN(T2.CLASSNAME) AS CLASSNAME";
        $query .= "              , MIN(T2.SUBCLASSNAME) AS SUBCLASSNAME";
        $query .= "        FROM  SUBCLASSGROUP T1, STUDYREC_DAT T2";
        $query .= "        WHERE  T1.SUBCLASSCD2 = T2.SUBCLASSCD";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "        AND T1.CLASSCD = T2.CLASSCD";
            $query .= "        AND T1.SCHOOL_KIND = T2.SCHOOL_KIND";
            $query .= "        AND T1.CURRICULUM_CD = T2.CURRICULUM_CD";
        }
        $query .= "           AND (T2.SUBCLASSNAME IS NOT NULL OR T2.CLASSNAME IS NOT NULL)";
        $query .= "        GROUP BY ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "        T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ";
        }
        $query .= "        T2.SUBCLASSCD";
        $query .= " )";
        
        $query .= ",STUDYREC AS(";
        $query .= "        SELECT  T1.SCHREGNO, T1.YEAR, T1.ANNUAL, T1.SCHOOLCD";
        $query .= "              , T1.GRADES, T1.CREDIT, T1.COMP_CREDIT, T1.GET_CREDIT, T1.ADD_CREDIT";
        $query .= "              , T1.CLASSCD";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "          , T1.SCHOOL_KIND, T1.CURRICULUM_CD ";
        }
        $query .= "              , T1.SUBCLASSCD AS STUDYREC_SUBCLASSCD ";
        $query .= "              , CASE WHEN T2.SUBCLASSCD2 IS NOT NULL THEN T2.SUBCLASSCD2";
        $query .= "                     ELSE T1.SUBCLASSCD END AS SUBCLASSCD";
        $query .= "              , CASE WHEN T3.CLASSNAME IS NOT NULL THEN T3.CLASSNAME";
        $query .= "                     WHEN T2.CLASSORDERNAME1 IS NOT NULL THEN T2.CLASSORDERNAME1";
        $query .= "                     WHEN T2.CLASSNAME IS NOT NULL THEN T2.CLASSNAME";
        $query .= "                     WHEN T1.CLASSNAME IS NOT NULL THEN T1.CLASSNAME ";
        $query .= "                     WHEN T4.CLASSORDERNAME1 IS NOT NULL THEN T4.CLASSORDERNAME1";
        $query .= "                     ELSE T4.CLASSNAME END AS CLASSNAME";
        $query .= "              , CASE WHEN T3.SUBCLASSNAME IS NOT NULL THEN T3.SUBCLASSNAME";
        $query .= "                     WHEN T2.SUBCLASSORDERNAME1 IS NOT NULL THEN T2.SUBCLASSORDERNAME1";
        $query .= "                     WHEN T2.SUBCLASSNAME IS NOT NULL THEN T2.SUBCLASSNAME";
        $query .= "                     WHEN T1.SUBCLASSNAME IS NOT NULL THEN T1.SUBCLASSNAME";
        $query .= "                     WHEN T5.SUBCLASSORDERNAME1 IS NOT NULL THEN T5.SUBCLASSORDERNAME1";
        $query .= "                     ELSE T5.SUBCLASSNAME END AS SUBCLASSNAME";
        $query .= "              , CASE WHEN T2.SHOWORDERCLASS IS NOT NULL THEN T2.SHOWORDERCLASS";
        $query .= "                     WHEN T4.SHOWORDER2 IS NOT NULL THEN T4.SHOWORDER2";
        $query .= "                     ELSE 999 END AS SHOWORDERCLASS";
        $query .= "              , CASE WHEN T2.SHOWORDERSUBCLASS IS NOT NULL THEN T2.SHOWORDERSUBCLASS";
        $query .= "                     WHEN T5.SHOWORDER2 IS NOT NULL THEN T5.SHOWORDER2";
        $query .= "                     ELSE 999 END AS SHOWORDERSUBCLASS";
        $query .= "              , SC.GVAL_CALC";
        $query .= "              , T4.SPECIALDIV";
        $query .= "              , T1.CREDIT_MST_CREDIT";
        if ("1" == $model->Properties["useProvFlg"]) {
            $query .= "              , PRV.PROV_FLG";
        } else {
            $query .= "              , CAST(NULL AS VARCHAR(1)) AS PROV_FLG ";
        }
        $query .= "        FROM  STUDYREC_DAT T1";
        $query .= "        LEFT JOIN SUBCLASSGROUP T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD";
        $query .= "        LEFT JOIN STUDYREC_SUBCLASSGROUP T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD2";
        $query .= "        LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = T1.CLASSCD";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "          AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= "        LEFT JOIN SUBCLASS_MST T5 ON ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "          T5.CLASSCD = T1.CLASSCD AND ";
            $query .= "          T5.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
            $query .= "          T5.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "              T5.SUBCLASSCD = T1.SUBCLASSCD";
        if ("1" == $model->Properties["useProvFlg"]) {
            $query .= "        LEFT JOIN STUDYREC_PROV_FLG_DAT PRV ON PRV.SCHOOLCD = T1.SCHOOLCD ";
            $query .= "          AND PRV.YEAR = T1.YEAR ";
            $query .= "          AND PRV.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND PRV.CLASSCD = T1.CLASSCD ";
            if ("1" == $model->Properties["useCurriculumcd"]) {
                $query .= "          AND PRV.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "          AND PRV.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "          AND PRV.SUBCLASSCD = T1.SUBCLASSCD ";
        }
        $query .= "        LEFT JOIN SCHOOL_MST SC ON SC.YEAR = T1.YEAR";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND SC.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        $query .= "    WHERE  (";
        $query .= "        T1.SCHOOLCD = '0'";
        $query .= "     OR (T1.SCHOOLCD = '2' AND T1.CREDIT IS NOT NULL)";
        $query .= "     OR (T1.SCHOOLCD = '1' OR T1.YEAR = '0')";
        $query .=          ")";
        $query .= " ), MAIN_T AS ( ";
        // 該当生徒の科目評定、修得単位及び教科評定平均
        $query .= "SELECT ";
        $query .= "       T1.YEAR";
        $query .= "     , T1.ANNUAL";
        $query .= "     , T1.CLASSCD";
        $query .= "     , T1.CLASSNAME";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "   , T1.SCHOOL_KIND ";
            $query .= "   , T1.CURRICULUM_CD ";
        }
        $query .= "     , T1.SUBCLASSCD";
        $query .= "     , T1.SUBCLASSNAME";
        $query .= "     , ".$h_1_2." AS GRADES";
        $query .= "     , T5.AVG_GRADES";
        $query .= "     , '' AS ASSESS_LEVEL";
        $query .= "     , T1.CREDIT AS GRADE_CREDIT";
        $query .= "     , T4.CREDIT AS CREDIT ";
        $query .= "     , T4.COMP_CREDIT";
        $query .= "     , T1.SCHOOLCD";
        $query .= "     , T1.SHOWORDERCLASS";
        $query .= "     , T1.SHOWORDERSUBCLASS";
        $query .= " FROM (";
        // 同一年度同一科目の場合単位は合計とします。
        //「0:平均」「1:重み付け」は「評定がNULL／ゼロ以外」
        $gradesCase = "case when 0 < GRADES then GRADES end";
        $creditCase = "case when 0 < GRADES then CREDIT end";
        $query .= "  SELECT CLASSCD ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "   , SCHOOL_KIND ";
            $query .= "   , CURRICULUM_CD ";
        }
        $query .= "    , SUBCLASSCD ";
        $query .= "    , YEAR ";
        $query .= "    , ANNUAL ";
        $query .= "    , GVAL_CALC";
        $query .= "    , MIN(CLASSNAME) AS CLASSNAME";
        $query .= "    , MIN(SUBCLASSNAME) AS SUBCLASSNAME";
        $query .= "    , case when COUNT(*) = 1 then MAX(GRADES)";//１レコードの場合、評定はそのままの値。
        $query .= "           when GVAL_CALC = '0' then ROUND(AVG(FLOAT(".$gradesCase.")),0)";
        $query .= "           when GVAL_CALC = '1' and 0 < SUM(".$creditCase.") then ROUND(FLOAT(SUM((".$gradesCase.")*CREDIT))/SUM(".$creditCase."),0)";
        $query .= "           else MAX(GRADES) end AS GRADES";
        $query .= "    , SUM(CREDIT) AS CREDIT";
        $query .= "    , MIN(VALUE(PROV_FLG, '0')) AS PROV_FLG";
        $query .= "    , MIN(SCHOOLCD) AS SCHOOLCD";
        $query .= "    , MIN(SHOWORDERCLASS) AS SHOWORDERCLASS";
        $query .= "    , MIN(SHOWORDERSUBCLASS) AS SHOWORDERSUBCLASS";
        $query .= "      FROM STUDYREC";
        $query .= "  GROUP BY CLASSCD ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "   , SCHOOL_KIND ";
            $query .= "   , CURRICULUM_CD ";
        }
        $query .= "       ,SUBCLASSCD ";
        $query .= "  ,YEAR ";
        $query .= "  ,ANNUAL ";
        $query .= "  , GVAL_CALC";
        $query .= "     ) T1";

        //  修得単位数の計
        $query .= " INNER JOIN (";
        $query .= "    SELECT  CLASSCD ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "   , SCHOOL_KIND ";
            $query .= "   , CURRICULUM_CD ";
        }
        $query .= "            , SUBCLASSCD, SUM(".$h_1_3.") AS CREDIT, SUM(T1.COMP_CREDIT) AS COMP_CREDIT, MAX(CREDIT_MST_CREDIT) AS CREDIT_MST_CREDIT ";
        $query .= "    FROM STUDYREC T1";
        $query .= "    WHERE CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."'";
        $query .= "    GROUP BY CLASSCD ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "   , SCHOOL_KIND ";
            $query .= "   , CURRICULUM_CD ";
        }
        $query .= "           ,SUBCLASSCD";
        $query .= " ) T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD AND T4.CLASSCD = T1.CLASSCD ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "   AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "   AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }

        //  各教科の評定平均値
        $query .= " LEFT JOIN (";
        $query .= "    SELECT  CLASSCD";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "   , SCHOOL_KIND ";
        }
        $query .= "          , DECIMAL(ROUND(DECIMAL(AVG(FLOAT(".$h_1_2.")),5,2),1),5,1) AS AVG_GRADES";
        $query .= " FROM(";
        // 同一年度同一科目の場合評価はどちらか一方とします。
        $query .= "  SELECT CLASSCD, SUBCLASSCD ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "   , SCHOOL_KIND ";
            $query .= "   , CURRICULUM_CD ";
        }
        $query .= "  , YEAR ";
        $query .= "  , ANNUAL ";
        $query .= "  , GVAL_CALC";
        $query .= "           , case when COUNT(*) = 1 then MAX(GRADES)"; //１レコードの場合、評定はそのままの値。
        $query .= "                  when GVAL_CALC = '0' then ROUND(AVG(FLOAT(".$gradesCase.")),0)";
        $query .= "                  when GVAL_CALC = '1' and 0 < SUM(".$creditCase.") then ROUND(FLOAT(SUM((".$gradesCase.") * CREDIT)) / SUM(".$creditCase."),0)";
        $query .= "                  else MAX(GRADES) end AS GRADES";
        $query .= "  , MAX(PROV_FLG) AS PROV_FLG";
        $query .= "      FROM STUDYREC";
        $query .= "  GROUP BY CLASSCD,SUBCLASSCD";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "   , SCHOOL_KIND ";
            $query .= "   , CURRICULUM_CD ";
        }
        $query .= "  ,YEAR";
        $query .= "  ,ANNUAL";
        $query .= "  , GVAL_CALC";
        $query .= "     ) T1";
        $query .= "    WHERE   CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."'";
        $query .= "        AND GRADES <> 0 ";
        $query .= "        AND NOT EXISTS ( ";
        $query .= "              SELECT ";
        $query .= "                'x' ";
        $query .= "              FROM ";
        $query .= "                NAME_MST E1 ";
        $query .= "              WHERE ";
        $query .= "                E1.NAMECD1 = 'D020' ";
        $query .= "                AND E1.NAME1 = T1.SUBCLASSCD ";
        $query .= "          ) ";
        $query .= "    GROUP BY CLASSCD";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "   , SCHOOL_KIND ";
        }
        $query .= " ) T5 ON T5.CLASSCD = T1.CLASSCD";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "   AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " WHERE  T1.CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";

        // 総合学習の修得単位数（合計または学年別）
        $query .= " UNION SELECT ";
        $query .= "        YEAR";
        $query .= "      , ANNUAL";
        $query .= "      , '".$subject_T."' AS CLASSCD";
        $query .= "      , 'sogo' AS CLASSNAME";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "  , '".$subject_T."' AS SCHOOL_KIND";
            $query .= "  , '".$subject_T."' AS CURRICULUM_CD";
        }
        $query .= "      , '".$subject_T."01' AS SUBCLASSCD";
        $query .= "      , 'sogo' AS SUBCLASSNAME";
        $query .= "      , 0 AS GRADES";
        $query .= "      , 0 AS AVG_GRADES";
        $query .= "      , '' AS ASSESS_LEVEL";
        $query .= "      , 0 AS GRADE_CREDIT";
        $query .= "      , SUM(CREDIT) AS CREDIT";
        $query .= "      , SUM(COMP_CREDIT) AS COMP_CREDIT";
        $query .= "      , '0' AS SCHOOLCD ";
        $query .= "      , 0 AS SHOWORDERCLASS ";
        $query .= "      , 0 AS SHOWORDERSUBCLASS ";
        $query .= " FROM   STUDYREC T1";
        $query .= " WHERE  T1.CLASSCD = '".$subject_T."'";
        $query .= " GROUP BY YEAR, ANNUAL ";
        
        // 留学中の修得単位数（学年別）
        $query .= "         UNION SELECT ";
        $query .= "              '0' AS YEAR";
        $query .= "            , '0' AS ANNUAL";
        $query .= "            , 'AA' AS CLASSCD";
        $query .= "            , 'abroad' AS CLASSNAME";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "  , 'abroad' AS SCHOOL_KIND";
            $query .= "  , 'abroad' AS CURRICULUM_CD";
        }
        $query .= "            , 'AAAA' AS SUBCLASSCD";
        $query .= "            , 'abroad' AS SUBCLASSNAME";
        $query .= "            , 0 AS GRADES,0 AS AVG_GRADES";
        $query .= "            , '' AS ASSESS_LEVEL";
        $query .= "            , 0 AS GRADE_CREDIT";
        $query .= "            , SUM(ABROAD_CREDITS) AS CREDIT ";
        $query .= "            , SUM(ABROAD_CREDITS) AS COMP_CREDIT ";
        $query .= "            , '0' AS SCHOOLCD ";
        $query .= "            , 0 AS SHOWORDERCLASS ";
        $query .= "            , 0 AS SHOWORDERSUBCLASS ";
        $query .= "         FROM(";
        $query .= "              SELECT ABROAD_CREDITS, INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ";
        $query .= "              FROM SCHREG_TRANSFER_DAT ";
        $query .= "              WHERE SCHREGNO = '".$schregno."' AND TRANSFERCD = '1' ";
        $query .= "         )ST1";
        $query .= "     WHERE TRANSFER_YEAR <= ".CTRL_YEAR." ";

        // 全体の評定平均値
        $query .= " UNION SELECT ";
        $query .= "     '0' AS YEAR, '0' AS ANNUAL,'ZZ' AS CLASSCD,'total' AS CLASSNAME, ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "  'total' AS SCHOOL_KIND, ";
            $query .= "  'total' AS CURRICULUM_CD, ";
        }
        $query .= "     'ZZZZ' AS SUBCLASSCD,";
        $query .= "     CASE VALUE(MAX(T2.COMMENTEX_A_CD), '0') WHEN '1' THEN '○' ELSE '  ' END AS SUBCLASSNAME,";
        $query .= "     0 AS GRADES,";
        
        $query .= "     ROUND(DECIMAL(AVG(FLOAT(".$h_1_2.")),5,2),1) AS AVG_GRADES,";
        if ($model->Properties["useAssessCourseMst"] == '1') {
            $query .= "     (SELECT    ST2.ASSESSMARK ";
            $query .= "      FROM      ASSESS_COURSE_MST ST2 ";
            $query .= "                INNER JOIN SCHREG_REGD_DAT ST3 ";
            $query .= "                                     ON ST3.YEAR         = '".CTRL_YEAR."' ";
            $query .= "                                    AND ST3.SEMESTER     = '".CTRL_SEMESTER."' ";
            $query .= "                                    AND ST3.SCHREGNO     = '".$schregno."' ";
            $query .= "                                    AND ST3.COURSECD     = ST2.COURSECD ";
            $query .= "                                    AND ST3.MAJORCD      = ST2.MAJORCD ";
            $query .= "                                    AND ST3.COURSECODE   = ST2.COURSECODE ";
            $query .= "      WHERE     ST2.ASSESSCD='4' ";
            $query .= "                 AND DECIMAL(ROUND(DECIMAL(AVG(FLOAT(".$h_1_2.")),5,2),1),5,1) ";
            $query .= "                         BETWEEN ST2.ASSESSLOW AND ST2.ASSESSHIGH) AS ASSESS_LEVEL,";
        } else {
            $query .= "     (SELECT    ST2.ASSESSMARK ";
            $query .= "      FROM      ASSESS_MST ST2 ";
            $query .= "      WHERE     ST2.ASSESSCD='4' ";
            $query .= "                 AND DECIMAL(ROUND(DECIMAL(AVG(FLOAT(".$h_1_2.")),5,2),1),5,1) ";
            $query .= "                         BETWEEN ST2.ASSESSLOW AND ST2.ASSESSHIGH) AS ASSESS_LEVEL,";
        }
        $query .= "     0 AS GRADE_CREDIT,";
        $query .= "     0 AS CREDIT ";
        $query .= "    ,0 AS COMP_CREDIT";
        $query .= "    ,'0' AS SCHOOLCD ";
        $query .= "    ,0 AS SHOWORDERCLASS ";  // 表示順教科
        $query .= "    ,0 AS SHOWORDERSUBCLASS ";  // 表示順科目
        $query .= " FROM(";
        $query .= "  SELECT SCHREGNO, CLASSCD ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "  , SCHOOL_KIND";
            $query .= "  , CURRICULUM_CD";
        }
        $query .= "       , SUBCLASSCD ";
        $query .= "       , YEAR ";
        $query .= "       , ANNUAL ";
        $query .= "       , GVAL_CALC";
        $query .= "       , case when COUNT(*) = 1 then MAX(GRADES)";//１レコードの場合、評定はそのままの値。
        $query .= "              when GVAL_CALC = '0' then ROUND(AVG(FLOAT(".$gradesCase.")),0)";
        $query .= "              when GVAL_CALC = '1' and 0 < SUM(".$creditCase.") then ROUND(FLOAT(SUM((".$gradesCase.") * CREDIT)) / SUM(".$creditCase."), 0)";
        $query .= "              else MAX(GRADES) end AS GRADES";
        $query .= "       , MIN(VALUE(PROV_FLG, '0')) AS PROV_FLG ";
        $query .= "      FROM STUDYREC";
        $query .= "      WHERE ";
        $query .= "          GRADES <> 0 ";
        $query .= "  GROUP BY SCHREGNO,CLASSCD ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "  , SCHOOL_KIND";
            $query .= "  , CURRICULUM_CD";
        }
        $query .= "  , SUBCLASSCD ";
        $query .= "  , YEAR ";
        $query .= "  , ANNUAL ";
        $query .= "  , GVAL_CALC";
        $query .= "     ) T1";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_HDAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
        
        // 全体の修得単位数
        $query .= " UNION SELECT ";
        $query .= "     '0' AS YEAR, '0' AS ANNUAL,'XX' AS CLASSCD,'totalCredit' AS CLASSNAME, ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "  'totalCredit' AS SCHOOL_KIND, ";
            $query .= "  'totalCredit' AS CURRICULUM_CD, ";
        }
        $query .= "     'XXXX' AS SUBCLASSCD,";
        $query .= "     CASE VALUE(MAX(T2.COMMENTEX_A_CD),'0') WHEN '1' THEN '○' ELSE '  ' END AS SUBCLASSNAME,";
        $query .= "         0 AS GRADES,";
        $query .= "         0 AS AVG_GRADES,";
        $query .= "         '' AS ASSESS_LEVEL,";
        $query .= "         0 AS GRADE_CREDIT,";
        $query .= "         SUM(T1.CREDIT) AS CREDIT ";
        $query .= "      , SUM(T1.COMP_CREDIT) AS COMP_CREDIT";
        $query .= "       ,'0' AS SCHOOLCD ";
        $query .= "        ,0 AS SHOWORDERCLASS ";  // 表示順教科
        $query .= "        ,0 AS SHOWORDERSUBCLASS ";  // 表示順科目
        $query .= " FROM (";
        $query .= "  SELECT SCHREGNO, CLASSCD ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "  , SCHOOL_KIND";
            $query .= "  , CURRICULUM_CD";
        }
        $query .= "  , SUBCLASSCD ";
        $query .= "  , YEAR";
        $query .= "  , ANNUAL";
        $query .= "  , GVAL_CALC";
        $query .= "  , case when COUNT(*) = 1 then MAX(GRADES)";//１レコードの場合、評定はそのままの値。
        $query .= "         when GVAL_CALC = '0' then ROUND(AVG(FLOAT(".$gradesCase.")),0)";
        $query .= "         when GVAL_CALC = '1' and 0 < SUM(".$creditCase.") then ROUND(FLOAT(SUM((".$gradesCase.") * CREDIT)) / SUM(".$creditCase."), 0)";
        $query .= "         else MAX(GRADES) end AS GRADES";
        $query .= "  , SUM(".$h_1_3.") AS CREDIT";
        $query .= "  , SUM(COMP_CREDIT) AS COMP_CREDIT";
        $query .= "      FROM STUDYREC";
        $query .= "  GROUP BY SCHREGNO, CLASSCD ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "  , SCHOOL_KIND";
            $query .= "  , CURRICULUM_CD";
        }
        $query .= "      , SUBCLASSCD ";
        $query .= "      , YEAR ";
        $query .= "      , ANNUAL ";
        $query .= "      , GVAL_CALC";
        $query .= "     ) T1";
        $query .= " LEFT JOIN HEXAM_ENTREMARK_HDAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
        $query .= " ) ";

        $query .= " SELECT";
        $query .= "    T1.ANNUAL, ";
        $query .= "    T1.CLASSCD, ";
        $query .= "    T1.CLASSNAME, ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "  T1.SCHOOL_KIND, ";
            $query .= "  T1.CURRICULUM_CD, ";
        }
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    T1.SUBCLASSNAME, ";
        $query .= "    T1.GRADES, ";
        $query .= "    T1.AVG_GRADES, ";
        $query .= "    T1.ASSESS_LEVEL, ";
        $query .= "    T1.GRADE_CREDIT, ";
        $query .= "    T1.CREDIT, ";
        $query .= "    T1.COMP_CREDIT, ";
        $query .= "    T1.SCHOOLCD, ";
        $query .= "    T1.SHOWORDERCLASS, ";
        $query .= "    T1.SHOWORDERSUBCLASS, ";
        $query .= "    VALUE(L2.SPECIALDIV, '0') AS SPECIALDIV ";
        $query .= " FROM ";
        $query .= "    MAIN_T T1 ";
        $query .= "    LEFT JOIN CLASS_MST L2 ON L2.CLASSCD = T1.CLASSCD ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "  AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .= " ORDER BY ";
        $query .= "    VALUE(L2.SPECIALDIV, '0'), ";
        $query .= "    SHOWORDERCLASS, ";
        $query .= "    CLASSCD, ";
        $query .= "    SHOWORDERSUBCLASS, ";
        $query .= "    SUBCLASSCD, ";
        if ("1" == $model->Properties["useCurriculumcd"]) {
            $query .= "  SCHOOL_KIND, ";
            $query .= "  CURRICULUM_CD, ";
        }
        $query .= "    ANNUAL";

        return $query;
    }


    //学習記録データ（就職用）
    function getinfo_ps1_2($schregno,$hyoutei,$model)
    {
        $subject_D = "01";   //教科コード
        $subject_U = "89";   //教科コード
        $subject_T = "90";   //総合的な学習の時間

        //    評定１を２と判定
        $h_1_2 = "";
        $h_1_3 = "";

        $chkProvFlg = "1" == $model->Properties["useProvFlg"] ? " PROV_FLG = '1' AND " : "";
        if( $hyoutei=="on" ){                            //----->評定読み替えのON/OFF
            $h_1_2 = "CASE WHEN ".$chkProvFlg." VALUE(T1.GRADES,0) = 1 THEN 2 ELSE T1.GRADES END";

            $h_1_3 = "CASE WHEN ".$chkProvFlg." VALUE(T1.GRADES,0)=1 AND VALUE(T1.CREDIT,0)=0 THEN T1.ADD_CREDIT + L1.CREDITS ELSE T1.ADD_CREDIT + T1.CREDIT END";
        } else {
            $h_1_2 = "T1.GRADES";

            $h_1_3 = "T1.CREDIT + T1.ADD_CREDIT ";
        }

        //該当生徒の成績データ表
        $query .= " WITH SUB_SCHREGNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR, ";
        $query .= "         MAX(SEMESTER) AS SEMESTER ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         YEAR ";

        $query .= " ), SCHREGNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         TT1.SCHREGNO, ";
        $query .= "         TT1.YEAR, ";
        $query .= "         TT1.GRADE, ";
        $query .= "         TT1.COURSECD, ";
        $query .= "         TT1.MAJORCD, ";
        $query .= "         TT1.COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT TT1 ";
        $query .= "     INNER JOIN ";
        $query .= "         SUB_SCHREGNO LL1 ON  LL1.SCHREGNO = TT1.SCHREGNO ";
        $query .= "                          AND LL1.YEAR     = TT1.YEAR ";
        $query .= "                          AND LL1.SEMESTER = TT1.SEMESTER ";

        $query .= " ), STUDYREC AS (";
        $query .=        "SELECT ";
        $query .=            "T1.SCHREGNO, ";
        $query .=            "T1.YEAR, ";
        $query .=            "T1.ANNUAL, ";
        $query .=            "T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=        "T1.SCHOOL_KIND, ";
            $query .=        "T1.CURRICULUM_CD, ";
        }
        $query .=            "T1.SUBCLASSCD, ";
        $query .=            "T1.VALUATION AS GRADES,";
        $query .=            "T1.GET_CREDIT AS CREDIT, ";
        $query .=            "T1.ADD_CREDIT, ";
        $query .=            "L1.COURSECD, ";
        $query .=            "L1.MAJORCD, ";
        $query .=            "L1.COURSECODE, ";
        $query .=            "L1.GRADE ";
        if ("1" == $model->Properties["useProvFlg"]) {
            $query .= "              , PRV.PROV_FLG ";
        } else {
            $query .= "              , CAST(NULL AS VARCHAR(1)) AS PROV_FLG ";
        }
        $query .=        "FROM ";
        $query .=            "SCHREG_STUDYREC_DAT T1 ";
        $query .=       " LEFT JOIN ";
        $query .=            "SCHREGNO L1 ON  L1.YEAR     = T1.YEAR ";
        $query .=            "            AND L1.SCHREGNO = T1.SCHREGNO ";
        if ("1" == $model->Properties["useProvFlg"]) {
            $query .= "        LEFT JOIN STUDYREC_PROV_FLG_DAT PRV ON PRV.SCHOOLCD = T1.SCHOOLCD ";
            $query .= "          AND PRV.YEAR = T1.YEAR ";
            $query .= "          AND PRV.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND PRV.CLASSCD = T1.CLASSCD ";
            if ("1" == $model->Properties["useCurriculumcd"]) {
                $query .= "          AND PRV.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "          AND PRV.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "          AND PRV.SUBCLASSCD = T1.SUBCLASSCD ";
        }
        $query .=        "WHERE ";
        $query .=            "T1.SCHREGNO = '{$schregno}' AND ";
        $query .=            "T1.YEAR    <= '" . CTRL_YEAR . "' AND ";
        $query .=            "(T1.CLASSCD BETWEEN '{$subject_D}' AND '{$subject_U}' OR T1.CLASSCD='{$subject_T}') ";
        $query .= " ) ";

        //該当生徒の科目評定、修得単位及び教科評定平均
        $query .=    "SELECT ";
        $query .=        "T1.ANNUAL,T1.CLASSCD,";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=    "T1.SCHOOL_KIND, ";
            $query .=    "T1.CURRICULUM_CD, ";
        }
        $query .=        "T2.CLASSNAME,";
        $query .=        "T1.SUBCLASSCD,";
        $query .=        "T3.SUBCLASSNAME,";
        $query .=        $h_1_2 . " AS GRADES,";
        $query .=        "T5.AVG_GRADES,'' AS ASSESS_LEVEL,";
        $query .=        "T1.CREDIT AS GRADE_CREDIT,T4.CREDIT ";
        $query .=    "FROM ";
        $query .=        "STUDYREC T1 ";
        $query .=        "INNER JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=                           "AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        }
        $query .=        "INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=                           "AND T3.CLASSCD = T1.CLASSCD ";
            $query .=                           "AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .=                           "AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        //    修得単位数の計
        $query .=        "INNER JOIN(SELECT ";
        $query .=                       "T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=                   "T1.SCHOOL_KIND, ";
            $query .=                   "T1.CURRICULUM_CD, ";
        }
        $query .=                       "T1.SUBCLASSCD, ";
        $query .=                       "SUM({$h_1_3}) AS CREDIT ";
        $query .=                   "FROM ";
        $query .=                       "STUDYREC T1 ";
        $query .=                   "LEFT JOIN ";
        $query .=                       "CREDIT_MST L1 ON  L1.YEAR = T1.YEAR ";
        $query .=                       "              AND L1.COURSECD = T1.COURSECD ";
        $query .=                       "              AND L1.MAJORCD = T1.MAJORCD ";
        $query .=                       "              AND L1.GRADE = T1.GRADE ";
        $query .=                       "              AND L1.COURSECODE = T1.COURSECODE ";
        $query .=                       "              AND L1.CLASSCD = T1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=                   "              AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .=                   "              AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .=                       "              AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .=                   "WHERE ";
        $query .=                       "T1.CLASSCD BETWEEN '{$subject_D}' AND '{$subject_U}' ";
        $query .=                   "GROUP BY ";
        $query .=                       "T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=                   "T1.SCHOOL_KIND, ";
            $query .=                   "T1.CURRICULUM_CD, ";
        }
        $query .=                       "T1.SUBCLASSCD ";
        $query .=        ")T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=       "AND T4.CLASSCD = T1.CLASSCD ";
            $query .=       "AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .=       "AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        //    各教科の評定平均値
        $query .=        "INNER JOIN(SELECT ";
        $query .=                "CLASSCD,";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=            "SCHOOL_KIND, ";
            $query .=            "CURRICULUM_CD, ";
        }
        $query .=                "DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" . $h_1_2 . ")),5,2),1),5,1) AS AVG_GRADES ";
        $query .=            "FROM ";
        $query .=                "STUDYREC T1 ";
        $query .=            "WHERE ";
        $query .=                "CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
        $query .=            "GROUP BY ";
        $query .=                "CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=            ",SCHOOL_KIND ";
            $query .=            ",CURRICULUM_CD ";
        }
        $query .=        ")T5 ON T5.CLASSCD = T1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=       "AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .=       "AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .=    "WHERE ";
        $query .=        "T1.CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";

        //    総合学習の修得単位数（学年別）
        $query .=    "UNION SELECT ";
        $query .=            "ANNUAL,'".$subject_T."' AS CLASSCD,";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=        "'H' AS SCHOOL_KIND, ";
            $query .=        "'2' AS CURRICULUM_CD, ";
        }
        $query .=            "'sogo' AS CLASSNAME,'".$subject_T."01' AS SUBCLASSCD,'sogo' AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT ";
        $query .=        "FROM ";
        $query .=            "STUDYREC ";
        $query .=        "WHERE ";
        $query .=            "CLASSCD = '".$subject_T."' ";
        $query .=        "GROUP BY ";
        $query .=            "ANNUAL ";
        //    総合学習の修得単位数（合計）
        $query .=    "UNION SELECT ";
        $query .=            "'0' AS ANNUAL,'".$subject_T."' AS CLASSCD,";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=        "'H' AS SCHOOL_KIND, ";
            $query .=        "'2' AS CURRICULUM_CD, ";
        }
        $query .=            "'sogo' AS CLASSNAME,'".$subject_T."01' AS SUBCLASSCD,'sogo' AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(CREDIT) AS CREDIT ";
        $query .=        "FROM ";
        $query .=            "STUDYREC ";
        $query .=        "WHERE ";
        $query .=            "CLASSCD = '".$subject_T."' ";

        //    留学中の修得単位数（学年別）
        $query .=        "UNION SELECT ";
        $query .=            "ANNUAL,'AA' AS CLASSCD,";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=        "'H' AS SCHOOL_KIND, ";
            $query .=        "'2' AS CURRICULUM_CD, ";
        }
        $query .=            "'abroad' AS CLASSNAME,'AAAA' AS SUBCLASSCD,'abroad' AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT ";
        $query .=        "FROM ";
        $query .=                "(SELECT ";
        $query .=                    "ABROAD_CREDITS,";
        $query .=                    "INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ";
        $query .=                "FROM ";
        $query .=                    "SCHREG_TRANSFER_DAT ";
        $query .=                "WHERE ";
        $query .=                    "SCHREGNO = '" . $schregno . "' AND TRANSFERCD = '1' ";
        $query .=                ")ST1,";
        $query .=                "(SELECT ";
        $query .=                    "ANNUAL,MAX(YEAR) AS YEAR ";
        $query .=                 "FROM ";
        $query .=                    "SCHREG_REGD_DAT ";
        $query .=                "WHERE ";
        $query .=                    "SCHREGNO = '" . $schregno . "' AND YEAR <= '" . CTRL_YEAR . "' ";
        $query .=                "GROUP BY ";
        $query .=                    "ANNUAL ";
        $query .=                ")ST2 ";
        $query .=        "WHERE ";
        $query .=            "ST1.TRANSFER_YEAR <= " . CTRL_YEAR . " ";
        $query .=            "and INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ";
        $query .=        "GROUP BY ";
        $query .=            "ANNUAL ";
        //    留学中の修得単位数（合計）
        $query .=        "UNION SELECT ";
        $query .=            "'0' AS ANNUAL,'AA' AS CLASSCD,";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=        "'H' AS SCHOOL_KIND, ";
            $query .=        "'2' AS CURRICULUM_CD, ";
        }
        $query .=            "'abroad' AS CLASSNAME,'AAAA' AS SUBCLASSCD,'abroad' AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT ";
        $query .=        "FROM ";
        $query .=            "(SELECT ";
        $query .=                "SCHREGNO,ABROAD_CREDITS,INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ";
        $query .=            "FROM  ";
        $query .=                "SCHREG_TRANSFER_DAT  ";
        $query .=            "WHERE  ";
        $query .=                "SCHREGNO = '" . $schregno . "' AND TRANSFERCD = '1' ";
        $query .=            ")ST1 ";
        $query .=        "WHERE ";
        $query .=            "TRANSFER_YEAR <= " . CTRL_YEAR . " ";

        //    修得単位数、評定平均（学年別）
        $query .=        "UNION SELECT ";
        $query .=            "ANNUAL,'ZZ' AS CLASSCD,";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=        "'H' AS SCHOOL_KIND, ";
            $query .=        "'2' AS CURRICULUM_CD, ";
        }
        $query .=            "'total' AS CLASSNAME,";
        $query .=            "'ZZZZ' AS SUBCLASSCD,'total' AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,";
        $query .=            "0 AS AVG_GRADES,";
        $query .=            "'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,";
        $query .=            "SUM({$h_1_3}) AS CREDIT ";
        $query .=        "FROM ";
        $query .=            "STUDYREC T1 ";
        $query .=         "LEFT JOIN ";
        $query .=             "CREDIT_MST L1 ON  L1.YEAR = T1.YEAR ";
        $query .=             "              AND L1.COURSECD = T1.COURSECD ";
        $query .=             "              AND L1.MAJORCD = T1.MAJORCD ";
        $query .=             "              AND L1.GRADE = T1.GRADE ";
        $query .=             "              AND L1.COURSECODE = T1.COURSECODE ";
        $query .=             "              AND L1.CLASSCD = T1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=         "              AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .=         "              AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .=             "              AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .=        "WHERE ";
        $query .=            "T1.CLASSCD BETWEEN '".$subject_D."' AND '".$subject_U."' ";
        $query .=        "GROUP BY ";
        $query .=            "T1.ANNUAL ";

        //    全体の修得単位数・全体の評定平均値
        $query .=        "UNION SELECT ";
        $query .=            "'0' AS ANNUAL,'ZZ' AS CLASSCD,";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=        "'H' AS SCHOOL_KIND, ";
            $query .=        "'2' AS CURRICULUM_CD, ";
        }
        $query .=            "'total' AS CLASSNAME,'ZZZZ' AS SUBCLASSCD,";
        $query .=            "CASE VALUE(MAX(T2.COMMENTEX_A_CD),'0') WHEN '1' THEN '○' ELSE '  ' END AS SUBCLASSNAME,";
        $query .=            "0 AS GRADES,";
        $query .=            "ROUND(DECIMAL(AVG(FLOAT({$h_1_2})),5,2),1) AS AVG_GRADES,";
        if ($model->Properties["useAssessCourseMst"] == '1') {
            $query .= "     (SELECT    ST2.ASSESSMARK ";
            $query .= "      FROM      ASSESS_COURSE_MST ST2 ";
            $query .= "                INNER JOIN SCHREG_REGD_DAT ST3 ";
            $query .= "                                     ON ST3.YEAR         = '".CTRL_YEAR."' ";
            $query .= "                                    AND ST3.SEMESTER     = '".CTRL_SEMESTER."' ";
            $query .= "                                    AND ST3.SCHREGNO     = '".$schregno."' ";
            $query .= "                                    AND ST3.COURSECD     = ST2.COURSECD ";
            $query .= "                                    AND ST3.MAJORCD      = ST2.MAJORCD ";
            $query .= "                                    AND ST3.COURSECODE   = ST2.COURSECODE ";
            $query .= "      WHERE     ST2.ASSESSCD='4' ";
            $query .= "                 AND DECIMAL(ROUND(DECIMAL(AVG(FLOAT({$h_1_2})),5,2),1),5,1) ";
            $query .= "                         BETWEEN ST2.ASSESSLOW AND ST2.ASSESSHIGH) AS ASSESS_LEVEL,";
        } else {
            $query .=            "(SELECT     ST2.ASSESSMARK ";
            $query .=             "FROM         ASSESS_MST ST2 ";
            $query .=             "WHERE     ST2.ASSESSCD='4' ";
            $query .=                        "AND DECIMAL(ROUND(DECIMAL(AVG(FLOAT({$h_1_2})),5,2),1),5,1) ";
            $query .=                                "BETWEEN ST2.ASSESSLOW AND ST2.ASSESSHIGH) AS ASSESS_LEVEL,";
        }
        $query .=            "0 AS GRADE_CREDIT,";
        $query .=            "SUM({$h_1_3}) AS CREDIT ";
        $query .=        "FROM ";
        $query .=            "STUDYREC T1 ";

        $query .=         "LEFT JOIN ";
        $query .=             "CREDIT_MST L1 ON  L1.YEAR = T1.YEAR ";
        $query .=             "              AND L1.COURSECD = T1.COURSECD ";
        $query .=             "              AND L1.MAJORCD = T1.MAJORCD ";
        $query .=             "              AND L1.GRADE = T1.GRADE ";
        $query .=             "              AND L1.COURSECODE = T1.COURSECODE ";
        $query .=             "              AND L1.CLASSCD = T1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=         "              AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .=         "              AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .=             "              AND L1.SUBCLASSCD = T1.SUBCLASSCD ";

        $query .=            "LEFT JOIN HEXAM_ENTREMARK_HDAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .=        "WHERE ";
        $query .=            "T1.CLASSCD BETWEEN '{$subject_D}' AND '{$subject_U}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .=    "ORDER BY 2,3,4,6,1";
        } else {
            $query .=    "ORDER BY 2,4,1";
        }

        return $query;
    }


    //出欠記録データ
    function getinfo_ps2($schregno, $schooldiv)
    {
        $query = "SELECT DISTINCT "
                    . "T1.YEAR,"
                    . "T1.ANNUAL,"
                    . "VALUE(CLASSDAYS,0) AS CLASSDAYS,"                           //授業日数
                    . "VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) AS ATTEND_1,"         //授業日数-休学日数:1
                    . "VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSP_MOUR,"         //出停・忌引
                    . "VALUE(SUSPEND,0) AS SUSPEND,"                               //出停:2
                    . "VALUE(MOURNING,0) AS MOURNING,"                             //忌引:3
                    . "VALUE(ABROAD,0) AS ABROAD,"                                 //留学:4
                    . "VALUE(REQUIREPRESENT,0) AS REQUIREPRESENT,"                 //要出席日数:5
                    . "VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) "
                                . "+ VALUE(NOACCIDENTNOTICE,0) AS ATTEND_6,"       //病欠＋事故欠（届・無）:6
                    . "VALUE(PRESENT,0) AS PRESENT,"                               //出席日数:7
                    . "VALUE(MOURNING,0) + VALUE(SUSPEND,0) AS ATTEND_8 "          //忌引＋出停:8
                . "FROM "
                    . "("
                        . "SELECT "
                            . "SCHREGNO,"
                            . "YEAR,"
                            . "ANNUAL,"
                            . "SUM(CLASSDAYS) AS CLASSDAYS,"
                            . "SUM(OFFDAYS) AS OFFDAYS,"
                            . "SUM(ABSENT) AS ABSENT,"
                            . "SUM(SUSPEND) AS SUSPEND,"
                            . "SUM(MOURNING) AS MOURNING,"
                            . "SUM(ABROAD) AS ABROAD,"
                            . "SUM(REQUIREPRESENT) AS REQUIREPRESENT,"
                            . "SUM(SICK) AS SICK,"
                            . "SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE,"
                            . "SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE,"
                            . "SUM(PRESENT) AS PRESENT "
                        . "FROM "
                            . "SCHREG_ATTENDREC_DAT "
                        . "WHERE "
                                . "SCHREGNO = '" . $schregno . "' "
                            . "AND YEAR <= '" . CTRL_YEAR . "' "
                        . "GROUP BY "
                            . "SCHREGNO,"
                            . "ANNUAL,"
                            . "YEAR "
                    . ")T1 ";
        if ($schooldiv == "0") {
            $query .= " INNER JOIN ( ";
            $query .= "   SELECT SCHREGNO, ANNUAL, MAX(YEAR) AS YEAR ";
            $query .= "   FROM  SCHREG_ATTENDREC_DAT ";
            $query .= "   WHERE SCHREGNO = '" .$schregno . "' ";
            $query .= "   GROUP BY SCHREGNO, ANNUAL ";
            $query .= " ) T3 ON T3.SCHREGNO = T1.SCHREGNO AND T3.YEAR = T1.YEAR AND T3.ANNUAL = T1.ANNUAL ";
        }
        $query .= ""
                . "ORDER BY "
                    . "T1.ANNUAL"
                    . ", T1.YEAR";

        return $query;
    }


    //健康診断データ
    function getinfo_ps3($schregno)
    {
        $query = "SELECT "
                . "T2.DATE,"
                . "T1.HEIGHT,"
                . "T1.WEIGHT,"
                . "T1.R_BAREVISION,"
                . "T1.L_BAREVISION,"
                . "T1.R_VISION,"
                . "T1.L_VISION,"
                . "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F010' AND NAMECD2 = R_EAR) AS R_EAR,"
                . "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F010' AND NAMECD2 = L_EAR) AS L_EAR,"
                . "(SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'F100' AND NAMECD2 = TB_REMARKCD) AS RB_REMARK "
            . "FROM "
                . "MEDEXAM_DET_DAT T1 "
                 . "INNER JOIN MEDEXAM_HDAT T2 ON T1.SCHREGNO = T2.SCHREGNO "
                                            . "AND T1.YEAR = T2.YEAR "
            . "WHERE  "
                    . "T1.SCHREGNO = '" . $schregno . "' "
                . "AND T1.YEAR <= '" . CTRL_YEAR . "' "
            . "ORDER BY "
                . "T1.YEAR DESC";

        return $query;
    }


    //所見データ（進学用）
    function getinfo_ps4_1($model, $schregno ,$schooldiv)
    {
        $query  = "SELECT ";
        $query .= "        W2.SCHREGNO, ";
        $query .= "        W1.COMMENTEX_A_CD, ";
        $query .= "        W1.DISEASE, ";
        $query .= "        W1.DOC_REMARK, ";
        $query .= "        W1.TR_REMARK, ";
        $query .= "        W1.TOTALSTUDYACT, ";
        $query .= "        W1.TOTALSTUDYVAL, ";
        $query .= "        W1.REMARK, ";
        $query .= "        W2.ANNUAL, ";
        $query .= "        W2.ATTENDREC_REMARK, ";
        $query .= "        W2.SPECIALACTREC, ";
        if ($model->field["useSyojikou3"] == "1") {
            $query .= "        W2.TRAIN_REF1 AS TRAIN_REF, ";
            $query .= "        W2.TRAIN_REF2, ";
            $query .= "        W2.TRAIN_REF3 ";
        } else {
            $query .= "        W2.TRAIN_REF ";
        }
        $query .= "FROM ";
        $query .= "        HEXAM_ENTREMARK_DAT W2 ";
        $query .= "        LEFT JOIN HEXAM_ENTREMARK_HDAT W1 ON W2.SCHREGNO = W1.SCHREGNO ";
        if ($schooldiv == "0") {
            $query .= " INNER JOIN ( ";
            $query .= "   SELECT SCHREGNO, ANNUAL, MAX(YEAR) AS YEAR ";
            $query .= "   FROM  HEXAM_ENTREMARK_DAT ";
            $query .= "   WHERE SCHREGNO = '" .$schregno . "' ";
            $query .= "   GROUP BY SCHREGNO, ANNUAL ";
            $query .= " ) W3 ON W3.SCHREGNO = W2.SCHREGNO AND W3.YEAR = W2.YEAR AND W3.ANNUAL = W2.ANNUAL ";
        }
        $query .= "WHERE ";
        $query .= "     W2.YEAR <= '" . CTRL_YEAR . "' AND ";
        $query .= "        W2.SCHREGNO = '" . $schregno . "' ";
        $query .= "ORDER BY ";
        $query .= "        W2.ANNUAL ";
        $query .= "        , W2.YEAR ";

        return $query;
    }


    //所見データ（就職用）
    function getinfo_ps4_2($schregno)
    {
        $query = "SELECT "
                . "W1.SCHREGNO,"
                . "JOBHUNT_REC,"
                . "JOBHUNT_RECOMMEND,"
                . "JOBHUNT_ABSENCE,"
                . "JOBHUNT_HEALTHREMARK "
            . "FROM "
                . "HEXAM_EMPREMARK_DAT W1 "
            . "WHERE "
                . "W1.SCHREGNO = '" . $schregno . "' ";

        return $query;
    }


    //成績概評人数データ
    function getinfo_ps5($schregno)
    {
        $query = "SELECT "
                    . "A_MEMBER AS MEMBER5,"
                    . "B_MEMBER AS MEMBER4,"
                    . "C_MEMBER AS MEMBER3,"
                    . "D_MEMBER AS MEMBER2,"
                    . "E_MEMBER AS MEMBER1,"
                    . "COURSE_MEMBER AS MEMBER0,"
                    . "GRADE_MEMBER AS MEMBER6 "
                . "FROM "
                    . "GENEVIEWMBR_DAT T1,"
                    . "SCHREG_REGD_DAT T2 "
                . "WHERE "
                        . "T2.SCHREGNO = '" . $schregno . "' "
                    . "AND T2.YEAR = '" . CTRL_YEAR . "' "
                    . "AND T2.SEMESTER = '" . CTRL_SEMESTER . "' "
                    . "AND T1.YEAR = T2.YEAR "
                    . "AND T1.GRADE = T2.GRADE "
                    . "AND T1.COURSECD = T2.COURSECD "
                    . "AND T1.MAJORCD = T2.MAJORCD "
                    . "AND VALUE(T1.COURSECODE,'0000') = VALUE(T2.COURSECODE,'0000')";

        return $query;
    }


    //個人データ
    function getinfo_ps6($model, $schregno)
    {
        $query  =    "SELECT ";
        $query .=        "T2.NAME,";
        $query .=        "T2.NAME_KANA,T2.BIRTHDAY,T7.ABBV1 AS SEX,";
        $query .=        "T1.GRADE,T1.ATTENDNO,T1.ANNUAL,T6.HR_NAME,";
            //課程・学科・コース
        $query .=        "T3.COURSENAME,T4.MAJORNAME,T5.COURSECODENAME,T3.COURSEABBV,T4.MAJORABBV,";
            //卒業
        $query .=    "CASE WHEN T2.GRD_DATE IS NULL THEN ";
        $query .=    "RTRIM(CHAR(INT(T1.YEAR)+case t1.annual when '01' then 3 when '02' then 2 else 1 end)) || '-' || ";
        $query .=    "RTRIM(CHAR(MONTH(T10.GRADUATE_DATE))) || '-01' ELSE VARCHAR(T2.GRD_DATE) END AS GRADU_DATE,";
        $query .=    "CASE WHEN T2.GRD_DATE IS NULL THEN '卒業見込み' ELSE ";
        $query .=        "(SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A003' ";
        $query .=            "AND T2.GRD_DIV = ST2.NAMECD2) END AS GRADU_NAME,";
            //入学
        $query .=        "T2.ENT_DATE,T2.ENT_DIV,";
        $query .=        "(SELECT MIN(ANNUAL) FROM SCHREG_REGD_DAT ST1 WHERE ST1.SCHREGNO=T1.SCHREGNO) AS ENTER_GRADE,";
        $query .=        "(SELECT NAME1 FROM NAME_MST ST2 WHERE ST2.NAMECD1 = 'A002' AND T2.ENT_DIV = ST2.NAMECD2) AS ENTER_NAME,";
            //住所
        $query .=        "VALUE(T8.ADDR1,'') || VALUE(T8.ADDR2,'') AS ADDR,";
        $query .=        "T8.ADDR1,T8.ADDR2,T8.TELNO,T8.ZIPCD,";
        $query .=        "T1.SCHREGNO ";
        $query .=    "FROM ";
            //学籍情報(??? or ????)
        $query .=        "(     SELECT        * ";
        $query .=            "FROM         SCHREG_REGD_DAT T1 ";
        $query .=            "WHERE         T1.SCHREGNO= '" . $schregno . "' AND T1.YEAR= '" . CTRL_YEAR . "' ";
        $query .=                    "AND T1.SEMESTER= '" . CTRL_SEMESTER . "' ";
        $query .=        ") T1 ";
        $query .=        "INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER ";
        $query .=                                    "AND T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS ";
            //卒業情報有りの場合
        $query .=        "INNER JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND T10.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
            //基礎情報
        $query .=        "INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .=        "LEFT JOIN NAME_MST T7 ON NAMECD1='Z002' AND NAMECD2=T2.SEX ";
            //課程、学科、コース
        $query .=        "LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ";
        $query .=        "LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ";
        $query .=        "LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR ";
        $query .=                            "AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')";
            //生徒住所(??)
        $query .=        "LEFT JOIN SCHREG_ADDRESS_DAT AS T8 ";
        $query .=        "INNER JOIN(";
        $query .=            "SELECT     MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .=            "FROM         SCHREG_ADDRESS_DAT ";
        $query .=            "WHERE         SCHREGNO= '" . $schregno . "' AND FISCALYEAR(ISSUEDATE) <= '" . CTRL_YEAR . "' ";
        $query .=        ")T9 ON T9.ISSUEDATE = T8.ISSUEDATE ON T8.SCHREGNO = T1.SCHREGNO ";

        return $query;
    }


    //学校データ
    function getinfo_ps7($model,$year2,$staffcd, $schregno)
    {
        $query  = "    SELECT ";
        $query .= "        T1.CLASSIFICATION,";
        $query .= "        T1.SCHOOLNAME1,";
        $query .= "        T1.SCHOOLZIPCD,T1.SCHOOLADDR1,T1.SCHOOLADDR2,";
        $query .= "        T1.SCHOOLDIV, ";
        $query .= "          T2.STAFFNAME AS PRINCIPAL_NAME,";
        $query .= "          T2.JOBNAME AS PRINCIPAL_JOBNAME, ";
        $query .= "          T3.STAFFNAME AS STAFF2_NAME,";
        $query .= "          T3.JOBNAME AS STAFF2_JOBNAME ";
        $query .= "    FROM ";
            //    学校
        $query .= "        (";
        $query .= "            SELECT ";
        $query .= "                'FRED' AS KEY,CLASSIFICATION,";
        $query .= "                SCHOOLNAME1,";
        $query .= "                SCHOOLZIPCD,SCHOOLADDR1,SCHOOLADDR2,";
        $query .= "                SCHOOLDIV ";
        $query .= "            FROM ";
        $query .= "                SCHOOL_MST W1 ";
        $query .= "            WHERE ";
        $query .= "                YEAR= '" . CTRL_YEAR . "' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND W1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        $query .= "        )T1 ";
            //    校長
        $query .= "    LEFT JOIN(";
        $query .= "        SELECT ";
        $query .= "            'FRED' AS KEY,STAFFNAME,JOBNAME ";
        $query .= "        FROM ";
        $query .= "            STAFF_YDAT W1 ";
        $query .= "            INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD ";
        $query .= "            LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ";
        $query .= "        WHERE ";
        $query .= "            W1.YEAR= '" . $year2 . "' AND W2.JOBCD='0001' ";
        $query .= "    )T2 ON T1.KEY = T2.KEY ";
            //    担当教諭
        $query .= "    LEFT JOIN(";
        $query .= "        SELECT ";
        $query .= "            'FRED' AS KEY,STAFFNAME,JOBNAME ";
        $query .= "        FROM ";
        $query .= "            STAFF_MST W2 ";
        $query .= "            LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD ";
        if ($model->Properties["tyousasyoPrintHomeRoomStaff"] == '1') {
            $query .= "        WHERE ";
            $query .= "            W2.STAFFCD = ( ";
            $query .= "               SELECT ";
            $query .= "                   TR_CD1 ";
            $query .= "               FROM ";
            $query .= "                   SCHREG_REGD_HDAT REG_H ";
            $query .= "                   INNER JOIN SCHREG_REGD_DAT REGD ON REG_H.YEAR = REGD.YEAR ";
            $query .= "                       AND REG_H.SEMESTER = REGD.SEMESTER ";
            $query .= "                       AND REG_H.GRADE = REGD.GRADE ";
            $query .= "                       AND REG_H.HR_CLASS = REGD.HR_CLASS";
            $query .= "                   INNER JOIN SCHREG_REGD_GDAT REGDG ON REG_H.YEAR = REGDG.YEAR ";
            $query .= "                       AND REG_H.GRADE = REGDG.GRADE ";
            $query .= "                   INNER JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON REGD.SCHREGNO = ENTGRD.SCHREGNO ";
            $query .= "                       AND REGDG.SCHOOL_KIND = ENTGRD.SCHOOL_KIND ";
            $query .= "                   INNER JOIN SEMESTER_MST SEME ON REG_H.YEAR = SEME.YEAR ";
            $query .= "                       AND REG_H.SEMESTER = SEME.SEMESTER ";
            $query .= "                       AND ENTGRD.GRD_DATE BETWEEN SEME.SDATE AND SEME.EDATE ";
            $query .= "                   INNER JOIN SCHREG_REGD_DAT CTRL_REGD ON REGD.SCHREGNO = CTRL_REGD.SCHREGNO ";
            $query .= "                       AND CTRL_REGD.YEAR = '".CTRL_YEAR."' ";
            $query .= "                       AND CTRL_REGD.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "                   INNER JOIN SCHREG_REGD_GDAT CTRL_REGDG ON CTRL_REGD.YEAR = CTRL_REGDG.YEAR ";
            $query .= "                       AND CTRL_REGD.GRADE = CTRL_REGDG.GRADE ";
            $query .= "                       AND REGDG.SCHOOL_KIND = CTRL_REGDG.SCHOOL_KIND ";
           $query .= "               WHERE ";
            $query .= "                   REGD.SCHREGNO = '".$schregno."' ";
            $query .= "          ) ";
        } else {
            $query .= "        WHERE ";
            $query .= "            W2.STAFFCD = '" . $staffcd . "' ";
        }
        $query .= "    )T3 ON T1.KEY = T3.KEY ";

        return $query;
    }

    //SCHOOLDIV を取得する(0:学年制、1:単位制)
    function getSchoolDiv($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOLDIV, ";
        $query .= "     VALUE(T2.NAMESPARE2,'0') AS NEN, ";
        $query .= "     VALUE(T3.NAMESPARE3,'0') AS IS_TUSIN ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR AND T2.NAMECD1 = 'Z001' AND ";
        $query .= "             T2.NAMECD2 = T1.SCHOOLDIV AND T2.NAMESPARE2 IN ('3','4','6') ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T3.YEAR = T1.YEAR AND T3.NAMECD1 = 'Z001' AND ";
        $query .= "             T3.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }

        return $query;
    }

    //近大チェック
    function checkKindai()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     UPPER(NAME1) IN ('KINDAI', 'KINJUNIOR') ";

        return $query;
    }

    //職員一覧取得
    function getStaffList() {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD AS VALUE, ";
        $query .= "     T1.STAFFCD || '　' || T1.STAFFNAME_SHOW AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_MST AS T1, ";
        $query .= "     STAFF_YDAT AS T2 ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = T2.STAFFCD AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
