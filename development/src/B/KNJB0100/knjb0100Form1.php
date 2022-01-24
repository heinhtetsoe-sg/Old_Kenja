<?php

require_once('for_php7.php');

/********************************************************************/
/* 科目割当て                                       山城 2005/02/15 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：変更内容                                 name yyyy/mm/dd */
/********************************************************************/

class knjb0100Form1
{
    function main(&$model)
    {
        $objForm = new form;

        $db         = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //状態取得
        //（JAVASCRIPTで状態変更時にラベルを切り替え表示する。
        $state_name = array();
        $state_name[0] = "設定不可";
        $state_name[1] = "独占";
        $state_name[2] = "優先";
        $arg["data2"][] = array("state_cd" => 0, "state_name" => "設定不可");
        $arg["data2"][] = array("state_cd" => 1, "state_name" => "独占");
        $arg["data2"][] = array("state_cd" => 2, "state_name" => "優先");

        //校時ＣＤ取得
        //（JAVASCRIPTで校時ＣＤ変更時にラベルを切り替え表示する。
        $pdata2 = $db->query(knjb0100Query::Getperiod("B001",$RowR["PERIODCD"],"true"));
        $pcnt = 0 ;
        $model->pdata = "";
        $setpcnt = array();
        $arg["change_flg"] = $model->isWarning() ? "true" : "false";
        while($p2 = $pdata2->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($pcnt != 0) $model->pdata .= ",";
            $model->pdata .= $p2["NAMECD2"];
            $pcnt++;
            $setpcnt[$p2["NAMECD2"]] = $p2["name1"];
            $arg["data3"][] = array("pcntcd" => $p2["NAMECD2"], "pcnt_name" => $p2["NAME1"]);
        }

        //データセット
        $result = $db->query(knjb0100Query::Getdata($model));
        $subcnt = 0;
        while($RowR = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $pdata  = $db->query(knjb0100Query::Getperiod("B001",$RowR["PERIODCD"],"false"));
            $peri   = $pdata->fetchRow(DB_FETCHMODE_ASSOC);
            $period = $peri["NAME1"];
            if($RowR["HINTDIV"] == 1){
                $hint_show = "独占";
            } else if($RowR["HINTDIV"] == 2){
                $hint_show = "優先";
            } else if($RowR["HINTDIV"] == 0){
                $hint_show = "設定不可";
            } else {
                $hint_show = "";
            }
            //取消押下用Field
            if($model->cmd == "" || $model->cmd == "reset"){
                $model->field2["SUBCD"][$subcnt]      = $RowR["SUBCLASSCD"];
                $model->field2["STATE"][$subcnt]      = $RowR["HINTDIV"];
                $model->field2["PERIOD_CD"][$subcnt]  = $RowR["PERIODCD"];
            }
            $model->field["SUBCD"][]     = $RowR["SUBCLASSCD"];
            $model->field["STATE"][]     = $RowR["HINTDIV"];
            $model->field["PERIOD_CD"][] = $RowR["PERIODCD"];
            $get_data[] = array( "SUBCD"        => $subcnt,             //ID設定
                                 "SUBCDNAME"    => $RowR["SUBCLASSNAME"],
                                 "STATE"        => $RowR["HINTDIV"],
                                 "STATE_SHOW"   => $hint_show,
                                 "PERIOD_CD"    => $RowR["PERIODCD"],
                                 "PERIOD_SHOW"  => $period,
                                 "PERIOD_SHOW"  => $period);
            $subcnt++;
        }

        $result->free();

        //データ設定
        $enable = array();
        for ($i = 0;$i < get_count($get_data);$i++){
            $Row["SUBCD"] = $get_data[$i]["SUBCD"];
            $idx = $get_data[$i]["SUBCD"];

            //ID設定
            $Row["STATE_ID"] = "STATE_NAME".(int)$get_data[$i]["SUBCD"];
            $Row["PERIOD_ID"] = "PERIOD_NAME".(int)$get_data[$i]["SUBCD"];

            //取消時は、最初のデータを設定。取消以外は、現在表示データを設定。
            if ($model->cmd == "reset"){
                $setst = $model->field2["STATE"][$i];
            } else{
                $setst = $model->field["STATE"][$i];
            }
            $enable[$i] = "";
            if($setst == '0'){
                $enable[$i] = "readOnly = true";
            }
            $objForm->ae( array("type"        => "text",
                                "name"        => "STATE",
                                "size"        => 1,
                                "maxlength"   => 1,
                                "multiple"    => "1",
                                "extrahtml"   => "  OnChange=\"setName(this,".(int)$idx.",'0');\" id=\"".$Row["SUBCD"]."\" style=\"text-align:right;\"",
                                "value"       => ($model->error_flg ? $setst : $get_data[$i]["STATE"])));
            $Row["STATE"] = $objForm->ge("STATE");

            //取消時は、最初のデータを設定。取消以外は、現在表示データを設定。
            if ($model->cmd == "reset"){
                $setpr = $model->field2["PERIOD_CD"][$i];
            } else{
                $setpr = $model->field["PERIOD_CD"][$i];
            }
            $objForm->ae( array("type"        => "text",
                                "name"        => "PERIOD_CD",
                                "size"        => 4,
                                "maxlength"   => 4,
                                "multiple"    => "1",
                                "extrahtml"   => " OnChange=\"setName(this,".(int)$idx.",'1');\" id=\"".$Row["SUBCD"]."\" style=\"text-align:right;\"".$enable[$i],
                                "value"       => ($model->error_flg ? $setpr : $get_data[$i]["PERIOD_CD"])));
            $Row["PERIOD_CD"] = $objForm->ge("PERIOD_CD");

            $Row["SUBCDNAME"]       = $get_data[$i]["SUBCDNAME"];
            $Row["STATE_SHOW"]      = $get_data[$i]["STATE_SHOW"];
            $Row["PERIOD_SHOW"]     = $get_data[$i]["PERIOD_SHOW"];

            $arg["data"][] = $Row;

        }

        //ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onClick=\"btn_submit('update');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onClick=\"btn_submit('reset');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_update");
        $arg["btn_reset"]  = $objForm->ge("btn_reset");
        $arg["btn_end"]    = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd) );
        //校時ＣＤチェック用
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PERIOD_ALL",
                            "value"     => $model->pdata) );

        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjb0100index.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjb0100Form1.html", $arg);
    }
}
?>
