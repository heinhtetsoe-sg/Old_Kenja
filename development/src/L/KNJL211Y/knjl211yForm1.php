<?php

require_once('for_php7.php');

class knjl211yForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl211yindex.php", "", "main");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //デフォルト設定
        if ($model->examno == "" || $model->cmd == "showdiv") {
            $db = Query::dbCheckOut();
            $result = $db->query(knjl211yQuery::get_name_cd($model->year, 'L003'));
            $defApplicantDiv = $model->field["APPLICANTDIV"];
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $defApplicantDiv = ($model->field["APPLICANTDIV"] == "" && $row["NAMESPARE2"] == '1') ? $row["NAMECD2"] : $defApplicantDiv;
            }
            $model->examno = $db->getOne(knjl211yQuery::getMinExam($model->year, $defApplicantDiv));
            Query::dbCheckIn($db);
        }

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            //データを取得
            $Row = knjl211yQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl211yQuery::get_edit_data($model);
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
            }
        }
        if ($model->cmd == 'testdiv') {
            $Row =& $model->field;
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        $db = Query::dbCheckOut();

        //------------------------------志願者情報-------------------------------------
        //入試制度コンボ
        $result     = $db->query(knjl211yQuery::get_name_cd($model->year, 'L003'));
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


        //入試区分コンボ(出願１回目)
        $opt        = array();
        $result     = $db->query(knjl211yQuery::get_name_cd($model->year, "L004"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
            if ($Row["TESTDIV"]=="" && $row["NAMESPARE2"]=='1') $Row["TESTDIV"] = $row["NAMECD2"];
        }
        $result->free();
        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTDIV",
                            "size"        => "1",
//                            "extrahtml"   => "onChange=\"change_flg(); return btn_submit('testdiv');\"",
                            "extrahtml"   => "onChange=\"change_flg(); \"",
                            "value"       => $Row["TESTDIV"],
                            "options"     => $opt ) );
        $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

        //合否(出願１回目)
        $arg["data"]["JUDGEDIV_NAME"] = $Row["JUDGEDIV_NAME"];
        knjCreateHidden($objForm, "JUDGEDIV_NAME", $Row["JUDGEDIV_NAME"]);
        knjCreateHidden($objForm, "JUDGEDIV", $Row["JUDGEDIV"]);

        //再受験フラグ
        $name   = "GENERAL_FLG";
        $extra  = (0 < strlen($Row[$name])) ? "checked" : "";
        $extra .= " id=\"".$name."\"";
        $extra .= ($Row["TESTDIV"] < 3 && $Row["JUDGEDIV"] == "2") ? "" : " disabled" ;
        $extra .= " onclick=\"disabledCommon('general_flg', this.value);\"";
        $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "1", $extra);

        //入試区分(出願２回目)(再受験)
        //前回入試が不合格の時、次回の入試を同一受験番号で登録する
        $opt        = array();
        $pre_testdiv = (int) $Row["TESTDIV"] + 1;
        $result     = $db->query(knjl211yQuery::get_name_cd($model->year, "L004", $pre_testdiv));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $disCmb = ($Row["TESTDIV"] < 3 && $Row["JUDGEDIV"] == "2" && strlen($Row["GENERAL_FLG"])) ? "" : " disabled";
        $objForm->ae( array("type"        => "select",
                            "name"        => "GENERAL_TESTDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg(); \"" . $disCmb,
                            "value"       => $Row["GENERAL_TESTDIV"],
                            "options"     => $opt ) );
        $arg["data"]["GENERAL_TESTDIV"] = $objForm->ge("GENERAL_TESTDIV");


        //受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "STYLE=\"ime-mode: inactive;\" onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $model->examno ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //氏名(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onchange=\"change_flg()\"",
                            "value"       => $Row["NAME"] ));
        $arg["data"]["NAME"] = $objForm->ge("NAME");

        //氏名かな(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME_KANA",
                            "size"        => 40,
                            "maxlength"   => 120,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onchange=\"change_flg()\"",
                            "value"       => $Row["NAME_KANA"] ));
        $arg["data"]["NAME_KANA"] = $objForm->ge("NAME_KANA");

        //性別コンボ
        $result     = $db->query(knjl211yQuery::get_name_cd($model->year, 'Z002'));
        $opt        = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $defSex = $model->cmd == 'showdivAdd' ? "2" : "";
        $objForm->ae( array("type"        => "select",
                            "name"        => "SEX",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"change_flg(); \"",
                            "value"       => strlen($Row["SEX"]) ? $Row["SEX"] : $defSex,
                            "options"     => $opt ) );
        $arg["data"]["SEX"] = $objForm->ge("SEX");

        //生年月日（西暦）
        $extra = " STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\"";
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar2($objForm, "BIRTHDAY", str_replace("-","/",$Row["BIRTHDAY"]), "", "", $extra);

        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl211yQuery::get_calendarno($model->year));
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


        global $sess;
        //郵便番号入力支援(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => ZIPCD,
                            "size"        => 10,
                            "extrahtml"   => "STYLE=\"ime-mode: inactive;\" onblur=\"isZipcd(this), toCopytxt(0, this.value)\" onchange=\"change_flg()\"",
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
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onblur=\"toCopytxt(1, this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["ADDRESS1"] ));
        $arg["data"]["ADDRESS1"] = $objForm->ge("ADDRESS1");
        //方書(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "ADDRESS2",
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onblur=\"toCopytxt(2, this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["ADDRESS2"] ));
        $arg["data"]["ADDRESS2"] = $objForm->ge("ADDRESS2");
        //電話番号(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "TELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toTelNo(this.value), toCopytxt(3, this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["TELNO"] ));
        $arg["data"]["TELNO"] = $objForm->ge("TELNO");

        //出身学校検索ボタン
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain2&fscdname=FS_CD_ID&fsname=FS_NAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        //出身学校確定ボタン
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/L/KNJL211Y/search_fin_name.php?cmd=apply&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&schoolcd='+document.forms[0]['FS_CD'].value+'&frame='+getFrameName(self))\"";
        $arg["button"]["btn_fin_kakutei"] = knjCreateBtn($objForm, "btn_fin_kakutei", "確定", $extra);

        //出身学校コード
        $extra = "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value)\" onchange=\"change_flg()\" id=\"FS_CD_ID\" ";
        $arg["data"]["FS_CD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FS_CD", 7, 7, $extra);

        //出身学校名
        $extra = "STYLE=\"ime-mode: active;\" onchange=\"change_flg()\" id=\"FS_NAME_ID\" ";
        $arg["data"]["FS_NAME"] = knjCreateTextBox($objForm, $Row["FS_NAME"], "FS_NAME", 51, 51, $extra);


        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GNAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onchange=\"change_flg()\"",
                            "value"       => $Row["GNAME"] ));
        $arg["data"]["GNAME"] = $objForm->ge("GNAME");
        //氏名かな(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GKANA",
                            "size"        => 40,
                            "maxlength"   => 120,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onchange=\"change_flg()\"",
                            "value"       => $Row["GKANA"] ));
        $arg["data"]["GKANA"] = $objForm->ge("GKANA");

        //郵便番号入力支援(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GZIPCD",
                            "size"        => 10,
                            "extrahtml"   => "STYLE=\"ime-mode: inactive;\" onblur=\"isZipcd(this)\" onchange=\"change_flg()\"",
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
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onchange=\"change_flg()\"",
                            "value"       => $Row["GADDRESS1"] ));
        $arg["data"]["GADDRESS1"] = $objForm->ge("GADDRESS1");
        //方書(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GADDRESS2",
                            "size"        => 50,
                            "maxlength"   => 50,
                            "extrahtml"   => "STYLE=\"ime-mode: active;\" onchange=\"change_flg()\"",
                            "value"       => $Row["GADDRESS2"] ));
        $arg["data"]["GADDRESS2"] = $objForm->ge("GADDRESS2");
        //電話番号(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GTELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toTelNo(this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["GTELNO"] ));
        $arg["data"]["GTELNO"] = $objForm->ge("GTELNO");
        //続柄コンボ
        $opt       = array();
        $opt[]     = array("label" => "", "value" => "");
        $result    = $db->query(knjl211yQuery::get_name_cd($model->year, 'H201'));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();
        $objForm->ae( array("type"        => "select",
                            "name"        => "RELATIONSHIP",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["RELATIONSHIP"],
                            "options"     => $opt ) );
        $arg["data"]["RELATIONSHIP"] = $objForm->ge("RELATIONSHIP");



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
                            "extrahtml"   => "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL211Y/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
        $arg["button"]["btn_kana_reference"] = $objForm->ge("btn_kana_reference");
        //前の志願者検索ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => " << ",
                            "extrahtml"   => "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"" ) );
        //次の志願者検索ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_next",
                            "value"       => " >> ",
                            "extrahtml"   => "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"" ) );
        $arg["button"]["btn_back_next"] = $objForm->ge("btn_back").$objForm->ge("btn_next");
        //画面クリアボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_clear",
                            "value"     => "画面クリア",
                            "extrahtml" => "style=\"width:80px\" onclick=\"return btn_submit('disp_clear');\"" ) );
        $arg["button"]["btn_clear"] = $objForm->ge("btn_clear");


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
        //欠席ボタン
        $extra = $disabled ." onclick=\"loadwindow('knjl211yindex.php?cmd=attend&ATTEND_APPLICANTDIV={$Row["APPLICANTDIV"]}&ATTEND_EXAMNO={$model->examno}',body.clientWidth/2-200,body.clientHeight/2-100,500,250);\"";
        $arg["button"]["btn_attend"] = knjCreateBtn($objForm, "btn_attend", "欠 席", $extra);

        if ($model->Properties["knjl211yShowBoshuKikakuBtn"] == '1') {
            //募集企画情報
            $extra  = " onClick=\" wopen('".REQUESTROOT."/L/KNJL410/knjl410index.php?";
            $extra .= "SEND_PRGRID=KNJL211Y";
            $extra .= "&SEND_AUTH=".AUTHORITY;
            $extra .= "&SEND_NAME=".$Row["NAME"];
            $extra .= "&SEND_KANA=".$Row["NAME_KANA"];
            $extra .= "&SEND_FINSCHOOLCD=".$Row["FS_CD"];
            $extra .= "&SUBWIN=SUBWIN3','SUBWIN3',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_boshukikaku"] = knjCreateBtn($objForm, "btn_event", "募集企画情報", $extra);
        }

        //------------------------------家族-------------------------------------
        //家族構成
        $link = REQUESTROOT."/L/KNJL216Y/knjl216yindex.php?cmd=&SEND_PRGID=KNJL211Y&SEND_AUTH=".AUTHORITY."&SEND_APPLICANTDIV=".$Row["APPLICANTDIV"]."&SEND_EXAMNO=".$model->examno;
        $extra = "$disabled onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_family"] = knjCreateBtn($objForm, "btn_family", "家族構成", $extra);
        //備考がある続柄・備考を表示する
        //表示例。続柄,備考/続柄,備考・・・
        $family_remark = "";
        $sula = "";
        $result = $db->query(knjl211yQuery::getFamily($model->year, $model->examno));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!strlen($row["FAMILY_REMARK"])) {
                $family_remark .= $sula . $row["FAMILY_RELATIONSHIP"];
            } else {
                $family_remark .= $sula . $row["FAMILY_RELATIONSHIP"] . "," . $row["FAMILY_REMARK"];
            }
            $sula = "/";
        }
        $result->free();
        $arg["data"]["FAMILY_REMARK"] = $family_remark;

        //------------------------------特徴-------------------------------------
        //size   = 文字数 * 2 + 1
        //height = 行数 * 13.5 + (行数 - 1) * 3 + 5
        //1.志願理由
        //2.ご家庭で大切にしていること
        $moji   = 40;
        $gyo    = 6;
        $size   = $moji * 2 + 1;//81
        $height = $gyo * 13.5 + ($gyo - 1) * 3 + 5;//101
        $arg["data"]["CHARACTER_REMARK1"] = KnjCreateTextArea($objForm, "CHARACTER_REMARK1", $gyo, $size, "soft", "style=\"ime-mode: active; height:{$height}px;\" onchange=\"change_flg()\"", $Row["CHARACTER_REMARK1"]);
        $arg["data"]["CHARACTER_REMARK2"] = KnjCreateTextArea($objForm, "CHARACTER_REMARK2", $gyo, $size, "soft", "style=\"ime-mode: active; height:{$height}px;\" onchange=\"change_flg()\"", $Row["CHARACTER_REMARK2"]);
        //3.本人の長所
        //4.本人の短所
        $moji   = 40;
        $gyo    = 3;
        $size   = $moji * 2 + 1;//81
        $height = $gyo * 13.5 + ($gyo - 1) * 3 + 5;//51.5
        $arg["data"]["CHARACTER_REMARK3"] = KnjCreateTextArea($objForm, "CHARACTER_REMARK3", $gyo, $size, "soft", "style=\"ime-mode: active; height:{$height}px;\" onchange=\"change_flg()\"", $Row["CHARACTER_REMARK3"]);
        $arg["data"]["CHARACTER_REMARK4"] = KnjCreateTextArea($objForm, "CHARACTER_REMARK4", $gyo, $size, "soft", "style=\"ime-mode: active; height:{$height}px;\" onchange=\"change_flg()\"", $Row["CHARACTER_REMARK4"]);

        Query::dbCheckIn($db);

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

        //郵便番号支援の都道府県ダミー用
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DUMMY_CD") );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl211yForm1.html", $arg);
    }
}
?>