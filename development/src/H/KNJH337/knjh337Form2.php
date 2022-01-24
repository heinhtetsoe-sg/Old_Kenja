<?php

require_once('for_php7.php');

class knjh337Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh337index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ($model->thisIsGet) {
            $Row = knjh337Query::getRow($model->mock_subclass_cd, $db, $model);
        } else {
            $Row =& $model->field;
        }
        //模試科目コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MOCK_SUBCLASS_CD"] = createTextBox($objForm, $Row["MOCK_SUBCLASS_CD"], "MOCK_SUBCLASS_CD", 6, 6, $extra);

        //模試科目名称
        $arg["data"]["SUBCLASS_NAME"] = createTextBox($objForm, $Row["SUBCLASS_NAME"], "SUBCLASS_NAME", 40, 40, "");

        //模試科目略称
        $arg["data"]["SUBCLASS_ABBV"] = createTextBox($objForm, $Row["SUBCLASS_ABBV"], "SUBCLASS_ABBV", 10, 10, "");

        //checkbox
        $extra = $Row["SUBCLASS_DIV"] == "1" ? " checked " : "";
        $arg["data"]["SUBCLASS_DIV"] = knjCreateCheckBox($objForm, "SUBCLASS_DIV", "1", $extra);

        //教科コード（コンボ）
        $query = knjh337Query::getClasscd($model);
        $extra = "onChange=\"btn_submit('edit')\";";
        makeCombo($objForm, $arg, $db, $query, $Row["CLASSCD"], 'data', 'CLASSCD', $extra, 1, $blank = "BLANK");

        //科目コード（コンボ）
        $classCd = $Row["CLASSCD"] ? $Row["CLASSCD"] : 'dummy';
        $query = knjh337Query::getSubclasscd($classCd, $model);
        $extra = "";
        makeCombo($objForm, $arg, $db, $query, $Row["SUBCLASSCD"], 'data', 'SUBCLASSCD', $extra, 1, $blank = "BLANK");

        //県下統一模試科目コード（コンボ）
        $query = knjh337Query::getPrefSubclasscd();
        $extra = "onChange=\"btn_submit('edit')\";";
        makeCombo($objForm, $arg, $db, $query, $Row["PREF_SUBCLASSCD"], 'data', 'PREF_SUBCLASSCD', $extra, 1, $blank = "BLANK");

        //県下統一模試科目登録
        if ($model->Properties["usePerfSubclasscd_Touroku"] == '1') {
            $arg["TourokuHyouji"] = '1';
        }
        $extra  = " onClick=\" wopen('".REQUESTROOT."/H/KNJH341/knjh341index.php?";
        $extra .= "cmd=&SEND_PRGID=KNJH337&SEND_AUTH=".AUTHORITY;
        $extra .= "','SUBWIN2',0,0,screen.availWidth,screen.availheight);\"";
        $arg["button"]["btn_prihist"] = createBtn($objForm, "btn_prihist", "県下模試科目登録", $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjh337index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh337Form2.html", $arg); 
    }
}


/********************************************* 以下関数 *******************************************************/
//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $argName, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($name == "SEMESTER") {
            $arg[$argName][$name ."NAME" .$row["VALUE"]] = $row["LABEL"];
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$argName][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}


//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = createBtn($objForm, "btn_add", "追 加", $extra);

    //修正ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = createBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = createBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $Row)
{
    $objForm->ae(createHiddenAe("cmd"));
}

//テキスト作成
function createTextBox(&$objForm, $value, $name, $size, $maxlen, $extra)
{
    $objForm->ae( array("type"      => "text",
                        "name"      => $name,
                        "size"      => $size,
                        "maxlength" => $maxlen,
                        "value"     => $value,
                        "extrahtml" => $extra) );
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
