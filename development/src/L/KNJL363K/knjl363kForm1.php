<?php
/********************************************************************/
/* 入試受験者情報                                   山城 2006/02/07 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 :                                          name yyyy/mm/dd */
/********************************************************************/

class knjl363kForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl363kForm1", "POST", "knjl363kindex.php", "", "knjl363kForm1");

    $opt=array();

    $arg["data"]["YEAR"] = $model->ObjYear;

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;
    $db = Query::dbCheckOut();

    $result = $db->query(knjl363kQuery::GetTestdiv($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_testdiv[] = array("label" => $row["NAME1"],
                               "value" => $row["NAMECD2"]);
        $testcnt++;
    }
    
    if (!$model->testdiv) $model->testdiv = $opt_testdiv[0]["value"];

    $result->free();
    Query::dbCheckIn($db);
    $objForm->ae( array("type"      => "select",
                        "name"      => "TESTDIV",
                        "size"      => 1,
                        "value"     => $model->testdiv,
                        "extrahtml" => "onchange=\" return btn_submit('knjl363k');\"",
                        "options"   => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //中高判別フラグを作成する
    $jhflg = 0;
    $db = Query::dbCheckOut();
    $row = $db->getOne(knjl363kQuery::GetJorH($model));
    if ($row == 1){
        $jhflg = 1;
    }else {
        $jhflg = 2;
    }
    Query::dbCheckIn($db);
    $objForm->ae( array("type" => "hidden",
                        "name" => "JHFLG",
                        "value"=> $jhflg ) );

    //CSV
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_exec",
                        "value"       => "ＣＳＶ出力",
                        "extrahtml"   => "onclick=\"return btn_submit('exec');\"" ));
    $arg["button"]["btn_exec"] = $objForm->ge("btn_exec");

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
                        "value"     => "KNJL363K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl363kForm1.html", $arg); 
    }
}
?>
