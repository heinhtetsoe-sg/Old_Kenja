<?php

require_once('for_php7.php');

/********************************************************************/
/* 成績一覧表(中学)                                 山城 2005/07/21 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 理科型の全員出力を選択不可にする。         山城 2005/11/08 */
/* NO002 前期→成績順でも、コース別の選択を可       山城 2005/11/08 */
/* NO003 附属出身者→附属推薦者                     山城 2006/01/11 */
/********************************************************************/

class knjl323kForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl323kForm1", "POST", "knjl323kindex.php", "", "knjl323kForm1");
    $db = Query::dbCheckOut();

    $opt=array();

    $arg["data"]["YEAR"] = $model->ObjYear;

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;

    $result = $db->query(knjl323kQuery::GetTestdiv($model));
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
                        "extrahtml" => "onchange =\" dis_test(this);\"",
                        "options"   => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl323kQuery::getSpecialReasonDiv($model);
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

    //出力対象ラジオ（1:一般受験者,2:附属出身者）
    $opt_out2[0]=1;
    $opt_out2[1]=2;

    if (!$model->output2) $model->output2 = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT2",
                        "value"      => $model->output2,
                        "multiple"   => $opt_out2));

    $arg["data"]["OUTPUT2_1"] = $objForm->ge("OUTPUT2",1);
    $arg["data"]["OUTPUT2_2"] = $objForm->ge("OUTPUT2",2);

    //各種帳票ラジオ（1:受験番号順,2:成績順）
    $opt_sort[0]=1;
    $opt_sort[1]=2;

    if (!$model->output) $model->output = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "SORT",
                        "value"      => $model->output,
                        "extrahtml"  => "onclick =\" dis_sort(this);\"",
                        "multiple"   => $opt_sort));

    $arg["data"]["SORT1"] = $objForm->ge("SORT",1);
    $arg["data"]["SORT2"] = $objForm->ge("SORT",2);

    //各種帳票ラジオ（1:理科型,2:アラカルト型）
    $opt_type[0]=1;
    $opt_type[1]=2;

    if (!$model->output) $model->output = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTTYPE",
                        "value"      => $model->output,
                        "extrahtml"  => "disabled onclick =\" dis_type(this);\"",
                        "multiple"   => $opt_type));

    $arg["data"]["OUTTYPE1"] = $objForm->ge("OUTTYPE",1);
    $arg["data"]["OUTTYPE2"] = $objForm->ge("OUTTYPE",2);

    //各種帳票ラジオ（1:医薬進学のみ,2:全志願者）
    $opt_rika[0]=1;
    $opt_rika[1]=2;

    if (!$model->output) $model->output = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTRIKA",
                        "value"      => $model->output,
                        "extrahtml"  => "disabled ",
                        "multiple"   => $opt_rika));

    $arg["data"]["OUTRIKA1"] = $objForm->ge("OUTRIKA",1);
    $arg["data"]["OUTRIKA2"] = $objForm->ge("OUTRIKA",2);

    //各種帳票ラジオ（1:全志願者,2:コース別）
    $opt_ara[0]=1;
    $opt_ara[1]=2;

    if (!$model->output) $model->output = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTARAKALT",
                        "value"      => $model->output,
                        "extrahtml"  => "disabled onclick =\" dis_ara(this);\"",
                        "multiple"   => $opt_ara));

    $arg["data"]["OUTARAKALT1"] = $objForm->ge("OUTARAKALT",1);
    $arg["data"]["OUTARAKALT2"] = $objForm->ge("OUTARAKALT",2);

    //重複チェック項目チェックボックス
    if ($model->output == "on" || $model->cmd == ""){
        $checked = "checked";
    }
    $objForm->ae( array("type"      => "checkbox",
                        "name"      => "OUTPUT",
                        "extrahtml" => $checked." disabled ",
                        "value"     => "on"));

    $arg["data"]["OUTPUT"] = $objForm->ge("OUTPUT");

    //コース別チェックボックス NO002
    $checked = "";
    if ($model->output3 == "on"){
        $checked = "checked";
    }
    $objForm->ae( array("type"      => "checkbox",
                        "name"      => "OUTPUT3",
                        "extrahtml" => $checked." disabled ",
                        "value"     => "on"));

    $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT3");

    //中高判別フラグを作成する
    $jhflg = 0;
    $row = $db->getOne(knjl323kQuery::GetJorH());
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
                        "value"     => "KNJL323K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl323kQuery::getSpecialReasonDiv($model);
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
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "SPECIAL_REASON_DIV",
                        "value"     => $model->special_reason_div
                        ) );

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl323kForm1.html", $arg); 
    }
}
?>
