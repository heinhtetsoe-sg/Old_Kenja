<?php

require_once('for_php7.php');

class knjd129iForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd129iindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd129iQuery::getSemester();
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        
        //項目名
        $arg["SCORE_TITLE"] = $db->getOne(knjd129iQuery::getSemester($model->field["SEMESTER"]));
        
        //学年コンボ
        $extra = "onChange=\"return btn_submit('main');\"";
        $query = knjd129iQuery::getGrade();
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //学校校種を取得
        $model->schoolkind = $db->getOne(knjd129iQuery::getSchoolKind($model));

        //コースグループコンボ
        $extra = "onChange=\"return btn_submit('main');\"";
        $query = knjd129iQuery::getCourseGroup($model);
        makeCmb($objForm, $arg, $db, $query, "GROUP_CD", $model->field["GROUP_CD"], $extra, 1);

        //科目
        $query = knjd129iQuery::getSubclass($model);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1, "blank");

        //単位認定ライン
        $lineScoreDiv = "IKA";
        $getLineScore = $db->getOne(knjd129iQuery::getAssessLevelSdivMstAssessLow($model));
        if ($getLineScore == '') {
            $lineScoreDiv = "MIMAN";
            $getLineScore = $db->getOne(knjd129iQuery::getPassScoreLine($model));
        }
        if (!$getLineScore) {
            $getLineScore = 30;
            $arg["LINE_SCORE"] = '<B>'.$getLineScore.'点</B>(デフォルト値)';
        } else {
            $arg["LINE_SCORE"] = '<B>'.$getLineScore.'点</B>';
        }
        knjCreateHidden($objForm, "GET_LINE_SCORE", $getLineScore);

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $query = knjd129iQuery::selectQuery($model, $getLineScore, $lineScoreDiv);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //フィールド名+カウントをセット
            $setName = "SCORE_PASS-".$counter;
        
            //学籍番号をHiddenで保持
            knjCreateHidden($objForm, "SCHREGNO"."-".$counter, $row["SCHREGNO"]);

            //出席番号
            if ($row["ATTENDNO"] != ""){
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
            
            //得点をhiddenで保持
            knjCreateHidden($objForm, "SCORE-".$counter, $row["SCORE"]);
            
            //フィールド名+カウントをセット
            $setName = "SCORE_PASS-".$counter;

            //補充点
            $opt = array();
            $opt[] = array('label' => "", 'value' => "");
            $extra = " onChange=\"valueCheck(this.value, '".$setName."');\"";
            $query = knjd129iQuery::getScorePass();
            $result2 = $db->query($query);
            $value = (!isset($model->warning)) ? $row["SCORE_PASS_FLG"] : $model->fields["SCORE_PASS_FLG"][$counter];
            while ($getRow = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $getRow["LABEL"],
                               'value' => $getRow["VALUE"]);
            }
            $result2->free();
            $row["SCORE_PASS_FLG"] = knjCreateCombo($objForm, "SCORE_PASS_FLG-".$counter, $value, $opt, $extra, $size);
            
            //ライン得点をhiddenで保持
            knjCreateHidden($objForm, "SCORE_PASS-".$counter, $row["SCORE_PASS"]);
            
            $row["SCORE_PASS_ID"] = "SCORE_PASS_ID"."_".$counter;

            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";
            $counter++;
            $arg["data"][] = $row;
        }
        $result->free();
        
        knjCreateHidden($objForm, "COUNTESR_SUM", $counter);
        //件数
        knjCreateHidden($objForm, "COUNTER", $counter);

        
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJD129I");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd129iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタンを作成する
    $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
