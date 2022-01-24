<?php

require_once('for_php7.php');
class knjl111dForm1 {
    function main(&$model) {
        $objForm      = new form;

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            //データを取得
            $Row = knjl111dQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl111dQuery::get_edit_data($model);
                }
                $model->examno  = $Row["EXAMNO"];
                $model->testdiv = $Row["TESTDIV"];
            }
            $disabled = "";
            if (!is_array($Row)) {
                $disabled = "disabled";
                if ($model->cmd == 'reference') {
                    $Row["TESTDIV"] = $model->field["TESTDIV"];
                    $model->setWarning("MSG303");
                }
            }
        } else {
            $Row =& $model->field;
        }

        if ($model->cmd == 'changeTest' || $model->cmd == 'main') {
            $Row["DESIREDIV"] = $model->field["DESIREDIV"];
            $Row["TESTDIV"]   = $model->field["TESTDIV"];
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //DB接続
        $db = Query::dbCheckOut();

        //エンター押下時の移動対象一覧
        $setTextField = array();
        $setTextField[] = "NAME";
        $setTextField[] = "NAME_KANA";
        $setTextField[] = "SEX";
        $setTextField[] = "ERACD";
        $setTextField[] = "BIRTH_Y";
        $setTextField[] = "BIRTH_M";
        $setTextField[] = "BIRTH_D";
        $setTextField[] = "FINSCHOOLCD";
        $setTextField[] = "FS_ERACD";
        $setTextField[] = "FS_Y";
        $setTextField[] = "FS_M";
        $setTextField[] = "FS_GRDDIV";
        $setTextField[] = "REMARK8_033";
        $setTextField[] = "REMARK2_033";
        $setTextField[] = "REMARK3_033";
        $setTextField[] = "REMARK4_033";
        $setTextField[] = "REMARK5_033";
        $setTextField[] = "REMARK6_033";
        $setTextField[] = "REMARK9_033";

        $setTextField[] = "ZIPCD";
        $setTextField[] = "ADDRESS1";
        $setTextField[] = "ADDRESS2";
        $setTextField[] = "TELNO";
        $setTextField[] = "GNAME";
        $setTextField[] = "GKANA";
        $setTextField[] = "RELATIONSHIP";
        $setTextField[] = "GZIPCD";
        $setTextField[] = "GADDRESS1";
        $setTextField[] = "GADDRESS2";
        $setTextField[] = "GTELNO";
        $setTextField[] = "GTELNO2";
        knjCreateHidden($objForm, "setTextField", implode(',', $setTextField));

        //年度
        $opt = array();
        $opt[] = array('value' => CTRL_YEAR,     'label' => CTRL_YEAR);
        $opt[] = array('value' => CTRL_YEAR + 1, 'label' => CTRL_YEAR + 1);
        $model->year = ($model->year == "") ? substr(CTRL_DATE, 0, 4): $model->year;
        $extra = "onChange=\"change_flg(); return btn_submit('changeTest');\"";
        $arg["TOP"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->year, $opt, $extra, 1);

        //志願区分
        $query = knjl111dQuery::get_name_cd($model->year, "L058");
        $extra = "onChange=\"change_flg(); return btn_submit('changeTest');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["DESIREDIV"], "DESIREDIV", $extra, 1, "");

        //入試区分
        $maxTestDiv = $db->getOne(knjl111dQuery::getMaxTestDiv($model));
        $query = knjl111dQuery::get_name_cd($model->year, "L004");
        $extra = "onChange=\"change_flg(); return btn_submit('changeTest');\"";
        $Row["TESTDIV"] = ($Row["TESTDIV"]) ? $Row["TESTDIV"]: $maxTestDiv;
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1, "");

        //受験番号
        $extra = " STYLE=\"ime-mode: inactive\" onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 3, 3, $extra);

        //------------------------------志願者情報-------------------------------------
        //氏名(志願者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"NAME\" onkeyup=\"keySet('NAME', 'NAME_KANA', 'H');\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //氏名かな(志願者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"NAME_KANA\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //性別(志願者)
        $query = knjl111dQuery::get_name_cd($model->year, "Z002");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "");

        //元号
        $query = knjl111dQuery::get_name_cd($model->year, "L007");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $Row["ERACD"] = ($Row["ERACD"]) ? $Row["ERACD"]: "4";//デフォルト4:平成
        makeCmb($objForm, $arg, $db, $query, $Row["ERACD"], "ERACD", $extra, 1, "");
        //年
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["BIRTH_Y"] = knjCreateTextBox($objForm, $Row["BIRTH_Y"], "BIRTH_Y", 2, 2, $extra);
        //月
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["BIRTH_M"] = knjCreateTextBox($objForm, $Row["BIRTH_M"], "BIRTH_M", 2, 2, $extra);
        //日
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["BIRTH_D"] = knjCreateTextBox($objForm, $Row["BIRTH_D"], "BIRTH_D", 2, 2, $extra);

        //出身校
        //出身学校コード（中学）
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\" id=\"FINSCHOOLCD_ID\" ";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 7, 7, $extra);
        //検索ボタン（中学）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&setSchoolKind=3&fscdname=FINSCHOOLCD&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference1"] = knjCreateBtn($objForm, "btn_fin_kana_reference1", "検 索", $extra);
        //学校名
        $query = knjl111dQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_NAME"];
        //卒業元号
        $query = knjl111dQuery::get_name_cd($model->year, "L007");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $Row["FS_ERACD"] = ($Row["FS_ERACD"]) ? $Row["FS_ERACD"]: "4";//デフォルト4:平成
        makeCmb($objForm, $arg, $db, $query, $Row["FS_ERACD"], "FS_ERACD", $extra, 1, "");
        //卒業年
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["FS_Y"] = knjCreateTextBox($objForm, $Row["FS_Y"], "FS_Y", 2, 2, $extra);
        //卒業月
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $Row["FS_M"] = ($Row["FS_M"]) ? $Row["FS_M"]: "03";
        $arg["data"]["FS_M"] = knjCreateTextBox($objForm, $Row["FS_M"], "FS_M", 2, 2, $extra);
        //卒業区分
        $query = knjl111dQuery::get_name_cd($model->year, "L016");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $Row["FS_GRDDIV"] = ($Row["FS_GRDDIV"]) ? $Row["FS_GRDDIV"]: "2";
        makeCmb($objForm, $arg, $db, $query, $Row["FS_GRDDIV"], "FS_GRDDIV", $extra, 1, "");
        //転編入学照会日
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["REMARK8_033"] = View::popUpCalendarAlp($objForm, "REMARK8_033", $Row["REMARK8_033"], $extra, "");

        //出身学校コード（高校）
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\" id=\"FINSCHOOLCD_ID2\" ";
        $arg["data"]["REMARK2_033"] = knjCreateTextBox($objForm, $Row["REMARK2_033"], "REMARK2_033", 7, 7, $extra);
        //検索ボタン（高校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&setSchoolKind=4&fscdname=REMARK2_033&fsname=FINSCHOOLNAME_ID2&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference2"] = knjCreateBtn($objForm, "btn_fin_kana_reference2", "検 索", $extra);
        //学校名
        $query = knjl111dQuery::getFinschoolName($Row["REMARK2_033"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME_H"] = $fsArray["FINSCHOOL_NAME"];
        //課程
        $opt = array();
        $opt[] = array(value => "" , label => "");
        $opt[] = array(value => "1", label => "1:全日制");
        $opt[] = array(value => "2", label => "2:定時制");
        $opt[] = array(value => "3", label => "3:通信制");
        $extra = " onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["REMARK3_033"] = knjCreateCombo($objForm, "REMARK3_033", $Row["REMARK3_033"], $opt, $extra, 1);
        //学科
        $extra = " onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["REMARK4_033"] = knjCreateTextBox($objForm, $Row["REMARK4_033"], "REMARK4_033", 20, 30, $extra);
        //学年
        $extra = "style=\"text-align:right; ime-mode: inactive;\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["REMARK5_033"] = knjCreateTextBox($objForm, $Row["REMARK5_033"], "REMARK5_033", 3, 3, $extra);
        //状況
        $opt = array();
        $opt[] = array(value => "" , label => "");
        $opt[] = array(value => "1", label => "1:在学中");
        $opt[] = array(value => "2", label => "2:退学");
        $extra = " onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["REMARK6_033"] = knjCreateCombo($objForm, "REMARK6_033", $Row["REMARK6_033"], $opt, $extra, 1);
        //調査書照会日
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["REMARK9_033"] = View::popUpCalendarAlp($objForm, "REMARK9_033", $Row["REMARK9_033"], $extra, "");

        global $sess;
        //郵便番号入力支援
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"isZipcd(this)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["ZIPCD"] = knjCreateTextBox($objForm, $Row["ZIPCD"], "ZIPCD", 10, "", $extra);
        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS1&addr2name=ADDRESS2&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);
        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS1&addr2name=ADDRESS2&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["ADDRESS1"] = knjCreateTextBox($objForm, $Row["ADDRESS1"], "ADDRESS1", 60, 150, $extra);

        //方書
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["ADDRESS2"] = knjCreateTextBox($objForm, $Row["ADDRESS2"], "ADDRESS2", 60, 150, $extra);

        //電話番号
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 14, 14, $extra);

        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl111dQuery::get_calendarno($model->year));
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

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg()\" id=\"GNAME\" onkeyup=\"keySet('GNAME', 'GKANA', 'H');\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 60, $extra);

        //氏名かな(保護者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"GKANA\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 40, 120, $extra);

        //続柄コンボ
        $query = knjl111dQuery::get_name_cd($model->year, "H201");
        $extra = "onChange=\"change_flg()\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["RELATIONSHIP"], "RELATIONSHIP", $extra, 1, "BLANK");

        global $sess;
        //郵便番号入力支援(保護者)
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"isZipcd(this)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GZIPCD"] = knjCreateTextBox($objForm, $Row["GZIPCD"], "GZIPCD", 10, "", $extra);

        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=GADDRESS1&zipname=GZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);

        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=GADDRESS1&zipname=GZIPCD&zip='+document.forms[0]['GZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GADDRESS1"] = knjCreateTextBox($objForm, $Row["GADDRESS1"], "GADDRESS1", 60, 150, $extra);

        //方書(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GADDRESS2"] = knjCreateTextBox($objForm, $Row["GADDRESS2"], "GADDRESS2", 60, 150, $extra);

        //電話番号(保護者)
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GTELNO"] = knjCreateTextBox($objForm, $Row["GTELNO"], "GTELNO", 14, 14, $extra);

        //携帯電話(保護者)
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GTELNO2"] = knjCreateTextBox($objForm, $Row["GTELNO2"], "GTELNO2", 14, 14, $extra);

        //-------------------------------- ボタン作成 ------------------------------------
        $gzip = $Row["GZIPCD"];
        $gadd = $Row["GADDRESS1"];

        //検索ボタン（受験番号）
        $extra = "onclick=\"return btn_submit('reference', '".$gzip."', '".$gadd."');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL111D/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv={$model->applicantdiv}&desirediv='+document.forms[0]['DESIREDIV'].value+'&testdiv='+document.forms[0]['TESTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);

        //前の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1', '".$gzip."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1', '".$gzip."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //画面クリアボタン
        $extra = "style=\"width:90px\" onclick=\"return btn_submit('disp_clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "画面クリア", $extra);

        //追加ボタン
        $extra = " onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新ボタン
        $extra = "$disabled onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
        //更新ボタン(更新後前の志願者)
        $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('back');\"";
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra);
        //更新ボタン(更新後次の志願者)
        $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('next');\"";
        $arg["button"]["btn_up_next"] .= knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra);
        //削除ボタン
        $extra = "$disabled onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["start"] = $objForm->get_start("main", "POST", "knjl111dindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl111dForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>