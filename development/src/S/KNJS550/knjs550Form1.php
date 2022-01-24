<?php

require_once('for_php7.php');


class knjs550form1 {
    function main(&$model) {
        
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjs550index.php", "", "main");

        $db = Query::dbCheckOut();

        //年度コンボボックス
        $query = knjs550Query::selectYearQuery();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "TITLE", "YEAR", $extra, 1, "");

        //学校区分コンボ
        $query = knjs550Query::getSchoolkind($model);
        $opt2 = array();
        $value_flg2 = false;
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt2[] = array('label' => $row2["LABEL"],
                           'value' => $row2["VALUE"]);
            if ($model->field["SCHOOL_KIND"] == $row2["VALUE"]) $value_flg2 = true;               
        }
        $model->field["SCHOOL_KIND"] = ($model->field["SCHOOL_KIND"] && $value_flg2) ? $model->field["SCHOOL_KIND"] : $opt2[0]["value"];
        $extra = "onchange=\"return btn_submit('main');\"";
        $arg["info"]["SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $opt2, $extra, 1);

        //表示データ
        setDispData($objForm, $arg, $db, $model);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);

        //前年度からコピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //Hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $row["UPDATED"]);

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjs550Form1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $argName, $name, $extra, $size, $blank = "", $retCmb = false)
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
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    if ($retCmb) {
        return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        $arg[$argName][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }

}

//データ表示用
function setDispData(&$objForm, &$arg, $db, $model) {

    //学年と教科を取得
    $opt = array();

    //教科を取得
    $model->optSubclass = array();
    $querySubclass  = knjs550Query::getSubclass($model);
    $result = $db->query($querySubclass);
    while ($rowSubclass = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->optSubclass[] = array('label'  => $rowSubclass["LABEL"],
                                      'value'  => $rowSubclass["VALUE"]);
        $arg["data"]["SUBCLASSCD"] = $rowSubclass["LABEL"];
    }

    $setKekka = array();
    for ($Scount = 0; $Scount < get_count($model->optSubclass); $Scount++) {
        $setKekka[$Scount]["SUBCLASSCD"] = $model->optSubclass[$Scount]['label'];
    }
    $result->free();

    //学年を取得
    $model->optGrade = array();

    $queryGrade = knjs550Query::getGrade($model);
    $result = $db->query($queryGrade);
    while ($rowGrade = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->optGrade[] = array('label' => $rowGrade["LABEL"],
                                   'value' => $rowGrade["VALUE"]);
    }

    for ($Gcount = 0; $Gcount < get_count($model->optGrade); $Gcount++) {
        $arg["data"]["GRADE_"."$Gcount"] = $model->optGrade[$Gcount]['label'];
    }
    $result->free();

    //教科のセット用変数
    $subclasscd;
    //学年のセット用変数
    $grade;

    for ($Scount = 0; $Scount < get_count($model->optSubclass); $Scount++) {
        //教科データの確認用（各学年の教科データが存在するかを確認していく）
        $subclasscd = $model->optSubclass[$Scount]['value'];
        for ($Gcount = 0; $Gcount < get_count($model->optGrade); $Gcount++) {

            //UNIT_STUDY_CLASS_DAT
            $grade = $model->optGrade[$Gcount]['value'];
            $query = knjs550Query::getUnitStudyCount($model, $subclasscd, $grade);
            $unitclassGradeCnt = $db->getOne($query);

            //UNIT_STUDY_CLASS_DATデータがある場合のテキストボックス作成
            if ($unitclassGradeCnt != 0) {

                //UNIT_CLASS_LESSON_SCHOOL_DATデータ取得
                $query = knjs550Query::setUnitClassDat($model, $subclasscd, $grade);
                $setStandardTime = $db->getOne($query);
                //標準時数
                $setName = "_".$Scount."_".$Gcount;
                $extra = "onblur=\"this.value=toInteger(this.value);\"";
                $setKekka[$Scount]["STANDARD_TIME_".$Gcount] = knjCreateTextBox($objForm, $setStandardTime, "STANDARD_TIME".$setName, 6, 9, $extra);
                $setKekka[$Scount]["STANDARD_COLOR_".$Gcount] = "#ffffff";

            //データがない場合はテキストボックスの表示
            } else {
                $setKekka[$Scount]["STANDARD_COLOR_".$Gcount] = "#cccccc";
            }
        }
    }
    for (; $Gcount < 6; $Gcount++) {
        for ($Scount = 0; $Scount < get_count($model->optSubclass); $Scount++) {
            $setKekka[$Scount]["STANDARD_COLOR_".$Gcount] = "#cccccc";
        }
    }
    $arg["kekka"] = $setKekka;

}

?>
