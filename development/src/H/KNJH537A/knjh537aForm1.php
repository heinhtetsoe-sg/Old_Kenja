<?php

require_once('for_php7.php');

class knjh537aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjh537aindex.php", "", "sel");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度、学期表示
        $arg["YEAR"] = CTRL_YEAR."年度";

        //学期コンボボックス
        $query = knjh537aQuery::getSemester();
        $extra = "onchange=\"return btn_submit('list')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);

        //実力区分コンボボックス
        $query = knjh537aQuery::getProficiencyDiv($model->semester);
        $extra = "onchange=\"return btn_submit('list')\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYDIV", $model->proficiencydiv, $extra, 1);

        //実力コードコンボボックス
        $query = knjh537aQuery::getProficiencyCd($model->semester, $model->proficiencydiv);
        $extra = "onchange=\"return btn_submit('list')\"";
        makeCmb($objForm, $arg, $db, $query, "PROFICIENCYCD", $model->proficiencycd, $extra, 1);

        //区分ラジオボタン 1:課程学科コース 2:コースグループ
        $opt_div = array(1, 2);
        $default = ($model->Properties["usePerfectCourseGroup"] == "1") ? "2" : "1";
        $model->div = ($model->div == "") ? $default : $model->div;
        $extra = array("id=\"DIV1\" onclick =\" return btn_submit('list');\"", "id=\"DIV2\" onclick =\" return btn_submit('list');\"");
        $radioArray = knjCreateRadio($objForm, "DIV", $model->div, $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //項目名
        $arg["TITLE"] = ($model->div == "1") ? '課程学科コース' : 'コースグループ';

        //考査科目リスト
        makeProficiencySubclassList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        if (VARS::post("cmd") == "list"){
            $arg["reload"] = "window.open('knjh537aindex.php?cmd=sel&SEMESTER={$model->semester}&PROFICIENCYDIV={$model->proficiencydiv}&PROFICIENCYCD={$model->proficiencycd}&DIV={$model->div}','right_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjh537aForm1.html", $arg); 
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
    $s_cnt = 1;
    $result = $db->query(knjh537aQuery::getProficiencySubclassYdatList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //行数取得
        $subclass_cnt  = $db->getOne(knjh537aQuery::getSubclassCnt($model, $row));
        $row["PROFICIENCY_SUBCLASS_NAME"] = View::alink("knjh537aindex.php",
                            $row["PROFICIENCY_SUBCLASS_NAME"],
                            "target=right_frame",
                            array("cmd"             => "sel",
                                  "SEMESTER"        => $model->semester,
                                  "PROFICIENCYDIV"  => $model->proficiencydiv,
                                  "PROFICIENCYCD"   => $model->proficiencycd,
                                  "DIV"             => $model->div,
                                  "GRADE"           => $row["GRADE"],
                                  "COURSE"          => $row["COURSE"],
                                  "PROFICIENCY_SUBCLASS_CD" => $row["PROFICIENCY_SUBCLASS_CD"]));


        if ($s_cnt == 1) $row["ROWSPAN"] = $subclass_cnt;  //行数

        $arg["data"][] = $row;

        if ($s_cnt == $subclass_cnt) {
            $s_cnt = 1;
        } else {
            $s_cnt++;
        }
    }
    $result->free();
}
?>
