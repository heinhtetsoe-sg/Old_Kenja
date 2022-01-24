<?php

require_once('for_php7.php');

class knjf303Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjf303index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //委員会有無
        $query = knjf303Query::getUseIinkai();
        $model->isIinkai = $db->getOne($query);

        //処理年度
        $query = knjf303Query::getYear();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1, "");

        //タイトル
        $arg["TITLE"] = "出席停止・欠席・登校入力画面";

        //教育委員会用学校コード取得
        $model->schoolcd = $db->getOne(knjf303Query::getSchoolcd($model));
        $model->schoolcd = $model->schoolcd ? $model->schoolcd : "000000000000";

        //県への報告用作成日(テーブルは報告履歴テーブルのみ)
        $arg["data"]["EXECUTE_DATE"] = View::popUpCalendar($objForm, "EXECUTE_DATE", str_replace("-","/",$model->field["EXECUTE_DATE"]),"");
        
        //指定年度開始日、終了日セット
        $model->setyear = (int)$model->year + 1;
        knjCreateHidden($objForm, "SEM_SDATE", $model->year.'/04/01');
        knjCreateHidden($objForm, "SEM_EDATE", $model->setyear.'/03/31');
        
        //新規のときはフィールドをNULLセット
        $data_div = ($model->data_div == '0_new') ? "" : explode(":", $model->data_div);
        if ($model->data_div != '0_new' && $model->cmd != 'change'  && str_replace("-","/",$data_div[0]) != str_replace("-","/",$model->suspend_direct_date)) {
            $model->data_div = "";
            unset($model->field);
        }

        //新規/作成済みの修正コンボ
        $query = knjf303Query::getDataRireki($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "DATA_DIV", $model->data_div, $extra, 1, "");
        if ($model->data_div == '0_new' && $model->cmd == 'change') unset($model->field);

        //出席停止を指示した日
        $data_div = ($model->data_div == '0_new') ? "" : explode(":", $model->data_div);
        $model->suspend_direct_date = ($model->data_div == '0_new' && $model->cmd == 'change') ? "" : (($model->data_div != '0_new' && $model->cmd == 'change') ? $data_div[0] : $model->suspend_direct_date);
        if ($model->data_div == '0_new') {
            $arg["data"]["SUSPEND_DIRECT_DATE"] = View::popUpCalendar2($objForm, "SUSPEND_DIRECT_DATE", str_replace("-","/",$model->suspend_direct_date), "reload=true", " btn_submit('main')");
        } else {
            $arg["data"]["SUSPEND_DIRECT_DATE"] = str_replace("-","/",$model->suspend_direct_date);
            $set_suspend_direct_date = str_replace("-","/",$model->suspend_direct_date);
            knjCreateHidden($objForm, "SUSPEND_DIRECT_DATE", $set_suspend_direct_date);
        }

        //データの取得
        if (($model->data_div !== '0_new' && $model->data_div != "" ) && ($model->cmd === 'change' || $model->cmd === 'back')) {
            $Row = $db->getRow(knjf303Query::getAdditionData($model), DB_FETCHMODE_ASSOC);
            if (!$Row) {
                unset($model->field);
            }
        } else if ($model->data_div === '0_new' && $model->cmd === 'change') {
            unset($model->field);
        } else {
            $Row =& $model->field;
        }

        //集計区分
        $opt = array(1, 2, 3);
        $Row["TOTAL_DIV"] = ($Row["TOTAL_DIV"] == "") ? "1" : $Row["TOTAL_DIV"];
        $disabled = ($model->data_div == '0_new') ? "" : " disabled";
        $click = " onclick=\"return btn_submit('main');\"";
        $extra = array("id=\"TOTAL_DIV1\"".$click.$disabled, "id=\"TOTAL_DIV2\"".$click.$disabled, "id=\"TOTAL_DIV3\"".$click.$disabled);
        $radioArray = knjCreateRadio($objForm, "TOTAL_DIV", $Row["TOTAL_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        if ($model->data_div != '0_new') knjCreateHidden($objForm, "TOTAL_DIV", $Row["TOTAL_DIV"]);


        //理由（疾患名）
        $query = knjf303Query::getDiseasecd($model);
        $extra = ($model->data_div == '0_new') ? "onchange=\"return btn_submit('main');\"" : " disabled";
        makeCmb($objForm, $arg, $db, $query, "DISEASECD", $Row["DISEASECD"], $extra, 1, 1);
        if ($model->data_div != '0_new') knjCreateHidden($objForm, "DISEASECD", $Row["DISEASECD"]);

        //理由（疾患名）その他 (フィールド名が不明)
        if ($Row["DISEASECD"] !== '008' && $Row["DISEASECD"] !== '019') {
            $extra = "readonly style=\"background-color:lightgray;\"";
        } else {
            $extra = "";
        }
        $arg["data"]["DISEASECD_REMARK"] = knjCreateTextBox($objForm, $Row["DISEASECD_REMARK"], "DISEASECD_REMARK", 30, 30, $extra);

        //出席停止期間
        $Row["SUSPEND_S_DATE"] = $model->suspend_direct_date;
        $arg["data"]["SUSPEND_S_DATE"] = str_replace("-","/",$model->suspend_direct_date);
        $arg["data"]["SUSPEND_E_DATE"] = View::popUpCalendar($objForm, "SUSPEND_E_DATE", str_replace("-","/",$Row["SUSPEND_E_DATE"]),"");

        //備考
        $extra = "";
        $arg["data"]["SUSPEND_REMARK"] = KnjCreateTextArea($objForm, "SUSPEND_REMARK", 2, 51, "soft", $extra, $Row["SUSPEND_REMARK"]);

        //備考1(学校医の意見)
        $extra = "";
        $arg["data"]["REMARK1"] = KnjCreateTextArea($objForm, "REMARK1", 2, 51, "soft", $extra, $Row["REMARK1"]);
        
        //備考2(今後の処置)
        $extra = "";
        $arg["data"]["REMARK2"] = KnjCreateTextArea($objForm, "REMARK2", 2, 51, "soft", $extra, $Row["REMARK2"]);
        
        //備考3(その他)
        $extra = "";
        $arg["data"]["REMARK3"] = KnjCreateTextArea($objForm, "REMARK3", 2, 51, "soft", $extra, $Row["REMARK3"]);

        if ($model->isIinkai > 0) {
            //報告履歴
            $query = knjf303Query::getReport($model, $Row);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "REPORT", $model->field["REPORT"], $extra, 1, 1);
        }

        //以下、クラス別の集計登録画面
        //初期化
        $model->data = array();
        $counter = 0;
        //一覧表示
        if ($model->year !== "" && $Row["SUSPEND_DIRECT_DATE"] !== "" && $Row["DISEASECD"] !== "" ) {
            $result = $db->query(knjf303Query::selectQuery($model, $Row));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //GRADE+HR_CLASSコードを配列で取得
                $model->data["GRADE_HR_CLASS"][] = $row["GRADE"].'-'.$row["HR_CLASS"];
                
                $model->fields["TEISHI_COUNT"][$counter]    = $row["TEISHI_COUNT"];
                $model->fields["KESSEKI_COUNT"][$counter]   = $row["KESSEKI_COUNT"];
                $model->fields["TOUKOU_COUNT"][$counter]    = $row["TOUKOU_COUNT"];
                
                //出席停止
                if ($Row["TOTAL_DIV"] === '1' && $model->data_div != '0_new') {
                    $extra = "onblur=\"this.value=toInteger(this.value)\" style=\"text-align:right\"";
                    $row["TEISHI_COUNT"] = knjCreateTextBox($objForm, $row["TEISHI_COUNT"], "TEISHI_COUNT-".$counter, 2, 2, $extra);
                }
                
                //欠席
                if ($Row["TOTAL_DIV"] === '2' && $model->data_div != '0_new') {
                    $extra = "onblur=\"this.value=toInteger(this.value)\" style=\"text-align:right\"";
                    $row["KESSEKI_COUNT"] = knjCreateTextBox($objForm, $row["KESSEKI_COUNT"], "KESSEKI_COUNT-".$counter, 2, 2, $extra);
                }
                
                //登校
                if ($Row["TOTAL_DIV"] === '3' && $model->data_div != '0_new') {
                    $extra = "onblur=\"this.value=toInteger(this.value)\" style=\"text-align:right\"";
                    $row["TOUKOU_COUNT"] = knjCreateTextBox($objForm, $row["TOUKOU_COUNT"], "TOUKOU_COUNT-".$counter, 2, 2, $extra);
                }

                $counter++;            
                
                $arg["data2"][] = $row;

            }
            $result->free();
        }
        
        //ボタン作成
        makeBtn($objForm, $arg, $model, $Row);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJF303");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf303Form1.html", $arg);
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

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model, &$Row) {

    if ($model->isIinkai > 0) {
        //県への報告
        $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
        $extra = "onclick=\"return btn_submit('houkoku');\"";
        $arg["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告／PDFプレビュー", $extra.$disabled);
    }

    //設定ボタンを作成する
    $link = REQUESTROOT."/F/KNJF303_SCHREG/knjf303_schregindex.php?cmd=&SEND_PRGRID=KNJF303&SEND_EDBOARD_SCHOOLCD={$model->schoolcd}&SEND_YEAR={$model->year}&SEND_DATA_DIV={$model->data_div}";
    $extra = ($model->data_div != "" && $model->data_div != '0_new') ? "onclick=\"document.location.href='$link';\"" : "disabled";
    $arg["btn_settei"] = knjCreateBtn($objForm, "btn_settei", "クラス別生徒登録", $extra);

    //更新ボタンを作成する
    $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //印刷
    $extra = "onclick=\"newwin('".SERVLET_URL."');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
}
?>
