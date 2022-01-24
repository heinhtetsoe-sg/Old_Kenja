<?php

require_once('for_php7.php');

class knjf150cSubForm4
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform4", "POST", "knjf150cindex.php", "", "subform4");

        //DB接続
        $db = Query::dbCheckOut();

        //種別区分（生徒以外）
        $model->type = '4';

        //生徒項目名
        $arg["SCH_LABEL"] = $model->sch_label;

        //警告メッセージを表示しない場合
        if(($model->cmd == "subform4A") || ($model->cmd == "subform4_clear")){
            if (isset($model->schregno) && !isset($model->warning)){
                $row = $db->getRow(knjf150cQuery::getRow($model), DB_FETCHMODE_ASSOC);
            } else {
                $row =& $model->field;
            }
        } else {
            $row =& $model->field;
        }

        //extra
        $extra_int = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";

        //生徒情報
        $hr_name = $db->getOne(knjf150cQuery::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

        //来室日付作成
        $value = ($row["VISIT_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $row["VISIT_DATE"]);
        $arg["data"]["VISIT_DATE"] = View::popUpCalendar($objForm, "VISIT_DATE", $value);

        //来室時間（時）
        $arg["data"]["VISIT_HOUR"] = knjCreateTextBox($objForm, $row["VISIT_HOUR"], "VISIT_HOUR", 2, 2, $extra_int);

        //来室時間（分）
        $arg["data"]["VISIT_MINUTE"] = knjCreateTextBox($objForm, $row["VISIT_MINUTE"], "VISIT_MINUTE", 2, 2, $extra_int);

        //来室校時
        $query = knjf150cQuery::getNameMstPeriod();
        makeCmb($objForm, $arg, $db, $query, "VISIT_PERIODCD", $row["VISIT_PERIODCD"], "", 1);

        //相談者名
        $arg["data"]["CONSULTATION_NAME"] = knjCreateTextBox($objForm, $row["CONSULTATION_NAME"], "CONSULTATION_NAME", 20, 20, "");

        //生徒との関係コンボ作成
        $query = knjf150cQuery::getNameMst('F214');
        makeCmb($objForm, $arg, $db, $query, "RELATIONSHIP", $row["RELATIONSHIP"], "", 1);

        //相談方法コンボ作成
        $query = knjf150cQuery::getNameMst('F215');
        makeCmb($objForm, $arg, $db, $query, "CONSULTATION_METHOD", $row["CONSULTATION_METHOD"], "", 1);

        //来室理由１コンボ作成
        $query = knjf150cQuery::getNameMst('F202');
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON1", $row["VISIT_REASON1"], "", 1);
        //来室理由２コンボ作成
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON2", $row["VISIT_REASON2"], "", 1);
        //来室理由３コンボ作成
        makeCmb($objForm, $arg, $db, $query, "VISIT_REASON3", $row["VISIT_REASON3"], "", 1);

        //処置１コンボ作成
        $query = knjf150cQuery::getNameMst('F210');
        makeCmb($objForm, $arg, $db, $query, "TREATMENT1", $row["TREATMENT1"], "", 1);
        //処置２コンボ作成
        makeCmb($objForm, $arg, $db, $query, "TREATMENT2", $row["TREATMENT2"], "", 1);
        //処置３コンボ作成
        makeCmb($objForm, $arg, $db, $query, "TREATMENT3", $row["TREATMENT3"], "", 1);

        //相談時間コンボ作成
        $query = knjf150cQuery::getNameMst('F212');
        makeCmb($objForm, $arg, $db, $query, "RESTTIME", $row["RESTTIME"], "", 1);

        //退出時間（時）
        $arg["data"]["LEAVE_HOUR"] = knjCreateTextBox($objForm, $row["LEAVE_HOUR"], "LEAVE_HOUR", 2, 2, $extra_int);

        //退出時間（分）
        $arg["data"]["LEAVE_MINUTE"] = knjCreateTextBox($objForm, $row["LEAVE_MINUTE"], "LEAVE_MINUTE", 2, 2, $extra_int);

        //退出校時
        $query = knjf150cQuery::getNameMstPeriod();
        makeCmb($objForm, $arg, $db, $query, "LEAVE_PERIODCD", $row["LEAVE_PERIODCD"], "", 1);

        //処理結果（休養）チェックボックス
        $extra = ($row["RESULT_REST"] == "1") ? "checked" : "";
        $extra .= " id=\"RESULT_REST\"";
        $arg["data"]["RESULT_REST"] = knjCreateCheckBox($objForm, "RESULT_REST", "1", $extra, "");

        //処理結果（早退）チェックボックス
        $extra = ($row["RESULT_EARLY"] == "1") ? "checked" : "";
        $extra .= " id=\"RESULT_EARLY\"";
        $arg["data"]["RESULT_EARLY"] = knjCreateCheckBox($objForm, "RESULT_EARLY", "1", $extra, "");

        //処理結果（医療機関）チェックボックス
        $extra = ($row["RESULT_MEDICAL"] == "1") ? "checked" : "";
        $extra .= " id=\"RESULT_MEDICAL\"";
        $arg["data"]["RESULT_MEDICAL"] = knjCreateCheckBox($objForm, "RESULT_MEDICAL", "1", $extra, "");

        //連絡コンボ作成
        $query = knjf150cQuery::getNameMst('F213');
        makeCmb($objForm, $arg, $db, $query, "CONTACT", $row["CONTACT"], "", 1);

        //病院名テキストボックス
        $arg["data"]["HOSPITAL"] = knjCreateTextBox($objForm, $row["HOSPITAL"], "HOSPITAL", 20, 20, "");

        //同伴者テキストボックス
        $arg["data"]["COMPANION"] = knjCreateTextBox($objForm, $row["COMPANION"], "COMPANION", 20, 20, "");

        //同伴者区分コンボ作成
        $query = knjf150cQuery::getNameMst('F218');
        makeCmb($objForm, $arg, $db, $query, "COMPANION_DIV", $row["COMPANION_DIV"], "", 1);

        //診断名テキストボックス
        $arg["data"]["DIAGNOSIS"] = knjCreateTextBox($objForm, $row["DIAGNOSIS"], "DIAGNOSIS", 20, 20, "");

        //特記事項テキスト
        if ($model->schoolName == "fukuiken"){
            $arg["data"]["SPECIAL_NOTE"] = knjCreateTextArea($objForm, "SPECIAL_NOTE", "2", "100", "", $extra, $row["SPECIAL_NOTE"]);
        } else {
            $arg["data"]["SPECIAL_NOTE"] = knjCreateTextBox($objForm, $row["SPECIAL_NOTE"], "SPECIAL_NOTE", 100, 100, "");
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf150cSubForm4.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //登録ボタン
    $update = ($model->cmd == "subform4") ? "insert" : "update";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "登 録", "onclick=\"return btn_submit('".$update."');\"");
    if($model->cmd != "subform4"){
        //取消ボタン
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('subform4_clear');\"");
    }
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
    //終了ボタン
    if ($model->sendSubmit != "") {
        $link = REQUESTROOT."/F/KNJF150D/knjf150dindex.php";
        $extra = "onclick=\"parent.location.href='$link';\"";
    } else {
        $extra = "onclick=\"return btn_submit('edit');\"";
    }
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "TYPE", $model->type);

    //印刷用
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJF150C");
    knjCreateHidden($objForm, "PRINT_VISIT_DATE", $model->visit_date);
    knjCreateHidden($objForm, "PRINT_VISIT_HOUR", $model->visit_hour);
    knjCreateHidden($objForm, "PRINT_VISIT_MINUTE", $model->visit_minute);
}
?>
