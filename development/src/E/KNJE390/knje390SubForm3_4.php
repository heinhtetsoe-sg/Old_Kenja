<?php

require_once('for_php7.php');

class knje390SubForm3_4
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3_4", "POST", "knje390index.php", "", "subform3_4");

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
        $info = $db->getRow(knje390Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"].$setHyoujiDate;

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "subform3_work") {
            unset($model->getYear);
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
            
            $model->field3["WORK_REMARK"] = "";
        }
        //労働情報取得
        if ($model->cmd == "subform3_work_set"){
            if (isset($model->schregno) && !isset($model->warning)){
                $Row = $db->getRow(knje390Query::getSubQuery3WorkGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field3;
            }
        } else {
            $Row =& $model->field3;
        }
        
        //1:相談支援、2:アルバイト
        $opt = array(1, 2);
        $Row["RECORD_DIV"] = ($Row["RECORD_DIV"] == "") ? "1" : $Row["RECORD_DIV"];
        $extra = array("id=\"RECORD_DIV1\" onclick=\"return btn_submit('subform3_work')\"", "id=\"RECORD_DIV2\" onclick=\"return btn_submit('subform3_work')\"");
        $radioArray = knjCreateRadio($objForm, "RECORD_DIV", $Row["RECORD_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;        

        //画面切換
        if ($Row["RECORD_DIV"] === '1') {
            $arg["RECORD_DIV1"] = "1";
            $arg["data"]["WORK_REMARK_NAME"] = '連帯内容';
        } else if ($Row["RECORD_DIV"] === '2') {
            $arg["RECORD_DIV2"] = "1";
            $arg["data"]["WORK_REMARK_NAME"] = '仕事内容';
        }

        //事業所
        $query = knje390Query::getAdviceCentercdComboName();
        makeCmb($objForm, $arg, $db, $query, "CENTERCD", $Row["CENTERCD"], "", 1, 1);
        
        //開始年度
        $arg["data"]["S_YEAR_MONTH"] = $my->MyMonthWin2($objForm, "S_YEAR_MONTH", $Row["S_YEAR_MONTH"]);
        
        //担当者
        $extra = "style=\"height:50px; overflow:auto;\"";
        $arg["data"]["STAFF_NAME"] = knjCreateTextArea($objForm, "STAFF_NAME", 2, 41, "soft", $extra, $Row["STAFF_NAME"]);
        $arg["data"]["STAFF_NAME_SIZE"] = '<font size="1" color="red">(全角20文字2行まで)</font>';
        //連帯内容、仕事内容
        $extra = "style=\"height:75px; overflow:auto;\"";
        $arg["data"]["WORK_REMARK"] = knjCreateTextArea($objForm, "WORK_REMARK", 4, 71, "soft", $extra, $Row["WORK_REMARK"]);
        $arg["data"]["WORK_REMARK_SIZE"] = '<font size="1" color="red">(全角35文字4行まで)</font>';

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje390SubForm3_4.html", $arg); 
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    $retCnt = 0;
    $query = knje390Query::getSubQuery3WorkRecordList($model);
    $result = $db->query($query);
    $divcount1 = 1;
    $divcount2 = 1;
    $centerName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $s_ym_array = array();
        $s_ym_array = explode("-", $rowlist["S_YEAR_MONTH"]);
        if ($rowlist["RECORD_DIV"] === '1') {
            $rowlist["RECORD_DIV_NAME"] = '相談支援'.$divcount1;
            //担当者
            $rowlist["CONTENTS_NAIYOU"] = '担当者:'.$rowlist["STAFF_NAME"];
            $divcount1++;
        } else if ($rowlist["RECORD_DIV"] === '2') {
            $rowlist["RECORD_DIV_NAME"] = 'アルバイト'.$divcount2;
            //開始月
            $rowlist["CONTENTS_NAIYOU"] = '開始月:'.$s_ym_array[0].'年'.$s_ym_array[1].'月から';
            $divcount2++;
        }
        $centerName = $db->getOne(knje390Query::getAdviceCentercdName($rowlist["CENTERCD"]));
        $rowlist["CENTER_NAME"] = $centerName;
        
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
    //追加ボタン
    $extra = "onclick=\"return btn_submit('work3_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('work3_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('work3_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform3A');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

