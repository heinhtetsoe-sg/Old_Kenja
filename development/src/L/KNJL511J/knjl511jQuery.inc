<?php

require_once('for_php7.php');

class knjl511jQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1, $nameCd2="") {
        $query  = " SELECT ";
        if ($nameCd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //入試種別取得
    function getTestDiv($model) {
        $query  = " SELECT ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     TESTDIV || ':' || TESTDIV_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより和暦の元号を取得
    function get_calendarno($year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L007' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     BASE.APPLICANTDIV, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     BASE.REMARK1, ";
        $query .= "     BASE.REMARK2, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GKANA, ";
        $query .= "     ADDR.GTELNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RCPT.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                             AND RCPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                             AND RCPT.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "                                             AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                                             AND ADDR.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST L007 ON L007.NAMECD1 = 'L007' ";
        $query .= "                            AND L007.NAMECD2 = BASE.FS_ERACD ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND RCPT.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND RCPT.TESTDIV      = '{$model->testdiv}' ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= "     AND RCPT.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     RCPT.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= "     AND RCPT.EXAMNO > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     RCPT.EXAMNO ASC ";
        } else {
            $query.= "     AND RCPT.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //出身学校マスタよりデータを取得
    function getFinschoolName($cd) {
        $query  = " SELECT  T1.FINSCHOOL_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

/***** 追加更新処理 *****/
    //存在チェック
    function getExistdataCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(EXAMNO) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND EXAMNO       = '".$model->examno."' ";
        return $query;
    }

    //更新
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->applicantdiv;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = "1";// 1固定
        $data["SHDIV"][TEXT]         = "1";// 1固定
        $data["DESIREDIV"][TEXT]     = "1";// 1固定
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["BIRTHDAY"][TEXT]      = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"])) ? sprintf("%07s",$model->field["FS_CD"]) : "";
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $where .= "   AND EXAMNO       = '".$model->examno."' ";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        //DELETE
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->applicantdiv;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>