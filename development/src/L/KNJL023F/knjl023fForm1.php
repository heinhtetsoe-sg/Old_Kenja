<?php

require_once('for_php7.php');

class knjl023fForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl023findex.php", "", "main");

        //一覧表示
        if (!isset($model->warning)) {
            //データを取得
            $Row = knjl023fQuery::get_edit_data($model);
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
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl023fQuery::get_name_cd($model->year, "L003", $Row["APPLICANTDIV"]));

        //入試区分
        $arg["data"]["TESTDIV"] = $db->getOne(knjl023fQuery::get_name_cd($model->year, "L004", $Row["TESTDIV"]));

        //入試回数
        $arg["data"]["TESTDIV0"] = $db->getOne(knjl023fQuery::get_name_cd($model->year, "L034", $Row["TESTDIV0"]));

        //状態
        $arg["data"]["JUDGEMENT_INFO"] = $Row["JUDGEMENT_INFO"];

        //4:欠席チェックボックス
        $chkJdg = ($Row["JUDGEMENT"] == "4") ? " checked" : "";
        $extra = " id=\"JUDGEMENT\" " .$chkJdg;
        $arg["data"]["JUDGEMENT"] = knjCreateCheckBox($objForm, "JUDGEMENT", "4", $extra);

        //更新ボタンをグレーアウトフラグ
        $disBtn = ($Row["JUDGEMENT"] == "" || $Row["JUDGEMENT"] == "4") ? "" : " disabled";

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"" .$disBtn;
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL011F/knjl011findex.php?cmd=reference&SEND_PRGID=KNJL023F&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
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
        View::toHTML($model, "knjl023fForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>