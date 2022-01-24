<?php

require_once('for_php7.php');

class knjl021pQuery extends Query 
{

    //名称マスタよりデータを取得
    function get_name_cd($year, $namecd1, $sitei = "") {
        $query  = " SELECT ";
        if ($namecd1 !== 'L008') {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        } else {
            $query .= "     ABBV1, ";
            $query .= "     NAMECD2 AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        if ($namecd1 == "L003") {
//            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }

        return $query;
    }


    //メインデータ取得
    function get_edit_data($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.NAME, ";
        for ($i = 1; $i <= 9; $i++) {
            $num = sprintf("%02d", $i);
            $query .= "     L1.REMARK{$i} AS CONF1_RPT{$num}, ";
            $query .= "     L2.REMARK{$i} AS CONF2_RPT{$num}, ";
            $query .= "     L0.CONFIDENTIAL_RPT{$num} AS CONF3_RPT{$num}, ";
        }
        $query .= "     L4.REMARK1 AS CLASS_JININ, ";
        $query .= "     L4.REMARK2 AS JUNI, ";
        $query .= "     L4.REMARK3 AS SHOKEN_KENKOU, ";
        $query .= "     L4.REMARK4 AS SHOKEN_SOUGOU, ";
        $query .= "     L4.REMARK5 AS SHOKEN_SONOTA, ";
        $query .= "     L5.REMARK1 AS SHOKEN_TOKUBETU1, ";
        $query .= "     L5.REMARK2 AS SHOKEN_TOKUBETU2, ";
        $query .= "     L5.REMARK3 AS SHOKEN_TOKUBETU3, ";
        $query .= "     L5.REMARK4 AS SHOKEN_SOUGAKU1, ";
        $query .= "     L5.REMARK5 AS SHOKEN_SOUGAKU2, ";
        $query .= "     L5.REMARK6 AS SHOKEN_SOUGAKU3, ";
        $query .= "     L6.REMARK1 AS ATTEND_SHUSSEKI_SUBEKI1, ";
        $query .= "     L6.REMARK2 AS ATTEND_KESSEKI1, ";
        $query .= "     L6.REMARK3 AS ATTEND_TIKOKU1, ";
        $query .= "     L6.REMARK4 AS ATTEND_SOUTAI1, ";
        $query .= "     L6.REMARK5 AS ATTEND_RIYUU1, ";
        $query .= "     L7.REMARK1 AS ATTEND_SHUSSEKI_SUBEKI2, ";
        $query .= "     L7.REMARK2 AS ATTEND_KESSEKI2, ";
        $query .= "     L7.REMARK3 AS ATTEND_TIKOKU2, ";
        $query .= "     L7.REMARK4 AS ATTEND_SOUTAI2, ";
        $query .= "     L7.REMARK5 AS ATTEND_RIYUU2, ";
        $query .= "     L8.REMARK1 AS ATTEND_SHUSSEKI_SUBEKI3, ";
        $query .= "     L8.REMARK2 AS ATTEND_KESSEKI3, ";
        $query .= "     L8.REMARK3 AS ATTEND_TIKOKU3, ";
        $query .= "     L8.REMARK4 AS ATTEND_SOUTAI3, ";
        $query .= "     L8.REMARK5 AS ATTEND_RIYUU3, ";
        $query .= "     L0.REMARK1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DAT L0 ";
        $query .= "          ON L0.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L0.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L0.EXAMNO = T1.EXAMNO ";
        for ($i = 1; $i <= 8; $i++) {
            if ($i == 3) continue;
            $seq = sprintf("%03d", $i);
            $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT L{$i} ";
            $query .= "          ON L{$i}.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "         AND L{$i}.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "         AND L{$i}.EXAMNO = T1.EXAMNO ";
            $query .= "         AND L{$i}.SEQ = '{$seq}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."'";
        $query .= "     AND T1.EXAMNO = '".$model->examno."'";

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

        //ENTEXAM_APPLICANTCONFRPT_DAT
        //cnt
        $getCount = $db->getOne(knjl021pQuery::getCountdata($model));
        //教科(3年)
        $data = array();
        for ($i = 1; $i <= 9; $i++) {
            $num = sprintf("%02d", $i);
            $data["CONFIDENTIAL_RPT{$num}"][NUMBER] = $model->field["CONF3_RPT{$num}"];
        }
        //備考
        $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
        }
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
        //cnt
        $getCount001 = $db->getOne(knjl021pQuery::getCountDetaildata($model, "001"));
        $getCount002 = $db->getOne(knjl021pQuery::getCountDetaildata($model, "002"));
        $getCount004 = $db->getOne(knjl021pQuery::getCountDetaildata($model, "004"));
        $getCount005 = $db->getOne(knjl021pQuery::getCountDetaildata($model, "005"));
        $getCount006 = $db->getOne(knjl021pQuery::getCountDetaildata($model, "006"));
        $getCount007 = $db->getOne(knjl021pQuery::getCountDetaildata($model, "007"));
        $getCount008 = $db->getOne(knjl021pQuery::getCountDetaildata($model, "008"));
        //教科(1年)
        $data = array();
        for ($i = 1; $i <= 9; $i++) {
            $num = sprintf("%02d", $i);
            $data["REMARK{$i}"][TEXT]       = $model->field["CONF1_RPT{$num}"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount001 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "001";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '001' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);
        //教科(2年)
        $data = array();
        for ($i = 1; $i <= 9; $i++) {
            $num = sprintf("%02d", $i);
            $data["REMARK{$i}"][TEXT]       = $model->field["CONF2_RPT{$num}"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount002 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "002";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '002' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);
        //クラス人員・順位・健康状況・総合所見・その他の活動の記録
        $data = array();
        $data["REMARK1"][TEXT]              = $model->field["CLASS_JININ"];
        $data["REMARK2"][TEXT]              = $model->field["JUNI"];
        $data["REMARK3"][TEXT]              = $model->field["SHOKEN_KENKOU"];
        $data["REMARK4"][TEXT]              = $model->field["SHOKEN_SOUGOU"];
        $data["REMARK5"][TEXT]              = $model->field["SHOKEN_SONOTA"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount004 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "004";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '004' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);
        //特別活動の記録(1,2,3年)・総合的な学習の時間の記録(1,2,3年)
        $data = array();
        $data["REMARK1"][TEXT]              = $model->field["SHOKEN_TOKUBETU1"];
        $data["REMARK2"][TEXT]              = $model->field["SHOKEN_TOKUBETU2"];
        $data["REMARK3"][TEXT]              = $model->field["SHOKEN_TOKUBETU3"];
        $data["REMARK4"][TEXT]              = $model->field["SHOKEN_SOUGAKU1"];
        $data["REMARK5"][TEXT]              = $model->field["SHOKEN_SOUGAKU2"];
        $data["REMARK6"][TEXT]              = $model->field["SHOKEN_SOUGAKU3"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount005 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "005";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '005' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);
        //出欠状況(1年)
        $data = array();
        $data["REMARK1"][TEXT]              = $model->field["ATTEND_SHUSSEKI_SUBEKI1"];
        $data["REMARK2"][TEXT]              = $model->field["ATTEND_KESSEKI1"];
        $data["REMARK3"][TEXT]              = $model->field["ATTEND_TIKOKU1"];
        $data["REMARK4"][TEXT]              = $model->field["ATTEND_SOUTAI1"];
        $data["REMARK5"][TEXT]              = $model->field["ATTEND_RIYUU1"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount006 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "006";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '006' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);
        //出欠状況(2年)
        $data = array();
        $data["REMARK1"][TEXT]              = $model->field["ATTEND_SHUSSEKI_SUBEKI2"];
        $data["REMARK2"][TEXT]              = $model->field["ATTEND_KESSEKI2"];
        $data["REMARK3"][TEXT]              = $model->field["ATTEND_TIKOKU2"];
        $data["REMARK4"][TEXT]              = $model->field["ATTEND_SOUTAI2"];
        $data["REMARK5"][TEXT]              = $model->field["ATTEND_RIYUU2"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount007 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "007";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '007' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);
        //出欠状況(3年)
        $data = array();
        $data["REMARK1"][TEXT]              = $model->field["ATTEND_SHUSSEKI_SUBEKI3"];
        $data["REMARK2"][TEXT]              = $model->field["ATTEND_KESSEKI3"];
        $data["REMARK3"][TEXT]              = $model->field["ATTEND_TIKOKU3"];
        $data["REMARK4"][TEXT]              = $model->field["ATTEND_SOUTAI3"];
        $data["REMARK5"][TEXT]              = $model->field["ATTEND_RIYUU3"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount008 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "008";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '008' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>