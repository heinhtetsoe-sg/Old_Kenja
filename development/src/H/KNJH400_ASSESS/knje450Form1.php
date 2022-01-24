<?php

require_once('for_php7.php');

class knje450Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje450index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒データ取得
        $row = $db->getRow(knje450Query::selectQuery($model), DB_FETCHMODE_ASSOC);

        $arg["data"]["SCHREGNO"]    = $row["SCHREGNO"];
        $arg["data"]["SCH_INFO"]    = ($row["ATTENDNO"]) ? $row["HR_NAME"].$row["ATTENDNO"].'番' : $row["HR_NAME"];
        $arg["data"]["NAME"]        = $row["NAME"];
        $arg["data"]["NAME_KANA"]   = $row["NAME_KANA"];
        $arg["data"]["BIRTHDAY"]    = ($row["BIRTHDAY"]) ? common::DateConv1(str_replace("-", "/", $row["BIRTHDAY"]),0).'生' : "";
        $arg["data"]["ZIPCD"]       = '〒'.$row["ZIPCD"];
        $arg["data"]["ADDR1"]       = $row["ADDR1"];
        $arg["data"]["ADDR2"]       = $row["ADDR2"];
        $arg["data"]["TELNO"]       = $row["TELNO"];

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje450Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //アセスメントボタン
    $extra = "style=\"height:30px;background:#00FFFF;color:#000080;font:bold\" onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "アセスメント", $extra);
    //諸機関との連携歴等ボタン
    $extra = "style=\"height:30px;background:#ADFF2F;color:#006400;font:bold\" onclick=\"return btn_submit('subform2');\"";
    $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "諸機関との連携歴等", $extra);
    //終了ボタンを作成する
    $extra = "style=\"height:30px\" onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)      {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}
?>
