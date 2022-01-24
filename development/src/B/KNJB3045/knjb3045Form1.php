<?php

require_once('for_php7.php');

class knjb3045Form1
{
    function main(&$model)
    {

        $objForm = new form;
        // フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjb3045index.php", "", "main");
        // 権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        // DB接続
        $db = Query::dbCheckOut();

        $arg["YEAR"] = CTRL_YEAR;
        // 年度学期コンボ
        $query = knjb3045Query::getYearSemester($model);
        $extra = "onchange=\"return btn_submit('')\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR_SEME", $model->field["YEAR_SEME"], $extra, 1);

        $weekJp = array('日', '月', '火', '水', '木', '金', '土');
        // 曜日
        $weekList = array();
        $weekList[] = array('class' => 'week_mon', 'value' => '2', 'label' => '月');
        $weekList[] = array('class' => 'week_tue', 'value' => '3', 'label' => '火');
        $weekList[] = array('class' => 'week_wed', 'value' => '4', 'label' => '水');
        $weekList[] = array('class' => 'week_thu', 'value' => '5', 'label' => '木');
        $weekList[] = array('class' => 'week_fri', 'value' => '6', 'label' => '金');
        $weekList[] = array('class' => 'week_sat', 'value' => '7', 'label' => '土');
        $weekList[] = array('class' => 'week_sun', 'value' => '1', 'label' => '日');

        $preSeqData = array();
        if ($model->field['PRESEQDATA']) {
            $preSeqData = json_decode($model->field['PRESEQDATA'], true);
        }

        if ($model->cmd == 'main' || $model->cmd == 'prevRead') {
            // 前回値の読込
            $prevBscSeq = $model->field['BSCSEQ'];
            if ($model->field['PREV_BSCSEQ']) {
                $prevBscSeq = $model->field['PREV_BSCSEQ'];
            }
            $preSeqData = array();
            $query = knjb3045Query::getSchPtrnPreChaToBasicDat($model, $prevBscSeq);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $key = $row["DAYCD"].'_'.$row["PERIODCD"];
                $value = !is_null($row["PRE_ORDER"]) ? $row["PRE_ORDER"] + 1 : '';
                $model->field['PRESEQ'] = sprintf('%02d', $row["PRESEQ"]);
                $preSeqData[$key] = $value;
            }
            $result->free();
        }

        // 講座展開表SEQコンボ
        $opt = array();
        // $opt[] = array("value" => "", "label" => "");
        $query = knjb3045Query::getSchPtrnPreChrHdat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dateTime = explode('.', $row["UPDATED"]);
            list($setDate, $setTime) = explode(' ', $dateTime[0]);
            $setWeek = $weekJp[date('w', strtotime($row["UPDATED"]))];
            $dispDate = str_replace("-", "/", $setDate)."({$setWeek}) {$setTime}";
            $row["PRESEQ"] = sprintf('%02d', $row["PRESEQ"]);
            $opt[] = array('value' => $row["PRESEQ"], 'label' => "{$row["PRESEQ"]} {$dispDate} {$row["TITLE"]}");
        }
        $result->free();
        $model->field['PRESEQ'] = strlen($model->field['PRESEQ']) > 0 ? $model->field['PRESEQ'] : $opt[0]["value"];
        $extra = "id=\"PRESEQ\" onChange=\"btn_submit('')\"";
        $arg['data']['PRESEQ'] = knjCreateCombo($objForm, 'PRESEQ', $model->field['PRESEQ'], $opt, $extra, $size);

        // 基本時間割SEQコンボ
        $opt = array();
        $opt[] = array("value" => "", "label" => "（新　規）");
        $titleList = array();
        $query = knjb3045Query::getSchPtrnHdat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dateTime = explode('.', $row["UPDATED"]);
            list($setDate, $setTime) = explode(' ', $dateTime[0]);
            $setWeek = $weekJp[date('w', strtotime($row["UPDATED"]))];
            $dispDate = str_replace("-", "/", $setDate)."({$setWeek}) {$setTime}";
            $row["BSCSEQ"] = sprintf('%02d', $row["BSCSEQ"]);
            $opt[] = array('value' => $row["BSCSEQ"], 'label' => "{$row["BSCSEQ"]} {$dispDate} {$row["TITLE"]}");
            $titleList[$row["BSCSEQ"]] = $row["TITLE"];
        }
        $result->free();
        $model->field['BSCSEQ'] = strlen($model->field['BSCSEQ']) > 0 ? $model->field['BSCSEQ'] : $opt[0]["value"];
        $extra = "id=\"BSCSEQ\" onChange=\"btn_submit('main')\"";
        $arg['data']['BSCSEQ'] = knjCreateCombo($objForm, 'BSCSEQ', $model->field['BSCSEQ'], $opt, $extra, $size);

        $model->chairCnt = 0;
        // 基本時間割が選択されてい部場合、基本時間割に設定されている講座数を取得
        if ($model->field['BSCSEQ']) {
            $query = knjb3045Query::getSchPtrnChrCnt($model);
            $model->chairCnt = $db->getOne($query);
        }
        $model->field['BSCTITLE'] = isset($titleList[$model->field['BSCSEQ']]) ? $titleList[$model->field['BSCSEQ']] : '';
        $arg['data']['BSCTITLE'] = knjCreateTextBox($objForm, $model->field['BSCTITLE'], 'BSCTITLE', 40, 20, "");

        // 校時
        $periodList = array();
        $unAttach = "";
        $sep = "";
        $query = knjb3045Query::getPeriod($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $periodList[] = array('value' => $row["VALUE"], 'label' => $row["LABEL"]);
            if ($row["UNATTACH"]) {
                $unAttach .= $sep.$row["UNATTACH"];
                $sep = ",";
            }
        }
        $result->free();
        $arg['data']['PERIOD'] = $periodList;
        $arg['data']['PERIOD_COUNT'] = get_count($periodList);
        knjCreateHidden($objForm, "UNATTACH", $unAttach);

        $query = knjb3045Query::getDefaultWeek($model);
        $result = $db->query($query);
        $checkedWeek = array();
        while ($defWeekRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $checkedWeek[] = $defWeekRow["VALUE"];
        }
        $result->free();
        if (get_count($checkedWeek) == 0) {
            $checkedWeek = array('2', '3', '4', '5', '6', '', '');
        }

        $isSetSeqList = array();
        // SEQの作成(曜日毎に校時分のSEQを作成)
        $seq = 1;
        for ($i=0; $i < get_count($weekList); $i++) { 
            $week = $weekList[$i];

            // タイトル部のチェック用
            $extra = "";
            if (in_array($week['value'], $checkedWeek)) $extra .= " checked ";
            $weekList[$i]["weekCheck"] = knjCreateCheckBox($objForm, 'weekCheck', $week['value'], $extra);


            // SEQ連番初期化
            $disabled = '';
            $extra = "onclick=\"seqInit('{$week['value']}');\"";
            $weekList[$i]["weekSeqInit"] = knjCreateBtn($objForm, "weekSeqInit", "列番号初期化", $disabled.$extra);

            $ptrnPre = array();
            for ($j=0; $j < get_count($periodList); $j++) { 
                $period = $periodList[$j];
                $key = $week['value'].'_'.$period['value'];
                if (isset($preSeqData[$key])) {
                    $ptrnPre[] = array('value' => $key, 'label' => $preSeqData[$key]);
                    if ($preSeqData[$key]) {
                        $isSetSeqList[] = $preSeqData[$key];
                    }
                } else {
                    $ptrnPre[] = array('value' => $key, 'label' => '');
                }
                $seq++;
            }
            $weekList[$i]['PTRN_PRE'] = $ptrnPre;
        }
        $arg['data']['WEEK'] = $weekList;

        // 単位マスタのコース毎の単位数の最大値を取得
        $query = knjb3045Query::getCreditsMax($model);
        $row= $db->getRow($query, DB_FETCHMODE_ASSOC);
        $subclassMaxCnt = $row['CREDITS_MAX'] + 5;
        if ($seq < $subclassMaxCnt) {
            $creditsSeqList = array();
            for ($i=1; $i <= $subclassMaxCnt; $i++) {
                if (!in_array($i, $isSetSeqList)) {
                    $key = 'NONE'.'_'.$i;
                    if (isset($preSeqData[$key])) {
                        $creditsSeqList[] = array('value' => $key, 'label' => $preSeqData[$key]);
                    } else {
                        $creditsSeqList[] = array('value' => $key, 'label' => $i);
                    }
                }
            }
            $arg['data']['CREDITS'] = $creditsSeqList;
        }
        // 未設定SEQ入替
        $extra = "onclick=\"btn_seqSwap();\"";
        $arg["btn_swap"] = knjCreateBtn($objForm, "btn_swap", "入替え", $extra);

        // 前回保存値読込
        $prevData = false;
        $query = knjb3045Query::getSchPtrnPreChaToBasicDat($model, $prevBscSeq);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $prevData = true;
            break;
        }
        $disabled = '';
        // 前回保存値がない場合、ボタンは使用不可
        if (!$prevData) {
            $disabled = ' disabled ';
        }
        $extra = $disabled ." onclick=\"loadwindow('./knjb3045index.php?cmd=selectBasic&YEAR={$model->year}&SEMESTER={$model->semester}&',0,document.documentElement.scrollTop || document.body.scrollTop,600,450);return;\"";
        $arg["btn_prevRead"] = knjCreateBtn($objForm, "btn_prevRead", "保存値読込", $disabled.$extra);

        // 展開表印刷
        $disabled = ' disabled ';
        $extra = "onclick=\"btn_submit('print');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_update", "展開表印刷", $disabled.$extra);

        // SEQ連番初期化
        $disabled = '';
        $extra = "onclick=\"seqInitChecked();\"";
        $arg["btn_seqInit"] = knjCreateBtn($objForm, "btn_seqInit", "列番号初期化", $disabled.$extra);
        // SEQ連番設定
        $disabled = '';
        $extra = "onclick=\"seqAttachChecked();\"";
        $arg["btn_seqAttach"] = knjCreateBtn($objForm, "btn_seqAttach", "未設定の列番号設定", $disabled.$extra);

        // 実行ボタン
        $disabled = '';
        $extra = "onclick=\"btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "登 録", $disabled.$extra);

        // 終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["IFRAME"] = VIEW::setIframeJs();
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjb3045Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array('value' => "", 'label' => "全　て");
    }
    if ($blank == "BLANK") {
        $opt[] = array('value' => "", 'label' => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('value' => $row["VALUE"], 'label' => $row["LABEL"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($value && $value_flg) {
        $value = $value;
    } else {
        $value = $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

// hidden項目作成
function makeHidden(&$objForm, $model) {

    //hiddenを作成する
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectCell");

    knjCreateHidden($objForm, "PRESEQDATA", $model->field['PRESEQDATA']);
    knjCreateHidden($objForm, "CHAIR_CNT", $model->chairCnt);
    // 前回保存値読込用
    knjCreateHidden($objForm, "PREV_BSCSEQ");
}

?>
