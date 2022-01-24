<?php

require_once('for_php7.php');


class knjd122pForm1
{
    function main(&$model){

        $objForm = new form;

        $arg["start"]   = $objForm->get_start("knjd122pForm1", "POST", "knjd122pindex.php", "", "knjd122pForm1");

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
        $query = knjd122pQuery::selectSubclassQuery($model);
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
                            "extrahtml"  => "onchange=\"return btn_submit('knjd122p'),AllClearList();\"",
                            "options"    => $opt_subcd));
        $arg["data"]["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");

        $db = Query::dbCheckOut();
        $opt_chrcd = array();
        $query = knjd122pQuery::selectChairQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_chrcd[] = array('label' => $row["CHAIRCD"] ."：" .$row["CHAIRNAME"],
                                 'value' => $row["CHAIRCD"]);
        }
        $result->free();
        Query::dbCheckIn($db);


        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => $opt_chrcd));
        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");


        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width=230px\" width=\"230px\" ondblclick=\"move1('right')\"",
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
                            "name"      => "TEST_DATE",
                            "value"      => CTRL_DATE
                            ) );

        //累積現在日
        $db = Query::dbCheckOut();
        $cur_date = $db->getRow(knjd122pQuery::GetMax($model->field["SUBCLASSCD"], $model), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTENDSUBYEAR",
                            "value"     => $cur_date["NEN_YEAR"] )  );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTENDSUBMONTH",
                            "value"     => $cur_date["MONTH"] )  );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ATTENDSUBDAY",
                            "value"     => $cur_date["APPOINTED_DAY"] )  );


        $db = Query::dbCheckOut();
        $tmp_msg = "";
        $absent_month = array();
        $query = knjd122pQuery::getAbsentMonth();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMESPARE1"] == "" || $row["NAMESPARE2"] == "") {
                $tmp_msg .= $row["NAME1"] ."\n";
                continue;
            }
            $smon = (3 < (int)$row["NAMESPARE1"]) ? (int)$row["NAMESPARE1"] : (int)$row["NAMESPARE1"] + 12;
            $emon = (3 < (int)$row["NAMESPARE2"]) ? (int)$row["NAMESPARE2"] : (int)$row["NAMESPARE2"] + 12;
            $tmp_month = $seq = "";
            for ($i = $smon; $i <= $emon; $i++) {
                $mon = ($i < 13) ? $i : ($i-12);
                $tmp_month .= $seq .sprintf("%02d",$mon);
                $seq = ",";
            }
            $absent_month[$row["NAMECD2"]] = $tmp_month;
        }
        $result->free();

        //休学時の欠課をカウントするかどうかのフラグ(1 or NULL)を取得。1:欠課をカウントする
        $offdaysFlg = $db->getRow(knjd122pQuery::getOffdaysFlg(),DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "SUB_OFFDAYS", $offdaysFlg["SUB_OFFDAYS"]);
        knjCreateHidden($objForm, "SUB_ABSENT", $offdaysFlg["SUB_ABSENT"]);
        knjCreateHidden($objForm, "SUB_SUSPEND", $offdaysFlg["SUB_SUSPEND"]);
        knjCreateHidden($objForm, "SUB_MOURNING", $offdaysFlg["SUB_MOURNING"]);
        knjCreateHidden($objForm, "SUB_VIRUS", $offdaysFlg["SUB_VIRUS"]);
        knjCreateHidden($objForm, "SUB_KOUDOME", $offdaysFlg["SUB_KOUDOME"]);

        Query::dbCheckIn($db);

        if ($tmp_msg != "") {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ABSENT_MONTH1",
                            "value"     => $absent_month["0111"] )  );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ABSENT_MONTH2",
                            "value"     => $absent_month["0121"] )  );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ABSENT_MONTH3",
                            "value"     => $absent_month["0211"] )  );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ABSENT_MONTH4",
                            "value"     => $absent_month["0221"] )  );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ABSENT_MONTH5",
                            "value"     => $absent_month["0231"] )  );


        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJD122P"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        //テスト項目マスタ
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "COUNTFLG",
                            "value"     => $model->testTable ) );
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd122pForm1.html", $arg); 
    }
}
?>
