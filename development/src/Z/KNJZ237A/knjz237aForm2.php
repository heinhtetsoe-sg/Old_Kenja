<?php

require_once('for_php7.php');

class knjz237aForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz237aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //Z010より適用学校情報取得
        $query = knjz237aQuery::getNameMst();
        $getname = $db->getone($query);

        //学期表示
        $query = knjz237aQuery::getSemester($getname, $model->semester);
        $setSeme = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["SEMESTER"] = $setSeme["LABEL"];

        //テスト表示
        if ($model->semester !== '9' && $model->testkindcd == '99-01' && $getname === 'meiji') {
            $arg["data"]["TESTNAME"] = '平常点';
        } else {
            $query = knjz237aQuery::getTestitemMstCountflgNew($model->semester, $model->testkindcd);
            $testName = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["data"]["TESTNAME"] = $testName["TESTITEMNAME"];
        }

        //科目コンボ
        $query = knjz237aQuery::getSubclassMst($model->semester, $model);
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", "", 1, "BLANK");

        //区分ラジオボタン 1:科目 2:学年 3:課程学科コース 4:コースグループ
        $opt = array(1, 2, 3, 4);
        $model->field["DIV"] = ($model->field["DIV"] == "" || $model->field["DIV"] == "00") ? "1" : $model->field["DIV"];
        $click = " onClick=\"btn_submit('edit');\"";
        $extra = array("id=\"DIV1\"".$click, "id=\"DIV2\"".$click, "id=\"DIV3\"".$click, "id=\"DIV4\"".$click);
        $radioArray = knjCreateRadio($objForm, "DIV", $model->field["DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //区分（コースグループ）表示
        if ($model->Properties["usePerfectCourseGroup"] == "1") {
            $arg["usePerfectCourseGroup"] = "1";
        }

        //各項目の表示
        if ($model->field["DIV"] == "02") {
            $arg["view1"] = 1;
        } else if ($model->field["DIV"] == "03") {
            $arg["view1"] = 1;
            $arg["view2"] = 1;
        } else if ($model->field["DIV"] == "04") {
            $arg["view1"] = 1;
            $arg["view3"] = 1;
        }

        //学年コンボ
        $query = knjz237aQuery::getGrade();
        $extra = "";
        if ($model->field["DIV"] == "04") {
            $extra = "onChange=\"document.forms[0].MAJORCD.value = ''; btn_submit('edit');\"";
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "BLANK");

        //課程コンボ
        $query = knjz237aQuery::getCourse();
        $extra = "onChange=\"document.forms[0].MAJORCD.value = ''; btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["COURSECD"], "COURSECD", $extra, 1, "BLANK");

        //学科コンボ
        if ($model->field["DIV"] == "04") {
            $query = knjz237aQuery::getCourseGroup($model->field["GRADE"]);
        } else {
            $query = knjz237aQuery::getMajor($model->field["COURSECD"]);
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["MAJORCD"], "MAJORCD", "", 1, "BLANK");

        //コースコンボ
        $query = knjz237aQuery::getCoursecode();
        makeCmb($objForm, $arg, $db, $query, $model->field["COURSECODE"], "COURSECODE", "", 1, "BLANK");

        //満点テキスト
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PERFECT"] = knjCreateTextBox($objForm, $model->field["PERFECT"], "PERFECT", 3, 3, $extra);

        //合格点テキスト
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PASS_SCORE"] = knjCreateTextBox($objForm, $model->field["PASS_SCORE"], "PASS_SCORE", 3, 3, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::post("cmd") == "update" || VARS::post("cmd") == "add" || VARS::post("cmd") == "delete") {
            $arg["reload"]  = "window.open('knjz237aindex.php?cmd=list_update','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz237aForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
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

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //追加ボタン
    $extra = "onclick=\"return doSubmit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return doSubmit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return doSubmit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return doSubmit('reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SEMESTER", $model->semester);
    knjCreateHidden($objForm, "TESTKINDCD", $model->testkindcd);
}
?>
