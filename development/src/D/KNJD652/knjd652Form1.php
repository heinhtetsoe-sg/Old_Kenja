<?php

require_once('for_php7.php');

class knjd652Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd652Form1", "POST", "knjd652index.php", "", "knjd652Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        $query = knjd652Query::getSemester();
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

        //学年コンボ
        $query = knjd652Query::getSelectGrade($model);
        $extra = "onChange=\"return btn_submit('knjd652');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //テスト種別コンボ
        $extra = "onChange=\"return btn_submit('knjd652');\"";
        $query = knjd652Query::getTestItem($model->field["SEMESTER"]);
        makeCmb($objForm, $arg, $db, $query, "SUB_TESTKINDCD", $model->field["SUB_TESTKINDCD"], $extra, 1);

        //出欠集計範囲（累計・学期）ラジオボタン 1:累計 2:学期
        $model->field["DATE_DIV"] = $model->field["DATE_DIV"] ? $model->field["DATE_DIV"] : '1';
        $opt_datediv = array(1, 2);
        $extra2 = " onclick=\"return btn_submit('knjd652');\"";
        $extra = array("id=\"DATE_DIV1\"".$extra2, "id=\"DATE_DIV2\"".$extra2);
        $radioArray = knjCreateRadio($objForm, "DATE_DIV", $model->field["DATE_DIV"], $extra, $opt_datediv, get_count($opt_datediv));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学期詳細コード取得
        $testkind = substr($model->field["SUB_TESTKINDCD"], 0, 4);
        $detail = $db->getRow(knjd652Query::getTestItem($model->field["SEMESTER"], $testkind), DB_FETCHMODE_ASSOC);
        $seme_detail = $detail["SEMESTER_DETAIL"];

        //出欠集計開始日付
        $query = knjd652Query::getSemesDetail($seme_detail);
        $date = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $sDate = $model->control["学期開始日付"][$model->field["SEMESTER"]];    //日付がない場合、学期開始日付を使用する。
        if ($model->field["SEMESTER"] != "9" || $testkind != "9900"){
            $query = knjd652Query::getSemesDetail($seme_detail);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sDate = $row["SDATE"];     //学期詳細マスタの開始日付
            }
            $result->free();
        }
        $sDate = str_replace("-", "/", $sDate);

        //日付が学期の範囲外の場合、学期開始日付を使用する。
        if ($sDate < $model->control["学期開始日付"][$model->field["SEMESTER"]] || 
            $sDate > $model->control["学期終了日付"][$model->field["SEMESTER"]]) {
            $sDate = $model->control["学期開始日付"][$model->field["SEMESTER"]];
        }

        //累計の場合、出欠集計範囲の開始日は、学期詳細マスタの詳細コード＝１の開始日とする。
        if ($model->field["DATE_DIV"] == "1") {
            $query = knjd652Query::getSemesDetail("1");
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $sDate = $row["SDATE"];
            $sDate = str_replace("-", "/", $sDate);
        }

        knjCreateHidden($objForm, "SDATE", $sDate);
        $arg["data"]["SDATE"] = $sDate;

        //出欠集計終了日付
        $eDate = ($model->field["DATE"]) ? $model->field["DATE"] : str_replace("-", "/", CTRL_DATE);
        knjCreateHidden($objForm, "DATE", $eDate);
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $eDate);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //単位保留チェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT4");

        //空行チェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT5");

        //総合的な学習の時間を表示しないチェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT_SOUGOU");

        //特別活動欠課数を表示しないチェックボックス
        makeCheckBox($objForm, $arg, $model, "OUTPUT_TOKUBETSU");

        //総合順位出力ラジオボタン 1:学級 2:学年 3:コース 4:クラス
        $opt_rank = array(1, 2, 3, 4); //1:学級はカットになった為htmlのところでカットした(帳票の関係で送る値を変えないため)
        $model->field["OUTPUT_RANK"] = ($model->field["OUTPUT_RANK"] == "") ? "2" : $model->field["OUTPUT_RANK"];
        $extra = array("id=\"OUTPUT_RANK1\"", "id=\"OUTPUT_RANK2\"", "id=\"OUTPUT_RANK3\"", "id=\"OUTPUT_RANK4\"");
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
        $disabled = " onclick=\"OptionUse('this');\"";
        $extra = array("id=\"SUBCLASS_MAX1\"".$disabled, "id=\"SUBCLASS_MAX2\"".$disabled);
        $radioArray = knjCreateRadio($objForm, "SUBCLASS_MAX", $model->field["SUBCLASS_MAX"], $extra, $opt_max, get_count($opt_max));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //フォーム選択ラジオボタン 1:45名用 2:50名用
        $form_select_Value = array(1, 2);
        $model->field["FORM_SELECT"] = ($model->field["FORM_SELECT"] == "") ? "1" : $model->field["FORM_SELECT"];
        $extra = array("id=\"FORM_SELECT1\"", "id=\"FORM_SELECT2\"");
        $radioArray = knjCreateRadio($objForm, "FORM_SELECT", $model->field["FORM_SELECT"], $extra, $form_select_Value, get_count($form_select_Value));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //備考欄ラジオボタン 1:有 2:無
        $opt_remark = array(1, 2);
        $model->field["REMARK_SELECT"] = ($model->field["REMARK_SELECT"] == "") ? "1" : $model->field["REMARK_SELECT"];
        $disabled = ($model->field["SUBCLASS_MAX"] == "2") ? "" : " disabled";
        $extra = array("id=\"REMARK_SELECT1\"".$disabled, "id=\"REMARK_SELECT2\"".$disabled);
        $radioArray = knjCreateRadio($objForm, "REMARK_SELECT", $model->field["REMARK_SELECT"], $extra, $opt_remark, get_count($opt_remark));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //注意・超過のタイトル
        $query = knjd652Query::getVSchooolMst();
        $v_shool_mst = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($v_shool_mst["JUGYOU_JISU_FLG"] == 1) { //1：法定時数 2：実時数
            $arg["data"]["TYUI_TYOUKA_TITLE"] = "欠課数上限値";
        } else {
            $arg["data"]["TYUI_TYOUKA_TITLE"] = "上限値コメント表示選択<br>　　　　　　　　";
        }

        //注意・超過ラジオ
        $opt = array(1, 2); //1:注意 2:超過
        $model->field["TYUI_TYOUKA"] = ($model->field["TYUI_TYOUKA"] == "") ? "1" : $model->field["TYUI_TYOUKA"];
        $extra = array("id=\"TYUI_TYOUKA1\"", "id=\"TYUI_TYOUKA2\"");
        $radioArray = knjCreateRadio($objForm, "TYUI_TYOUKA", $model->field["TYUI_TYOUKA"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //欠点テキストボックス表示判定
        if ($testkind == '9900' || $testkind == '9901') {
            //「欠点(評価)は、不振チェック参照するか？」の判定
            if ($model->useSlumpD048 == '1') {
                $arg["USE_SLUMP_D048"] = '1'; //null以外なら何でもいい
            } else {
                $arg["KETTEN_FLG"] = '1'; //null以外なら何でもいい
            }
        } else {
            unset($arg["KETTEN_FLG"]);
            unset($arg["USE_SLUMP_D048"]);
        }

        //学校種別取得
        $query = knjd652Query::getGdat($model->field["GRADE"]);
        $h_j = $db->getOne($query);

        //欠点テキストボックス
        if($model->field["KETTEN"] == "" || $h_j != $model->field["SCHOOL_KIND"] || $model->cmd == "gakki"){
            if ($h_j == 'J') {
                $model->field["KETTEN"] = ($model->field["SEMESTER"] == '9' && $testkind == '9900') ? 1 : 2;
            } else {
                $model->field["KETTEN"] = ($model->field["SEMESTER"] == '9' && $testkind == '9900') ? 1 : 2;
            }
        }
        $extra = " onBlur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["KETTEN"] = knjCreateTextBox($objForm, $model->field["KETTEN"], "KETTEN", 3, 3, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg, $h_j);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd652Form1.html", $arg); 
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
        if ($name == "SUB_TESTKINDCD") $row["VALUE"] = $row["VALUE"]."_1";

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

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = $db->query(knjd652Query::getGradeHrclass($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 12);

    //出力対象作成
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 12);

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

//チェックボックスを作成する
function makeCheckBox(&$objForm, &$arg, $model, $name)
{
    $extra  = ($model->field[$name] == "1") ? "checked" : "";
    $extra .= " id=\"$name\"";

    $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "1", $extra, "");
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //ＣＳＶボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg, $h_j)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD652");
    knjCreateHidden($objForm, "cmd");

    //学期
    knjCreateHidden($objForm, "SEME_DATE", $seme);
    //学期開始日付
    knjCreateHidden($objForm, "SEME_SDATE", $model->control["学期開始日付"][$model->field["SEMESTER"]]);
    //学期終了日付
    knjCreateHidden($objForm, "SEME_EDATE", $model->control["学期終了日付"][$model->field["SEMESTER"]]);
    //学期終了日付
    knjCreateHidden($objForm, "SEME_FLG", $semeflg);
    //年度
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    //日付
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    //切替コード
    knjCreateHidden($objForm, "SCORE_FLG");
    //テスト種別コード + テスト項目コード
    knjCreateHidden($objForm, "TESTKINDCD");
    //備考欄
    knjCreateHidden($objForm, "REMARK", $model->field["REMARK"]);

    //学校種別
    knjCreateHidden($objForm, "SCHOOL_KIND", $h_j);

    //累積情報の遅刻・早退欄のフラグ
    knjCreateHidden($objForm, "chikokuHyoujiFlg", $model->Properties["chikokuHyoujiFlg"]);

    //教育課程コード
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

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
}
?>
