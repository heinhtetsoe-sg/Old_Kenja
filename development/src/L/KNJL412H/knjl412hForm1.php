<?php
class knjl412hForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("main", "POST", "knjl412hindex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata)) && $model->cmd != 'changeFscd') {
            //データを取得
            $Row = knjl412hQuery::getEditData($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303", "更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl412hQuery::getEditData($model);
                }
                $model->examno = $Row["EXAMNO"];
                $model->receptno = $Row["RECEPTNO"];
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
        if ($model->cmd == 'changeQualified') {
            $Row =& $model->field;
        }

        if (isset($Row["RECEPTNO"])) {
            $model->checkrecept = $Row["RECEPTNO"];
        }

        //DB接続
        $db = Query::dbCheckOut();

        //ログインユーザーSEQ
        $extra = "disabled";
        $arg["data"]["USERSEQ"] = knjCreateTextBox($objForm, $Row["USERSEQ"], "USERSEQ", 8, 8, $extra);
        knjCreateHidden($objForm, "USERSEQ", $Row["USERSEQ"]);

        //メールアドレス ログイン
        $extra = " STYLE=\"ime-mode: inactive;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["MAIL_LOGIN"] = knjCreateTextBox($objForm, $Row["MAIL_LOGIN"], "MAIL_LOGIN", 20, 20, $extra);

        //メールアドレス 申込時
        $extra = " STYLE=\"ime-mode: inactive;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["MAIL_APP"] = knjCreateTextBox($objForm, $Row["MAIL_APP"], "MAIL_APP", 20, 20, $extra);

        //入試区分コンボ
        $Row["APPLICANTDIV"] = ($Row["APPLICANTDIV"]) ? $Row["APPLICANTDIV"] : $model->field["APPLICANTDIV"];
        $query = knjl412hQuery::getNameCd($model->year, "L003");
        $extra = "onChange=\"change_flg(); btn_submit('changeApplicantdiv', '', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //志願者SEQ
        $extra = " STYLE=\"ime-mode: inactive;\" onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["RECEPTNO"] = knjCreateTextBox($objForm, $model->receptno, "RECEPTNO", 5, 5, $extra);
        $model->examno = $Row["EXAMNO"];
        knjCreateHidden($objForm, "EXAMNO", $model->examno);

        //------------------------------志願者情報-------------------------------------
        //氏名(志願者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //氏名かな(志願者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //宗内生
        $extra  = "onChange=\"change_flg()\" id=\"SEQ005_REAMRK1\" ";
        $extra .= strlen($Row["SEQ005_REAMRK1"]) ? "checked" : "";
        $arg["data"]["SEQ005_REAMRK1"] = knjCreateCheckBox($objForm, "SEQ005_REAMRK1", "1", $extra);

        //生徒会
        $extra  = "onChange=\"change_flg()\" id=\"SEQ005_REAMRK2\" ";
        $extra .= strlen($Row["SEQ005_REAMRK2"]) ? "checked" : "";
        $arg["data"]["SEQ005_REAMRK2"] = knjCreateCheckBox($objForm, "SEQ005_REAMRK2", "1", $extra);

        //資格チェックボックス
        $extra  = "onChange=\"change_flg()\" id=\"SEQ005_REAMRK3\" ";
        $extra .= strlen($Row["SEQ005_REAMRK3"]) ? "checked" : "";
        $arg["data"]["SEQ005_REAMRK3"] = knjCreateCheckBox($objForm, "SEQ005_REAMRK3", "1", $extra);

        //資格名称
        $query = knjl412hQuery::getQualifiedMst();
        $extra = "onChange=\"change_flg(); btn_submit('changeQualified', '', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEQ005_REAMRK4"], "SEQ005_REAMRK4", $extra, 1, "BLANK");

        $model->managementFlg = "";
        if ($model->Properties["useQualifiedManagementFlg"] == '1') {
            $model->managementFlg = $db->getOne(knjl412hQuery::getManagementFlg($Row["SEQ005_REAMRK4"]));
        }

        //級・段位
        $query = knjl412hQuery::getSelectedRank($model, $Row["SEQ005_REAMRK4"]);
        if ($model->managementFlg == "1") {
            $query = knjl412hQuery::getRankResultMst($model, $Row["SEQ005_REAMRK4"]);
        }
        $rankRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (!is_array($rankRow)) {
            $query = knjl412hQuery::getSelectedRank2($model, $Row["SEQ005_REAMRK4"]); //QUALIFIED_RANK_DAT に1件も無かった場合
        }
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEQ005_REAMRK5"], "SEQ005_REAMRK5", $extra, 1, "BLANK");

        //その他
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["SEQ005_REAMRK6"] = knjCreateTextBox($objForm, $Row["SEQ005_REAMRK6"], "SEQ005_REAMRK6", 30, 90, $extra);

        //生年月日（西暦）
        $extra = " STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\"";
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar2($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), "", "", $extra);

        //性別
        $query = knjl412hQuery::getNameCd($model->year, "Z002");
        $extra = "onChange=\"change_flg();\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "BLANK");

        global $sess;
        //郵便番号入力支援(志願者)
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"isZipcd(this), toCopytxt(0, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ZIPCD"] = knjCreateTextBox($objForm, $Row["ZIPCD"], "ZIPCD", 10, "", $extra);

        //住所(志願者)
        $extra = " STYLE=\"ime-mode: active;\" onblur=\"toCopytxt(1, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ADDRESS1"] = knjCreateTextBox($objForm, $Row["ADDRESS1"], "ADDRESS1", 60, 60, $extra);

        //方書(志願者)
        $extra = " STYLE=\"ime-mode: active;\" onblur=\"toCopytxt(2, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ADDRESS2"] = knjCreateTextBox($objForm, $Row["ADDRESS2"], "ADDRESS2", 60, 60, $extra);

        //電話番号(志願者)
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toTelNo(this.value), toCopytxt(3, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 14, 14, $extra);

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&fscdname=FINSCHOOLCD_ID&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        //出身学校コード
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg(); return btn_submit('changeFscd');\" onkeydown=\"changeEnterToTab(this)\" id=\"FINSCHOOLCD_ID\" ";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 7, 7, $extra);

        //学校名
        $query = knjl412hQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_DISTCD_NAME"].$fsArray["FINSCHOOL_NAME"];

        //塾
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\"; id=\"PRISCHOOLCD_ID\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["PRISCHOOLCD"] = knjCreateTextBox($objForm, $Row["PRISCHOOLCD"], "PRISCHOOLCD", 7, 7, $extra);
        //教室コード
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\"; id=\"PRISCHOOL_CLASS_CD_ID\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["PRISCHOOL_CLASS_CD"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_CLASS_CD"], "PRISCHOOL_CLASS_CD", 7, 7, $extra);
        //かな検索ボタン（塾）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_PRISCHOOL/knjwpri_searchindex.php?cmd=searchMain&pricd=PRISCHOOLCD_ID&priname=label_priName&priclasscd=PRISCHOOL_CLASS_CD_ID&priclassname=label_priClassName&priaddr=&prischool_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY - 200 + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 280)\"";
        $arg["button"]["btn_pri_kana_reference"] = knjCreateBtn($objForm, "btn_pri_kana_reference", "検 索", $extra);
        //塾名
        $arg["data"]["PRISCHOOL_NAME"] = $db->getOne(knjl412hQuery::getPriSchoolName($Row["PRISCHOOLCD"]));
        //教室名
        $arg["data"]["PRISCHOOL_CLASS_NAME"] = $db->getOne(knjl412hQuery::getPriSchoolClassName($Row["PRISCHOOLCD"], $Row["PRISCHOOL_CLASS_CD"]));

        $defGrdEraY = '';
        $gengouCd = "";
        $gengouName = "";
        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl412hQuery::getCalendarno($model->year));
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
        $Row["FS_ERACD"] = strlen($Row["FS_ERACD"]) ? $Row["FS_ERACD"] : $gengouCd;
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onChange=\" toDatecheck(0, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        // $arg["data"]["FS_ERACD"] = knjCreateTextBox($objForm, strlen($Row["FS_ERACD"]) ? $Row["FS_ERACD"] : $gengouCd, "FS_ERACD", 1, 1, $extra);
        $query = knjl412hQuery::getNameCd($model->year, "L007");
        makeCmb($objForm, $arg, $db, $query, $Row["FS_ERACD"], "FS_ERACD", $extra, 1, "");

        //卒業和暦名
        $fs_wname = isset($Row["FS_WNAME"]) ? str_replace("&nbsp;", "", $Row["FS_WNAME"]) : $gengouName;
        knjCreateHidden($objForm, "FS_WNAME", $fs_wname);
        $arg["data"]["FS_WNAME"] = $fs_wname;

        //卒業年
        $Row["FS_Y"] = (strlen($Row["FS_Y"])) ? $Row["FS_Y"]: $defGrdEraY;
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_Y"] = knjCreateTextBox($objForm, $Row["FS_Y"], "FS_Y", 2, 2, $extra);

        //卒業月
        $Row["FS_M"] = (strlen($Row["FS_M"])) ? sprintf("%02d", $Row["FS_M"]): "03";
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_M"] = knjCreateTextBox($objForm, strlen($Row["FS_M"]) ? $Row["FS_M"] : '03', "FS_M", 2, 2, $extra);

        //西暦表示
        $setInnerFsYM = $Row["FS_Y"].".".$Row["FS_M"];
        $arg["data"]["FS_Y_M_INNER"] = $setInnerFsYM;
        knjCreateHidden($objForm, "FS_Y_M_INNER", $setInnerFsYM);

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg()\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 60, $extra);

        //続柄コンボ
        $query = knjl412hQuery::getNameCd($model->year, "H201");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["GRELATIONSHIP"], "GRELATIONSHIP", $extra, 1, "BLANK");

        //氏名かな(保護者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 40, 120, $extra);

        //郵便番号入力支援(保護者)
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"isZipcd(this), toCopytxt(0, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GZIPCD"] = knjCreateTextBox($objForm, $Row["GZIPCD"], "GZIPCD", 10, "", $extra);

        //住所(保護者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS1"] = knjCreateTextBox($objForm, $Row["GADDRESS1"], "GADDRESS1", 60, 60, $extra);

        //方書(保護者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS2"] = knjCreateTextBox($objForm, $Row["GADDRESS2"], "GADDRESS2", 60, 60, $extra);

        //連絡先1(保護者)
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["EMERGENCYTELNO"] = knjCreateTextBox($objForm, $Row["EMERGENCYTELNO"], "EMERGENCYTELNO", 14, 14, $extra);

        //連絡先2(保護者)
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["EMERGENCYCALL"] = knjCreateTextBox($objForm, $Row["EMERGENCYCALL"], "EMERGENCYCALL", 14, 14, $extra);

        //------------------------------出願内容-------------------------------------
        $hopeReceptnoKeta = 5;
        $arg["data"]["HOPE_ROWSPAN"] = $model->maxHopeNum;
        $arg["data"]["HOPE_COURSE3_HYOJI"] = ($Row["APPLICANTDIV"] == "2") ? "1" : ""; //第３希望を表示
        $arg["data"]["HOPE_MAX_HYOJI"] = ($Row["APPLICANTDIV"] == "1") ? "1" : ""; //第３回以降を表示
        for ($hopeNum = 1; $hopeNum <= $model->maxHopeNum; $hopeNum++) {
            $hopeReceptno = "HOPE".$hopeNum."_RECEPTNO";
            $hopeExamType = "HOPE".$hopeNum."_EXAM_TYPE";
            $hopeCourse = "HOPE".$hopeNum."_COURSE";
            $hopeTestdiv = "HOPE".$hopeNum."_TESTDIV";
            $hopeTestdiv = "HOPE".$hopeNum."_TESTDIV";

            //出願内容の取得
            $query = knjl412hQuery::getHopeInfo($model, $hopeNum);
            $hopeInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //受験番号
            $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
            $arg["data"][$hopeReceptno] = knjCreateTextBox($objForm, $hopeInfo["RECEPTNO"], $hopeReceptno, $hopeReceptnoKeta, $hopeReceptnoKeta, $extra);

            //受験型
            $extra = "onChange=\"change_flg()\"";
            $query = knjl412hQuery::getExamtypeMst($model, $hopeNum);
            makeCmb($objForm, $arg, $db, $query, $hopeInfo["EXAM_TYPE"], $hopeExamType, $extra, 1, "BLANK");

            //第１希望 ～ 第３希望
            for ($hopeCourseNum = 1; $hopeCourseNum <= $model->maxHopeCourseNum; $hopeCourseNum++) {
                $field1 = $hopeCourse.$hopeCourseNum;
                $field2 = $hopeTestdiv.$hopeCourseNum;

                //志望コース
                $query = knjl412hQuery::getCourseMst($model, "0");
                makeCmb($objForm, $arg, $db, $query, $hopeInfo["HOPE_COURSE".$hopeCourseNum], $field1, $extra, 1, "BLANK");

                //出願区分
                $query = knjl412hQuery::getEntexamSettingMst($model, "L006");
                makeCmb($objForm, $arg, $db, $query, $hopeInfo["HOPE_TESTDIV".$hopeCourseNum], $field2, $extra, 1, "BLANK");
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $Row);

        //hidden作成
        makeHidden($objForm, $model, $Row);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl412hForm1.html", $arg);
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


function makeBtn(&$objForm, &$arg, $Row)
{
    $zip = $Row["ZIPCD"];
    $gzip = $Row["GZIPCD"];
    $zadd = $Row["ADDRESS1"];
    $gadd = $Row["GADDRESS1"];

    // //読込ボタンを作成する
    // $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=GADDRESS1&zipname=GZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
    // $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);

    //確定ボタンを作成する
    $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=GADDRESS1&zipname=GZIPCD&zip='+document.forms[0]['GZIPCD'].value+'&frame='+getFrameName(self))\"";
    $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

    //検索ボタン
    $extra = "onclick=\"return btn_submit('reference', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
    $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

    // //かな検索ボタン
    // $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL412H/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
    // $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);

    //前の志願者検索ボタン
    $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
    $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

    //次の志願者検索ボタン
    $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
    $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

    // //画面クリアボタン
    // $extra = "style=\"width:90px\" onclick=\"return btn_submit('disp_clear');\"";
    // $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "画面クリア", $extra);

    // //志願者よりコピーボタン
    // $extra = "$disabled style=\"width:135px\" onclick=\"return btn_submit('copy');\"";
    // $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "志願者よりコピー", $extra);

    //更新ボタン
    $extra = "$disabled onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

    //更新ボタン(更新後前の志願者)
    $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('back');\"";
    $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra);

    //更新ボタン(更新後次の志願者)
    $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('next');\"";
    $arg["button"]["btn_up_next"] .= knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "year", $model->year);
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
}
