<?php

require_once('for_php7.php');

class knjd132dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd132dindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //学期コンボ
        $query = knjd132dQuery::getSemester($model);
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : $model->exp_semester;
        if ($model->cmd == "edit2") {
            $model->field["SEMESTER"] = $model->ikkatsuSeme;
        }
        $extra = "onChange=\"return btn_submit('changeSeme')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");
        knjCreateHidden($objForm, "SEMESTER_FLG", $model->field["SEMESTER"]);

        //"総合的な学習の時間"５年と６年のときは非表示
        if (!($model->schoolkind == "P" && ($model->grade_cd == "05" || $model->grade_cd == "06"))) {
            $arg["setSougou"] = "1";
        }
        
        //学期性に合わせた画面表示
        if ($model->semesCnt == 2){
            $arg["semes2"] = "1";
        } else if ($model->semesCnt == 3){
            $arg["semes3"] = "1";
        } else {
            $arg["notSemes"] = "1";
        }

        //生活ようすの取得
        $Row = $row = array();
        $result = $db->query(knjd132dQuery::getBehavior($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semes = "";
            if ($row["SEMESTER"] == "1") {
                $semes = "_1";  //1学期
            } else if ($row["SEMESTER"] == "2") {
                $semes = "_2";  //2学期
            } else if ($row["SEMESTER"] == "3") {
                $semes = "_3";  //3学期
            }
            $Row["RECORD"][$row["L_CD"]."_".$row["M_CD"].$semes] = $row["RECORD"];
        }
        $result->free();

        //所見取得
        $row = $db->getRow(knjd132dQuery::getHreportremarkDat($model), DB_FETCHMODE_ASSOC);

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $Row =& $model->record;
            $row =& $model->field;
        }

        //出力項目取得
        $query = knjd132dQuery::getHreportBehaviorCnt($model);
        $gradeCnt = $db->getOne($query);
        $query = knjd132dQuery::getHreportBehavior($model, $gradeCnt);
        $result = $db->query($query);
        $model->itemArrayL = array();
        $model->itemArrayM = array();
        while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->itemArrayL[$setItem["L_CD"]] = $setItem["L_NAME"];
            $model->itemArrayM[$setItem["L_CD"]][$setItem["M_CD"]] = $setItem["M_NAME"];
        }

        if ($model->Properties["knjdBehaviorsd_UseText_P"] == "1") {
            //出力項目取得
            $query = knjd132dQuery::getNameMst($model, "D036");
            $result = $db->query($query);
            $setCheckVal = "";
            $checkSep = "/";
            $setTextTitle = "(";
            $sep = "";
            $model->textValue = array();
            while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setCheckVal .= $checkSep.$setItem["NAME1"];
                $model->textValue[$setItem["NAMECD1"]] = $setItem;
                $setTextTitle .= $sep.$setItem["NAME1"].":".$setItem["NAME2"];
                $sep = "　";
                $checkSep = "|";
                $dataArray[] = array("VAL"  => "\"javascript:setClickValue('".$setItem["NAME1"]."')\"",
                                     "NAME" => $setItem["NAME1"].":".$setItem["NAME2"]);
            }
            $setCheckVal .= "/";
            $setTextTitle .= ")";
            $arg["TEXT_TITLE"] = $setTextTitle;
            knjCreateHidden($objForm, "CHECK_VAL", $setCheckVal);
            knjCreateHidden($objForm, "CHECK_ERR_MSG", $setTextTitle);

            //ドロップダウンリスト
            $arg["menuTitle"]["CLICK_NAME"] = knjCreateBtn($objForm, "btn_end", "×", "onclick=\"return setClickValue('999');\"");
            $arg["menuTitle"]["CLICK_VAL"] = "javascript:setClickValue('999')";
            if (is_array($dataArray)) {
                foreach ($dataArray as $key => $val) {
                    $setData["CLICK_NAME"] = $val["NAME"];
                    $setData["CLICK_VAL"] = $val["VAL"];
                    $arg["menu"][] = $setData;
                }
            }

        }
        if (is_array($model->itemArrayL)) {
            $setData = "";
            foreach ($model->itemArrayL as $Lkey => $Lval) {
                $lspan = get_count($model->itemArrayM[$Lkey]);
                $setData .= "<tr align=\"center\" height=\"30\">";
                $setData .= "<th width=\"22%\" align=\"left\" class=\"no_search\" rowspan=\"{$lspan}\" nowrap>{$Lval}</th>";
                $mCnt = 0;
                if ($model->Properties["knjdBehaviorsd_UseText_P"] == "1") {
                    foreach ($model->itemArrayM[$Lkey] as $Mkey => $Mval) {
                        if ($mCnt > 0) {
                            $setData .= "<tr align=\"center\" height=\"30\">";
                        }
                        $extra1 = "STYLE=\"text-align: center\"; onblur=\"calc(this);\" oncontextmenu=\"kirikae2(this, '".$Lkey."')\"; ";
                        $extra2 = "STYLE=\"text-align: center\"; onblur=\"calc(this);\" oncontextmenu=\"kirikae2(this, '".$Lkey."')\"; ";
                        $extra3 = "STYLE=\"text-align: center\"; onblur=\"calc(this);\" oncontextmenu=\"kirikae2(this, '".$Lkey."')\"; ";
                        if ($model->field["SEMESTER"] == "1"){
                            $extra2 .= "disabled";  //2学期を非活性
                            $extra3 .= "disabled";  //3学期を非活性
                        } else if ($model->field["SEMESTER"] == "2"){
                            $extra1 .= "disabled";  //1学期を非活性
                            $extra3 .= "disabled";  //3学期を非活性
                        } else if ($model->field["SEMESTER"] == "3"){
                            $extra1 .= "disabled";  //1学期を非活性
                            $extra2 .= "disabled";  //1学期を非活性
                        }
                        $recordVal1 = knjCreateTextBox($objForm, $Row["RECORD"][$Lkey."_".$Mkey."_1"], "RECORD_{$Lkey}_{$Mkey}_1", 3, 1, $extra1);
                        $recordVal2 = knjCreateTextBox($objForm, $Row["RECORD"][$Lkey."_".$Mkey."_2"], "RECORD_{$Lkey}_{$Mkey}_2", 3, 1, $extra2);
                        $recordVal3 = knjCreateTextBox($objForm, $Row["RECORD"][$Lkey."_".$Mkey."_3"], "RECORD_{$Lkey}_{$Mkey}_3", 3, 1, $extra3);
                        if ($model->semesCnt == 2){
                            $setData .= "<th width=\"54%\" align=\"left\" bgcolor=\"#ffffff\" nowrap>{$Mval}</th>";
                            $setData .= "<td width=\"12%\" bgcolor=\"#ffffff\">{$recordVal1}</td>";
                            $setData .= "<td width=\"12%\" bgcolor=\"#ffffff\">{$recordVal2}</td>";
                        } else if ($model->semesCnt == 3){
                            $setData .= "<th width=\"42%\" align=\"left\" bgcolor=\"#ffffff\" nowrap>{$Mval}</th>";
                            $setData .= "<td width=\"12%\" bgcolor=\"#ffffff\">{$recordVal1}</td>";
                            $setData .= "<td width=\"12%\" bgcolor=\"#ffffff\">{$recordVal2}</td>";
                            $setData .= "<td width=\"12%\" bgcolor=\"#ffffff\">{$recordVal3}</td>";
                        }

                        if ($mCnt > 0) {
                            $setData .= "</tr>";
                        }
                        $mCnt++;
                    }
                } else {
                    foreach ($model->itemArrayM[$Lkey] as $Mkey => $Mval) {
                        if ($mCnt > 0) {
                            $setData .= "<tr align=\"center\" height=\"30\">";
                        }
                        $check1 = ($Row["RECORD"][$Lkey."_".$Mkey."_1"] == "1") ? "checked" : "";
                        $check2 = ($Row["RECORD"][$Lkey."_".$Mkey."_2"] == "1") ? "checked" : "";
                        $check3 = ($Row["RECORD"][$Lkey."_".$Mkey."_3"] == "1") ? "checked" : "";
                        $extra1 = $check1." id=\"RECORD_{$Lkey}_{$Mkey}_1\"";
                        $extra2 = $check2." id=\"RECORD_{$Lkey}_{$Mkey}_2\"";
                        $extra3 = $check3." id=\"RECORD_{$Lkey}_{$Mkey}_3\"";
                        if ($model->field["SEMESTER"] == "1"){
                            $extra2 .= "disabled";  //2学期を非活性
                            $extra3 .= "disabled";  //3学期を非活性
                        } else if ($model->field["SEMESTER"] == "2"){
                            $extra1 .= "disabled";  //1学期を非活性
                            $extra3 .= "disabled";  //3学期を非活性
                        } else if ($model->field["SEMESTER"] == "3"){
                            $extra1 .= "disabled";  //1学期を非活性
                            $extra2 .= "disabled";  //1学期を非活性
                        }
                        $recordVal1 = knjCreateCheckBox($objForm, "RECORD_{$Lkey}_{$Mkey}_1", "1", $extra1, "");
                        $recordVal2 = knjCreateCheckBox($objForm, "RECORD_{$Lkey}_{$Mkey}_2", "1", $extra2, "");
                        $recordVal3 = knjCreateCheckBox($objForm, "RECORD_{$Lkey}_{$Mkey}_3", "1", $extra3, "");
                        if ($model->semesCnt == 2){
                            $setData .= "<th width=\"54%\" align=\"left\" bgcolor=\"#ffffff\" nowrap>{$Mval}</th>";
                            $setData .= "<td width=\"12%\" bgcolor=\"#ffffff\">{$recordVal1}</td>";
                            $setData .= "<td width=\"12%\" bgcolor=\"#ffffff\">{$recordVal2}</td>";
                        } else if ($model->semesCnt == 3){
                            $setData .= "<th width=\"42%\" align=\"left\" bgcolor=\"#ffffff\" nowrap>{$Mval}</th>";
                            $setData .= "<td width=\"12%\" bgcolor=\"#ffffff\">{$recordVal1}</td>";
                            $setData .= "<td width=\"12%\" bgcolor=\"#ffffff\">{$recordVal2}</td>";
                            $setData .= "<td width=\"12%\" bgcolor=\"#ffffff\">{$recordVal3}</td>";
                        }
                        if ($mCnt > 0) {
                            $setData .= "</tr>";
                        }
                        $mCnt++;
                    }
                }
                $setData .= "</tr>";
            }
            $arg["data"]["setData"] = $setData;
        }

        //テキストボックス
        //内容
        $extra = " id=\"TOTALSTUDYTIME\" onkeyup=\"charCount(this.value, {$model->totalstudytime_gyou}, ({$model->totalstudytime_moji} * 2), true);\"";
        $arg["TOTALSTUDYTIME"] = knjCreateTextArea($objForm, "TOTALSTUDYTIME", $model->totalstudytime_gyou, ($model->totalstudytime_moji * 2), "soft", $extra, $row["TOTALSTUDYTIME"]);
        $arg["TOTALSTUDYTIME_COMMENT"] = "(全角".$model->totalstudytime_moji."文字X".$model->totalstudytime_gyou."行まで)";
        $extra  = "onclick=\"return btn_submit('teikei1');\"";
        $arg["btn_teikei1"] = knjCreateBtn($objForm, "btn_teikei1", "定型文選択", $extra);

        //取り組みのようす
        $extra = " id=\"REMARK1\" onkeyup=\"charCount(this.value, {$model->remark1_gyou}, ({$model->remark1_moji} * 2), true);\"";
        $arg["REMARK1"] = knjCreateTextArea($objForm, "REMARK1", $model->remark1_gyou, ($model->remark1_moji * 2), "soft", $extra, $row["REMARK1"]);
        $arg["REMARK1_COMMENT"] = "(全角".$model->remark1_moji."文字X".$model->remark1_gyou."行まで)";
        $extra  = "onclick=\"return btn_submit('teikei2');\"";
        $arg["btn_teikei2"] = knjCreateBtn($objForm, "btn_teikei2", "定型文選択", $extra);

        //出席のようす備考
        $extra = "id=\"ATTENDREC_REMARK\" onkeyup=\"charCount(this.value, {$model->attendrec_remark_gyou}, ({$model->attendrec_remark_moji} * 2), true);\"";
        $arg["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_gyou, ($model->attendrec_remark_moji * 2), "soft", $extra, $row["ATTENDREC_REMARK"]);
        $arg["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";

        //学校から
        $extra = "id=\"COMMUNICATION\" onkeyup=\"charCount(this.value, {$model->communication_gyou}, ({$model->communication_moji} * 2), true);\"";
        $arg["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION", $model->communication_gyou, ($model->communication_moji * 2), "soft", $extra, $row["COMMUNICATION"]);
        $arg["COMMUNICATION_COMMENT"] = "(全角".$model->communication_moji."文字X".$model->communication_gyou."行まで)";

        //ボタン作成
        //一括更新
        $link = REQUESTROOT."/D/KNJD132D/knjd132dindex.php?cmd=ikkatsu&SCHREGNO={$model->schregno}&sendSEME={$model->field["SEMESTER"]}";
        $extra = "onclick=\"Page_jumper('$link');\"";
        $arg["button"]["ikkatsu"] = knjCreateBtn($objForm, "ikkatsu", "一括更新", $extra);

        //更新ボタン
        $extra = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update')\"" : "disabled";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //更新後前後の生徒へ
        if (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
        } else {
            $extra = "disabled style=\"width:130px\"";
            $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の生徒へ", $extra);
            $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の生徒へ", $extra);
        }

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "SCHOOLKIND", $model->schoolkind);
        knjCreateHidden($objForm, "GRADE_CD", $model->grade_cd);

        if (get_count($model->warning) == 0 && $model->cmd != "reset") {
            $arg["next"] = "NextStudent2(0);";
        } else if (get_count($model->warning) != 0 || $model->cmd == "reset") {
            $arg["next"] = "NextStudent2(1);";
        }
        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd132dForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") $opt[] = array("label" => "", "value" => "");
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
?>
