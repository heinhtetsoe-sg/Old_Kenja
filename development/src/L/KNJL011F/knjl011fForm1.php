<?php

require_once('for_php7.php');

class knjl011fForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("main", "POST", "knjl011findex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata)) && $model->cmd != 'addnew') {
            //データを取得
            $Row = knjl011fQuery::getEditData($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303", "更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl011fQuery::getEditData($model);
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
            $Row["TESTDIV"] = $model->field["TESTDIV"];
            $Row["SHDIV"] = $model->field["SHDIV"];
            $Row["EXAMCOURSE"] = $model->field["EXAMCOURSE"];
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
        $query = knjl011fQuery::getNameCd($model->year, "L003", "2");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //受験番号
        $extra = " STYLE=\"ime-mode: inactive;\" onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 4, 4, $extra);

        //入試区分
        $query = knjl011fQuery::getNameCd($model->year, "L004");
        $extra = "onChange=\"change_flg(); btn_submit('changeTest', '', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1, "BLANK");

        //入試回数(2:高校のみ)
        $query = knjl011fQuery::getTestdiv0($model->year, $Row["TESTDIV"]);
        $extra = "onChange=\"change_flg(); btn_submit('changeTest', '', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV0"], "TESTDIV0", $extra, 1, "BLANK");

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

        //学力診断テスト対象者
        $arg["data"]["GAKU_TEST"] = strlen($Row["GAKU_TEST_FLG"]) ? "学力診断テスト対象者" : "";
        knjCreateHidden($objForm, "GAKU_TEST_FLG", $Row["GAKU_TEST_FLG"]);

        //事前番号
        $extra = " STYLE=\"ime-mode: inactive;\" onChange=\"change_flg();\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["RECRUIT_NO"] = knjCreateTextBox($objForm, $Row["RECRUIT_NO"], "RECRUIT_NO", 8, 8, $extra);

        //受付日付
        $extra = " STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\"";
        $arg["data"]["RECEPTDATE"] = View::popUpCalendar2($objForm, "RECEPTDATE", str_replace("-", "/", $Row["RECEPTDATE"]), "", "", $extra);

        //志望区分コンボ
        $query = knjl011fQuery::getExamcourse($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"]);
        $extra = "onChange=\"change_flg(); btn_submit('changeTest', '', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["EXAMCOURSE"], "EXAMCOURSE", $extra, 1, "BLANK");

        //コースコードを取得
        $course_array = array();
        if (strlen($Row["EXAMCOURSE"])) {
            $course_array = explode("-", $Row["EXAMCOURSE"]);
        } else {
            $course_array[0] = "";
            $course_array[1] = "";
            $course_array[2] = "";
        }


        //専併区分コンボ
        $query = knjl011fQuery::getNameCd($model->year, "L006");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SHDIV"], "SHDIV", $extra, 1, "");

        //入学手続延期願提出（1:提出する）
        $checkShift = ($Row["SHIFT_DESIRE_FLG"] == "1") ? " checked" : "";
        $extra = "onChange=\"change_flg()\" id=\"SHIFT_DESIRE_FLG\" " .$checkShift;
        $arg["data"]["SHIFT_DESIRE_FLG"] = knjCreateCheckBox($objForm, "SHIFT_DESIRE_FLG", "1", $extra);

        //併願校名
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\"";
        $arg["data"]["SH_SCHOOLNAME"] = knjCreateTextBox($objForm, $Row["SH_SCHOOLNAME"], "SH_SCHOOLNAME", 40, 60, $extra);

        //併願校合格発表日
        $extra = " STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\"";
        $arg["data"]["SH_JUDGEMENT_DATE"] = View::popUpCalendar2($objForm, "SH_JUDGEMENT_DATE", str_replace("-", "/", $Row["SH_JUDGEMENT_DATE"]), "", "", $extra);

        //アドバンストへの変更（1:希望する）　※理数キャリア、国際教養のスタンダード志望の方
        $disSlide = ($course_array[2] == "1002" || $course_array[2] == "2002") ? "" : " disabled";
        $checkSlide = ($Row["SLIDE_FLG"] == "1") ? " checked" : "";
        $extra = "onChange=\"change_flg()\" id=\"SLIDE_FLG\" " .$checkSlide.$disSlide;
        $arg["data"]["SLIDE_FLG"] = knjCreateCheckBox($objForm, "SLIDE_FLG", "1", $extra);

        //受験科目コンボ　※一般特別受験の方、スポーツ科学のＡ推薦・推薦または帰国生Ａ方式受験の方
        $disSelSub = ($Row["TESTDIV"] == "7" || $course_array[2] == "3001" && ($Row["TESTDIV"] == "1" || $Row["TESTDIV"] == "4")) ? "" : " disabled";
        $query = knjl011fQuery::getSelectSubclassDiv($model->year, "L009");
        $extra = "onChange=\"change_flg()\"".$disSelSub;
        makeCmb($objForm, $arg, $db, $query, $Row["SELECT_SUBCLASS_DIV"], "SELECT_SUBCLASS_DIV", $extra, 1, "BLANK");


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

        //性別コンボ
        $query = knjl011fQuery::getNameCd($model->year, "Z002");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "BLANK");

        /***/
        //生年月日（西暦）
        $extra = " STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\"";
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar2($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), "", "", $extra);
        /***/

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
        $query = knjl011fQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_NAME"].$fsArray["FINSCHOOL_TYPE_NAME"];

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);
        /***
                //卒業年月日（西暦）
                $extra = " onchange=\"change_flg()\"";
                $arg["data"]["FS_DAY"] = View::popUpCalendar2($objForm, "FS_DAY", str_replace("-", "/", $Row["FS_DAY"]), "", "", $extra);
        ***/

        $defGrdEraY = '';
        $gengouCd = "";
        $gengouName = "";
        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl011fQuery::getCalendarno($model->year));
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
        $query = knjl011fQuery::getNameCd($model->year, "L016");
        $extra = "onChange=\"change_flg(); btn_submit('changeTest', '', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["FS_GRDDIV"], "FS_GRDDIV", $extra, 1, "BLANK");

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg()\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 60, $extra);

        //続柄コンボ
        $query = knjl011fQuery::getNameCd($model->year, "H201");
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



        //校友会
        //氏名
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\"";
        $arg["data"]["MOTHER_NAME"] = knjCreateTextBox($objForm, $Row["MOTHER_NAME"], "MOTHER_NAME", 40, 60, $extra);
        //卒業年度
        $extra = " STYLE=\"ime-mode: inactive;\" onChange=\"change_flg();\"";
        $arg["data"]["MOTHER_NENDO"] = knjCreateTextBox($objForm, $Row["MOTHER_NENDO"], "MOTHER_NENDO", 4, 4, $extra);
        //年組コンボ
        $query = knjl011fQuery::getMotherHrName();
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["MOTHER_HR_CLASS"], "MOTHER_HR_CLASS", $extra, 1, "BLANK");
        //生年月日
        $extra = " STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\"";
        $arg["data"]["MOTHER_BIRTHDAY"] = View::popUpCalendar2($objForm, "MOTHER_BIRTHDAY", str_replace("-", "/", $Row["MOTHER_BIRTHDAY"]), "", "", $extra);
        //続柄コンボ
        $query = knjl011fQuery::getNameCd($model->year, "L054");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["KOUYUU_DIV"], "KOUYUU_DIV", $extra, 1, "BLANK");
        //中高コンボ
        $query = knjl011fQuery::getJHName();
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["KOUYUU_SCHOOL_KIND"], "KOUYUU_SCHOOL_KIND", $extra, 1, "BLANK");
        //大学・学部
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\"";
        $arg["data"]["KOUYUU_COLLEGE"] = knjCreateTextBox($objForm, $Row["KOUYUU_COLLEGE"], "KOUYUU_COLLEGE", 40, 60, $extra);



        //------------------------------欠席-------------------------------------
        //欠席登録
        $link = REQUESTROOT."/L/KNJL023F/knjl023findex.php?cmd=&SEND_PRGID=KNJL011F&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "$disabled onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_kesseki"] = knjCreateBtn($objForm, "btn_kesseki", "欠席登録", $extra);
        $arg["data"]["JUDGEMENT_INFO"] = $Row["JUDGEMENT_INFO"];

        //------------------------------内申-------------------------------------
        //調査書登録
        $link = REQUESTROOT."/L/KNJL021F/knjl021findex.php?cmd=&SEND_PRGID=KNJL011F&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "$disabled onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_chousasho"] = knjCreateBtn($objForm, "btn_chousasho", "調査書登録", $extra);
        
        //各項目の教科名称取得
        $query = knjl011fQuery::getNameCd($model->year, "L008");
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
        $arg["data"]["TOTAL3"]        = $Row["TOTAL3"];
        $arg["data"]["TOTAL5"]        = $Row["TOTAL5"];
        $arg["data"]["TOTAL_ALL"]     = $Row["TOTAL_ALL"];
        $arg["data"]["KASANTEN_ALL"]  = $Row["KASANTEN_ALL"]; //段階
        $arg["data"]["ABSENCE_DAYS"]  = $Row["ABSENCE_DAYS"];
        $arg["data"]["ABSENCE_DAYS2"] = $Row["ABSENCE_DAYS2"];
        $arg["data"]["ABSENCE_DAYS3"] = $Row["ABSENCE_DAYS3"];
        knjCreateHidden($objForm, "TOTAL3", $Row["TOTAL3"]);
        knjCreateHidden($objForm, "TOTAL5", $Row["TOTAL5"]);
        knjCreateHidden($objForm, "TOTAL_ALL", $Row["TOTAL_ALL"]);
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
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011F/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
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
        $query = knjl011fQuery::getNameCd($model->year, "L025");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SHOUGAKU1"], "SHOUGAKU1", $extra, 1, "BLANK");
        makeCmb($objForm, $arg, $db, $query, $Row["SHOUGAKU5"], "SHOUGAKU5", $extra, 1, "BLANK");

        //教育相談コンボ
        $query = knjl011fQuery::getNameCd($model->year, "L026");
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
        View::toHTML($model, "knjl011fForm1.html", $arg);
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
        if ($name == 'APPLICANTDIV' || $name == 'TESTDIV') {
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
