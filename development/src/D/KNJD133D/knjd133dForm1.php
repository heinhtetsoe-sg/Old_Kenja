<?php

require_once('for_php7.php');
class knjd133dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ
        knjCreateHidden($objForm, "KNJD133D_semesCombo", $model->Properties["KNJD133D_semesCombo"]);
        $query = knjd133dQuery::getSemesterCmb();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, "");

        //校種コンボ
        $query = knjd133dQuery::getSchoolKind($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1, "");

        //名称マスタ D校種08チェック
        $model->che_school_kind = "D".$model->field["SCHOOL_KIND"]."08";
        $model->count = $db->getone(knjd133dquery::getNameMstche($model));

        //科目コンボ
        $query = knjd133dQuery::selectSubclassQuery($model);
        $extra = "onchange=\"return btn_submit('subclasscd');\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "BLANK");

        //講座コンボ
        $query = knjd133dQuery::selectChairQuery($model);
        $extra = "onchange=\"return btn_submit('chaircd');\"";
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->field["CHAIRCD"], $extra, 1, "BLANK");

        if ($model->field["SUBCLASSCD"] != "" && $model->field["CHAIRCD"] != "") {
            //定型文のDATA_DIVを取得
            $model->datadiv = $db->getOne(knjd133dQuery::getNameA042($model));
        }

        $teikeiGradeArray = array();
        $query = knjd133dQuery::getTeikeiData($model, $model->datadiv);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $teikeiGradeArray[$row["GRADE"]][] = $row["PATTERN_CD"]."-".str_replace("\r\n", "<br>", $row["REMARK"]);
        }
        foreach ($teikeiGradeArray as $grade => $patternRemarkArray) {
            knjCreateHidden($objForm, "TEIKEI_GRADE_".$grade, implode(",", $patternRemarkArray));
        }

        //学期開始日、終了日
        $seme = $db->getRow(knjd133dQuery::getSemester($model), DB_FETCHMODE_ASSOC);
        //学籍処理日が学期範囲外の場合、学期終了日を使用する。
        if ($seme["SDATE"] <= CTRL_DATE && CTRL_DATE <= $seme["EDATE"]) {
            $execute_date = CTRL_DATE;  //初期値
        } else {
            $execute_date = $seme["EDATE"];     //初期値
        }

        //学期コース毎定型文取得
        $htrainremarkArray = array();
        $query = knjd133dQuery::getHtrainremarkTempSemesCourseDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key  = $row["GRADE"]."-".$row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"];
            $key .= "-".$row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
            $htrainremarkArray[] = $key;
        }

        //コメントタイトル
        $moji = $model->moji;
        $gyou = $model->gyou;
        $arg["COMMENT_TITLE"] = $model->commentTitle;
        $arg["MOJI_SIZE"] = "(全角{$moji}文字×{$gyou}行まで)";

        //初期化
        $model->data = array();
        $counter = 0;
        //一覧表示
        $colorFlg = false;
        $query = knjd133dQuery::selectQuery($model, $execute_date);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //クラス-出席番号
            $row["ATTENDNO"] = $row["HR_NAME"]."-".$row["ATTENDNO"];

            //評定の入力 可/不可 設定
            $disabled = "";
            if (substr($model->field["SUBCLASSCD"], 0, 2) == "90") {
                $key  = $row["GRADE"]."-".$row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"]."-".$model->field["SUBCLASSCD"];
                if (!in_array($key, $htrainremarkArray)) {
                    $disabled = " disabled ";
                }
            }
            //定型文コード(評定)
            $value = (!isset($model->warning) && $model->cmd != "edit") ? $row["PATTERN_CD"] : $model->fields["PATTERN_CD_".$row["SCHREGNO"]];
            $extra = " onkeydown=\"moveEnter(this, '{$counter}')\"; onblur=\"return chkTorikomi('{$row["SCHREGNO"]}', '{$row["GRADE"]}');\"";
            $row["PATTERN_CD"] = knjCreateTextBox($objForm, $value, "PATTERN_CD_".$row["SCHREGNO"], 3, 3, $extra.$disabled);

            //定型文
            $value = (!isset($model->warning) && $model->cmd != "edit") ? $row["REMARK"] : $model->fields["REMARK_".$row["SCHREGNO"]];
            $extra = "";
            $row["REMARK"] = "<span id=\"REMARK_".$row["SCHREGNO"]."\">".$row["REMARK"]."</span>";

            knjCreateHidden($objForm, "REMARK_".$row["SCHREGNO"], $row["REMARK"]);
            knjCreateHidden($objForm, "ATTENDO_NAME_".$row["SCHREGNO"], $row["ATTENDNO"]); //エラーメッセージ用
            knjCreateHidden($objForm, "COUNTER_".$counter, "PATTERN_CD_".$row["SCHREGNO"]); //Enter移動用(行番号とテキストフィールド名の対応)

            //背景色
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }
        knjCreateHidden($objForm, "COUNTER_TOTAL", $counter);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "itemMstJson", $model->itemMstJson); 

        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd133dindex.php", "", "main");
        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd133dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {

    //更新ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->datadiv != "") ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = ($model->datadiv != "") ? "onclick=\"return btn_submit('reset');\"" : "disabled";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
