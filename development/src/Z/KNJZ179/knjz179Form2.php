<?php

require_once('for_php7.php');

class knjz179Form2 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz179index.php", "", "edit");
        $db  = Query::dbCheckOut();
        if (!isset($model->warning)) {
            $query = knjz179Query::getSchregRegdGdat($model->term, $model->grade);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->fields;
        }

        //学年
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["GRADE"] = knjCreateTextBox($objForm, $Row["GRADE"], "GRADE", 2, 2, $extra);
        //学校区分
        $query = knjz179Query::getSchoolKind($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1, "");

        //学年略称コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["GRADE_CD"] = knjCreateTextBox($objForm, $Row["GRADE_CD"], "GRADE_CD", 2, 2, $extra);

        //学年名称1
        $extra = "";
        $arg["data"]["GRADE_NAME1"] = knjCreateTextBox($objForm, $Row["GRADE_NAME1"], "GRADE_NAME1", 40, 20, $extra);

        //学年名称2
        $extra = "";
        $arg["data"]["GRADE_NAME2"] = knjCreateTextBox($objForm, $Row["GRADE_NAME2"], "GRADE_NAME2", 40, 20, $extra);

        //学年名称3
        $extra = "";
        $arg["data"]["GRADE_NAME3"] = knjCreateTextBox($objForm, $Row["GRADE_NAME3"], "GRADE_NAME3", 40, 20, $extra);

        //追加ボタン
        $objForm->ae( array("type"           => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //更新ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae( array("type"           => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset')\"" ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );


        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjz179index.php?cmd=list&ed=1','left_frame');";
        }
        View::toHTML($model, "knjz179Form2.html", $arg);
    }
}

function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
