<?php

require_once('for_php7.php');

class knjl014oForm1
{
    public function main(&$model)
    {
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $query = knjl014oQuery::getNameMst($model, "L003");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //入試区分
        $query = knjl014oQuery::getTestdivMst($model->ObjYear);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "");

        //ファイルからの取り込み
        $extra = "";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

        //ヘッダ有
        $extra = ($model->field["HEADER"] == "on" || $model->cmd == "") ? "checked" : "";
        $extra .= " id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra, "");


        //ヘッダ出力
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_headcsv"] = knjCreateBtn($objForm, "btn_headcsv", "ヘッダ出力", $extra);

        //データ取込
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "データ取込", $extra);

        //データ生成
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_l015e"] = knjCreateBtn($objForm, "btn_l015e", "データ生成", $extra);

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
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl014oindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl014oForm1.html", $arg);
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
