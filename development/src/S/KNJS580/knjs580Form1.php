<?php

require_once('for_php7.php');

class knjs580Form1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjs580index.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        //年度コンボボックス
        $query = knjs580Query::selectYearQuery($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1, "");

        //学年コンボ
        $query = knjs580Query::getGrade($model);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "");

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model);

        /* ボタン作成 */
        makeButton($objForm, $arg, $model);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素(cmdをセット)作成 */
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjs580Form1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $retCmb = false)
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
        //データが存在する学年の最小値を初期値とする
        $gradequery = knjs580Query::getMinGrade();
        $MinGrade = $db->getOne($gradequery);
        for ($i = 0; $i <= get_count($opt); $i++) {
            if ($MinGrade == $opt[$i]["value"]) {
                $value = ($value && $value_flg) ? $value : $opt[$i]["value"];
                break;
            }
        }
    }

    if ($retCmb) {
        return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }

}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model) {

    $query = knjs580Query::getUnitStudySubclass($model);
    $result = $db->query($query);
    $model->updSubclass = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->updSubclass[$row["VALUE"]] = $row["LABEL"];
    }

    $setCnt = 0;
    $setData = array();
    foreach ($model->updSubclass as $subclassCd => $subclassName) {
        $issueCd = $db->getOne(knjs580Query::getTextIssue($model, $subclassCd));
        $query = knjs580Query::getIssueCompany($model, $subclassCd);
        $extra = " style=\"width:50px\" size=\"15\" onChange=\"setChangeColor('MEISAI_ID_".$subclassCd."')\"";
        $setCmb = makeCmb($objForm, $arg, $db, $query, $issueCd, "MEISAI".$subclassCd, $extra, 15, "BLANK", true);
        $setData[$setCnt]["MEISAI_NAME"] = $subclassName;
        $setData[$setCnt]["MEISAI"] = $setCmb;
        $setData[$setCnt]["MEISAI_ID"] = "MEISAI_ID_".$subclassCd;
        $setCnt++;
    }
    $arg["set_data"] = $setData;
    $keisan = $setCnt > 3 ? $setCnt : 4;
    $arg["setPx"] = 69.4 * $keisan;
    $arg["setCol"] = $setCnt;
    if ($setCnt == 0) {
        $arg["dataNasi"] = "1";
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //保存ボタン
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "保 存", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //終了ボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}
?>
