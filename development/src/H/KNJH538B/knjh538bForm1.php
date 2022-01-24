<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjh538bForm1
{
    public function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form();
        /* CSV */
        $objUp = new csvFile();

        $arg["start"]   = $objForm->get_start("main", "POST", "knjh538bindex.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["YEAR"] = CTRL_YEAR;

        /* 学期コンボ */
        $query = knjh538bQuery::getSemester();
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        /* 学年コンボ */
        $query = knjh538bQuery::getGradeHr($model);
        $extra = "onchange=\"btn_submit('main');\"";
        $hrName = makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "BLANK");

        /* データ種別コンボ */
        $query = knjh538bQuery::getProfDiv();
        $extra = "onchange=\"btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCYDIV"], "PROFICIENCYDIV", $extra, 1);

        /* テスト名称コンボ */
        $query = knjh538bQuery::getProfMst($model);
        $extra = "onchange=\"btn_submit('main');\"";
        $testName = makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCY_TARGET"], "PROFICIENCY_TARGET", $extra, 1);

        /* 実力科目コンボ */
        $query = knjh538bQuery::getProfSubclassMst($model);
        $extra = "onchange=\"btn_submit('main');\"";
        $subclassName = makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCY_SUBCLASS_CD"], "PROFICIENCY_SUBCLASS_CD", $extra, 1);

        /* CSV用ヘッダ */
        $header = getHead();

        /* CSV設定 */
        setCsv($objForm, $arg, $objUp, $hrName, $testName, $subclassName, $header);

        /* 編集対象データリスト */
        makeDataList($objForm, $arg, $db, $model, $objUp, $hrName, $testName, $subclassName, $header);

        /* ボタン作成 */
        makeButton($objForm, $arg);

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden作成 */
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJH538B");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "STAFFCD", STAFFCD);
        knjCreateHidden($objForm, "useProficiencyPerfect", $model->Properties["useProficiencyPerfect"]);

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjh538bForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    if ($name == "SEMESTER") {
        $value = ($value && in_array($value, $serch)) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $rtnName = "";
    for ($i = 0; $i < get_count($opt); $i++) {
        $rtnName = ($opt[$i]["value"] == $value) ? $opt[$i][label] : $rtnName;
    }

    return $rtnName;
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, $model, &$objUp, $hrName, $testName, $subclassName, $headerData)
{
    $query  = knjh538bQuery::selectMainData($model);
    $result = $db->query($query);

    $textbox_dnt = 0; //貼付け機能用
    $totalScore = 0;
    $totalCnt = 0;
    $totalAvg = 0;
    $data = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (isset($model->warning)) {
            if (is_array($model->reset[$row["SCHREGNO"]])) {
                foreach ($model->reset[$row["SCHREGNO"]] as $key => $val) {
                    $row[$key] = $val;
                }
            }
        }

        $totalScore += $row["SCORE"];
        $totalCnt += $row["SCORE"] != "" ? 1 : 0;
        //CSVデータセット
        setCsvValue($objUp, $model, $headerData, $hrName, $testName, $row);

        //満点マスタ
        $query = knjh538bQuery::getPerfect($model, $row);
        $perfect = $db->getOne($query);
        if ($perfect == "") {
            $perfect = 100;
        }

        $extra = "STYLE=\"text-align: right\" class=\"SCORE_".$perfect."\" onPaste=\"return showPaste(this, ".$textbox_dnt.");\" onKeyDown=\"keyChangeEntToTab(this)\" onBlur=\"return checkVal(this, ".$perfect.");\"";
        $row["SCORE"] = knjCreateTextBox($objForm, $row["SCORE"], "SCORE[]", 3, 3, $extra);
        $textbox_dnt++;

        /* hidden(学籍番号) */
        $row["SCHREGNO"] = "<input type=\"hidden\" name=\"SCHREGNO[]\" value=\"".$row["SCHREGNO"]."\">";

        $data[] = $row;
    }
    knjCreateHidden($objForm, "objCntSub", $textbox_dnt);

    $arg["attend_data"] = $data;
    $arg["data"]["TOTAL_SCORE"] = $totalScore;
    $arg["data"]["TOTAL_CNT"] = $totalCnt;
    if ($totalCnt > 0) {
        $totalAvg = round(($totalScore / $totalCnt) * 10) / 10;
    }
    $arg["data"]["AVG_SCORE"] = $totalAvg;
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //保存ボタン
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "保 存", " onclick=\"return btn_submit('update');\"");
    //取消ボタン
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"");
    //印刷
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
    //終了ボタン
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//CSV設定
function getHead()
{
    //CSVヘッダ名
    $header = array("0"  => "学期コード",
                    "1"  => "対象年組",
                    "2"  => "対象年組名",
                    "3"  => "データ種別",
                    "4"  => "テスト名称",
                    "5"  => "科目コード",
                    "6"  => "学籍番号",
                    "7"  => "出席番号",
                    "8"  => "氏名",
                    "9"  => "得点",
                    "10" => "席次",
                    "11" => "偏差値",
                    "12" => $model->lastColumn);
    return $header;
}

//CSV設定
function setCsv(&$objForm, &$arg, &$objUp, $hrName, $testName, $subclassName, $header)
{
    $objUp->setHeader(array_values($header));

    //CSV出力ファイル名
    $objUp->setFileName(CTRL_YEAR."年度_".$hrName."_".$testName."_実力テスト(".$subclassName.").csv");

    //ゼロ埋めフラグ
    $flg = array("対象年組"   => array(true, 5),
                 "科目コード" => array(true, 6),
                 "学籍番号"   => array(true, 8));

    $objUp->setEmbed_flg($flg);

    $objUp->setType(array(9=>'S'));
    $objUp->setSize(array(9=>3));
}

//CSV値設定
function setCsvValue(&$objUp, $model, $headerData, $hrName, $testName, $row)
{
    //キー値をセット
    $key = array("学期コード" => $model->field["SEMESTER"],
                 "対象年組"   => $model->field["GRADE_HR_CLASS"],
                 "データ種別" => $model->field["PROFICIENCYDIV"],
                 "テスト名称" => $testName,
                 "科目コード" => $model->field["PROFICIENCY_SUBCLASS_CD"],
                 "学籍番号"   => $row["SCHREGNO"]);

    $csv = array($model->field["SEMESTER"],
                 $model->field["GRADE_HR_CLASS"],
                 $hrName,
                 $model->field["PROFICIENCYDIV"],
                 $testName,
                 $model->field["PROFICIENCY_SUBCLASS_CD"]);

    $header = array("SCORE");
    $headerKey = array("SCORE" => 9);
    foreach ($row as $rowKey => $val) {
        if ($rowKey == "SCORE_DI") {
            continue;
        }
        if ($rowKey == "COURSE") {
            continue;
        }
        if ($rowKey == "ATTENDNO") {
            continue;
        }
        if ($rowKey == "NAME_SHOW") {
            continue;
        }
        if ($rowKey == "GRADE") {
            continue;
        }
        if ($rowKey == "GROUP_CD") {
            continue;
        }
        $csv[] = $val;
        if (in_array($rowKey, $header)) {
            //入力エリアとキーをセットする
            $objUp->setElementsValue($rowKey."[]", $headerData[$headerKey[$rowKey]], $key);
        }
    }
    $csv[] = $model->lastColumn;
    $objUp->addCsvValue($csv);
}
