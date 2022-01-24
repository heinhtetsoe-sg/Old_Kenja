<?php

require_once('for_php7.php');

class knjc200_3Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjc200_3Form1", "POST", "knjc200_3index.php", "", "knjc200_3Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //クラス／担任
        $row = $db->getRow(knjc200_3Query::getHrName($model), DB_FETCHMODE_ASSOC);
        $arg["data"]["HR_NAME"] = $row["HR_NAME"] ." ／ " .$row["STAFFNAME"];

        //生徒
        $opt_schno = array();
        $opt_schno[] = array('label' => "　", 'value' => "");//初期値
        $result = $db->query(knjc200_3Query::getSchno($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_schno[] = array('label' => $row["ATTENDNO"] ."番　" .$row["NAME"],
                                 'value' => $row["SCHREGNO"]);
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "SCHREGNO",
                            "size"       => "1",
                            "value"      => $model->field["SCHREGNO"],
          					"extrahtml"  => "onChange=\"return btn_submit('schno');\"",
                            "options"    => $opt_schno));
        $arg["data"]["SCHREGNO"] = $objForm->ge("SCHREGNO");

        //直近データ(最大50件表示)
        $show_limit = 0;
        $opt_seqno = array();
        $opt_seqno[] = array('label' => "(((なし)))", 'value' => "");//初期値
        $show_seqno = array();
        $result = $db->query(knjc200_3Query::getList($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($show_limit == 50) break;
            $opt_seqno[] = array('label' => "受付番号：" .$row["SEQNO"] ."、更新年月日：" .str_replace("-","/",$row["UPDATED1"]),
                                 'value' => $row["SEQNO"]);
            $show_seqno[$row["SEQNO"]] = "受付番号：" .$row["SEQNO"] ." ／ 最終更新日時：" .str_replace("-","/",$row["UPDATED1"]) ." " .$row["UPDATED2"];
            $show_limit++;
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "SEQNO",
                            "size"       => "1",
                            "value"      => $model->field["SEQNO"],
          					"extrahtml"  => "onChange=\"return btn_submit('schno');\"",
                            "options"    => $opt_seqno));
        $arg["data"]["SEQNO"] = $objForm->ge("SEQNO");
        //受付番号／最終更新日時
        $arg["data"]["SHOW_SEQNO"] = $show_seqno[$model->field["SEQNO"]];

        //１レコード取得
        if ($model->field["SEQNO"] != "" && !isset($model->warning)) {
            $Row = $db->getRow(knjc200_3Query::getListRow($model), DB_FETCHMODE_ASSOC);
        } elseif (isset($model->warning)) {
            $Row =& $model->field;
        }

        //連絡元 9:保護者,1:生徒
        $Row["CONTACTER"] = str_replace("0","9",$Row["CONTACTER"]);
        if (!$Row["CONTACTER"]) $Row["CONTACTER"] = 9;
        $arg["data"]["CONTACTER1"] = ($Row["CONTACTER"] == 1) ? "checked" : "";
        $arg["data"]["CONTACTER9"] = ($Row["CONTACTER"] == 9) ? "checked" : "";

        //開始日付
        $date1 = isset($Row["FROMDATE"])?str_replace("-","/",$Row["FROMDATE"]):str_replace("-","/",CTRL_DATE);
        $arg["data"]["FROMDATE"] = View::popUpCalendar($objForm, "FROMDATE", $date1);

        //終了日付
        $date2 = isset($Row["TODATE"])?str_replace("-","/",$Row["TODATE"]):str_replace("-","/",CTRL_DATE);
        $arg["data"]["TODATE"] = View::popUpCalendar($objForm, "TODATE", $date2);

        //校時
        $data_cnt = 0;
        $opt_periodcd = $model->arr_period = array();
        $opt_periodcd[] = array('label' => "", 'value' => "");//初期値
        $result = $db->query(knjc200_3Query::getPeriodcd());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_periodcd[] = array('label' => $row["NAMECD2"] ."　" .$row["NAME1"],
                                    'value' => $row["NAMECD2"]);
            if ($data_cnt == 0) $model->s_period = $row["NAMECD2"];
            $model->e_period = $row["NAMECD2"];
            $model->arr_period[] = $row["NAMECD2"];
            $data_cnt++;
        }
        //開始校時
        $objForm->ae( array("type"       => "select",
                            "name"       => "FROMPERIOD",
                            "size"       => "1",
                            "value"      => $Row["FROMPERIOD"],
          					"extrahtml"  => "",
                            "options"    => $opt_periodcd));
        $arg["data"]["FROMPERIOD"] = $objForm->ge("FROMPERIOD");
        //終了校時
        $objForm->ae( array("type"       => "select",
                            "name"       => "TOPERIOD",
                            "size"       => "1",
                            "value"      => $Row["TOPERIOD"],
          					"extrahtml"  => "",
                            "options"    => $opt_periodcd));
        $arg["data"]["TOPERIOD"] = $objForm->ge("TOPERIOD");

        //勤怠
        $opt_dicd = array();
        $opt_dicd[] = array('label' => "(((なし)))", 'value' => "");//初期値
        $result = $db->query(knjc200_3Query::getDicd());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_dicd[] = array('label' => $row["NAME1"],
                                'value' => $row["NAMECD2"]);
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "DI_CD",
                            "size"       => "5",
                            "value"      => $Row["DI_CD"],
          					"extrahtml"  => "",
                            "options"    => $opt_dicd));
        $arg["data"]["DI_CD"] = $objForm->ge("DI_CD");

        //理由(備考)
        $opt_diremark = array();
        $opt_diremark[] = array('label' => "(((なし)))", 'value' => "");//初期値
        $result = $db->query(knjc200_3Query::getDiremark());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_diremark[] = array('label' => $row["DI_REMARK"],
                                    'value' => $row["DI_REMARK"]);
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "DI_REMARK",
                            "size"       => "10",
                            "value"      => $Row["DI_REMARK"],
          					"extrahtml"  => "",
                            "options"    => $opt_diremark));
        $arg["data"]["DI_REMARK"] = $objForm->ge("DI_REMARK");

        //その他
        $objForm->ae( array("type"        => "text",
        					"name"        => "SONOTA",
                            "size"        => 20,
                            "maxlength"   => 20,
                            "extrahtml"   => "",
                            "value"       => $Row["SONOTA"]));

        $arg["data"]["SONOTA"] = $objForm->ge("SONOTA");

        //返電 1:必要,9:不要
        $Row["CALLBACK"] = str_replace("0","9",$Row["CALLBACK"]);
        if (!$Row["CALLBACK"]) $Row["CALLBACK"] = 1;
        $arg["data"]["CALLBACK1"] = ($Row["CALLBACK"] == 1) ? "checked" : "";
        $arg["data"]["CALLBACK9"] = ($Row["CALLBACK"] == 9) ? "checked" : "";


        //DB切断
        $result->free();
        Query::dbCheckIn($db);


        //更新ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );


        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjc200_3Form1.html", $arg); 
    }
}
?>
