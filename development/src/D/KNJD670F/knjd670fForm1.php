<?php

require_once('for_php7.php');
class knjd670fForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd670fForm1", "POST", "knjd670findex.php", "", "knjd670fForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd670fQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd670f'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //DBの学校校種取得
        $model->dbSchoolKind = $db->getOne(knjd670fQuery::getDbSchoolKind());

        //学年コンボ作成
        $query = knjd670fQuery::getGrade($model->field["SEMESTER"], $model);
        $extra = "onchange=\"return btn_submit('knjd670f'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //学校校種取得
        $model->schoolkind = $db->getOne(knjd670fQuery::getSchoolKind($model->field["GRADE"]));

        //テストコンボ作成
        $query = knjd670fQuery::getTest($model->field["SEMESTER"], $model);
        $extra = "onchange=\"return btn_submit('knjd670f'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], $extra, 1);

        //クラス方式選択 (1:法定クラス 2:複式クラス)
        $opt = array(1, 2);
        if ($model->field["HR_CLASS_TYPE"] == "") {
            $model->field["HR_CLASS_TYPE"] = ($model->Properties["useFi_Hrclass"] == "1" && $model->dbSchoolKind != "H") ? "2" : "1";
        }
        $extra = array("id=\"HR_CLASS_TYPE1\"", "id=\"HR_CLASS_TYPE2\"");
        $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->field["HR_CLASS_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //順位出力
        $extra = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RANK"] = knjCreateTextBox($objForm, $model->field["RANK"], "RANK", 4, 4, $extra);

        if ($model->schoolkind == 'H') {
            $extra  = ($model->field["CHAIR_PAGE"] == "1") ? "checked='checked' " : "";
            $extra .= " id=\"CHAIR_PAGE\"";
            $extra .= " onchange=\"return btn_submit('knjd670f')\"";
            $arg["data"]["CHAIR_PAGE"] = knjCreateCheckBox($objForm, "CHAIR_PAGE", "1", $extra, "");
            $arg["useChairRank"] = "1";

            //科目コンボ作成
            if ($model->field["CHAIR_PAGE"] == '1') {
                $query = knjd670fQuery::getSubclass($model, $model->field["SEMESTER"], 0);
                $extra = "onchange=\"return btn_submit('knjd670f'), AllClearList();\"";
                makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1);
            }
        }

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
        View::toHTML($model, "knjd670fForm1.html", $arg); 
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model) {

    //対象者リストを作成する
    if ($model->field["CHAIR_PAGE"] == '1') {
        $arg["data"]["NAME_LIST"] = '講座一覧';
        $query = knjd670fQuery::getChair($model, $model->field["SEMESTER"]);
    } else {
        $arg["data"]["NAME_LIST"] = '科目一覧';
        $query = knjd670fQuery::getSubclass($model, $model->field["SEMESTER"]);
    }
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left', 1)\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

    //出力対象一覧リストを作成する
    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right', 1)\"";
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
    knjCreateHidden($objForm, "PRGID", "KNJD670F");
    knjCreateHidden($objForm, "cmd");
}

?>
