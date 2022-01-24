<?php

require_once('for_php7.php');


class knjh563qForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh563qForm1", "POST", "knjh563qindex.php", "", "knjh563qForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //クラス・個人ラジオボタン 1:クラス選択 2:個人選択
        $opt_div = array(1, 2);
        $model->field["CATEGORY_IS_CLASS"] = ($model->field["CATEGORY_IS_CLASS"] == "") ? "1" : $model->field["CATEGORY_IS_CLASS"];
        $extra = array("id=\"CATEGORY_IS_CLASS1\" onClick=\"return btn_submit('knjh563q')\"", "id=\"CATEGORY_IS_CLASS2\" onClick=\"return btn_submit('knjh563q')\"");
        $radioArray = knjCreateRadio($objForm, "CATEGORY_IS_CLASS", $model->field["CATEGORY_IS_CLASS"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjh563qQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjh563q'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //データ種別
        $query = knjh563qQuery::getDataDiv();
        $extra = "onchange=\"return btn_submit('knjh563q'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYDIV", $model->field["PROFICIENCYDIV"], $extra, 1);

        //テスト名称
        $query = knjh563qQuery::getProName($model);
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYCD", $model->field["PROFICIENCYCD"], "", 1);

        //学年コンボ作成
        $query = knjh563qQuery::getGradeHrClass($model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('knjh563q'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        if ($model->field["CATEGORY_IS_CLASS"] == 2){
            //クラスコンボ作成
            $query = knjh563qQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
            $extra = "onchange=\"return btn_submit('knjh563q'), AllClearList();\"";
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1);
        }

        //リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

//        //グループラジオボタン 各値 1:学年　2:クラス　3:コース　4:学科　5:コースグループ
//        $set_group_div = "";
//        $opt_group = array();
//        $rankdiv_array = array();
//        $check_array = array();
//        //プロパティの値チェック
//        $rankdiv_array = explode("-", $model->Properties["useRadioPattern"]);
//        foreach($rankdiv_array as $key => $val) {
//            if ($val != "1" && $val != "2" && $val != "3" && $val != "4" && $val != "5") {
//                //値が不正の場合は下記をセット
//                $model->Properties["useRadioPattern"] = "1-2";
//            }
//        }
//        //プロパティの値をセット
//        $rankdiv_array = explode("-", $model->Properties["useRadioPattern"]);
//        foreach($rankdiv_array as $key => $val) {
//            $opt_group[$key + 1] = $val;
//            //5:コースグループの初期値をセット（プロパティusePerfectCourseGroup用）
//            if ($val == "5") {
//                $set_group_div = $key + 1;
//            }
//        }
//        $set_name_array = array();
//        $set_name_array[1] = '学年';
//        $set_name_array[2] = 'クラス';
//        $set_name_array[3] = 'コース';
//        $set_name_array[4] = '学科';
//        $set_name_array[5] = 'コースグループ';

//        //ラジオ作成
//        if ($model->Properties["usePerfectCourseGroup"] === '1') {
//            if ($set_group_div) {
//                $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? $set_group_div : $model->field["GROUP_DIV"];
//            } else {
//                $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? "1" : $model->field["GROUP_DIV"];
//            }
//        } else {
//            $model->field["GROUP_DIV"] = ($model->field["GROUP_DIV"] == "") ? "1" : $model->field["GROUP_DIV"];
//        }
//        $radioArray = array();
//        $ret = array();
//        for ($count = 1; $count <= get_count($opt_group); $count++) {
//            $objForm->ae( array("type"      => "radio",
//                                "name"      => "GROUP_DIV",
//                                "value"     => $model->field["GROUP_DIV"],
//                                "extrahtml" => "id=\"GROUP_DIV{$count}\"",
//                                "multiple"  => $opt_group));
//            $ret["GROUP_DIV".$count] = $objForm->ge("GROUP_DIV", $count);
//            $arg["data"]["GROUP_DIV_NAME".$count] = $set_name_array[$opt_group[$count]].'　';
//        }
//        $radioArray = $ret;
//        foreach($radioArray as $key => $val) {
//            $arg["data"][$key] = $val;
//        }

//        //順位ラジオボタン 1:総合点 2:平均点 3:偏差値 4:傾斜総合点
//        $opt_sort = array(1, 2, 3, 4);
//        $model->field["JUNI"] = ($model->field["JUNI"] == "") ? "1" : $model->field["JUNI"];
//        $extra = array("id=\"JUNI1\"", "id=\"JUNI2\"", "id=\"JUNI3\"", "id=\"JUNI4\"");
//        $radioArray = knjCreateRadio($objForm, "JUNI", $model->field["JUNI"], $extra, $opt_sort, get_count($opt_sort));
//        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

//        //偏差値選択ラジオボタン 1:偏差値 2:標準偏差 3:出力しない
//        $opt_addr = array(1, 2, 3);
//        $model->field["DEVIATION_PRINT"] = ($model->field["DEVIATION_PRINT"] == "") ? "2" : $model->field["DEVIATION_PRINT"];
//        $extra = array("id=\"DEVIATION_PRINT1\"", "id=\"DEVIATION_PRINT2\"", "id=\"DEVIATION_PRINT3\"");
//        $radioArray = knjCreateRadio($objForm, "DEVIATION_PRINT", $model->field["DEVIATION_PRINT"], $extra, $opt_addr, get_count($opt_addr));
//        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //順位出力チェックボックス
        $extra = ($model->field["JUNI_PRINT"] == "1" || $model->cmd == '') ? "checked" : "";
        $extra .= " id=\"JUNI_PRINT\"";
        $arg["data"]["JUNI_PRINT"] = knjCreateCheckBox($objForm, "JUNI_PRINT", "1", $extra, "");

//        //保護者欄印刷出力チェックボックス
//        $extra = ($model->field["HOGOSHA_PRINT"] != "1") ? "" : "checked";
//        $extra .= " id=\"HOGOSHA_PRINT\"";
//        $arg["data"]["HOGOSHA_PRINT"] = knjCreateCheckBox($objForm, "HOGOSHA_PRINT", "1", $extra, "");

        //提出日作成
        $model->field["JISSHI_DATE"] = $model->field["JISSHI_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["JISSHI_DATE"];
        $arg["data"]["JISSHI_DATE"] = View::popUpCalendar($objForm, "JISSHI_DATE", $model->field["JISSHI_DATE"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh563qForm1.html", $arg); 
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model) {

    if ($model->field["CATEGORY_IS_CLASS"] == 1){
        //対象クラスリストを作成する
        $query = knjh563qQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
        $result = $db->query($query);
        $opt1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        $extra = "multiple style=\"width:230px; height:230px;\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 15);

        $arg["data"]["NAME_LIST"] = 'クラス一覧';

        //出力対象一覧リストを作成する
        $extra = "multiple style=\"width:230px; height:230px;\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 15);

        //extra
        $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";

    }else {

        //対象外の生徒取得
        $query = knjh563qQuery::getSchnoIdou($model, $model->field["SEMESTER"]);
        $result = $db->query($query);
        $opt_idou = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_idou[] = $row["SCHREGNO"];
        }
        $result->free();

        //対象者リストを作成する
        $query = knjh563qQuery::getStudent($model, $model->field["SEMESTER"]);
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
        $extra = "multiple style=\"width:230px; height:230px;\" ondblclick=\"move1('left', 1)\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 15);

        $arg["data"]["NAME_LIST"] = '生徒一覧';

        //出力対象一覧リストを作成する
        $extra = "multiple style=\"width:230px; height:230px;\" ondblclick=\"move1('right', 1)\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 15);

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
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEME", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJH563Q");
    knjCreateHidden($objForm, "IMAGE_PATH", "/usr/local/development/src/image");
    knjCreateHidden($objForm, "SUBCLASS_GROUP", $model->subclassGroup);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useKnjd106cJuni1", $model->useKnjd106cJuni1);
    knjCreateHidden($objForm, "useKnjd106cJuni2", $model->useKnjd106cJuni2);
    knjCreateHidden($objForm, "useKnjd106cJuni3", $model->useKnjd106cJuni3);
    knjCreateHidden($objForm, "FORM_GROUP_DIV");
    knjCreateHidden($objForm, "useRadioPattern", $model->Properties["useRadioPattern"]);
    knjCreateHidden($objForm, "knjh563qPrintScoreKansannashi", $model->Properties["knjh563qPrintScoreKansannashi"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
}

?>
