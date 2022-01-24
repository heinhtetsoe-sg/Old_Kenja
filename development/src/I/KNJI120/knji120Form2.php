<?php

require_once('for_php7.php');

class knji120Form2
{
    public function main(&$model)
    {
        $objForm       = new form();
        $arg["start"]  = $objForm->get_start("edit", "POST", "knji120index.php", "", "edit");
        $arg["reload"] = "";
        //年度
        if (!$model->yearseme) {
            $db           = Query::dbCheckOut();
            $ycnt=0;
            $result = $db->query(knji120Query::getYear());
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($ycnt == 0) {
                    $model->yearseme = $row["YEAR"]."-".$row["SEMESTER"];
                    $model->year = SUBSTR($model->yearseme, 0, 4);
                    $model->seme = SUBSTR($model->yearseme, 5);
                }
                $ycnt++;
            }
            Query::dbCheckIn($db);
        }

        if (isset($model->schregno) && !isset($model->warning)) {
            $Row = knji120Query::getStudent_data($model->schregno, $model);
        } else {
            $Row =& $model->field;
        }

        //出席番号
        $objForm->ae(array("type"        => "text",
                           "name"        => "ATTENDNO",
                           "size"        => 3,
                           "maxlength"   => 3,
                           "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";",
                           "value"       => $Row["ATTENDNO"] ));

        $arg["data"]["ATTENDNO"] = $objForm->ge("ATTENDNO");

        $db           = Query::dbCheckOut();

        //事前処理チェック

        $chgari = "";
        if ($model->cmd == 'chg_year') {
            if (!knji120Query::checktoStart($db, $model)) {
                $arg["Closing"] = " closing_window(2);";
                $chgari = "on";
            }
        } else {
            $model->cmd = 'edit';
        }

        //年組コンボボックス
        $result = $db->query(knji120Query::getGrdClasQuery($model));
        $opt_grcl = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_grcl[] = array("label" => htmlspecialchars($row["HR_NAME"]),"value" => $row["GC"]);
        }

        //左リストの年組とあわせる
//        if ($model->GradeClass=="" && $model->chargeclass_flg == 1) $model->GradeClass = $opt_grcl[0]["value"];
        if ($model->GradeClass=="") {
            $model->GradeClass = $opt_grcl[0]["value"];
        }

        $grcl = ($Row["GRCL"]=="") ? $model->GradeClass : $Row["GRCL"];
        $objForm->ae(array("type"      => "select",
                           "name"      => "GRADE_CLASS",
                           "size"      => "1",
                           "extrahtml" => "",
                           "value"     => $grcl,
                           "options"   => $opt_grcl));

        $arg["data"]["GRADE_CLASS"] = $objForm->ge("GRADE_CLASS");

        //年次
        $objForm->ae(array("type"      => "text",
                           "name"      => "ANNUAL",
                           "size"      => 2,
                           "maxlength" => 2,
                           "value"     => $Row["ANNUAL"],
                           "extrahtml" => "onblur=\"this.value=toInteger(this.value)\";"));

        $arg["data"]["ANNUAL"] = $objForm->ge("ANNUAL");


        //課程学科
        $result       = $db->query(knji120Query::getCourse_Subject($model));
        $opt_coursecd = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_coursecd[] = array("label" => str_replace(",", "", $row["COURSEMAJORCD"])."  ".htmlspecialchars($row["COURSE_SUBJECT"]),
                                    "value" => $row["COURSEMAJORCD"]);
        }
        $objForm->ae(array("type"        => "select",
                           "name"        => "COURSEMAJORCD",
                           "size"        => 1,
                           "maxlength"   => 10,
                           "value"       => $Row["COURSEMAJORCD"],
                           "options"     => $opt_coursecd ));

        $arg["data"]["COURSEMAJORCD"] = $objForm->ge("COURSEMAJORCD");

        //コース
        $result = $db->query(knji120Query::getCourseCode($model));
        $opt_course3 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_course3[] = array("label" => $row["COURSECODE"]."  ".htmlspecialchars($row["COURSECODENAME"]),
                                     "value" => $row["COURSECODE"]);
        }

        $objForm->ae(array("type"        => "select",
                           "name"        => "COURSECODE",
                           "size"        => 1,
                           "maxlength"   => 10,
                           "value"       => $Row["COURSECODE"],
                           "options"     => $opt_course3));

        $arg["data"]["COURSECODE"] = $objForm->ge("COURSECODE");

        //学籍番号
        $objForm->ae(array("type"      => "text",
                           "name"      => "SCHREGNO",
                           "size"      => 10,
                           "maxlength" => 8,
                           "value"     => $Row["SCHREGNO"],
                           "extrahtml" => "onblur=\"this.value=toAlphaNumber(this.value)\";"));

        $arg["data"]["SCHREGNO"] = $objForm->ge("SCHREGNO");

        //内外区分
        $arg["data"]["INOUTCD"] = $model->CreateCombo($objForm, $db, "A001", "INOUTCD", $Row["INOUTCD"], 1);

        //氏名
        $objForm->ae(array("type"        => "text",
                           "name"        => "NAME",
                           "size"        => 40,
                           "maxlength"   => 60,
                           "extrahtml"   => "onBlur=\" Name_Clip(this);\"",
                           "value"       => $Row["NAME"] ));

        $arg["data"]["NAME"] = $objForm->ge("NAME");

        //表示用氏名
        $objForm->ae(array("type"        => "text",
                           "name"        => "NAME_SHOW",
                           "size"        => 20,
                           "maxlength"   => 30,
                           "extrahtml"   => "",
                           "value"       => $Row["NAME_SHOW"] ));

        $arg["data"]["NAME_SHOW"] = $objForm->ge("NAME_SHOW");

        //氏名かな
        $objForm->ae(array("type"        => "text",
                           "name"        => "NAME_KANA",
                           "size"        => 65,
                           "maxlength"   => 120,
                           "extrahtml"   => "",
                           "value"       => $Row["NAME_KANA"] ));

        $arg["data"]["NAME_KANA"] = $objForm->ge("NAME_KANA");

        //英字氏名
        $objForm->ae(array("type"        => "text",
                           "name"        => "NAME_ENG",
                           "size"        => 40,
                           "maxlength"   => 40,
                           "extrahtml"   => "",
                           "value"       => $Row["NAME_ENG"] ));

        $arg["data"]["NAME_ENG"] = $objForm->ge("NAME_ENG");

        //誕生日
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), "");

        //性別
        $arg["data"]["SEX"] = $model->CreateCombo($objForm, $db, "Z002", "SEX", $Row["SEX"], 1);

        //血液型(型)
        $objForm->ae(array("type"        => "text",
                           "name"        => "BLOODTYPE",
                           "size"        => 3,
                           "maxlength"   => 2,
                           "extrahtml"   => "",
                           "value"       => $Row["BLOODTYPE"] ));

        $arg["data"]["BLOODTYPE"] = $objForm->ge("BLOODTYPE");

        //血液型(RH型)
        $objForm->ae(array("type"        => "text",
                           "name"        => "BLOOD_RH",
                           "size"        => 1,
                           "maxlength"   => 1,
                           "extrahtml"   => "",
                           "value"       => $Row["BLOOD_RH"] ));

        $arg["data"]["BLOOD_RH"] = $objForm->ge("BLOOD_RH");

        //その他
        $arg["data"]["HANDICAP"] = $model->CreateCombo($objForm, $db, "A025", "HANDICAP", $Row["HANDICAP"], 1);

        //国籍
        $arg["data"]["NATIONALITY"] = $model->CreateCombo($objForm, $db, "A024", "NATIONALITY", $Row["NATIONALITY"], 1);

        //出身中学校
        $result = $db->query(knji120Query::getFinschoolName($model));
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array( "label"  => $row["FINSCHOOLCD"]."  ".htmlspecialchars($row["FINSCHOOL_NAME"]),
                            "value"  => $row["FINSCHOOLCD"]);
        }

        $objForm->ae(array("type"        => "select",
                           "name"        => "FINSCHOOLCD",
                           "size"        => 1,
                           "maxlength"   => 10,
                           "extrahtml"   => "style=width:\"35%\"",
                           "value"       => $Row["FINSCHOOLCD"],
                           "options"     => $opt));

        $arg["data"]["FINSCHOOLCD"] = $objForm->ge("FINSCHOOLCD");

        //出身中学校 卒業年月日
        $arg["data"]["FINISH_DATE"] = View::popUpCalendar($objForm, "FINISH_DATE", str_replace("-", "/", $Row["FINISH_DATE"]), "");

        //入学
        $arg["data"]["ENT_DATE"] = View::popUpCalendar($objForm, "ENT_DATE", str_replace("-", "/", $Row["ENT_DATE"]), "");
        $arg["data"]["ENT_DIV"] = $model->CreateCombo($objForm, $db, "A002", "ENT_DIV", $Row["ENT_DIV"], 1);

        //事由
        $objForm->ae(array("type"        => "text",
                           "name"        => "ENT_REASON",
                           "size"        => 45,
                           "maxlength"   => 75,
                           "value"       => $db->getOne(knji120Query::getEnt_reason($model->schregno))));

        $arg["data"]["ENT_REASON"] = $objForm->ge("ENT_REASON");

        //学校名
        $objForm->ae(array("type"        => "text",
                           "name"        => "ENT_SCHOOL",
                           "value"       => $Row["ENT_SCHOOL"],
                           "size"        => 45,
                           "maxlength"   => 75,
                           "extrahtml"   => ""));

        $arg["data"]["ENT_SCHOOL"] = $objForm->ge("ENT_SCHOOL");

        //学校住所1
        $objForm->ae(array("type"        => "text",
                           "name"        => "ENT_ADDR",
                           "value"       => $Row["ENT_ADDR"],
                           "size"        => 45,
                           "maxlength"   => 90,
                           "extrahtml"   => ""));

        $arg["data"]["ENT_ADDR"] = $objForm->ge("ENT_ADDR");

        //住所２使用
        if ($model->Properties["useAddrField2"] == "1") {
            $arg["useAddrField2"] = $model->Properties["useAddrField2"];
            $arg["addrSpan"] = "4";
        } else {
            $arg["addrSpan"] = "3";
        }
        //学校住所2
        $objForm->ae(array("type"        => "text",
                           "name"        => "ENT_ADDR2",
                           "value"       => $Row["ENT_ADDR2"],
                           "size"        => 45,
                           "maxlength"   => 90,
                           "extrahtml"   => ""));

        $arg["data"]["ENT_ADDR2"] = $objForm->ge("ENT_ADDR2");



        //卒業
        $arg["data"]["GRD_DATE"] = View::popUpCalendar($objForm, "GRD_DATE", str_replace("-", "/", $Row["GRD_DATE"]), "");
        $arg["data"]["GRD_DIV"] = $model->CreateCombo($objForm, $db, "A003", "GRD_DIV", $Row["GRD_DIV"], 1);

        //事由
        $objForm->ae(array("type"        => "text",
                           "name"        => "GRD_REASON",
                           "size"        => 45,
                           "maxlength"   => 75,
                           "value"       => $db->getOne(knji120Query::getGrd_reason($model->schregno))));

        $arg["data"]["GRD_REASON"] = $objForm->ge("GRD_REASON");

        //学校名
        $objForm->ae(array("type"        => "text",
                           "name"        => "GRD_SCHOOL",
                           "value"       => $Row["GRD_SCHOOL"],
                           "size"        => 45,
                           "maxlength"   => 75,
                           "extrahtml"   => ""));

        $arg["data"]["GRD_SCHOOL"] = $objForm->ge("GRD_SCHOOL");

        //学校住所1
        $objForm->ae(array("type"        => "text",
                           "name"        => "GRD_ADDR",
                           "value"       => $Row["GRD_ADDR"],
                           "size"        => 45,
                           "maxlength"   => 90,
                           "extrahtml"   => ""));

        $arg["data"]["GRD_ADDR"] = $objForm->ge("GRD_ADDR");

        //学校住所2
        $objForm->ae(array("type"        => "text",
                           "name"        => "GRD_ADDR2",
                           "value"       => $Row["GRD_ADDR2"],
                           "size"        => 45,
                           "maxlength"   => 90,
                           "extrahtml"   => ""));

        $arg["data"]["GRD_ADDR2"] = $objForm->ge("GRD_ADDR2");


        //顔写真
        $arg["data"]["FACE_IMG"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$Row["SCHREGNO"].".".$model->control_data["Extension"];
        $arg["data"]["IMG_PATH"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$Row["SCHREGNO"].".".$model->control_data["Extension"];

        //出身塾
        $result = $db->query(knji120Query::getPrischoolName($model));
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array( "label"  => $row["PRISCHOOLCD"]."  ".htmlspecialchars($row["PRISCHOOL_NAME"]),
                            "value"  => $row["PRISCHOOLCD"]);
        }

        $objForm->ae(array("type"        => "select",
                           "name"        => "PRISCHOOLCD",
                           "size"        => 1,
                           "maxlength"   => 10,
                           "extrahtml"   => "style=width:\"35%\"",
                           "value"       => $Row["PRISCHOOLCD"],
                           "options"     => $opt));

        $arg["data"]["PRISCHOOLCD"] = $objForm->ge("PRISCHOOLCD");


        //備考1
        $objForm->ae(array("type"        => "text",
                           "name"        => "REMARK1",
                           "value"       => $Row["REMARK1"],
                           "size"        => "50",
                           "maxlength"   => "75",
                           "extrahtml"   => ""));

        $arg["data"]["REMARK1"] = $objForm->ge("REMARK1");

        //備考2
        $objForm->ae(array("type"        => "text",
                           "name"        => "REMARK2",
                           "value"       => $Row["REMARK2"],
                           "size"        => "50",
                           "maxlength"   => "75",
                           "extrahtml"   => ""));

        $arg["data"]["REMARK2"] = $objForm->ge("REMARK2");

        //備考3
        $objForm->ae(array("type"        => "text",
                           "name"        => "REMARK3",
                           "value"       => $Row["REMARK3"],
                           "size"        => "50",
                           "maxlength"   => "75",
                           "extrahtml"   => ""));

        $arg["data"]["REMARK3"] = $objForm->ge("REMARK3");


        $link = REQUESTROOT."/I/KNJI120/knji120index.php?cmd=replace&SCHREGNO=".$model->schregno;
        $objForm->ae(array("type"        => "button",
                           "name"        => "btn_replace",
                           "value"       => "一括更新",
                           "extrahtml"   => "onclick=\"Page_jumper('$link');\"" ));

        $arg["button"]["btn_replace"] = $objForm->ge("btn_replace");

        $objForm->ae(array("type"        => "button",
                           "name"        => "btn_add",
                           "value"       => "追 加",
                           "extrahtml"   => "onclick=\"return btn_submit('add');\"" ));

        if ($model->grdcheck != "on") {
            $arg["button"]["btn_add"] = $objForm->ge("btn_add");
        }

        $objForm->ae(array("type"        => "button",
                           "name"        => "btn_udpate",
                           "value"       => "更 新",
                           "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));


        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");


        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_udpate');

        $objForm->ae(array("type"        => "button",
                           "name"        => "btn_del",
                           "value"       => "削 除",
                           "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ));

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        $objForm->ae(array("type"        => "reset",
                           "name"        => "btn_reset",
                           "value"       => "取 消",
                           "extrahtml"   => "onclick=\"return btn_submit('clear');\""  ));

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae(array("type"        => "button",
                           "name"        => "btn_end",
                           "value"       => "終 了",
                           "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");


        //hidden
        $objForm->ae(array("type"      => "hidden",
                           "name"      => "cmd"));

        $objForm->ae(array("type"      => "hidden",
                           "name"      => "GAMENKANYEAR",
                           "value"     => $model->yearseme));

        $objForm->ae(array("type"      => "hidden",
                           "name"      => "UPDATED1",
                           "value"     => $Row["UPDATED1"]));

        $objForm->ae(array("type"      => "hidden",
                           "name"      => "UPDATED2",
                           "value"     => $Row["UPDATED2"]));

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && !isset($model->warning) && $model->grdcheck != "on") {
            if (VARS::get("cmd") != "chg_year") {
                $arg["reload"]  = "parent.left_frame.location.href='".REQUESTROOT."/X/KNJXEXP2/index.php?cmd=datachenge&GAMENKANYEAR=".$model->yearseme."&GAMENKANGRADE=".$model->grade."','left_frame'";
            } else {
                $arg["reload"]  = "parent.left_frame.location.href='".REQUESTROOT."/X/KNJXEXP2/index.php?cmd=chg_year2&GAMENKANYEAR=".$model->yearseme."&GAMENKANGRADE=".$model->grade."','left_frame'.";
                "";
            }
        }
        View::toHTML($model, "knji120Form2.html", $arg);
    }
}
