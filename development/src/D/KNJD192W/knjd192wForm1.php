<?php

require_once('for_php7.php');

class knjd192wForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd192wForm1", "POST", "knjd192windex.php", "", "knjd192wForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //学校名取得
        $query = knjd192wQuery::getSchoolname();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $schoolName = $rowZ010["NAME1"];
        $schoolCode = $rowZ010["NAME2"];

        //クラス・個人ラジオボタン 1:クラス選択 2:個人選択
        $opt_div = array(1, 2);
        if ($model->field["CATEGORY_IS_CLASS"] == "") {
            $model->field["CATEGORY_IS_CLASS"] = "1";
        }
        $extra = array("id=\"CATEGORY_IS_CLASS1\" onClick=\"return btn_submit('knjd192w')\"", "id=\"CATEGORY_IS_CLASS2\" onClick=\"return btn_submit('knjd192w')\"");
        $radioArray = knjCreateRadio($objForm, "CATEGORY_IS_CLASS", $model->field["CATEGORY_IS_CLASS"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd192wQuery::getSemester();
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ作成
        $query = knjd192wQuery::getGradeHrClass($model->field["SEMESTER"], $model, "GRADE");
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        if ($model->field["CATEGORY_IS_CLASS"] == 2){
            //クラスコンボ作成
            $query = knjd192wQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
            $extra = "onchange=\"return btn_submit('knjd192w'), AllClearList();\"";
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1);
        }

        //テストコンボ作成
        $query = knjd192wQuery::getTest($model->field["SEMESTER"], $model->field["GRADE"]);
        $opt = array();
        $value = $model->field["SUB_TESTCD"];
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        $value = ($value) ? $value : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('change_grade'), AllClearList();\"";
        $arg["data"]["SUB_TESTCD"] = knjCreateCombo($objForm, "SUB_TESTCD", $value, $opt, $extra, 1);

        //リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //欠点
        $testkind = substr($value, 0, 4);
        if (($model->Properties["checkKettenDiv"] != '1' && $model->Properties["checkKettenDiv"] != '2') || 
            ($model->Properties["checkKettenDiv"] == '1' && $model->field["SEMESTER"] == '9') || 
            ($model->Properties["checkKettenDiv"] == '1' && $model->field["SEMESTER"] != '9' && ($testkind == '9900' || $testkind == '9901') && $model->useSlumpD048 != '1')) {
            $arg["KETTEN_FLG"] = '1'; //null以外なら何でもいい
        } else {
            unset($arg["KETTEN_FLG"]);
        }
        //欠点
        if ($model->cmd == 'change_grade' || $model->cmd == '') {
            $query = knjd192wQuery::getGdat($model->field["GRADE"]);
            $h_j = $db->getOne($query);
            if ($h_j == 'J') {
                $model->field["KETTEN"] = 60;
            } else {
                $model->field["KETTEN"] = 30;
            }
            if ($model->Properties["checkKettenDiv"] == '1') {
                $model->field["KETTEN"] = ($model->field["SEMESTER"] == '9' && $testkind == '9900') ? 1 : 2;
            }
        }
        $extra = "onblur=\"calc(this);\"";
        $arg["data"]["KETTEN"] = knjCreateTextBox($objForm, $model->field["KETTEN"], "KETTEN", 3, 3, $extra);

        //学校マスタ取得
        $schInfo = $db->getRow(knjd192wQuery::getSchoolMst($model), DB_FETCHMODE_ASSOC);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd192wForm1.html", $arg); 
    }
}

function makeStudentList(&$objForm, &$arg, $db, $model) {

    $sort = 0;
    if ($model->field["CATEGORY_IS_CLASS"] == 1){
        //対象クラスリストを作成する
        $query = knjd192wQuery::getGradeHrClass($model->field["SEMESTER"], $model, "HR_CLASS");
        $result = $db->query($query);
        $opt1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt1[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        $arg["data"]["NAME_LIST"] = 'クラス一覧';
        $sort = 0;
    }else {

        //対象外の生徒取得
        $query = knjd192wQuery::getSchnoIdou($model, $model->field["SEMESTER"]);
        $result = $db->query($query);
        $opt_idou = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_idou[] = $row["SCHREGNO"];
        }
        $result->free();

        //対象者リストを作成する
        $query = knjd192wQuery::getStudent($model, $model->field["SEMESTER"]);
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
        $arg["data"]["NAME_LIST"] = '生徒一覧';

        $sort = 1;
    }
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
    knjCreateHidden($objForm, "PRGID", "KNJD192W");
    knjCreateHidden($objForm, "FORMNAME");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "TESTCD");
    knjCreateHidden($objForm, "checkKettenDiv", $model->Properties["checkKettenDiv"]);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
}

?>
