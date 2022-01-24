<?php

require_once('for_php7.php');

class knjl023wForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl023windex.php", "", "main");

        //一覧表示
        if (!isset($model->warning)) {
            //データを取得
            $Row = knjl023wQuery::get_edit_data($model);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度
        $arg["TOP"]["YEAR"] = $model->year;

        //受付番号
        $arg["TOP"]["EXAMNO"] = $model->examno;

        //氏名(志願者)
        $arg["TOP"]["NAME"] = $Row["NAME"];

        //入試制度
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl023wQuery::get_name_cd($model->year, "L003", $Row["APPLICANTDIV"]));

        //入試区分
        $arg["data"]["TESTDIV"] = $db->getOne(knjl023wQuery::get_name_cd($model->year, "L004", $Row["TESTDIV"]));

        //状態
        $arg["data"]["JUDGEMENT_INFO"] = $Row["JUDGEMENT_INFO"];

        //4:欠席チェックボックス
        $chkJdg = ($Row["KESSEKI"] == "4") ? " checked" : "";
        $extra = " id=\"KESSEKI\" " .$chkJdg;
        $arg["data"]["KESSEKI"] = knjCreateCheckBox($objForm, "KESSEKI", "4", $extra);
        $arg["data"]["KESSEKI_LABEL"] = $db->getOne(knjl023wQuery::get_name_cd($model->year, "L013", "4"));

        //願変チェックボックス
        $chkJdg = ($Row["GANSHO_HENKOU"] == "5") ? " checked" : "";
        $extra = " id=\"GANSHO_HENKOU\" " .$chkJdg;
        $arg["data"]["GANSHO_HENKOU"] = knjCreateCheckBox($objForm, "GANSHO_HENKOU", "5", $extra);
        $arg["data"]["GANSHO_HENKOU_LABEL"] = $db->getOne(knjl023wQuery::get_name_cd($model->year, "L013", "5"));

        //更新ボタンをグレーアウトフラグ
        $disBtn = ($Row["KESSEKI"] == "" || $Row["KESSEKI"] == "4" || $Row["GANSHO_HENKOU"] == "" || $Row["GANSHO_HENKOU"] == "5") ? "" : " disabled";

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"" .$disBtn;
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL011W/knjl011windex.php?cmd=reference&SEND_PRGID=KNJL023W&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
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
        View::toHTML($model, "knjl023wForm1.html", $arg);
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