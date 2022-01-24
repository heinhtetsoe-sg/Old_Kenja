<?php

require_once('for_php7.php');

class knjh340aForm2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh340aindex.php", "", "edit");
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
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        /**********/
        /* テスト */
        /**********/
        $query = knjh340aQuery::getMockName($model);
        $mockName = $db->getOne($query);
        $arg["data"]["MOCKNAME"] = $mockName;

        /************************/
        /* コース区分コンボ作成 */
        /************************/
        $opt   = array();
        $opt[] = array("label" => "",
                       "value" => "");
        $query = knjh340aQuery::getCourseDiv();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["COURSE_DIV"] = knjCreateCombo($objForm, "COURSE_DIV", $Row["COURSE_DIV"], $opt, $extra, 1);

        /**************************/
        /* 科目コンボボックス作成 */
        /**************************/
        $opt   = array();
        $opt[] = array("label" => "",
                       "value" => "");
        $query = knjh340aQuery::getMockSubclassCd();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $objForm->ae( array("type"      => "select",
                            "name"      => "MOCK_SUBCLASS_CD",
                            "value"     => $Row["MOCK_SUBCLASS_CD"],
                            "options"   => $opt,
                            "extrahtml" => ""
                            ));
        $arg["data"]["MOCK_SUBCLASS_CD"] = $objForm->ge("MOCK_SUBCLASS_CD");

        /********************/
        /* 区分ラジオボタン */
        /********************/
        $opt = array("01", "02", "03");
        $model->field["DIV"] = $model->field["DIV"] ? $model->field["DIV"] : "01";
        $onClick = " onClick=\"btn_submit('edit');\"";
        $extra = array("id=\"DIV1\"".$onClick, "id=\"DIV2\"".$onClick, "id=\"DIV3\"".$onClick);
        createRadio($objForm, $arg, "DIV", $model->field["DIV"], $extra, $opt, get_count($opt));

        /**********************/
        /* 学年コンボボックス */
        /**********************/
        $opt   = array();
        $opt[] = array("label" => "",
                       "value" => "");
        $query = knjh340aQuery::getGrade();
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
        $query = knjh340aQuery::getCourse();
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
        $query = knjh340aQuery::getMajor($Row["COURSECD"]);
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
        $query = knjh340aQuery::getCoursecode();
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
        $extra = "onblur=\"calc(this);\" style=\"text-align: right\"";
        $arg["data"]["PERFECT"] = knjCreateTextBox($objForm, $Row["PERFECT"], "PERFECT", 3, 3, $extra);
        /**********/
        /* 合格点 */
        /**********/
        $extra = "onblur=\"calc(this);\" style=\"text-align: right\"";
        $arg["data"]["PASS_SCORE"] = knjCreateTextBox($objForm, $Row["PASS_SCORE"], "PASS_SCORE", 3, 3, $extra);

        //追加ボタンを作成する
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return doSubmit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタンを作成する
        $objForm->ae( array("type" 		  => "button",
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
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SEMESTER", $model->semester);
        knjCreateHidden($objForm, "MOCKCD", $model->mockcd);


        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        if (VARS::post("cmd") == "update" || VARS::post("cmd") == "add" || VARS::post("cmd") == "delete") {
            $arg["reload"]  = "window.open('knjh340aindex.php?cmd=list_update','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh340aForm2.html", $arg);
    }
}

//ラジオ作成
function createRadio(&$objForm, &$arg, $name, $value, $extra, $multi, $count)
{
    for ($i = 1; $i <= $count; $i++) {
        if (is_array($extra)) $ext = $extra[$i-1];
        else $ext = $extra;
        
        $objForm->ae( array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $ext,
                            "multiple"  => $multi));

        if($name == "DIV") {
            $arg["data"][$name.$i]  = $objForm->ge($name, sprintf("%02d",$i));
        } else {
            $arg["data"][$name.$i]  = $objForm->ge($name, $i);
        }
    }
}
?>
