<?php

require_once('for_php7.php');

class knjd617cForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd617cForm1", "POST", "knjd617cindex.php", "", "knjd617cForm1");
        //DB接続
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        $query = knjd617cQuery::getSemester();
        $extra = "onchange=\"return btn_submit('gakki');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }
        //学校名取得
        $query = knjd617cQuery::getSchoolname();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //学年コンボ
        $extra = "onChange=\"return btn_submit('knjd617c');\"";
        $query = knjd617cQuery::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //テスト種別コンボ
        $query = knjd617cQuery::getTestItem($model->field["SEMESTER"]);
        $opt = array();
        $value = $model->field["SUB_TESTKINDCD"];
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            /******************************************************/
            /* アンダーバーの後ろの数字は切替コードです。         */
            /* (テスト種別コード + テスト項目コード)_(切替コード) */
            /******************************************************/
            if (preg_match('/(0101|0102|0201|0202)/', $row["VALUE"])) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"] . "_1");
            } else if ($row["VALUE"] == '9900' && $model->field["SEMESTER"] == '9') {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"] . "_1");
                $opt[] = array('label' => $row["VALUE"] . ':学年評定',
                               'value' => $row["VALUE"] . "_2");
            } else {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"] . "_2");
            }

            if (preg_match("/^{$row["VALUE"]}_./", $value)) $value_flg = true;
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "onChange=\"return btn_submit('knjd617c');\"";
        $arg["data"]["SUB_TESTKINDCD"] = knjCreateCombo($objForm, "SUB_TESTKINDCD", $value, $opt, $extra, 1);

        //出欠集計範囲（累計・学期）ラジオボタン 1:累計 2:学期
        $model->field["DATE_DIV"] = $model->field["DATE_DIV"] ? $model->field["DATE_DIV"] : '1';
        $opt_datediv = array(1, 2);
        $extra2 = " onclick=\"return btn_submit('knjd617c');\"";
        $extra = array("id=\"DATE_DIV1\"".$extra2, "id=\"DATE_DIV2\"".$extra2);
        $radioArray = knjCreateRadio($objForm, "DATE_DIV", $model->field["DATE_DIV"], $extra, $opt_datediv, get_count($opt_datediv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出欠集計開始日付
        $seme_kind = $model->field["SEMESTER"] . $value;
        $semesterDetailS = $model->semesterDetailS[$schoolCode][$seme_kind];
        $query = knjd617cQuery::getSemesterDetailMst(CTRL_YEAR, $semesterDetailS);
        $result = $db->query($query);
        $sDate = $model->control["学期開始日付"][$model->field["SEMESTER"]];//日付がない場合、学期開始日付を使用する。
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $sDate = $row["SDATE"];//学期詳細マスタの終了日付
        }
        $result->free();
        $sDate = str_replace("-", "/", $sDate);
        //日付が学期の範囲外の場合、学期開始日付を使用する。
        if ($sDate < $model->control["学期開始日付"][$model->field["SEMESTER"]] || 
            $sDate > $model->control["学期終了日付"][$model->field["SEMESTER"]]) {
            $sDate = $model->control["学期開始日付"][$model->field["SEMESTER"]];
        }
        //累計の場合、出欠集計範囲の開始日は、学期詳細マスタの詳細コード＝１の開始日とする。
        if ($model->field["DATE_DIV"] == "1") {
            $query = knjd617cQuery::getSemesterDetailMst(CTRL_YEAR, "1");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sDate = $row["SDATE"];
            }
            $result->free();
            $sDate = str_replace("-", "/", $sDate);
        }
        $objForm->ae(createHiddenAe("SDATE", $sDate));
        $arg["data"]["SDATE"] = $sDate;

        //出欠集計終了日付
        $seme_kind = $model->field["SEMESTER"] . substr($value, 0, 4);
        $semesterDetailE = $model->semesterDetailE[$schoolCode][$seme_kind];
        $query = knjd617cQuery::getSemesterDetailMst(CTRL_YEAR, $semesterDetailE);
        $result = $db->query($query);
        $eDate = CTRL_DATE;//日付がない場合、学籍処理日を使用する。
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eDate = $row["EDATE"];//学期詳細マスタの終了日付
        }
        $result->free();
        $eDate = str_replace("-", "/", $eDate);
        //日付が学期の範囲外の場合、学期終了日付を使用する。
        if ($eDate < $model->control["学期開始日付"][$model->field["SEMESTER"]] || 
            $eDate > $model->control["学期終了日付"][$model->field["SEMESTER"]]) {
            $eDate = $model->control["学期終了日付"][$model->field["SEMESTER"]];
        }
        $objForm->ae(createHiddenAe("DATE", $eDate));
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $eDate);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //単位保留チェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT4");

        //空行チェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT5");

        //コース毎に改頁チェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT_COURSE");

        //総合的な学習の時間を表示しないチェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT_SOUGOU");

        //生徒別授業時数チェックボックス
        makeCheckBox($objForm, $arg, $model, "SEITO_BETU_JUGYOU_JISU");

        //欠点テキストボックス
        $extra = "style=\"text-align:right;\" onBlur=\"return toInteger(this.value);\"";
        $arg["data"]["KETTEN"] = knjCreateTextBox($objForm, $model->field["KETTEN"], "KETTEN", 4, 2, $extra);

        //総合順位出力ラジオボタン 1:学級 2:学年 3:コース
        $opt_rank = array(1, 2, 3); //1:学級はカットになった為htmlのところでカットした(帳票の関係で送る値を変えないため)
        $model->field["OUTPUT_RANK"] = ($model->field["OUTPUT_RANK"] == "") ? "2" : $model->field["OUTPUT_RANK"];
        $extra = array("id=\"OUTPUT_RANK1\"", "id=\"OUTPUT_RANK2\"", "id=\"OUTPUT_RANK3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_RANK", $model->field["OUTPUT_RANK"], $extra, $opt_rank, get_count($opt_rank));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //順位の基準点ラジオボタン 1:総合点 2:平均点
        $model->field["OUTPUT_KIJUN"] = $model->field["OUTPUT_KIJUN"] ? $model->field["OUTPUT_KIJUN"] : '2';
        $opt_kijun = array(1, 2);
        $extra = array("id=\"OUTPUT_KIJUN1\"", "id=\"OUTPUT_KIJUN2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_KIJUN", $model->field["OUTPUT_KIJUN"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //最大科目数ラジオボタン 1:１５科目 2:２０科目
        $model->field["SUBCLASS_MAX"] = $model->field["SUBCLASS_MAX"] ? $model->field["SUBCLASS_MAX"] : '1';
        $opt_max = array(1, 2);
        $extra = array("id=\"SUBCLASS_MAX1\"", "id=\"SUBCLASS_MAX2\"");
        $radioArray = knjCreateRadio($objForm, "SUBCLASS_MAX", $model->field["SUBCLASS_MAX"], $extra, $opt_max, get_count($opt_max));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //授業日数ラジオボタン 1:授業日数 2:出席すべき日数
        $model->field["OUTPUT_LESSON"] = $model->field["OUTPUT_LESSON"] ? $model->field["OUTPUT_LESSON"] : '1';
        $opt_lesson = array(1, 2);
        $extra = array("id=\"OUTPUT_LESSON1\"", "id=\"OUTPUT_LESSON2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_LESSON", $model->field["OUTPUT_LESSON"], $extra, $opt_lesson, get_count($opt_lesson));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //フォーム選択ラジオボタン 1:45名用 2:50名用
        $form_select_Value = array(1, 2);
        $model->field["FORM_SELECT"] = ($model->field["FORM_SELECT"] == "") ? "1" : $model->field["FORM_SELECT"];
        $extra = array("id=\"FORM_SELECT1\"", "id=\"FORM_SELECT2\"");
        $radioArray = knjCreateRadio($objForm, "FORM_SELECT", $model->field["FORM_SELECT"], $extra, $form_select_Value, get_count($form_select_Value));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //注意・超過のタイトル
        $arg["data"]["TYUI_TYOUKA_TITLE"] = "欠課数上限値（履修／修得）<br>　　　　　　　　";

        //注意・超過ラジオ
        $opt = array(1, 2); //1:注意 2:超過
        $model->field["TYUI_TYOUKA"] = ($model->field["TYUI_TYOUKA"] == "") ? "1" : $model->field["TYUI_TYOUKA"];
        $extra = array("id=\"TYUI_TYOUKA1\"", "id=\"TYUI_TYOUKA2\"");
        $radioArray = knjCreateRadio($objForm, "TYUI_TYOUKA", $model->field["TYUI_TYOUKA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd617cForm1.html", $arg); 
    }
}
/**************************************** 以下関数 **************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $schoolName = "", $semester = "")
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = createCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = $db->query(knjd617cQuery::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = createCombo($objForm, "CLASS_NAME", "", $row1, $extra, 12);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = createCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 12);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = createBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = createBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = createBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = createBtn($objForm, "btn_rights", ">>", $extra);
}

//チェックボックスを作成する
function makeCheckBox(&$objForm, &$arg, $model, $name)
{
    $extra  = ($model->field[$name] == "1") ? "checked" : "";
    if ($name == 'SEITO_BETU_JUGYOU_JISU') {
        if ($model->field["SEITO_BETU_JUGYOU_JISU"] == "1" || !$model->cmd) {
            $extra = "checked";
        } else {
            $extra = "";
        }
    }
    $extra .= " id=\"$name\"";

    $arg["data"][$name] = createCheckBox($objForm, $name, 1, $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //ＣＳＶボタン
    $arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"return btn_submit('csv');\"");
    //実行ボタン
    $arg["button"]["btn_print"] = createBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //閉じるボタン
    $arg["button"]["btn_end"]   = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg)
{
    $objForm->ae(createHiddenAe("DBNAME", DB_DATABASE));
    $objForm->ae(createHiddenAe("PRGID", "KNJD617C"));
    $objForm->ae(createHiddenAe("cmd"));
    //学期
    $objForm->ae(createHiddenAe("SEME_DATE", $seme));
    //学期開始日付
    $objForm->ae(createHiddenAe("SEME_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]));
    //学期終了日付
    $objForm->ae(createHiddenAe("SEME_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]));
    //学期終了日付
    $objForm->ae(createHiddenAe("SEME_FLG", $semeflg));
    //年度
    $objForm->ae(createHiddenAe("YEAR", CTRL_YEAR));
    //教育課程コードを使用するか
    $objForm->ae(createHiddenAe("useCurriculumcd", $model->Properties["useCurriculumcd"]));
    //切替コード
    knjCreateHidden($objForm, "SCORE_FLG");
    //テスト種別コード + テスト項目コード
    knjCreateHidden($objForm, "TESTKINDCD");

    /*** 以下、ＣＳＶ出力用 ***/
    //選択クラスを保持
    $objForm->ae(createHiddenAe("selectdata"));
    //選択学期名を保持
    $objForm->ae(createHiddenAe("selectSemeName"));
    //選択テスト名を保持
    $objForm->ae(createHiddenAe("selectTestName"));

    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
}

//コンボ作成
function createCombo(&$objForm, $name, $value, $options, $extra, $size)
{
    $objForm->ae( array("type"      => "select",
                        "name"      => $name,
                        "size"      => $size,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "options"   => $options));
    return $objForm->ge($name);
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"       => "checkbox",
                        "name"       => $name,
                        "extrahtml"  => $extra,    
                        "value"      => $value));

    return $objForm->ge($name);
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

?>
