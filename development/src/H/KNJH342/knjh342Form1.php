<?php

require_once('for_php7.php');


//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjh342Form1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        /* CSV */
        $objUp = new csvFile();

        $arg["start"]   = $objForm->get_start("main", "POST", "knjh342index.php", "", "main");

        //プロパティチェック
        if ($model->Properties["usePerfSubclasscd_Touroku"] !== '1') {
            $arg["jscript"] = "OnPropertiesError();";
        }

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["YEAR"] = CTRL_YEAR;

        /* 学年コンボ */
        $query = knjh342Query::getGrade($model);
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "BLANK");

        /* データ種別コンボ */
        $query = knjh342Query::getMockDiv();
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["DATA_DIV"], "DATA_DIV", $extra, 1);

        /* テスト名称コンボ */
        $query = knjh342Query::getMockMst($model->field["DATA_DIV"]);
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["MOCK_TARGET"], "MOCK_TARGET", $extra, 1);

        if ($model->field["GRADE"]) {
            /* 編集対象データリスト */
            makeDataList($objForm, $arg, $db, $model);
        }

        /* ボタン作成 */
        makeButton($objForm, $arg);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden作成 */
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjh342Form1.html", $arg);
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
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model)
{
    $query  = knjh342Query::selectMainData($model);
    $result = $db->query($query);

    $subclassCd = "";
    $sep = "";
    $data = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $subclassCd .= $sep.$row["PREF_SUBCLASSCD"];

        if ($row["AVG"]) {
            $avgData = explode(".", $row["AVG"]);
            $row["AVG"] = $avgData[0].".".substr($avgData[1], 0, 1);
        }
        $extra = "STYLE=\"text-align: right\" onPaste=\"return showPaste(this);\" onBlur=\"return checkVal(this);\"";
        $row["AVG"] = knjCreateTextBox($objForm, $row["AVG"], "AVG-".$row["PREF_SUBCLASSCD"], 9, 9, $extra);
        $row["PREF_SUBCLASSNAME"] = $row["PREF_SUBCLASSCD"]."：".$row["SUBCLASS_NAME"];

        $arg["data"][] = $row;
        $sep = ":";
    }
    knjCreateHidden($objForm, "subclassCd", $subclassCd);

}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //保存ボタン
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "保 存", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

?>
