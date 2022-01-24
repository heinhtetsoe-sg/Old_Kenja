<?php

require_once('for_php7.php');

class knje390mSubList
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("sublist", "POST", "knje390mindex.php", "", "sublist");

        //DB接続
        $db = Query::dbCheckOut();
        
        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //年度の最新データを取得
        $getMaxDate = "";
        $getMaxDate = $db->getOne(knje390mQuery::getMaxRecordDataQuery($model));
        
        //作成日付
        $extra = " STYLE=\"background:darkgray\" readOnly ";
        $setRecordDate = str_replace("-", "/", $getMaxDate);
        $arg["data"]["SPACE"] = ' ';
        $arg["data"]["WRITING_DATE"] = knjCreateTextBox($objForm, $setRecordDate, "WRITING_DATE", 12, 12, $extra);

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model, $getMaxDate);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "subform2_check_B_sanshou" || $model->cmd == "subform2_check_C_sanshou") {
            unset($model->getYear);
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
        }
        //発達検査情報取得
        if ($model->cmd != "subform2_growup_B_sanshou") {
            $arg["CHECK_HYOUJI"] = '1';
            $arg["TITLE"] = '(発達検査)';
            if ($model->cmd == "subform2_check_B_sanshou_set" || $model->cmd == "subform2_check_C_sanshou_set") {
                $Row = $db->getRow(knje390mQuery::getSubQuery2CheckGetData($model, $getMaxDate), DB_FETCHMODE_ASSOC);
            }
            //検査日
            $Row["CHECK_DATE"] = str_replace("-", "/", $Row["CHECK_DATE"]);
            $arg["data"]["CHECK_DATE"] = View::popUpCalendar($objForm, "CHECK_DATE", $Row["CHECK_DATE"]);
            
            //検査機関
            $query = knje390mQuery::getCheckCentercdComboName();
            makeCmb($objForm, $arg, $db, $query, "CENTERCD", $Row["CENTERCD"], "", 1, 1);
            
            //検査名
            $extra = "style=\"height:35px; overflow:auto;\"";
            $arg["data"]["CHECK_NAME"] = knjCreateTextArea($objForm, "CHECK_NAME", 2, 21, "soft", $extra, $Row["CHECK_NAME"]);

            //検査者
            $extra = "";
            $arg["data"]["CHECKER"] = knjCreateTextBox($objForm, $Row["CHECKER"], "CHECKER", 20, 20, $extra);

            //検査結果・所見等
            $extra = "style=\"height:180px; overflow:auto;\"";
            $arg["data"]["CHECK_REMARK"] = knjCreateTextArea($objForm, "CHECK_REMARK", 10, 57, "soft", $extra, $Row["CHECK_REMARK"]);
        //課題・つけたい力取得
        } elseif ($model->cmd == "subform2_growup_B_sanshou") {
            $arg["GROWUP_HYOUJI"] = '1';
            $arg["TITLE"] = '(課題・つけたい力)';
            $Row = $db->getRow(knje390mQuery::getSubQuery2($model, $getMaxDate), DB_FETCHMODE_ASSOC);

            $extra = "style=\"height:210px; overflow:auto;\"";
            $arg["data"]["SUMMARY_GROWUP"] = knjCreateTextArea($objForm, "SUMMARY_GROWUP", 15, 71, "soft", $extra, $Row["SUMMARY_GROWUP"]);
            $arg["data"]["LIFESTYLE_GROWUP"] = knjCreateTextArea($objForm, "LIFESTYLE_GROWUP", 15, 71, "soft", $extra, $Row["LIFESTYLE_GROWUP"]);
            $arg["data"]["SOCIALITY_GROWUP"] = knjCreateTextArea($objForm, "SOCIALITY_GROWUP", 15, 71, "soft", $extra, $Row["SOCIALITY_GROWUP"]);
            $arg["data"]["COMMUNICATION_GROWUP"] = knjCreateTextArea($objForm, "COMMUNICATION_GROWUP", 15, 71, "soft", $extra, $Row["COMMUNICATION_GROWUP"]);
            $arg["data"]["PHYSICAL_ACTIVITY_GROWUP"] = knjCreateTextArea($objForm, "PHYSICAL_ACTIVITY_GROWUP", 15, 71, "soft", $extra, $Row["PHYSICAL_ACTIVITY_GROWUP"]);
            $arg["data"]["STUDY_GROWUP"] = knjCreateTextArea($objForm, "STUDY_GROWUP", 15, 71, "soft", $extra, $Row["STUDY_GROWUP"]);
            $arg["data"]["INTERESTING_GROWUP"] = knjCreateTextArea($objForm, "INTERESTING_GROWUP", 15, 71, "soft", $extra, $Row["INTERESTING_GROWUP"]);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubList.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model, $getMaxDate)
{
    $retCnt = 0;
    $query = knje390mQuery::getSubQuery2CheckRecordList($model, $getMaxDate);
    $result = $db->query($query);
    $centerName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->cmd === 'subform2_check_B_sanshou' || $model->cmd === 'subform2_check_B_sanshou_set') {
            $rowlist["SET_CMD"] = 'subform2_check_B_sanshou_set';
        } elseif ($model->cmd === 'subform2_check_C_sanshou' || $model->cmd === 'subform2_check_C_sanshou_set') {
            $rowlist["SET_CMD"] = 'subform2_check_C_sanshou_set';
        }
        $rowlist["RECORD_DIV_NAME"] = '検査機関'.($retCnt+1);
        $centerName = $db->getOne(knje390mQuery::getCheckCentercdName($rowlist["CENTERCD"]));
        $rowlist["CENTER_NAME"] = $centerName;
        $rowlist["CHECK_DATE"] = str_replace("-", "/", $rowlist["CHECK_DATE"]);
        
        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //選択ボタン
    if ($model->cmd === 'subform2_check_B_sanshou' || $model->cmd === 'subform2_check_C_sanshou' || $model->cmd === 'subform2_growup_B_sanshou') {
        $disabled = "disabled";
    }
    $extra = "onclick=\"return btn_submit()\"";
    $arg["button"]["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "選 択", $extra.$disabled);
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "GET_CMD", $model->cmd);
}
?>

