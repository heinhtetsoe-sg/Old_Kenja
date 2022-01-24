<?php

require_once('for_php7.php');


class knjz067bForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm        = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz067bindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjz067bQuery::getIBYear();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "IBYEAR", $model->ibyear, $extra, 1, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz067bForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, &$model)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    if ($name === 'IBPRG_COURSE') {
        $opt[] = array('label' => '--全て--', 'value' => '');
    }
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{

    //ＣＳＶボタン
    $extra = "onclick=\"return btn_submit('downloadCsv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
}
