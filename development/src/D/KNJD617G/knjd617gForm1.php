<?php

require_once('for_php7.php');

class knjd617gForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd617gForm1", "POST", "knjd617gindex.php", "", "knjd617gForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd617gQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd617g')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //テストコンボ作成
        $query = knjd617gQuery::getTest($model->field["SEMESTER"]);
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], "", 1);

        //学年コンボ作成
        $query = knjd617gQuery::getGradeHrClass($model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('knjd617g')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //出力方法ラジオボタン 1:クラス 2:学年
        $opt_sort = array(1, 2);
        $model->field["PRINT_DIV"] = ($model->field["PRINT_DIV"] == "") ? "1" : $model->field["PRINT_DIV"];
        $printSubmit = "onClick=\"return btn_submit('knjd617g')\"";
        $extra = array("id=\"PRINT_DIV1\" {$printSubmit}", "id=\"PRINT_DIV2\" {$printSubmit}");
        $radioArray = knjCreateRadio($objForm, "PRINT_DIV", $model->field["PRINT_DIV"], $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        makeListToList($objForm, $arg, $db, $model);

        //順位の基準点ラジオボタン 1:総合点 2:平均点
        $opt = array(1, 2);
        $model->field["AVG_DIV"] = ($model->field["AVG_DIV"] == "") ? "1" : $model->field["AVG_DIV"];
        $extra = array("id=\"AVG_DIV1\"", "id=\"AVG_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "AVG_DIV", $model->field["AVG_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //親科目のみを対象とした平均点や順位を印字チェックボックスを作成
        $extra = $model->field["ATTEND_SUBCLASS"] == "1" ? "checked" : "";
        $extra .= " id=\"ATTEND_SUBCLASS\" ";
        $arg["data"]["ATTEND_SUBCLASS"] = knjCreateCheckBox($objForm, "ATTEND_SUBCLASS", "1", $extra);

        //平均点・順位ラジオボタン 1:学年 2:コース
        $opt_addr = array(1, 2);
        $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? "1" : $model->field["GROUP_DIV"];
        $extra = array("id=\"GROUP_DIV1\"", "id=\"GROUP_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "GROUP_DIV", $model->field["GROUP_DIV"], $extra, $opt_addr, get_count($opt_addr));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力順ラジオボタン 1:年組番号順 2:成績順
        $opt_sort = array(1, 2);
        $model->field["SORT_DIV"] = ($model->field["SORT_DIV"] == "") ? "2" : $model->field["SORT_DIV"];
        $extra = array("id=\"SORT_DIV1\"", "id=\"SORT_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "SORT_DIV", $model->field["SORT_DIV"], $extra, $opt_sort, get_count($opt_sort));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd617gForm1.html", $arg); 
    }
}


//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {

    if ($model->field["PRINT_DIV"] == 1) {
        //対象クラスリストを作成する
        $query = knjd617gQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS_LIST");
        $result = $db->query($query);
        $opt1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

        //出力対象一覧リストを作成する//
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

        //extra
        $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";

    }

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
    knjCreateHidden($objForm, "PRGID", "KNJD617G");
    knjCreateHidden($objForm, "IMAGE_PATH", "/usr/local/development/src/image");
    knjCreateHidden($objForm, "SUBCLASS_GROUP", $model->subclassGroup);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "cmd");
}

?>
