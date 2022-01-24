<?php

require_once('for_php7.php');

class knja055Form1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja055Form1", "POST", "knja055index.php", "", "knja055Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $query = knja055Query::getYear($model);
        $extra = "onChange=\" return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->Year, "YEAR", $extra);

        //------------------------- 開始終了日付 -------------------------

            $model->Sdate = $model->Year . "-04-01";
            $model->Edate = ((int)$model->Year+1) . "-03-31";

        //------------------------- 卒業生 -------------------------

        //ボタンを作成
        makeButton($objForm, $arg);

        //hiddenを作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja055Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $result = $db->query($query);
    $opt = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $value = ($value) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //実行
    $extra = "onclick=\"return btn_submit('execute');\"";
    $arg["button"]["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "実  行", $extra);
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

?>
