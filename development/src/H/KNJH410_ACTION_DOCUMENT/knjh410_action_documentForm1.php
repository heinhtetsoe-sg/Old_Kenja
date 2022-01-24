<?php

class knjh410_action_documentForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh410_action_documentForm1", "POST", "knjh410_action_documentindex.php", "", "knjh410_action_documentSubForm1");

        //DB接続
        $db = Query::dbCheckOut();
		//名称設定
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $db->getOne(knjh410_action_documentQuery::getName($model->schregno));

		//データ取得
        $row = ($model->actiondate) ? $db->getRow(knjh410_action_documentQuery::getActionDucSub($model), DB_FETCHMODE_ASSOC) : array();
        //行動日付
        $arg["data"]["ACTIONDATE"] = View::popUpCalendar($objForm, "ACTIONDATE", $model->actiondate);

        //時分
        $time = ($row["ACTIONTIME"]) ? split(":",$row["ACTIONTIME"]) : array();

        $arg["data"]["ACTIONHOUR"] = createTextBox($objForm, $time[0], "ACTIONHOUR", 2, 2, "onblur=\"this.value=toInteger(this.value)\";");
        $arg["data"]["ACTIONMINUTE"] = createTextBox($objForm, $time[1], "ACTIONMINUTE", 2, 2, "onblur=\"this.value=toInteger(this.value)\";");

        //区分
        $model->dividecd = ($model->dividecd) ? $model->dividecd : $row["DIVIDECD"];
        $query = knjh410_action_documentQuery::getNameMst($model, "H307");
        makeCombo($objForm, $arg, $db, $query, $model->dividecd, "DIVIDECD", "", 1);

        //件名
        $arg["data"]["TITLE"] = createTextBox($objForm, $row["TITLE"], "TITLE", 40, 40, "");

        //内容
        $arg["data"]["TEXT"] = createTextArea($objForm, "TEXT", 7, 66, "soft", "", $row["TEXT"]);

        //非公開チェックボックス
        $checked = "";
        if ($row["PRIVATE"]) $checked = "checked";
        $extra = " id=\"PRIVATE\" ".$checked;
        $arg["data"]["PRIVATE"] = knjCreateCheckBox($objForm, "PRIVATE", "1", $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $row["STAFFCD"]);

        //hidden
        makeHidden($objForm, $model);
        knjCreateHidden($objForm, "sendCmd", $model->sendCmd);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
   	    View::toHTML($model, "knjh410_action_documentForm1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $staffcd)
{
    //追加
    $disable = ($model->auth != DEF_UPDATABLE && ( $model->sendCmd == "updateSub") && $staffcd != STAFFCD) ? "disabled" : "";
    $cmd  = ($model->sendCmd == "updateSub") ? "update" : "insert";
    $btnName = ($model->sendCmd == "updateSub") ? "更 新" : "追 加";
    $extra = $disable." onclick=\"return btn_submit('".$cmd."');\"";
    $arg["btn_up"] = createBtn($objForm, "btn_up", $btnName, $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $arg["btn_back"] = createBtn($objForm, "btn_back", "終 了", "onclick=\"return btn_submit('subEnd');\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//テキスト作成
function createTextBox(&$objForm, $data, $name, $size, $maxlen, $extra)
{
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "value"     => $data,
                        "extrahtml" => $extra) );
    return $objForm->ge($name);
}

//テキストエリア作成
function createTextArea(&$objForm, $name, $rows, $cols, $wrap, $extra, $value)
{
    $objForm->ae( array("type"        => "textarea",
                        "name"        => $name,
                        "rows"        => $rows,
                        "cols"        => $cols,
                        "wrap"        => $wrap,
                        "extrahtml"   => $extra,
                        "value"       => $value));
    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

?>
