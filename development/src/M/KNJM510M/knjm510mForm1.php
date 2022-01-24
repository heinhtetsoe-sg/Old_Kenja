<?php

require_once('for_php7.php');


class knjm510mForm1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm510mForm1", "POST", "knjm510mindex.php", "", "knjm510mForm1");

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR
                            ) );

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTER;

        //学期
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"     => CTRL_SEMESTER
                            ) );

        //クラス選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query = knjm510mQuery::getClass();

        $result = $db->query($query);
        $grade_hr_class_flg = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) {
                $grade_hr_class_flg = true;
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        if (!isset($model->field["GRADE_HR_CLASS"]) || !$grade_hr_class_flg) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjm510m');\"",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //生徒選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query = knjm510mQuery::getSch($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[]= array('label' => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番　".$row["NAME"],
                           'value' => $row["SCHREGNO"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        //対象者リストを作成する
        $objForm->ae( array("type"      => "select",
                            "name"      => "category_name",
                            "extrahtml" => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"",
                            "size"      => "20",
                            "options"   => array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"      => "select",
                            "name"      => "category_selected",
                            "extrahtml" => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"",
                            "size"      => "20",
                            "options"   => isset($opt1)?$opt1:array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        //対象取り消しボタンを作成する(個別)
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_right1",
                            "value"     => "　＞　",
                            "extrahtml" => " onclick=\"move('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取り消しボタンを作成する(全て)
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_right2",
                            "value"     => "　≫　",
                            "extrahtml" => " onclick=\"move('rightall');\"" ) );

        $arg["button"]["btn_right2"] = $objForm->ge("btn_right2");

        //対象選択ボタンを作成する(個別)
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_left1",
                            "value"     => "　＜　",
                            "extrahtml" => " onclick=\"move('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //対象選択ボタンを作成する(全て)
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_left2",
                            "value"     => "　≪　",
                            "extrahtml" => " onclick=\"move('leftall');\"" ) );

        $arg["button"]["btn_left2"] = $objForm->ge("btn_left2");

        //印刷対象ラジオボタンを作成
        $opt2 = array(1, 2, 3, 4, 5);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $disabled = " onclick=\"radioChange('this');\"";
        $extra = array("id=\"OUTPUT1\"".$disabled, "id=\"OUTPUT2\"".$disabled, "id=\"OUTPUT3\"".$disabled, "id=\"OUTPUT4\"".$disabled, "id=\"OUTPUT5\"".$disabled);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt2, get_count($opt2));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷対象ラジオボタンを作成
        $opt3 = array(1, 2);
        $model->field["OUTPUT2"] = ($model->field["OUTPUT2"] == "") ? "1" : $model->field["OUTPUT2"];
        $extra = array("id=\"OUTPUT21\"", "id=\"OUTPUT22\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT2", $model->field["OUTPUT2"], $extra, $opt3, get_count($opt3));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //生徒名チェックボックスを作成
        if ($model->field["CHECK1"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["CHECK1"] = knjCreateCheckBox($objForm, "CHECK1", "1", $extra);

        //学籍番号チェックボックスを作成
        if ($model->field["OUTPUT"] == "5") {
            $disabled = "";
        } else {
            $disabled = "disabled";
        }
        if ($model->field["CHECK2"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["CHECK2"] = knjCreateCheckBox($objForm, "CHECK2", "1", $extra.$disabled);

        //出力条件チェックボックス
        $extra = ($model->field["GRDDIV"] == "1") ? "checked" : "";
        $extra .= " id=\"GRDDIV\"";
        $arg["data"]["GRDDIV"] = knjCreateCheckBox($objForm, "GRDDIV", "1", $extra, "");

        //印刷ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_print",
                            "value"     => "プレビュー／印刷",
                            "extrahtml" => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJM510M"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm510mForm1.html", $arg); 
    }
}
?>
