<?php

require_once('for_php7.php');

/********************************************************************/
/* 入試事前相談データ重複チェックリスト             山城 2005/09/13 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 : 出力順ラジオ追加                         山城 2005/10/26 */
/* NO002 : 追加と繰上をひとつにまとめる             山城 2005/12/20 */
/* NO003 : 受験番号順で合格者と不合格者の指定可     山城 2006/01/14 */
/* NO004 : 印刷日付を追加                           仲本 2006/02/07 */
/********************************************************************/

class knjl352kForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl352kForm1", "POST", "knjl352kindex.php", "", "knjl352kForm1");
    $db = Query::dbCheckOut();

    $opt=array();

    $arg["data"]["YEAR"] = $model->ObjYear;

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;

    $result = $db->query(knjl352kQuery::GetTestdiv($model));
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
                        "extrahtml" => "",
                        "options"   => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl352kQuery::getSpecialReasonDiv($model);
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
    $extra = "onChange=\"btn_submit('knjl352k')\"";
    $arg["data"]["SPECIAL_REASON_DIV"] = knjCreateCombo($objForm, "SPECIAL_REASON_DIV", $model->special_reason_div, $opt, $extra, 1);

    //出力対象ラジオ（1:一般受験者,2:附属出身者）---2005.08.23
    $opt_out[0]=1;
    $opt_out[1]=2;

    if (!$model->output2) $model->output2 = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT2",
                        "value"      => $model->output2,
                        "extrahtml"  => "onclick =\" return btn_submit('knjl352k');\"",
                        "multiple"   => $opt_out));

    $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT2",1);
    $arg["data"]["OUTPUT5"] = $objForm->ge("OUTPUT2",2);

    //各種帳票ラジオ（1:合格者全員,2:追加/繰上合格者）NO002
    $opt[0]=1;
    $opt[1]=2;
    $disable = "";
    if ($model->output2 == 2) $disable = "disabled";
    if (!$model->output) $model->output = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT",
                        "value"      => $model->output,
                        "extrahtml"  => $disable." onclick =\" return btn_submit('knjl352k');\"",
                        "multiple"   => $opt));

    $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
    $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",5);

    //追加繰上合格グループNoを作成する
    $opt_passdiv = array();

    $opt_passdiv[] = array("label" => "　　",
                           "value" => "99");

    $result = $db->query(knjl352kQuery::GetPassdiv($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_passdiv[] = array("label" => $row["GROUP_NO"],
                               "value" => $row["GROUP_NO"]);
    }
    
    if (!$model->passdiv) $model->passdiv = $opt_passdiv[0]["value"];

    $result->free();
    if (!$disable){
//NO002
//      $disable = ($model->output == 5 || $model->output == 6) ? "" : "disabled" ;
        $disable = ($model->output == 5) ? "" : "disabled" ;
    }

    $objForm->ae( array("type"      => "select",
                        "name"      => "PASSDIV",
                        "size"      => 1,
                        "value"     => $model->passdiv,
                        "extrahtml" => $disable,
                        "options"   => $opt_passdiv ) );

    $arg["data"]["PASSDIV"] = $objForm->ge("PASSDIV");

    //NO001
    //出力順ラジオ（1:合格コース順＆受験番号順,2:受験番号順）
    $opt_srt[0]=1;
    $opt_srt[1]=2;

    if (!$model->output3) $model->output3 = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT3",
                        "value"      => $model->output3,
                        "extrahtml"  => " onclick =\" setdisabled(this);\"",    //NO003
                        "multiple"   => $opt_srt));

    $arg["data"]["SORT1"] = $objForm->ge("OUTPUT3",1);
    $arg["data"]["SORT2"] = $objForm->ge("OUTPUT3",2);

    //受験番号順時の種別（1:合格者,2:不合格者）NO003
    $opt_pass[0]=1;
    $opt_pass[1]=2;
    $disable = "";
    if ($model->output3 == 1) $disable = "disabled";
    if (!$model->output4) $model->output4 = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT4",
                        "value"      => $model->output4,
                        "extrahtml"  => $disable,
                        "multiple"   => $opt_pass));

    $arg["data"]["PASS1"] = $objForm->ge("OUTPUT4",1);
    $arg["data"]["PASS2"] = $objForm->ge("OUTPUT4",2);

    //中高判別フラグを作成する
    $jhflg = 0;
    $row = $db->getOne(knjl352kQuery::GetJorH());
    if ($row == 1){
        $jhflg = 1;
    }else {
        $jhflg = 2;
    }
    $objForm->ae( array("type" => "hidden",
                        "name" => "JHFLG",
                        "value"=> $jhflg ) );

    //印刷日付//NO004
    if ($model->date == "") $model->date = str_replace("-","/",CTRL_DATE);
    $arg["data"]["DATE"] = View::popUpCalendar($objForm ,"DATE" ,$model->date);

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
                        "value"     => "KNJL352K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl352kForm1.html", $arg); 
    }
}
?>
