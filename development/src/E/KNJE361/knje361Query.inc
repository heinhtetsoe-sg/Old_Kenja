<?php

require_once('for_php7.php');
class knje361query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJE361' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年度一覧
    public function getYearSeme()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //大学
    public function getCollege()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHOOL_CD || ':' || T1.SCHOOL_NAME AS LABEL, ";
        $query .= "     T1.SCHOOL_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 学校系列
    public function getSchoolGroup($schoolCd)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_GROUP ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD = '{$schoolCd}' ";

        return $query;
    }
    //学部
    public function getFaculty($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.FACULTYCD || ':' || T1.FACULTYNAME AS LABEL, ";
        $query .= "     T1.FACULTYCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLEGE_FACULTY_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_CD = '{$model->field["SCHOOL_CD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学科
    public function getDepartment($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.DEPARTMENTCD || ':' || T1.DEPARTMENTNAME AS LABEL, ";
        $query .= "     T1.DEPARTMENTCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLEGE_DEPARTMENT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_CD = '{$model->field["SCHOOL_CD"]}' ";
        $query .= "     AND T1.FACULTYCD = '{$model->field["FACULTYCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //更新データの有無
    public function updDataExists($year, $seq, $schregNo)
    {
        $query  = "SELECT COUNT(*) ";
        $query .= " FROM AFT_GRAD_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEQ = {$seq} ";
        $query .= "     AND SCHREGNO = '{$schregNo}' ";

        return $query;
    }

    //新規
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $seq = $db->getOne(knje361Query::getMaxSeq2($data_arr[$i]["YEAR"])) + 1;

            $data = array();
            //AFT_GRAD_COURSE_DAT
            $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
            $data["SEQ"][NUMBER]        = $seq;
            $data["SCHREGNO"][TEXT]     = $data_arr[$i]["SCHREGNO"];
            $data["SENKOU_KIND"][TEXT]  = "0";
            $data["TOROKU_DATE"][TEXT]  = $data_arr[$i]["TOROKU_DATE"];
            $data["STAT_CD"][TEXT]      = $data_arr[$i]["STAT_CD"];
            $data["SCHOOL_GROUP"][TEXT] = $db->getOne(knje361Query::getSchoolGroup($data_arr[$i]["STAT_CD"]));
            $data["FACULTYCD"][TEXT]    = $data_arr[$i]["FACULTYCD"];
            $data["DEPARTMENTCD"][TEXT] = $data_arr[$i]["DEPARTMENTCD"];
            $data["HOWTOEXAM"][TEXT]    = $data_arr[$i]["HOWTOEXAM"];
            $setDecision = $data_arr[$i]["DECISION1"];
            $setDecision = $data_arr[$i]["DECISION2"] ? $data_arr[$i]["DECISION2"] : $setDecision;
            $setDecision = $data_arr[$i]["DECISION3"] ? $data_arr[$i]["DECISION3"] : $setDecision;
            $data["DECISION"][TEXT]     = $setDecision;
            $data["PLANSTAT"][TEXT]     = $data_arr[$i]["PLANSTAT"];
            $data["STAT_DATE1"][TEXT]   = $data_arr[$i]["STAT_DATE1"];
            $data["STAT_DATE3"][TEXT]   = $data_arr[$i]["STAT_DATE3"];
            $data["CONTENTEXAM"][TEXT]  = $data_arr[$i]["CONTENTEXAM"];
            $data["REASONEXAM"][TEXT]   = $data_arr[$i]["REASONEXAM"];

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "SYSDATE()";

            $query = Query::insertSQL($data, "AFT_GRAD_COURSE_DAT");
            $db->query($query);

            $data = array();
            //AFT_GRAD_COURSE_DETAIL_DAT
            $query  = " DELETE ";
            $query .= " FROM AFT_GRAD_COURSE_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
            $query .= "     AND SEQ = {$seq} ";
            $query .= "     AND DETAIL_SEQ = 1 ";
            $db->query($query);

            $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
            $data["SEQ"][NUMBER]        = $seq;
            $data["DETAIL_SEQ"][NUMBER] = 1;
            $data["REMARK1"][TEXT]      = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]      = $data_arr[$i]["REMARK2"];
            $data["REMARK3"][TEXT]      = $data_arr[$i]["REMARK3"];
            $data["REMARK4"][TEXT]      = $data_arr[$i]["REMARK4"];
            $data["REMARK5"][TEXT]      = $data_arr[$i]["REMARK5"];
            $data["REMARK6"][TEXT]      = $data_arr[$i]["REMARK6"];
            $data["REMARK7"][TEXT]      = $data_arr[$i]["REMARK7"];
            $data["REMARK8"][TEXT]      = $data_arr[$i]["REMARK8"];
            $data["REMARK9"][TEXT]      = $data_arr[$i]["REMARK9"];

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "SYSDATE()";

            $query = Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT");
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //更新
    public function updateQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $seq = $data_arr[$i]["SEQ"];

            $data = array();
            //AFT_GRAD_COURSE_DAT
            $data["SENKOU_KIND"][TEXT]  = "0";
            $data["TOROKU_DATE"][TEXT]  = $data_arr[$i]["TOROKU_DATE"];
            $data["STAT_CD"][TEXT]      = $data_arr[$i]["STAT_CD"];
            $data["SCHOOL_GROUP"][TEXT] = $db->getOne(knje361Query::getSchoolGroup($data_arr[$i]["STAT_CD"]));
            $data["FACULTYCD"][TEXT]    = $data_arr[$i]["FACULTYCD"];
            $data["DEPARTMENTCD"][TEXT] = $data_arr[$i]["DEPARTMENTCD"];
            $data["HOWTOEXAM"][TEXT]    = $data_arr[$i]["HOWTOEXAM"];
            $setDecision = $data_arr[$i]["DECISION1"];
            $setDecision = $data_arr[$i]["DECISION2"] ? $data_arr[$i]["DECISION2"] : $setDecision;
            $setDecision = $data_arr[$i]["DECISION3"] ? $data_arr[$i]["DECISION3"] : $setDecision;
            $data["DECISION"][TEXT]     = $setDecision;
            $data["PLANSTAT"][TEXT]     = $data_arr[$i]["PLANSTAT"];
            $data["STAT_DATE1"][TEXT]   = $data_arr[$i]["STAT_DATE1"];
            $data["STAT_DATE3"][TEXT]   = $data_arr[$i]["STAT_DATE3"];
            $data["CONTENTEXAM"][TEXT]  = $data_arr[$i]["CONTENTEXAM"];
            $data["REASONEXAM"][TEXT]   = $data_arr[$i]["REASONEXAM"];

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
            $where .= "     AND SEQ = {$seq} ";
            $where .= "     AND SCHREGNO = '{$data_arr[$i]["SCHREGNO"]}' ";

            $query = Query::updateSQL($data, "AFT_GRAD_COURSE_DAT", $where);
            $db->query($query);

            $data = array();
            //AFT_GRAD_COURSE_DETAIL_DAT
            $query  = " DELETE ";
            $query .= " FROM AFT_GRAD_COURSE_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
            $query .= "     AND SEQ = {$seq} ";
            $query .= "     AND DETAIL_SEQ = 1 ";
            $db->query($query);

            $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
            $data["SEQ"][NUMBER]        = $seq;
            $data["DETAIL_SEQ"][NUMBER] = 1;
            $data["REMARK1"][TEXT]      = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]      = $data_arr[$i]["REMARK2"];
            $data["REMARK3"][TEXT]      = $data_arr[$i]["REMARK3"];
            $data["REMARK4"][TEXT]      = $data_arr[$i]["REMARK4"];
            $data["REMARK5"][TEXT]      = $data_arr[$i]["REMARK5"];
            $data["REMARK6"][TEXT]      = $data_arr[$i]["REMARK6"];
            $data["REMARK7"][TEXT]      = $data_arr[$i]["REMARK7"];
            $data["REMARK8"][TEXT]      = $data_arr[$i]["REMARK8"];
            $data["REMARK9"][TEXT]      = $data_arr[$i]["REMARK9"];

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "SYSDATE()";

            $query = Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT");
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //SEQのMAX値取得
    public function getMaxSeq2($year)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEQ) AS SEQ ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }

    //駿台かどうか
    public function isSundaikoufu($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";
        $query .= "     AND NAME1='sundaikoufu' ";

        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model, $isSundaikoufu)
    {
        $query  = " SELECT ";
        $query .= "     AFT.YEAR, ";
        $query .= "     AFT.SEQ, ";
        $query .= "     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.GRADE    ELSE G2.GRD_GRADE    END AS GRADE, ";
        $query .= "     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.HR_CLASS ELSE G2.GRD_HR_CLASS END AS HR_CLASS, ";
        $query .= "     CASE WHEN T1.SCHREGNO IS NOT NULL THEN T1.ATTENDNO ELSE G2.GRD_ATTENDNO END AS ATTENDNO, ";
        $query .= "     AFT.SCHREGNO, ";
        $query .= "     CASE WHEN T2.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T2.NAME ";
        $query .= "          ELSE G2.NAME ";
        $query .= "     END AS NAME, ";
        $query .= "     N6.ABBV1, ";
        if ($isSundaikoufu) {
            $query .= "     CASE WHEN N7.NAMECD1 IS NOT NULL THEN '〇' ELSE '' END AS GRD_TERM, ";
        }
        $query .= "     FIN.FINSCHOOL_NAME, ";
        //「現役/既卒」model側で以下の通り表示する。
        $query .= "     CASE WHEN KISOTU_FLAG = '0' ";
        $query .= "          THEN 1 ";                      //「現役」と表示
        $query .= "          WHEN G2.GRD_DATE IS NULL ";
        $query .= "          THEN 2 ";                      //空白・・・TODO：宮城さんが「既卒」と表示すべきか？
        $query .= "          WHEN MONTH(G2.GRD_DATE) <= 3 ";
        $query .= "          THEN YEAR(G2.GRD_DATE) - 1 ";  //「xxxx年度卒」と表示
        $query .= "          ELSE YEAR(G2.GRD_DATE) ";      //「xxxx年度卒」と表示
        $query .= "     END AS GENEKI_KISOTU, ";
        $query .= "     AFT.TOROKU_DATE, ";
        if ($model->Properties["useEntDiv_Hyouji"] == "1") {
            $query .= "     CASE ";
            $query .= "         WHEN T1.SCHREGNO IS NOT NULL THEN N4.NAME1 ";
            $query .= "         ELSE N5.NAME1 ";
            $query .= "     END AS ENT_DIV, ";
        }
        $query .= "     AFT.STAT_CD, ";
        $query .= "     COLLEGE.SCHOOL_NAME, ";
        $query .= "     AFT.FACULTYCD, ";
        $query .= "     FACULTY.FACULTYNAME, ";
        $query .= "     AFT.DEPARTMENTCD, ";
        $query .= "     DEPARTMENT.DEPARTMENTNAME, ";
        $query .= "     AFT_D.REMARK1, ";
        $query .= "     N3.NAME1 AS REMARK1_NAME, ";
        $query .= "     AFT_D.REMARK2, ";
        $query .= "     COL_CAL.PROGRAM_NAME, ";
        $query .= "     AFT_D.REMARK3, ";
        $query .= "     COL_CAL.FORM_NAME, ";
        $query .= "     AFT_D.REMARK4, ";
        $query .= "     AFT_D.REMARK5, ";
        $query .= "     AFT.HOWTOEXAM, ";
        $query .= "     N8.NAME1 AS HOWTOEXAM_NAME, ";
        $query .= "     CASE WHEN N2.NAMESPARE1 = '1' ";
        $query .= "          THEN AFT.DECISION ";
        $query .= "          ELSE '' ";
        $query .= "     END AS DECISION1, ";
        $query .= "     CASE WHEN N2.NAMESPARE1 = '2' ";
        $query .= "          THEN AFT.DECISION ";
        $query .= "          ELSE '' ";
        $query .= "     END AS DECISION2, ";
        $query .= "     CASE WHEN N2.NAMESPARE1 = '3' ";
        $query .= "          THEN AFT.DECISION ";
        $query .= "          ELSE '' ";
        $query .= "     END AS DECISION3, ";
        $query .= "     AFT.PLANSTAT, ";
        $query .= "     AFT_D.REMARK6, ";
        $query .= "     AFT_D.REMARK7, ";
        $query .= "     AFT_D.REMARK8, ";
        $query .= "     AFT.STAT_DATE1, ";
        $query .= "     AFT.STAT_DATE3, ";
        $query .= "     AFT_D.REMARK9, ";
        $query .= "     AFT.CONTENTEXAM, ";
        $query .= "     AFT.REASONEXAM, ";
        $query .= "     '{$model->lastColumn}' AS LASTCOLUMN ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT AFT ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = AFT.SCHREGNO ";
        $query .= "     LEFT JOIN GRD_BASE_MST G2 ON G2.SCHREGNO = AFT.SCHREGNO ";

        $query .= " INNER JOIN ( ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         '0' AS KISOTU_FLAG ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT GT1 ON GT1.YEAR = T1.YEAR ";
        $query .= "         AND GT1.GRADE = T1.GRADE ";
        $query .= "         AND GT1.SCHOOL_KIND = 'H' ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."')";
        if ($model->field["KISOTU"] == "on") {
            $query .= "     UNION ";
            $query .= "         (SELECT ";
            $query .= "             T2.SCHREGNO, ";
            $query .= "             GRD_GRADE AS GRADE, ";
            $query .= "             GRD_HR_CLASS AS HR_CLASS, ";
            $query .= "             GRD_ATTENDNO AS ATTENDNO, ";
            $query .= "             '1' AS KISOTU_FLAG ";
            $query .= "         FROM ";
            $query .= "             AFT_GRAD_COURSE_DAT T2 ";
            $query .= "         LEFT JOIN GRD_BASE_MST GMST ";
            $query .= "             ON GMST.SCHREGNO = T2.SCHREGNO ";
            $query .= "         WHERE ";
            $query .= "             T2.YEAR = '".substr($model->field["YEAR"], 0, 4)."' ";
            $query .= "         AND NOT EXISTS( ";
            $query .= "             SELECT ";
            $query .= "                 * ";
            $query .= "             FROM ";
            $query .= "                 SCHREG_REGD_DAT ";
            $query .= "             WHERE ";
            $query .= "                 SCHREG_REGD_DAT.YEAR || SCHREG_REGD_DAT.SEMESTER = '".$model->field["YEAR"]."' ";
            $query .= "             AND T2.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ";
            $query .= "             ) ";
            $query .= "         ) ";
        }
        $query .= "     ) T1 ON T1.SCHREGNO = AFT.SCHREGNO ";

        $query .= "     LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT AFT_D ON AFT_D.YEAR = AFT.YEAR ";
        $query .= "          AND AFT_D.SEQ = AFT.SEQ ";
        $query .= "          AND AFT_D.DETAIL_SEQ = 1 ";
        $query .= "     LEFT JOIN COLLEGE_MST COLLEGE ON COLLEGE.SCHOOL_CD = AFT.STAT_CD ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST FACULTY ON FACULTY.SCHOOL_CD = AFT.STAT_CD ";
        $query .= "          AND FACULTY.FACULTYCD = AFT.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST DEPARTMENT ON DEPARTMENT.SCHOOL_CD = AFT.STAT_CD ";
        $query .= "          AND DEPARTMENT.FACULTYCD = AFT.FACULTYCD ";
        $query .= "          AND DEPARTMENT.DEPARTMENTCD = AFT.DEPARTMENTCD ";
        $query .= "     LEFT JOIN COLLEGE_EXAM_CALENDAR COL_CAL ON COL_CAL.YEAR = AFT.YEAR ";
        $query .= "          AND COL_CAL.SCHOOL_CD = AFT.STAT_CD ";
        $query .= "          AND COL_CAL.FACULTYCD = AFT.FACULTYCD ";
        $query .= "          AND COL_CAL.DEPARTMENTCD = AFT.DEPARTMENTCD ";
        $query .= "          AND COL_CAL.ADVERTISE_DIV = AFT_D.REMARK1 ";
        $query .= "          AND COL_CAL.PROGRAM_CD = AFT_D.REMARK2 ";
        $query .= "          AND COL_CAL.FORM_CD = AFT_D.REMARK3 ";
        $query .= "          AND COL_CAL.S_CD = AFT_D.REMARK5 ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'E005' ";
        $query .= "                          AND N2.NAMECD2 = AFT.DECISION ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'E044' ";
        $query .= "                          AND N3.NAMECD2 = AFT_D.REMARK1 ";
        $query .= "     LEFT JOIN NAME_MST N4 ON N4.NAMECD1 = 'A002' ";
        $query .= "                          AND N4.NAMECD2 = T2.ENT_DIV ";
        $query .= "     LEFT JOIN NAME_MST N5 ON N5.NAMECD1 = 'A002' ";
        $query .= "                          AND N5.NAMECD2 = G2.ENT_DIV ";
        $query .= "     LEFT JOIN NAME_MST N6 ON N6.NAMECD1 = 'Z002' ";
        $query .= "                          AND N6.NAMECD2 = T2.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD=T2.FINSCHOOLCD ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT HDAT ON HDAT.SCHREGNO = AFT.SCHREGNO ";
        $query .= "                          AND HDAT.SCHOOL_KIND = 'J' ";
        $query .= "     LEFT JOIN NAME_MST N7 ON N7.NAMECD1 = 'A023' ";
        $query .= "                          AND N7.NAME1 = 'P' ";
        $query .= "                          AND N7.NAMESPARE1 = HDAT.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST N8 ON N8.NAMECD1 = 'E002' AND N8.NAMECD2 = AFT.HOWTOEXAM ";
        $query .= " WHERE ";
        $query .= "     AFT.YEAR = '".substr($model->field["YEAR"], 0, 4)."' ";
        $query .= "     AND AFT.SENKOU_KIND = '0' ";
        if ($model->field["SCHOOL_CD"]) {
            $query .= "     AND AFT.STAT_CD = '{$model->field["SCHOOL_CD"]}' ";
        }
        if ($model->field["FACULTYCD"]) {
            $query .= "     AND AFT.FACULTYCD = '{$model->field["FACULTYCD"]}' ";
        }
        if ($model->field["DEPARTMENTCD"]) {
            $query .= "     AND AFT.DEPARTMENTCD = '{$model->field["DEPARTMENTCD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     AFT.SCHREGNO, ";
        $query .= "     AFT.SEQ ";

        return $query;
    }
}
