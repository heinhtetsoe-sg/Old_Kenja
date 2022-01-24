<?php

require_once('for_php7.php');

class knja110bSubForm1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knja110bindex.php", "", "sel");
        $arg["jscript"] = "";
        //更新する内容があった場合に日付を入力させる。
        if ($model->cmd == "subReplace") {
            $arg["reload"] = " window.onload=function() {loadCheck('".REQUESTROOT."')};";
        }

        if ($model->schoolKind == "K") {
            $arg["KIND_K"] = "1";
        } else {
            $arg["UNKIND_K"] = "1";
        }

        //生徒一覧
        $opt_left = $opt_right = array();

        $array = explode(",", $model->replace_data["selectdata"]);

        //リストが空であれば置換処理選択時の生徒を加える
        if ($array[0]=="") $array[0] = $model->schregno;

        if ($model->cmd == 'subReplace') {
            $Row = $model->field;
        } else {
            $Row = knja110bQuery::getStudent_data($array[0], $model);
        }
        //生徒情報

        if (!is_array($Row)) {
            $Row["FINSCHOOLCD"] = $model->replace_data["FINSCHOOLCD"];
        }
        $db = Query::dbCheckOut();

        $result = $db->query(knja110bQuery::GetStudent($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if (!in_array($row["SCHREGNO"], $array)){
                $opt_right[]  = array("label" => $row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"],
                                     "value" => $row["SCHREGNO"]);
            } else {
                $opt_left[] = array("label" => $row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"],
                                     "value" => $row["SCHREGNO"]);
            }
        }

        $result->free();

        //チェックボックス
        for ($i=0;$i<20;$i++)
        {
            $extra = "";
            if ($i==19) {
                $extra = "onClick=\"return check_all(this);\"";
            }

            $objForm->ae(array("type"       => "checkbox",
                                "name"      => "RCHECK".$i,
                                "value"     => "1",
                                "extrahtml" => $extra,
                                "checked"   => (($model->replace_data["check"][$i] == "1") ? 1 : 0)));
            $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
        }

        //出身学校
        if ($model->schoolKind == "P" || $model->schoolKind == "K") {
            $extra = "";
            $arg["data"]["NYUGAKUMAE_SYUSSIN_JOUHOU"] = knjCreateTextArea($objForm, "NYUGAKUMAE_SYUSSIN_JOUHOU", "4", "51", "soft", $extra, $Row["NYUGAKUMAE_SYUSSIN_JOUHOU"]);
        } else {
            //出身学校
            $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
            $arg["button"]["btn_searchfs"] = knjCreateBtn($objForm, "btn_searchfs", "検 索", $extra);
            $extra = "";
            $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", 7, 7, $extra);
            $finschoolname = $db->getOne(knja110bQuery::getFinschoolName($Row["FINSCHOOLCD"]));
            $arg["data"]["FINSCHOOLNAME"] = $Row["FINSCHOOLNAME"] ? $Row["FINSCHOOLNAME"] : $finschoolname;

            //出身学校卒業年月日
            $arg["data"]["FINISH_DATE"] = View::popUpCalendar($objForm, "FINISH_DATE", str_replace("-","/",$Row["FINISH_DATE"]),"");
        }

        //内外区分
        $arg["data"]["INOUTCD"] = $model->CreateCombo($objForm,$db,"A001","INOUTCD",$Row["INOUTCD"],1);

        //生年月日
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar($objForm, "BIRTHDAY", str_replace("-","/",$Row["BIRTHDAY"]),"");

        //入学区分
        $arg["data"]["ENT_DIV"] = $model->CreateCombo($objForm,$db,"A002","ENT_DIV",$Row["ENT_DIV"],1);

        //入学日付
        $arg["data"]["ENT_DATE"] = View::popUpCalendar($objForm, "ENT_DATE", str_replace("-","/",$Row["ENT_DATE"]),"");

        //課程入学年度
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["CURRICULUM_YEAR"] = knjCreateTextBox($objForm, $Row["CURRICULUM_YEAR"], "CURRICULUM_YEAR", 4, 4, $extra);

        //事由
        $objForm->ae( array("type"      => "text",
                            "name"      => "ENT_REASON",
                            "size"      => 33,
                            "maxlength" => 75,
                            "value"     => $Row["ENT_REASON"]));

        $arg["data"]["ENT_REASON"] = $objForm->ge("ENT_REASON");

        //学校名
        $objForm->ae( array("type"      => "text",
                            "name"      => "ENT_SCHOOL",
                            "value"     => $Row["ENT_SCHOOL"],
                            "size"      => 33,
                            "maxlength" => 75));

        $arg["data"]["ENT_SCHOOL"] = $objForm->ge("ENT_SCHOOL");

        //学校住所1
        $objForm->ae( array("type"      => "text",
                            "name"      => "ENT_ADDR",
                            "value"     => $Row["ENT_ADDR"],
                            "size"      => 33,
                            "maxlength" => 90));

        $arg["data"]["ENT_ADDR"] = $objForm->ge("ENT_ADDR");

        //住所２使用
        if ($model->Properties["useAddrField2"] == "1" && $model->schoolKind != 'H') {
            $arg["useAddrField2"] = $model->Properties["useAddrField2"];
        } else if ($model->schoolKind == 'H') {
            $arg["hyoujiFieldCourse"] = "1";
        }

        //学校住所2
        $objForm->ae( array("type"      => "text",
                            "name"      => "ENT_ADDR2",
                            "value"     => $Row["ENT_ADDR2"],
                            "size"      => 33,
                            "maxlength" => 90));

        $arg["data"]["ENT_ADDR2"] = $objForm->ge("ENT_ADDR2");



        //卒業区分
        $arg["data"]["GRD_DIV"] = $model->CreateCombo($objForm,$db,"A003","GRD_DIV",$Row["GRD_DIV"],1);

        //事由
        $objForm->ae( array("type"      => "text",
                            "name"      => "GRD_REASON",
                            "size"      => 33,
                            "maxlength" => 75,
                            "value"     => $Row["GRD_REASON"]));

        $arg["data"]["GRD_REASON"] = $objForm->ge("GRD_REASON");

        //学校名
        $objForm->ae( array("type"      => "text",
                            "name"      => "GRD_SCHOOL",
                            "value"     => $Row["GRD_SCHOOL"],
                            "size"      => 33,
                            "maxlength" => 75));

        $arg["data"]["GRD_SCHOOL"] = $objForm->ge("GRD_SCHOOL");

        //学校住所1
        $objForm->ae( array("type"      => "text",
                            "name"      => "GRD_ADDR",
                            "value"     => $Row["GRD_ADDR"],
                            "size"      => 33,
                            "maxlength" => 90));

        $arg["data"]["GRD_ADDR"] = $objForm->ge("GRD_ADDR");

        //学校住所2
        $objForm->ae( array("type"      => "text",
                            "name"      => "GRD_ADDR2",
                            "value"     => $Row["GRD_ADDR2"],
                            "size"      => 33,
                            "maxlength" => 90));

        $arg["data"]["GRD_ADDR2"] = $objForm->ge("GRD_ADDR2");


        //卒業年月日
        $arg["data"]["GRD_DATE"] = View::popUpCalendar($objForm, "GRD_DATE", str_replace("-","/",$Row["GRD_DATE"]),"");

        //課程学科
        $result       = $db->query(knja110bQuery::getCourse_Subject());
        $opt_coursecd = array();
        while($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_coursecd[] = array("label" => str_replace(",","",$row2["COURSEMAJORCD"])."  ".htmlspecialchars($row2["COURSE_SUBJECT"]),
                                    "value" => $row2["COURSEMAJORCD"]);
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "COURSEMAJORCD",
                            "size"       => 1,
                            "maxlength"  => 10,
#                            "extrahtml" => "style=width:\"100%\"",
                            "extrahtml"  => "",
                            "value"      => $Row["COURSEMAJORCD"],
                            "options"    => $opt_coursecd ) );

        $arg["data"]["COURSEMAJORCD"] = $objForm->ge("COURSEMAJORCD");

        //コース
        $result = $db->query(knja110bQuery::getCourseCode());
        $opt = array();
        while($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
              $opt[] = array("label" => $row2["COURSECODE"]."  ".htmlspecialchars($row2["COURSECODENAME"]),
                                       "value" => $row2["COURSECODE"]);
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "COURSECODE",
                            "size"       => 1,
                            "maxlength"  => 10,
                            "extrahtml"  => "",
                            "value"      => $Row["COURSECODE"],
                            "options"    => $opt));
        $arg["data"]["COURSECODE"] = $objForm->ge("COURSECODE");

        //年組名
        $hr_name = $db->getOne(knja110bQuery::getHR_Name($model));

        Query::dbCheckIn($db);

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit('replace_update')\"" ) );

        //戻るボタン
        $link = REQUESTROOT."/A/KNJA110B/knja110bindex.php?cmd=back&ini2=1";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"window.open('$link','_self');\"" ) );

        $arg["BUTTONS"] = $objForm->ge("btn_update")."    ".$objForm->ge("btn_back");

        //対象生徒
        $objForm->ae( array("type"        => "select",
                            "name"        => "left_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\" ",
                            "options"     => $opt_left));
        //その他の生徒
        $objForm->ae( array("type"        => "select",
                            "name"        => "right_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\" ",
                            "options"     => $opt_right));

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','left_select','right_select',1);\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','left_select','right_select',1);\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("left_select"),
                                   "RIGHT_PART"  => $objForm->ge("right_select"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

        $arg["info"] = array("TOP"        =>  sprintf("%d年度  %s  対象クラス  %s",
                                                CTRL_YEAR,$model->control_data["学期名"][CTRL_SEMESTER],$hr_name),
                             "LEFT_LIST"  => "対象者一覧",
                             "RIGHT_LIST" => "生徒一覧");
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "E_APPDATE") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata") );

        knjCreateHidden($objForm, "GRADE_FLG");
        knjCreateHidden($objForm, "HR_CLASS_FLG");
        knjCreateHidden($objForm, "ATTENDNO_FLG");
        knjCreateHidden($objForm, "ANNUAL_FLG");
        knjCreateHidden($objForm, "COURSECD_FLG");
        knjCreateHidden($objForm, "MAJORCD_FLG");
        knjCreateHidden($objForm, "COURSECODE_FLG");
        knjCreateHidden($objForm, "NAME_FLG");
        knjCreateHidden($objForm, "NAME_SHOW_FLG");
        knjCreateHidden($objForm, "NAME_KANA_FLG");
        knjCreateHidden($objForm, "NAME_ENG_FLG");
        knjCreateHidden($objForm, "REAL_NAME_FLG");
        knjCreateHidden($objForm, "REAL_NAME_KANA_FLG");

        knjCreateHidden($objForm, "CHECK_COURSEMAJORCD", $Row["COURSEMAJORCD"]);
        knjCreateHidden($objForm, "CHECK_COURSECODE", $Row["COURSECODE"]);
        knjCreateHidden($objForm, "SCHREGNO", $model->replace_data["selectdata"]);

        knjCreateHidden($objForm, "CHK_GRD_SDATE", CTRL_YEAR."/04/01");
        knjCreateHidden($objForm, "CHK_GRD_EDATE", (CTRL_YEAR + 1)."/03/31");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja110bSubForm1.html", $arg);
    }
}
?>
