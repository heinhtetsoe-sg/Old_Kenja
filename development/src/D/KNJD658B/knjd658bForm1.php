<?php

require_once('for_php7.php');


class knjd658bForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd658bForm1", "POST", "knjd658bindex.php", "", "knjd658bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => CTRL_YEAR ) );

        /******************/
        /* コンボボックス */
        /******************/
        //学期
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        $query = knjd658bQuery::getSemesterMst();
        $extra = "onchange=\"return btn_submit('knjd658b')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年
        $query = knjd658bQuery::getSelectGrade($model);
        $extra = "onchange=\"return btn_submit('knjd658b')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1);
        //テスト種別
        $query = knjd658bQuery::getTestKind($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], $extra, 1);

        //クラス一覧リスト作成する
        $query = knjd658bQuery::getStudent($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "CATEGORY_NAME",
                            "extrahtml"  => "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('left')\"",
                            "size"       => "15",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("CATEGORY_NAME");

        //出力対象クラスリストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "CATEGORY_SELECTED",
                            "extrahtml"  => "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('right')\"",
                            "size"       => "15",
                            "options"    => array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("CATEGORY_SELECTED");

        //対象選択ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象取消ボタンを作成する（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


        //教科一覧リスト作成する
        $query = knjd658bQuery::getMock2Dat();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row2[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        //出力順位
        $opt = array(1, 2);
        $model->field["JUNI"] = ($model->field["JUNI"] == "") ? "1" : $model->field["JUNI"];
        $extra = array("id=\"JUNI1\"", "id=\"JUNI2\"");
        $radioArray = knjCreateRadio($objForm, "JUNI", $model->field["JUNI"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //順位の基準点
        $opt = array(1, 2);
        $model->field["KIJUNTEN"] = ($model->field["KIJUNTEN"] == "") ? "1" : $model->field["KIJUNTEN"];
        $extra = array("id=\"KIJUNTEN1\"", "id=\"KIJUNTEN2\"");
        $radioArray = knjCreateRadio($objForm, "KIJUNTEN", $model->field["KIJUNTEN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //最大科目数
        $opt = array(1, 2);
        $model->field["SAIDAIKAMOKU"] = ($model->field["SAIDAIKAMOKU"] == "") ? "1" : $model->field["SAIDAIKAMOKU"];
        $extra = array("id=\"SAIDAIKAMOKU1\"", "id=\"SAIDAIKAMOKU2\"");
        $radioArray = knjCreateRadio($objForm, "SAIDAIKAMOKU", $model->field["SAIDAIKAMOKU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //偏差値出力チェックボックス
        $extra = ($model->field["KANSAN"] == "1") ? "checked" : "";
        $extra .= " id=\"KANSAN\"";
        $arg["data"]["KANSAN"] = knjCreateCheckBox($objForm, "KANSAN", "1", $extra, "");

        if ($model->schoolName == 'CHIBEN') {
            $arg["KANSAN_FLG"] = '1';
        } else {
            unset($arg["KANSAN_FLG"]);
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "MOCK_NAME",
                            "extrahtml"  => "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move2('left')\"",
                            "size"       => "10",
                            "options"    => isset($row2)?$row2:array()));

        $arg["data"]["MOCK_NAME"] = $objForm->ge("MOCK_NAME");

        //出力対象教科リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "MOCK_SELECTED",
                            "extrahtml"  => "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move2('right')\"",
                            "size"       => "10",
                            "options"    => array()));

        $arg["data"]["MOCK_SELECTED"] = $objForm->ge("MOCK_SELECTED");

        //対象選択ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn2_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move2('right');\"" ) );

        $arg["button"]["btn2_right1"] = $objForm->ge("btn2_right1");

        //対象取消ボタンを作成する（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn2_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move2('left');\"" ) );

        $arg["button"]["btn2_left1"] = $objForm->ge("btn2_left1");


        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $objForm->ae( array("type"           => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する(必須)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD658B"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd658bForm1.html", $arg); 
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $gradeH3 = "";
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
