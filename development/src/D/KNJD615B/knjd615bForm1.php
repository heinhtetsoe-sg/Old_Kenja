<?php

require_once('for_php7.php');

class knjd615bForm1
{
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd615bForm1", "POST", "knjd615bindex.php", "", "knjd615bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        $query = knjd615bQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], "onchange=\"return btn_submit('gakki');\"", 1);
        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }

        //学年コンボ
        $query = knjd615bQuery::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "onChange=\"return btn_submit('change_grade');\"", 1);

        //テスト種別コンボ
        $extra = "STYLE=\"WIDTH:140\" ";
        if ($model->field["SEMESTER"] == "9") {
            $extra .= "onChange=\"return btn_submit('change_testkindcd');\" ";
        }
        $query = knjd615bQuery::getTestItem($model->field["SEMESTER"]);
        makeCmb($objForm, $arg, $db, $query, "SUB_TESTKINDCD", $model->field["SUB_TESTKINDCD"], $extra, 1, $model->field["SEMESTER"]);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);
        //職一覧リストToリスト
        makeJobListToList($objForm, $arg, $db, $model);

        /********************/
        /* チェックボックス */
        /********************/
        //単位保留チェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT4");
        //空行チェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT5");
        //コース毎に改頁チェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT_COURSE");
        //総合的な学習の時間を表示しないチェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT_SOUGOU");
        //段階値出力チェックボックス
        makeCheckBox($objForm, $arg, $model, "ASSESS_LEVEL");

        /********************/
        /* テキストボックス */
        /********************/
        //欠点
        $query = knjd615bQuery::getGdat($model->field["GRADE"]);
        $h_j = $db->getOne($query);
        knjCreateHidden($objForm, "hjFlg", $h_j);
        if ($model->field["SUB_TESTKINDCD"] == "9900_2") {
            if ($model->cmd == 'change_testkindcd' || $model->cmd == 'gakki') {
                $model->field["KETTEN"] = 2;
            }
        } else {
            if ($model->cmd == 'change_testkindcd' || $model->cmd == 'gakki' || $model->hjFlg != $h_j || $model->cmd == '') {
                if ($h_j == 'J') {
                    $model->field["KETTEN"] = 60;
                } else {
                    $model->field["KETTEN"] = 30;
                }
            }
        }
        $extra = " onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
        $arg["data"]["KETTEN"] = knjCreateTextBox($objForm, $model->field["KETTEN"], "KETTEN", 3, 3, $extra);
        if (($model->Properties["checkKettenDiv"] != '1' && $model->Properties["checkKettenDiv"] != '2') || ($model->Properties["checkKettenDiv"] == '1' && $model->field["SEMESTER"] == '9') || ($model->Properties["checkKettenDiv"] == '2' && $model->field["SEMESTER"] == '9' && $model->field["SUB_TESTKINDCD"] == "9900_2")) {
            $arg["KETTEN_HYOUJI"] = 1; //null以外なら何でもいい
        } else {
            unset($arg["KETTEN_HYOUJI"]);
        }

        /****************/
        /* ラジオボタン */
        /****************/
        //総合順位出力ラジオボタン 1:学級 2:学年 3:コース
        $opt_rank = array(1, 2, 3); //1:学級はカットになった為htmlのところでカットした(帳票の関係で送る値を変えないため)
        $model->field["OUTPUT_RANK"] = ($model->field["OUTPUT_RANK"] == "") ? "2" : $model->field["OUTPUT_RANK"];
        $extra = array("id=\"OUTPUT_RANK1\"", "id=\"OUTPUT_RANK2\"", "id=\"OUTPUT_RANK3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_RANK", $model->field["OUTPUT_RANK"], $extra, $opt_rank, get_count($opt_rank));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //順位の基準点ラジオボタン 1:総合点 2:平均点 3:偏差値
        $model->field["OUTPUT_KIJUN"] = $model->field["OUTPUT_KIJUN"] ? $model->field["OUTPUT_KIJUN"] : '1';
        $opt_kijun = array(1, 2, 3);
        $extra = array("id=\"OUTPUT_KIJUN1\"", "id=\"OUTPUT_KIJUN2\"", "id=\"OUTPUT_KIJUN3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_KIJUN", $model->field["OUTPUT_KIJUN"], $extra, $opt_kijun, get_count($opt_kijun));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //最大科目数ラジオボタン 1:１５科目 2:２０科目
        $model->field["SUBCLASS_MAX"] = $model->field["SUBCLASS_MAX"] ? $model->field["SUBCLASS_MAX"] : '1';
        $opt_max = array(1, 2);
        $extra = array("id=\"SUBCLASS_MAX1\"", "id=\"SUBCLASS_MAX2\"");
        $radioArray = knjCreateRadio($objForm, "SUBCLASS_MAX", $model->field["SUBCLASS_MAX"], $extra, $opt_max, get_count($opt_max));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //フォーム選択ラジオボタン 1:45名用 2:50名用
        $form_select_Value = array(1, 2);
        $model->field["FORM_SELECT"] = ($model->field["FORM_SELECT"] == "") ? "1" : $model->field["FORM_SELECT"];
        $extra = array("id=\"FORM_SELECT1\"", "id=\"FORM_SELECT2\"");
        $radioArray = knjCreateRadio($objForm, "FORM_SELECT", $model->field["FORM_SELECT"], $extra, $form_select_Value, get_count($form_select_Value));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //カレンダーコントロール
        $value = isset($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-","/",CTRL_DATE);
        $arg["el"]["DATE"] = View::popUpCalendar($objForm, "DATE", $value);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd615bForm1.html", $arg); 
    }
}
/**************************************** 以下関数 **************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $semester = "")
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
    $result = $db->query(knjd615bQuery::getAuth($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px; height:160px;\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = createCombo($objForm, "CLASS_NAME", "", $row1, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:230px; height:160px;\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = createCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

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

//クラス一覧リストToリスト作成
function makeJobListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $row2 = array();
    $selectedJobs = preg_split("/,/", $model->field["selectedJobs"]);
    $result = $db->query(knjd615bQuery::getJob($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $selectedJobs)) {
            $row2[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        } else {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //職一覧作成
    $extra = "multiple style=\"width:230px; height:120px;\" ondblclick=\"move1J('left')\"";
    $arg["data"]["JOB_NAME"] = createCombo($objForm, "JOB_NAME", "", $row1, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:230px; height:120px;\" ondblclick=\"move1J('right')\"";
    $arg["data"]["JOB_SELECTED"] = createCombo($objForm, "JOB_SELECTED", "", $row2, $extra, 15);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"movesJ('left');\"";
    $arg["button"]["btn_leftsj"] = knjCreateBtn($objForm, "btn_leftsj", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1J('left');\"";
    $arg["button"]["btn_left1j"] = knjCreateBtn($objForm, "btn_left1j", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1J('right');\"";
    $arg["button"]["btn_right1j"] = knjCreateBtn($objForm, "btn_right1j", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"movesJ('right');\"";
    $arg["button"]["btn_rightsj"] = knjCreateBtn($objForm, "btn_rightsj", ">>", $extra);
}

//チェックボックスを作成する
function makeCheckBox(&$objForm, &$arg, $model, $name)
{
    $check  = ($model->field[$name] == "1") ? "checked" : "";
    $check .= " id=\"$name\"";
    $value  = isset($model->field[$name]) ? $model->field[$name] : 1;

    $arg["data"][$name] = knjCreateCheckBox($objForm, $name, $value, $check, "");
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //ＣＳＶボタン
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", "onclick=\"return newwin('".SERVLET_URL."', 'csv');\"");
    //印刷ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "', 'print');\"");
    //終了ボタン
    $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg)
{
    knjCreateHidden($objForm, "checkKettenDiv", $model->Properties["checkKettenDiv"]);

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD615B");
    knjCreateHidden($objForm, "cmd");

    //学期
    knjCreateHidden($objForm, "SEME_DATE", $seme);
    //学期開始日付
    knjCreateHidden($objForm, "SEME_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
    //学期終了日付
    knjCreateHidden($objForm, "SEME_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
    //学期
    knjCreateHidden($objForm, "SEME_FLG", $semeflg);
    //年度
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    //教育課程コード
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    //切替コード
    knjCreateHidden($objForm, "SCORE_FLG");
    //テスト種別コード + テスト項目コード
    knjCreateHidden($objForm, "TESTKINDCD");

    /*** 以下、ＣＳＶ出力用 ***/
    //選択クラスを保持
    knjCreateHidden($objForm, "selectdata");
    //選択学期名を保持
    knjCreateHidden($objForm, "selectSemeName");
    //選択テスト名を保持
    knjCreateHidden($objForm, "selectTestName");
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
    knjCreateHidden($objForm, "selectedJobs", $model->field["selectedJobs"]);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
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
?>
