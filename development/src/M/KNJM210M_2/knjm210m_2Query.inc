<?php

require_once('for_php7.php');

class knjm210m_2Query extends Query {

    //氏名
    function getSchName($model)
    {
        $query  = "SELECT NAME FROM SCHREG_BASE_MST WHERE SCHREGNO='".$model->schregno."' ";

        return $query;
    }
    //科目名
    function getSubclassName($model)
    {
        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        } else {
            $query .= "       SUBCLASSCD = '".$model->subclasscd."' ";
        }

        return $query;
    }
    //スクーリング出席状況詳細（科目別）
    function getSchDetailSubclass($model)
    {
        $query  = "WITH SCH_ATTEND AS ( ";
        $query .= "    SELECT EXECUTEDATE, ";
        $query .= "           PERIODCD, ";
        $query .= "           CREDIT_TIME, ";
        $query .= "           SCHOOLINGKINDCD, ";
        $query .= "           STAFFCD, ";
        $query .= "           REMARK ";
        $query .= "    FROM   SCH_ATTEND_DAT ";
        $query .= "    WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "           SCHREGNO='".$model->schregno."' AND ";
        $query .= "           CHAIRCD='".$model->chaircd."' ) ";
        //メイン
        $query .= "SELECT T1.EXECUTEDATE, ";
        $query .= "       CASE WHEN T1.SCHOOLINGKINDCD = '2' THEN NULL ELSE T2.NAME1 END AS PERIOD_NAME, ";
        if ($model->Properties["useCreditTime"] == '1') {
            $query .= "       VALUE(T1.CREDIT_TIME, 0.0) AS SCH_SEQ_NAME, ";
        } else {
            $query .= "       VALUE(T1.CREDIT_TIME, 1.0) AS SCH_SEQ_NAME, ";
        }
        $query .= "       CASE WHEN T1.SCHOOLINGKINDCD > '1' THEN NULL ELSE T3.STAFFNAME END AS TANTOU, ";
        $query .= "       T4.NAME1 AS KIND_NAME, ";
        $query .= "       T1.REMARK ";
        $query .= "FROM   SCH_ATTEND T1 ";
        $query .= "       LEFT JOIN NAME_MST T2 ON T2.NAMECD1='B001' AND T2.NAMECD2=T1.PERIODCD ";
        $query .= "       LEFT JOIN STAFF_MST T3 ON T3.STAFFCD=T1.STAFFCD ";
        $query .= "       LEFT JOIN NAME_MST T4 ON T4.NAMECD1='M001' AND T4.NAMECD2=T1.SCHOOLINGKINDCD ";
        $query .= "ORDER BY T1.EXECUTEDATE DESC ";

        return $query;
    }
    //レポート提出状況詳細（科目別）
    function getRepDetailSubclass($model)
    {
        $query  = "WITH REP_MAIN AS ( ";
        $query .= "    SELECT STANDARD_SEQ, ";
        $query .= "           RECEIPT_DATE, ";
        $query .= "           STAFFCD, ";
        $query .= "           GRAD_DATE,GRAD_TIME,GRAD_VALUE,REPRESENT_SEQ ";
        $query .= "    FROM   REP_PRESENT_DAT ";
        $query .= "    WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "           SCHREGNO='".$model->schregno."' AND ";
        $query .= "           CHAIRCD='".$model->chaircd."' ) ";
        //メイン
        $query .= "SELECT T1.RECEIPT_DATE, ";
        $query .= "       '第'||RTRIM(CHAR(T1.STANDARD_SEQ))||'回' AS REP_SEQ_NAME, ";
        $query .= "       T2.STAFFNAME AS TENSAKU, ";
        $query .= "       T1.GRAD_DATE, ";
        $query .= "       CASE WHEN (T1.GRAD_VALUE IS NULL OR T1.GRAD_VALUE = '') ";
        $query .= "                 AND T1.GRAD_DATE IS NULL ";
        $query .= "                 AND T1.GRAD_TIME IS NULL ";
        $query .= "           THEN '受付中' ";
        $query .= "           ELSE T3.NAME1 END AS HYOUKA, ";
        $query .= "       CASE WHEN T1.REPRESENT_SEQ > 0 THEN '再'||RTRIM(CHAR(T1.REPRESENT_SEQ)) ";
        $query .= "            ELSE NULL END AS SAIHYOUKA ";
        $query .= "FROM   REP_MAIN T1 ";
        $query .= "       LEFT JOIN STAFF_MST T2 ON T2.STAFFCD=T1.STAFFCD ";
        $query .= "       LEFT JOIN NAME_MST T3 ON T3.NAMECD1='M003' AND ";
        $query .= "                                T3.NAMECD2=T1.GRAD_VALUE ";
        $query .= "ORDER BY T1.RECEIPT_DATE DESC ";

        return $query;
    }
    //スクーリング出席状況詳細（全科目）
    function getSchDetailSubclassAll($model)
    {
        $query  = "WITH CHAIR AS ( ";
        $query .= "    SELECT DISTINCT  ";
        $query .= "           CHAIRCD ";
        $query .= "    FROM   CHAIR_STD_DAT ";
        $query .= "    WHERE  YEAR      ='".CTRL_YEAR."' AND ";
        $query .= "           SEMESTER  ='".CTRL_SEMESTER."' AND ";
        $query .= "           SCHREGNO  ='".$model->schregno."' AND ";
        $query .= "           SUBSTR(CHAIRCD,1,2) < '91' ) ";
        $query .= ",CHAIR_NAME AS ( ";
        $query .= "    SELECT DISTINCT  ";
        $query .= "           T1.CHAIRCD, ";
        $query .= "           T1.CHAIRNAME, ";
        $query .= "           T2.SUBCLASSNAME ";
        $query .= "    FROM   CHAIR_DAT T1 ";
        $query .= "           LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD=T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          AND T1.CLASSCD = T2.CLASSCD ";
            $query .= "          AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= "          AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "    WHERE  T1.YEAR      ='".CTRL_YEAR."' AND ";
        $query .= "           T1.SEMESTER  ='".CTRL_SEMESTER."' AND ";
        $query .= "           T1.CHAIRCD   IN (SELECT CHAIRCD FROM CHAIR) ) ";
        $query .= ",SCH_ATTEND AS ( ";
        $query .= "    SELECT CHAIRCD, ";
        $query .= "           EXECUTEDATE, ";
        $query .= "           PERIODCD, ";
        $query .= "           CREDIT_TIME, ";
        $query .= "           SCHOOLINGKINDCD, ";
        $query .= "           STAFFCD, ";
        $query .= "           REMARK ";
        $query .= "    FROM   SCH_ATTEND_DAT ";
        $query .= "    WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "           SCHREGNO='".$model->schregno."' AND ";
        $query .= "           CHAIRCD IN (SELECT CHAIRCD FROM CHAIR) ) ";
        //メイン
        $query .= "SELECT T1.EXECUTEDATE, ";
        $query .= "       CASE WHEN T1.SCHOOLINGKINDCD = '2' THEN NULL ELSE T2.NAME1 END AS PERIOD_NAME, ";
        $query .= "       T5.SUBCLASSNAME, ";
        if ($model->Properties["useCreditTime"] == '1') {
            $query .= "       VALUE(T1.CREDIT_TIME, 0.0) AS SCH_SEQ_NAME, ";
        } else {
            $query .= "       VALUE(T1.CREDIT_TIME, 1.0) AS SCH_SEQ_NAME, ";
        }
        $query .= "       CASE WHEN T1.SCHOOLINGKINDCD > '1' THEN NULL ELSE T3.STAFFNAME END AS TANTOU, ";
        $query .= "       T4.NAME1 AS KIND_NAME, ";
        $query .= "       T1.REMARK ";
        $query .= "FROM   SCH_ATTEND T1 ";
        $query .= "       LEFT JOIN NAME_MST T2 ON T2.NAMECD1='B001' AND T2.NAMECD2=T1.PERIODCD ";
        $query .= "       LEFT JOIN STAFF_MST T3 ON T3.STAFFCD=T1.STAFFCD ";
        $query .= "       LEFT JOIN NAME_MST T4 ON T4.NAMECD1='M001' AND T4.NAMECD2=T1.SCHOOLINGKINDCD ";
        $query .= "       LEFT JOIN CHAIR_NAME T5 ON T5.CHAIRCD=T1.CHAIRCD ";
        $query .= "ORDER BY T1.EXECUTEDATE DESC,T1.CHAIRCD ";

        return $query;
    }
    //レポート提出状況詳細（全科目）
    function getRepDetailSubclassAll($model)
    {
        $query  = "WITH CHAIR AS ( ";
        $query .= "    SELECT DISTINCT  ";
        $query .= "           CHAIRCD ";
        $query .= "    FROM   CHAIR_STD_DAT ";
        $query .= "    WHERE  YEAR      ='".CTRL_YEAR."' AND ";
        $query .= "           SEMESTER  ='".CTRL_SEMESTER."' AND ";
        $query .= "           SCHREGNO  ='".$model->schregno."' AND ";
        $query .= "           SUBSTR(CHAIRCD,1,2) < '91' ) ";
        $query .= ",CHAIR_NAME AS ( ";
        $query .= "    SELECT DISTINCT  ";
        $query .= "           T1.CHAIRCD, ";
        $query .= "           T1.CHAIRNAME, ";
        $query .= "           T2.SUBCLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          T2.CLASSCD, ";
            $query .= "          T2.SCHOOL_KIND, ";
            $query .= "          T2.CURRICULUM_CD, ";
        }
        $query .= "           T2.SUBCLASSNAME ";
        $query .= "    FROM   CHAIR_DAT T1 ";
        $query .= "           LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD=T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          AND T1.CLASSCD = T2.CLASSCD ";
            $query .= "          AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= "          AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "    WHERE  T1.YEAR      ='".CTRL_YEAR."' AND ";
        $query .= "           T1.SEMESTER  ='".CTRL_SEMESTER."' AND ";
        $query .= "           T1.CHAIRCD   IN (SELECT CHAIRCD FROM CHAIR) ) ";
        $query .= ",REP_MAIN AS ( ";
        $query .= "    SELECT STANDARD_SEQ, ";
        $query .= "           RECEIPT_DATE, ";
        $query .= "           STAFFCD, ";
        $query .= "           GRAD_DATE, ";
        $query .= "           GRAD_TIME, ";
        $query .= "           GRAD_VALUE, ";
        $query .= "           REPRESENT_SEQ, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          CLASSCD, ";
            $query .= "          SCHOOL_KIND, ";
            $query .= "          CURRICULUM_CD, ";
        }
        $query .= "           SUBCLASSCD ";
        $query .= "    FROM   REP_PRESENT_DAT ";
        $query .= "    WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "           SCHREGNO='".$model->schregno."' AND ";
        $query .= "           CHAIRCD   IN (SELECT CHAIRCD FROM CHAIR) ) ";
        //メイン
        $query .= "SELECT T1.RECEIPT_DATE, ";
        $query .= "       T4.SUBCLASSNAME, ";
        $query .= "       '第'||RTRIM(CHAR(T1.STANDARD_SEQ))||'回' AS REP_SEQ_NAME, ";
        $query .= "       T2.STAFFNAME AS TENSAKU, ";
        $query .= "       T1.GRAD_DATE, ";
        $query .= "       CASE WHEN (T1.GRAD_VALUE IS NULL OR T1.GRAD_VALUE = '') ";
        $query .= "                 AND T1.GRAD_DATE IS NULL ";
        $query .= "                 AND T1.GRAD_TIME IS NULL ";
        $query .= "           THEN '受付中' ";
        $query .= "           ELSE T3.NAME1 END AS HYOUKA, ";
        $query .= "       CASE WHEN T1.REPRESENT_SEQ > 0 THEN '再'||RTRIM(CHAR(T1.REPRESENT_SEQ)) ";
        $query .= "            ELSE NULL END AS SAIHYOUKA ";
        $query .= "FROM   REP_MAIN T1 ";
        $query .= "       LEFT JOIN STAFF_MST T2 ON T2.STAFFCD=T1.STAFFCD ";
        $query .= "       LEFT JOIN NAME_MST T3 ON T3.NAMECD1='M003' AND ";
        $query .= "                                T3.NAMECD2=T1.GRAD_VALUE ";
        $query .= "       LEFT JOIN CHAIR_NAME T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          AND T1.CLASSCD = T4.CLASSCD ";
            $query .= "          AND T1.SCHOOL_KIND = T4.SCHOOL_KIND ";
            $query .= "          AND T1.CURRICULUM_CD = T4.CURRICULUM_CD ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.RECEIPT_DATE DESC, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          T1.CLASSCD, ";
            $query .= "          T1.SCHOOL_KIND, ";
            $query .= "          T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }
    //講座名（科目名）リスト
    function getChairSubclassList($model)
    {
        $query  = "WITH CHAIR AS ( ";
        $query .= "    SELECT DISTINCT  ";
        $query .= "           CHAIRCD ";
        $query .= "    FROM   CHAIR_STD_DAT ";
        $query .= "    WHERE  YEAR      ='".CTRL_YEAR."' AND ";
        $query .= "           SEMESTER  ='".CTRL_SEMESTER."' AND ";
        $query .= "           SCHREGNO  ='".$model->schregno."' AND ";
        $query .= "           SUBSTR(CHAIRCD,1,2) < '91' ) ";
        //メイン
        $query .= "SELECT DISTINCT  ";
        $query .= "       T1.CHAIRCD, ";
        $query .= "       T1.CHAIRNAME, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query .= "       T2.SUBCLASSCD, ";
        }
        $query .= "       T2.SUBCLASSNAME ";
        $query .= "FROM   CHAIR_DAT T1 ";
        $query .= "       LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "          AND T1.CLASSCD = T2.CLASSCD ";
            $query .= "          AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= "          AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "WHERE  T1.YEAR      ='".CTRL_YEAR."' AND ";
        $query .= "       T1.SEMESTER  ='".CTRL_SEMESTER."' AND ";
        $query .= "       T1.CHAIRCD   IN (SELECT CHAIRCD FROM CHAIR) ";
        $query .= "ORDER BY T1.CHAIRCD ";

        return $query;
    }
    //規定回数(スクーリング・レポート)
    function getKiteiCount($db,$chaircd)
    {
        $query  = "SELECT SCH_SEQ_MIN AS KITEI1,REP_SEQ_ALL AS KITEI2 ";
        $query .= "FROM   CHAIR_CORRES_DAT ";
        $query .= "WHERE  YEAR='".CTRL_YEAR."' AND CHAIRCD='".$chaircd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }
    //回数・履歴(スクーリング)
    function getSchCountRireki($db,$chaircd,$model)
    {
        $query  = "WITH SCH_ATTEND AS ( ";
        $query .= "    SELECT EXECUTEDATE,CREDIT_TIME,SCHOOLINGKINDCD ";
        $query .= "    FROM   SCH_ATTEND_DAT ";
        $query .= "    WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "           SCHREGNO='".$model->schregno."' AND ";
        $query .= "           CHAIRCD='".$chaircd."' ) ";
        if ($model->Properties["useCreditTime"] == '1') {
            $query .= ",MIN_DATE AS ( ";
            $query .= "    SELECT ";
            $query .= "        EXECUTEDATE, ";
            $query .= "        SUM(VALUE(CREDIT_TIME, 0.0)) AS CREDIT_TIME ";
            $query .= "    FROM ";
            $query .= "        SCH_ATTEND ";
            $query .= "    WHERE ";
            $query .= "        SCHOOLINGKINDCD='1' ";
            $query .= "    GROUP BY ";
            $query .= "        EXECUTEDATE ) ";
        } else {
            $query .= ",MIN_DATE AS ( ";
            $query .= "    SELECT EXECUTEDATE ";
            $query .= "    FROM   SCH_ATTEND ";
            $query .= "    WHERE  SCHOOLINGKINDCD='1' ";
            $query .= "    GROUP BY EXECUTEDATE ) ";
        }
        //メイン
        $query .= "SELECT '1' AS SCHOOLINGKINDCD, ";
        $query .= "       EXECUTEDATE, ";
        if ($model->Properties["useCreditTime"] == '1') {
            $query .= "       CREDIT_TIME, ";
        } else {
            $query .= "       1 AS CREDIT_TIME, ";
        }
        $query .= "       SUBSTR(RTRIM(CHAR(EXECUTEDATE)),6,2)||SUBSTR(RTRIM(CHAR(EXECUTEDATE)),9,2) AS E_DATE ";
        $query .= "FROM   MIN_DATE ";
        $query .= "UNION ALL ";
        $query .= "SELECT '2' AS SCHOOLINGKINDCD, ";
        $query .= "       EXECUTEDATE, ";
        $query .= "       VALUE(CREDIT_TIME, 1) AS CREDIT_TIME, ";
        $query .= "       SUBSTR(RTRIM(CHAR(EXECUTEDATE)),6,2)||SUBSTR(RTRIM(CHAR(EXECUTEDATE)),9,2) AS E_DATE ";
        $query .= "FROM   SCH_ATTEND ";
        $query .= "WHERE  SCHOOLINGKINDCD='2' ";
        $query .= "UNION ALL ";
        $query .= "SELECT '3' AS SCHOOLINGKINDCD, ";
        $query .= "       EXECUTEDATE, ";
        $query .= "       VALUE(CREDIT_TIME, 1) AS CREDIT_TIME, ";
        $query .= "       SUBSTR(RTRIM(CHAR(EXECUTEDATE)),6,2)||SUBSTR(RTRIM(CHAR(EXECUTEDATE)),9,2) AS E_DATE ";
        $query .= "FROM   SCH_ATTEND ";
        $query .= "WHERE  SCHOOLINGKINDCD='3' ";
        $query .= "UNION ALL ";
        $query .= "SELECT '4' AS SCHOOLINGKINDCD, ";
        $query .= "       EXECUTEDATE, ";
        $query .= "       VALUE(CREDIT_TIME, 1) AS CREDIT_TIME, ";
        $query .= "       SUBSTR(RTRIM(CHAR(EXECUTEDATE)),6,2)||SUBSTR(RTRIM(CHAR(EXECUTEDATE)),9,2) AS E_DATE ";
        $query .= "FROM   SCH_ATTEND ";
        $query .= "WHERE  SCHOOLINGKINDCD='4' ";
        $query .= "ORDER BY EXECUTEDATE ";

        return $query;
    }
    //回数・履歴(レポート)
    function getRepCountRireki($db,$chaircd,$model)
    {
        $query .= "WITH REP_MAIN AS ( ";
        $query .= "    SELECT STANDARD_SEQ,RECEIPT_DATE,REPRESENT_SEQ,GRAD_VALUE,GRAD_DATE,GRAD_TIME ";
        $query .= "    FROM   REP_PRESENT_DAT ";
        $query .= "    WHERE  YEAR='".CTRL_YEAR."' AND ";
        $query .= "           SCHREGNO='".$model->schregno."' AND ";
        $query .= "           CHAIRCD='".$chaircd."' ) ";
        $query .= ",REP_DATE_MAX AS ( ";
        $query .= "    SELECT STANDARD_SEQ,MAX(RECEIPT_DATE) AS RECEIPT_DATE ";
        $query .= "    FROM   REP_MAIN ";
        $query .= "    GROUP BY STANDARD_SEQ ) ";
        $query .= ",REP_SEQ AS ( ";
        $query .= "    SELECT ";
        $query .= "        T1.STANDARD_SEQ, ";
        $query .= "        T1.RECEIPT_DATE, ";
        $query .= "        MAX(T1.REPRESENT_SEQ) AS REPRESENT_SEQ ";
        $query .= "    FROM ";
        $query .= "        REP_MAIN T1, ";
        $query .= "        REP_DATE_MAX T2 ";
        $query .= "    WHERE  T1.STANDARD_SEQ = T2.STANDARD_SEQ ";
        $query .= "           AND T1.RECEIPT_DATE = T2.RECEIPT_DATE ";
        $query .= "    GROUP BY T1.STANDARD_SEQ, ";
        $query .= "             T1.RECEIPT_DATE ) ";
        //メイン
        $query .= "SELECT T1.STANDARD_SEQ, ";
        $query .= "       CASE WHEN T2.GRAD_VALUE = '1' THEN  ";
        $query .= "           CASE WHEN T2.REPRESENT_SEQ = 0 THEN '再' ";
        $query .= "           ELSE '再'||RTRIM(CHAR(T2.REPRESENT_SEQ)) END ";
        $query .= "       ELSE ";
        $query .= "           CASE WHEN (T2.GRAD_VALUE IS NULL OR T2.GRAD_VALUE = '') ";
        $query .= "                     AND T2.GRAD_DATE IS NULL ";
        $query .= "                     AND T2.GRAD_TIME IS NULL ";
        $query .= "           THEN '受' ";
        $query .= "           ELSE T3.NAME1 END ";
        $query .= "       END AS HYOUKA ";
        $query .= "FROM   REP_SEQ T1 ";
        $query .= "       LEFT JOIN REP_MAIN T2 ON T2.STANDARD_SEQ=T1.STANDARD_SEQ ";
        $query .= "                                AND T2.RECEIPT_DATE=T1.RECEIPT_DATE ";
        $query .= "                                AND T2.REPRESENT_SEQ=T1.REPRESENT_SEQ ";
        $query .= "       LEFT JOIN NAME_MST T3 ON T3.NAMECD1='M003' ";
        $query .= "                                AND T3.NAMECD2=T2.GRAD_VALUE ";
        $query .= "ORDER BY T1.STANDARD_SEQ ";

        return $query;
    }

}
?>