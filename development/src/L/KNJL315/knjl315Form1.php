<?php

require_once('for_php7.php');

class knjl315Form1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl315Form1", "POST", "knjl315index.php", "", "knjl315Form1");

    $opt=array();

    $arg["TOP"]["YEAR"] = $model->ObjYear;

    //入試制度コンボの設定
    $opt_apdv_typ = array();
    $db = Query::dbCheckOut();
    $result = $db->query(knjl315Query::get_apct_div("L003",$model->ObjYear));

    while($Rowtyp = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_apdv_typ[]= array("label" => $Rowtyp["NAME1"], 
                               "value" => $Rowtyp["NAMECD2"]);
    }

    $result->free();
    Query::dbCheckIn($db);

    if (!isset($model->field["APDIV"])) {
        $model->field["APDIV"] = $opt_apdv_typ[0]["value"];
    }

    $objForm->ae( array("type"       => "select",
                        "name"       => "APDIV",
                        "size"       => "1",
                        "value"      => $model->field["APDIV"],
                        "extrahtml"  => "",
                        "options"    => $opt_apdv_typ));

    $arg["data"]["APDIV"] = $objForm->ge("APDIV");

    //入試区分コンボの設定
    $opt_test_dv = array();
    $defoult_flg = false;
    $defoult     = 1 ;
    $db = Query::dbCheckOut();
    $result = $db->query(knjl315Query::get_test_div($model->ObjYear));
    $opt_test_dv[]= array("label" => "", "value" => "");
    while($Rowtdv = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_test_dv[]= array("label" => $Rowtdv["NAME1"], 
                              "value" => $Rowtdv["NAMECD2"]);

        if ($Rowtdv["NAMESPARE2"] != 1 && !$defoult_flg){
            $defoult++;
        } else {
            $defoult_flg = true;
        }
    }
    $result->free();
    Query::dbCheckIn($db);
    if (!isset($model->field["TESTDV"])) {
        $model->field["TESTDV"] = $opt_test_dv[$defoult]["value"];
    }

    $objForm->ae( array("type"       => "select",
                        "name"       => "TESTDV",
                        "size"       => "1",
                        "value"      => $model->field["TESTDV"],
                        "extrahtml"  => "",
                        "options"    => $opt_test_dv));

    $arg["data"]["TESTDV"] = $objForm->ge("TESTDV");

    //印刷ボタンを作成する
    $objForm->ae( array("type"      => "button",
                        "name"      => "btn_print",
                        "value"     => "プレビュー／印刷",
                        "extrahtml" => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

    $arg["button"]["btn_print"] = $objForm->ge("btn_print");

    //終了ボタンを作成する
    $objForm->ae( array("type"      => "button",
                        "name"      => "btn_end",
                        "value"     => "終 了",
                        "extrahtml" => "onclick=\"closeWin();\"" ) );

    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

    //hiddenを作成する
    $objForm->ae( array("type"  => "hidden",
                        "name"  => "YEAR",
                        "value" => $model->ObjYear
                         ) );

    $objForm->ae( array("type"  => "hidden",
                        "name"  => "DBNAME",
                        "value" => DB_DATABASE
                         ) );

    $objForm->ae( array("type"  => "hidden",
                        "name"  => "PRGID",
                        "value" => "KNJL315"
                        ) );

    $objForm->ae( array("type" => "hidden",
                        "name" => "cmd"
                        ) );

    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl315Form1.html", $arg); 
    }
}
?>
