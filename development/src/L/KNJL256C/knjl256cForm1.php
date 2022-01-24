<?php

require_once('for_php7.php');


class knjl256cForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl256cForm1", "POST", "knjl256cindex.php", "", "knjl256cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //プレテスト区分
        $query = knjl256cQuery::getPreTestdiv($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "PRE_TESTDIV", $model->field["PRE_TESTDIV"], $extra, 1);

        //入試制度
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl256cQuery::getApctDiv($model));

        //ボタン作成
        makeBtn(&$objForm, &$arg);

        //hidden作成
        makeHidden(&$objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl256cForm1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //CSVボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn(&$objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn(&$objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden(&$objForm, "PRGID", "KNJL256C");
    knjCreateHidden(&$objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden(&$objForm, "YEAR", $model->ObjYear);
    knjCreateHidden(&$objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden(&$objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden(&$objForm, "APPLICANTDIV", "1");
    knjCreateHidden(&$objForm, "cmd");
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
    $value = ($value && $value_flg) ? $value : $opt[0]["VALUE"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
