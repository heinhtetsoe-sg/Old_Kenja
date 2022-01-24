<?php

require_once('for_php7.php');

class knjx_l510iForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //入試年度及びタイトルの表示
        $arg["data"]["TITLE"] = $model->year."年度入試"."　志願者基礎データ出力";

        //DB接続
        $db = Query::dbCheckOut();

        //ヘッダ有チェックボックス
        $extra  = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

        //学科
        $extra = "";
        $opt = array();
        $opt[] = array("label" => "1:普通科", "value" => "1");
        $opt[] = array("label" => "2:工業科", "value" => "2");
        $arg["data"]["TESTDIV0"] = knjCreateCombo($objForm, "TESTDIV0", $model->field["TESTDIV0"], $opt, $extra, 1);

        //入試区分
        $query = knjx_l510iquery::getTestdivMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->year);
        knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx_l510iindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_l510iForm1.html", $arg);
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

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg)
{
    //実行ボタン csv
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
