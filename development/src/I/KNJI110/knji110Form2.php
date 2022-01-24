<?php

require_once('for_php7.php');

class knji110Form2
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knji110index.php", "", "edit");

        if (!isset($model->warning)) {
            $Row = knji110Query::getRow($model, $model->term, $model->grade, $model->hr_class);
        } else {
            $Row =& $model->fields;
        }

        //学年
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["GRADE"] = knjCreateTextBox($objForm, $Row["GRADE"], "GRADE", 2, 2, $extra);

        //組
        $extra = "";
        $arg["data"]["HR_CLASS"] = knjCreateTextBox($objForm, $Row["HR_CLASS"], "HR_CLASS", 3, 3, $extra);

        //年組名称
        $extra = "";
        $arg["data"]["HR_NAME"] = knjCreateTextBox($objForm, $Row["HR_NAME"], "HR_NAME", 11, 10, $extra);

        //年組略称
        $extra = "";
        $arg["data"]["HR_NAMEABBV"] = knjCreateTextBox($objForm, $Row["HR_NAMEABBV"], "HR_NAMEABBV", 6, 5, $extra);

        //組名称1
        $extra = "";
        $arg["data"]["HR_CLASS_NAME1"] = knjCreateTextBox($objForm, $Row["HR_CLASS_NAME1"], "HR_CLASS_NAME1", 20, 10, $extra);

        //組名称2
        $extra = "";
        $arg["data"]["HR_CLASS_NAME2"] = knjCreateTextBox($objForm, $Row["HR_CLASS_NAME2"], "HR_CLASS_NAME2", 20, 10, $extra);

        //年名称
        $extra = "";
        $arg["data"]["GRADE_NAME"] = knjCreateTextBox($objForm, $Row["GRADE_NAME"], "GRADE_NAME", 20, 10, $extra);

        //STAFF
        $optStaff = knji110query::getStaff(substr($model->term,0,4), $model);

        //担任１
        $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        $arg["data"]["TR_CD1"] = knjCreateCombo($objForm, "TR_CD1", $Row["TR_CD1"], $optStaff, $extra, 1);

        //担任2
        $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        $arg["data"]["TR_CD2"] = knjCreateCombo($objForm, "TR_CD2", $Row["TR_CD2"], $optStaff, $extra, 1);

        //担任3
        $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        $arg["data"]["TR_CD3"] = knjCreateCombo($objForm, "TR_CD3", $Row["TR_CD3"], $optStaff, $extra, 1);

        //副担任１
        $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        $arg["data"]["SUBTR_CD1"] = knjCreateCombo($objForm, "SUBTR_CD1", $Row["SUBTR_CD1"], $optStaff, $extra, 1);

        //副担任２
        $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        $arg["data"]["SUBTR_CD2"] = knjCreateCombo($objForm, "SUBTR_CD2", $Row["SUBTR_CD2"], $optStaff, $extra, 1);

        //副担任３
        $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        $arg["data"]["SUBTR_CD3"] = knjCreateCombo($objForm, "SUBTR_CD3", $Row["SUBTR_CD3"], $optStaff, $extra, 1);

        //HR施設
        $extra = "STYLE=\"WIDTH:100%\" WIDTH=\"100%\"";
        $optFac = knji110query::getFacility();
        $arg["data"]["HR_FACCD"] = knjCreateCombo($objForm, "HR_FACCD", $Row["HR_FACCD"], $optFac, $extra, 1);

        //学期授業週数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CLASSWEEKS"] = knjCreateTextBox($objForm, $Row["CLASSWEEKS"], "CLASSWEEKS", 3, 2, $extra);

        //学期授業日数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CLASSDAYS"] = knjCreateTextBox($objForm, $Row["CLASSDAYS"], "CLASSDAYS", 4, 3, $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra, "reset");

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knji110index.php?cmd=list&ed=1','left_frame');";
        }

        View::toHTML($model, "knji110Form2.html", $arg); 
    }
}
?>
