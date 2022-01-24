<?php

require_once('for_php7.php');

class knjl021gForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl021gindex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && $model->cmd !== 'keisan') {
            //データを取得
            $Row = knjl021gQuery::get_edit_data($model);
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
        $applicantdiv_name = $db->getOne(knjl021gQuery::get_name_cd($model->year, "L003", $Row["APPLICANTDIV"]));
        $arg["data"]["APPLICANTDIV"] = $applicantdiv_name;

        //入試区分
        $test_name = $db->getOne(knjl021gQuery::get_name_cd($model->year, "L004", $Row["TESTDIV"]));
        $arg["data"]["TESTDIV"] = $test_name;

        //------------------------------内申-------------------------------------
        
        //各項目の教科名称取得
        $query = knjl021gQuery::get_name_cd($model->year, "L008");
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
            $arg["data"]["CONFIDENTIAL_RPT".$num] = knjCreateTextBox($objForm, $Row["CONFIDENTIAL_RPT".$num], "CONFIDENTIAL_RPT".$num, 2, 2, $extra);
        }
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); \" ";
        $arg["data"]["TOTAL_ALL"]     = knjCreateTextBox($objForm, $Row["TOTAL_ALL"], "TOTAL_ALL", 2, 2, $extra);
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toFloat(this.value); \" ";
        $arg["data"]["AVERAGE_ALL"]   = knjCreateTextBox($objForm, round($Row["AVERAGE_ALL"]*10)/10, "AVERAGE_ALL", 4, 4, $extra);

        //欠席の記録
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toFloat(this.value)\"";
        $arg["data"]["ABSENCE_DAYS"] = knjCreateTextBox($objForm, $Row["ABSENCE_DAYS"], "ABSENCE_DAYS", 3, 3, $extra);
        $arg["data"]["ABSENCE_DAYS2"] = knjCreateTextBox($objForm, $Row["ABSENCE_DAYS2"], "ABSENCE_DAYS2", 3, 3, $extra);
        $arg["data"]["ABSENCE_DAYS3"] = knjCreateTextBox($objForm, $Row["ABSENCE_DAYS3"], "ABSENCE_DAYS3", 3, 3, $extra);
        $extra = "";
        $arg["data"]["ABSENCE_REMARK"] = knjCreateTextBox($objForm, $Row["ABSENCE_REMARK"], "ABSENCE_REMARK", 20, 20, $extra);
        $arg["data"]["ABSENCE_REMARK2"] = knjCreateTextBox($objForm, $Row["ABSENCE_REMARK2"], "ABSENCE_REMARK2", 20, 20, $extra);
        $arg["data"]["ABSENCE_REMARK3"] = knjCreateTextBox($objForm, $Row["ABSENCE_REMARK3"], "ABSENCE_REMARK3", 20, 20, $extra);

        //実力
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); \"";
        $arg["data"]["JITURYOKU_KOKUGO"] = knjCreateTextBox($objForm, $Row["JITURYOKU_KOKUGO"], "JITURYOKU_KOKUGO", 3, 3, $extra);
        $arg["data"]["JITURYOKU_EIGO"] = knjCreateTextBox($objForm, $Row["JITURYOKU_EIGO"], "JITURYOKU_EIGO", 3, 3, $extra);
        $arg["data"]["JITURYOKU_SUUGAKU"] = knjCreateTextBox($objForm, $Row["JITURYOKU_SUUGAKU"], "JITURYOKU_SUUGAKU", 3, 3, $extra);
        $arg["data"]["JITURYOKU_TOTAL"] = knjCreateTextBox($objForm, $Row["JITURYOKU_TOTAL"], "JITURYOKU_TOTAL", 3, 3, $extra);
        //模擬・偏差値
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toFloat(this.value); \"";
        $arg["data"]["MOGI_HENSATI"] = knjCreateTextBox($objForm, $Row["MOGI_HENSATI"], "MOGI_HENSATI", 5, 5, $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL011G/knjl011gindex.php?cmd=reference&SEND_PRGID=KNJL021G&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
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
        View::toHTML($model, "knjl021gForm1.html", $arg);
    }
}

?>