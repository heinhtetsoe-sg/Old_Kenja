<?php

require_once('for_php7.php');

class knje464bForm1 {

    function main(&$model) {

        $arg["jscript"] = "";

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knje464bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["YEAR"] = CTRL_YEAR;
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        $model->year = CTRL_YEAR;

        //前年度からコピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //一覧表示
        $key = "";
        $query = knje464bQuery::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //列結合
            if ($key !== $row["SPRT_FACILITY_GRP"]) {
                $cnt = $db->getOne(knje464bQuery::getList($model, $row["SPRT_FACILITY_GRP"]));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;

                $row["CDLINK"] = View::alink("knje464bindex.php", $row["SPRT_FACILITY_GRP"], "target=\"right_frame\"",
                                             array("cmd"               => "edit",
                                                   "SPRT_FACILITY_GRP" => $row["SPRT_FACILITY_GRP"]));
            }

            $arg["data"][] = $row;

            $key = $row["SPRT_FACILITY_GRP"];
        }

        //Hidden作成
        knjCreateHidden($objForm, "cmd");

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "update") {
            $arg["jscript"] = "window.open('knje464bindex.php?cmd=edit','right_frame')";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje464bForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, &$model) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name === 'YEAR') {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else if ($name === 'SEMESTER') {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
