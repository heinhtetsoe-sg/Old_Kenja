<?php
class knjl379iForm1
{
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]  = $objForm->get_start("csv", "POST", "knjl379iindex.php", "", "csv");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR + 1;
        
        //入試制度
        if (!$model->field["APPLICANTDIV"]) {
            $model->field["APPLICANTDIV"] = ($model->schoolKind == "H") ? "2" : "1";
        }
        $query = knjl379iQuery::getNameMst("L003", $model->field["APPLICANTDIV"]);
        $extra = "onChange=\"btn_submit('main')\"";
        
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, "");

        //入試区分
        $query = knjl379iQuery::getEntexamTestDivMst($model->field["APPLICANTDIV"]);
        $extra = "onChange=\"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, "");

        /**********/
        /* ボタン */
        /**********/
        //印刷ボタンを作成する
        //$extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "確定／プレビュー／印刷", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolKind);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR + 1);
        knjCreateHidden($objForm, "DATE", CTRL_DATE);
        knjCreateHidden($objForm, "TIME", date("H:i"));
        knjCreateHidden($objForm, "PRGID", "KNJL379I");

        
        //DB切断
        Query::dbCheckIn($db);

        
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = VIEW::setIframeJs();
        
        //更新処理後に帳票出力
        $arg["print"] = $model->print == "on" ? "newwin('" . SERVLET_URL . "');" :"";
        $model->print = "off";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl379iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
