<?php

require_once('for_php7.php');

class knjl371iForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();
        
        //フォーム作成
        $arg["start"]  = $objForm->get_start("csv", "POST", "knjl371iindex.php", "", "csv");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR + 1;
        
        //入試制度
        if (!$model->field["APPLICANTDIV"]) {
            $model->field["APPLICANTDIV"] = ($model->schoolKind == "H") ? "2" : "1";
        }
        $query = knjl371iQuery::getNameMst("L003", $model->field["APPLICANTDIV"]);
        $extra = "onChange=\"btn_submit('main')\"";
        
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, "");

        //入試区分
        $query = knjl371iQuery::getEntexamTestDivMst($model->field["APPLICANTDIV"]);
        // $extra = "onChange=\"btn_submit('main')\"";
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, "");
        
        
        //抽出区分ラジオボタン 1:全員 2:合格者のみ
        $opt_disp = array(1, 2);
        $model->field["DISP"] = ($model->field["DISP"] == "") ? "1" : $model->field["DISP"];
        $extra = array("id=\"DISP1\" onClick=\"return btn_submit('knjl371i')\"", "id=\"DISP2\" onClick=\"return btn_submit('knjl371i')\"");
        $radioArray = knjCreateRadio($objForm, "DISP", $model->field["DISP"], $extra, $opt_disp, count($opt_disp));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }
        
        //帳票区分ラジオボタン 1:出身学校住所ラベル 2:対象者住所ラベル
        $opt_disp = array(1, 2);
        $model->field["FORMDIV"] = ($model->field["FORMDIV"] == "") ? "1" : $model->field["FORMDIV"];
        $extra = array("id=\"FORMDIV1\" onClick=\"return btn_submit('knjl371i')\"", "id=\"FORMDIV2\" onClick=\"return btn_submit('knjl371i')\"");
        $radioArray = knjCreateRadio($objForm, "FORMDIV", $model->field["FORMDIV"], $extra, $opt_disp, count($opt_disp));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }
        
        if ($model->field["FORMDIV"] == "1") {
            $arg["PRG_NAME"] = "出身学校住所ラベル";
        } else {
            $arg["PRG_NAME"] = "対象者住所ラベル";
        }
        
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= $disabled;
        $arg["data"]["BANGOU1"] = knjCreateTextBox($objForm, $arg["data"]["BANGOU1"], "BANGOU1", 10, 4, $extra);
        $arg["data"]["BANGOU2"] = knjCreateTextBox($objForm, $arg["data"]["BANGOU2"], "BANGOU2", 10, 4, $extra);
        

        /**********/
        /* ボタン */
        /**********/
        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
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
        knjCreateHidden($objForm, "PRGID", "KNJL371I");

        $arg["IFRAME"] = VIEW::setIframeJs();
        //DB切断
        Query::dbCheckIn($db);

        
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl371iForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
