<?php

require_once('for_php7.php');

class knjl410_1Query extends Query {

    //イベント参加者データ取得
    function getRecruitDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO  = '".$model->recruit_no."' ";

        return $query;
    }

    //来校者情報データ取得
    function getRecruitVisitDat($model, $dateDiv="") {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     C1.COURSE AS HOPE_COURSE, ";
        $query .= "     C1.EXAMCOURSE_NAME AS HOPE_COURSE_NAME, ";
        $query .= "     N1.NAME1 AS TESTDIV_NAME, ";
        $query .= "     N2.NAME2 AS JUDGE_KIND_NAME, ";
        $query .= "     N3.NAME1 AS SCHOOL_DIV_NAME, ";
        //2.RECRUIT_VISIT_SCORE_DAT
        for ($sem = 1; $sem <= 2; $sem++) {
            for ($s = 1; $s <= 9; $s++) {
                $sub = sprintf("%02d", $s);
                $query .= "     VS{$sem}.SUBCLASSCD{$sub} AS SUBCLASSCD{$sub}_S_{$sem}, ";
            }
            $query .= "     VS{$sem}.TOTAL3 AS TOTAL3_S_{$sem}, ";
            $query .= "     VS{$sem}.TOTAL5 AS TOTAL5_S_{$sem}, ";
            $query .= "     VS{$sem}.TOTAL9 AS TOTAL9_S_{$sem}, ";
            $query .= "     VS{$sem}.SELECT_DIV AS SELECT_DIV_S_{$sem}, ";
        }
        //3.RECRUIT_VISIT_MOCK_DAT
        for ($i = 4; $i <= 12; $i++) {
            $mon = sprintf("%02d", $i);
            for ($s = 1; $s <= 5; $s++) {
                $sub = sprintf("%02d", $s);
                $query .= "     VM{$mon}.SUBCLASSCD{$sub} AS SUBCLASSCD{$sub}_M_{$mon}, ";
            }
            $query .= "     VM{$mon}.AVG3 AS AVG3_M_{$mon}, ";
            $query .= "     VM{$mon}.AVG5 AS AVG5_M_{$mon}, ";
            $query .= "     VM{$mon}.COMPANYCD AS COMPANYCD_M_{$mon}, ";
            $query .= "     VM{$mon}.COMPANY_TEXT AS COMPANY_TEXT_M_{$mon}, ";
        }
        for ($i = 99; $i <= 99; $i++) {
            $mon = sprintf("%02d", $i);
            $query .= "     VM{$mon}.TOP1_AVG3 AS TOP1_AVG3_M_{$mon}, ";
            $query .= "     VM{$mon}.TOP1_AVG5 AS TOP1_AVG5_M_{$mon}, ";
            $query .= "     VM{$mon}.TOP1_COMPANYCD AS TOP1_COMPANYCD_M_{$mon}, ";
            $query .= "     VM{$mon}.TOP1_COMPANY_TEXT AS TOP1_COMPANY_TEXT_M_{$mon}, ";
            $query .= "     VM{$mon}.TOP2_AVG3 AS TOP2_AVG3_M_{$mon}, ";
            $query .= "     VM{$mon}.TOP2_AVG5 AS TOP2_AVG5_M_{$mon}, ";
            $query .= "     VM{$mon}.TOP2_COMPANYCD AS TOP2_COMPANYCD_M_{$mon}, ";
            $query .= "     VM{$mon}.TOP2_COMPANY_TEXT AS TOP2_COMPANY_TEXT_M_{$mon}, ";
            $query .= "     VM{$mon}.TOP_AVG AS TOP_AVG_M_{$mon}, ";
        }
        //4.RECRUIT_VISIT_ACTIVE_DAT
        foreach ($model->actArray as $actKey => $actRow) {
            $i = $actRow["VALUE"];
            $div = "1";
            $seq = sprintf("%03d", $i);
            $query .= "     VA{$div}{$seq}.REMARK1 AS REMARK1_A_{$div}_{$seq}, ";
        }
        for ($i = 1; $i <= 2; $i++) {
            $div = "2";
            $seq = sprintf("%03d", $i);
            $query .= "     VA{$div}{$seq}.REMARK1 AS REMARK1_A_{$div}_{$seq}, ";
        }
        $query .= "     S1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     RECRUIT_VISIT_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST S1 ON S1.STAFFCD = T1.STAFFCD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ON N1.YEAR      = T1.YEAR ";
        $query .= "                            AND N1.NAMECD1   = 'L407' ";
        $query .= "                            AND N1.NAMECD2   = T1.TESTDIV ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ON N2.YEAR      = T1.YEAR ";
        $query .= "                            AND N2.NAMECD1   = 'L025' ";
        $query .= "                            AND N2.NAMECD2   = T1.JUDGE_KIND ";
        $query .= "     LEFT JOIN V_NAME_MST N3 ON N3.YEAR      = T1.YEAR ";
        $query .= "                            AND N3.NAMECD1   = 'L015' ";
        $query .= "                            AND N3.NAMECD2   = T1.SCHOOL_DIV ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT DISTINCT ";
        $query .= "             COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD AS COURSE, ";
        $query .= "             EXAMCOURSE_NAME ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_COURSE_MST ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "             AND APPLICANTDIV = '2' "; //2:高校
        $query .= "     ) C1 ON C1.COURSE = T1.HOPE_COURSECD || '-' || T1.HOPE_MAJORCD || '-' || T1.HOPE_COURSECODE ";
        //2.RECRUIT_VISIT_SCORE_DAT
        for ($sem = 1; $sem <= 2; $sem++) {
            $query .= "     LEFT JOIN RECRUIT_VISIT_SCORE_DAT VS{$sem} ON VS{$sem}.YEAR = T1.YEAR ";
            $query .= "             AND VS{$sem}.RECRUIT_NO = T1.RECRUIT_NO ";
            $query .= "             AND VS{$sem}.SEMESTER = '{$sem}' ";
        }
        //3.RECRUIT_VISIT_MOCK_DAT
        for ($i = 4; $i <= 12; $i++) {
            $mon = sprintf("%02d", $i);
            $query .= "     LEFT JOIN RECRUIT_VISIT_MOCK_DAT VM{$mon} ON VM{$mon}.YEAR = T1.YEAR ";
            $query .= "             AND VM{$mon}.RECRUIT_NO = T1.RECRUIT_NO ";
            $query .= "             AND VM{$mon}.MONTH = '{$mon}' ";
        }
        for ($i = 99; $i <= 99; $i++) {
            $mon = sprintf("%02d", $i);
            $query .= "     LEFT JOIN RECRUIT_VISIT_MOCK_DAT VM{$mon} ON VM{$mon}.YEAR = T1.YEAR ";
            $query .= "             AND VM{$mon}.RECRUIT_NO = T1.RECRUIT_NO ";
            $query .= "             AND VM{$mon}.MONTH = '{$mon}' ";
        }
        //4.RECRUIT_VISIT_ACTIVE_DAT
        foreach ($model->actArray as $actKey => $actRow) {
            $i = $actRow["VALUE"];
            $div = "1";
            $seq = sprintf("%03d", $i);
            $query .= "     LEFT JOIN RECRUIT_VISIT_ACTIVE_DAT VA{$div}{$seq} ON VA{$div}{$seq}.YEAR = T1.YEAR ";
            $query .= "             AND VA{$div}{$seq}.RECRUIT_NO = T1.RECRUIT_NO ";
            $query .= "             AND VA{$div}{$seq}.SEQ_DIV = '{$div}' ";
            $query .= "             AND VA{$div}{$seq}.SEQ = '{$seq}' ";
        }
        for ($i = 1; $i <= 2; $i++) {
            $div = "2";
            $seq = sprintf("%03d", $i);
            $query .= "     LEFT JOIN RECRUIT_VISIT_ACTIVE_DAT VA{$div}{$seq} ON VA{$div}{$seq}.YEAR = T1.YEAR ";
            $query .= "             AND VA{$div}{$seq}.RECRUIT_NO = T1.RECRUIT_NO ";
            $query .= "             AND VA{$div}{$seq}.SEQ_DIV = '{$div}' ";
            $query .= "             AND VA{$div}{$seq}.SEQ = '{$seq}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".(CTRL_YEAR + 1)."' ";
        $query .= " AND T1.RECRUIT_NO   = '".$model->recruit_no."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //希望コース取得
    function getHopeCourse() {
        $query  = " SELECT DISTINCT ";
        $query .= "     COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD AS VALUE, ";
        $query .= "     EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND APPLICANTDIV = '2' "; //2:高校
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験種別取得
    function getTestdiv() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND NAMECD1 = 'L407' "; //1:A推薦・推薦 2:B推薦 3:併願優遇 4:一般
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //担当取得
    function getStaffMst($model) {
        $query  = " SELECT ";
        $query .= "     S1.STAFFCD AS VALUE, ";
        $query .= "     S1.STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST S1 ";
        $query .= "     INNER JOIN USERGROUP_DAT U1 ";
        $query .= "          ON U1.YEAR = S1.YEAR ";
        $query .= "         AND U1.STAFFCD = S1.STAFFCD ";
        $query .= "         AND U1.GROUPCD = '0010' ";  //0010:進路指導
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND U1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND U1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     S1.YEAR = '".(CTRL_YEAR)."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //特待生取得
    function getJudgeKind() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME2 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND NAMECD1 = 'L025' ";
        $query .= "     AND NAME2 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科・科目名取得
    function getSubclassName($sub) {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 AS FLG5, ";
        $query .= "     NAMESPARE3 AS FLG3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND NAMECD1 = 'L008' ";
        $query .= "     AND NAMECD2 = '{$sub}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //教科・科目名取得
    function getNameMstL008() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1 AS FLG5, ";
        $query .= "     NAMESPARE3 AS FLG3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND NAMECD1 = 'L008' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試名コンボ取得
    function getNameMstL406() {
        $query  = " SELECT ";
        $query .= "     '0000' || NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND NAMECD1 = 'L406' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //諸活動取得
    function getNameMstL408() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMESPARE1 AS POINT ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND NAMECD1 = 'L408' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //存在チェック
    function getRow_check($model) {

        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECRUIT_VISIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' ";

        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }

    // UPDATE---RECRUIT_VISIT_DAT
    function &getUpdateQuery($model, $fields) {

        $db = Query::dbCheckOut();

        //1.RECRUIT_VISIT_DAT
        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_VISIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' ";
        $db->query($query);
        //INSERT
        $data = array();
        $data["YEAR"][TEXT]             = (CTRL_YEAR + 1);
        $data["RECRUIT_NO"][TEXT]       = $model->recruit_no;
        $data["TOUROKU_DATE"][TEXT]     = str_replace("/", "-", $fields["TOUROKU_DATE"]);
        $data["KAKUTEI_DATE"][TEXT]     = str_replace("/", "-", $fields["KAKUTEI_DATE"]);
        if ($fields["HOPE_COURSE"]) {
            list($hope_coursecd, $hope_majorcd, $hope_coursecode) = explode('-', $fields["HOPE_COURSE"]);
        } else {
            $hope_coursecd = $hope_majorcd = $hope_coursecode = "";
        }
        $data["HOPE_COURSECD"][TEXT]    = $hope_coursecd;
        $data["HOPE_MAJORCD"][TEXT]     = $hope_majorcd;
        $data["HOPE_COURSECODE"][TEXT]  = $hope_coursecode;
        $data["TESTDIV"][TEXT]          = $fields["TESTDIV"];
        $data["STAFFCD"][TEXT]          = $fields["STAFFCD"];
        $data["JUDGE_KIND"][TEXT]       = $fields["JUDGE_KIND"];
        $data["SCHOOL_DIV"][TEXT]       = $fields["SCHOOL_DIV"];
        $data["SCHOOL_NAME"][TEXT]      = $fields["SCHOOL_NAME"];
        $data["SCORE_CHK"][TEXT]        = $fields["SCORE_CHK"];
        $data["MOCK_CHK"][TEXT]         = $fields["MOCK_CHK"];
        $data["REMARK1"][TEXT]          = $fields["REMARK1"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $db->query(Query::insertSQL($data, "RECRUIT_VISIT_DAT"));

        //2.RECRUIT_VISIT_SCORE_DAT
        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_VISIT_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' ";
        $db->query($query);
        //INSERT(SEMESTER='1'-'2')
        for ($sem = 1; $sem <= 2; $sem++) {
            $data = array();
            $data["YEAR"][TEXT]             = (CTRL_YEAR + 1);
            $data["RECRUIT_NO"][TEXT]       = $model->recruit_no;
            $data["SEMESTER"][TEXT]         = $sem;
            for ($s = 1; $s <= 9; $s++) {
                $sub = sprintf("%02d", $s);
                $data["SUBCLASSCD{$sub}"][NUMBER]   = $fields["SUBCLASSCD{$sub}"."_S_{$sem}"];
            }
            $data["TOTAL3"][NUMBER]         = $fields["TOTAL3"."_S_{$sem}"];
            $data["TOTAL5"][NUMBER]         = $fields["TOTAL5"."_S_{$sem}"];
            $data["TOTAL9"][NUMBER]         = $fields["TOTAL9"."_S_{$sem}"];
            $data["SELECT_DIV"][TEXT]       = $fields["SELECT_DIV"."_S_{$sem}"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            $db->query(Query::insertSQL($data, "RECRUIT_VISIT_SCORE_DAT"));
        }

        //3.RECRUIT_VISIT_MOCK_DAT
        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_VISIT_MOCK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' ";
        $db->query($query);
        //INSERT(MONTH='04'-'12')
        for ($i = 4; $i <= 12; $i++) {
            $mon = sprintf("%02d", $i);
            $data = array();
            $data["YEAR"][TEXT]             = (CTRL_YEAR + 1);
            $data["RECRUIT_NO"][TEXT]       = $model->recruit_no;
            $data["MONTH"][TEXT]            = $mon;
            for ($s = 1; $s <= 5; $s++) {
                $sub = sprintf("%02d", $s);
                $data["SUBCLASSCD{$sub}"][NUMBER]   = $fields["SUBCLASSCD{$sub}"."_M_{$mon}"];
            }
            $data["AVG3"][NUMBER]           = $fields["AVG3"."_M_{$mon}"];
            $data["AVG5"][NUMBER]           = $fields["AVG5"."_M_{$mon}"];
            $data["COMPANYCD"][TEXT]        = $fields["COMPANYCD"."_M_{$mon}"]; //頭4桁ゼロ埋め
            $data["COMPANY_TEXT"][TEXT]     = $fields["COMPANY_TEXT"."_M_{$mon}"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            $db->query(Query::insertSQL($data, "RECRUIT_VISIT_MOCK_DAT"));
        }
        //INSERT(MONTH='99')
        for ($i = 99; $i <= 99; $i++) {
            $mon = sprintf("%02d", $i);
            $data = array();
            $data["YEAR"][TEXT]             = (CTRL_YEAR + 1);
            $data["RECRUIT_NO"][TEXT]       = $model->recruit_no;
            $data["MONTH"][TEXT]            = $mon;
            //TOP1
            $data["TOP1_AVG3"][NUMBER]      = $fields["TOP1_AVG3"."_M_{$mon}"];
            $data["TOP1_AVG5"][NUMBER]      = $fields["TOP1_AVG5"."_M_{$mon}"];
            $data["TOP1_COMPANYCD"][TEXT]   = $fields["TOP1_COMPANYCD"."_M_{$mon}"]; //頭4桁ゼロ埋め
            $data["TOP1_COMPANY_TEXT"][TEXT]= $fields["TOP1_COMPANY_TEXT"."_M_{$mon}"];
            //TOP2
            $data["TOP2_AVG3"][NUMBER]      = $fields["TOP2_AVG3"."_M_{$mon}"];
            $data["TOP2_AVG5"][NUMBER]      = $fields["TOP2_AVG5"."_M_{$mon}"];
            $data["TOP2_COMPANYCD"][TEXT]   = $fields["TOP2_COMPANYCD"."_M_{$mon}"]; //頭4桁ゼロ埋め
            $data["TOP2_COMPANY_TEXT"][TEXT]= $fields["TOP2_COMPANY_TEXT"."_M_{$mon}"];
            //偏差値平均
            $data["TOP_AVG"][NUMBER]        = $fields["TOP_AVG"."_M_{$mon}"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            $db->query(Query::insertSQL($data, "RECRUIT_VISIT_MOCK_DAT"));
        }

        //4.RECRUIT_VISIT_ACTIVE_DAT
        //DELETE
        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_VISIT_ACTIVE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' ";
        $db->query($query);
        //INSERT(SEQ_DIV='1')
        foreach ($model->actArray as $actKey => $actRow) {
            $i = $actRow["VALUE"];
            $div = "1";
            $seq = sprintf("%03d", $i);
            $data = array();
            $data["YEAR"][TEXT]             = (CTRL_YEAR + 1);
            $data["RECRUIT_NO"][TEXT]       = $model->recruit_no;
            $data["SEQ_DIV"][TEXT]          = $div;
            $data["SEQ"][TEXT]              = $seq;
            $data["REMARK1"][TEXT]          = $fields["REMARK1"."_A_{$div}_{$seq}"]; //チェック
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            $db->query(Query::insertSQL($data, "RECRUIT_VISIT_ACTIVE_DAT"));
        }
        //INSERT(SEQ_DIV='2')
        for ($i = 1; $i <= 2; $i++) {
            $div = "2";
            $seq = sprintf("%03d", $i);
            $data = array();
            $data["YEAR"][TEXT]             = (CTRL_YEAR + 1);
            $data["RECRUIT_NO"][TEXT]       = $model->recruit_no;
            $data["SEQ_DIV"][TEXT]          = $div;
            $data["SEQ"][TEXT]              = $seq;
            $data["REMARK1"][TEXT]          = $fields["REMARK1"."_A_{$div}_{$seq}"]; //001:中学時代の部活動 002:学校外での諸活動
            $data["REMARK2"][TEXT]          = ""; //未使用
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";
            $db->query(Query::insertSQL($data, "RECRUIT_VISIT_ACTIVE_DAT"));
        }

        Query::dbCheckIn($db);

        return;
    }

    // DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        //1.RECRUIT_VISIT_DAT
        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_VISIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' ";
        $db->query($query);

        //2.RECRUIT_VISIT_SCORE_DAT
        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_VISIT_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' ";
        $db->query($query);

        //3.RECRUIT_VISIT_MOCK_DAT
        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_VISIT_MOCK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' ";
        $db->query($query);

        //4.RECRUIT_VISIT_ACTIVE_DAT
        $query  = " DELETE FROM ";
        $query .= "     RECRUIT_VISIT_ACTIVE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR            = '".(CTRL_YEAR + 1)."' AND ";
        $query .= "     RECRUIT_NO      = '".$model->recruit_no."' ";
        $db->query($query);

        Query::dbCheckIn($db);
        return ;
    }
}
?>
