<?php
class knjl660hForm1
{
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]  = $objForm->get_start("csv", "POST", "knjl660hindex.php", "", "csv");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR + 1;
        
        //入試区分
        $model->testdiv = ($model->cmd == "chgAppDiv") ? "" : $model->testdiv;
        $query = knjl660hQuery::getEntexamTestDivMst($model->applicantdiv);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        //加点設定
        $opt = array(1, 2);
        $model->field["PLUS_CONFIG"] = ($model->field["PLUS_CONFIG"] == "") ? "1" : $model->field["PLUS_CONFIG"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"PLUS_CONFIG{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "PLUS_CONFIG", $model->field["PLUS_CONFIG"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }
        
        /**********/
        /* ボタン */
        /**********/
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR + 1);
        knjCreateHidden($objForm, "DATE", CTRL_DATE);

        $arg["IFRAME"] = VIEW::setIframeJs();
        //DB切断
        Query::dbCheckIn($db);
        
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl660hForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
