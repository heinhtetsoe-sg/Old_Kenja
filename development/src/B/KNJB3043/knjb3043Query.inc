<?php

require_once('for_php7.php');

require_once("PrimarySchoolProcess.php");

class knjb3043Query extends Query {

    /**
     * 共通処理  校種取得
     */
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE YEAR    = '".CTRL_YEAR."' ";
        $query .= "   AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ABBV3,NAMECD2 ";
        $query .= "  ";

        return $query;
    }

    /**
     * 共通処理  年度学期取得
     */
    function getYearSemester() {
        $query  = " SELECT ";
        $query .= "     SEME.YEAR || '-' || SEME.SEMESTER AS VALUE, ";
        $query .= "     SEME.YEAR || '年度 ' || SEME.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST SEME ";
        $query .= " WHERE ";
        $query .= "     SEME.YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        $query .= "     AND SEME.SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /**
     * 共通処理 学期情報取得(学期開始・学期終了)
     */
    function getSemesterInfo($model) {
        $query  = " SELECT ";
        $query .= "     SEME.* ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST SEME ";
        $query .= " WHERE ";
        $query .= "     SEME.YEAR || '-' || SEME.SEMESTER = '{$model->yearSeme}' ";

        return $query;
    }

    /**
     * 共通処理
     *   年組一覧取得(設定科目一覧・設定講座一覧)
     */
    function getHrClassCmb($model) {

        $query  = "";

        $query .= " SELECT ";
        $query .= "     REGDH.GRADE || '-' || REGDH.HR_CLASS AS VALUE ";
        $query .= "   , REGDH.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_HDAT REGDH ";
        $query .= " WHERE REGDH.YEAR = '{$model->year}' ";
        $query .= "   AND REGDH.SEMESTER = '{$model->semester}' ";
        
        return $query;
    }

    /**
     * 共通処理
     *   科目情報取得(設定科目一覧・設定講座一覧)
     */
    function getSubclassCmb($model) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= "   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ' ' || T6.SUBCLASSNAME AS LABEL ";
        $query .= "   , T1.YEAR ";
        $query .= "   , T1.SCHOOL_KIND ";
        if ($model->courseCd) {
            $query .= "   , T1.GRADE ";
            $query .= "   , T5.GRADE_NAME1 ";
            $query .= "   , T1.COURSECD ";
            $query .= "   , T1.MAJORCD ";
            $query .= "   , T2.COURSENAME ";
            $query .= "   , T3.MAJORNAME ";
            $query .= "   , T1.COURSECODE ";
            $query .= "   , T4.COURSECODENAME ";
            $query .= "   , T1.CREDITS ";
        }
        $query .= "   , T1.CLASSCD ";
        $query .= "   , T1.SUBCLASSCD ";
        $query .= "   , T1.CURRICULUM_CD ";
        $query .= "   , T6.SUBCLASSNAME ";
        $query .= "   , T6.SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "   CREDIT_MST T1 ";
        $query .= "   INNER JOIN COURSE_MST T2 ";
        $query .= "     ON T1.COURSECD = T2.COURSECD ";
        $query .= "   INNER JOIN MAJOR_MST T3 ";
        $query .= "     ON T1.COURSECD = T3.COURSECD ";
        $query .= "     AND T1.MAJORCD = T3.MAJORCD ";
        $query .= "   INNER JOIN COURSECODE_MST T4 ";
        $query .= "     ON T1.COURSECODE = T4.COURSECODE ";
        $query .= "   INNER JOIN SCHREG_REGD_GDAT T5 ";
        $query .= "     ON T1.YEAR = T5.YEAR ";
        $query .= "     AND T1.GRADE = T5.GRADE ";
        $query .= "   INNER JOIN V_SUBCLASS_MST T6 ";
        $query .= "     ON T1.YEAR = T6.YEAR ";
        $query .= "     AND T1.CLASSCD = T6.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND = T6.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD = T6.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD = T6.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->year}' ";

        // コースIDのパラメタが設定されている場合は、条件に追加
        if ($model->courseCd) {
            $coursecd = explode('-', $model->courseCd);
            $query .= "   AND T1.COURSECD = '{$coursecd[0]}' ";
            $query .= "   AND T1.MAJORCD = '{$coursecd[1]}' ";
            $query .= "   AND T1.GRADE = '{$coursecd[2]}' ";
            $query .= "   AND T1.COURSECODE = '{$coursecd[3]}' ";
        }

        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD ";
        $query .= "   , T1.SCHOOL_KIND ";
        $query .= "   , T1.CURRICULUM_CD ";
        $query .= "   , T1.SUBCLASSCD ";

        if ($model->courseCd) {
            $query .= "   , T1.COURSECD ";
            $query .= "   , T1.MAJORCD ";
            $query .= "   , T1.GRADE ";
            $query .= "   , T1.COURSECODE ";
        }

        return $query;
    }

    /**
     * 共通処理
     *   群一覧取得(設定科目一覧・設定講座一覧)
     */
    function getGunCmb($model) {

        $query  = "";

        $query .= " SELECT ";
        $query .= "     ELECT.GROUPCD AS VALUE ";
        $query .= "   , ELECT.GROUPNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   ELECTCLASS_DAT ELECT ";
        $query .= " WHERE ELECT.YEAR = '{$model->year}' ";
        
        return $query;
    }

    /**
     * 共通処理
     *   職員一覧取得(設定科目一覧・設定講座一覧)
     */
    function getStaffCmb($model) {

        $query  = "";

        $query .= " SELECT ";
        $query .= "     STAFF.STAFFCD AS VALUE ";
        $query .= "   , STAFF.STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "   V_STAFF_MST STAFF ";
        $query .= " WHERE ";
        $query .= "   STAFF.YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "   STAFF.STAFFCD ";
        
        return $query;
    }

    /**
     * 共通処理
     *   コース名一覧取得(設定科目一覧・設定講座一覧)
     */
    function getCourseNameCmb($model) {

        $query  = "";

        $query .= " SELECT DISTINCT ";
        $query .= "  CRE.COURSECD || '-' || CRE.MAJORCD || '-' || CRE.GRADE || '-' || CRE.COURSECODE AS VALUE ";
        $query .= " , REGDG.GRADE_NAME1 || '  (' || CRE.COURSECD || CRE.MAJORCD || ') ' || COURSE.COURSENAME || MAJOR.MAJORNAME ";
        $query .= "   || '  (' || CRE.COURSECODE || ')  ' || COURSECD.COURSECODENAME  AS LABEL ";

        $query .= "  , CRE.GRADE ";
        $query .= "  , REGDG.GRADE_NAME1 ";
        $query .= "  , CRE.COURSECD ";
        $query .= "  , CRE.MAJORCD ";
        $query .= "  , COURSE.COURSENAME ";
        $query .= "  , MAJOR.MAJORNAME ";
        $query .= "  , CRE.COURSECODE ";
        $query .= "  , COURSECD.COURSECODENAME ";
        $query .= "FROM ";
        $query .= "  CREDIT_MST CRE ";
        $query .= "  INNER JOIN V_COURSE_MST COURSE ";
        $query .= "    ON CRE.YEAR = COURSE.YEAR ";
        $query .= "    AND CRE.COURSECD = COURSE.COURSECD ";
        $query .= "  INNER JOIN V_MAJOR_MST MAJOR ";
        $query .= "    ON CRE.YEAR = MAJOR.YEAR ";
        $query .= "    AND CRE.COURSECD = MAJOR.COURSECD ";
        $query .= "    AND CRE.MAJORCD = MAJOR.MAJORCD ";
        $query .= "  INNER JOIN COURSECODE_MST COURSECD ";
        $query .= "    ON CRE.COURSECODE = COURSECD.COURSECODE ";
        $query .= "  INNER JOIN SCHREG_REGD_GDAT REGDG ";
        $query .= "    ON CRE.YEAR = REGDG.YEAR ";
        $query .= "    AND CRE.GRADE = REGDG.GRADE ";
        $query .= "WHERE ";
        $query .= "  CRE.YEAR = '{$model->year}' ";
        $query .= "ORDER BY ";
        $query .= "    CRE.COURSECD ";
        $query .= "  , CRE.GRADE ";
        $query .= "  , CRE.MAJORCD ";
        $query .= "  , CRE.COURSECODE ";

        return $query;
    }


    function getChairCmb($model) {

        $query  = "";

        $query .= " WITH CHAIR_CLS AS ( ";
        $query .= "   SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.CHAIRCD ";
        $query .= "     , T1.GROUPCD ";
        $query .= "     , T1.CHAIRNAME ";
        $query .= "     , T1.CHAIRABBV ";
        $query .= "     , T2.TRGTGRADE ";
        $query .= "     , T2.TRGTCLASS ";
        $query .= "   FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_CLS_DAT T2 ";
        $query .= "       ON T1.YEAR = T2.YEAR ";
        $query .= "       AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "       AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "   WHERE ";
        $query .= "     VALUE (T1.GROUPCD, '0000') = '0000' ";
        $query .= "     AND T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "   UNION ";
        $query .= "   SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.CHAIRCD ";
        $query .= "     , T1.GROUPCD ";
        $query .= "     , T1.CHAIRNAME ";
        $query .= "     , T1.CHAIRABBV ";
        $query .= "     , T2.TRGTGRADE ";
        $query .= "     , T2.TRGTCLASS ";
        $query .= "   FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_CLS_DAT T2 ";
        $query .= "       ON T1.YEAR = T2.YEAR ";
        $query .= "       AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "       AND T1.GROUPCD = T2.GROUPCD ";
        $query .= "   WHERE ";
        $query .= "     VALUE (T1.GROUPCD, '0000') != '0000' ";
        $query .= "     AND T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "   CHAIR.* ";
        $query .= " FROM ";
        $query .= "   CHAIR_DAT CHAIR ";
        $query .= "   LEFT JOIN CHAIR_STF_DAT STF ";
        $query .= "     ON CHAIR.YEAR = STF.YEAR ";
        $query .= "     AND CHAIR.SEMESTER = STF.SEMESTER ";
        $query .= "     AND CHAIR.CHAIRCD = STF.CHAIRCD ";
        $query .= "   LEFT JOIN CHAIR_CLS CLS ";
        $query .= "     ON CHAIR.YEAR = CLS.YEAR ";
        $query .= "     AND CHAIR.SEMESTER = CLS.SEMESTER ";
        $query .= "     AND CHAIR.CHAIRCD = CLS.CHAIRCD ";
        $query .= "   LEFT JOIN CREDIT_MST CREDIT ";
        $query .= "     ON CHAIR.YEAR = CREDIT.YEAR ";
        $query .= "     AND CHAIR.CLASSCD = CREDIT.CLASSCD ";
        $query .= "     AND CHAIR.SCHOOL_KIND = CREDIT.SCHOOL_KIND ";
        $query .= "     AND CHAIR.CURRICULUM_CD = CREDIT.CURRICULUM_CD ";
        $query .= "     AND CHAIR.SUBCLASSCD = CREDIT.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "   CHAIR.YEAR = '{$model->year}' ";
        $query .= "   AND CHAIR.SEMESTER = '{$model->semester}' ";

        // 年組
        if ($model->ajaxParam['HRCLASSCD']) {
            $hrClassCd = explode('-', $model->ajaxParam['HRCLASSCD']);
            $query .= "   AND CLS.TRGTGRADE = '{$hrClassCd[0]}' ";
            $query .= "   AND CLS.TRGTCLASS = '{$hrClassCd[1]}' ";
        }
        // 科目
        if ($model->ajaxParam['SUBCLASSCD']) {
            $subclassCd = explode('-', $model->ajaxParam['SUBCLASSCD']);
            $query .= "   AND CHAIR.CLASSCD = '{$subclassCd[0]}' ";
            $query .= "   AND CHAIR.SCHOOL_KIND = '{$subclassCd[1]}' ";
            $query .= "   AND CHAIR.CURRICULUM_CD = '{$subclassCd[2]}' ";
            $query .= "   AND CHAIR.SUBCLASSCD = '{$subclassCd[3]}' ";
        }
        // 群
        if ($model->ajaxParam['GUNCD']) {
            $query .= "   AND CHAIR.GROUPCD = '{$model->ajaxParam['GUNCD']}' ";
        }
        // 職員
        if ($model->ajaxParam['STAFFCD']) {
            $query .= "   AND STF.STAFFCD = '{$model->ajaxParam['STAFFCD']}' ";
        }
        // コース
        if ($model->ajaxParam['COURSECD']) {
            $coursecd = explode('-', $model->ajaxParam['COURSECD']);
            $query .= "   AND CREDIT.COURSECD = '{$coursecd[0]}' ";
            $query .= "   AND CREDIT.MAJORCD = '{$coursecd[1]}' ";
            $query .= "   AND CREDIT.GRADE = '{$coursecd[2]}' ";
            $query .= "   AND CREDIT.COURSECODE = '{$coursecd[3]}' ";

        }

        $query .= "ORDER BY ";
        $query .= "    CHAIR.CHAIRCD ";

        return $query;
    }

    /**
     * 科目展開表
     *   科目展開表テンプレート取得
     *   ※講座展開表の[科目展開表読込]でも使用
     */
    function getPtrnPreHdat($model) {

        // 科目展開表のテンプレート取得
        $query  = " SELECT ";
        $query .= "     PRESEQ, ";
        $query .= "     TITLE, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_PRE_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     PRESEQ ";

        return $query;
    }

    /**
     * 科目展開表
     *   コース名一覧取得
     */
    function getCourseName($model) {

        $query  = "";

        if ($model->leftMenu == "2") {
            $query .= " WITH REGD AS ( ";
            $query .= "   SELECT DISTINCT ";
            $query .= "     T1.YEAR ";
            $query .= "     , T1.SEMESTER ";
            $query .= "     , T1.GRADE ";
            $query .= "     , T1.COURSECD ";
            $query .= "     , T1.MAJORCD ";
            $query .= "     , T1.COURSECODE ";
            $query .= "     , T1.HR_CLASS ";
            $query .= "     , T2.HR_NAME ";
            $query .= "   FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ";
            $query .= "       ON T1.YEAR = T2.YEAR ";
            $query .= "       AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "       AND T1.GRADE = T2.GRADE ";
            $query .= "       AND T1.HR_CLASS = T2.HR_CLASS ";
            $query .= "   WHERE ";
            $query .= "     T1.YEAR = '{$model->year}' ";
            $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
            $query .= " ) ";
        }

        $query .= " SELECT DISTINCT ";
        $query .= "  CRE.COURSECD || '-' || CRE.MAJORCD || '-' || CRE.GRADE || '-' || CRE.COURSECODE AS VALUE ";
        $query .= " , REGDG.GRADE_NAME1 || '  (' || CRE.COURSECD || CRE.MAJORCD || ') ' || COURSE.COURSENAME || MAJOR.MAJORNAME ";
        $query .= "   || '  (' || CRE.COURSECODE || ')  ' || COURSECD.COURSECODENAME  AS LABEL ";

        $query .= "  , CRE.GRADE ";
        $query .= "  , REGDG.GRADE_NAME1 ";
        // クラス毎
        if ($model->leftMenu == "2") {
            $query .= "  , REGD.HR_CLASS ";
            $query .= "  , REGD.HR_NAME ";
        }
        $query .= "  , CRE.COURSECD ";
        $query .= "  , CRE.MAJORCD ";
        $query .= "  , COURSE.COURSENAME ";
        $query .= "  , MAJOR.MAJORNAME ";
        $query .= "  , CRE.COURSECODE ";
        $query .= "  , COURSECD.COURSECODENAME ";
        $query .= "  , REGDG.SCHOOL_KIND ";

        $query .= "FROM ";
        $query .= "  CREDIT_MST CRE ";
        $query .= "  INNER JOIN V_COURSE_MST COURSE ";
        $query .= "     ON CRE.YEAR = COURSE.YEAR ";
        $query .= "    AND CRE.COURSECD = COURSE.COURSECD ";
        $query .= "  INNER JOIN V_MAJOR_MST MAJOR ";
        $query .= "     ON CRE.YEAR = MAJOR.YEAR ";
        $query .= "    AND CRE.COURSECD = MAJOR.COURSECD ";
        $query .= "    AND CRE.MAJORCD = MAJOR.MAJORCD ";
        $query .= "  INNER JOIN V_COURSECODE_MST COURSECD ";
        $query .= "     ON CRE.YEAR = COURSECD.YEAR ";
        $query .= "    AND CRE.COURSECODE = COURSECD.COURSECODE ";
        $query .= "  INNER JOIN SCHREG_REGD_GDAT REGDG ";
        $query .= "     ON CRE.YEAR = REGDG.YEAR ";
        $query .= "    AND CRE.GRADE = REGDG.GRADE ";
        // クラス毎
        if ($model->leftMenu == "2") {
            $query .= "  INNER JOIN REGD ";
            $query .= "     ON CRE.GRADE = REGD.GRADE ";
            $query .= "    AND CRE.COURSECD = REGD.COURSECD ";
            $query .= "    AND CRE.MAJORCD = REGD.MAJORCD ";
            $query .= "    AND CRE.COURSECODE = REGD.COURSECODE ";
        }
        $query .= "WHERE ";
        $query .= "  CRE.YEAR = '{$model->year}' ";
        $query .= "ORDER BY ";
        $query .= "    CRE.COURSECD ";
        $query .= "  , CRE.GRADE ";
        $query .= "  , CRE.MAJORCD ";
        $query .= "  , CRE.COURSECODE ";
        // クラス毎
        if ($model->leftMenu == "2") {
            $query .= "  , REGD.HR_CLASS ";
        }

        return $query;
    }


    /**
     * 校時の件数を取得する
     */
    function getPeriodCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = 'B001' ";

        return $query;
    }

    /**
     * 科目展開表
     *   単位数の最大を取得する(展開表の最大列数)
     */
    function getCreditsMax($model) {

        $query  = "";
        $query .= " WITH CREDITS AS ( ";
        $query .= "   SELECT ";
        $query .= "     T1.COURSECD ";
        $query .= "     , T1.MAJORCD ";
        $query .= "     , T1.GRADE ";
        $query .= "     , T1.COURSECODE ";
        $query .= "     , SUM(T1.CREDITS) CREDITS_SUM ";
        $query .= "   FROM ";
        $query .= "     CREDIT_MST T1 ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "   GROUP BY ";
        $query .= "     T1.COURSECD ";
        $query .= "     , T1.MAJORCD ";
        $query .= "     , T1.GRADE ";
        $query .= "     , T1.COURSECODE ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   MAX(CREDITS_SUM) CREDITS_MAX ";
        $query .= " FROM ";
        $query .= "   CREDITS ";

        return $query;
    }

    /**
     * 科目展開表
     *   単位マスタのテーブルからデータ取得
     */
    function getCreditsMstList($model) {

        $query  = "";

        $query .= " SELECT DISTINCT ";
        $query .= "    T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.GRADE || '-' || T1.COURSECODE AS KEY_VALUE ";

        $query .= "  , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= "  , T6.SUBCLASSABBV AS LABEL ";

        $query .= "  , T1.YEAR ";
        $query .= "  , T1.SCHOOL_KIND ";
        $query .= "  , T1.GRADE ";
        $query .= "  , T5.GRADE_NAME1 ";
        $query .= "  , T1.COURSECD ";
        $query .= "  , T1.MAJORCD ";
        $query .= "  , T2.COURSENAME ";
        $query .= "  , T3.MAJORNAME ";
        $query .= "  , T1.COURSECODE ";
        $query .= "  , T4.COURSECODENAME ";
        $query .= "  , T1.CLASSCD ";
        $query .= "  , T1.SUBCLASSCD ";
        $query .= "  , T1.CREDITS ";
        $query .= "  , T1.CURRICULUM_CD ";
        $query .= "  , T6.SUBCLASSNAME ";
        $query .= "  , T6.SUBCLASSABBV ";
        $query .= "FROM ";
        $query .= "  CREDIT_MST T1 ";
        $query .= "  INNER JOIN COURSE_MST T2 ";
        $query .= "    ON T1.COURSECD = T2.COURSECD ";
        $query .= "  INNER JOIN MAJOR_MST T3 ";
        $query .= "    ON T1.COURSECD = T3.COURSECD ";
        $query .= "    AND T1.MAJORCD = T3.MAJORCD ";
        $query .= "  INNER JOIN COURSECODE_MST T4 ";
        $query .= "    ON T1.COURSECODE = T4.COURSECODE ";
        $query .= "  INNER JOIN SCHREG_REGD_GDAT T5 ";
        $query .= "    ON T1.YEAR = T5.YEAR ";
        $query .= "    AND T1.GRADE = T5.GRADE ";
        $query .= "  INNER JOIN V_SUBCLASS_MST T6 ";
        $query .= "    ON T1.YEAR = T6.YEAR ";
        $query .= "    AND T1.CLASSCD = T6.CLASSCD ";
        $query .= "    AND T1.SCHOOL_KIND = T6.SCHOOL_KIND ";
        $query .= "    AND T1.CURRICULUM_CD = T6.CURRICULUM_CD ";
        $query .= "    AND T1.SUBCLASSCD = T6.SUBCLASSCD ";
        $query .= "WHERE ";
        $query .= "  T1.YEAR = '".$model->year."' ";
        $query .= "ORDER BY ";
        $query .= "  T1.COURSECD ";
        $query .= "  , T1.GRADE ";
        $query .= "  , T1.MAJORCD ";
        $query .= "  , T1.COURSECODE ";
        $query .= "  , T1.CLASSCD ";
        $query .= "  , T1.SCHOOL_KIND ";
        $query .= "  , T1.CURRICULUM_CD ";
        $query .= "  , T1.SUBCLASSCD ";

        return $query;
    }

    /**
     * 科目展開表
     *   科目展開表のテーブルから科目情報データ取得
     */
    function getSubclassList($model) {

        $query  = "";

        if ($model->leftMenu == "2") {
            $query .= " WITH REGD AS ( ";
            $query .= "   SELECT DISTINCT ";
            $query .= "     T1.YEAR ";
            $query .= "     , T1.SEMESTER ";
            $query .= "     , T1.GRADE ";
            $query .= "     , T1.COURSECD ";
            $query .= "     , T1.MAJORCD ";
            $query .= "     , T1.COURSECODE ";
            $query .= "     , T1.HR_CLASS ";
            $query .= "     , T2.HR_NAME ";
            $query .= "   FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ";
            $query .= "       ON T1.YEAR = T2.YEAR ";
            $query .= "       AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "       AND T1.GRADE = T2.GRADE ";
            $query .= "       AND T1.HR_CLASS = T2.HR_CLASS ";
            $query .= "   WHERE ";
            $query .= "     T1.YEAR = '{$model->year}' ";
            $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
            $query .= " ) ";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "    T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.GRADE || '-' || T1.COURSECODE AS KEY_VALUE ";
        $query .= "  , T1.YEAR ";
        $query .= "  , T1.SCHOOL_KIND ";
        $query .= "  , T1.GRADE ";
        $query .= "  , T5.GRADE_NAME1 ";
        if ($model->leftMenu == "2") {
            $query .= "  , T7.HR_CLASS ";
        }
        $query .= "  , T1.COURSECD ";
        $query .= "  , T1.MAJORCD ";
        $query .= "  , T2.COURSENAME ";
        $query .= "  , T3.MAJORNAME ";
        $query .= "  , T1.COURSECODE ";
        $query .= "  , T4.COURSECODENAME ";
        $query .= "  , T1.CLASSCD ";
        $query .= "  , T1.SUBCLASSCD ";
        $query .= "  , 1 AS CREDITS ";
        $query .= "  , T1.CURRICULUM_CD ";
        $query .= "  , T6.SUBCLASSNAME ";
        $query .= "  , T6.SUBCLASSABBV ";
        $query .= "  , T1.PRE_ORDER ";
        $query .= "FROM ";
        $query .= "  SCH_PTRN_PRE_DAT T1 ";
        $query .= "  INNER JOIN COURSE_MST T2 ";
        $query .= "     ON T1.COURSECD = T2.COURSECD ";
        $query .= "  INNER JOIN MAJOR_MST T3 ";
        $query .= "     ON T1.COURSECD = T3.COURSECD ";
        $query .= "    AND T1.MAJORCD = T3.MAJORCD ";
        $query .= "  INNER JOIN COURSECODE_MST T4 ";
        $query .= "     ON T1.COURSECODE = T4.COURSECODE ";
        $query .= "  INNER JOIN SCHREG_REGD_GDAT T5 ";
        $query .= "     ON T1.YEAR = T5.YEAR ";
        $query .= "    AND T1.GRADE = T5.GRADE ";
        $query .= "  INNER JOIN V_SUBCLASS_MST T6 ";
        $query .= "     ON T1.YEAR = T6.YEAR ";
        $query .= "    AND T1.CLASSCD = T6.CLASSCD ";
        $query .= "    AND T1.SCHOOL_KIND = T6.SCHOOL_KIND ";
        $query .= "    AND T1.CURRICULUM_CD = T6.CURRICULUM_CD ";
        $query .= "    AND T1.SUBCLASSCD = T6.SUBCLASSCD ";

        if ($model->leftMenu == "2") {
            $query .= "  INNER JOIN REGD T7 ";
            $query .= "     ON T1.YEAR = T7.YEAR ";
            $query .= "    AND T1.SEMESTER = T7.SEMESTER ";
            $query .= "    AND T1.GRADE = T7.GRADE ";
            $query .= "    AND T1.COURSECD = T7.COURSECD ";
            $query .= "    AND T1.MAJORCD = T7.MAJORCD ";
            $query .= "    AND T1.COURSECODE = T7.COURSECODE ";
        }

        $query .= "WHERE T1.YEAR = '{$model->year}' ";
        $query .= "  AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "  AND T1.PRESEQ = {$model->preSeq} ";
        $query .= "ORDER BY ";
        $query .= "    T1.COURSECD ";
        $query .= "  , T1.GRADE ";
        if ($model->leftMenu == "2") {
            $query .= "  , T7.HR_CLASS ";
        }
        $query .= "  , T1.MAJORCD ";
        $query .= "  , T1.COURSECODE ";
        $query .= "  , T1.PRE_ORDER ";

        return $query;
    }

    // 講座展開表のテンプレート
    /**
     * 講座展開表
     *   講座展開表テンプレート取得
     */
    function getPtrnPreChairHdat($model) {

        // 講座展開表のテンプレート取得
        $query  = " SELECT ";
        $query .= "     PRESEQ AS PRECHAIRSEQ, ";
        $query .= "     TITLE, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_PRE_CHR_HDAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     PRESEQ ";

        return $query;
    }

    /**
     * 講座展開表
     *   講座展開表の行タイトル取得(教育課程)
     */
    function getPreChairTitle1($model) {

        $query  = "";

        $query .= " SELECT DISTINCT ";
        $query .= "  CRE.COURSECD || '-' || CRE.MAJORCD || '-' || CRE.GRADE || '-' || CRE.COURSECODE AS VALUE ";
        $query .= " , REGDG.GRADE_NAME1 || '  (' || CRE.COURSECD || CRE.MAJORCD || ') ' || COURSE.COURSENAME || MAJOR.MAJORNAME ";
        $query .= "   || '  (' || CRE.COURSECODE || ')  ' || COURSECD.COURSECODENAME  AS LABEL ";

        $query .= "  , CRE.GRADE ";
        $query .= "  , REGDG.GRADE_NAME1 ";

        $query .= "  , CRE.COURSECD ";
        $query .= "  , CRE.MAJORCD ";
        $query .= "  , COURSE.COURSENAME ";
        $query .= "  , MAJOR.MAJORNAME ";
        $query .= "  , CRE.COURSECODE ";
        $query .= "  , COURSECD.COURSECODENAME ";
        $query .= "  , REGDG.SCHOOL_KIND ";

        $query .= " FROM ";
        $query .= "  CREDIT_MST CRE ";
        $query .= "  INNER JOIN V_COURSE_MST COURSE ";
        $query .= "     ON CRE.YEAR = COURSE.YEAR ";
        $query .= "    AND CRE.COURSECD = COURSE.COURSECD ";
        $query .= "  INNER JOIN V_MAJOR_MST MAJOR ";
        $query .= "     ON CRE.YEAR = MAJOR.YEAR ";
        $query .= "    AND CRE.COURSECD = MAJOR.COURSECD ";
        $query .= "    AND CRE.MAJORCD = MAJOR.MAJORCD ";
        $query .= "  INNER JOIN V_COURSECODE_MST COURSECD ";
        $query .= "     ON CRE.YEAR = COURSECD.YEAR ";
        $query .= "    AND CRE.COURSECODE = COURSECD.COURSECODE ";
        $query .= "  INNER JOIN SCHREG_REGD_GDAT REGDG ";
        $query .= "     ON CRE.YEAR = REGDG.YEAR ";
        $query .= "    AND CRE.GRADE = REGDG.GRADE ";

        $query .= "WHERE ";
        $query .= "  CRE.YEAR = '{$model->year}' ";
        $query .= "ORDER BY ";
        $query .= "    CRE.COURSECD ";
        $query .= "  , CRE.GRADE ";
        $query .= "  , CRE.MAJORCD ";
        $query .= "  , CRE.COURSECODE ";

        return $query;
    }

    /**
     * 講座展開表
     *   講座展開表の行タイトル取得(教育課程(年組))
     */
    function getPreChairTitle2($model) {

        $query  = "";

        $query .= " WITH REGD AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.GRADE ";
        $query .= "     , T1.COURSECD ";
        $query .= "     , T1.MAJORCD ";
        $query .= "     , T1.COURSECODE ";
        $query .= "     , T1.HR_CLASS ";
        $query .= "     , T2.HR_NAME ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "       ON T1.YEAR = T2.YEAR ";
        $query .= "       AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "       AND T1.GRADE = T2.GRADE ";
        $query .= "       AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "  CRE.COURSECD || '-' || CRE.MAJORCD || '-' || CRE.GRADE || '-' || CRE.COURSECODE AS VALUE ";
        $query .= " , REGDG.GRADE_NAME1 || '  (' || CRE.COURSECD || CRE.MAJORCD || ') ' || COURSE.COURSENAME || MAJOR.MAJORNAME ";
        $query .= "   || '  (' || CRE.COURSECODE || ')  ' || COURSECD.COURSECODENAME  AS LABEL ";

        $query .= "  , CRE.GRADE ";
        $query .= "  , REGDG.GRADE_NAME1 ";
        $query .= "  , REGD.HR_CLASS ";
        $query .= "  , REGD.HR_NAME ";

        $query .= "  , CRE.COURSECD ";
        $query .= "  , CRE.MAJORCD ";
        $query .= "  , COURSE.COURSENAME ";
        $query .= "  , MAJOR.MAJORNAME ";
        $query .= "  , CRE.COURSECODE ";
        $query .= "  , COURSECD.COURSECODENAME ";
        $query .= "  , REGDG.SCHOOL_KIND ";

        $query .= " FROM ";
        $query .= "  CREDIT_MST CRE ";
        $query .= "  INNER JOIN V_COURSE_MST COURSE ";
        $query .= "     ON CRE.YEAR = COURSE.YEAR ";
        $query .= "    AND CRE.COURSECD = COURSE.COURSECD ";
        $query .= "  INNER JOIN V_MAJOR_MST MAJOR ";
        $query .= "     ON CRE.YEAR = MAJOR.YEAR ";
        $query .= "    AND CRE.COURSECD = MAJOR.COURSECD ";
        $query .= "    AND CRE.MAJORCD = MAJOR.MAJORCD ";
        $query .= "  INNER JOIN V_COURSECODE_MST COURSECD ";
        $query .= "     ON CRE.YEAR = COURSECD.YEAR ";
        $query .= "    AND CRE.COURSECODE = COURSECD.COURSECODE ";
        $query .= "  INNER JOIN SCHREG_REGD_GDAT REGDG ";
        $query .= "     ON CRE.YEAR = REGDG.YEAR ";
        $query .= "    AND CRE.GRADE = REGDG.GRADE ";

        $query .= "  INNER JOIN REGD ";
        $query .= "     ON CRE.GRADE = REGD.GRADE ";
        $query .= "    AND CRE.COURSECD = REGD.COURSECD ";
        $query .= "    AND CRE.MAJORCD = REGD.MAJORCD ";
        $query .= "    AND CRE.COURSECODE = REGD.COURSECODE ";

        $query .= "WHERE ";
        $query .= "  CRE.YEAR = '{$model->year}' ";
        $query .= "ORDER BY ";
        $query .= "    CRE.COURSECD ";
        $query .= "  , CRE.GRADE ";
        $query .= "  , CRE.MAJORCD ";
        $query .= "  , CRE.COURSECODE ";
        $query .= "  , REGD.HR_CLASS ";

        return $query;
    }

    /**
     * 講座展開表
     *   講座展開表の行タイトル取得(職員)
     */
    function getPreChairTitle3($model) {

        $query  = "";

        $query .= " WITH FUMEI(ORDERDIV, STAFFCD, STAFFNAME) AS ( ";
        $query .= "   VALUES('0', '---', '不明') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     ORDERDIV ";
        $query .= "   , STAFFCD ";
        $query .= "   , STAFFNAME ";
        $query .= " FROM ";
        $query .= "     FUMEI STF ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '1' AS ORDERDIV ";
        $query .= "   , STF.STAFFCD ";
        $query .= "   , STF.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST STF ";
        $query .= " WHERE ";
        $query .= "     STF.YEAR = '{$model->year}' ";
        $query .= " ORDER  BY ";
        $query .= "     ORDERDIV ";
        $query .= "   , STAFFCD ";

        return $query;
    }

    /**
     * 講座展開表
     *   講座展開表の行タイトル取得(年組)
     */
    function getPreChairTitle4($model) {

        $query  = "";

        $query .= " WITH REGD AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.GRADE ";
        $query .= "     , T1.COURSECD ";
        $query .= "     , T1.MAJORCD ";
        $query .= "     , T1.COURSECODE ";
        $query .= "     , T1.HR_CLASS ";
        $query .= "     , T2.HR_NAME ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "       ON T1.YEAR = T2.YEAR ";
        $query .= "       AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "       AND T1.GRADE = T2.GRADE ";
        $query .= "       AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "  CRE.GRADE || '-' || REGD.HR_CLASS AS VALUE ";
        $query .= " , REGDG.GRADE_NAME1 || '  ' || REGD.HR_NAME AS LABEL ";

        $query .= "  , CRE.GRADE ";
        $query .= "  , REGDG.GRADE_NAME1 ";
        $query .= "  , REGD.HR_CLASS ";
        $query .= "  , REGD.HR_NAME ";

        $query .= " FROM ";
        $query .= "  CREDIT_MST CRE ";
        $query .= "  INNER JOIN SCHREG_REGD_GDAT REGDG ";
        $query .= "     ON CRE.YEAR = REGDG.YEAR ";
        $query .= "    AND CRE.GRADE = REGDG.GRADE ";

        $query .= "  INNER JOIN REGD ";
        $query .= "     ON CRE.GRADE = REGD.GRADE ";
        $query .= "    AND CRE.COURSECD = REGD.COURSECD ";
        $query .= "    AND CRE.MAJORCD = REGD.MAJORCD ";
        $query .= "    AND CRE.COURSECODE = REGD.COURSECODE ";

        $query .= "WHERE ";
        $query .= "  CRE.YEAR = '{$model->year}' ";
        $query .= "ORDER BY ";
        $query .= "    CRE.GRADE ";
        $query .= "  , REGD.HR_CLASS ";

        return $query;
    }


    /**
     * 講座展開表
     *   科目展開表から講座一覧を取得
     */
    function getPtrnPreDat($model) {

        $query = "";

        $query .= " WITH REGD AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.GRADE ";
        $query .= "     , T1.COURSECD ";
        $query .= "     , T1.MAJORCD ";
        $query .= "     , T1.COURSECODE ";
        $query .= "     , T1.HR_CLASS ";
        $query .= "     , T2.HR_NAME ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "       ON T1.YEAR = T2.YEAR ";
        $query .= "       AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "       AND T1.GRADE = T2.GRADE ";
        $query .= "       AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= "   WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= " ) ";

        if ($model->leftMenu == "3") {
            $query .= " WITH STAFF AS ( ";
            $query .= "   SELECT ";
            $query .= "     CHAIRCD ";
            $query .= "     , STAFFCD ";
            $query .= "   FROM ";
            $query .= "     CHAIR_STF_DAT ";
            $query .= "   WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            $query .= " ) ";
        }


        $query .= " SELECT DISTINCT ";
        $query .= "     PRE.YEAR ";
        $query .= "   , PRE.SEMESTER ";
        $query .= "   , PRE.PRESEQ ";
        $query .= "   , PRE.PRE_ORDER ";

        if ($model->leftMenu == "1" || $model->leftMenu == "2") {
            $query .= "   , PRE.COURSECD ";
            $query .= "   , PRE.MAJORCD ";
            $query .= "   , PRE.GRADE ";
            $query .= "   , PRE.COURSECODE ";
            $query .= "   , PRE.CLASSCD ";
            $query .= "   , PRE.SCHOOL_KIND ";
            $query .= "   , PRE.CURRICULUM_CD ";
            $query .= "   , PRE.SUBCLASSCD ";
        }

        $query .= "   , CHAIR.CHAIRCD ";
        $query .= "   , CHAIR.CHAIRNAME ";
        $query .= "   , CHAIR.CHAIRABBV ";
        if ($model->leftMenu == "2" || $model->leftMenu == "4") {
            $query .= "   , PRE.GRADE ";
            $query .= "   , REGD.HR_CLASS ";
            $query .= "   , REGD.HR_NAME ";
        }
        if ($model->leftMenu == "3") {
            $query .= "   , STAFF.STAFFCD ";
        }
        $query .= " FROM ";
        $query .= "   SCH_PTRN_PRE_DAT PRE ";

        $query .= "   INNER JOIN REGD ";
        $query .= "     ON PRE.GRADE = REGD.GRADE ";
        $query .= "     AND PRE.COURSECD = REGD.COURSECD ";
        $query .= "     AND PRE.MAJORCD = REGD.MAJORCD ";
        $query .= "     AND PRE.COURSECODE = REGD.COURSECODE ";

        $query .= "   INNER JOIN CHAIR_DAT CHAIR ";
        $query .= "     ON PRE.YEAR = CHAIR.YEAR ";
        $query .= "     AND PRE.SEMESTER = CHAIR.SEMESTER ";
        $query .= "     AND PRE.CLASSCD = CHAIR.CLASSCD ";
        $query .= "     AND PRE.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
        $query .= "     AND PRE.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
        $query .= "     AND PRE.SUBCLASSCD = CHAIR.SUBCLASSCD ";

        $query .= "   INNER JOIN CHAIR_CLS_DAT CHACLS ";
        $query .= "     ON CHAIR.YEAR = CHACLS.YEAR ";
        $query .= "     AND CHAIR.SEMESTER = CHACLS.SEMESTER ";
        $query .= "     AND CHAIR.CHAIRCD = CHACLS.CHAIRCD ";
        $query .= "     AND REGD.GRADE = CHACLS.TRGTGRADE ";
        $query .= "     AND REGD.HR_CLASS = CHACLS.TRGTCLASS ";

        if ($model->leftMenu == "3") {
            $query .= "   INNER JOIN STAFF ";
            $query .= "     ON CHAIR.CHAIRCD = STAFF.CHAIRCD ";
        }

        $query .= " WHERE ";
        $query .= "   PRE.YEAR = '{$model->year}' ";
        $query .= "   AND PRE.SEMESTER = '{$model->semester}' ";
        $query .= "   AND PRE.PRESEQ = {$model->preSeq} ";
        $query .= " ORDER BY ";
        $query .= "   PRE.YEAR ";
        $query .= "   , PRE.SEMESTER ";
        $query .= "   , PRE.PRESEQ ";
        $query .= "   , PRE.PRE_ORDER ";
        if ($model->leftMenu == "1" || $model->leftMenu == "2") {
            $query .= "   , PRE.COURSECD ";
            $query .= "   , PRE.MAJORCD ";
            $query .= "   , PRE.GRADE ";
            $query .= "   , PRE.COURSECODE ";
            $query .= "   , PRE.CLASSCD ";
            $query .= "   , PRE.SCHOOL_KIND ";
            $query .= "   , PRE.CURRICULUM_CD ";
            $query .= "   , PRE.SUBCLASSCD ";
        }

        $query .= "   , CHAIR.CHAIRCD ";
        
        return $query;
    }

    /**
     * 講座展開表
     *   講座展開表から講座一覧を取得
     */
    function getPtrnPreChairDat($model) {

        $query = "";

        $query .= " SELECT DISTINCT ";
        $query .= "   PRE.YEAR ";
        $query .= "   , PRE.SEMESTER ";
        $query .= "   , PRE.PRESEQ ";
        $query .= "   , PRE.COURSECD ";
        $query .= "   , PRE.MAJORCD ";
        $query .= "   , PRE.GRADE ";
        $query .= "   , PRE.COURSECODE ";
        $query .= "   , PRE.PRE_ORDER ";
        $query .= "   , CHAIR.CLASSCD ";
        $query .= "   , CHAIR.SCHOOL_KIND ";
        $query .= "   , CHAIR.CURRICULUM_CD ";
        $query .= "   , CHAIR.SUBCLASSCD ";
        $query .= "   , PRE.HR_CLASS ";
        $query .= "   , REGDH.HR_NAME ";
        $query .= "   , PRE.CHAIRCD ";
        $query .= "   , CHAIR.CHAIRNAME ";
        $query .= "   , CHAIR.CHAIRABBV ";
        if ($model->leftMenu == '3') {
            $query .= "   , VALUE(STF.STAFFCD, '---') AS STAFFCD ";
        }

        $query .= " FROM ";
        $query .= "   SCH_PTRN_PRE_CHR_DAT PRE ";
        $query .= "   LEFT JOIN SCHREG_REGD_HDAT REGDH ";
        $query .= "     ON PRE.YEAR = REGDH.YEAR ";
        $query .= "     AND PRE.SEMESTER = REGDH.SEMESTER ";
        $query .= "     AND PRE.GRADE = REGDH.GRADE ";
        $query .= "     AND PRE.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "   LEFT JOIN CHAIR_DAT CHAIR ";
        $query .= "     ON PRE.YEAR = CHAIR.YEAR ";
        $query .= "     AND PRE.SEMESTER = CHAIR.SEMESTER ";
        $query .= "     AND PRE.CHAIRCD = CHAIR.CHAIRCD ";

        if ($model->leftMenu == '3') {
            $query .= "   LEFT JOIN CHAIR_STF_DAT STF ";
            $query .= "     ON PRE.YEAR = STF.YEAR ";
            $query .= "     AND PRE.SEMESTER = STF.SEMESTER ";
            $query .= "     AND PRE.CHAIRCD = STF.CHAIRCD ";
        }

        $query .= " WHERE ";
        $query .= "   PRE.YEAR = '{$model->year}' ";
        $query .= "   AND PRE.SEMESTER = '{$model->semester}' ";
        $query .= "   AND PRE.PRESEQ = {$model->preChairSeq} ";
        $query .= " ORDER BY ";
        $query .= "   PRE.YEAR ";
        $query .= "   , PRE.SEMESTER ";
        $query .= "   , PRE.PRESEQ ";
        $query .= "   , PRE.COURSECD ";
        $query .= "   , PRE.MAJORCD ";
        $query .= "   , PRE.COURSECODE ";
        $query .= "   , PRE.PRE_ORDER ";
        $query .= "   , PRE.GRADE ";
        $query .= "   , PRE.HR_CLASS ";
        $query .= "   , CHAIR.CLASSCD ";
        $query .= "   , CHAIR.SCHOOL_KIND ";
        $query .= "   , CHAIR.CURRICULUM_CD ";
        $query .= "   , CHAIR.SUBCLASSCD ";
        $query .= "   , PRE.CHAIRCD ";

        return $query;
    }

    /**
     * 受講生人数オーバーの講座一覧取得
     */
    function getChairStdCapaOver($model) {
        $query  = " WITH CHAIR_STD_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , CHAIRCD ";
        $query .= "         , COUNT(*) STD_CNT ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "         AND SEMESTER = '{$model->semester}' ";
        $query .= "         AND '{$model->ajaxParam['UPDDATE']}' BETWEEN APPDATE AND APPENDDATE ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , CHAIRCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CHAIR_STD_MAX.YEAR ";
        $query .= "     , CHAIR_STD_MAX.SEMESTER ";
        $query .= "     , CHAIR_STD_MAX.CHAIRCD ";
        $query .= "     , CHAIR_STD_MAX.REMARK1 STD_MAX ";
        $query .= "     , CHAIR_STD_CNT.STD_CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_DETAIL_DAT CHAIR_STD_MAX ";
        $query .= "     INNER JOIN CHAIR_STD_CNT ";
        $query .= "         ON CHAIR_STD_MAX.YEAR = CHAIR_STD_CNT.YEAR ";
        $query .= "         AND CHAIR_STD_MAX.SEMESTER = CHAIR_STD_CNT.SEMESTER ";
        $query .= "         AND CHAIR_STD_MAX.CHAIRCD = CHAIR_STD_CNT.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     CHAIR_STD_MAX.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR_STD_MAX.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHAIR_STD_MAX.SEQ = '001' ";
        $query .= "     AND INT(CHAIR_STD_MAX.REMARK1) < CHAIR_STD_CNT.STD_CNT ";

        return $query;
    }

    /**
     * 同一名簿の講座一覧取得
     */
    function getChairStdSameMeibo($model) {
        $query  = " WITH TARGET_STD AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , CHAIRCD ";
        $query .= "         , SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "         AND SEMESTER = '{$model->semester}' ";
        $query .= "         AND CHAIRCD = '{$model->ajaxParam['CHAIRCD']}' ";
        $query .= "         AND '{$model->ajaxParam['UPDDATE']}' BETWEEN APPDATE AND APPENDDATE ";
        $query .= " ) ";
        
        $query .= " , CHAIR_STD_CNT AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.CHAIRCD ";
        $query .= "     , COUNT(T1.SCHREGNO) STD_CNT ";
        $query .= "     , COUNT(T2.SCHREGNO) CHK_STD_CNT ";
        $query .= "     , (SELECT COUNT(*) FROM TARGET_STD) BASE_STD_CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     LEFT JOIN TARGET_STD T2 ";
        $query .= "         ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND '{$model->ajaxParam['UPDDATE']}' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.CHAIRCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_CNT ";
        $query .= " WHERE ";
        $query .= "     STD_CNT = CHK_STD_CNT ";
        $query .= "     AND STD_CNT = BASE_STD_CNT ";
        
        return $query;
    }

    /**
     * HRクラスの名簿一覧取得
     */
    function getHrClassStdMeibo($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.SCHREGNO ";
        $query .= "     , T1.GRADE ";
        $query .= "     , T1.HR_CLASS ";
        $query .= "     , T2.NAME ";
        $query .= "     , T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "         ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.GRADE = '{$model->ajaxParam['GRADE']}' ";
        $query .= "     AND T1.HR_CLASS = '{$model->ajaxParam['HR_CLASS']}' ";

        return $query;
    }

    /**
     * 名簿の講座一覧取得
     */
    function getStdChair($model) {
        $query  = " WITH CHAIR_STD AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , CHAIRCD ";
        $query .= "         , SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "         AND SEMESTER = '{$model->semester}' ";
        $query .= "         AND '{$model->ajaxParam['UPDDATE']}' BETWEEN APPDATE AND APPENDDATE ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.SCHREGNO ";
        $query .= "     , T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->ajaxParam['SCHREGNO']}' ";

        return $query;
    }

    function getCourseChair($model, $chairList) {

        $query  = "";
        $query .= " WITH CHAIR AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , CHAIRCD ";
        $query .= "     , CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "   FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHAIRCD IN ('".implode("','", $chairList)."') ";
        $query .= " ) ";

        $query .= " , CHAIR_CLS AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     CHR.YEAR ";
        $query .= "     , CHR.SEMESTER ";
        $query .= "     , CHR.CHAIRCD ";
        $query .= "     , CLS.TRGTGRADE ";
        $query .= "     , CLS.TRGTCLASS ";
        $query .= "   FROM ";
        $query .= "     CHAIR_DAT CHR ";
        $query .= "     INNER JOIN CHAIR_CLS_DAT CLS ";
        $query .= "       ON CHR.YEAR = CLS.YEAR ";
        $query .= "       AND CHR.SEMESTER = CLS.SEMESTER ";
        $query .= "       AND CHR.CHAIRCD = CLS.CHAIRCD ";
        $query .= "   WHERE ";
        $query .= "     CHR.YEAR = '{$model->year}' ";
        $query .= "     AND CHR.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CLS.GROUPCD = '0000' ";
        $query .= "   UNION ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     CHR.YEAR ";
        $query .= "     , CHR.SEMESTER ";
        $query .= "     , CHR.CHAIRCD ";
        $query .= "     , CLS.TRGTGRADE ";
        $query .= "     , CLS.TRGTCLASS ";
        $query .= "   FROM ";
        $query .= "     CHAIR_DAT CHR ";
        $query .= "     INNER JOIN CHAIR_CLS_DAT CLS ";
        $query .= "       ON CHR.YEAR = CLS.YEAR ";
        $query .= "       AND CHR.SEMESTER = CLS.SEMESTER ";
        $query .= "       AND CHR.GROUPCD = CLS.GROUPCD ";
        $query .= "   WHERE ";
        $query .= "     CHR.YEAR = '{$model->year}' ";
        $query .= "     AND CHR.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CLS.GROUPCD != '0000' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "   CREDIT.YEAR ";
        $query .= "   , CREDIT.COURSECD ";
        $query .= "   , CREDIT.MAJORCD ";
        $query .= "   , CREDIT.GRADE ";
        $query .= "   , CREDIT.COURSECODE ";
        $query .= "   , CREDIT.CLASSCD ";
        $query .= "   , CREDIT.SCHOOL_KIND ";
        $query .= "   , CREDIT.CURRICULUM_CD ";
        $query .= "   , CREDIT.SUBCLASSCD ";
        $query .= "   , CHAIR.CHAIRCD ";
        $query .= "   , CLS.TRGTGRADE ";
        $query .= "   , CLS.TRGTCLASS ";

        $query .= " FROM ";
        $query .= "   CREDIT_MST CREDIT ";
        $query .= "   INNER JOIN CHAIR ";
        $query .= "     ON CREDIT.YEAR = CHAIR.YEAR ";
        $query .= "     AND CREDIT.CLASSCD = CHAIR.CLASSCD ";
        $query .= "     AND CREDIT.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
        $query .= "     AND CREDIT.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
        $query .= "     AND CREDIT.SUBCLASSCD = CHAIR.SUBCLASSCD ";

        $query .= "   INNER JOIN CHAIR_CLS CLS ";
        $query .= "     ON CHAIR.YEAR = CLS.YEAR ";
        $query .= "     AND CHAIR.SEMESTER = CLS.SEMESTER ";
        $query .= "     AND CHAIR.CHAIRCD = CLS.CHAIRCD ";

        $query .= " WHERE ";
        $query .= "   CREDIT.YEAR = '{$model->year}' ";

        return $query;
    }

    /**
     * 受講生の重複チェック
     */
    function getStdDupliCheck($model, $chairList, $div) {

        $query = "";
        $query .= " WITH CHAIR_STD AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     STD.CHAIRCD ";
        $query .= "     , STD.SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     CHAIR_STD_DAT STD ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "       ON STD.YEAR = REGD.YEAR ";
        $query .= "       AND STD.SEMESTER = REGD.SEMESTER ";
        $query .= "       AND STD.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   WHERE ";
        $query .= "     STD.YEAR = '{$model->year}' ";
        $query .= "     AND STD.SEMESTER = '{$model->semester}' ";
        $query .= "     AND STD.CHAIRCD IN ( '".implode("','", $chairList)."')";
        $query .= "     AND '{$model->semesterInfo["SDATE"]}' BETWEEN STD.APPDATE AND STD.APPENDDATE ";
        $query .= " )  ";
        $query .= " , STD_CHAIRCNT AS ( ";
        $query .= "   SELECT ";
        $query .= "     SCHREGNO ";
        $query .= "     , COUNT(CHAIRCD) CHAIRCNT ";
        $query .= "   FROM ";
        $query .= "     CHAIR_STD ";
        $query .= "   GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";

        if ($div == 'CHAIR_LIST') {
            // 受講生が重複している講座の一覧
            $query .= " SELECT DISTINCT ";
            $query .= "   CHAIR_STD.CHAIRCD ";
            $query .= "   , COUNT(CHAIR_STD.SCHREGNO) STDCNT ";
            $query .= " FROM ";
            $query .= "   CHAIR_STD ";
            $query .= "   INNER JOIN STD_CHAIRCNT ";
            $query .= "     ON CHAIR_STD.SCHREGNO = STD_CHAIRCNT.SCHREGNO ";
            $query .= " WHERE STD_CHAIRCNT.CHAIRCNT > 1 ";
            $query .= " GROUP BY ";
            $query .= "   CHAIR_STD.CHAIRCD ";
        } else {
            // 重複している受講生の人数
            $query .= " SELECT DISTINCT ";
            $query .= "   COUNT(*) STDCNT ";
            $query .= " FROM ";
            $query .= "   STD_CHAIRCNT ";
            $query .= " WHERE STD_CHAIRCNT.CHAIRCNT > 1 ";
        }

        return $query;
    }

    /**
     * 受講生の未配置チェック
     * ※未配置のある科目を全て返却
     */
    function getStdUnPlacedCheck($model) {

        $query = "";
        $query .= " WITH CHRSUBCLASS AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     CLASSCD ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , CURRICULUM_CD ";
        $query .= "     , SUBCLASSCD ";
        $query .= "   FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= " ) ";
        $query .= " , STDRI AS ( ";
        $query .= "   SELECT ";
        $query .= "     STDSEL.YEAR ";
        $query .= "     , STDSEL.SEMESTER ";
        $query .= "     , STDSEL.SCHREGNO ";
        $query .= "     , REGD.COURSECD ";
        $query .= "     , REGD.MAJORCD ";
        $query .= "     , REGD.GRADE ";
        $query .= "     , REGD.COURSECODE ";
        $query .= "     , STDSEL.CLASSCD ";
        $query .= "     , STDSEL.SCHOOL_KIND ";
        $query .= "     , STDSEL.CURRICULUM_CD ";
        $query .= "     , STDSEL.SUBCLASSCD ";
        $query .= "   FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT STDSEL ";
        $query .= "     INNER JOIN CHRSUBCLASS ";
        $query .= "       ON STDSEL.CLASSCD = CHRSUBCLASS.CLASSCD ";
        $query .= "       AND STDSEL.SCHOOL_KIND = CHRSUBCLASS.SCHOOL_KIND ";
        $query .= "       AND STDSEL.CURRICULUM_CD = CHRSUBCLASS.CURRICULUM_CD ";
        $query .= "       AND STDSEL.SUBCLASSCD = CHRSUBCLASS.SUBCLASSCD ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ";
        $query .= "       ON STDSEL.YEAR = REGD.YEAR ";
        $query .= "       AND STDSEL.SEMESTER = REGD.SEMESTER ";
        $query .= "       AND STDSEL.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     INNER JOIN CREDIT_MST CREDIT ";
        $query .= "       ON STDSEL.YEAR = CREDIT.YEAR ";
        $query .= "       AND REGD.COURSECD = CREDIT.COURSECD ";
        $query .= "       AND REGD.MAJORCD = CREDIT.MAJORCD ";
        $query .= "       AND REGD.GRADE = CREDIT.GRADE ";
        $query .= "       AND REGD.COURSECODE = CREDIT.COURSECODE ";
        $query .= "       AND STDSEL.CLASSCD = CREDIT.CLASSCD ";
        $query .= "       AND STDSEL.SCHOOL_KIND = CREDIT.SCHOOL_KIND ";
        $query .= "       AND STDSEL.CURRICULUM_CD = CREDIT.CURRICULUM_CD ";
        $query .= "       AND STDSEL.SUBCLASSCD = CREDIT.SUBCLASSCD ";
        $query .= "   WHERE ";
        $query .= "     STDSEL.YEAR = '{$model->year}' ";
        $query .= "     AND STDSEL.SEMESTER = '{$model->semester}' ";
        $query .= " ) ";
        $query .= " , CHAIRSTD AS ( ";
        $query .= "   SELECT ";
        $query .= "     CHR.YEAR ";
        $query .= "     , CHR.SEMESTER ";
        $query .= "     , CHR.CHAIRCD ";
        $query .= "     , CHR.CLASSCD ";
        $query .= "     , CHR.SCHOOL_KIND ";
        $query .= "     , CHR.CURRICULUM_CD ";
        $query .= "     , CHR.SUBCLASSCD ";
        $query .= "     , CHRSTD.SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     CHAIR_DAT CHR ";
        $query .= "     INNER JOIN CHAIR_STD_DAT CHRSTD ";
        $query .= "       ON CHR.YEAR = CHRSTD.YEAR ";
        $query .= "       AND CHR.SEMESTER = CHRSTD.SEMESTER ";
        $query .= "       AND CHR.CHAIRCD = CHRSTD.CHAIRCD ";
        $query .= "   WHERE ";
        $query .= "     CHR.YEAR = '{$model->year}' ";
        $query .= "     AND CHR.SEMESTER = '{$model->semester}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   STDRI.COURSECD ";
        $query .= "   , STDRI.MAJORCD ";
        $query .= "   , STDRI.GRADE ";
        $query .= "   , STDRI.COURSECODE ";
        $query .= "   , STDRI.CLASSCD ";
        $query .= "   , STDRI.SCHOOL_KIND ";
        $query .= "   , STDRI.CURRICULUM_CD ";
        $query .= "   , STDRI.SUBCLASSCD ";
        $query .= "   , COUNT(STDRI.SCHREGNO) STDCNT ";

        $query .= " FROM ";
        $query .= "   STDRI ";
        $query .= "   LEFT JOIN CHAIRSTD ";
        $query .= "     ON STDRI.YEAR = CHAIRSTD.YEAR ";
        $query .= "     AND STDRI.SEMESTER = CHAIRSTD.SEMESTER ";
        $query .= "     AND STDRI.SCHREGNO = CHAIRSTD.SCHREGNO ";
        $query .= "     AND STDRI.CLASSCD = CHAIRSTD.CLASSCD ";
        $query .= "     AND STDRI.SCHOOL_KIND = CHAIRSTD.SCHOOL_KIND ";
        $query .= "     AND STDRI.CURRICULUM_CD = CHAIRSTD.CURRICULUM_CD ";
        $query .= "     AND STDRI.SUBCLASSCD = CHAIRSTD.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "   CHAIRSTD.SCHREGNO IS NULL ";
        $query .= " GROUP BY ";
        $query .= "   STDRI.COURSECD ";
        $query .= "   , STDRI.MAJORCD ";
        $query .= "   , STDRI.GRADE ";
        $query .= "   , STDRI.COURSECODE ";
        $query .= "   , STDRI.CLASSCD ";
        $query .= "   , STDRI.SCHOOL_KIND ";
        $query .= "   , STDRI.CURRICULUM_CD ";
        $query .= "   , STDRI.SUBCLASSCD ";

        return $query;
    }

    /**
     * 施設の収容講座数が超えている講座チェック
     */
    function getFacCapOverCheck($model, $chairList) {

        $query = "";
        $query .= " WITH FACCHAIRCNT AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , FACCD ";
        $query .= "     , COUNT(CHAIRCD) CHAIRCNT ";
        $query .= "   FROM ";
        $query .= "     CHAIR_FAC_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHAIRCD IN ('".implode("','", $chairList)."') ";
        $query .= "   GROUP BY ";
        $query .= "     YEAR ";
        $query .= "     , SEMESTER ";
        $query .= "     , FACCD ";
        $query .= " ) ";
        $query .= " , FACCAPOVER AS ( ";
        $query .= "   SELECT ";
        $query .= "     M1.YEAR ";
        $query .= "     , M1.FACCD ";
        $query .= "     , M1.CHR_CAPACITY ";
        $query .= "     , FAC.CHAIRCNT ";
        $query .= "   FROM ";
        $query .= "     V_FACILITY_MST M1 ";
        $query .= "     INNER JOIN FACCHAIRCNT FAC ";
        $query .= "       ON M1.YEAR = FAC.YEAR ";
        $query .= "       AND M1.FACCD = FAC.FACCD ";
        $query .= "   WHERE ";
        $query .= "     M1.YEAR = '{$model->year}' ";
        $query .= "     AND M1.CHR_CAPACITY < FAC.CHAIRCNT ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.YEAR ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.CHAIRCD ";
        $query .= "   , T1.FACCD ";
        $query .= "   , CAP.CHR_CAPACITY ";
        $query .= "   , CAP.CHAIRCNT ";
        $query .= " FROM ";
        $query .= "   CHAIR_FAC_DAT T1 ";
        $query .= "   INNER JOIN FACCAPOVER CAP ";
        $query .= "     ON T1.FACCD = CAP.FACCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '{$model->year}' ";
        $query .= "   AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "   AND T1.CHAIRCD IN ('".implode("','", $chairList)."') ";

        return $query;
    }



    /**
     * 科目展開表更新(ヘッダ更新)
     */
    function updatePtrnPreTemplate($db, $model){

        if($model->field['PTRN_UPDATE_RADIO'] == 1){
            $data = array();
            $data["TITLE"][TEXT]        = $model->field['PTRN_UPDATE_TITLE'];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $where  = " WHERE YEAR      = '{$model->year}'";
            $where .= "   AND SEMESTER  = '{$model->semester}'";
            $where .= "   AND PRESEQ    = {$model->preSeq}";

            $query = Query::updateSQL($data, "SCH_PTRN_PRE_HDAT", $where);
            $db->query($query);
            $preSeq = $model->preSeq;
        }

        if($model->field['PTRN_UPDATE_RADIO'] == 2){
            $query .= " SELECT ";
            $query .= "     MAX(PRESEQ) ";
            $query .= " FROM ";
            $query .= "     SCH_PTRN_PRE_HDAT ";
            $query .= " WHERE YEAR = '{$model->year}' ";
            $query .= "   AND SEMESTER = '{$model->semester}' ";

            $preSeq = $db->getOne($query);
            if(!isset($preSeq)){
                $preSeq = 1;
            } else {
                $preSeq ++;
            }

            $data = array();
            $data["YEAR"][TEXT]         = $model->year;
            $data["SEMESTER"][TEXT]     = $model->semester;
            $data["PRESEQ"][NUMBER]     = $preSeq;
            $data["TITLE"][TEXT]        = $model->field['PTRN_UPDATE_TITLE'];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "SCH_PTRN_PRE_HDAT");
            $db->query($query);
        }
        return $preSeq;
    }

    /**
     * 科目展開表の更新
     */
    function updatePtrnPre($db, $model) {

        // データの削除
        // 変更のあったコースを削除
        for ($i = 0; $i < get_count($model->updateDataList); $i++) {
            $delData = $model->updateDataList[$i];

            // コースID
            $coursecd = explode('-', $delData['COURSE']);

            $query  = " DELETE FROM ";
            $query .= "     SCH_PTRN_PRE_DAT ";
            $query .= " WHERE YEAR = '{$model->year}' ";
            $query .= "   AND SEMESTER = '{$model->semester}' ";
            $query .= "   AND PRESEQ = {$model->preSeq} ";
            $query .= "   AND COURSECD = '{$coursecd[0]}' ";
            $query .= "   AND MAJORCD = '{$coursecd[1]}' ";
            $query .= "   AND GRADE = '{$coursecd[2]}' ";
            $query .= "   AND COURSECODE = '{$coursecd[3]}' ";

            $db->query($query);
        }

        // 変更のあったコースを追加(科目情報が無いのは追加しない)
        for ($i = 0; $i < get_count($model->updateDataList); $i++) {
            $addData = $model->updateDataList[$i];

            // コースID
            $coursecd = explode('-', $addData['COURSE']);

            $subclassList = $addData['SUBCLASS'];
            for ($j=0; $j < get_count($subclassList); $j++) {
                $subclass = $subclassList[$j];

                $data = array();
                $data["YEAR"][TEXT]          = $model->year;
                $data["SEMESTER"][TEXT]      = $model->semester;

                $data["PRESEQ"][NUMBER]      = $model->preSeq;

                $data["COURSECD"][TEXT]      = $coursecd[0];
                $data["MAJORCD"][TEXT]       = $coursecd[1];
                $data["GRADE"][TEXT]         = $coursecd[2];
                $data["COURSECODE"][TEXT]    = $coursecd[3];
                // 科目情報
                $data["PRE_ORDER"][NUMBER]   = $subclass['preOrder'];
                $data["CLASSCD"][TEXT]       = $subclass['classcd'];
                $data["SCHOOL_KIND"][TEXT]   = $subclass['school_kind'];
                $data["CURRICULUM_CD"][TEXT] = $subclass['curriculum_cd'];
                $data["SUBCLASSCD"][TEXT]    = $subclass['subclasscd'];

                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][NUMBER]     = "sysdate()";

                $query = Query::insertSQL($data, "SCH_PTRN_PRE_DAT");
                $db->query($query);
            }

        }
    }

    // 科目展開表 テンプレート削除
    function deletePtrnPreTemplate($db, $model) {
        // SCH_PTRN_PRE_HDAT DEL
        $query  = " DELETE FROM SCH_PTRN_PRE_HDAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= "   AND PRESEQ = {$model->preSeq} ";
        $db->query($query);

        // SCH_PTRN_PRE_DAT DEL
        $query  = " DELETE FROM SCH_PTRN_PRE_DAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= "   AND PRESEQ = {$model->preSeq} ";
        $db->query($query);
    }

    /**
     * 講座展開表更新(ヘッダ更新)
     */
    function updatePtrnPreChairTemplate($db, $model){

        if($model->field['PTRN_UPDATE_RADIO'] == 1){
            $data = array();
            $data["TITLE"][TEXT]        = $model->field['PTRN_UPDATE_TITLE'];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $where  = " WHERE YEAR      = '{$model->year}'";
            $where .= "   AND SEMESTER  = '{$model->semester}'";
            $where .= "   AND PRESEQ    = {$model->preChairSeq}";

            $query = Query::updateSQL($data, "SCH_PTRN_PRE_CHR_HDAT", $where);
            $db->query($query);
            $preSeq = $model->preChairSeq;
        }

        if($model->field['PTRN_UPDATE_RADIO'] == 2){
            $query .= " SELECT ";
            $query .= "     MAX(PRESEQ) ";
            $query .= " FROM ";
            $query .= "     SCH_PTRN_PRE_CHR_HDAT ";
            $query .= " WHERE YEAR = '{$model->year}' ";
            $query .= "   AND SEMESTER = '{$model->semester}' ";

            $preSeq = $db->getOne($query);
            if(!isset($preSeq)){
                $preSeq = 1;
            } else {
                $preSeq ++;
            }

            $data = array();
            $data["YEAR"][TEXT]         = $model->year;
            $data["SEMESTER"][TEXT]     = $model->semester;
            $data["PRESEQ"][NUMBER]     = $preSeq;
            $data["TITLE"][TEXT]        = $model->field['PTRN_UPDATE_TITLE'];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "SCH_PTRN_PRE_CHR_HDAT");
            $db->query($query);
        }
        return $preSeq;
    }

    /**
     * 講座展開表の更新
     */
    function updatePtrnPreChair($db, $model) {

        // データの削除
        // 変更のあったコースを削除
        for ($i = 0; $i < get_count($model->updateDataList); $i++) {
            $delData = $model->updateDataList[$i];

            // コースID
            $coursecd = explode('-', $delData['COURSE']);
            // HR_CLASS
            $hrClass = explode('-', $delData['HR_CLASS']);
            // ORDER
            $order = $delData['ORDER'];

            $query  = " DELETE FROM ";
            $query .= "     SCH_PTRN_PRE_CHR_DAT ";
            $query .= " WHERE YEAR = '{$model->year}' ";
            $query .= "   AND SEMESTER = '{$model->semester}' ";
            $query .= "   AND PRESEQ = {$model->preChairSeq} ";
            $query .= "   AND COURSECD = '{$coursecd[0]}' ";
            $query .= "   AND MAJORCD = '{$coursecd[1]}' ";
            $query .= "   AND COURSECODE = '{$coursecd[3]}' ";
            $query .= "   AND GRADE = '{$coursecd[2]}' ";
            $query .= "   AND HR_CLASS = '{$hrClass[1]}' ";
            $query .= "   AND PRE_ORDER = '{$order}' ";

            $db->query($query);
        }
        // 変更のあったコースを追加(科目情報が無いのは追加しない)
        for ($i = 0; $i < get_count($model->updateDataList); $i++) {
            $addData = $model->updateDataList[$i];

            // コースID
            $coursecd = explode('-', $addData['COURSE']);
            // HR_CLASS
            $hrClass = explode('-', $addData['HR_CLASS']);
            // ORDER
            $order = $addData['ORDER'];

            $chairList = $addData['CHAIR'];
            for ($j=0; $j < get_count($chairList); $j++) {
                // $subclass = $subclassList[$j];
                $chair = $chairList[$j];

                $data = array();
                $data["YEAR"][TEXT]          = $model->year;
                $data["SEMESTER"][TEXT]      = $model->semester;

                $data["PRESEQ"][NUMBER]      = $model->preChairSeq;

                $data["COURSECD"][TEXT]      = $coursecd[0];
                $data["MAJORCD"][TEXT]       = $coursecd[1];
                $data["COURSECODE"][TEXT]    = $coursecd[3];
                $data["GRADE"][TEXT]         = $coursecd[2];
                $data["HR_CLASS"][TEXT]      = $hrClass[1];
                $data["PRE_ORDER"][NUMBER]   = $order;
                // 講座情報
                $data["CHAIRCD"][TEXT]       = $chair;

                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][NUMBER]     = "sysdate()";

                $query = Query::insertSQL($data, "SCH_PTRN_PRE_CHR_DAT");
                $db->query($query);
            }

        }
    }

    // 講座展開表 テンプレート削除
    function deletePtrnPreChairTemplate($db, $model) {
        // SCH_PTRN_PRE_CHR_HDAT DEL
        $query  = " DELETE FROM SCH_PTRN_PRE_CHR_HDAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= "   AND PRESEQ = {$model->preChairSeq} ";
        $db->query($query);

        // SCH_PTRN_PRE_CHR_DAT DEL
        $query  = " DELETE FROM SCH_PTRN_PRE_CHR_DAT ";
        $query .= " WHERE YEAR = '{$model->year}' ";
        $query .= "   AND SEMESTER = '{$model->semester}' ";
        $query .= "   AND PRESEQ = {$model->preChairSeq} ";
        $db->query($query);

    }

}
?>
