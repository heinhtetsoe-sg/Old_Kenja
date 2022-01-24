<?php

require_once('for_php7.php');

class knjl016cForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl016cindex.php", "", "main");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //デフォルト設定
        if ($model->examno == "" || $model->cmd == "showdiv") {
            $db = Query::dbCheckOut();
            $result = $db->query(knjl016cQuery::get_name_cd($model->year, 'L003'));
            $defApplicantDiv = $model->field["APPLICANTDIV"];
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $defApplicantDiv = ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') ? $row["NAMECD2"] : $defApplicantDiv;
            }
            $model->examno = $db->getOne(knjl016cQuery::getMinExam($model->year, $defApplicantDiv));
            Query::dbCheckIn($db);
        }

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            //データを取得
            $Row = knjl016cQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl016cQuery::get_edit_data($model);
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
                $Row["SHDIV"] = $model->field["SHDIV"];
                $Row["SELECT_SUBCLASS_DIV"] = $model->field["SELECT_SUBCLASS_DIV"];
            }
        }
        if ($model->cmd == 'testdiv' || $model->cmd == 'shdiv') {
            $Row =& $model->field;
        }
        /***
        if ($model->cmd == 'showdiv' || $model->cmd == 'showdivAdd' || $model->cmd == 'testdiv' || $model->cmd == 'shdiv') {
            $Row =& $model->field;
        }
        ***/

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        $db = Query::dbCheckOut();

        //------------------------------志願者情報-------------------------------------
        //入試制度コンボ
        $result     = $db->query(knjl016cQuery::get_name_cd($model->year, 'L003'));
        $opt        = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
            if ($Row["APPLICANTDIV"]=="" && $row["NAMESPARE2"]=='1') $Row["APPLICANTDIV"] = $row["NAMECD2"];
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "APPLICANTDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg(); return btn_submit('showdiv');\"",
                            "value"       => $Row["APPLICANTDIV"],
                            "options"     => $opt ) );

        $arg["data"]["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");
        $result->free();

        //入試区分コンボ
        $result     = $db->query(knjl016cQuery::get_name_cd($model->year, 'L004'));
        $opt_testdiv = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_testdiv[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                                   "value" => $row["NAMECD2"]);
            if ($Row["TESTDIV"]=="" && $row["NAMESPARE2"]=='1') $Row["TESTDIV"] = $row["NAMECD2"];
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg(); return btn_submit('testdiv');\"",
                            "value"       => $Row["TESTDIV"],
                            "options"     => $opt_testdiv ) );

        $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");
        $result->free();

        //専併コンボ
        $result     = $db->query(knjl016cQuery::get_name_cd($model->year, 'L006'));
        $opt_shdiv = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_shdiv[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                                 "value" => $row["NAMECD2"]);
            if ($Row["SHDIV"]=="") $Row["SHDIV"] = $row["NAMECD2"];
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "SHDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg(); return btn_submit('shdiv');\"",
                            "value"       => $Row["SHDIV"],
                            "options"     => $opt_shdiv ) );

        $arg["data"]["SHDIV"] = $objForm->ge("SHDIV");
        $result->free();

        /****************/
        /* 画面表示切替 */
        /****************/
        $arg["csShdiv"] = "colspan=\"8\"";
        $arg["csDormitory"] = "colspan=\"3\"";
        $arg["csName"] = "colspan=\"8\"";

        if ($Row["APPLICANTDIV"] == "2" && $Row["TESTDIV"] == "3" && $Row["SHDIV"] == "2") {
            $arg["isSlide"] = "1";//高校・編入・併願
            $arg["csShdiv"] = "colspan=\"6\"";
        } else 
        if ($Row["APPLICANTDIV"] == "2" && ($Row["TESTDIV"] == "7" || $Row["TESTDIV"] == "8")) {
            $arg["isSports"] = "1";//高校・文系
            $arg["csShdiv"] = "colspan=\"6\"";
            $arg["csName"] = "colspan=\"6\"";
            $arg["data"]["SPORTS_CLASS"] = $Row["TESTDIV"] == "7" ? "S組" : "ｽﾎﾟｰﾂ1組";
        } else 
        if ($Row["APPLICANTDIV"] == "1" && ($Row["TESTDIV"] == "1" || $Row["TESTDIV"] == "2" || $Row["TESTDIV"] == "7")) {
            $arg["isShift"] = "1";//中学
            $arg["isPersonal"] = "1";//中学
            $arg["csShdiv"] = "colspan=\"6\"";
            $arg["csDormitory"] = "";
        }

        if ($Row["APPLICANTDIV"] == "2" && $Row["SHDIV"] == "2" && ($Row["TESTDIV"] == "3" || $Row["TESTDIV"] == "7" || $Row["TESTDIV"] == "8")) {
            $arg["isShSchool"] = "1";//高校・併願
            $arg["csShdiv"] = "";
        }


        //カレッジ併願チェックボックス
        $extra  = "id=\"SHIFT_DESIRE_FLG\" onchange=\"change_flg()\"";
        $extra .= strlen($Row["SHIFT_DESIRE_FLG"]) ? " checked" : "";
        $arg["data"]["SHIFT_DESIRE_FLG"] = knjCreateCheckBox($objForm, "SHIFT_DESIRE_FLG", "1", $extra, "");

        //選択受験科目コンボ
        $result     = $db->query(knjl016cQuery::get_name_cd($model->year, 'L009'));
        $opt_select_subclass_div = array();
        $opt_select_subclass_div[] = array("label" => "", "value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMESPARE1"]=='1') {
                $opt_select_subclass_div[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                                                   "value" => $row["NAMECD2"]);
            }
        }

        $disSelSubDiv = ($Row["APPLICANTDIV"] == "2" && $Row["TESTDIV"] == "7") ? "" : "disabled ";
        $objForm->ae( array("type"        => "select",
                            "name"        => "SELECT_SUBCLASS_DIV",
                            "size"        => "1",
                            "extrahtml"   => $disSelSubDiv ."onChange=\"change_flg()\"",
                            "value"       => $Row["SELECT_SUBCLASS_DIV"],
                            "options"     => $opt_select_subclass_div ) );

        $arg["data"]["SELECT_SUBCLASS_DIV"] = $objForm->ge("SELECT_SUBCLASS_DIV");
        $result->free();

        //受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $model->examno ));

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
        $result     = $db->query(knjl016cQuery::get_name_cd($model->year, 'Z002'));
        $opt        = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "SEX",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["SEX"],
                            "options"     => $opt ) );

        $arg["data"]["SEX"] = $objForm->ge("SEX");
        $result->free();

        //生年月日（西暦）
        $extra = " onchange=\"change_flg()\"";
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar2($objForm, "BIRTHDAY", str_replace("-","/",$Row["BIRTHDAY"]), "", "", $extra);

        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl016cQuery::get_calendarno($model->year));
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

        if (isset($Row["NAME1"])) {
            $name1 = $Row["NAME1"];
        } else if(isset($Row["WNAME"])) {
            $name1 = str_replace("&nbsp;", "", $Row["WNAME"]);
        } else {
            $name1 = "　　";
        }

        $arg["data"]["WNAME"] = $name1;

        //和暦名
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "WNAME",
                            "value"     => $name1) );

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
                            "value"       => $Row["FS_ERACD"] ));
        $arg["data"]["FS_ERACD"] = $objForm->ge("FS_ERACD");
        //卒業和暦名
        $fs_wname = isset($Row["FS_WNAME"]) ? str_replace("&nbsp;", "", $Row["FS_WNAME"]) : "　　";
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

        //撮影元号
        $objForm->ae( array("type"        => "text",
                            "name"        => "PICTURE_ERACD",
                            "size"        => 1,
                            "maxlength"   => 1,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(0, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onchange=\"change_flg()\"",
                            "value"       => $Row["PICTURE_ERACD"] ));
        $arg["data"]["PICTURE_ERACD"] = $objForm->ge("PICTURE_ERACD");
        //撮影和暦名
        $picture_wname = isset($Row["PICTURE_WNAME"]) ? str_replace("&nbsp;", "", $Row["PICTURE_WNAME"]) : "　　";
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PICTURE_WNAME",
                            "value"     => $picture_wname ) );
        $arg["data"]["PICTURE_WNAME"] = $picture_wname;
        //撮影年
        $objForm->ae( array("type"        => "text",
                            "name"        => "PICTURE_Y",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["PICTURE_Y"] ));
        $arg["data"]["PICTURE_Y"] = $objForm->ge("PICTURE_Y");
        //撮影月
        $objForm->ae( array("type"        => "text",
                            "name"        => "PICTURE_M",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["PICTURE_M"] ));
        $arg["data"]["PICTURE_M"] = $objForm->ge("PICTURE_M");


        //都道府県マスタよりデータを取得
        $opt_pref = array();
        $opt_pref[] = array("label" => "", "value" => "");
        $result = $db->query(knjl016cQuery::getPrefMst());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_pref[] = array("label" => $row["PREF_CD"].":".$row["PREF_NAME"],
                                "value" => $row["PREF_CD"]);
        }
        $result->free();
        //都道府県コンボ（本籍地）
        $objForm->ae( array("type"        => "select",
                            "name"        => "FAMILY_REGISTER",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["FAMILY_REGISTER"],
                            "options"     => $opt_pref ) );
        $arg["data"]["FAMILY_REGISTER"] = $objForm->ge("FAMILY_REGISTER");
        //都道府県コンボ（志願者）
        $objForm->ae( array("type"        => "select",
                            "name"        => "PREF_CD",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["PREF_CD"],
                            "options"     => $opt_pref ) );
        $arg["data"]["PREF_CD"] = $objForm->ge("PREF_CD");
        //都道府県コンボ（保護者）
        $objForm->ae( array("type"        => "select",
                            "name"        => "GPREF_CD",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["GPREF_CD"],
                            "options"     => $opt_pref ) );
        $arg["data"]["GPREF_CD"] = $objForm->ge("GPREF_CD");

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
                            "extrahtml"   => "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd2.php?cmd=search&prefname=PREF_CD&prefname2=FAMILY_REGISTER&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );

        //確定ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_apply",
                            "value"       => "確定",
                            "extrahtml"   => "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd2.php?cmd=apply&prefname=PREF_CD&prefname2=FAMILY_REGISTER&addrname=ADDRESS1&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"") );

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

        //所在地教育委員会コンボ
        $query = knjl016cQuery::getEdboard();
        makeCmb($objForm, $arg, $db, $query, "EDBOARDCD", $Row["EDBOARDCD"], "", 1);

        //出身学校
        $FsName     = array();
        $FsNatpName = array();
        $FsDistName = array();
        $result     = $db->query(knjl016cQuery::getFinschoolcd($model->year, $Row["FS_CD"]));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $FsName[$row["FINSCHOOLCD"]] = $row["FINSCHOOL_NAME"];
            $FsNatpName[$row["FINSCHOOLCD"]] = $row["FS_NATPUBPRIDIV_NAME"];
            $FsDistName[$row["FINSCHOOLCD"]] = $row["DISTRICTCD_NAME"];
        }
        $result->free();
        //コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "FINSCHOOLCD",
                            "size"        => 7,
                            "maxlength"   => 7,
                            "extrahtml"   => "onchange=\"change_flg()\" onblur=\"hiddenWin('" .REQUESTROOT ."/L/KNJL016C/search_fin_name.php?cmd=apply&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&schoolcd='+document.forms[0]['FINSCHOOLCD'].value+'&frame='+getFrameName(self))\"",
                            "value"       => $Row["FS_CD"] ) );
        $arg["data"]["FINSCHOOLCD"] = $objForm->ge("FINSCHOOLCD");
        //学校名
        $arg["data"]["FINSCHOOLNAME"] = $FsName[$Row["FS_CD"]];
        //学校立
        $arg["data"]["FS_NATPUBPRIDIV"] = $FsNatpName[$Row["FS_CD"]];
        //地区
        $arg["data"]["DISTRICTCD_NAME"] = $FsDistName[$Row["FS_CD"]];

        //塾
        $PsName     = array();
        $result     = $db->query(knjl016cQuery::getPrischoolcd($model->year, $Row["PRISCHOOLCD"]));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $PsName[$row["PRISCHOOLCD"]] = $row["PRISCHOOL_NAME"];
        }
        $result->free();
        //コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "PRISCHOOLCD",
                            "size"        => 7,
                            "maxlength"   => 7,
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["PRISCHOOLCD"] ) );
        $arg["data"]["PRISCHOOLCD"] = $objForm->ge("PRISCHOOLCD");
        //塾名
        $arg["data"]["PS_NAME"] = $PsName[$Row["PRISCHOOLCD"]];


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
                            "extrahtml"   => "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd2.php?cmd=search&prefname=GPREF_CD&prefname2=GPREF_CD&addrname=GADDRESS1&zipname=GZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_apply",
                            "value"       => "確定",
                            "extrahtml"   => "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd2.php?cmd=apply&prefname=GPREF_CD&prefname2=GPREF_CD&addrname=GADDRESS1&zipname=GZIPCD&zip='+document.forms[0]['GZIPCD'].value+'&frame='+getFrameName(self))\"") );

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

        //続柄コンボ
        $opt       = array();
        $opt[]     = array("label" => "", "value" => "");
        $result    = $db->query(knjl016cQuery::get_name_cd($model->year, 'H201'));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "RELATIONSHIP",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["RELATIONSHIP"],
                            "options"     => $opt ) );
        $arg["data"]["RELATIONSHIP"] = $objForm->ge("RELATIONSHIP");
        $result->free();

        //------------------------------内申科目---------------------------------
        $result = $db->query(knjl016cQuery::get_name_cd($model->year, "L008"));
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
        $arg["data"]["RPT11"] = $Row["CONFIDENTIAL_RPT11"];
        $arg["data"]["RPT12"] = $Row["CONFIDENTIAL_RPT12"];
        $arg["data"]["AVERAGE5"]     = $Row["AVERAGE5"];
        $arg["data"]["AVERAGE_ALL"]  = $Row["AVERAGE_ALL"];
        $arg["data"]["ABSENCE_DAYS"] = $Row["ABSENCE_DAYS"];

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
                            "size"        => 80,
                            "maxlength"   => 80,
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["REMARK2"] ));

        $arg["data"]["REMARK2"] = $objForm->ge("REMARK2");

        //受験番号（前期・後期出願者）
        $objForm->ae( array("type"        => "text",
                            "name"        => "RECOM_EXAMNO",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onchange=\"change_flg()\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["RECOM_EXAMNO"] ));
        $arg["data"]["RECOM_EXAMNO"] = $objForm->ge("RECOM_EXAMNO");

        if ($model->isGojou) {
            $arg["isGojou"] = "1";
            //学園バス利用
            $extra  = "id=\"BUS_USE\" onchange=\"change_flg()\" onClick=\"disBusUse(this);\"";
            $extra .= strlen($Row["BUS_USE"]) ? " checked" : "";
            $arg["data"]["BUS_USE"] = knjCreateCheckBox($objForm, "BUS_USE", "1", $extra, "");
            //以下は、学園バス利用する場合、有効。
            $disBusUse = strlen($Row["BUS_USE"]) ? "" : " disabled";
            //乗降地 1:林間田園都市駅 2:福神駅 3:JR五条駅
            $opt = array(1, 2, 3);
            $extra = array("id=\"STATIONDIV1\" onchange=\"change_flg()\"".$disBusUse, "id=\"STATIONDIV2\" onchange=\"change_flg()\"".$disBusUse, "id=\"STATIONDIV3\" onchange=\"change_flg()\"".$disBusUse);
            $Row["STATIONDIV"] = strlen($Row["STATIONDIV"]) ? $Row["STATIONDIV"] : "3";
            $radioArray = knjCreateRadio($objForm, "STATIONDIV", $Row["STATIONDIV"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;
            //ご利用人数
            $extra = "onchange=\"change_flg()\" onblur=\"this.value=toInteger(this.value)\"".$disBusUse;
            $arg["data"]["BUS_USER_COUNT"] = knjCreateTextBox($objForm, $Row["BUS_USER_COUNT"], "BUS_USER_COUNT", 4, 2, $extra);
            //ﾌﾟﾚﾃｽﾄ受験番号
            $extra  = "onchange=\"change_flg()\" onblur=\"this.value=toInteger(this.value)\"";
            $extra .= ($Row["APPLICANTDIV"] == "1" && ($Row["TESTDIV"] == "1" || $Row["TESTDIV"] == "2")) ? "" : " disabled";
            $arg["data"]["PRE_RECEPTNO"] = knjCreateTextBox($objForm, $Row["PRE_RECEPTNO"], "PRE_RECEPTNO", 5, 5, $extra);
            //入寮
            $extra  = "id=\"DORMITORY_FLG\" onchange=\"change_flg()\"";
            $extra .= strlen($Row["DORMITORY_FLG"]) ? " checked" : "";
            $arg["data"]["DORMITORY_FLG"] = knjCreateCheckBox($objForm, "DORMITORY_FLG", "1", $extra, "");
            //特進併願希望
            $extra  = "id=\"SLIDE_FLG\" onchange=\"change_flg()\"";
            $extra .= strlen($Row["SLIDE_FLG"]) ? " checked" : "";
            $arg["data"]["SLIDE_FLG"] = knjCreateCheckBox($objForm, "SLIDE_FLG", "1", $extra, "");
            //S組
            $extra  = "id=\"SPORTS_FLG\" onchange=\"change_flg()\"";
            $extra .= strlen($Row["SPORTS_FLG"]) ? " checked" : "";
            $arg["data"]["SPORTS_FLG"] = knjCreateCheckBox($objForm, "SPORTS_FLG", "1", $extra, "");
            //スポーツコースコンボ
            $query = knjl016cQuery::getGeneralFlg();
            makeCmb($objForm, $arg, $db, $query, "GENERAL_FLG", $Row["GENERAL_FLG"], "", 1);
            //個人成績希望
            $extra  = "id=\"PERSONAL_FLG\" onchange=\"change_flg()\"";
            $extra .= strlen($Row["PERSONAL_FLG"]) ? " checked" : "";
            $arg["data"]["PERSONAL_FLG"] = knjCreateCheckBox($objForm, "PERSONAL_FLG", "1", $extra, "");
            //併願校（高校）
            $shFsName    = array();
            $result     = $db->query(knjl016cQuery::getFinHighschoolcd($Row["SH_SCHOOLCD"]));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $shFsName[$row["FINSCHOOLCD"]] = $row["FINSCHOOL_NAME"];
            }
            $result->free();
            //コード
            $objForm->ae( array("type"        => "text",
                                "name"        => "SH_SCHOOLCD",
                                "size"        => 7,
                                "maxlength"   => 7,
                                "extrahtml"   => "onchange=\"change_flg()\" onblur=\"hiddenWin('" .REQUESTROOT ."/L/KNJL016C/search_fin_high_name.php?cmd=apply&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&schoolcd='+document.forms[0]['SH_SCHOOLCD'].value+'&frame='+getFrameName(self))\"",
                                "value"       => $Row["SH_SCHOOLCD"] ) );
            $arg["data"]["SH_SCHOOLCD"] = $objForm->ge("SH_SCHOOLCD");
            //学校名
            $arg["data"]["SH_FS_NAME"] = $shFsName[$Row["SH_SCHOOLCD"]];
            //かな検索ボタン（併願校）
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_fin_high_kana_reference",
                                "value"       => "かな検索",
                                "extrahtml"   => "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL016C/search_fin_high_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 335, 370)\"") );
            $arg["button"]["btn_fin_high_kana_reference"] = $objForm->ge("btn_fin_high_kana_reference");
        } else {
            $arg["isWakayama"] = "1";
        }

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
                            "extrahtml"   => "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL016C/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );

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
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        //かな検索ボタン（塾）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_pri_kana_reference",
                            "value"       => "かな検索",
                            "extrahtml"   => "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL016C/search_pri_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}()-180, 320, 370)\"") );
        $arg["button"]["btn_pri_kana_reference"] = $objForm->ge("btn_pri_kana_reference");

        //兄弟検索ボタン（塾／兄弟情報）
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_guard_kana_reference",
                            "value"       => "兄弟検索",
                            "extrahtml"   => "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL016C/search_guard_name.php?cmd=search&year='+document.forms[0]['year'].value+'&ctrl_year='+document.forms[0]['ctrl_year'].value+'&ctrl_semester='+document.forms[0]['ctrl_semester'].value+'&gtelno='+document.forms[0]['GTELNO'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}()-140, 340, 300)\"") );
        $arg["button"]["btn_guard_kana_reference"] = $objForm->ge("btn_guard_kana_reference");

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

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ctrl_year",
                            "value"     => CTRL_YEAR) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ctrl_semester",
                            "value"     => CTRL_SEMESTER) );
        
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl016cForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array("label" => "", "value" => "");

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>