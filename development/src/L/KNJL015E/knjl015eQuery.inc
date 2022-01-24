<?php

require_once('for_php7.php');

class knjl015eQuery extends Query {

    function getNameMst($model, $namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getCkipCsv($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CKIP_CSV_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";

        return $query;
    }

    //更新
    function updateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjl015eQuery::getNameMst($model, 'H201');
        $result = $db->query($query);
        $h201 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $h201[$row["NAME1"]] = $row["VALUE"];
        }

        $query = knjl015eQuery::getCkipCsv($model);
        $result = $db->query($query);
        while ($rowCkip = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$rowCkip["ENTEXAMYEAR"]}' ";
            $query .= "     AND APPLICANTDIV = '{$rowCkip["APPLICANTDIV"]}' ";
            $query .= "     AND EXAMNO = '{$rowCkip["EXAMNO"]}' ";
            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$rowCkip["ENTEXAMYEAR"]}' ";
            $query .= "     AND APPLICANTDIV = '{$rowCkip["APPLICANTDIV"]}' ";
            $query .= "     AND EXAMNO = '{$rowCkip["EXAMNO"]}' ";
            $db->query($query);

            $query  = " DELETE FROM ";
            $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '{$rowCkip["ENTEXAMYEAR"]}' ";
            $query .= "     AND APPLICANTDIV = '{$rowCkip["APPLICANTDIV"]}' ";
            $query .= "     AND EXAMNO = '{$rowCkip["EXAMNO"]}' ";
            $db->query($query);

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
            $data["BIRTHDAY"][DATE]           = $rowCkip["BIRTHDAY"];
            $data["FS_CD"][TEXT]              = substr($rowCkip["FS_CD"], -7);
            $data["FS_DAY"][DATE]             = $rowCkip["FS_DAY"];
            $data["FS_GRDDIV"][TEXT]          = $rowCkip["FS_GRDNAME"] == '卒' ? '1' : '2';
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][NUMBER]          = "SYSDATE()";

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);

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

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $rowCkip["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $rowCkip["EXAMNO"];
            $data["SEQ"][TEXT]              = '002';
            $data["REMARK1"][TEXT]          = $rowCkip["LOGIN_ID"];

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
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

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);

            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $rowCkip["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $rowCkip["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $rowCkip["EXAMNO"];
            $data["SEQ"][TEXT]              = '033';
            $data["REMARK1"][TEXT]          = $rowCkip["REMARK1"];
            $data["REMARK2"][TEXT]          = $rowCkip["REMARK2"];
            $data["REMARK3"][TEXT]          = $rowCkip["REMARK3"];

            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);

        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

}
?>
