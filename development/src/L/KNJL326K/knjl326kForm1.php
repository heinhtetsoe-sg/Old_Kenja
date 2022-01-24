<?php

require_once('for_php7.php');

/********************************************************************/
/* 成績一覧表                                       山城 2005/09/02 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 : 出力対象にスポーツ推薦者追加             山城 2005/11/14 */
/* NO002 : 名前・受験番号出力を個別指定に変更       山城 2005/11/14 */
/* NO003 : 出力コースに全コースを追加               山城 2005/11/14 */
/********************************************************************/

class knjl326kForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl326kForm1", "POST", "knjl326kindex.php", "", "knjl326kForm1");
    $db = Query::dbCheckOut();

    $opt=array();

    $arg["data"]["YEAR"] = $model->ObjYear;

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl326kQuery::getSpecialReasonDiv($model);
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

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;

    $result = $db->query(knjl326kQuery::GetTestdiv($model));
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
                        "options"   => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //出力対象ラジオ（1:一般受験者,2:附属出身者,3:スポーツ推薦者）NO001
    $opt_out2[0]=1;
    $opt_out2[1]=2;
    $opt_out2[2]=3;
    $opt_out2[3]=4;

    if (!$model->output2) $model->output2 = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT2",
                        "value"      => $model->output2,
                        "multiple"   => $opt_out2));

    $arg["data"]["OUTPUT2_1"] = $objForm->ge("OUTPUT2",1);
    $arg["data"]["OUTPUT2_2"] = $objForm->ge("OUTPUT2",2);
    $arg["data"]["OUTPUT2_3"] = $objForm->ge("OUTPUT2",3);
    $arg["data"]["OUTPUT2_4"] = $objForm->ge("OUTPUT2",4);

    //各種帳票ラジオ（1:受験番号順,2:成績順）
    $opt_sort[0]=1;
    $opt_sort[1]=2;

    if (!$model->output) $model->output = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "SORT",
                        "value"      => $model->output,
                        "multiple"   => $opt_sort));

    $arg["data"]["SORT1"] = $objForm->ge("SORT",1);
    $arg["data"]["SORT2"] = $objForm->ge("SORT",2);

    //各種帳票ラジオ（1:理数科,2:国際コース3:特進コース,4:進学コース,5:全コース）NO003
    $opt_type[0]=1;
    $opt_type[1]=2;
    $opt_type[2]=3;
    $opt_type[3]=4;
    $opt_type[4]=5;

    if (!$model->output) $model->output = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTTYPE",
                        "value"      => $model->output,
                        "multiple"   => $opt_type));

    $arg["data"]["OUTTYPE1"] = $objForm->ge("OUTTYPE",1);
    $arg["data"]["OUTTYPE2"] = $objForm->ge("OUTTYPE",2);
    $arg["data"]["OUTTYPE3"] = $objForm->ge("OUTTYPE",3);
    $arg["data"]["OUTTYPE4"] = $objForm->ge("OUTTYPE",4);
    $arg["data"]["OUTTYPE5"] = $objForm->ge("OUTTYPE",5);

    //重複チェック項目チェックボックス
    $objForm->ae( array("type"      => "checkbox",
                        "name"      => "SENGAN",
                        "extrahtml" => "checked",
                        "value"     => "on"));

    $arg["data"]["SENGAN"] = $objForm->ge("SENGAN");

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => "HEIGAN",
                        "extrahtml" => "checked",
                        "value"     => "on"));

    $arg["data"]["HEIGAN"] = $objForm->ge("HEIGAN");

    //名前チェックボックス NO002
    if ($model->output == "on" || $model->cmd == ""){
        $checked = "checked";
    }
    $objForm->ae( array("type"      => "checkbox",
                        "name"      => "OUTPUTNAME",
                        "extrahtml" => "checked",
                        "value"     => "on"));

    $arg["data"]["OUTPUTNAME"] = $objForm->ge("OUTPUTNAME");

    //受験番号チェックボックス NO002
    if ($model->output == "on" || $model->cmd == ""){
        $checked = "checked";
    }
    $objForm->ae( array("type"      => "checkbox",
                        "name"      => "OUTPUTEXAM",
                        "extrahtml" => "checked",
                        "value"     => "on"));

    $arg["data"]["OUTPUTEXAM"] = $objForm->ge("OUTPUTEXAM");

    //中高判別フラグを作成する
    $jhflg = 0;
    $row = $db->getOne(knjl326kQuery::GetJorH());
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
                        "value"     => "KNJL326K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl326kForm1.html", $arg); 
    }
}
?>
