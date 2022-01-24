<?php

require_once('for_php7.php');

class knjl515hForm1 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl515hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        $arg["TITLE"] = "<b>".$model->year."年度　入試判別</b>";

        //対象年度コンボボックス
        $opt_year   = array();
        $opt_year[] = array("label" => (CTRL_YEAR),     "value" => CTRL_YEAR);
        $opt_year[] = array("label" => (CTRL_YEAR + 1), "value" => (CTRL_YEAR + 1));
        $extra = "onChange=\"return btn_submit('list');\"";
        $model->year = ($model->year == "") ? substr(CTRL_DATE, 0, 4): $model->year;
        $arg["TITLE"] = knjCreateCombo($objForm, "YEAR", $model->year, $opt_year, $extra, 1)."<b>年度　入試判別</b>";

        //学校種別コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjl515hQuery::getNameMst($model, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //リスト作成
        $query  = knjl515hQuery::selectQuery($model);
        $result = $db->query($query);
        while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $hash = array("cmd"             => "sendLink",
                          "APPLICANTDIV"    => $row["APPLICANTDIV"],
                          "DISTINCT_ID"     => $row["DISTINCT_ID"],
                          "TESTDIV"         => $row["TESTDIV"],
                          "EXAM_TYPE"       => $row["EXAM_TYPE"],
                          "TEST_DATE"       => $row["TEST_DATE"]
                          );

            $row["DISTINCT_NAME"] = View::alink("knjl515hindex.php", $row["DISTINCT_NAME"], "target=\"right_frame\"", $hash);
            $row["TEST_DATE"]     = str_replace("-", "/", $row["TEST_DATE"]);

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd", "");
        knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl515hForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    if ($name == "year") {
        $value = ($value != "" && $value_flg) ? $value : (CTRL_YEAR + 1);
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
