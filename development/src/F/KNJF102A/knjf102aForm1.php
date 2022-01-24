<?php

require_once('for_php7.php');

class knjf102aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjf102aForm1", "POST", "knjf102aindex.php", "", "knjf102aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        //出力順ラジオボタンを作成
        $radioValue = array(1, 2);
        $div = 0;
        if (!$model->field["OUTPUT"]) {
            $model->field["OUTPUT"] = 1;
        }
        if ($model->field["OUTPUT"] == 1) {
            $div = 1;
        }
        $extra = array("id=\"OUTPUT1\" onclick =\" return btn_submit('clickchange');\"", "id=\"OUTPUT2\" onclick =\" return btn_submit('clickchange');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $radioValue, get_count($radioValue));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        if ($div == 1) {
            $arg["hr_class"] = 1;
        } else {
            $arg["student"] = 1;
        }

        //クラス選択コンボボックスを作成する
        if ($div == 1) {
            $query = knjf102aQuery::getGrade($model);
        } else {
            $query = knjf102aQuery::getHrClass($model, $div);
        }
        $row1 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        if (!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        if ($model->cmd == 'clickchange') {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            $model->cmd = 'knjf102a';
        }

        $extra = "onchange=\"return btn_submit('knjf102a'),AllClearList();\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //生徒項目名切替処理
        $sch_label = "";
        //テーブルの有無チェック
        $query = knjf102aQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($model->field["GRADE_HR_CLASS"] || (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1"))) {
            //生徒項目名取得
            $sch_label = $db->getOne(knjf102aQuery::getSchName($model, $div));
        }
        $arg["SCH_LABEL"] = (strlen($sch_label) > 0) ? $sch_label : '生徒';

        //リストtoリストを作成する
        makeListToList($objForm, $arg, $db, $model, $div);

        //来室種別チェックボックス
        $opt_check = array("NAIKA", "GEKA", "KENKO_SODAN", "SONOTA", "SEITO_IGAI");
        foreach ($opt_check as $name) {
            $extra = ($model->field[$name] == "1" || $model->cmd == "") ? "checked" : "";
            $extra .= " id=\"".$name."\"";
            $arg["data"][$name] = knjCreateCheckBox($objForm, $name, "1", $extra, "");
        }

        //来室種別ごとの改ページありチェックボックス
        $extra = ($model->field["CHECK1"] == "1" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"CHECK1\"";
        $arg["data"]["CHECK1"] = knjCreateCheckBox($objForm, "CHECK1", "1", $extra, "");

        //来室日付範囲（開始）
        $value = isset($model->field["DATE1"]) ? $model->field["DATE1"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE1"] = View::popUpCalendar($objForm, "DATE1", $value);

        //来室日付範囲（終了）
        $value2 = isset($model->field["DATE2"]) ? $model->field["DATE2"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm, "DATE2", $value2);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf102aForm1.html", $arg);
    }
}
//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $div)
{
    if ($div == 1) {
        $query = knjf102aQuery::getHrClass($model, $div);
    } else {
        $query = knjf102aQuery::getStudentList($model);
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left', $div)\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", isset($opt1)?$opt1:array(), $extra, 20);

    //出力対象一覧リストを作成する
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right', $div)\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", array(), $extra, 20);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', $div);\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', $div);\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', $div);\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', $div);\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJF102A");
    knjCreateHidden($objForm, "cmd");

    knjCreateHidden($objForm, "CHK_SDATE", CTRL_YEAR . "/04/01");
    knjCreateHidden($objForm, "CHK_EDATE", (CTRL_YEAR + 1) . "/03/31");
}
