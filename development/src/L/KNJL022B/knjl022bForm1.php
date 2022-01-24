<?php

require_once('for_php7.php');

class knjl022bForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl022bindex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && $model->cmd !== 'keisan') {
            //データを取得
            $Row = knjl022bQuery::get_edit_data($model);
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
        $applicantdiv_name = $db->getOne(knjl022bQuery::get_name_cd($model->year, "L003", $Row["APPLICANTDIV"]));
        $arg["data"]["APPLICANTDIV"] = $applicantdiv_name;

        //入試区分
        $test_name = $db->getOne(knjl022bQuery::get_name_cd($model->year, "L004", $Row["TESTDIV"]));
        $arg["data"]["TESTDIV"] = $test_name;

        //------------------------------内申-------------------------------------
        
        //各項目の教科名称取得
        $query = knjl022bQuery::get_name_cd($model->year, "L008");
        $result = $db->query($query);
        $kyouka_count = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["ABBV1_".$row["VALUE"]] = $row["ABBV1"];
            $kyouka_count++;
        }
        knjCreateHidden($objForm, "kyouka_count", $kyouka_count);
        
        //実力テスト
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toNumber(this.value)\"";
        $arg["data"]["DETAIL_REMARK3"] = knjCreateTextBox($objForm, $Row["DETAIL_REMARK3"], "DETAIL_REMARK3", 3, 3, $extra);
        $arg["data"]["DETAIL_REMARK4"] = knjCreateTextBox($objForm, $Row["DETAIL_REMARK4"], "DETAIL_REMARK4", 3, 3, $extra);
        //確約区分
        $opt = array();
        $value_flg = false;
        $query = knjl022bQuery::getKakuyakuData($model, $Row["TESTDIV"]);
        $result = $db->query($query);
        $opt[] = array('label' => "",'value' => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $extra = "";
        $arg["data"]["KAKUYAKU_SET"] = knjCreateCombo($objForm, "KAKUYAKU_SET", $Row["KAKUYAKU_SET"], $opt, $extra, 1);
        
        //推薦
        //推薦備考
        $arg["data"]["DETAIL_REMARK2"] = knjCreateTextBox($objForm, $Row["DETAIL_REMARK2"], "DETAIL_REMARK2", 60, 60, $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL011B/knjl011bindex.php?cmd=reference&SEND_PRGID=KNJL022B&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
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
        View::toHTML($model, "knjl022bForm1.html", $arg);
    }
}

?>