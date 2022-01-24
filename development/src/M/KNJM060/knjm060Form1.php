<?php

require_once('for_php7.php');

class knjm060Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm060Form1", "POST", "knjm060index.php", "", "knjm060Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //親画面なし
        $securityCnt = $db->getOne(knjm060Query::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }

        //年度コンボを作成する
        $query = knjm060Query::GetYear($model);
        $extra = "onchange = \"return btn_submit('knjm060')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1, "");

        //学期コンボを作成する
        $query = knjm060Query::GetSem($model);
        $extra = "onchange = \"return btn_submit('knjm060')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GAKKI"], "GAKKI", $extra, 1, "ALL");

        //科目コンボを作成する
        $query = knjm060Query::GetSub($model);
        $extra = "onchange = \"return btn_submit('knjm060')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSNAME"], "SUBCLASSNAME", $extra, 1, "ALL");

        //出力区分を作成する
        $opt_div = array();
        $opt_div[0] = array('label' => "全て出力",
                            'value' => 0);
        $opt_div[1] = array('label' => "素点",
                            'value' => 1);
        $opt_div[2] = array('label' => "評定",
                            'value' => 2);
        if ($model->field["OUTDIV"] == "") $model->field["OUTDIV"] = $opt_div[0]["value"];

        $arg["data"]["OUTDIV"] = knjCreateCombo($objForm, "OUTDIV", $model->field["OUTDIV"], $opt_div, "", 1);

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //実行ボタン
        if ($model->Properties["useXLS"]) {
            $model->schoolCd = $db->getOne(knjm060Query::getSchoolCd());
            $extra = "onclick=\"return newwin('" . SERVLET_URL . "', '" . $model->schoolCd . "', '" . $model->Properties["xlsVer"] . "');\"";
            //タイトルの表示
            $arg["data"]["TITLE"] = "エクセル出力";
        } else {
            $extra = "onclick=\"return btn_submit('csv');\"";
            //タイトルの表示
            $arg["data"]["TITLE"] = "ＣＳＶ出力";
        }
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJM060");
        knjCreateHidden($objForm, "TEMPLATE_PATH");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm060Form1.html", $arg); 

    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て出力", "value" => "0");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
