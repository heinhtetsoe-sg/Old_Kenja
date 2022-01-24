<?php

require_once('for_php7.php');

class knjd429dForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd429dForm1", "POST", "knjd429dindex.php", "", "knjd429dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd429dQuery::getSemester();
        $extra = "id=\"SEMESTER\" onchange=\"current_cursor('SEMESTER'); return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, false);

        //コンボ切替ラジオボタン 1:法定クラス 2:実クラス
        $opt = array(1, 2);
        $extra = array("id=\"HUKUSIKI_RADIO1\" onClick=\"current_cursor('HUKUSIKI_RADIO1'); btn_submit('changeHukusiki');\"", "id=\"HUKUSIKI_RADIO2\" onClick=\"current_cursor('HUKUSIKI_RADIO2'); btn_submit('changeHukusiki');\"");
        $model->field["HUKUSIKI_RADIO"] = $model->field["HUKUSIKI_RADIO"] ? $model->field["HUKUSIKI_RADIO"] : '1';
        $radioArray = knjCreateRadio($objForm, "HUKUSIKI_RADIO", $model->field["HUKUSIKI_RADIO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //校種コンボ
        if ($model->cmd == 'changeHukusiki') {
            $model->field["SCHOOL_KIND"] = "";
        }
        if ($model->field["HUKUSIKI_RADIO"] == "1") {
            $query = knjd429dQuery::getSchoolKind($model);
            $extra = "id=\"SCHOOL_KIND\" onchange=\"current_cursor('SCHOOL_KIND'); return btn_submit('changeSchoolKind')\"";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, false);
        }

        //年組コンボ
        if ($model->field["HUKUSIKI_RADIO"] == "2") {
            $query = knjd429dQuery::getGhr($model);
            $ghr_cd_label = "実クラス";
        } else {
            $query = knjd429dQuery::getGradeHrClass($model);
            $ghr_cd_label = "年組";
        }
        $label = ($model->field["HUKUSIKI_RADIO"] == "2") ? "aria-label='実クラス'": "aria-label='年組'";
        $submitCmd = ($model->field["HUKUSIKI_RADIO"] == "2") ? "changeGhr" : "main";
        $extra = "id=\"GHR_CD\" onchange=\"current_cursor('GHR_CD'); return btn_submit('".$submitCmd."')\" $label";
        makeCmb($objForm, $arg, $db, $query, "GHR_CD", $model->field["GHR_CD"], $extra, 1, true);
        $arg["data"]["GHR_CD_LABEL"] = $ghr_cd_label;
        if ($model->field["HUKUSIKI_RADIO"] == "2" && $model->field["GHR_CD"] != '') {
            $query = knjd429dQuery::getGhr($model, $model->field["GHR_CD"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row["SCHOOL_KIND"] && $row["SCHOOL_KIND_FLG"] != "1") {
                $model->setWarning("実クラス内に校種が複数存在します。");
            } else {
                $model->field["SCHOOL_KIND"] = $row["SCHOOL_KIND"];
                knjCreateHidden($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"]);
            }
        }

        //異動対象日付初期値セット
        if ($model->field["MOVE_DATE"] == "") $model->field["MOVE_DATE"] = str_replace("-", "/", CTRL_DATE);
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); btn_submit('seldate')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field["MOVE_DATE"], "MOVE_DATE", 12, 12, $extra);
        //異動対象日付（カレンダー）
        global $sess;
        $extra = "onclick=\"tmp_list('seldate', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=MOVE_DATE&frame='+getFrameName(self) + '&date=' + document.forms[0]['MOVE_DATE'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen", "･･･", $extra);
        //異動対象日付
        $arg["data"]["MOVE_DATE"] = View::setIframeJs().$date_textbox.$date_button;

        //帳票パターンラジオボタン（名称マスタ「A035」より取得）
        if (in_array($model->cmd, array("", "changeHukusiki", "changeSchoolKind", "changeGhr")) && $model->field["SCHOOL_KIND"]) {
            $query = knjd429dQuery::getHreportConditionDat(CTRL_YEAR, $model, "202");
            $model->field["PRINT_PATTERN"] = $db->getOne($query);
        }
        $query = knjd429dQuery::getPatternUseMainly();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = $row["NAMECD2"];
            $arg["data"]["PRINT_PATTERN".$row["NAMECD2"]."_LABEL"] = $row["NAME1"];
        }
        if(!$model->field["PRINT_PATTERN"]) {
            $model->field["PRINT_PATTERN"] = "201";
        }
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PRINT_PATTERN{$val}\"");
        }
        $radioArray = paternRadio($objForm, "PRINT_PATTERN", $model->field["PRINT_PATTERN"], $extra, $opt, get_count($opt), $opt);
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力ページチェックボックス 1:表紙・裏表紙 2:学習の記録 3:出欠の記録
        $name = array("PRINT_SIDE1", "PRINT_SIDE2", "PRINT_SIDE3");
        foreach ($name as $key => $val) {
            $extra  = ($model->field[$val] == "1" || $model->cmd == "") ? "checked" : "";
            $extra .= " id=\"$val\"";
            if ($val == "PRINT_SIDE1") $extra .= " onchange=\"return resetOutput1(this);\" ";
            $arg["data"][$val] = knjCreateCheckBox($objForm, $val, "1", $extra, "");
        }

        //修了証書チェックボックス
        $disabled = ($model->field["SCHOOL_KIND"] != "H") ? " disabled " : "";
        if ($model->cmd == "") {
            $checked = " checked ";
            $model->field["OUTPUT1"] = "1";
        } else if ($model->field["PRINT_SIDE1"] == "") {
            $checked = "";
            $model->field["OUTPUT1"] = "";
        } else {
            $checked = ($model->field["OUTPUT1"]) ? " checked " : "";
        }
        $extra = " id=\"OUTPUT1\" ".$checked.$disabled;
        $extra .= " onchange=\"return chkPrintSide1(this);\" ";
        $arg["data"]["OUTPUT1"] = knjCreateCheckBox($objForm, "OUTPUT1", "1", $extra, "");

        if ($model->field["SCHOOL_KIND"] != "H") {
            knjCreateHidden($objForm, "OUTPUT1", $model->field["OUTPUT1"]);
        }

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //プレビュー/印刷
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "id =\"PRINT\" onclick=\"current_cursor('PRINT'); return newwin('" . SERVLET_URL . "');\"");

        //終了
        $extra = "onclick=\"closeWin();\" aria-label='終了'";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
        knjCreateHidden($objForm, "PRGID",         "KNJD429D");
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
        View::toHTML($model, "knjd429dForm1.html", $arg);
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
    $query = knjd429dQuery::getSchnoIdou($model, $seme);
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

    $query = knjd429dQuery::getStudent($model);
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
    $extra = "multiple style=\"width:230px{$addStyle}\" ondblclick=\"move1('right', '{$selLeft}', '{$selRight}', {$sortFlg})\" aria-label='出力対象者一覧'";
    $arg["data"][$selLeft] = knjCreateCombo($objForm, $selLeft, "", $opt_left, $extra, 20);

    // << ボタン作成
    $label = "aria-label='全てを生徒一覧から出力対象者一覧へ移動'";
    $extra = "id=\"btn_lefts\" style=\"height:20px;width:40px\" onclick=\"moves('left', '{$selLeft}', '{$selRight}', {$sortFlg});\" $label";
    $arg["button"]["btn_lefts{$s}"] = knjCreateBtn($objForm, "btn_lefts{$s}", "<<", $extra);

    // ＜ ボタン作成
    $label = "aria-label='クリックしたリストを生徒一覧から出力対象者一覧へ移動'";
    $extra = "id=\"btn_left1\" style=\"height:20px;width:40px\" onclick=\"move1('left', '{$selLeft}', '{$selRight}', {$sortFlg});\" $label";
    $arg["button"]["btn_left1{$s}"] = knjCreateBtn($objForm, "btn_left1{$s}", "＜", $extra);
    
    // ＞ ボタン作成
    $label = "aria-label='クリックしたリストを出力対象者一覧から生徒一覧へ移動'";
    $extra = "id=\"btn_right1\" style=\"height:20px;width:40px\" onclick=\"move1('right', '{$selLeft}', '{$selRight}', {$sortFlg});\" $label";
    $arg["button"]["btn_right1{$s}"] = knjCreateBtn($objForm, "btn_right1{$s}", "＞", $extra);
    
    // >> ボタン作成
    $label = "aria-label='全てを出力対象者一覧から生徒一覧へ移動'";
    $extra = "id=\"btn_rights\" style=\"height:20px;width:40px\" onclick=\"moves('right', '{$selLeft}', '{$selRight}', {$sortFlg});\" $label";
    $arg["button"]["btn_rights{$s}"] = knjCreateBtn($objForm, "btn_rights{$s}", ">>", $extra);
}

//ラジオ作成
function paternRadio(&$objForm, $name, $value, $extra, $multi, $count, $valArray)
{
    $ret = array();

    for ($i = 1; $i <= $count; $i++) {
        if (is_array($extra)) $ext = $extra[$i-1];
        else $ext = $extra;
        $objForm->ae( array("type"      => "radio",
                            "name"      => $name,
                            "value"     => $value,
                            "extrahtml" => $ext,
                            "multiple"  => $multi));
        $ret[$name.$valArray[$i - 1]] = $objForm->ge($name, $valArray[$i - 1]);
    }

    return $ret;
}
?>
