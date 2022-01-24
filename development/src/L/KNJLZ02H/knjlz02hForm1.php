<?php
class knjlz02hForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjlz02hindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度コンボ
        $query = knjlz02hQuery::getYear($model);
        $extra = "onchange=\"return btn_submit('chgYear');\"";
        makeCmb($objForm, $arg, $db, $query, "ENTEXAMYEAR", $model->leftYear, $extra, 1, "");

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "次年度作成", $extra);

        //学校種別コンボ
        $extra = "onChange=\"return btn_submit('chgAppDiv')\"";
        $model->applicantdiv = ($model->cmd == "chgYear") ? "" : $model->applicantdiv;
        $query = knjlz02hQuery::getNameMst($model->leftYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボ
        $extra = "onChange=\"return btn_submit('chgTestDiv')\"";
        $model->testdiv = ($model->cmd == "chgYear" || $model->cmd == "chgAppDiv") ? "" : $model->testdiv;
        $model->testSubCd = ($model->cmd == "chgYear" || $model->cmd == "chgAppDiv"|| $model->cmd == "chgTestDiv") ? "" : $model->testSubCd;
        $query = knjlz02hQuery::getTestDiv($model);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //リスト作成
        $result = $db->query(knjlz02hQuery::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             array_walk($row, "htmlspecialchars_array");
             $hash = array(
                "cmd"            => "edit",
                "ENTEXAMYEAR"    => $row["ENTEXAMYEAR"],
                "APPLICANTDIV"   => $row["APPLICANTDIV"],
                "TESTDIV"        => $row["TESTDIV"],
                "TESTSUBCLASSCD" => $row["TESTSUBCLASSCD"],
             );
            $row["TESTSUBCLASSCD"] = View::alink("knjlz02hindex.php", $row["TESTSUBCLASSCD"], "target=\"right_frame\"", $hash);
             $arg["data"][] = $row;
        }
    
        $result->free();
    
        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        $flg = in_array($model->cmd, array("list", "chgYear", "chgAppDiv", "chgTestDiv", "copy"));
        if (!isset($model->warning) && $flg) {
            $arg["reload"]= "parent.right_frame.location.href='knjlz02hindex.php?cmd=edit"
                             . "&ENTEXAMYEAR=".$model->leftYear."&APPLICANTDIV=".$model->applicantdiv."&TESTDIV=".$model->testdiv."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjlz02hForm1.html", $arg); 
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
                   
        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
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
//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}
?>
