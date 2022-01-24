<?php

require_once('for_php7.php');

class knjh543Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjh543index.php", "", "sel");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度、学期表示
        $arg["YEAR"] = CTRL_YEAR."年度";

        //学期コンボボックス
        $query = knjh543Query::getSemester();
        $extra = "onchange=\"return btn_submit('list')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //実力区分コンボボックス
        $query = knjh543Query::getProficiencyDiv($model->field["SEMESTER"]);
        $extra = "onchange=\"return btn_submit('list')\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYDIV", $model->field["PROFICIENCYDIV"], $extra, 1);

        //実力コードコンボボックス
        $query = knjh543Query::getProficiencyCd($model->field["SEMESTER"], $model->field["PROFICIENCYDIV"]);
        $extra = "onchange=\"return btn_submit('list')\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYCD", $model->field["PROFICIENCYCD"], $extra, 1);

        //区分ラジオボタン 1:課程学科コース 2:コースグループ
        $opt_div = array(1, 2);
        $divDefault = "1";
        if ($model->Properties["usePerfectCourse"] == "1") {
            $arg["usePerfectCourse"] = "1";
            if ($model->Properties["usePerfectCourseGroup"] == "1") {
                $divDefault = "2";
            }
        }
        $model->field["DIV"] = ($model->field["DIV"] == "") ? $divDefault : $model->field["DIV"];

        $extra = array("id=\"DIV1\" onclick =\" return btn_submit('list');\"", "id=\"DIV2\" onclick =\" return btn_submit('list');\"");
        $radioArray = knjCreateRadio($objForm, "DIV", $model->field["DIV"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //項目名
        $arg["TITLE"] = ($model->field["DIV"] == "1") ? '課程学科コース' : 'コースグループ';

        //考査科目リスト
        makeProficiencySubclassList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        if (VARS::post("cmd") == "list"){
            $arg["reload"] = "window.open('knjh543index.php?cmd=sel&SEMESTER={$model->semester}&PROFICIENCYDIV={$model->proficiencydiv}&PROFICIENCYCD={$model->proficiencycd}&DIV={$model->div}','right_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjh543Form1.html", $arg); 
    }
}

//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//考査科目リスト
function makeProficiencySubclassList(&$arg, $db, $model) {
    $p_cnt = $a_cnt = 1;
    $result = $db->query(knjh543Query::getProficiencySubclassRepCombDatList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //行数取得
        $p_subclass_cnt = $db->getOne(knjh543Query::getP_SubclassCnt($model, $row));
        $a_subclass_cnt = $db->getOne(knjh543Query::getA_SubclassCnt($model, $row));

        $row["PROFICIENCY_SUBCLASS_NAME"] = View::alink("knjh543index.php",
                            $row["PROFICIENCY_SUBCLASS_NAME"],
                            "target=right_frame",
                            array("cmd"             => "sel",
                                  "SEND_FLG"        => "P_SUB",
                                  "SEMESTER"        => $model->field["SEMESTER"],
                                  "PROFICIENCYDIV"  => $model->field["PROFICIENCYDIV"],
                                  "PROFICIENCYCD"   => $model->field["PROFICIENCYCD"],
                                  "DIV"             => $model->field["DIV"],
                                  "PROFICIENCY_SUBCLASS_CD" => $row["PROFICIENCY_SUBCLASS_CD"]));

        $row["COURSE_NAME"] = View::alink("knjh543index.php",
                            $row["COURSE_NAME"],
                            "target=right_frame",
                            array("cmd"             => "sel",
                                  "SEND_FLG"        => "A_SUB",
                                  "SEMESTER"        => $model->field["SEMESTER"],
                                  "PROFICIENCYDIV"  => $model->field["PROFICIENCYDIV"],
                                  "PROFICIENCYCD"   => $model->field["PROFICIENCYCD"],
                                  "DIV"             => $model->field["DIV"],
                                  "PROFICIENCY_SUBCLASS_CD" => $row["PROFICIENCY_SUBCLASS_CD"],
                                  "GRADE"           => $row["GRADE"],
                                  "COURSE"          => $row["COURSE"]));

        if ($p_cnt == 1) $row["ROWSPAN1"] = ($p_subclass_cnt == 0) ? 1 : $p_subclass_cnt;   //合併先科目の行数
        if ($a_cnt == 1) $row["ROWSPAN2"] = ($a_subclass_cnt == 0) ? 1 : $a_subclass_cnt;   //合併元科目の行数

        $arg["data"][] = $row;

        if ($p_cnt == $p_subclass_cnt || ($p_subclass_cnt == 0)) {
            $p_cnt = 1;
        } else {
            $p_cnt++;
        }

        if (($a_cnt == $a_subclass_cnt) || ($a_subclass_cnt == 0)) {
            $a_cnt = 1;
        } else {
            $a_cnt++;
        }
    }
    $result->free();
}
?>
