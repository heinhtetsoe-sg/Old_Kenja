<?php

require_once('for_php7.php');


class knje150mQuery extends Query {

    //学年取得
    function getGrade()
    {
        $query .= " SELECT ";
        $query .= "     GRADE_NAME1 AS LABEL, ";
        $query .= "     GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCHOOL_KIND = 'J' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //観点マスタ
    function getJviewMst()
    {
        $query = knje150mQuery::getJviewAll();
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.VIEWNAME, ";
        $query .= "     L1.CLASSNAME ";
        $query .= " FROM ";
        $query .= "     J_MST T1 ";
        $query .= "     LEFT JOIN CLASS_MST L1 ON L1.CLASSCD = substr(T1.VIEWCD, 1, 2) ";
        $query .= " ORDER BY ";
        $query .= "     VIEWCD ";

        return $query;
    }

    //観点ALL
    function getJviewAll()
    {
        $sub = (CTRL_YEAR < "2012") ? "" : "_SUB";
        $query .= " WITH J_MST AS ( ";
        $query .= " SELECT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T1.VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME{$sub}_MST T1, ";
        $query .= "     JVIEWNAME{$sub}_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.VIEWCD = T2.VIEWCD ";
        $query .= "     AND T2.YEAR = '".CTRL_YEAR."' ";
        if ($sub == "_SUB") {
            $query .= "     AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
            $query .= "     AND T1.SUBCLASSCD LIKE '%0001' ";
        }
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     T2.YEAR, ";
        $query .= "     substr(T1.VIEWCD, 1, 2) || '99' AS VIEWCD, ";
        $query .= "     '評定' AS VIEWNAME ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME{$sub}_MST T1, ";
        $query .= "     JVIEWNAME{$sub}_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.VIEWCD = T2.VIEWCD ";
        $query .= "     AND T2.YEAR = '".CTRL_YEAR."' ";
        if ($sub == "_SUB") {
            $query .= "     AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
            $query .= "     AND T1.SUBCLASSCD LIKE '%0001' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T2.YEAR, ";
        $query .= "     substr(T1.VIEWCD, 1, 2) ";
        $query .= " ) ";

        return $query;
    }

    //観点の記録
    function getJviewCsv($model) {
        $query  = knje150mQuery::getJviewAll();
        $query .= " , REGD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     MAX(T1.SEMESTER) AS MAX_SEM ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.GRADE = '".$model->grade."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
        $query .= ", STUDYREC_DAT AS ( ";
        $query .= "    SELECT ";
        $query .= "        T1.YEAR, ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        T1.CLASSCD || '99' AS VIEWCD, ";
        $query .= "        rtrim(char(max(T1.VALUATION))) AS STATUS ";
        $query .= "    FROM ";
        $query .= "        SCHREG_STUDYREC_DAT T1 ";
        $query .= "    WHERE ";
        $query .= "        T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    GROUP BY ";
        $query .= "        T1.YEAR, ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        T1.CLASSCD ";
        $query .= "    ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     REGH.HR_CLASS, ";
        $query .= "     REGH.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     CASE WHEN J_MST.VIEWCD LIKE '%99' THEN VALUE(S_DAT.STATUS, '/') ";
        $query .= "          ELSE VALUE(J_DAT.STATUS, '/') END AS STATUS ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGH ON T1.YEAR = REGH.YEAR ";
        $query .= "          AND T1.SEMESTER = REGH.SEMESTER ";
        $query .= "          AND T1.GRADE = REGH.GRADE ";
        $query .= "          AND T1.HR_CLASS = REGH.HR_CLASS ";
        $query .= "     LEFT JOIN J_MST ON T1.YEAR = J_MST.YEAR ";
        $sub = (CTRL_YEAR < "2012") ? "" : "_SUB";
        $query .= "     LEFT JOIN JVIEWSTAT{$sub}_DAT J_DAT ON J_MST.YEAR = J_DAT.YEAR ";
        $query .= "          AND J_DAT.SEMESTER = '9' ";
        $query .= "          AND T1.SCHREGNO = J_DAT.SCHREGNO ";
        $query .= "          AND J_MST.VIEWCD = J_DAT.VIEWCD ";
        if ($sub == "_SUB") {
            $query .= "          AND J_DAT.SUBCLASSCD = substr(J_MST.VIEWCD, 1, 2) || '0001' ";
        }
        $query .= "     LEFT JOIN STUDYREC_DAT S_DAT ON J_MST.YEAR = S_DAT.YEAR ";
        $query .= "          AND T1.SCHREGNO = S_DAT.SCHREGNO ";
        $query .= "          AND J_MST.VIEWCD = S_DAT.VIEWCD ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             REGD_T E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "             AND T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.SEMESTER = E1.MAX_SEM ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     REGH.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     J_MST.VIEWCD ";

        return $query;
    }

}
?>
