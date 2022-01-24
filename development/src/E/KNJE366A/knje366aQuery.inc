<?php

require_once('for_php7.php');

class knje366aQuery extends Query {

    //年度一覧
    function getYearSeme()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' AS LABEL, ";
        $query .= "     T1.YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //大学
    function getCollege()
    {
        $query  = " SELECT ";
        $query .= "     '全て' AS LABEL, ";
        $query .= "     '0-ALL' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1  T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '国公立' AS LABEL, ";
        $query .= "     '1' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1  T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '私立' AS LABEL, ";
        $query .= "     '2' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1  T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //大学
    function getScd()
    {
        $query  = " SELECT ";
        $query .= "     '全て(全学部・学科)' AS LABEL, ";
        $query .= "     '0' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1  T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '医学部(医学科)' AS LABEL, ";
        $query .= "     '601' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1  T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '歯学部(歯学科)' AS LABEL, ";
        $query .= "     '611' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1  T1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '薬学部' AS LABEL, ";
        $query .= "     '621' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1  T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入学区分名称取得
    function getEntDivName($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->field["YEAR"]."' ";
        $query .= "     AND NAMECD1 = 'A002' ";
        $query .= "     AND NAMECD2 IN ('1', '2') ";

        return $query;
    }

    //CSV出力データ
    function getCsvQuery($model) {
        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         L1.HR_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT L1 ";
        $query .= "                     ON T1.YEAR      = L1.YEAR ";
        $query .= "                    AND T1.GRADE     = L1.GRADE ";
        $query .= "                    AND T1.HR_CLASS  = L1.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."'  ";
        $query .= " ), SCH_LIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.*, ";
        if ($model->field["S_CD"] != "0") {
            $query .= "         L0.REMARK5 AS S_CD, ";
        }
        $query .= "         L1.ATTENDNO, ";
        $query .= "         L1.HR_NAME, ";
        $query .= "         CASE ";
        $query .= "             WHEN L1.ATTENDNO IS NOT NULL THEN BASE.ENT_DIV ";
        $query .= "             ELSE G_BASE.ENT_DIV ";
        $query .= "         END AS ENTDIV ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT T1 ";
        if ($model->field["S_CD"] != "0") {
            $query .= "         LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT L0 ON T1.YEAR = L0.YEAR AND T1.SEQ = L0.SEQ AND L0.DETAIL_SEQ = 1 ";
        }
        $query .= "         LEFT JOIN SCH_INFO L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ";
        $query .= "         LEFT JOIN GRD_BASE_MST G_BASE  ON T1.SCHREGNO = G_BASE.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.SENKOU_KIND  = '0' AND ";
        $query .= "         T1.DECISION     = '1' AND ";
        $query .= "         T1.SCHOOL_GROUP IN ('01', '02', '03', '04', '05', '08') ";
        $query .= " ), PASS_SCHOOL_CNT1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         COUNT(*) AS CNT, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '1' THEN '1' ELSE '0' END AS INT)) AS CNT_A, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '2' THEN '1' ELSE '0' END AS INT)) AS CNT_B ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     WHERE ";
        $query .= "         ATTENDNO IS NOT NULL ";
        if ($model->field["S_CD"] != "0") {
            $query .= " AND S_CD = '".$model->field["S_CD"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD ";
        $query .= " ), PASS_SCHOOL_CNT2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         COUNT(*) AS CNT, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '1' THEN '1' ELSE '0' END AS INT)) AS CNT_A, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '2' THEN '1' ELSE '0' END AS INT)) AS CNT_B ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     WHERE ";
        $query .= "         ATTENDNO IS NULL ";
        if ($model->field["S_CD"] != "0") {
            $query .= " AND S_CD = '".$model->field["S_CD"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD ";
        $query .= " ), PASS_FACULTY_CNT1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD, ";
        $query .= "         DEPARTMENTCD, ";
        $query .= "         COUNT(*) AS CNT, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '1' THEN '1' ELSE '0' END AS INT)) AS CNT_A, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '2' THEN '1' ELSE '0' END AS INT)) AS CNT_B ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     WHERE ";
        $query .= "         ATTENDNO IS NOT NULL ";
        if ($model->field["S_CD"] != "0") {
            $query .= " AND S_CD = '".$model->field["S_CD"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD, ";
        $query .= "         DEPARTMENTCD ";
        $query .= " ), PASS_FACULTY_CNT2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD, ";
        $query .= "         DEPARTMENTCD, ";
        $query .= "         COUNT(*) AS CNT, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '1' THEN '1' ELSE '0' END AS INT)) AS CNT_A, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '2' THEN '1' ELSE '0' END AS INT)) AS CNT_B ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     WHERE ";
        $query .= "         ATTENDNO IS NULL ";
        if ($model->field["S_CD"] != "0") {
            $query .= " AND S_CD = '".$model->field["S_CD"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD, ";
        $query .= "         DEPARTMENTCD ";
        $query .= " ), GO_FACULTY_CNT1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD, ";
        $query .= "         DEPARTMENTCD, ";
        $query .= "         COUNT(*) AS CNT, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '1' THEN '1' ELSE '0' END AS INT)) AS CNT_A, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '2' THEN '1' ELSE '0' END AS INT)) AS CNT_B ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     WHERE ";
        $query .= "         PLANSTAT = '1' ";
        $query .= "     AND ATTENDNO IS NOT NULL ";
        if ($model->field["S_CD"] != "0") {
            $query .= " AND S_CD = '".$model->field["S_CD"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD, ";
        $query .= "         DEPARTMENTCD ";
        $query .= " ), GO_FACULTY_CNT2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD, ";
        $query .= "         DEPARTMENTCD, ";
        $query .= "         COUNT(*) AS CNT, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '1' THEN '1' ELSE '0' END AS INT)) AS CNT_A, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '2' THEN '1' ELSE '0' END AS INT)) AS CNT_B ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     WHERE ";
        $query .= "         PLANSTAT = '1' ";
        $query .= "     AND ATTENDNO IS NULL ";
        if ($model->field["S_CD"] != "0") {
            $query .= " AND S_CD = '".$model->field["S_CD"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD, ";
        $query .= "         FACULTYCD, ";
        $query .= "         DEPARTMENTCD ";
        $query .= " ), GO_SCHOOL_CNT1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         COUNT(*) AS CNT, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '1' THEN '1' ELSE '0' END AS INT)) AS CNT_A, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '2' THEN '1' ELSE '0' END AS INT)) AS CNT_B ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     WHERE ";
        $query .= "         PLANSTAT = '1' AND ";
        $query .= "         ATTENDNO IS NOT NULL ";
        if ($model->field["S_CD"] != "0") {
            $query .= " AND S_CD = '".$model->field["S_CD"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD ";
        $query .= " ), GO_SCHOOL_CNT2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         STAT_CD, ";
        $query .= "         COUNT(*) AS CNT, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '1' THEN '1' ELSE '0' END AS INT)) AS CNT_A, ";
        $query .= "         SUM(CAST(CASE WHEN ENTDIV = '2' THEN '1' ELSE '0' END AS INT)) AS CNT_B ";
        $query .= "     FROM ";
        $query .= "         SCH_LIST ";
        $query .= "     WHERE ";
        $query .= "         PLANSTAT = '1' AND ";
        $query .= "         ATTENDNO IS NULL ";
        if ($model->field["S_CD"] != "0") {
            $query .= " AND S_CD = '".$model->field["S_CD"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         STAT_CD ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     CASE WHEN (T1.SCHOOL_GROUP = '01' OR T1.SCHOOL_GROUP = '02' OR T1.SCHOOL_GROUP = '04' OR T1.SCHOOL_GROUP = '08') THEN '1' ";
        $query .= "          WHEN (T1.SCHOOL_GROUP = '03' OR T1.SCHOOL_GROUP = '05') THEN '2' ";
        $query .= "          ELSE '9' END AS SCHOOL_GROUP, ";
        $query .= "     CASE WHEN (T1.SCHOOL_GROUP = '01' OR T1.SCHOOL_GROUP = '02' OR T1.SCHOOL_GROUP = '04' OR T1.SCHOOL_GROUP = '08') THEN '国公立大学' ";
        $query .= "          WHEN (T1.SCHOOL_GROUP = '03' OR T1.SCHOOL_GROUP = '05') THEN '私立大学' ";
        $query .= "          ELSE '' END AS SCHOOL_GROUP_NAME, ";
        $query .= "     T1.STAT_CD, ";
        $query .= "     L1.SCHOOL_NAME_SHOW1 AS SCHOOL_NAME, ";
        if ($model->field["OUTPUT"] == "1") {
            $query .= "     VALUE(RTRIM(CHAR(C1.CNT))  ,'0') AS PASS_SCHOOL_CNT1, ";
            $query .= "     VALUE(RTRIM(CHAR(C1.CNT_A)),'0') AS PASS_SCHOOL_CNT1_A, ";
            $query .= "     VALUE(RTRIM(CHAR(C1.CNT_B)),'0') AS PASS_SCHOOL_CNT1_B, ";
            $query .= "     VALUE(RTRIM(CHAR(C2.CNT))  ,'0') AS PASS_SCHOOL_CNT2, ";
            $query .= "     VALUE(RTRIM(CHAR(C2.CNT_A)),'0') AS PASS_SCHOOL_CNT2_A, ";
            $query .= "     VALUE(RTRIM(CHAR(C2.CNT_B)),'0') AS PASS_SCHOOL_CNT2_B, ";
            $query .= "     VALUE(RTRIM(CHAR(C7.CNT))  ,'0') AS GO_SCHOOL_CNT1, ";
            $query .= "     VALUE(RTRIM(CHAR(C7.CNT_A)),'0') AS GO_SCHOOL_CNT1_A, ";
            $query .= "     VALUE(RTRIM(CHAR(C7.CNT_B)),'0') AS GO_SCHOOL_CNT1_B, ";
            $query .= "     VALUE(RTRIM(CHAR(C8.CNT)),  '0') AS GO_SCHOOL_CNT2, ";
            $query .= "     VALUE(RTRIM(CHAR(C8.CNT_A)),'0') AS GO_SCHOOL_CNT2_A, ";
            $query .= "     VALUE(RTRIM(CHAR(C8.CNT_B)),'0') AS GO_SCHOOL_CNT2_B ";
        } else if ($model->field["OUTPUT"] == "2") {
            $query .= "     T1.FACULTYCD, ";
            $query .= "     L2.FACULTYNAME_SHOW1 AS FACULTYNAME, ";
            $query .= "     T1.DEPARTMENTCD, ";
            $query .= "     L3.DEPARTMENTNAME_SHOW1 AS DEPARTMENTNAME, ";
            $query .= "     VALUE(RTRIM(CHAR(C3.CNT))  ,'0') AS PASS_FACULTY_CNT1, ";
            $query .= "     VALUE(RTRIM(CHAR(C3.CNT_A)),'0') AS PASS_FACULTY_CNT1_A, ";
            $query .= "     VALUE(RTRIM(CHAR(C3.CNT_B)),'0') AS PASS_FACULTY_CNT1_B, ";
            $query .= "     VALUE(RTRIM(CHAR(C4.CNT))  ,'0') AS PASS_FACULTY_CNT2, ";
            $query .= "     VALUE(RTRIM(CHAR(C4.CNT_A)),'0') AS PASS_FACULTY_CNT2_A, ";
            $query .= "     VALUE(RTRIM(CHAR(C4.CNT_B)),'0') AS PASS_FACULTY_CNT2_B, ";
            $query .= "     VALUE(RTRIM(CHAR(C5.CNT))  ,'0') AS GO_FACULTY_CNT1, ";
            $query .= "     VALUE(RTRIM(CHAR(C5.CNT_A)),'0') AS GO_FACULTY_CNT1_A, ";
            $query .= "     VALUE(RTRIM(CHAR(C5.CNT_B)),'0') AS GO_FACULTY_CNT1_B, ";
            $query .= "     VALUE(RTRIM(CHAR(C6.CNT))  ,'0') AS GO_FACULTY_CNT2, ";
            $query .= "     VALUE(RTRIM(CHAR(C6.CNT_A)),'0') AS GO_FACULTY_CNT2_A, ";
            $query .= "     VALUE(RTRIM(CHAR(C6.CNT_B)),'0') AS GO_FACULTY_CNT2_B ";
        }
        //$query .= "     CASE WHEN T1.PLANSTAT = '1' THEN '進学' ELSE '*' END AS PLANSTAT, ";
        //$query .= "     CASE WHEN T1.ATTENDNO IS NULL THEN '卒' ELSE '' END AS REGD_DIV, ";
        //$query .= "     S1.NAME, ";
        //$query .= "     T1.SCHREGNO, ";
        //$query .= "     T1.HR_NAME, ";
        //$query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCH_LIST T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST S1 ";
        $query .= "             ON T1.SCHREGNO      = S1.SCHREGNO ";
        $query .= "     LEFT JOIN COLLEGE_MST L1 ";
        $query .= "             ON T1.STAT_CD       = L1.SCHOOL_CD ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST L2 ";
        $query .= "             ON T1.STAT_CD       = L2.SCHOOL_CD ";
        $query .= "            AND T1.FACULTYCD     = L2.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ";
        $query .= "             ON T1.STAT_CD       = L3.SCHOOL_CD ";
        $query .= "            AND T1.FACULTYCD     = L3.FACULTYCD ";
        $query .= "            AND T1.DEPARTMENTCD  = L3.DEPARTMENTCD ";
        $query .= "     LEFT JOIN PASS_SCHOOL_CNT1 C1 ";
        $query .= "             ON T1.STAT_CD       = C1.STAT_CD ";
        $query .= "     LEFT JOIN PASS_SCHOOL_CNT2 C2 ";
        $query .= "             ON T1.STAT_CD       = C2.STAT_CD ";
        $query .= "     LEFT JOIN PASS_FACULTY_CNT1 C3 ";
        $query .= "             ON T1.STAT_CD       = C3.STAT_CD ";
        $query .= "            AND T1.FACULTYCD     = C3.FACULTYCD ";
        $query .= "            AND T1.DEPARTMENTCD  = C3.DEPARTMENTCD ";
        $query .= "     LEFT JOIN PASS_FACULTY_CNT2 C4 ";
        $query .= "             ON T1.STAT_CD       = C4.STAT_CD ";
        $query .= "            AND T1.FACULTYCD     = C4.FACULTYCD ";
        $query .= "            AND T1.DEPARTMENTCD  = C4.DEPARTMENTCD ";
        $query .= "     LEFT JOIN GO_FACULTY_CNT1 C5 ";
        $query .= "             ON T1.STAT_CD       = C5.STAT_CD ";
        $query .= "            AND T1.FACULTYCD     = C5.FACULTYCD ";
        $query .= "            AND T1.DEPARTMENTCD  = C5.DEPARTMENTCD ";
        $query .= "     LEFT JOIN GO_FACULTY_CNT2 C6 ";
        $query .= "             ON T1.STAT_CD       = C6.STAT_CD ";
        $query .= "            AND T1.FACULTYCD     = C6.FACULTYCD ";
        $query .= "            AND T1.DEPARTMENTCD  = C6.DEPARTMENTCD ";
        $query .= "     LEFT JOIN GO_SCHOOL_CNT1 C7 ";
        $query .= "             ON T1.STAT_CD       = C7.STAT_CD ";
        $query .= "     LEFT JOIN GO_SCHOOL_CNT2 C8 ";
        $query .= "             ON T1.STAT_CD       = C8.STAT_CD ";
        if ($model->field["SCHOOL_GROUP"] == "1") {
            $query .= " WHERE ";
            $query .= "      T1.SCHOOL_GROUP IN ('01', '02', '04', '08') ";
            if ($model->field["S_CD"] != "0") {
                $query .= " AND T1.S_CD = '".$model->field["S_CD"]."' ";
            }
        } else if ($model->field["SCHOOL_GROUP"] == "2") {
            $query .= " WHERE ";
            $query .= "      T1.SCHOOL_GROUP IN ('03', '05') ";
            if ($model->field["S_CD"] != "0") {
                $query .= " AND T1.S_CD = '".$model->field["S_CD"]."' ";
            }
        } else {
            if ($model->field["S_CD"] != "0") {
                $query .= " WHERE ";
                $query .= "      T1.S_CD = '".$model->field["S_CD"]."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOL_GROUP, ";
        $query .= "     T1.STAT_CD ";
        if ($model->field["OUTPUT"] == "2") {
            $query .= "    ,T1.FACULTYCD ";
            $query .= "    ,T1.DEPARTMENTCD ";
        }
        return $query;
    }
}
?>
