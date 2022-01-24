<?php

require_once('for_php7.php');

class knjz238Form2 {
    function main(&$model) {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz238index.php", "", "edit");
        $db  = Query::dbCheckOut();
        if (!isset($model->warning)) {
            $query = knjz238Query::getAttendSubclassSpecial($model->special_group_cd, $model->subclasscd, $model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //特活グループコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SPECIAL_GROUP_CD"] = knjCreateTextBox($objForm, $Row["SPECIAL_GROUP_CD"], "SPECIAL_GROUP_CD", 3, 3, $extra);

        //特活グループ名称
        $extra = "";
        $arg["data"]["SPECIAL_GROUP_NAME"] = knjCreateTextBox($objForm, $Row["SPECIAL_GROUP_NAME"], "SPECIAL_GROUP_NAME", 40, 20, $extra);

        //特活グループ略称
        $extra = "";
        $arg["data"]["SPECIAL_GROUP_ABBV"] = knjCreateTextBox($objForm, $Row["SPECIAL_GROUP_ABBV"], "SPECIAL_GROUP_ABBV", 6, 3, $extra);

        //科目コード
        $query = knjz238Query::getSubclassCd($model);
        $opt = array();
        $value_flg = false;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $value = $model->field["CLASSCD"]."-".$model->field["SCHOOL_KIND"]."-".$model->field["CURRICULUM_CD"]."-".$model->field["SUBCLASSCD"];
            if (VARS::post("cmd") == 'edit' || VARS::post("cmd") == 'update' || VARS::post("cmd") == 'add' || VARS::post("cmd") == 'delete') {
                $value = $model->field["SUBCLASSCD"];
                $model->field["SUBCLASSCD"] = substr($model->field["SUBCLASSCD"],7,6);
            }
        } else {
            $value = $model->field["SUBCLASSCD"];
        }
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["data"]["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $value, $opt, $extra, 1);
        } else {
            $arg["data"]["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $Row["SUBCLASSCD"], $opt, $extra, 1);
        }

        //時間(分)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MINUTES"] = knjCreateTextBox($objForm, $Row["MINUTES"], "MINUTES", 3, 3, $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "window.open('knjz238index.php?cmd=list&ed=1','left_frame');";
        }
        View::toHTML($model, "knjz238Form2.html", $arg);
    }
}
?>
