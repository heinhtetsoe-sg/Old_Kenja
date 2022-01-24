<?php

require_once('for_php7.php');

class knjd041vForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd041vForm1", "POST", "knjd041vindex.php", "", "knjd041vForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $query = knjd041vQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd041v');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $arg["USE_MAJOR"] = '1';
            //学科名コンボ
            $query = knjd041vQuery::getCourseMajor($model);
            $extra = "onchange=\"return btn_submit('knjd041v');\"";
            makeCmb($objForm, $arg, $db, $query, "MAJOR", $model->field["MAJOR"], $extra, 1);
        }

        //テスト名コンボ
        $query = knjd041vQuery::getTestItem($model);
        makeCmb($objForm, $arg, $db, $query, "TESTCD", $model->field["TESTCD"], "", 1);

        //クラス一覧リスト
        makeClassList($objForm, $arg, $db, $model);

        //帳票出力指定ラジオボタン
        $opt_sitei = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_sitei, get_count($opt_sitei));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力項目指定ラジオボタン
        $opt_sitei = array(1, 2);
        $model->field["OUTPUTCOL"] = ($model->field["OUTPUTCOL"] == "") ? "1" : $model->field["OUTPUTCOL"];
        $extra = array("id=\"OUTPUTCOL1\"", "id=\"OUTPUTCOL2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUTCOL", $model->field["OUTPUTCOL"], $extra, $opt_sitei, get_count($opt_sitei));
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
        View::toHTML($model, "knjd041vForm1.html", $arg); 
    }
}

//クラス一覧リスト作成
function makeClassList(&$objForm, &$arg, $db, &$model)
{
    $row1 = array();
    $query = knjd041vQuery::getClassData($model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $row1[]= array('label' => $row["VALUE"]." ".$row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //教科一覧リストを作成する
    $extra = "multiple style=\"width:170px\" width:\"170px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 15);

    //出力対象教科リストを作成する
    $extra = "multiple style=\"width:170px\" width:\"170px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 15);

    //対象選択ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

    //対象取消ボタンを作成する（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

    //対象選択ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

    //対象取消ボタンを作成する（一部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
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
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJD041V");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
    knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "cmd");
}

?>
