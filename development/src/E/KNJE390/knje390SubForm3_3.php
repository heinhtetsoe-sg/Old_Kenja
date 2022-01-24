<?php

require_once('for_php7.php');

class knje390SubForm3_3
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3_3", "POST", "knje390index.php", "", "subform3_3");

        //DB接続
        $db = Query::dbCheckOut();

        //表示日付をセット
        if ($model->record_date === 'NEW' && $model->main_year === CTRL_YEAR) {
            $setHyoujiDate = '';
        } else {
            if ($model->record_date === 'NEW') {
                $setHyoujiDate = '　　<font color="RED"><B>'.$model->main_year.'年度 最終更新データ 参照中</B></font>';
            } else {
                $setHyoujiDate = '　　<font color="RED"><B>'.str_replace("-", "/", $model->record_date).' 履歴データ 参照中</B></font>';
            }
        }

        //生徒情報
        $info = $db->getRow(knje390Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"].$setHyoujiDate;

        /************/
        /* 履歴一覧 */
        /************/
        //福祉支援内容の切換
        $extra = "onchange=\"return btn_submit('subform3_welfare')\"";
        $query = knje390Query::getSupportPlanWelfareRecordDiv();
        makeCmb($objForm, $arg, $db, $query, "RECORD_DIV", $model->field3["RECORD_DIV"], $extra, 1, "");
        
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "subform3_welfare") {
            unset($model->getYear);
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
        }
        //福祉情報取得
        if ($model->cmd == "subform3_welfare_set"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje390Query::getSubQuery3WelfareGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field3;
            }
        } else {
            $Row =& $model->field3;
        }
        
        if ($model->field3["RECORD_DIV"] === '1') {
            $arg["RECORD_DIV1"] = "1";
            //現在の支援と課題、手立て、評価
            //現在の支援と課題
            $extra = "style=\"height:75px; overflow:auto;\"";
            $arg["data"]["SUPPORT_PRESENT"] = knjCreateTextArea($objForm, "SUPPORT_PRESENT", 5, 33, "soft", $extra, $Row["SUPPORT_PRESENT"]);
            //手立て
            $extra = "style=\"height:75px; overflow:auto;\"";
            $arg["data"]["SUPPORT_MEANS"] = knjCreateTextArea($objForm, "SUPPORT_MEANS", 5, 33, "soft", $extra, $Row["SUPPORT_MEANS"]);
            //評価
            $extra = "style=\"height:75px; overflow:auto;\"";
            $arg["data"]["SUPPORT_ASSESS"] = knjCreateTextArea($objForm, "SUPPORT_ASSESS", 5, 33, "soft", $extra, $Row["SUPPORT_ASSESS"]);
            $arg["data"]["SUPPORT_SIZE"] = '<font size="1" color="red">(全角16文字5行まで)</font>';
        } else if ($model->field3["RECORD_DIV"] === '2') {
            $arg["RECORD_DIV2"] = "1";
            //将来必要となると考えられるサービス
            $extra = "style=\"height:75px; overflow:auto;\"";
            $arg["data"]["SERVICE_NEED_FUTURE"] = knjCreateTextArea($objForm, "SERVICE_NEED_FUTURE", 5, 41, "soft", $extra, $Row["SERVICE_NEED_FUTURE"]);
            $arg["data"]["SERVICE_NEED_FUTURE_SIZE"] = '<font size="1" color="red">(全角20文字5行まで)</font>';
        } else if ($model->field3["RECORD_DIV"] === '3') {
            $arg["RECORD_DIV3"] = "1";
            //連携会議・ケース会議の記録
            //会議日
            $Row["MEETING_DATE"] = str_replace("-", "/", $Row["MEETING_DATE"]);
            $arg["data"]["MEETING_DATE"] = View::popUpCalendar($objForm, "MEETING_DATE", $Row["MEETING_DATE"]);
            //構成員
            $extra = "style=\"height:60px; overflow:auto;\"";
            $arg["data"]["TEAM_MEMBERS"] = knjCreateTextArea($objForm, "TEAM_MEMBERS", 4, 21, "soft", $extra, $Row["TEAM_MEMBERS"]);
            $arg["data"]["TEAM_MEMBERS_SIZE"] = '<font size="1" color="red">(全角10文字4行まで)</font>';
            //概要
            $extra = "style=\"height:95px; overflow:auto;\"";
            $arg["data"]["MEETING_SUMMARY"] = knjCreateTextArea($objForm, "MEETING_SUMMARY", 7, 51, "soft", $extra, $Row["MEETING_SUMMARY"]);
            $arg["data"]["MEETING_SUMMARY_SIZE"] = '<font size="1" color="red">(全角25文字7行まで)</font>';
        }
        
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje390SubForm3_3.html", $arg); 
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    $retCnt = 0;
    $query = knje390Query::getSubQuery3WelfareRecordList($model);
    $result = $db->query($query);
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($model->field3["RECORD_DIV"] === '1') {
            $rowlist["CONTENTS_NAME"] = '記載内容'.($retCnt+1);
            $rowlist["CONTENTS_NAIYOU"] = '現在の支援と課題:'.substr($rowlist["SUPPORT_PRESENT"], 0, 110);
        } else if ($model->field3["RECORD_DIV"] === '2') {
            $rowlist["CONTENTS_NAME"] = '必要サービス'.($retCnt+1);
            $rowlist["CONTENTS_NAIYOU"] = $rowlist["SERVICE_NEED_FUTURE"];
        } else if ($model->field3["RECORD_DIV"] === '3') {
            $rowlist["MEETING_DATE"] = str_replace("-", "/", $rowlist["MEETING_DATE"]);
            $rowlist["CONTENTS_NAME"] = '連携会議'.($retCnt+1);
            $rowlist["CONTENTS_NAIYOU"] = '会議日:'.$rowlist["MEETING_DATE"].'　会議概要:'.substr($rowlist["MEETING_SUMMARY"], 0, 120);
        }
        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //福祉サービスマスタ参照
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390/knje390index.php?cmd=aftertime_need_service_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_service"] = knjCreateBtn($objForm, "btn_service", "福祉サービスマスタ参照", $extra.$disabled);
    //構成員参照
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390/knje390index.php?cmd=team_member_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_staff"] = knjCreateBtn($objForm, "btn_staff", "構成員参照", $extra.$disabled);

    //追加ボタン
    $extra = "onclick=\"return btn_submit('welfare3_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra.$disabled);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('welfare3_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('welfare3_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra.$disabled);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('subform3A');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra.$disabled);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

