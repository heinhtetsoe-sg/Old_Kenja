<?php

require_once('for_php7.php');


class knjd620lForm1
{
    function main(&$model){

        $objForm = new form;

        $arg["start"]   = $objForm->get_start("knjd620lForm1", "POST", "knjd620lindex.php", "", "knjd620lForm1");

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"      => CTRL_YEAR) );
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $ga = CTRL_SEMESTER;
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"      => CTRL_SEMESTER) );
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        $db = Query::dbCheckOut();
        $opt_subcd = array();
        $query = knjd620lQuery::selectSubclassQuery($model);
        $result = $db->query($query);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_subcd[] = array('label' => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]."：".$row["SUBCLASSNAME"],
                                     'value' => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]);
            }
        } else {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_subcd[] = array('label' => $row["SUBCLASSCD"] ."：" .$row["SUBCLASSNAME"],
                                     'value' => $row["SUBCLASSCD"]);
            }
        }
        $result->free();
        Query::dbCheckIn($db);

        if (!isset($model->field["SUBCLASSCD"])) $model->field["SUBCLASSCD"] = $opt_subcd[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "SUBCLASSCD",
                            "size"       => "1",
                            "value"      => $model->field["SUBCLASSCD"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjd620l'),AllClearList();\"",
                            "options"    => $opt_subcd));
        $arg["data"]["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");

        $db = Query::dbCheckOut();
        $opt_chrcd = array();
        $query = knjd620lQuery::selectChairQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_chrcd[] = array('label' => $row["CHAIRCD"] ."：" .$row["CHAIRNAME"],
                                 'value' => $row["CHAIRCD"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        //出欠集計日付作成
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => $opt_chrcd));
        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");


        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"",
                            "size"       => "20",
                            "options"    => array()));
        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");


        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );
        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );
        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );
        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );
        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //出力選択ラジオ
        $radioValue = array(1, 2);
        if ($model->field["PRINT_DIV"] == '') $model->field["PRINT_DIV"] = '1';
        $radioArray = knjCreateRadio($objForm, "PRINT_DIV", $model->field["PRINT_DIV"], $extra, $radioValue, get_count($radioValue));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********************/
        /* チェックボックス */
        /********************/
        //欠課時数を0表記する
        $extra  = ($model->field["PRINT_KEKKA0"] == "1") ? "checked='checked' " : "";
        $extra .= " id='PRINT_KEKKA0'";
        $arg["data"]["PRINT_KEKKA0"] = knjCreateCheckBox($objForm, "PRINT_KEKKA0", "1", $extra);

        //教科正担任をすべて出力（最大4名まで）
        $extra  = ($model->field["PRINT_TANNIN"] == "1") ? "checked='checked' " : "";
        $extra .= " id='PRINT_TANNIN'";
        $arg["data"]["PRINT_TANNIN"] = knjCreateCheckBox($objForm, "PRINT_TANNIN", "1", $extra);

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );
        $arg["button"]["btn_print"] = $objForm->ge("btn_print");


        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STAFF",
                            "value"      => STAFFCD
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD620L"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        //テスト項目マスタ
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "COUNTFLG",
                            "value"     => $model->testTable ) );

        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);

        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd620lForm1.html", $arg); 
    }
}
?>
