<?php

require_once('for_php7.php');

class knjl640iForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        $depts = array(
            array("label" => "-- 全て --", "value" => "ALL"),
            array("label" => "1:普通科"  , "value" => "1"  ),
            array("label" => "2:工業科"  , "value" => "2"  )
        );

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->examYear;

        //学科コンボ
        if (is_null($model->field["DEPT"])) {
            $model->field["DEPT"] = "ALL";
        }
        $arg["data"]["DEPT"] = knjCreateCombo($objForm, "DEPT", $model->field["DEPT"], $depts, "", 1);

        //入試区分コンボ
        $testdivLst = array();
        $result = $db->query(knjl640iQuery::getTestDivMst($model->examYear, $model->applicantdiv));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivLst[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
        $result->free();
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $testdivLst, "", 1);

        //出力順ラジオボタン
        if (is_null($model->field["ORDER_BY"])) {
            $model->field["ORDER_BY"] = 1;
        }
        $orderbyNo =1;
        $radioBtns = knjCreateRadio($objForm, "ORDER_BY", $model->field["ORDER_BY"], " id='{0}'", "", 2);
        for ($idx =0; is_array($radioBtns) && $idx < count($radioBtns); $idx++) {
            $controlId = $idx + 1;
            $arg["data"]["ORDER_BY{$controlId}_ID"] = "ORDER_BY{$controlId}";
            $arg["data"]["ORDER_BY{$controlId}"] = str_replace("{0}", "ORDER_BY{$controlId}", $radioBtns["ORDER_BY{$controlId}"]);
        }

        //帳票種類
        if ($model->field["OUTPUT_TYP"] == "") {
            $model->field["OUTPUT_TYP"] = 1;
        }
        if ($model->field["OUTPUT_TYP1_SUB"]  == "") {
            $model->field["OUTPUT_TYP1_SUB"] = 1;
        }
        if ($model->field["OUTPUT_TYP2_SUB"] == "") {
            $model->field["OUTPUT_TYP2_SUB"] = 1;
        }
        if ($model->field["OUTPUT_TYP3_SUB"] == "") {
            $model->field["OUTPUT_TYP3_SUB"] = 1;
        }

        $opt = array(1, 2, 3, 4, 5);
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT_TYP{$val}\" onClick=\"selectOutputTyp(this)\"");

            //ラジオボタンラベルFor～指定
            $arg["data"]["OUTPUT_TYP{$val}_ID"] = "OUTPUT_TYP{$val}";
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT_TYP", $model->field["OUTPUT_TYP"], $extra, $opt, count($opt));

        $outputTypNo = 1;
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;

            //メインのラジオ1～3の子ラジオを作成
            if ($outputTypNo < 4) {
                //納金種別（施設設備費、入学申込金　など）
                $opt = array(1, 2);
                $extra = array();
                $radioDisabled = $model->field["OUTPUT_TYP"] == $outputTypNo ? "" : " disabled";
                foreach ($opt as $optKey => $optVal) {
                    array_push($extra, " id=\"OUTPUT_TYP{$outputTypNo}_SUB{$optVal}\"{$radioDisabled}");

                    //ラジオボタンラベルFor～指定
                    $arg["data"]["OUTPUT_TYP{$outputTypNo}_SUB{$optVal}_ID"] = "OUTPUT_TYP{$outputTypNo}_SUB{$optVal}";
                }
                $radioArray = knjCreateRadio($objForm, "OUTPUT_TYP{$outputTypNo}_SUB", $model->field["OUTPUT_TYP{$outputTypNo}_SUB"], $extra, $opt, count($opt));

                foreach ($radioArray as $keySub => $radioSub) {
                    $arg["data"][$keySub] = $radioSub;
                }
            }
            $outputTypNo++;
        }

        //csv出力ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "TIME", date("H:i"));
        knjCreateHidden($objForm, "PRGID", "KNJL640I");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl640iForm1", "POST", "knjl640iindex.php", "", "knjl640iForm1");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl640iForm1.html", $arg);
    }
}
