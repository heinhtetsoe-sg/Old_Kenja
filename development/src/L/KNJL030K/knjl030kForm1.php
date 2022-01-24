<?php

require_once('for_php7.php');

class knjl030kForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl030kindex.php", "", "main");

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //一覧表示
        if((!isset($model->warning))){
            //データを取得
            $disabled = "";
            $Row = knjl030kQuery::get_edit_data($model);
            if($model->cmd == 'reference') {
                if(!is_array($Row)) {
                    $disabled = "disabled";
                    $model->setWarning("MSG303");
                }
            }
            if($model->cmd == 'main'){
                $disabled = ($model->examno == "")? "disabled" : "";
            }
            if($model->cmd == 'disp_clear'){
                $disabled = "disabled";
            }
            if($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if(is_array($Row)) {
                    $disabled = "";
                    $model->examno       = $Row["EXAMNO"];
                    $model->testdiv      = $Row["TESTDIV"];
                } else {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = 'main';
                    $Row = knjl030kQuery::get_edit_data($model);
                }
            }
        }else{
            $Row =& $model->field;
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        $db = Query::dbCheckOut();

        //------------------------------志願者情報-------------------------------------
        //試験区分コンボ
        $result = $db->query(knjl030kQuery::getNamecd($model->year,"L003"));
        $opt    = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\" return btn_submit('reload1');\"",
                            "value"       => $model->testdiv,
                            "options"     => $opt ) );
        $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");
        if (!isset($model->testdiv)) {
            $model->testdiv = $opt[0]["value"];
        }
        $result->free();

        //受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $model->examno ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //フォームNo.
        $objForm->ae( array("type"        => "text",
                            "name"        => "FORMNO",
                            "size"        => 6,
                            "maxlength"   => 6,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg()\"",
                            "value"       => $Row["FORMNO"] ));
        $arg["data"]["FORMNO"] = $objForm->ge("FORMNO");

        //専併区分
        $result = $db->query(knjl030kQuery::getNamecd($model->year,"L006"));
        $opt    = array();
        /*** ADD 2005/11/15 by ameku ***/
        $opt[] = array("label" => "",
                       "value" => "");
        /*** ADD 2005/11/15 by ameku ***/
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "SHDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["SHDIV"],
                            "options"     => $opt ) );
        $arg["data"]["SHDIV"] = $objForm->ge("SHDIV");
        $result->free();

        //出願区分
        $result = $db->query(knjl030kQuery::getNamecd($model->year,"L005"));
        $opt    = array();
        /*** ADD 2005/11/15 by ameku ***/
        $opt[] = array("label" => "",
                       "value" => "");
        /*** ADD 2005/11/15 by ameku ***/
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

        //クラブコード 2006.01.17 alp m-yama
        $result = $db->query(knjl030kQuery::getClubcd($model->year,"L005"));
        $opt    = array();

        $opt[] = array("label" => "",
                       "value" => "");

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["CLUBCD"].":".$row["CLUBNAME"],
                           "value" => $row["CLUBCD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "CLUBCD",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["CLUBCD"],
                            "options"     => $opt ) );
        $arg["data"]["CLUBCD"] = $objForm->ge("CLUBCD");
        $result->free();

        //志望区分
        if ($model->cmd == "reload2") {
            $Row["DESIREDIV"] = VARS::post("DESIREDIV");
        }
        $result = $db->query(knjl030kQuery::getExamcourse($model));
        $examcourse = array();
        $desirediv = array();
        /*** ADD 2005/11/15 by ameku ***/
        $desirediv[] = array("label" => "",
                       "value" => "");
        /*** ADD 2005/11/15 by ameku ***/
        $i=0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if($i==0)   $desire = $row["DESIREDIV"];

            //desirediv が同じなら配列に入れていく
            if($desire == $row["DESIREDIV"]){
                $examcourse[] = $row["EXAMCOURSE_NAME"];
                $desire = $row["DESIREDIV"];
                $i++;
            //desirediv が違ったらコンボ表示の配列に入れていく
            }else{
#            var_dump($examcourse);
                $desirediv[] = array("label" => $desire."：".implode("/",$examcourse),
                                     "value" => $desire );
                $desire = $row["DESIREDIV"];
                $examcourse = array();
                $examcourse[] = $row["EXAMCOURSE_NAME"];
            }
        }
        $desirediv[] = array("label" => $desire."：".implode("/",$examcourse),
                             "value" => $desire );
        $objForm->ae( array("type"        => "select",
                            "name"        => "DESIREDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\" return btn_submit('reload2');\"",
                            "value"       => $Row["DESIREDIV"],
                            "options"     => $desirediv ) );
        $arg["data"]["DESIREDIV"] = $objForm->ge("DESIREDIV");
        if (!isset($model->desirediv)) {
            $model->desirediv = $desirediv[0]["value"];
        }
        $result->free();

        //志望学科
        $model->desirediv = $Row["DESIREDIV"];
        $coursemajor = $db->getRow(knjl030kQuery::getCourseMajor($model),DB_FETCHMODE_ASSOC);
        if($coursemajor["COURSECD"]){
            $arg["data"]["COURSEMAJOR"] = $coursemajor["COURSECD"].$coursemajor["MAJORCD"]."：" .$coursemajor["COURSENAME"].$coursemajor["MAJORNAME"] ;
        }

        //氏名(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["NAME"] ));
        $arg["data"]["NAME"] = $objForm->ge("NAME");

        //特別理由区分
        $result = $db->query(knjl030kQuery::getNamecd($model->year, "L017", 9));
        $opt    = array();
        $opt[] = array("label" => "",
                       "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
            if ($row["NAMESPARE1"] == "1") {
                if ($Row["SPECIAL_REASON_DIV"] == "") {
                    $Row["SPECIAL_REASON_DIV"] = $row["NAMECD2"];
                }
            }
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "SPECIAL_REASON_DIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["SPECIAL_REASON_DIV"],
                            "options"     => $opt ) );
        $arg["data"]["SPECIAL_REASON_DIV"] = $objForm->ge("SPECIAL_REASON_DIV");
        $result->free();

        //氏名かな(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "NAME_KANA",
                            "size"        => 80,
                            "maxlength"   => 80,
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["NAME_KANA"] ));
        $arg["data"]["NAME_KANA"] = $objForm->ge("NAME_KANA");

        //性別コンボ
        $result = $db->query(knjl030kQuery::getNamecd($model->year,"Z002"));
        $opt    = array();
        /*** ADD 2005/11/15 by ameku ***/
        $opt[] = array("label" => "",
                       "value" => "");
        /*** ADD 2005/11/15 by ameku ***/
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "SEX",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["SEX"],
                            "options"     => $opt ) );
        $arg["data"]["SEX"] = $objForm->ge("SEX");
        $result->free();

        global $sess;
        $birthday = str_replace("-","/",$Row["BIRTHDAY"]);
        //生年月日
        $objForm->ae( array("type"        => "text",
                            "name"        => "BIRTHDAY",
                            "size"        => 12,
                            "maxlength"   => 12,
                            "extrahtml"   => "onkeydown=\"if(event.keyCode == 13) return false;\" onblur=\"isDate(this), setWarekiName(this)\" onChange=\"change_flg()\"",
                            "value"       => $birthday ));

        //読込ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_calen",
                            "value"       => "･･･",
                            "extrahtml"   => "onclick=\"loadwindow('" .REQUESTROOT ."/common/calendar.php?name=BIRTHDAY&frame='+getFrameName(self) + '&date=' + document.forms[0]['BIRTHDAY'].value + '&CAL_SESSID=$sess->id&$param', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"") );

        $arg["data"]["BIRTHDAY"] = $objForm->ge("BIRTHDAY") .$objForm->ge("btn_calen");

        //現住所コード
        $result = $db->query(knjl030kQuery::getNamecd($model->year,"L007"));
        $opt    = array();
        /*** ADD 2005/11/15 by ameku ***/
        $opt[] = array("label" => "",
                       "value" => "");
        /*** ADD 2005/11/15 by ameku ***/
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "ADDRESSCD",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["ADDRESSCD"],
                            "options"     => $opt ) );
        $arg["data"]["ADDRESSCD"] = $objForm->ge("ADDRESSCD");
        $result->free();

        //郵便番号入力支援(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => ZIPCD,
                            "size"        => 10,
                            "extrahtml"   => "onblur=\"isZipcd(this), toCopytxt(0, this.value)\" onChange=\"change_flg()\"",
                            "value"       => $Row["ZIPCD"]));

        //読込ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_zip",
                            "value"       => "郵便番号入力支援",
                            "extrahtml"   => "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );

        //確定ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_apply",
                            "value"       => "確定",
                            "extrahtml"   => "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"") );

        $arg["data"]["ZIPCD"] = $objForm->ge("ZIPCD") .$objForm->ge("btn_zip") .$objForm->ge("btn_apply");


        //電話番号(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "TELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "onblur=\"this.value=toTelNo(this.value), toCopytxt(3, this.value)\" onChange=\"change_flg()\"",
                            "value"       => $Row["TELNO"] ));
        $arg["data"]["TELNO"] = $objForm->ge("TELNO");

        //住所(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "ADDRESS",
                            "size"        => 102,
                            "maxlength"   => 102,
                            "extrahtml"   => "onblur=\"toCopytxt(1, this.value)\" onChange=\"change_flg()\"",
                            "value"       => $Row["ADDRESS"] ));
        $arg["data"]["ADDRESS"] = $objForm->ge("ADDRESS");

        //所在地コード
        $result = $db->query(knjl030kQuery::getNamecd($model->year,"L007"));
        $opt    = array();
        /*** ADD 2005/11/15 by ameku ***/
        $opt[] = array("label" => "",
                       "value" => "");
        /*** ADD 2005/11/15 by ameku ***/
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "LOCATIONCD",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["LOCATIONCD"],
                            "options"     => $opt ) );
        $arg["data"]["LOCATIONCD"] = $objForm->ge("LOCATIONCD");
        $result->free();

        //国公私立区分
        $result = $db->query(knjl030kQuery::getNamecd($model->year,"L004"));
        $opt    = array();
        /*** ADD 2005/11/15 by ameku ***/
        $opt[] = array("label" => "",
                       "value" => "");
        /*** ADD 2005/11/15 by ameku ***/
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "NATPUBPRIDIV",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["NATPUBPRIDIV"],
                            "options"     => $opt ) );
        $arg["data"]["NATPUBPRIDIV"] = $objForm->ge("NATPUBPRIDIV");
        $result->free();

        //卒業年
        $objForm->ae( array("type"        => "text",
                            "name"        => "FS_GRDYEAR",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg()\"",
                            "value"       => $Row["FS_GRDYEAR"] ));
        $arg["data"]["FS_GRDYEAR"] = $objForm->ge("FS_GRDYEAR");

        //学校コード
#        $result = $db->query(knjl030kQuery::getFscd($model->year));    2006/01/30
        $result = $db->query(knjl030kQuery::getFscd($model));
        $opt    = array();
        /*** ADD 2005/11/15 by ameku ***/
        $opt[] = array("label" => "",
                       "value" => "");
        /*** ADD 2005/11/15 by ameku ***/
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["FINSCHOOLCD"].":".$row["FINSCHOOL_NAME"],
                           "value" => $row["FINSCHOOLCD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "FS_CD",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["FS_CD"],
                            "options"     => $opt ));
        $arg["data"]["FS_CD"] = $objForm->ge("FS_CD");
        $result->free();

        //出身塾名称
        $result = $db->query(knjl030kQuery::selectQueryPrischool($model));
        $opt = array();
        $opt[] = array("label"  =>  '　　　　　　　　', "value"  => "");

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"  =>  $row["PS_CD"] .":" .htmlspecialchars($row["PRISCHOOL_NAME"]), "value"  => $row["PS_CD"]);
        }
        /*** UPDATE 2005/11/15 by ameku ***/        
        
        $objForm->ae( array("type"       => "select",
                            "name"       => "PS_CD",
                            "size"       => "1",
                            "extrahtml"   =>"onChange=\"change_flg();\"",//NO001
//                            "extrahtml"   =>"onChange=\"change_flg();check_approval(this)\"",
                            "value"      => $Row["PS_CD"],
                            "options"    => $opt));
        /*** UPDATE 2005/11/15 by ameku ***/        
        $arg["data"]["PS_CD"] = $objForm->ge("PS_CD");
        $result->free();

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GNAME",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["GNAME"] ));
        $arg["data"]["GNAME"] = $objForm->ge("GNAME");

        //氏名かな(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GKANA",
                            "size"        => 80,
                            "maxlength"   => 80,
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["GKANA"] ));
        $arg["data"]["GKANA"] = $objForm->ge("GKANA");

        //郵便番号入力支援(保護者)
        $arg["data"]["GZIPCD"] = View::popUpZipCode($objForm, "GZIPCD", $Row["GZIPCD"],"GADDRESS");

        //住所(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GADDRESS",
                            "size"        => 102,
                            "maxlength"   => 102,
                            "extrahtml"   => "onChange=\"change_flg()\"",
                            "value"       => $Row["GADDRESS"] ));
        $arg["data"]["GADDRESS"] = $objForm->ge("GADDRESS");

        //電話番号(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GTELNO",
                            "size"        => 14,
                            "maxlength"   => 14,
                            "extrahtml"   => "onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg()\"",
                            "value"       => $Row["GTELNO"] ));
        $arg["data"]["GTELNO"] = $objForm->ge("GTELNO");


        Query::dbCheckIn($db);

        //-------------------------------- ボタン作成 ------------------------------------
        /*** ADD 2005/11/15 by ameku ***/
        $disabled2 = "";//NO001
//        $disabled2 = ($Row["PS_CD"] == "")? "disabled":"";
//2005.12.31 alp m-yama↓
		$opt_approval = array();
		$opt_approval[0] = array("label" => "意思表示なし",
								 "value" => "");
		$opt_approval[1] = array("label" => "可",
								 "value" => "1");
		$opt_approval[2] = array("label" => "否",
								 "value" => "2");

        $objForm->ae(array("type"      => "select",
                           "name"      => "CHK_APPROVAL",
                           "value"     => $Row["APPROVAL_FLG"],
                           "extrahtml" => $disabled2,
						   "options"   => $opt_approval));

        $arg["CHK_APPROVAL"] = $objForm->ge("CHK_APPROVAL");
/*
        $objForm->ae(array("type"      => "checkbox",
                           "name"      => "CHK_APPROVAL",
                           "value"     => "1",
                           "checked"   => (($Row["APPROVAL_FLG"] == "1")? true:false),
                           "extrahtml" => $disabled2 ));

        $arg["CHK_APPROVAL"] = $objForm->ge("CHK_APPROVAL");
*/
        /*** ADD 2005/11/15 by ameku ***/
//2005.12.31 alp m-yama↑

        $zip = $Row["ZIPCD"];
        $gzip = $Row["GZIPCD"];
        $zadd = $Row["ADDRESS"];
        $gadd = $Row["GADDRESS"];

        //検索
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reference",
                            "value"     => "検 索",
                            "extrahtml" => "style=\"width:45px\" onclick=\"return btn_submit('reference', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"" ) );
        $arg["button"]["btn_reference"] = $objForm->ge("btn_reference");

        //かな検索ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_kana_reference",
                            "value"       => "かな検索",
                            "extrahtml"   => "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL030K/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&testdiv='+document.forms[0]['TESTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
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

        //画面クリア
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_clear",
                            "value"     => "画面クリア",
                            "extrahtml" => "style=\"width:80px\" onclick=\"return btn_submit('disp_clear');\"" ) );
        $arg["button"]["btn_clear"] = $objForm->ge("btn_clear");

        //志願者よりコピーボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_copy",
                            "value"     => "志願者よりコピー",
                            "extrahtml" => "style=\"width:135px\" $disabled onclick=\"return btn_submit('copy');\"" ) );

        $arg["button"]["btn_copy"] = $objForm->ge("btn_copy");

        //追加
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_add",
                            "value"     => "追 加",
                            "extrahtml" => "onclick=\"return btn_submit('add');\"" ) );
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //更新
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_udpate",
                            "value"     => "更 新",
                            "extrahtml" => "$disabled onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //更新(更新後前の志願者)
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の志願者",
                            "extrahtml" => "style=\"width:150px\" $disabled onclick=\"return btn_submit('back');\"" ) );

        //更新(更新後次の志願者)
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の志願者",
                            "extrahtml" => "style=\"width:150px\" $disabled onclick=\"return btn_submit('next');\"" ) );

        $arg["button"]["btn_up_next"] = $objForm->ge("btn_up_pre") . $objForm->ge("btn_up_next");

        //削除
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_del",
                            "value"     => "削 除",
                            "extrahtml" => "$disabled onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //取消
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return btn_submit('reset');\""  ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了
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
                            "name"      => "cflg",
                            "value"     => $model->cflg) );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl030kForm1.html", $arg);
    }
}
?>