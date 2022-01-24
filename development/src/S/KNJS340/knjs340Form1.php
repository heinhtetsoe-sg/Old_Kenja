<?php

require_once('for_php7.php');

class knjs340Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        /* Add by HPA for title 2020-01-10 start */
        $arg["TITLE"] = "児童_生徒出席簿画面";
        /* Add by HPA for title 2020-01-17 end */

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjs340Form1", "POST", "knjs340index.php", "", "knjs340Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        if ($model->Properties["useFi_Hrclass"] == "1") {
            $arg["useFi_HrclassSelect"] = "1";
            //クラス方式選択 (1:法定クラス 2:複式クラス)
            $opt = array(1, 2);
            $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];

            /* Edit by HPA for CurrentCursor 2020-01-10 start */
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"current_cursor('HR_CLASS_TYPE1'); return btn_submit('main');\"", "id=\"HR_CLASS_TYPE2\" onclick=\"current_cursor('HR_CLASS_TYPE2'); return btn_submit('main');\"");
            /* Edit by HPA for CurrentCursor 2020-01-17 end */

            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        } else if ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $arg["useSpecial_Support_HrclassSelect"] = "1";
            //クラス方式選択 (1:法定クラス 2:実クラス 3:統計学級)
            $opt = array(1, 2, 3);
            $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];

            /* Edit by HPA for CurrentCursor 2020-01-10 start */
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"current_cursor('HR_CLASS_TYPE1'); return btn_submit('main');\"", "id=\"HR_CLASS_TYPE2\" onClick=\"current_cursor('HR_CLASS_TYPE2'); return btn_submit('main');\"", "id=\"HR_CLASS_TYPE3\" onclick=\"current_cursor('HR_CLASS_TYPE3')； return btn_submit('main');\"");
            /* Edit by HPA for CurrentCursor 2020-01-17 end */

            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            //学年混合チェックボックス
            $extra = $model->field["GAKUNEN_KONGOU"] == "1" ? "checked" : "";

            /* Edit by HPA for CurrentCursor 2020-01-10 start */
            $extra .= " id=\"GAKUNEN_KONGOU\" onclick=\"current_cursor('GAKUNEN_KONGOU'); return btn_submit('main');\"";
            /* Edit by HPA for CurrentCursor 2020-01-17 end */

            $arg["data"]["GAKUNEN_KONGOU"] = knjCreateCheckBox($objForm, "GAKUNEN_KONGOU", "1", $extra, "");
        }
        
        //年組コンボボックス
        $query = knjs340Query::getGradeHrClass($model);

        // Edit by HPA for CurrentCursor and PC_Talker 読み 2020-01-10 START
        $extra = "id=\"GRADE_HR_CLASS\" onChange=\"current_cursor('GRADE_HR_CLASS'); return btn_submit('main');\" aria-label = \"対象クラス\"";
        // Edit by HPA for CurrentCursor and PC_Talker 読み 2020-01-17 END

        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);
        //校種取得
        $model->schKind = $db->getOne(knjs340Query::getSchoolKind($model));

        //対象月コンボボックス
        makeMonthSemeCmb($objForm, $arg, $db, $model);
        
        //対象月の月と学期を取得
        $target_month_array = explode("-", $model->field["TARGET_MONTH"]); //月、学期がハイフン区切りだからそれを配列にする
        $model->MONTH    = $target_month_array[0];
        $model->SEMESTER = $target_month_array[1];
        
        //対象日テキスト作成
        //選択されている月の値を設定
        $nen = ($model->MONTH < '04') ? (CTRL_YEAR + 1) : CTRL_YEAR;
        $lastday = date("t", mktime( 0, 0, 0, $model->MONTH, 1, $nen ));
        //学期の境目をまたいでないかチェック
        $query = knjs340Query::checker(CTRL_YEAR, $model->SEMESTER);
        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "SMONTH", $row['SMONTH']);
        knjCreateHidden($objForm, "SDAY", $row['SDAY']);
        knjCreateHidden($objForm, "EMONTH", $row['EMONTH']);
        knjCreateHidden($objForm, "EDAY", $row['EDAY']);
        knjCreateHidden($objForm, "SETMONTH", $model->MONTH);
        knjCreateHidden($objForm, "LASTDAY", $lastday);
        //対象日の初期値をセット
        if ($model->MONTH == $row['EMONTH']) {
            $model->field["TARGET_DAY"] = $row['EDAY'];
        }
        else {
            $model->field["TARGET_DAY"] = $lastday;
        }
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["TARGET_DAY"] = knjCreateTextBox($objForm,  sprintf("%02s", $model->field["TARGET_DAY"]), "TARGET_DAY", 2, 2, $extra);


        // 帳票選択　(1:A3横 2:A4縦 3:Cパターン 4:A4横2パターン 5:A4横3パターン 6:A4横4パターン)
        $div = $model->Properties["knjs340FORM_DIV"];
        if ($div != '') {
            if($div == '6'){
                $model->field["FORM_DIV"] = $model->field["FORM_DIV"] == "" ? "1" : $model->field["FORM_DIV"];
            } else {
                $model->field["FORM_DIV"] = $div;
            }
        } else {
            if ($model->field["FORM_DIV"] == "") {
                $model->field["FORM_DIV"] = $model->Properties["useFi_Hrclass"] == "1" ? "2" : "1";
            }
        }
        if ($model->Properties["knjs340FORM_DIV"] == '6') {
            $opt = array(1, 2, 3, 4);
            $arr = ['A4横(7日以上連・断続欠席者調査あり)','A4横(7日以上連・断続欠席者調査なし)','A4横(7日以上連・断続欠席者調査あり・訪問生用)','A4横(7日以上連・断続欠席者調査なし・訪問生用)'];
            $arg["showFORM_B"] = "1";
        } else {
            $arg["showFORM_A"] = "1";
            $opt = array(1, 2, 3, 4);
            $arr = ['A3横','A4縦','A3縦/A4横(出席統計有)','A4横2(連続欠席調査)','A4横3(連続欠席調査)'];
            if ($model->Properties["useEventAbbv"] == "1" || $div == "5") {
                // 帳票選択　(5:A4横3パターン)
                $opt[] = 5;
            }
        }
        $sbstr = " onclick=\"current_cursor(\"FORM_DIV$o\"); return btn_submit('main')\"";
        $extra = [];
        foreach ($opt as $o) {
            if ($div == '' || $div == $o || $div == '6') {
                $arg["form".$o] = "1";
            }
            $bb = $arr[$o-1];
            /* Edit by HPA for PC_Talker 読み 2020-01-10 start */
            $extra[] = "id=\"FORM_DIV".$o."\" aria-label =\"帳票選択の$bb\"".$sbstr;
            /* Edit by HPA for PC_Talker 読み 2020-01-17 end */

        }
        if ($arg["form1"] || $arg["form2"] || $arg["form3"]) {
           $arg["form_div_line1"] = "<br>　　　　　　　　　　"; 
        }
        $radioArray = knjCreateRadio($objForm, "FORM_DIV", $model->field["FORM_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //15行で表示
        if ($div != '4' && $div != '5' && $div != '6') {
            $arg["showFORM_SELECT"] = "1";
            $extra = ( ($model->field["FORM_DIV"] != "4" && $model->field["FORM_DIV"] != "5") && ($model->field["FORM_SELECT"] == "1" || $model->cmd == '' && $model->Properties["useSpecial_Support_Hrclass"] == "1")) ? "checked" : "";
            $extra .= " id=\"FORM_SELECT\"";
            $extra .= ($model->field["FORM_DIV"] == "4" || $model->field["FORM_DIV"] == "5" ) ? "disabled" : "";
            $arg["data"]["FORM_SELECT"] = knjCreateCheckBox($objForm, "FORM_SELECT", "1", $extra, "");
        }

        //未在籍者は詰めて印字する
        $extra = ($model->field["PRINT_ZAISEKI_ONLY"] == "1") ? "checked" : "";

        /* Edit by HPA for PC_Talker 読み 2020-01-10 start */
        $extra .= " id=\"PRINT_ZAISEKI_ONLY\" aria-label = \"帳票選択の未在籍者は詰めて印字する\"";
        /* Edit by HPA for PC_Talker 読み 2020-01-17 end */

        $arg["data"]["PRINT_ZAISEKI_ONLY"] = knjCreateCheckBox($objForm, "PRINT_ZAISEKI_ONLY", "1", $extra, "");

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjs340Form1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //印刷ボタン

    // Edit by HPA for PC-Talker 読み 2018-01-10 start
    $extra = " id=\"btn_print\" onclick=\"current_cursor('btn_print'); return newwin('" . SERVLET_URL . "');\"";
    // Edit by HPA for PC-Talker 読み 2018-01-17 start

    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    
    // Edit by HPA for PC-Talker 読み 2018-01-10 start
    $extra = "onclick=\"closeWin();\" aria-label = \"終了\"";
    // Edit by HPA for PC-Talker 読み 2018-01-17 end

    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model, $db)
{
    knjCreateHidden($objForm, "PRGID", "KNJS340");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "useFi_Hrclass", $model->Properties["useFi_Hrclass"]);
    knjCreateHidden($objForm, "useSpecial_Support_Hrclass", $model->Properties["useSpecial_Support_Hrclass"]);
    knjCreateHidden($objForm, "RESTRICT_FLG", (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) ? "1" : "0");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
    knjCreateHidden($objForm, "knjs340FORM_DIV", $model->Properties["knjs340FORM_DIV"]);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $result->free();

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model) {
    if ($model->field["TARGET_MONTH"] == '') {
        // 初期値はログイン日付の月
        $ctrl_date = preg_split("/-/", CTRL_DATE);
        $query = knjs340Query::getSemesAll();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($i = $row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
                $month = ($i > 12) ? ($i - 12) : $i;

                //対象月名称取得
                $monthname = $db->getOne(knjs340Query::getMonthName($month, $model));
                if ($monthname) {
                    if (((int) $ctrl_date[1]) == $month) {
                        $model->field["TARGET_MONTH"] = $month.'-'.$row["SEMESTER"];
                    }
                }
            }
        }
    }
    $value_flg = false;
    $query = knjs340Query::getSemesAll();
    $result = $db->query($query);
    $opt_month = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        for ($i = $row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
            $month = ($i > 12) ? ($i - 12) : $i;

            //対象月名称取得
            $monthname = $db->getOne(knjs340Query::getMonthName($month, $model));
            if ($monthname) {
                $opt_month[] = array("label" => $monthname." (".$row["SEMESTERNAME"].") ",
                                     "value" => $month.'-'.$row["SEMESTER"]);
                if ($model->field["TARGET_MONTH"] == $month.'-'.$row["SEMESTER"]) {
                    $value_flg = true;
                }
            }
        }
    }
    $result->free();

    //初期値はログイン月
    $ctrl_date = preg_split("/-/", CTRL_DATE);
    $model->field["TARGET_MONTH"] = ($model->field["TARGET_MONTH"] && $value_flg) ? $model->field["TARGET_MONTH"] : (int)$ctrl_date[1].'-'.CTRL_SEMESTER;

    // EDIT by HPA for CurrentCursor and PC_Talker 読み 2020-01-10 START
    $arg["data"]["TARGET_MONTH"] = knjCreateCombo($objForm, "TARGET_MONTH", $model->field["TARGET_MONTH"], $opt_month, "onChange=\"current_cursor('TARGET_MONTH'); return btn_submit('main');\" id=\"TARGET_MONTH\" aria-label = \"対象月\"", 1);
    // EDIT by HPA for CurrentCursor and PC_Talker 読み 2020-01-17 END
}
?>
