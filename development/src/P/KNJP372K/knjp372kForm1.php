<?php

require_once('for_php7.php');

class knjp372kForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //権限チェック
        authCheck($arg);

        //ＤＢ接続
        $db = Query::dbCheckOut();

        //学年コンボボックス
        $query = knjp372kQuery::getGrade();
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1);

        //相殺区分ラジオ
        $opt = array(1, 2);
        $model->reducDiv = ($model->reducDiv == "") ? "1" : $model->reducDiv;
        $extra = array("id=\"REDUC_DIV1\" onClick=\"return btn_submit('main');\"", "id=\"REDUC_DIV2\" onClick=\"return btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "REDUC_DIV", $model->reducDiv, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //相殺完了年月コンボボックス
        $div = $model->reducDiv == "1" ? "" : "ADD_";
        $query = knjp372kQuery::getYearMonth($model, $div);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->paidYearMonth, "PAID_YEARMONTH", $extra, 1);

        //支援補助金を相殺
        $extra = $model->lock ? "checked=checked" : "";
        $arg["data"]["LOCK"] = knjCreateCheckBox($objForm, "LOCK", "1", $extra);

        //ボタン作成
        makeButton($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //ＤＢ切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]  = $objForm->get_start("main", "POST", "knjp372kindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp372kForm1.html", $arg);
    }
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //実行
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('execute');\"");
    //終了
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

?>
