<?php
/********************************************************************/
/* スカラシップ認定者名簿                           山城 2006/01/19 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 :                                          name yyyy/mm/dd */
/********************************************************************/

class knjl361kForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl361kForm1", "POST", "knjl361kindex.php", "", "knjl361kForm1");
    $db = Query::dbCheckOut();

    $opt=array();

    $arg["data"]["YEAR"] = $model->ObjYear;

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;

    $result = $db->query(knjl361kQuery::GetTestdiv($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_testdiv[] = array("label" => $row["NAME1"],
                               "value" => $row["NAMECD2"]);
        $testcnt++;
    }
    
    if (!$model->testdiv) $model->testdiv = $opt_testdiv[0]["value"];

    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "TESTDIV",
                        "size"      => 1,
                        "value"     => $model->testdiv,
                        "extrahtml" => "onchange=\" return btn_submit('knjl361k');\"",
                        "options"   => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl361kQuery::getSpecialReasonDiv($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($model->special_reason_div == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE1"] == '1') {
            $special_reason_div = $row["VALUE"];
        }
    }
    $model->special_reason_div = (strlen($model->special_reason_div) && $value_flg) ? $model->special_reason_div : $special_reason_div;
    $extra = "onChange=\"btn_submit('knjl361k')\"";
    $arg["data"]["SPECIAL_REASON_DIV"] = knjCreateCombo($objForm, "SPECIAL_REASON_DIV", $model->special_reason_div, $opt, $extra, 1);

    //スカラシップコンボボックスを作成する
    $opt_scalashipdiv = array();

    $opt_scalashipdiv[] = array("label" => "　　",
                                "value" => "99");

    $result = $db->query(knjl361kQuery::GetScalashipdiv($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_scalashipdiv[] = array("label" => $row["NAME1"],
                                    "value" => $row["SCALASHIPDIV"]);
    }

    if (!$model->scalashipdiv) $model->scalashipdiv = $opt_scalashipdiv[0]["value"];

    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "SCALASHIPDIV",
                        "size"      => 1,
                        "value"     => $model->scalashipdiv,
                        "extrahtml" => "",
                        "options"   => $opt_scalashipdiv ) );

    $arg["data"]["SCALASHIPDIV"] = $objForm->ge("SCALASHIPDIV");

    //中高判別フラグを作成する
    $jhflg = 0;
    $row = $db->getOne(knjl361kQuery::GetJorH($model));
    if ($row == 1){
        $jhflg = 1;
    }else {
        $jhflg = 2;
    }
    $objForm->ae( array("type" => "hidden",
                        "name" => "JHFLG",
                        "value"=> $jhflg ) );

    //印刷ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_print",
                        "value"       => "プレビュー／印刷",
                        "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

    $arg["button"]["btn_print"] = $objForm->ge("btn_print");

    //終了ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_end",
                        "value"       => "終 了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ) );

    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "YEAR",
                        "value"     => $model->ObjYear
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "DBNAME",
                        "value"     => DB_DATABASE
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "PRGID",
                        "value"     => "KNJL361K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl361kForm1.html", $arg); 
    }
}
?>
