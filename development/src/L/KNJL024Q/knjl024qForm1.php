<?php

require_once('for_php7.php');

class knjl024qForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl024qindex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && $model->cmd !== 'keisan') {
            //データを取得
            $Row = knjl024qQuery::get_edit_data($model);
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
        $applicantdiv_name = $db->getOne(knjl024qQuery::get_name_cd($model->year, "L003", $model->applicantdiv));
        $arg["data"]["APPLICANTDIV"] = $applicantdiv_name;

        //入試区分
        $test_name = $db->getOne(knjl024qQuery::get_name_cd($model->year, "L004", $Row["TESTDIV"]));
        $arg["data"]["TESTDIV"] = $test_name;

        //活動実績1
        $extra = "onkeyup =\"charCount(this.value, {$model->TextSize["REMARK1"]["gyo"]}, ({$model->TextSize["REMARK1"]["moji"]} * 2), true);\" oncontextmenu =\"charCount(this.value, {$model->TextSize["REMARK1"]["gyo"]}, ({$model->TextSize["REMARK1"]["moji"]} * 2), true);\"";
        $arg["data"]["REMARK1"] = knjCreateTextArea($objForm, "REMARK1", $model->TextSize["REMARK1"]["gyo"], ($model->TextSize["REMARK1"]["moji"] * 2), "wrap", $extra, $Row["REMARK1"]);
        $arg["data"]["REMARK1_COMMENT"] = "(全角".$model->TextSize["REMARK1"]["moji"]."文字×".$model->TextSize["REMARK1"]["gyo"]."行)";

        //活動実績2
        $extra = "onkeyup =\"charCount(this.value, {$model->TextSize["REMARK2"]["gyo"]}, ({$model->TextSize["REMARK2"]["moji"]} * 2), true);\" oncontextmenu =\"charCount(this.value, {$model->TextSize["REMARK2"]["gyo"]}, ({$model->TextSize["REMARK2"]["moji"]} * 2), true);\"";
        $arg["data"]["REMARK2"] = knjCreateTextArea($objForm, "REMARK2", $model->TextSize["REMARK2"]["gyo"], ($model->TextSize["REMARK2"]["moji"] * 2), "wrap", $extra, $Row["REMARK2"]);
        $arg["data"]["REMARK2_COMMENT"] = "(全角".$model->TextSize["REMARK2"]["moji"]."文字×".$model->TextSize["REMARK2"]["gyo"]."行)";

        //活動実績3
        $extra = "onkeyup =\"charCount(this.value, {$model->TextSize["REMARK3"]["gyo"]}, ({$model->TextSize["REMARK3"]["moji"]} * 2), true);\" oncontextmenu =\"charCount(this.value, {$model->TextSize["REMARK3"]["gyo"]}, ({$model->TextSize["REMARK3"]["moji"]} * 2), true);\"";
        $arg["data"]["REMARK3"] = knjCreateTextArea($objForm, "REMARK3", $model->TextSize["REMARK3"]["gyo"], ($model->TextSize["REMARK3"]["moji"] * 2), "wrap", $extra, $Row["REMARK3"]);
        $arg["data"]["REMARK3_COMMENT"] = "(全角".$model->TextSize["REMARK3"]["moji"]."文字×".$model->TextSize["REMARK3"]["gyo"]."行)";

        //特記事項
        $extra = "onkeyup =\"charCount(this.value, {$model->TextSize["REMARK9"]["gyo"]}, ({$model->TextSize["REMARK9"]["moji"]} * 2), true);\" oncontextmenu =\"charCount(this.value, {$model->TextSize["REMARK9"]["gyo"]}, ({$model->TextSize["REMARK9"]["moji"]} * 2), true);\"";
        $arg["data"]["REMARK9"] = knjCreateTextArea($objForm, "REMARK9", $model->TextSize["REMARK9"]["gyo"], ($model->TextSize["REMARK9"]["moji"] * 2), "wrap", $extra, $Row["REMARK9"]);
        $arg["data"]["REMARK9_COMMENT"] = "(全角".$model->TextSize["REMARK9"]["moji"]."文字×".$model->TextSize["REMARK9"]["gyo"]."行)";

        /***
        $moji = 25;
        $gyou = 2;
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
        $link = REQUESTROOT."/L/KNJL011Q/knjl011qindex.php?cmd=reference&SEND_PRGID=KNJL024Q&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl024qForm1.html", $arg);
    }
}

?>