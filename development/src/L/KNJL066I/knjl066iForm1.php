<?php
class knjl066iForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl066iindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjl066iQuery::getYear($model);
        
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "ENTEXAMYEAR", $model->leftYear, $extra, 1, "");

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "次年度作成", $extra);

        //班区分(1:受験班, 2:面接班)
        $opt = array(1, 2);
        $model->groupdiv = ($model->groupdiv == "") ? "1" : $model->groupdiv;
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"GROUPDIV{$val}\" onClick=\"btn_submit('list')\"");
        }
        $radioArray = knjCreateRadio($objForm, "GROUPDIV", $model->groupdiv, $extra, $opt, count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        if ($model->groupdiv == "1") {
            $arg["jyuken"] = 1;
            $arg["mensetu"]  = "";
        } else {
            $arg["jyuken"] = "";
            $arg["mensetu"]  = 1;
        }

        //リスト作成
        $result = $db->query(knjl066iQuery::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             array_walk($row, "htmlspecialchars_array");
             $hash = array(
                "cmd"            => "edit",
                "APPLICANTDIV"   => $row["APPLICANTDIV"],
                "TESTDIV"        => $row["TESTDIV"],
                "GROUPCD"        => $row["GROUPCD"],
                "GROUPDIV"       => $row["GROUPDIV"],
             );
             $row["GROUPCD"] = View::alink("knjl066iindex.php", $row["GROUPCD"], "target=\"right_frame\"", $hash);
             $arg["data"][] = $row;
        }
    
        $result->free();
    
        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl066iForm1.html", $arg); 
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}
?>
