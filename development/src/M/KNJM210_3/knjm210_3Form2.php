<?php

require_once('for_php7.php');

class knjm210_3Form2
{
    function main(&$model)
    {
        $objForm       = new form;
        $arg["start"]  = $objForm->get_start("edit", "POST", "knjm210_3index.php", "", "edit");
        $arg["reload"] = "";

//        $model->schregno = "20031935";
        $Row = knjm210_3Query::getStudent_data($model->schregno, $model);
        //出席番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "ATTENDNO",
                            "size"        => 3,
                            "maxlength"   => 3,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";",
                            "value"       => $Row["ATTENDNO"] ));

        $arg["data"]["ATTENDNO"] = $objForm->ge("ATTENDNO");

        $db           = Query::dbCheckOut();

        //年組コンボボックス
        $result = $db->query(knjm210_3Query::getGrd_ClasQuery($model));
        $opt_grcl = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
             $opt_grcl[] = array("label" => htmlspecialchars($row["HR_NAME"]),"value" => $row["GC"]);
        }

        //左リストの年組とあわせる
        if ($model->GradeClass=="") $model->GradeClass = $opt_grcl[0]["value"];
        
        $grcl = ($Row["GRCL"]=="") ? $model->GradeClass : $Row["GRCL"];
        $objForm->ae( array("type"      => "select",
                            "name"      => "GRADE_CLASS",
                            "size"      => "1",
                            "extrahtml" => "",
                            "value"     => $grcl,
                            "options"   => $opt_grcl));

        $arg["data"]["GRADE_CLASS"] = $objForm->ge("GRADE_CLASS");

        //年次
        $objForm->ae( array("type"      => "text",
                            "name"      => "ANNUAL",
                            "size"      => 2,
                            "maxlength" => 2,
                            "value"     => $Row["ANNUAL"],
                            "extrahtml" => "onblur=\"this.value=toInteger(this.value)\";"));

        $arg["data"]["ANNUAL"] = $objForm->ge("ANNUAL");


        //課程学科
        $result       = $db->query(knjm210_3Query::getCourse_Subject());
        $opt_coursecd = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_coursecd[] = array("label" => str_replace(",","",$row["COURSEMAJORCD"])."  ".htmlspecialchars($row["COURSE_SUBJECT"]),
                                    "value" => $row["COURSEMAJORCD"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "COURSEMAJORCD",
                            "size"        => 1,
                            "maxlength"   => 10,
                            "value"       => $Row["COURSEMAJORCD"],
                            "options"     => $opt_coursecd ) );

        $arg["data"]["COURSEMAJORCD"] = $objForm->ge("COURSEMAJORCD");

        //コース
        $result = $db->query(knjm210_3Query::getCourseCode());
        $opt_course3 = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
              $opt_course3[] = array("label" => $row["COURSECODE"]."  ".htmlspecialchars($row["COURSECODENAME"]),
                                     "value" => $row["COURSECODE"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "COURSECODE",
                            "size"        => 1,
                            "maxlength"   => 10,
//                            "extrahtml"   => "style=width:\"30%\"",
                            "value"       => $Row["COURSECODE"],
                            "options"     => $opt_course3));

        $arg["data"]["COURSECODE"] = $objForm->ge("COURSECODE");

        //学籍番号
        $objForm->ae( array("type"      => "text",
                            "name"      => "SCHREGNO",
                            "size"      => 10,
                            "maxlength" => 8,
                            "value"     => $Row["SCHREGNO"],
                            "extrahtml" => "onblur=\"this.value=toAlphaNumber(this.value)\";"));

        $arg["data"]["SCHREGNO"] = $objForm->ge("SCHREGNO");

        //内外区分
        $arg["data"]["INOUTCD"] = $model->CreateCombo($objForm,$db,"A001","INOUTCD",$Row["INOUTCD"],1);

        //氏名
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "onBlur=\" Name_Clip(this);\"",
                            "value"       => $Row["NAME"] ));

        $arg["data"]["NAME"] = $objForm->ge("NAME");

        //表示用氏名
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME_SHOW",
                            "size"        => 20,
                            "maxlength"   => 30,
                            "extrahtml"   => "",
                            "value"       => $Row["NAME_SHOW"] ));

        $arg["data"]["NAME_SHOW"] = $objForm->ge("NAME_SHOW");

        //氏名かな
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME_KANA",
                            "size"        => 65,//80
                            "maxlength"   => 120,
                            "extrahtml"   => "",
                            "value"       => $Row["NAME_KANA"] ));

        $arg["data"]["NAME_KANA"] = $objForm->ge("NAME_KANA");

        //英字氏名
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME_ENG",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "",
                            "value"       => $Row["NAME_ENG"] ));

        $arg["data"]["NAME_ENG"] = $objForm->ge("NAME_ENG");

        //誕生日
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar($objForm, "BIRTHDAY", str_replace("-","/",$Row["BIRTHDAY"]),"");

        //性別
        $arg["data"]["SEX"] = $model->CreateCombo($objForm,$db,"Z002","SEX",$Row["SEX"],1);

        //血液型(型)
        $objForm->ae( array("type"        => "text",
                            "name"        => "BLOODTYPE",
                            "size"        => 3,
                            "maxlength"   => 2, 
                            "extrahtml"   => "",
                            "value"       => $Row["BLOODTYPE"] ));

        $arg["data"]["BLOODTYPE"] = $objForm->ge("BLOODTYPE");

        //血液型(RH型)
        $objForm->ae( array("type"        => "text",
                            "name"        => "BLOOD_RH",
                            "size"        => 1,
                            "maxlength"   => 1,
                            "extrahtml"   => "",
                            "value"       => $Row["BLOOD_RH"] ));

        $arg["data"]["BLOOD_RH"] = $objForm->ge("BLOOD_RH");

        //出身中学校
        $result = $db->query(knjm210_3Query::getFinschoolName());
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array( "label"  => $row["FINSCHOOLCD"]."  ".htmlspecialchars($row["FINSCHOOL_NAME"]),
                            "value"  => $row["FINSCHOOLCD"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "FINSCHOOLCD",
                            "size"        => 1,
                            "maxlength"   => 10,
                            "extrahtml"   => "style=width:\"35%\"",
                            "value"       => $Row["FINSCHOOLCD"],
                            "options"     => $opt));

        $arg["data"]["FINSCHOOLCD"] = $objForm->ge("FINSCHOOLCD");

        //出身中学校 卒業年月日
        $arg["data"]["FINISH_DATE"] = View::popUpCalendar($objForm, "FINISH_DATE",str_replace("-","/",$Row["FINISH_DATE"]),"");

        //入学
        $arg["data"]["ENT_DATE"] = View::popUpCalendar($objForm, "ENT_DATE",str_replace("-","/",$Row["ENT_DATE"]),"");
        $arg["data"]["ENT_DIV"] = $model->CreateCombo($objForm,$db,"A002","ENT_DIV",$Row["ENT_DIV"],1);

        //事由
        $objForm->ae( array("type"        => "text",
                            "name"        => "ENT_REASON",
                            "size"        => 45,
                            "maxlength"   => 75,
                            "value"       => $db->getOne(knjm210_3Query::getEnt_reason($model->schregno))));

        $arg["data"]["ENT_REASON"] = $objForm->ge("ENT_REASON");

        //学校名
        $objForm->ae( array("type"        => "text",
                            "name"        => "ENT_SCHOOL",
                            "value"       => $Row["ENT_SCHOOL"],
                            "size"        => 45,
                            "maxlength"   => 75,
                            "extrahtml"   => ""));

        $arg["data"]["ENT_SCHOOL"] = $objForm->ge("ENT_SCHOOL");

        //学校住所1
        $objForm->ae( array("type"        => "text",
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
            $arg["addr1"] = "1";
        } else {
            $arg["addrSpan"] = "3";
        }

        //学校住所2
        $objForm->ae( array("type"        => "text",
                            "name"        => "ENT_ADDR2",
                            "value"       => $Row["ENT_ADDR2"],
                            "size"        => 45,
                            "maxlength"   => 90,
                            "extrahtml"   => ""));

        $arg["data"]["ENT_ADDR2"] = $objForm->ge("ENT_ADDR2");



        //卒業
        $arg["data"]["GRD_DATE"] = View::popUpCalendar($objForm, "GRD_DATE",str_replace("-","/",$Row["GRD_DATE"]),"");
        $arg["data"]["GRD_DIV"] = $model->CreateCombo($objForm,$db,"A003","GRD_DIV",$Row["GRD_DIV"],1);
        //事由
        $objForm->ae( array("type"        => "text",
                            "name"        => "GRD_REASON",
                            "size"        => 45,
                            "maxlength"   => 75,
                            "value"       => $db->getOne(knjm210_3Query::getGrd_reason($model->schregno))));

        $arg["data"]["GRD_REASON"] = $objForm->ge("GRD_REASON");

        //学校名
        $objForm->ae( array("type"        => "text",
                            "name"        => "GRD_SCHOOL",
                            "value"       => $Row["GRD_SCHOOL"],
                            "size"        => 45,
                            "maxlength"   => 75,
                            "extrahtml"   => ""));

        $arg["data"]["GRD_SCHOOL"] = $objForm->ge("GRD_SCHOOL");

        //学校住所1
        $objForm->ae( array("type"        => "text",
                            "name"        => "GRD_ADDR",
                            "value"       => $Row["GRD_ADDR"],
                            "size"        => 45,
                            "maxlength"   => 90,
                            "extrahtml"   => ""));

        $arg["data"]["GRD_ADDR"] = $objForm->ge("GRD_ADDR");

        //学校住所2
        $objForm->ae( array("type"        => "text",
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
        $result = $db->query(knjm210_3Query::getPrischoolName());
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array( "label"  => $row["PRISCHOOLCD"]."  ".htmlspecialchars($row["PRISCHOOL_NAME"]),
                            "value"  => $row["PRISCHOOLCD"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "PRISCHOOLCD",
                            "size"        => 1,
                            "maxlength"   => 10,
                            "extrahtml"   => "style=width:\"35%\"",
                            "value"       => $Row["PRISCHOOLCD"],
                            "options"     => $opt));

        $arg["data"]["PRISCHOOLCD"] = $objForm->ge("PRISCHOOLCD");


       //備考1
       $objForm->ae( array("type"        => "text",
                           "name"        => "REMARK1",
                           "value"       => $Row["REMARK1"],
                           "size"        => "50",
                           "maxlength"   => "75",
                           "extrahtml"   => ""));

       $arg["data"]["REMARK1"] = $objForm->ge("REMARK1");

       //備考2
       $objForm->ae( array("type"        => "text",
                           "name"        => "REMARK2",
                           "value"       => $Row["REMARK2"],
                           "size"        => "50",
                           "maxlength"   => "75",
                           "extrahtml"   => ""));

       $arg["data"]["REMARK2"] = $objForm->ge("REMARK2");

       //備考3
       $objForm->ae( array("type"        => "text",
                           "name"        => "REMARK3",
                           "value"       => $Row["REMARK3"],
                           "size"        => "50",
                           "maxlength"   => "75",
                           "extrahtml"   => ""));

       $arg["data"]["REMARK3"] = $objForm->ge("REMARK3");


        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED1",
                            "value"     => $Row["UPDATED1"]) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED2",
                            "value"     => $Row["UPDATED2"]) );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm210_3Form2.html", $arg);
    }
}
?>
