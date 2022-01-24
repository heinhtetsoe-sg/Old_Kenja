<?php

require_once('for_php7.php');

class knja610cForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knja610cForm1", "POST", "knja610cindex.php", "", "knja610cForm1");

        //データベース接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->field["YEAR"];

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knja610cQuery::getSchoolkind($model);
            $extra = "onchange=\"return btn_submit('init');\"";
            $model->field['SCHOOL_KIND'] = $model->field['SCHOOL_KIND'] ? $model->field['SCHOOL_KIND'] : SCHOOLKIND;
            makeCmb($objForm, $arg, $db, $query, $model->field["SCHOOL_KIND"], "SCHOOL_KIND", $extra, 1);
        }

        //学期コンボボックス
        $opt_sem = array();
        $query = knja610cQuery::getSemester($model->field["YEAR"]);
        $model->field['SEMESTER'] = $model->field['SEMESTER'] ? $model->field['SEMESTER'] : CTRL_SEMESTER;
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        //テスト種別リスト
        $extra = "";
        $query = knja610cQuery::getTestItem($model);
        $model->field['TESTKINDCD'] = $model->field['TESTKINDCD'] ? $model->field['TESTKINDCD'] : "";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTKINDCD"], "TESTKINDCD", $extra, 1);

        //適用日付
        $query = knja610cQuery::getSemesterInfo($model->field["YEAR"], $model->field["SEMESTER"]);
        $semesterInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->field["APPDATE"] = $model->field["APPDATE"] ? $model->field["APPDATE"] : str_replace("-", "/", CTRL_DATE);
        //適用日付が学期の範囲外の場合、学期開始日付を設定
        if ($model->field["APPDATE"] < str_replace("-", "/", $semesterInfo["SDATE"]) 
         || str_replace("-", "/", $semesterInfo["EDATE"]) < $model->field["APPDATE"]) {
            $model->field["APPDATE"] = str_replace("-", "/", $semesterInfo["SDATE"]);
        }
        $arg["data"]["APPDATE"] = View::popUpCalendar2($objForm, "APPDATE", $model->field["APPDATE"], "", "", "");

        //クラス一覧リスト作成する
        $opt_class_left = $opt_class_right = array();
        $query = knja610cQuery::getHrClass($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (in_array($row["VALUE"], $model->selectHrClass)) {
                $opt_class_left[]  = array('value' => $row["VALUE"], 'label' => $row["LABEL"]);
            } else {
                $opt_class_right[] = array('value' => $row["VALUE"], 'label' => $row["LABEL"]);
            }
        }
        $result->free();
        //出力対象クラスリストを作成する 
        $extra = "style=\"width:180px\" ondblclick=\"move('left')\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", '', $opt_class_right, $extra, 15, "");
        //出力対象クラスリストを作成する 
        $extra = "style=\"width:180px\" ondblclick=\"move('right')\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", '', $opt_class_left, $extra, 15, "");

        //対象選択ボタンを作成する（全部）/////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('right', 'ALL');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
        //対象取消ボタンを作成する（全部）/////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('left', 'ALL');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
        //対象選択ボタンを作成する（一部）/////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        //対象取消ボタンを作成する（一部）/////////////////////////////////
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //csvボタンを作成する
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "実行", $extra);
        //ヘッダ有チェックボックス
        $extra = " checked ";
        $arg["chk_header"] = knjCreateCheckBox($objForm, "chk_header", "1", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //データベース接続切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja610cForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeHidden(&$objForm, $model) {

    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    // 学期情報
    knjCreateHidden($objForm, "SEMESTER_SDATE", $model->semesterInfo['SDATE']);
    knjCreateHidden($objForm, "SEMESTER_EDATE", $model->semesterInfo['EDATE']);
    // 出力対象クラス
    knjCreateHidden($objForm, "SELECT_HR_CLASS", implode($model->selectHrClass, ","));
    // フレームロック
    knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
}

?>
