<?php

require_once('for_php7.php');

class knjl030yForm2
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl030yindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //会場データ
        if ($model->isWarning() || $model->cmd == "detail") {
            $Row =& $model->field;
        } else {
            $query = knjl030yQuery::selectQuery($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        //会場名
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMHALL_NAME",
                            "size"        => 30,
                            "maxlength"   => 30,
                            "value"       => $Row["EXAMHALL_NAME"]
                            ));
        $arg["EXAMHALL_NAME"] = $objForm->ge("EXAMHALL_NAME");
/***
        //会場人数
        $objForm->ae( array("type"        => "text",
                            "name"        => "CAPA_CNT",
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => $Row["CAPA_CNT"]
                            ));
        $arg["CAPA_CNT"] = $objForm->ge("CAPA_CNT");
        $arg["CAPA_CNT"] = $Row["CAPA_CNT"];
***/
        //分割数
        $objForm->ae( array("type"        => "text",
                            "name"        => "DETAIL_CNT",
                            "size"        => 5,
                            "maxlength"   => 1,
                            "extrahtml"   => "style=\"text-align:right\" onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => $Row["DETAIL_CNT"]
                            ));
        $arg["DETAIL_CNT"] = $objForm->ge("DETAIL_CNT");



        //分割データ
        if ($model->isWarning() || $model->cmd == "detail") {
            for ($detail_no = 1; $detail_no <= $Row["DETAIL_CNT"]; $detail_no++) {
                $name = "DETAIL_CAPA_CNT";
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name .$detail_no,
                                    "size"        => 5,
                                    "maxlength"   => 3,
                                    "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"",
                                    "value"       => $Row[$name .$detail_no]
                                    ));
                $detail_capa_cnt = $objForm->ge($name .$detail_no);

                $row = array();
                $row = array("DETAIL_NO"         => $detail_no,
                             "DETAIL_CAPA_CNT"   => $detail_capa_cnt,
                             "DETAIL_S_RECEPTNO" => "",
                             "DETAIL_E_RECEPTNO" => "",
                             "DETAIL_S_EXAMNO"   => "",
                             "DETAIL_E_EXAMNO"   => "");

                $arg["data2"][] = $row;
            }
        } else {
            $query = knjl030yQuery::getDetailList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $name = "DETAIL_CAPA_CNT";
                $objForm->ae( array("type"        => "text",
                                    "name"        => $name .$row["DETAIL_NO"],
                                    "size"        => 5,
                                    "maxlength"   => 3,
                                    "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"",
                                    "value"       => $row[$name]
                                    ));
                $row[$name] = $objForm->ge($name .$row["DETAIL_NO"]);

                $arg["data2"][] = $row;
            }
            $result->free();
        }

        //確定ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_detail",
                            "value"       => "確 定",
                            "extrahtml"   => "onclick=\"return btn_submit('detail')\"" ) );
        $arg["btn_detail"]  = $objForm->ge("btn_detail");



        //追加・更新ボタン
        if ($model->mode == "update"){
            $value = "更 新";
        }else{
            $value = "追 加";
        }
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => $value,
                            "extrahtml"   => "onclick=\"return btn_submit('".$model->mode ."')\"" ) );
        $arg["btn_update"]  = $objForm->ge("btn_update");

        //戻るボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"top.main_frame.closeit()\"" ) );
        $arg["btn_back"]  = $objForm->ge("btn_back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl030yForm2.html", $arg); 
    }
}
?>
