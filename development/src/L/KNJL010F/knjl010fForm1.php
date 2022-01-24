<?php

require_once('for_php7.php');

class knjl010fForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("main", "POST", "knjl010findex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata)) && $model->cmd != 'addnew') {
            //データを取得
            $Row = knjl010fQuery::getEditData($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303", "更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl010fQuery::getEditData($model);
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

        $arg["TOP"]["YEAR"] = $model->year;
        if ($model->cmd == 'showdivAdd') {
            $Row["APPLICANTDIV"] = $model->field["APPLICANTDIV"];
            $Row["TEST_L_DIV"] = $model->field["TEST_L_DIV"];
        }
        if ($model->cmd == 'changeTest') {
            $Row =& $model->field;
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //DB接続
        $db = Query::dbCheckOut();

        //------------------------------志願者情報-------------------------------------
        //入試制度コンボ
        $query = knjl010fQuery::getNameCd($model->year, "L003", "1");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");
        /***
                //入試区分大分類コンボ　1:帰国生入試 2:一般入試
                foreach ($model->testdivArray as $key => $codeArray) {
                    if (strlen($Row["TESTDIV".$codeArray["TESTDIV"]])) {
                        if ($codeArray["KIKOKU_FLG"] == "1") $Row["TEST_L_DIV"] = "1";
                        if ($codeArray["KIKOKU_FLG"] != "1") $Row["TEST_L_DIV"] = "2";
                    }
                }
        ***/
        //入試区分大分類コンボ　1:帰国生入試 2:一般入試
        $query = knjl010fQuery::getTestLDiv($model->year, "L024");
        $extra = "onChange=\"change_flg(); btn_submit('changeTest', '', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["TEST_L_DIV"], "TEST_L_DIV", $extra, 1, "");
        $arg["KIKOKU_FLG1"] = ($Row["TEST_L_DIV"] == "1") ? "1" : "";
        $arg["KIKOKU_FLG2"] = ($Row["TEST_L_DIV"] != "1") ? "1" : "";

        //管理番号(EXAMNO)
        //$extra = "onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $extra = " style=\"background:#cccccc;\" readOnly";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 5, 5, $extra);

        //入試区分
        foreach ($model->testdivArray as $key => $codeArray) {
            $name = "TESTDIV".$codeArray["TESTDIV"];
            $checked = strlen($Row[$name]) ? "checked" : "";
            //重複受験チェック
            $disTestdivA = $disTestdivB = $disTestdivC = "";
            if ($name == "TESTDIV16") {
                $disTestdivA .= strlen($Row["TESTDIV9"])  ? " disabled" : "";
                $disTestdivA .= strlen($Row["TESTDIV12"]) ? " disabled" : "";
                $disTestdivA .= strlen($Row["TESTDIV18"]) ? " disabled" : "";
            }
            if ($name == "TESTDIV9") {
                $disTestdivA .= strlen($Row["TESTDIV16"])  ? " disabled" : "";
                $disTestdivA .= strlen($Row["TESTDIV12"]) ? " disabled" : "";
                $disTestdivA .= strlen($Row["TESTDIV18"]) ? " disabled" : "";
            }
            if ($name == "TESTDIV12") {
                $disTestdivA .= strlen($Row["TESTDIV16"])  ? " disabled" : "";
                $disTestdivA .= strlen($Row["TESTDIV9"])  ? " disabled" : "";
                $disTestdivA .= strlen($Row["TESTDIV18"]) ? " disabled" : "";
            }
            if ($name == "TESTDIV18") {
                $disTestdivA .= strlen($Row["TESTDIV9"])  ? " disabled" : "";
                $disTestdivA .= strlen($Row["TESTDIV12"]) ? " disabled" : "";
                $disTestdivA .= strlen($Row["TESTDIV16"]) ? " disabled" : "";
            }
            if ($name == "TESTDIV10") {
                $disTestdivB = strlen($Row["TESTDIV13"]) ? " disabled" : "";
            }
            if ($name == "TESTDIV13") {
                $disTestdivB = strlen($Row["TESTDIV10"]) ? " disabled" : "";
            }
            $extra = "onChange=\"change_flg(); disTestdiv(this);\" id=\"{$name}\" " .$checked.$disTestdivA.$disTestdivB.$disTestdivC;
            $arg["data"][$name] = knjCreateCheckBox($objForm, $name, $codeArray["TESTDIV"], $extra);
            $arg["data"][$name."_ID"] = $name;
            $arg["data"][$name."_NAME"] = $codeArray["TESTDIV_NAME"];
            $arg["data"][$name."_DATE"] = $codeArray["TESTDIV_DATE"];
        }

        //受験型を配列にセット（全て）
        $examTypeALL = array();
        $query = knjl010fQuery::getExamTypeALL("L005");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $examTypeALL[] = array("EXAM_TYPE" => $row["NAMECD2"], "EXAM_TYPE_NAME" => $row["NAME1"]);
        }
        $result->free();

        //科目を配列にセット
        $subcdArray = array();
        $query = knjl010fQuery::getNameCd($model->year, "L009");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $subcdArray[] = array("VALUE" => $row["NAMECD2"], "LABEL" => $row["NAME1"]);
        }
        $result->free();

        //受験型(全て)
        foreach ($model->testdivArray as $key => $codeArray) {
            $disSubcdExtra = ($codeArray["KIKOKU_FLG"] == "1") ? "onChange=\"disSubcd(this, {$codeArray["TESTDIV"]});\" " : "";
            $name = "EXAM_TYPE".$codeArray["TESTDIV"];
            $extra = array();
            $opt = array();
            $dataCnt = 0;
            foreach ($examTypeALL as $keyALL => $codeALL) {
                $opt[] = $codeALL["EXAM_TYPE"];
                $dataCnt++;
                $nameID = $name.$dataCnt;
                $extra[] = $disSubcdExtra."id=\"{$nameID}\"";
                $arg["data"][$nameID."_ID"] = $nameID;
                $arg["data"][$nameID."_LABEL"] = $codeALL["EXAM_TYPE_NAME"];
            }
            $radioArray = knjCreateRadio($objForm, $name, ($Row[$name] == "A") ? "10" : $Row[$name], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key2 => $val2) {
                $arg["data"][$key2] = $val2;
            }
            //帰国生Ｂ方式選択科目
            $disSubcd = ($codeArray["KIKOKU_FLG"] == "1" && $Row[$name] == "5") ? "" : " disabled";
            $name = "TESTSUBCLASSCD".$codeArray["TESTDIV"];
            $extra = array();
            $opt = array();
            $dataCnt = 0;
            foreach ($subcdArray as $keySUB => $codeSUB) {
                $opt[] = $codeSUB["VALUE"];
                $dataCnt++;
                $nameID = $name.$dataCnt;
                $extra[] = "id=\"{$nameID}\"".$disSubcd;
                $arg["data"][$nameID."_ID"] = $nameID;
                $arg["data"][$nameID."_LABEL"] = $codeSUB["LABEL"];
            }
            $radioArray = knjCreateRadio($objForm, $name, $Row[$name], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key2 => $val2) {
                $arg["data"][$key2] = $val2;
            }
        }

        //受験番号(RECEPTNO)
        foreach ($model->testdivArray as $key => $codeArray) {
            $name = "RECEPTNO".$codeArray["TESTDIV"];
            $extra = " STYLE=\"ime-mode: inactive;\" onChange=\"change_flg();\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
            $arg["data"][$name] = knjCreateTextBox($objForm, $Row[$name], $name, 4, 4, $extra);
        }





        //受験料
        //入金方法ラジオ 1:振込 2:窓口
        $opt = array(1,2);
        $extra = array("id=\"EXAM_PAY_DIV1\"", "id=\"EXAM_PAY_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "EXAM_PAY_DIV", $Row["EXAM_PAY_DIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }
        //入金日
        $extra = " STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\"";
        $arg["data"]["EXAM_PAY_DATE"] = View::popUpCalendar2($objForm, "EXAM_PAY_DATE", str_replace("-", "/", $Row["EXAM_PAY_DATE"]), "", "", $extra);
        //着金日
        $extra = " STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\"";
        $arg["data"]["EXAM_PAY_CHAK_DATE"] = View::popUpCalendar2($objForm, "EXAM_PAY_CHAK_DATE", str_replace("-", "/", $Row["EXAM_PAY_CHAK_DATE"]), "", "", $extra);

        //特別入試対象者
        $arg["data"]["TOKU_TEST"] = $arg["KIKOKU_FLG2"] == "1" && strlen($Row["TOKU_TEST_FLG"]) ? "特別入試対象者" : "";
        knjCreateHidden($objForm, "TOKU_TEST_FLG", $Row["TOKU_TEST_FLG"]);

        //事前番号
        $extra = " STYLE=\"ime-mode: inactive;\" onChange=\"change_flg();\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["RECRUIT_NO"] = knjCreateTextBox($objForm, $Row["RECRUIT_NO"], "RECRUIT_NO", 8, 8, $extra);

        //受付日付
        $extra = " STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\"";
        $arg["data"]["RECEPTDATE"] = View::popUpCalendar2($objForm, "RECEPTDATE", str_replace("-", "/", $Row["RECEPTDATE"]), "", "", $extra);

        //専併区分コンボ
        $query = knjl010fQuery::getNameCd($model->year, "L006");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SHDIV"], "SHDIV", $extra, 1, "BLANK");

        //志望区分コンボ
        $query = knjl010fQuery::getExamcourse($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"]);
        $extra = "onChange=\"change_flg(); btn_submit('changeTest', '', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["EXAMCOURSE"], "EXAMCOURSE", $extra, 1, "");

        //特別措置者(インフルエンザ)
        $extra  = "onChange=\"change_flg()\" id=\"SPECIAL_REASON_DIV\" ";
        $extra .= strlen($Row["SPECIAL_REASON_DIV"]) ? "checked" : "";
        $arg["data"]["SPECIAL_REASON_DIV"] = knjCreateCheckBox($objForm, "SPECIAL_REASON_DIV", "1", $extra);

        //願書郵送チェックボックス
        $extra  = "onChange=\"change_flg()\" id=\"GANSHO_YUUSOU\" ";
        $extra .= strlen($Row["GANSHO_YUUSOU"]) ? "checked" : "";
        $arg["data"]["GANSHO_YUUSOU"] = knjCreateCheckBox($objForm, "GANSHO_YUUSOU", "1", $extra);

        //氏名(志願者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //氏名かな(志願者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //生年月日（西暦）
        $extra = " STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\"";
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar2($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), "", "", $extra);

        global $sess;
        //郵便番号入力支援(志願者)
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"isZipcd(this), toCopytxt(0, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ZIPCD"] = knjCreateTextBox($objForm, $Row["ZIPCD"], "ZIPCD", 10, "", $extra);

        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);

        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS1&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所(志願者)
        $extra = " STYLE=\"ime-mode: active;\" onblur=\"toCopytxt(1, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ADDRESS1"] = knjCreateTextBox($objForm, $Row["ADDRESS1"], "ADDRESS1", 60, 60, $extra);

        //方書(志願者)
        $extra = " STYLE=\"ime-mode: active;\" onblur=\"toCopytxt(2, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ADDRESS2"] = knjCreateTextBox($objForm, $Row["ADDRESS2"], "ADDRESS2", 60, 60, $extra);

        //電話番号(志願者)
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toTelNo(this.value), toCopytxt(3, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 14, 14, $extra);

        //出身学校コード
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 7, 7, $extra);

        //学校名
        $query = knjl010fQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_DISTCD_NAME"].$fsArray["FINSCHOOL_NAME"].$fsArray["FINSCHOOL_TYPE_NAME"];

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);
        /***
                //卒業年月日（西暦）
                $extra = " onchange=\"change_flg()\"";
                $arg["data"]["FS_DAY"] = View::popUpCalendar2($objForm, "FS_DAY", str_replace("-", "/", $Row["FS_DAY"]), "", "", $extra);
        ***/

        //塾コード
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["PRISCHOOLCD"] = knjCreateTextBox($objForm, $Row["PRISCHOOLCD"], "PRISCHOOLCD", 7, 7, $extra);

        //教室コード
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["PRISCHOOL_CLASS_CD"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_CLASS_CD"], "PRISCHOOL_CLASS_CD", 7, 7, $extra);

        //塾
        $query = knjl010fQuery::getFinschoolName($Row["PRI_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["PRISCHOOL_NAME"] = $fsArray["PRISCHOOL_NAME"];

        //かな検索ボタン（塾）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_PRISCHOOL/knjwpri_searchindex.php?cmd=&pricdname=&priname=&priaddr=&prischool_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_pri_kana_reference"] = knjCreateBtn($objForm, "btn_pri_kana_reference", "検 索", $extra);

        $defGrdEraY = '';
        $gengouCd = "";
        $gengouName = "";
        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl010fQuery::getCalendarno($model->year));
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

            $setDefGrdDate = $model->year.'/03/01';
            if ($row['NAMESPARE2'] < $setDefGrdDate && $setDefGrdDate < $row['NAMESPARE3']) {
                $defGrdEraY = $model->year - $row['NAMESPARE1'] + 1;
                $gengouCd = $row["NAMECD2"];
                $gengouName = $row["NAME1"];
            }
        }
        knjCreateHidden($objForm, "ERCD_Y", $calno.':'.$spare);

        //卒業元号
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(0, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_ERACD"] = knjCreateTextBox($objForm, strlen($Row["FS_ERACD"]) ? $Row["FS_ERACD"] : $gengouCd, "FS_ERACD", 1, 1, $extra);

        //卒業和暦名
        $fs_wname = isset($Row["FS_WNAME"]) ? str_replace("&nbsp;", "", $Row["FS_WNAME"]) : $gengouName;
        knjCreateHidden($objForm, "FS_WNAME", $fs_wname);
        $arg["data"]["FS_WNAME"] = $fs_wname;

        //卒業年
        $Row["FS_Y"] = (strlen($Row["FS_Y"])) ? $Row["FS_Y"]: $defGrdEraY;
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_Y"] = knjCreateTextBox($objForm, $Row["FS_Y"], "FS_Y", 2, 2, $extra);

        //卒業月
        $defGrdmon = $model->cmd == 'showdivAdd' ? "03" : "";
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_M"] = knjCreateTextBox($objForm, strlen($Row["FS_M"]) ? $Row["FS_M"] : '03', "FS_M", 2, 2, $extra);

        //西暦表示
        $setInnerFsYM = $model->year.'.03';
        $arg["data"]["FS_Y_M_INNER"] = $setInnerFsYM;
        knjCreateHidden($objForm, "FS_Y_M_INNER", $setInnerFsYM);

        //卒業区分（1:見込み,2:卒業）
        $defGrddiv = $model->cmd == 'showdivAdd' ? "1" : "";
        $Row["FS_GRDDIV"] = strlen($Row["FS_GRDDIV"]) ? $Row["FS_GRDDIV"] : '1';
        $query = knjl010fQuery::getNameCd($model->year, "L016");
        $extra = "onChange=\"change_flg(); btn_submit('changeTest', '', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["FS_GRDDIV"], "FS_GRDDIV", $extra, 1, "BLANK");

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg()\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 60, $extra);

        //続柄コンボ
        $query = knjl010fQuery::getNameCd($model->year, "H201");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["RELATIONSHIP"], "RELATIONSHIP", $extra, 1, "BLANK");

        //氏名かな(保護者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 40, 120, $extra);

        //郵便番号入力支援(保護者)
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"isZipcd(this), toCopytxt(0, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GZIPCD"] = knjCreateTextBox($objForm, $Row["GZIPCD"], "GZIPCD", 10, "", $extra);

        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=GADDRESS1&zipname=GZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);

        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=GADDRESS1&zipname=GZIPCD&zip='+document.forms[0]['GZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所(保護者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS1"] = knjCreateTextBox($objForm, $Row["GADDRESS1"], "GADDRESS1", 60, 60, $extra);

        //方書(保護者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS2"] = knjCreateTextBox($objForm, $Row["GADDRESS2"], "GADDRESS2", 60, 60, $extra);

        //電話番号(保護者)
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GTELNO"] = knjCreateTextBox($objForm, $Row["GTELNO"], "GTELNO", 14, 14, $extra);



        /***
                //入学志望動機
                $extra = "onChange=\"change_flg();\"";
                $arg["data"]["NYUUGAKU_SIBOU_DOUKI"] = knjCreateTextBox($objForm, $Row["NYUUGAKU_SIBOU_DOUKI"], "NYUUGAKU_SIBOU_DOUKI", 80, 120, $extra);
        ***/

        //校友会
        //氏名
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\"";
        $arg["data"]["MOTHER_NAME"] = knjCreateTextBox($objForm, $Row["MOTHER_NAME"], "MOTHER_NAME", 40, 60, $extra);
        //卒業年度
        $extra = " STYLE=\"ime-mode: inactive;\" onChange=\"change_flg();\"";
        $arg["data"]["MOTHER_NENDO"] = knjCreateTextBox($objForm, $Row["MOTHER_NENDO"], "MOTHER_NENDO", 4, 4, $extra);
        //年組コンボ
        $query = knjl010fQuery::getMotherHrName();
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["MOTHER_HR_CLASS"], "MOTHER_HR_CLASS", $extra, 1, "BLANK");
        //生年月日
        $extra = " STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\"";
        $arg["data"]["MOTHER_BIRTHDAY"] = View::popUpCalendar2($objForm, "MOTHER_BIRTHDAY", str_replace("-", "/", $Row["MOTHER_BIRTHDAY"]), "", "", $extra);
        //続柄コンボ
        $query = knjl010fQuery::getNameCd($model->year, "L054");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["KOUYUU_DIV"], "KOUYUU_DIV", $extra, 1, "BLANK");
        //中高コンボ
        $query = knjl010fQuery::getJHName();
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["KOUYUU_SCHOOL_KIND"], "KOUYUU_SCHOOL_KIND", $extra, 1, "BLANK");
        //大学・学部
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\"";
        $arg["data"]["KOUYUU_COLLEGE"] = knjCreateTextBox($objForm, $Row["KOUYUU_COLLEGE"], "KOUYUU_COLLEGE", 40, 60, $extra);

        //英検取得級コンボ
        $query = knjl010fQuery::getNameCd($model->year, "L055");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["EIKEN_SHUTOKU_KYUU"], "EIKEN_SHUTOKU_KYUU", $extra, 1, "BLANK");



        //------------------------------欠席-------------------------------------
        //欠席登録
        $link = REQUESTROOT."/L/KNJL023N/knjl023nindex.php?cmd=&SEND_PRGID=KNJL010F&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "$disabled onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_kesseki"] = knjCreateBtn($objForm, "btn_kesseki", "欠席登録", $extra);
        $arg["data"]["JUDGEMENT_INFO"] = $Row["JUDGEMENT_INFO"];

        //------------------------------内申-------------------------------------
        //調査書登録
        $link = REQUESTROOT."/L/KNJL021N/knjl021nindex.php?cmd=&SEND_PRGID=KNJL010F&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "$disabled onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_chousasho"] = knjCreateBtn($objForm, "btn_chousasho", "調査書登録", $extra);
        
        //各項目の教科名称取得
        $query = knjl010fQuery::getNameCd($model->year, "L008");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["ABBV1_".$row["VALUE"]] = $row["ABBV1"];
        }
        
        //各項目をセット
        for ($i = 1; $i <= 10; $i++) {
            $num = sprintf("%02d", $i);
            $arg["data"]["CONFIDENTIAL_RPT".$num]     = $Row["CONFIDENTIAL_RPT".$num];
            knjCreateHidden($objForm, "CONFIDENTIAL_RPT".$num, $Row["CONFIDENTIAL_RPT".$num]);
        }
        $arg["data"]["TOTAL_ALL"]     = $Row["TOTAL_ALL"];
        $arg["data"]["TOTAL5"]        = $Row["TOTAL5"];
        $arg["data"]["KASANTEN_ALL"]  = $Row["KASANTEN_ALL"]; //段階
        $arg["data"]["ABSENCE_DAYS"]  = $Row["ABSENCE_DAYS"];
        $arg["data"]["ABSENCE_DAYS2"] = $Row["ABSENCE_DAYS2"];
        $arg["data"]["ABSENCE_DAYS3"] = $Row["ABSENCE_DAYS3"];
        knjCreateHidden($objForm, "TOTAL_ALL", $Row["TOTAL_ALL"]);
        knjCreateHidden($objForm, "TOTAL5", $Row["TOTAL5"]);
        knjCreateHidden($objForm, "KASANTEN_ALL", $Row["KASANTEN_ALL"]);
        knjCreateHidden($objForm, "ABSENCE_DAYS", $Row["ABSENCE_DAYS"]);
        knjCreateHidden($objForm, "ABSENCE_DAYS2", $Row["ABSENCE_DAYS2"]);
        knjCreateHidden($objForm, "ABSENCE_DAYS3", $Row["ABSENCE_DAYS3"]);
        //調査書画面にある他の項目
        knjCreateHidden($objForm, "ABSENCE_REMARK", $Row["ABSENCE_REMARK"]);
        knjCreateHidden($objForm, "ABSENCE_REMARK2", $Row["ABSENCE_REMARK2"]);
        knjCreateHidden($objForm, "ABSENCE_REMARK3", $Row["ABSENCE_REMARK3"]);
        knjCreateHidden($objForm, "CONFRPT_REMARK1", $Row["CONFRPT_REMARK1"]);
        knjCreateHidden($objForm, "DETAIL4_REMARK1", $Row["DETAIL4_REMARK1"]);

        //-------------------------------- ボタン作成 ------------------------------------
        $zip = $Row["ZIPCD"];
        $gzip = $Row["GZIPCD"];
        $zadd = $Row["ADDRESS1"];
        $gadd = $Row["GADDRESS1"];

        //検索ボタン
        $extra = "onclick=\"return btn_submit('reference', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL010F/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
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

        //備考
        $extra = "";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 40, 40, $extra);
        $arg["data"]["REMARK2"] = knjCreateTextBox($objForm, $Row["REMARK2"], "REMARK2", 50, 50, $extra);
        $arg["data"]["REMARK3"] = knjCreateTextBox($objForm, $Row["REMARK3"], "REMARK3", 50, 50, $extra);
        $arg["data"]["REMARK4"] = knjCreateTextBox($objForm, $Row["REMARK4"], "REMARK4", 50, 50, $extra);
        $arg["data"]["REMARK5"] = knjCreateTextBox($objForm, $Row["REMARK5"], "REMARK5", 40, 40, $extra);
        $arg["data"]["REMARK6"] = knjCreateTextBox($objForm, $Row["REMARK6"], "REMARK6", 50, 50, $extra);

        //奨学生コンボ 備考1,備考5
        $query = knjl010fQuery::getNameCd($model->year, "L025");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SHOUGAKU1"], "SHOUGAKU1", $extra, 1, "BLANK");
        makeCmb($objForm, $arg, $db, $query, $Row["SHOUGAKU5"], "SHOUGAKU5", $extra, 1, "BLANK");

        //教育相談コンボ
        $query = knjl010fQuery::getNameCd($model->year, "L026");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SOUDAN"], "SOUDAN", $extra, 1, "BLANK");

        //備考1→備考5コピーボタン
        $extra = "$disabled style=\"width:135px\" onclick=\"return btn_submit('copy_shougaku');\"";
        $arg["button"]["btn_copy_shougaku"] = knjCreateBtn($objForm, "btn_copy_shougaku", "備考1→備考5コピー", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl010fForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($name == 'APPLICANTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') {
                $value = $row["VALUE"];
            }
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
