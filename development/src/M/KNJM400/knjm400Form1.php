<?php

require_once('for_php7.php');
/********************************************************************/
/* スクーリングチェックリスト                       山城 2005/03/12 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm400Form1
{
    function main(&$model){

    //オブジェクト作成
    $objForm = new form;

    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjm400Form1", "POST", "knjm400index.php", "", "knjm400Form1");

    //スクーリング種別リストボックスを作成する
    $opt_kinname = array();
    $namecnt = 0;

    $db = Query::dbCheckOut();
    
    if($model->Properties["useTsushinSemesKonboHyoji"] == '1') {
        $arg["data"]["SELECT_SEMESTER_FLAG"] = true;
    }
    //学期
    $opt = array();
    $query = knjm400Query::getSemesterMst();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $model->field["SELECT_SEMESTER"] = ($model->field["SELECT_SEMESTER"]) ? $model->field["SELECT_SEMESTER"] : CTRL_SEMESTER;
    $extra = "onChange=\"return btn_submit('main');\"";
    $arg["data"]["SELECT_SEMESTER"] = knjCreateCombo($objForm, "SELECT_SEMESTER", $model->field["SELECT_SEMESTER"], $opt, $extra, 1);

    
    $query = knjm400Query::GetName("M001");
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_grade[] = array('label' => $row["NAME1"],
                             'value' => $row["NAMECD2"]);
        $namecnt++;
    }
    if($model->field["KINNAME"]=="") $model->field["KINNAME"] = $opt_grade[0]["value"];
    $result->free();

    $z010 = $db->getOne(knjm400Query::getZ010());
    Query::dbCheckIn($db);
    if ($z010 == "sagaken") {
        $extra = "multiple onchange = \"return kin(this, [3, 4])\"";
    } else {
        $extra = "multiple onchange = \"return kin(this, [1, 2, 3])\"";
    }
    $objForm->ae( array("type"       => "select",
                        "name"       => "KINNAME",
                        "size"       => $namecnt,
                        "value"      => $model->field["KINNAME"],
                        "options"    => $opt_grade,
                        "extrahtml"  => $extra ) );

    $arg["data"]["KINNAME"] = $objForm->ge("KINNAME");

    //日付コンボを作成する
    $opt_date = array();
    $i = 1;

    $db = Query::dbCheckOut();
    
    $opt_date[0] = array('label' => str_replace("-","/",CTRL_DATE),
                          'value' => CTRL_DATE);
    if($model->Properties["useTsushinSemesKonboHyoji"] == '1') {
        $query = knjm400Query::getSemesterMst();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if($row['VALUE'] == $model->field['SELECT_SEMESTER']){
                if(strtotime($row['SDATE'])>strtotime($opt_date[0]['value']) || strtotime($opt_date[0]['value'])>strtotime($row['EDATE'])){
                	unset($opt_date[0]);
                	$i = 0;
                }
            }
        }
    }
    $query = knjm400Query::GetDate($model);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_date[$i] = array('label' => str_replace("-","/",$row["EXECUTEDATE"]),
                            'value' => $row["EXECUTEDATE"]);
        $i++;
    }
    if($model->field["DATE"]=="") $model->field["DATE"] = $opt_date[0]["value"];
    $result->free();
    Query::dbCheckIn($db);

    $objForm->ae( array("type"       => "select",
                        "name"       => "DATE",
                        "size"       => "1",
                        "value"      => $model->field["DATE"],
                        "extrahtml"  => "",
                        "options"    => $opt_date));

    $arg["data"]["DATE"] = $objForm->ge("DATE");

    //講座コンボを作成する
    $opt_chr = array();
    $i = 1;

    $db = Query::dbCheckOut();
    $query = knjm400Query::GetChr($model);
    $result = $db->query($query);
    $opt_chr[0] = array('label' => "",
                        'value' => 0);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_chr[$i] = array('label' => str_replace("-","/",$row["CHAIRNAME"]),
                             'value' => $row["CHAIRCD"]);
        $i++;
    }
    if($model->field["CHRNAME"]=="") $model->field["CHRNAME"] = $opt_chr[0]["value"];
    $result->free();
    Query::dbCheckIn($db);

    $objForm->ae( array("type"       => "select",
                        "name"       => "CHRNAME",
                        "size"       => "1",
                        "value"      => $model->field["CHRNAME"],
                        "extrahtml"  => "",
                        "options"    => $opt_chr));

    $arg["data"]["CHRNAME"] = $objForm->ge("CHRNAME");

    //校時コンボを作成する
    $opt_schltime = array();

    $db = Query::dbCheckOut();
    $query = knjm400Query::GetName("B001");
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_schltime[] = array('label' => $row["NAME1"],
                                'value' => $row["NAMECD2"]);
        $namecnt++;
    }
    if($model->field["SCHLTIME"]=="") $model->field["SCHLTIME"] = $opt_schltime[0]["value"];
    $result->free();
    Query::dbCheckIn($db);

    $objForm->ae( array("type"       => "select",
                        "name"       => "SCHLTIME",
                        "size"       => "1",
                        "value"      => $model->field["SCHLTIME"],
                        "extrahtml"  => "",
                        "options"    => $opt_schltime));

    $arg["data"]["SCHLTIME"] = $objForm->ge("SCHLTIME");

    //印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
    $objForm->ae( array("type"      => "button",
                        "name"      => "btn_print",
                        "value"     => "プレビュー／印刷",
                        "extrahtml" => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

    $arg["button"]["btn_print"] = $objForm->ge("btn_print");

    //終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
    $objForm->ae( array("type"      => "button",
                        "name"      => "btn_end",
                        "value"     => "終 了",
                        "extrahtml" => "onclick=\"closeWin();\"" ) );

    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

    //hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "DBNAME",
                        "value"     => DB_DATABASE
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "YEAR",
                        "value"     => CTRL_YEAR
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "SEMESTER",
                        "value"     => CTRL_SEMESTER
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "PRGID",
                        "value"     => "KNJM400"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    //フォーム終わり
    $arg["finish"]  = $objForm->get_finish();

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjm400Form1.html", $arg); 

    }
}
?>
