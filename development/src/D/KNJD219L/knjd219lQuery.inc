<?php

require_once('for_php7.php');
class knjd219lQuery extends Query {

    //学年取得
    function getSelectGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER IN ('1', '9') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getClass($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."'";
            if ($model->field["SEMESTER"] !== '9') {
                $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            } else {
                $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            }
            $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."'";
            if ($model->field["SEMESTER"] !== '9') {
                $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            } else {
                $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            }
            $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";
            $query .= "     AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }
    
    
    
    //名称マスタA034
    function getMypDp($model)
    {
        $query  = " SELECT ";
        $query .= "    NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = 'A034' ";
        $query .= " AND '".$model->field["GRADE"]."' BETWEEN NAME2 AND NAME3 ";

        return $query;
    }

    
    //対象データ取得
    function getSelectData($model)
    {
        $select_array = array();
        $select_array = explode(',', $model->selectdata);

        $query  = "WITH IBSCORE_RECORD AS ( ";
        $query .= "SELECT ";
        $query .= "    L0.SEMESTER, ";
        $query .= "    L0.CLASSCD, ";
        $query .= "    L0.SCHOOL_KIND, ";
        $query .= "    L0.CURRICULUM_CD, ";
        $query .= "    L0.SUBCLASSCD, ";
        $query .= "    T1.IBSEQ, ";
        $query .= "    T1.IBYEAR, ";
        $query .= "    T1.IBGRADE, ";
        $query .= "    T1.IBCLASSCD, ";
        $query .= "    T1.IBPRG_COURSE, ";
        $query .= "    T1.IBCURRICULUM_CD, ";
        $query .= "    T1.IBSUBCLASSCD, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    L2.IBPERFECT, ";
        $query .= "    T1.VIEWCD, ";
        $query .= "    T1.SCORE ";
        $query .= "FROM ";
        $query .= "    IBSUBCLASS_SCORE_DAT T1 ";
        $query .= "    INNER JOIN IBSUBCLASS_UNIT_DAT L0     ON L0.IBYEAR = T1.IBYEAR ";
        $query .= "                                         AND L0.IBGRADE = T1.IBGRADE ";
        $query .= "                                         AND L0.IBCLASSCD = T1.IBCLASSCD ";
        $query .= "                                         AND L0.IBPRG_COURSE = T1.IBPRG_COURSE ";
        $query .= "                                         AND L0.IBCURRICULUM_CD = T1.IBCURRICULUM_CD ";
        $query .= "                                         AND L0.IBSUBCLASSCD = T1.IBSUBCLASSCD ";
        $query .= "                                         AND L0.IBSEQ = T1.IBSEQ ";
        if ($model->field["SEMESTER"] !== '9') {
            $query .= "                                         AND L0.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "    INNER JOIN IBSUBCLASS_UNITPLAN_DAT L1 ON L1.IBYEAR = T1.IBYEAR ";
        $query .= "                                         AND L1.IBGRADE = T1.IBGRADE ";
        $query .= "                                         AND L1.IBCLASSCD = T1.IBCLASSCD ";
        $query .= "                                         AND L1.IBPRG_COURSE = T1.IBPRG_COURSE ";
        $query .= "                                         AND L1.IBCURRICULUM_CD = T1.IBCURRICULUM_CD ";
        $query .= "                                         AND L1.IBSUBCLASSCD = T1.IBSUBCLASSCD ";
        $query .= "                                         AND L1.IBSEQ = T1.IBSEQ ";
        $query .= "                                         AND L1.IBEVAL_DIV1 = T1.IBEVAL_DIV1 ";
        $query .= "                                         AND L1.IBEVAL_DIV2 = T1.IBEVAL_DIV2 ";
        $query .= "                                         AND L1.IBEVAL_MARK = T1.IBEVAL_MARK ";
        $query .= "                                         AND L1.VIEWCD = T1.VIEWCD ";
        $query .= "    INNER JOIN IBVIEW_NAME_YMST L2        ON L2.IBYEAR = T1.IBYEAR ";
        $query .= "                                         AND L2.IBGRADE = T1.IBGRADE ";
        $query .= "                                         AND L2.IBCLASSCD = T1.IBCLASSCD ";
        $query .= "                                         AND L2.IBPRG_COURSE = T1.IBPRG_COURSE ";
        $query .= "                                         AND L2.IBCURRICULUM_CD = T1.IBCURRICULUM_CD ";
        $query .= "                                         AND L2.IBSUBCLASSCD = T1.IBSUBCLASSCD ";
        $query .= "                                         AND L2.IBEVAL_DIV1 = T1.IBEVAL_DIV1 ";
        $query .= "                                         AND L2.IBEVAL_DIV2 = T1.IBEVAL_DIV2 ";
        $query .= "                                         AND L2.IBEVAL_MARK = T1.IBEVAL_MARK ";
        $query .= "    LEFT JOIN SCHREG_REGD_DAT H1         ON H1.YEAR = T1.IBYEAR ";
        $query .= "                                         AND H1.SCHREGNO = T1.SCHREGNO ";
        if ($model->field["SEMESTER"] !== '9') {
            $query .= "                                         AND H1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        } else {
            $query .= "                                         AND H1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= "                                         AND H1.GRADE = T1.IBGRADE ";
        $query .= "WHERE ";
        $query .= "    T1.IBYEAR = '".CTRL_YEAR."' ";
        $query .= "AND T1.IBGRADE = '".$model->field["GRADE"]."' ";
        $query .= "AND T1.SCORE IS NOT NULL ";
        //クラスを指定する場合
        if ($model->field["OUTPUT_DIV"] == "1") {
            $query .= "AND H1.GRADE || H1.HR_CLASS IN ( ";
            $connma = "";
            foreach ($select_array as $value) {
                $query .= " $connma'".$value."' ";
                $connma = ",";
            }
            $query .= " ) ";
        }
        $query .= "), KANTEN_IBSCORE_RECORD AS ( ";
        $query .= "SELECT ";
        $query .= "    T1.SEMESTER, ";
        $query .= "    T1.CLASSCD, ";
        $query .= "    T1.SCHOOL_KIND, ";
        $query .= "    T1.CURRICULUM_CD, ";
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    T1.IBYEAR, ";
        $query .= "    T1.IBGRADE, ";
        $query .= "    T1.IBCLASSCD, ";
        $query .= "    T1.IBPRG_COURSE, ";
        $query .= "    T1.IBCURRICULUM_CD, ";
        $query .= "    T1.IBSUBCLASSCD, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.VIEWCD, ";
        $query .= "    SUM(T1.IBPERFECT) AS KANTEN_IBPERFECT, ";
        $query .= "    SUM(T1.SCORE) AS KANTEN_SCORE, ";
        $query .= "    CAST(ROUND(1.0 * SUM(T1.SCORE)/SUM(T1.IBPERFECT) * 100, 1) AS DECIMAL(4, 1)) AS CUTTING_DATA ";
        $query .= "FROM ";
        $query .= "    IBSCORE_RECORD T1 ";
        $query .= "GROUP BY ";
        $query .= "    T1.SEMESTER, ";
        $query .= "    T1.CLASSCD, ";
        $query .= "    T1.SCHOOL_KIND, ";
        $query .= "    T1.CURRICULUM_CD, ";
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    T1.IBYEAR, ";
        $query .= "    T1.IBGRADE, ";
        $query .= "    T1.IBCLASSCD, ";
        $query .= "    T1.IBPRG_COURSE, ";
        $query .= "    T1.IBCURRICULUM_CD, ";
        $query .= "    T1.IBSUBCLASSCD, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.VIEWCD ";
        if ($model->field["SEMESTER"] === '9') {
            $query .= "UNION ";
            $query .= "SELECT ";
            $query .= "    '9' AS SEMESTER, ";
            $query .= "    T1.CLASSCD, ";
            $query .= "    T1.SCHOOL_KIND, ";
            $query .= "    T1.CURRICULUM_CD, ";
            $query .= "    T1.SUBCLASSCD, ";
            $query .= "    T1.IBYEAR, ";
            $query .= "    T1.IBGRADE, ";
            $query .= "    T1.IBCLASSCD, ";
            $query .= "    T1.IBPRG_COURSE, ";
            $query .= "    T1.IBCURRICULUM_CD, ";
            $query .= "    T1.IBSUBCLASSCD, ";
            $query .= "    T1.SCHREGNO, ";
            $query .= "    T1.VIEWCD, ";
            $query .= "    SUM(T1.IBPERFECT) AS KANTEN_IBPERFECT, ";
            $query .= "    SUM(T1.SCORE) AS KANTEN_SCORE, ";
            $query .= "    CAST(ROUND(1.0 * SUM(T1.SCORE)/SUM(T1.IBPERFECT) * 100, 1) AS DECIMAL(4, 1)) AS CUTTING_DATA ";
            $query .= "FROM ";
            $query .= "    IBSCORE_RECORD T1 ";
            $query .= "GROUP BY ";
            $query .= "    T1.CLASSCD, ";
            $query .= "    T1.SCHOOL_KIND, ";
            $query .= "    T1.CURRICULUM_CD, ";
            $query .= "    T1.SUBCLASSCD, ";
            $query .= "    T1.IBYEAR, ";
            $query .= "    T1.IBGRADE, ";
            $query .= "    T1.IBCLASSCD, ";
            $query .= "    T1.IBPRG_COURSE, ";
            $query .= "    T1.IBCURRICULUM_CD, ";
            $query .= "    T1.IBSUBCLASSCD, ";
            $query .= "    T1.SCHREGNO, ";
            $query .= "    T1.VIEWCD ";
        }

        $query .= "), KANTEN_IBSCORE_RECORD_COMBINED AS ( ";
        $query .= "SELECT  ";
        $query .= "    T1.SEMESTER,  ";
        $query .= "    T1.CLASSCD,  ";
        $query .= "    T1.SCHOOL_KIND,  ";
        $query .= "    T1.CURRICULUM_CD,  ";
        $query .= "    T1.SUBCLASSCD,  ";
        $query .= "    T1.IBYEAR,  ";
        $query .= "    T1.IBGRADE,  ";
        $query .= "    T1.IBCLASSCD,  ";
        $query .= "    T1.IBPRG_COURSE,  ";
        $query .= "    T1.IBCURRICULUM_CD,  ";
        $query .= "    T1.IBSUBCLASSCD,  ";
        $query .= "    T1.SCHREGNO,  ";
        $query .= "    T1.VIEWCD,  ";
        $query .= "    T1.KANTEN_IBPERFECT,  ";
        $query .= "    T1.KANTEN_SCORE,  ";
        $query .= "    T1.CUTTING_DATA  ";
        $query .= "FROM  ";
        $query .= "    KANTEN_IBSCORE_RECORD T1  ";
        $query .= "UNION ALL ";
        $query .= "SELECT  ";
        $query .= "    T1.SEMESTER,  ";
        $query .= "    I1.COMBINED_CLASSCD AS CLASSCD,  ";
        $query .= "    I1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND,  ";
        $query .= "    I1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD,  ";
        $query .= "    I1.COMBINED_SUBCLASSCD AS SUBCLASSCD,  ";
        $query .= "    T1.IBYEAR,  ";
        $query .= "    T1.IBGRADE,  ";
        $query .= "    CAST(NULL AS VARCHAR(2)) AS IBCLASSCD,  ";
        $query .= "    T1.IBPRG_COURSE,  ";
        $query .= "    CAST(NULL AS VARCHAR(2)) AS IBCURRICULUM_CD,  ";
        $query .= "    CAST(NULL AS VARCHAR(6)) AS IBSUBCLASSCD,  ";
        $query .= "    T1.SCHREGNO,  ";
        $query .= "    T1.VIEWCD,  ";
        $query .= "    SUM(KANTEN_IBPERFECT) AS KANTEN_IBPERFECT,  ";
        $query .= "    SUM(KANTEN_SCORE) AS KANTEN_SCORE,  ";
        $query .= "    CAST(ROUND(1.0 * SUM(KANTEN_SCORE)/SUM(KANTEN_IBPERFECT) * 100, 1) AS DECIMAL(4, 1)) AS CUTTING_DATA  ";
        $query .= "FROM  ";
        $query .= "    KANTEN_IBSCORE_RECORD T1 ";
        $query .= "INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT I1 ON I1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = ";
        $query .= "    I1.ATTEND_CLASSCD || '-' || I1.ATTEND_SCHOOL_KIND || '-' || I1.ATTEND_CURRICULUM_CD || '-' || I1.ATTEND_SUBCLASSCD ";
        $query .= "GROUP BY  ";
        $query .= "    T1.SEMESTER,  ";
        $query .= "    I1.COMBINED_CLASSCD,  ";
        $query .= "    I1.COMBINED_SCHOOL_KIND,  ";
        $query .= "    I1.COMBINED_CURRICULUM_CD,  ";
        $query .= "    I1.COMBINED_SUBCLASSCD,  ";
        $query .= "    T1.IBYEAR,  ";
        $query .= "    T1.IBGRADE,  ";
        $query .= "    T1.IBPRG_COURSE,  ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.VIEWCD ";
        $query .= "), TOTAL_IBSCORE_RECORD AS (  ";
        $query .= "SELECT  ";
        $query .= "    SEMESTER,  ";
        $query .= "    CLASSCD,  ";
        $query .= "    SCHOOL_KIND,  ";
        $query .= "    CURRICULUM_CD,  ";
        $query .= "    SUBCLASSCD,  ";
        $query .= "    IBYEAR,  ";
        $query .= "    IBGRADE,  ";
        $query .= "    IBCLASSCD,  ";
        $query .= "    IBPRG_COURSE,  ";
        $query .= "    IBCURRICULUM_CD,  ";
        $query .= "    IBSUBCLASSCD,  ";
        $query .= "    SCHREGNO,  ";
        $query .= "    '0000' AS VIEWCD,  ";
        $query .= "    SUM(KANTEN_IBPERFECT) AS KANTEN_IBPERFECT,  ";
        $query .= "    SUM(KANTEN_SCORE) AS KANTEN_SCORE,  ";
        $query .= "    CAST(ROUND(1.0 * SUM(KANTEN_SCORE)/SUM(KANTEN_IBPERFECT) * 100, 1) AS DECIMAL(4, 1)) AS CUTTING_DATA  ";
        $query .= "FROM  ";
        $query .= "    KANTEN_IBSCORE_RECORD_COMBINED   ";
        $query .= "GROUP BY  ";
        $query .= "    SEMESTER,  ";
        $query .= "    CLASSCD,  ";
        $query .= "    SCHOOL_KIND,  ";
        $query .= "    CURRICULUM_CD,  ";
        $query .= "    SUBCLASSCD,  ";
        $query .= "    IBYEAR,  ";
        $query .= "    IBGRADE,  ";
        $query .= "    IBCLASSCD,  ";
        $query .= "    IBPRG_COURSE,  ";
        $query .= "    IBCURRICULUM_CD,  ";
        $query .= "    IBSUBCLASSCD,  ";
        $query .= "    SCHREGNO  ";
        $query .= "), T_IBVIEW_CUTTING AS (  ";
        $query .= "SELECT ";
        $query .= "    T1.YEAR, ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.IBPRG_COURSE, ";
        $query .= "    T1.CLASSCD, ";
        $query .= "    T1.SCHOOL_KIND, ";
        $query .= "    T1.CURRICULUM_CD, ";
        $query .= "    T1.SUBCLASSCD, ";
        $query .= "    T1.DATA_DIV, ";
        $query .= "    T1.SEQ, ";
        $query .= "    T1.CUTTING_MARK, ";
        $query .= "    T1.CUTTING_LOW, ";
        $query .= "    T1.CUTTING_HIGH ";
        $query .= "FROM IBVIEW_CUTTING_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        $query .= "    T1.YEAR, ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.IBPRG_COURSE, ";
        $query .= "    I1.COMBINED_CLASSCD AS CLASSCD, ";
        $query .= "    I1.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ";
        $query .= "    I1.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ";
        $query .= "    I1.COMBINED_SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "    T1.DATA_DIV, ";
        $query .= "    T1.SEQ, ";
        $query .= "    MAX(T1.CUTTING_MARK) AS CUTTING_MARK, ";
        $query .= "    MIN(T1.CUTTING_LOW) AS CUTTING_LOW, ";
        $query .= "    MAX(T1.CUTTING_HIGH) AS CUTTING_HIGH ";
        $query .= "FROM IBVIEW_CUTTING_DAT T1 ";
        $query .= "INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT I1 ON I1.YEAR = T1.YEAR AND ";
        $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = ";
        $query .= "    I1.ATTEND_CLASSCD || '-' || I1.ATTEND_SCHOOL_KIND || '-' || I1.ATTEND_CURRICULUM_CD || '-' || I1.ATTEND_SUBCLASSCD ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "GROUP BY ";
        $query .= "    T1.YEAR, ";
        $query .= "    T1.GRADE, ";
        $query .= "    T1.IBPRG_COURSE, ";
        $query .= "    I1.COMBINED_CLASSCD, ";
        $query .= "    I1.COMBINED_SCHOOL_KIND, ";
        $query .= "    I1.COMBINED_CURRICULUM_CD, ";
        $query .= "    I1.COMBINED_SUBCLASSCD, ";
        $query .= "    T1.DATA_DIV, ";
        $query .= "    T1.SEQ ";
        $query .= ") ";
        if ($model->isMyp) {
            $query .= "SELECT ";
            $query .= "    T1.*, ";
            $query .= "    '1' AS DATA_DIV, ";
            if ($model->Properties["KNJD126J_useCtlHyoutei"] == "1" &&  $model->Properties["displayHyoutei"] != "1" && $model->Properties["displayHyoutei"] != "2") {
                $query .= "    T4.PROV_FLG, ";
            }
            $query .= "    CASE WHEN L1.SEQ IS NOT NULL THEN L1.SEQ ELSE L2.SEQ END AS SEQ, ";
            $query .= "    CASE WHEN L1.CUTTING_MARK IS NOT NULL THEN L1.CUTTING_MARK WHEN L1.CUTTING_MARK IS NULL AND L2.CUTTING_MARK IS NOT NULL THEN L2.CUTTING_MARK ELSE L1.CUTTING_MARK END AS KANTEN_CUTTING_MARK ";
            $query .= "FROM ";
            $query .= "    KANTEN_IBSCORE_RECORD_COMBINED T1 ";
            $query .= "    LEFT JOIN T_IBVIEW_CUTTING L1 ON L1.YEAR = T1.IBYEAR ";
            $query .= "                                   AND L1.GRADE = T1.IBGRADE ";
            $query .= "                                   AND L1.IBPRG_COURSE = T1.IBPRG_COURSE ";
            $query .= "                                   AND L1.CLASSCD = T1.CLASSCD ";
            $query .= "                                   AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                   AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                                   AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "                                   AND L1.DATA_DIV = '1' ";
            $query .= "                                   AND T1.CUTTING_DATA BETWEEN L1.CUTTING_LOW AND L1.CUTTING_HIGH ";
            $query .= "    LEFT JOIN T_IBVIEW_CUTTING L2 ON L2.YEAR = T1.IBYEAR ";
            $query .= "                                   AND L2.GRADE = T1.IBGRADE ";
            $query .= "                                   AND L2.IBPRG_COURSE = T1.IBPRG_COURSE ";
            $query .= "                                   AND L2.CLASSCD = '00' ";
            $query .= "                                   AND L2.SCHOOL_KIND = '00' ";
            $query .= "                                   AND L2.CURRICULUM_CD = '00' ";
            $query .= "                                   AND L2.SUBCLASSCD = '000000' ";
            $query .= "                                   AND L2.DATA_DIV = '1' ";
            $query .= "                                   AND T1.CUTTING_DATA BETWEEN L2.CUTTING_LOW AND L2.CUTTING_HIGH ";
            if ($model->Properties["KNJD126J_useCtlHyoutei"] == "1" &&  $model->Properties["displayHyoutei"] != "1" && $model->Properties["displayHyoutei"] != "2") {
                $query .= "  LEFT JOIN JVIEWSTAT_RECORD_PROV_FLG_DAT T4 ";
                $query .= "    ON T4.YEAR          = T1.IBYEAR ";
                $query .= "   AND T4.SEMESTER      = '9' ";
                $query .= "   AND T4.TESTKINDCD    = '99' ";
                $query .= "   AND T4.TESTITEMCD    = '00' ";
                $query .= "   AND T4.SCORE_DIV     = '09' ";
                $query .= "   AND T4.CLASSCD       = T1.CLASSCD ";
                $query .= "   AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ";
                $query .= "   AND T4.CURRICULUM_CD = T1.CURRICULUM_CD";
                $query .= "   AND T4.SUBCLASSCD    = T1.SUBCLASSCD";
                $query .= "   AND T4.SCHREGNO      = T1.SCHREGNO ";
                $query .= "   AND T4.PROV_DIV      = '01' ";
            }
            $query .= " ";
            $query .= "UNION ";
        }
        $query .= "SELECT ";
        $query .= "    T1.*, ";
        $query .= "    '2' AS DATA_DIV, ";
        if ($model->Properties["KNJD126J_useCtlHyoutei"] == "1" &&  $model->Properties["displayHyoutei"] != "1" && $model->Properties["displayHyoutei"] != "2") {
            $query .= "    T4.PROV_FLG, ";
        }
        $query .= "    CASE WHEN L1.SEQ IS NOT NULL THEN L1.SEQ ELSE L2.SEQ END AS SEQ, ";
        $query .= "    CASE WHEN L1.CUTTING_MARK IS NOT NULL THEN L1.CUTTING_MARK WHEN L1.CUTTING_MARK IS NULL AND L2.CUTTING_MARK IS NOT NULL THEN L2.CUTTING_MARK ELSE L1.CUTTING_MARK END AS KANTEN_CUTTING_MARK ";
        $query .= "FROM ";
        $query .= "    TOTAL_IBSCORE_RECORD T1 ";
        $query .= "    LEFT JOIN T_IBVIEW_CUTTING L1 ON L1.YEAR = T1.IBYEAR ";
        $query .= "                                   AND L1.GRADE = T1.IBGRADE ";
        $query .= "                                   AND L1.IBPRG_COURSE = T1.IBPRG_COURSE ";
        $query .= "                                   AND L1.CLASSCD = T1.CLASSCD ";
        $query .= "                                   AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                   AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                                   AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                                   AND L1.DATA_DIV = '2' ";
        $query .= "                                   AND T1.CUTTING_DATA BETWEEN L1.CUTTING_LOW AND L1.CUTTING_HIGH ";
        $query .= "    LEFT JOIN T_IBVIEW_CUTTING L2 ON L2.YEAR = T1.IBYEAR ";
        $query .= "                                   AND L2.GRADE = T1.IBGRADE ";
        $query .= "                                   AND L2.IBPRG_COURSE = T1.IBPRG_COURSE ";
        $query .= "                                   AND L2.CLASSCD = '00' ";
        $query .= "                                   AND L2.SCHOOL_KIND = '00' ";
        $query .= "                                   AND L2.CURRICULUM_CD = '00' ";
        $query .= "                                   AND L2.SUBCLASSCD = '000000' ";
        $query .= "                                   AND L2.DATA_DIV = '2' ";
        $query .= "                                   AND T1.CUTTING_DATA BETWEEN L2.CUTTING_LOW AND L2.CUTTING_HIGH ";
        if ($model->Properties["KNJD126J_useCtlHyoutei"] == "1" &&  $model->Properties["displayHyoutei"] != "1" && $model->Properties["displayHyoutei"] != "2") {
            $query .= "  LEFT JOIN JVIEWSTAT_RECORD_PROV_FLG_DAT T4 ";
            $query .= "    ON T4.YEAR          = T1.IBYEAR ";
            $query .= "   AND T4.SEMESTER      = '9' ";
            $query .= "   AND T4.TESTKINDCD    = '99' ";
            $query .= "   AND T4.TESTITEMCD    = '00' ";
            $query .= "   AND T4.SCORE_DIV     = '09' ";
            $query .= "   AND T4.CLASSCD       = T1.CLASSCD ";
            $query .= "   AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "   AND T4.CURRICULUM_CD = T1.CURRICULUM_CD";
            $query .= "   AND T4.SUBCLASSCD    = T1.SUBCLASSCD";
            $query .= "   AND T4.SCHREGNO      = T1.SCHREGNO ";
            $query .= "   AND T4.PROV_DIV      = '01' ";
        }
        $query .= "ORDER BY ";
        $query .= "    SCHREGNO, ";
        $query .= "    IBYEAR, ";
        $query .= "    IBGRADE, ";
        $query .= "    SEMESTER, ";
        $query .= "    VIEWCD ";

        return $query;
    }

    //対象データ削除
    function DeleteQuery($db, $row, $model) {
    
        $query  = "DELETE FROM ";
        $query .= "    JVIEWSTAT_RECORD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$row["IBYEAR"]."' ";
        $query .= "AND SCHREGNO = '".$row["SCHREGNO"]."' ";
        $query .= "AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $db->query($query);
        
        $query  = "DELETE FROM ";
        $query .= "    JVIEWSTAT_RECORD_DETAIL_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$row["IBYEAR"]."' ";
        $query .= "AND SCHREGNO = '".$row["SCHREGNO"]."' ";
        $query .= "AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $db->query($query);
        
        return;
    }

    //RECORD_SCORE_DATの確認
    function CheckRecordScoreDat($row) {
    
        $query  = "SELECT ";
        $query .= "    COUNT(*) AS CNT ";
        $query .= "FROM ";
        $query .= "    RECORD_SCORE_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$row["IBYEAR"]."' ";
        $query .= "AND SEMESTER = '".$row["SEMESTER"]."' ";
        $query .= "AND TESTKINDCD = '99' ";
        $query .= "AND TESTITEMCD = '00' ";
        $query .= "AND SCORE_DIV = '09' ";
        $query .= "AND CLASSCD = '".$row["CLASSCD"]."' ";
        $query .= "AND SCHOOL_KIND = '".$row["SCHOOL_KIND"]."' ";
        $query .= "AND CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' ";
        $query .= "AND SUBCLASSCD = '".$row["SUBCLASSCD"]."' ";
        $query .= "AND SCHREGNO = '".$row["SCHREGNO"]."' ";
        
        return $query;
    }

    //対象データ更新
    function updateQuery($db, $row, $model) {
        
        //指定学期のみ実行
        if ($model->field["SEMESTER"] == $row["SEMESTER"]) {
            $data = array();
            $data["YEAR"][TEXT]           = $row["IBYEAR"];
            $data["SEMESTER"][TEXT]       = $row["SEMESTER"];
            $data["SCHREGNO"][TEXT]       = $row["SCHREGNO"];
            $data["CLASSCD"][TEXT]        = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]    = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]  = $row["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]     = $row["SUBCLASSCD"];
            $data["VIEWCD"][TEXT]         = $row["VIEWCD"];
            if ($row["DATA_DIV"] == '2') {
                $data["STATUS"][TEXT]         = $row["SEQ"];
            } else {
                $data["STATUS"][TEXT]         = $row["KANTEN_CUTTING_MARK"];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            //更新
            $query = Query::insertSQL($data, "JVIEWSTAT_RECORD_DAT");
            $db->query($query);
            
            $data = array();
            $data["YEAR"][TEXT]           = $row["IBYEAR"];
            $data["SEMESTER"][TEXT]       = $row["SEMESTER"];
            $data["SCHREGNO"][TEXT]       = $row["SCHREGNO"];
            $data["CLASSCD"][TEXT]        = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]    = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]  = $row["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]     = $row["SUBCLASSCD"];
            $data["VIEWCD"][TEXT]         = $row["VIEWCD"];
            $data["REMARK1"][NUMBER]      = $row["KANTEN_SCORE"];
            $data["REMARK2"][NUMBER]      = $row["KANTEN_IBPERFECT"];
            $data["REMARK3"][NUMBER]      = $row["CUTTING_DATA"];
            
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]     = "sysdate()";
            //更新
            $query = Query::insertSQL($data, "JVIEWSTAT_RECORD_DETAIL_DAT");
            $db->query($query);
            
            //RECORD_SCORE_DATの更新
            if ($row["SEMESTER"] === '9' && $row["VIEWCD"] === '0000') {
                //入力画面で、評定が入力可能かつチェックが付いているデータであれば更新しない
                if ($model->Properties["KNJD126J_useCtlHyoutei"] == "1" &&  $model->Properties["displayHyoutei"] != "1" && $model->Properties["displayHyoutei"] != "2") {
                    if ($row["PROV_FLG"] == "1") {
                        return;
                    }
                }
                $query = knjd219lQuery::CheckRecordScoreDat($row);
                $checkCnt = $db->getOne($query);
                
                $data = array();
                $data["SCORE"][NUMBER]         = $row["SEQ"];
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][NUMBER]     = "sysdate()";
                if ($checkCnt == 0) {
                    $data["YEAR"][TEXT]           = $row["IBYEAR"];
                    $data["SEMESTER"][TEXT]       = $row["SEMESTER"];
                    $data["TESTKINDCD"][TEXT]     = "99";
                    $data["TESTITEMCD"][TEXT]     = "00";
                    $data["SCORE_DIV"][TEXT]      = "09";
                    $data["CLASSCD"][TEXT]        = $row["CLASSCD"];
                    $data["SCHOOL_KIND"][TEXT]    = $row["SCHOOL_KIND"];
                    $data["CURRICULUM_CD"][TEXT]  = $row["CURRICULUM_CD"];
                    $data["SUBCLASSCD"][TEXT]     = $row["SUBCLASSCD"];
                    $data["SCHREGNO"][TEXT]       = $row["SCHREGNO"];
                    //追加
                    $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
                } else {
                    $where  = "WHERE ";
                    $where .= "    YEAR = '".$row["IBYEAR"]."' ";
                    $where .= "AND SEMESTER = '".$row["SEMESTER"]."' ";
                    $where .= "AND TESTKINDCD = '99' ";
                    $where .= "AND TESTITEMCD = '00' ";
                    $where .= "AND SCORE_DIV = '09' ";
                    $where .= "AND CLASSCD = '".$row["CLASSCD"]."' ";
                    $where .= "AND SCHOOL_KIND = '".$row["SCHOOL_KIND"]."' ";
                    $where .= "AND CURRICULUM_CD = '".$row["CURRICULUM_CD"]."' ";
                    $where .= "AND SUBCLASSCD = '".$row["SUBCLASSCD"]."' ";
                    $where .= "AND SCHREGNO = '".$row["SCHREGNO"]."' ";
                    //更新
                    $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
                }
                $db->query($query);
            }
        }
        return;
    }

}
?>
