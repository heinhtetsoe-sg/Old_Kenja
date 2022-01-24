<?php

require_once('for_php7.php');

class knjl011tForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl011tindex.php", "", "main");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //デフォルト設定
        if ($model->examno == "" || $model->cmd == "showdiv") {
            $db = Query::dbCheckOut();
            $result = $db->query(knjl011tQuery::get_name_cd($model->year, 'L003'));
            $defApplicantDiv = $model->field["APPLICANTDIV"];
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $defApplicantDiv = ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') ? $row["NAMECD2"] : $defApplicantDiv;
            }
            $model->examno = $db->getOne(knjl011tQuery::getMinExam($defApplicantDiv));
            Query::dbCheckIn($db);
        }

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            //データを取得
            $Row = knjl011tQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl011tQuery::get_edit_data($model);
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
        }
        if ($model->cmd == 'major') {
            $Row =& $model->field;
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        $db = Query::dbCheckOut();

        //------------------------------志願者情報-------------------------------------
        //入試制度コンボ
        $result     = $db->query(knjl011tQuery::get_name_cd($model->year, 'L003'));
        $opt        = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
            if ($Row["APPLICANTDIV"]=="" && $row["NAMESPARE2"]=='1') $Row["APPLICANTDIV"] = $row["NAMECD2"];
        }
        $result->free();
        $objForm->ae( array("type"        => "select",
                            "name"        => "APPLICANTDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg(); return btn_submit('showdiv');\"",
                            "value"       => $Row["APPLICANTDIV"],
                            "options"     => $opt ) );

        $arg["data"]["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");



        //1:推薦と2:一般の表示切替フラグ
        if ($Row["APPLICANTDIV"] == "1") {
            $arg["showdiv1"]    = 1;
            $arg["rowspan"]     = "9";
        } else {
            $arg["showdiv2"]    = 1;
            $arg["rowspan"]     = "8";
        }

        //学科マスタ（大）
        $optMajorL = array();
        $optMajorL[] = array("label" => "", "value" => "");
        $result = $db->query(knjl011tQuery::getMajorLMst());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $optMajorL[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
        }
        $result->free();
        //志望区分（順位）コンボ
        for ($m = 1; $m <= 6; $m++) {
            $defNameCd = $Row["APPLICANTDIV"] == "1" ? "L020" : "L021";
            $defCdRow = $db->getRow(knjl011tQuery::get_name_cd($model->year, $defNameCd, $m), DB_FETCHMODE_ASSOC);
            $defLcd = $model->cmd == 'showdivAdd' ? $defCdRow["NAME1"] : "";
            $defScd = $model->cmd == 'showdivAdd' ? $defCdRow["NAME2"] : "";

            $nameL = "MAJORLCD" .$m;
            $objForm->ae( array("type"        => "select",
                                "name"        => $nameL,
                                "size"        => "1",
                                "extrahtml"   => "onChange=\"change_flg(); return btn_submit('major');\"",
                                "value"       => strlen($Row[$nameL]) ? $Row[$nameL] : $defLcd,
                                "options"     => $optMajorL ) );
            $arg["data"][$nameL] = $objForm->ge($nameL);
            //学科マスタ（小）
            $extra = "style=\"width:80px\" ";
            $optMajorS = array();
            $optMajorS[] = array("label" => "", "value" => "");
            $result = $db->query(knjl011tQuery::getMajorSMst($Row[$nameL] ? $Row[$nameL] : $defLcd));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $optMajorS[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
                $extra = "";
            }
            $result->free();
            $nameS = "MAJORSCD" .$m;
            $objForm->ae( array("type"        => "select",
                                "name"        => $nameS,
                                "size"        => "1",
                                "extrahtml"   => $extra ."onChange=\"change_flg()\"",
                                "value"       => strlen($Row[$nameS]) ? $Row[$nameS] : $defScd,
                                "options"     => $optMajorS ) );
            $arg["data"][$nameS] = $objForm->ge($nameS);
        }

        //学校コード取得
        $query = knjl011tQuery::getSchoolname();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolCode = $rowZ010["NAME2"];

        //推薦区分の表示フラグ・・・境高校の推薦入試の場合
        if ($schoolCode == "220410" && $Row["APPLICANTDIV"] == "1") {
            $arg["SHOW_SUISEN"] = '1'; //null以外なら何でもいい
            $arg["COLSPAN_SUISEN"] = '3';
        } else {
            unset($arg["SHOW_SUISEN"]);
            $arg["COLSPAN_SUISEN"] = '7';
        }

        //推薦区分コンボ（1:体育,2:学力,3:文化）
        $optSuisen = array();
        $optSuisen[] = array("label" => "", "value" => "");
        $result = $db->query(knjl011tQuery::getSuisenKubun($model->year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $optSuisen[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
        }
        $result->free();
        $objForm->ae( array("type"        => "select",
                            "name"        => "SHIFT_DESIRE_FLG",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["SHIFT_DESIRE_FLG"],
                            "options"     => $optSuisen ) );
        $arg["data"]["SHIFT_DESIRE_FLG"] = $objForm->ge("SHIFT_DESIRE_FLG");

        //学力検査受検希望教科など
        $optTestSubcd = array();
        $result = $db->query(knjl011tQuery::getTestSubcd($model->year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $optTestSubcd[$row["VALUE"]] = $row["LABEL"];
        }
        $result->free();
        for ($m = 1; $m <= 5; $m++) {
            //学力検査受検希望教科・名称
            $arg["data"]["TESTSUBCLASSNAME" .$m] = $optTestSubcd[$m];
            //学力検査受検希望教科・コード
            $name = "HOPEFLG" .$m;
            $extra = (0 < strlen($Row[$name])) ? "checked" : "";
            $extra .= " id=\"".$name."\"";
            $objForm->ae( array("type"      => "checkbox",
                                "name"      => $name,
                                "value"     => $m,
                                "extrahtml" => $extra,
                                "multiple"  => ""));
            $arg["data"][$name] = $objForm->ge($name);
            //傾斜配点希望教科・コード
            $name = "INC_HOPEFLG" .$m;
            $extra = (0 < strlen($Row[$name])) ? "checked" : "";
            $extra .= " id=\"".$name."\"";
            $objForm->ae( array("type"      => "checkbox",
                                "name"      => $name,
                                "value"     => $m,
                                "extrahtml" => $extra,
                                "multiple"  => ""));
            $arg["data"][$name] = $objForm->ge($name);
        }


        //追検査
        $name = "TESTDIV2";
        $extra = (0 < strlen($Row[$name])) ? "checked" : "";
        $extra .= " id=\"".$name."\"";
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => $name,
                            "value"     => "1",
                            "extrahtml" => $extra,
                            "multiple"  => ""));
        $arg["data"][$name] = $objForm->ge($name);


        //受検番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => preg_replace('/^0*/', '', $model->examno) ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //氏名(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["NAME"] ));
        $arg["data"]["NAME"] = $objForm->ge("NAME");

        //氏名かな(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME_KANA",
                            "size"        => 40,
                            "maxlength"   => 120,
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["NAME_KANA"] ));
        $arg["data"]["NAME_KANA"] = $objForm->ge("NAME_KANA");

        //性別コンボ
        $result     = $db->query(knjl011tQuery::get_name_cd($model->year, 'Z002'));
        $opt        = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $objForm->ae( array("type"        => "select",
                            "name"        => "SEX",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["SEX"],
                            "options"     => $opt ) );
        $arg["data"]["SEX"] = $objForm->ge("SEX");

        //生年月日（西暦）
        $extra = " onchange=\"change_flg()\"";
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar2($objForm, "BIRTHDAY", str_replace("-","/",$Row["BIRTHDAY"]), "", "", $extra);

        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl011tQuery::get_calendarno($model->year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
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
        $objForm->ae( array("type"        => "text",
                            "name"        => "ERACD",
                            "size"        => 1,
                            "maxlength"   => 1,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(0, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onchange=\"change_flg()\"",
                            "value"       => strlen($Row["ERACD"]) ? $Row["ERACD"] : "4" ));
        $arg["data"]["ERACD"] = $objForm->ge("ERACD");

        //和暦名
        if (isset($Row["NAME1"])) {
            $name1 = $Row["NAME1"];
        } else if(isset($Row["WNAME"])) {
            $name1 = str_replace("&nbsp;", "", $Row["WNAME"]);
        } else {
            $name1 = "平成";
        }
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "WNAME",
                            "value"     => $name1) );
        $arg["data"]["WNAME"] = $name1;

        //生年月日年
        $objForm->ae( array("type"        => "text",
                            "name"        => "BIRTH_Y",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["BIRTH_Y"] ));
        $arg["data"]["BIRTH_Y"] = $objForm->ge("BIRTH_Y");

        //生年月日月
        $objForm->ae( array("type"        => "text",
                            "name"        => "BIRTH_M",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["BIRTH_M"] ));
        $arg["data"]["BIRTH_M"] = $objForm->ge("BIRTH_M");

        //生年月日日
        $objForm->ae( array("type"        => "text",
                            "name"        => "BIRTH_D",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(3, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["BIRTH_D"] ));
        $arg["data"]["BIRTH_D"] = $objForm->ge("BIRTH_D");

        //卒業元号
        $objForm->ae( array("type"        => "text",
                            "name"        => "FS_ERACD",
                            "size"        => 1,
                            "maxlength"   => 1,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(0, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onchange=\"change_flg()\"",
                            "value"       => strlen($Row["FS_ERACD"]) ? $Row["FS_ERACD"] : "4" ));
        $arg["data"]["FS_ERACD"] = $objForm->ge("FS_ERACD");
        //卒業和暦名
        $fs_wname = isset($Row["FS_WNAME"]) ? str_replace("&nbsp;", "", $Row["FS_WNAME"]) : "平成";
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "FS_WNAME",
                            "value"     => $fs_wname ) );
        $arg["data"]["FS_WNAME"] = $fs_wname;
        //卒業年
        $objForm->ae( array("type"        => "text",
                            "name"        => "FS_Y",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["FS_Y"] ));
        $arg["data"]["FS_Y"] = $objForm->ge("FS_Y");
        //卒業月
        $objForm->ae( array("type"        => "text",
                            "name"        => "FS_M",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["FS_M"] ));
        $arg["data"]["FS_M"] = $objForm->ge("FS_M");
        //卒業区分（1:見込み,2:卒業）
        $optGrddiv = array();
        $optGrddiv[] = array("label" => "", "value" => "");
        $result = $db->query(knjl011tQuery::get_name_cd($model->year, 'L016'));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $optGrddiv[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                                 "value" => $row["NAMECD2"]);
        }
        $result->free();
        $defGrddiv = $model->cmd == 'showdivAdd' ? "1" : "";
        $objForm->ae( array("type"        => "select",
                            "name"        => "FS_GRDDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => strlen($Row["FS_GRDDIV"]) ? $Row["FS_GRDDIV"] : $defGrddiv,
                            "options"     => $optGrddiv ) );
        $arg["data"]["FS_GRDDIV"] = $objForm->ge("FS_GRDDIV");



        global $sess;
        //郵便番号入力支援(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => ZIPCD,
                            "size"        => 10,
                            "extrahtml"   => "onblur=\"isZipcd(this), toCopytxt(0, this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["ZIPCD"]));
        //読込ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_zip",
                            "value"       => "郵便番号入力支援",
                            "extrahtml"   => "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd2.php?cmd=search&prefname=DUMMY_CD&prefname2=DUMMY_CD&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
        //確定ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_apply",
                            "value"       => "確定",
                            "extrahtml"   => "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd2.php?cmd=apply&prefname=DUMMY_CD&prefname2=DUMMY_CD&addrname=ADDRESS1&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"") );
        $arg["data"]["ZIPCD"] = $objForm->ge("ZIPCD") .$objForm->ge("btn_zip") .$objForm->ge("btn_apply");


        //住所(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "ADDRESS1",
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "onblur=\"toCopytxt(1, this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["ADDRESS1"] ));
        $arg["data"]["ADDRESS1"] = $objForm->ge("ADDRESS1");
        //方書(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "ADDRESS2",
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "onblur=\"toCopytxt(2, this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["ADDRESS2"] ));
        $arg["data"]["ADDRESS2"] = $objForm->ge("ADDRESS2");
        //電話番号(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "TELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "onblur=\"this.value=toTelNo(this.value), toCopytxt(3, this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["TELNO"] ));
        $arg["data"]["TELNO"] = $objForm->ge("TELNO");

        //出身学校
        $FsName     = array();
        $FsDistName = array();
        $optFscd[] = array("label" => "", "value" => "");
        $result     = $db->query(knjl011tQuery::getFinschoolcd($model->year, $Row["FS_CD"]));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $FsName[$row["FINSCHOOLCD"]] = $row["FINSCHOOL_NAME"];
            $FsDistName[$row["FINSCHOOLCD"]] = $row["DISTRICTCD_NAME"];

            $optFscd[] = array("label" => $row["FINSCHOOLCD"].":".$row["FINSCHOOL_NAME"],
                               "value" => $row["FINSCHOOLCD"]);
        }
        $result->free();
/***
        //コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "FS_CD",
                            "size"        => 7,
                            "maxlength"   => 7,
                            "extrahtml"   => "onchange=\"change_flg()\" onblur=\"hiddenWin('" .REQUESTROOT ."/L/KNJL011T/search_fin_name.php?cmd=apply&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&schoolcd='+document.forms[0]['FS_CD'].value+'&frame='+getFrameName(self))\"",
                            "value"       => $Row["FS_CD"] ) );
        $arg["data"]["FS_CD"] = $objForm->ge("FS_CD");
***/
        //学校名
        $arg["data"]["FS_NAME"] = $FsName[$Row["FS_CD"]];
        //地区
        $arg["data"]["DISTRICTCD_NAME"] = $FsDistName[$Row["FS_CD"]];

        //出身学校コンボ
        $objForm->ae( array("type"        => "select",
                            "name"        => "FS_CD",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["FS_CD"],
                            "options"     => $optFscd ) );
        $arg["data"]["FS_CD"] = $objForm->ge("FS_CD");



        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GNAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["GNAME"] ));
        $arg["data"]["GNAME"] = $objForm->ge("GNAME");
        //氏名かな(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GKANA",
                            "size"        => 40,
                            "maxlength"   => 120,
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["GKANA"] ));
        $arg["data"]["GKANA"] = $objForm->ge("GKANA");

        //郵便番号入力支援(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GZIPCD",
                            "size"        => 10,
                            "extrahtml"   => "onblur=\"isZipcd(this)\" onchange=\"change_flg()\"",
                            "value"       => $Row["GZIPCD"]));
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_zip",
                            "value"       => "郵便番号入力支援",
                            "extrahtml"   => "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd2.php?cmd=search&prefname=DUMMY_CD&prefname2=DUMMY_CD&addrname=GADDRESS1&zipname=GZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_apply",
                            "value"       => "確定",
                            "extrahtml"   => "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd2.php?cmd=apply&prefname=DUMMY_CD&prefname2=DUMMY_CD&addrname=GADDRESS1&zipname=GZIPCD&zip='+document.forms[0]['GZIPCD'].value+'&frame='+getFrameName(self))\"") );
        $arg["data"]["GZIPCD"] = View::setIframeJs() .$objForm->ge("GZIPCD") .$objForm->ge("btn_zip") .$objForm->ge("btn_apply");

        //住所(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GADDRESS1",
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["GADDRESS1"] ));
        $arg["data"]["GADDRESS1"] = $objForm->ge("GADDRESS1");
        //方書(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GADDRESS2",
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["GADDRESS2"] ));
        $arg["data"]["GADDRESS2"] = $objForm->ge("GADDRESS2");
        //電話番号(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GTELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "onblur=\"this.value=toTelNo(this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["GTELNO"] ));
        $arg["data"]["GTELNO"] = $objForm->ge("GTELNO");


        //------------------------------内申科目---------------------------------
        $result = $db->query(knjl011tQuery::get_name_cd($model->year, "L008"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
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
        $arg["data"]["ABSENCE_DAYS1"] = $Row["ABSENCE_DAYS1"];
        $arg["data"]["ABSENCE_DAYS2"] = $Row["ABSENCE_DAYS2"];
        $arg["data"]["ABSENCE_DAYS3"] = $Row["ABSENCE_DAYS3"];

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
        knjCreateHidden($objForm, "ABSENCE_DAYS1", $Row["ABSENCE_DAYS1"]);
        knjCreateHidden($objForm, "ABSENCE_DAYS2", $Row["ABSENCE_DAYS2"]);
        knjCreateHidden($objForm, "ABSENCE_DAYS3", $Row["ABSENCE_DAYS3"]);

        //------------------------------備考-------------------------------------
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


        Query::dbCheckIn($db);

        //-------------------------------- ボタン作成 ------------------------------------
        $zip = $Row["ZIPCD"];
        $gzip = $Row["GZIPCD"];
        $zadd = $Row["ADDRESS1"];
        $gadd = $Row["GADDRESS1"];
        //新規ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_addnew",
                            "value"     => "新 規",
                            "extrahtml" => "onclick=\"return btn_submit('addnew');\""  ) );
        $arg["button"]["btn_addnew"] = $objForm->ge("btn_addnew");
        //検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reference",
                            "value"     => "検 索",
                            "extrahtml" => "onclick=\"return btn_submit('reference', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"" ) );
        $arg["button"]["btn_reference"] = $objForm->ge("btn_reference");
        //かな検索ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_kana_reference",
                            "value"       => "かな検索",
                            "extrahtml"   => "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011T/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
        $arg["button"]["btn_kana_reference"] = $objForm->ge("btn_kana_reference");
        //前の志願者検索ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => " << ",
                            "extrahtml"   => "style=\"width:32px\" onClick=\"btn_submit('back1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"" ) );
        //次の志願者検索ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => " >> ",
                            "extrahtml"   => "style=\"width:32px\" onClick=\"btn_submit('next1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"" ) );
        $arg["button"]["btn_back_next"] = $objForm->ge("btn_back").$objForm->ge("btn_next");
        //画面クリアボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_clear",
                            "value"     => "画面クリア",
                            "extrahtml" => "style=\"width:80px\" onclick=\"return btn_submit('disp_clear');\"" ) );
        $arg["button"]["btn_clear"] = $objForm->ge("btn_clear");
        //かな検索ボタン（出身学校）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_fin_kana_reference",
                            "value"       => "検 索",
                            "extrahtml"   => "onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011T/search_fin_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 335, 370)\"") );
        $arg["button"]["btn_fin_kana_reference"] = $objForm->ge("btn_fin_kana_reference");
        //志願者よりコピーボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_copy",
                            "value"     => "志願者よりコピー",
                            "extrahtml" => "style=\"width:135px\" onclick=\"return btn_submit('copy');\"" ) );
        $arg["button"]["btn_copy"] = $objForm->ge("btn_copy");
        //追加ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_add",
                            "value"     => "追 加",
                            "extrahtml" => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");
        //更新ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_udpate",
                            "value"     => "更 新",
                            "extrahtml" => "$disabled onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");
        //更新ボタン(更新後前の志願者)
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の志願者",
                            "extrahtml" => "$disabled style=\"width:150px\" onclick=\"return btn_submit('back');\"" ) );
        //更新ボタン(更新後次の志願者)
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の志願者",
                            "extrahtml" =>  "$disabled style=\"width:150px\" onclick=\"return btn_submit('next');\"" ) );
        $arg["button"]["btn_up_next"] = $objForm->ge("btn_up_pre") . $objForm->ge("btn_up_next");
        //削除ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_del",
                            "value"     => "削 除",
                            "extrahtml" => "$disabled onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");
        //取消ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('reset');\""  ) );
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");
        //終了ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ctrl_year",
                            "value"     => CTRL_YEAR) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ctrl_semester",
                            "value"     => CTRL_SEMESTER) );
/***
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "FS_CD",
                            "value"     => $Row["FS_CD"]) );
***/
        //郵便番号支援の都道府県ダミー用
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DUMMY_CD") );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl011tForm1.html", $arg);
    }
}
?>