<?php

require_once('for_php7.php');

class knje390nSubForm3_6
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3_6", "POST", "knje390nindex.php", "", "subform3_6");

        //DB接続
        $db = Query::dbCheckOut();
        //カレンダー呼び出し
        $my = new mycalendar();

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
        $info = $db->getRow(knje390nQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"].$setHyoujiDate;

        /************/
        /* 履歴一覧 */
        /************/
        //目指す将来像、長期目標(年間)と評価、学校短期目標、部活動の記録の切換
        $extra = "onchange=\"return btn_submit('subform3_school')\"";
        $query = knje390nQuery::getSupportSchoolRecordDiv();
        makeCmb($objForm, $arg, $db, $query, "RECORD_DIV", $model->field3["RECORD_DIV"], $extra, 1, "");
        
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "subform3_school") {
            unset($model->getYear);
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
        }
        if ($model->field3["RECORD_DIV"] !== '1') {
            $arg["RIREKI"] = "1";
            //学校情報取得
            if ($model->cmd == "subform3_school_set"){
                if (isset($model->schregno) && !isset($model->warning)){
                    $Row = $db->getRow(knje390nQuery::getSubQuery3SchoolGetData($model, $model->field3["RECORD_DIV"]), DB_FETCHMODE_ASSOC);
                } else {
                    $Row =& $model->field3;
                }
            } else {
                $Row =& $model->field3;
            }
            
            //短期の目標、支援・手立て、評価を取得(RECORD_DIV="2")
            if ($model->field3["RECORD_DIV"] === '2') {
                $arg["RECORD_DIV2"] = "1";
                $extra = "style=\"height:90px; overflow:auto;\"";
                //短期の目標
                $arg["data"]["SHORT_TERM_GOAL"] = knjCreateTextArea($objForm, "SHORT_TERM_GOAL", 7, 21, "soft", $extra, $Row["SHORT_TERM_GOAL"]);
                $arg["data"]["SHORT_TERM_GOAL_SIZE"] = '<font size="1" color="red">(全角10文字7行まで)</font>';
                //短期の支援・手立て
                $arg["data"]["SHORT_TERM_MEANS"] = knjCreateTextArea($objForm, "SHORT_TERM_MEANS", 7, 29, "soft", $extra, $Row["SHORT_TERM_MEANS"]);
                $arg["data"]["SHORT_TERM_MEANS_SIZE"] = '<font size="1" color="red">(全角14文字7行まで)</font>';
                //短期の評価
                $arg["data"]["SHORT_TERM_ASSESS"] = knjCreateTextArea($objForm, "SHORT_TERM_ASSESS", 7, 29, "soft", $extra, $Row["SHORT_TERM_ASSESS"]);
                $arg["data"]["SHORT_TERM_ASSESS_SIZE"] = '<font size="1" color="red">(全角14文字7行まで)</font>';
            
            //部活動の記録を取得(RECORD_DIV="3")
            } else if ($model->field3["RECORD_DIV"] === '3') {
                $arg["RECORD_DIV3"] = "1";
                //クラブ名
                $extra = "style=\"height:40px; overflow:auto;\"";
                $arg["data"]["CLUB_NAME"] = knjCreateTextArea($objForm, "CLUB_NAME", 2, 21, "soft", $extra, $Row["CLUB_NAME"]);
                $arg["data"]["CLUB_NAME_SIZE"] = '<font size="1" color="red">(全角10文字2行まで)</font>';
                //開始年
                $arg["data"]["CLUB_S_YEAR_MONTH"] = $my->MyMonthWin2($objForm, "CLUB_S_YEAR_MONTH", $Row["CLUB_S_YEAR_MONTH"]);
            }
        } else {
            $arg["RECORD_DIV1"] = "1";
            //目指す将来像、長期目標、評価を取得(RECORD_DIV="1" RECORD_SEQ="1"のみ)
            if (isset($model->schregno) && !isset($model->warning)){
                $Row2 = $db->getRow(knje390nQuery::getSubQuery3SchoolGetData($model, "1"), DB_FETCHMODE_ASSOC);
            } else {
                $Row2 =& $model->field3;
            }
            //将来像
            $extra = "style=\"height:75px; overflow:auto;\"";
            $arg["data"]["GOAL_FUTURE"] = knjCreateTextArea($objForm, "GOAL_FUTURE", 5, 71, "soft", $extra, $Row2["GOAL_FUTURE"]);
            $arg["data"]["GOAL_FUTURE_SIZE"] = '<font size="1" color="red">(全角35文字5行まで)</font>';
            //長期目標
            $arg["data"]["LONG_TERM_GOAL"] = knjCreateTextArea($objForm, "LONG_TERM_GOAL", 5, 29, "soft", $extra, $Row2["LONG_TERM_GOAL"]);
            $arg["data"]["LONG_TERM_GOAL_SIZE"] = '<font size="1" color="red">(全角14文字5行まで)</font>';
            //取得
            $arg["data"]["LONG_TERM_ASSESS"] = knjCreateTextArea($objForm, "LONG_TERM_ASSESS", 5, 31, "soft", $extra, $Row2["LONG_TERM_ASSESS"]);
            $arg["data"]["LONG_TERM_ASSESS_SIZE"] = '<font size="1" color="red">(全角15文字5行まで)</font>';
        }
        
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
        View::toHTML($model, "knje390nSubForm3_6.html", $arg); 
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    $retCnt = 0;
    $query = knje390nQuery::getSubQuery3SchoolRecordList($model);
    $result = $db->query($query);
    $classShubetsuName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $s_ym_array = array();
        $s_ym_array = explode("-", $rowlist["CLUB_S_YEAR_MONTH"]);
        if ($model->field3["RECORD_DIV"] === '2') {
            $rowlist["RECORD_DIV_NAME"] = '短期項目'.($retCnt+1);
            $rowlist["CONTENTS_NAIYOU"] = '目標:'.substr($rowlist["SHORT_TERM_GOAL"], 0, 110);;
        } else if ($model->field3["RECORD_DIV"] === '3') {
            $rowlist["RECORD_DIV_NAME"] = 'クラブ'.($retCnt+1);
            $rowlist["CONTENTS_NAIYOU"] = '開始月:'.$s_ym_array[0].'年'.$s_ym_array[1].'月から';
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
function makeBtn(&$objForm, &$arg, $model)
{
    //D様式参照ボタン
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390N/knje390nindex.php?cmd=subform2_growup_B_sanshou&MAIN_YEAR=".$model->main_year."&SCHREGNO=".$model->schregno."&RECORD_DATE=".$model->record_date."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_Dsanshou"] = KnjCreateBtn($objForm, "btn_Dsanshou", "D様式課題・つけたい力参照", $extra.$disabled);

    //短期の目標、支援・手立て、評価　または　部活動の記録
    //追加ボタン
    $extra = "onclick=\"return btn_submit('school3_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra.$disabled);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('school3_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('school3_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra.$disabled);
    
    //目指す将来像、長期目標、評価
    //更新ボタン
    $extra = "onclick=\"return btn_submit('school3_update2');\"";
    $arg["button"]["btn_update2"] = knjCreateBtn($objForm, "btn_update2", "更 新", $extra.$disabled);
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

