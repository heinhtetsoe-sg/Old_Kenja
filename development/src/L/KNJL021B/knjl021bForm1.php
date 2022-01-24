<?php

require_once('for_php7.php');

class knjl021bForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl021bindex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && $model->cmd !== 'keisan') {
            //データを取得
            $Row = knjl021bQuery::get_edit_data($model);
        } else {
            $Row =& $model->field;
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //入試年度
        $arg["TOP"]["YEAR"] = $model->year;

        //受験番号
        $arg["TOP"]["EXAMNO"] = $model->examno;

        //氏名(志願者)
        $arg["TOP"]["NAME"] = $Row["NAME"];
        
        //入試制度
        $applicantdiv_name = $db->getOne(knjl021bQuery::get_name_cd($model->year, "L003", $Row["APPLICANTDIV"]));
        $arg["data"]["APPLICANTDIV"] = $applicantdiv_name;

        //入試区分
        $test_name = $db->getOne(knjl021bQuery::get_name_cd($model->year, "L004", $Row["TESTDIV"]));
        $arg["data"]["TESTDIV"] = $test_name;

        //------------------------------内申-------------------------------------
        
        //各項目の教科名称取得
        $query = knjl021bQuery::get_name_cd($model->year, "L008");
        $result = $db->query($query);
        $kyouka_count = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["ABBV1_".$row["VALUE"]] = $row["ABBV1"];
            $kyouka_count++;
        }
        knjCreateHidden($objForm, "kyouka_count", $kyouka_count);
        
        //内申
        $set_total_all = "";
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); Keisan();\"";
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $arg["data"]["CONFIDENTIAL_RPT".$num] = knjCreateTextBox($objForm, $Row["CONFIDENTIAL_RPT".$num], "CONFIDENTIAL_RPT".$num, 3, 3, $extra);
        }
        $rOnlyExtra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $arg["data"]["TOTAL_ALL"]     = knjCreateTextBox($objForm, $Row["TOTAL_ALL"], "TOTAL_ALL", 4, 4, $rOnlyExtra);
        $arg["data"]["AVERAGE_ALL"]   = knjCreateTextBox($objForm, $Row["AVERAGE_ALL"], "AVERAGE_ALL", 5, 5, $rOnlyExtra);

        //行動の記録
        //基本フラグ
        if ($Row["BASE_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["BASE_FLG"] = knjCreateCheckBox($objForm, "BASE_FLG", "1", $extra);
        //健康フラグ
        if ($Row["HEALTH_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["HEALTH_FLG"] = knjCreateCheckBox($objForm, "HEALTH_FLG", "1", $extra);
        //自主フラグ
        if ($Row["ACTIVE_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["ACTIVE_FLG"] = knjCreateCheckBox($objForm, "ACTIVE_FLG", "1", $extra);
        //責任フラグ
        if ($Row["RESPONSIBLE_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["RESPONSIBLE_FLG"] = knjCreateCheckBox($objForm, "RESPONSIBLE_FLG", "1", $extra);
        //創意フラグ
        if ($Row["ORIGINAL_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["ORIGINAL_FLG"] = knjCreateCheckBox($objForm, "ORIGINAL_FLG", "1", $extra);
        //思いフラグ
        if ($Row["MIND_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["MIND_FLG"] = knjCreateCheckBox($objForm, "MIND_FLG", "1", $extra);
        //自然フラグ
        if ($Row["NATURE_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["NATURE_FLG"] = knjCreateCheckBox($objForm, "NATURE_FLG", "1", $extra);
        //勤労フラグ
        if ($Row["WORK_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["WORK_FLG"] = knjCreateCheckBox($objForm, "WORK_FLG", "1", $extra);
        //公正フラグ
        if ($Row["JUSTICE_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["JUSTICE_FLG"] = knjCreateCheckBox($objForm, "JUSTICE_FLG", "1", $extra);
        //公共フラグ
        if ($Row["PUBLIC_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["PUBLIC_FLG"] = knjCreateCheckBox($objForm, "PUBLIC_FLG", "1", $extra);
        
        //欠席の記録
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toNumber(this.value)\"";
        $arg["data"]["ABSENCE_DAYS"] = knjCreateTextBox($objForm, $Row["ABSENCE_DAYS"], "ABSENCE_DAYS", 3, 3, $extra);
        $arg["data"]["ABSENCE_DAYS2"] = knjCreateTextBox($objForm, $Row["ABSENCE_DAYS2"], "ABSENCE_DAYS2", 3, 3, $extra);
        $arg["data"]["ABSENCE_DAYS3"] = knjCreateTextBox($objForm, $Row["ABSENCE_DAYS3"], "ABSENCE_DAYS3", 3, 3, $extra);
        
        $extra = "";
        $arg["data"]["ABSENCE_REMARK"] = knjCreateTextBox($objForm, $Row["ABSENCE_REMARK"], "ABSENCE_REMARK", 20, 20, $extra);
        $arg["data"]["ABSENCE_REMARK2"] = knjCreateTextBox($objForm, $Row["ABSENCE_REMARK2"], "ABSENCE_REMARK2", 20, 20, $extra);
        $arg["data"]["ABSENCE_REMARK3"] = knjCreateTextBox($objForm, $Row["ABSENCE_REMARK3"], "ABSENCE_REMARK3", 20, 20, $extra);
        
        //特活・部活・特記事項要約
        $extra = "";
        $arg["data"]["DETAIL_REMARK1"] = knjCreateTextBox($objForm, $Row["DETAIL_REMARK1"], "DETAIL_REMARK1", 30, 30, $extra);
        $extra = "";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 60, 60, $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL011B/knjl011bindex.php?cmd=reference&SEND_PRGID=KNJL021B&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl021bForm1.html", $arg);
    }
}

?>