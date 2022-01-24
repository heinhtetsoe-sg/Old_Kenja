<?php

require_once('for_php7.php');

class knjh540aForm2 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh540aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $Row =& $model->field;

        if ($model->field["DIV"] == "02") {
            $arg["view1"] = 1;
        } else if ($model->field["DIV"] == "03") {
            $arg["view1"] = 1;
            $arg["view2"] = 1;
        } else if ($model->field["DIV"] == "04") {
            $arg["view1"] = 1;
            $arg["view3"] = 1;
        }

        /* 学期 */
        $query = knjh540aQuery::getSemester($model->semester);
        $setSeme = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["SEMESTER"] = $setSeme["LABEL"];

        /* テスト */
        $query = knjh540aQuery::getProficiencyName($model);
        $proficiencyName = $db->getOne($query);
        $arg["data"]["PROFICIENCYNAME"] = $proficiencyName;

        /* 科目コンボボックス作成 */
        $query = knjh540aQuery::getProficiencySubclassCd();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["PROFICIENCY_SUBCLASS_CD"], "PROFICIENCY_SUBCLASS_CD", $extra, 1, "BLANK");

        /* 区分ラジオボタン */
        if ($model->Properties["usePerfectCourseGroup"] == "1") {
            $arg["usePerfectCourseGroup"] = "1";
        }
        $opt = array(1, 2, 3, 4);
        if ($model->Properties["usePerfectCourseGroup"] == "1") {
            $model->field["DIV"] = ($model->field["DIV"] == "" || $model->field["DIV"] == "00") ? "4" : $model->field["DIV"];
        } else {
            $model->field["DIV"] = ($model->field["DIV"] == "" || $model->field["DIV"] == "00") ? "1" : $model->field["DIV"];
        }
        $extra = array("id=\"DIV1\" onClick=\"btn_submit('edit');\"", "id=\"DIV2\" onClick=\"btn_submit('edit');\"", "id=\"DIV3\" onClick=\"btn_submit('edit');\"", "id=\"DIV4\" onClick=\"btn_submit('edit');\"");
        $radioArray = knjCreateRadio($objForm, "DIV", $model->field["DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        /* 学年コンボボックス */
        $query = knjh540aQuery::getGrade($model);
        $extra = "";
        if ($model->field["DIV"] == "04") {
            $extra = "onChange=\"document.forms[0].MAJORCD.value = ''; btn_submit('edit');\"";
        }
        makeCmb($objForm, $arg, $db, $query, $Row["GRADE"], "GRADE", $extra, 1, "BLANK");

        /* 課程コンボボックス */
        $query = knjh540aQuery::getCourse();
        $extra = "onChange=\"document.forms[0].MAJORCD.value = ''; btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["COURSECD"], "COURSECD", $extra, 1, "BLANK");

        /* 学科コンボボックス */
        if ($model->field["DIV"] == "04") {
            $query = knjh540aQuery::getCourseGroup($Row["GRADE"]);
        } else {
            $query = knjh540aQuery::getMajor($Row["COURSECD"]);
        }
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["MAJORCD"], "MAJORCD", $extra, 1, "BLANK");

        /* コースコンボボックス */
        $query = knjh540aQuery::getCoursecode();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["COURSECODE"], "COURSECODE", $extra, 1, "BLANK");

        /* 満点 */
        $extra = "onblur=\"calc(this);\"";
        $arg["data"]["PERFECT"] = knjCreateTextBox($objForm, $Row["PERFECT"], "PERFECT", 3, 3, $extra);

        /* 合格点 */
        $extra = "onblur=\"calc(this);\"";
        $arg["data"]["PASS_SCORE"] = knjCreateTextBox($objForm, $Row["PASS_SCORE"], "PASS_SCORE", 3, 3, $extra);

        /* 傾斜 */
        $extra = "onblur=\"calc(this);\"";
        $arg["data"]["WEIGHTING"] = knjCreateTextBox($objForm, $Row["WEIGHTING"], "WEIGHTING", 3, 3, $extra);

        //追加ボタンを作成する
        $extra = "onclick=\"return doSubmit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタンを作成する
        $extra = "onclick=\"return doSubmit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return doSubmit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタンを作成する
        $extra = "onclick=\"return doSubmit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SEMESTER", $model->semester);
        knjCreateHidden($objForm, "PROFICIENCYDIV", $model->proficiencydiv);
        knjCreateHidden($objForm, "PROFICIENCYCD", $model->proficiencycd);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::post("cmd") == "update" || VARS::post("cmd") == "add" || VARS::post("cmd") == "delete") {
            $arg["reload"]  = "window.open('knjh540aindex.php?cmd=list_update','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh540aForm2.html", $arg);
    }
}

function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = (strlen($value) > 0 && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
