<?php

require_once('for_php7.php');

class knjd655cForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd655cindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();
        //処理年度
        $arg["YEAR"] = CTRL_YEAR;
        //入力可能な文字数
        $arg["MOJI"] = $model->moji;

        /**********/
        /* コンボ */
        /**********/
        //学期
        $query = knjd655cQuery::getSemesterList();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末は今学期とする
        $semester = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];
        //テスト種別
        $query = knjd655cQuery::getTestList($model);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], $extra, 1, 1);
        //年組
        $query = knjd655cQuery::getGradeHrclass($semester);
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, 1);
        //区分
        $opt = array();
        $opt[] = array('label' => '成績の完備しない生徒の理由',                   'value' => '5');
        $opt[] = array('label' => '指導全般にわたって注意を必要とする生徒の理由', 'value' => '6');
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('main');\"";
        $arg["RIYUU"] = knjCreateCombo($objForm, "RIYUU", $model->field["RIYUU"], $opt, $extra, 1);


        /********************/
        /* チェックボックス */
        /********************/
        //全て選択
        if ($model->field["CHECK_ALL"] == "1") {
            $extra = " checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " onclick='check_all();' ";
        $arg["CHECK_ALL"] = knjCreateCheckBox($objForm, "CHECK_ALL", "1", $extra);

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $colorFlg = false;
        $result = $db->query(knjd655cQuery::selectQuery($model, $semester));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //出席番号
            if($row["ATTENDNO"] != ""){
                $row["ATTENDNO"] = sprintf("%01d", $row["ATTENDNO"]).'番';
            }

            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }

            //チェックボックス
            $extra = "";
            if (isset($model->warning)) {
                if ($model->field["CHECK"][$counter] == "1") {
                    $extra = "checked='checked' ";
                }
            }
            $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK-{$counter}", "1", $extra);

            //備考
            $value = (!isset($model->warning)) ? $row["REMARK"] : $model->fields["REMARK"][$counter];
            $row["REMARK"] = knjCreateTextBox($objForm, $value, "REMARK-{$counter}", $model->moji * 2, $model->moji, " onPaste=\"return showPaste(this);\"");

            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            $counter++;
            $arg["data"][] = $row;
        }

        /**********/
        /* ボタン */
        /**********/
        //更新
        $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //データCSV
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX655CCSV/knjx655ccsvindex.php?','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "moji", $model->moji);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd655cForm1.html", $arg);
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
?>
