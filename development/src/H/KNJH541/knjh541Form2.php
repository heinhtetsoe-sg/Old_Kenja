<?php

require_once('for_php7.php');

class knjh541Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh541index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ($model->thisIsGet) {
            $Row = knjh541Query::getRow($model->pref_subclasscd, $db);
        } else {
            $Row =& $model->field;
        }
        //実力科目コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PREF_SUBCLASSCD"] = createTextBox($objForm, $Row["PREF_SUBCLASSCD"], "PREF_SUBCLASSCD", 6, 6, $extra);

        //実力科目名称
        $arg["data"]["SUBCLASS_NAME"] = createTextBox($objForm, $Row["SUBCLASS_NAME"], "SUBCLASS_NAME", 40, 40, "");

        //実力科目略称
        $arg["data"]["SUBCLASS_ABBV"] = createTextBox($objForm, $Row["SUBCLASS_ABBV"], "SUBCLASS_ABBV", 10, 10, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjh541index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh541Form2.html", $arg); 
    }
}


/********************************************* 以下関数 *******************************************************/

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

    //戻るボタン    
    $extra = "onclick=\"closeMethod();\"";
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "戻 る", $extra);
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
