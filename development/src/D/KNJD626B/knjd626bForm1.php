<?php

require_once('for_php7.php');

class knjd626bForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd626bForm1", "POST", "knjd626bindex.php", "", "knjd626bForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //学校名取得
        $query = knjd626bQuery::getSchoolname();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd626bQuery::getSemester();
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ作成
        $query = knjd626bQuery::getGradeHrClass($model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //テストコンボ作成
        $query = knjd626bQuery::getTest($model->field["SEMESTER"], $model->field["GRADE"]);
        $opt = array();
        $value = $model->field["SUB_TESTCD"];
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        $value = ($value) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["SUB_TESTCD"] = knjCreateCombo($objForm, "SUB_TESTCD", $value, $opt, $extra, 1);

        //リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //学校マスタ取得
        $schInfo = $db->getRow(knjd626bQuery::getSchoolMst($model), DB_FETCHMODE_ASSOC);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd626bForm1.html", $arg); 
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model) {

    $sort = 0;
    //対象クラスリストを作成する
    $query = knjd626bQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt1[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $arg["data"]["NAME_LIST"] = 'クラス一覧';
    $sort = 0;
    $extra = "multiple style=\"height:130px;width:230px;\" ondblclick=\"move1('left', ".$sort.")\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);


    //出力対象一覧リストを作成する//
    $extra = "multiple style=\"height:130px;width:230px;\" ondblclick=\"move1('right', ".$sort.")\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    //extra
    $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right', ".$sort.");\"";
    $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left', ".$sort.");\"";
    $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right', ".$sort.");\"";
    $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"move1('left', ".$sort.");\"";

    //対象選択ボタンを作成する
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra_rights);
    //対象取消ボタンを作成する
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra_lefts);
    //対象選択ボタンを作成する
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra_right1);
    //対象取消ボタンを作成する
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra_left1);
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
    $sp = ($name == "HR_CLASS") ? "&nbsp;&nbsp;&nbsp;" : "";
    $arg["data"][$name] = $sp.knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
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
    knjCreateHidden($objForm, "PRGID", "KNJD626B");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCORE_FLG");
    knjCreateHidden($objForm, "TESTCD");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useProvFlg", $model->Properties["useProvFlg"]);
}

?>
