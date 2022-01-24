<?php

require_once('for_php7.php');

class knjh335Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh335index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = $db->getRow(knjh335Query::getRow($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //グループコード
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["GROUPCD"] = createTextBox($objForm, $Row["GROUPCD"], "GROUPCD", 4, 4, $extra);

        //ユーザーコード
        if ($model->group_div == "1") {
            $query = knjh335Query::getStaff($model->stf_auth_cd);
        } else {
            $query = knjh335Query::getUser($model->stf_auth_cd);
        }
        makeCombo($objForm, $arg, $db, $query, $model->stf_auth_cd, "STF_AUTH_CD", "", 1);

        //グループ名称１
        $arg["data"]["GROUPNAME1"] = createTextBox($objForm, $Row["GROUPNAME1"], "GROUPNAME1", 40, 40, "");

        //グループ名称２
        $arg["data"]["GROUPNAME2"] = createTextBox($objForm, $Row["GROUPNAME2"], "GROUPNAME2", 40, 40, "");

        //グループ名称３
        $arg["data"]["GROUPNAME3"] = createTextBox($objForm, $Row["GROUPNAME3"], "GROUPNAME3", 40, 40, "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjh335index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh335Form2.html", $arg); 
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
function makeBtn(&$objForm, &$arg, $model)
{
    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = createBtn($objForm, "btn_add", "追 加", $extra);

    //修正ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = createBtn($objForm, "btn_udpate", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = createBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    if ($model->programid == "KNJH335") {
        $value = "終 了";
        $extra = "onclick=\"closeWin();\"";
    } else {
        $value = "戻 る";
        $link = REQUESTROOT."/H/".$model->programid."/".strtolower($model->programid)."index.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
    }
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", $value, $extra);
}

//Hidden作成
function makeHidden(&$objForm)
{
    $objForm->ae(createHiddenAe("cmd"));
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
