<?php

require_once('for_php7.php');
class knje362query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJE362' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年度一覧
    public function getYearSeme($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
                $query .= "          ON G1.YEAR         = T1.YEAR ";
                $query .= "         AND G1.GRADE        = T1.GRADE ";
                $query .= "         AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "          ON G1.YEAR         = T1.YEAR ";
            $query .= "         AND G1.GRADE        = T1.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    public function getGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE_NAME1 AS LABEL, ";
        $query .= "     T1.GRADE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->yearOnly}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->auth == DEF_UPDATE_RESTRICT) {
            $query .= " AND EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 SCHREG_REGD_HDAT S1 ";
            $query .= "             WHERE ";
            $query .= "                 T1.YEAR     = S1.YEAR AND ";
            $query .= "                 T1.GRADE    = S1.GRADE AND ";
            $query .= "                 S1.YEAR || S1.SEMESTER = '".$model->field["YEAR"]."' AND ";
            $query .= "                 '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
            $query .= "             ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス
    public function getHrclass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.HR_NAME AS LABEL, ";
        $query .= "     T1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";
        if ($model->auth == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース
    public function getCourse($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE || ':' || L1.MAJORNAME || L2.COURSECODENAME AS LABEL, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN V_MAJOR_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "           AND T1.COURSECD = L1.COURSECD ";
        $query .= "           AND T1.MAJORCD = L1.MAJORCD ";
        $query .= "     INNER JOIN V_COURSECODE_MST L2 ON T1.YEAR = L2.YEAR ";
        $query .= "           AND T1.COURSECODE = L2.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";
        if ($model->field["HR_CLASS"]) {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学籍番号
    public function getSchreg($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     L2.HR_NAME || '-' || T1.ATTENDNO || '：' || L1.NAME || '(' || T1.SCHREGNO || ')' AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT L2 ON T1.YEAR = L2.YEAR ";
        $query .= "           AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "           AND T1.GRADE = L2.GRADE ";
        $query .= "           AND T1.HR_CLASS = L2.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";
        if ($model->field["HR_CLASS"]) {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        }
        if ($model->field["COURSE"]) {
            $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
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

    //権限チェック
    public function checkAuth($model, $year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
                $query .= "          ON T1.YEAR         = T2.YEAR ";
                $query .= "         AND T1.GRADE        = T2.GRADE ";
                $query .= "         AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
            $query .= "          ON T1.YEAR         = T2.YEAR ";
            $query .= "         AND T1.GRADE        = T2.GRADE ";
            $query .= "         AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$year."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     EXISTS(SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_HDAT S1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR     = S1.YEAR AND ";
        $query .= "                 T1.SEMESTER = S1.SEMESTER AND ";
        $query .= "                 T1.GRADE    = S1.GRADE AND ";
        $query .= "                 T1.HR_CLASS = S1.HR_CLASS ";
        $query .= "             ) ";

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
            $seq = $db->getOne(knje362Query::getMaxSeq2($data_arr[$i]["YEAR"])) + 1;

            $data = array();
            //AFT_GRAD_COURSE_DAT
            $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
            $data["SEQ"][NUMBER]        = $seq;
            $data["SCHREGNO"][TEXT]     = $data_arr[$i]["SCHREGNO"];
            $data["SENKOU_KIND"][TEXT]  = "0";
            $data["TOROKU_DATE"][TEXT]  = $data_arr[$i]["TOROKU_DATE"];
            $data["STAT_CD"][TEXT]      = $data_arr[$i]["STAT_CD"];
            $data["SCHOOL_GROUP"][TEXT] = $db->getOne(knje362Query::getSchoolGroup($data_arr[$i]["STAT_CD"]));
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
            $data["SCHOOL_GROUP"][TEXT] = $db->getOne(knje362Query::getSchoolGroup($data_arr[$i]["STAT_CD"]));
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
    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     AFT.SEQ, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     AFT.TOROKU_DATE, ";
        if ($model->Properties["useEntDiv_Hyouji"] == "1") {
            $query .= "     N4.NAME1 AS ENT_DIV, ";
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
        $query .= "     N5.NAME1 AS HOWTOEXAM_NAME, ";
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
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN AFT_GRAD_COURSE_DAT AFT ON AFT.YEAR = T1.YEAR ";
        $query .= "          AND AFT.SCHREGNO = T1.SCHREGNO ";
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
        $query .= "     LEFT JOIN NAME_MST N5 ON N5.NAMECD1 = 'E002' AND N5.NAMECD2 = AFT.HOWTOEXAM ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND AFT.SENKOU_KIND = '0' ";
        if ($model->field["GRADE"]) {
            $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";
        }
        if ($model->field["HR_CLASS"]) {
            $query .= "     AND T1.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        }
        if ($model->field["COURSE"]) {
            $query .= "     AND T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '{$model->field["COURSE"]}' ";
        }
        if ($model->field["SCHREGNO"]) {
            $query .= "     AND T1.SCHREGNO = '{$model->field["SCHREGNO"]}' ";
        }
        if ($model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "     AND EXISTS (SELECT ";
            $query .= "                     'X' ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_HDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     T1.YEAR     = S1.YEAR AND ";
            $query .= "                     T1.SEMESTER = S1.SEMESTER AND ";
            $query .= "                     T1.GRADE    = S1.GRADE AND ";
            $query .= "                     T1.HR_CLASS = S1.HR_CLASS AND ";
            $query .= "                     '".STAFFCD."' IN (S1.TR_CD1,S1.TR_CD2,S1.TR_CD3,S1.SUBTR_CD1,S1.SUBTR_CD2,S1.SUBTR_CD3) ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }
}
