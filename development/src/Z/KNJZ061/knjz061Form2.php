<?php

require_once('for_php7.php');

class knjz061Form2
{
    function main(&$model)
    {
        $arg["reload"] = "";

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz061index.php", "", "edit");
        //校種の値をセット
        if ($model->school_kind == "") {
            $model->school_kind = VARS::get("SCHOOL_KIND");
        }
        
        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz061Query::getRow($model->classcd, $model, $model->school_kind);
        } else {
            $Row =& $model->field;
        }
        $db = Query::dbCheckOut();
        
        //教科コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CLASSCD"] = createTextBox($objForm, $Row["CLASSCD"], "CLASSCD", 3, 2, $extra);

        //学校校種
        $opt_school = array();
        $query = knjz061Query::getNamecd('A023', $model);
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_school[] =  array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt_school[0]["value"];
        $extra = "";
        $arg["data"]["SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $Row["SCHOOL_KIND"], $opt_school, $extra, 1);

        //教科名称
        $arg["data"]["CLASSNAME"] = createTextBox($objForm, $Row["CLASSNAME"], "CLASSNAME", 20, 30, "");

        //教科略称
        $arg["data"]["CLASSABBV"] = createTextBox($objForm, $Row["CLASSABBV"], "CLASSABBV", 10, 15, "");

        //教科名称英字
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["CLASSNAME_ENG"] = createTextBox($objForm, $Row["CLASSNAME_ENG"], "CLASSNAME_ENG", 40, 40, $extra);

        //教科略称英字
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data"]["CLASSABBV_ENG"] = createTextBox($objForm, $Row["CLASSABBV_ENG"], "CLASSABBV_ENG", 30, 30, $extra);

        //調査書用教科名
        $arg["data"]["CLASSORDERNAME1"] = createTextBox($objForm, $Row["CLASSORDERNAME1"], "CLASSORDERNAME1", 40, 60, "");

        //科目名その他２
        $arg["data"]["CLASSORDERNAME2"] = createTextBox($objForm, $Row["CLASSORDERNAME2"], "CLASSORDERNAME2", 40, 60, "");

        //科目名その他３
        $arg["data"]["CLASSORDERNAME3"] = createTextBox($objForm, $Row["CLASSORDERNAME3"], "CLASSORDERNAME3", 40, 60, "");

        //科目数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SUBCLASSES"] = createTextBox($objForm, $Row["SUBCLASSES"], "SUBCLASSES", 3, 2, $extra);

        //表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SHOWORDER"] = createTextBox($objForm, $Row["SHOWORDER"], "SHOWORDER", 3, 2, $extra);

        //調査書用表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SHOWORDER2"] = createTextBox($objForm, $Row["SHOWORDER2"], "SHOWORDER2", 3, 2, $extra);

        //通知表用表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SHOWORDER3"] = createTextBox($objForm, $Row["SHOWORDER3"], "SHOWORDER3", 3, 2, $extra);

        //成績一覧用表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SHOWORDER4"] = createTextBox($objForm, $Row["SHOWORDER4"], "SHOWORDER4", 3, 2, $extra);

        //選択
        $arg["data"]["ELECTDIV"] = createCheckBox($objForm, "ELECTDIV", "1", ($Row["ELECTDIV"]==1) ? "checked" : "nocheck", "");

        //専門･その他
        $opt = array();
        $opt[] = array('label' => "", 'value' => "0");
        $opt[] = array('label' => '1：専門', 'value' => '1');
        $opt[] = array('label' => '2：その他', 'value' => '2');
        $Row["SPECIALDIV"] = ($Row["SPECIALDIV"]) ? $Row["SPECIALDIV"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["SPECIALDIV"] = knjCreateCombo($objForm, "SPECIALDIV", $Row["SPECIALDIV"], $opt, $extra, 1);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $Row, $model);

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz061index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz061Form2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //追加ボタンを作成する
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = createBtn($objForm, "btn_add", "追 加", $extra);

    //修正ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = createBtn($objForm, "btn_udpate", "更 新", $extra);

    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = createBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = createBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    if ($model->send_prgid) {
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"closeMethod();\"");
    } else {
        $extra  = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
    }

}

//Hidden作成
function makeHidden(&$objForm, $Row, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
    knjCreateHidden($objForm, "callId", $model->send_prgid);

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

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

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
