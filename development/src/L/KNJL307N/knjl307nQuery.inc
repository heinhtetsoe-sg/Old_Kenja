<?php

require_once('for_php7.php');

class knjl307nquery extends Query
{
    //入試制度
    public function getApplicantdiv($model, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->test_year}' AND ";
        $query .= "     NAMECD1 = 'L003' ";
        if ($flg) {
            $query .= " AND NAMECD2 = '{$model->field["APPLICANTDIV"]}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     NAMESPARE2, ";
            $query .= "     NAMECD2 ";
        }
        return $query;
    }

    //入試区分取得
    public function getTestDiv($model, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAME1 AS LABEL, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->test_year}' AND ";
        $query .= "     NAMECD1 = 'L004' ";
        if ($flg) {
            $query .= " AND NAMECD2 = '{$model->field["TESTDIV"]}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //志望区分取得
    public function getEntExamCourse($model, $flg = "")
    {
        $query .= " SELECT ";
        if ($flg) {
            $query .= "     EXAMCOURSE_NAME ";
        } else {
            $query .= "     COURSECD || MAJORCD || EXAMCOURSECD AS VALUE, ";
            $query .= "     EXAMCOURSECD || ':' || EXAMCOURSE_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' ";
        $query .= " AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        if ($flg) {
            $query .= " AND COURSECD || MAJORCD || EXAMCOURSECD = '{$model->field["TOTALCD"]}' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     COURSECD, ";
            $query .= "     MAJORCD, ";
            $query .= "     EXAMCOURSECD ";
        }

        return $query;
    }

    //願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjl307nQuery::getCalendarno($model->test_year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }

        $result->free();

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //誕生日用
            $birth_eracd = $birth_y = $birth_m = $birth_d = "";
            if (strlen($data_arr[$i]["BIRTHDAY"])) {
                list($s_year, $birth_m, $birth_d) = explode("/", $data_arr[$i]["BIRTHDAY"]);
                for ($ere_no=0; $ere_no<get_count($eracd); $ere_no++) {
                    if ($defsdate[$ere_no] <= $data_arr[$i]["BIRTHDAY"] && $data_arr[$i]["BIRTHDAY"] <= $defedate[$ere_no]) {
                        $birth_eracd = $eracd[$ere_no];
                        $birth_y = ((int)$s_year + 1) - ((int)$defyear[$ere_no]);
                        $birth_y = sprintf("%02d", $birth_y);
                        break;
                    }
                }
            } else {
                $birth_eracd = "";
                $birth_y = "";
                $birth_m = "";
                $birth_d = "";
            }

            $base_cnt = $db->getOne(knjl307nQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt = $db->getOne(knjl307nQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $baseDetail_009_cnt = $db->getOne(knjl307nQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "9"));
            $baseDetail_001_cnt = $db->getOne(knjl307nQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "1"));
            $baseDetail_017_cnt = $db->getOne(knjl307nQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "17"));

            $finschool = $db->getRow(knjl307nQuery::getFinschoolMst($data_arr[$i]["FS_CD"]), DB_FETCHMODE_ASSOC);

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->test_year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
            $data["SHDIV"][TEXT]            = $data_arr[$i]["SHDIV"];
            $data["DESIREDIV"][TEXT]        = '1';
            $data["RECEPTDATE"][TEXT]       = str_replace("/", "-", $data_arr[$i]["RECEPTDATE"]);
            $data["NAME"][TEXT]             = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]        = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]              = $data_arr[$i]["SEX"];
            $data["ERACD"][TEXT]            = $birth_eracd;
            $data["BIRTH_Y"][TEXT]          = $birth_y;
            $data["BIRTH_M"][TEXT]          = $birth_m;
            $data["BIRTH_D"][TEXT]          = $birth_d;
            $data["BIRTHDAY"][TEXT]         = str_replace("/", "-", $data_arr[$i]["BIRTHDAY"]);
            $data["FS_CD"][TEXT]            = $data_arr[$i]["FS_CD"];
            $data["FS_DAY"][TEXT]           = str_replace("/", "-", $data_arr[$i]["FS_DAY"]);
            $data["FS_GRDDIV"][TEXT]        = $data_arr[$i]["FS_GRDDIV"];

            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->test_year}' AND";
            $where .= "       EXAMNO       = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $base_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);
            
            //ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->test_year;
            $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
            $data["ZIPCD"][TEXT]         = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]      = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]      = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]         = $data_arr[$i]["TELNO"];
            $data["GKANA"][TEXT]         = $data_arr[$i]["GKANA"];
            $data["GNAME"][TEXT]         = $data_arr[$i]["GNAME"];
            $data["GZIPCD"][TEXT]        = $data_arr[$i]["GZIPCD"];
            $data["GADDRESS1"][TEXT]     = $data_arr[$i]["GADDRESS1"];
            $data["GADDRESS2"][TEXT]     = $data_arr[$i]["GADDRESS2"];
            $data["GTELNO"][TEXT]        = $data_arr[$i]["GTELNO"];

            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->test_year}' AND";
            $where .= "       EXAMNO       = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=001
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->test_year;
            $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]           = "001";
            $data["REMARK8"][TEXT]       = substr($model->field["TOTALCD"], 0, 1);
            $data["REMARK9"][TEXT]       = substr($model->field["TOTALCD"], 1, 3);
            $data["REMARK10"][TEXT]      = substr($model->field["TOTALCD"], 4, 7);

            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->test_year}' AND";
            $where .= "       EXAMNO       = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ       =    '001' ";

            if (0 == $baseDetail_001_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=017
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->test_year;
            $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]           = "017";
            $data["REMARK1"][TEXT]       = substr($model->field["TOTALCD"], 0, 1);
            $data["REMARK2"][TEXT]       = substr($model->field["TOTALCD"], 1, 3);
            $data["REMARK3"][TEXT]       = $data_arr[$i]["EXAMCOURSECD2"];

            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->test_year}' AND";
            $where .= "       EXAMNO       = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ          = '017' ";

            if (0 == $baseDetail_017_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=009
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->test_year;
            $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]           = "009";
            $data["REMARK7"][TEXT]       = $data_arr[$i]["REMARK7"];
            $data["REMARK1"][TEXT]       = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]       = $data_arr[$i]["REMARK2"];
            $data["REMARK3"][TEXT]       = $data_arr[$i]["REMARK3"];
            $data["REMARK4"][TEXT]       = $data_arr[$i]["REMARK4"];
            $data["REMARK8"][TEXT]       = $data_arr[$i]["REMARK8"];
            $data["REMARK5"][TEXT]       = $data_arr[$i]["REMARK5"];
            $data["REMARK6"][TEXT]       = $data_arr[$i]["REMARK6"];
            //内諾
            $data["REMARK9"][TEXT]       = $data_arr[$i]["REMARK9"];

            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->test_year}' AND";
            $where .= "       EXAMNO       = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ       =    '009' ";

            if (0 == $baseDetail_009_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }
    //調査書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv2($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $confRpt_cnt = $db->getOne(knjl307nQuery::cntEntexamApplicantconfrptDat($model, $data_arr[$i]));
            $baseDetail_004_cnt = $db->getOne(knjl307nQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "4"));

            //ENTEXAM_APPLICANTCONFRPT_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->test_year;
            $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
            $data["CONFIDENTIAL_RPT01"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT01"];
            $data["CONFIDENTIAL_RPT02"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT02"];
            $data["CONFIDENTIAL_RPT03"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT03"];
            $data["CONFIDENTIAL_RPT04"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT04"];
            $data["CONFIDENTIAL_RPT05"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT05"];
            $data["CONFIDENTIAL_RPT06"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT06"];
            $data["CONFIDENTIAL_RPT07"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT07"];
            $data["CONFIDENTIAL_RPT08"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT08"];
            $data["CONFIDENTIAL_RPT09"][NUMBER]   = $data_arr[$i]["CONFIDENTIAL_RPT09"];
            $data["TOTAL_ALL"][NUMBER]   = $data_arr[$i]["TOTAL_ALL"];
            $data["TOTAL5"][NUMBER]      = $data_arr[$i]["TOTAL5"];
            $data["KASANTEN_ALL"][NUMBER]= $data_arr[$i]["KASANTEN_ALL"];
            $data["ABSENCE_DAYS"][NUMBER]         = $data_arr[$i]["ABSENCE_DAYS"];
            $data["ABSENCE_DAYS2"][NUMBER]        = $data_arr[$i]["ABSENCE_DAYS2"];
            $data["ABSENCE_DAYS3"][NUMBER]        = $data_arr[$i]["ABSENCE_DAYS3"];
            $data["ABSENCE_REMARK"][TEXT]         = $data_arr[$i]["ABSENCE_REMARK"];
            $data["ABSENCE_REMARK2"][TEXT]        = $data_arr[$i]["ABSENCE_REMARK2"];
            $data["ABSENCE_REMARK3"][TEXT]        = $data_arr[$i]["ABSENCE_REMARK3"];
            $data["REMARK1"][TEXT]                = $data_arr[$i]["TOKU_REMARK1"];

            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->test_year}' AND";
            $where .= "       EXAMNO       = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $confRpt_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=004
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]   = $model->test_year;
            $data["EXAMNO"][TEXT]        = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]           = "004";
            $data["REMARK1"][TEXT]       = $data_arr[$i]["REMARK1_004"];

            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][NUMBER]     = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '{$model->test_year}' AND";
            $where .= "       EXAMNO       = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ       =    '004' ";

            if (0 == $baseDetail_004_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = "DELETE FROM W_CSVMSG_DAT ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "w_csvmsg_dat");

        $result = $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = "SELECT * FROM W_CSVMSG_DAT";
        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        if ($model->field["DATADIV"] == "1") {
            //願書
            $query .= "     T1.RECEPTDATE, ";
            $query .= "     T2.REMARK9, ";
            $query .= "     T1.EXAMNO, ";
            $query .= "     T1.NAME, ";
            $query .= "     T1.NAME_KANA, ";
            $query .= "     T1.SHDIV, ";
            $query .= "     T1.SEX, ";
            $query .= "     T1.BIRTHDAY, ";
            $query .= "     T4.ZIPCD, ";
            $query .= "     T4.ADDRESS1, ";
            $query .= "     T4.ADDRESS2, ";
            $query .= "     T4.TELNO, ";
            $query .= "     T1.FS_CD, ";
            $query .= "     L4.FINSCHOOL_NAME, ";
            $query .= "     T1.FS_DAY, ";
            $query .= "     T1.FS_GRDDIV, ";
            $query .= "     T4.GNAME, ";
            $query .= "     T4.GKANA, ";
            $query .= "     T4.GZIPCD, ";
            $query .= "     T4.GADDRESS1, ";
            $query .= "     T4.GADDRESS2, ";
            $query .= "     T4.GTELNO, ";
            $query .= "     T6.REMARK3 AS EXAMCOURSECD2, ";
            $query .= "     M3.EXAMCOURSE_NAME AS EXAMCOURSE_NAME2, ";
            $query .= "     T2.REMARK7, ";
            $query .= "     T2.REMARK1, ";
            $query .= "     T2.REMARK2, ";
            $query .= "     T2.REMARK3, ";
            $query .= "     T2.REMARK4, ";
            $query .= "     T2.REMARK8, ";
            $query .= "     T2.REMARK5, ";
            $query .= "     T2.REMARK6 ";
        } else {
            //調査書
            $query .= "     T1.EXAMNO, ";
            $query .= "     T3.CONFIDENTIAL_RPT01, ";
            $query .= "     T3.CONFIDENTIAL_RPT02, ";
            $query .= "     T3.CONFIDENTIAL_RPT03, ";
            $query .= "     T3.CONFIDENTIAL_RPT04, ";
            $query .= "     T3.CONFIDENTIAL_RPT05, ";
            $query .= "     T3.CONFIDENTIAL_RPT06, ";
            $query .= "     T3.CONFIDENTIAL_RPT07, ";
            $query .= "     T3.CONFIDENTIAL_RPT08, ";
            $query .= "     T3.CONFIDENTIAL_RPT09, ";
            $query .= "     T3.TOTAL_ALL, ";
            $query .= "     T3.TOTAL5, ";
            $query .= "     T3.KASANTEN_ALL, ";
            $query .= "     T3.ABSENCE_DAYS, ";
            $query .= "     T3.ABSENCE_DAYS2, ";
            $query .= "     T3.ABSENCE_DAYS3, ";
            $query .= "     T3.ABSENCE_REMARK, ";
            $query .= "     T3.ABSENCE_REMARK2, ";
            $query .= "     T3.ABSENCE_REMARK3, ";
            $query .= "     T5.REMARK1 AS REMARK1_004, ";
            $query .= "     T3.REMARK1 AS TOKU_REMARK1";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "                                               AND T1.EXAMNO      = L1.EXAMNO ";
        $query .= "                                               AND L1.SEQ         = '001' ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "                                              AND T1.EXAMNO = T2.EXAMNO ";
        $query .= "                                              AND T2.SEQ = '009' ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T3 ON T1.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "                                          AND T1.EXAMNO = T3.EXAMNO ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T4 ON T1.ENTEXAMYEAR = T4.ENTEXAMYEAR ";
        $query .= "                                       AND T1.EXAMNO = T4.EXAMNO ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T5 ON T1.ENTEXAMYEAR = T5.ENTEXAMYEAR ";
        $query .= "                                              AND T1.EXAMNO = T5.EXAMNO ";
        $query .= "                                              AND T5.SEQ = '004' ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T6 ON T1.ENTEXAMYEAR = T6.ENTEXAMYEAR ";
        $query .= "                                              AND T1.EXAMNO = T6.EXAMNO ";
        $query .= "                                              AND T6.SEQ = '017' ";
        $query .= " LEFT JOIN ENTEXAM_COURSE_MST M3 ON T1.ENTEXAMYEAR  = M3.ENTEXAMYEAR";
        $query .= "                                AND T1.APPLICANTDIV = M3.APPLICANTDIV ";
        $query .= "                                AND T1.TESTDIV      = M3.TESTDIV ";
        $query .= "                                AND T6.REMARK1      = M3.COURSECD ";
        $query .= "                                AND T6.REMARK2      = M3.MAJORCD ";
        $query .= "                                AND T6.REMARK3      = M3.EXAMCOURSECD ";
        $query .= " LEFT JOIN FINSCHOOL_MST L4 ON T1.FS_CD = L4.FINSCHOOLCD";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->test_year}' ";
        $query .= " AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        if ($model->field["TOTALCD"] != "9999") {
            $query .= " AND L1.REMARK8 || L1.REMARK9 || L1.REMARK10 = '{$model->field["TOTALCD"]}' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["TOTALCD"] == "9999") {
            $query .= "     L1.REMARK8 || L1.REMARK9 || L1.REMARK10, ";
        }
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //受験科目
    public function getTestSubclasscd($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1   AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->test_year}' AND ";
        $query .= "     NAMECD1     = 'L009' AND ";
        $query .= "     NAME1 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSVデータ出力(調査書＋願書)
    public function getCsvData3($model)
    {
        $query  = " SELECT ";
        //願書
        $query .= "     L1.REMARK10 AS TOTALCD, ";
        $query .= "     M1.EXAMCOURSE_NAME AS TOTALCD_NAME, ";
        $query .= "     T1.SUC_COURSECODE || M2.EXAMCOURSE_NAME AS SUC_COURSECODE, ";
        $query .= "     CASE ";
        $query .= "         WHEN T1.JUDGEMENT = '3' THEN N1.NAME1 ";
        $query .= "         ELSE '' ";
        $query .= "     END AS JUDGEMENT_3, ";
        $query .= "     T1.JUDGEMENT || '　' || N1.NAME1 AS JUDGEMENT, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SHDIV, ";
        $query .= "     N2.NAME1 AS SHDIV_NAME, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T2.REMARK9, ";
        $query .= "     N5.NAME1 AS REMARK9_NAME, ";
        $query .= "     SUBSTR(CAST(T1.BIRTHDAY AS VARCHAR(10)), 1, 4) AS BIRTH_WAREKI, ";
        $query .= "     SUBSTR(CAST(T1.BIRTHDAY AS VARCHAR(10)), 1, 4) AS BIRTH_YEAR, ";
        $query .= "     SUBSTR(CAST(T1.BIRTHDAY AS VARCHAR(10)), 6, 2) AS BIRTH_MANTH, ";
        $query .= "     SUBSTR(CAST(T1.BIRTHDAY AS VARCHAR(10)), 9, 2) AS BIRTH_DAY, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T4.ZIPCD, ";
        $query .= "     T4.ADDRESS1, ";
        $query .= "     T4.ADDRESS2, ";
        $query .= "     T4.TELNO, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     L4.FINSCHOOL_NAME, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     T1.FS_GRDDIV, ";
        $query .= "     T4.GNAME, ";
        $query .= "     T4.GKANA, ";
        $query .= "     T4.GZIPCD, ";
        $query .= "     T4.GADDRESS1, ";
        $query .= "     T4.GADDRESS2, ";
        $query .= "     T4.GTELNO, ";
        $query .= "     T6.REMARK3 AS EXAMCOURSECD2, ";
        $query .= "     M3.EXAMCOURSE_NAME AS EXAMCOURSE_NAME2, ";
        $query .= "     T2.REMARK7, ";
        $query .= "     N3.NAME1 AS REMARK7_NAME, ";
        $query .= "     T2.REMARK1, ";
        $query .= "     T2.REMARK2, ";
        $query .= "     T2.REMARK3, ";
        $query .= "     T2.REMARK4, ";
        $query .= "     T2.REMARK8, ";
        $query .= "     N4.NAME1 AS REMARK8_NAME, ";
        $query .= "     T2.REMARK5, ";
        $query .= "     T2.REMARK6, ";
        //調査書
        $query .= "     T1.EXAMNO AS EXAMNO2, ";
        $query .= "     T3.CONFIDENTIAL_RPT01, ";
        $query .= "     T3.CONFIDENTIAL_RPT02, ";
        $query .= "     T3.CONFIDENTIAL_RPT03, ";
        $query .= "     T3.CONFIDENTIAL_RPT04, ";
        $query .= "     T3.CONFIDENTIAL_RPT05, ";
        $query .= "     T3.CONFIDENTIAL_RPT06, ";
        $query .= "     T3.CONFIDENTIAL_RPT07, ";
        $query .= "     T3.CONFIDENTIAL_RPT08, ";
        $query .= "     T3.CONFIDENTIAL_RPT09, ";
        $query .= "     T3.TOTAL_ALL, ";
        $query .= "     T3.TOTAL5, ";
        $query .= "     T3.KASANTEN_ALL, ";
        $query .= "     T3.ABSENCE_DAYS, ";
        $query .= "     T3.ABSENCE_DAYS2, ";
        $query .= "     T3.ABSENCE_DAYS3, ";
        $query .= "     T3.ABSENCE_REMARK, ";
        $query .= "     T3.ABSENCE_REMARK2, ";
        $query .= "     T3.ABSENCE_REMARK3, ";
        $query .= "     T5.REMARK1 AS REMARK1_004, ";
        $query .= "     T3.REMARK1 AS TOKU_REMARK1, ";
        $query .= "     T1.EXAMNO AS EXAMNO3, ";
        $query .= "     T1.NAME AS E_NAME, ";
        $query .= "     '' AS E_TOTAL ";
        foreach ($model->testSubClassCd as $key => $testName) {
            $query .= "     ,EXAM_SCORE{$key}.SCORE AS EXAM_SCORE{$key} ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "                                                   AND T1.EXAMNO      = L1.EXAMNO ";
        $query .= "                                                   AND L1.SEQ         = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "                                                  AND T1.EXAMNO      = T2.EXAMNO ";
        $query .= "                                                  AND T2.SEQ         = '009' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T3 ON T1.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "                                              AND T1.EXAMNO      = T3.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T4 ON T1.ENTEXAMYEAR = T4.ENTEXAMYEAR ";
        $query .= "                                           AND T1.EXAMNO      = T4.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T5 ON T1.ENTEXAMYEAR = T5.ENTEXAMYEAR ";
        $query .= "                                                  AND T1.EXAMNO      = T5.EXAMNO ";
        $query .= "                                                  AND T5.SEQ         = '004' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T6 ON T1.ENTEXAMYEAR = T6.ENTEXAMYEAR ";
        $query .= "                                                  AND T1.EXAMNO = T6.EXAMNO ";
        $query .= "                                                  AND T6.SEQ = '017' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST L4 ON T1.FS_CD = L4.FINSCHOOLCD";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST M1 ON T1.ENTEXAMYEAR  = M1.ENTEXAMYEAR";
        $query .= "                                    AND T1.APPLICANTDIV = M1.APPLICANTDIV ";
        $query .= "                                    AND T1.TESTDIV      = M1.TESTDIV ";
        $query .= "                                    AND L1.REMARK8      = M1.COURSECD ";
        $query .= "                                    AND L1.REMARK9      = M1.MAJORCD ";
        $query .= "                                    AND L1.REMARK10     = M1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST M2 ON T1.ENTEXAMYEAR    = M2.ENTEXAMYEAR";
        $query .= "                                    AND T1.APPLICANTDIV   = M2.APPLICANTDIV ";
        $query .= "                                    AND T1.TESTDIV        = M2.TESTDIV ";
        $query .= "                                    AND T1.SUC_COURSECD   = M2.COURSECD ";
        $query .= "                                    AND T1.SUC_MAJORCD    = M2.MAJORCD ";
        $query .= "                                    AND T1.SUC_COURSECODE = M2.EXAMCOURSECD ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST M3 ON T1.ENTEXAMYEAR  = M3.ENTEXAMYEAR";
        $query .= "                                    AND T1.APPLICANTDIV = M3.APPLICANTDIV ";
        $query .= "                                    AND T1.TESTDIV      = M3.TESTDIV ";
        $query .= "                                    AND T6.REMARK1      = M3.COURSECD ";
        $query .= "                                    AND T6.REMARK2      = M3.MAJORCD ";
        $query .= "                                    AND T6.REMARK3      = M3.EXAMCOURSECD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ON T1.ENTEXAMYEAR = N1.YEAR";
        $query .= "                            AND T1.JUDGEMENT   = N1.NAMECD2 ";
        $query .= "                            AND N1.NAMECD1     = 'L013' ";
        $query .= "     LEFT JOIN V_NAME_MST N2 ON T1.ENTEXAMYEAR = N2.YEAR";
        $query .= "                            AND T1.SHDIV       = N2.NAMECD2 ";
        $query .= "                            AND N2.NAMECD1     = 'L006' ";
        $query .= "     LEFT JOIN V_NAME_MST N3 ON T1.ENTEXAMYEAR = N3.YEAR";
        $query .= "                            AND T2.REMARK7     = N3.NAMECD2 ";
        $query .= "                            AND N3.NAMECD1     = 'L025' ";
        $query .= "     LEFT JOIN V_NAME_MST N4 ON T1.ENTEXAMYEAR = N4.YEAR";
        $query .= "                            AND T2.REMARK8     = N4.NAMECD2 ";
        $query .= "                            AND N4.NAMECD1     = 'L025' ";
        $query .= "     LEFT JOIN V_NAME_MST N5 ON T1.ENTEXAMYEAR = N5.YEAR";
        $query .= "                            AND T2.REMARK9     = N5.NAMECD2 ";
        $query .= "                            AND N5.NAMECD1     = 'L026' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT REC ";
        $query .= "                          ON T1.ENTEXAMYEAR  = REC.ENTEXAMYEAR ";
        $query .= "                         AND T1.APPLICANTDIV = REC.APPLICANTDIV ";
        $query .= "                         AND T1.TESTDIV      = REC.TESTDIV ";
        $query .= "                         AND REC.EXAM_TYPE   = '1' ";
        $query .= "                         AND T1.EXAMNO       = REC.RECEPTNO ";
        foreach ($model->testSubClassCd as $key => $testName) {
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT EXAM_SCORE{$key} ";
            $query .= "                          ON REC.ENTEXAMYEAR            = EXAM_SCORE{$key}.ENTEXAMYEAR ";
            $query .= "                         AND REC.APPLICANTDIV           = EXAM_SCORE{$key}.APPLICANTDIV ";
            $query .= "                         AND REC.TESTDIV                = EXAM_SCORE{$key}.TESTDIV ";
            $query .= "                         AND REC.EXAM_TYPE              = EXAM_SCORE{$key}.EXAM_TYPE ";
            $query .= "                         AND REC.RECEPTNO               = EXAM_SCORE{$key}.RECEPTNO ";
            $query .= "                         AND EXAM_SCORE{$key}.TESTSUBCLASSCD = '{$key}' ";
        }
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->test_year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        if ($model->field["TOTALCD"] != "9999") {
            $query .= "     AND L1.REMARK8 || L1.REMARK9 || L1.REMARK10 = '{$model->field["TOTALCD"]}' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["TOTALCD"] == "9999") {
            $query .= "     L1.REMARK8 || L1.REMARK9 || L1.REMARK10, ";
        }
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    /******************************************************************************/
    /*************************** そのデータが存在するか ***************************/
    /******************************************************************************/
    //入試制度
    public function applicantdivCntSql($applicantdiv)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L003' AND ";
        $query .= "     NAMECD2 = '{$applicantdiv}' ";

        return $query;
    }

    //入試区分
    public function testdivCntSql($tstdiv)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L004' AND ";
        $query .= "     NAMECD2 = '{$tstdiv}' ";

        return $query;
    }

    //入試区分
    public function relationshipCntSql($relationship)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H201' AND ";
        $query .= "     NAMECD2 = '{$relationship}' ";

        return $query;
    }

    //名称マスタ登録コードチェック
    public function nameCntSql($namecd, $setcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd}' AND ";
        $query .= "     NAMECD2 = '{$setcd}' ";

        return $query;
    }

    //出身学校コード
    public function fsCdCntSql($fs_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$fs_cd}' ";

        return $query;
    }

    //第2志望コースコード
    public function examcourseCdCntSql($model, $examcoursecd2)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->test_year}' ";
        $query .= " AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " AND TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= " AND COURSECD     = '".substr($model->field["TOTALCD"], 0, 1)."' ";
        $query .= " AND MAJORCD      = '".substr($model->field["TOTALCD"], 1, 3)."' ";
        $query .= " AND EXAMCOURSECD = '{$examcoursecd2}' ";

        return $query;
    }

    /**************************************************************/
    /************************* 更新時に使う ***********************/
    /**************************************************************/
    public function cntEntexamApplicantbaseDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' AND ";
        $query .= "     EXAMNO = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    public function cntEntexamApplicantbaseDetailDat($model, $data_arr, $seq)
    {
        $setSeq = sprintf("%03d", $seq);
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' AND ";
        $query .= "     EXAMNO = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ = '{$setSeq}' ";

        return $query;
    }

    public function cntEntexamApplicantaddrDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' AND ";
        $query .= "     EXAMNO = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    public function cntEntexamApplicantconfrptDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->test_year}' AND ";
        $query .= "     EXAMNO = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //出身学校
    public function getFinschoolMst($finschool_cd)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$finschool_cd}' ";

        return $query;
    }

    //名称マスタより和暦の元号を取得
    public function getCalendarno($year)
    {
        $query = "  SELECT
                        NAMECD2,
                        NAME1,
                        NAMESPARE1,
                        NAMESPARE2,
                        NAMESPARE3
                    FROM
                        V_NAME_MST
                    WHERE
                        YEAR = '".$year."' AND
                        NAMECD1 = 'L007'
                 ";

        return $query;
    }
}
