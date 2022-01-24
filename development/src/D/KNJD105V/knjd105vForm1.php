<?php

require_once('for_php7.php');

class knjd105vForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd105vForm1", "POST", "knjd105vindex.php", "", "knjd105vForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //クラス・個人ラジオボタン 1:クラス選択 2:個人選択
        $opt_div = array(1, 2);
        $model->field["CATEGORY_IS_CLASS"] = ($model->field["CATEGORY_IS_CLASS"] == "") ? "1" : $model->field["CATEGORY_IS_CLASS"];
        $extra = array("id=\"CATEGORY_IS_CLASS1\" onClick=\"return btn_submit('knjd105v')\"", "id=\"CATEGORY_IS_CLASS2\" onClick=\"return btn_submit('knjd105v')\"");
        $radioArray = knjCreateRadio($objForm, "CATEGORY_IS_CLASS", $model->field["CATEGORY_IS_CLASS"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd105vQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd105v'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //テストコンボ作成
        $query = knjd105vQuery::getTest($model->field["SEMESTER"]);
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], "", 1);

        //学年コンボ作成
        $query = knjd105vQuery::getGradeHrClass($model, $model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        if ($model->field["CATEGORY_IS_CLASS"] == 2){
            //クラスコンボ作成
            $query = knjd105vQuery::getGradeHrClass($model, $model->field["SEMESTER"], $model, "HR_CLASS");
            $extra = "onchange=\"return btn_submit('knjd105v'), AllClearList();\"";
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1);
        }

        //リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);
        if ($model->Properties["knjd105vShowClassRadio"] == "1") {
            $arg["knjd105vShowClassRadio"] = "1";
            //グループラジオボタン 1:学年 2:コース 3:クラス
            $opt_group = array(1, 2, 3);
            $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? "1" : $model->field["GROUP_DIV"];
            $extra = array("id=\"GROUP_DIV1\"" , "id=\"GROUP_DIV2\"" , "id=\"GROUP_DIV3\"");
            $radioArray = knjCreateRadio($objForm, "GROUP_DIV", $model->field["GROUP_DIV"], $extra, $opt_group, get_count($opt_group));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        } else {
            //グループラジオボタン 1:学年 2:コース
            $opt_group = array(1, 2);
            $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? "1" : $model->field["GROUP_DIV"];
            $extra = array("id=\"GROUP_DIV1\"" , "id=\"GROUP_DIV2\"");
            $radioArray = knjCreateRadio($objForm, "GROUP_DIV", $model->field["GROUP_DIV"], $extra, $opt_group, get_count($opt_group));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        }

        //順位の基準点 1:総合点 2:平均点 3:偏差値
        $syokiti = ($model->field["OUTPUT_KIJUN"] == "") ? '1' : $model->field["OUTPUT_KIJUN"];
        if ($model->cmd == "edit") {
            if ($model->field["GRADE"] >= '04') {
                $syokiti = 2;
            } else {
                $syokiti = 1;
            }
        }
        $opt_kijun = array(1, 2, 3);
        $extra = array("id=\"OUTPUT_KIJUN1\"" , "id=\"OUTPUT_KIJUN2\"" , "id=\"OUTPUT_KIJUN3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT_KIJUN", $syokiti, $extra, $opt_kijun, get_count($opt_kijun));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //偏差値選択ラジオボタン 1:偏差値 2:標準偏差 3:出力しない
        $opt_addr = array(1, 2, 3);
        $model->field["DEVIATION_PRINT"] = ($model->field["DEVIATION_PRINT"] == "") ? "2" : $model->field["DEVIATION_PRINT"];
        $extra = array("id=\"DEVIATION_PRINT1\"", "id=\"DEVIATION_PRINT2\"", "id=\"DEVIATION_PRINT3\"");
        $radioArray = knjCreateRadio($objForm, "DEVIATION_PRINT", $model->field["DEVIATION_PRINT"], $extra, $opt_addr, get_count($opt_addr));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /********************/
        /* テキストボックス */
        /********************/
        //欠点
        if ($model->cmd == 'change_grade' || $model->cmd == '') {
            $query = knjd105vQuery::getGdat($model->field["GRADE"]);
            $h_j = $db->getOne($query);
            if ($h_j == 'H') {
                $model->field["KETTEN"] = 40;
            } else {
                $model->field["KETTEN"] = 60;
            }
        }
        $extra = "style=\"text-align:right;\" onBlur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["KETTEN"] = knjCreateTextBox($objForm, $model->field["KETTEN"], "KETTEN", 3, 3, $extra);

        if ($model->Properties["useFormNameKNJD105V"] && $model->Properties["useFormNameKNJD105V"] != "") {
            $arg["useFormNameKNJD105V"] = "";
            knjCreateHidden($objForm, "useFormNameKNJD105V", $model->Properties["useFormNameKNJD105V"]);
        } else {
            $arg["useFormNameKNJD105V"] = "1";

            //checkbox(保護者確認欄を出力する)
            $extra = ($model->field["PRINT_HOGOSHA_KAKUNINRAN"] == "1" || $model->cmd == "") ? "checked" : "";
            $extra .= " id=\"PRINT_HOGOSHA_KAKUNINRAN\"";
            $arg["data"]["PRINT_HOGOSHA_KAKUNINRAN"] = knjCreateCheckBox($objForm, "PRINT_HOGOSHA_KAKUNINRAN", "1", $extra, "");

            //提出日作成
            $model->field["SUBMIT_DATE"] = $model->field["SUBMIT_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["SUBMIT_DATE"];
            $arg["data"]["SUBMIT_DATE"] = View::popUpCalendar($objForm, "SUBMIT_DATE", $model->field["SUBMIT_DATE"]);
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd105vForm1.html", $arg); 
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model) {

    if ($model->field["CATEGORY_IS_CLASS"] == 1){
        //対象クラスリストを作成する
        $query = knjd105vQuery::getGradeHrClass($model, $model->field["SEMESTER"], $model, "HR_CLASS");
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

    }else {

        //対象外の生徒取得
        $query = knjd105vQuery::getSchnoIdou($model, $model->field["SEMESTER"]);
        $result = $db->query($query);
        $opt_idou = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_idou[] = $row["SCHREGNO"];
        }
        $result->free();

        //対象者リストを作成する
        $query = knjd105vQuery::getStudent($model, $model->field["SEMESTER"]);
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
    //ＣＳＶボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD105V");
    knjCreateHidden($objForm, "IMAGE_PATH", "/usr/local/development/src/image");
    knjCreateHidden($objForm, "SUBCLASS_GROUP", $model->subclassGroup);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
    knjCreateHidden($objForm, "knjd105vNotPrintMajorname", $model->Properties["knjd105vNotPrintMajorname"]);
    knjCreateHidden($objForm, "cmd");
}

?>
