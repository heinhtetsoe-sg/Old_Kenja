<?php

require_once('for_php7.php');

class knjl111dQuery extends Query {
    //名称マスタよりデータを取得
    function get_name_cd($year, $namecd1, $sitei = "") {
        $query  = " SELECT ";
        if ($namecd1 == "L004") {
            $query .= "     NAME1 AS LABEL, ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        }
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        if ($namecd1 == "L004") {
            $query .= "     int(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //MAX_TESTDIV取得
    function getMaxTestDiv($model) {
        $query  = " SELECT ";
        $query .= "     max(TESTDIV) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= " GROUP BY ";
        $query .= "     ENTEXAMYEAR ";

        return $query;
    }

    //転籍フラグ取得
    function getTrabsferedFlg($model) {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'L058' ";
        $query .= "     AND NAMECD2 = '".$model->field["DESIREDIV"]."' ";

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

        $query  = "";
        $query .= " SELECT ";
        $query .= "     BASE.APPLICANTDIV, ";
        $query .= "     BASE.DESIREDIV, ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     SEQ033.REMARK7 AS VISIT_NO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.SEX, ";
        $query .= "     BASE.ERACD, ";
        $query .= "     BASE.BIRTH_Y, ";
        $query .= "     BASE.BIRTH_M, ";
        $query .= "     BASE.BIRTH_D, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     BASE.FS_ERACD, ";
        $query .= "     BASE.FS_Y, ";
        $query .= "     BASE.FS_M, ";
        $query .= "     BASE.FS_GRDDIV, ";
        for ($i = 1; $i <= 9; $i++) {
            $query .= "     SEQ033.REMARK{$i} AS REMARK{$i}_033, ";
        }
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GKANA, ";
        $query .= "     ADDR.RELATIONSHIP, ";
        $query .= "     ADDR.GZIPCD, ";
        $query .= "     ADDR.GADDRESS1, ";
        $query .= "     ADDR.GADDRESS2, ";
        $query .= "     ADDR.GTELNO, ";
        $query .= "     ADDR.GTELNO2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "          ON ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ033 ";
        $query .= "          ON SEQ033.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ033.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ033.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ033.SEQ          = '033' ";
        $query .= "     LEFT JOIN NAME_MST L007 ON L007.NAMECD1 = 'L007' ";
        $query .= "                            AND L007.NAMECD2 = BASE.FS_ERACD ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND BASE.DESIREDIV    = '".$model->desirediv."' ";
        $query .= "     AND BASE.TESTDIV      = '".$model->testdiv."' ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= "     AND BASE.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= "     AND BASE.EXAMNO > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO ASC ";
        } else {
            $query.= "     AND BASE.EXAMNO = '".$model->examno."'";
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

    //追加・更新
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->applicantdiv;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["DESIREDIV"][TEXT]     = $model->field["DESIREDIV"];
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["SHDIV"][TEXT]         = $model->shdiv;
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTH_Y"][TEXT]       = $model->field["BIRTH_Y"];
        $data["BIRTH_M"][TEXT]       = $model->field["BIRTH_M"];
        $data["BIRTH_D"][TEXT]       = $model->field["BIRTH_D"];
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"])) ? sprintf("%07s",$model->field["FS_CD"]) : "";
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = sprintf("%02d", $model->field["FS_Y"]);
        $data["FS_M"][TEXT]          = sprintf("%02d", $model->field["FS_M"]);
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        if ($model->cmd == "add") {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO       = '".$model->examno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        //ENTEXAM_APPLICANTADDR_DAT
        //DELETE
        if ($model->cmd != "add") {
            $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
            $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
            $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
            $query .= "   AND EXAMNO       = '".$model->examno."' ";
            $db->query($query);
        }
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->applicantdiv;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["GTELNO2"][TEXT]       = $model->field["GTELNO2"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        //cnt
        $getCount033 = $db->getOne(knjl111dQuery::getCountDetail($model, "033"));
        //where
        $getWhere033 = knjl111dQuery::getWhereDetail($model, "033");

        //事前相談番号等
        $data = array();
        $data["REMARK1"][TEXT]              = $model->transferedFlg;        //転籍フラグ
        $data["REMARK2"][TEXT]              = $model->field["REMARK2_033"]; //出身学校コード（高校）
        $data["REMARK3"][TEXT]              = $model->field["REMARK3_033"]; //課程
        $data["REMARK4"][TEXT]              = $model->field["REMARK4_033"]; //学科
        $data["REMARK5"][TEXT]              = $model->field["REMARK5_033"]; //学年
        $data["REMARK6"][TEXT]              = $model->field["REMARK6_033"]; //状況
        $data["REMARK8"][TEXT]              = $model->field["REMARK8_033"]; //転編入学照会日
        $data["REMARK9"][TEXT]              = $model->field["REMARK9_033"]; //調査書照会日
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount033 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "033";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere033);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    function getCountDetail($model, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }

    function getWhereDetail($model, $seq) {
        $query  = " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->applicantdiv}' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }

    //--- DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

/*
        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);
*/

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV      = '".$model->testdiv."'";
        $query .= "   AND EXAM_TYPE    = '1'";
        $query .= "   AND RECEPTNO     = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>