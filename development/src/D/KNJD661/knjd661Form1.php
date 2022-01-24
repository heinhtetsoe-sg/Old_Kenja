<?php

require_once('for_php7.php');

class knjd661Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd661Form1", "POST", "knjd661index.php", "", "knjd661Form1");
        //DB接続
        $db = Query::dbCheckOut();
        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //データ種別コンボ作成
        $query = knjd661Query::getDataDiv();
        $extra = "onchange=\"return btn_submit('knjd661')\"";
        makeCmb($objForm, $arg, $db, $query, "DATA_DIV", $model->field["DATA_DIV"], $extra, 1);

        //テスト名称コンボ作成
        $query = knjd661Query::getMockName($model);
        makeCmb($objForm, $arg, $db, $query, "MOCKCD", $model->field["MOCKCD"], "", 1);

        //転退学者を除くチェックボックス
        $extra = ($model->field["CHK_TENTAI"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"CHK_TENTAI\"";
        $arg["data"]["CHK_TENTAI"] = knjCreateCheckBox($objForm, "CHK_TENTAI", "1", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd661Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
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

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD661");
    knjCreateHidden($objForm, "cmd");
}
?>
