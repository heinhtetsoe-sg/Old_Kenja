<?php

require_once('for_php7.php');

class knjl510aForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl510aindex.php", "", "main");

        //入試年度
        $arg["TOP"]["YEAR"] = $model->year;

        //一覧表示
        if((!isset($model->warning))){
            //データを取得
            $disabled = "";
            $Row = knjl510aQuery::get_edit_data($model);
            if($model->cmd == 'reference') {
                if(!is_array($Row)) {
                    $disabled = "disabled";
                    $model->setWarning("MSG303");
                } else {
                    $model->testdiv      = $Row["TESTDIV"];
                }
            }
            if($model->cmd == 'main'){
                $disabled = ($model->examno == "")? "disabled" : "";
                if(is_array($Row)) {
                    //reset時に入試区分を変更前に戻すため
                    $model->testdiv      = $Row["TESTDIV"];
                }
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
                    $Row = knjl510aQuery::get_edit_data($model);
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
        //入試区分コンボ
        $result = $db->query(knjl510aQuery::getNamecd($model->year,"L004"));
        $opt   = array();
        $opt[] = array("label" => "", "value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }

        $extra = "";
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->testdiv, $opt, $extra, "1");
        $result->free();

        //受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $model->examno ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //氏名(志願者)
        $extra = "onChange=\"change_flg()\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 82, 40, $extra);

        //氏名かな(志願者)
        $extra = "onChange=\"change_flg()\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 82, 40, $extra);

        //出身学校
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_ENTEXAM_FINSCHOOL/knjwexam_fin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=&entexamyear={$model->year}', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["data"]["btn_fs_reference"] = knjCreateBtn($objForm, "btn_fs_reference", "検 索", $extra);
        $finschoolname = $db->getOne(knjl510aQuery::getFinschoolName($model, $Row["FS_CD"]));
        $arg["data"]["FINSCHOOLNAME"] = $Row["FINSCHOOLNAME"] ? $Row["FINSCHOOLNAME"] : $finschoolname;

        $extra = "onkeyup=\"SetFsCd(this)\"; onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["FS_CD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FS_CD", 4, 4, $extra);

        //続柄
        $result = $db->query(knjl510aQuery::getRelationship($model));
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
        }
        $extra = "";
        $arg["data"]["RELATIONSHIP"] = knjCreateCombo($objForm, "RELATIONSHIP", $Row["RELATIONSHIP"], $opt, $extra, "1");

        //志望区分
        $result = $db->query(knjl510aQuery::getExamcourse($model));
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
        }
        $arg["data"]["HOPE_COURSECODE"] = knjCreateCombo($objForm, "HOPE_COURSECODE", $Row["HOPE_COURSECODE"], $opt, $extra, "1");
        $result->free();

        //生年月日
        global $sess;
        $birthday = str_replace("-","/",$Row["BIRTHDAY"]);
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar($objForm, "BIRTHDAY", $birthday);

        //卒業年月日
        $fs_day = str_replace("-","/",$Row["FS_DAY"]);
        $arg["data"]["FS_DAY"] = View::popUpCalendar($objForm, "FS_DAY", $fs_day);

        //現住所コード
        $result = $db->query(knjl510aQuery::getNamecd($model->year,"L007"));
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
        $arg["data"]["ZIPCD"] = View::popUpZipCode($objForm, "ZIPCD", $Row["ZIPCD"],"ADDRESS1");

        //読込ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_zip",
                            "value"       => "郵便番号入力支援",
                            "extrahtml"   => "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );

        //確定ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_apply",
                            "value"       => "確定",
                            "extrahtml"   => "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"") );

        $arg["data"]["ZIPCD"] = $objForm->ge("ZIPCD") .$objForm->ge("btn_zip") .$objForm->ge("btn_apply");


        //電話番号(志願者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "TELNO",
                            "size"        => 16,
                            "maxlength"   => 14,
                            "extrahtml"   => "onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg()\"",
                            "value"       => $Row["TELNO"] ));
        $arg["data"]["TELNO"] = $objForm->ge("TELNO");

        //住所(志願者)
        $extra = " onChange=\"change_flg()\"";
        $arg["data"]["ADDRESS1"] = knjCreateTextBox($objForm, $Row["ADDRESS1"], "ADDRESS1", 102, 50, $extra);

        //方書(志願者)
        $extra = " onChange=\"change_flg()\"";
        $arg["data"]["ADDRESS2"] = knjCreateTextBox($objForm, $Row["ADDRESS2"], "ADDRESS2", 102, 50, $extra);


        //所在地コード
        $result = $db->query(knjl510aQuery::getNamecd($model->year,"L007"));
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

        //卒業年
        $objForm->ae( array("type"        => "text",
                            "name"        => "FS_GRDYEAR",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg()\"",
                            "value"       => $Row["FS_GRDYEAR"] ));
        $arg["data"]["FS_GRDYEAR"] = $objForm->ge("FS_GRDYEAR");


        //------------------------------保護者情報-------------------------------------

        //氏名(保護者)
        $extra = "onChange=\"change_flg()\"";
        $arg["data"]["GNAME"] =  knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 82, 40, $extra);

        //氏名かな(保護者)
        $extra = "onChange=\"change_flg()\"";
        $arg["data"]["GKANA"] =  knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 82, 40, $extra);

        //郵便番号入力支援(保護者)
        $arg["data"]["GZIPCD"] = View::popUpZipCode($objForm, "GZIPCD", $Row["GZIPCD"],"GADDRESS1");

        //住所(保護者)
        $extra = "onChange=\"change_flg()\"";
        $arg["data"]["GADDRESS1"] = knjCreateTextBox($objForm, $Row["GADDRESS1"], "GADDRESS1", 102, 50, $extra);

        //方書(保護者)
        $extra = "";
        $arg["data"]["GADDRESS2"] = knjCreateTextBox($objForm, $Row["GADDRESS2"], "GADDRESS2", 102, 50, $extra);

        //電話番号(保護者)
        $objForm->ae( array("type"        => "text",
                            "name"        => "GTELNO",
                            "size"        => 16,
                            "maxlength"   => 14,
                            "extrahtml"   => "onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg()\"",
                            "value"       => $Row["GTELNO"] ));
        $arg["data"]["GTELNO"] = $objForm->ge("GTELNO");


        Query::dbCheckIn($db);

        //-------------------------------- ボタン作成 ------------------------------------

        $zip = $Row["ZIPCD"];
        $gzip = $Row["GZIPCD"];
        $zadd = $Row["ADDRESS1"];
        $gadd = $Row["GADDRESS1"];

        //検索
        $extra = "style=\"width:60px;text-align:center\" onclick=\"return btn_submit('reference', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_reference"]       =  knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //かな検索ボタン
        //$extra = "style=\"width:80px;text-align:center\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL510A/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&testdiv='+document.forms[0]['TESTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL510A/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv={$model->field["APPLICANTDIV"]}&testdiv='+document.forms[0]['TESTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"]  =  knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);

        //前の志願者検索ボタン
        //次の志願者検索ボタン
        $extra1 = "style=\"width:40px;text-align:center\" onClick=\"btn_submit('back1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $extra2 = "style=\"width:40px;text-align:center\" onClick=\"btn_submit('next1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"]       =  knjCreateBtn($objForm, "btn_back", " << ", $extra1).knjCreateBtn($objForm, "btn_next", " >> ", $extra2);

        //画面クリア
        $extra = "style=\"width:90px;text-align:center\" onclick=\"return btn_submit('disp_clear');\"";
        $arg["button"]["btn_clear"]           =  knjCreateBtn($objForm, "btn_clear", "画面クリア", $extra);

        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"]             =  knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新
        $extra = "$disabled onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"]          =  knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //更新(更新後前の志願者)
        $extra1 = "style=\"width:150px\" $disabled onclick=\"return btn_submit('back');\"";
        $extra2 = "style=\"width:150px\" $disabled onclick=\"return btn_submit('next');\"";
        $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra1);
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra2);

        //削除
        $extra = "$disabled onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"]             =  knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"]           =  knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "preExamno", $model->examno);
        knjCreateHidden($objForm, "preTestdiv", $model->testdiv); //データ更新前のtestdivを保持

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl510aForm1.html", $arg);
    }
}
?>