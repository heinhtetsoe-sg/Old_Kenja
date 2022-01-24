<?php

require_once('for_php7.php');

class knjd126hQuery extends Query {

    /**********/
    /* コンボ */
    /**********/

    /**
     ** 年組
     **/
    function selectGradeHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REGD_H.GRADE || '-' || REGD_H.HR_CLASS AS VALUE, ";
        $query .= "     REGD_H.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT REGD_G, ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= " WHERE ";
        $query .= "     REGD_G.YEAR = REGD_H.YEAR ";
        $query .= "     AND REGD_G.GRADE = REGD_H.GRADE ";
        $query .= "     AND REGD_H.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_H.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."' ";
        }
        if ($model->auth != DEF_UPDATABLE) {
            $query .= "     AND (REGD_H.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function selectSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     REGD_G.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT REGD_G ";
        $query .= " WHERE ";
        $query .= "     REGD_G.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_G.GRADE = '{$grade}' ";

        return $query;
    }

    /**
     ** 教科名
     **/
    function selectSubclasscd($model) {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     T1.SUBCLASSCD AS VALUE, ";
        }
        $query .= "     L1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_INPUTSEQ_DAT T1 ";
        $query .= "     INNER JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD   = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L1.CLASSCD      = T1.CLASSCD ";
            $query .= "             AND L1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "             AND L1.CURRICULUM_CD= T1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.UNIT_ASSESSHIGH > 1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /**
     ** 処理学期
     **     1～3学期を表示する。
     **/
    function selectSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /**************/
    /* ヘッダ表示 */
    /**************/

    //テスト単元・観点取得
    function getUnitViewList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SEQ, ";          //key1
        $query .= "     T1.SORT, ";             //1行目
        $query .= "     T1.UNIT_L_NAME, ";      //2行目
        $query .= "     S1.VIEWCD, ";       //key2
        $query .= "     L1.VIEWABBV, ";         //3行目
        $query .= "     S1.UNIT_ASSESSHIGH ";   //4行目
        $query .= " FROM ";
        $query .= "     UNIT_TEST_DAT T1 ";
        $query .= "     INNER JOIN UNIT_TEST_INPUTSEQ_DAT S1 ";
        $query .= "              ON S1.YEAR         = T1.YEAR ";
        $query .= "             AND S1.GRADE        = T1.GRADE ";
        $query .= "             AND S1.HR_CLASS     = T1.HR_CLASS ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND S1.CLASSCD      = T1.CLASSCD ";
            $query .= "             AND S1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "             AND S1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "             AND S1.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "             AND S1.SEQ          = T1.SEQ ";
        $query .= "             AND S1.UNIT_ASSESSHIGH > 1 ";
        $query .= "     LEFT JOIN JVIEWNAME_GRADE_MST L1 ";
        $query .= "              ON L1.GRADE        = S1.GRADE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L1.CLASSCD      = S1.CLASSCD ";
            $query .= "             AND L1.SCHOOL_KIND  = S1.SCHOOL_KIND ";
            $query .= "             AND L1.CURRICULUM_CD = S1.CURRICULUM_CD ";
        }
        $query .= "             AND L1.SUBCLASSCD   = S1.SUBCLASSCD ";
        $query .= "             AND L1.VIEWCD       = S1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.DATA_DIV = '2' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SORT, ";
        $query .= "     S1.VIEWCD ";

        return $query;
    }

    //学期・観点取得
    function getSemeViewList($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);

        $query  = "";
        $query .= " WITH SUM_UNIT_ASSESSHIGH AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         S1.VIEWCD, ";
        $query .= "         SUM(S1.UNIT_ASSESSHIGH) AS UNIT_ASSESSHIGH ";
        $query .= "     FROM ";
        $query .= "         UNIT_TEST_DAT T1 ";
        $query .= "         INNER JOIN UNIT_TEST_INPUTSEQ_DAT S1 ";
        $query .= "                  ON S1.YEAR         = T1.YEAR ";
        $query .= "                 AND S1.GRADE        = T1.GRADE ";
        $query .= "                 AND S1.HR_CLASS     = T1.HR_CLASS ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND S1.CLASSCD      = T1.CLASSCD ";
            $query .= "                 AND S1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
            $query .= "                 AND S1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                 AND S1.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "                 AND S1.SEQ          = T1.SEQ ";
        $query .= "                 AND S1.UNIT_ASSESSHIGH > 1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.DATA_DIV = '2' AND ";
        $query .= "         T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "         T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         S1.VIEWCD ";
        $query .= "     ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SEMESTER, ";         //key1
        $query .= "     T1.SEMESTERNAME, ";     //1行目
        $query .= "     L1.VIEWCD, ";           //key2
        $query .= "     L1.VIEWABBV, ";         //3行目
        $query .= "     S1.UNIT_ASSESSHIGH ";   //4行目
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_YDAT L0 ";
        $query .= "              ON L0.YEAR         = T1.YEAR ";
        $query .= "             AND L0.GRADE        = '{$grade}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L0.CLASSCD || '-' || L0.SCHOOL_KIND || '-' || L0.CURRICULUM_CD || '-' || L0.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "             AND L0.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     INNER JOIN JVIEWNAME_GRADE_MST L1 ";
        $query .= "              ON L1.GRADE        = L0.GRADE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L1.CLASSCD      = L0.CLASSCD ";
            $query .= "             AND L1.SCHOOL_KIND  = L0.SCHOOL_KIND ";
            $query .= "             AND L1.CURRICULUM_CD = L0.CURRICULUM_CD ";
        }
        $query .= "             AND L1.SUBCLASSCD   = L0.SUBCLASSCD ";
        $query .= "             AND L1.VIEWCD       = L0.VIEWCD ";
        $query .= "     LEFT JOIN SUM_UNIT_ASSESSHIGH S1 ";
        $query .= "              ON S1.SEMESTER     = T1.SEMESTER ";
        $query .= "             AND S1.VIEWCD       = L1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L1.VIEWCD ";

        return $query;
    }

    /**************/
    /* リスト表示 */
    /**************/

    //生徒一覧取得
    function getSchList($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //テスト単元得点データ取得
    function getScoreData($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.SEQ, ";
        $query .= "     L1.VIEWCD, ";
        $query .= "     L1.SCORE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN UNIT_TEST_SCORE_DAT L1 ";
        $query .= "              ON L1.YEAR     = T1.YEAR ";
        $query .= "             AND L1.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "             AND L1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || '-' || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.SEQ, ";
        $query .= "     L1.VIEWCD ";

        return $query;
    }

    //観点データ（DB）取得
    function getJviewstatRecord($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.SEMESTER, ";
        $query .= "     L1.VIEWCD, ";
        $query .= "     L1.STATUS, ";
        $query .= "     L2.REMARK3 "; //到達度
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN JVIEWSTAT_RECORD_DAT L1 ";
        $query .= "              ON L1.YEAR     = T1.YEAR ";
        $query .= "             AND L1.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "             AND L1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     LEFT JOIN JVIEWSTAT_RECORD_DETAIL_DAT L2 ";
        $query .= "              ON L2.YEAR         = L1.YEAR ";
        $query .= "             AND L2.SEMESTER     = L1.SEMESTER ";
        $query .= "             AND L2.SCHREGNO     = L1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND L2.CLASSCD      = L1.CLASSCD ";
            $query .= "             AND L2.SCHOOL_KIND  = L1.SCHOOL_KIND ";
            $query .= "             AND L2.CURRICULUM_CD= L1.CURRICULUM_CD ";
        }
        $query .= "             AND L2.SUBCLASSCD   = L1.SUBCLASSCD ";
        $query .= "             AND L2.VIEWCD       = L1.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || '-' || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.SEMESTER, ";
        $query .= "     L1.VIEWCD ";

        return $query;
    }

    /**
     ** 観点データの更新
     **/
    function getUpdateQuery($db, $model) {
        //変数
        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->field["SUBCLASSCD"]);

        //削除
        //JVIEWSTAT_RECORD_DAT
        $query = knjd126hQuery::getDelJviewstatRecordDat($model);
        $db->query($query);
        //JVIEWSTAT_RECORD_DETAIL_DAT
        $query = knjd126hQuery::getDelJviewstatRecordDetailDat($model);
        $db->query($query);

        //観点データ（集計）を取得
        $updDataList = array();
        $query = knjd126hQuery::getCalcJviewstatRecord($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $updDataList[] = $row;
        }
        $result->free();

        if (get_count($updDataList) == 0) {
            return false;
        }

        //追加
        foreach ($updDataList as $key => $setArray) {
//echo "SCHREGNO=".$setArray["SCHREGNO"] .", SEMESTER=".$setArray["SEMESTER"] .", VIEWCD=".$setArray["VIEWCD"] .", STATUS=".$setArray["STATUS"] .", REMARK1=".$setArray["REMARK1"] .", REMARK2=".$setArray["REMARK2"] .", REMARK3=".$setArray["REMARK3"] ."<BR>";
            //JVIEWSTAT_RECORD_DAT
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $setArray["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $setArray["SCHREGNO"];
            if ($model->Properties["useCurriculumcd"] == "1") {
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            } else {
                $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
            }
            $data["VIEWCD"][TEXT]           = $setArray["VIEWCD"];
            $data["STATUS"][TEXT]           = $setArray["STATUS"];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "JVIEWSTAT_RECORD_DAT");
            $db->query($query);
            //JVIEWSTAT_RECORD_DETAIL_DAT
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $setArray["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $setArray["SCHREGNO"];
            if ($model->Properties["useCurriculumcd"] == "1") {
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            } else {
                $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
            }
            $data["VIEWCD"][TEXT]           = $setArray["VIEWCD"];
            $data["REMARK1"][NUMBER]        = $setArray["REMARK1"];
            $data["REMARK2"][NUMBER]        = $setArray["REMARK2"];
            $data["REMARK3"][NUMBER]        = $setArray["REMARK3"];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "JVIEWSTAT_RECORD_DETAIL_DAT");
            $db->query($query);
        }

        return true;
    }

    /**
     ** JVIEWSTAT_RECORD_DATの削除
     **/
    function getDelJviewstatRecordDat($model) {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_RECORD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND SCHREGNO IN ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T1 ";
        $query .= "             INNER JOIN SCHREG_BASE_MST L1 ";
        $query .= "                      ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     ) ";

        return $query;
    }

    /**
     ** JVIEWSTAT_RECORD_DETAIL_DATの削除
     **/
    function getDelJviewstatRecordDetailDat($model) {
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_RECORD_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '{$model->field["SEMESTER"]}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND SCHREGNO IN ( ";
        $query .= "         SELECT ";
        $query .= "             T1.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T1 ";
        $query .= "             INNER JOIN SCHREG_BASE_MST L1 ";
        $query .= "                      ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "             AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "     ) ";

        return $query;
    }

    /**
     ** JVIEWSTAT_RECORD_DAT、JVIEWSTAT_RECORD_DETAIL_DATの追加用
     **/
    //観点データ（集計）を取得
    function getCalcJviewstatRecord($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);

        $query  = "";
        $query .= " WITH T_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         L0.SEMESTER, ";
        $query .= "         L0.SEQ, ";
        $query .= "         L1.VIEWCD, ";
        $query .= "         L1.SCORE, ";
        $query .= "         L2.UNIT_ASSESSHIGH ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "                  ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         INNER JOIN UNIT_TEST_DAT L0 ";
        $query .= "                  ON L0.YEAR     = T1.YEAR ";
        $query .= "                 AND L0.DATA_DIV = '2' ";
        $query .= "                 AND L0.GRADE || '-' || L0.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND L0.CLASSCD || '-' || L0.SCHOOL_KIND || '-' || L0.CURRICULUM_CD || '-' || L0.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "                 AND L0.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "                 AND L0.SEMESTER = '{$model->field["SEMESTER"]}' ";
        $query .= "         INNER JOIN UNIT_TEST_INPUTSEQ_DAT L2 ";
        $query .= "                  ON L2.YEAR     = T1.YEAR ";
        $query .= "                 AND L2.GRADE || '-' || L2.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "                 AND L2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "                 AND L2.SEQ = L0.SEQ ";
        $query .= "         INNER JOIN UNIT_TEST_SCORE_DAT L1 ";
        $query .= "                  ON L1.YEAR     = T1.YEAR ";
        $query .= "                 AND L1.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                 AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "                 AND L1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "                 AND L1.SEQ = L2.SEQ ";
        $query .= "                 AND L1.VIEWCD = L2.VIEWCD ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "         AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        $query .= " ), T_TOUTATUDO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.VIEWCD, ";
        $query .= "         SUM(T1.SCORE) AS SCORE_TOTAL, ";
        $query .= "         SUM(T1.UNIT_ASSESSHIGH) AS UNIT_ASSESSHIGH_TOTAL, ";
        $query .= "         DECIMAL(ROUND(FLOAT(SUM(T1.SCORE))/SUM(T1.UNIT_ASSESSHIGH)*100*10,0)/10,4,1) AS TOUTATUDO ";
        $query .= "     FROM ";
        $query .= "         T_SCORE T1 ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.VIEWCD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.ASSESSMARK AS STATUS, ";
        $query .= "     T1.SCORE_TOTAL AS REMARK1, ";
        $query .= "     T1.UNIT_ASSESSHIGH_TOTAL AS REMARK2, ";
        $query .= "     T1.TOUTATUDO AS REMARK3 ";
        $query .= " FROM ";
        $query .= "     T_TOUTATUDO T1 ";
        $query .= "     LEFT JOIN JVIEWSTAT_LEVEL_SEMES_MST T2 ";
        $query .= "              ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "             AND T2.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "             AND T2.VIEWCD = T1.VIEWCD ";
        $query .= "             AND T2.DIV = '1' ";
        $query .= "             AND T2.GRADE = '{$grade}' ";
        $query .= "             AND ROUND(T1.TOUTATUDO, 0) BETWEEN T2.ASSESSLOW AND T2.ASSESSHIGH ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.VIEWCD ";

        return $query;
    }

}
?>