<?php

class knjl098iForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear."年度";

        //入試制度コンボボックス
        $model->field["APPLICANTDIV"] = ($model->field["APPLICANTDIV"] == "") ? "1" : $model->field["APPLICANTDIV"];
        $extra = "onchange=\"return btn_submit('main');\" ";
        $query = knjl098iQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボボックス
        $model->field["TESTDIV"] = ($model->field["TESTDIV"] == "") ? "1" : $model->field["TESTDIV"];
        $extra = "";
        $query = knjl098iQuery::getTestDiv($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //合否判定後結果一覧
        $totalCount = 0;
        $query = knjl098iQuery::SelectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            //合格者数 合計
            $totalCount += $row["PASS_COUNT"];

            $arg["data"][] = $row;
        }

        //合格者数 合計
        $arg["data2"]["TOTAL_COUNT"] = $totalCount;

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl098iindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl098iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    if ($blank == "ALL") $opt[] = array("label" => "全て", "value" => "ALL");
    $value_flg = false;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //実行ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
