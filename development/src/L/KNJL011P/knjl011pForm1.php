<?php

require_once('for_php7.php');

class knjl011pForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl011pindex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata)) && $model->cmd != 'addnew') {
            //データを取得
            $Row = knjl011pQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl011pQuery::get_edit_data($model);
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
/***
            //新規ボタンを押した時、データがあればエラー表示する。
            if ($model->examno != "") {
                $addnewRow = knjl011pQuery::get_edit_data($model);
                if (is_array($addnewRow)) {
                    if ($model->cmd == 'addnew') {
                        $model->setWarning("MSG302");
                    }
                }
            }
***/
        }

        $arg["TOP"]["YEAR"] = $model->year;

        if ($model->cmd == 'showdivAdd') {
            $Row["APPLICANTDIV"] = $model->field["APPLICANTDIV"];
            $Row["TESTDIV"] = $model->field["TESTDIV"];
        }

        if ($model->cmd == 'changeApp') {
            $Row["APPLICANTDIV"] = $model->field["APPLICANTDIV"];
        }

        if ($model->cmd == 'changeTest') {
            $Row =& $model->field;
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //DB接続
        $db = Query::dbCheckOut();

        //入試制度コンボ
        $query = knjl011pQuery::get_name_cd($model->year, "L003");
        $extra = "onChange=\"change_flg(); return btn_submit('changeApp');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //受験番号
        $extra = " STYLE=\"ime-mode: inactive\" onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 6, 6, $extra);

        if ($model->examno != "") {
            //受験区分取得したい
            $kubunRow = $db->getRow(knjl011pQuery::getKubun($model), DB_FETCHMODE_ASSOC);
            $arg["data"]["KUBUN"] = $kubunRow["KUBUN_TESTDIV"];
            knjCreateHidden($objForm, "KUBUN", $arg["data"]["KUBUN"]);
        }

        //受付日付
        $extra = " onchange=\"change_flg()\"";
        $Row["RECEPTDATE"] = ($Row["RECEPTDATE"] == "") ? CTRL_DATE: $Row["RECEPTDATE"];
        $arg["data"]["RECEPTDATE"] = View::popUpCalendar2($objForm, "RECEPTDATE", str_replace("-", "/", $Row["RECEPTDATE"]), "", "", $extra);

        //入試区分
        $query = knjl011pQuery::get_name_cd($model->year, ($Row["APPLICANTDIV"] == "2") ? "L004" : "L024");
        $extra = "onChange=\"change_flg(); return btn_submit('changeTest');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1, "BLANK");

        //試験会場コンボ
        $query = knjl011pQuery::getHall($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"]);
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SIKEN_KAIJOU_CD1"], "SIKEN_KAIJOU_CD1", $extra, 1, "BLANK");

        //------------------------------志願者情報-------------------------------------
        //氏名(志願者)
        $extra = " STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"NAME\" onkeyup=\"keySet('NAME', 'NAME_KANA', 'H');\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //氏名かな(志願者)
        $extra = " STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"NAME_KANA\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //性別コンボ
        $query = knjl011pQuery::get_name_cd($model->year, "Z002");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "BLANK");

        //生年月日（西暦）
        $extra = " STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\"";
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar2($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), "", "", $extra);

        //年齢
        $extra = " STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["REMARK2"] = knjCreateTextBox($objForm, $Row["REMARK2"], "REMARK2", 2, 2, $extra);

        global $sess;
        //郵便番号入力支援(志願者)
        $extra = " STYLE=\"ime-mode: inactive\" onblur=\"isZipcd(this), toCopytxt(0, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ZIPCD"] = knjCreateTextBox($objForm, $Row["ZIPCD"], "ZIPCD", 10, "", $extra);

        //確定ボタン
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS1&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "①確定", $extra);

        //住所検索ボタン
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "②〒・住所検索", $extra);

        //住所(志願者)
        $extra = " STYLE=\"ime-mode: active\" onblur=\"toCopytxt(1, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ADDRESS1"] = knjCreateTextBox($objForm, $Row["ADDRESS1"], "ADDRESS1", 60, 60, $extra);

        //方書(志願者)
        $extra = " STYLE=\"ime-mode: active\" onblur=\"toCopytxt(2, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ADDRESS2"] = knjCreateTextBox($objForm, $Row["ADDRESS2"], "ADDRESS2", 60, 60, $extra);

        //電話番号(志願者)
        $defTelno = $model->cmd == 'showdivAdd' && !($Row["APPLICANTDIV"] == "1" && $Row["TESTDIV"] == "1") ? "088-" : "";
        $extra = " STYLE=\"ime-mode: inactive\" onblur=\"this.value=toTelNo(this.value), toCopytxt(3, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, strlen($Row["TELNO"]) ? $Row["TELNO"] : $defTelno, "TELNO", 14, 14, $extra);

        //出身学校コード
        $extra = " STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\" id=\"FINSCHOOLCD_ID\" ";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 7, 7, $extra);

        //学校名
        $query = knjl011pQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_DISTCD_NAME"].$fsArray["FINSCHOOL_NAME"];

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&fscdname=FINSCHOOLCD_ID&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        //塾
        $extra = " STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["PRISCHOOLCD"] = knjCreateTextBox($objForm, $Row["PRISCHOOLCD"], "PRISCHOOLCD", 7, 7, $extra);

        //教室コード
        $extra = " STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["PRISCHOOL_CLASS_CD"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_CLASS_CD"], "PRISCHOOL_CLASS_CD", 7, 7, $extra);

        //かな検索ボタン（塾）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_PRISCHOOL/knjwpri_searchindex.php?cmd=&pricdname=&priname=&priaddr=&prischool_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_pri_kana_reference"] = knjCreateBtn($objForm, "btn_pri_kana_reference", "検 索", $extra);

        $query = knjl011pQuery::getPriSchoolName($Row["PRISCHOOLCD"]);
        $setFin = $db->getOne($query);
        $arg["data"]["PRISCHOOL_NAME"] = $setFin;

        $query = knjl011pQuery::getPriSchoolClassName($Row["PRISCHOOLCD"], $Row["PRISCHOOL_CLASS_CD"]);
        $setFin = $db->getOne($query);
        $arg["data"]["PRISCHOOL_CLASS_NAME"] = $setFin;

        $gengouCd = "";
        $gengouName = "";
        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl011pQuery::get_calendarno($model->year));
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
            if ($row["NAMESPARE2"] <= $model->year."/03/01" && $model->year."/03/01" <= $row["NAMESPARE3"]) {
                $gengouCd = $row["NAMECD2"];
                $gengouName = $row["NAME1"];
            }
        }

        //卒業元号
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(0, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_ERACD"] = knjCreateTextBox($objForm, strlen($Row["FS_ERACD"]) ? $Row["FS_ERACD"] : $gengouCd, "FS_ERACD", 1, 1, $extra);

        //卒業和暦名
        $fs_wname = isset($Row["FS_WNAME"]) ? str_replace("&nbsp;", "", $Row["FS_WNAME"]) : $gengouName;
        knjCreateHidden($objForm, "FS_WNAME", $fs_wname);
        $arg["data"]["FS_WNAME"] = $fs_wname;

        //卒業年
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_Y"] = knjCreateTextBox($objForm, $Row["FS_Y"], "FS_Y", 2, 2, $extra);

        //卒業月
        $defGrdmon = $model->cmd == 'showdivAdd' ? "03" : "";
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_M"] = knjCreateTextBox($objForm, strlen($Row["FS_M"]) ? $Row["FS_M"] : $defGrdmon, "FS_M", 2, 2, $extra);

        //卒業区分（1:見込み,2:卒業）
        $defGrddiv = $model->cmd == 'showdivAdd' ? "1" : "";
        $Row["FS_GRDDIV"] = strlen($Row["FS_GRDDIV"]) ? $Row["FS_GRDDIV"] : $defGrddiv;
        $query = knjl011pQuery::get_name_cd($model->year, "L016");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["FS_GRDDIV"], "FS_GRDDIV", $extra, 1, "BLANK");

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $extra = " STYLE=\"ime-mode: active\" onChange=\"change_flg()\" id=\"GNAME\" onkeyup=\"keySet('GNAME', 'GKANA', 'H');\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 60, $extra);

        //氏名かな(保護者)
        $extra = " STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"GKANA\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 40, 120, $extra);

        //続柄コンボ
        $query = knjl011pQuery::get_name_cd($model->year, "H201");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["RELATIONSHIP"], "RELATIONSHIP", $extra, 1, "BLANK");

        //郵便番号入力支援(保護者)
//      $arg["data"]["GZIPCD"] = View::popUpZipCode($objForm, "GZIPCD", $Row["GZIPCD"], "GADDRESS1");
        $extra = " STYLE=\"ime-mode: inactive\" onblur=\"isZipcd(this)\"";
        $arg["data"]["GZIPCD"] = knjCreateTextBox($objForm, $Row["GZIPCD"], "GZIPCD", 10, "", $extra);

        //確定ボタン
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=GADDRESS1&zipname=GZIPCD&zip='+document.forms[0]['GZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "①確定", $extra);

        //住所検索ボタン
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=GADDRESS1&zipname=GZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "②〒・住所検索", $extra);

        //住所(保護者)
        $extra = " STYLE=\"ime-mode: active\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS1"] = knjCreateTextBox($objForm, $Row["GADDRESS1"], "GADDRESS1", 60, 60, $extra);

        //方書(保護者)
        $extra = " STYLE=\"ime-mode: active\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS2"] = knjCreateTextBox($objForm, $Row["GADDRESS2"], "GADDRESS2", 60, 60, $extra);

        //電話番号(保護者)
        $defGTelno = $model->cmd == 'showdivAdd' && !($Row["APPLICANTDIV"] == "1" && $Row["TESTDIV"] == "1") ? "088-" : "";
        $extra = " STYLE=\"ime-mode: inactive\" onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GTELNO"] = knjCreateTextBox($objForm, strlen($Row["GTELNO"]) ? $Row["GTELNO"] : $defGTelno, "GTELNO", 14, 14, $extra);

        //電話番号(保護者)(携帯)
        $extra = " STYLE=\"ime-mode: inactive\" onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GTELNO2"] = knjCreateTextBox($objForm, $Row["GTELNO2"], "GTELNO2", 14, 14, $extra);

        //通学方法コンボ
        $query = knjl011pQuery::get_name_cd($model->year, "L047");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["DORMITORY_FLG"], "DORMITORY_FLG", $extra, 1, "BLANK");

        //本校在籍又は卒業の保護者・兄弟姉妹
        $extra = " STYLE=\"ime-mode: active\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["SIMAI_NAME"] = knjCreateTextBox($objForm, $Row["SIMAI_NAME"], "SIMAI_NAME", 40, 60, $extra);

        //検索ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011P/knjl011pFamilySearch.php?cmd=search&year='+document.forms[0]['CTRL_YEAR'].value+'&semester='+document.forms[0]['CTRL_SEMESTER'].value+'&MODE=reflect&useGuardian2={$model->Properties["useGuardian2"]}&STUCD_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY - 300 + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 600, 350)\"";
        $arg["data"]["BTN_STUCD"] = knjCreateBtn($objForm, "btn_stucd", "兄弟姉妹検索", $extra);

        //志願理由(全角で45文字)
        $extra = " STYLE=\"ime-mode: active\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 90, 90, $extra);

        //前期受験番号
        $disText = ($Row["APPLICANTDIV"] == "1" && $Row["TESTDIV"] == "3") ? "" : " disabled";
        $extra = " STYLE=\"ime-mode: inactive\" onChange=\"change_flg();\" onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["DAI1_RECEPTNO"] = knjCreateTextBox($objForm, $Row["DAI1_RECEPTNO"], "DAI1_RECEPTNO", 6, 6, $extra.$disText);

        //前期受験番号検索ボタン
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011P/knjl011pZenkiExamnoSearch.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&ZENKI_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY - 300 + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["BTN_DAI1_RECEPTNO"] = knjCreateBtn($objForm, "btn_dai1_receptno", "前期受験番号検索", $extra.$disText);

        //------------------------------内申-------------------------------------
        //調査書登録
        $link = REQUESTROOT."/L/KNJL021P/knjl021pindex.php?cmd=&SEND_PRGID=KNJL011P&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "$disabled onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_chousasho"] = knjCreateBtn($objForm, "btn_chousasho", "調査書入力", $extra);

        //-------------------------------- ボタン作成 ------------------------------------
        $zip = $Row["ZIPCD"];
        $gzip = $Row["GZIPCD"];
        $zadd = $Row["ADDRESS1"];
        $gadd = $Row["GADDRESS1"];

        //検索ボタン
        $extra = "onclick=\"return btn_submit('reference', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011P/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);

        //前の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //画面クリアボタン
        $extra = "style=\"width:90px\" onclick=\"return btn_submit('disp_clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "画面クリア", $extra);

        //志願者よりコピーボタン
        $extra = "$disabled style=\"width:135px\" onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "志願者よりコピー", $extra);

        //新規ボタン
        $extra = "onclick=\"return btn_submit('addnew');\"";
        $arg["button"]["btn_addnew"] = knjCreateBtn($objForm, "btn_addnew", "新 規", $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
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

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl011pForm1.html", $arg);
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
        if ($name == 'APPLICANTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>