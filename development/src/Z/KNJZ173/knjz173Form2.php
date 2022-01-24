<?php

require_once('for_php7.php');

class knjz173Form2
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz173index.php", "", "edit");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();


        //リストから選択した場合のみ、データ取得(フラグは、modelでセットしてform.phpの最後でクリア)。
        if ($model->selKaikinCd == "1") {
            //データを取得
            $Row = knjz173Query::getRow($model);
            //読込みタイミングで画面の取得対象学年ラジオはクリア
            $model->field["LE_EXCHGTYPE"] = "";
            unset($model->field["LE_EXCHGTYPE"]);
        } else {
            $Row =& $model->field;
        }
        // 以降、基本的には$Rowを元に処理を行うが、一部例外(SET_PREFATTEND_GRADE, LE_EXCHGTYPEは$model->fieldのみに保持)があるので注意。

        //皆勤コード
        $extra = "id=\"KAIKIN_CD\" onchange=\"paddNumCode();\"";
        $arg["data"]["KAIKIN_CD"] = knjCreateTextBox($objForm, $Row["KAIKIN_CD"], "KAIKIN_CD", 2, 2, $extra);

        //区分
        $opt_kubun = array(1, 2);
        $Row["KAIKIN_DIV"] = ($Row["KAIKIN_DIV"]) ? $Row["KAIKIN_DIV"] : "1";
        $extra = array("id=\"KAIKIN_DIV1\"", "id=\"KAIKIN_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "KAIKIN_DIV", $Row["KAIKIN_DIV"], $extra, $opt_kubun, get_count($opt_kubun));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }
        $model->field["KAIKIN_CD"] = $Row["KAIKIN_CD"];
        
        //皆勤名称
        $extra = "id=\"KAIKIN_NAME\"";
        $arg["data"]["KAIKIN_NAME"] = knjCreateTextBox($objForm, $Row["KAIKIN_NAME"], "KAIKIN_NAME", 30, 15, $extra);

        //参照年数
        $extra = "id=\"REF_YEAR\" onblur=\"chkSuji(this)\" onchange=\"btn_submit('edit');\"";
        $arg["data"]["REF_YEAR"] = knjCreateTextBox($objForm, $Row["REF_YEAR"], "REF_YEAR", 2, 2, $extra);

        $db = Query::dbCheckOut();

        //取得対象学年(SET_PREFATTEND_GRADEは$Rowに保持していないので、注意。)
        $defRadVal = (get_count($db->getCol(knjz173Query::getTargetGrade($Row["KAIKIN_CD"]))) > 0) ? "1" : "2";
        $opt_kubun = array(1, 2);
        $model->field["SET_PREFATTEND_GRADE"] = $model->field["SET_PREFATTEND_GRADE"] ? $model->field["SET_PREFATTEND_GRADE"] : $defRadVal;
        $click = "onclick=\"return btn_submit('edit');\"";
        $extra = array("id=\"SET_PREFATTEND_GRADE1\"".$click, "id=\"SET_PREFATTEND_GRADE2\"".$click);
        $radioArray = knjCreateRadio($objForm, "SET_PREFATTEND_GRADE", $model->field["SET_PREFATTEND_GRADE"], $extra, $opt_kubun, get_count($opt_kubun));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        $grdarry = array();
        $grdarry["A"] = array();
        $grdarry["H"] = array();
        $grdarry["J"] = array();
        $grdarry["P"] = array();
        $grdarry["K"] = array();
        $query = knjz173Query::getGradeInfo($model);
        $result = $db->query($query);
        while ($qrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $grdarry[$qrow["SCHOOL_KIND"]][] = $qrow;
        }
        $schKindArr = array("A", "H", "J", "P", "K");
        foreach ($schKindArr as $schKind) {
            $targetGradeCnt[$schKind] = get_count($grdarry[$schKind]);
        }
        $showShcKindArr = array_keys($grdarry);

        foreach ($showShcKindArr as $key => $schKind) {
            //校種ごとにチェックボックスを作成
            makeGrdCheckBox($objForm, $arg, $model, $db, $schKind, $grdarry, $Row["KAIKIN_CD"]);
        }
        Query::dbCheckIn($db);

        //欠席
        $extra = "id=\"KESSEKI_CONDITION\"";
        $arg["data"]["KESSEKI_CONDITION"] = knjCreateTextBox($objForm, $Row["KESSEKI_CONDITION"], "KESSEKI_CONDITION", 2, 2, $extra);

        //遅刻・早退(LE_EXCHGTYPEは$Rowに保持していないので、注意。)
        $opt = array();
        $opt[] = array("label" => "遅刻・早退を欠席日数に換算する", "value" => "1");
        $opt[] = array("label" => "遅刻・早退それぞれに条件を設定する", "value" => "2");
        $extra = "id=\"LE_EXCHGTYPE\" onchange=\"btn_submit('edit')\"";
        //(読み込んできた時の)チェック対象を先に決定しておく
        $jdgflg = ($Row["KESSEKI_KANSAN"] == "" && $Row["TIKOKU_CONDITION"] == "" && $Row["SOUTAI_CONDITION"] == "") ? "" : ($Row["KESSEKI_KANSAN"] ? "1" : "2");
        //画面設定値($model->field["LE_EXCHGTYPE"]) > 上記チェック結果 > デフォルト値で判定。読込みタイミングなら$model->field["LE_EXCHGTYPE"]は$Row設定タイミングでクリア。
        ////LE_EXCHGTYPEはmodelのみ保持のため、$Rowではない事に注意。
        $model->field["LE_EXCHGTYPE"] = $model->field["LE_EXCHGTYPE"] ? $model->field["LE_EXCHGTYPE"] : ($jdgflg ? $jdgflg : "1");
        $arg["data"]["LE_EXCHGTYPE"] = knjCreateCombo($objForm, "LE_EXCHGTYPE", $model->field["LE_EXCHGTYPE"], $opt, $extra, 1);
        if ($model->field["LE_EXCHGTYPE"] == "1") {
            $arg["TYPE_EXC1"] = "1";
            $arg["TYPE_EXC2"] = "";
            $extra = "onblur=\"chkSuji(this)\"";
            $arg["data"]["KESSEKI_KANSAN"] = knjCreateTextBox($objForm, $Row["KESSEKI_KANSAN"], "KESSEKI_KANSAN", 2, 2, $extra);
        } else {
            $arg["TYPE_EXC1"] = "";
            $arg["TYPE_EXC2"] = "1";
            $extra = "onblur=\"chkSuji(this)\"";
            $arg["data"]["TIKOKU_CONDITION"] = knjCreateTextBox($objForm, $Row["TIKOKU_CONDITION"], "TIKOKU_CONDITION", 2, 2, $extra);
            $arg["data"]["SOUTAI_CONDITION"] = knjCreateTextBox($objForm, $Row["SOUTAI_CONDITION"], "SOUTAI_CONDITION", 2, 2, $extra);
        }

        //欠課時数
        $extra = "onblur=\"chkSuji(this)\"";
        $arg["data"]["KEKKA_JISU_CONDITION"] = knjCreateTextBox($objForm, $Row["KEKKA_JISU_CONDITION"], "KEKKA_JISU_CONDITION", 3, 3, $extra);
        //優先順位
        $extra = "onblur=\"chkSuji(this);\"";
        $arg["data"]["PRIORITY"] = knjCreateTextBox($objForm, $Row["PRIORITY"], "PRIORITY", 2, 2, $extra);
        
        //今年度使用する
        $defChkFlg = ($Row["KAIKIN_FLG"] && $Row["KAIKIN_FLG"] != "0") ? " checked" : "";
        $extra = "id=\"KAIKIN_FLG\" ".$defChkFlg;
        $arg["data"]["KAIKIN_FLG"] = knjCreateCheckBox($objForm, "KAIKIN_FLG", "1", $extra, "");

        //勤怠
        $arg["KNJZ173_DISPPATTERN_1"] = "";
        $arg["KNJZ173_DISPPATTERN_2"] = "";
        $arg["KNJZ173_DISPPATTERN_UNUSED"] = "1";
        $knjz173Disppattern = 0;
        if ($model->Properties["KNJZ173_DISPPATTERN"] == "1") {
            $knjz173Disppattern = 1;
            $arg["KNJZ173_DISPPATTERN_UNUSED"] = "";
            $refYear = ($model->field["REF_YEAR"] == "") ? $Row["REF_YEAR"] : $model->field["REF_YEAR"];
            //参照年数=1の時
            if ($refYear <= "1") {
                $arg["KNJZ173_DISPPATTERN_1"] = "1";
                $arg["KNJZ173_DISPPATTERN_2"] = "";
            //参照年数=2以上の時
            } elseif ($refYear > "1") {
                $arg["KNJZ173_DISPPATTERN_1"] = "";
                $arg["KNJZ173_DISPPATTERN_2"] = "1";
                //必須項目のため欠席のhiddenを追加（固定で「0」をセット）
                knjCreateHidden($objForm, "KESSEKI_CONDITION", "0");
            }
        }
        knjCreateHidden($objForm, "KNJZ173_DISPPATTERN", $knjz173Disppattern);

        $disabledExtra = "";

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタンを作成する
        $extra = $disabledExtra."onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //削除ボタンを作成する
        $extra = $disabledExtra."onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタンを作成する
        $extra = $disabledExtra."onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "HID_KESSEKI_KANSAN", $Row["KESSEKI_KANSAN"]);
        knjCreateHidden($objForm, "HID_TIKOKU_CONDITION", $Row["TIKOKU_CONDITION"]);
        knjCreateHidden($objForm, "HID_SOUTAI_CONDITION", $Row["SOUTAI_CONDITION"]);
        knjCreateHidden($objForm, "targetGradeCntA", $targetGradeCnt["A"]);
        knjCreateHidden($objForm, "targetGradeCntH", $targetGradeCnt["H"]);
        knjCreateHidden($objForm, "targetGradeCntJ", $targetGradeCnt["J"]);
        knjCreateHidden($objForm, "targetGradeCntP", $targetGradeCnt["P"]);
        knjCreateHidden($objForm, "targetGradeCntK", $targetGradeCnt["K"]);
        knjCreateHidden($objForm, "targetGradeCntKey", implode(",", array_keys($targetGradeCnt)));
        knjCreateHidden($objForm, "targetGradeCntVal", implode(",", array_values($targetGradeCnt)));

        $model->selKaikinCd = "";  //フラグは、modelでセットしてform.phpの最後でクリア
        $arg["finish"]  = $objForm->get_finish();

        $arg["reload"]="";
        $arg["reload"]  = "window.open('knjz173index.php?cmd=list','left_frame');";
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz173Form2.html", $arg);
    }
}

//対象学年欄チェックボックス作成
function makeGrdCheckBox(&$objForm, &$arg, $model, $db, $schKind, $grdarry, $kaikinCd)
{
    $arg["KIND_".$schKind] = (get_count($grdarry[$schKind]) > 0) ? "1" : "";
    $test = (get_count($grdarry[$schKind]) > 0) ? "1" : "";
    $idFixNStr = "G_NAME_".$schKind;
    
    $disflg = $model->field["SET_PREFATTEND_GRADE"] == "2" ? " disabled" : "";
    $trFlg      = false;
    $trEndFlg   = false;

    foreach ($grdarry[$schKind] as $index => $schKind_grd_grdName1) {
        $count = $index + 1;
        if ($count == 1 || $trEndFlg) {
            $trFlg = true;
        } else {
            $trFlg = false;
        }
        $targetGradeArr = $db->getCol(knjz173Query::getTargetGrade($kaikinCd));
        $idStr = "G_KIND_".$schKind."_".$count;
        $defChkFlg = in_array($schKind_grd_grdName1["GRADE"], $targetGradeArr) ? " checked" : "";
        $defChkFlg2 = ($model->selKaikinCd != "1" && in_array($schKind_grd_grdName1["GRADE"], $model->mrgearry)) ? " checked " : "";
        $extra = " id=\"".$idStr."\" ".$disflg.$defChkFlg.$defChkFlg2;
        $row["ID"] = $idStr;
        $row["CHKBOX"] = knjCreateCheckBox($objForm, $idStr, $schKind_grd_grdName1["GRADE"], $extra, "");
        $row["NAME"] = $schKind_grd_grdName1["GRADE_NAME1"];

        //改行の追加
        if ($count % 3 == 0 || $count == get_count($grdarry[$schKind])) {
            $trEndFlg = true;
        } else {
            $trEndFlg = false;
        }
        $row["TR"] = $trFlg;
        $row["TR_END"] = $trEndFlg;

        $arg["checkboxes"][] = $row;
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == "" && $row["NAMESPARE2"] == '1') {
            $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
