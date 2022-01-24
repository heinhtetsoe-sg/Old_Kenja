<?php
/********************************************************************/
/* 志願者/入試事前相談データ突合せリスト(中学用)    山城 2005/08/03 */
/*                                                                  */
/* 変更履歴                                                         */
/*  ・NO001 帳票追加(OUTPUT5、OUTPUT6)              山城 2005/08/16 */
/********************************************************************/

class knjl302kForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl302kForm1", "POST", "knjl302kindex.php", "", "knjl302kForm1");
    $db = Query::dbCheckOut();

    $opt=array();

    $arg["data"]["YEAR"] = $model->ObjYear;

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;

    $result = $db->query(knjl302kQuery::GetTestdiv($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_testdiv[] = array("label" => $row["NAME1"],
                               "value" => $row["NAMECD2"]);
        $testcnt++;
    }
    if ($testcnt == 0){
        $opt_testdiv[$testcnt] = array("label" => "　　",
                                       "value" => "99");
    }

    if (!$model->testdiv) $model->testdiv = $opt_testdiv[0]["value"];

    $objForm->ae( array("type"      => "select",
                        "name"      => "TESTDIV",
                        "size"      => 1,
                        "value"     => $model->testdiv,
                        "extrahtml" => "",
                        "options"   => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl302kQuery::getSpecialReasonDiv($model);
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
    $extra = "";
    $arg["data"]["SPECIAL_REASON_DIV"] = knjCreateCombo($objForm, "SPECIAL_REASON_DIV", $model->special_reason_div, $opt, $extra, 1);

    /*----------------------------------------*/
    /*重複チェック項目                        */
    /* 1:氏名一致・かな不一致                 */
    /* 2:氏名・かな・試験区分一致             */
    /* 3:氏名・かな一致、試験区分不一致       */
    /* 4:氏名・かな不一致、試験区分一致       */
    /* 5:氏名・かな不一致、試験区分一致 NO001 */
    /* 6:氏名・かな不一致、試験区分一致 NO001 */
    /*----------------------------------------*/
    $opt_out[0]=1;
    $opt_out[1]=2;
    $opt_out[2]=3;
    $opt_out[3]=4;
    $opt_out[4]=5;  //NO001
    $opt_out[5]=6;  //NO001

    if (!$model->output) $model->output = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT",
                        "value"      => $model->output,
                        "extrahtml"  => "",
                        "multiple"   => $opt_out));

    $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
    $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
    $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);
    $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT",4);
    $arg["data"]["OUTPUT5"] = $objForm->ge("OUTPUT",5); //NO001
    $arg["data"]["OUTPUT6"] = $objForm->ge("OUTPUT",6); //NO001

    //中高判別フラグを作成する
    $jhflg = 0;
    $db = Query::dbCheckOut();
    $row = $db->getOne(knjl302kQuery::GetJorH());
    if ($row == 1){
        $jhflg = 1;
    }else {
        $jhflg = 2;
    }
    Query::dbCheckIn($db);
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
                        "value"     => "KNJL302K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjl302kForm1.html", $arg);
    }
}
?>
