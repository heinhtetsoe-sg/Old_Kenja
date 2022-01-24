<?php

require_once('for_php7.php');

class knjl110oForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl110oindex.php", "", "main");
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボ
        $query = knjl110oQuery::GetName($model->year, "L003");
        $extra = " onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //入試区分コンボ
        $query = knjl110oQuery::getTestdivMst($model->year);
        $extra = " onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "");

        //受験型コンボ
        $query = knjl110oQuery::GetName($model->year, "L005", "2");
        $extra = " onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["EXAM_TYPE"], "EXAM_TYPE", $extra, 1, "");

        //受験コース取得
        $query = knjl110oQuery::selectQueryCourse($model);
        $model->course = $db->getOne($query);

        makeMeisai($objForm, $arg, $db, $model);

        //再計算ボタン
        $extra = "onclick=\"btn_submit('saikeisan');\"";
        $arg["button"]["btn_saikeisan"] = knjCreateBtn($objForm, "btn_saikeisan", "再計算", $extra);

        //更新ボタン
        $extra = "onclick=\"btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl110oForm1.html", $arg);
    }

}
//makeCmb
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

function makeMeisai(&$objForm, &$arg, $db, $model)
{
    $query = knjl110oQuery::getClassDivName($model->year, $model);
    $setClassDiv = $db->getOne($query);
    if ($setClassDiv == "1") {
        $specialTitle = "アップ合格　最低点";
    } else if ($setClassDiv == "2") {
        $specialTitle = "スライド合格　最低点";
    } else {
        $specialTitle = "特別進学クラス合格　最低点";
    }
    $arg["SPECIAL_TITLE"] = $specialTitle;

    $query = knjl110oQuery::GetName($model->year, "L009");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arg["KAMOKU".$row["VALUE"]] = $row["NAME1"];
    }
    $result->free();
    if ($model->cmd == "saikeisan") {
        $query = knjl110oQuery::getSaikeisanSql($model, $setClassDiv);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SEX"] == "1") {
                $model->meisaiField["M_AVG".$row["TESTSUBCLASSCD"]] = floor($row["AVG1"] * 10) / 10;
            } else if ($row["SEX"] == "2") {
                $model->meisaiField["W_AVG".$row["TESTSUBCLASSCD"]] = floor($row["AVG1"] * 10) / 10;
            } else if ($row["SEX"] == "9") {
                $model->meisaiField["T_AVG".$row["TESTSUBCLASSCD"]] = floor($row["AVG1"] * 10) / 10;
                $model->meisaiField["MAX".$row["TESTSUBCLASSCD"]] = $row["MAX_SCORE"];
                $model->meisaiField["MIN".$row["TESTSUBCLASSCD"]] = $row["MIN_SCORE"];
            }
        }
        $result->free();
    } else if ($model->cmd == "" || $model->cmd == "main") {
        //クリア
        foreach ($model->kamoku as $key => $val) {
            $model->meisaiField["M_AVG".$key] = "";
            $model->meisaiField["W_AVG".$key] = "";
            $model->meisaiField["T_AVG".$key] = "";
            $model->meisaiField["MAX".$key]   = "";
            $model->meisaiField["MIN".$key]   = "";
        }

        $query = knjl110oQuery::getJudgeAvg($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->meisaiField["M_AVG".$row["TESTSUBCLASSCD"]] = $row["AVARAGE_MEN"];
            $model->meisaiField["W_AVG".$row["TESTSUBCLASSCD"]] = $row["AVARAGE_WOMEN"];
            $model->meisaiField["T_AVG".$row["TESTSUBCLASSCD"]] = $row["AVARAGE_TOTAL"];
            $model->meisaiField["MAX".$row["TESTSUBCLASSCD"]]   = $row["MAX_SCORE"];
            $model->meisaiField["MIN".$row["TESTSUBCLASSCD"]]   = $row["MIN_SCORE"];
        }
        $result->free();
    }

    foreach ($model->meisaiField as $key => $val) {
        $pos = strpos($key, "AVG");
        if ($pos === false) {
            $extra = "style=\"text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        } else {
            $extra = "style=\"text-align:right;\" onblur=\"this.value=toNumber(this.value);\"";
        }
        $arg["data"][$key] = knjCreateTextBox($objForm, $val, $key, 5, 5, $extra);
    }
}
?>