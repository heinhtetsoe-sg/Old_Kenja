<?php

require_once('for_php7.php');

class knjl014equery extends Query {

    function getNameMst($model, $namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //願書の追加（ＣＳＶデータより読込）
    function updateQueryCsv($model, &$data_arr) {
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

    function getCkipCsv($model) {

        if ($model->field["TESTDIV"] == '1') {
            $nameSpare1 = "'1'";
        } else if ($model->field["TESTDIV"] == '2') {
            $nameSpare1 = "'2', '3'";
        } else if ($model->field["TESTDIV"] == '3') {
            $nameSpare1 = "'4'";
        }

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
        $query .= " LEFT JOIN NAME_MST L007 ON L007.NAMECD1 = 'L007' ";
        $query .= "  AND T1.BIRTHDAY BETWEEN DATE(REPLACE(L007.NAMESPARE2, '/', '-')) AND DATE(REPLACE(L007.NAMESPARE3, '/', '-')) ";
        $query .= " LEFT JOIN NAME_MST L007_FS ON L007_FS.NAMECD1 = 'L007' ";
        $query .= "  AND T1.FS_DAY BETWEEN DATE(REPLACE(L007_FS.NAMESPARE2, '/', '-')) AND DATE(REPLACE(L007_FS.NAMESPARE3, '/', '-'))  ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV IN ( ";
        $query .= "                        SELECT ";
        $query .= "                            NAMECD2 ";
        $query .= "                        FROM ";
        $query .= "                            V_NAME_MST ";
        $query .= "                        WHERE ";
        $query .= "                                YEAR       = '{$model->ObjYear}' ";
        $query .= "                            AND NAMECD1    = 'L004' ";
        $query .= "                            AND value(NAMESPARE1, '0') IN ({$nameSpare1}) ";
        $query .= "                        ) ";

        return $query;
    }

    //カウント取得
    function getCntData($table, $year, $applicantDiv, $examNo, $seq='') {
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
    function updateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl014eQuery::getNameMst($model, 'H201');
        $result = $db->query($query);
        $h201 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $h201[$row["NAME1"]] = $row["VALUE"];
        }

        $query = knjl014eQuery::getCkipCsv($model);
        $result = $db->query($query);
        while ($rowCkip = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //件数取得
            $baseCnt = $db->getOne(knjl014eQuery::getCntData('ENTEXAM_APPLICANTBASE_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["EXAMNO"]));
            $addrCnt = $db->getOne(knjl014eQuery::getCntData('ENTEXAM_APPLICANTADDR_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["EXAMNO"]));
            $s002Cnt = $db->getOne(knjl014eQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["EXAMNO"], '002'));
            $s005Cnt = $db->getOne(knjl014eQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["EXAMNO"], '005'));
            $s033Cnt = $db->getOne(knjl014eQuery::getCntData('ENTEXAM_APPLICANTBASE_DETAIL_DAT', $rowCkip["ENTEXAMYEAR"], $rowCkip["APPLICANTDIV"], $rowCkip["EXAMNO"], '033'));

            //ENTEXAM_APPLICANTBASE_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]        = $rowCkip["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]       = $rowCkip["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]             = $rowCkip["EXAMNO"];
            $data["TESTDIV"][TEXT]            = $rowCkip["TESTDIV"];
            $data["TESTDIV1"][TEXT]           = $rowCkip["TESTDIV1"];
            $data["SHDIV"][TEXT]              = $rowCkip["SHDIV"];
            $data["DESIREDIV"][TEXT]          = $rowCkip["DESIREDIV"];
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
            $data["FS_CD"][TEXT]              = substr('0000000'.$rowCkip["FS_CD"], -7);
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
                $where .= "   AND EXAMNO        = '".$rowCkip["EXAMNO"]."'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTADDR_DAT
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $rowCkip["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $rowCkip["EXAMNO"];
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
            $data["GTELNO2"][TEXT]          = $rowCkip["GTELNO2"];
            $data["RELATIONSHIP"][TEXT]     = $h201[$rowCkip["RELATIONSHIP_NAME"]];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if ($addrCnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."'";
                $where .= "   AND EXAMNO        = '".$rowCkip["EXAMNO"]."'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            }
            $db->query($query);

            //ENTEXAM_APPLICANTBASE_DETAIL_DAT(002)
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $rowCkip["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $rowCkip["EXAMNO"];
            $data["SEQ"][TEXT]              = '002';
            $data["REMARK1"][TEXT]          = $rowCkip["LOGIN_ID"];

            if ($s002Cnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."'";
                $where .= "   AND EXAMNO        = '".$rowCkip["EXAMNO"]."'";
                $where .= "   AND SEQ           = '002'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $rowCkip["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $rowCkip["EXAMNO"];
            $data["SEQ"][TEXT]              = '005';
            $data["REMARK1"][TEXT]          = $rowCkip["FS_NAME"];
            $data["REMARK2"][TEXT]          = $rowCkip["FS_TELNO"];
            $data["REMARK5"][TEXT]          = $rowCkip["ENT_MONEY_STATUS"];
            $data["REMARK6"][TEXT]          = $rowCkip["REMARK4"];

            if ($s005Cnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."'";
                $where .= "   AND EXAMNO        = '".$rowCkip["EXAMNO"]."'";
                $where .= "   AND SEQ           = '005'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $rowCkip["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $rowCkip["EXAMNO"];
            $data["SEQ"][TEXT]              = '033';
            $data["REMARK1"][TEXT]          = $rowCkip["REMARK1"];
            $data["REMARK2"][TEXT]          = $rowCkip["REMARK2"];
            $data["REMARK3"][TEXT]          = $rowCkip["REMARK3"];

            if ($s033Cnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR   = '".$rowCkip["ENTEXAMYEAR"]."' ";
                $where .= "   AND APPLICANTDIV  = '".$rowCkip["APPLICANTDIV"]."'";
                $where .= "   AND EXAMNO        = '".$rowCkip["EXAMNO"]."'";
                $where .= "   AND SEQ           = '033'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);

        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

}
?>
