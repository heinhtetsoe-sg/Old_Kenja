<?php

require_once('for_php7.php');


class knje380Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knje380Form1", "POST", "knje380index.php", "", "knje380Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期名
        $arg["data"]["SEMESTER"] = CTRL_SEMESTERNAME;

        $query = knje380Query::getQuestionnaireList();
        makeCmb($objForm, $arg, $db, $query, "QUESTIONNAIRECD", $model->field["QUESTIONNAIRECD"], "", 1);

        //校種コンボ作成
        $query = knje380Query::getNameMstA023($model);
        $extra = "onchange=\"return btn_submit('knje380')\"";
        makeCmb($objForm, $arg, $db, $query, "A023_SCHOOL_KIND", $model->field["A023_SCHOOL_KIND"], $extra, 1);

        //1:学年, 2:クラス
        $opt_data = array(1, 2);
        $model->field["KUBUN"] = ($model->field["KUBUN"] == "") ? "1" : $model->field["KUBUN"];
        $extra = array("id=\"KUBUN1\" onClick=\"btn_submit('knje380')\"", "id=\"KUBUN2\" onClick=\"btn_submit('knje380')\"");
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["KUBUN"] == 1) $arg["clsno"] = $model->field["KUBUN"];
        if ($model->field["KUBUN"] == 2) $arg["schno"] = $model->field["KUBUN"];

        //クラス一覧リスト
        makeClassItiran($objForm, $arg, $db, $model);

        //学科一覧リスト
        makeMajorItiran($objForm, $arg, $db, $model);

        //県報告用進路状況調査表チェックボックス
        $extra  = " id=\"PRINT1\"";
        $extra .= $model->field["PRINT1"] == "on" ? " checked" : "";
        $arg["data"]["PRINT1"] = knjCreateCheckBox($objForm, "PRINT1", "on", $extra);
        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv1');\"";
        $arg["button"]["btn_csv1"] = knjCreateBtn($objForm, "btn_csv1", "ＣＳＶ出力", $extra);

        //新規高等学校卒業者の進路状況チェックボックス
        $extra  = " id=\"PRINT2\"";
        $extra .= $model->field["PRINT2"] == "on" ? " checked" : "";
        $arg["data"]["PRINT2"] = knjCreateCheckBox($objForm, "PRINT2", "on", $extra);
        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv2');\"";
        $arg["button"]["btn_csv2"] = knjCreateBtn($objForm, "btn_csv2", "ＣＳＶ出力", $extra);

        //県報告用進路状況調査表チェックボックス
        $extra  = " id=\"PRINT3\"";
        $extra .= $model->field["PRINT3"] == "on" ? " checked" : "";
        $arg["data"]["PRINT3"] = knjCreateCheckBox($objForm, "PRINT3", "on", $extra);
        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv3');\"";
        $arg["button"]["btn_csv3"] = knjCreateBtn($objForm, "btn_csv3", "ＣＳＶ出力", $extra);

        //県報告用進路状況調査表チェックボックス
        $extra  = " id=\"PRINT4\"";
        $extra .= $model->field["PRINT4"] == "on" ? " checked" : "";
        $arg["data"]["PRINT4"] = knjCreateCheckBox($objForm, "PRINT4", "on", $extra);
        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv4');\"";
        $arg["button"]["btn_csv4"] = knjCreateBtn($objForm, "btn_csv4", "ＣＳＶ出力", $extra);

        //県報告用進路状況調査表チェックボックス
        $extra  = " id=\"PRINT5\"";
        $extra .= $model->field["PRINT5"] == "on" ? " checked" : "";
        $arg["data"]["PRINT5"] = knjCreateCheckBox($objForm, "PRINT5", "on", $extra);
        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv5');\"";
        $arg["button"]["btn_csv5"] = knjCreateBtn($objForm, "btn_csv5", "ＣＳＶ出力", $extra);

        //県報告用進路状況調査表チェックボックス
        $extra  = " id=\"PRINT6\"";
        $extra .= $model->field["PRINT6"] == "on" ? " checked" : "";
        $arg["data"]["PRINT6"] = knjCreateCheckBox($objForm, "PRINT6", "on", $extra);
        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv6');\"";
        $arg["button"]["btn_csv6"] = knjCreateBtn($objForm, "btn_csv6", "ＣＳＶ出力", $extra);

        //県報告用進路状況調査表チェックボックス
        $extra  = " id=\"PRINT7\"";
        $extra .= $model->field["PRINT7"] == "on" ? " checked" : "";
        $arg["data"]["PRINT7"] = knjCreateCheckBox($objForm, "PRINT7", "on", $extra);
        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv7');\"";
        $arg["button"]["btn_csv7"] = knjCreateBtn($objForm, "btn_csv7", "ＣＳＶ出力", $extra);

        //ボタンを作成する
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje380Form1.html", $arg); 

    }

}

function makeClassItiran(&$objForm, &$arg, $db, &$model) {
    $row1 = array();
    $query = knje380Query::getGrade($model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //2:クラス表示指定用
    $opt_left = array();
    if ($model->field["KUBUN"] == 2) {
        if ($model->field["GRADE"] == "") $model->field["GRADE"] = $row1[0]["value"];

        $extra = "onChange=\"return btn_submit('change_grade');\"";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $row1, $extra, 1);

        //クラス単位
        $query = knje380Query::getRegdDat($model);
    }

    if ($model->field["KUBUN"] == 2 || $model->cmd == "csv1" ||
        $model->cmd == "csv2" || $model->cmd == "csv3" ||
        $model->cmd == "csv4" || $model->cmd == "csv5" ||
        $model->cmd == "csv6" || $model->cmd == "csv7"
    ) {
        $row1 = array();
        $selectleft = explode(",", $model->selectleft);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->select_opt[$row["VALUE"]] = array("label" => $row["LABEL"], 
                                                      "value" => $row["VALUE"]);

            if($model->cmd != 'knje380' && $model->cmd != '') {
                if (!in_array($row["VALUE"], $selectleft)){
                    $row1[] = array('label' => $row["LABEL"],
                                    'value' => $row["VALUE"]);
                }
            } else {
                $row1[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
            }
        }
        $result->free();
        //左リストで選択されたものを再セット
        if($model->cmd != 'knje380' && $model->cmd != '') {
            foreach ($model->select_opt as $key => $val){
                if (in_array($key, $selectleft)) {
                    $opt_left[] = $val;
                }
            }
        }
    }

    $chdt = $model->field["KUBUN"];

    //対象クラスリスト
    $extra = "multiple style=\"width:300px\" ondblclick=\"move1('left', $chdt, 'CLASS')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 15);

    //出力クラスリスト
    $extra = "multiple style=\"width:300px\" ondblclick=\"move1('right', $chdt, 'CLASS')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", $opt_left, $extra, 15);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', $chdt, 'CLASS');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', $chdt, 'CLASS');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', $chdt, 'CLASS');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', $chdt, 'CLASS');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

function makeMajorItiran(&$objForm, &$arg, $db, &$model) {

    $row1 = array();
    $selectMajorLeft = explode(",", $model->selectMajorLeft);
    $query = knje380Query::getMajor($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->select_MajorOpt[$row["VALUE"]] = array("label" => $row["LABEL"], 
                                                       "value" => $row["VALUE"]);

        if($model->cmd != 'knje380' && $model->cmd != '') {
            if (!in_array($row["VALUE"], $selectMajorLeft)){
                $row1[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
            }
        } else {
            $row1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
    }
    //左リストで選択されたものを再セット
    $opt_left = array();
    if($model->cmd != 'knje380' && $model->cmd != '') {
        foreach ($model->select_MajorOpt as $key => $val){
            if (in_array($key, $selectMajorLeft)) {
                $opt_left[] = $val;
            }
        }
    }
    $result->free();

    $chdt = $model->field["KUBUN"];

    //対象クラスリスト
    $extra = "multiple style=\"height:100px; width:300px\" ondblclick=\"move1('left', $chdt, 'MAJOR')\"";
    $arg["data"]["MAJOR_NAME"] = knjCreateCombo($objForm, "MAJOR_NAME", "", $row1, $extra, 7);

    //出力クラスリスト
    $extra = "multiple style=\"height:100px; width:300px\" ondblclick=\"move1('right', $chdt, 'MAJOR')\"";
    $arg["data"]["MAJOR_SELECTED"] = knjCreateCombo($objForm, "MAJOR_SELECTED", "", $opt_left, $extra, 7);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', $chdt, 'MAJOR');\"";
    $arg["button"]["btn_major_rights"] = knjCreateBtn($objForm, "btn_major_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', $chdt, 'MAJOR');\"";
    $arg["button"]["btn_major_lefts"] = knjCreateBtn($objForm, "btn_major_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', $chdt, 'MAJOR');\"";
    $arg["button"]["btn_major_right1"] = knjCreateBtn($objForm, "btn_major_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', $chdt, 'MAJOR');\"";
    $arg["button"]["btn_major_left1"] = knjCreateBtn($objForm, "btn_major_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blanc="")
{
    $opt = array();
    if ($blanc) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", PROGRAMID);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "selectMajorLeft");
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
    knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}

?>
