<?php

require_once('for_php7.php');

class knje364bquery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID   = 'KNJE364B' AND ";
        $query .= "     INVALID_FLG = '0' ";

        return $query;
    }

    //年度一覧
    function getYearSeme() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR       = T2.YEAR ";
        $query .= "                               AND T1.SEMESTER   = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //大学
    function getCollege()
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

    //学部
    function getFaculty($model)
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
    function getDepartment($model)
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

    //アンケート一覧取得
    function getQuestionnaireList($model) {
        $query  = " SELECT ";
        $query .= "     T1.QUESTIONNAIRECD AS VALUE, ";
        $query .= "     T1.QUESTIONNAIRECD || '：' || T2.QUESTIONNAIRENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_YDAT T1 ";
        $query .= "     LEFT JOIN QUESTIONNAIRE_MST T2 ON T1.QUESTIONNAIRECD = T2.QUESTIONNAIRECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".substr($model->field["YEAR"],0,4)."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //更新データの有無
    function updDataExists($entrydate, $seq, $schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTRYDATE = '".str_replace("/", "-", $entrydate)."' AND ";
        $query .= "     SEQ         =  ".$seq." AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //存在チェック -- QUESTIONNAIRE_MST
    function chkQuestionnaireMst($year, $questionnairecd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     QUESTIONNAIRE_YDAT T1, ";
        $query .= "     QUESTIONNAIRE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' AND ";
        $query .= "     T1.QUESTIONNAIRECD = T2.QUESTIONNAIRECD AND ";
        $query .= "     T1.QUESTIONNAIRECD = '".$questionnairecd."' ";

        return $query;
    }

    //存在チェック -- FACULTY_GROUP_MST
    function chkFacultyGroupMst($faculty_group) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     FACULTY_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     FACULTY_GROUP = '".$faculty_group."' ";

        return $query;
    }

    //存在チェック -- DEPARTMENT_GROUP_MST
    function chkDepartmentGroupMst($department_group) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     DEPARTMENT_GROUP_MST ";
        $query .= " WHERE ";
        $query .= "     DEPARTMENT_GROUP = '".$department_group."' ";

        return $query;
    }

    //存在チェック -- COLLEGE_MST
    function chkCollegeMst($school_cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD = '".$school_cd."' ";

        return $query;
    }

    //存在チェック -- COLLEGE_FACULTY_MST
    function chkCollegeFacultyMst($school_cd, $facultycd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLEGE_FACULTY_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD = '".$school_cd."' AND ";
        $query .= "     FACULTYCD = '".$facultycd."' ";

        return $query;
    }

    //存在チェック -- COLLEGE_DEPARTMENT_MST
    function chkCollegeDepartmentMst($school_cd, $facultycd, $departmentcd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     COLLEGE_DEPARTMENT_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD   = '".$school_cd."' AND ";
        $query .= "     FACULTYCD   = '".$facultycd."' AND ";
        $query .= "     DEPARTMENTCD = '".$departmentcd."' ";

        return $query;
    }

    //存在チェック -- JOBTYPE_L_MST
    function chkJobtypeLMst($jobtype_lcd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_L_MST ";
        $query .= " WHERE ";
        $query .= "     JOBTYPE_LCD = '".$jobtype_lcd."' ";

        return $query;
    }

    //存在チェック -- JOBTYPE_M_MST
    function chkJobtypeMMst($jobtype_lcd, $jobtype_mcd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_M_MST ";
        $query .= " WHERE ";
        $query .= "     JOBTYPE_LCD = '".$jobtype_lcd."' AND ";
        $query .= "     JOBTYPE_MCD = '".$jobtype_mcd."' ";

        return $query;
    }

    //存在チェック -- JOBTYPE_S_MST
    function chkJobtypeSMst($jobtype_lcd, $jobtype_mcd, $jobtype_scd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JOBTYPE_S_MST ";
        $query .= " WHERE ";
        $query .= "     JOBTYPE_LCD = '".$jobtype_lcd."' AND ";
        $query .= "     JOBTYPE_MCD = '".$jobtype_mcd."' AND ";
        $query .= "     JOBTYPE_SCD = '".$jobtype_scd."' ";

        return $query;
    }

    //存在チェック -- V_NAME_MST
    function chkNameMst($year, $namecd1, $namecd2, $int="") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        if ($int) {
            $query .= "     INT(NAMECD2) = ".(int)$namecd2." ";
        } else {
            $query .= "     NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //新規
    function insertQueryCsv($model,&$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            $seq = $db->getOne(knje364bQuery::getMaxSeq($data_arr[$i]["ENTRYDATE"], $data_arr[$i]["SCHREGNO"])) + 1;

            $data = array();
            $data["ENTRYDATE"][TEXT]            = $data_arr[$i]["ENTRYDATE"];
            $data["SEQ"][NUMBER]                = $seq;
            $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
            $data["COURSE_KIND"][TEXT]          = $data_arr[$i]["COURSE_KIND"];
            $data["QUESTIONNAIRECD"][TEXT]      = $data_arr[$i]["QUESTIONNAIRECD"];
            if ($data_arr[$i]["COURSE_KIND"] == "1") {
                $data["SCHOOL_GROUP1"][TEXT]        = $data_arr[$i]["SCHOOL_GROUP1"];
                $data["FACULTY_GROUP1"][TEXT]       = $data_arr[$i]["FACULTY_GROUP1"];
                $data["DEPARTMENT_GROUP1"][TEXT]    = $data_arr[$i]["DEPARTMENT_GROUP1"];
                $data["SCHOOL_CD1"][TEXT]           = $data_arr[$i]["SCHOOL_CD1"];
                $data["FACULTYCD1"][TEXT]           = $data_arr[$i]["FACULTYCD1"];
                $data["DEPARTMENTCD1"][TEXT]        = $data_arr[$i]["DEPARTMENTCD1"];
                $data["HOWTOEXAM1"][NUMBER]         = $data_arr[$i]["HOWTOEXAM1"];
                $data["SCHOOL_GROUP2"][TEXT]        = $data_arr[$i]["SCHOOL_GROUP2"];
                $data["FACULTY_GROUP2"][TEXT]       = $data_arr[$i]["FACULTY_GROUP2"];
                $data["DEPARTMENT_GROUP2"][TEXT]    = $data_arr[$i]["DEPARTMENT_GROUP2"];
                $data["SCHOOL_CD2"][TEXT]           = $data_arr[$i]["SCHOOL_CD2"];
                $data["FACULTYCD2"][TEXT]           = $data_arr[$i]["FACULTYCD2"];
                $data["DEPARTMENTCD2"][TEXT]        = $data_arr[$i]["DEPARTMENTCD2"];
                $data["HOWTOEXAM2"][NUMBER]         = $data_arr[$i]["HOWTOEXAM2"];
            } else if ($data_arr[$i]["COURSE_KIND"] == "2") {
                $data["JOBTYPE_LCD1"][TEXT]         = $data_arr[$i]["JOBTYPE_LCD1"];
                $data["JOBTYPE_MCD1"][TEXT]         = $data_arr[$i]["JOBTYPE_MCD1"];
                $data["JOBTYPE_SCD1"][TEXT]         = $data_arr[$i]["JOBTYPE_SCD1"];
                $data["WORK_AREA1"][TEXT]           = $data_arr[$i]["WORK_AREA1"];
                $data["INTRODUCTION_DIV1"][TEXT]    = $data_arr[$i]["INTRODUCTION_DIV1"];
                $data["JOBTYPE_LCD2"][TEXT]         = $data_arr[$i]["JOBTYPE_LCD2"];
                $data["JOBTYPE_MCD2"][TEXT]         = $data_arr[$i]["JOBTYPE_MCD2"];
                $data["JOBTYPE_SCD2"][TEXT]         = $data_arr[$i]["JOBTYPE_SCD2"];
                $data["WORK_AREA2"][TEXT]           = $data_arr[$i]["WORK_AREA2"];
                $data["INTRODUCTION_DIV2"][TEXT]    = $data_arr[$i]["INTRODUCTION_DIV2"];
            }
            $data["REMARK"][TEXT]               = $data_arr[$i]["REMARK"];
            $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $query = Query::insertSQL($data, "COURSE_HOPE_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //更新
    function updateQueryCsv($model,&$data_arr) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            $seq = $data_arr[$i]["SEQ"];

            $data = array();
            $data["COURSE_KIND"][TEXT]          = $data_arr[$i]["COURSE_KIND"];
            $data["QUESTIONNAIRECD"][TEXT]      = $data_arr[$i]["QUESTIONNAIRECD"];
            if ($data_arr[$i]["COURSE_KIND"] == "1") {
                $data["SCHOOL_GROUP1"][TEXT]        = $data_arr[$i]["SCHOOL_GROUP1"];
                $data["FACULTY_GROUP1"][TEXT]       = $data_arr[$i]["FACULTY_GROUP1"];
                $data["DEPARTMENT_GROUP1"][TEXT]    = $data_arr[$i]["DEPARTMENT_GROUP1"];
                $data["SCHOOL_CD1"][TEXT]           = $data_arr[$i]["SCHOOL_CD1"];
                $data["FACULTYCD1"][TEXT]           = $data_arr[$i]["FACULTYCD1"];
                $data["DEPARTMENTCD1"][TEXT]        = $data_arr[$i]["DEPARTMENTCD1"];
                $data["HOWTOEXAM1"][NUMBER]         = $data_arr[$i]["HOWTOEXAM1"];
                $data["SCHOOL_GROUP2"][TEXT]        = $data_arr[$i]["SCHOOL_GROUP2"];
                $data["FACULTY_GROUP2"][TEXT]       = $data_arr[$i]["FACULTY_GROUP2"];
                $data["DEPARTMENT_GROUP2"][TEXT]    = $data_arr[$i]["DEPARTMENT_GROUP2"];
                $data["SCHOOL_CD2"][TEXT]           = $data_arr[$i]["SCHOOL_CD2"];
                $data["FACULTYCD2"][TEXT]           = $data_arr[$i]["FACULTYCD2"];
                $data["DEPARTMENTCD2"][TEXT]        = $data_arr[$i]["DEPARTMENTCD2"];
                $data["HOWTOEXAM2"][NUMBER]         = $data_arr[$i]["HOWTOEXAM2"];
            } else if ($data_arr[$i]["COURSE_KIND"] == "2") {
                $data["JOBTYPE_LCD1"][TEXT]         = $data_arr[$i]["JOBTYPE_LCD1"];
                $data["JOBTYPE_MCD1"][TEXT]         = $data_arr[$i]["JOBTYPE_MCD1"];
                $data["JOBTYPE_SCD1"][TEXT]         = $data_arr[$i]["JOBTYPE_SCD1"];
                $data["WORK_AREA1"][TEXT]           = $data_arr[$i]["WORK_AREA1"];
                $data["INTRODUCTION_DIV1"][TEXT]    = $data_arr[$i]["INTRODUCTION_DIV1"];
                $data["JOBTYPE_LCD2"][TEXT]         = $data_arr[$i]["JOBTYPE_LCD2"];
                $data["JOBTYPE_MCD2"][TEXT]         = $data_arr[$i]["JOBTYPE_MCD2"];
                $data["JOBTYPE_SCD2"][TEXT]         = $data_arr[$i]["JOBTYPE_SCD2"];
                $data["WORK_AREA2"][TEXT]           = $data_arr[$i]["WORK_AREA2"];
                $data["INTRODUCTION_DIV2"][TEXT]    = $data_arr[$i]["INTRODUCTION_DIV2"];
            }
            $data["REMARK"][TEXT]               = $data_arr[$i]["REMARK"];
            $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ";
            $where .= "     ENTRYDATE   = '".$data_arr[$i]["ENTRYDATE"]."' AND ";
            $where .= "     SEQ         = ".$seq." AND ";
            $where .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";

            $query = Query::updateSQL($data, "COURSE_HOPE_DAT", $where);
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //SEQのMAX値取得
    function getMaxSeq($entrydate, $schregno) {
        $query  = " SELECT ";
        $query .= "     MAX(SEQ) AS SEQ ";
        $query .= " FROM ";
        $query .= "     COURSE_HOPE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTRYDATE = '".str_replace("/", "-", $entrydate)."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " GROUP BY ";
        $query .= "     ENTRYDATE, ";
        $query .= "     SCHREGNO ";

        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    function selectMainQuery($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.NAME, ";
        $query .= "     T2.ENTRYDATE, ";
        $query .= "     T2.SEQ, ";
        $query .= "     T2.COURSE_KIND, ";
        $query .= "     CASE WHEN T2.COURSE_KIND = '1' THEN '進学' ";
        $query .= "          WHEN T2.COURSE_KIND = '2' THEN '就職' ";
        $query .= "          WHEN T2.COURSE_KIND = '3' THEN 'その他（家事手伝い等）' ";
        $query .= "          WHEN T2.COURSE_KIND = '4' THEN '未定' ";
        $query .= "          ELSE '' END COURSE_KIND_NAME, ";
        $query .= "     T2.QUESTIONNAIRECD, ";
        $query .= "     L2.QUESTIONNAIRENAME, ";
        $query .= "     T2.SCHOOL_GROUP1, ";
        $query .= "     S11.NAME1 AS SCHOOL_GROUP1_NAME, ";
        $query .= "     T2.FACULTY_GROUP1, ";
        $query .= "     S12.FACULTY_GROUPNAME AS FACULTY_GROUP1_NAME, ";
        $query .= "     T2.DEPARTMENT_GROUP1, ";
        $query .= "     S13.DEPARTMENT_GROUPNAME AS DEPARTMENT_GROUP1_NAME, ";
        $query .= "     T2.SCHOOL_CD1, ";
        $query .= "     S14.SCHOOL_NAME AS SCHOOL_NAME1, ";
        $query .= "     T2.FACULTYCD1, ";
        $query .= "     S15.FACULTYNAME AS FACULTYNAME1, ";
        $query .= "     T2.DEPARTMENTCD1, ";
        $query .= "     S16.DEPARTMENTNAME AS DEPARTMENTNAME1, ";
        $query .= "     T2.HOWTOEXAM1, ";
        $query .= "     S17.NAME1 AS HOWTOEXAM1_NAME, ";
        $query .= "     T2.SCHOOL_GROUP2, ";
        $query .= "     S21.NAME1 AS SCHOOL_GROUP2_NAME, ";
        $query .= "     T2.FACULTY_GROUP2, ";
        $query .= "     S22.FACULTY_GROUPNAME AS FACULTY_GROUP2_NAME, ";
        $query .= "     T2.DEPARTMENT_GROUP2, ";
        $query .= "     S23.DEPARTMENT_GROUPNAME AS DEPARTMENT_GROUP2_NAME, ";
        $query .= "     T2.SCHOOL_CD2, ";
        $query .= "     S24.SCHOOL_NAME AS SCHOOL_NAME2, ";
        $query .= "     T2.FACULTYCD2, ";
        $query .= "     S25.FACULTYNAME AS FACULTYNAME2, ";
        $query .= "     T2.DEPARTMENTCD2, ";
        $query .= "     S26.DEPARTMENTNAME AS DEPARTMENTNAME2, ";
        $query .= "     T2.HOWTOEXAM2, ";
        $query .= "     S27.NAME1 AS HOWTOEXAM2_NAME, ";
        $query .= "     T2.JOBTYPE_LCD1, ";
        $query .= "     S31.JOBTYPE_LNAME AS JOBTYPE_LCD1_NAME, ";
        $query .= "     T2.JOBTYPE_MCD1, ";
        $query .= "     S32.JOBTYPE_MNAME AS JOBTYPE_MCD1_NAME, ";
        $query .= "     T2.JOBTYPE_SCD1, ";
        $query .= "     S33.JOBTYPE_SNAME AS JOBTYPE_SCD1_NAME, ";
        $query .= "     T2.WORK_AREA1, ";
        $query .= "     CASE WHEN T2.WORK_AREA1 = '1' THEN '県内' ";
        $query .= "          WHEN T2.WORK_AREA1 = '2' THEN '県外' ";
        $query .= "          ELSE '' END WORK_AREA1_NAME, ";
        $query .= "     T2.INTRODUCTION_DIV1, ";
        $query .= "     CASE WHEN T2.INTRODUCTION_DIV1 = '1' THEN '学校紹介' ";
        $query .= "          WHEN T2.INTRODUCTION_DIV1 = '2' THEN '自己・縁故' ";
        $query .= "          WHEN T2.INTRODUCTION_DIV1 = '3' THEN '公務員' ";
        $query .= "          ELSE '' END INTRODUCTION_DIV1_NAME, ";
        $query .= "     T2.JOBTYPE_LCD2, ";
        $query .= "     S41.JOBTYPE_LNAME AS JOBTYPE_LCD2_NAME, ";
        $query .= "     T2.JOBTYPE_MCD2, ";
        $query .= "     S42.JOBTYPE_MNAME AS JOBTYPE_MCD2_NAME, ";
        $query .= "     T2.JOBTYPE_SCD2, ";
        $query .= "     S43.JOBTYPE_SNAME AS JOBTYPE_SCD2_NAME, ";
        $query .= "     T2.WORK_AREA2, ";
        $query .= "     CASE WHEN T2.WORK_AREA2 = '1' THEN '県内' ";
        $query .= "          WHEN T2.WORK_AREA2 = '2' THEN '県外' ";
        $query .= "          ELSE '' END WORK_AREA2_NAME, ";
        $query .= "     T2.INTRODUCTION_DIV2, ";
        $query .= "     CASE WHEN T2.INTRODUCTION_DIV2 = '1' THEN '学校紹介' ";
        $query .= "          WHEN T2.INTRODUCTION_DIV2 = '2' THEN '自己・縁故' ";
        $query .= "          WHEN T2.INTRODUCTION_DIV2 = '3' THEN '公務員' ";
        $query .= "          ELSE '' END INTRODUCTION_DIV2_NAME, ";
        $query .= "     T2.REMARK, ";
        $query .= "     VALUE(ATSBD.CLASS_SCORE,0) + VALUE(ATSBD.ABILITY_SCORE,0) ";
        $query .= "   + VALUE(ATSBD.TOEFL_SCORE,0) + VALUE(ATSBD.QUALIFIED_SCORE,0) ";
        $query .= "   + VALUE(ATSBD.ADJUSTMENT_SCORE,0) AS TOTAL_SCORE, ";
        $query .= "     ATSBD.CLASS_SCORE, ";
        $query .= "     ATSBD.ABILITY_SCORE, ";
        $query .= "     ATSBD.TOEFL_SCORE, ";
        $query .= "     ATSBD.QUALIFIED_SCORE, ";
        $query .= "     ATSBD.ADJUSTMENT_SCORE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ";
        $query .= "              ON T1.SCHREGNO     = L1.SCHREGNO ";
        $query .= "     INNER JOIN COURSE_HOPE_DAT T2 ";
        $query .= "              ON T1.SCHREGNO     = T2.SCHREGNO ";
        if ($this->field["COURSE_KIND"] == "1") {
            if ($model->field["SCHOOL_CD"]) {
                $query .= "             AND (    T2.SCHOOL_CD1 = '{$model->field["SCHOOL_CD"]}' ";
                $query .= "                   OR T2.SCHOOL_CD2 = '{$model->field["SCHOOL_CD"]}' ) ";
            }
            if ($model->field["FACULTYCD"]) {
                $query .= "             AND (    T2.FACULTYCD1 = '{$model->field["FACULTYCD"]}' ";
                $query .= "                   OR T2.FACULTYCD2 = '{$model->field["FACULTYCD"]}' ) ";
            }
            if ($model->field["DEPARTMENTCD"]) {
                $query .= "             AND (    T2.DEPARTMENTCD1 = '{$model->field["DEPARTMENTCD"]}' ";
                $query .= "                   OR T2.DEPARTMENTCD2 = '{$model->field["DEPARTMENTCD"]}' ) ";
            }
            if ($model->field["SUISEN"] == "on") {
                $query .= "     INNER JOIN NAME_MST E002 ";
                $query .= "            ON (    E002.NAMECD2 = T2.HOWTOEXAM1 ";
                $query .= "                 OR E002.NAMECD2 = T2.HOWTOEXAM2 ) ";
                $query .= "           AND E002.NAMECD1    = 'E002' ";
                $query .= "           AND E002.NAMESPARE3 = '2' ";
            }
        }
        $query .= "     LEFT JOIN QUESTIONNAIRE_MST L2 ";
        $query .= "              ON T2.QUESTIONNAIRECD = L2.QUESTIONNAIRECD ";
        $query .= "     LEFT JOIN NAME_MST S11 ";
        $query .= "              ON S11.NAMECD1      = 'E012' ";
        $query .= "             AND T2.SCHOOL_GROUP1 = S11.NAMECD2 ";
        $query .= "     LEFT JOIN FACULTY_GROUP_MST S12 ";
        $query .= "              ON T2.FACULTY_GROUP1 = S12.FACULTY_GROUP ";
        $query .= "     LEFT JOIN DEPARTMENT_GROUP_MST S13 ";
        $query .= "              ON T2.DEPARTMENT_GROUP1 = S13.DEPARTMENT_GROUP ";
        $query .= "     LEFT JOIN COLLEGE_MST S14 ";
        $query .= "              ON T2.SCHOOL_CD1   = S14.SCHOOL_CD ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST S15 ";
        $query .= "              ON T2.SCHOOL_CD1   = S15.SCHOOL_CD ";
        $query .= "             AND T2.FACULTYCD1   = S15.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST S16 ";
        $query .= "              ON T2.SCHOOL_CD1   = S16.SCHOOL_CD ";
        $query .= "             AND T2.FACULTYCD1   = S16.FACULTYCD ";
        $query .= "             AND T2.DEPARTMENTCD1 = S16.DEPARTMENTCD ";
        $query .= "     LEFT JOIN NAME_MST S17 ";
        $query .= "              ON S17.NAMECD1     = 'E002' ";
        $query .= "             AND T2.HOWTOEXAM1   = INT(S17.NAMECD2) ";
        $query .= "     LEFT JOIN NAME_MST S21 ";
        $query .= "              ON S21.NAMECD1      = 'E012' ";
        $query .= "             AND T2.SCHOOL_GROUP2 = S21.NAMECD2 ";
        $query .= "     LEFT JOIN FACULTY_GROUP_MST S22 ";
        $query .= "              ON T2.FACULTY_GROUP2 = S22.FACULTY_GROUP ";
        $query .= "     LEFT JOIN DEPARTMENT_GROUP_MST S23 ";
        $query .= "              ON T2.DEPARTMENT_GROUP2 = S23.DEPARTMENT_GROUP ";
        $query .= "     LEFT JOIN COLLEGE_MST S24 ";
        $query .= "              ON T2.SCHOOL_CD2   = S24.SCHOOL_CD ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST S25 ";
        $query .= "              ON T2.SCHOOL_CD2   = S25.SCHOOL_CD ";
        $query .= "             AND T2.FACULTYCD2   = S25.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST S26 ";
        $query .= "              ON T2.SCHOOL_CD2   = S26.SCHOOL_CD ";
        $query .= "             AND T2.FACULTYCD2   = S26.FACULTYCD ";
        $query .= "             AND T2.DEPARTMENTCD2 = S26.DEPARTMENTCD ";
        $query .= "     LEFT JOIN NAME_MST S27 ";
        $query .= "              ON S27.NAMECD1     = 'E002' ";
        $query .= "             AND T2.HOWTOEXAM2   = INT(S27.NAMECD2) ";
        $query .= "     LEFT JOIN JOBTYPE_L_MST S31 ";
        $query .= "              ON T2.JOBTYPE_LCD1 = S31.JOBTYPE_LCD ";
        $query .= "     LEFT JOIN JOBTYPE_M_MST S32 ";
        $query .= "              ON T2.JOBTYPE_LCD1 = S32.JOBTYPE_LCD ";
        $query .= "             AND T2.JOBTYPE_MCD1 = S32.JOBTYPE_MCD ";
        $query .= "     LEFT JOIN JOBTYPE_S_MST S33 ";
        $query .= "              ON T2.JOBTYPE_LCD1 = S33.JOBTYPE_LCD ";
        $query .= "             AND T2.JOBTYPE_MCD1 = S33.JOBTYPE_MCD ";
        $query .= "             AND T2.JOBTYPE_SCD1 = S33.JOBTYPE_SCD ";
        $query .= "     LEFT JOIN JOBTYPE_L_MST S41 ";
        $query .= "              ON T2.JOBTYPE_LCD2 = S41.JOBTYPE_LCD ";
        $query .= "     LEFT JOIN JOBTYPE_M_MST S42 ";
        $query .= "              ON T2.JOBTYPE_LCD2 = S42.JOBTYPE_LCD ";
        $query .= "             AND T2.JOBTYPE_MCD2 = S42.JOBTYPE_MCD ";
        $query .= "     LEFT JOIN JOBTYPE_S_MST S43 ";
        $query .= "              ON T2.JOBTYPE_LCD2 = S43.JOBTYPE_LCD ";
        $query .= "             AND T2.JOBTYPE_MCD2 = S43.JOBTYPE_MCD ";
        $query .= "             AND T2.JOBTYPE_SCD2 = S43.JOBTYPE_SCD ";
        $query .= "     LEFT JOIN AFT_TOTAL_STUDY_BATCH_DAT ATSBD ";
        $query .= "             ON ATSBD.SCHREGNO = T1.SCHREGNO ";
        $query .= "            AND ATSBD.YEAR     = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "     AND T2.COURSE_KIND      = '".$model->field["COURSE_KIND"]."' ";
        $query .= "     AND T2.QUESTIONNAIRECD  = '".$model->field["QUESTIONNAIRECD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.ENTRYDATE, ";
        $query .= "     T2.SEQ ";

        return $query;
    }
}
?>
