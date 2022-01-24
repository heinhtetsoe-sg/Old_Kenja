<?php

require_once('for_php7.php');

class knjf305Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjf305index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        //委員会有無
        $query = knjf305Query::getUseIinkai();
        $model->isIinkai = $db->getOne($query);

        //処理年度
        $query = knjf305Query::getYear();
        $extra = "onchange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1, "");

        //タイトル
        $arg["TITLE"] = "休業措置入力画面";

        //教育委員会用学校コード取得
        $model->schoolcd = $db->getOne(knjf305Query::getSchoolcd($model));
        $model->schoolcd = $model->schoolcd ? $model->schoolcd : "000000000000";

        //県への報告用作成日(テーブルは報告履歴テーブルのみ)
        $arg["data"]["EXECUTE_DATE"] = View::popUpCalendar($objForm, "EXECUTE_DATE", str_replace("-","/",$model->field["EXECUTE_DATE"]),"");

        //新規/作成済みの修正コンボ
        $query = knjf305Query::getDataRireki($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, "DATA_DIV", $model->data_div, $extra, 1, "");

        //措置開始日
        if ($model->data_div == '0_new') {
            $arg["data"]["ACTION_S_DATE"] = View::popUpCalendar2($objForm, "ACTION_S_DATE", str_replace("-","/",$model->field["ACTION_S_DATE"]), "reload=true", " btn_submit('main')");
        } else {
            $arg["data"]["ACTION_S_DATE"] = str_replace("-","/",$model->field["ACTION_S_DATE"]);
            knjCreateHidden($objForm, "ACTION_S_DATE", str_replace("-","/",$model->field["ACTION_S_DATE"]));
        }

        //理由（疾患名）
        $query = knjf305Query::getDiseasecd($model);
        $extra = ($model->data_div == '0_new') ? "onchange=\"return btn_submit('main');\"" : " disabled";
        makeCmb($objForm, $arg, $db, $query, "DISEASECD", $model->field["DISEASECD"], $extra, 1, 1);
        if ($model->data_div != '0_new') knjCreateHidden($objForm, "DISEASECD", $model->field["DISEASECD"]);

        //閉鎖区分
        $opt = array(1, 2, 3);
        $model->field["HEISA_DIV"] = ($model->field["HEISA_DIV"] == "") ? "1" : $model->field["HEISA_DIV"];
        $disabled = ($model->data_div == '0_new') ? "" : " disabled";
        $click = " onclick=\"return btn_submit('main');\"";
        $extra = array("id=\"HEISA_DIV1\"".$click.$disabled, "id=\"HEISA_DIV2\"".$click.$disabled, "id=\"HEISA_DIV3\"".$click.$disabled);
        $radioArray = knjCreateRadio($objForm, "HEISA_DIV", $model->field["HEISA_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        if ($model->data_div != '0_new') knjCreateHidden($objForm, "HEISA_DIV", $model->field["HEISA_DIV"]);
        //hidden
        knjCreateHidden($objForm, "HEISA_DIV_VALUE", $model->field["HEISA_DIV"]);

        //学年コンボ作成
        if ($model->field["HEISA_DIV"] === '1' || $model->field["HEISA_DIV"] === '2') {
            $query = knjf305Query::getGrade($model);
            $extra = ($model->data_div == '0_new') ? "onchange=\"return btn_submit('main');\"" : " disabled";
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, 1);
            if ($model->data_div != '0_new') knjCreateHidden($objForm, "GRADE", $model->field["GRADE"]);
            if ($model->field["HEISA_DIV"] === '1') {
                $arg["name"]["HEISA_DIV_NAME"] = '学級閉鎖';
                $arg["name"]["GRADE_NAME"] = '学年：';
                $arg["name"]["HR_CLASS_NAME"] = '組：';
            } else if ($model->field["HEISA_DIV"] === '2') {
                $arg["name"]["HEISA_DIV_NAME"] = '学年閉鎖';
                $arg["name"]["GRADE_NAME"] = '学年：';
            }
        } else {
            $model->field["GRADE"] = '99';
            $arg["name"]["HEISA_DIV_NAME"] = '学校閉鎖';
            knjCreateHidden($objForm, "GRADE", $model->field["GRADE"]);
        }

        //クラス選択コンボ
        if ($model->field["HEISA_DIV"] === '1') {
            $query = knjf305Query::getAuth($model->field["YEAR"], CTRL_SEMESTER, $model->field["GRADE"]);
            $extra = ($model->data_div == '0_new') ? "onchange=\"return btn_submit('main');\"" : " disabled";
            makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1, 1);
            if ($model->data_div != '0_new') knjCreateHidden($objForm, "HR_CLASS", $model->field["HR_CLASS"]);
        } else {
            $model->field["HR_CLASS"] = '999';
            knjCreateHidden($objForm, "HR_CLASS", $model->field["HR_CLASS"]);
        }
        
        if ($model->isIinkai > 0) {
            //報告履歴
            $query = knjf305Query::getReport($model);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, "REPORT", $model->field["REPORT"], $extra, 1, 1);
        }
        /*以下データを取得し、セットする*/
        //データ取得
        if ($model->data_div !== '0_new' && $model->data_div != "" && $model->cmd === 'change') {
            $Row = $db->getRow(knjf305Query::getAddition4Dat($model, "DATA"), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }
        
        //出席停止者数
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PATIENT_COUNT"] = knjCreateTextBox($objForm, $Row["PATIENT_COUNT"], "PATIENT_COUNT", 4, 4, $extra);
        
        //罹患欠席者数
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ABSENCE_COUNT"] = knjCreateTextBox($objForm, $Row["ABSENCE_COUNT"], "ABSENCE_COUNT", 4, 4, $extra);

        //罹患出席者数
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PRESENCE_COUNT"] = knjCreateTextBox($objForm, $Row["PRESENCE_COUNT"], "PRESENCE_COUNT", 4, 4, $extra);

        //措置期間
        $arg["data"]["ACTION_S_DATE_SHOW"] = str_replace("-","/",$model->field["ACTION_S_DATE"]);
        $arg["data"]["ACTION_E_DATE"] = View::popUpCalendar($objForm, "ACTION_E_DATE", str_replace("-","/",$Row["ACTION_E_DATE"]),"");

        //症状
        for ($i = 0; $i < 13; $i++ ) {
            $number = sprintf("%02d", $i);//頭0埋め
            if ($Row["SYMPTOM".$number] == "1") {
                $extra = "checked='checked' ";
            } else {
                $extra = "";
            }
            $extra .= " id=\"SYMPTOM{$number}\"";
            $arg["data"]["SYMPTOM".$number] = knjCreateCheckBox($objForm, "SYMPTOM".$number, "1", $extra);
        }

        //発熱（℃）
        $extra = "onblur=\"this.value=toFloat(this.value)\"";
        $arg["data"]["SYMPTOM01_REMARK"] = knjCreateTextBox($objForm, $Row["SYMPTOM01_REMARK"], "SYMPTOM01_REMARK", 4, 4, $extra);

        //その他（備考）
        $extra = "";
        $arg["data"]["SYMPTOM12_REMARK"] = knjCreateTextBox($objForm, $Row["SYMPTOM12_REMARK"], "SYMPTOM12_REMARK", 95, 50, $extra);

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 100, 50, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJF305");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム
        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf305Form1.html", $arg);
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
        //データが0件の時
        if (empty($opt)) {
            $opt[] = array('label' => CTRL_YEAR, 'value' => CTRL_YEAR);
        }
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model) {

    if ($model->isIinkai > 0) {
        //県への報告
        $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
        $extra = "onclick=\"return btn_submit('houkoku');\"";
        $arg["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告／PDFプレビュー", $extra.$disabled);
    }

    //更新ボタンを作成する
    $disabled = (AUTHORITY > DEF_REFER_RESTRICT) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$nullcheck_disabled);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra.$nullcheck_disabled);

    //印刷
    $extra = "onclick=\"newwin('".SERVLET_URL."');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
}
?>
