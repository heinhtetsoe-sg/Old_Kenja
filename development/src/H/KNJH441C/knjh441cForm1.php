<?php

require_once('for_php7.php');

class knjh441cForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh441cForm1", "POST", "knjh441cindex.php", "", "knjh441cForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        /**********/
        /* コンボ */
        /**********/
        //学年
        $query = knjh441cQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "");
        
        //学科
        $query = knjh441cQuery::getMajor($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "MAJOR", $model->field["MAJOR"], $extra, 1, "");

        //学期
        $query = knjh441cQuery::getSemester($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);
        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }
        
        //学校校種を取得
        $model->schoolKind = $db->getOne(knjh441cQuery::getSchoolkindQuery($model->field["GRADE"]));
        knjCreateHidden($objForm, "setSchoolKind", $model->schoolKind);

        //模試名
        $query = knjh441cQuery::getMockName($model, $model->field["GRADE"]);
        $opt = array();
        $value_flg = false;
        $result = $db->query($query);
        $add = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            $add = true;
            if ($model->mock_cd == $row["VALUE"]) $value_flg = true;
        }

        $model->mock_cd = ($model->mock_cd && $value_flg) ? $model->mock_cd : $opt[0]["value"];
        $extra = " onchange=\"return btn_submit('main');\"";
        $arg["data"]["MOCKCD"] = knjCreateCombo($objForm, "MOCKCD", $model->mock_cd, $opt, $extra, 1);

        /********************/
        /* テキストボックス */
        /********************/

        //度数分布を出力する
        $extra = $model->cmd == '' || $model->field["OUTPUT_DOSUBUPU"] == "1" ? "checked" : "";
        $extra .= " id=\"OUTPUT_DOSUBUPU\" ";
        $arg["data"]["OUTPUT_DOSUBUPU"] = knjCreateCheckBox($objForm, "OUTPUT_DOSUBUPU", "1", $extra, "");

        //優良者を出力する
        $extra = $model->cmd == '' || $model->field["OUTPUT_YURYO"] == "1" ? "checked" : "";
        $extra .= " id=\"OUTPUT_YURYO\" ";
        $arg["data"]["OUTPUT_YURYO"] = knjCreateCheckBox($objForm, "OUTPUT_YURYO", "1", $extra, "");
        //優良者
        $extra = " id=\"YURYO\" onBlur=\"return this.value=toInteger(this.value);\" style=\"text-align: right;\"";
        $model->field["YURYO"] = $model->field["YURYO"] ? $model->field["YURYO"] : "40";
        $arg["data"]["YURYO"] = knjCreateTextBox($objForm, $model->field["YURYO"], "YURYO", 3, 3, $extra);

        //不振を出力する
        $extra = $model->cmd == '' || $model->field["OUTPUT_FUSHIN"] == "1" ? "checked" : "";
        $extra .= " id=\"OUTPUT_FUSHIN\" ";
        $arg["data"]["OUTPUT_FUSHIN"] = knjCreateCheckBox($objForm, "OUTPUT_FUSHIN", "1", $extra, "");
        //不振者
        $extra = " id=\"FUSHIN\" onBlur=\"return this.value=toInteger(this.value);\" style=\"text-align: right;\"";
        $model->field["FUSHIN"] = $model->field["FUSHIN"] ? $model->field["FUSHIN"] : "40";
        $arg["data"]["FUSHIN"] = knjCreateTextBox($objForm, $model->field["FUSHIN"], "FUSHIN", 3, 3, $extra);

        /**********/
        /* ボタン */
        /**********/
        //CSV出力
        //プレビュー/印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'print');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR",     CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE",     str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "DBNAME",        DB_DATABASE);
        knjCreateHidden($objForm, "PRGID",         "KNJH441C");
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);

        //学期
        knjCreateHidden($objForm, "SEME_DATE",  $seme);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh441cForm1.html", $arg);
    }
}
/****************************************************** 以下関数 ************************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = ($name == "SEMESTER") ? CTRL_SEMESTER : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
