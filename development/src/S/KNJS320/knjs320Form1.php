<?php

require_once('for_php7.php');

class knjs320Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjs320Form1", "POST", "knjs320index.php", "", "knjs320Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        //年組コンボ
        $query = knjs320Query::getGradeHrClass($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", "", 1);

        //対象月コンボ
        $query = knjs320Query::getMonthAll($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TARGET_MONTH"], "TARGET_MONTH", "", 1);
        //makeMonthSemeCmb($objForm, $arg, $db, $model);

        //帳票選択ラジオ 1:行事予定表(単月) 2:行事予定表(３ヶ月分) 3:月間計画表
        $opt = array(1, 2, 3);
        $extra = array("id=\"TARGET_FORM1\"", "id=\"TARGET_FORM2\"", "id=\"TARGET_FORM3\"");
        $model->field["TARGET_FORM"] = strlen($model->field["TARGET_FORM"]) ? $model->field["TARGET_FORM"] : "1";
        $radioArray = knjCreateRadio($objForm, "TARGET_FORM", $model->field["TARGET_FORM"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //行事予定表(３ヶ月分)ラジオ 1:行事 2:備考
        $opt = array(1, 2);
        $extra = array("id=\"FORM2_REMARK1\"", "id=\"FORM2_REMARK2\"");
        $model->field["FORM2_REMARK"] = strlen($model->field["FORM2_REMARK"]) ? $model->field["FORM2_REMARK"] : "1";
        $radioArray = knjCreateRadio($objForm, "FORM2_REMARK", $model->field["FORM2_REMARK"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //月間計画表ラジオ 1:行事 2:備考
        $opt = array(1, 2);
        $extra = array("id=\"FORM3_REMARK1\"", "id=\"FORM3_REMARK2\"");
        $model->field["FORM3_REMARK"] = strlen($model->field["FORM3_REMARK"]) ? $model->field["FORM3_REMARK"] : "1";
        $radioArray = knjCreateRadio($objForm, "FORM3_REMARK", $model->field["FORM3_REMARK"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjs320Form1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model, $db)
{
    knjCreateHidden($objForm, "PRGID", "KNJS320");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
    knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
    knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
    knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
    knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "TARGET_MONTH") {
        $ctrl_date = preg_split("/-/", CTRL_DATE);
        $value = ($value && $value_flg) ? $value : $ctrl_date[1];
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model) {
    $opt_month = array();
    $value_flg = false;
    $value = "";
    $query = knjs320Query::getSemesAll();
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        for ($i = $row["S_MONTH"]; $i <= $row["E_MONTH"]; $i++) {
            $month = ($i > 12) ? ($i - 12) : $i;

            //対象月名称取得
            $monthname = $db->getOne(knjs320Query::getMonthName($month, $model));
            if ($monthname) {
                $opt_month[] = array("label" => $monthname." (".$row["SEMESTERNAME"].") ",
                                     "value" => $month.'-'.$row["SEMESTER"]);

                if ($model->field["TARGET_MONTH"] == $month.'-'.$row["SEMESTER"]) {
                    $value_flg = true;
                }
            }
        }
    }
    $result->free();

    //初期値はログイン月
    $ctrl_date = preg_split("/-/", CTRL_DATE);
    $model->field["TARGET_MONTH"] = ($value && $value_flg) ? $value : (int)$ctrl_date[1].'-'.CTRL_SEMESTER;

    $arg["data"]["TARGET_MONTH"] = knjCreateCombo($objForm, "TARGET_MONTH", $model->field["TARGET_MONTH"], $opt_month, "", 1);
}
?>
