<?php

require_once('for_php7.php');

class knjl011dQuery extends Query {
    //名称マスタよりデータを取得
    function get_name_cd($year, $namecd1, $sitei = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
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

        $query  = "";
        $query .= " SELECT ";
        $query .= "     BASE.APPLICANTDIV, ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.SHDIV, ";
        $query .= "     BASE.SEX, ";
        $query .= "     BASE.ERACD, ";
        $query .= "     BASE.BIRTH_Y, ";
        $query .= "     BASE.BIRTH_M, ";
        $query .= "     BASE.BIRTH_D, ";
        $query .= "     DECI.DECISION_NAME AS SUB_ORDER, ";
        $query .= "     CLUB.CLUBNAME AS CLUB_NAME, ";
        $query .= "     SEQ015.REMARK1 AS CHILD, ";
        $query .= "     BASE.DESIREDIV, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     BASE.FS_GRDYEAR, ";
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
        $query .= "     BASE.REMARK1, ";
        $query .= "     BASE.REMARK2, ";
        $query .= "     SEQ008.REMARK1 AS PRISCHOOLCD1, ";
        $query .= "     SEQ008.REMARK3 AS PRISCHOOL_CLASS_CD1, ";
        $query .= "     SEQ008.REMARK4 AS PRISCHOOLCD2, ";
        $query .= "     SEQ008.REMARK5 AS PRISCHOOL_CLASS_CD2 ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "          ON ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ015 ";
        $query .= "          ON SEQ015.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ015.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ015.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ015.SEQ          = '015' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ019 ";
        $query .= "          ON SEQ019.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ019.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ019.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ019.SEQ          = '019' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ008 ";
        $query .= "          ON SEQ008.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ008.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ008.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ008.SEQ          = '008' ";
        $query .= "     LEFT JOIN CLUB_MST CLUB ";
        $query .= "          ON CLUB.SCHOOLCD    = SEQ019.REMARK1 ";
        $query .= "         AND CLUB.SCHOOL_KIND = SEQ019.REMARK2 ";
        $query .= "         AND CLUB.CLUBCD      = SEQ019.REMARK3 ";
        $query .= "     LEFT JOIN ENTEXAM_INTERNAL_DECISION_MST DECI ON DECI.DECISION_CD = BASE.SUB_ORDER ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND BASE.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND VALUE(BASE.JUDGEMENT,'0') != '5' "; //受験不可

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
        $query  = " SELECT  T1.FINSCHOOL_NAME, VALUE(L3.NAME1, '') AS FINSCHOOL_DISTCD_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST L3 ON  L3.NAMECD1 = 'L001' ";
        $query .= "                               AND L3.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }
    //出身学校所在地
    function getFinLocationMst($cd) {
        $query  = " SELECT ";
        $query .= "     DISTRICT_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_LOCATION_MST ";
        $query .= " WHERE ";
        $query .= "     DISTRICTCD = '{$cd}' ";
        return $query;
    }

    //塾名
    function getPriSchoolName($priSchoolCd) {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";
        return $query;
    }
    //教室名
    function getPriSchoolClassName($priSchoolCd, $priSchoolClassCd) {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "         PRISCHOOLCD        = '{$priSchoolCd}' ";
        $query .= "     AND PRISCHOOL_CLASS_CD = '{$priSchoolClassCd}' ";
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
        $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
        $data["SHDIV"][TEXT]         = $model->field["SHDIV"];
        $data["DESIREDIV"][TEXT]     = $model->field["DESIREDIV"];
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $model->field["ERACD"];
        $data["BIRTH_Y"][TEXT]       = $model->field["BIRTH_Y"];
        $data["BIRTH_M"][TEXT]       = $model->field["BIRTH_M"];
        $data["BIRTH_D"][TEXT]       = $model->field["BIRTH_D"];

        $data["BIRTHDAY"][TEXT]      = "";
        $year = common::getDateHenkan($model->field["ERACD"].sprintf("%02s", $model->field["BIRTH_Y"]), "wy");
        if (checkdate((int)$model->field["BIRTH_M"], (int)$model->field["BIRTH_D"], (int)$year)) {
            $data["BIRTHDAY"][TEXT]       = $year."-".sprintf("%02s", $model->field["BIRTH_M"])."-".sprintf("%02s", $model->field["BIRTH_D"]);
        }
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"])) ? sprintf("%07s",$model->field["FS_CD"]) : "";
        $data["FS_GRDYEAR"][TEXT]    = $model->field["FS_GRDYEAR"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["SLIDE_FLG"][TEXT]     = $model->field["SLIDE_FLG"];
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
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        //cnt
        $getCount008 = $db->getOne(knjl011dQuery::getCountDetail($model, "008"));
        //where
        $getWhere008 = knjl011dQuery::getWhereDetail($model, "008");

        //塾・教室(PRISCHOOL_MST、PRISCHOOL_CLASS_MST)
        $data = array();
        $data["REMARK1"][TEXT]              = $model->field["PRISCHOOLCD1"];
        $data["REMARK3"][TEXT]              = $model->field["PRISCHOOL_CLASS_CD1"];
        $data["REMARK4"][TEXT]              = $model->field["PRISCHOOLCD2"];
        $data["REMARK5"][TEXT]              = $model->field["PRISCHOOL_CLASS_CD2"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount008 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "008";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere008);
        }
        $db->query($query);

        //学園子女
        if ($model->field["CHILD"]) {
            //cnt
            $getCount015 = $db->getOne(knjl011dQuery::getCountDetail($model, "015"));
            //where
            $getWhere015 = knjl011dQuery::getWhereDetail($model, "015");

            $data = array();
            $data["REMARK1"][TEXT]              = $model->field["CHILD"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            if ($getCount015 == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = "015";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere015);
            }
            $db->query($query);
        }

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

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND EXAMNO       = '".$model->examno."'";
        $db->query($query);

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