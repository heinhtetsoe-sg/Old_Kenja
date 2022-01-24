<?php

require_once('for_php7.php');

class knjd421Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd421Form1", "POST", "knjd421index.php", "", "knjd421Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd421Query::getSemester();
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, false);

        //コンボ切替ラジオボタン 1:法定クラス 2:実クラス
        $opt = array(1, 2);
        $extra = array("id=\"HUKUSIKI_RADIO1\" onClick=\"btn_submit('changeHukusiki');\"", "id=\"HUKUSIKI_RADIO2\" onClick=\"btn_submit('changeHukusiki');\"");
        $model->field["HUKUSIKI_RADIO"] = $model->field["HUKUSIKI_RADIO"] ? $model->field["HUKUSIKI_RADIO"] : '1';
        $radioArray = knjCreateRadio($objForm, "HUKUSIKI_RADIO", $model->field["HUKUSIKI_RADIO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学年混合チェックボックス
        $extra  = ($model->field["GRADE_MIX"] == "1") ? "checked" : "";
        $extra .= ($model->field["HUKUSIKI_RADIO"] != "1") ? " disabled" : "";
        $extra .= " onClick=\"return btn_submit('main');\"";
        $extra .= " id=\"GRADE_MIX\"";
        $arg["data"]["GRADE_MIX"] = knjCreateCheckBox($objForm, "GRADE_MIX", "1", $extra, "");

        //校種コンボ
        if ($model->field["HUKUSIKI_RADIO"] == "1") {
            $query = knjd421Query::getSchoolKind($model);
            $extra = "onchange=\"return btn_submit('main')\"";
            makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, false);
        }

        //年組コンボ
        if ($model->field["HUKUSIKI_RADIO"] == "2") {
            $query = knjd421Query::getGhr($model);
            $ghr_cd_label = "実クラス";
        } else if ($model->field["HUKUSIKI_RADIO"] == "1" && $model->field["GRADE_MIX"] == "1") {
            $query = knjd421Query::getStaffHr($model);
            $ghr_cd_label = "学年混合";
        } else {
            $query = knjd421Query::getGradeHrClass($model);
            $ghr_cd_label = "年組";
        }
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GHR_CD", $model->field["GHR_CD"], $extra, 1, true);
        $arg["data"]["GHR_CD_LABEL"] = $ghr_cd_label;
        if ($model->cmd == 'changeHukusiki') {
            $model->field["SCHOOL_KIND"] = "";
        }
        if ($model->field["HUKUSIKI_RADIO"] == "2" && $model->field["GHR_CD"] != '') {
            $query = knjd421Query::getGhr($model, $model->field["GHR_CD"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row["SCHOOL_KIND"] && $row["SCHOOL_KIND_FLG"] != "1") {
                $model->setWarning("実クラス内に校種が複数存在します。");
            } else {
                $model->field["SCHOOL_KIND"] = $row["SCHOOL_KIND"];
                knjCreateHidden($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"]);
            }
        }

        //D078コンボ
        $query = knjd421Query::getD078("D".$model->field["SCHOOL_KIND"]."78");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "D078", $model->field["D078"], $extra, 1, false);


        //状態区分グループ
        $query = knjd421Query::getGradeKindGroup($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_KIND_GROUP", $model->field["GRADE_KIND_GROUP"], $extra, 1, true);

        //異動対象日付
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm ,"DATE" ,$model->field["DATE"]);

        //記載日付
        if ($model->field["DESC_DATE"] == "") $model->field["DESC_DATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DESC_DATE"] = View::popUpCalendar($objForm ,"DESC_DATE" ,$model->field["DESC_DATE"]);

        //学校名取得
        $query = knjd421Query::getSchoolName();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //教科区分リストToリスト
        makeClassDivListToList($objForm, $arg, $db, $model);

        //通知表 個別の指導計画
        $radioValue = array(1, 2);
        if (!$model->field["OUTPUT_DIV"]) $model->field["OUTPUT_DIV"] = "1";
        $extra = array("id=\"OUTPUT_DIV1\" onclick =\" return btn_submit('change');\"", "id=\"OUTPUT_DIV2\" onclick =\" return btn_submit('change');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_DIV", $model->field["OUTPUT_DIV"], $extra, $radioValue, get_count($radioValue));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷面チェックボックス
        if ($model->field["OUTPUT_DIV"] == "1") {
            $arg["data"]["PRINT_SIDE2_TITLE"] = "通知表";
            $arg["PRINT_SIDE"] = "1";
            $name = array("PRINT_SIDE1", "PRINT_SIDE1_ATTEND", "PRINT_SIDE2", "PRINT_SIDE3", "PRINT_SIDE4");
            foreach ($name as $key => $val) {
                $arg["disp_".$val] = "1";
                $extra  = ($model->field[$val] == "1" || $model->cmd == "" || $model->cmd == "change") ? "checked" : "";
                $extra .= " onclick=\"kubun(this);\" id=\"$val\"";
                $arg["data"][$val] = knjCreateCheckBox($objForm, $val, "1", $extra, "");
            }
        } else {
            $arg["data"]["PRINT_SIDE2_TITLE"] = "指導計画";
        }

        //教科区分を出力しないチェックボックスを作成
        $extra = $model->field["OUTPUT_CATEGORY_NAME2"] == "1" || $model->cmd == '' ? "checked" : "";
        $extra .= " id=\"OUTPUT_CATEGORY_NAME2\" onclick =\"chkcat2();\"";
        $arg["data"]["OUTPUT_CATEGORY_NAME2"] = knjCreateCheckBox($objForm, "OUTPUT_CATEGORY_NAME2", "1", $extra, ""); 

        /****************/
        /* ラジオボタン */
        /****************/
        //帳票パターン
        $opt = array(1, 2, 3);
        $model->field["TYOUHYOU_PATTERN"] = ($model->field["TYOUHYOU_PATTERN"] == "") ? "1" : $model->field["TYOUHYOU_PATTERN"];
        $extra = array( "id=\"TYOUHYOU_PATTERN1\"", "id=\"TYOUHYOU_PATTERN2\"", "id=\"TYOUHYOU_PATTERN3\"");
        $radioArray = knjCreateRadio($objForm, "TYOUHYOU_PATTERN", $model->field["TYOUHYOU_PATTERN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /**********/
        /* ボタン */
        /**********/
        //プレビュー/印刷
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
        knjCreateHidden($objForm, "PRGID",         "KNJD421");
        knjCreateHidden($objForm, "DOCUMENTROOT",  DOCUMENTROOT);
        knjCreateHidden($objForm, "SELECT_GHR");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useGradeKindCompGroupSemester", $model->Properties["useGradeKindCompGroupSemester"]);
        knjCreateHidden($objForm, "HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE", $model->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE"]);
        knjCreateHidden($objForm, "reportSpecialSize05_01", $model->Properties["reportSpecialSize05_01"]);
        knjCreateHidden($objForm, "HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE", $model->Properties["HREPORTREMARK_DAT_SPECIALACTREMARK_SIZE"]);
        knjCreateHidden($objForm, "reportSpecialSize05_02", $model->Properties["reportSpecialSize05_02"]);
        knjCreateHidden($objForm, "HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE", $model->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE"]);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd421Form1.html", $arg);
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

    //クラス一覧
    $opt = array();
    $query = knjd421Query::getStudent($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    
    //クラス一覧作成
    $extra = "multiple style=\"width:230px{$addStyle}\" ondblclick=\"move1('left', '{$selLeft}', '{$selRight}', {$sortFlg})\"";
    $arg["data"][$selRight] = knjCreateCombo($objForm, $selRight, "", $opt, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px{$addStyle}\" ondblclick=\"move1('right', '{$selLeft}', '{$selRight}', {$sortFlg})\"";
    $arg["data"][$selLeft] = knjCreateCombo($objForm, $selLeft, "", array(), $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', '{$selLeft}', '{$selRight}', {$sortFlg});\"";
    $arg["button"]["btn_lefts{$s}"] = knjCreateBtn($objForm, "btn_lefts{$s}", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', '{$selLeft}', '{$selRight}', {$sortFlg});\"";
    $arg["button"]["btn_left1{$s}"] = knjCreateBtn($objForm, "btn_left1{$s}", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', '{$selLeft}', '{$selRight}', {$sortFlg});\"";
    $arg["button"]["btn_right1{$s}"] = knjCreateBtn($objForm, "btn_right1{$s}", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', '{$selLeft}', '{$selRight}', {$sortFlg});\"";
    $arg["button"]["btn_rights{$s}"] = knjCreateBtn($objForm, "btn_rights{$s}", ">>", $extra);
}

//教科区分リストToリスト作成
function makeClassDivListToList(&$objForm, &$arg, $db, $model) {
    $s = "2";
    $addStyle = ";height:70px";
    $selRight = "CATEGORY_NAME".$s;
    $selLeft = "CATEGORY_SELECTED".$s;
    $sortFlg = "0";

    //教科区分一覧
    $opt = array();
    $opt2 = array();
    if ($model->field["selectdata"]) {
        $opt2 = explode(',', $model->field["selectdata"]);
    }
    
    $query = knjd421Query::getClassDivOrder($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt2idx = -1;
        for ($i = 0; $i < get_count($opt2); $i++) {
            if ($opt2[$i] == $row["VALUE"]) {
                $opt2idx = $i;
                break;
            }
        }
        if (0 <= $opt2idx) {
            $opt2[$opt2idx] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
    }
    $result->free();
    

    //クラス一覧作成
    $extra = "multiple style=\"width:230px{$addStyle}\" ondblclick=\"move1('left', '{$selLeft}', '{$selRight}', {$sortFlg})\"";
    $arg["data"][$selRight] = knjCreateCombo($objForm, $selRight, "", $opt, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px{$addStyle}\" ondblclick=\"move1('right', '{$selLeft}', '{$selRight}', {$sortFlg})\"";
    $arg["data"][$selLeft] = knjCreateCombo($objForm, $selLeft, "", $opt2, $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', '{$selLeft}', '{$selRight}', {$sortFlg});\"";
    $arg["button"]["btn_lefts{$s}"] = knjCreateBtn($objForm, "btn_lefts{$s}", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', '{$selLeft}', '{$selRight}', {$sortFlg});\"";
    $arg["button"]["btn_left1{$s}"] = knjCreateBtn($objForm, "btn_left1{$s}", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', '{$selLeft}', '{$selRight}', {$sortFlg});\"";
    $arg["button"]["btn_right1{$s}"] = knjCreateBtn($objForm, "btn_right1{$s}", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', '{$selLeft}', '{$selRight}', {$sortFlg});\"";
    $arg["button"]["btn_rights{$s}"] = knjCreateBtn($objForm, "btn_rights{$s}", ">>", $extra);
}
?>
