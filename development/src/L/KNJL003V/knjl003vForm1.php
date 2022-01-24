<?php

require_once('for_php7.php');

class knjl003vForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl003vindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjl003vQuery::getYear($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->leftYear, $extra, 1, "");

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度コピー", $extra);

        //一覧表示
        $query  = knjl003vQuery::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg["data"]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $hash = array("cmd"                => "edit",
                          "PLACE_ID"           => $row["PLACE_ID"],
                          "YEAR"               => $model->leftYear,
                        );
            $row["PLACE_ID"] = View::alink("knjl003vindex.php", $row["PLACE_ID"], "target=\"right_frame\"", $hash) ;
            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjl003vindex.php?cmd=edit"
                             . "&YEAR=".$model->leftYear."';";
}

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl003vForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
