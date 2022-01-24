<?php

require_once('for_php7.php');

class knjd_behavior_lmForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("form1", "POST", "knjd_behavior_lmindex.php", "", "form1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $schInfo = $db->getRow(knjd_behavior_lmQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $arg["NAME_SHOW"] = $model->schregno."　".$schInfo["NAME"];

        //学期コンボ
        $query = knjd_behavior_lmQuery::getSemester($model);
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : $model->exp_semester;
        $extra = "onChange=\"return btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knjd_behavior_lmQuery::getBehavior($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $Row["RECORD"][$row["L_CD"]."_".$row["M_CD"]] = $row["RECORD"];
        }
        $result->free();

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $Row =& $model->record;
        }

        //出力項目取得
        $query = knjd_behavior_lmQuery::getHreportBehaviorCnt($model);
        $gradeCnt = $db->getOne($query);
        $query = knjd_behavior_lmQuery::getHreportBehavior($model, $gradeCnt);
        $result = $db->query($query);
        $model->itemArrayL = array();
        $model->itemArrayM = array();
        while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->itemArrayL[$setItem["L_CD"]] = $setItem["L_NAME"];
            $model->itemArrayM[$setItem["L_CD"]][$setItem["M_CD"]] = $setItem["M_NAME"];
        }

        if ($model->getPro["knjdBehaviorsd_UseText_P"] == "1") {
            //出力項目取得
            $query = knjd_behavior_lmQuery::getNameMst($model, "D036");
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
                if ($model->getPro["knjdBehaviorsd_UseText_P"] == "1") {
                    foreach ($model->itemArrayM[$Lkey] as $Mkey => $Mval) {
                        if ($mCnt > 0) {
                            $setData .= "<tr align=\"center\" height=\"30\">";
                        }
                        $extra = "STYLE=\"text-align: center\"; onblur=\"calc(this);\" oncontextmenu=\"kirikae2(this, '".$Lkey."')\";";
                        $recordVal = knjCreateTextBox($objForm, $Row["RECORD"][$Lkey."_".$Mkey], "RECORD_{$Lkey}_{$Mkey}", 3, 1, $extra);
                        $setData .= "<th width=\"66%\" align=\"left\" class=\"no_search\" nowrap>{$Mval}</th>";
                        $setData .= "<td width=\"12%\" bgcolor=\"#ffffff\">{$recordVal}</td>";
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
                        $check1 = ($Row["RECORD"][$Lkey."_".$Mkey] == "1") ? "checked" : "";
                        $extra = $check1." id=\"RECORD_{$Lkey}_{$Mkey}\"";
                        $recordVal = knjCreateCheckBox($objForm, "RECORD_{$Lkey}_{$Mkey}", "1", $extra, "");
                        $setData .= "<th width=\"66%\" align=\"left\" class=\"no_search\" nowrap>{$Mval}</th>";
                        $setData .= "<td width=\"12%\" bgcolor=\"#ffffff\">{$recordVal}</td>";
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

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear')\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //DB切断
        Query::dbCheckIn($db);

        //画面のリロード
        if ($model->cmd == "updEdit2") {
            $arg["reload"] = "parent.parent.left_frame.btn_submit('list');";
            $arg["reload"] .= " updateFrameUnLock(parent.frames);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd_behavior_lmForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
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

?>