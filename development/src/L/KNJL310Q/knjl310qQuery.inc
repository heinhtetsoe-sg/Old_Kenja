<?php

require_once('for_php7.php');
class knjl310qquery extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/

    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/

    //小学・願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv3_1($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //元号取得
        $result = $db->query(knjl310qQuery::getNameMstAll($model->ObjYear, "L007"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //分解する日付一覧
            $date_array = array("BIRTHDAY" => "birth");
            //部品名
            $parts = array("_eracd", "_y", "_m", "_d");

            foreach ($date_array as $dkey => $dval) {
                if (strlen($data_arr[$i][$dkey])) {
                    //日付を分解
                    list($y, $m, $d) = preg_split('/-/', $data_arr[$i][$dkey]);
                    //部品の変数名
                    list($e_id, $y_id, $m_id, $d_id) = array($dval."_eracd", $dval."_y", $dval."_m", $dval."_d");

                    $$e_id = $$y_id = $$m_id = $$d_id = "";
                    for ($ere_no=0; $ere_no < get_count($eracd); $ere_no++) {
                        if ($defsdate[$ere_no] <= $data_arr[$i][$dkey] && $data_arr[$i][$dkey] <= $defedate[$ere_no]) {
                            $$e_id = $eracd[$ere_no];
                            $$y_id = ((int)$y + 1) - ((int)$defyear[$ere_no]);
                            $$y_id = sprintf("%02d", $$y_id);
                            $$m_id = $m;
                            $$d_id = $d;
                            break;
                        }
                    }
                }
            }

            //受験番号範囲データからデータ取得
            $noData = $db->getRow(knjl310qQuery::getEntexamNoDat($model->ObjYear, $data_arr[$i]["APPLICANTDIV"], $data_arr[$i]["EXAMNO"]), DB_FETCHMODE_ASSOC);

            //データ件数取得
            $base_cnt       = $db->getOne(knjl310qQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt       = $db->getOne(knjl310qQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $baseD001_cnt   = $db->getOne(knjl310qQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "001"));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]              = $noData["TESTDIV"];
            $data["SHDIV"][TEXT]                = '1';
            $data["DESIREDIV"][TEXT]            = '1';
            $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]                  = $data_arr[$i]["SEX"];
            $data["ERACD"][TEXT]                = $birth_eracd;
            $data["BIRTH_Y"][TEXT]              = $birth_y;
            $data["BIRTH_M"][TEXT]              = $birth_m;
            $data["BIRTH_D"][TEXT]              = $birth_d;
            $data["BIRTHDAY"][TEXT]             = $data_arr[$i]["BIRTHDAY"];
            $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $base_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["FAMILY_REGISTER"][TEXT]  = $data_arr[$i]["FAMILY_REGISTER"];
            $data["ZIPCD"][TEXT]            = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]         = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TELNO"];
            $data["GKANA"][TEXT]            = $data_arr[$i]["GKANA"];
            $data["GNAME"][TEXT]            = $data_arr[$i]["GNAME"];
            $data["GZIPCD"][TEXT]           = $data_arr[$i]["GZIPCD"];
            $data["GADDRESS1"][TEXT]        = $data_arr[$i]["GADDRESS1"];
            $data["GADDRESS2"][TEXT]        = $data_arr[$i]["GADDRESS2"];
            $data["GTELNO"][TEXT]           = $data_arr[$i]["GTELNO"];
            $data["RELATIONSHIP"][TEXT]     = $data_arr[$i]["RELATIONSHIP"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=001
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "001";
            $data["REMARK8"][TEXT]          = $noData["COURSECD"];
            $data["REMARK9"][TEXT]          = $noData["MAJORCD"];
            $data["REMARK10"][TEXT]         = $noData["EXAMCOURSECD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '001' ";

            if (0 == $baseD001_cnt) {
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

    //中学・願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv1_1($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //元号取得
        $result = $db->query(knjl310qQuery::getNameMstAll($model->ObjYear, "L007"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //分解する日付一覧
            $date_array = array("BIRTHDAY" => "birth");
            //部品名
            $parts = array("_eracd", "_y", "_m", "_d");

            foreach ($date_array as $dkey => $dval) {
                if (strlen($data_arr[$i][$dkey])) {
                    //日付を分解
                    list($y, $m, $d) = preg_split('/-/', $data_arr[$i][$dkey]);
                    //部品の変数名
                    list($e_id, $y_id, $m_id, $d_id) = array($dval."_eracd", $dval."_y", $dval."_m", $dval."_d");

                    $$e_id = $$y_id = $$m_id = $$d_id = "";
                    for ($ere_no=0; $ere_no < get_count($eracd); $ere_no++) {
                        if ($defsdate[$ere_no] <= $data_arr[$i][$dkey] && $data_arr[$i][$dkey] <= $defedate[$ere_no]) {
                            $$e_id = $eracd[$ere_no];
                            $$y_id = ((int)$y + 1) - ((int)$defyear[$ere_no]);
                            $$y_id = sprintf("%02d", $$y_id);
                            $$m_id = $m;
                            $$d_id = $d;
                            break;
                        }
                    }
                }
            }

            //受験番号範囲データからデータ取得
            $noData = $db->getRow(knjl310qQuery::getEntexamNoDat($model->ObjYear, $data_arr[$i]["APPLICANTDIV"], $data_arr[$i]["EXAMNO"]), DB_FETCHMODE_ASSOC);

            //データ件数取得
            $base_cnt       = $db->getOne(knjl310qQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt       = $db->getOne(knjl310qQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $baseD001_cnt   = $db->getOne(knjl310qQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "001"));
            $baseD005_cnt   = $db->getOne(knjl310qQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "005"));
            $baseD014_cnt   = $db->getOne(knjl310qQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "014"));
            $confRpt_cnt    = $db->getOne(knjl310qQuery::cntEntexamApplicantconfrptDat($model, $data_arr[$i]));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]              = $noData["TESTDIV"];
            $data["SHDIV"][TEXT]                = $data_arr[$i]["SHDIV"];
            $data["DESIREDIV"][TEXT]            = '1';
            $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]                  = $data_arr[$i]["SEX"];
            $data["ERACD"][TEXT]                = $birth_eracd;
            $data["BIRTH_Y"][TEXT]              = $birth_y;
            $data["BIRTH_M"][TEXT]              = $birth_m;
            $data["BIRTH_D"][TEXT]              = $birth_d;
            $data["BIRTHDAY"][TEXT]             = $data_arr[$i]["BIRTHDAY"];
            $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
            $data["REMARK1"][TEXT]              = $data_arr[$i]["REMARK1"];
            $data["RECOM_EXAMNO"][TEXT]         = $data_arr[$i]["RECOM_EXAMNO"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $base_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["FAMILY_REGISTER"][TEXT]  = $data_arr[$i]["FAMILY_REGISTER"];
            $data["ZIPCD"][TEXT]            = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]         = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TELNO"];
            $data["GKANA"][TEXT]            = $data_arr[$i]["GKANA"];
            $data["GNAME"][TEXT]            = $data_arr[$i]["GNAME"];
            $data["GZIPCD"][TEXT]           = $data_arr[$i]["GZIPCD"];
            $data["GADDRESS1"][TEXT]        = $data_arr[$i]["GADDRESS1"];
            $data["GADDRESS2"][TEXT]        = $data_arr[$i]["GADDRESS2"];
            $data["GTELNO"][TEXT]           = $data_arr[$i]["GTELNO"];
            $data["RELATIONSHIP"][TEXT]     = $data_arr[$i]["RELATIONSHIP"];
            $data["GJOB"][TEXT]             = $data_arr[$i]["GJOB"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=001
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "001";
            $data["REMARK8"][TEXT]          = $noData["COURSECD"];
            $data["REMARK9"][TEXT]          = $noData["MAJORCD"];
            $data["REMARK10"][TEXT]         = $noData["EXAMCOURSECD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '001' ";

            if (0 == $baseD001_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=005
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "005";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["SH_SCHOOLCD1"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["SH_SCHOOLCD2"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["SH_SCHOOLCD3"];
            $data["REMARK4"][TEXT]          = $data_arr[$i]["SH_SCHOOLCD4"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '005' ";

            if (0 == $baseD005_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=014
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "014";
            $data["REMARK2"][TEXT]          = $data_arr[$i]["BROS_INFO1"];
            $data["REMARK4"][TEXT]          = $data_arr[$i]["BROS_INFO2"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '014' ";

            if (0 == $baseD014_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["ABSENCE_DAYS2"][NUMBER]      = $data_arr[$i]["ABSENCE_DAYS2"];
            $data["ABSENCE_DAYS3"][NUMBER]      = $data_arr[$i]["ABSENCE_DAYS3"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $confRpt_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //高校・願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv2_1($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //元号取得
        $result = $db->query(knjl310qQuery::getNameMstAll($model->ObjYear, "L007"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[]    = $row["NAMECD2"];
            $defyear[]  = $row["NAMESPARE1"];
            $defsdate[] = $row["NAMESPARE2"];
            $defedate[] = $row["NAMESPARE3"];
        }
        $result->free();

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //分解する日付一覧
            $date_array = array("BIRTHDAY" => "birth", "FS_DAY" => "fs");
            //部品名
            $parts = array("_eracd", "_y", "_m", "_d");

            foreach ($date_array as $dkey => $dval) {
                if (strlen($data_arr[$i][$dkey])) {
                    //日付を分解
                    list($y, $m, $d) = preg_split('/-/', $data_arr[$i][$dkey]);
                    //部品の変数名
                    list($e_id, $y_id, $m_id, $d_id) = array($dval."_eracd", $dval."_y", $dval."_m", $dval."_d");

                    $$e_id = $$y_id = $$m_id = $$d_id = "";
                    for ($ere_no=0; $ere_no < get_count($eracd); $ere_no++) {
                        if ($defsdate[$ere_no] <= $data_arr[$i][$dkey] && $data_arr[$i][$dkey] <= $defedate[$ere_no]) {
                            $$e_id = $eracd[$ere_no];
                            $$y_id = ((int)$y + 1) - ((int)$defyear[$ere_no]);
                            $$y_id = sprintf("%02d", $$y_id);
                            $$m_id = $m;
                            $$d_id = $d;
                            break;
                        }
                    }
                }
            }

            //受験番号範囲データからデータ取得
            $noData = $db->getRow(knjl310qQuery::getEntexamNoDat($model->ObjYear, $data_arr[$i]["APPLICANTDIV"], $data_arr[$i]["EXAMNO"]), DB_FETCHMODE_ASSOC);

            //データ件数取得
            $base_cnt       = $db->getOne(knjl310qQuery::cntEntexamApplicantbaseDat($model, $data_arr[$i]));
            $addr_cnt       = $db->getOne(knjl310qQuery::cntEntexamApplicantaddrDat($model, $data_arr[$i]));
            $baseD001_cnt   = $db->getOne(knjl310qQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "001"));
            $baseD002_cnt   = $db->getOne(knjl310qQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "002"));
            $baseD005_cnt   = $db->getOne(knjl310qQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "005"));
            $baseD023_cnt   = $db->getOne(knjl310qQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "023"));
            $baseD024_cnt   = $db->getOne(knjl310qQuery::cntEntexamApplicantbaseDetailDat($model, $data_arr[$i], "024"));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["TESTDIV"][TEXT]              = $noData["TESTDIV"];
            if ($noData["TESTDIV0"] != "1") {
                $data["SHDIV"][TEXT]                = $data_arr[$i]["SHDIV"];
            }
            $data["DESIREDIV"][TEXT]            = '1';
            $data["TESTDIV0"][TEXT]             = $noData["TESTDIV0"];
            $data["NAME"][TEXT]                 = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]            = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]                  = $data_arr[$i]["SEX"];
            $data["ERACD"][TEXT]                = $birth_eracd;
            $data["BIRTH_Y"][TEXT]              = $birth_y;
            $data["BIRTH_M"][TEXT]              = $birth_m;
            $data["BIRTH_D"][TEXT]              = $birth_d;
            $data["BIRTHDAY"][TEXT]             = $data_arr[$i]["BIRTHDAY"];
            $data["FS_CD"][TEXT]                = $data_arr[$i]["FS_CD"];
            $data["FS_ERACD"][TEXT]             = $fs_eracd;
            $data["FS_Y"][TEXT]                 = $fs_y;
            $data["FS_M"][TEXT]                 = $fs_m;
            $data["FS_DAY"][TEXT]               = $data_arr[$i]["FS_DAY"];
            $data["FS_GRDDIV"][TEXT]            = $data_arr[$i]["FS_GRDDIV"];
            $data["DORMITORY_FLG"][TEXT]        = $data_arr[$i]["DORMITORY_FLG"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $base_cnt) {
                if ($noData["TESTDIV0"] == "1") {
                    $data["SHDIV"][TEXT]                = "0";
                }
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["ZIPCD"][TEXT]            = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]         = $data_arr[$i]["ADDRESS2"];
            $data["TELNO"][TEXT]            = $data_arr[$i]["TELNO"];
            $data["GKANA"][TEXT]            = $data_arr[$i]["GKANA"];
            $data["GNAME"][TEXT]            = $data_arr[$i]["GNAME"];
            $data["GZIPCD"][TEXT]           = $data_arr[$i]["GZIPCD"];
            $data["GADDRESS1"][TEXT]        = $data_arr[$i]["GADDRESS1"];
            $data["GADDRESS2"][TEXT]        = $data_arr[$i]["GADDRESS2"];
            $data["GTELNO"][TEXT]           = $data_arr[$i]["GTELNO"];
            $data["RELATIONSHIP"][TEXT]     = $data_arr[$i]["RELATIONSHIP"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $addr_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=001
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "001";
            $data["REMARK8"][TEXT]          = $noData["COURSECD"];
            $data["REMARK9"][TEXT]          = $noData["MAJORCD"];
            $data["REMARK10"][TEXT]         = $noData["EXAMCOURSECD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '001' ";

            if (0 == $baseD001_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=002
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "002";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["JIZEN_BANGOU"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '002' ";

            if (0 == $baseD002_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=005
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "005";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["SH_SCHOOLCD1"];
            $data["REMARK2"][TEXT]          = $data_arr[$i]["SH_SCHOOLCD2"];
            $data["REMARK3"][TEXT]          = $data_arr[$i]["SH_SCHOOLCD3"];
            $data["REMARK4"][TEXT]          = $data_arr[$i]["SH_SCHOOLCD4"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '005' ";

            if (0 == $baseD005_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=023
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "023";
            $data["REMARK1"][TEXT]          = $data_arr[$i]["SCHOLAR_KIBOU"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '023' ";

            if (0 == $baseD023_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=024
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]              = "024";
            $data["REMARK1"][TEXT]          = $noData["EXAMHALLCD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND ";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '024' ";

            if (0 == $baseD024_cnt) {
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

    //高校・調査書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv2_2($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //教科取得
        $class_array = $avg5_class = array();
        $result = $db->query(knjl310qQuery::getNameMstAll($model->ObjYear, "L008"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $class_array[] = $row["NAMECD2"];
            if ($row["NAMESPARE1"] == "1") {
                $avg5_class[] = $row["NAMECD2"];
            }
        }

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データ件数取得
            $confRpt_cnt        = $db->getOne(knjl310qQuery::cntEntexamApplicantconfrptDat($model, $data_arr[$i]));
            $confRptD001_cnt    = $db->getOne(knjl310qQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "001"));
            $confRptD002_cnt    = $db->getOne(knjl310qQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "002"));

            //ENTEXAM_APPLICANTCONFRPT_DAT
            $sum5 = $sumAll = 0;
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            foreach ($class_array as $class) {
                $confrpt = ($data_arr[$i]["CONFRPT".$class."_3"]) ? $data_arr[$i]["CONFRPT".$class."_3"] : 0;
                $data["CONFIDENTIAL_RPT".$class][NUMBER] = $confrpt;
                if (in_array($class, $avg5_class)) {
                    $sum5 += $confrpt;
                }
                $sumAll += $confrpt;
            }
            $avg5   = ($sum5 > 0) ? round(($sum5 / 5), 1) : 0;
            $avgAll = ($sumAll > 0) ? round(($sumAll /get_count($class_array)), 1) : 0;
            $data["AVERAGE5"][NUMBER]           = ($data_arr[$i]["AVG5_3"]) ? $data_arr[$i]["AVG5_3"] : $avg5;
            $data["AVERAGE_ALL"][NUMBER]        = ($data_arr[$i]["AVGALL_3"]) ? $data_arr[$i]["AVGALL_3"] : $avgAll;
            $data["ABSENCE_DAYS"][NUMBER]       = $data_arr[$i]["ABSENCE_DAYS1"];
            $data["ABSENCE_DAYS2"][NUMBER]      = $data_arr[$i]["ABSENCE_DAYS2"];
            $data["ABSENCE_DAYS3"][NUMBER]      = $data_arr[$i]["ABSENCE_DAYS3"];
            $data["ABSENCE_REMARK"][TEXT]       = $data_arr[$i]["ABSENCE_REMARK1"];
            $data["ABSENCE_REMARK2"][TEXT]      = $data_arr[$i]["ABSENCE_REMARK2"];
            $data["ABSENCE_REMARK3"][TEXT]      = $data_arr[$i]["ABSENCE_REMARK3"];
            $data["REMARK1"][TEXT]              = $data_arr[$i]["REMARK1"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' ";

            if (0 == $confRpt_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT  SEQ=001,002
            for ($grade = 1; $grade <= 2; $grade++) {
                $seq = sprintf("%03d", $grade);
                $sum5 = $sumAll = 0;
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
                $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
                $data["SEQ"][TEXT]                  = $seq;
                foreach ($class_array as $class) {
                    $num = (int)$class;
                    if ($class == "11" || $class == "12") {
                        continue;
                    }
                    $confrpt = ($data_arr[$i]["CONFRPT".$class."_".$grade]) ? $data_arr[$i]["CONFRPT".$class."_".$grade] : 0;
                    $data["REMARK".$num][TEXT]         = $confrpt;
                    if (in_array($class, $avg5_class)) {
                        $sum5 += $confrpt;
                    }
                    $sumAll += $confrpt;
                }
                $avg5   = ($sum5 > 0) ? round(($sum5 / 5), 1) : 0;
                $avgAll = ($sumAll > 0) ? round(($sumAll /get_count($class_array)), 1) : 0;
                $data["REMARK11"][TEXT]             = ($data_arr[$i]["AVG5_".$grade]) ? $data_arr[$i]["AVG5_".$grade] : $avg5;
                $data["REMARK12"][TEXT]             = ($data_arr[$i]["AVGALL_".$grade]) ? $data_arr[$i]["AVGALL_".$grade] : $avgAll;
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "SYSDATE()";

                $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
                $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
                $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
                $where .= "       SEQ           = '{$seq}' ";

                $cntdata = "confRptD".$seq."_cnt";
                if (0 == $$cntdata) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
                } else {
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
                }
                $db->query($query);
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //高校・活動実績の追加（ＣＳＶデータより読込）
    public function updateQueryCsv2_3($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データ件数取得
            $confRptD003_cnt = $db->getOne(knjl310qQuery::cntEntexamApplicantconfrptDetailDat($model, $data_arr[$i], "003"));

            //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT  SEQ=003
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]         = $data_arr[$i]["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]               = $data_arr[$i]["EXAMNO"];
            $data["SEQ"][TEXT]                  = '003';
            $data["REMARK1"][TEXT]              = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]              = $data_arr[$i]["REMARK2"];
            $data["REMARK3"][TEXT]              = $data_arr[$i]["REMARK3"];
            $data["REMARK9"][TEXT]              = $data_arr[$i]["REMARK9"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' AND";
            $where .= "       APPLICANTDIV  = '{$data_arr[$i]["APPLICANTDIV"]}' AND ";
            $where .= "       EXAMNO        = '{$data_arr[$i]["EXAMNO"]}' AND ";
            $where .= "       SEQ           = '003' ";

            if (0 == $confRptD003_cnt) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
            }
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    //CVSエラー作成用のQUERY
    public function selectCsvErrQuery()
    {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/

    //受験番号範囲取得
    public function getEntexamNoDat($entexamyear, $applicantdiv, $examno = "")
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$entexamyear}' AND ";
        $query .= "     APPLICANTDIV    = '{$applicantdiv}' ";
        if ($examno) {
            $query .= " AND '".$examno."' BETWEEN EXAMNO_FROM AND EXAMNO_TO ";
        }

        return $query;
    }

    //名称マスタ登録コードチェック
    public function nameCntSql($namecd, $setcd, $where = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd}' AND ";
        $query .= "     NAMECD2 = '{$setcd}' ";
        if ($where) {
            $query .= $where;
        }

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

    //本籍コード
    public function prefCdCntSql($pref_cd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " WHERE ";
        $query .= "     PREF_CD = '{$pref_cd}' ";

        return $query;
    }

    /********************/
    /**  更新時に使用  **/
    /********************/

    //件数取得 -- ENTEXAM_APPLICANTBASE_DAT
    public function cntEntexamApplicantbaseDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        if (SCHOOLKIND == "P") {
            $query .= "     EXAMNO          = '".sprintf("%04d", $data_arr["EXAMNO"])."' ";
        } else {
            $query .= "     EXAMNO          = '".sprintf("%05d", $data_arr["EXAMNO"])."' ";
        }

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTADDR_DAT
    public function cntEntexamApplicantaddrDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function cntEntexamApplicantbaseDetailDat($model, $data_arr, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ             = '{$seq}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTCONFRPT_DAT
    public function cntEntexamApplicantconfrptDat($model, $data_arr)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' ";

        return $query;
    }

    //件数取得 -- ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
    public function cntEntexamApplicantconfrptDetailDat($model, $data_arr, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "     APPLICANTDIV    = '{$data_arr["APPLICANTDIV"]}' AND ";
        $query .= "     EXAMNO          = '{$data_arr["EXAMNO"]}' AND ";
        $query .= "     SEQ             = '{$seq}' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/

    //名称マスタより取得
    public function getNameMstAll($year, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //ヘッダ例用データを名称マスタより取得
    public function getHeaderReiNameMst($year, $namecd1, $name, $namecd2 = "", $where = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || {$name} AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        if ($where) {
            $query .= " AND ".$where;
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出身学校名取得
    public function getFinschoolDistcdName()
    {
        $query  = " SELECT ";
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     VALUE(L1.NAME1,'') || T1.FINSCHOOL_NAME AS FINSCHOOL_DISTCD_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ";
        $query .= "          ON L1.NAMECD1 = 'L001' ";
        $query .= "         AND L1.NAMECD2 = T1.FINSCHOOL_DISTCD ";

        return $query;
    }

    //CSVデータ出力（小学・願書）
    public function selectMainQuery3_1($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.EXAMNO, ";
        //受験区分
        $query .= "     L1.EXAMCOURSE_NAME, ";
        $query .= "     N1.NAME1 AS TESTDIV_NAME, ";
        //志願者情報
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     L3.ZIPCD, ";
        $query .= "     L3.ADDRESS1, ";
        $query .= "     L3.ADDRESS2, ";
        $query .= "     L3.TELNO, ";
        //出身学校
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        //本籍
        $query .= "     L3.FAMILY_REGISTER, ";
        //保護者情報
        $query .= "     L3.GNAME, ";
        $query .= "     L3.GKANA, ";
        $query .= "     L3.GZIPCD, ";
        $query .= "     L3.GADDRESS1, ";
        $query .= "     L3.GADDRESS2, ";
        $query .= "     L3.GTELNO, ";
        $query .= "     L3.RELATIONSHIP, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "              ON T1.ENTEXAMYEAR      = L1.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV     = L1.APPLICANTDIV ";
        $query .= "             AND T1.TESTDIV          = L1.TESTDIV ";
        $query .= "             AND T1.DAI1_COURSECD    = L1.COURSECD ";
        $query .= "             AND T1.DAI1_MAJORCD     = L1.MAJORCD ";
        $query .= "             AND T1.DAI1_COURSECODE  = L1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON T1.ENTEXAMYEAR      = N1.YEAR ";
        $query .= "             AND N1.NAMECD1          = '{$model->nameMstTest}' ";
        $query .= "             AND N1.NAMECD2          = T1.TESTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L3 ";
        $query .= "              ON T1.ENTEXAMYEAR      = L3.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV     = L3.APPLICANTDIV ";
        $query .= "             AND T1.EXAMNO           = L3.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //CSVデータ出力（中学・願書）
    public function selectMainQuery1_1($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.EXAMNO, ";
        //受験区分
        $query .= "     L1.EXAMCOURSE_NAME, ";
        $query .= "     N1.NAME1 AS TESTDIV_NAME, ";
        //志願者情報
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     L3.ZIPCD, ";
        $query .= "     L3.ADDRESS1, ";
        $query .= "     L3.ADDRESS2, ";
        $query .= "     L3.TELNO, ";
        //出身学校
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        //本籍
        $query .= "     L3.FAMILY_REGISTER, ";
        //兄弟情報1/2
        $query .= "     L5.REMARK2 AS BROS_INFO1, ";
        $query .= "     L5.REMARK4 AS BROS_INFO2, ";
        //保護者情報
        $query .= "     L3.GNAME, ";
        $query .= "     L3.GKANA, ";
        $query .= "     L3.GZIPCD, ";
        $query .= "     L3.GADDRESS1, ";
        $query .= "     L3.GADDRESS2, ";
        $query .= "     L3.GTELNO, ";
        $query .= "     L3.RELATIONSHIP, ";
        $query .= "     L3.GJOB, ";
        $query .= "     T1.REMARK1, ";
        //出欠の記録
        $query .= "     L4.ABSENCE_DAYS2, ";
        $query .= "     L4.ABSENCE_DAYS3, ";
        //専併区分
        $query .= "     T1.SHDIV, ";
        //推薦受験番号
        $query .= "     T1.RECOM_EXAMNO, ";
        //志望校
        $query .= "     T1.SH_SCHOOLCD1, ";
        $query .= "     T1.SH_SCHOOLCD2, ";
        $query .= "     T1.SH_SCHOOLCD3, ";
        $query .= "     T1.SH_SCHOOLCD4, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "              ON T1.ENTEXAMYEAR      = L1.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV     = L1.APPLICANTDIV ";
        $query .= "             AND T1.TESTDIV          = L1.TESTDIV ";
        $query .= "             AND T1.DAI1_COURSECD    = L1.COURSECD ";
        $query .= "             AND T1.DAI1_MAJORCD     = L1.MAJORCD ";
        $query .= "             AND T1.DAI1_COURSECODE  = L1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON T1.ENTEXAMYEAR      = N1.YEAR ";
        $query .= "             AND N1.NAMECD1          = '{$model->nameMstTest}' ";
        $query .= "             AND N1.NAMECD2          = T1.TESTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L3 ";
        $query .= "              ON T1.ENTEXAMYEAR      = L3.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV     = L3.APPLICANTDIV ";
        $query .= "             AND T1.EXAMNO           = L3.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT L4 ";
        $query .= "              ON T1.ENTEXAMYEAR      = L4.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV     = L4.APPLICANTDIV ";
        $query .= "             AND T1.EXAMNO           = L4.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L5 ";
        $query .= "              ON T1.ENTEXAMYEAR      = L5.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV     = L5.APPLICANTDIV ";
        $query .= "             AND T1.EXAMNO           = L5.EXAMNO ";
        $query .= "             AND L5.SEQ = '014' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        if ($model->field["TESTDIV"] != '99') {
            $query .= "     AND T1.TESTDIV      = '".$model->field["TESTDIV"]."' ";
        }
        $query .= " ORDER BY ";
        if ($model->field["TESTDIV"] == '99') {
            $query .= "     T1.TESTDIV, ";
        }
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //CSVデータ出力（高校・願書）
    public function selectMainQuery2_1($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV0, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.EXAMNO, ";
        //受験区分
        $query .= "     L1.EXAMCOURSE_NAME, ";
        $query .= "     N1.NAME1 AS TESTDIV_NAME, ";
        $query .= "     L2.EXAMHALL_NAME, ";
        //実践模試受験番号
        $query .= "     T1.JIZEN_BANGOU, ";
        //志願者情報
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     L3.ZIPCD, ";
        $query .= "     L3.ADDRESS1, ";
        $query .= "     L3.ADDRESS2, ";
        $query .= "     L3.TELNO, ";
        //出身学校
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     T1.FS_GRDDIV, ";
        //保護者情報
        $query .= "     L3.GNAME, ";
        $query .= "     L3.GKANA, ";
        $query .= "     L3.GZIPCD, ";
        $query .= "     L3.GADDRESS1, ";
        $query .= "     L3.GADDRESS2, ";
        $query .= "     L3.GTELNO, ";
        $query .= "     L3.RELATIONSHIP, ";
        //入寮希望
        $query .= "     T1.DORMITORY_FLG, ";
        //スカラーシップ希望
        $query .= "     T1.SCHOLAR_KIBOU, ";
        //専併区分
        $query .= "     T1.SHDIV, ";
        //志望校
        $query .= "     T1.SH_SCHOOLCD1, ";
        $query .= "     T1.SH_SCHOOLCD2, ";
        $query .= "     T1.SH_SCHOOLCD3, ";
        $query .= "     T1.SH_SCHOOLCD4, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST L1 ";
        $query .= "              ON T1.ENTEXAMYEAR      = L1.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV     = L1.APPLICANTDIV ";
        $query .= "             AND T1.TESTDIV          = L1.TESTDIV ";
        $query .= "             AND T1.DAI1_COURSECD    = L1.COURSECD ";
        $query .= "             AND T1.DAI1_MAJORCD     = L1.MAJORCD ";
        $query .= "             AND T1.DAI1_COURSECODE  = L1.EXAMCOURSECD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "              ON T1.ENTEXAMYEAR      = N1.YEAR ";
        $query .= "             AND N1.NAMECD1          = '{$model->nameMstTest}' ";
        $query .= "             AND N1.NAMECD2          = T1.TESTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_YDAT L2 ";
        $query .= "              ON T1.ENTEXAMYEAR      = L2.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV     = L2.APPLICANTDIV ";
        $query .= "             AND T1.TESTDIV          = L2.TESTDIV ";
        $query .= "             AND T1.SIKEN_KAIJOU_CD1 = L2.EXAMHALLCD ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT L3 ";
        $query .= "              ON T1.ENTEXAMYEAR      = L3.ENTEXAMYEAR ";
        $query .= "             AND T1.APPLICANTDIV     = L3.APPLICANTDIV ";
        $query .= "             AND T1.EXAMNO           = L3.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV0     = '".$model->field["TESTDIV0"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //CSVデータ出力（高校・調査書）
    public function selectMainQuery2_2($model, $class_array)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV0, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.EXAMNO, ";
        for ($grade = 1; $grade <= 3; $grade++) {
            foreach ($class_array as $key => $label) {
                $num = (int)$key;
                if ($grade == "3") {
                    $query .= "     L1.CONFIDENTIAL_RPT{$key} AS CONFRPT{$key}_{$grade}, ";
                } else {
                    $query .= "     D{$grade}.REMARK{$num} AS CONFRPT{$key}_{$grade}, ";
                }
            }
            if ($grade == "3") {
                $query .= "     L1.AVERAGE5 AS AVG5_{$grade}, ";
                $query .= "     L1.AVERAGE_ALL AS AVGALL_{$grade}, ";
            } else {
                $query .= "     D{$grade}.REMARK11 AS AVG5_{$grade}, ";
                $query .= "     D{$grade}.REMARK12 AS AVGALL_{$grade}, ";
            }
        }
        $query .= "     L1.ABSENCE_DAYS AS ABSENCE_DAYS1, ";
        $query .= "     L1.ABSENCE_DAYS2, ";
        $query .= "     L1.ABSENCE_DAYS3, ";
        $query .= "     L1.ABSENCE_REMARK AS ABSENCE_REMARK1, ";
        $query .= "     L1.ABSENCE_REMARK2, ";
        $query .= "     L1.ABSENCE_REMARK3, ";
        $query .= "     L1.REMARK1, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT L1 ";
        $query .= "          ON T1.ENTEXAMYEAR  = L1.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "         AND T1.EXAMNO       = L1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT D1 ";
        $query .= "          ON T1.ENTEXAMYEAR  = D1.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = D1.APPLICANTDIV ";
        $query .= "         AND T1.EXAMNO       = D1.EXAMNO ";
        $query .= "         AND D1.SEQ          = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT D2 ";
        $query .= "          ON T1.ENTEXAMYEAR  = D2.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = D2.APPLICANTDIV ";
        $query .= "         AND T1.EXAMNO       = D2.EXAMNO ";
        $query .= "         AND D2.SEQ          = '002' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV0     = '".$model->field["TESTDIV0"]."' ";
        $query .= " ORDER BY ";
        $query .= "      T1.EXAMNO ";

        return $query;
    }

    //CSVデータ出力（高校・活動実績）
    public function selectMainQuery2_3($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV0, ";
        $query .= "     '".$model->field["DATADIV"]."' AS DATADIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     L1.REMARK1, ";
        $query .= "     L1.REMARK2, ";
        $query .= "     L1.REMARK3, ";
        $query .= "     L1.REMARK9, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT L1 ";
        $query .= "          ON T1.ENTEXAMYEAR  = L1.ENTEXAMYEAR ";
        $query .= "         AND T1.APPLICANTDIV = L1.APPLICANTDIV ";
        $query .= "         AND T1.EXAMNO       = L1.EXAMNO ";
        $query .= "         AND L1.SEQ          = '003' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR  = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "     T1.TESTDIV0     = '".$model->field["TESTDIV0"]."' ";
        $query .= " ORDER BY ";
        $query .= "      T1.EXAMNO ";

        return $query;
    }
}
