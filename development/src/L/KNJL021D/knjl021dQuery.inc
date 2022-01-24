<?php

require_once('for_php7.php');

class knjl021dQuery extends Query {
    //名称マスタよりデータを取得
    function get_name_cd($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.SHDIV || ':' || L006.NAME1 AS SHDIV, ";
        $query .= "     L025.NAME1 AS JUDGE_KIND, ";
        $query .= "     CLUB.CLUBNAME AS CLUBNAME, ";
        $query .= "     BASE.SEX || ':' || Z002.ABBV1 AS SEX, ";
        $query .= "     L007.NAME1 || ' ' || BASE.BIRTH_Y || ' 年 ' || BASE.BIRTH_M || ' 月 ' || BASE.BIRTH_D || ' 日生' AS BIRTHDAY, ";
        $query .= "     BASE.DESIREDIV || ':' || L058.NAME1 AS DESIREDIV, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     FINS.FINSCHOOL_NAME_ABBV AS FINSCHOOLNAME, ";
        $query .= "     BASE.FS_GRDYEAR, ";
        //教科(1,2年)
        for ($i = 1; $i <= 11; $i++) {
            $num = sprintf("%02d", $i);
            $query .= "     SEQ001.REMARK{$i} AS CONF1_RPT{$num}, ";
            $query .= "     SEQ002.REMARK{$i} AS CONF2_RPT{$num}, ";
        }
        $query .= "     SEQ003.REMARK1 AS DE003REMARK1, ";//行動の記録
        $query .= "     SEQ003.REMARK2 AS DE003REMARK2, ";//英検
        $query .= "     SEQ003.REMARK3 AS DE003REMARK3, ";//数検
        $query .= "     SEQ003.REMARK4 AS DE003REMARK4, ";//漢検
        $query .= "     SEQ003.REMARK5 AS DE003REMARK5, ";//特別活動の記録
        $query .= "     L3.*, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.APPLICANTDIV ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT SEQ001 ";
        $query .= "          ON SEQ001.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ001.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ001.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ001.SEQ          = '001' ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT SEQ002 ";
        $query .= "          ON SEQ002.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ002.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ002.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ002.SEQ          = '002' ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT SEQ003 ";
        $query .= "          ON SEQ003.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ003.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ003.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ003.SEQ          = '003' ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ019 ";
        $query .= "          ON SEQ019.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ019.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ019.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ019.SEQ          = '019' ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DAT L3 ";
        $query .= "          ON L3.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND L3.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND L3.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN  CLUB_MST CLUB ON CLUB.CLUBCD = SEQ019.REMARK3 ";
        $query .= "     LEFT JOIN V_NAME_MST L006 ON L006.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L006.NAMECD1 = 'L006' ";
        $query .= "                              AND L006.NAMECD2 = BASE.SHDIV ";
        $query .= "     LEFT JOIN V_NAME_MST L007 ON L007.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L007.NAMECD1 = 'L007' ";
        $query .= "                              AND L007.NAMECD2 = BASE.ERACD ";
        $query .= "     LEFT JOIN V_NAME_MST L025 ON L025.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L025.NAMECD1 = 'L025' ";
        $query .= "                              AND L025.NAMECD2 = BASE.JUDGE_KIND ";
        $query .= "     LEFT JOIN V_NAME_MST L058 ON L058.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L058.NAMECD1 = 'L058' ";
        $query .= "                              AND L058.NAMECD2 = BASE.DESIREDIV ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ON Z002.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND Z002.NAMECD1 = 'Z002' ";
        $query .= "                              AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON BASE.FS_CD = FINS.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "     AND BASE.TESTDIV      = '".$model->field["TESTDIV"]."'";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= "     AND BASE.EXAMNO < '".$model->examno."' ";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= "     AND BASE.EXAMNO > '".$model->examno."' ";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO ASC ";
        } else {
            $query.= "     AND BASE.EXAMNO = '".$model->examno."' ";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //データカウント(ENTEXAM_APPLICANTCONFRPT_DAT)
    function getCountdata($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "     AND EXAMNO = '".$model->examno."'";

        return $query;
    }

    //データカウント(ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT)
    function getCountDetaildata($model, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "     AND EXAMNO = '".$model->examno."'";
        $query .= "     AND SEQ = '{$seq}' ";
        return $query;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データチェック
        $getCount = $db->getOne(knjl021dQuery::getCountdata($model));
        //ENTEXAM_APPLICANTCONFRPT_DAT ---UPDATE
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]          = $model->year;
            $data["APPLICANTDIV"][TEXT]         = $model->applicantdiv;
            $data["EXAMNO"][TEXT]               = $model->examno;
        }
        $data["CONFIDENTIAL_RPT01"][NUMBER] = $model->field["CONFIDENTIAL_RPT01"];
        $data["CONFIDENTIAL_RPT02"][NUMBER] = $model->field["CONFIDENTIAL_RPT02"];
        $data["CONFIDENTIAL_RPT03"][NUMBER] = $model->field["CONFIDENTIAL_RPT03"];
        $data["CONFIDENTIAL_RPT04"][NUMBER] = $model->field["CONFIDENTIAL_RPT04"];
        $data["CONFIDENTIAL_RPT05"][NUMBER] = $model->field["CONFIDENTIAL_RPT05"];
        $data["CONFIDENTIAL_RPT06"][NUMBER] = $model->field["CONFIDENTIAL_RPT06"];
        $data["CONFIDENTIAL_RPT07"][NUMBER] = $model->field["CONFIDENTIAL_RPT07"];
        $data["CONFIDENTIAL_RPT08"][NUMBER] = $model->field["CONFIDENTIAL_RPT08"];
        $data["CONFIDENTIAL_RPT09"][NUMBER] = $model->field["CONFIDENTIAL_RPT09"];
        $data["TOTAL5"][NUMBER]             = $model->field["TOTAL5"];
        $data["TOTAL_ALL"][NUMBER]          = $model->field["TOTAL_ALL"];
        $data["ABSENCE_DAYS"][NUMBER]       = $model->field["ABSENCE_DAYS"];
        $data["ABSENCE_DAYS2"][NUMBER]      = $model->field["ABSENCE_DAYS2"];
        $data["ABSENCE_DAYS3"][NUMBER]      = $model->field["ABSENCE_DAYS3"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO       = '".$model->examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
        //教科(1年)
        $getCount = $db->getOne(knjl021dQuery::getCountDetaildata($model, "001"));
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "001";
        }
        for ($i = 1; $i <= 11; $i++) {
            $num = sprintf("%02d", $i);
            $data["REMARK{$i}"][TEXT]   = $model->field["CONF1_RPT{$num}"];
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO       = '".$model->examno."'";
            $where .= "   AND SEQ          = '001' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);
        //教科(2年)
        $getCount = $db->getOne(knjl021dQuery::getCountDetaildata($model, "002"));
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "002";
        }
        for ($i = 1; $i <= 11; $i++) {
            $num = sprintf("%02d", $i);
            $data["REMARK{$i}"][TEXT]   = $model->field["CONF2_RPT{$num}"];
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO       = '".$model->examno."'";
            $where .= "   AND SEQ          = '002' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);

        //各項目
        $getCount = $db->getOne(knjl021dQuery::getCountDetaildata($model, "003"));
        $data = array();
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = "003";
        }
        $data["REMARK1"][TEXT]      = $model->field["DE003REMARK1"];//行動の記録
        $data["REMARK2"][TEXT]      = $model->field["DE003REMARK2"];//英検
        $data["REMARK3"][TEXT]      = $model->field["DE003REMARK3"];//数検
        $data["REMARK4"][TEXT]      = $model->field["DE003REMARK4"];//漢検
        $data["REMARK5"][TEXT]      = $model->field["DE003REMARK5"];//特別活動の記録
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO       = '".$model->examno."'";
            $where .= "   AND SEQ          = '003' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>