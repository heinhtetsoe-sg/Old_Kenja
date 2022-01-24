<?php

require_once('for_php7.php');

class knja210aForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]  = $objForm->get_start("knja210aForm1", "POST", "knja210aindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        /**********/
        /* コンボ */
        /**********/
        //学期
        $query = knja210aQuery::getSemester($model);
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], "onchange=\"return btn_submit('knja210a');\"", 1);
        //教科コード
        $query = knja210aQuery::getClassMst($model);
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $model->field["CLASSCD"], "onChange=\"return btn_submit('knja210a');\"", 1);
        //科目コード
        $query = knja210aQuery::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], "onChange=\"return btn_submit('knja210a');\"", 1);
        //講座コード
        $query = knja210aQuery::getChairDat($model);
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], "", 1);

        /**********/
        /* ラジオ */
        /**********/
        //フォーム選択 (1:5列×5行 2:6列×7行)
        $opt = array(1, 2);
        $model->field["FORM_SENTAKU"] = ($model->field["FORM_SENTAKU"] == "") ? "1" : $model->field["FORM_SENTAKU"];
        $extra = array("id=\"FORM_SENTAKU1\"", "id=\"FORM_SENTAKU2\"");
        $radioArray = knjCreateRadio($objForm, "FORM_SENTAKU", $model->field["FORM_SENTAKU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力情報選択(1:出身学校、2:ふりがな)
        $disflg = $model->field["SCHOOLNAME"] == "1" ? "" : " disabled ";
        $opt = array(1, 2);
        $model->field["PRINT_INFO"] = ($model->field["PRINT_INFO"] == "") ? "1" : $model->field["PRINT_INFO"];
        $extra = array("id=\"PRINT_INFO1\"".$disflg, "id=\"PRINT_INFO2\"".$disflg);
        $radioArray = knjCreateRadio($objForm, "PRINT_INFO", $model->field["PRINT_INFO"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********************/
        /* チェックボックス */
        /********************/
        //学籍番号を出力する
        $extra = "checked='checked' id='PRINT_SCHREGNO' ";
        $arg["data"]["PRINT_SCHREGNO"] = knjCreateCheckBox($objForm, "PRINT_SCHREGNO", 1, $extra);

        //出身学校チェックボックス
        $extra = " id=\"SCHOOLNAME\" onchange=submit('knja210a');";
        $extra .=  $model->field["SCHOOLNAME"] == "1" ? " checked " : "";
        $arg["data"]["SCHOOLNAME"] = knjCreateCheckBox($objForm, "SCHOOLNAME", "1", $extra);

        /********/
        /* 日付 */
        /********/
        //作成日付
        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);
        //対象日
        $model->field["TAISYOU_DATE"] = $model->field["TAISYOU_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["TAISYOU_DATE"];
        $arg["data"]["TAISYOU_DATE"] = View::popUpCalendar($objForm, "TAISYOU_DATE", $model->field["TAISYOU_DATE"],"reload=true");
        //チェック用hidden
        knjCreateHidden($objForm, "DEFOULTDATE", $model->field["TAISYOU_DATE"]);

        /**********/
        /* ボタン */
        /**********/
        //印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA210A");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "GRADE");
        knjCreateHidden($objForm, "HR_CLASS");
        knjCreateHidden($objForm, "NAME_SHOW");
        knjCreateHidden($objForm, "ATTENDCLASSCD");
        knjCreateHidden($objForm, "GROUPCD");
        knjCreateHidden($objForm, "APPDATE");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        
        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja210aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == 'CHAIRCD') {
            $chargediv_mark = ($row["CHARGEDIV"] == '1') ? '＊' : '　';
            $start = str_replace("-","/",$row["APPDATE"]);
            $end   = str_replace("-","/",$row["APPENDDATE"]);
            $opt[]= array('label' => "{$row["ATTENDCLASSCD"]}:{$row["CLASSALIAS"]} {$start}～{$end} {$row["STAFFNAME_SHOW"]} {$chargediv_mark} {$row["GROUPCD"]}",
                          'value' => "{$row["SUBCLASSCD"]},{$row["GRADE_HR_CLASS"]},{$row["STAFFCD"]},{$row["APPDATE"]},{$row["ATTENDCLASSCD"]},{$row["GROUPCD"]},{$row["CHARGEDIV"]},{$row["APPDATE"]},{$row["GRADE"]},{$row["HR_CLASS"]}"
                         );
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
