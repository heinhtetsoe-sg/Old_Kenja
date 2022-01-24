<?php

require_once('for_php7.php');

class knjz406Form1
{
    function main(&$model)
    {
        /* フォーム作成 */
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz406index.php", "", "main");

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
        /* データ区分 1:学校用 2:担任・教科担当用 */
        $query = knjz406Query::selectDataDiv($model);
        $extra = "onChange=\"btn_submit('changeDataDiv')\";" .$setupFlgCheck;
        makeCmb($objForm, $arg, $db, $query, $model->field["DATA_DIV"], "DATA_DIV", $extra, 1, "");
        /* 学年 or 年組 */
        $arg["GRADE_HR_NAME"] = ($model->field["DATA_DIV"] == "1") ? "学年" : "年組";
        $query = knjz406Query::selectGradeHrClass($model);
        $extra = "onChange=\"btn_submit('changeGradeHrClass')\";" .$setupFlgCheck;
        $retFirstVal = makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "BLANK");

        /* 教科名 */
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);
        if (!$grade) {
            list($grade, $hrClass) = explode("-", $retFirstVal);
        }

        $query = knjz406Query::selectSchoolKind($grade);
        $schooLkind = $db->getOne($query);
        if ($schooLkind == "H") {
            $arg["SUBCLASS_TITLE"] = "科目名";
        } else {
            $arg["SUBCLASS_TITLE"] = "教科名";
        }
        $query = knjz406Query::selectSubclasscd($model);
        $extra = "onChange=\"btn_submit('changeSubclasscd')\";" .$setupFlgCheck;
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        /****************/
        /* 表示(ヘッダ) */
        /****************/
        /* 担任名 */
        $query = knjz406Query::getStaffName($model);
        $staffRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["STAFF_NAME"] = $staffRow["STAFFNAME"];

        //コンボ選択したか？
        $isSelectCmb = strlen($model->field["DATA_DIV"]) && strlen($model->field["GRADE_HR_CLASS"]) && strlen($model->field["SUBCLASSCD"]);

        //配列(保持)をクリア
        $model->keepUnitDatArray = array();

        /* 編集対象データリスト */
        if ($isSelectCmb) {
            makeDataList($objForm, $arg, $db, $model);
        }

        //配列(更新)をクリア
        $model->updUnitDatArray = array();

        /* ボタン作成 */
        makeButton($objForm, $arg, $isSelectCmb, $model, $db);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        /* hidden要素作成 */
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "setupFlg", $model->setupFlg); //編集フラグ 1:編集中
        $arg["finish"]  = $objForm->get_finish();
        /* テンプレート呼び出し */
        View::toHTML($model, "knjz406Form1.html", $arg);
    }
}

//編集対象データリスト作成
function makeDataList(&$objForm, &$arg, $db, &$model) {
    //初期化
    $setData = array();
    $dataCnt = 0;
    $colorFlg = false;

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
        $query = knjz406Query::getListUnitDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //初期値：単元（学校用）を読込
            if ($model->cmd == "def" && $model->field["DATA_DIV"] == "2") {
                $row["SEQ"] = "";
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
    $setRow = array();

    //color
    if ($dataCnt % 5 == 0) {
        $colorFlg = !$colorFlg;
    }

    //checkbox
    $extra  = "onClick=\"rankClick(this)\"";
    $extra .= strlen($row["RANK"]) ? " checked " : "";
    $extra .= strlen($row["UNIT_TEST_DATE"]) ? " disabled" : "";
    $setRow["RANK"] = knjCreateCheckBox($objForm, "RANK".$dataCnt, $dataCnt, $extra);

    //textbox
    $extra  = " onChange=\"setupFlgOn();\" ";//編集中フラグON
    $extra .= strlen($row["UNIT_TEST_DATE"]) ? " style=\"width:95%; background-color:#cccccc;\" readOnly" : " style=\"width:95%\"";
    $setRow["UNIT_L_NAME"] = knjCreateTextBox($objForm, $row["UNIT_L_NAME"], "UNIT_L_NAME".$dataCnt, 60, 90, $extra);

    //学期、テスト実施日・・・表示のみ
    $setRow["SEMESTER"] = $model->control_data["学期名"][$row["SEMESTER"]];
    $setRow["UNIT_TEST_DATE"] = str_replace("-", "/", $row["UNIT_TEST_DATE"]);
//    $model->field["UNIT_TEST_DATE"] = str_replace("-", "/", $model->field["UNIT_TEST_DATE"]);
    $setRow["UNIT_TEST_DATE"] = View::popUpCalendar($objForm, "UNIT_TEST_DATE".$dataCnt, $setRow["UNIT_TEST_DATE"]);

    //bgcolor
    $setRow["BGCOLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

    return $setRow;
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $kyoukaTantouList = array();        //教科担当の担当している年組を保持
    $nenkumiFlg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if (isset($row["KYOUKA_TANTOU_FLG"]) && $row["KYOUKA_TANTOU_FLG"] == "1") {
            $nenkumiFlg = true;
            $kyoukaTantouList[] =    $row["VALUE"];
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    if ($nenkumiFlg) {
        knjCreateHidden($objForm, "kyoukaTantouList", implode(",", $kyoukaTantouList));
    }
    $result->free();

    //教科名、科目名の切り替えで使用する。
    return $opt[1]["value"];
}

//ボタン作成
function makeButton(&$objForm, &$arg, $isSelectCmb, $model, $db) {
    $disBtn = $isSelectCmb ? "" : " disabled";
    $disBtnDef = (!$isSelectCmb) ? " disabled" : "";
    //初期値・・・データ区分が担任用の時、ボタンを表示する。
    if ($model->field["DATA_DIV"] == "2") {
        $arg["btn_def"] = knjCreateBtn($objForm, "btn_def", "初期値", "onclick=\"btn_submit('def');\"".$disBtnDef);
        //テスト単元観点別評価・配点設定があるか
        if ($isSelectCmb) {
            $inputseqDataCnt = $db->getOne(knjz406Query::getInputseqDataCnt($model));
            knjCreateHidden($objForm, "INPUTSEQ_DATA_CNT", $inputseqDataCnt);
        }
    }
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
