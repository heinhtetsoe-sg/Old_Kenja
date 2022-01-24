<?php

require_once('for_php7.php');
class knje390mSubForm2_2
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2_2", "POST", "knje390mindex.php", "", "subform2_2");

        //DB接続
        $db = Query::dbCheckOut();

        //カレンダー呼び出し
        $my = new mycalendar();

        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "subform2_check") {
            unset($model->getYear);
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
        }
        //発達検査情報取得
        if ($model->cmd == "subform2_check_set") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row = $db->getRow(knje390mQuery::getSubQuery2CheckGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field2;
            }
        } else {
            $Row =& $model->field2;
        }
        
        //検査日
        // $Row["CHECK_DATE"] = str_replace("-", "/", $Row["CHECK_DATE"]);
        // $arg["data"]["CHECK_DATE"] = View::popUpCalendar($objForm, "CHECK_DATE", $Row["CHECK_DATE"]);
        $datecutcnt = get_count(preg_split("{-}", $Row["CHECK_DATE"]));
        if ($datecutcnt > 1) {
            $Row["CHECK_DATE"] = substr($Row["CHECK_DATE"], 0, -3);
        }
        $arg["data"]["CHECK_DATE"] = str_replace("\n", "", $my->MyMonthWin2($objForm, "CHECK_DATE", $Row["CHECK_DATE"]));
        
        //検査機関
        $extra = "style=\"overflow:auto;\"";
        // $arg["data"]["CHECK_CENTER_TEXT"] = knjCreateTextArea($objForm, "CHECK_CENTER_TEXT", 2, 101, "soft", $extra, $Row["CHECK_CENTER_TEXT"]);
        $arg["data"]["CHECK_CENTER_TEXT"] = getTextOrArea($objForm, "CHECK_CENTER_TEXT", 50, 2, $Row["CHECK_CENTER_TEXT"]);
        $arg["data"]["CHECK_CENTER_TEXT_SIZE"] = '<font size="2" color="red">(全角50文字X2行まで)</font>';
        
        //検査名
        $extra = "style=\"height:35px; overflow:auto;\"";
        // $arg["data"]["CHECK_NAME"] = knjCreateTextArea($objForm, "CHECK_NAME", 2, 21, "soft", $extra, $Row["CHECK_NAME"]);
        $arg["data"]["CHECK_NAME"] = getTextOrArea($objForm, "CHECK_NAME", 10, 2, $Row["CHECK_NAME"]);
        $arg["data"]["CHECK_NAME_SIZE"] = '<font size="2" color="red">(全角10文字X2行まで)</font>';

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm2_2.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model)
{
    $retCnt = 0;
    $query = knje390mQuery::getSubQuery2CheckRecordList($model);
    $result = $db->query($query);
    $centerName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $rowlist["RECORD_DIV_NAME"] = '検査機関'.($retCnt+1);
        $centerName = $rowlist["CHECK_CENTER_TEXT"];
        $rowlist["CENTER_NAME"] = $centerName;
        $checkDate = preg_split("/-/", $rowlist["CHECK_DATE"]);
        $rowlist["CHECK_DATE"] = $checkDate[0].'/'.$checkDate[1];
        
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
    //検査機関マスタ参照
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=check_center_search&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 700, 600)\"";
    $arg["button"]["btn_check_center_search"] = knjCreateBtn($objForm, "btn_check_center_search", "検査機関", $extra.$disabled);

    //検査名マスタ
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=checkname_master&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_checkname"] = knjCreateBtn($objForm, "btn_checkname", "検査名参照", $extra);

    //追加ボタン
    $extra = "onclick=\"return btn_submit('check2_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('check2_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('check2_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform2A');\"");
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

