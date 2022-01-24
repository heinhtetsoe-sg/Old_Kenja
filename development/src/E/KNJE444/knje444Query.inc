<?php

require_once('for_php7.php');

class knje444Query extends Query
{
    //教育委員会用の学校コード取得
    public function getSchoolMst($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";

        return $query;
    }

    //教育委員会用の学校名取得
    public function getEdboardSchoolMst($model)
    {
        $query  = " SELECT ";
        $query .= "     EDBOARD_SCHOOLNAME ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";

        return $query;
    }

    //対象学科コンボ
    public function getMajorcd($model)
    {
        $query  = " SELECT ";
        $query .= "     COURSECD || '-' || MAJORCD AS VALUE, ";
        $query .= "     MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND (COURSECD,MAJORCD) IN ( ";
        $query .= "         SELECT ";
        $query .= "             COURSECD, ";
        $query .= "             MAJORCD ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '{$model->field["YEAR"]}' ";
        $query .= "         GROUP BY ";
        $query .= "             COURSECD, ";
        $query .= "             MAJORCD ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //実行日時
    public function getExecDate($model)
    {
        $query  = " SELECT ";
        $query .= "     CALC_DATE, ";
        $query .= "     CALC_TIME ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_STAT_EXEC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND COURSECD || '-' || MAJORCD = '{$model->field["MAJORCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     CALC_DATE DESC, ";
        $query .= "     CALC_TIME DESC ";

        return $query;
    }

    //CSV出力（生徒データ）（進路データ）
    public function getCsvRegdAndAft($model)
    {
        $query = "";
        //生徒データ
        $query .= " WITH T_REGD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, "; //key
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         L1.MAJORNAME, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T2.HR_CLASS_NAME1 AS HR_CLASS_NAME, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T3.NAME, ";
        $query .= "         T3.NAME_KANA, ";
        $query .= "         T3.BIRTHDAY, ";
        $query .= "         T3.SEX, ";
        $query .= "         T3.FINSCHOOLCD, ";
        $query .= "         L2.FINSCHOOL_NAME, ";
        $query .= "         L2.FINSCHOOL_PREF_CD, ";
        $query .= "         L3.PREF_NAME AS FINSCHOOL_PREF_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "              ON T2.YEAR = T1.YEAR ";
        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "             AND T2.GRADE = T1.GRADE ";
        $query .= "             AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "         LEFT JOIN MAJOR_MST L1 ON L1.COURSECD = T1.COURSECD AND L1.MAJORCD = T1.MAJORCD ";
        $query .= "         LEFT JOIN FINSCHOOL_MST L2 ON L2.FINSCHOOLCD = T3.FINSCHOOLCD ";
        $query .= "         LEFT JOIN PREF_MST L3 ON L3.PREF_CD = L2.FINSCHOOL_PREF_CD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["MAJORCD"] != "A-ALL") {
            $query .= "         AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["MAJORCD"]}' ";
        }
        $query .= " ) ";
        //進路データ
        $query .= " , T_AFT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.TOROKU_DATE, ";
        $query .= "         T1.SEQ, "; //key
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.SENKOU_KIND, ";
        $query .= "         CASE WHEN T1.SENKOU_KIND = '0' THEN '学校' ";
        $query .= "              WHEN T1.SENKOU_KIND = '1' THEN '会社' ";
        $query .= "              ELSE 'その他' ";
        $query .= "         END AS SENKOU_NAME, ";
        $query .= "         T1.STAT_CD, ";
        $query .= "         CASE WHEN T1.SENKOU_KIND = '0' THEN CASE WHEN T1.THINKEXAM IS NOT NULL THEN T1.THINKEXAM ELSE L1.SCHOOL_NAME END ";
        $query .= "              WHEN T1.SENKOU_KIND = '1' THEN CASE WHEN T1.JOBEX_THINK IS NOT NULL THEN T1.JOBEX_THINK ELSE L2.COMPANY_NAME END ";
        $query .= "              ELSE NULL ";
        $query .= "         END AS STAT_NAME, ";
        $query .= "         T1.SCHOOL_GROUP, ";
        $query .= "         NM_E012.NAME1 AS SCHOOL_GROUP_NAME, ";
        $query .= "         T1.FACULTYCD, ";
        $query .= "         L3.FACULTYNAME, ";
        $query .= "         T1.DEPARTMENTCD, ";
        $query .= "         L4.DEPARTMENTNAME, ";
        $query .= "         T1.JOBTYPE_LCD, ";
        $query .= "         J1.JOBTYPE_LNAME, ";
        $query .= "         T1.JOBTYPE_MCD, ";
        $query .= "         J2.JOBTYPE_MNAME, ";
        $query .= "         T1.JOBTYPE_SCD, ";
        $query .= "         T1.JOBTYPE_SSCD, ";
        $query .= "         J3.JOBTYPE_SNAME, ";
        $query .= "         CASE WHEN T1.SENKOU_KIND = '0' THEN P1.PREF_CD ";
        $query .= "              WHEN T1.SENKOU_KIND = '1' THEN T1.PREF_CD ";
        $query .= "              ELSE NULL ";
        $query .= "         END AS AFT_GRAD_PREF_CD, ";
        $query .= "         CASE WHEN T1.SENKOU_KIND = '0' THEN L1.AREA_NAME ";
        $query .= "              WHEN T1.SENKOU_KIND = '1' THEN P2.PREF_NAME ";
        $query .= "              ELSE NULL ";
        $query .= "         END AS AFT_GRAD_PREF_NAME, ";
        $query .= "         T1.HOWTOEXAM, ";
        $query .= "         NM_E002.NAME1 AS HOWTOEXAM_NAME, ";
        $query .= "         T1.DECISION, ";
        $query .= "         NM_E005.NAME1 AS DECISION_NAME, ";
        $query .= "         T1.PLANSTAT, ";
        $query .= "         NM_E006.NAME1 AS PLANSTAT_NAME ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_SS_DAT T1 ";
        $query .= "         LEFT JOIN COLLEGE_MST L1 ON L1.SCHOOL_CD = T1.STAT_CD ";
        $query .= "         LEFT JOIN COMPANY_MST L2 ON L2.COMPANY_CD = T1.STAT_CD ";
        $query .= "         LEFT JOIN COLLEGE_FACULTY_MST L3 ON L3.SCHOOL_CD = T1.STAT_CD AND L3.FACULTYCD = T1.FACULTYCD ";
        $query .= "         LEFT JOIN COLLEGE_DEPARTMENT_MST L4 ON L4.SCHOOL_CD = T1.STAT_CD AND L4.FACULTYCD = T1.FACULTYCD AND L4.DEPARTMENTCD = T1.DEPARTMENTCD ";
        $query .= "         LEFT JOIN JOBTYPE_L_MST J1 ON J1.JOBTYPE_LCD = T1.JOBTYPE_LCD ";
        $query .= "         LEFT JOIN JOBTYPE_M_MST J2 ON J2.JOBTYPE_LCD = T1.JOBTYPE_LCD AND J2.JOBTYPE_MCD = T1.JOBTYPE_MCD ";
        $query .= "         LEFT JOIN JOBTYPE_SS_MST J3 ON J3.JOBTYPE_LCD = T1.JOBTYPE_LCD AND J3.JOBTYPE_MCD = T1.JOBTYPE_MCD AND J3.JOBTYPE_SCD = T1.JOBTYPE_SCD AND J3.JOBTYPE_SSCD = T1.JOBTYPE_SSCD ";
        $query .= "         LEFT JOIN PREF_MST P1 ON P1.PREF_NAME = L1.AREA_NAME ";
        $query .= "         LEFT JOIN PREF_MST P2 ON P2.PREF_CD = T1.PREF_CD ";
        $query .= "         LEFT JOIN NAME_MST NM_E002 ON NM_E002.NAMECD1 = 'E002' AND NM_E002.NAMECD2 = T1.HOWTOEXAM ";
        $query .= "         LEFT JOIN NAME_MST NM_E005 ON NM_E005.NAMECD1 = 'E005' AND NM_E005.NAMECD2 = T1.DECISION ";
        $query .= "         LEFT JOIN NAME_MST NM_E006 ON NM_E006.NAMECD1 = 'E006' AND NM_E006.NAMECD2 = T1.PLANSTAT ";
        $query .= "         LEFT JOIN NAME_MST NM_E012 ON NM_E012.NAMECD1 = 'E012' AND NM_E012.NAMECD2 = T1.SCHOOL_GROUP ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "         AND T1.TOROKU_DATE IS NOT NULL ";
        $query .= "         AND T1.SENKOU_KIND IN ('0', '1') ";
        $query .= " ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.MAJORNAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.HR_CLASS_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     T1.FINSCHOOL_NAME, ";
        $query .= "     T1.FINSCHOOL_PREF_CD, ";
        $query .= "     T1.FINSCHOOL_PREF_NAME, ";
        //進路データ
        $query .= "     T2.YEAR, ";
        $query .= "     T2.TOROKU_DATE, ";
        $query .= "     T2.SEQ, ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.SENKOU_KIND, ";
        $query .= "     T2.SENKOU_NAME, ";
        $query .= "     T2.STAT_CD, ";
        $query .= "     T2.STAT_NAME, ";
        $query .= "     T2.SCHOOL_GROUP, ";
        $query .= "     T2.SCHOOL_GROUP_NAME, ";
        $query .= "     T2.FACULTYCD, ";
        $query .= "     T2.FACULTYNAME, ";
        $query .= "     T2.DEPARTMENTCD, ";
        $query .= "     T2.DEPARTMENTNAME, ";
        $query .= "     T2.JOBTYPE_LCD, ";
        $query .= "     T2.JOBTYPE_LNAME, ";
        $query .= "     T2.JOBTYPE_MCD, ";
        $query .= "     T2.JOBTYPE_MNAME, ";
        $query .= "     T2.JOBTYPE_SCD, ";
        $query .= "     T2.JOBTYPE_SSCD, ";
        $query .= "     T2.JOBTYPE_SNAME, ";
        $query .= "     T2.AFT_GRAD_PREF_CD, ";
        $query .= "     T2.AFT_GRAD_PREF_NAME, ";
        $query .= "     T2.HOWTOEXAM, ";
        $query .= "     T2.HOWTOEXAM_NAME, ";
        $query .= "     T2.DECISION, ";
        $query .= "     T2.DECISION_NAME, ";
        $query .= "     T2.PLANSTAT, ";
        $query .= "     T2.PLANSTAT_NAME ";
        $query .= " FROM ";
        $query .= "     T_REGD T1 ";
        $query .= "     INNER JOIN T_AFT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.SEQ ";

        return $query;
    }

    //CSV出力（評定データ）
    public function getCsvStudyrec($model)
    {
        $query = "";
        //生徒データ
        $query .= " WITH T_REGD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "              ON T2.YEAR = T1.YEAR ";
        $query .= "             AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "             AND T2.GRADE = T1.GRADE ";
        $query .= "             AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->field["MAJORCD"] != "A-ALL") {
            $query .= "         AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["MAJORCD"]}' ";
        }
        $query .= " ) ";
        //MAX年度（留年していない年度かな？）
        $query .= " , STUDYREC_YEAR AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         ANNUAL, ";
        $query .= "         MAX(YEAR) AS YEAR ";
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         ANNUAL ";
        $query .= " ) ";
        //評定データ
        $query .= " , T_STUDYREC AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ANNUAL, ";
        $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "         L1.SUBCLASSNAME, ";
        $query .= "         T1.VALUATION ";
        $query .= "     FROM ";
        $query .= "         SCHREG_STUDYREC_DAT T1 ";
        $query .= "         LEFT JOIN SUBCLASS_MST L1 ON L1.CLASSCD = T1.CLASSCD AND L1.SCHOOL_KIND = T1.SCHOOL_KIND AND L1.CURRICULUM_CD = T1.CURRICULUM_CD AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOLCD = '0' ";
        $query .= "         AND T1.VALUATION IS NOT NULL ";
        $query .= "         AND T1.VALUATION >= 1 ";
        $query .= "         AND T1.VALUATION <= 5 ";
        $query .= "         AND T1.ANNUAL BETWEEN '01' AND '04' ";
        $query .= "         AND T1.YEAR IN (SELECT W1.YEAR FROM STUDYREC_YEAR W1 WHERE W1.SCHREGNO = T1.SCHREGNO) ";
        $query .= " ) ";
        //評定データ（念のためグループ化しておく）
        $query .= " , T_VALUATION AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         ANNUAL, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         MAX(VALUATION) AS VALUATION ";
        $query .= "     FROM ";
        $query .= "         T_STUDYREC ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         ANNUAL, ";
        $query .= "         SUBCLASSCD ";
        $query .= " ) ";
        //評定平均
        $query .= " , T_AVG AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         ANNUAL, ";
        $query .= "         DECIMAL(ROUND(AVG(FLOAT(VALUATION))*10,0)/10,5,1) AS AVG ";
        $query .= "     FROM ";
        $query .= "         T_VALUATION ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO, ";
        $query .= "         ANNUAL ";
        $query .= " ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     T2.SUBCLASSNAME, ";
        $query .= "     T2.ANNUAL, ";
        $query .= "     T2.VALUATION, ";
        $query .= "     T3.AVG ";
        $query .= " FROM ";
        $query .= "     T_REGD T1 ";
        $query .= "     INNER JOIN T_STUDYREC T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN T_AVG T3 ON T3.SCHREGNO = T2.SCHREGNO AND T3.ANNUAL = T2.ANNUAL ";
        $query .= " ORDER BY ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     T2.ANNUAL ";

        return $query;
    }

    //UPDATE
    public function getUpdateHoukoku($model, $houkokuList)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //変数
        list($coursecd, $majorcd) = explode("-", $model->field["MAJORCD"]);

        //DELETE
        //学校側（提出履歴記録用テーブル）
        $query  = "DELETE FROM AFT_GRAD_STAT_EXEC_DAT ";
        $query .= " WHERE YEAR              = '{$model->field["YEAR"]}' ";
        if ($model->field["MAJORCD"] != "A-ALL") {
            $query .= "     AND COURSECD        = '{$coursecd}' ";
            $query .= "     AND MAJORCD         = '{$majorcd}' ";
        }
        $db->query($query);

        //DELETE
        //県側（格納先テーブル）
        $query  = "DELETE FROM AFT_STAT_COURSE_STAT_SS_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '{$model->schoolcd}' ";
        $query .= "   AND YEAR              = '{$model->field["YEAR"]}' ";
        if ($model->field["MAJORCD"] != "A-ALL") {
            $query .= "     AND COURSECD        = '{$coursecd}' ";
            $query .= "     AND MAJORCD         = '{$majorcd}' ";
        }
        $db2->query($query);

        //INSERT
        //学校側（提出履歴記録用テーブル）
        $data = array();
        $data["CALC_DATE"][FUNC]            = "date(sysdate())";
        $data["CALC_TIME"][FUNC]            = "time(sysdate())";
        $data["COURSECD"][TEXT]             = $coursecd;
        $data["MAJORCD"][TEXT]              = $majorcd;
        $data["YEAR"][TEXT]                 = $model->field["YEAR"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query  = Query::insertSQL($data, "AFT_GRAD_STAT_EXEC_DAT");
        $db->query($query);

        //INSERT
        foreach ($houkokuList as $key => $row) {
            //県側（格納先テーブル）
            $data = array();
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["SCHOOLNAME"][TEXT]           = $row["EDBOARD_SCHOOLNAME"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["TOROKU_DATE"][TEXT]          = str_replace("/", "-", $row["TOROKU_DATE"]);
            $data["SEQ"][NUMBER]                = $row["SEQ"];
            $data["SCHREGNO"][TEXT]             = $row["SCHREGNO"];
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["MAJORNAME"][TEXT]            = $row["MAJORNAME"];
            $data["GRADE"][TEXT]                = $row["GRADE"];
            $data["HR_CLASS"][TEXT]             = $row["HR_CLASS"];
            $data["HR_CLASS_NAME"][TEXT]        = $row["HR_CLASS_NAME"];
            $data["ATTENDNO"][TEXT]             = $row["ATTENDNO"];
            $data["NAME"][TEXT]                 = $row["NAME"];
            $data["NAME_KANA"][TEXT]            = $row["NAME_KANA"];
            $data["BIRTHDAY"][TEXT]             = str_replace("/", "-", $row["BIRTHDAY"]);
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["FINSCHOOLCD"][TEXT]          = $row["FINSCHOOLCD"];
            $data["FINSCHOOLNAME"][TEXT]        = $row["FINSCHOOL_NAME"];
            $data["PREF_CD"][TEXT]              = $row["FINSCHOOL_PREF_CD"];
            $data["PREF_NAME"][TEXT]            = $row["FINSCHOOL_PREF_NAME"];
            //進路データ
            $data["SENKOU_KIND"][TEXT]          = $row["SENKOU_KIND"];
            $data["SENKOU_NAME"][TEXT]          = $row["SENKOU_NAME"];
            $data["STAT_CD"][TEXT]              = $row["STAT_CD"];
            $data["STAT_NAME"][TEXT]            = $row["STAT_NAME"];
            $data["SCHOOL_GROUP"][TEXT]         = $row["SCHOOL_GROUP"];
            $data["SCHOOL_GROUP_NAME"][TEXT]    = $row["SCHOOL_GROUP_NAME"];
            $data["FACULTYCD"][TEXT]            = $row["FACULTYCD"];
            $data["FACULTYNAME"][TEXT]          = $row["FACULTYNAME"];
            $data["DEPARTMENTCD"][TEXT]         = $row["DEPARTMENTCD"];
            $data["DEPARTMENTNAME"][TEXT]       = $row["DEPARTMENTNAME"];
            $data["JOBTYPE_LCD"][TEXT]          = $row["JOBTYPE_LCD"];
            $data["JOBTYPE_LNAME"][TEXT]        = $row["JOBTYPE_LNAME"];
            $data["JOBTYPE_MCD"][TEXT]          = $row["JOBTYPE_MCD"];
            $data["JOBTYPE_MNAME"][TEXT]        = $row["JOBTYPE_MNAME"];
            $data["JOBTYPE_SCD"][TEXT]          = $row["JOBTYPE_SCD"];
            $data["JOBTYPE_SSCD"][TEXT]         = $row["JOBTYPE_SSCD"];
            $data["JOBTYPE_SNAME"][TEXT]        = $row["JOBTYPE_SNAME"];
            $data["AFT_GRAD_PREF_CD"][TEXT]     = $row["AFT_GRAD_PREF_CD"];
            $data["AFT_GRAD_PREF_NAME"][TEXT]   = $row["AFT_GRAD_PREF_NAME"];
            $data["HOWTOEXAM"][TEXT]            = $row["HOWTOEXAM"];
            $data["HOWTOEXAM_NAME"][TEXT]       = $row["HOWTOEXAM_NAME"];
            $data["DECISION"][TEXT]             = $row["DECISION"];
            $data["DECISION_NAME"][TEXT]        = $row["DECISION_NAME"];
            $data["PLANSTAT"][TEXT]             = $row["PLANSTAT"];
            $data["PLANSTAT_NAME"][TEXT]        = $row["PLANSTAT_NAME"];
            //評定データ
            for ($annSeq = 1; $annSeq <= 4; $annSeq++) {
                $data["AVG".$annSeq][NUMBER]        = $row["AVG".$annSeq];
            }
            for ($subSeq = 1; $subSeq <= 40; $subSeq++) {
                $data["SUBCLASSCD_".$subSeq][TEXT]      = $row["SUBCLASSCD_".$subSeq];
                $data["SUBCLASSNAME_".$subSeq][TEXT]    = $row["SUBCLASSNAME_".$subSeq];
                for ($annSeq = 1; $annSeq <= 4; $annSeq++) {
                    $data["SUBCLASS".$subSeq."_VALUE".$annSeq][NUMBER]  = $row["SUBCLASS".$subSeq."_VALUE".$annSeq];
                }
            }
            $data["SUBCLASS_OVERFLG"][TEXT]     = $row["SUBCLASS_OVERFLG"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            $query  = Query::insertSQL($data, "AFT_STAT_COURSE_STAT_SS_DAT");
            $db2->query($query);
        }

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }
}
