<?php

require_once('for_php7.php');

class knjl013qForm1
{
    function main(&$model){

        $objForm = new form;
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl013qForm1", "POST", "knjl013qindex.php", "", "knjl013qForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->year;
        
        //入試制度コンボ
        $query = knjl013qQuery::getNameMst($model->year, "L003");
        $extra = "onChange=\"return btn_submit('knjl013q')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //入試区分
        if (SCHOOLKIND == "P") {
            $query = knjl013qQuery::getNameMst($model->year, "LP24");
        } else if (SCHOOLKIND == "J") {
            $query = knjl013qQuery::getNameMst($model->year, "L024");
        } else {
            $query = knjl013qQuery::getNameMst($model->year, "L004");
        }
        $extra = "onChange=\"return btn_submit('knjl013q')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "");

        //実行ボタン
        $extra = " onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "実 行", $extra);

        //終了ボタン
        $extra = " onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //チェック　受付データ（欠席入力～入力済み）有り
        $receptCnt = $db->getOne(knjl013qQuery::getReceptCnt($model));
        knjCreateHidden($objForm, "RECEPT_CNT", $receptCnt);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl013qForm1.html", $arg); 
        
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
        if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
