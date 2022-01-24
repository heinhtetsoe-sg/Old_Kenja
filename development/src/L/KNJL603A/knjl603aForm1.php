<?php

require_once('for_php7.php');

class knjl603aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl603aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度取得
        $result = $db->query(knjl603aQuery::getEntExamYear());
        $opt = array();
        $flg = false;
        //レコードが存在しなければ処理年度を登録
        if ($result->numRows() == 0) { 
            $opt[] = array("label" => CTRL_YEAR+1, "value" => CTRL_YEAR+1);
            unset($model->entexamyear);
        } else {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array("label" => $row["ENTEXAMYEAR"], "value" => $row["ENTEXAMYEAR"]);
                if ($model->entexamyear == $row["ENTEXAMYEAR"]) {
                    $flg = true;
                }
            }
        }
        $result->free();

        //初期表示の年度設定
        if (!$flg) {
            if (!isset($model->entexamyear)) {
                $model->entexamyear = CTRL_YEAR + 1;
            } else if ($model->entexamyear > $opt[0]["value"]) {
                $model->entexamyear = $opt[0]["value"];
            } else if ($model->entexamyear < $opt[get_count($opt) - 1]["value"]) {
                $model->entexamyear = $opt[get_count($opt) - 1]["value"];
            } else {
                $model->entexamyear = $db->getOne(knjl603aQuery::DeleteAtExist($model));
            }
            if ($model->cmd == 'list') {
                $arg["reload"][] = "parent.right_frame.location.href='knjl603aindex.php?cmd=edit"
                                 . "&ENTEXAMYEAR=".$model->entexamyear."&APPLICANTDIV=".$model->applicantdiv."&TESTDIV=".$model->testdiv."';";
            }
        }

        //年度コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["ENTEXAMYEAR"] = knjCreateCombo($objForm, "ENTEXAMYEAR", $model->entexamyear, $opt, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('list');\"";
        $query = knjl603aQuery::getNameMst($model, "L003", "default");
        makeCombo($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボ
        $model->testdiv = ($model->cmd != "" || $model->applicantdiv == $model->field["APP_HOLD"]) ? $model->testdiv : "";
        $namecd1 = $model->applicantdiv == "1" ? "L024" : "L004";
        $query = knjl603aQuery::getNameMst($model, $namecd1, "default");
        makeCombo($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //一覧表示
        $bifKey = "";
        $query  = knjl603aQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             //レコードを連想配列のまま配列$arg["data"]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $hash1 = array("cmd"                => "edit2",
                           "ENTEXAMYEAR"        => $model->entexamyear,
                           "APPLICANTDIV"       => $model->applicantdiv,
                           "TESTDIV"            => $model->testdiv,
                           "EXAMHALLCD"         => $row["EXAMHALLCD"]);
            $row["EXAMHALLCD"] = View::alink("knjl603aindex.php", $row["EXAMHALLCD"], "target=\"right_frame\"", $hash1) ;

            $bifKey = $row["EXAMHALLCD"];

            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (!isset($model->warning) && VARS::post("cmd") == "copy") {
            $arg["reload"][] = "parent.right_frame.location.href='knjl603aindex.php?cmd=edit"
                             . "&ENTEXAMYEAR=".$model->entexamyear."&APPLICANTDIV=".$model->applicantdiv."&TESTDIV=".$model->testdiv."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl603aForm1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["DEFAULT"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
