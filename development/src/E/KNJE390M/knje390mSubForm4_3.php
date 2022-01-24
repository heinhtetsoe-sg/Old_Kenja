<?php

require_once('for_php7.php');
class knje390mSubForm4_3
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform4_3", "POST", "knje390mindex.php", "", "subform4_3");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //医療・福祉・労働・家庭地域生活と相談の記録の切換(テーブルが違う)
        $extra = "onchange=\"return btn_submit('subform4_smooth')\"";
        $query = knje390mQuery::getSmoothDataDiv();
        makeCmb($objForm, $arg, $db, $query, "DATA_DIV", $model->field4["DATA_DIV"], $extra, 1, "");
        
        /************/
        /* 履歴一覧 */
        /************/
        if ($model->field4["DATA_DIV"] === '2') {
            $rirekiCnt = makeList($arg, $db, $model);
        }

        /************/
        /* テキスト */
        /************/
        //医療・福祉・労働・家庭地域生活取得
        if ($model->field4["DATA_DIV"] === '1') {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row = $db->getRow(knje390mQuery::getSubQuery4MainGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field4;
            }
            //相談の記録取得
        } else {
            if ($model->cmd == "subform4_smooth_set") {
                if (isset($model->schregno) && !isset($model->warning)) {
                    $Row = $db->getRow(knje390mQuery::getSubQuery4ConsultationGetData($model), DB_FETCHMODE_ASSOC);
                } else {
                    $Row =& $model->field4;
                }
            } else {
                $Row =& $model->field4;
            }
        }
        
        if ($model->field4["DATA_DIV"] === '1') {
            $arg["DATA_DIV1"] = "1";
            //医療
            $extra = "style=\"height:65px; overflow:auto;\"";
            // $arg["data"]["MEDICAL_GOAL_AFTER_GRADUATION"] = knjCreateTextArea($objForm, "MEDICAL_GOAL_AFTER_GRADUATION", 4, 19, "soft", $extra, $Row["MEDICAL_GOAL_AFTER_GRADUATION"]);
            $arg["data"]["MEDICAL_GOAL_AFTER_GRADUATION"] = getTextOrArea($objForm, "MEDICAL_GOAL_AFTER_GRADUATION", 9, 4, $Row["MEDICAL_GOAL_AFTER_GRADUATION"]);
            // $arg["data"]["MEDICAL_STATUS"] = knjCreateTextArea($objForm, "MEDICAL_STATUS", 4, 19, "soft", $extra, $Row["MEDICAL_STATUS"]);
            $arg["data"]["MEDICAL_STATUS"] = getTextOrArea($objForm, "MEDICAL_STATUS", 9, 4, $Row["MEDICAL_STATUS"]);
            // $arg["data"]["MEDICAL_SHORT_TERM_GOAL"] = knjCreateTextArea($objForm, "MEDICAL_SHORT_TERM_GOAL", 4, 19, "soft", $extra, $Row["MEDICAL_SHORT_TERM_GOAL"]);
            $arg["data"]["MEDICAL_SHORT_TERM_GOAL"] = getTextOrArea($objForm, "MEDICAL_SHORT_TERM_GOAL", 9, 4, $Row["MEDICAL_SHORT_TERM_GOAL"]);
            // $arg["data"]["MEDICAL_TANGIBLE_SUPPORT"] = knjCreateTextArea($objForm, "MEDICAL_TANGIBLE_SUPPORT", 4, 19, "soft", $extra, $Row["MEDICAL_TANGIBLE_SUPPORT"]);
            $arg["data"]["MEDICAL_TANGIBLE_SUPPORT"] = getTextOrArea($objForm, "MEDICAL_TANGIBLE_SUPPORT", 9, 4, $Row["MEDICAL_TANGIBLE_SUPPORT"]);
            //福祉
            // $arg["data"]["WELFARE_GOAL_AFTER_GRADUATION"] = knjCreateTextArea($objForm, "WELFARE_GOAL_AFTER_GRADUATION", 4, 19, "soft", $extra, $Row["WELFARE_GOAL_AFTER_GRADUATION"]);
            $arg["data"]["WELFARE_GOAL_AFTER_GRADUATION"] = getTextOrArea($objForm, "WELFARE_GOAL_AFTER_GRADUATION", 9, 4, $Row["WELFARE_GOAL_AFTER_GRADUATION"]);
            // $arg["data"]["WELFARE_STATUS"] = knjCreateTextArea($objForm, "WELFARE_STATUS", 4, 19, "soft", $extra, $Row["WELFARE_STATUS"]);
            $arg["data"]["WELFARE_STATUS"] = getTextOrArea($objForm, "WELFARE_STATUS", 9, 4, $Row["WELFARE_STATUS"]);
            // $arg["data"]["WELFARE_SHORT_TERM_GOAL"] = knjCreateTextArea($objForm, "WELFARE_SHORT_TERM_GOAL", 4, 19, "soft", $extra, $Row["WELFARE_SHORT_TERM_GOAL"]);
            $arg["data"]["WELFARE_SHORT_TERM_GOAL"] = getTextOrArea($objForm, "WELFARE_SHORT_TERM_GOAL", 9, 4, $Row["WELFARE_SHORT_TERM_GOAL"]);
            // $arg["data"]["WELFARE_TANGIBLE_SUPPORT"] = knjCreateTextArea($objForm, "WELFARE_TANGIBLE_SUPPORT", 4, 19, "soft", $extra, $Row["WELFARE_TANGIBLE_SUPPORT"]);
            $arg["data"]["WELFARE_TANGIBLE_SUPPORT"] = getTextOrArea($objForm, "WELFARE_TANGIBLE_SUPPORT", 9, 4, $Row["WELFARE_TANGIBLE_SUPPORT"]);
            //労働
            // $arg["data"]["WORK_GOAL_AFTER_GRADUATION"] = knjCreateTextArea($objForm, "WORK_GOAL_AFTER_GRADUATION", 4, 19, "soft", $extra, $Row["WORK_GOAL_AFTER_GRADUATION"]);
            $arg["data"]["WORK_GOAL_AFTER_GRADUATION"] = getTextOrArea($objForm, "WORK_GOAL_AFTER_GRADUATION", 9, 4, $Row["WORK_GOAL_AFTER_GRADUATION"]);
            // $arg["data"]["WORK_STATUS"] = knjCreateTextArea($objForm, "WORK_STATUS", 4, 19, "soft", $extra, $Row["WORK_STATUS"]);
            $arg["data"]["WORK_STATUS"] = getTextOrArea($objForm, "WORK_STATUS", 9, 4, $Row["WORK_STATUS"]);
            // $arg["data"]["WORK_SHORT_TERM_GOAL"] = knjCreateTextArea($objForm, "WORK_SHORT_TERM_GOAL", 4, 19, "soft", $extra, $Row["WORK_SHORT_TERM_GOAL"]);
            $arg["data"]["WORK_SHORT_TERM_GOAL"] = getTextOrArea($objForm, "WORK_SHORT_TERM_GOAL", 9, 4, $Row["WORK_SHORT_TERM_GOAL"]);
            // $arg["data"]["WORK_TANGIBLE_SUPPORT"] = knjCreateTextArea($objForm, "WORK_TANGIBLE_SUPPORT", 4, 19, "soft", $extra, $Row["WORK_TANGIBLE_SUPPORT"]);
            $arg["data"]["WORK_TANGIBLE_SUPPORT"] = getTextOrArea($objForm, "WORK_TANGIBLE_SUPPORT", 9, 4, $Row["WORK_TANGIBLE_SUPPORT"]);
            //家庭地域生活
            // $arg["data"]["COMMU_GOAL_AFTER_GRADUATION"] = knjCreateTextArea($objForm, "COMMU_GOAL_AFTER_GRADUATION", 4, 19, "soft", $extra, $Row["COMMU_GOAL_AFTER_GRADUATION"]);
            $arg["data"]["COMMU_GOAL_AFTER_GRADUATION"] = getTextOrArea($objForm, "COMMU_GOAL_AFTER_GRADUATION", 9, 4, $Row["COMMU_GOAL_AFTER_GRADUATION"]);
            // $arg["data"]["COMMU_STATUS"] = knjCreateTextArea($objForm, "COMMU_STATUS", 4, 19, "soft", $extra, $Row["COMMU_STATUS"]);
            $arg["data"]["COMMU_STATUS"] = getTextOrArea($objForm, "COMMU_STATUS", 9, 4, $Row["COMMU_STATUS"]);
            // $arg["data"]["COMMU_SHORT_TERM_GOAL"] = knjCreateTextArea($objForm, "COMMU_SHORT_TERM_GOAL", 4, 19, "soft", $extra, $Row["COMMU_SHORT_TERM_GOAL"]);
            $arg["data"]["COMMU_SHORT_TERM_GOAL"] = getTextOrArea($objForm, "COMMU_SHORT_TERM_GOAL", 9, 4, $Row["COMMU_SHORT_TERM_GOAL"]);
            // $arg["data"]["COMMU_TANGIBLE_SUPPORT"] = knjCreateTextArea($objForm, "COMMU_TANGIBLE_SUPPORT", 4, 19, "soft", $extra, $Row["COMMU_TANGIBLE_SUPPORT"]);
            $arg["data"]["COMMU_TANGIBLE_SUPPORT"] = getTextOrArea($objForm, "COMMU_TANGIBLE_SUPPORT", 9, 4, $Row["COMMU_TANGIBLE_SUPPORT"]);
            $arg["data"]["TEXT_SIZE"] = '<font size="2" color="red">(全角9文字X4行まで)</font>';
        //相談の記録取得
        } else {
            $arg["DATA_DIV2"] = "1";
            //会議名
            $extra = "style=\"height:60px; overflow:auto;\"";
            // $arg["data"]["MEETING_NAME"] = knjCreateTextArea($objForm, "MEETING_NAME", 4, 21, "soft", $extra, $Row["MEETING_NAME"]);
            $arg["data"]["MEETING_NAME"] = getTextOrArea($objForm, "MEETING_NAME", 40, 1, $Row["MEETING_NAME"]);
            $arg["data"]["MEETING_NAME_SIZE"] = '<font size="2" color="red">(全角40文字まで)</font>';
            //会議日
            $Row["MEETING_DATE"] = str_replace("-", "/", $Row["MEETING_DATE"]);
            $arg["data"]["MEETING_DATE"] = View::popUpCalendar($objForm, "MEETING_DATE", $Row["MEETING_DATE"]);
            //構成員
            $extra = "style=\"height:60px; overflow:auto;\"";
            // $arg["data"]["TEAM_MEMBERS"] = knjCreateTextArea($objForm, "TEAM_MEMBERS", 4, 21, "soft", $extra, $Row["TEAM_MEMBERS"]);
            $arg["data"]["TEAM_MEMBERS"] = getTextOrArea($objForm, "TEAM_MEMBERS", 10, 4, $Row["TEAM_MEMBERS"]);
            $arg["data"]["TEAM_MEMBERS_SIZE"] = '<font size="2" color="red">(全角10文字X4行まで)</font>';
            //概要
            $extra = "style=\"height:105px; overflow:auto;\"";
            // $arg["data"]["MEETING_SUMMARY"] = knjCreateTextArea($objForm, "MEETING_SUMMARY", 7, 51, "soft", $extra, $Row["MEETING_SUMMARY"]);
            $arg["data"]["MEETING_SUMMARY"] = getTextOrArea($objForm, "MEETING_SUMMARY", 25, 7, $Row["MEETING_SUMMARY"]);
            $arg["data"]["MEETING_SUMMARY_SIZE"] = '<font size="2" color="red">(全角25文字X7行まで)</font>';
        }
        
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm4_3.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model)
{
    $retCnt = 0;
    $query = knje390mQuery::getSubQuery4ConsultationRecordList($model);
    $result = $db->query($query);
    $classShubetsuName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rowlist["RECORD_DIV_NAME"] = '会議'.($retCnt+1);
        $rowlist["MEETING_DATE"] = str_replace("-", "/", $rowlist["MEETING_DATE"]);
        $rowlist["CONTENTS_NAIYOU"] = '会議日:'.$rowlist["MEETING_DATE"].'　会議概要:'.substr($rowlist["MEETING_SUMMARY"], 0, 120);
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
function makeBtn(&$objForm, &$arg)
{
    //医療・福祉・労働・家庭地域生活
    //更新ボタン
    $extra = "onclick=\"return btn_submit('smooth4_updatemain');\"";
    $arg["button"]["btn_updatemain"] = knjCreateBtn($objForm, "btn_updatemain", "更 新", $extra.$disabled);

    //相談の記録
    //構成員参照
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=team_member_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_staff"] = knjCreateBtn($objForm, "btn_staff", "構成員参照", $extra.$disabled);
    
    //追加ボタン
    $extra = "onclick=\"return btn_submit('smooth4_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra.$disabled);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('smooth4_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('smooth4_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra.$disabled);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('subform4A');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra.$disabled);
}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val)
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $extra = "style=\"overflow-y:scroll\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2), "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

