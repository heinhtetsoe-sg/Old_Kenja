<?php

require_once('for_php7.php');

class knjf090Form2
{

    function main(&$model)
    {


    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("edit", "POST", "knjf090index.php", "", "edit");

    $db = Query::dbCheckOut();

    if ($model->cmd == "chg_div" || $model->warning){
            $row = $model->field;
            //TREATMENT_DIV_CD を作成
            $row["TREATMENT_DIV_CD"] = $model->field["TREATMENT_DIV"];
    }else{
            //SQL文発行
            $query = knjf090Query::selectQuery($model);
            $model->org_data = $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
    }

    $arg["data"] = $row;

    //来室日時コンボボックス
    $opt1 = array();
    $result = $db->query(knjf090Query::getV_name_mst("F700",$model));
    while($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt1[] =  array("label" => $row1["NAMECD2"]."：".$row1["NAME1"],
                         "value" => $row1["NAMECD2"]);
    }
    $result->free();
    list($code, $v) = preg_split("/：/", $row["PERIOD"]);
    $objForm->ae( array("type"      => "select",
                        "name"      => "PERIOD",
                        "value"     => $code,
                        "options"   => $opt1));
    $arg["data"]["PERIOD"] = $objForm->ge("PERIOD");

    //診療区分コンボボックス
    $opt2 = array();
    $result = $db->query(knjf090Query::getV_name_mst("F710",$model));
    while($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt2[] =  array("label" => $row2["NAMECD2"]."：".$row2["NAME1"],
                         "value" => $row2["NAMECD2"]);
    }
    $result->free();
    list($code, $v) = preg_split("/：/", $row["TREATMENT_DIV"]);
    $objForm->ae( array("type"      => "select",
                        "name"      => "TREATMENT_DIV",
                        "value"     => $code,
                        "extrahtml" => "onchange=\"return btn_submit('chg_div');\"",
                        "options"   => $opt2));
    $arg["data"]["TREATMENT_DIV"] = $objForm->ge("TREATMENT_DIV");

    //来室理由コンボボックス
    $opt3 = array();
    $opt3[] = array("label" => "","value" => "0");
    $cd = ($row["TREATMENT_DIV_CD"] == '02')? 'F730' : 'F720';
    $result = $db->query(knjf090Query::getV_name_mst($cd,$model));
    while($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt3[] =  array("label" => $row3["NAMECD2"]."：".$row3["NAME1"],
                         "value" => $row3["NAMECD2"]);
    }
    $result->free();
    list($code, $v) = preg_split("/：/", $row["VISIT_REASON"]);
    $objForm->ae( array("type"      => "select",
                        "name"      => "VISIT_REASON",
                        "value"     => $code,
                        "options"   => $opt3));
    $arg["data"]["VISIT_REASON"] = $objForm->ge("VISIT_REASON");

    //症状が出た時間コンボボックス
    $opt4 = array();
    $opt4[] = array("label" => "","value" => "0");
    $result = $db->query(knjf090Query::getV_name_mst("F740",$model));
    while($row4 = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt4[] =  array("label" => $row4["NAMECD2"]."：".$row4["NAME1"],
                         "value" => $row4["NAMECD2"]);
    }
    $result->free();
    list($code, $v) = preg_split("/：/", $row["OCCURTIMECD"]);
    $objForm->ae( array("type"      => "select",
                        "name"      => "OCCURTIMECD",
                        "value"     => $code,
                        "options"   => $opt4));
    $arg["data"]["OCCURTIMECD"] = $objForm->ge("OCCURTIMECD");

    //体温
    $objForm->ae( array("type"        => "text",
                        "name"        => "TEMPERATURE",
                        "size"        => 4,
                        "maxlength"   => 4,
                        "extrahtml"   => "onblur=\"check(this)\"",
                        "value"       => $row["TEMPERATURE"]));

    $arg["data"]["TEMPERATURE"] = $objForm->ge("TEMPERATURE") ."　℃";

    //朝食コンボボックス
    $opt5 = array();
    $opt5[] = array("label" => "","value" => "0");
    $result = $db->query(knjf090Query::getV_name_mst("F750",$model));
    while($row5 = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt5[] =  array("label" => $row5["NAMECD2"]."：".$row5["NAME1"],
                         "value" => $row5["NAMECD2"]);
    }
    $result->free();
    list($code, $v) = preg_split("/：/", $row["BREAKFAST"]);
    $objForm->ae( array("type"      => "select",
                        "name"      => "BREAKFAST",
                        "value"     => $code,
                        "options"   => $opt5));
    $arg["data"]["BREAKFAST"] = $objForm->ge("BREAKFAST");

    //睡眠時間
    $item = array('DATE_H',
                  'DATE_M',
                  'BEDTIME_H',
                  'BEDTIME_M',
                  'RISINGTIME_H',
                  'RISINGTIME_M',
                  'OCCURTIME_H',
                  'OCCURTIME_M'
                  );

    $DATE = (isset($row["DATE"]))? str_replace("-","/",$row["DATE"]) : date('Y/m/d');
    if ($model->cmd != "chg_div" && !$model->warning){
            $row["DATE_H"] = (isset($row["TIME"]))? substr($row["TIME"], 0, 2) : date('H');
            $row["DATE_M"] = (isset($row["TIME"]))? substr($row["TIME"], 3, 2) : date('i');
    }
    foreach($item as $field){
            $objForm->ae( array("type"        => "text",
                                "name"        => $field,
                                "size"        => 2,
                                "maxlength"   => 2,
                                "extrahtml"   => "onblur=\"check(this)\"",
                                "value"       => $row[$field]));
    }

    $arg["data"]["BEDTIME"] = "就寝　：　" .$objForm->ge("BEDTIME_H") ."　時　" 
                              .$objForm->ge("BEDTIME_M") ."　分　／　起床：" 
                              .$objForm->ge("RISINGTIME_H") ."　時　" .$objForm->ge("RISINGTIME_M") ."　分";

    //来室日時
    $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $DATE) ."　　　" .$objForm->ge("DATE_H")."　時　" .$objForm->ge("DATE_M") ."　分　";

    //症状が出た時間
    $arg["data"]["OCCURTIME"] = $objForm->ge("OCCURTIME_H")."　時　" .$objForm->ge("OCCURTIME_M") ."　分　";

    //睡眠状況コンボボックス
    $opt6 = array();
    $opt6[] = array("label" => "","value" => "0");
    $result = $db->query(knjf090Query::getV_name_mst("F760",$model));
    while($row6 = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt6[] =  array("label" => $row6["NAMECD2"]."：".$row6["NAME1"],
                         "value" => $row6["NAMECD2"]);
    }
    $result->free();
    list($code, $v) = preg_split("/：/", $row["SLEEPING"]);
    $objForm->ae( array("type"      => "select",
                        "name"      => "SLEEPING",
                        "value"     => $code,
                        "options"   => $opt6));
    $arg["data"]["SLEEPING"] = $objForm->ge("SLEEPING");

    //保健室での処置コンボボックス
    $opt7 = array();
    $opt7[] = array("label" => "","value" => "0");
    $result = $db->query(knjf090Query::getV_name_mst("F770",$model));
    while($row7 = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt7[] =  array("label" => $row7["NAMECD2"]."：".$row7["NAME1"],
                         "value" => $row7["NAMECD2"]);
    }
    $result->free();
    list($code, $v) = preg_split("/：/", $row["NURSETREAT"]);    
    $objForm->ae( array("type"      => "select",
                        "name"      => "NURSETREAT",
                        "value"     => $code,
                        "options"   => $opt7));
    $arg["data"]["NURSETREAT"] = $objForm->ge("NURSETREAT");

    //備考
    $objForm->ae( array("type"        => "text",
                        "name"        => "REMARK",
                        "size"        => 80,
                        "maxlength"   => 80,
                        "extrahtml"   => "onblur=\"check(this)\"",
                        "value"       => $row["REMARK"]));

    $arg["data"]["REMARK"] = $objForm->ge("REMARK");
/*
    $item = array('PERIOD' => 'F180',
                  'TREATMENT_DIV'   => 'F160',
                  'VISIT_REASON'    => ($row["TREATMENT_DIV_CD"] == '02')? 'F170' : 'F230',
                  'OCCURTIMECD'     => 'F190',
                  'BREAKFAST'       => 'F210',
                  'SLEEPING'        => 'F200',
                  'NURSETREAT'      => 'F220'
                 );


    //来室校時・来室診療区分・来室理由・症状がでた時間・朝食・睡眠状況・保健室での処置のコンボ
    $result = $db->query(knjf090Query::selectQueryCombo());
    $opt = array();
    while($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {

        if($key != $row2["NAMECD1"]){
            if ($row2["NAMECD1"] != "F180" && $row2["NAMECD1"] != "F160") {
                $opt[$row2["NAMECD1"]][]  = array("label" => "","value" => "00");
            }
            $key = $row2["NAMECD1"];
        }
        $opt[$row2["NAMECD1"]][]  = array("label" => $row2["NAMECD2"] ."&nbsp;".htmlspecialchars($row2["NAME1"]),
                                          "value" => $row2["NAMECD2"]);

    }

    foreach($item as $field => $namecd1){

        //list($code, $v) = preg_split("/　/", $row[$field]);

        //分割対象文字を変更
        list($code, $v) = preg_split("/：/", $row[$field]);
    
        $objForm->ae( array("type"    => "select",
                            "name"    => $field,
                            "size"    => "1",
                            "value"   => $code,
                            "extrahtml" => ($field == "TREATMENT_DIV")? "onchange=\"return btn_submit('chg_div');\"" : "" ,
                            "options" => $opt[$namecd1]));
        $arg["data"][$field] = $objForm->ge($field);
    }
*/
    Query::dbCheckIn($db);

    $arg["SCHREGNO"]    = $model->schregno;
    $arg["NAME"]        = $model->name;

    //追加ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_add",
                        "value"       => "追 加",
                        "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );
    $arg["button"]["btn_add"] = $objForm->ge("btn_add");

    //修正ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_update",
                        "value"       => "更 新",
                        "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
    $arg["button"]["btn_update"] = $objForm->ge("btn_update");

    //削除ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_del",
                        "value"       => "削 除",
                        "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
    $arg["button"]["btn_del"] = $objForm->ge("btn_del");

    //クリアボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_reset",
                        "value"       => "取 消",
                        "extrahtml"   => "onclick=\"return btn_submit('reset');\"") );
    $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

    //終了ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_end",
                        "value"       => "終 了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ) );
    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"));

    $arg["finish"]  = $objForm->get_finish();
    if ($model->isMessage()){
            $arg["reload"] = "window.open('knjf090index.php?cmd=main','right_frame');";
    }

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjf090Form2.html", $arg);

    }
}
?>
