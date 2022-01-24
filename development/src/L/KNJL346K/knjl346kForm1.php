<?php
/********************************************************************/
/* 各種通知書                                       山城 2005/11/08 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 変更内容                                   name yyyy/mm/dd */
/* NO002 合格通知書 ラジオを追加に対応。            仲本 2006/01/05 */
/* NO003 ソート順指定をを合格通知書の場合に指定可   山城 2006/01/20 */
/********************************************************************/

class knjl346kForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl346kForm1", "POST", "knjl346kindex.php", "", "knjl346kForm1");
    $db = Query::dbCheckOut();

    $opt=array();

    $arg["data"]["YEAR"] = $model->ObjYear;

    //中高判別フラグを作成する
    $jhflg = 0;
    $row = $db->getOne(knjl346kQuery::GetJorH());
    if ($row == 1){
        $jhflg = 1;
    }else {
        $jhflg = 2;
    }
    $objForm->ae( array("type" => "hidden",
                        "name" => "JHFLG",
                        "value"=> $jhflg ) );

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;

    $result = $db->query(knjl346kQuery::GetTestdiv($model));
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
                        "extrahtml" => "onchange=\" return btn_submit('knjl346k');\"",
                        "options"   => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl346kQuery::getSpecialReasonDiv($model);
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
    $extra = "onchange=\" return btn_submit('knjl346k');\"";
    $arg["data"]["SPECIAL_REASON_DIV"] = knjCreateCombo($objForm, "SPECIAL_REASON_DIV", $model->special_reason_div, $opt, $extra, 1);

    //各種帳票ラジオ（1:合格通知書,2:通知書,3:入学許可書）
    $opt[0]=1;
    $opt[1]=2;
    $opt[2]=3;

    if (!$model->output) $model->output = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT",
                        "value"      => $model->output,
                        "extrahtml" => "onclick=\" return btn_submit('knjl346k');\"",
                        "multiple"   => $opt));

    if ($jhflg == "1"){
        $arg["JSCHL"] = $jhflg;
    }else {
        $arg["HSCHL"] = $jhflg;
    }

    $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
    $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
    $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);

    //----------↓----------NO002----------↓----------//

    //合格通知書用ラジオ（1:全員,2:追加／繰上グループNo）
    $opt_out[0]=1;
    $opt_out[1]=2;
    $dis_out[0] = "";
    $dis_out[1] = "disabled";
    $dis_out_no = 1;//合格通知書以外は全て選択不可とするため

    if (!$model->out) $model->out = 1;

    if ($model->output == 1) $dis_out_no = 0;
    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUT",
                        "value"      => $model->out,
                        "extrahtml"  => "onclick=\" return btn_submit('knjl346k');\" " .$dis_out[$dis_out_no],
                        "multiple"   => $opt_out));
    $arg["data"]["OUT1"] = $objForm->ge("OUT",1);
    $arg["data"]["OUT2"] = $objForm->ge("OUT",2);

    //全員用ラジオ（1:合格コース+受験番号順,2:受験番号順）
    $opt_sort[0]=1;
    $opt_sort[1]=2;

    if (!$model->sort) $model->sort = 1;

//    if ($model->output == 1) $dis_out_no = ($model->out == 1) ? 0 : 1;    //NO003
    $objForm->ae( array("type"       => "radio",
                        "name"       => "SORT",
                        "value"      => $model->sort,
                        "extrahtml"  => $dis_out[$dis_out_no],
                        "multiple"   => $opt_sort));
    $arg["data"]["SORT1"] = $objForm->ge("SORT",1);
    $arg["data"]["SORT2"] = $objForm->ge("SORT",2);

    //追加繰上グループNoを作成する
    $opt_passdiv = array();
    $opt_passdiv[] = array("label" => "　　",
                           "value" => "99");
    $result = $db->query(knjl346kQuery::GetPassdiv($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_passdiv[] = array("label" => $row["GROUP_NO"],
                               "value" => $row["GROUP_NO"]);
    }
    if (!$model->passdiv) $model->passdiv = $opt_passdiv[0]["value"];
    $result->free();

    if ($model->output == 1) $dis_out_no = ($model->out == 2) ? 0 : 1;
    $objForm->ae( array("type"      => "select",
                        "name"      => "PASSDIV",
                        "size"      => 1,
                        "value"     => $model->passdiv,
                        "extrahtml" => $dis_out[$dis_out_no],
                        "options"   => $opt_passdiv ) );
    $arg["data"]["PASSDIV"] = $objForm->ge("PASSDIV");

    //----------↑----------NO002----------↑----------//

    //印刷日付
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
                        "value"     => "KNJL346K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl346kForm1.html", $arg); 
    }
}
?>
