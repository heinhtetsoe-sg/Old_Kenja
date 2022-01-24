<?php

require_once('for_php7.php');

class knjl021pForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl021pindex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && $model->cmd !== 'keisan') {
            //データを取得
            $Row = knjl021pQuery::get_edit_data($model);
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
        $applicantdiv_name = $db->getOne(knjl021pQuery::get_name_cd($model->year, "L003", $model->applicantdiv));
        $arg["data"]["APPLICANTDIV"] = $applicantdiv_name;

        //入試区分
        $test_name = $db->getOne(knjl021pQuery::get_name_cd($model->year, ($model->applicantdiv == "2") ? "L004" : "L024", $Row["TESTDIV"]));
        $arg["data"]["TESTDIV"] = $test_name;

        //------------------------------内申-------------------------------------
        
        //各項目の教科名称取得
        $query = knjl021pQuery::get_name_cd($model->year, "L008");
        $result = $db->query($query);
        $kyouka_count = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["ABBV1_".$row["VALUE"]] = $row["ABBV1"];
            $kyouka_count++;
        }
        knjCreateHidden($objForm, "kyouka_count", $kyouka_count);
        
        //学習の記録
        //教科(1年)
        $extra = " STYLE=\"text-align:right; ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value);\"";
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $arg["data"]["CONF1_RPT".$num] = knjCreateTextBox($objForm, $Row["CONF1_RPT".$num], "CONF1_RPT".$num, 3, 2, $extra);
        }
        //教科(2年)
        $extra = " STYLE=\"text-align:right; ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value);\"";
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $arg["data"]["CONF2_RPT".$num] = knjCreateTextBox($objForm, $Row["CONF2_RPT".$num], "CONF2_RPT".$num, 3, 2, $extra);
        }
        //教科(3年)
        $extra = " STYLE=\"text-align:right; ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value);\"";
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $arg["data"]["CONF3_RPT".$num] = knjCreateTextBox($objForm, $Row["CONF3_RPT".$num], "CONF3_RPT".$num, 3, 2, $extra);
        }
        //クラス人員・順位
        $extra = " STYLE=\"text-align:right; ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["CLASS_JININ"] = knjCreateTextBox($objForm, $Row["CLASS_JININ"], "CLASS_JININ", 3, 3, $extra);
        $arg["data"]["JUNI"] = knjCreateTextBox($objForm, $Row["JUNI"], "JUNI", 3, 3, $extra);

        //出欠状況(1,2,3年)
        for ($i = 1; $i <= 3; $i++) {
            $extra = " STYLE=\"text-align:right; ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["ATTEND_SHUSSEKI_SUBEKI{$i}"] = knjCreateTextBox($objForm, $Row["ATTEND_SHUSSEKI_SUBEKI{$i}"], "ATTEND_SHUSSEKI_SUBEKI{$i}", 3, 3, $extra);
            $arg["data"]["ATTEND_KESSEKI{$i}"] = knjCreateTextBox($objForm, $Row["ATTEND_KESSEKI{$i}"], "ATTEND_KESSEKI{$i}", 3, 3, $extra);
            $arg["data"]["ATTEND_TIKOKU{$i}"] = knjCreateTextBox($objForm, $Row["ATTEND_TIKOKU{$i}"], "ATTEND_TIKOKU{$i}", 3, 3, $extra);
            $arg["data"]["ATTEND_SOUTAI{$i}"] = knjCreateTextBox($objForm, $Row["ATTEND_SOUTAI{$i}"], "ATTEND_SOUTAI{$i}", 3, 3, $extra);
            $extra = " STYLE=\"ime-mode: active;\"";
            $arg["data"]["ATTEND_RIYUU{$i}"] = knjCreateTextBox($objForm, $Row["ATTEND_RIYUU{$i}"], "ATTEND_RIYUU{$i}", 20, 20, $extra);
        }

        for ($i = 1; $i <= 3; $i++) {
            //特別活動の記録(1,2,3年)
            $extra = " STYLE=\"ime-mode: active;\" onkeyup =\"charCount(this.value, 2, (25 * 2), true);\" oncontextmenu =\"charCount(this.value, 2, (25 * 2), true);\"";
            $arg["data"]["SHOKEN_TOKUBETU{$i}"] = knjCreateTextArea($objForm, "SHOKEN_TOKUBETU{$i}", "2", "50", "wrap", $extra, $Row["SHOKEN_TOKUBETU{$i}"]);
            //総合的な学習の時間の記録(1,2,3年)
            $extra = " STYLE=\"ime-mode: active;\" onkeyup =\"charCount(this.value, 2, (25 * 2), true);\" oncontextmenu =\"charCount(this.value, 2, (25 * 2), true);\"";
            $arg["data"]["SHOKEN_SOUGAKU{$i}"] = knjCreateTextArea($objForm, "SHOKEN_SOUGAKU{$i}", "2", "50", "wrap", $extra, $Row["SHOKEN_SOUGAKU{$i}"]);
        }

        //健康状況
        $extra = " STYLE=\"ime-mode: active;\" onkeyup =\"charCount(this.value, 2, (25 * 2), true);\" oncontextmenu =\"charCount(this.value, 2, (25 * 2), true);\"";
        $arg["data"]["SHOKEN_KENKOU"] = knjCreateTextArea($objForm, "SHOKEN_KENKOU", "2", "50", "wrap", $extra, $Row["SHOKEN_KENKOU"]);

        //総合所見
        $extra = " STYLE=\"ime-mode: active;\" onkeyup =\"charCount(this.value, 2, (25 * 2), true);\" oncontextmenu =\"charCount(this.value, 2, (25 * 2), true);\"";
        $arg["data"]["SHOKEN_SOUGOU"] = knjCreateTextArea($objForm, "SHOKEN_SOUGOU", "2", "50", "wrap", $extra, $Row["SHOKEN_SOUGOU"]);

        //その他の活動の記録
        $extra = " STYLE=\"ime-mode: active;\" onkeyup =\"charCount(this.value, 2, (25 * 2), true);\" oncontextmenu =\"charCount(this.value, 2, (25 * 2), true);\"";
        $arg["data"]["SHOKEN_SONOTA"] = knjCreateTextArea($objForm, "SHOKEN_SONOTA", "2", "50", "wrap", $extra, $Row["SHOKEN_SONOTA"]);

        //備考
        $extra = " STYLE=\"ime-mode: active;\" onkeyup =\"charCount(this.value, 4, (20 * 2), true);\" oncontextmenu =\"charCount(this.value, 4, (20 * 2), true);\"";
        $arg["data"]["REMARK1"] = knjCreateTextArea($objForm, "REMARK1", "4", "40", "wrap", $extra, $Row["REMARK1"]);
        /***
        $moji = 40;
        $gyou = 7;
        $height = $gyou * 13.5 + ($gyou -1 ) * 3 + 5;
        $extra = "style=\"height:{$height}px;\"";
        $extra = "style=\"height:145px;\"";
        $arg["data"]["REMARK1"] = knjCreateTextArea($objForm, "REMARK1", $gyou, ($moji * 2 + 1), "soft", $extra, $Row["REMARK1"]);
        ***/

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL011P/knjl011pindex.php?cmd=reference&SEND_PRGID=KNJL021P&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl021pForm1.html", $arg);
    }
}

?>