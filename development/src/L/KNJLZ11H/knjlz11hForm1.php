<?php
class knjlz11hForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjlz11hindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjlz11hQuery::getYear($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "ENTEXAMYEAR", $model->leftYear, $extra, 1, "");

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "次年度作成", $extra);

        //学校種別コンボ
        $extra = "onChange=\"return btn_submit('list')\"";
        $query = knjlz11hQuery::getNameMst($model->leftYear, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //資格コンボ
        $extra = "onChange=\"return btn_submit('list')\"";
        $query = knjlz11hQuery::getEntexamSettingMst($model, "L026");
        makeCmb($objForm, $arg, $db, $query, "QUALIFIED_CD", $model->qualifiedCd, $extra, 1);

        //リスト作成
        $result = $db->query(knjlz11hQuery::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             array_walk($row, "htmlspecialchars_array");
             $hash = array(
                "cmd"                => "edit",
                "ENTEXAMYEAR"        => $row["ENTEXAMYEAR"],
                "APPLICANTDIV"       => $row["APPLICANTDIV"],
                "QUALIFIED_CD"       => $row["QUALIFIED_CD"],
                "QUALIFIED_JUDGE_CD" => $row["QUALIFIED_JUDGE_CD"],
             );
             $row["QUALIFIED_JUDGE_CD"] = View::alink("knjlz11hindex.php", $row["QUALIFIED_JUDGE_CD"], "target=\"right_frame\"", $hash);
             $arg["data"][] = $row;
        }
    
        $result->free();
    
        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjlz11hForm1.html", $arg); 
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $i = $default = 0;
    $default_flg = true;

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

    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
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

