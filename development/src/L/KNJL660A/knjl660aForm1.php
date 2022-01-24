<?php

require_once('for_php7.php');

class knjl660aForm1
{

    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl660aindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["YEAR"] = $model->examyear;

        //入試制度コンボボックス
        $extra = "onChange=\"return btn_submit('main')\"";
        $query = knjl660aQuery::getNameMst($model->examyear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $extra = "";
        $namecd1 = $model->applicantdiv == "1" ? "L024" : "L004";
        $query = knjl660aQuery::getNameMst($model->examyear, $namecd1);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl660aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') {
            $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HIDDEN_APPLICANTDIV");
    knjCreateHidden($objForm, "HIDDEN_TESTDIV");
}