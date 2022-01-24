<?php

require_once('for_php7.php');

class knjl016rquery extends Query
{
    public function getNameMst($model, $namecd1, $nameCd2 = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '{$nameCd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //願書の追加（ＣＳＶデータより読込）
    public function updateQueryCsv($model, &$data_arr)
    {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_CKIP_CSV_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";

        $db->query($query);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //ENTEXAM_CKIP_CSV_DAT
            $data = array();
            $data["LOGIN_ID"][TEXT]           = $data_arr[$i]["LOGIN_ID"];
            $data["ENTEXAMYEAR"][TEXT]        = $data_arr[$i]["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]       = $data_arr[$i]["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]            = $data_arr[$i]["TESTDIV"];
            $data["TESTDIV1"][TEXT]           = $data_arr[$i]["TESTDIV1"];
            $data["DESIREDIV"][TEXT]          = $data_arr[$i]["DESIREDIV"];
            $data["SHDIV"][TEXT]              = $data_arr[$i]["SHDIV"];
            $data["EXAMNO"][TEXT]             = $data_arr[$i]["EXAMNO"];
            $data["EXAMHALL_NAME"][TEXT]      = $data_arr[$i]["EXAMHALL_NAME"];
            $data["EXAMHALL_CLASSNAME"][TEXT] = $data_arr[$i]["EXAMHALL_CLASSNAME"];
            $data["NAME"][TEXT]               = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]          = $data_arr[$i]["NAME_KANA"];
            $data["SEX"][TEXT]                = $data_arr[$i]["SEX"];
            $data["BIRTHDAY"][DATE]           = $data_arr[$i]["BIRTHDAY"];
            $data["ZIPCD"][TEXT]              = $data_arr[$i]["ZIPCD"];
            $data["ADDRESS1"][TEXT]           = $data_arr[$i]["ADDRESS1"];
            $data["ADDRESS2"][TEXT]           = $data_arr[$i]["ADDRESS2"];
            $data["ADDRESS3"][TEXT]           = $data_arr[$i]["ADDRESS3"];
            $data["TELNO"][TEXT]              = $data_arr[$i]["TELNO"];
            $data["GTELNO"][TEXT]             = $data_arr[$i]["GTELNO"];
            $data["GTELNO2"][TEXT]            = $data_arr[$i]["GTELNO2"];
            $data["FS_CD"][TEXT]              = $data_arr[$i]["FS_CD"];
            $data["FS_NAME"][TEXT]            = $data_arr[$i]["FS_NAME"];
            $data["FS_TELNO"][TEXT]           = $data_arr[$i]["FS_TELNO"];
            $data["FS_ZIPCD"][TEXT]           = $data_arr[$i]["FS_ZIPCD"];
            $data["FS_ADDR1"][TEXT]           = $data_arr[$i]["FS_ADDR1"];
            $data["FS_DAY"][DATE]             = $data_arr[$i]["FS_DAY"];
            $data["FS_GRDNAME"][TEXT]         = $data_arr[$i]["FS_GRDNAME"];
            $data["GNAME"][TEXT]              = $data_arr[$i]["GNAME"];
            $data["GKANA"][TEXT]              = $data_arr[$i]["GKANA"];
            $data["GZIPCD"][TEXT]             = $data_arr[$i]["GZIPCD"];
            $data["GADDRESS1"][TEXT]          = $data_arr[$i]["GADDRESS1"];
            $data["GADDRESS2"][TEXT]          = $data_arr[$i]["GADDRESS2"];
            $data["GADDRESS3"][TEXT]          = $data_arr[$i]["GADDRESS3"];
            $data["RELATIONSHIP_NAME"][TEXT]  = $data_arr[$i]["RELATIONSHIP_NAME"];
            $data["ENT_MONEY_STATUS"][TEXT]   = $data_arr[$i]["ENT_MONEY_STATUS"];
            $data["PRI_NAME"][TEXT]           = $data_arr[$i]["PRI_NAME"];
            $data["PRI_CLASSNAME"][TEXT]      = $data_arr[$i]["PRI_CLASSNAME"];
            $data["REMARK1"][TEXT]            = $data_arr[$i]["REMARK1"];
            $data["REMARK2"][TEXT]            = $data_arr[$i]["REMARK2"];
            $data["REMARK3"][TEXT]            = $data_arr[$i]["REMARK3"];
            $data["REMARK4"][TEXT]            = $data_arr[$i]["REMARK4"];
            $data["REMARK5"][TEXT]            = $data_arr[$i]["REMARK5"];
            $data["REMARK6"][TEXT]            = $data_arr[$i]["REMARK6"];
            $data["REMARK7"][TEXT]            = $data_arr[$i]["REMARK7"];
            $data["REMARK8"][TEXT]            = $data_arr[$i]["REMARK8"];
            $data["REMARK9"][TEXT]            = $data_arr[$i]["REMARK9"];
            $data["REMARK10"][TEXT]           = $data_arr[$i]["REMARK10"];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_CKIP_CSV_DAT");
            $db->query($query);

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    public function getCkipCsv($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= "   , L007.NAMECD2 AS ERACD ";
        $query .= "   , YEAR(BIRTHDAY) - INT(L007.NAMESPARE1) + 1 AS BIRTH_Y ";
        $query .= "   , RIGHT('00' || RTRIM(CAST(MONTH(BIRTHDAY) AS CHAR(2))), 2) AS BIRTH_M ";
        $query .= "   , RIGHT('00' || RTRIM(CAST(DAY(BIRTHDAY) AS CHAR(2))), 2)  AS BIRTH_D ";
        $query .= "   , YEAR(FS_DAY) AS FS_GRDYEAR ";
        $query .= "   , L007_FS.NAMECD2 AS FS_ERACD ";
        $query .= "   , YEAR(FS_DAY) - INT(L007_FS.NAMESPARE1) + 1 AS FS_Y ";
        $query .= "   , RIGHT('00' || RTRIM(CAST(MONTH(FS_DAY) AS CHAR(2))), 2) AS FS_M ";
        $query .= "   , RIGHT('00' || RTRIM(CAST(DAY(FS_DAY) AS CHAR(2))), 2)  AS FS_D ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CKIP_CSV_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L007 ON L007.NAMECD1 = 'L007' ";
        $query .= "         AND T1.BIRTHDAY BETWEEN DATE(REPLACE(L007.NAMESPARE2, '/', '-')) AND DATE(REPLACE(L007.NAMESPARE3, '/', '-')) ";
        $query .= "     LEFT JOIN NAME_MST L007_FS ON L007_FS.NAMECD1 = 'L007' ";
        $query .= "         AND T1.FS_DAY BETWEEN DATE(REPLACE(L007_FS.NAMESPARE2, '/', '-')) AND DATE(REPLACE(L007_FS.NAMESPARE3, '/', '-'))  ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR           = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV          = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND substr(T1.TESTDIV, 1, 1) = '{$model->field["TESTDIV"]}' ";

        return $query;
    }

    //カウント取得
    public function getCntData($table, $year, $applicantDiv, $examNo, $seq = '')
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     {$table} ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR   = '{$year}' ";
        $query .= "     AND APPLICANTDIV  = '{$applicantDiv}' ";
        $query .= "     AND EXAMNO        = '{$examNo}' ";
        if ($seq != '') {
            $query .= " AND SEQ           = '{$seq}' ";
        }

        return $query;
    }

    //更新
    public function updateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl016rQuery::getNameMst($model, 'H201');
        $result = $db->query($query);
        $h201 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $h201[$row["NAME1"]] = $row["VALUE"];
        }

        $query = knjl016rQuery::getCkipCsv($model);
        $result = $db->query($query);
        while ($rowCkip = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rowCkip["LOGIN_ID"] = sprintf("%05d", substr($rowCkip["LOGIN_ID"], 0, 5));

            //件数取得
            $baseCnt = $db->getOne(knjl016rQuery::getCntData('ENTEXAM_APPLICANTBASE_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["LOGIN_ID"]));
            $addrCnt = $db->getOne(knjl016rQuery::getCntData('ENTEXAM_APPLICANTADDR_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["LOGIN_ID"]));
            $s005Cnt = $db->getOne(knjl016rQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["LOGIN_ID"], '005'));
            $s010Cnt = $db->getOne(knjl016rQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["LOGIN_ID"], '010'));
            $s012Cnt = $db->getOne(knjl016rQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["LOGIN_ID"], '012'));
            $s013Cnt = $db->getOne(knjl016rQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["LOGIN_ID"], '013'));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]        = $rowCkip["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]       = $rowCkip["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]             = $rowCkip["LOGIN_ID"];
            $data["TESTDIV"][TEXT]            = '0';
            $data["SHDIV"][TEXT]              = '0';
            $data["DESIREDIV"][TEXT]          = substr($rowCkip["DESIREDIV"], 0, 1);
            $data["NAME"][TEXT]               = $rowCkip["NAME"];
            $data["NAME_KANA"][TEXT]          = $rowCkip["NAME_KANA"];
            $data["SEX"][TEXT]                = $rowCkip["SEX"];
            if ($rowCkip["BIRTHDAY"]) {
                $data["ERACD"][VARCHAR]       = $rowCkip["ERACD"];
                $data["BIRTH_Y"][VARCHAR]     = $rowCkip["BIRTH_Y"];
                $data["BIRTH_M"][VARCHAR]     = $rowCkip["BIRTH_M"];
                $data["BIRTH_D"][VARCHAR]     = $rowCkip["BIRTH_D"];
            }
            $data["BIRTHDAY"][DATE]           = $rowCkip["BIRTHDAY"];
            $data["FS_CD"][TEXT]              = $rowCkip["FS_CD"] == "" ? "" : (strlen($rowCkip["FS_CD"]) >= 6 ? substr("2".substr($rowCkip["FS_CD"], -6), -7) : "2".str_repeat("0", 6-strlen($rowCkip["FS_CD"])).$rowCkip["FS_CD"]);
            if ($rowCkip["FS_DAY"]) {
                $data["FS_GRDYEAR"][DATE]     = $rowCkip["FS_GRDYEAR"];
                $data["FS_ERACD"][DATE]       = $rowCkip["FS_ERACD"];
                $data["FS_Y"][DATE]           = $rowCkip["FS_Y"];
                $data["FS_M"][DATE]           = $rowCkip["FS_M"];
            }
            $data["FS_DAY"][DATE]             = $rowCkip["FS_DAY"];
            $data["FS_GRDDIV"][TEXT]          = $rowCkip["FS_GRDNAME"] == '卒' ? '1' : '2';
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "SYSDATE()";

            if ($baseCnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."'";
                $where .= "   AND EXAMNO        = '".$rowCkip["LOGIN_ID"]."'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $rowCkip["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $rowCkip["LOGIN_ID"];
            $data["ZIPCD"][TEXT]            = $rowCkip["ZIPCD"];
            $data["ADDRESS1"][TEXT]         = $rowCkip["ADDRESS1"].$rowCkip["ADDRESS2"];
            $data["ADDRESS2"][TEXT]         = $rowCkip["ADDRESS3"];
            $data["TELNO"][TEXT]            = $rowCkip["TELNO"];
            $data["GNAME"][TEXT]            = $rowCkip["GNAME"];
            $data["GKANA"][TEXT]            = $rowCkip["GKANA"];
            $data["GZIPCD"][TEXT]           = $rowCkip["GZIPCD"];
            $data["GADDRESS1"][TEXT]        = $rowCkip["GADDRESS1"].$rowCkip["GADDRESS2"];
            $data["GADDRESS2"][TEXT]        = $rowCkip["GADDRESS3"];
            $data["GTELNO"][TEXT]           = $rowCkip["GTELNO"];
            $data["EMERGENCYTELNO"][TEXT]   = $rowCkip["GTELNO2"];
            $data["RELATIONSHIP"][TEXT]     = $h201[$rowCkip["RELATIONSHIP_NAME"]];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if ($addrCnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."'";
                $where .= "   AND EXAMNO        = '".$rowCkip["LOGIN_ID"]."'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT(005)
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $rowCkip["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $rowCkip["LOGIN_ID"];
            $data["SEQ"][TEXT]              = '005';
            $data["REMARK1"][TEXT]          = $rowCkip["FS_NAME"];
            $data["REMARK2"][TEXT]          = $rowCkip["FS_TELNO"];
            $data["REMARK5"][TEXT]          = $rowCkip["ENT_MONEY_STATUS"];

            if ($s005Cnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."'";
                $where .= "   AND EXAMNO        = '".$rowCkip["LOGIN_ID"]."'";
                $where .= "   AND SEQ           = '005'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            $chkTestDiv = substr($rowCkip["TESTDIV"], 0, 1);

            //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT(010)
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $rowCkip["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $rowCkip["LOGIN_ID"];
            $data["SEQ"][TEXT]              = '010';
            if ($chkTestDiv == '1') {
                $data["REMARK1"][TEXT]          = $chkTestDiv;
            } elseif ($chkTestDiv == '2') {
                $data["REMARK2"][TEXT]          = $chkTestDiv;
            }

            if ($s010Cnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."'";
                $where .= "   AND EXAMNO        = '".$rowCkip["LOGIN_ID"]."'";
                $where .= "   AND SEQ           = '010'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT(012)
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $rowCkip["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $rowCkip["LOGIN_ID"];
            $data["SEQ"][TEXT]              = '012';
            if ($chkTestDiv == '1') {
                $data["REMARK1"][TEXT]          = $rowCkip["EXAMNO"];
            } elseif ($chkTestDiv == '2') {
                $data["REMARK2"][TEXT]          = $rowCkip["EXAMNO"];
            }

            if ($s012Cnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."'";
                $where .= "   AND EXAMNO        = '".$rowCkip["LOGIN_ID"]."'";
                $where .= "   AND SEQ           = '012'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT(013)
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $rowCkip["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $rowCkip["LOGIN_ID"];
            $data["SEQ"][TEXT]              = '013';
            $setSHdiv = ($rowCkip["SHDIV"] == '0') ? '2': $rowCkip["SHDIV"];
            if ($chkTestDiv == '1') {
                $data["REMARK1"][TEXT]          = $setSHdiv;
            } elseif ($chkTestDiv == '2') {
                $data["REMARK2"][TEXT]          = $setSHdiv;
            }

            if ($s013Cnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."'";
                $where .= "   AND EXAMNO        = '".$rowCkip["LOGIN_ID"]."'";
                $where .= "   AND SEQ           = '013'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT", $where);
            }
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }
}
