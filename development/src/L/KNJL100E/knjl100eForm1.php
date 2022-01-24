<?php

require_once('for_php7.php');

class knjl100eForm1
{

    public function main(&$model)
    {

        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl100eQuery::getNameMst($model, "L003");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //入試区分
        $opt = array();
        $opt[] = array('label' => '1:推薦', 'value' => '1');
        $opt[] = array('label' => '2:一般', 'value' => '2');
        $opt[] = array('label' => '3:二次', 'value' => '3');
        $extra = " onchange=\"return cmbChgChkDisabled(this);\"";
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);

        //合格内示
        $disable = ($model->field["TESTDIV"] <> "2") ? "" : " disabled";
        $extra  = ($model->field["CHECK1"] == "on") ? "checked" : "";
        $extra .= " id=\"CHECK1\"".$disable;
        $arg["data"]["CHECK1"] = knjCreateCheckBox($objForm, "CHECK1", "on", $extra, "");

        //実行
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "ＣＳＶ出力", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHOOL_KIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl100eindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl100eForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
