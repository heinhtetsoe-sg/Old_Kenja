<?php

require_once('for_php7.php');

class knjd181hForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd181hForm1", "POST", "knjd181hindex.php", "", "knjd181hForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //データ取得
        $dataTmp = array();
        $query = knjd181hQuery::getHreportConditionDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dataTmp[$row["SEQ"]] = $row;
        }
        $result->free();
        
        //学期数取得
        $query = knjd181hQuery::getCountSemester($model);
        $getCountsemester = $db->getOne($query);

        //学期コンボ
        $query = knjd181hQuery::getSemester($getCountsemester);
        $extra = "onchange=\"return btn_submit('main')\"";
        //ログイン学期が3学期のときはコンボの初期値は学年末を表示
        if ($getCountsemester == 3 && $model->semester == 3 || $getCountsemester == 2 && $model->semester == 2 ) {
            $model->semester = 9;
        }
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);
        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }

        if ($model->Properties["useFi_Hrclass"] == "1" || $model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $arg["HR_CLASS_TYPE_SELECT"] = 1;
            //クラス方式選択    1:法定クラス 2:複式クラス/実クラス
            $opt = array(1, 2);
            $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"return btn_submit('main');\"", "id=\"HR_CLASS_TYPE2\" onclick=\"return btn_submit('main');\"");
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            if ($model->Properties["useFi_Hrclass"] == "1") {
                $arg["data"]["HR_CLASS_TYPE2_LABEL"] = "複式クラス";
            } else if ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
                $arg["data"]["HR_CLASS_TYPE2_LABEL"] = "実クラス";

                //学年混合チェックボックス
                $extra  = ($model->field["GAKUNEN_KONGOU"] == "1") ? "checked"   : "";
                $extra .= ($model->field["HR_CLASS_TYPE"]  != "1") ? " disabled" : "";
                $extra .= " onclick=\"return btn_submit('main');\" id=\"GAKUNEN_KONGOU\"";
                $arg["data"]["GAKUNEN_KONGOU"] = knjCreateCheckBox($objForm, "GAKUNEN_KONGOU", "1", $extra, "");
            }
        }

        //年組コンボ
        $query = knjd181hQuery::getAuth($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->hrClass, $extra, 1);

        //学校名取得
        $query = knjd181hQuery::getSchoolName();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /********************/
        /* チェックボックス */
        /********************/

        //校長印の出力あり
        if ($model->field["KOUTYOU"] == "1" || $model->cmd == '') {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " id='KOUTYOU'";
        $arg["data"]["KOUTYOU"] = knjCreateCheckBox($objForm, "KOUTYOU", "1", $extra);

        //学期
        $semeGr9 = array();
        $query = knjd181hQuery::getSemester2("9");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semeGr9 = $row;
        }

        //出欠集計開始日付
        $sDate = $semeGr9["SDATE"]; //日付がない場合、学期開始日付を使用する。
        $sDate = str_replace("-", "/", $sDate);
        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //学期
        $semeGr = array();
        $query = knjd181hQuery::getSemester2($model->field["SEMESTER"]);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semeGr = $row;
        }

        //パターンラジオ
        $opt = array(1, 2, 3); //1:A 2:B 3:C
        $extra = array("id=\"PATTERN1\" disabled=\"disabled\"", "id=\"PATTERN2\" disabled=\"disabled\"", "id=\"PATTERN3\" disabled=\"disabled\"");
        $radioArray = knjCreateRadio($objForm, "PATTERN", $dataTmp["001"]["REMARK1"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出欠集計終了日付
        $eDate = CTRL_DATE;//日付がない場合、学籍処理日を使用する。
        //日付が学期の範囲外の場合、学期終了日付を使用する。
        if ($eDate < $semeGr["SDATE"] || 
            $eDate > $semeGr["EDATE"]) {
            $eDate = $semeGr["EDATE"];
        }
        $eDate = str_replace("-", "/", $dataTmp["002"]["REMARK1"]);
        knjCreateHidden($objForm, "DATE", $dataTmp["002"]["REMARK1"]);
        $extra = " disabled=\"disabled\"";
        $arg["data"]["EDATE_DUMMY"] = knjCreateTextBox($objForm, $dataTmp["002"]["REMARK1"], "EDATE_DUMMY", 12, 12, $extra);

        //記載日付
        if ($model->field["DESC_DATE"] == "") $model->field["DESC_DATE"] = str_replace("-", "/", $dataTmp["003"]["REMARK1"]);
        $extra = " disabled=\"disabled\"";
        $arg["data"]["DESC_DATE_DUMMY"] = knjCreateTextBox($objForm, $model->field["DESC_DATE"], "DESC_DATE_DUMMY", 12, 12, $extra);

        /********************/
        /* テキストボックス */
        /********************/
        //学校種別取得
        $grade = substr($model->hrClass, 0, 2);
        $query = knjd181hQuery::getGdat($grade);
        $h_j = $db->getOne($query);

        //欠点テキストボックス表示判定
        //「欠点(評価)は、不振チェック参照するか？」の判定
        if ($model->useSlumpD048 == '1') {
            $arg["USE_SLUMP_D048"] = '1'; //null以外なら何でもいい
            unset($arg["KETTEN_FLG"]);
        } else {
            $arg["KETTEN_FLG"] = '1'; //null以外なら何でもいい
            unset($arg["USE_SLUMP_D048"]);
        }

        //欠点（評価）テキストボックス
        if($model->field["KETTEN"] == "" || $h_j != $model->field["SCHOOL_KIND"]){
            if ($h_j == 'J') {
                $model->field["KETTEN"] = 2;
            } else {
                $model->field["KETTEN"] = 2;
            }
        }
        $extra = "style=\"text-align: right\" onBlur=\"this.value=toInteger(this.value);\" disabled=\"disabled\"";
        $arg["data"]["KETTEN"] = knjCreateTextBox($objForm, $dataTmp["004"]["REMARK1"], "KETTEN", 3, 3, $extra);

        //欠点（評定）テキストボックス
        if($model->cmd == '') $model->field["KETTEN_HYOTEI"] = 1;
        $extra = "style=\"text-align: right\" onBlur=\"this.value=toInteger(this.value);\" disabled=\"disabled\"";
        $arg["data"]["KETTEN_HYOTEI"] = knjCreateTextBox($objForm, $dataTmp["004"]["REMARK2"], "KETTEN_HYOTEI", 3, 3, $extra);

        //増加単位を反映させる
        $extra  = ($dataTmp["005"]["REMARK1"] == "1") ? "checked='checked' " : "";
        $extra .= " id='ZOUKA' disabled=\"disabled\"";
        $arg["data"]["ZOUKA"] = knjCreateCheckBox($objForm, "ZOUKA", $dataTmp["005"]["REMARK1"], $extra);

        //担任印出力
        $extra  = ($dataTmp["006"]["REMARK1"] == "1") ? "checked='checked' " : "";
        $extra .= " id='TR_PRINT' disabled=\"disabled\"";
        $arg["data"]["TR_PRINT"] = knjCreateCheckBox($objForm, "TR_PRINT", $dataTmp["006"]["REMARK1"], $extra);

        knjCreateHidden($objForm, "PATTERN",        $dataTmp["001"]["REMARK1"]);
        knjCreateHidden($objForm, "EDATE",          str_replace("-", "/", $dataTmp["002"]["REMARK1"]));
        knjCreateHidden($objForm, "DESC_DATE",      str_replace("-", "/", $dataTmp["003"]["REMARK1"]));
        knjCreateHidden($objForm, "KETTEN",         $dataTmp["004"]["REMARK1"]);
        knjCreateHidden($objForm, "KETTEN_HYOTEI",  $dataTmp["004"]["REMARK2"]);
        knjCreateHidden($objForm, "ZOUKA",          $dataTmp["005"]["REMARK1"]);
        knjCreateHidden($objForm, "TR_PRINT",       $dataTmp["006"]["REMARK1"]);
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
        knjCreateHidden($objForm, "PRGID",         "KNJD181H");
        knjCreateHidden($objForm, "DOCUMENTROOT",  DOCUMENTROOT);
        knjCreateHidden($objForm, "useFi_Hrclass",               $model->Properties["useFi_Hrclass"]);
        knjCreateHidden($objForm, "useSpecial_Support_Hrclass",  $model->Properties["useSpecial_Support_Hrclass"]);

        //学期
        knjCreateHidden($objForm, "SEME_DATE", $seme);
        //学期開始日付
        knjCreateHidden($objForm, "SEME_SDATE", str_replace("-", "/", $semeGr["SDATE"]));
        //学期終了日付
        knjCreateHidden($objForm, "SEME_EDATE", str_replace("-", "/", $semeGr["EDATE"]));
        //学期終了日付
        knjCreateHidden($objForm, "SEME_FLG", $semeflg);
        knjCreateHidden($objForm, "tutisyoTokubetuKatudo", $model->Properties["tutisyoTokubetuKatudo"]);
        knjCreateHidden($objForm, "tutisyoTotalstudyWideForm", $model->Properties["tutisyoTotalstudyWideForm"]);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);

        //学校種別
        knjCreateHidden($objForm, "SCHOOL_KIND", $h_j);

        //累積情報の遅刻・早退欄のフラグ
        knjCreateHidden($objForm, "chikokuHyoujiFlg", $model->Properties["chikokuHyoujiFlg"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
        knjCreateHidden($objForm, "knjd181hOutputGakunenhyoka" , $model->Properties["knjd181hOutputGakunenhyoka"]);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd181hForm1.html", $arg);
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $opt = array();
    $query = knjd181hQuery::getStudent($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}
?>
