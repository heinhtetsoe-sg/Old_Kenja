<?php

require_once('for_php7.php');

class knjz237Form2 {
    function main(&$model) {
    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("edit", "POST", "knjz237index.php", "", "edit");
    $db = Query::dbCheckOut();

    $Row =& $model->field;

    if ($model->field["DIV"] == "02") {
        $arg["view1"] = 1;
    } elseif ($model->field["DIV"] == "03") {
        $arg["view1"] = 1;
        $arg["view2"] = 1;
    }

    /********/
    /* 学期 */
    /********/
    $query = knjz237Query::getSemesterName($model->semester);
    $semesterName = $db->getOne($query);
    $arg["data"]["SEMESTER"] = $semesterName;

    /**********/
    /* テスト */
    /**********/
    $query = knjz237Query::getTestName($model->testTable, $model->test, $model->semester);
    $testName = $db->getOne($query);
    if (!$testName && preg_match("/^99/", $model->test) && $model->testTable == "TESTITEM_MST_COUNTFLG") {
        $testName = "評価成績";
    }
    $arg["data"]["TEST"] = $testName;

    /**************************/
    /* 科目コンボボックス作成 */
    /**************************/

    $opt = array();
    $value_flg = false;
    //教育課程対応
    if ($model->Properties["useCurriculumcd"] == '1') {
        $value = $model->field["CLASSCD"]."-".$model->field["SCHOOL_KIND"]."-".$model->field["CURRICULUM_CD"]."-".$model->field["SUBCLASSCD"];
        if (VARS::post("cmd") == 'edit' || VARS::post("cmd") == 'update' || VARS::post("cmd") == 'add' || VARS::post("cmd") == 'delete') {
            $value = $model->field["SUBCLASSCD"];
            $model->field["CLASSCD"]    = substr($model->field["SUBCLASSCD"],0,2);
            $model->field["SUBCLASSCD"] = substr($model->field["SUBCLASSCD"],7,6);
        }
    } else {
        $value = $model->field["SUBCLASSCD"];
    }
    $query = knjz237Query::getSubclass($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $extra = "";
    $arg["data"]["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $value, $opt, $extra, 1);

    /********************/
    /* 区分ラジオボタン */
    /********************/
    $opt = array("01", "02", "03");
    $model->field["DIV"] = $model->field["DIV"] ? $model->field["DIV"] : "01";
    $click = " onClick=\"btn_submit('edit');\"";
    $extra = array("id=\"DIV1\"".$click, "id=\"DIV2\"".$click, "id=\"DIV3\"".$click);
    $radioArray = knjCreateRadio($objForm, "DIV", $model->field["DIV"], $extra, $opt, get_count($opt));
    foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

    /**********************/
    /* 学年コンボボックス */
    /**********************/
    $opt   = array();
    $opt[] = array("label" => "",
                   "value" => "");
    $query = knjz237Query::getGrade();
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row["LABEL"] = preg_replace("/^0*/i", "", $row["LABEL"]);
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "GRADE",
                        "value"     => $Row["GRADE"],
                        "options"   => $opt,
                        "extrahtml" => ""
                        ));
    $arg["data"]["GRADE"] = $objForm->ge("GRADE");

    /**********************/
    /* 課程コンボボックス */
    /**********************/
    $opt   = array();
    $opt[] = array("label" => "",
                   "value" => "");
    $query = knjz237Query::getCourse();
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "COURSECD",
                        "value"     => $Row["COURSECD"],
                        "options"   => $opt,
                        "extrahtml" => "onChange=\"document.forms[0].MAJORCD.value = ''; btn_submit('edit');\""
                        ));
    $arg["data"]["COURSECD"] = $objForm->ge("COURSECD");

    /**********************/
    /* 学科コンボボックス */
    /**********************/
    $opt   = array();
    $opt[] = array("label" => "",
                   "value" => "");
    $query = knjz237Query::getMajor($Row["COURSECD"]);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "MAJORCD",
                        "value"     => $Row["MAJORCD"],
                        "options"   => $opt,
                        "extrahtml" => ""
                        ));
    $arg["data"]["MAJORCD"] = $objForm->ge("MAJORCD");

    /************************/
    /* コースコンボボックス */
    /************************/
    $opt   = array();
    $opt[] = array("label" => "",
                   "value" => "");
    $query = knjz237Query::getCoursecode();
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "COURSECODE",
                        "value"     => $Row["COURSECODE"],
                        "options"   => $opt,
                        "extrahtml" => ""
                        ));
    $arg["data"]["COURSECODE"] = $objForm->ge("COURSECODE");

    /********/
    /* 満点 */
    /********/
    $objForm->ae( array("type"        => "text",
                        "name"        => "PERFECT",
                        "size"        => 3,
                        "maxlength"   => 3,
                        "extrahtml"   => " onblur=\"calc(this);\" ",
                        "value"       => $Row["PERFECT"] ));
    $arg["data"]["PERFECT"] = $objForm->ge("PERFECT");

    /********/
    /* 満点 */
    /********/
    $objForm->ae( array("type"        => "text",
                        "name"        => "PASS_SCORE",
                        "size"        => 3,
                        "maxlength"   => 3,
                        "extrahtml"   => " onblur=\"calc(this);\" ",
                        "value"       => $Row["PASS_SCORE"] ));
    $arg["data"]["PASS_SCORE"] = $objForm->ge("PASS_SCORE");

    //追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_add",
                        "value"       => "追 加",
                        "extrahtml"   => "onclick=\"return doSubmit('add');\"" ) );

    $arg["button"]["btn_add"] = $objForm->ge("btn_add");

    //更新ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_udpate",
                        "value"       => "更 新",
                        "extrahtml"   => "onclick=\"return doSubmit('update');\"" ) );

    $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

    //削除ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_del",
                        "value"       => "削 除",
                        "extrahtml"   => "onclick=\"return doSubmit('delete');\"" ) );

    $arg["button"]["btn_del"] = $objForm->ge("btn_del");

    //クリアボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_reset",
                        "value"       => "取 消",
                        "extrahtml"   => "onclick=\"return doSubmit('reset')\"" ) );

    $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

    //終了ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_end",
                        "value"       => "終 了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ) );

    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

    //hiddenを作成する
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SEMESTER", $model->semester);
    knjCreateHidden($objForm, "TEST", $model->test);


    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    if (VARS::post("cmd") == "update" || VARS::post("cmd") == "add" || VARS::post("cmd") == "delete") {
        $arg["reload"]  = "window.open('knjz237index.php?cmd=list_update','left_frame');";
    }

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjz237Form2.html", $arg);
    }
}
?>
