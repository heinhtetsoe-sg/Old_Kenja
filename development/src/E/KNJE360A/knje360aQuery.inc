<?php

require_once('for_php7.php');

class knje360aQuery extends Query {
    //学校一覧取得
    function getSchoolList($model) {
        if ($model->search_div == "1") {//学校名検索
            //学校名を全角スペースで分割
            $school_name = explode('　', $model->select["field"]["SEARCH_TXT"]);

            $query  = "";
            for ($i = 0; $i < get_count($school_name); $i++) {
                if ($school_name[$i] == "") {
                    continue;
                }
                if ($query != "") {
                    $query .= " UNION ";
                }
                $query .= " SELECT DISTINCT ";
                $query .= "     T2.SCHOOL_CD, ";
                $query .= "     T2.SCHOOL_NAME ";
                $query .= " FROM ";
                $query .= "     AFT_GRAD_COURSE_DAT T1, ";
                $query .= "     COLLEGE_MST T2 ";
                $query .= " WHERE ";
                $query .= "     T1.STAT_CD      = T2.SCHOOL_CD AND ";
                $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
                $query .= "    (T2.SCHOOL_NAME       LIKE '%".$school_name[$i]."%' OR ";
                $query .= "     T2.SCHOOL_NAME_SHOW1 LIKE '%".$school_name[$i]."%' OR ";
                $query .= "     T2.SCHOOL_NAME_SHOW2 LIKE '%".$school_name[$i]."%') ";
            }
        } else if ($model->search_div == "2") {//学校コード検索
            $query  = "";
            for ($i = 0; $i < get_count($model->search_no); $i++) {
                $school_cd = "";
                //学校コードを分解
                $school_cd    = sprintf("%08d", substr($model->search_no[$i], 0, 4));

                if ($model->search_no[$i] == "") {
                    continue;
                }
                if ($query != "") {
                    $query .= " UNION ";
                }
                $query .= " SELECT DISTINCT ";
                $query .= "     T2.SCHOOL_CD, ";
                $query .= "     T2.SCHOOL_NAME ";
                $query .= " FROM ";
                $query .= "     AFT_GRAD_COURSE_DAT T1, ";
                $query .= "     COLLEGE_MST T2 ";
                $query .= " WHERE ";
                $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
                $query .= "     AND T1.STAT_CD      = '".$school_cd."' ";
            }
        }
        if ($query != "") {
            $query .= " ORDER BY ";
            $query .= "     SCHOOL_CD ";
        }

        return $query;
    }

    //データ一覧取得
    function getDataList($model, $asc_or_desc) {
        $sort  = ($model->sort == "examno") ? "EXAMNO" : "GRADE || HR_CLASS || ATTENDNO";

        $query  = " WITH REGD_SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         '9999' AS GRD_YEAR, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         L2.HR_NAME, ";
        $query .= "         L1.NAME_SHOW ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST L1 ";
        $query .= "                  ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT L2 ";
        $query .= "                  ON T1.YEAR     = L2.YEAR ";
        $query .= "                 AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "                 AND T1.GRADE    = L2.GRADE ";
        $query .= "                 AND T1.HR_CLASS = L2.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ), GRD_SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.GRD_YEAR, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRD_GRADE AS GRADE, ";
        $query .= "         T1.GRD_HR_CLASS AS HR_CLASS, ";
        $query .= "         T1.GRD_ATTENDNO AS ATTENDNO, ";
        $query .= "         L1.HR_NAME, ";
        $query .= "         T1.NAME_SHOW ";
        $query .= "     FROM ";
        $query .= "        (SELECT ";
        $query .= "             SCHREGNO, ";
        $query .= "             FISCALYEAR(GRD_DATE) AS GRD_YEAR, ";
        $query .= "             GRD_SEMESTER, ";
        $query .= "             GRD_GRADE, ";
        $query .= "             GRD_HR_CLASS, ";
        $query .= "             GRD_ATTENDNO, ";
        $query .= "             NAME_SHOW ";
        $query .= "         FROM ";
        $query .= "             GRD_BASE_MST ) T1 ";
        $query .= "         LEFT JOIN GRD_REGD_HDAT L1 ";
        $query .= "                  ON T1.GRD_YEAR     = L1.YEAR ";
        $query .= "                 AND T1.GRD_SEMESTER = L1.SEMESTER ";
        $query .= "                 AND T1.GRD_GRADE    = L1.GRADE ";
        $query .= "                 AND T1.GRD_HR_CLASS = L1.HR_CLASS ";
        $query .= " ), SUBMAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SEQ, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.DECISION, ";
        $query .= "         T1.PLANSTAT, ";
        $query .= "         T2.SCHOOL_CD, ";
        $query .= "         T2.SCHOOL_NAME, ";
        $query .= "         T1.FACULTYCD, ";
        $query .= "         T3.FACULTYNAME, ";
        $query .= "         T1.DEPARTMENTCD, ";
        $query .= "         T4.DEPARTMENTNAME, ";
        $query .= "         D1.REMARK1 AS ADVERTISE_DIV, ";
        $query .= "         N1.NAME1 AS ADVERTISE_NAME, ";
        // 入試カレンダーの使用
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $query .= "         D1.REMARK2 AS PROGRAM_CD, ";
            $query .= "         C1.PROGRAM_NAME, ";
            $query .= "         D1.REMARK3 AS FORM_CD, ";
            $query .= "         C2.FORM_NAME, ";
            $query .= "         D1.REMARK4 AS L_CD, ";
            $query .= "         L2.L_NAME, ";
            $query .= "         D1.REMARK5 AS S_CD, ";
            $query .= "         C3.S_NAME, ";
        }
        $query .= "         T1.STAT_DATE3, ";
        $query .= "         D1.REMARK9 AS EXAMNO, ";
        $query .= "         T1.HOWTOEXAM ";
        $query .= "     FROM ";
        $query .= "         AFT_GRAD_COURSE_DAT T1 ";
        $query .= "         LEFT JOIN AFT_GRAD_COURSE_DETAIL_DAT D1 ON ";
        $query .= "                 T1.YEAR         = D1.YEAR AND ";
        $query .= "                 T1.SEQ          = D1.SEQ AND ";
        $query .= "                 D1.DETAIL_SEQ   = 1 ";
        $query .= "         LEFT JOIN COLLEGE_MST T2 ON ";
        $query .= "                 T1.STAT_CD      = T2.SCHOOL_CD ";
        $query .= "         LEFT JOIN COLLEGE_FACULTY_MST T3 ON ";
        $query .= "                 T1.STAT_CD      = T3.SCHOOL_CD AND ";
        $query .= "                 T1.FACULTYCD    = T3.FACULTYCD ";
        $query .= "         LEFT JOIN COLLEGE_DEPARTMENT_MST T4 ON ";
        $query .= "                 T1.STAT_CD      = T4.SCHOOL_CD AND ";
        $query .= "                 T1.FACULTYCD    = T4.FACULTYCD AND ";
        $query .= "                 T1.DEPARTMENTCD = T4.DEPARTMENTCD ";
        $query .= "         LEFT JOIN PREF_MST L1 ON ";
        $query .= "                 L1.PREF_CD      = T1.PREF_CD ";
        $query .= "         LEFT JOIN COLLEGE_EXAM_LDAT L2 ON ";
        $query .= "                 L2.L_CD         = D1.REMARK4 ";
        $query .= "         LEFT JOIN NAME_MST N1 ON ";
        $query .= "                 N1.NAMECD1      = 'E044' AND ";
        $query .= "                 N1.NAMECD2      = D1.REMARK1 ";
        // 入試カレンダーの使用
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $query .= "         LEFT JOIN COLLEGE_EXAM_CALENDAR C1 ON ";
            $query .= "                 C1.YEAR         = T1.YEAR AND ";
            $query .= "                 C1.SCHOOL_CD    = T1.STAT_CD AND ";
            $query .= "                 C1.FACULTYCD    = T1.FACULTYCD AND ";
            $query .= "                 C1.DEPARTMENTCD = T1.DEPARTMENTCD AND ";
            $query .= "                 C1.ADVERTISE_DIV = D1.REMARK1 AND ";
            $query .= "                 C1.PROGRAM_CD   = D1.REMARK2 ";
            $query .= "         LEFT JOIN COLLEGE_EXAM_CALENDAR C2 ON ";
            $query .= "                 C2.YEAR         = T1.YEAR AND ";
            $query .= "                 C2.SCHOOL_CD    = T1.STAT_CD AND ";
            $query .= "                 C2.FACULTYCD    = T1.FACULTYCD AND ";
            $query .= "                 C2.DEPARTMENTCD = T1.DEPARTMENTCD AND ";
            $query .= "                 C2.ADVERTISE_DIV = D1.REMARK1 AND ";
            $query .= "                 C2.PROGRAM_CD   = D1.REMARK2 AND ";
            $query .= "                 C2.FORM_CD      = D1.REMARK3 ";
            $query .= "         LEFT JOIN COLLEGE_EXAM_CALENDAR C3 ON ";
            $query .= "                 C3.YEAR         = T1.YEAR AND ";
            $query .= "                 C3.SCHOOL_CD    = T1.STAT_CD AND ";
            $query .= "                 C3.FACULTYCD    = T1.FACULTYCD AND ";
            $query .= "                 C3.DEPARTMENTCD = T1.DEPARTMENTCD AND ";
            $query .= "                 C3.ADVERTISE_DIV = D1.REMARK1 AND ";
            $query .= "                 C3.PROGRAM_CD   = D1.REMARK2 AND ";
            $query .= "                 C3.FORM_CD      = D1.REMARK3 AND ";
            $query .= "                 C3.L_CD1        = D1.REMARK4 AND ";
            $query .= "                 C3.S_CD         = D1.REMARK5 ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SENKOU_KIND  = '0' AND ";
        $query .= "         T1.STAT_CD IN ('".implode(explode(',', $model->schoolcd),"','")."') AND ";
        $query .= "         T1.SCHREGNO IS NOT NULL ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.*, ";
        $query .= "         CASE WHEN L1.SCHREGNO IS NOT NULL THEN L1.GRD_YEAR ELSE L2.GRD_YEAR END AS GRD_YEAR, ";
        $query .= "         CASE WHEN L1.SCHREGNO IS NOT NULL THEN L1.GRADE ELSE L2.GRADE END AS GRADE, ";
        $query .= "         CASE WHEN L1.SCHREGNO IS NOT NULL THEN L1.HR_CLASS ELSE L2.HR_CLASS END AS HR_CLASS, ";
        $query .= "         CASE WHEN L1.SCHREGNO IS NOT NULL THEN L1.ATTENDNO ELSE L2.ATTENDNO END AS ATTENDNO, ";
        $query .= "         CASE WHEN L1.SCHREGNO IS NOT NULL THEN L1.HR_NAME ELSE L2.HR_NAME END AS HR_NAME, ";
        $query .= "         CASE WHEN L1.SCHREGNO IS NOT NULL THEN L1.NAME_SHOW ELSE L2.NAME_SHOW END AS NAME_SHOW ";
        $query .= "     FROM ";
        $query .= "         SUBMAIN T1 ";
        $query .= "         LEFT JOIN REGD_SCHINFO L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "         LEFT JOIN GRD_SCHINFO L2 ON T1.SCHREGNO = L2.SCHREGNO ";
        if ($model->data_select == "1" || $model->data_select == "2") {
            $query .= "     WHERE ";
            if ($model->data_select == "1") {
                $query .= "         T1.DECISION IN ('1', '2') ";
            } else {
                $query .= "         T1.DECISION NOT IN ('1', '2') OR ";
                $query .= "         T1.DECISION IS NULL ";
            }
        }
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .=       $sort." AS VALUE ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= " ORDER BY ";
        $query .= "     SCHOOL_CD, ";
        $query .= "     FACULTYCD, ";
        $query .= "     DEPARTMENTCD, ";
        $query .= "     VALUE ".$asc_or_desc.", ";
        // 入試カレンダーの使用
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $query .= "     ADVERTISE_DIV, ";
            $query .= "     PROGRAM_CD, ";
            $query .= "     FORM_CD, ";
            $query .= "     S_CD, ";
        }
        $query .= "     GRD_YEAR DESC, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     SEQ ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験方式取得
    function getHowtoexamName($howtoexam) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E002' ";
        $query .= "     AND NAMECD2 = '".$howtoexam."' ";

        return $query;
    }

    //更新処理
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //AFT_GRAD_COURSE_DETAIL_DATのSEQ取得
        $query  = " SELECT ";
        $query .= "     SEQ ";
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     DETAIL_SEQ  = 1 ";
        $detail_seq = $db->getCol($query);

        $seq_array = preg_split("/,/", $model->seq_list);
        foreach ($seq_array as $seq) {
            //AFT_GRAD_COURSE_DAT
            $data = array();
            $data["DECISION"][TEXT]     = $fields["DECISION"][$seq];
            $data["PLANSTAT"][TEXT]     = $fields["PLANSTAT"][$seq];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' AND ";
            $where .= "     SEQ  = ".$seq." ";

            $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DAT", $where));

            //AFT_GRAD_COURSE_DETAIL_DAT
            $data = array();
            if (!in_array($seq, $detail_seq)) {
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SEQ"][NUMBER]            = $seq;
                $data["DETAIL_SEQ"][NUMBER]     = 1;
            }
            $data["REMARK9"][TEXT]          = $fields["EXAMNO"][$seq];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            if (in_array($seq, $detail_seq)) {
                $where  = " WHERE ";
                $where .= "     YEAR = '".CTRL_YEAR."' AND ";
                $where .= "     SEQ  = ".$seq." AND ";
                $where .= "     DETAIL_SEQ = 1 ";

                $db->query(Query::updateSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT", $where));
            } else {
                $db->query(Query::insertSQL($data, "AFT_GRAD_COURSE_DETAIL_DAT"));
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
