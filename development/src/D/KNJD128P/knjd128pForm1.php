<?php

require_once('for_php7.php');

class knjd128pForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ
        knjCreateHidden($objForm, "KNJD128P_semesCombo", $model->Properties["KNJD128P_semesCombo"]);
        $query = knjd128pQuery::getSemesterCmb();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, "");

        //名称マスタ D校種08チェック
        $model->che_school_kind = "D".$model->field["SCHOOL_KIND"]."08";
        $model->count = $db->getone(knjd128pquery::getNameMstche($model));

        //科目コンボ
        $query = knjd128pQuery::selectSubclassQuery($model);
        $extra = "onchange=\"return btn_submit('subclasscd');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "BLANK");

        //講座コンボ
        $query = knjd128pQuery::selectChairQuery($model);
        $extra = "onchange=\"return btn_submit('chaircd');\"";
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "BLANK");

        //エンター押下時の移動方向
        $opt = array(1, 2);
        $model->field["MOVE_ENTER"] = ($model->field["MOVE_ENTER"] == "") ? "1" : $model->field["MOVE_ENTER"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"MOVE_ENTER{$val}\" onchange=\"return btn_submit('chgMoveEnter');\" ");
        }
        $radioArray = knjCreateRadio($objForm, "MOVE_ENTER", $model->field["MOVE_ENTER"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //学期開始日、終了日
        $seme = $db->getRow(knjd128pQuery::getSemester($model), DB_FETCHMODE_ASSOC);
        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        if ($seme["SDATE"] <= CTRL_DATE && CTRL_DATE <= $seme["EDATE"]) {
            $execute_date = CTRL_DATE;  //初期値
        } else {
            $execute_date = $seme["EDATE"];     //初期値
        }

        //コメントタイトル
        $moji = $model->moji;
        $gyou = $model->gyou;
        $arg["COMMENT_TITLE"] = $model->commentTitle;
        $arg["MOJI_SIZE"] = "(全角{$moji}文字×{$gyou}行まで)";

        //学籍番号・学期毎のデータを学籍番号でグループ化
        $mainData = array();
        $query = knjd128pQuery::selectQuery($model, $execute_date);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!isset($mainData[$row["SCHREGNO"]])) {
                //生徒情報
                $mainData[$row["SCHREGNO"]] = array("SCHREGNO"   => $row["SCHREGNO"],
                                                    "GRADE"      => $row["GRADE"],
                                                    "ATTENDNO"   => $row["HR_NAME"]."-".$row["ATTENDNO"],
                                                    "NAME_SHOW"  => $row["NAME_SHOW"]
                                                    );
            }

            //生徒・学期毎の成績情報
            $mainData[$row["SCHREGNO"]]["SEMES".$row["SEMESTER"]] = array("OUTPUTS_SCORE"   => $row["OUTPUTS_SCORE"],
                                                                          "SKILLS_SCORE"    => $row["SKILLS_SCORE"],
                                                                          "TOTAL_SCORE"     => $row["TOTAL_SCORE"]
                                                                          );
        }

        //管理者コントロール設定取得
        $model->adminControlFlg = array();
        $query = knjd128pQuery::getAdminControl($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->adminControlFlg[$row["SEMESTER"]] = $row["CONTROL_FLG"];
        }

        //初期化
        $model->data = array();
        $counter = 0;
        $moveEnterArray = array();
        //一覧表示
        $colorFlg = false;
        foreach ($mainData as $schregno => $rowData) {
            $model->data["SCHREGNO"][] = $schregno;

            for ($semester = 1; $semester <= 3; $semester++) {
                $semesScore = isset($rowData["SEMES".$semester]) ? $rowData["SEMES".$semester] : array("OUTPUTS_SCORE" => "", "SKILLS_SCORE" => "", "TOTAL_SCORE" => "");
                $outputs  = $semesScore["OUTPUTS_SCORE"];
                $skills   = $semesScore["SKILLS_SCORE"];
                $total    = $semesScore["TOTAL_SCORE"];

                if ($model->adminControlFlg[$semester] == "1") { //入力可能
                    //フィールド名
                    $outputFieldName = "OUTPUTS_SCORE".$semester."_".$schregno;
                    $skillFieldName = "SKILLS_SCORE".$semester."_".$schregno;

                    //Enter押し時の移動先フィールド名を保持
                    $moveEnterArray[] = array("ROW" => $counter, "COL" => 2 * ($semester - 1), "FIELD_NAME" => $outputFieldName);
                    $moveEnterArray[] = array("ROW" => $counter, "COL" => 2 * ($semester - 1) + 1, "FIELD_NAME" => $skillFieldName);

                    //Outputs
                    $value = (!isset($model->warning) && $model->cmd != "chgMoveEnter") ? $outputs : $model->fields[$outputFieldName];
                    $value = ($model->cmd == "csvInputMain" && isset($model->inputFields[$outputFieldName])) ? $model->inputFields[$outputFieldName] : $outputs;
                    $extra = " STYLE=\"text-align: right\" onkeydown=\"moveEnter(this);\" onblur=\"inputScore(this, '{$rowData["GRADE"]}')\"; ";
                    $rowData["OUTPUTS_SCORE".$semester] = knjCreateTextBox($objForm, $value, $outputFieldName, 1, 1, $extra);
                    knjCreateHidden($objForm, "BK_".$outputFieldName, $outputs);
                    //Skills
                    $value = (!isset($model->warning) && $model->cmd != "chgMoveEnter") ? $skills : $model->fields[$skillFieldName];
                    $value = ($model->cmd == "csvInputMain" && isset($model->inputFields[$skillFieldName])) ? $model->inputFields[$skillFieldName] : $skills;
                    $extra = " STYLE=\"text-align: right\" onkeydown=\"moveEnter(this);\" onblur=\"inputScore(this, '{$rowData["GRADE"]}')\"; ";
                    $rowData["SKILLS_SCORE".$semester]  = knjCreateTextBox($objForm, $value, $skillFieldName, 1, 1, $extra);
                    knjCreateHidden($objForm, "BK_".$skillFieldName, $skills);
                    //総括評価
                    $rowData["TOTAL_SCORE".$semester]   = $total;
                } elseif (isset($model->adminControlFlg[$semester])) { //表示のみ
                    $rowData["OUTPUTS_SCORE".$semester] = $outputs;
                    $rowData["SKILLS_SCORE".$semester]  = $skills;
                    //総括評価
                    $rowData["TOTAL_SCORE".$semester]   = $total;
                }
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
            //背景色
            $rowData["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
            knjCreateHidden($objForm, "ATTENDO_NAME_".$rowData["SCHREGNO"], $rowData["ATTENDNO"]); //エラーメッセージ用
            knjCreateHidden($objForm, "SCHREG_GRADE_".$rowData["SCHREGNO"], $rowData["GRADE"]); //エラーメッセージ用

            $counter++;
            $arg["data"][] = $rowData;
        }

        //Enter移動処理用
        $rowArray = array();
        $colArray = array();
        foreach ($moveEnterArray as $fieldInfo) {
            $rowArray[] = $fieldInfo["ROW"];
            $colArray[] = $fieldInfo["COL"];
        }

        if ($model->field["MOVE_ENTER"] == "1") { //縦順
            array_multisort($colArray, SORT_ASC, SORT_NUMERIC, $rowArray, SORT_ASC, SORT_NUMERIC, $moveEnterArray);
        } else { //横順
            array_multisort($rowArray, SORT_ASC, SORT_NUMERIC, $colArray, SORT_ASC, SORT_NUMERIC, $moveEnterArray);
        }
        $totalCnt = 0;
        foreach ($moveEnterArray as $order => $fieldInfo) {
            knjCreateHidden($objForm, "ORDER_".$order, $fieldInfo["FIELD_NAME"]); //「フィールド名 => 移動の順番」取得用
            knjCreateHidden($objForm, "FIELD_ORDER_".$fieldInfo["FIELD_NAME"], $order); //「移動の順番 => フィールド名」取得用
            $totalCnt++;
        }
        knjCreateHidden($objForm, "TOTAL_COUNT", $totalCnt);

        //CSV処理作成
        makeCsv($objForm, $arg, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd128pindex.php", "", "main");
        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd128pForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//CSV処理作成
function makeCsv(&$objForm, &$arg, $model)
{
    //ファイル
    $extra = "";
    $dis = "";
    if ($model->field["CHAIRCD"] == '') {
        $dis = " disabled=\"disabled\" ";
    }
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra.$dis, 1024000);

    //ヘッダ有
    $extra = ($model->field["HEADER"] == "on" || $model->cmd == "main") ? "checked" : "";
    $extra .= " id=\"HEADER\"";
    $arg["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");

    //取込ボタン
    $extra = "onclick=\"return btn_submit('csvInput');\"";
    $arg["btn_input"] = knjCreateBtn($objForm, "btn_input", "CSV取込", $extra.$dis);
    //出力ボタン
    $extra = "onclick=\"return btn_submit('csvOutput');\"";
    $arg["btn_output"] = knjCreateBtn($objForm, "btn_output", "CSV出力", $extra.$dis);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{

    //更新ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
