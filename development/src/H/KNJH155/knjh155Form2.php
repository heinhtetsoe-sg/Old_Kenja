<?php

require_once('for_php7.php');

class knjh155form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh155index.php", "", "edit");

        //表彰実績区分
        $opt_detaildiv = array();
        $opt_detaildiv[] = array("label" => "自転車データ","value" => "3");

        if (!$model->detaildiv) $model->detaildiv = $opt_detaildiv[0]["value"];
        if ($model->cmd == "clear") $model->detaildiv = $model->org_detaildiv;

        $objForm->ae( array("type"      => "select",
                            "name"      => "DETAIL_DIV",
                            "size"      => "1",
                            "value"     => $model->detaildiv,
                            "extrahtml" => "Onchange=\" return btn_submit('edit2')\"",
                            "options"   => $opt_detaildiv));
        $arg["data"]["DETAIL_DIV"] = $objForm->ge("DETAIL_DIV");

        //警告メッセージを表示しない場合
        if ($model->dtclick && !isset($model->warning)){
            $Row = knjh155Query::getRow($model->detail_edate,$model->detail_sdate,$model->schregno,$model->detaildiv);
            $temp_cd = $Row["SCHREGNO"];
        }else{
            if ($model->cmd == "clear"){
                $Row =& $model->clear;
            }else {
                $Row =& $model->field;
            }
        }

        //取消押下時退避用
        if ($model->dtclick){
            $model->clear = array("DETAIL_SDATE"    => $Row["DETAIL_SDATE"],    //登録日付
                                  "DETAIL_EDATE"    => $Row["DETAIL_EDATE"],    //終了日
                                  "DETAIL_DIV"      => $Row["DETAIL_DIV"],      //詳細区分
                                  "CONTENT"         => $Row["CONTENT"],         //賞罰内容
                                  "REMARK"          => $Row["REMARK"],          //備考
                                  "BICYCLE_CD"      => $Row["BICYCLE_CD"],      //自転車許可番号
                                  "BICYCLE_NO"      => $Row["BICYCLE_NO"]       //駐輪所番号
                                  );
        }

        //登録日付
        $date_ymd = strtr($model->detail_sdate,"-","/");
        $arg["data"]["DETAIL_SDATE"] = View::popUpCalendar($objForm, "DETAIL_SDATE", $date_ymd);
        //終了日付
        $date_ymd = strtr($model->detail_edate,"-","/");
        $arg["data"]["DETAIL_EDATE"] = View::popUpCalendar($objForm, "DETAIL_EDATE", $date_ymd);

        //自転車許可番号
        $objForm->ae( array("type"      => "text",
                            "name"      => "BICYCLE_CD",
                            "size"      => 8,
                            "maxlength" => 8,
                            "extrahtml" => "onblur=\"this.value=toInteger(this.value);\"",
                            "value"     => $Row["BICYCLE_CD"]));
        $arg["data"]["BICYCLE_CD"] = $objForm->ge("BICYCLE_CD");

        //駐輪場番号
        $objForm->ae( array("type"      => "text",
                            "name"      => "BICYCLE_NO",
                            "size"      => 4,
                            "maxlength" => 4,
                            "extrahtml" => "",
                            "value"     => $Row["BICYCLE_NO"]));
        $arg["data"]["BICYCLE_NO"] = $objForm->ge("BICYCLE_NO");

        //詳細内容
        $objForm->ae( array("type"      => "textarea",
                            "name"      => "CONTENT",
                            "cols"      => 50,
                            "rows"      => 3,
                            "extrahtml" => "",
                            "wrap"      => "soft",
                            "value"     => $Row["CONTENT"] ));
        $arg["data"]["CONTENT"] = $objForm->ge("CONTENT");

        //備考
        $objForm->ae( array("type"      => "textarea",
                            "name"      => "REMARK",
                            "cols"      => 50,
                            "rows"      => 2,
                            "extrahtml" => "",
                            "wrap"      => "soft",
                            "value"     => $Row["REMARK"]));
        $arg["data"]["REMARK"] = $objForm->ge("REMARK");

        //追加ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_add",
                            "value"     => "追 加",
                            "extrahtml" => "onclick=\"return btn_submit('add');\""));
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_update",
                            "value"     => "更 新",
                            "extrahtml" => "onclick=\"return btn_submit('update');\""));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_del",
                            "value"     => "削 除",
                            "extrahtml" => "onclick=\"return btn_submit('delete');\""));
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return Btn_reset('clear');\""));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終了",
                            "extrahtml" => "onclick=\"closeWin();\""));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      =>      "hidden",
                            "name"      =>      "cmd"));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "org_detail_sdate",
                            "value"     => strtr($model->org_detail_sdate,"/","-")) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "org_detail_edate",
                            "value"     => strtr($model->org_detail_edate,"/","-")) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "org_detaildiv",
                            "value"     => $model->org_detaildiv) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno));

        if ($temp_cd=="") $temp_cd = $model->field["temp_cd"];

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "temp_cd",
                            "value"     => $temp_cd));

        $cd_change = false;
        if ($temp_cd==$Row["SCHREGNO"] ) $cd_change = true;

        $arg["finish"]  = $objForm->get_finish();
        if (!$model->prg && VARS::get("cmd") != "edit" && VARS::get("cmd") != "clear" && ($cd_change==true || $model->isload != 1)){
            $arg["reload"]  = "window.open('knjh155index.php?cmd=list&SCHREGNO=$model->schregno','right_frame');";
//            $arg["reload"]  = "parent.top_frame.location.href='knjh155index.php?cmd=list&SCHREGNO=$model->schregno';";
        }else if ($model->prg && VARS::get("cmd") != "edit" && VARS::get("cmd") != "clear" && ($cd_change==true || $model->isload != 1)){
            $arg["reload"]  = "window.open('knjh155index.php?cmd=list&SCHREGNO=$model->schregno','top_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh155Form2.html", $arg);
    }
}       

?>
