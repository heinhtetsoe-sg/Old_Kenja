<?php

require_once('for_php7.php');

class knjl021fForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl021findex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && $model->cmd !== 'keisan') {
            //データを取得
            $Row = knjl021fQuery::get_edit_data($model);
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
        $applicantdiv_name = $db->getOne(knjl021fQuery::get_name_cd($model->year, "L003", $Row["APPLICANTDIV"]));
        $arg["data"]["APPLICANTDIV"] = $applicantdiv_name;

        //入試区分
        $test_name = $db->getOne(knjl021fQuery::get_name_cd($model->year, "L004", $Row["TESTDIV"]));
        $arg["data"]["TESTDIV"] = $test_name;

        //------------------------------内申-------------------------------------
        
        //各項目の教科名称取得
        $query = knjl021fQuery::get_name_cd($model->year, "L008");
        $result = $db->query($query);
        $kyouka_count = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["ABBV1_".$row["VALUE"]] = $row["ABBV1"];
            $kyouka_count++;
        }
        knjCreateHidden($objForm, "kyouka_count", $kyouka_count);
        
        //内申
        $set_total_all = "";
        //$extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); Keisan();\"";
        $extra = " STYLE=\"text-align:right; ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value); Keisan();\"";
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $arg["data"]["CONFIDENTIAL_RPT".$num] = knjCreateTextBox($objForm, $Row["CONFIDENTIAL_RPT".$num], "CONFIDENTIAL_RPT".$num, 3, 3, $extra);
        }
        //$rOnlyExtra = " STYLE=\"text-align:right;background:darkgray\" readOnly ";
        $rOnlyExtra = " STYLE=\"text-align:right; ime-mode: inactive;\" ";
        $arg["data"]["TOTAL3"]        = knjCreateTextBox($objForm, $Row["TOTAL3"], "TOTAL3", 4, 4, $rOnlyExtra);
        $arg["data"]["TOTAL5"]        = knjCreateTextBox($objForm, $Row["TOTAL5"], "TOTAL5", 4, 4, $rOnlyExtra);
        $arg["data"]["TOTAL_ALL"]     = knjCreateTextBox($objForm, $Row["TOTAL_ALL"], "TOTAL_ALL", 4, 4, $rOnlyExtra);
        $extra = " STYLE=\"text-align:right; ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["KASANTEN_ALL"]  = knjCreateTextBox($objForm, $Row["KASANTEN_ALL"], "KASANTEN_ALL", 3, 3, $extra);

        //備考２（活動評価点）
        $extra = " STYLE=\"ime-mode: active\"";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 41, 60, $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL011F/knjl011findex.php?cmd=reference&SEND_PRGID=KNJL021F&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
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
        View::toHTML($model, "knjl021fForm1.html", $arg);
    }
}

?>