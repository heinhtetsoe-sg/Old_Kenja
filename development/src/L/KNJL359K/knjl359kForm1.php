<?php

class knjl359kForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl359kForm1", "POST", "knjl359kindex.php", "", "knjl359kForm1");
    $db = Query::dbCheckOut();

    $opt=array();

    $arg["data"]["YEAR"] = $model->ObjYear;

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;

    $result = $db->query(knjl359kQuery::GetTestdiv($model));
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

    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "TESTDIV",
                        "size"      => 1,
                        "value"     => $model->testdiv,
                        "extrahtml" => "onChange=\"return btn_submit('knjl359k');\"",
                        "options"   => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl359kQuery::getSpecialReasonDiv($model);
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

    //出力帳票ラジオ---1:平均点,2:最高・最低点---NO001
    $opt_output1[0]=1;
    $opt_output1[1]=2;

    if (!$model->output1) $model->output1 = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT1",
                        "value"      => $model->output1,
                        "multiple"   => $opt_output1,
                        "extrahtml"  => "" ) );

    $arg["data"]["OUTPUT1_1"] = $objForm->ge("OUTPUT1",1);
    $arg["data"]["OUTPUT1_2"] = $objForm->ge("OUTPUT1",2);

    //対象１ラジオ---1:受験者,2:合格者---NO001
    $opt_output2[0]=1;
    $opt_output2[1]=2;

    if (!$model->output2) $model->output2 = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT2",
                        "value"      => $model->output2,
                        "multiple"   => $opt_output2,
                        "extrahtml"  => "" ) );

    $arg["data"]["OUTPUT2_1"] = $objForm->ge("OUTPUT2",1);
    $arg["data"]["OUTPUT2_2"] = $objForm->ge("OUTPUT2",2);

    //対象２ラジオ---1:一般,2:中高一貫---NO001
    $opt_output3[0]=1;
    $opt_output3[1]=2;

    if (!$model->output3) $model->output3 = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT3",
                        "value"      => $model->output3,
                        "multiple"   => $opt_output3,
                        "extrahtml"  => "onclick=\"dis_fzkflg(this.value);\"" ) );

    $arg["data"]["OUTPUT3_1"] = $objForm->ge("OUTPUT3",1);
    $arg["data"]["OUTPUT3_2"] = $objForm->ge("OUTPUT3",2);

    //全体(9)・専願(1)・併願(2)ラジオ---NO001
    $opt_shdiv = array();
    $opt_shdiv[0] = 9;
    $opt_shdiv[1] = 1;
    $opt_shdiv[2] = 2;

    if (!$model->shdiv) $model->shdiv = 9;
    $dis_shdiv = ($model->output3 == 1) ? "" : "disabled";

    $objForm->ae( array("type"      => "radio",
                        "name"      => "SHDIV",
                        "value"     => $model->shdiv,
                        "multiple"  => $opt_shdiv,
                        "extrahtml" => $dis_shdiv ) );

    $arg["data"]["SHDIV9"] = $objForm->ge("SHDIV",9);
    $arg["data"]["SHDIV1"] = $objForm->ge("SHDIV",1);
    $arg["data"]["SHDIV2"] = $objForm->ge("SHDIV",2);
/*** NO001

    //出力対象ラジオ---1:平均点(受験者),2:平均点(合格者),3:最高・最低点(受験者),4:最高・最低点(合格者)
    $opt_out2[0]=1;
    $opt_out2[1]=2;
    $opt_out2[2]=3;
    $opt_out2[3]=4;

    if (!$model->output2) $model->output2 = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT2",
                        "value"      => $model->output2,
                        "multiple"   => $opt_out2,
                        "extrahtml"  => "onclick=\"dis_fzkflg(this.value);\"" ) );

    $arg["data"]["OUTPUT1_1"] = $objForm->ge("OUTPUT2",1);
    $arg["data"]["OUTPUT1_2"] = $objForm->ge("OUTPUT2",2);
    $arg["data"]["OUTPUT2_1"] = $objForm->ge("OUTPUT2",3);
    $arg["data"]["OUTPUT2_2"] = $objForm->ge("OUTPUT2",4);

    //1一般・2附属
    $opt_fzk1[0]=1;
    $opt_fzk1[1]=2;
    $opt_fzk2[0]=1;
    $opt_fzk2[1]=2;
    $opt_fzk3[0]=1;
    $opt_fzk3[1]=2;
    $opt_fzk4[0]=1;
    $opt_fzk4[1]=2;
    $dis_fzk[0] = "";
    $dis_fzk[1] = "disabled";
    $dis_fzk_no = 1;//後期は全て選択不可
    //ラジオ１
    if ($model->testdiv != "2") $dis_fzk_no = ($model->output2 == 1) ? 0 : 1;
    $objForm->ae( array("type"       => "radio",
                        "name"       => "FZKFLG1",
                        "value"      => (!$model->fzkflg1) ? 1 : $model->fzkflg1,
                        "extrahtml"  => $dis_fzk[$dis_fzk_no],
                        "multiple"   => $opt_fzk1));
    //ラジオ２
    if ($model->testdiv != "2") $dis_fzk_no = ($model->output2 == 2) ? 0 : 1;
    $objForm->ae( array("type"       => "radio",
                        "name"       => "FZKFLG2",
                        "value"      => (!$model->fzkflg2) ? 1 : $model->fzkflg2,
                        "extrahtml"  => $dis_fzk[$dis_fzk_no],
                        "multiple"   => $opt_fzk2));
    //ラジオ３
    if ($model->testdiv != "2") $dis_fzk_no = ($model->output2 == 3) ? 0 : 1;
    $objForm->ae( array("type"       => "radio",
                        "name"       => "FZKFLG3",
                        "value"      => (!$model->fzkflg3) ? 1 : $model->fzkflg3,
                        "extrahtml"  => $dis_fzk[$dis_fzk_no],
                        "multiple"   => $opt_fzk3));
    //ラジオ４
    if ($model->testdiv != "2") $dis_fzk_no = ($model->output2 == 4) ? 0 : 1;
    $objForm->ae( array("type"       => "radio",
                        "name"       => "FZKFLG4",
                        "value"      => (!$model->fzkflg4) ? 1 : $model->fzkflg4,
                        "extrahtml"  => $dis_fzk[$dis_fzk_no],
                        "multiple"   => $opt_fzk4));

    $arg["data"]["FZKFLG1_1"] = $objForm->ge("FZKFLG1",1);
    $arg["data"]["FZKFLG1_2"] = $objForm->ge("FZKFLG1",2);
    $arg["data"]["FZKFLG2_1"] = $objForm->ge("FZKFLG2",1);
    $arg["data"]["FZKFLG2_2"] = $objForm->ge("FZKFLG2",2);
    $arg["data"]["FZKFLG3_1"] = $objForm->ge("FZKFLG3",1);
    $arg["data"]["FZKFLG3_2"] = $objForm->ge("FZKFLG3",2);
    $arg["data"]["FZKFLG4_1"] = $objForm->ge("FZKFLG4",1);
    $arg["data"]["FZKFLG4_2"] = $objForm->ge("FZKFLG4",2);

***/

    //中高判別フラグを作成する
    $jhflg = 0;
    $row = $db->getOne(knjl359kQuery::GetJorH());
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
                        "value"     => "KNJL359K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl359kForm1.html", $arg); 
    }
}
?>
