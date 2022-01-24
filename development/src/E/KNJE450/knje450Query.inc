<?php

require_once('for_php7.php');

class knje450Query extends Query {
    //データを取得（メイン画面）
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     S2.HR_NAME, ";
        $query .= "     INT(T1.ATTENDNO) AS ATTENDNO, ";
        $query .= "     S1.NAME, ";
        $query .= "     S1.NAME_KANA, ";
        $query .= "     S1.BIRTHDAY, ";
        $query .= "     S3.ZIPCD, ";
        $query .= "     S3.ADDR1, ";
        $query .= "     S3.ADDR2, ";
        $query .= "     S3.TELNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST S1 ON ";
        $query .= "             T1.SCHREGNO     = S1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT S2 ON ";
        $query .= "             T1.YEAR         = S2.YEAR AND ";
        $query .= "             T1.SEMESTER     = S2.SEMESTER AND ";
        $query .= "             T1.GRADE        = S2.GRADE AND ";
        $query .= "             T1.HR_CLASS     = S2.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_ADDRESS_DAT S3 ON ";
        $query .= "             T1.SCHREGNO     = S3.SCHREGNO AND ";
        $query .= "             S3.ISSUEDATE   <= '".CTRL_DATE."' AND ";
        $query .= "             S3.EXPIREDATE  >= '".CTRL_DATE."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //クラス名称取得
    function getHrName($model) {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        $query .= "     HR_CLASS    = '".$model->hr_class."' ";

        return $query;
    }

    //作成年月日一覧取得
    function getWritingDateList($model) {
        $query  = " SELECT ";
        $query .= "     WRITING_DATE ";
        $query .= " FROM ";
        $query .= "     ASSESSMENT_ANS_HDAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     WRITING_DATE DESC ";

        return $query;
    }

    //アセスメント解答データ取得
    function getAssessmentAnsDat($model, $assess_div) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ASSESSMENT_ANS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $query .= "     WRITING_DATE    = '".$model->writing_date."' AND ";
        $query .= "     ASSESS_DIV      = '".$assess_div."' ";

        return $query;
    }

    //アセスメント設問マスタ取得
    function getAssessmentQMst($assess_div) {
        $query  = " SELECT ";
        $query .= "     ASSESS_CD, ";
        $query .= "     QUESTION ";
        $query .= " FROM ";
        $query .= "     ASSESSMENT_Q_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     ASSESS_DIV  = '".$assess_div."' ";
        $query .= " ORDER BY ";
        $query .= "     int(ASSESS_CD) ";

        return $query;
    }

    //定型文取得
    function getAssessmentTempMst() {
        $query  = " SELECT ";
        $query .= "     DATA_DIV, ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     ASSESSMENT_TEMP_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '00' ";

        return $query;
    }

    //データ取得
    function getSubQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.HANDICAP, ";
        $query .= "     L1.DIAGNOSIS_DATE, ";
        $query .= "     L1.INSTITUTES_CD, ";
        $query .= "     L1.ATTENDING_DOCTOR, ";
        $query .= "     L1.REMARK, ";
        $query .= "     L1.MEDICINE_FLG, ";
        $query .= "     L1.MEDICINE_NAME, ";
        $query .= "     E1.EXAMINATION_DATE AS EXAMINATION_DATE_1, ";
        $query .= "     E1.INSTITUTES_CD AS EXAM_INST_CD_1, ";
        $query .= "     E1.TESTER_NAME AS TESTER_NAME_1, ";
        $query .= "     E1.REMARK1 AS REMARK1_1, ";
        $query .= "     E1.REMARK2 AS REMARK2_1, ";
        $query .= "     E1.REMARK3 AS REMARK3_1, ";
        $query .= "     E1.OTHER_TEXT AS OTHER_TEXT_1, ";
        $query .= "     E2.EXAMINATION_DATE AS EXAMINATION_DATE_2, ";
        $query .= "     E2.INSTITUTES_CD AS EXAM_INST_CD_2, ";
        $query .= "     E2.TESTER_NAME AS TESTER_NAME_2, ";
        $query .= "     E2.REMARK1 AS REMARK1_2, ";
        $query .= "     E2.REMARK2 AS REMARK2_2, ";
        $query .= "     E2.REMARK3 AS REMARK3_2, ";
        $query .= "     E2.REMARK4 AS REMARK4_2, ";
        $query .= "     E2.REMARK5 AS REMARK5_2, ";
        $query .= "     E2.REMARK6 AS REMARK6_2, ";
        $query .= "     E2.REMARK7 AS REMARK7_2, ";
        $query .= "     E2.OTHER_TEXT AS OTHER_TEXT_2, ";
        $query .= "     L2.P_S_YM, ";
        $query .= "     L2.P_E_YM, ";
        $query .= "     L2.P_PASSING_GRADE_FLG, ";
        $query .= "     L2.P_SUPPORT_FLG, ";
        $query .= "     L2.P_ETC_FLG, ";
        $query .= "     L2.P_ETC, ";
        $query .= "     L2.P_DATE_S_YM, ";
        $query .= "     L2.P_DATE_E_YM, ";
        $query .= "     L2.J_S_YM, ";
        $query .= "     L2.J_E_YM, ";
        $query .= "     L2.J_PASSING_GRADE_FLG, ";
        $query .= "     L2.J_SUPPORT_FLG, ";
        $query .= "     L2.J_ETC_FLG, ";
        $query .= "     L2.J_ETC, ";
        $query .= "     L2.J_DATE_S_YM, ";
        $query .= "     L2.J_DATE_E_YM, ";
        $query .= "     L2.EDUCATION_TEXT, ";
        $query .= "     C1.INSTITUTES_CD AS CONS_INST_CD_1, ";
        $query .= "     C1.CONSULT_DATE AS CONSULT_DATE_1, ";
        $query .= "     C1.CONSULT_TEXT AS CONSULT_TEXT_1, ";
        $query .= "     C2.INSTITUTES_CD AS CONS_INST_CD_2, ";
        $query .= "     C2.CONSULT_DATE AS CONSULT_DATE_2, ";
        $query .= "     C2.CONSULT_TEXT AS CONSULT_TEXT_2, ";
        $query .= "     C3.INSTITUTES_CD AS CONS_INST_CD_3, ";
        $query .= "     C3.CONSULT_DATE AS CONSULT_DATE_3, ";
        $query .= "     C3.CONSULT_TEXT AS CONSULT_TEXT_3 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN ASSESSMENT_ANS_INSTITUTES_DAT L1 ON T1.SCHREGNO = L1.SCHREGNO AND L1.WRITING_DATE = '".$model->writing_date."' ";
        $query .= "     LEFT JOIN ASSESSMENT_ANS_EXAMINATION_DAT E1 ON T1.SCHREGNO = E1.SCHREGNO AND E1.WRITING_DATE = '".$model->writing_date."' AND E1.EXAMINATION_CD = '1' ";
        $query .= "     LEFT JOIN ASSESSMENT_ANS_EXAMINATION_DAT E2 ON T1.SCHREGNO = E2.SCHREGNO AND E2.WRITING_DATE = '".$model->writing_date."' AND E2.EXAMINATION_CD = '2' ";
        $query .= "     LEFT JOIN ASSESSMENT_ANS_EDUCATION_DAT L2 ON T1.SCHREGNO = L2.SCHREGNO AND L2.WRITING_DATE = '".$model->writing_date."' ";
        $query .= "     LEFT JOIN ASSESSMENT_ANS_CONSULT_DAT C1 ON T1.SCHREGNO = C1.SCHREGNO AND C1.WRITING_DATE = '".$model->writing_date."' AND C1.CONSULT_CD = '1' ";
        $query .= "     LEFT JOIN ASSESSMENT_ANS_CONSULT_DAT C2 ON T1.SCHREGNO = C2.SCHREGNO AND C2.WRITING_DATE = '".$model->writing_date."' AND C2.CONSULT_CD = '2' ";
        $query .= "     LEFT JOIN ASSESSMENT_ANS_CONSULT_DAT C3 ON T1.SCHREGNO = C3.SCHREGNO AND C3.WRITING_DATE = '".$model->writing_date."' AND C3.CONSULT_CD = '3' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //機関取得
    function getCenter($table) {
        $query  = " SELECT ";
        $query .= "     CENTERCD AS VALUE, ";
        $query .= "     NAME AS LABEL ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック
    function checkExistsData($model, $writing_date, $table) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $query .= "     WRITING_DATE    = '".str_replace("/", "-", $writing_date)."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery1($db, $model, $fields, $div, $writing_date) {
        $db->autoCommit(false);

        //追加
        if ($div == "insert") {
            $data = array();
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["WRITING_DATE"][TEXT]     = $writing_date;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $db->query(Query::insertSQL($data, "ASSESSMENT_ANS_HDAT"));
        }

        //削除
        $query = knje450Query::getDeleteQuery($model, "ASSESSMENT_ANS_DAT", $writing_date);
        $db->query($query);

        //追加
        for ($i=1; $i <= 10; $i++) {
            if ($i == 7 || $i == 8) continue;
            $div = sprintf("%02d", $i);

            $data = array();
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["WRITING_DATE"][TEXT]     = $writing_date;
            $data["ASSESS_DIV"][TEXT]       = $div;
            $data["QUESTION1"][TEXT]        = $fields[$div."_QUESTION1"];
            $data["QUESTION2"][TEXT]        = $fields[$div."_QUESTION2"];
            $data["QUESTION3"][TEXT]        = $fields[$div."_QUESTION3"];
            $data["QUESTION4"][TEXT]        = $fields[$div."_QUESTION4"];
            $data["QUESTION5"][TEXT]        = $fields[$div."_QUESTION5"];
            $data["QUESTION6"][TEXT]        = $fields[$div."_QUESTION6"];
            $data["QUESTION7"][TEXT]        = $fields[$div."_QUESTION7"];
            $data["QUESTION8"][TEXT]        = $fields[$div."_QUESTION8"];
            $data["QUESTION9"][TEXT]        = $fields[$div."_QUESTION9"];
            $data["QUESTION10"][TEXT]       = $fields[$div."_QUESTION10"];
            $data["QUESTION11"][TEXT]       = $fields[$div."_QUESTION11"];
            $data["QUESTION12"][TEXT]       = $fields[$div."_QUESTION12"];
            $data["QUESTION13"][TEXT]       = $fields[$div."_QUESTION13"];
            $data["QUESTION14"][TEXT]       = $fields[$div."_QUESTION14"];
            $data["QUESTION15"][TEXT]       = $fields[$div."_QUESTION15"];
            $data["REMARK1"][TEXT]          = $fields[$div."_REMARK1"];
            $data["REMARK2"][TEXT]          = $fields[$div."_REMARK2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $db->query(Query::insertSQL($data, "ASSESSMENT_ANS_DAT"));
        }

        $db->commit();
        return;
    }

    //UPDATE
    function &getUpdateQuery2($db, $model, $fields, $div, $writing_date) {
        $db->autoCommit(false);

        //追加
        if ($div == "insert") {
            $data = array();
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["WRITING_DATE"][TEXT]     = $writing_date;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $db->query(Query::insertSQL($data, "ASSESSMENT_ANS_HDAT"));
        }

        //削除
        $query = knje450Query::getDeleteQuery($model, "ASSESSMENT_ANS_INSTITUTES_DAT", $writing_date);
        $db->query($query);

        //追加
        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["WRITING_DATE"][TEXT]         = $writing_date;
        $data["HANDICAP"][TEXT]             = $fields["HANDICAP"];
        $data["DIAGNOSIS_DATE"][TEXT]       = $fields["DIAGNOSIS_DATE"];
        $data["INSTITUTES_CD"][TEXT]        = $fields["INSTITUTES_CD"];
        $data["ATTENDING_DOCTOR"][TEXT]     = $fields["ATTENDING_DOCTOR"];
        $data["REMARK"][TEXT]               = $fields["REMARK"];
        $data["MEDICINE_FLG"][TEXT]         = $fields["MEDICINE_FLG"];
        $data["MEDICINE_NAME"][TEXT]        = $fields["MEDICINE_NAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $db->query(Query::insertSQL($data, "ASSESSMENT_ANS_INSTITUTES_DAT"));

        //削除
        $query = knje450Query::getDeleteQuery($model, "ASSESSMENT_ANS_EXAMINATION_DAT", $writing_date);
        $db->query($query);

        //追加
        for ($cd=1; $cd <= 2; $cd++) {
            $data = array();
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["WRITING_DATE"][TEXT]         = $writing_date;
            $data["EXAMINATION_CD"][TEXT]       = $cd;
            $data["EXAMINATION_DATE"][TEXT]     = $fields["EXAMINATION_DATE_".$cd];
            $data["INSTITUTES_CD"][TEXT]        = $fields["EXAM_INST_CD_".$cd];
            $data["TESTER_NAME"][TEXT]          = $fields["TESTER_NAME_".$cd];
            if ($cd == "1") {
                $data["REMARK1"][TEXT]              = $fields["REMARK1_".$cd];
                $data["REMARK2"][TEXT]              = $fields["REMARK2_".$cd];
            }
            $data["REMARK3"][TEXT]              = $fields["REMARK3_".$cd];
            if ($cd == "2") {
                $data["REMARK4"][TEXT]              = $fields["REMARK4_".$cd];
                $data["REMARK5"][TEXT]              = $fields["REMARK5_".$cd];
                $data["REMARK6"][TEXT]              = $fields["REMARK6_".$cd];
                $data["REMARK7"][TEXT]              =  $fields["REMARK7_".$cd];
            }
            $data["OTHER_TEXT"][TEXT]           = $fields["OTHER_TEXT_".$cd];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";
            $db->query(Query::insertSQL($data, "ASSESSMENT_ANS_EXAMINATION_DAT"));
        }

        //削除
        $query = knje450Query::getDeleteQuery($model, "ASSESSMENT_ANS_EDUCATION_DAT", $writing_date);
        $db->query($query);

        //追加
        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["WRITING_DATE"][TEXT]         = $writing_date;
        $data["P_S_YM"][TEXT]               = $fields["P_S_YM"];
        $data["P_E_YM"][TEXT]               = $fields["P_E_YM"];
        $data["P_PASSING_GRADE_FLG"][TEXT]  = $fields["P_PASSING_GRADE_FLG"];
        $data["P_SUPPORT_FLG"][TEXT]        = $fields["P_SUPPORT_FLG"];
        $data["P_ETC_FLG"][TEXT]            = $fields["P_ETC_FLG"];
        $data["P_ETC"][TEXT]                = $fields["P_ETC"];
        $data["P_DATE_S_YM"][TEXT]          = $fields["P_DATE_S_YM"];
        $data["P_DATE_E_YM"][TEXT]          = $fields["P_DATE_E_YM"];
        $data["J_S_YM"][TEXT]               = $fields["J_S_YM"];
        $data["J_E_YM"][TEXT]               = $fields["J_E_YM"];
        $data["J_PASSING_GRADE_FLG"][TEXT]  = $fields["J_PASSING_GRADE_FLG"];
        $data["J_SUPPORT_FLG"][TEXT]        = $fields["J_SUPPORT_FLG"];
        $data["J_ETC_FLG"][TEXT]            = $fields["J_ETC_FLG"];
        $data["J_ETC"][TEXT]                = $fields["J_ETC"];
        $data["J_DATE_S_YM"][TEXT]          = $fields["J_DATE_S_YM"];
        $data["J_DATE_E_YM"][TEXT]          = $fields["J_DATE_E_YM"];
        $data["EDUCATION_TEXT"][TEXT]       = $fields["EDUCATION_TEXT"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $db->query(Query::insertSQL($data, "ASSESSMENT_ANS_EDUCATION_DAT"));

        //削除
        $query = knje450Query::getDeleteQuery($model, "ASSESSMENT_ANS_CONSULT_DAT", $writing_date);
        $db->query($query);

        //追加
        for ($cd=1; $cd <= 3; $cd++) {
            $data = array();
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["WRITING_DATE"][TEXT]         = $writing_date;
            $data["CONSULT_CD"][TEXT]           = $cd;
            $data["INSTITUTES_CD"][TEXT]        = $fields["CONS_INST_CD_".$cd];
            $data["CONSULT_DATE"][TEXT]         = $fields["CONSULT_DATE_".$cd];
            $data["CONSULT_TEXT"][TEXT]         = $fields["CONSULT_TEXT_".$cd];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";
            $db->query(Query::insertSQL($data, "ASSESSMENT_ANS_CONSULT_DAT"));
        }

        $db->commit();
        return;
    }

    //DELETE
    function getDeleteQuery($model, $table, $writing_date) {
        $query  = " DELETE FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     SCHREGNO        = '".$model->schregno."' AND ";
        $query .= "     WRITING_DATE    = '".$writing_date."' ";

        return $query;
    }

    //障害名マスタ取得
    function getChallengedNameMst() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHALLENGED_NAME_MST ";
        $query .= " ORDER BY ";
        $query .= "     CHALLENGECD ";

        return $query;
    }
}
?>