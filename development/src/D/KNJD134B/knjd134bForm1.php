<?php

require_once('for_php7.php');

class knjd134bForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd134bForm1", "POST", "knjd134bindex.php", "", "knjd134bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //名称マスタ D校種08チェック
        $model->che_school_kind = "D".$model->urlSchoolKind."08";
        $model->count = $db->getone(knjd134bquery::getNameMstche($model));

        //科目コンボボックス
        $query = knjd134bQuery::getSubclassList($model);
        $extra = "onchange=\"return btn_submit('knjd134b');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1);

        //出欠集計範囲（累計・学期）ラジオボタン 1:累計 2:学期
        $model->field["DATE_DIV"] = $model->field["DATE_DIV"] ? $model->field["DATE_DIV"] : '1';
        $opt_datediv = array(1, 2);
        $click = " onclick=\"return btn_submit('knjd134b');\"";
        $extra = array("id=\"DATE_DIV1\"".$click, "id=\"DATE_DIV2\"".$click);
        $radioArray = knjCreateRadio($objForm, "DATE_DIV", $model->field["DATE_DIV"], $extra, $opt_datediv, get_count($opt_datediv));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出欠集計開始日付
        $seme = ($model->field["DATE_DIV"] == "1") ? "9" : CTRL_SEMESTER;
        $sDate = $model->control["学期開始日付"][$seme];
        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //出欠集計終了日付
        $eDate = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $eDate);

        //終了日付チェック用
        $seme_edate = $model->control["学期終了日付"][$seme];
        knjCreateHidden($objForm, "SEME_EDATE", $seme_edate);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //フォーム選択ラジオボタン 1:45名用 2:50名用
        $form_select_Value = array(1, 2);
        $model->field["FORM_SELECT"] = ($model->field["FORM_SELECT"] == "") ? "1" : $model->field["FORM_SELECT"];
        $extra = array("id=\"FORM_SELECT1\"", "id=\"FORM_SELECT2\"");
        $radioArray = knjCreateRadio($objForm, "FORM_SELECT", $model->field["FORM_SELECT"], $extra, $form_select_Value, get_count($form_select_Value));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //注意・超過ラジオ
        $opt = array(1, 2); //1:注意 2:超過
        $model->field["TYUI_TYOUKA"] = ($model->field["TYUI_TYOUKA"] == "") ? "1" : $model->field["TYUI_TYOUKA"];
        $extra = array("id=\"TYUI_TYOUKA1\"", "id=\"TYUI_TYOUKA2\"");
        $radioArray = knjCreateRadio($objForm, "TYUI_TYOUKA", $model->field["TYUI_TYOUKA"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd134bForm1.html", $arg);
    }
}

/**************************************** 以下関数 **************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = $db->query(knjd134bQuery::getChairList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //講座一覧作成
    $extra = "multiple style=\"width:100%;\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 15);

    //講座対象作成
    $extra = "multiple style=\"width:100%;\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

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

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", PROGRAMID);
    knjCreateHidden($objForm, "cmd");

    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "chikokuHyoujiFlg", $model->Properties["chikokuHyoujiFlg"]);
    knjCreateHidden($objForm, "useTestCountflg", $model->Properties["useTestCountflg"]);
    knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);

    //学籍処理日が学期範囲外の場合、学期終了日を使用する。
    $sdate = str_replace("/", "-", $model->control["学期開始日付"][CTRL_SEMESTER]);
    $edate = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);
    if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
        $execute_date = CTRL_DATE;//初期値
    } else {
        $execute_date = $edate;//初期値
    }
    knjCreateHidden($objForm, "execute_date", $execute_date);
}
