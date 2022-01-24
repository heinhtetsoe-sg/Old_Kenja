<?php

require_once('for_php7.php');

class knjl011qForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl011qindex.php", "", "main");

        if (SCHOOLKIND == "P") {
            $arg["SCHOOLKIND_P"] = "1";
            $arg["HISSHU_MARK"]  = "";
            $arg["NOT_KIND_P"]   = "";
        } else if (SCHOOLKIND == "J") {
            $arg["SCHOOLKIND_J"] = "1";
            $arg["HISSHU_MARK"]  = "";
            $arg["NOT_KIND_P"]   = "1";
        } else {
            $arg["SCHOOLKIND_H"] = "1";
            $arg["HISSHU_MARK"]  = "※ ";
            $arg["NOT_KIND_P"]   = "1";
        }

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata)) && $model->cmd != 'addnew') {
            //データを取得
            $Row = knjl011qQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl011qQuery::get_edit_data($model);
                }
                $model->examno = $Row["EXAMNO"];
                $model->applicantdiv = $Row["APPLICANTDIV"];
            }
            //実践模試データ情報取得
            if ($model->cmd == 'j_torikomi') {
                $Row =& $model->field;
                $jissenRow = knjl011qQuery::torikomiShigansya($model);
                if (!is_array($jissenRow)) {
                    $model->setWarning("MSG303");
                }
                $jissenRow["ZIPCD"]  = ($jissenRow["ZIPCD"]) ? substr($jissenRow["ZIPCD"], 0, 3) ."-".substr($jissenRow["ZIPCD"], 3, 4): "";
                $jissenRow["GZIPCD"] = ($jissenRow["GZIPCD"]) ? $jissenRow["ZIPCD"] :"";
                foreach ($jissenRow as $key => $val) {
                    $Row[$key] = $val;
                }
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
            //新規ボタンを押した時、データがあればエラー表示する。
            if ($model->examno != "") {
                $addnewRow = knjl011qQuery::get_edit_data($model);
                if (is_array($addnewRow)) {
                    if ($model->cmd == 'addnew') {
                        $model->setWarning("MSG302");
                    }
                }
            }
        }

        $arg["TOP"]["YEAR"] = $model->year;

        if ($model->cmd == 'changeTest') {
            $Row =& $model->field;
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //DB接続
        $db = Query::dbCheckOut();

        //入試制度コンボ
        $query = knjl011qQuery::get_name_cd($model->year, "L003");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //小学入試は受験番号4桁
        if (SCHOOLKIND == "P") {
            $model->examNoLength = 4;
        } else {
            $model->examNoLength = 5;
        }

        //受験番号
        $extra = "onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", $model->examNoLength, $model->examNoLength, $extra);

        if ($model->examno != "") {
            //受験区分取得したい
            $kubunRow = $db->getRow(knjl011qQuery::getKubun($model), DB_FETCHMODE_ASSOC);
            $arg["data"]["KUBUN"] = $kubunRow["KUBUN_EXAMCOURSE"]."　　".$kubunRow["KUBUN_TESTDIV"]."　　".$kubunRow["KUBUN_EXAMHALLCD"]."　　".$kubunRow["KUBUN_REMARK_DIV"];
            knjCreateHidden($objForm, "KUBUN", $arg["data"]["KUBUN"]);
        }

        //出欠の記録5年
        $extra = "onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ABSENCE2"] = knjCreateTextBox($objForm, $Row["ABSENCE2"], "ABSENCE2", 3, 3, $extra);

        //出欠の記録6年
        $extra = "onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ABSENCE3"] = knjCreateTextBox($objForm, $Row["ABSENCE3"], "ABSENCE3", 3, 3, $extra);

        //専併区分コンボ
        //「2:推薦入試」「9:駿中生」の場合、「1:専願」のみ入力可能とする。
        $shdiv = ($kubunRow["TESTDIV0"] == "2" || $kubunRow["TESTDIV0"] == "9") ? "1" : "";
        $query = knjl011qQuery::get_name_cd($model->year, "L006", $shdiv);
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SHDIV"], "SHDIV", $extra, 1);
        //「1:海外入試」の場合、専併区分コンボは表示しない。
        $arg["isKaigaiIgai"] = ($kubunRow["TESTDIV0"] == "1") ? "" : "1";

        //推薦受験番号
        $extra = "onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["RECOM_EXAMNO"] = knjCreateTextBox($objForm, $Row["RECOM_EXAMNO"], "RECOM_EXAMNO", 5, 5, $extra);

        //入寮希望コンボ
        $query = knjl011qQuery::getDormitory();
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["DORMITORY_FLG"], "DORMITORY_FLG", $extra, 1, "BLANK");

        //スカラー希望コンボ
        $query = knjl011qQuery::getScholar();
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SCHOLAR_KIBOU"], "SCHOLAR_KIBOU", $extra, 1, "BLANK");

        //基準テスト対象者
        $arg["data"]["GENERAL_FLG"] = strlen($Row["GENERAL_FLG"]) ? "基準テスト対象者" : "";
        knjCreateHidden($objForm, "GENERAL_FLG", $Row["GENERAL_FLG"]);

        //実践模試受験番号
        $extra = "onChange=\"change_flg();\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["JIZEN_BANGOU"] = knjCreateTextBox($objForm, $Row["JIZEN_BANGOU"], "JIZEN_BANGOU", 5, 5, $extra);

        //------------------------------志願者情報-------------------------------------
        //氏名(志願者)
        $extra = "onChange=\"change_flg();\" id=\"NAME\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //氏名かな(志願者)
        $extra = "onChange=\"change_flg();\" id=\"NAME_KANA\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //性別コンボ
        $query = knjl011qQuery::get_name_cd($model->year, "Z002");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "BLANK");

        //生年月日（西暦）
        $extra = " onchange=\"change_flg()\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar2($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), "", "", $extra);

        global $sess;
        //郵便番号入力支援(志願者)
        $extra = "onblur=\"isZipcd(this), toCopytxt(0, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ZIPCD"] = knjCreateTextBox($objForm, $Row["ZIPCD"], "ZIPCD", 10, "", $extra);

        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);

        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS1&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所(志願者)
        $extra = "onblur=\"toCopytxt(1, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ADDRESS1"] = knjCreateTextBox($objForm, $Row["ADDRESS1"], "ADDRESS1", 60, 60, $extra);

        //方書(志願者)
        $extra = "onblur=\"toCopytxt(2, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ADDRESS2"] = knjCreateTextBox($objForm, $Row["ADDRESS2"], "ADDRESS2", 60, 60, $extra);

        //電話番号(志願者)
        $extra = "onblur=\"this.value=toTelNo(this.value), toCopytxt(3, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 14, 14, $extra);

        $arg["title"]["FINSHCOOL"] = (SCHOOLKIND == "P") ? "・園": "";

        //出身学校コード
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\" id=\"FINSCHOOLCD_ID\" ";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 7, 7, $extra);

        //学校名
        $query = knjl011qQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_DISTCD_NAME"].$fsArray["FINSCHOOL_NAME"];

        //かな検索ボタン（出身学校）
        if (SCHOOLKIND == "P") {
            $finSchKind = "1";//"L019"参照
        } else if (SCHOOLKIND == "J") {
            $finSchKind = "2";//"L019"参照
        } else {
            $finSchKind = "3";//"L019"参照
        }
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&setschooltype={$finSchKind}&fscdname=FINSCHOOLCD_ID&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        //本籍コンボ
        $query = knjl011qQuery::getPref($model);
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["FAMILY_REGISTER"], "FAMILY_REGISTER", $extra, 1, "BLANK");

        for ($i = 1; $i <= 4; $i++) {
            //志望校コード
            $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\" id=\"SH_SCHOOLCD{$i}_ID\" ";
            $arg["data"]["SH_SCHOOLCD{$i}"] = knjCreateTextBox($objForm, $Row["SH_SCHOOLCD{$i}"], "SH_SCHOOLCD{$i}", 7, 7, $extra);
            //学校名
            $fsArray = $db->getRow(knjl011qQuery::getFinschoolName($Row["SH_SCHOOLCD{$i}"]), DB_FETCHMODE_ASSOC);
            $arg["data"]["SH_SCHOOLNAME{$i}"] = $fsArray["FINSCHOOL_DISTCD_NAME"].$fsArray["FINSCHOOL_NAME"];
            //かな検索ボタン（志望校）
            $setSchoolKind = (SCHOOLKIND == "J") ? "3" : "4";//"L019"参照
            $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&setschooltype={$setSchoolKind}&fscdname=SH_SCHOOLCD{$i}_ID&fsname=SH_SCHOOLNAME{$i}_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
            $arg["button"]["btn_fin_kana_reference{$i}"] = knjCreateBtn($objForm, "btn_fin_kana_reference{$i}", "検 索", $extra);
        }

        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl011qQuery::get_calendarno($model->year));
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
            if ($row["NAMESPARE2"] <= $model->year."/03/01" && $model->year."/03/01" <= $row["NAMESPARE3"]) {
                // 卒業元号・卒業年の初期値
                $gengouCd = $row["NAMECD2"];
                $fs_y = $model->year - $row["NAMESPARE1"] + 1;
            }
            $arg["data2"][] = array("eracd" => $row["NAMECD2"], "wname" => $row["NAME1"]);
        }

        //卒業元号
        $Row["FS_ERACD"] = strlen($Row["FS_ERACD"]) ? $Row["FS_ERACD"] : $gengouCd;
        $query = knjl011qQuery::get_name_cd($model->year, "L007");
        $extra = "onChange=\"change_flg();\"";
        makeCmb($objForm, $arg, $db, $query, $Row["FS_ERACD"], "FS_ERACD", $extra, 1, "BLANK");

        //卒業年
        $defGrdYear = $model->cmd == 'addnew' ? $fs_y : "";
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_Y"] = knjCreateTextBox($objForm, strlen($Row["FS_Y"]) ? $Row["FS_Y"] : $defGrdYear, "FS_Y", 2, 2, $extra);

        //卒業月
        $defGrdmon = $model->cmd == 'addnew' ? "03" : "";
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_M"] = knjCreateTextBox($objForm, strlen($Row["FS_M"]) ? $Row["FS_M"] : $defGrdmon, "FS_M", 2, 2, $extra);

        //hidden
        knjCreateHidden($objForm, "SET_WAREKI", $fs_y);

        //卒業区分（1:見込み,2:卒業）
        $defGrddiv = $model->cmd == 'addnew' ? "1" : "";
        $Row["FS_GRDDIV"] = strlen($Row["FS_GRDDIV"]) ? $Row["FS_GRDDIV"] : $defGrddiv;
        $query = knjl011qQuery::get_name_cd($model->year, "L016");
        $extra = "onChange=\"changeAndSet_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["FS_GRDDIV"], "FS_GRDDIV", $extra, 1, "BLANK");

        //団体コード
        if (SCHOOLKIND == "J") {
            $query = knjl011qQuery::getSatGroup();
            $extra = "onChange=\"change_flg()\"";
            makeCmb($objForm, $arg, $db, $query, $Row["SAT_GROUPCD"], "SAT_GROUPCD", $extra, 1, "BLANK");
        }

        //兄弟情報(校種)(P,J,H)
        knjCreateHidden($objForm, "SIMAI_SCHOOL_KIND1", $Row["SIMAI_SCHOOL_KIND1"]);
        knjCreateHidden($objForm, "SIMAI_SCHOOL_KIND2", $Row["SIMAI_SCHOOL_KIND2"]);

        //兄弟情報(年組・氏名)　例：「小学1年1組　駿台　三郎」「中学1年A組　駿台　次郎」「高校1年A組　駿台　太郎」
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["SIMAI_NAME1"] = knjCreateTextBox($objForm, $Row["SIMAI_NAME1"], "SIMAI_NAME1", 40, 60, $extra);
        $arg["data"]["SIMAI_NAME2"] = knjCreateTextBox($objForm, $Row["SIMAI_NAME2"], "SIMAI_NAME2", 40, 60, $extra);

        //兄弟情報検索ボタン
        $extra1 = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011Q/knjl011qFamilySearch.php?cmd=search&simaiSchoolKind=SIMAI_SCHOOL_KIND1&simaiName=SIMAI_NAME1&year='+document.forms[0]['CTRL_YEAR'].value+'&semester='+document.forms[0]['CTRL_SEMESTER'].value+'&STUCD_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 600, 350)\"";
        $extra2 = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011Q/knjl011qFamilySearch.php?cmd=search&simaiSchoolKind=SIMAI_SCHOOL_KIND2&simaiName=SIMAI_NAME2&year='+document.forms[0]['CTRL_YEAR'].value+'&semester='+document.forms[0]['CTRL_SEMESTER'].value+'&STUCD_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 600, 350)\"";
        $arg["data"]["btn_family1"] = knjCreateBtn($objForm, "btn_family1", "検 索", $extra1);
        $arg["data"]["btn_family2"] = knjCreateBtn($objForm, "btn_family2", "検 索", $extra2);

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        if (SCHOOLKIND == "J" || SCHOOLKIND == "P") {
            $extra = "onChange=\"change_flg()\" id=\"GNAME\" onkeydown=\"keyChangeEntToTab(this)\"";
        } else {
            $extra = "onChange=\"change_flg()\" id=\"GNAME\" onkeydown=\"keyChangeEntToTab(this)\"";
        }
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 60, $extra);

        //氏名かな(保護者)
        $extra = "onChange=\"change_flg();\" id=\"GKANA\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 40, 120, $extra);

        //続柄コンボ
        $query = knjl011qQuery::get_name_cd($model->year, "H201");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["RELATIONSHIP"], "RELATIONSHIP", $extra, 1, "BLANK");

        //郵便番号入力支援(保護者)
        $extra = "onblur=\"isZipcd(this)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GZIPCD"] = knjCreateTextBox($objForm, $Row["GZIPCD"], "GZIPCD", 10, "", $extra);

        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=GADDRESS1&zipname=GZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);

        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=GADDRESS1&zipname=GZIPCD&zip='+document.forms[0]['GZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS1"] = knjCreateTextBox($objForm, $Row["GADDRESS1"], "GADDRESS1", 60, 60, $extra);

        //方書(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS2"] = knjCreateTextBox($objForm, $Row["GADDRESS2"], "GADDRESS2", 60, 60, $extra);

        //電話番号(保護者)
        $extra = "onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GTELNO"] = knjCreateTextBox($objForm, $Row["GTELNO"], "GTELNO", 14, 14, $extra);

        //職業(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GJOB"] = knjCreateTextBox($objForm, $Row["GJOB"], "GJOB", 100, 100, $extra);

        //備考(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 100, 100, $extra);

        //------------------------------内申-------------------------------------
        //調査書登録
        $link = REQUESTROOT."/L/KNJL021Q/knjl021qindex.php?cmd=&SEND_PRGID=KNJL011Q&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "$disabled onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_chousasho"] = knjCreateBtn($objForm, "btn_chousasho", "調査書入力", $extra);
        //活動実績登録
        $link = REQUESTROOT."/L/KNJL024Q/knjl024qindex.php?cmd=&SEND_PRGID=KNJL011Q&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "$disabled onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_ao"] = knjCreateBtn($objForm, "btn_ao", "活動実績入力", $extra);

        //-------------------------------- ボタン作成 ------------------------------------
        $zip = $Row["ZIPCD"];
        $gzip = $Row["GZIPCD"];
        $zadd = $Row["ADDRESS1"];
        $gadd = $Row["GADDRESS1"];

        //検索ボタン
        $extra = "onclick=\"return btn_submit('reference', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011Q/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
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

        //実践模試データ取込button
        $extra = "$disabled onclick=\"return btn_submit('j_torikomi');\"";
        $arg["button"]["j_torikomi"] = knjCreateBtn($objForm, "j_torikomi", "模試データ取込", $extra);

        //実戦模試受験者より検索ボタン
        $extra = "$disabled style=\"width:170px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011Q/knjl011qSatNameSearch.php?cmd=search&year='+document.forms[0]['CTRL_YEAR'].value+'&semester='+document.forms[0]['CTRL_SEMESTER'].value+'&STUCD_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 600, 210)\"";
        $arg["button"]["j_search"] = knjCreateBtn($objForm, "j_search", "実戦模試受験者より検索", $extra);

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
        View::toHTML($model, "knjl011qForm1.html", $arg);
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