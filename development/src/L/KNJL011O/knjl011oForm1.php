<?php

require_once('for_php7.php');

class knjl011oForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl011oindex.php", "", "main");

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            //データを取得
            $Row = knjl011oQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl011oQuery::get_edit_data($model);
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

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        $db = Query::dbCheckOut();

        //------------------------------志願者情報-------------------------------------
        //入試制度コンボ
        $result     = $db->query(knjl011oQuery::get_name_cd($model->year, 'L003'));
        $opt        = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "APPLICANTDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["APPLICANTDIV"],
                            "options"     => $opt ) );

        $arg["data"]["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");
        $result->free();

        //受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $model->examno ));

        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //入試区分チェックボックス
        $model->all_testdiv = array();
        $result     = $db->query(knjl011oQuery::getTestdivMst($model->year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $fieldname = "TESTDIV".$row["NAMECD2"];
            $checked = strlen($Row[$fieldname]) ? "checked" : "";
            $objForm->ae( array("type"        => "checkbox",
                                "name"        => $fieldname,
                                "extrahtml"   => "onchange=\"change_flg()\" id=\"$fieldname\" " .$checked,
                                "value"       => $row["NAMECD2"] ));
            $arg["data"][$fieldname] = $objForm->ge($fieldname);
            $arg["data"][$fieldname."_ID"] = $fieldname;
            $arg["data"][$fieldname."_NAME"] = $row["NAME1"];
            $model->all_testdiv[$row["NAMECD2"]] = $row["NAME1"];
        }
        $result->free();

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
        $result     = $db->query(knjl011oQuery::get_name_cd($model->year, 'Z002'));
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

        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl011oQuery::get_calendarno($model->year));
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
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(0, this.value, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onchange=\"change_flg()\"",
                            "value"       => $Row["ERACD"] ));

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
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(1, this.value, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["BIRTH_Y"] ));

        $arg["data"]["BIRTH_Y"] = $objForm->ge("BIRTH_Y");

        //生年月日月
        $objForm->ae( array("type"        => "text",
                            "name"        => "BIRTH_M",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(2, this.value, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["BIRTH_M"] ));

        $arg["data"]["BIRTH_M"] = $objForm->ge("BIRTH_M");

        //生年月日日
        $objForm->ae( array("type"        => "text",
                            "name"        => "BIRTH_D",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "STYLE=\"text-align: center;\" onblur=\" toDatecheck(3, this.value, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onchange=\"change_flg()\"",
                            "value"       => $Row["BIRTH_D"] ));

        $arg["data"]["BIRTH_D"] = $objForm->ge("BIRTH_D");

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
                            "extrahtml"   => "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );

        //確定ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_apply",
                            "value"       => "確定",
                            "extrahtml"   => "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS1&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"") );

        $arg["data"]["ZIPCD"] = $objForm->ge("ZIPCD") .$objForm->ge("btn_zip") .$objForm->ge("btn_apply");


        //住所(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "ADDRESS1",
                            "size"        => 60,
                            "maxlength"   => 150,
                            "extrahtml"   => "onblur=\"toCopytxt(1, this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["ADDRESS1"] ));

        $arg["data"]["ADDRESS1"] = $objForm->ge("ADDRESS1");

        //方書(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "ADDRESS2",
                            "size"        => 60,
                            "maxlength"   => 150,
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

        //出身学校検索ボタン
        $extra = "style=\"width:40px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain2&fscdname=FS_CD_ID&fsname=FS_NAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検索", $extra);

        //出身学校コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "FS_CD",
                            "size"        => 7,
                            "maxlength"   => 7,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" onchange=\"change_flg()\" id=\"FS_CD_ID\" ",
                            "value"       => $Row["FS_CD"] ));

        $arg["data"]["FS_CD"] = $objForm->ge("FS_CD");

        //出身学校名
        $objForm->ae( array("type"        => "text",
                            "name"        => "FS_NAME",
                            "size"        => 30,
                            "maxlength"   => 30,
                            "extrahtml"   => "onchange=\"change_flg()\" id=\"FS_NAME_ID\" ",
                            "value"       => $Row["FS_NAME"] ));

        $arg["data"]["FS_NAME"] = $objForm->ge("FS_NAME");

/*2005.12.28 minei*/
       //出身学校地区コンボ
        $result     = $db->query(knjl011oQuery::get_name_cd($model->year, 'Z003'));
        $opt        = array();
        $opt[] = array("label" => "", "value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }

        $objForm->ae( array("type"        => "select",
                            "name"        => "FS_AREA_CD",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["FS_AREA_CD"],
                            "options"     => $opt ) );

        $arg["data"]["FS_AREA_CD"] = $objForm->ge("FS_AREA_CD");
        $result->free();

/*2005.12.28 minei*/

        //卒業年
        $objForm->ae( array("type"        => "text",
                            "name"        => "FS_GRDYEAR",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" onchange=\"change_flg()\"",
                            "value"       => $Row["FS_GRDYEAR"] ));

        $arg["data"]["FS_GRDYEAR"] = $objForm->ge("FS_GRDYEAR");


        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GNAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "extrahtml"   => "onchange=\"change_flg()\" onkeyup=\" keySet('GNAME', 'GKANA', 'H')\"",
                            "value"       => $Row["GNAME"] ));

        $arg["data"]["GNAME"] = $objForm->ge("GNAME");


        //氏名かな(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GKANA",
                            "size"        => 80,
                            "maxlength"   => 120,
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["GKANA"] ));

        $arg["data"]["GKANA"] = $objForm->ge("GKANA");

        //郵便番号入力支援(保護者)
        $arg["data"]["GZIPCD"] = View::popUpZipCode($objForm, "GZIPCD", $Row["GZIPCD"],"GADDRESS1");

        //住所(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GADDRESS1",
                            "size"        => 60,
                            "maxlength"   => 150,
                            "extrahtml"   => "onchange=\"change_flg()\"",
                            "value"       => $Row["GADDRESS1"] ));

        $arg["data"]["GADDRESS1"] = $objForm->ge("GADDRESS1");

        //方書(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GADDRESS2",
                            "size"        => 60,
                            "maxlength"   => 150,
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

        //2005.12.28 minei
        //------------------------------内申科目---------------------------------
        $result = $db->query(knjl011oQuery::get_name_cd($model->year, "L008"));
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
        //2005.12.28 minei

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

        //重複受験番号
        for ($number = 1; $number <= 3; $number++) {
            $name = "RECOM_EXAMNO" .$number;
            $objForm->ae( array("type"        => "text",
                                "name"        => $name,
                                "size"        => 5,
                                "maxlength"   => 5,
                                "extrahtml"   => "onchange=\"change_flg();\" onblur=\"this.value=toInteger(this.value)\"",
                                "value"       => $Row[$name] ));
            $arg["data"][$name] = $objForm->ge($name);
        }

        //塾
        $PsName     = array();
        $result     = $db->query(knjl011oQuery::getPrischoolcd($model->year, $Row["PS_CD"]));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $PsName[$row["PRISCHOOLCD"]] = $row["PRISCHOOL_NAME"];
        }
        $result->free();
        //コード
        $extra = "onchange=\"change_flg()\" onblur=\"hiddenWin('" .REQUESTROOT ."/L/KNJL011O/search_pri_name.php?cmd=apply&flg=1&year='+document.forms[0]['year'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self)+'&schoolcd='+document.forms[0]['PS_CD'].value)\"";
        $arg["data"]["PS_CD"] = knjCreateTextBox($objForm, $Row["PS_CD"], "PS_CD", 7, 7, $extra);
        //塾名
        $arg["data"]["PS_NAME"] = $PsName[$Row["PS_CD"]];
        //教室名
        $extra = "onchange=\"change_flg()\"";
        $arg["data"]["CLASS_ROOM"] = knjCreateTextBox($objForm, $Row["CLASS_ROOM"], "CLASS_ROOM", 20, 20, $extra);

        //塾
        $PsName2    = array();
        $result     = $db->query(knjl011oQuery::getPrischoolcd($model->year, $Row["PS_CD2"]));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $PsName2[$row["PRISCHOOLCD"]] = $row["PRISCHOOL_NAME"];
        }
        $result->free();
        //コード
        $extra = "onchange=\"change_flg()\" onblur=\"hiddenWin('" .REQUESTROOT ."/L/KNJL011O/search_pri_name.php?cmd=apply&flg=2&year='+document.forms[0]['year'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self)+'&schoolcd='+document.forms[0]['PS_CD2'].value)\"";
        $arg["data"]["PS_CD2"] = knjCreateTextBox($objForm, $Row["PS_CD2"], "PS_CD2", 7, 7, $extra);
        //塾名
        $arg["data"]["PS_NAME2"] = $PsName2[$Row["PS_CD2"]];
        //教室名
        $extra = "onchange=\"change_flg()\"";
        $arg["data"]["CLASS_ROOM2"] = knjCreateTextBox($objForm, $Row["CLASS_ROOM2"], "CLASS_ROOM2", 20, 20, $extra);

        Query::dbCheckIn($db);

        //-------------------------------- ボタン作成 ------------------------------------
        $zip = $Row["ZIPCD"];
        $gzip = $Row["GZIPCD"];
        $zadd = $Row["ADDRESS1"];
        $gadd = $Row["GADDRESS1"];

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
                            "extrahtml"   => "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011O/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );

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
                            "extrahtml" => "style=\"width:90px\" onclick=\"return btn_submit('disp_clear');\"" ) );

        $arg["button"]["btn_clear"] = $objForm->ge("btn_clear");

        //志願者よりコピーボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_copy",
                            "value"     => "志願者よりコピー",
                            "extrahtml" => "$disabled style=\"width:135px\" onclick=\"return btn_submit('copy');\"" ) );

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

        //兄弟受験画面ボタン
        $extra = $disabled ." onclick=\"loadwindow('knjl011oindex.php?cmd=brother&BROTHER_EXAMNO={$model->examno}',body.clientWidth/2-200,body.clientHeight/2-100,500,300);\"";
        $arg["button"]["btn_brother"] = knjCreateBtn($objForm, "btn_brother", "兄弟受験画面", $extra);
        
        //塾検索ボタン
        $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011O/search_pri_name.php?cmd=search&flg=1&year='+document.forms[0]['year'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}()-180, 320, 370)\"";
        $arg["button"]["btn_pri_kana_reference"] = knjCreateBtn($objForm, "btn_pri_kana_reference", "塾検索", $extra);

        //塾検索ボタン
        $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011O/search_pri_name.php?cmd=search&flg=2&year='+document.forms[0]['year'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}()-180, 320, 370)\"";
        $arg["button"]["btn_pri_kana_reference2"] = knjCreateBtn($objForm, "btn_pri_kana_reference2", "塾検索", $extra);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year) );

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl011oForm1.html", $arg);
    }
}
?>