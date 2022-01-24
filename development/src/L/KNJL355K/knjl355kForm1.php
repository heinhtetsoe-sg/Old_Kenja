<?php
/********************************************************************/
/* 府県別集計表                                     山城 2005/12/30 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 : 地域別集計一覧を追加                     山城 2006/01/14 */
/* NO002 : 出力帳票種類を変更　                     仲本 2006/01/17 */
/********************************************************************/


class knjl355kForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl355kForm1", "POST", "knjl355kindex.php", "", "knjl355kForm1");
    $db = Query::dbCheckOut();

    $opt=array();

    $arg["data"]["YEAR"] = $model->ObjYear;

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;

    $result = $db->query(knjl355kQuery::GetTestdiv($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_testdiv[] = array("label" => $row["NAME1"],
                               "value" => $row["NAMECD2"]);
        $testcnt++;
    }
//  if ($testcnt == 0){
//      $opt_testdiv[$testcnt] = array("label" => "　　",
//                                     "value" => "99");
//  }

    if (!$model->testdiv) $model->testdiv = $opt_testdiv[0]["value"];

    $result->free();
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
    $query = knjl355kQuery::getSpecialReasonDiv($model);
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

    //出力帳票種類ラジオ---1:府県別集計一覧,2:地域別集計一覧---NO002
    $opt_out[0]=1;
    $opt_out[1]=2;
//    $opt_out[2]=3;    //NO001 NO002

    if (!$model->output) $model->output = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT",
                        "value"      => $model->output,
                        "extrahtml"  => " onclick =\" setdisabled(this);\"",    //NO002
                        "multiple"   => $opt_out));

    $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
    $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
//    $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);   //NO001 NO002

    //1:府県別集計一覧用ラジオ---1:学校所在地,2:現住所所在地---NO002
    $opt_out3[0]=1;
    $opt_out3[1]=2;

    if (!$model->output3) $model->output3 = 1;
    $disable = ($model->output == 1) ? "" : "disabled";

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT3",
                        "value"      => $model->output3,
                        "extrahtml"  => $disable,
                        "multiple"   => $opt_out3));

    $arg["data"]["OUTPUT3_1"] = $objForm->ge("OUTPUT3",1);
    $arg["data"]["OUTPUT3_2"] = $objForm->ge("OUTPUT3",2);

    //2:地域別集計一覧用ラジオ---1:学校所在地,2:現住所所在地---NO002
    $opt_out4[0]=1;
    $opt_out4[1]=2;

    if (!$model->output4) $model->output4 = 1;
    $disable = ($model->output == 2) ? "" : "disabled";

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT4",
                        "value"      => $model->output4,
                        "extrahtml"  => $disable,
                        "multiple"   => $opt_out4));

    $arg["data"]["OUTPUT4_1"] = $objForm->ge("OUTPUT4",1);
    $arg["data"]["OUTPUT4_2"] = $objForm->ge("OUTPUT4",2);

    //出力対象ラジオ---1:一般 2:附属推薦
    $opt_fzk[0]=1;
    $opt_fzk[1]=2;

    if (!$model->output2) $model->output2 = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT2",
                        "value"      => $model->output2,
                        "multiple"   => $opt_fzk));

    $arg["data"]["FZKFLG1"] = $objForm->ge("OUTPUT2",1);
    $arg["data"]["FZKFLG2"] = $objForm->ge("OUTPUT2",2);

    //中高判別フラグを作成する
    $jhflg = 0;
    $row = $db->getOne(knjl355kQuery::GetJorH());
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
                        "value"     => "KNJL355K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl355kForm1.html", $arg); 
    }
}
?>
