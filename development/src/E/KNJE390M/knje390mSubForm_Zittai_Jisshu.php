<?php

require_once('for_php7.php');

class knje390mSubForm_Zittai_Jisshu
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1_2", "POST", "knje390mindex.php", "", "subform1_2");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

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
        if ($model->cmd == "subform1_educate") {
            unset($model->getYear);
            unset($model->getRecordSeq);
        }
        //療育・教育歴情報取得
        if ($model->cmd == "subformZittaiJisshu_set") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row = $db->getRow(knje390mQuery::getSubQuery1JisshuGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field9;
            }
        } else {
            $Row =& $model->field9;
        }

        //実習先（直接入力）
        $extra = "";
        $arg["data"]["COMPANY_NAME"] = knjCreateTextBox($objForm, $Row["COMPANY_NAME"], "COMPANY_NAME", 90, 90, $extra);
        $arg["data"]["NAME_SIZE"] = '<font size="2" color="red">(全角45文字まで)</font>';

        //開始年月、終了年月
        $arg["data"]["START_DATE"] = View::popUpCalendar($objForm, "START_DATE", str_replace("-", "/", $Row["START_DATE"]));
        $arg["data"]["FINISH_DATE"] = View::popUpCalendar($objForm, "FINISH_DATE", str_replace("-", "/", $Row["FINISH_DATE"]));

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 90, 90, $extra);
        $arg["data"]["REMARK_SIZE"] = '<font size="2" color="red">(全角45文字まで)</font>';

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm_Zittai_Jisshu.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model)
{
    //年号取得
    $arrayL007 = array();
    $query = knje390mQuery::getNameMstL007();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $arrayL007[] = $row;
    }
    $result->free();

    //和暦対象項目
    $ym_array = array();
    $ym_array["START_DATE"] = "s";
    $ym_array["FINISH_DATE"] = "e";

    $retCnt = 0;
    $query = knje390mQuery::getZittaiJisshuRecordList($model);
    $result = $db->query($query);
    $classShubetsuName = "";
    $divcount1 = 1;
    $model->startDateList = "";
    $sep = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //開始日付の保持(データ登録時の重複チェックに使用)
        $model->startDateList .= $sep.$rowlist["START_DATE"];
        $sep = ",";
        //和暦表記
        $sWareki = $eWareki = "";
        foreach ($ym_array as $ymKey => $ymDiv) {
            $name = $ymDiv."Wareki";
            $$name = getWareki($rowlist, $arrayL007, $ymKey);
        }

        //在籍期間
        $addmsg = ($rowlist["START_DATE"] || $rowlist["FINISH_DATE"]) ? "～" : "";
        $rowlist["S_E_YEAR_MONTH"] = $sWareki.$addmsg.$eWareki;

        //項目名
        $rowlist["RECORD_DIV_NAME"] = '実習先'.$divcount1;
        $divcount1++;

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
    //追加ボタン
    $extra = "onclick=\"return btn_submit('subformZittaiJisshu_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('subformZittaiJisshu_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('subformZittaiJisshu_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subformZittaiA');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useFinschoolcdFieldSize", $model->Properties["useFinschoolcdFieldSize"]);
    knjCreateHidden($objForm, "startDateList", $model->startDateList); //開始日付の保持(データ登録時のチェックに使用)
}

//和暦取得
function getWareki(&$Row, $arrayL007, $name)
{
    $wareki = "";
    if ($Row[$name]) {
        list($y, $m, $d) =  explode("-", $Row[$name]);
        $date = $y."/".$m."/".$d;   //基準日

        $nen = "";
        foreach ($arrayL007 as $key => $val) {
            if ($val["NAMESPARE2"] <= $date && $date <= $val["NAMESPARE3"]) {
                $nen = $y - $val["NAMESPARE1"] + 1;
                if ($nen == 1) {
                    $nen = "元";
                }
                //和暦
                $wareki = $val["NAME1"].$nen.'年'.$m.'月'.$d.'日';
            }
        }
    }
    return $wareki;
}
