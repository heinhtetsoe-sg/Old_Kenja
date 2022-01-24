<?php

require_once('for_php7.php');

class knjl021wQuery extends Query
{

    //名称マスタよりデータを取得
    public function getNameCd($year, $namecd1, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
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
    public function getEditData($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.SEX, ";
        for ($i = 1; $i <= $model->kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $query .= "     L1.REMARK{$i} AS CONF1_RPT{$num}, ";
            $query .= "     L2.REMARK{$i} AS CONF2_RPT{$num}, ";
            $query .= "     L0.CONFIDENTIAL_RPT{$num} AS CONF3_RPT{$num}, ";
            $query .= "     L11.REMARK{$i} AS KANTEN1_RPT{$num}, ";
            $query .= "     L12.REMARK{$i} AS KANTEN2_RPT{$num}, ";
            $query .= "     L13.REMARK{$i} AS KANTEN3_RPT{$num}, ";
            $query .= "     L14.REMARK{$i} AS KANTEN4_RPT{$num}, ";
            $query .= "     L15.REMARK{$i} AS KANTEN5_RPT{$num}, ";
        }
        $query .= "     L4.REMARK3 AS SHOKEN_KENKOU, ";
        $query .= "     L5.REMARK1 AS SHOKEN_TOKUBETU1, ";
        $query .= "     L5.REMARK2 AS SHOKEN_TOKUBETU2, ";
        $query .= "     L5.REMARK3 AS SHOKEN_TOKUBETU3, ";
        $query .= "     L5.REMARK4 AS SHOKEN_SOUGAKU1, ";
        $query .= "     L5.REMARK5 AS SHOKEN_SOUGAKU2, ";
        $query .= "     L5.REMARK6 AS SHOKEN_SOUGAKU3, ";
        $query .= "     L0.ABSENCE_DAYS AS ATTEND_KESSEKI1, ";
        $query .= "     L0.ABSENCE_REMARK AS ATTEND_RIYUU1, ";
        $query .= "     L0.ABSENCE_DAYS2 AS ATTEND_KESSEKI2, ";
        $query .= "     L0.ABSENCE_REMARK2 AS ATTEND_RIYUU2, ";
        $query .= "     L0.ABSENCE_DAYS3 AS ATTEND_KESSEKI3, ";
        $query .= "     L0.ABSENCE_REMARK3 AS ATTEND_RIYUU3, ";
        $query .= "     L9.REMARK1 AS SHOKEN_KOUDOU1, ";
        $query .= "     L9.REMARK2 AS SHOKEN_KOUDOU2, ";
        $query .= "     L9.REMARK3 AS SHOKEN_KOUDOU3, ";
        $query .= "     L9.REMARK4 AS SHOKEN_KOUDOU4, ";
        $query .= "     L9.REMARK5 AS SHOKEN_KOUDOU5, ";
        $query .= "     L9.REMARK6 AS SHOKEN_KOUDOU6, ";
        $query .= "     L9.REMARK7 AS SHOKEN_KOUDOU7, ";
        $query .= "     L9.REMARK8 AS SHOKEN_KOUDOU8, ";
        $query .= "     L9.REMARK9 AS SHOKEN_KOUDOU9, ";
        $query .= "     L9.REMARK10 AS SHOKEN_KOUDOU10, ";
        $query .= "     L0.REMARK1, ";
        $query .= "     L0.REMARK2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN  ENTEXAM_APPLICANTCONFRPT_DAT L0 ";
        $query .= "          ON L0.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L0.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND L0.EXAMNO = T1.EXAMNO ";
        for ($i = 1; $i <= 15; $i++) {
            if ($i == 3 || $i == 6 || $i == 7 || $i == 8 || $i == 10) {
                continue;
            }
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

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " AND T1.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " AND T1.EXAMNO > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO ASC ";
        } else {
            $query .= "     AND T1.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //データカウント(ENTEXAM_APPLICANTCONFRPT_DAT)
    public function getCountdata($model)
    {
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
    public function getCountDetaildata($model, $seq)
    {
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
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->applicantdiv;
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["SHDIV"][TEXT]         = "1";
        $data["DESIREDIV"][TEXT]     = "1";
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
        $where .= "   AND EXAMNO        = '".$model->examno."'";
        $getCount = $db->getOne(" SELECT COUNT(*) AS CNT FROM ENTEXAM_APPLICANTBASE_DAT ".$where);
        if ($getCount == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        //ENTEXAM_APPLICANTCONFRPT_DAT
        //cnt
        $getCount = $db->getOne(knjl021wQuery::getCountdata($model));
        //１．学習の記録
        //教科(3年)
        $data = array();
        $total = 0;
        for ($i = 1; $i <= $model->kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $data["CONFIDENTIAL_RPT{$num}"][NUMBER] = $model->field["CONF3_RPT{$num}"];
            $confrpt = ($model->field["CONF3_RPT{$num}"]) ? $model->field["CONF3_RPT{$num}"] : 0;
            if ($i <= 9) {
                $total += $confrpt;
            }
        }
        $data["TOTAL_ALL"][NUMBER]          = (0 < $total) ? $total : "";
        //出欠状況(1年)
        //出欠状況(2年)
        //出欠状況(3年)
        $data["ABSENCE_DAYS"][NUMBER]       = $model->field["ATTEND_KESSEKI1"];
        $data["ABSENCE_DAYS2"][NUMBER]      = $model->field["ATTEND_KESSEKI2"];
        $data["ABSENCE_DAYS3"][NUMBER]      = $model->field["ATTEND_KESSEKI3"];
        $data["ABSENCE_REMARK"][TEXT]       = $model->field["ATTEND_RIYUU1"];
        $data["ABSENCE_REMARK2"][TEXT]      = $model->field["ATTEND_RIYUU2"];
        $data["ABSENCE_REMARK3"][TEXT]      = $model->field["ATTEND_RIYUU3"];
        //備考、５．その他参考となる諸事項
        $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]              = $model->field["REMARK2"];
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
        $getCount001 = $db->getOne(knjl021wQuery::getCountDetaildata($model, "001"));
        $getCount002 = $db->getOne(knjl021wQuery::getCountDetaildata($model, "002"));
        $getCount004 = $db->getOne(knjl021wQuery::getCountDetaildata($model, "004"));
        $getCount005 = $db->getOne(knjl021wQuery::getCountDetaildata($model, "005"));
        $getCount009 = $db->getOne(knjl021wQuery::getCountDetaildata($model, "009"));
        $getCount011 = $db->getOne(knjl021wQuery::getCountDetaildata($model, "011"));
        $getCount012 = $db->getOne(knjl021wQuery::getCountDetaildata($model, "012"));
        $getCount013 = $db->getOne(knjl021wQuery::getCountDetaildata($model, "013"));
        $getCount014 = $db->getOne(knjl021wQuery::getCountDetaildata($model, "014"));
        $getCount015 = $db->getOne(knjl021wQuery::getCountDetaildata($model, "015"));
        //教科(1年)
        $data = array();
        $total = 0;
        for ($i = 1; $i <= $model->kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $data["REMARK{$i}"][TEXT]       = $model->field["CONF1_RPT{$num}"];
            $confrpt = ($model->field["CONF1_RPT{$num}"]) ? $model->field["CONF1_RPT{$num}"] : 0;
            $total += $confrpt;
        }
        $data["REMARK12"][TEXT]             = (0 < $total) ? $total : "";
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
        $total = 0;
        for ($i = 1; $i <= $model->kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $data["REMARK{$i}"][TEXT]       = $model->field["CONF2_RPT{$num}"];
            $confrpt = ($model->field["CONF2_RPT{$num}"]) ? $model->field["CONF2_RPT{$num}"] : 0;
            $total += $confrpt;
        }
        $data["REMARK12"][TEXT]             = (0 < $total) ? $total : "";
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
        //健康状況
        $data = array();
        $data["REMARK3"][TEXT]              = $model->field["SHOKEN_KENKOU"];
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
        //２．総合的な学習の時間の記録（学習活動、観点、評価）
        //３．特別活動の記録
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
        //３．行動の記録
        $data = array();
        for ($i = 1; $i <= 10; $i++) {
            $data["REMARK{$i}"][TEXT]       = $model->field["SHOKEN_KOUDOU{$i}"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount009 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "009";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '009' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);
        //１．学習の記録
        //観点1
        $data = array();
        for ($i = 1; $i <= $model->kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $data["REMARK{$i}"][TEXT]       = $model->field["KANTEN1_RPT{$num}"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount011 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "011";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '011' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);
        //観点2
        $data = array();
        for ($i = 1; $i <= $model->kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $data["REMARK{$i}"][TEXT]       = $model->field["KANTEN2_RPT{$num}"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount012 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "012";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '012' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);
        //観点3
        $data = array();
        for ($i = 1; $i <= $model->kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $data["REMARK{$i}"][TEXT]       = $model->field["KANTEN3_RPT{$num}"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount013 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "013";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '013' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);
        //観点4
        $data = array();
        for ($i = 1; $i <= $model->kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $data["REMARK{$i}"][TEXT]       = $model->field["KANTEN4_RPT{$num}"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount014 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "014";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '014' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);
        //観点5
        $data = array();
        for ($i = 1; $i <= $model->kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $data["REMARK{$i}"][TEXT]       = $model->field["KANTEN5_RPT{$num}"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount015 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "015";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ENTEXAMYEAR   = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."'";
            $where .= "   AND EXAMNO        = '".$model->examno."'";
            $where .= "   AND SEQ           = '015' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT", $where);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
