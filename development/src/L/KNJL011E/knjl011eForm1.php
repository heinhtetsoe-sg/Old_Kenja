<?php

require_once('for_php7.php');

class knjl011eForm1 {
    function main(&$model) {
        $objForm      = new form;

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            //データを取得
            $Row = knjl011eQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl011eQuery::get_edit_data($model);
                }
                $model->examno  = $Row["EXAMNO"];
            }
            $disabled = "";
            if (!is_array($Row)) {
                $disabled = "disabled";
                if ($model->cmd == 'reference') {
                    $Row["APPLICANTDIV"] = $model->field["APPLICANTDIV"];
                    $Row["TESTDIV"] = $model->field["TESTDIV"];
                    $model->setWarning("MSG303");
                }
            }
        } else {
            $Row =& $model->field;
        }

        if ($model->cmd == 'changeTest' || $model->cmd == 'main') {
            $Row["APPLICANTDIV"] = $model->field["APPLICANTDIV"];
            $Row["TESTDIV"]      = $model->field["TESTDIV"];
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
        $setTextField[] = "BIRTHDAY";
        $setTextField[] = "FINSCHOOLCD";
        $setTextField[] = "FS_DAY";
        $setTextField[] = "FS_GRDDIV";
        $setTextField[] = "ZIPCD";
        $setTextField[] = "ADDRESS1";
        $setTextField[] = "ADDRESS2";
        $setTextField[] = "TELNO";
        $setTextField[] = "ENT_MONEY_STATUS";
        $setTextField[] = "GNAME";
        $setTextField[] = "GKANA";
        $setTextField[] = "RELATIONSHIP";
        $setTextField[] = "GZIPCD";
        $setTextField[] = "GADDRESS1";
        $setTextField[] = "GADDRESS2";
        $setTextField[] = "GTELNO";
        if ($model->musicFlg)  $setTextField[] = "HOPE1";
        if ($model->musicFlg)  $setTextField[] = "HOPE2";
        if ($model->musicFlg)  $setTextField[] = "HOPE";
        if ($model->musicFlg)  $setTextField[] = "NOMAL_SHDIV";
        if ($model->suisenFlg) $setTextField[] = "QUALIFIED_CONTENT";
        knjCreateHidden($objForm, "setTextField", implode(',', $setTextField));

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度
        $query = knjl011eQuery::get_name_cd($model->year, "L003");
        $extra = "onChange=\"change_flg(); return btn_submit('changeTest');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //入試区分
        $query = knjl011eQuery::get_name_cd($model->year, "L004");
        $extra = "onChange=\"change_flg(); return btn_submit('changeTest');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1, "");

        //各フラグセット
        $model->suisenFlg = $model->musicFlg = false;
        $nameCdL004Row = $db->getRow(knjl011eQuery::get_name_cd($model->year, "L004", $Row["TESTDIV"]), DB_FETCHMODE_ASSOC);

        //推薦フラグ
        if ($nameCdL004Row["NAMESPARE1"] == "1") $model->suisenFlg = true;

        //音楽フラグ
        if ($nameCdL004Row["NAMESPARE3"] == "2") $model->musicFlg  = true;

        //受験番号
        $extra = " STYLE=\"ime-mode: inactive\" onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 7, 7, $extra);

        //名称セット
        $nameCd1In  = "('L004', 'L006', 'L045', 'L058')";
        $setNameArr = array();
        $query = knjl011eQuery::getNameMstList($model->year, $nameCd1In);
        $result = $db->query($query);
        while ($nameRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setNameArr[$nameRow["NAMECD1"]][$nameRow["NAMECD2"]] = $nameRow["LABEL"];
        }

        //受験区分
        $arg["data"]["TESTDIV1"] = $setNameArr['L045'][$Row["TESTDIV1"]];
        knjCreateHidden($objForm, "TESTDIV1", $Row["TESTDIV1"]);

        //出願コース
        $arg["data"]["DESIREDIV"] = $setNameArr['L058'][$Row["DESIREDIV"]];
        knjCreateHidden($objForm, "DESIREDIV", $Row["DESIREDIV"]);

        //専併
        $arg["data"]["SHDIV"] = $setNameArr['L006'][$Row["SHDIV"]];
        knjCreateHidden($objForm, "SHDIV", $Row["SHDIV"]);

        //ログインID
        $arg["data"]["LOGIN_ID"] = $Row["LOGIN_ID"];
        knjCreateHidden($objForm, "LOGIN_ID", $Row["LOGIN_ID"]);

        //併願状況
        $shCnt = '1';
        $query = knjl011eQuery::getHeiganData($model, $Row["LOGIN_ID"]);
        $result = $db->query($query);
        while ($shRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //入試区分
            $arg["data"]["H_TESTDIV_".$shCnt] = $setNameArr['L004'][$shRow["TESTDIV"]];

            //出願コース
            $arg["data"]["H_DESIREDIV_".$shCnt] = $setNameArr['L058'][$shRow["DESIREDIV"]];

            //受験番号
            $arg["data"]["H_EXAMNO_".$shCnt] = $shRow["EXAMNO"];

            $shCnt++;
        }

        //------------------------------志願者情報-------------------------------------
        //氏名(志願者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"NAME\" onkeyup=\"keySet('NAME', 'NAME_KANA', 'H');\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //氏名カナ(志願者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"NAME_KANA\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //性別(志願者)
        $query = knjl011eQuery::get_name_cd($model->year, "Z002");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "");

        //生年月日
        $extra = " onkeydown=\"changeEnterToTab(this)\"";
        $Row["BIRTHDAY"] = str_replace('-', '/', $Row["BIRTHDAY"]);
        $arg["data"]["BIRTHDAY"] = View::popUpCalendarAlp($objForm, "BIRTHDAY", $Row["BIRTHDAY"], $extra, "");

        //出身校
        //出身学校コード（中学）
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\" id=\"FINSCHOOLCD_ID\" ";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", 7, 7, $extra);
        //検索ボタン（中学）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&setSchoolKind=3&fscdname=FINSCHOOLCD&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference1"] = knjCreateBtn($objForm, "btn_fin_kana_reference1", "検 索", $extra);
        //学校名
        $query = knjl011eQuery::getFinschoolName($Row["FINSCHOOLCD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_NAME"];
        //卒業年月
        $extra = " onkeydown=\"changeEnterToTab(this)\"";
        $Row["FS_DAY"] = str_replace('-', '/', $Row["FS_DAY"]);
        $arg["data"]["FS_DAY"] = View::popUpCalendarAlp($objForm, "FS_DAY", $Row["FS_DAY"], $extra, "");
        //卒業区分
        $query = knjl011eQuery::get_name_cd($model->year, "L016");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $Row["FS_GRDDIV"] = ($Row["FS_GRDDIV"]) ? $Row["FS_GRDDIV"]: "2";
        makeCmb($objForm, $arg, $db, $query, $Row["FS_GRDDIV"], "FS_GRDDIV", $extra, 1, "");

        global $sess;
        //郵便番号入力支援
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"isZipcd(this)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["ZIPCD"] = knjCreateTextBox($objForm, $Row["ZIPCD"], "ZIPCD", 10, "", $extra);
        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);
        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS1&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"";
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

        //入学金支払状況
        $query = knjl011eQuery::get_name_cd($model->year, "L060");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["ENT_MONEY_STATUS"], "ENT_MONEY_STATUS", $extra, 1, "BLANK");

        //本校同窓会推薦書checkbox
        $checked = ($Row["RECOMMENDATION"] == "1") ? " checked": "";
        $extra = "id=\"RECOMMENDATION\"";
        $arg["data"]["RECOMMENDATION"] = knjCreateCheckBox($objForm, "RECOMMENDATION", "1", $extra.$checked);

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg()\" id=\"GNAME\" onkeyup=\"keySet('GNAME', 'GKANA', 'H');\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 60, $extra);

        //氏名カナ(保護者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"GKANA\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 40, 120, $extra);

        //続柄コンボ
        $query = knjl011eQuery::get_name_cd($model->year, "H201");
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

        //------------------------------音楽専攻科-------------------------------------
        if ($model->musicFlg) {
            $arg["MUSIC"] = '1';

            //第一希望
            $extra = " onChange=\"change_flg()\" onkeydown=\"changeEnterToTab(this)\"";
            $arg["data"]["HOPE1"] = knjCreateTextBox($objForm, $Row["HOPE1"], "HOPE1", 40, 60, $extra);

            //第二希望
            $extra = " onChange=\"change_flg()\" onkeydown=\"changeEnterToTab(this)\"";
            $arg["data"]["HOPE2"] = knjCreateTextBox($objForm, $Row["HOPE2"], "HOPE2", 40, 60, $extra);

            //出願専攻
            $opt = array();
            $opt[] = array('label' => '', 'value' => '');
            if ($Row["HOPE1"] != '') $opt[] = array('label' => '1:'.$Row["HOPE1"], 'value' => '1');
            if ($Row["HOPE2"] != '') $opt[] = array('label' => '2:'.$Row["HOPE2"], 'value' => '2');
            $extra = " onChange=\"change_flg()\" onkeydown=\"changeEnterToTab(this)\"";
            $arg["data"]["HOPE"] = knjCreateCombo($objForm, "HOPE", $Row["HOPE"], $opt, $extra, 1);

            //普通科併願
            $query = knjl011eQuery::get_name_cd($model->year, "L061");
            $extra = "onChange=\"change_flg()\" onkeydown=\"changeEnterToTab(this)\"";
            makeCmb($objForm, $arg, $db, $query, $Row["NOMAL_SHDIV"], "NOMAL_SHDIV", $extra, 1, "BLANK");
        }

        //------------------------------推薦資格審査-------------------------------------
        if ($model->suisenFlg) {
            $arg["SUISEN"] = '1';

            //資格内容
            $extra = " onChange=\"change_flg()\" id=\"QUALIFIED_CONTENT\" onkeyup=\"charCount(this.value, 4, 80, true);\"";
            $arg["data"]["QUALIFIED_CONTENT"] = knjCreateTextArea($objForm, "QUALIFIED_CONTENT", 4, 80, "wrap", $extra, $Row["QUALIFIED_CONTENT"]);

            //資格（1:実績 2:推薦）
            $opt = array(1, 2);
            $Row["QUALIFIED"] = ($Row["QUALIFIED"] == "") ? "1" : $Row["QUALIFIED"];
            $extra = array();
            foreach($opt as $key => $val) {
                array_push($extra, " id=\"QUALIFIED{$val}\"");
            }
            $radioArray = knjCreateRadio($objForm, "QUALIFIED", $Row["QUALIFIED"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            //実技有無（1:未設定 2:有り 3:無し）
            $opt = array(1, 2, 3);
            $Row["PRACTICE"] = ($Row["PRACTICE"] == "") ? "1" : $Row["PRACTICE"];
            $extra = array();
            foreach($opt as $key => $val) {
                array_push($extra, " id=\"PRACTICE{$val}\"");
            }
            $radioArray = knjCreateRadio($objForm, "PRACTICE", $Row["PRACTICE"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            //判定（1:未設定 2:可 3:不可）
            $opt = array(1, 2, 3);
            $Row["POSSIBLE"] = ($Row["POSSIBLE"] == "") ? "1" : $Row["POSSIBLE"];
            $extra = array();
            foreach($opt as $key => $val) {
                array_push($extra, " id=\"POSSIBLE{$val}\"");
            }
            $radioArray = knjCreateRadio($objForm, "POSSIBLE", $Row["POSSIBLE"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            //PDF作成
            $disPdf  = ($model->examno == '') ? ' disabled': '';
            $disPdf2 = '';
            //ファイルからの取り込み
            $arg["data"]["PDF_FILE"] = knjCreateFile($objForm, "PDF_FILE", $disPdf, 512000);
            //実行ボタン
            $extra = "onclick=\"return btn_submit('exec_pdf');\"".$disPdf;
            $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);

            //ファイルダウンロード
            $dir      = '/image/ENTEXAM/';
            $fileName = 'KNJL011E_'.$model->year.'_'.$model->field["APPLICANTDIV"].'_'.$model->examno.'.pdf';
            $fileName = mb_convert_encoding($fileName, "SJIS-win", "UTF-8");

            $path_file = DOCUMENTROOT .$dir.$fileName;

            /* ファイルの存在確認 */
            if (!file_exists($path_file)) {
                $arg["NOTHING_PDF"] = "1";
                $disPdf2 = ' disabled';
            } else {
                $arg["down"]["PDF_URL"] = REQUESTROOT.$dir.$fileName;
            }

            //添付削除ボタンを作成する
            $extra = "onclick=\"return btn_submit('pdf_del');\"".$disPdf.$disPdf2;
            $arg["button"]["btn_pdf_del"] = knjCreateBtn($objForm, "btn_pdf_del", "添付削除", $extra);
        }

        //-------------------------------- ボタン作成 ------------------------------------
        $gzip = $Row["GZIPCD"];
        $gadd = $Row["GADDRESS1"];

        //検索ボタン（受験番号）
        $extra = "onclick=\"return btn_submit('reference', '".$gzip."', '".$gadd."');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //カナ検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011E/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&testdiv='+document.forms[0]['TESTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "カナ検索", $extra);

        //前の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1', '".$gzip."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1', '".$gzip."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //画面クリアボタン
        $extra = "style=\"width:90px\" onclick=\"return btn_submit('disp_clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "画面クリア", $extra);

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

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["start"] = $objForm->get_start("main", "POST", "knjl011eindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl011eForm1.html", $arg);
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

        if ($name == 'SEX') {
            if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>