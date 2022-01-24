<?php

require_once('for_php7.php');

class knjd429Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        /* Add by PP for Title 2020-01-10 start */
        $arg["TITLE"]   = "通知表印刷画面";
        /* Add by PP for Title 2020-01-17 end */

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd429Form1", "POST", "knjd429index.php", "", "knjd429Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd429Query::getSemester();
        // Add by PP for current cursor 2020-01-10 start
        $extra = "id=\"SEMESTER\" onchange=\"current_cursor('SEMESTER'); return btn_submit('main')\"";
        // Add by PP for current cursor 2020-01-17 end
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, false);

        //コンボ切替ラジオボタン 1:法定クラス 2:実クラス
        $opt = array(1, 2);
        // Add by PP for current cursor 2020-01-10 start
        $extra = array("id=\"HUKUSIKI_RADIO1\" onClick=\"current_cursor('HUKUSIKI_RADIO1'); btn_submit('changeHukusiki');\"", "id=\"HUKUSIKI_RADIO2\" onClick=\"current_cursor('HUKUSIKI_RADIO2'); btn_submit('changeHukusiki');\"");
        // Add by PP for current cursor 2020-01-17 end
        $model->field["HUKUSIKI_RADIO"] = $model->field["HUKUSIKI_RADIO"] ? $model->field["HUKUSIKI_RADIO"] : '1';
        $radioArray = knjCreateRadio($objForm, "HUKUSIKI_RADIO", $model->field["HUKUSIKI_RADIO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //校種コンボ
        if ($model->cmd == 'changeHukusiki') {
            $model->field["SCHOOL_KIND"] = "";
        }
        if ($model->field["HUKUSIKI_RADIO"] == "1") {
            $query = knjd429Query::getSchoolKind($model);
            // Add by PP for current cursor 2020-01-10 start
            $extra = "id=\"SCHOOL_KIND\" onchange=\"current_cursor('SCHOOL_KIND'); return btn_submit('main')\"";
            // Add by PP for current cursor 2020-01-17 end
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, false);
        }

        //年組コンボ
        if ($model->field["HUKUSIKI_RADIO"] == "2") {
            $query = knjd429Query::getGhr($model);
            $ghr_cd_label = "実クラス";
        } else {
            $query = knjd429Query::getGradeHrClass($model);
            $ghr_cd_label = "年組";
        }
        // Add by PP for current cursor 2020-01-10 start
        $label = ($model->field["HUKUSIKI_RADIO"] == "2") ? "aria-label='実クラス'": "aria-label='年組'";
        $extra = "id=\"GHR_CD\" onchange=\"current_cursor('GHR_CD'); return btn_submit('main')\" $label";
        // Add by PP for current cursor 2020-01-17 end
        makeCmb($objForm, $arg, $db, $query, "GHR_CD", $model->field["GHR_CD"], $extra, 1, true);
        $arg["data"]["GHR_CD_LABEL"] = $ghr_cd_label;
        if ($model->field["HUKUSIKI_RADIO"] == "2" && $model->field["GHR_CD"] != '') {
            $query = knjd429Query::getGhr($model, $model->field["GHR_CD"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row["SCHOOL_KIND"] && $row["SCHOOL_KIND_FLG"] != "1") {
                $model->setWarning("実クラス内に校種が複数存在します。");
            } else {
                $model->field["SCHOOL_KIND"] = $row["SCHOOL_KIND"];
                knjCreateHidden($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"]);
            }
        }

        //学校名取得
        $query = knjd429Query::getSchoolName();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //日付
        $param = "extra=btn_submit(\'seldate\');";
        if ($model->field["MOVE_DATE"] == "") $model->field["MOVE_DATE"] = str_replace("-", "/", CTRL_DATE);
        //$arg["data"]["MOVE_DATE"] = View::popUpCalendar($objForm, "MOVE_DATE", str_replace("-", "/", $model->field["MOVE_DATE"]), $param);

        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); btn_submit('seldate')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["MOVE_DATE"], "MOVE_DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('seldate', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=MOVE_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['MOVE_DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\" aria-label='カレンダー'";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["MOVE_DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        // 表紙印刷
        $extra = "id=\"HYOSI\" checked";
        $arg["data"]["HYOSI"] = knjCreateCheckBox($objForm, "HYOSI", "1", $extra);

        //表紙サイズラジオボタン 1:A3 2:A4
        $opt_div = array(1, 2);
        $model->field["PRINTSIZE"] = ($model->field["PRINTSIZE"]=="") ? "1" : $model->field["PRINTSIZE"];
        $extra = array("id=\"PRINTSIZE1\" onClick=\"current_cursor('PRINTSIZE1'); chgCheck();\"", "id=\"PRINTSIZE2\" onClick=\"current_cursor('PRINTSIZE2'); chgCheck();\"");
        $radioArray = knjCreateRadio($objForm, "PRINTSIZE", $model->field["PRINTSIZE"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /**********/
        /* ボタン */
        /**********/
        //プレビュー/印刷
        // Add by PP for current cursor 2020-01-10 start
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "id =\"PRINT\" onclick=\"current_cursor('PRINT'); return newwin('" . SERVLET_URL . "');\"");
        // Add by PP for current cursor 2020-01-17 start

        //終了
        // Edit by PP for PC-Talker(voice) 2020-01-10 start
        $extra = "onclick=\"closeWin();\" aria-label='終了'";
        // Edit by PP for PC-Talker(voice) 2020-01-17 end
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
        knjCreateHidden($objForm, "PRGID",         "KNJD429");
        knjCreateHidden($objForm, "DOCUMENTROOT",  DOCUMENTROOT);
        knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
        knjCreateHidden($objForm, "SELECT_GHR");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd429Form1.html", $arg);
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $addBlank) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    if ($addBlank) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $defValue = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($row["DEF_VALUE_FLG"] == '1') {
            $defValue = $row["VALUE"];
        }
    }
    $result->free();

    if ($value && $value_flg) {
        $value = $value;
    } else if ($name == "SEMESTER") {
        $value = CTRL_SEMESTER;
    } else if ($name == "D078") {
        $value = $defValue ? $defValue : $opt[0]["value"];
    } else {
        $value = $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    $s = "";
    $selRight = "CATEGORY_NAME".$s;
    $selLeft = "CATEGORY_SELECTED".$s;
    $addStyle = ";height:180px";
    $sortFlg = "1";

    //対象外の生徒取得
    $query = knjd429Query::getSchnoIdou($model, $seme);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //クラス一覧
    $opt_left = array();
    $opt_right = array();
    $leftselarry = explode(",", $model->selectdata);

    $query = knjd429Query::getStudent($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        list($houtei, $hr, $schregNo) = explode("-", $row["VALUE"]);
        $idou = (in_array($schregNo, $opt_idou, true)) ? "●" : "　";
        if (!in_array($row["VALUE"], $leftselarry, true)) {
            $opt_right[] = array('label' => $idou.$row["LABEL"],
                       'value' => $row["VALUE"]);
        } else {
            $opt_left[] = array('label' => $idou.$row["LABEL"],
                       'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px{$addStyle}\" ondblclick=\"move1('left', '{$selLeft}', '{$selRight}', {$sortFlg})\"";
    $arg["data"][$selRight] = knjCreateCombo($objForm, $selRight, "", $opt_right, $extra, 20);

    //出力対象作成
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $extra = "multiple style=\"width:230px{$addStyle}\" ondblclick=\"move1('right', '{$selLeft}', '{$selRight}', {$sortFlg})\" aria-label='出力対象一覧'";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["data"][$selLeft] = knjCreateCombo($objForm, $selLeft, "", $opt_left, $extra, 20);

    // << ボタン作成
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $label = "aria-label='全てを生徒一覧から出力対象者一覧へ移動'";
    $extra = "id=\"btn_lefts\" style=\"height:20px;width:40px\" onclick=\"moves('left', '{$selLeft}', '{$selRight}', {$sortFlg});\" $label";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["button"]["btn_lefts{$s}"] = knjCreateBtn($objForm, "btn_lefts{$s}", "<<", $extra);

    // ＜ ボタン作成
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $label = "aria-label='クリックしたリストを生徒一覧から出力対象者一覧へ移動'";
    $extra = "id=\"btn_left1\" style=\"height:20px;width:40px\" onclick=\"move1('left', '{$selLeft}', '{$selRight}', {$sortFlg});\" $label";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["button"]["btn_left1{$s}"] = knjCreateBtn($objForm, "btn_left1{$s}", "＜", $extra);

    // ＞ ボタン作成
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $label = "aria-label='クリックしたリストを出力対象者一覧から生徒一覧へ移動'";
    $extra = "id=\"btn_right1\" style=\"height:20px;width:40px\" onclick=\"move1('right', '{$selLeft}', '{$selRight}', {$sortFlg});\" $label";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["button"]["btn_right1{$s}"] = knjCreateBtn($objForm, "btn_right1{$s}", "＞", $extra);

    // >> ボタン作成
    // Edit by PP for PC-Talker(voice) 2020-01-10 start
    $label = "aria-label='全てを出力対象者一覧から生徒一覧へ移動'";
    $extra = "id=\"btn_rights\" style=\"height:20px;width:40px\" onclick=\"moves('right', '{$selLeft}', '{$selRight}', {$sortFlg});\" $label";
    // Edit by PP for PC-Talker(voice) 2020-01-17 end
    $arg["button"]["btn_rights{$s}"] = knjCreateBtn($objForm, "btn_rights{$s}", ">>", $extra);
}

?>
