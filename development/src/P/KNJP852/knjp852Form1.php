<?php

require_once('for_php7.php');

class knjp852Form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp852Form1", "POST", "knjp852index.php", "", "knjp852Form1");
        //DB接続
        $db = Query::dbCheckOut();

        //クラス・個人ラジオボタン 1:クラス選択 2:個人選択
        $opt_div = array(1, 2);
        $model->field["CATEGORY_IS_CLASS"] = ($model->field["CATEGORY_IS_CLASS"] == "") ? "1" : $model->field["CATEGORY_IS_CLASS"];
        $extra = array("id=\"CATEGORY_IS_CLASS1\" onClick=\"return btn_submit('knjp852')\"", "id=\"CATEGORY_IS_CLASS2\" onClick=\"return btn_submit('knjp852')\"");
        $radioArray = knjCreateRadio($objForm, "CATEGORY_IS_CLASS", $model->field["CATEGORY_IS_CLASS"], $extra, $opt_div, get_count($opt_div));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学年コンボ作成
        $query = knjp852Query::getGradeHrClass($model, $model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        if ($model->field["CATEGORY_IS_CLASS"] == 2) {
            $arg["schreg_pattern"] = "1";

            //クラスコンボ作成
            $query = knjp852Query::getGradeHrClass($model, $model->field["SEMESTER"], $model, "HR_CLASS");
            $extra = "onchange=\"return btn_submit('knjp852'), AllClearList();\"";
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1);
        }

        if ($model->field["CATEGORY_IS_CLASS"] == 1) {
            $arg["class_pattern"] = "1";

            //出力対象ラジオ
            $opt = array(1, 2, 3);
            $model->field["OUTPUT_DIV"] = ($model->field["OUTPUT_DIV"] == "") ? "1" : $model->field["OUTPUT_DIV"];
            $extra = array();
            foreach ($opt as $key => $val) {
                array_push($extra, " id=\"OUTPUT_DIV{$val}\" ");
            }
            $radioArray = knjCreateRadio($objForm, "OUTPUT_DIV", $model->field["OUTPUT_DIV"], $extra, $opt, count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }

        //宛先住所ラジオ
        $opt = array(1, 2, 3);
        $model->field["ADDRESS_DIV"] = ($model->field["ADDRESS_DIV"] == "") ? "1" : $model->field["ADDRESS_DIV"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"ADDRESS_DIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "ADDRESS_DIV", $model->field["ADDRESS_DIV"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //帳票日付
        $model->field["PRINT_DATE"] = $model->field["PRINT_DATE"] ? $model->field["PRINT_DATE"] : CTRL_DATE;
        $model->field["PRINT_DATE"] = str_replace("-", "/", $model->field["PRINT_DATE"]);
        $arg["data"]["PRINT_DATE"] = View::popUpCalendar($objForm, "PRINT_DATE", $model->field["PRINT_DATE"]);

        //出力文面
        $extra = "";
        $query = knjp852Query::getDocument($model);
        makeCmb($objForm, $arg, $db, $query, "DOCUMENTCD", $model->field["DOCUMENTCD"], $extra, "1");

        //リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp852Form1.html", $arg);
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model)
{
    if ($model->field["CATEGORY_IS_CLASS"] == 1) {
        //対象クラスリストを作成する
        $query = knjp852Query::getGradeHrClass($model, $model->field["SEMESTER"], $model, "HR_CLASS");
        $result = $db->query($query);
        $opt1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

        $arg["data"]["NAME_LIST"] = 'クラス一覧';

        //出力対象一覧リストを作成する
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

        //extra
        $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    } else {

        //対象者リストを作成する
        $query = knjp852Query::getStudent($model, $model->field["SEMESTER"]);
        $result = $db->query($query);
        $opt1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt1[] = array('label' => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番"."　".$row["NAME_SHOW"],
                            'value' => $row["SCHREGNO"]);
        }
        $result->free();
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left', 1)\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

        $arg["data"]["NAME_LIST"] = '生徒一覧';

        //出力対象一覧リストを作成する
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right', 1)\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

        //extra
        $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right', 1);\"";
        $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left', 1);\"";
        $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right', 1);\"";
        $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"move1('left', 1);\"";
    }

    //対象選択ボタンを作成する
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra_rights);
    //対象取消ボタンを作成する
    $arg["button"]["btn_lefts"]  = knjCreateBtn($objForm, "btn_lefts", "<<", $extra_lefts);
    //対象選択ボタンを作成する
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra_right1);
    //対象取消ボタンを作成する
    $arg["button"]["btn_left1"]  = knjCreateBtn($objForm, "btn_left1", "＜", $extra_left1);
}

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

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJP852");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "TEXT_MOJI");
    knjCreateHidden($objForm, "TEXT_GYOU");
    foreach ($model->docSizeProperties as $documentCd => $size) {
        knjCreateHidden($objForm, "documentMstSize_".$documentCd, $size);
    }
}
