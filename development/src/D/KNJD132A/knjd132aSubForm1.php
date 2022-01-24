<?php

require_once('for_php7.php');

class knjd132aSubForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjd132aindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        if ($model->Properties["KNJD133J_semesCombo"] == "1") {
            $arg["SEME_NAME"]  = $db->getOne(knjd132aQuery::getSemesterCmb($model->exp_semester));
            $arg["NO_SEME"]    = "";
        } else {
            $arg["SEME_NAME"]  = "";
            $arg["NO_SEME"]    = "1";
        }
        if ($model->cmd == "teikei") {
            $arg["TITLE"] = ($model->isChiyoda) ? "観点" : "学習内容";
            $teikeiCmd = "teikei";
            knjCreateHidden($objForm, "target", "TOTALSTUDYACT");
        } else if ($model->cmd == "teikei2") {
            $arg["TITLE"] = "評価";
            $teikeiCmd = "teikei2";
            knjCreateHidden($objForm, "target", "TOTALSTUDYTIME");
        } else if ($model->cmd == "teikei3") {
            $arg["TITLE"] = $model->remark1Name;
            $teikeiCmd = "teikei3";
            knjCreateHidden($objForm, "target", "REMARK1");
        }
        $model->semester = $db->getOne(knjd132aQuery::getSemesterCmb($model->exp_semester));

        //学年
        $model->grade = $db->getOne(knjd132aQuery::getGrade($model, $model->exp_semester));
        //学年名取得
        $arg["GRADE"] = $db->getOne(knjd132aQuery::getGradeName($model, $model->grade));
        knjCreateHidden($objForm, "GRADE", $model->grade);

        //リスト作成
        $datacnt = makeList($objForm, $arg, $db, $model);

        if (VARS::get("REPLACE_FLG") != "") {
            $replaceFlg = VARS::get("REPLACE_FLG");
        } else {
            $replaceFlg = "";
        }

        //ボタン作成
        makeBtn($objForm, $arg, $datacnt, $replaceFlg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd132aSubForm1.html", $arg); 
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
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {
    $datacnt = 0;
    $query = knjd132aQuery::getHtrainRemarkTempDat($model);
    var_dump($query);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        //選択チェックボックス
        $check = "";
        $objForm->ae(array("type"       => "checkbox",
                           "name"       => "CHECK",
                           "value"      => $row["REMARK"],
                           "extrahtml"  => $check,
                           "multiple"   => "1" ));
        $row["CHECK"] = $objForm->ge("CHECK");
        $arg["data"][] = $row;
        $datacnt++;
    }
    $result->free();
    return $datacnt;
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $datacnt, $replaceFlg) {
    //選択ボタン
    $extra = "onclick=\"return btn_submit2('".$datacnt."', '".$replaceFlg."')\"";
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
?>
