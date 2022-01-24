<?php

require_once('for_php7.php');

class knjz069_2Form2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz069_2index.php", "", "edit");

        if (isset($model->element_div) && isset($model->element_cd) && !isset($model->warning)) {
            if ($model->cmd !== 'main') {
                $Row = knjz069_2Query::getRow($model->element_div, $model->element_cd);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        // 評価の要素区分
        $opt = array(
            array("value" => "1",    "label" => "1：Outputs"),
            array("value" => "2",    "label" => "2：Skills"),
        );
        $extra = "";
        $arg["ELEMENT_DIV"] = knjCreateCombo($objForm, "ELEMENT_DIV", $Row["ELEMENT_DIV"], $opt, $extra, 1);

        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["ELEMENT_CD"] = knjCreateTextBox($objForm, $Row["ELEMENT_CD"], "ELEMENT_CD", 3, 3, $extra);

        $extra = "";
        $arg["ELEMENT_NAME"] = knjCreateTextBox($objForm, $Row["ELEMENT_NAME"], "ELEMENT_NAME", 52, 26, $extra);

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return Btn_reset('reset');\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ069/knjz069index.php";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){ 
            $arg["reload"]  = "parent.left_frame.location.href='knjz069_2index.php?cmd=list';";
        }
        View::toHTML($model, "knjz069_2Form2.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
            "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
            "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
