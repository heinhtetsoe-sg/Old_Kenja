<?php

require_once('for_php7.php');

class knjxschregForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "knjxschregindex.php", "", "detail");
        
        //データ取得
        $Row = knjxschregQuery::getStudent_data($model->schregno, $model);

        //DB OPEN
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = $model->year;

        //学籍番号
        $arg["SCHREGNO"] = $model->schregno;

        //氏名
        $query = knjxschregQuery::getName($model->schregno);
        $schName = $db->getOne($query);
        $arg["NAME"] = $schName;

        //出席番号
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["ATTENDNO"] = knjCreateTextBox($objForm, $Row["ATTENDNO"], "ATTENDNO", 3, 3, $extra);

        //年組コンボボックス
        $result = $db->query(knjxschregQuery::getGrd_ClasQuery($model));
        $opt_grcl = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             $opt_grcl[] = array("label" => htmlspecialchars($row["HR_NAME"]),"value" => $row["GC"]);
        }
        $extra = "";
        $arg["data"]["GRADE_CLASS"] = knjCreateCombo($objForm, "GRADE_CLASS", $Row["GRCL"], $opt_grcl, $extra, 1);

        //年次
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["ANNUAL"] = knjCreateTextBox($objForm, $Row["ANNUAL"], "ANNUAL", 2, 2, $extra);

        //課程学科
        $result       = $db->query(knjxschregQuery::getCourse_Subject());
        $opt_coursecd = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_coursecd[] = array("label" => str_replace(",","",$row["COURSEMAJORCD"])."  ".htmlspecialchars($row["COURSE_SUBJECT"]),
                                    "value" => $row["COURSEMAJORCD"]);
        }
        $extra = "";
        $arg["data"]["COURSEMAJORCD"] = knjCreateCombo($objForm, "COURSEMAJORCD", $Row["COURSEMAJORCD"], $opt_coursecd, $extra, 1);

        //コース
        $result = $db->query(knjxschregQuery::getCourseCode());
        $opt_course3 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
              $opt_course3[] = array("label" => $row["COURSECODE"]."  ".htmlspecialchars($row["COURSECODENAME"]),
                                     "value" => $row["COURSECODE"]);
        }
        $extra = "";
        $arg["data"]["COURSECODE"] = knjCreateCombo($objForm, "COURSECODE", $Row["COURSECODE"], $opt_course3, $extra, 1);

        //学籍番号
        $extra = "onblur=\"this.value=toAlphaNumber(this.value)\";";
        $arg["data"]["SCHREGNO"] = knjCreateTextBox($objForm, $Row["SCHREGNO"], "SCHREGNO", 10, 8, $extra);

        //内外区分
        $arg["data"]["INOUTCD"] = CreateCombo($objForm,$db,"A001","INOUTCD",$Row["INOUTCD"],1);

        //氏名
        $extra = "onBlur=\" Name_Clip(this);\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 50, 80, $extra);

        //表示用氏名
        $extra = "";
        $arg["data"]["NAME_SHOW"] = knjCreateTextBox($objForm, $Row["NAME_SHOW"], "NAME_SHOW", 20, 30, $extra);

        //氏名かな
        $extra = "";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 80, 160, $extra);

        //英字氏名
        $extra = "";
        $arg["data"]["NAME_ENG"] = knjCreateTextBox($objForm, $Row["NAME_ENG"], "NAME_ENG", 40, 40, $extra);

        //戸籍氏名
        $extra = "onBlur=\" Name_Clip(this);\"";
        $arg["data"]["REAL_NAME"] = knjCreateTextBox($objForm, $Row["REAL_NAME"], "REAL_NAME", 50, 80, $extra);

        //戸籍氏名かな
        $extra = "";
        $arg["data"]["REAL_NAME_KANA"] = knjCreateTextBox($objForm, $Row["REAL_NAME_KANA"], "REAL_NAME_KANA", 60, 160, $extra);

        //誕生日
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar($objForm, "BIRTHDAY", str_replace("-","/",$Row["BIRTHDAY"]),"");

        //性別
        $arg["data"]["SEX"] = CreateCombo($objForm,$db,"Z002","SEX",$Row["SEX"],1);

        //血液型(型)
        $extra = "";
        $arg["data"]["BLOODTYPE"] = knjCreateTextBox($objForm, $Row["BLOODTYPE"], "BLOODTYPE", 3, 2, $extra);

        //血液型(RH型)
        $extra = "";
        $arg["data"]["BLOOD_RH"] = knjCreateTextBox($objForm, $Row["BLOOD_RH"], "BLOOD_RH", 1, 1, $extra);

        //その他
        $arg["data"]["HANDICAP"] = CreateCombo($objForm,$db,"A025","HANDICAP",$Row["HANDICAP"],1);

        //国籍
        $arg["data"]["NATIONALITY"] = CreateCombo($objForm,$db,"A024","NATIONALITY",$Row["NATIONALITY"],1);

        //出身中学校
        if ($model->schoolKind == "P") {
            $arg["data"]["NYUGAKUMAE_SYUSSIN_JOUHOU"] = knjCreateTextArea($objForm, "NYUGAKUMAE_SYUSSIN_JOUHOU", "4", "51", "soft", $extra, $Row["NYUGAKUMAE_SYUSSIN_JOUHOU"]);
        } else {
            $extra = "";
            $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", $model->finschoolcdKeta, $model->finschoolcdKeta, $extra);
            $finschoolname = $db->getOne(knjxschregQuery::getFinschoolName($Row["FINSCHOOLCD"]));
            $arg["data"]["FINSCHOOLNAME"] = $Row["FINSCHOOLNAME"] ? $Row["FINSCHOOLNAME"] : $finschoolname;

            //出身中学校 卒業年月日
            $arg["data"]["FINISH_DATE"] = View::popUpCalendar($objForm, "FINISH_DATE",str_replace("-","/",$Row["FINISH_DATE"]),"");
        }

        //入学
        $query = knjxschregQuery::getComeBackT();
        $isComeBack = $db->getOne($query) > 0 ? true : false;
        if ($isComeBack) {
            $query = knjxschregQuery::getCB_entDate($model);
            $comeBackEntDate = $db->getOne($query);
            if ($comeBackEntDate) {
                $arg["data"]["CB_ENT_DATE"] = str_replace("-", "/", $comeBackEntDate);
            }
        }

        $arg["data"]["ENT_DATE"] = View::popUpCalendar($objForm, "ENT_DATE",str_replace("-","/",$Row["ENT_DATE"]),"");
        //課程入学年度
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["CURRICULUM_YEAR"] = knjCreateTextBox($objForm, $Row["CURRICULUM_YEAR"], "CURRICULUM_YEAR", 4, 4, $extra);
        //入学区分
        $arg["data"]["ENT_DIV"] = CreateCombo($objForm,$db,"A002","ENT_DIV",$Row["ENT_DIV"],1);
        //受験番号
        $extra = "onblur=\"this.value=toInteger(this.value)\";";
        $examLen = $model->Properties["examnoLen"] ? $model->Properties["examnoLen"] : "5";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $Row["EXAMNO"], "EXAMNO", $examLen, $examLen, $extra);

        //事由
        $extra = "";
        $arg["data"]["ENT_REASON"] = knjCreateTextBox($objForm, $Row["ENT_REASON"], "ENT_REASON", 45, 75, $extra);

        //学校名
        $extra = "";
        $arg["data"]["ENT_SCHOOL"] = knjCreateTextBox($objForm, $Row["ENT_SCHOOL"], "ENT_SCHOOL", 45, 75, $extra);

        //学校住所1
        $extra = "";
        $arg["data"]["ENT_ADDR"] = knjCreateTextBox($objForm, $Row["ENT_ADDR"], "ENT_ADDR", 45, 90, $extra);

        //住所２使用(小学校、中学校)
        if ($model->Properties["useAddrField2"] == "1" && $model->schoolKind != "H") {
            $arg["useAddrField2"] = $model->Properties["useAddrField2"];
            $arg["addrSpan"] = "4";
        //高校(住所2と同一フィールド名だが、項目名が異なる)
        } else if ($model->schoolKind == "H") {
            $arg["hyoujiFieldCourse"] = "1";
            $arg["addrSpan"] = "4";
        } else {
            $arg["addrSpan"] = "3";
        }

        //学校住所2
        $extra = "";
        $arg["data"]["ENT_ADDR2"] = knjCreateTextBox($objForm, $Row["ENT_ADDR2"], "ENT_ADDR2", 45, 90, $extra);

        //卒業
        $arg["data"]["GRD_DATE"] = View::popUpCalendar($objForm, "GRD_DATE",str_replace("-","/",$Row["GRD_DATE"]),"");
        $arg["data"]["GRD_DIV"] = CreateCombo($objForm,$db,"A003","GRD_DIV",$Row["GRD_DIV"],1);
        if ($model->schoolKind != "H") {
            $arg["data"]["TENGAKU_SAKI_ZENJITU"] = View::popUpCalendar($objForm, "TENGAKU_SAKI_ZENJITU",str_replace("-","/",$Row["TENGAKU_SAKI_ZENJITU"]),"");
        }
        $arg["data"]["TENGAKU_SAKI_GRADE"] = knjCreateTextBox($objForm, $Row["TENGAKU_SAKI_GRADE"], "TENGAKU_SAKI_GRADE", 10, 15, $extra);

        //事由
        $extra = "";
        $arg["data"]["GRD_REASON"] = knjCreateTextBox($objForm, $Row["GRD_REASON"], "GRD_REASON", 45, 75, $extra);

        //学校名
        $extra = "";
        $arg["data"]["GRD_SCHOOL"] = knjCreateTextBox($objForm, $Row["GRD_SCHOOL"], "GRD_SCHOOL", 45, 75, $extra);

        //学校住所1
        $extra = "";
        $arg["data"]["GRD_ADDR"] = knjCreateTextBox($objForm, $Row["GRD_ADDR"], "GRD_ADDR", 45, 90, $extra);

        //学校住所2
        $extra = "";
        $arg["data"]["GRD_ADDR2"] = knjCreateTextBox($objForm, $Row["GRD_ADDR2"], "GRD_ADDR2", 45, 90, $extra);

        //顔写真
        $arg["data"]["FACE_IMG"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$Row["SCHREGNO"].".".$model->control_data["Extension"];
        $arg["data"]["IMG_PATH"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$Row["SCHREGNO"].".".$model->control_data["Extension"];

        if ($model->Properties["useDispUnDispPicture"] === '1') {
            $arg["unDispPicture"] = "1";
        } else {
            $arg["dispPicture"] = "1";
        }

        //出身塾
        $result = $db->query(knjxschregQuery::getPrischoolName());
        $opt = array();
        $opt[] = array("label" => "","value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array( "label"  => $row["PRISCHOOLCD"]."  ".htmlspecialchars($row["PRISCHOOL_NAME"]),
                            "value"  => $row["PRISCHOOLCD"]);
        }
        $extra = "style=width:\"35%\"";
        $arg["data"]["PRISCHOOLCD"] = knjCreateCombo($objForm, "PRISCHOOLCD", $Row["PRISCHOOLCD"], $opt, $extra, 1);

        //備考1
        $extra = "";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 50, 75, $extra);

        //備考2
        $extra = "";
        $arg["data"]["REMARK2"] = knjCreateTextBox($objForm, $Row["REMARK2"], "REMARK2", 50, 75, $extra);

        //備考3
        $extra = "";
        $arg["data"]["REMARK3"] = knjCreateTextBox($objForm, $Row["REMARK3"], "REMARK3", 50, 75, $extra);

        //終了ボタンを作成する
        if ($model->buttonFlg) {
            $extra = "onclick=\"closeWin()\"";
        } else {
            $extra = "onclick=\"return parent.closeit()\"";
        }
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJXSCHREG");
        knjCreateHidden($objForm, "cmd", "");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjxschregForm1.html", $arg);
    }
}
//各コンボボックス作成
function CreateCombo(&$objForm,$db,$namecd,$varname,$value,$fst)
{
    $result = $db->query(knjxschregQuery::getNameMst_data($namecd));
    $opt = array();

    //性別と卒業区分には先頭に空をセット
    if ($fst=="1") $opt[] = array("label" => "","value" => "");

    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
          $opt[] = array( "label" => $row["NAMECD2"]."  ".htmlspecialchars($row["NAME1"]),
                          "value" => $row["NAMECD2"]);
    }

    $objForm->ae( array("type"        => "select",
                        "name"        => $varname,
                        "size"        => 1,
                        "maxlength"   => 10,
                        "extrahtml"   => "",
                        "value"       => $value,
                        "options"     => $opt));

    return $objForm->ge($varname);
}
?>
