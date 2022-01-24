<?php

require_once('for_php7.php');


class knjc153aForm1
{
    function main(&$model){

        $objForm = new form;

        $arg["start"]   = $objForm->get_start("knjc153aForm1", "POST", "knjc153aindex.php", "", "knjc153aForm1");

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
        $query = knjc153aQuery::getSubclassSaki($model);
        $result = $db->query($query);
        $subclasscdSaki = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclasscdSaki[] = $row["SUBCLASSCD"];
        }
        $result->free();

        $opt_subcd = array();
        $query = knjc153aQuery::selectSubclassQuery($model);
        $result = $db->query($query);
        $selectSchoolKind = "";
        $schoolKind = array();
        //教育課程対応
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subclasscd = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"];
            if (!in_array($row["SCHOOL_KIND"], $schoolKind)) {
                $schoolKind[] = $row["SCHOOL_KIND"];
            }
            if ($subclasscd == $model->field["SUBCLASSCD"]) {
                $selectSchoolKind = $row["SCHOOL_KIND"];
            }
            $saki = (in_array($subclasscd, $subclasscdSaki)) ? "●" : "　";
            $opt_subcd[] = array('label' => $saki . $subclasscd."：".$row["SUBCLASSNAME"],
                                 'value' => $subclasscd);
        }
        $result->free();

        if (!isset($model->field["SUBCLASSCD"])) $model->field["SUBCLASSCD"] = $opt_subcd[0]["value"];

        $objForm->ae( array("type"       => "select",
                            "name"       => "SUBCLASSCD",
                            "size"       => "1",
                            "value"      => $model->field["SUBCLASSCD"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjc153a'),AllClearList();\"",
                            "options"    => $opt_subcd));
        $arg["data"]["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");

        $opt_chrcd = array();
        $query = knjc153aQuery::selectChairQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_chrcd[] = array('label' => $row["CHAIRCD"] ."：" .$row["CHAIRNAME"],
                                 'value' => $row["CHAIRCD"]);
        }
        $result->free();

        //出欠集計日付作成
        if ($model->field["SDATE"] == "") {
            $model->field["SDATE"] = $db->getOne(knjc153aQuery::getSemesterSdate());
        } 
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", str_replace("-", "/", $model->field["SDATE"]));

        //出欠集計日付作成
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => $opt_chrcd));
        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");


        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:250px\" width=\"250px\" ondblclick=\"move1('right')\"",
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

        //年度日付範囲
        $query = knjc153aQuery::getSemester9();
        $result = $db->query($query);
        $yearSdate = CTRL_DATE;
        $yearEdate = CTRL_DATE;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $yearSdate = $row["SDATE"];
            $yearEdate = $row["EDATE"];
        }
        $result->free();

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


        knjCreateHidden($objForm, "STAFF", STAFFCD);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC153A");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COUNTFLG", $model->testTable);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);

        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);
        knjCreateHidden($objForm, "YEAR_SDATE", str_replace('-', '/', $yearSdate));
        knjCreateHidden($objForm, "YEAR_EDATE", str_replace('-', '/', $yearEdate));
        knjCreateHidden($objForm, "PRINT_JUGYO_JISU", "1"); //帳票に授業時数欄を追加
        knjCreateHidden($objForm, "useRemarkFlg", $model->Properties["useRemarkFlg"]);
        knjCreateHidden($objForm, "useMikomiFlg", $model->Properties["useMikomiFlg"]);

        $arg["finish"]  = $objForm->get_finish();

        Query::dbCheckIn($db);

        View::toHTML($model, "knjc153aForm1.html", $arg); 
    }
}
?>
