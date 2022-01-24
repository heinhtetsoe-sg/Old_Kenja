<?php

require_once('for_php7.php');

class knjs010Form1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjs010index.php", "", "main");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* 処理年度 */
        $arg["YEAR"] = CTRL_YEAR;

        /* 処理学期 */
        $arg["SEMESTER"] = $model->control_data["学期名"][CTRL_SEMESTER];

        /**********/
        /* コンボ */
        /**********/
        $setupFlgCheck = " onclick=\"return setupFlgCheck();\" ";
        /* データ区分 1:学校用 2:担任用 */
        $query = knjs010Query::selectDataDiv($model);
        $extra = "onChange=\"btn_submit('changeDataDiv')\";" .$setupFlgCheck;
        makeCmb($objForm, $arg, $db, $query, $model->field["DATA_DIV"], "DATA_DIV", $extra, 1, "");
        /* 学年 or 年組 */
        $arg["GRADE_HR_NAME"] = ($model->field["DATA_DIV"] == "1") ? "学年" : "年組";
        $query = knjs010Query::selectGradeHrClass($model);
        $extra = "onChange=\"btn_submit('changeGradeHrClass')\";" .$setupFlgCheck;
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "BLANK");
        /* 教科名 */
        $query = knjs010Query::selectSubclasscd($model);
        $extra = "onChange=\"btn_submit('changeSubclasscd')\";" .$setupFlgCheck;
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        /****************/
        /* 表示(ヘッダ) */
        /****************/
        /* 担任名 */
        $query = knjs010Query::getStaffName($model);
        $staffRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["STAFF_NAME"] = $staffRow["STAFFNAME"];
        /* 出版社 */
        $query = knjs010Query::getIssuecompanyName($model);
        $issueRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["ISSUECOMPANY_NAME"] = $issueRow["ISSUECOMPANYABBV"];
        $model->field["ISSUECOMPANYCD"] = $issueRow["ISSUECOMPANYCD"];

        //コンボ選択したか？
        $isSelectCmb = strlen($model->field["DATA_DIV"]) && strlen($model->field["GRADE_HR_CLASS"]) && strlen($model->field["SUBCLASSCD"]);

        //配列(学期と月)をクリア
        $model->semeMonArray = array();

        //配列(保持)をクリア
        $model->keepUnitDatArray = array();

        /* 編集対象データリスト */
        if ($isSelectCmb) {
            makeDataList($objForm, $arg, $db, $model);
        }

        //配列(更新)をクリア
        $model->updUnitDatArray = array();

        /* ボタン作成 */
        makeButton($objForm, $arg, $isSelectCmb, $model);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素作成 */
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "setupFlg", $model->setupFlg); //編集フラグ 1:編集中

        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjs010Form1.html", $arg);
    }
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, &$model) {
    //初期化
    $setData = array();
    $dataCnt = 0;
    $colorFlg = false;

    //DBから取得(学期と月)
    //学期と月・・・データ区分コンボが単元（担任用）の場合のみ表示する。
    if ($model->field["DATA_DIV"] == "2") {
        $query = knjs010Query::getListUnitRankDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rank = $row["RANK"] - 1;
            $model->semeMonArray[$rank]["SEMESTER"] = $row["SEMESTER"];
            $model->semeMonArray[$rank]["MONTH"] = sprintf("%02d", $row["MONTH"]);
        }
        $result->free();
    }

    //編集フラグ 1:編集中
    if ($model->setupFlg == "1") {
        //配列から取得
        foreach ($model->updUnitDatArray as $key => $row) {
            $model->keepUnitDatArray[$dataCnt] = $row;//DB情報保持
            $setData[$dataCnt] = makeTextData($objForm, $model, $row, $dataCnt, $colorFlg, $db);
            $dataCnt++;
        }
    } else {
        //DBから取得
        $query = knjs010Query::getListUnitDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //KNJS030からコールされた時、渡されたパラメータ(単元)をチェックありとする。
            if (strlen($model->sendSeq) && $row["SEQ"] == $model->sendSeq) {
                $row["RANK"] = $dataCnt;
                $row["RANK_JUMP"] = "<a name=\"Target\"></a>";
                $subdata = REQUESTROOT."/S/KNJS010/knjs010index.php?&cmd=main&SEND_PRGID=KNJS030&SEND_GRADE_HR={$model->field["GRADE_HR_CLASS"]}&SEND_SUBCLASSCD={$model->field["SUBCLASSCD"]}&SEND_DATADIV={$model->field["DATA_DIV"]}&SEND_SEQ={$model->sendSeq}&SEND_AUTH={$model->auth}";
                $arg["jumping"] = "rankJump('{$subdata}#Target');";
            }
            $model->keepUnitDatArray[$dataCnt] = $row;//DB情報保持
            $setData[$dataCnt] = makeTextData($objForm, $model, $row, $dataCnt, $colorFlg, $db);
            $dataCnt++;
        }
        $result->free();
    }

    //セット
    $arg["data"] = $setData;
}

//編集可能データの作成
function makeTextData(&$objForm, $model, $row, $dataCnt, &$colorFlg, $db) {
    //学期と月・・・データ区分コンボが単元（担任用）の場合のみ表示する。
    if ($model->field["DATA_DIV"] == "2") {
        $row["SEMESTER"] = $model->semeMonArray[$dataCnt]["SEMESTER"];
        $row["MONTH"] = $model->semeMonArray[$dataCnt]["MONTH"];
    }
    //color
    if ($dataCnt % 5 == 0) {
        $colorFlg = !$colorFlg;
    }
    //checkbox
    $extra  = "onClick=\"rankClick(this)\"";
    $extra .= strlen($row["RANK"]) ? " checked " : "";
    $row["RANK"] = knjCreateCheckBox($objForm, "RANK".$dataCnt, $dataCnt, $extra);

    $setArray = array("ALLOTMENT_MONTH"   => array("SIZE" =>  2, "MAXLEN" =>  2),
                      "L_TITOL"           => array("SIZE" =>  2, "MAXLEN" =>  2),
                      "UNIT_L_NAME"       => array("SIZE" => 18, "MAXLEN" => 20),
                      "UNIT_M_NAME"       => array("SIZE" => 20, "MAXLEN" => 20),
                      "UNIT_S_NAME"       => array("SIZE" => 20, "MAXLEN" => 20),
                      "UNIT_DATA"         => array("SIZE" => 15, "MAXLEN" => 20),
                      "ALLOTMENT_TIME"    => array("SIZE" =>  2, "MAXLEN" =>  2),
                      "UNIT_DIV"          => array("SIZE" =>  2, "MAXLEN" =>  1)
                      );
    foreach ($setArray as $key => $val) {
        //textbox
        $extra = "";
        if (in_array($key,array("ALLOTMENT_MONTH","L_TITOL","ALLOTMENT_TIME","UNIT_DIV"))) {
            //数値チェック
            $extra .= " style=\"text-align: center\"; onblur=\"this.value=toInteger(this.value)\"; ";
        }
        if (in_array($key,array("L_TITOL"))) {
            $extra .= " style=\"width:15%\" ";
        } else if (in_array($key,array("UNIT_L_NAME"))) {
            $extra .= " style=\"width:80%\" ";
        } else if (in_array($key,array("UNIT_DIV"))) {

        } else {
            $extra .= " style=\"width:95%\" ";
        }
        //編集中フラグON
        $extra .= " onChange=\"setupFlgOn();\" ";
        $row[$key] = knjCreateTextBox($objForm, $row[$key], $key.$dataCnt, $val["SIZE"], $val["MAXLEN"], $extra);
    }
    //bgcolor
    $row["BGCOLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
    //KNJS030からコールされた時、コールされた単元に背景色を付ける。保存するまで色は消えない。
    if (strlen($model->sendSeqKeep) && $row["SEQ"] == $model->sendSeqKeep) {
        $row["BGCOLOR"] = "#ccffcc"; //うすい緑
    }

    return $row;
}

//makeCmb
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
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, $isSelectCmb, $model) {
    $disBtn = $isSelectCmb ? "" : " disabled";
    $disBtnDef = (!$isSelectCmb || $model->getPrgId) ? " disabled" : "";
    //初期値
    $arg["btn_def"] = knjCreateBtn($objForm, "btn_def", "初期値", "onclick=\"btn_submit('def');\"".$disBtnDef);
    /********/
    /* 編集 */
    /********/
    //追加・・・最終行に空白行を追加。
    $arg["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", "onclick=\"btn_submit('add');\"".$disBtn);
    //挿入・・・指定行の上に空白行を追加。
    $arg["btn_ins"] = knjCreateBtn($objForm, "btn_ins", "挿 入", "onclick=\"btn_submit('ins');\"".$disBtn);
    //削除・・・指定行を削除。
    $arg["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", "onclick=\"btn_submit('del');\"".$disBtn);
    //延長・・・指定行の下に同一行を追加。つまり、指定行のコピー。
    $arg["btn_extend"] = knjCreateBtn($objForm, "btn_extend", "延 長", "onclick=\"btn_submit('extend');\"".$disBtn);
    //上へ移動・・・指定行を１行上へ移動。
    $arg["btn_moveUp"] = knjCreateBtn($objForm, "btn_moveUp", "↑移動", "onclick=\"btn_submit('moveUp');\"".$disBtn);
    //下へ移動・・・指定行を１行下へ移動。
    $arg["btn_moveDown"] = knjCreateBtn($objForm, "btn_moveDown", "↓移動", "onclick=\"btn_submit('moveDown');\"".$disBtn);
    /********/
    /********/
    /********/
    //保存
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "保 存", " onclick=\"return btn_submit('update');\"".$disBtn);
    //取消
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"btn_submit('reset');\"".$disBtn);
    //終了
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"return btnEnd();\"");
}
?>
