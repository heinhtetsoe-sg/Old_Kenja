<?php

require_once('for_php7.php');

class knjs342Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        /* Add by HPA for CurrentCursor 2020-01-10 start */
        $arg["TITLE"] = "健康観察簿画面";
        /* Add by HPA for CurrentCursor 2020-01-17 end */

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjs342Form1", "POST", "knjs342index.php", "", "knjs342Form1");

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
            $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "2" : $model->field["HR_CLASS_TYPE"];

            /* Edit by HPA for CurrentCursor 2020-01-10 start */
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"current_cursor('HR_CLASS_TYPE1'); return btn_submit('main');\"", "id=\"HR_CLASS_TYPE2\" onclick=\"current_cursor('HR_CLASS_TYPE2'); return btn_submit('main');\"");
            /* Edit by HPA for CurrentCursor 2020-01-17 end */

            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        } elseif ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
            $arg["useSpecial_Support_HrclassSelect"] = "1";
            //クラス方式選択 (1:法定クラス 2:実クラス 3:統計学級)
            $opt = array(1, 2, 3);
            $model->field["HR_CLASS_TYPE"] = ($model->field["HR_CLASS_TYPE"] == "") ? "1" : $model->field["HR_CLASS_TYPE"];

            /* Edit by HPA for CurrentCursor 2020-01-10 start */
            $extra = array("id=\"HR_CLASS_TYPE1\" onclick=\"current_cursor('HR_CLASS_TYPE1'); return btn_submit('main');\"", "id=\"HR_CLASS_TYPE2\" onclick=\"current_cursor('HR_CLASS_TYPE2'); return btn_submit('main');\"", "id=\"HR_CLASS_TYPE3\" onclick=\"current_cursor('HR_CLASS_TYPE3'); return btn_submit('main');\"");
            /* Edit by HPA for CurrentCursor 2020-01-17 end */

            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }

            //学年混合チェックボックス
            $extra = $model->field["GAKUNEN_KONGOU"] == "1" ? "checked" : "";

            /* Edit by HPA for CurrentCursor 2020-01-10 start */
            $extra .= " onclick=\"current_cursor('GAKUNEN_KONGOU'); return btn_submit('main');\" id=\"GAKUNEN_KONGOU\"";
            /* Edit by HPA for CurrentCursor 2020-01-17 end */

            $arg["data"]["GAKUNEN_KONGOU"] = knjCreateCheckBox($objForm, "GAKUNEN_KONGOU", "1", $extra, "");
        }

        //年組コンボボックス
        $query = knjs342Query::getGradeHrClass($model);

        /* Edit by HPA for CurrentCursor and PC_Talker 読み 2020-01-10 start */
        $extra = " id=\"GRADE_HR_CLASS\" onChange=\"current_cursor('GRADE_HR_CLASS'); return btn_submit('main');\" aria-label='対象クラス'";
        /* Edit by HPA for CurrentCursor and PC_Talker 読み 2020-01-17 end */

        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);

        //校種取得
        $model->schKind = $db->getOne(knjs342Query::getSchoolKind($model));

        //対象月コンボボックス
        makeMonthSemeCmb($objForm, $arg, $db, $model);
        
        //対象月の月と学期を取得
        $target_month_array = explode("-", $model->field["TARGET_MONTH"]); //月、学期がハイフン区切りだからそれを配列にする
        $model->MONTH    = $target_month_array[0];
        $model->SEMESTER = $target_month_array[1];
        
        //対象日テキスト作成
        //選択されている月の値を設定
        $nen = ($model->MONTH < '04') ? (CTRL_YEAR + 1) : CTRL_YEAR;
        $lastday = date("t", mktime(0, 0, 0, $model->MONTH, 1, $nen));
        //学期の境目をまたいでないかチェック
        $query = knjs342Query::checker(CTRL_YEAR, $model->SEMESTER);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        knjCreateHidden($objForm, "SMONTH", $row['SMONTH']);
        knjCreateHidden($objForm, "SDAY", $row['SDAY']);
        knjCreateHidden($objForm, "EMONTH", $row['EMONTH']);
        knjCreateHidden($objForm, "EDAY", $row['EDAY']);
        knjCreateHidden($objForm, "SETMONTH", $model->MONTH);
        knjCreateHidden($objForm, "LASTDAY", $lastday);
        //対象日の初期値をセット
        if ($model->MONTH == $row['EMONTH']) {
            $model->field["TARGET_DAY"] = $row['EDAY'];
        } else {
            $model->field["TARGET_DAY"] = $lastday;
        }
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["TARGET_DAY"] = knjCreateTextBox($objForm, sprintf("%02s", $model->field["TARGET_DAY"]), "TARGET_DAY", 2, 2, $extra);

        //15行で表示
        $extra = ($model->field["FORM_SELECT"] == "1" || $model->cmd == '' && $model->Properties["useSpecial_Support_Hrclass"] == "1") ? "checked" : "";
        $extra .= " id=\"FORM_SELECT\" aria-label = \"帳票選択の15行で印刷\"";
        $arg["data"]["FORM_SELECT"] = knjCreateCheckBox($objForm, "FORM_SELECT", "1", $extra, "");

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjs342Form1.html", $arg);
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //印刷ボタン

    /* Edit by HPA for PC_Talker 読み 2020-01-10 start */
    $extra = " id=\"btn_print\" onclick=\"current_cursor('btn_print'); return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    /* Edit by HPA for PC_Talker 読み 2020-01-17 end */

    //終了ボタン

    /* Edit by HPA for PC_Talker 読み 2020-01-10 start */
    $extra = "onclick=\"closeWin();\" aria-label='終了'";
    /* Edit by HPA for PC_Talker 読み 2020-01-17 end */

    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model, $db)
{
    knjCreateHidden($objForm, "PRGID", "KNJS342");
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
    knjCreateHidden($objForm, "knjs342FORM_DIV", $model->Properties["knjs342FORM_DIV"]);
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
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $result->free();

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model)
{
    if ($model->field["TARGET_MONTH"] == '') {
        // 初期値はログイン日付の月
        $ctrl_date = preg_split("/-/", CTRL_DATE);
        $query = knjs342Query::getSemesAll();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($i = $row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
                $month = ($i > 12) ? ($i - 12) : $i;

                //対象月名称取得
                $monthname = $db->getOne(knjs342Query::getMonthName($month, $model));
                if ($monthname) {
                    if (((int) $ctrl_date[1]) == $month) {
                        $model->field["TARGET_MONTH"] = $month.'-'.$row["SEMESTER"];
                    }
                }
            }
        }
    }
    $value_flg = false;
    $query = knjs342Query::getSemesAll();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        for ($i = $row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
            $month = ($i > 12) ? ($i - 12) : $i;

            //対象月名称取得
            $monthname = $db->getOne(knjs342Query::getMonthName($month, $model));
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

    /* Edit by HPA for CurrentCursor and PC_Talker 読み 2020-01-10 start */
    $arg["data"]["TARGET_MONTH"] = knjCreateCombo($objForm, "TARGET_MONTH", $model->field["TARGET_MONTH"], $opt_month, " id=\"TARGET_MONTH\"onChange=\"current_cursor('TARGET_MONTH'); return btn_submit('main');\" aria-label='対象月'", 1);
    /* Edit by HPA for CurrentCursor and PC_Talker 読み 2020-01-17 end */
}
