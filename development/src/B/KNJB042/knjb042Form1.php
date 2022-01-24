<?php

require_once('for_php7.php');

class knjb042Form1 {
    function main(&$model){
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb042Form1", "POST", "knjb042index.php", "", "knjb042Form1");
        $db = Query::dbCheckOut();

        $arg["data"]["YEAR"] = CTRL_YEAR;

        /**********/
        /* ラジオ */
        /**********/
        //時間割種別・指定日付
        $opt = array(1, 2); //1:基本時間割 2:通常時間割
        $model->field["JIKANWARI_SYUBETU"] = ($model->field["JIKANWARI_SYUBETU"] == "") ? "1" : $model->field["JIKANWARI_SYUBETU"];
        $click = "onclick=\"jikanwari(this);\"";
        $extra = array($click." id=\"JIKANWARI_SYUBETU1\"", $click." id=\"JIKANWARI_SYUBETU2\"");
        $radioArray = knjCreateRadio($objForm, "JIKANWARI_SYUBETU", $model->field["JIKANWARI_SYUBETU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if( $model->field["JIKANWARI_SYUBETU"] == 2 ) {             //通常時間割選択時
            $dis_jikan = "disabled";                    //時間割選択コンボ使用不可
            $dis_date  = "";                            //指定日付テキスト使用可
            $arg["Dis_Date"] = " dis_date(false); " ;
        } else {                                        //基本時間割選択時
            $dis_jikan = "";                            //時間割選択コンボ使用可
            $dis_date  = "disabled";                    //指定日付テキスト使用不可
            $arg["Dis_Date"] = " dis_date(true); " ;
        }

        //時間割選択コンボボックスを作成
        $opt = knjb042Query::getBscHdQuery($model, $db);
        $opt = isset($opt) ? $opt : array();
        $extra = $dis_jikan;
        $arg["data"]["TITLE"] = knjCreateCombo($objForm, "TITLE", $model->field["TITLE"], $opt, $extra, 1);

        //指定日付(開始)
        if ($model->field["JIKANWARI_SYUBETU"] == 2){
            if (!isset($model->field["SDATE"])) {
                $model->field["SDATE"] = str_replace("-","/",CTRL_DATE);
            }
            //指定日を含む指定週の開始日(月曜日)と終了日(日曜日)を取得
            common::DateConv2($model->field["SDATE"], $OutDate1, $OutDate2, 1);
            $model->field["EDATE"] = $OutDate2;
        } else {
            $model->field["SDATE"] = "";
            $model->field["EDATE"] = "";
        }
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"], "reload=true");

        //指定日付(終了)
        $extra = "disabled";
        $arg["data"]["EDATE"] = knjCreateTextBox($objForm, $model->field["EDATE"], "EDATE", 12, "", $extra);

        //学年
        $query = knjb042Query::getGrade($model);
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE"] == $row["VALUE"]) $value_flg = true;
        }
        $model->field["GRADE"] = ($model->field["GRADE"] && $value_flg) ? $model->field["GRADE"] : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('knjb042');\"";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $opt, $extra, 1);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        /**********/
        /* ラジオ */
        /**********/
        //出力項目(上段)
        $opt = array(1, 2); //1:科目名 2:講座名
        $model->field["SUBCLASS_CHAIR_DIV"] = ($model->field["SUBCLASS_CHAIR_DIV"] == "") ? "1" : $model->field["SUBCLASS_CHAIR_DIV"];
        $extra = array("id=\"SUBCLASS_CHAIR_DIV1\"", "id=\"SUBCLASS_CHAIR_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "SUBCLASS_CHAIR_DIV", $model->field["SUBCLASS_CHAIR_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        //出力項目(下段)
        $opt = array(1, 2); //1:職員名 2:施設名
        $model->field["SYUTURYOKU_KOUMOKU"] = ($model->field["SYUTURYOKU_KOUMOKU"] == "") ? "1" : $model->field["SYUTURYOKU_KOUMOKU"];
        $extra = array("id=\"SYUTURYOKU_KOUMOKU1\"", "id=\"SYUTURYOKU_KOUMOKU2\"");
        $radioArray = knjCreateRadio($objForm, "SYUTURYOKU_KOUMOKU", $model->field["SYUTURYOKU_KOUMOKU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

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
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", str_replace("-","/",CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJB042");
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        //JavaScriptで参照するため
        knjCreateHidden($objForm, "T_YEAR");
        knjCreateHidden($objForm, "T_BSCSEQ");
        knjCreateHidden($objForm, "T_SEMESTER");
        //チェック用
        knjCreateHidden($objForm, "CHK_SDATE", $model->control['学期開始日付'][9]);
        knjCreateHidden($objForm, "CHK_EDATE", $model->control['学期終了日付'][9]);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();
        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb042Form1.html", $arg);
    }
}
/*********************************************** 以下関数 *******************************************************/
/******************/
/* リストToリスト */
/******************/
function makeListToList(&$objForm, &$arg, $db, $model) {
    //クラス一覧
    $row1 = array();
    $result = $db->query(knjb042Query::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $row1, $extra, 20);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
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
