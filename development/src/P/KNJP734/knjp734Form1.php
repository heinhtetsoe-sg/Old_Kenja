<?php

require_once('for_php7.php');


class knjp734Form1 {
    function main(&$model) {

        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //校種コンボ
        $query = knjp734query::getSchkind($model);
        $extra = "onchange=\"btn_submit('knjp731');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //課程コンボボックス
        $opt = array();
        $opt[] = array('label' => "全日制",       'value' => "1");
        $opt[] = array('label' => "定時・通信制", 'value' => "2");
        $extra = "";
        $arg["data"]["COURSECD"] = knjCreateCombo($objForm, "COURSECD", $model->field["COURSECD"], $opt, $extra, 1);

        //申請月
        $optMonth = array();
        $optMonth[] = array ("label" => "4月申請", "value" => "04");
        $optMonth[] = array ("label" => "7月申請", "value" => "07");
        $model->month = (strlen($model->month)) ? $model->month : $optMonth[0]["value"];
        $extra = "";
        $arg["data"]["MONTH"] = knjCreateCombo($objForm, "MONTH", $model->month, $optMonth, $extra, "1");

        //1:取込, 2:エラー出力
        $opt = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["start"]   = $objForm->get_start("knjp734Form1", "POST", "knjp734index.php", "", "knjp734Form1");
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp734Form1.html", $arg); 
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
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
