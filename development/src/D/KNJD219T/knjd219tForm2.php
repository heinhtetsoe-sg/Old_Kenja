<?php

require_once('for_php7.php');

class knjd219tForm2
{
    function main(&$model)
    {
        //権限チェック(更新可、制限付き更新可)
        if (AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjd219tindex.php", "", "edit");
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $query = knjd219tQuery::getRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            //初期化
            $model->field2 = array();
            $Row =& $model->field2;
        }
        
        //科目コンボ
        $extra = "onChange=\"return btn_submit('chenge_cd');\"";
        $query = knjd219tQuery::getSubclass($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->subclasscd, $extra, 1, "BLANK");
        
        //満点、赤点を抽出(高校)
        //考査別時の満点、算出赤点
        $scoreLine = "";
        $perfect = "";
        if ($model->ruiseki_div === '1') {
            $scoreLine = $db->getOne(knjd219tQuery::getPassScoreLineTantai($model));
            if (!$scoreLine) {
                $scoreLine = 30;
            }
            $scorePerfect = $model->schoolkind === 'H' ? 100 : 200;
        }

        //累積時の満点、赤点を抽出
        $model->disabled = "";
        if ($model->schoolkind === 'H' && ($model->ruiseki_div === '2' || $model->ruiseki_div === '3') && $model->groupcd != "" && $model->testkind_itemcd != "") {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->subclasscd);
        
            $query = knjd219tQuery::getTestSubclassCnt($model);
            $testSubclassCnt = $db->getOne($query);
            if ($testSubclassCnt > 0) {
                $query = knjd219tQuery::getTestCnt($model, $classcd, $school_kind, $curriculum_cd, $subclasscd, $model->groupcd);
            } else {
                $query = knjd219tQuery::getTestCnt($model, "00", $model->schoolkind, "00", "000000", $model->groupcd);
            }

            $testCnt = $db->getOne($query);
            
            //満点
            $query = knjd219tQuery::getPerfect($model, $model->groupcd);
            $perfect = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //赤点
            $query = knjd219tQuery::getPassScoreLineRuiseki($model);
            $Line = $db->getRow($query, DB_FETCHMODE_ASSOC);
            
            if ($perfect["CNT"] == 0 && $testCnt == 0) {
                $arg["jscript"] = "OnTestCntError();";
                $model->disabled = "disabled ";
            } else {
                $model->disabled = "";
            }
            
            //満点および赤点を抽出
            $scorePerfect = $perfect["SUM_PERFECT"];
            $scoreLine = $Line["SUM_SCORE_LINE"];
            if ($perfect["CNT"] != $testCnt) {
                $multiPerfect = $model->schoolkind === 'H' ? 100 : 200;
                $scorePerfect += (($testCnt - $perfect["CNT"]) * $multiPerfect);
                if ($model->field["SEMESTER"] != "9") {
                    $scoreLine += (($testCnt - $Line["CNT"]) * 30);
                } else if ($model->field["SEMESTER"] == "9" && $Line["CNT"] == 0) {
                    $scoreLine += (($testCnt - $Line["CNT"]) * 30);
                }
            }
        }

        for ($i = 1; $i <= $model->setAssesslevelCount; $i++) {
            //評定項目
            $arg["data"]["ASSESSLEVEL_NAME_".$i] = '評定'.$i;
            
            //下限値
            //最低点
            if ($i == 1) {
                if (!$Row["ASSESSLOW_".$i]) {
                    $Row["ASSESSLOW_".$i] = 0;
                }
            }
            //赤点(評定2の最低点)
            if ($i == 2) {
                if (!$Row["ASSESSLOW_".$i]) {
                    //算出赤点
                    $Row["ASSESSLOW_".$i] = $scoreLine;
                    $extra = " STYLE=\"text-align:right;background:#ff0099\" ";
                } else {
                    if ($Row["ASSESSLOW_".$i] != $scoreLine && $scoreLine != "") {
                        $extra = " STYLE=\"text-align:right;background:#ff0099\" ";
                    } else {
                        $extra = " STYLE=\"text-align:right;\" ";
                    }
                }
                $extra .= " onblur=\"isNumbJyougen(this, ".$i.");\"";
                $arg["data"]["ASSESSLOW_".$i] = knjCreateTextBox($objForm, $Row["ASSESSLOW_".$i], "ASSESSLOW_".$i, 3, 3, $extra);
            } else {
                $arg["data"]["ASSESSLOW_".$i] = $Row["ASSESSLOW_".$i];
                knjCreateHidden($objForm, "ASSESSLOW_".$i, $Row["ASSESSLOW_".$i]);
            }
            
            //上限値
            if ($i == 1 || $i == $model->setAssesslevelCount) {
                //算出赤点のマイナス-1点を表示
                if ($i == 1) {
                    if ($Row["ASSESSHIGH_".$i] == "") {
                        //算出赤点
                        $Row["ASSESSHIGH_".$i] = $scoreLine - 1;
                    }
                //満点
                } else {
                    if (!$Row["ASSESSHIGH_".$i]) {
                        $Row["ASSESSHIGH_".$i] = $scorePerfect;
                    }
                }
                $arg["data"]["ASSESSHIGH_".$i] = $Row["ASSESSHIGH_".$i];
                knjCreateHidden($objForm, "ASSESSHIGH_".$i, $Row["ASSESSHIGH_".$i]);
            } else {
                $extra = " STYLE=\"text-align:right;\" onblur=\"isNumb(this, ".$i.");\"";
                $arg["data"]["ASSESSHIGH_".$i] = knjCreateTextBox($objForm, $Row["ASSESSHIGH_".$i], "ASSESSHIGH_".$i, 3, 3, $extra);
            }
        }
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJD219T");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        $arg["finish"]  = $objForm->get_finish();
        
        Query::dbCheckIn($db);
        
        if (VARS::get("cmd") != "edit" && $model->cmd != 'chenge_cd' && $model->cmd != 'reset'){
            $arg["reload"]  = "window.open('knjd219tindex.php?cmd=up_list&shori=add','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd219tForm2.html", $arg);
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
function makeBtn(&$objForm, &$arg, &$model) {
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$model->disabled);
    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra.$model->disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$model->disabled);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //CSV出力ボタンを作成する
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra.$model->disabled);
}

?>
