<?php
/********************************************************************/
/* 入学試験志願者名簿                               山城 2005/07/21 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 : クラブ推薦と受験番号を択可能にする。     山城 2006/01/14 */
/* NO002 : クラブ推薦にクラブ別と全体のラジオを追加 仲本 2006/01/24 */
/********************************************************************/

class knjl310kForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl310kForm1", "POST", "knjl310kindex.php", "", "knjl310kForm1");
    $db = Query::dbCheckOut();

    $opt=array();

    $arg["data"]["YEAR"] = $model->ObjYear;

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;

    $result = $db->query(knjl310kQuery::GetTestdiv($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_testdiv[] = array("label" => $row["NAME1"],
                               "value" => $row["NAMECD2"]);
        $testcnt++;
    }
    if ($testcnt == 0){
        $opt_testdiv[$testcnt] = array("label" => "　　",
                                       "value" => "99");
    }else {
        $opt_testdiv[$testcnt] = array("label" => "全て",
                                       "value" => "99");
    }
    
    if (!$model->testdiv) $model->testdiv = $opt_testdiv[0]["value"];

    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "TESTDIV",
                        "size"      => 1,
                        "value"     => $model->testdiv,
                        "extrahtml" => "onchange=\" return btn_submit('knjl310k');\"",
                        "options"   => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl310kQuery::getSpecialReasonDiv($model);
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

    //中高判別フラグを作成する
    $jhflg = 0;
    $db = Query::dbCheckOut();
    $row = $db->getOne(knjl310kQuery::GetJorH());
    if ($row == 1){
        $jhflg = 1;
    }else {
        $jhflg = 2;
    }
    Query::dbCheckIn($db);
    $objForm->ae( array("type" => "hidden",
                        "name" => "JHFLG",
                        "value"=> $jhflg ) );

    if ($jhflg == 1){
        $arg[dispj] = $jhflg;
    }else {
        $arg[disph] = $jhflg;
    }
    //NO001
    //切替ラジオ（1:受験番号,2:クラブ推薦）
    $opt[0]=1;
    $opt[1]=2;

    if (!$model->output) $model->output = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT",
                        "value"      => $model->output,
                        "extrahtml"  => "onclick =\" setdisabled(this);\"",
                        "multiple"   => $opt));

    $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
    $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

    //受験番号入力テキスト
    //NO001
    $disabled = "";
    if ($model->output == 2) {
        $disabled = disabled;
    }

    $db = Query::dbCheckOut();
    $row = $db->getOne(knjl310kQuery::GetExamno($model));

    $objForm->ae( array("type"      => "text",
                        "name"      => "EXAMNOF",
                        "size"      => 4,
                        "maxlength" => 4,
                        "extrahtml" => $disabled,   //NO001
                        "value"     => $row) );

    $arg["data"]["EXAMNOF"] = $objForm->ge("EXAMNOF");

    $objForm->ae( array("type"      => "text",
                        "name"      => "EXAMNOT",
                        "size"      => 4,
                        "maxlength" => 4,
                        "extrahtml" => $disabled,   //NO001
                        "value"     => $row) );

    $arg["data"]["EXAMNOT"] = $objForm->ge("EXAMNOT");

    Query::dbCheckIn($db);

    //NO002
    //切替ラジオ2（1:クラブ別,2:全体）
    $opt2[0]=1;
    $opt2[1]=2;

    if (!$model->output2) $model->output2 = 1;

    if ($model->output == 1) {
        $disabled = disabled;
    }

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT2",
                        "value"      => $model->output2,
                        "extrahtml" => $disabled,
                        "multiple"   => $opt2));

    $arg["data"]["OUTPUT2_1"] = $objForm->ge("OUTPUT2",1);
    $arg["data"]["OUTPUT2_2"] = $objForm->ge("OUTPUT2",2);

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
                        "value"     => "KNJL310K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl310kForm1.html", $arg); 
    }
}
?>
