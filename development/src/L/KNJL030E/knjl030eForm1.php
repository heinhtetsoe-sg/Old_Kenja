<?php

require_once('for_php7.php');

class knjl030eForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl030eindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度取得
        $result = $db->query(knjl030eQuery::getEntExamYear());
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
                $model->entexamyear = $db->getOne(knjl030eQuery::DeleteAtExist($model));
            }
            if ($model->cmd == 'list') {
                $arg["reload"][] = "parent.right_frame.location.href='knjl030eindex.php?cmd=edit"
                                 . "&ENTEXAMYEAR=".$model->entexamyear."&APPLICANTDIV=".$model->applicantdiv."&TESTDIV=".$model->testdiv."&EXAMHALL_TYPE=".$model->examhall_type."';";
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
        $query = knjl030eQuery::getNameMst($model, "L003", "default");
        makeCombo($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボ
        $model->testdiv = ($model->cmd != "" || $model->applicantdiv == $model->field["APP_HOLD"]) ? $model->testdiv : "";
        $query = knjl030eQuery::getNameMst($model, "L004", "default");
        makeCombo($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //会場区分ラジオボタン 1:面接 2:試験
        $opt = array(1, 2);
        if (!$model->examhall_type) $model->examhall_type = 1;
        $click = " onclick=\"return btn_submit('list');\"";
        $extra = array("id=\"EXAMHALL_TYPE1\"".$click, "id=\"EXAMHALL_TYPE2\"".$click);
        $radioArray = knjCreateRadio($objForm, "EXAMHALL_TYPE", $model->examhall_type, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //一覧表示
        $bifKey = "";
        $query  = knjl030eQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             //レコードを連想配列のまま配列$arg["data"]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $hash1 = array("cmd"                => "edit2",
                           "ENTEXAMYEAR"        => $model->entexamyear,
                           "APPLICANTDIV"       => $model->applicantdiv,
                           "TESTDIV"            => $model->testdiv,
                           "EXAMHALL_TYPE"      => $model->examhall_type,
                           "EXAMHALLCD"         => $row["EXAMHALLCD"]);
            if ($cntArray[$row["EXAMHALLCD"]] == 0) $row["EXAMHALL_NAME"] = View::alink("knjl030eindex.php", $row["EXAMHALL_NAME"], "target=\"right_frame\"", $hash1) ;

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
            $arg["reload"][] = "parent.right_frame.location.href='knjl030eindex.php?cmd=edit"
                             . "&ENTEXAMYEAR=".$model->entexamyear."&APPLICANTDIV=".$model->applicantdiv."&TESTDIV=".$model->testdiv."&EXAMHALL_TYPE=".$model->examhall_type."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl030eForm1.html", $arg);
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
