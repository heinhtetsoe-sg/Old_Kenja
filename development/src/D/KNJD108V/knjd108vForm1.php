<?php

require_once('for_php7.php');

class knjd108vForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd108vForm1", "POST", "knjd108vindex.php", "", "knjd108vForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd108vQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd108v'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ作成
        $query = knjd108vQuery::getGradeHrClass($model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //クラスコンボ作成
        $query = knjd108vQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
        $extra = "onchange=\"return btn_submit('knjd108v'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1);

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
        View::toHTML($model, "knjd108vForm1.html", $arg); 
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model) {

    //対象外の生徒取得
    $query = knjd108vQuery::getSchnoIdou($model, $model->field["SEMESTER"]);
    $result = $db->query($query);
    $opt_idou = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_idou[] = $row["SCHREGNO"];
    }
    $result->free();

    //対象者リストを作成する
    $query = knjd108vQuery::getStudent($model, $model->field["SEMESTER"]);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $idou = "　";
        if (in_array($row["SCHREGNO"],$opt_idou)) {
            $idou = "●";
        }
        $opt1[] = array('label' => $row["SCHREGNO"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
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

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD108V");
    knjCreateHidden($objForm, "IMAGE_PATH", "/usr/local/development/src/image");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "cmd");
}

?>
