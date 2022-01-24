<?php

require_once('for_php7.php');

class knjl011wForm1
{
    public function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl011windex.php", "", "main");

        //権限チェック
        $adminFlg = knjl011wQuery::getAdminFlg();
        if (AUTHORITY != DEF_UPDATABLE || $adminFlg != "1") {
            $arg["jscript"] = "OnAuthError();";
        }

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata)) && $model->cmd != 'addnew') {
            //データを取得
            $Row = knjl011wQuery::getEditData($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303", "更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl011wQuery::getEditData($model);
                }
                $model->recomExamno = $Row["RECOM_EXAMNO"];
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
            if ($model->cmd != 'changeTest' && $model->cmd != 'backFinCopy' && $model->cmd != 'changeCourse') {
                $model->defVal = $Row;
            }
        } else {
            $Row =& $model->field;
        }

        $arg["TOP"]["YEAR"] = $model->year;
        if ($model->cmd == 'showdivAdd') {
            $Row["APPLICANTDIV"] = $model->field["APPLICANTDIV"];
            $Row["TESTDIV"] = $model->field["TESTDIV"];
            $Row["RECEPTDATE"] = $model->field["RECEPTDATE"];
        }
        if ($model->cmd == 'changeTest' || $model->cmd == 'backFinCopy' || $model->cmd == 'changeCourse') {
            $Row =& $model->field;
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //DB接続
        $db = Query::dbCheckOut();

        //入試制度コンボ
        $query = knjl011wQuery::getNameCd($model->year, "L003");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //受検番号
        $extra = "onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["RECOM_EXAMNO"] = knjCreateTextBox($objForm, $model->recomExamno, "RECOM_EXAMNO", 5, 5, $extra);

        //受付番号
        $extra = "onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 5, 5, $extra);

        //受付日付
        $extra = " onchange=\"change_flg()\"";
        $arg["data"]["RECEPTDATE"] = View::popUpCalendar2($objForm, "RECEPTDATE", str_replace("-", "/", $Row["RECEPTDATE"]), "", "", $extra);

        //入試区分
        $query = knjl011wQuery::getNameCd($model->year, "L004");
        $extra = "onChange=\"change_flg(); btn_submit('changeTest', '', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1, "BLANK");

        //募集競技
        $disSport = ($Row["TESTDIV"] == "4") ? "" : " disabled";
        $query = knjl011wQuery::getSportCd($model->year);
        $extra = "onChange=\"change_flg(); \"" .$disSport;
        makeCmb($objForm, $arg, $db, $query, $Row["SPORT_CD"], "SPORT_CD", $extra, 1, "BLANK");

        //第１志望コンボ
        $query = knjl011wQuery::getExamcourse($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"], $Row["EXAMCOURSE_HENKOU"]);
        $extra = "onChange=\"change_flg();\"";
        makeCmb($objForm, $arg, $db, $query, $Row["EXAMCOURSE"], "EXAMCOURSE", $extra, 1, "BLANK");

        //第２志望コンボ
        $query = knjl011wQuery::getExamcourse($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"], $Row["EXAMCOURSE_HENKOU2"]);
        $extra = "onChange=\"change_flg();\"";
        makeCmb($objForm, $arg, $db, $query, $Row["EXAMCOURSE2"], "EXAMCOURSE2", $extra, 1, "BLANK");

        //第１志望コンボ(願書変更)
        $query = knjl011wQuery::getExamcourse($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"]);
        $extra = "onChange=\"change_flg(); btn_submit('changeCourse');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["EXAMCOURSE_HENKOU"], "EXAMCOURSE_HENKOU", $extra, 1, "BLANK");

        //第２志望コンボ(願書変更)
        $query = knjl011wQuery::getExamcourse($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"]);
        $extra = "onChange=\"change_flg(); btn_submit('changeCourse');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["EXAMCOURSE_HENKOU2"], "EXAMCOURSE_HENKOU2", $extra, 1, "BLANK");

        //追検査
        $extra  = "onChange=\"change_flg()\" id=\"TESTDIV2\" ";
        $extra .= strlen($Row["TESTDIV2"]) ? "checked" : "";
        $arg["data"]["TESTDIV2"] = knjCreateCheckBox($objForm, "TESTDIV2", "1", $extra);

        //覚書志願者
        $extra  = "onChange=\"change_flg()\" id=\"OBOEGAKI_SIGANSYA\" ";
        $extra .= strlen($Row["OBOEGAKI_SIGANSYA"]) ? "checked" : "";
        $arg["data"]["OBOEGAKI_SIGANSYA"] = knjCreateCheckBox($objForm, "OBOEGAKI_SIGANSYA", "1", $extra);
        //海外帰国生等(様式16)
        $extra  = "onChange=\"change_flg()\" id=\"KAIGAI_KIKOKUSEI_NADO\" ";
        $extra .= strlen($Row["KAIGAI_KIKOKUSEI_NADO"]) ? "checked" : "";
        $arg["data"]["KAIGAI_KIKOKUSEI_NADO"] = knjCreateCheckBox($objForm, "KAIGAI_KIKOKUSEI_NADO", "1", $extra);
        //通学区域外許可(様式12)
        $extra  = "onChange=\"change_flg()\" id=\"TUUGAKU_KUIKIGAI_KYOKA\" ";
        $extra .= strlen($Row["TUUGAKU_KUIKIGAI_KYOKA"]) ? "checked" : "";
        $arg["data"]["TUUGAKU_KUIKIGAI_KYOKA"] = knjCreateCheckBox($objForm, "TUUGAKU_KUIKIGAI_KYOKA", "1", $extra);

        //保証人届(様式15)
        $extra  = "onChange=\"change_flg()\" id=\"HOSHOUNIN_TODOKE\" ";
        $extra .= strlen($Row["HOSHOUNIN_TODOKE"]) ? "checked" : "";
        $arg["data"]["HOSHOUNIN_TODOKE"] = knjCreateCheckBox($objForm, "HOSHOUNIN_TODOKE", "1", $extra);
        //県外在住(様式13-1又は様式13-2)
        $extra  = "onChange=\"change_flg()\" id=\"KENGAI_ZAIJUU\" ";
        $extra .= strlen($Row["KENGAI_ZAIJUU"]) ? "checked" : "";
        $arg["data"]["KENGAI_ZAIJUU"] = knjCreateCheckBox($objForm, "KENGAI_ZAIJUU", "1", $extra);
        //県外中学校出身(様式14ア)
        $extra  = "onChange=\"change_flg()\" id=\"KENGAI_CHUUGAKKOU_SHUSSHIN\" ";
        $extra .= strlen($Row["KENGAI_CHUUGAKKOU_SHUSSHIN"]) ? "checked" : "";
        $arg["data"]["KENGAI_CHUUGAKKOU_SHUSSHIN"] = knjCreateCheckBox($objForm, "KENGAI_CHUUGAKKOU_SHUSSHIN", "1", $extra);
        //県外中学校出身(様式14イ)
        $extra  = "onChange=\"change_flg()\" id=\"KENGAI_CHUUGAKKOU_SHUSSHIN2\" ";
        $extra .= strlen($Row["KENGAI_CHUUGAKKOU_SHUSSHIN2"]) ? "checked" : "";
        $arg["data"]["KENGAI_CHUUGAKKOU_SHUSSHIN2"] = knjCreateCheckBox($objForm, "KENGAI_CHUUGAKKOU_SHUSSHIN2", "1", $extra);

        //------------------------------志願者情報-------------------------------------

        //氏名(志願者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 120, $extra);

        //氏名かな(志願者)
        $extra = "onChange=\"change_flg();\" id=\"NAME_KANA\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 240, $extra);

        //生年月日（西暦）
        //$extra = " onchange=\"change_flg()\"";
        //$arg["data"]["BIRTHDAY"] = View::popUpCalendar2($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), "", "", $extra);

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

        //前の志願者の出身学校をコピー
        if ($model->cmd == 'backFinCopy') {
            $query = knjl011wQuery::getBackFinCopy($model);
            $backFin = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["FS_CD"] = $backFin["FS_CD"];
        }

        //出身学校コード
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 7, 7, $extra);

        //学校名
        $query = knjl011wQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["PREF_NAME"] . "　" . $fsArray["FINSCHOOL_NAME"];

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        $gengouCd = "";
        $gengouName = "";
        $gannen = "";
        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl011wQuery::getCalendarno($model->year));
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
                $gannen = $row["NAMESPARE1"];
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
        $defGrdY = (int)$model->year + 1 - $gannen;
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_Y"] = knjCreateTextBox($objForm, strlen($Row["FS_Y"]) ? $Row["FS_Y"] : $defGrdY, "FS_Y", 2, 2, $extra);

        //卒業月
        $defGrdmon = $model->cmd == 'showdivAdd' ? "03" : "";
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_M"] = knjCreateTextBox($objForm, strlen($Row["FS_M"]) ? $Row["FS_M"] : $defGrdmon, "FS_M", 2, 2, $extra);

        //卒業区分（1:見込み,2:卒業）
        $defGrddiv = $model->cmd == 'showdivAdd' ? "1" : "";
        $Row["FS_GRDDIV"] = strlen($Row["FS_GRDDIV"]) ? $Row["FS_GRDDIV"] : $defGrddiv;
        $query = knjl011wQuery::getNameCd($model->year, "L016");
        $extra = "onChange=\"change_flg(); \"";
        makeCmb($objForm, $arg, $db, $query, $Row["FS_GRDDIV"], "FS_GRDDIV", $extra, 1, "BLANK");

        //生年月日元号
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(0, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ERACD"] = knjCreateTextBox($objForm, strlen($Row["ERACD"]) ? $Row["ERACD"] : "4", "ERACD", 1, 1, $extra);

        //生年月日和暦名
        $wname = isset($Row["WNAME"]) ? str_replace("&nbsp;", "", $Row["WNAME"]) : "平成";
        knjCreateHidden($objForm, "WNAME", $wname);
        $arg["data"]["WNAME"] = $wname;

        //生年月日年
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["BIRTH_Y"] = knjCreateTextBox($objForm, $Row["BIRTH_Y"], "BIRTH_Y", 2, 2, $extra);

        //生年月日月
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["BIRTH_M"] = knjCreateTextBox($objForm, $Row["BIRTH_M"], "BIRTH_M", 2, 2, $extra);

        //生年月日日
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(3, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["BIRTH_D"] = knjCreateTextBox($objForm, $Row["BIRTH_D"], "BIRTH_D", 2, 2, $extra);

        //年齢(基準年)・・・とりあえず入試年度を参照
        $age_base_y = (int)$model->year + 1 - $gannen;
        $arg["data"]["AGE_COMMENT"] = "{$gengouName}{$age_base_y}年4月1日時点";

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $extra = "onChange=\"change_flg()\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 120, $extra);

        //続柄コンボ
        $query = knjl011wQuery::getNameCd($model->year, "H201");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["RELATIONSHIP"], "RELATIONSHIP", $extra, 1, "BLANK");

        //氏名かな(保護者)
        $extra = "onChange=\"change_flg();\" id=\"GKANA\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 40, 240, $extra);

        //郵便番号入力支援(保護者)
        $arg["data"]["GZIPCD"] = View::popUpZipCode($objForm, "GZIPCD", $Row["GZIPCD"], "GADDRESS1");

        //住所(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS1"] = knjCreateTextBox($objForm, $Row["GADDRESS1"], "GADDRESS1", 60, 60, $extra);

        //方書(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS2"] = knjCreateTextBox($objForm, $Row["GADDRESS2"], "GADDRESS2", 60, 60, $extra);

        //志願者の住所と同じ
        $arg["data"]["ADDRESS_SAME"] = (strlen($Row["GADDRESS1"]) && $Row["ADDRESS1"] === $Row["GADDRESS1"]) ? "志願者の住所と同じ" : "";

        //電話番号(保護者)
        $extra = "onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GTELNO"] = knjCreateTextBox($objForm, $Row["GTELNO"], "GTELNO", 14, 14, $extra);

        //------------------------------内申科目---------------------------------

        $result = $db->query(knjl011wQuery::getNameCd($model->year, "L008"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["CONFIDENTIAL".$row["VALUE"]] = $row["NAME1"];
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
        $setTotal = 0;
        for ($totalCnt = 1; $totalCnt <= 10; $totalCnt++) {
            $setTotal += $Row["CONFIDENTIAL_RPT".sprintf("%02d", $totalCnt)];
        }
        $setTotal = $setTotal > 0 ? $setTotal : "";
        $arg["data"]["TOTAL9"] = $setTotal;
        $arg["data"]["ABSENCE_DAYS1"] = $Row["ABSENCE_DAYS1"];
        $arg["data"]["ABSENCE_DAYS2"] = $Row["ABSENCE_DAYS2"];
        $arg["data"]["ABSENCE_DAYS3"] = $Row["ABSENCE_DAYS3"];

        knjCreateHidden($objForm, "CONFIDENTIAL_RPT01", $Row["CONFIDENTIAL_RPT01"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT02", $Row["CONFIDENTIAL_RPT02"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT03", $Row["CONFIDENTIAL_RPT03"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT04", $Row["CONFIDENTIAL_RPT04"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT05", $Row["CONFIDENTIAL_RPT05"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT06", $Row["CONFIDENTIAL_RPT06"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT07", $Row["CONFIDENTIAL_RPT07"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT08", $Row["CONFIDENTIAL_RPT08"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT09", $Row["CONFIDENTIAL_RPT09"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT10", $Row["CONFIDENTIAL_RPT10"]);
        knjCreateHidden($objForm, "TOTAL9", $setTotal);
        knjCreateHidden($objForm, "ABSENCE_DAYS1", $Row["ABSENCE_DAYS1"]);
        knjCreateHidden($objForm, "ABSENCE_DAYS2", $Row["ABSENCE_DAYS2"]);
        knjCreateHidden($objForm, "ABSENCE_DAYS3", $Row["ABSENCE_DAYS3"]);

        //------------------------------備考-------------------------------------

        //備考１テキスト　全角２０文字
        $extra = "onChange=\"change_flg();\"";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 40, 60, $extra);
        //備考２テキスト　全角２０文字
        $extra = "onChange=\"change_flg();\"";
        $arg["data"]["REMARK2"] = knjCreateTextBox($objForm, $Row["REMARK2"], "REMARK2", 40, 60, $extra);

        //------------------------------欠席-------------------------------------
        //欠席登録
        $link = REQUESTROOT."/L/KNJL023W/knjl023windex.php?cmd=&SEND_PRGID=KNJL011W&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "$disabled onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_kesseki"] = knjCreateBtn($objForm, "btn_kesseki", "欠席・願変登録", $extra);
        $arg["data"]["JUDGEMENT_INFO"] = $Row["JUDGEMENT_INFO"];

        //------------------------------内申-------------------------------------
        //調査書登録
        $link = REQUESTROOT."/L/KNJL021W/knjl021windex.php?cmd=&SEND_PRGID=KNJL011W&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "$disabled onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_chousasho"] = knjCreateBtn($objForm, "btn_chousasho", "調査書登録", $extra);

        //-------------------------------- ボタン作成 ------------------------------------
        $zip = $Row["ZIPCD"];
        $gzip = $Row["GZIPCD"];
        $zadd = $Row["ADDRESS1"];
        $gadd = $Row["GADDRESS1"];

        //検索ボタン
        $extra = "onclick=\"return btn_submit('reference', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011W/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
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

        //前の志願者の出身学校をコピーボタン
        $extra = " style=\"width:235px\" onclick=\"return btn_submit('backFinCopy');\"";
        $arg["button"]["btn_backFinCopy"] = knjCreateBtn($objForm, "btn_backFinCopy", "前の志願者の出身学校をコピー", $extra);

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

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl011wForm1.html", $arg);
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
