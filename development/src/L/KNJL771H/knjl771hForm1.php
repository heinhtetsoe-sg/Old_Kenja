<?php

require_once('for_php7.php');

class knjl771hForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();
        
        //フォーム作成
        $arg["start"]  = $objForm->get_start("csv", "POST", "knjl771hindex.php", "", "csv");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR + 1;

        //学校種別
        $query = knjl771hQuery::getNameMst($model->examyear, "L003");
        $extra = "onChange=\"btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //入試区分
        $query = knjl771hQuery::getEntexamTestDivMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1, "");

        /**********/
        /* ボタン */
        /**********/
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl771hForm1.html", $arg);
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
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
