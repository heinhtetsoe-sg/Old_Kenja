<?php

require_once('for_php7.php');

class knjl021wForm1
{
    public function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl021windex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && $model->cmd !== 'keisan') {
            //データを取得
            $Row = knjl021wQuery::getEditData($model);
            if ($model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    $model->cmd = "main";
                    $Row = knjl021wQuery::getEditData($model);
                }
            }
            $model->examno = $Row["EXAMNO"];
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度
        $arg["TOP"]["YEAR"] = $model->year;

        //受検番号
        $arg["TOP"]["EXAMNO"] = $model->examno;

        //氏名(志願者)
        $arg["TOP"]["NAME"] = $Row["NAME"];
        
        //入試制度
        $applicantdiv_name = $db->getOne(knjl021wQuery::getNameCd($model->year, "L003", $model->applicantdiv));
        $arg["data"]["APPLICANTDIV"] = $applicantdiv_name;

        //入試区分
        $test_name = $db->getOne(knjl021wQuery::getNameCd($model->year, ($model->applicantdiv == "2") ? "L004" : "L024", $Row["TESTDIV"]));
        $arg["data"]["TESTDIV"] = $test_name;
        
        //性別コンボ
        $query = knjl021wQuery::getNameCd($model->year, "Z002");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "BLANK");

        //------------------------------内申-------------------------------------
        
        //各項目の教科名称取得
        $query = knjl021wQuery::getNameCd($model->year, "L008");
        $result = $db->query($query);
        $kyouka_count = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["ABBV1_".$row["VALUE"]] = $row["ABBV1"];
            $kyouka_count++;
        }
        knjCreateHidden($objForm, "kyouka_count", $kyouka_count);
        
        //１．学習の記録
        //教科(1年)
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $arg["data"]["CONF1_RPT".$num] = knjCreateTextBox($objForm, $Row["CONF1_RPT".$num], "CONF1_RPT".$num, 3, 2, $extra);
        }
        //教科(2年)
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $arg["data"]["CONF2_RPT".$num] = knjCreateTextBox($objForm, $Row["CONF2_RPT".$num], "CONF2_RPT".$num, 3, 2, $extra);
        }
        //教科(3年)
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $arg["data"]["CONF3_RPT".$num] = knjCreateTextBox($objForm, $Row["CONF3_RPT".$num], "CONF3_RPT".$num, 3, 2, $extra);
        }
        //観点①～⑤(3年)
        $extra = " STYLE=\"text-align:right;\"";
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $arg["data"]["KANTEN1_RPT".$num] = knjCreateTextBox($objForm, $Row["KANTEN1_RPT".$num], "KANTEN1_RPT".$num, 3, 2, $extra);
            $arg["data"]["KANTEN2_RPT".$num] = knjCreateTextBox($objForm, $Row["KANTEN2_RPT".$num], "KANTEN2_RPT".$num, 3, 2, $extra);
            $arg["data"]["KANTEN3_RPT".$num] = knjCreateTextBox($objForm, $Row["KANTEN3_RPT".$num], "KANTEN3_RPT".$num, 3, 2, $extra);
            $arg["data"]["KANTEN4_RPT".$num] = knjCreateTextBox($objForm, $Row["KANTEN4_RPT".$num], "KANTEN4_RPT".$num, 3, 2, $extra);
            //観点⑤は国語のみ
            if ($i == 1) {
                $arg["data"]["KANTEN5_RPT".$num] = knjCreateTextBox($objForm, $Row["KANTEN5_RPT".$num], "KANTEN5_RPT".$num, 3, 2, $extra);
            }
        }
        //備考
        $extra = "";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 80, 80, $extra);

        //出欠状況(1,2,3年)
        for ($i = 1; $i <= 3; $i++) {
            $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["ATTEND_KESSEKI{$i}"] = knjCreateTextBox($objForm, $Row["ATTEND_KESSEKI{$i}"], "ATTEND_KESSEKI{$i}", 3, 3, $extra);
            $extra = "";
            $arg["data"]["ATTEND_RIYUU{$i}"] = knjCreateTextBox($objForm, $Row["ATTEND_RIYUU{$i}"], "ATTEND_RIYUU{$i}", 60, 60, $extra);
        }

        //２．総合的な学習の時間の記録（学習活動、観点、評価）
        for ($i = 1; $i <= 3; $i++) {
            $extra = "onkeyup =\"charCount(this.value, 10, (25 * 2), true);\" oncontextmenu =\"charCount(this.value, 10, (25 * 2), true);\"";
            $arg["data"]["SHOKEN_SOUGAKU{$i}"] = knjCreateTextArea($objForm, "SHOKEN_SOUGAKU{$i}", "10", "50", "wrap", $extra, $Row["SHOKEN_SOUGAKU{$i}"]);
        }

        //３．特別活動の記録及び行動の記録
        //特別活動の記録
        for ($i = 1; $i <= 3; $i++) {
            $chkJdg = ($Row["SHOKEN_TOKUBETU{$i}"] == "1") ? " checked" : "";
            $extra = " id=\"SHOKEN_TOKUBETU{$i}\" " .$chkJdg;
            $arg["data"]["SHOKEN_TOKUBETU{$i}"] = knjCreateCheckBox($objForm, "SHOKEN_TOKUBETU{$i}", "1", $extra);
        }
        //行動の記録
        for ($i = 1; $i <= 10; $i++) {
            $chkJdg = ($Row["SHOKEN_KOUDOU{$i}"] == "1") ? " checked" : "";
            $extra = " id=\"SHOKEN_KOUDOU{$i}\" " .$chkJdg;
            $arg["data"]["SHOKEN_KOUDOU{$i}"] = knjCreateCheckBox($objForm, "SHOKEN_KOUDOU{$i}", "1", $extra);
        }

        //健康状況
        $extra = "onkeyup =\"charCount(this.value, 2, (25 * 2), true);\" oncontextmenu =\"charCount(this.value, 2, (25 * 2), true);\"";
        $arg["data"]["SHOKEN_KENKOU"] = knjCreateTextArea($objForm, "SHOKEN_KENKOU", "2", "50", "wrap", $extra, $Row["SHOKEN_KENKOU"]);

        //５．その他参考となる諸事項
        $extra = "onkeyup =\"charCount(this.value, 7, (80 * 2), true);\" oncontextmenu =\"charCount(this.value, 7, (80 * 2), true);\"";
        $arg["data"]["REMARK2"] = knjCreateTextArea($objForm, "REMARK2", "7", "160", "wrap", $extra, $Row["REMARK2"]);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL011W/knjl011windex.php?cmd=reference&SEND_PRGID=KNJL021W&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //前の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1');\"";
        $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1');\"";
        $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl021wForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    if ($name == "SEX") {
        $opt[] = array("label" => "9:空欄等", "value" => "9");
        if ($value == "9") {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
