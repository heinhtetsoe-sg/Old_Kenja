<?php

require_once('for_php7.php');
class knje390mSubForm1_2
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
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
        }
        //療育・教育歴情報取得
        if ($model->cmd == "subform1_educate_set") {
            if (isset($model->schregno) && !isset($model->warning)) {
                $Row = $db->getRow(knje390mQuery::getSubQuery1EducateGetData($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }
        
        //学校種別
        $extra = "onchange=\"return btn_submit('subform1_educate')\"";
        $query = knje390mQuery::getEducateRecordDiv();
        makeCmb($objForm, $arg, $db, $query, "RECORD_DIV", $Row["RECORD_DIV"], $extra, 1, 1);

        //画面切換
        if ($Row["RECORD_DIV"] === '1' || $Row["RECORD_DIV"] === '2' || $Row["RECORD_DIV"] === '3' || $Row["RECORD_DIV"] === '8') {
            $arg["RECORD_DIV123"] = "1";
        } elseif ($Row["RECORD_DIV"] === '4' || $Row["RECORD_DIV"] === '5' || $Row["RECORD_DIV"] === '6' || $Row["RECORD_DIV"] === '7') {
            $arg["RECORD_DIV456"] = "1";
        }

        //保育所・幼稚園・学校名等（直接入力）
        $extra = "";
        // $arg["data"]["SCHOOL_NAME"] = knjCreateTextBox($objForm, $Row["SCHOOL_NAME"], "SCHOOL_NAME", 40, 20, $extra);
        $arg["data"]["SCHOOL_NAME"] = getTextOrArea($objForm, "SCHOOL_NAME", 20, 1, $Row["SCHOOL_NAME"]);
        $arg["data"]["SCHOOL_NAME_SIZE"] = '<font size="2" color="red">(全角20文字まで)</font>';

        //開始年月、終了年月
        $arg["data"]["S_YEAR_MONTH"] = $my->MyMonthWin($objForm, "S_YEAR_MONTH", $Row["S_YEAR_MONTH"]);
        $arg["data"]["E_YEAR_MONTH"] = $my->MyMonthWin($objForm, "E_YEAR_MONTH", $Row["E_YEAR_MONTH"]);

        //学部・学年・支援学級在籍等
        $extra = "";
        // $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 50, 25, $extra);
        $arg["data"]["REMARK"] = getTextOrArea($objForm, "REMARK", 25, 1, $Row["REMARK"]);
        $arg["data"]["REMARK_SIZE"] = '<font size="2" color="red">(全角25文字まで)</font>';

        //保育所・幼稚園・学校名等（学校検索）
        $query = knje390mQuery::getSchoolInfo($Row["P_J_SCHOOL_CD"]);
        $schoolRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["P_J_SCHOOL_NAME"] = $schoolRow["FINSCHOOL_NAME"];
        //学校検索
        $setKind = $db->getOne(knje390mQuery::getL019namecd2($Row["RECORD_DIV"]));
        $schoolCdVal = "document.forms[0]['P_J_SCHOOL_CD'].value";
        $arg["data"]["P_J_SCHOOL_CD"] = View::popUpSchoolCd($objForm, "P_J_SCHOOL_CD", $Row["P_J_SCHOOL_CD"], $schoolCdVal, "btn_kensaku", "", "P_J_SCHOOL_CD", "P_J_SCHOOL_NAME", "", "P_J_SCHOOL_RITSU", "", "", $setKind, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm1_2.html", $arg);
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
    $ym_array["S_YEAR_MONTH"] = "s";
    $ym_array["E_YEAR_MONTH"] = "e";

    $retCnt = 0;
    $query = knje390mQuery::getSubQuery1EducateRecordList($model, "");
    $result = $db->query($query);
    $classShubetsuName = "";
    $divcount1 = 1;
    $divcount2 = 1;
    $divcount3 = 1;
    $divcount4 = 1;
    $divcount5 = 1;
    $divcount6 = 1;
    $divcount7 = 1;
    $divcount8 = 1;
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //和暦表記
        $sWareki = $eWareki = "";
        foreach ($ym_array as $ymKey => $ymDiv) {
            $name = $ymDiv."Wareki";
            $$name = getWareki($rowlist, $arrayL007, $ymKey);
        }

        //在籍期間
        $addmsg = ($rowlist["S_YEAR_MONTH"] || $rowlist["E_YEAR_MONTH"]) ? "～" : "";
        $rowlist["S_E_YEAR_MONTH"] = $sWareki.$addmsg.$eWareki;
        //学校の種別
        if ($rowlist["RECORD_DIV"] === '1') {
            $rowlist["RECORD_DIV_NAME"] = '保育園'.$divcount1;
            $divcount1++;
        } elseif ($rowlist["RECORD_DIV"] === '2') {
            $rowlist["RECORD_DIV_NAME"] = '幼稚園'.$divcount2;
            $divcount2++;
        } elseif ($rowlist["RECORD_DIV"] === '3') {
            $rowlist["RECORD_DIV_NAME"] = '療育施設等'.$divcount3;
            $divcount3++;
        } elseif ($rowlist["RECORD_DIV"] === '4') {
            $rowlist["RECORD_DIV_NAME"] = '小学校'.$divcount4;
            $divcount4++;
        } elseif ($rowlist["RECORD_DIV"] === '5') {
            $rowlist["RECORD_DIV_NAME"] = '中学校'.$divcount5;
            $divcount5++;
        } elseif ($rowlist["RECORD_DIV"] === '6') {
            $rowlist["RECORD_DIV_NAME"] = '特別支援学校'.$divcount6;
            $divcount6++;
        } elseif ($rowlist["RECORD_DIV"] === '7') {
            $rowlist["RECORD_DIV_NAME"] = '高校'.$divcount7;
            $divcount7++;
        } elseif ($rowlist["RECORD_DIV"] === '8') {
            $rowlist["RECORD_DIV_NAME"] = '大学'.$divcount8;
            $divcount8++;
        }
        //保育所・幼稚園・学校名等（学校検索）
        if ($rowlist["RECORD_DIV"] === '4' || $rowlist["RECORD_DIV"] == '5' || $rowlist["RECORD_DIV"] == '6' || $rowlist["RECORD_DIV"] == '7') {
            $query = knje390mQuery::getSchoolInfo($rowlist["P_J_SCHOOL_CD"]);
            $schoolRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $rowlist["SCHOOL_NAME"] = $schoolRow["FINSCHOOL_NAME"];
        }

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
    $extra = "onclick=\"return btn_submit('educate1_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('educate1_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('educate1_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform1A');\"");
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
    knjCreateHidden($objForm, "useFinschoolcdFieldSize", $model->Properties["useFinschoolcdFieldSize"]);
}

//和暦取得
function getWareki(&$Row, $arrayL007, $name)
{
    $wareki = "";
    if ($Row[$name]) {
        list($y, $m) =  explode("-", $Row[$name]);
        if (is_numeric($y) && is_numeric($m)) {
            $d = date('t', mktime(0, 0, 0, $m, 1, $y));
            $date = $y."/".$m."/".$d;   //基準日
    
            $nen = "";
            foreach ($arrayL007 as $key => $val) {
                if ($val["NAMESPARE2"] <= $date && $date <= $val["NAMESPARE3"]) {
                    $nen = $y - $val["NAMESPARE1"] + 1;
                    if ($nen == 1) {
                        $nen = "元";
                    }
                    //和暦
                    $wareki = $val["NAME1"].$nen.'年'.$m.'月';
                }
            }
        }
    }
    return $wareki;
}
