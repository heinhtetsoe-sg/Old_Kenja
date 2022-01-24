<?php

require_once('for_php7.php');

class knjl510fForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl510findex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata)) && $model->cmd != 'addnew') {
            //データを取得
            $Row = knjl510fQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl510fQuery::get_edit_data($model);
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
        $query = knjl510fQuery::get_name_cd($model->year, "L003", "1");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //管理番号(EXAMNO)
        //$extra = "onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $extra = " style=\"background:#cccccc;\" readOnly";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 5, 5, $extra);



        $setRow = array();
        foreach ($model->testdivArray as $key => $codeArray) {
            //入試区分チェック
            $name = "TESTDIV".$codeArray["TESTDIV"];
            $checked = strlen($Row[$name]) ? "checked" : "";
            $extra = "onChange=\"change_flg(); \" id=\"{$name}\" " .$checked;
            $setRow["TESTDIV"] = knjCreateCheckBox($objForm, $name, $codeArray["TESTDIV"], $extra);
            $setRow["TESTDIV_ID"] = $name;
            $setRow["TESTDIV_NAME"] = $codeArray["TESTDIV_NAME"];
            $setRow["TESTDIV_DATE"] = $codeArray["TESTDIV_DATE"];

            //受験型コンボ
            $name = "EXAM_TYPE".$codeArray["TESTDIV"];
            $query = knjl510fQuery::getExamType($model->year, $Row["APPLICANTDIV"], $codeArray["TESTDIV"], "CNT");
            $dataCnt = $db->getOne($query);
            if ($dataCnt == 1) {
                $query = knjl510fQuery::getExamType($model->year, $Row["APPLICANTDIV"], $codeArray["TESTDIV"]);
                $getRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $setRow["EXAM_TYPE"] = $getRow["LABEL"];
                knjCreateHidden($objForm, $name, $getRow["VALUE"]);
            } else {
                $query = knjl510fQuery::getExamType($model->year, $Row["APPLICANTDIV"], $codeArray["TESTDIV"]);
                $extra = "onChange=\"change_flg()\"";
                $setRow["EXAM_TYPE"] = makeCmbReturn($objForm, $arg, $db, $query, $Row[$name], $name, $extra, 1, "");
            }

            //受験番号テキスト
            $name = "RECEPTNO".$codeArray["TESTDIV"];
            $extra = " STYLE=\"ime-mode: inactive;\" onChange=\"change_flg();\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
            $setRow["RECEPTNO"] = knjCreateTextBox($objForm, $Row[$name], $name, 4, 4, $extra);

            $arg["dataT"][] = $setRow;
        }



        //受付日付
        $extra = " STYLE=\"ime-mode: inactive;\" onchange=\"change_flg()\"";
        $arg["data"]["RECEPTDATE"] = View::popUpCalendar2($objForm, "RECEPTDATE", str_replace("-", "/", $Row["RECEPTDATE"]), "", "", $extra);

        //専併区分コンボ
        $query = knjl510fQuery::get_name_cd($model->year, "L006");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SHDIV"], "SHDIV", $extra, 1, "");

        //第一志望コースコンボ
        $query = knjl510fQuery::getExamcourse($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"]);
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["EXAMCOURSE"], "EXAMCOURSE", $extra, 1, "");

        //第二志望コースコンボ
        $query = knjl510fQuery::getExamcourse($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"], "1002");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["EXAMCOURSE2"], "EXAMCOURSE2", $extra, 1, "BLANK");

        //英語見なし得点（英検取得級）コンボ
        $query = knjl510fQuery::getEiken($model->year, $Row["APPLICANTDIV"]);
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["EIKEN_SHUTOKU_KYUU"], "EIKEN_SHUTOKU_KYUU", $extra, 1, "BLANK");

        //氏名(志願者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //氏名かな(志願者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //性別コンボ
        $Row["SEX"] = strlen($Row["SEX"]) ? $Row["SEX"] : '2';
        $query = knjl510fQuery::get_name_cd($model->year, "Z002");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "");

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
        $query = knjl510fQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_DISTCD_NAME"].$fsArray["FINSCHOOL_NAME"].$fsArray["FINSCHOOL_TYPE_NAME"];

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        $defGrdEraY = '';
        $gengouCd = "";
        $gengouName = "";
        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl510fQuery::get_calendarno($model->year));
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
                $defGrdEraY = sprintf("%02d",$defGrdEraY);
                $gengouCd = $row["NAMECD2"];
                $gengouName = $row["NAME1"];
            }
        }
        knjCreateHidden($objForm, "ERCD_Y", $calno.':'.$spare);

        //卒業元号
        $Row["FS_ERACD"] = strlen($Row["FS_ERACD"]) ? $Row["FS_ERACD"] : $gengouCd;
        $query = knjl510fQuery::get_name_cd($model->year, "L007");
        $extra = "onChange=\"change_flg();\"";
        makeCmb($objForm, $arg, $db, $query, $Row["FS_ERACD"], "FS_ERACD", $extra, 1, "BLANK");

        //卒業年
        $Row["FS_Y"] = (strlen($Row["FS_Y"])) ? $Row["FS_Y"]: $defGrdEraY;
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_Y"] = knjCreateTextBox($objForm, $Row["FS_Y"], "FS_Y", 2, 2, $extra);

        //卒業月
        $defGrdmon = $model->cmd == 'showdivAdd' ? "03" : "";
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_M"] = knjCreateTextBox($objForm, strlen($Row["FS_M"]) ? $Row["FS_M"] : '03', "FS_M", 2, 2, $extra);

        //卒業区分（1:見込み,2:卒業）
        $defGrddiv = $model->cmd == 'showdivAdd' ? "1" : "";
        $Row["FS_GRDDIV"] = strlen($Row["FS_GRDDIV"]) ? $Row["FS_GRDDIV"] : '1';
        $query = knjl510fQuery::get_name_cd($model->year, "L016");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["FS_GRDDIV"], "FS_GRDDIV", $extra, 1, "BLANK");

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg()\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 60, $extra);

        //続柄コンボ
        $query = knjl510fQuery::get_name_cd($model->year, "H201");
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



        //プレテスト得点
        $setPre = array();
        $extra = " STYLE=\"ime-mode: inactive; text-align: right;\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        for ($kai = 1; $kai <= 2; $kai++) {
            $setPre["PRE_KAI"] = "第{$kai}回"; //回
            $setPre["PRE_KOKUGO"] = knjCreateTextBox($objForm, $Row["PRE_KOKUGO".$kai], "PRE_KOKUGO".$kai, 3, 3, $extra);
            $setPre["PRE_SANSUU"] = knjCreateTextBox($objForm, $Row["PRE_SANSUU".$kai], "PRE_SANSUU".$kai, 3, 3, $extra);
            $setPre["PRE_EIGO"] = knjCreateTextBox($objForm, $Row["PRE_EIGO".$kai], "PRE_EIGO".$kai, 3, 3, $extra);
            $setPre["PRE_2KA"] = $Row["PRE_2KA".$kai]; //2教科
            $setPre["PRE_3KA"] = $Row["PRE_3KA".$kai]; //3教科200点換算
            $setPre["PRE_TOTAL"] = $Row["PRE_TOTAL".$kai]; //計

            $arg["dataP"][] = $setPre;
        }

        //奨学生コンボ
        $query = knjl510fQuery::getShougaku($model->year, $Row["APPLICANTDIV"]);
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SHOUGAKU"], "SHOUGAKU", $extra, 1, "BLANK");

        //特別活動(クラブ名等)、備考(大会の成績等)
        $extra = " STYLE=\"ime-mode: active;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["TOKU_KATU"] = knjCreateTextBox($objForm, $Row["TOKU_KATU"], "TOKU_KATU", 80, 120, $extra);
        $arg["data"]["BIKOU"] = knjCreateTextBox($objForm, $Row["BIKOU"], "BIKOU", 80, 120, $extra);



        //-------------------------------- ボタン作成 ------------------------------------
        $zip = $Row["ZIPCD"];
        $gzip = $Row["GZIPCD"];
        $zadd = $Row["ADDRESS1"];
        $gadd = $Row["GADDRESS1"];

        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL510F/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
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

        //新規ボタンの有効・無効の切換
        if ($model->cmd == "showdivAdd") {
            $model->fubanMukou = "1"; //無効
        } else if ($model->fubanMukou == "1" && isset($model->warning)) {
            //無効のまま
        } else {
            $model->fubanMukou = ""; //有効
        }
        $disFuban = ($model->fubanMukou == "1") ? " disabled" : "";
        $disAdd = ($model->fubanMukou == "1") ? "" : " disabled";

        //新規ボタン
        $extra = "onclick=\"return btn_submit('addnew');\"";
        $arg["button"]["btn_addnew"] = knjCreateBtn($objForm, "btn_addnew", "新 規", $extra.$disFuban);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra.$disAdd);

        //更新ボタン
        $extra = "$disabled onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra.$disFuban);

        //更新ボタン(更新後前の志願者)
        $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('back');\"";
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra.$disFuban);

        //更新ボタン(更新後次の志願者)
        $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('next');\"";
        $arg["button"]["btn_up_next"] .= knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra.$disFuban);

        //削除ボタン
        $extra = "$disabled onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disFuban);

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
        View::toHTML($model, "knjl510fForm1.html", $arg);
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
            if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//makeCmbReturn
function makeCmbReturn(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
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
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>