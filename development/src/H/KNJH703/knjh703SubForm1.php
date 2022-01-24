<?php

require_once('for_php7.php');

class knjh703SubForm1
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjh703index.php", "", "sel");
        $arg["jscript"] = "";
        $opt_left = $opt_right = array();

        $db = Query::dbCheckOut();

        //文理区分コンボボックス
        $query = knjh703Query::getNameMst("H319");
        $extra = " onChange=\"changeSubclassBunri()\"";
        makeCmb($objForm, $arg, $db, $query, "BUNRIDIV", $model->replace_data["BUNRIDIV"], $extra, 1);
        knjCreateHidden($objForm, "H_BUNRIDIV", $model->replace_data["BUNRIDIV"]);

        //選択科目コンボボックス
        $query = knjh703Query::getSubclass($model->replace_data["BUNRIDIV"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->replace_data["SUBCLASSCD"], $extra, 1, "BLANK");
        knjCreateHidden($objForm, "H_SUBCLASSCD", $model->replace_data["SUBCLASSCD"]);

        //辞退チェックボックスを作成
        $extra  = ($model->replace_data["DECLINE_FLG"] == "1")? "checked" : "";
        $extra .= " id=\"DECLINE\"";
        $arg["data"]["DECLINE_FLG"] = knjCreateCheckBox($objForm, "DECLINE_FLG", "1", $extra, "");
        knjCreateHidden($objForm, "H_DECLINE_FLG", $model->replace_data["DECLINE_FLG"]);

        //対象者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\" ";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "left_select", "left", $opt_left, $extra, 20);

        //生徒一覧
        $query = knjh703Query::getStudentInfo($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array("label" => $row["NAME"],
                                 "value" => $row["SCHREGNO"]);
        }
        $result->free();
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\" ";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_select", "left", $opt_right, $extra, 20);

        //クラス名
        $query = knjh703Query::getHrClassName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $className = $row["HR_NAME"];
        }
        $result->free();

        $arg["info"] = array("TOP"        => CTRL_YEAR."年度  ".$className,
                             "LEFT_LIST"  => "対象生徒一覧",
                             "RIGHT_LIST" => "生徒一覧");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm, $model);

        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjh703SubForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn($objForm, &$arg)
{
    //更新・戻るボタン
    $extra = "onclick=\"return doSubmit('replace_update')\"";
    //更新
    $arg["BUTTONS"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻るボタン
    $link = REQUESTROOT."/H/KNJH703/knjh703index.php?cmd=back";
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["BUTTONS"] .= "    ".knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

    //全て追加
    $extra = "onclick=\"return move('sel_add_all','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    //追加
    $extra = "onclick=\"return move('left','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //削除
    $extra = "onclick=\"return move('right','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //全て削除
    $extra = "onclick=\"return move('sel_del_all','left_select','right_select',1);\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);
}

//Hidden作成
function makeHidden($objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "GRADE_HR_CLASS", $model->grade_hr_class);
}
