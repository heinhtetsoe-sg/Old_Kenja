<?php

require_once('for_php7.php');

class knjz380vForm2
{
    function main(&$model)
    {
    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("edit", "POST", "knjz380vindex.php", "", "edit");

    //警告メッセージを表示しない場合
    if (!isset($model->warning) && $model->cmd != "knjz380vForm2")
    {
        $Row = knjz380vQuery::getRow($model);
    } else {
        $Row =& $model->field;
    }

    $db = Query::dbCheckOut();
    
    //学期コンボボックス作成
    $opt = array();
    $opt[] = array("label" => "","value" => "");
    $value_flg = false;
    $result = $db->query(knjz380vQuery::getSemester($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($Row["SEMESTER"] == $row["VALUE"]) $value_flg = true;
    }
    $Row["SEMESTER"] = ($Row["SEMESTER"] && $value_flg) ? $Row["SEMESTER"] : $opt[0]["value"];
    $extra = "onChange=\"btn_submit('knjz380vForm2')\"";
    $arg["data"]["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $Row["SEMESTER"], $opt, $extra, 1);

    //テスト項目種別名コンボボックスの中身を作成------------------------------
    //考査種別（大分類）
    $opt = array();
    $opt[] = array("label" => "","value" => "");
    $value_flg = false;
    $result = $db->query(knjz380vQuery::getTestKindName($model));
    if ($Row["SEMESTER"]) {
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["TESTKINDCD"] == $row["VALUE"]) $value_flg = true;
        }
    }
    $Row["TESTKINDCD"] = ($Row["TESTKINDCD"] && $value_flg) ? $Row["TESTKINDCD"] : $opt[0]["value"];
    $extra = "onChange=\"btn_submit('knjz380vForm2')\"";
    $arg["data"]["TESTKINDCD"] = knjCreateCombo($objForm, "TESTKINDCD", $Row["TESTKINDCD"], $opt, $extra, 1);
    $result->free();

    //考査種別（中分類）
    $opt = array();
    $opt[] = array('label' => $row["LABEL"],
                   'value' => $row["VALUE"]);
    if ($Row["SEMESTER"] != "" && $Row["TESTKINDCD"] != "") {
        if ($Row["TESTKINDCD"] !== '99') {
            $opt[] = array('label' => '01 1回目', 'value' => '01');
            $opt[] = array('label' => '02 2回目', 'value' => '02');
        } else {
            if ($Row["SEMESTER"] === '9') {
                $opt[] = array('label' => '00 学年末', 'value' => '00');
            } else {
                $opt[] = array('label' => '00 学期末', 'value' => '00');
            }
        }
    }
    $Row["TESTITEMCD"] = ($Row["TESTITEMCD"]) ? $Row["TESTITEMCD"] : "";
    $extra = "onChange=\"btn_submit('knjz380vForm2')\"";
    $arg["data"]["TESTITEMCD"] = knjCreateCombo($objForm, "TESTITEMCD", $Row["TESTITEMCD"], $opt, $extra, 1);

    //考査種別（小分類）
    $opt = array();
    $opt[] = array('label' => $row["LABEL"],
                   'value' => $row["VALUE"]);
    $result = $db->query(knjz380vQuery::getScoreDiv($model));
    if ($Row["SEMESTER"] != "" && $Row["TESTKINDCD"] != "" && $Row["TESTITEMCD"] != "") {
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
    }
    $Row["SCORE_DIV"] = ($Row["SCORE_DIV"]) ? $Row["SCORE_DIV"] : "";
    $extra = "";
    $arg["data"]["SCORE_DIV"] = knjCreateCombo($objForm, "SCORE_DIV", $Row["SCORE_DIV"], $opt, $extra, 1);
    $result->free();

    //考査種別名称
    $extra = "";
    $arg["data"]["TESTITEMNAME"] = knjCreateTextBox($objForm, $Row["TESTITEMNAME"], "TESTITEMNAME", 20, 30, $extra);

    //考査種別名称１
    $extra = "";
    $arg["data"]["TESTITEMABBV1"] = knjCreateTextBox($objForm, $Row["TESTITEMABBV1"], "TESTITEMABBV1", 10, 10, $extra);

    //集計フラグ 1:集計する 0:集計しない
    $extra = "id=\"COUNTFLG\"";
    if ($Row["COUNTFLG"] == "1") {
        $extra .= "checked='checked' ";
    } else {
        $extra .= "";
    }
    $arg["data"]["COUNTFLG"] = knjCreateCheckBox($objForm, "COUNTFLG", "1", $extra);

    //出欠集計範囲表示切替
    if($model->Properties["Semester_Detail_Hyouji"] == "1") {
        $arg["sem_detail"] = 1;
    }

    //出欠集計範囲コンボボックス作成
    $opt = array();
    $opt[] = array("label" => "","value" => "");
    $result = $db->query(knjz380vQuery::getSemesterDetail($Row));
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $sdate = str_replace("-","/",$row["SDATE"]);
        $edate = str_replace("-","/",$row["EDATE"]);
        $label = $row["SEMESTERNAME"];
        $value = "{$row["SEMESTER_DETAIL"]},{$sdate}～{$edate}";
        $opt[] = array("label" => "{$row["SEMESTER_DETAIL"]}：{$row["SEMESTERNAME"]}",
                       "value" => $row["SEMESTER_DETAIL"]);
    }
    $result->free();

    //出欠集計範囲
    $objForm->ae( array("type"        => "select",
                        "name"        => "SEMESTER_DETAIL",
                        "value"       => $Row["SEMESTER_DETAIL"],
                        "extrahtml"   => "onChange=\"btn_submit('knjz380vForm2')\"",
                        "options"     => $opt
                        ));
    $arg["data"]["SEMESTER_DETAIL"] = $objForm->ge("SEMESTER_DETAIL");

    $query = knjz380vQuery::getSemesterDetail_sdate_edate($Row);
    $s_e_date = $db->getRow($query, DB_FETCHMODE_ASSOC);
    if (get_count($s_e_date)) {
        $arg["data"]["S_E_DATE"] = str_replace("-", "/", $s_e_date["SDATE"]) . ' ～ ' . str_replace("-", "/", $s_e_date["EDATE"]);
    }

    //指導入力 1:あり 0:なし
    $extra = "id=\"SIDOU_INPUT\"";
    if ($Row["SIDOU_INPUT"] == "1") {
        $extra .= "checked='checked' ";
    } else {
        $extra .=  "";
    }
    $extra .= "onClick=\"btn_submit('knjz380vForm2')\"";
    $arg["data"]["SIDOU_INPUT"] = knjCreateCheckBox($objForm, "SIDOU_INPUT", "1", $extra);
    
    //指導入力ありの場合の記号、得点
    if ($Row["SIDOU_INPUT"] !== "1") {
        $disabled = "disabled";
    } else {
        $disabled = "";
    }
    $opt = array(1, 2);
    $Row["SIDOU_INPUT_INF"] = ($Row["SIDOU_INPUT_INF"] == "") ? "1" : $Row["SIDOU_INPUT_INF"];
    $extra = array("id=\"SIDOU_INPUT_INF1\"".$disabled, "id=\"SIDOU_INPUT_INF2\"".$disabled);
    $radioArray = knjCreateRadio($objForm, "SIDOU_INPUT_INF", $Row["SIDOU_INPUT_INF"], $extra, $opt, get_count($opt));
    foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

    //見込点入力表示切替
    if ($model->Properties["useMikomiFlg"] == "1") {
        $arg["useMikomiFlg"] = 1;
    }
    //見込点入力 1:あり 0:なし
    $extra  = "id=\"MIKOMI_FLG\"";
    $extra .= ($Row["MIKOMI_FLG"] == "1") ? " checked " : "";
    $arg["data"]["MIKOMI_FLG"] = knjCreateCheckBox($objForm, "MIKOMI_FLG", "1", $extra);

    //参考点入力表示切替
    if ($model->Properties["useSankouFlg"] == "1") {
        $arg["useSankouFlg"] = 1;
    }
    //参考点入力 1:あり 0:なし
    $extra  = "id=\"SANKOU_FLG\"";
    $extra .= ($Row["SANKOU_FLG"] == "1") ? " checked " : "";
    $arg["data"]["SANKOU_FLG"] = knjCreateCheckBox($objForm, "SANKOU_FLG", "1", $extra);

    //備考入力表示切替
    if ($model->Properties["useRemarkFlg"] == "1") {
        $arg["useRemarkFlg"] = 1;
    }
    //備考入力 1:あり 0:なし
    $extra  = "id=\"REMARK_FLG\"";
    $extra .= ($Row["REMARK_FLG"] == "1") ? " checked " : "";
    $arg["data"]["REMARK_FLG"] = knjCreateCheckBox($objForm, "REMARK_FLG", "1", $extra);

    //序列対象 1:あり 0:なし
    $extra = "id=\"JYORETSU_FLG\"";
    if ($Row["JYORETSU_FLG"] == "1") {
        $extra .= "checked='checked' ";
    } else {
        $extra .= "";
    }
    $arg["data"]["JYORETSU_FLG"] = knjCreateCheckBox($objForm, "JYORETSU_FLG", "1", $extra);

    //CSV取込不可 1:不可 0:なし
    $extra = "id=\"NOT_USE_CSV_FLG\"";
    if ($Row["NOT_USE_CSV_FLG"] == "1") {
        $extra .= "checked='checked' ";
    } else {
        $extra .= "";
    }
    $arg["data"]["NOT_USE_CSV_FLG"] = knjCreateCheckBox($objForm, "NOT_USE_CSV_FLG", "1", $extra);


    //追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_add",
                        "value"       => "追 加",
                        "extrahtml"   => "onclick=\"return doSubmit('add');\"" ) );

    $arg["button"]["btn_add"] = $objForm->ge("btn_add");

    //修正ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_udpate",
                        "value"       => "更 新",
                        "extrahtml"   => "onclick=\"return doSubmit('update');\"" ) );

    $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

    //削除ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_del",
                        "value"       => "削 除",
                        "extrahtml"   => "onclick=\"return doSubmit('delete');\"" ) );

    $arg["button"]["btn_del"] = $objForm->ge("btn_del");

    //クリアボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_reset",
                        "value"       => "取 消",
                        "extrahtml"   => "onclick=\"return doSubmit('reset')\"" ) );

    $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

    //終了ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_end",
                        "value"       => "終 了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ) );

    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "UPDATED",
                        "value"     => $Row["UPDATED"]
                        ) );

    $arg["finish"]  = $objForm->get_finish();

    if (VARS::get("cmd") != "edit" && $model->cmd != "knjz380vForm2"){
        $arg["reload"]  = "window.open('knjz380vindex.php?cmd=list','left_frame');";
    }

    Query::dbCheckIn($db);
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjz380vForm2.html", $arg);
    }
}
?>
