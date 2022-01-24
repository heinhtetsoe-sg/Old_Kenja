<?php

require_once('for_php7.php');

class knjl011yForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("main", "POST", "knjl011yindex.php", "", "main");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //デフォルト設定
        if ($model->examno == "" || $model->cmd == "showdiv") {
            $db = Query::dbCheckOut();
            $result = $db->query(knjl011yQuery::get_name_cd($model->year, 'L003'));
            $defApplicantDiv = $model->field["APPLICANTDIV"];
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $defApplicantDiv = ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') ? $row["NAMECD2"] : $defApplicantDiv;
            }
            $model->examno = $db->getOne(knjl011yQuery::getMinExam($model->year, $defApplicantDiv));
            Query::dbCheckIn($db);
        }

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            //データを取得
            $Row = knjl011yQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303", "更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl011yQuery::get_edit_data($model);
                }
                $model->examno = $Row["EXAMNO"];
                $model->applicantdiv = $Row["APPLICANTDIV"];
            }
            $disabled = "";
            if (!is_array($Row)) {
                $disabled = "disabled";
                if ($model->cmd == 'reference') {
                    $model->setWarning("MSG303");
                }
            }
        } else {
            $Row =& $model->field;
        }
        if ($model->cmd == 'showdiv' || $model->cmd == 'showdivAdd') {
            $Row["APPLICANTDIV"] = $model->field["APPLICANTDIV"];
            if ($model->cmd == 'showdivAdd') {
                $Row["TESTDIV"] = $model->field["TESTDIV"];
                $Row["DESIREDIV"] = $model->field["DESIREDIV"];
                $Row["RECOM_KIND"] = $model->field["RECOM_KIND"];
                $Row["INTERVIEW_ATTEND_FLG"] = $model->field["INTERVIEW_ATTEND_FLG"];
            }
        }
        if ($model->cmd == 'testdiv' || $model->cmd == 'desirediv') {
            $Row =& $model->field;
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        $db = Query::dbCheckOut();

        //------------------------------志願者情報-------------------------------------
        //入試制度コンボ
        $result     = $db->query(knjl011yQuery::get_name_cd($model->year, 'L003'));
        $opt        = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
            if ($Row["APPLICANTDIV"]=="" && $row["NAMESPARE2"]=='1') {
                $Row["APPLICANTDIV"] = $row["NAMECD2"];
            }
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "APPLICANTDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg(); return btn_submit('showdiv');\"",
                            "value"       => $Row["APPLICANTDIV"],
                            "options"     => $opt ));

        $arg["data"]["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");



        //1:中学と2:高校の表示切替フラグ
        $isJuniorFlg = ($Row["APPLICANTDIV"] == "1") ? true : false ;
        $isHighFlg   = ($Row["APPLICANTDIV"] == "1") ? false : true ;

        //入試区分コンボ
        $testDiv_namecd1 = ($isHighFlg) ? "L004" : "L024" ;
        $opt        = array();
        $result     = $db->query(knjl011yQuery::get_name_cd($model->year, $testDiv_namecd1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
            if ($Row["TESTDIV"]=="" && $row["NAMESPARE2"]=='1') {
                $Row["TESTDIV"] = $row["NAMECD2"];
            }
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "TESTDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg(); return btn_submit('testdiv');\"",
                            "value"       => $Row["TESTDIV"],
                            "options"     => $opt ));
        $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //帰国生(高校のみ)
        $extra  = (strlen($Row["INTERVIEW_ATTEND_FLG"])) ? "checked" : "";
        $extra .= " id=\"INTERVIEW_ATTEND_FLG\"";
        $extra .= ($isHighFlg) ? "" : " disabled";
        $extra .= " onChange=\"change_flg();\"";
        $arg["data"]["INTERVIEW_ATTEND_FLG"] = knjCreateCheckBox($objForm, "INTERVIEW_ATTEND_FLG", "1", $extra);

        //推薦事項(中学第一回推薦のみ)
        for ($i = 1; $i <= 4; $i++) {
            $name   = "RECOM_ITEM" .$i;
            $extra  = (0 < strlen($Row[$name])) ? "checked" : "";
            $extra .= " id=\"".$name."\"";
            $extra .= ($isJuniorFlg && $Row["TESTDIV"] == "1") ? "" : " disabled" ;
            $objForm->ae(array("type"      => "checkbox",
                                "name"      => $name,
                                "value"     => "1",
                                "extrahtml" => $extra,
                                "multiple"  => ""));
            $arg["data"][$name] = $objForm->ge($name);
            $arg["data"]["viewhelp" .$i] = ($isJuniorFlg && $Row["TESTDIV"] == "1") ? "onMouseOver=\"ViewcdMousein(".$i.")\" onMouseOut=\"ViewcdMouseout()\"" : "";
        }

        //志望区分コンボ
        $opt        = array();
        $result     = $db->query(knjl011yQuery::getDesirediv($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"], "", "1"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["DESIREDIV"].":".$row["EXAMCOURSE_NAME"],
                           "value" => $row["DESIREDIV"]);
            if ($Row["DESIREDIV"]=="") {
                $Row["DESIREDIV"] = $row["DESIREDIV"];
            }
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "DESIREDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg(); return btn_submit('desirediv');\"",
                            "value"       => $Row["DESIREDIV"],
                            "options"     => $opt ));
        $arg["data"]["DESIREDIV"] = $objForm->ge("DESIREDIV");

        //推薦区分コンボ(高校推薦のみ)
        $opt        = array();
        if ($isHighFlg && $Row["TESTDIV"] == "2") {
            $result     = $db->query(knjl011yQuery::get_name_cd($model->year, "L023"));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($Row["DESIREDIV"] == "2" && $row["NAMECD2"] == "3") {
                    continue; //普通科進学は英検を表示しない
                }
                if (($Row["DESIREDIV"] == "1" || $Row["DESIREDIV"] == "3") && $row["NAMECD2"] == "3") {
                    continue; //特進は英検を表示しない
                }
                $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                               "value" => $row["NAMECD2"]);
            }
            $result->free();
        } else {
            $opt[] = array("label" => "", "value" => "");
        }
        $objForm->ae(array("type"        => "select",
                            "name"        => "RECOM_KIND",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg();\" style=\"width:120px\"",
                            "value"       => $Row["RECOM_KIND"],
                            "options"     => $opt ));
        $arg["data"]["RECOM_KIND"] = $objForm->ge("RECOM_KIND");

        //専併区分コンボ
        $opt        = array();
        $result     = $db->query(knjl011yQuery::get_name_cd($model->year, "L006"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $defShdiv = $isHighFlg && $model->cmd == 'showdivAdd' ? "2" : "";
        $Row["SHDIV"] = strlen($Row["SHDIV"]) ? $Row["SHDIV"] : $defShdiv;
        $objForm->ae(array("type"        => "select",
                            "name"        => "SHDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg(); disabledCommon('shdiv', this.value);\"",
                            "value"       => $Row["SHDIV"],
                            "options"     => $opt ));
        $arg["data"]["SHDIV"] = $objForm->ge("SHDIV");

        //併願校（高校）
        $shFsName    = array();
        $shRitsuName = array();
        $result     = $db->query(knjl011yQuery::getFinHighschoolcd($Row["SH_SCHOOLCD"]));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $shFsName[$row["FINSCHOOLCD"]] = $row["FINSCHOOL_NAME"];
            $shRitsuName[$row["FINSCHOOLCD"]] = $row["RITSU_NAME"];
        }
        $result->free();
        //コード
        $disBtnFinHigh = ($Row["SHDIV"] == "2") ? "" : "disabled " ;
        $objForm->ae(array("type"        => "text",
                            "name"        => "SH_SCHOOLCD",
                            "size"        => 7,
                            "maxlength"   => 7,
                            "extrahtml"   => $disBtnFinHigh ."STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\" onblur=\"hiddenWin('" .REQUESTROOT ."/L/KNJL011Y/search_fin_high_name.php?cmd=apply&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&schoolcd='+document.forms[0]['SH_SCHOOLCD'].value+'&frame='+getFrameName(self))\"",
                            "value"       => $Row["SH_SCHOOLCD"] ));
        $arg["data"]["SH_SCHOOLCD"] = $objForm->ge("SH_SCHOOLCD");
        //学校名
        $arg["data"]["SH_FS_NAME"] = $shFsName[$Row["SH_SCHOOLCD"]];
        //ＸＸ立
        $arg["data"]["SH_RITSU_NAME"] = $shRitsuName[$Row["SH_SCHOOLCD"]];


        //受験番号
        $objForm->ae(array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "STYLE=\"ime-mode: inactive;\" onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $model->examno ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //氏名(志願者)
        $objForm->ae(array("type"        => "text",
                            "name"        => "NAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onchange=\"change_flg()\"",
                            "value"       => $Row["NAME"] ));
        $arg["data"]["NAME"] = $objForm->ge("NAME");

        //氏名かな(志願者)
        $objForm->ae(array("type"        => "text",
                            "name"        => "NAME_KANA",
                            "size"        => 40,
                            "maxlength"   => 120,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onchange=\"change_flg()\"",
                            "value"       => $Row["NAME_KANA"] ));
        $arg["data"]["NAME_KANA"] = $objForm->ge("NAME_KANA");

        //性別コンボ
        $result     = $db->query(knjl011yQuery::get_name_cd($model->year, 'Z002'));
        $opt        = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $defSex = $model->cmd == 'showdivAdd' ? "2" : "";
        $objForm->ae(array("type"        => "select",
                            "name"        => "SEX",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"change_flg(); disabledCommon('sex', this.value);\"",
                            "value"       => strlen($Row["SEX"]) ? $Row["SEX"] : $defSex,
                            "options"     => $opt ));
        $arg["data"]["SEX"] = $objForm->ge("SEX");

        //生年月日（西暦）
        $extra = " STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\"";
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar2($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), "", "", $extra);

        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl011yQuery::get_calendarno($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($calno == "") {
                $calno = $row["NAMECD2"];
                $spare = $row["NAMESPARE1"];
                $spare2 = $row["NAMESPARE2"];
                $spare3 = $row["NAMESPARE3"];
            } else {
                $calno.= "," . $row["NAMECD2"];
                $spare.= "," . $row["NAMESPARE1"];
                $spare2.= "," . $row["NAMESPARE2"];
                $spare3.= "," . $row["NAMESPARE3"];
            }
            $arg["data2"][] = array("eracd" => $row["NAMECD2"], "wname" => $row["NAME1"]);
        }

        //生年月日元号
        $objForm->ae(array("type"        => "text",
                            "name"        => "ERACD",
                            "size"        => 1,
                            "maxlength"   => 1,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(0, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onchange=\"change_flg()\"",
                            "value"       => strlen($Row["ERACD"]) ? $Row["ERACD"] : "4" ));
        $arg["data"]["ERACD"] = $objForm->ge("ERACD");

        //和暦名
        if (isset($Row["NAME1"])) {
            $name1 = $Row["NAME1"];
        } elseif (isset($Row["WNAME"])) {
            $name1 = str_replace("&nbsp;", "", $Row["WNAME"]);
        } else {
            $name1 = "平成";
        }
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "WNAME",
                            "value"     => $name1));
        $arg["data"]["WNAME"] = $name1;

        //生年月日年
        $objForm->ae(array("type"        => "text",
                            "name"        => "BIRTH_Y",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["BIRTH_Y"] ));
        $arg["data"]["BIRTH_Y"] = $objForm->ge("BIRTH_Y");

        //生年月日月
        $objForm->ae(array("type"        => "text",
                            "name"        => "BIRTH_M",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["BIRTH_M"] ));
        $arg["data"]["BIRTH_M"] = $objForm->ge("BIRTH_M");

        //生年月日日
        $objForm->ae(array("type"        => "text",
                            "name"        => "BIRTH_D",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(3, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["BIRTH_D"] ));
        $arg["data"]["BIRTH_D"] = $objForm->ge("BIRTH_D");

        //卒業元号
        $objForm->ae(array("type"        => "text",
                            "name"        => "FS_ERACD",
                            "size"        => 1,
                            "maxlength"   => 1,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(0, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onchange=\"change_flg()\"",
                            "value"       => strlen($Row["FS_ERACD"]) ? $Row["FS_ERACD"] : "4" ));
        $arg["data"]["FS_ERACD"] = $objForm->ge("FS_ERACD");
        //卒業和暦名
        $fs_wname = isset($Row["FS_WNAME"]) ? str_replace("&nbsp;", "", $Row["FS_WNAME"]) : "平成";
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "FS_WNAME",
                            "value"     => $fs_wname ));
        $arg["data"]["FS_WNAME"] = $fs_wname;
        //卒業年
        $objForm->ae(array("type"        => "text",
                            "name"        => "FS_Y",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["FS_Y"] ));
        $arg["data"]["FS_Y"] = $objForm->ge("FS_Y");
        //卒業月
        $defGrdmon = $model->cmd == 'showdivAdd' ? "03" : "";
        $objForm->ae(array("type"        => "text",
                            "name"        => "FS_M",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => strlen($Row["FS_M"]) ? $Row["FS_M"] : $defGrdmon ));
        $arg["data"]["FS_M"] = $objForm->ge("FS_M");
        //卒業年(西暦)
        $defGrdyear = $model->cmd == 'showdivAdd' ? $model->year : "";
        $objForm->ae(array("type"        => "text",
                            "name"        => "FS_GRDYEAR",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" this.value=toInteger(this.value)\" onchange=\"change_flg()\"",
                            "value"       => strlen($Row["FS_GRDYEAR"]) ? $Row["FS_GRDYEAR"] : $defGrdyear ));
        $arg["data"]["FS_GRDYEAR"] = $objForm->ge("FS_GRDYEAR");
        //卒業区分（1:見込み,2:卒業）
        $optGrddiv = array();
        $optGrddiv[] = array("label" => "", "value" => "");
        $result = $db->query(knjl011yQuery::get_name_cd($model->year, 'L016'));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optGrddiv[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                                 "value" => $row["NAMECD2"]);
        }
        $result->free();
        $defGrddiv = $model->cmd == 'showdivAdd' ? "1" : "";
        $objForm->ae(array("type"        => "select",
                            "name"        => "FS_GRDDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => strlen($Row["FS_GRDDIV"]) ? $Row["FS_GRDDIV"] : $defGrddiv,
                            "options"     => $optGrddiv ));
        $arg["data"]["FS_GRDDIV"] = $objForm->ge("FS_GRDDIV");

        //不合格回数取得(試験を３回以上は受けれない)
        $query = knjl011yQuery::getReceptDatCount($model, '2');
        $unPassCnt = $db->getOne($query);

        //合格回数取得
        $query = knjl011yQuery::getReceptDatCount($model, '1');
        $passCnt = $db->getOne($query);

        /******************************* 希望 *******************************/
        //一般入試希望(高校：学特・推薦のみ)(中学：第一回推薦・第一回一般のみ)
        $name   = "GENERAL_FLG";
        if ($isJuniorFlg) {
            $defGeneral = (($Row["TESTDIV"] == "1" || $Row["TESTDIV"] == "2") && ($Row["JUDGEDIV"] == "2" || $Row["JUDGEDIV"] == "4") && $model->cmd == 'showdivAdd') ? "1" : "" ;
        } else {
            $defGeneral = (($Row["TESTDIV"] == "1" || $Row["TESTDIV"] == "2") && $model->cmd == 'showdivAdd')                            ? "1" : "" ;
        }
        $Row[$name] = 0 < strlen($Row[$name]) ? $Row[$name] : $defGeneral;
        $extra  = (0 < strlen($Row[$name])) ? "checked" : "";
        $extra .= " id=\"".$name."\"";
//        $extra .= ($isHighFlg && ($Row["TESTDIV"] == "1" || $Row["TESTDIV"] == "2")) ? "" : " disabled" ;
        if ($isJuniorFlg) {
            $extra .= (($Row["TESTDIV"] == "1" || $Row["TESTDIV"] == "2") && ($Row["JUDGEDIV"] == "2" || $Row["JUDGEDIV"] == "4")) ? "" : " disabled" ;
        } else {
            $extra .=  ($Row["TESTDIV"] == "1" || $Row["TESTDIV"] == "2")                             ? "" : " disabled" ;
        }
        $extra .= " onclick=\"disabledCommon('general_flg', this.value);\"";
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => "1",
                            "extrahtml" => $extra,
                            "multiple"  => ""));
        $arg["data"][$name] = $objForm->ge($name);
        $arg["data"][$name."_NAME"] = ($isHighFlg) ? "一般入試" : "第二回一般";
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "JUDGEDIV",
                            "value"     => $Row["JUDGEDIV"]));

        //特進チャレンジ==>高校：学特入試のスライド合格(3)・特別判定合格(5)を含む進学コース(2,4)合格者(1)
        $extra  = (strlen($Row["SELECT_SUBCLASS_DIV"])) ? "checked" : "";
        $extra .= " id=\"SELECT_SUBCLASS_DIV\"";
        if ($isHighFlg && $Row["TESTDIV"] == "1" && strlen($Row["GENERAL_FLG"]) && (
            $Row["JUDGEDIV"] == "1" && ($Row["DESIREDIV"] == "2" || $Row["DESIREDIV"] == "4") ||
            $Row["JUDGEDIV"] == "3" ||
            $Row["JUDGEDIV"] == "5")) {
            $extra .= "";
            $dis_select_subclass_div = false;
        } else {
            $extra .= " disabled";
            $dis_select_subclass_div = true;
        }
        $extra .= " onChange=\"change_flg();\"";
        $arg["data"]["SELECT_SUBCLASS_DIV"] = knjCreateCheckBox($objForm, "SELECT_SUBCLASS_DIV", "1", $extra);
        knjCreateHidden($objForm, "DIS_SELECT_SUBCLASS_DIV", $dis_select_subclass_div);

        //志望区分コンボ
        $opt        = array();
        $opt[]      = array("label" => "", "value" => "");
        $result     = $db->query(knjl011yQuery::getDesirediv($model->year, $Row["APPLICANTDIV"], $model->general_testdiv, "", "1"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["DESIREDIV"].":".$row["EXAMCOURSE_NAME"],
                           "value" => $row["DESIREDIV"]);
        }
        $result->free();
        $name   = "GENERAL_DESIREDIV";
        $extra  = (strlen($Row["GENERAL_FLG"])) ? "" : " disabled";
        $extra .= " onChange=\"change_flg();\"";
        $objForm->ae(array("type"        => "select",
                            "name"        => $name,
                            "size"        => "1",
                            "extrahtml"   => $extra,
                            "value"       => $Row[$name],
                            "options"     => $opt ));
        $arg["data"][$name] = $objForm->ge($name);

        //専併区分コンボ
        $opt        = array();
        $opt[]      = array("label" => "", "value" => "");
        $result     = $db->query(knjl011yQuery::get_name_cd($model->year, "L006"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $name   = "GENERAL_SHDIV";
        $extra  = (strlen($Row["GENERAL_FLG"])) ? "" : " disabled";
        $extra .= " onChange=\"change_flg();\"";
        $objForm->ae(array("type"        => "select",
                            "name"        => $name,
                            "size"        => "1",
                            "extrahtml"   => $extra,
                            "value"       => $Row[$name],
                            "options"     => $opt ));
        $arg["data"][$name] = $objForm->ge($name);


        /******************************* 希望2 *******************************/
        //一般入試希望(高校：学特・推薦のみ)(中学：第一回推薦・第一回一般のみ)
        $name   = "GENERAL_FLG2";
        $defGeneral2 = "";
        if ($isJuniorFlg) {
            $defGeneral2 = (($Row["TESTDIV"] == "1" || $Row["TESTDIV"] == "2" || $Row["TESTDIV"] == "3" || $Row["TESTDIV"] == "5") && $unPassCnt < 2 && $model->cmd == 'showdivAdd') ? "1" : "" ;
            if (($Row["JUDGEDIV"] != "2" && $Row["JUDGEDIV"] != "4") || $passCnt > 0) {
                $defGeneral2 = "";
            }
        }
        $Row[$name] = 0 < strlen($Row[$name]) ? $Row[$name] : $defGeneral2;
        $extra  = (0 < strlen($Row[$name])) ? "checked" : "";
        $extra .= " id=\"".$name."\"";
        if ($isJuniorFlg) {
            $extra .= (($Row["TESTDIV"] == "1" || $Row["TESTDIV"] == "2" || $Row["TESTDIV"] == "3" || $Row["TESTDIV"] == "5") && $unPassCnt < 2) ? "" : " disabled" ;
            if (($Row["JUDGEDIV"] != "2" && $Row["JUDGEDIV"] != "4") || $passCnt > 0) {
                $extra .= " disabled";
            }
        } else {
            $extra .= " disabled" ;
        }
        $extra .= " onclick=\"disabledCommon('general_flg2', this.value);\"";
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => "1",
                            "extrahtml" => $extra,
                            "multiple"  => ""));
        $arg["data"][$name] = $objForm->ge($name);
        $arg["data"][$name."_NAME"] = ($isHighFlg) ? "" : "第三回一般";

        //志望区分コンボ
        $opt        = array();
        $opt[]      = array("label" => "", "value" => "");
        $result     = $db->query(knjl011yQuery::getDesirediv($model->year, $Row["APPLICANTDIV"], $model->general_testdiv2, "", "1"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["DESIREDIV"].":".$row["EXAMCOURSE_NAME"],
                           "value" => $row["DESIREDIV"]);
        }
        $result->free();
        $name   = "GENERAL_DESIREDIV2";
        $extra  = (strlen($Row["GENERAL_FLG2"])) ? "" : " disabled";
        $extra .= " onChange=\"change_flg();\"";
        $objForm->ae(array("type"        => "select",
                            "name"        => $name,
                            "size"        => "1",
                            "extrahtml"   => $extra,
                            "value"       => $Row[$name],
                            "options"     => $opt ));
        $arg["data"][$name] = $objForm->ge($name);

        //専併区分コンボ
        $opt        = array();
        $opt[]      = array("label" => "", "value" => "");
        $result     = $db->query(knjl011yQuery::get_name_cd($model->year, "L006"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $name   = "GENERAL_SHDIV2";
        $extra  = (strlen($Row["GENERAL_FLG2"])) ? "" : " disabled";
        $extra .= " onChange=\"change_flg();\"";
        $objForm->ae(array("type"        => "select",
                            "name"        => $name,
                            "size"        => "1",
                            "extrahtml"   => $extra,
                            "value"       => $Row[$name],
                            "options"     => $opt ));
        $arg["data"][$name] = $objForm->ge($name);


        /******************************* 希望3 *******************************/
        //適性検査型入試希望(中学：第一回推薦・第一回一般のみ)
        $name   = "GENERAL_FLG3";
        $defGeneral3 = "";
        if ($isJuniorFlg) {
            $defGeneral3 = (($Row["TESTDIV"] == "1" || $Row["TESTDIV"] == "2") && ($Row["JUDGEDIV"] == "2" || $Row["JUDGEDIV"] == "4") && $model->cmd == 'showdivAdd') ? "1" : "" ;
        }
        $Row[$name] = 0 < strlen($Row[$name]) ? $Row[$name] : $defGeneral3;
        $extra  = (0 < strlen($Row[$name])) ? "checked" : "";
        $extra .= " id=\"".$name."\"";
        if ($isJuniorFlg) {
            $extra .= (($Row["TESTDIV"] == "1" || $Row["TESTDIV"] == "2") && ($Row["JUDGEDIV"] == "2" || $Row["JUDGEDIV"] == "4")) ? "" : " disabled" ;
        } else {
            $extra .= " disabled" ;
        }
        $extra .= " onclick=\"disabledCommon('general_flg3', this.value);\"";
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => "1",
                            "extrahtml" => $extra,
                            "multiple"  => ""));
        $arg["data"][$name] = $objForm->ge($name);
        $arg["data"][$name."_NAME"] = ($isHighFlg) ? "" : "適性検査型";

        //志望区分コンボ
        $opt        = array();
        $opt[]      = array("label" => "", "value" => "");
        $result     = $db->query(knjl011yQuery::getDesirediv($model->year, $Row["APPLICANTDIV"], $model->general_testdiv3, "", "1"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["DESIREDIV"].":".$row["EXAMCOURSE_NAME"],
                           "value" => $row["DESIREDIV"]);
        }
        $result->free();
        $name   = "GENERAL_DESIREDIV3";
        $extra  = (strlen($Row["GENERAL_FLG3"])) ? "" : " disabled";
        $extra .= " onChange=\"change_flg();\"";
        $objForm->ae(array("type"        => "select",
                            "name"        => $name,
                            "size"        => "1",
                            "extrahtml"   => $extra,
                            "value"       => $Row[$name],
                            "options"     => $opt ));
        $arg["data"][$name] = $objForm->ge($name);

        //専併区分コンボ
        $opt        = array();
        $opt[]      = array("label" => "", "value" => "");
        $result     = $db->query(knjl011yQuery::get_name_cd($model->year, "L006"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $name   = "GENERAL_SHDIV3";
        $extra  = (strlen($Row["GENERAL_FLG3"])) ? "" : " disabled";
        $extra .= " onChange=\"change_flg();\"";
        $objForm->ae(array("type"        => "select",
                            "name"        => $name,
                            "size"        => "1",
                            "extrahtml"   => $extra,
                            "value"       => $Row[$name],
                            "options"     => $opt ));
        $arg["data"][$name] = $objForm->ge($name);


        //志望区分(スライド)(表示のみ)
        $isTokusin = false;
        $arg["data"]["SLIDE_DESIREDIV"] = "";
        $result    = $db->query(knjl011yQuery::getDesirediv($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"], $Row["DESIREDIV"], "2"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $isTokusin = true; //志望区分マスタに第２志望があればスライド希望可能とする
            $defSlide = $isHighFlg && $isTokusin && $model->cmd == 'showdivAdd' ? "1" : "";
            $Row["SLIDE_FLG"] = 0 < strlen($Row["SLIDE_FLG"]) ? $Row["SLIDE_FLG"] : $defSlide;
            //スライド希望ありの場合、表示する
            if (0 < strlen($Row["SLIDE_FLG"])) {
                $arg["data"]["SLIDE_DESIREDIV"] = "志望区分：".$row["DESIREDIV"].":".$row["EXAMCOURSE_NAME"];
            }
        }
        $result->free();

        //スライド希望(高校特進のみ)
        $name   = "SLIDE_FLG";
        $extra  = (0 < strlen($Row[$name])) ? "checked" : "";
        $extra .= " id=\"".$name."\"";
        $extra .= ($isHighFlg && $isTokusin) ? "" : " disabled" ;
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => "1",
                            "extrahtml" => $extra,
                            "multiple"  => ""));
        $arg["data"][$name] = $objForm->ge($name);

        //特別判定希望(高校学特のみ)
        $name   = "SHIFT_DESIRE_FLG";
        $extra  = (0 < strlen($Row[$name])) ? "checked" : "";
        $extra .= " id=\"".$name."\"";
        $extra .= ($isHighFlg && $Row["TESTDIV"] == "1") ? "" : " disabled" ;
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => "1",
                            "extrahtml" => $extra,
                            "multiple"  => ""));
        $arg["data"][$name] = $objForm->ge($name);

        //スポ優秀希望(高校進学、特進)
        $name   = "SPORTS_FLG";
        $extra  = (0 < strlen($Row[$name])) ? "checked" : "";
        $extra .= " id=\"".$name."\"";
        $extra .= ($isHighFlg) ? "" : " disabled" ;
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => "1",
                            "extrahtml" => $extra,
                            "multiple"  => ""));
        $arg["data"][$name] = $objForm->ge($name);

        //T特奨希望
        $name   = "SPORTS_FLG2";
        $extra  = (0 < strlen($Row[$name])) ? "checked" : "";
        $extra .= " id=\"".$name."\"";
        $extra .= ($isHighFlg) ? "" : " disabled" ;
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => "1",
                            "extrahtml" => $extra,
                            "multiple"  => ""));
        $arg["data"][$name] = $objForm->ge($name);

        //入寮希望
        $name   = "DORMITORY_FLG";
        $extra  = (0 < strlen($Row[$name])) ? "checked" : "";
        $extra .= " id=\"".$name."\"";
        $extra .= ($Row["SEX"] == "1") ? " disabled" : "" ;
        $objForm->ae(array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => "1",
                            "extrahtml" => $extra,
                            "multiple"  => ""));
        $arg["data"][$name] = $objForm->ge($name);

        /******************************* 希望 *******************************/



        global $sess;
        //郵便番号入力支援(志願者)
        $objForm->ae(array("type"        => "text",
                            "name"        => ZIPCD,
                            "size"        => 10,
                            "extrahtml"   => "STYLE=\"ime-mode: inactive;\" onblur=\"isZipcd(this), toCopytxt(0, this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["ZIPCD"]));
        //読込ボタンを作成する
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_zip",
                            "value"       => "郵便番号入力支援",
                            "extrahtml"   => "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd2.php?cmd=search&prefname=DUMMY_CD&prefname2=DUMMY_CD&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\""));
        //確定ボタンを作成する
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_apply",
                            "value"       => "確定",
                            "extrahtml"   => "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd2.php?cmd=apply&prefname=DUMMY_CD&prefname2=DUMMY_CD&addrname=ADDRESS1&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\""));
        $arg["data"]["ZIPCD"] = $objForm->ge("ZIPCD") .$objForm->ge("btn_zip") .$objForm->ge("btn_apply");


        //住所(志願者)
        $objForm->ae(array("type"        => "text",
                            "name"        => "ADDRESS1",
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onblur=\"toCopytxt(1, this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["ADDRESS1"] ));
        $arg["data"]["ADDRESS1"] = $objForm->ge("ADDRESS1");
        //方書(志願者)
        $objForm->ae(array("type"        => "text",
                            "name"        => "ADDRESS2",
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onblur=\"toCopytxt(2, this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["ADDRESS2"] ));
        $arg["data"]["ADDRESS2"] = $objForm->ge("ADDRESS2");
        //電話番号(志願者)
        $objForm->ae(array("type"        => "text",
                            "name"        => "TELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toTelNo(this.value), toCopytxt(3, this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["TELNO"] ));
        $arg["data"]["TELNO"] = $objForm->ge("TELNO");

        //出身学校
        $FsName     = array();
        $FsRitsuName = array();
        $result     = $db->query(knjl011yQuery::getFinschoolcd($model->year, $Row["FS_CD"]));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $FsName[$row["FINSCHOOLCD"]] = $row["FINSCHOOL_NAME"];
            $FsRitsuName[$row["FINSCHOOLCD"]] = $row["RITSU_NAME"];
        }
        $result->free();
        //コード
        $objForm->ae(array("type"        => "text",
                            "name"        => "FINSCHOOLCD",
                            "size"        => 7,
                            "maxlength"   => 7,
                            "extrahtml"   => "STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\" onblur=\"hiddenWin('" .REQUESTROOT ."/L/KNJL011Y/search_fin_name.php?cmd=apply&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&schoolcd='+document.forms[0]['FINSCHOOLCD'].value+'&frame='+getFrameName(self))\"",
                            "value"       => $Row["FS_CD"] ));
        $arg["data"]["FINSCHOOLCD"] = $objForm->ge("FINSCHOOLCD");
        //学校名
        $arg["data"]["FINSCHOOLNAME"] = $FsName[$Row["FS_CD"]];
        //ＸＸ立
        $arg["data"]["RITSU_NAME"] = $FsRitsuName[$Row["FS_CD"]];



        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $objForm->ae(array("type"        => "text",
                            "name"        => "GNAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onchange=\"change_flg()\"",
                            "value"       => $Row["GNAME"] ));
        $arg["data"]["GNAME"] = $objForm->ge("GNAME");
        //氏名かな(保護者)
        $objForm->ae(array("type"        => "text",
                            "name"        => "GKANA",
                            "size"        => 40,
                            "maxlength"   => 120,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onchange=\"change_flg()\"",
                            "value"       => $Row["GKANA"] ));
        $arg["data"]["GKANA"] = $objForm->ge("GKANA");

        //郵便番号入力支援(保護者)
        $objForm->ae(array("type"        => "text",
                            "name"        => "GZIPCD",
                            "size"        => 10,
                            "extrahtml"   => "STYLE=\"ime-mode: inactive;\" onblur=\"isZipcd(this)\" onchange=\"change_flg()\"",
                            "value"       => $Row["GZIPCD"]));
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_zip",
                            "value"       => "郵便番号入力支援",
                            "extrahtml"   => "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd2.php?cmd=search&prefname=DUMMY_CD&prefname2=DUMMY_CD&addrname=GADDRESS1&zipname=GZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\""));
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_apply",
                            "value"       => "確定",
                            "extrahtml"   => "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd2.php?cmd=apply&prefname=DUMMY_CD&prefname2=DUMMY_CD&addrname=GADDRESS1&zipname=GZIPCD&zip='+document.forms[0]['GZIPCD'].value+'&frame='+getFrameName(self))\""));
        $arg["data"]["GZIPCD"] = View::setIframeJs() .$objForm->ge("GZIPCD") .$objForm->ge("btn_zip") .$objForm->ge("btn_apply");

        //住所(保護者)
        $objForm->ae(array("type"        => "text",
                            "name"        => "GADDRESS1",
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onchange=\"change_flg()\"",
                            "value"       => $Row["GADDRESS1"] ));
        $arg["data"]["GADDRESS1"] = $objForm->ge("GADDRESS1");
        //方書(保護者)
        $objForm->ae(array("type"        => "text",
                            "name"        => "GADDRESS2",
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onchange=\"change_flg()\"",
                            "value"       => $Row["GADDRESS2"] ));
        $arg["data"]["GADDRESS2"] = $objForm->ge("GADDRESS2");
        //電話番号(保護者)
        $objForm->ae(array("type"        => "text",
                            "name"        => "GTELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toTelNo(this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["GTELNO"] ));
        $arg["data"]["GTELNO"] = $objForm->ge("GTELNO");
        //続柄コンボ
        $opt       = array();
        $opt[]     = array("label" => "", "value" => "");
        $result    = $db->query(knjl011yQuery::get_name_cd($model->year, 'H201'));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "RELATIONSHIP",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["RELATIONSHIP"],
                            "options"     => $opt ));
        $arg["data"]["RELATIONSHIP"] = $objForm->ge("RELATIONSHIP");


        //------------------------------内申科目---------------------------------
        $result = $db->query(knjl011yQuery::get_name_cd($model->year, "L008"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["CONFIDENTIAL".$row["NAMECD2"]] = $row["NAME1"];
        }
        $result->free();

        //------------------------------内申-------------------------------------
        $arg["data"]["RPT01"] = $Row["CONFIDENTIAL_RPT01"];
        $arg["data"]["RPT02"] = $Row["CONFIDENTIAL_RPT02"];
        $arg["data"]["RPT03"] = $Row["CONFIDENTIAL_RPT03"];
        $arg["data"]["RPT04"] = $Row["CONFIDENTIAL_RPT04"];
        $arg["data"]["RPT05"] = $Row["CONFIDENTIAL_RPT05"];
        $arg["data"]["RPT06"] = $Row["CONFIDENTIAL_RPT06"];
        $arg["data"]["RPT07"] = $Row["CONFIDENTIAL_RPT07"];
        $arg["data"]["RPT08"] = $Row["CONFIDENTIAL_RPT08"];
        $arg["data"]["RPT09"] = $Row["CONFIDENTIAL_RPT09"];
        $arg["data"]["RPT10"] = $Row["CONFIDENTIAL_RPT10"];
        $arg["data"]["TOTAL3"] = $Row["TOTAL3"];
        $arg["data"]["TOTAL5"] = $Row["TOTAL5"];
        $arg["data"]["TOTAL9"] = $Row["TOTAL9"];
        $arg["data"]["AVERAGE_ALL"]  = $Row["AVERAGE_ALL"];
        $arg["data"]["AVERAGE5"]  = $Row["AVERAGE5"];
        $arg["data"]["ABSENCE_DAYS1"] = $Row["ABSENCE_DAYS1"];
        $arg["data"]["ABSENCE_DAYS2"] = $Row["ABSENCE_DAYS2"];
        $arg["data"]["ABSENCE_DAYS3"] = $Row["ABSENCE_DAYS3"];
        $arg["data"]["ABSENCE_DAYS_NAME1"] = ($isHighFlg) ? "１年" : "１学期";
        $arg["data"]["ABSENCE_DAYS_NAME2"] = ($isHighFlg) ? "２年" : "２学期";
        $arg["data"]["ABSENCE_DAYS_NAME3"] = ($isHighFlg) ? "３年" : "３学期";

        knjCreateHidden($objForm, "CONFIDENTIAL_RPT01", $Row["CONFIDENTIAL_RPT01"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT02", $Row["CONFIDENTIAL_RPT02"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT03", $Row["CONFIDENTIAL_RPT03"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT04", $Row["CONFIDENTIAL_RPT04"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT05", $Row["CONFIDENTIAL_RPT05"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT06", $Row["CONFIDENTIAL_RPT06"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT07", $Row["CONFIDENTIAL_RPT07"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT08", $Row["CONFIDENTIAL_RPT08"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT09", $Row["CONFIDENTIAL_RPT09"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT10", $Row["CONFIDENTIAL_RPT10"]);
        knjCreateHidden($objForm, "TOTAL3", $Row["TOTAL3"]);
        knjCreateHidden($objForm, "TOTAL5", $Row["TOTAL5"]);
        knjCreateHidden($objForm, "TOTAL9", $Row["TOTAL9"]);
        knjCreateHidden($objForm, "AVERAGE_ALL", $Row["AVERAGE_ALL"]);
        knjCreateHidden($objForm, "AVERAGE5", $Row["AVERAGE5"]);
        knjCreateHidden($objForm, "ABSENCE_DAYS1", $Row["ABSENCE_DAYS1"]);
        knjCreateHidden($objForm, "ABSENCE_DAYS2", $Row["ABSENCE_DAYS2"]);
        knjCreateHidden($objForm, "ABSENCE_DAYS3", $Row["ABSENCE_DAYS3"]);

        //------------------------------備考-------------------------------------
/***
        //備考１
        $objForm->ae( array("type"        => "text",
                            "name"        => "REMARK1",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["REMARK1"] ));
        $arg["data"]["REMARK1"] = $objForm->ge("REMARK1");
        //備考２
        $objForm->ae( array("type"        => "text",
                            "name"        => "REMARK2",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["REMARK2"] ));
        $arg["data"]["REMARK2"] = $objForm->ge("REMARK2");
***/
        //size   = 文字数 * 2 + 1
        //height = 行数 * 13.5 + (行数 -1) * 3 + 5
        //備考１
        $arg["data"]["REMARK1"] = KnjCreateTextArea($objForm, "REMARK1", 4, 41, "soft", "style=\"ime-mode: active; height:68px;\" onchange=\"change_flg()\"", $Row["REMARK1"]);
        //備考２
        $arg["data"]["REMARK2"] = KnjCreateTextArea($objForm, "REMARK2", 2, 41, "soft", "style=\"ime-mode: active; height:35px;\" onchange=\"change_flg()\"", $Row["REMARK2"]);

        Query::dbCheckIn($db);

        //-------------------------------- ボタン作成 ------------------------------------
        $zip = $Row["ZIPCD"];
        $gzip = $Row["GZIPCD"];
        $zadd = $Row["ADDRESS1"];
        $gadd = $Row["GADDRESS1"];
        //新規ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_addnew",
                            "value"     => "新 規",
                            "extrahtml" => "onclick=\"return btn_submit('addnew');\""  ));
        $arg["button"]["btn_addnew"] = $objForm->ge("btn_addnew");
        //検索ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_reference",
                            "value"     => "検 索",
                            "extrahtml" => "onclick=\"return btn_submit('reference', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"" ));
        $arg["button"]["btn_reference"] = $objForm->ge("btn_reference");
        //かな検索ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_kana_reference",
                            "value"       => "かな検索",
                            "extrahtml"   => "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011Y/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\""));
        $arg["button"]["btn_kana_reference"] = $objForm->ge("btn_kana_reference");
        //前の志願者検索ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => " << ",
                            "extrahtml"   => "style=\"width:32px\" onClick=\"btn_submit('back1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"" ));
        //次の志願者検索ボタン
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => " >> ",
                            "extrahtml"   => "style=\"width:32px\" onClick=\"btn_submit('next1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"" ));
        $arg["button"]["btn_back_next"] = $objForm->ge("btn_back").$objForm->ge("btn_next");
        //画面クリアボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_clear",
                            "value"     => "画面クリア",
                            "extrahtml" => "style=\"width:80px\" onclick=\"return btn_submit('disp_clear');\"" ));
        $arg["button"]["btn_clear"] = $objForm->ge("btn_clear");
        //かな検索ボタン（併願校）
        $disBtnFinHigh = ($Row["SHDIV"] == "2") ? "" : "disabled " ;
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_fin_high_kana_reference",
                            "value"       => "検 索",
                            "extrahtml"   => $disBtnFinHigh ."onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011Y/search_fin_high_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 335, 370)\""));
        $arg["button"]["btn_fin_high_kana_reference"] = $objForm->ge("btn_fin_high_kana_reference");

        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);


        //志願者よりコピーボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_copy",
                            "value"     => "志願者よりコピー",
                            "extrahtml" => "style=\"width:135px\" onclick=\"return btn_submit('copy');\"" ));
        $arg["button"]["btn_copy"] = $objForm->ge("btn_copy");
        //追加ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_add",
                            "value"     => "追 加",
                            "extrahtml" => "onclick=\"return btn_submit('add');\"" ));
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");
        //更新ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_udpate",
                            "value"     => "更 新",
                            "extrahtml" => "$disabled onclick=\"return btn_submit('update');\"" ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");
        //更新ボタン(更新後前の志願者)
        $objForm->ae(array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の志願者",
                            "extrahtml" => "$disabled style=\"width:150px\" onclick=\"return btn_submit('back');\"" ));
        //更新ボタン(更新後次の志願者)
        $objForm->ae(array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の志願者",
                            "extrahtml" =>  "$disabled style=\"width:150px\" onclick=\"return btn_submit('next');\"" ));
        $arg["button"]["btn_up_next"] = $objForm->ge("btn_up_pre") . $objForm->ge("btn_up_next");
        //削除ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_del",
                            "value"     => "削 除",
                            "extrahtml" => "$disabled onclick=\"return btn_submit('delete');\"" ));
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");
        //取消ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('reset');\""  ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");
        //終了ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");
        //欠席ボタン
        $extra = $disabled ." onclick=\"loadwindow('knjl011yindex.php?cmd=attend&ATTEND_APPLICANTDIV={$Row["APPLICANTDIV"]}&ATTEND_EXAMNO={$model->examno}',body.clientWidth/2-200,body.clientHeight/2-100,500,250);\"";
        $arg["button"]["btn_attend"] = knjCreateBtn($objForm, "btn_attend", "欠 席", $extra);

        if ($model->Properties["knjl011yShowBoshuKikakuBtn"] == '1') {
            //募集企画情報
            $extra  = " onClick=\" wopen('".REQUESTROOT."/L/KNJL410/knjl410index.php?";
            $extra .= "SEND_PRGRID=KNJL011Y";
            $extra .= "&SEND_AUTH=".AUTHORITY;
            $extra .= "&SEND_NAME=".$Row["NAME"];
            $extra .= "&SEND_KANA=".$Row["NAME_KANA"];
            $extra .= "&SEND_FINSCHOOLCD=".$Row["FS_CD"];
            $extra .= "&SUBWIN=SUBWIN3','SUBWIN3',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_boshukikaku"] = knjCreateBtn($objForm, "btn_event", "募集企画情報", $extra);
        }

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "ctrl_year",
                            "value"     => CTRL_YEAR));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "ctrl_semester",
                            "value"     => CTRL_SEMESTER));
/***
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "FS_CD",
                            "value"     => $Row["FS_CD"]) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SH_SCHOOLCD",
                            "value"     => $Row["SH_SCHOOLCD"]) );
***/
        //郵便番号支援の都道府県ダミー用
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "DUMMY_CD"));

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl011yForm1.html", $arg);
    }
}