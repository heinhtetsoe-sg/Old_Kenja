<?php

require_once('for_php7.php');

class knjs030Bunkatu
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjs030index.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* タイトル */
        setTitleData2($arg, $db, $model);

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model);

        /* ボタン作成 */
        makeButton($objForm, $arg, $model);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素(cmdをセット)作成 */
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "bunkatuSu", $model->bunkatuSu);


        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjs030Bunkatu.html", $arg);
    }
}

//タイトル設定
function setTitleData2(&$arg, $db, $model)
{
    foreach ($model->weekArray as $key => $val) {
        foreach ($val["DAY"] as $valkey => $valval) {
            if ($valval == $model->bunKatuField["BUNKATU_DATE"]) {
                $arg["TOP"]["TITLE"] = $val["WAREKI"][$valkey]."(".$val["WEEK"].")";
                break;
            }
        }
    }
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model) {

    //講座コンボ
    $optSubclassAll = array();
    $optSubclass = array();
    $optSubclass["1"][] = array('label' => "",
                                'value' => "");
    $optSubclass["2"][] = array('label' => "",
                                'value' => "");
    $optSubclass["3"][] = array('label' => "",
                                'value' => "");
    $query = knjs030Query::getChair($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setLabel = $model->field["JISU_SHOW"] == "1" ? $row["SUBCLASSNAME"] : $row["CLASSABBV"];
        if ($model->field["HR_OR_STAFF"] == "1") {
            if ($model->field["DISP_SHOW"] == "2") {
                $setLabel = $row["STAFFNAME"];
            } else if ($model->field["DISP_SHOW"] == "3") {
                $setLabel = $setLabel."/".$row["STAFFNAME"];
            }
        }
        $setLabel = strlen($setLabel) > 27 ? substr($setLabel, 0, 24) : $setLabel;
        $optSubclass[$row["SEMESTER"]][] = array('label' => $setLabel,
                                                 'value' => $row["VALUE"]);
        //教育課程対応
        $classcd = $row["CLASSCD"];
        if ($model->Properties["useCurriculumcd"] == '1') {
            $classcd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"];
        }
        $setAllCd = $model->field["JISU_SHOW"] == "1" ? $row["VALUE"] : $classcd;
        $setAllName = $model->field["JISU_SHOW"] == "1" ? $row["SUBCLASSABBV"] : $row["CLASSABBV"];
        $optSubclassAll[$setAllCd] = $setAllName;
    }
    $result->free();

    $query = knjs030Query::getBunkatu($model, $model->bunKatuField["BUNKATU_DATE"], $model->bunKatuField["BUNKATU_PERIOD"], $model->bunKatuField["BUNKATU_CHAIRCD"]);
    $result = $db->query($query);
    $bunkatuData = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $bunkatuData[$row["SEQ"]] = $row;
    }

    for ($i = 1; $i <= $model->bunkatuSu; $i++) {
        $extra = "";
        $bunChair = $bunkatuData[$i]["CHAIRCD"];
        $arg["data"]["B_CHAIRCD".$i] = knjCreateCombo($objForm, "B_CHAIRCD".$i, $bunChair, $optSubclass[$model->bunKatuField["BUNKATU_SEME"]], $extra, 1);

        $extra = "onblur=\"checkMinute(this)\"";
        $bunMinute = $bunkatuData[$i]["MINUTE"];
        $arg["data"]["B_MINUTE".$i] = knjCreateTextBox($objForm, $bunMinute, "B_MINUTE".$i, 2, 2, $extra);
    }

}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    if ($model->field["HR_OR_STAFF"] == "1") {
        //保存ボタン
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "保 存", " onclick=\"return btn_submit('bunUpd');\"");
        //取消ボタン
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    }
    //終了
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"return btn_submit('subEnd');\"");
}
?>
