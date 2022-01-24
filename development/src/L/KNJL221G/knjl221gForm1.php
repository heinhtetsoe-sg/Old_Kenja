<?php

require_once('for_php7.php');

class knjl221gForm1 {
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl221gindex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && $model->cmd !== 'keisan') {
            //データを取得
            $Row = knjl221gQuery::get_edit_data($model);
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
        $applicantdiv_name = $db->getOne(knjl221gQuery::get_name_cd($model->year, "L003", $Row["APPLICANTDIV"]));
        $arg["data"]["APPLICANTDIV"] = $applicantdiv_name;

        //入試区分
        $test_name = $db->getOne(knjl221gQuery::get_name_cd($model->year, "L004", $Row["TESTDIV"]));
        $arg["data"]["TESTDIV"] = $test_name;

        //エンター押下時の移動対象一覧
        $setTextField = array();
        foreach ($model->l008Arr as $key => $abbv) {
            $setTextField[] = "CONFIDENTIAL_RPT{$key}";
        }
        $setTextField[] = "ABSENCE_DAYS3";
        $setTextField[] = "TOTALSTUDYTIME";
        $setTextField[] = "REMARK1_004";
        $setTextField[] = "REMARK2_004";
        $setTextField[] = "REMARK3_004";
        $setTextField[] = "REMARK10_031";
        $setTextField[] = "REMARK6_031";
        $setTextField[] = "REMARK7_031";
        $setTextField[] = "REMARK8_031";
        $setTextField[] = "REMARK9_031";
        knjCreateHidden($objForm, "setTextField", implode(',', $setTextField));

        //------------------------------内申-------------------------------------

        //各項目の教科名称セット
        $kyouka_count = 0;
        foreach ($model->l008Arr as $key => $abbv) {
            $arg["data"]["ABBV1_".$key] = $abbv;
            $kyouka_count++;
        }
        knjCreateHidden($objForm, "kyouka_count", $kyouka_count);
        knjCreateHidden($objForm, "total5Flg", $model->total5Flg);

        //内申
        $set_total_all = "";
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); Keisan();\" onkeydown=\"changeEnterToTab(this)\" onChange=\"change_flg();\"";
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $arg["data"]["CONFIDENTIAL_RPT".$num] = knjCreateTextBox($objForm, $Row["CONFIDENTIAL_RPT".$num], "CONFIDENTIAL_RPT".$num, 2, 2, $extra);
        }
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); \" ";
        $arg["data"]["TOTAL5"]    = knjCreateTextBox($objForm, $Row["TOTAL5"], "TOTAL5", 2, 2, $extra);
        $arg["data"]["TOTAL_ALL"] = knjCreateTextBox($objForm, $Row["TOTAL_ALL"], "TOTAL_ALL", 2, 2, $extra);

        //欠席の記録
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toFloat(this.value)\" onkeydown=\"changeEnterToTab(this)\" onChange=\"change_flg();\"";
        $arg["data"]["ABSENCE_DAYS3"] = knjCreateTextBox($objForm, $Row["ABSENCE_DAYS3"], "ABSENCE_DAYS3", 3, 3, $extra);

        //クラブ活動
        $extra = " onkeydown=\"changeEnterToTab(this)\" onChange=\"change_flg();\"";
        $arg["data"]["TOTALSTUDYTIME"] = knjCreateTextBox($objForm, $Row["TOTALSTUDYTIME"], "TOTALSTUDYTIME", 31, 45, $extra);

        //入試相談日
        $extra = " STYLE=\"ime-mode: inactive;\" onkeydown=\"changeEnterToTab(this)\" onChange=\"change_flg();\"";
        $arg["data"]["REMARK1_004"] = View::popUpCalendar2($objForm, "REMARK1_004", str_replace("-", "/", $Row["REMARK1_004"]), "", "", $extra);

        //相談結果
        $extra = " onkeydown=\"changeEnterToTab(this)\" onChange=\"change_flg();\"";
        $arg["data"]["REMARK2_004"] = knjCreateTextBox($objForm, $Row["REMARK2_004"], "REMARK2_004", 9, 12, $extra);

        //体験ゼミ結果
        $extra = " onkeydown=\"changeEnterToTab(this)\" onChange=\"change_flg();\"";
        $arg["data"]["REMARK3_004"] = knjCreateTextBox($objForm, $Row["REMARK3_004"], "REMARK3_004", 9, 12, $extra);

        //特記事項
        $extra = " onChange=\"change_flg()\" id=\"REMARK10_031\" onkeyup=\"charCount(this.value, 6, 24, true);\"";
        $arg["data"]["REMARK10_031"] = knjCreateTextArea($objForm, "REMARK10_031", 6, 24, "wrap", $extra, $Row["REMARK10_031"]);

        //面接日
        $query = knjl221gQuery::get_name_cd($model->year, "L004", $Row["TESTDIV"]);
        $rowL004 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $defDate = $rowL004["NAMESPARE1"];
        $Row["REMARK6_031"] = str_replace("-", "/", ($Row["REMARK6_031"] != '') ? $Row["REMARK6_031"]: $defDate);
        $extra = " STYLE=\"ime-mode: inactive;\" onkeydown=\"changeEnterToTab(this)\" onChange=\"change_flg();\"";
        $arg["data"]["REMARK6_031"] = View::popUpCalendar2($objForm, "REMARK6_031", $Row["REMARK6_031"], "", "", $extra);

        //開始時間
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onChange=\"change_flg()\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["REMARK7_031"] = knjCreateTextBox($objForm, $Row["REMARK7_031"], "REMARK7_031", 2, 2, $extra);
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onChange=\"change_flg()\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["REMARK8_031"] = knjCreateTextBox($objForm, $Row["REMARK8_031"], "REMARK8_031", 2, 2, $extra);

        //面接会場
        $query = knjl221gQuery::get_name_cd($model->year, "L050");
        $extra = " onChange=\"change_flg()\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, "REMARK9_031", $Row["REMARK9_031"], $extra, 1, "BLANK");

        /**********/
        /* ボタン */
        /**********/
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL210G/knjl210gindex.php?cmd=reference&SEND_PRGID=KNJL221G&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
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
        View::toHTML($model, "knjl221gForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>