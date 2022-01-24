<?php

require_once('for_php7.php');

class knjp190qform2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp190qindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if(isset($model->schregno) && isset($model->grantcd) && !isset($model->warning)){
            $Row = knjp190qQuery::getRow($model->grantcd,$model->schregno);
            $model->temp_cd = $Row["GRANTCD"];
        }else{
            $model->field["GRANTCD"] = $model->field["temp_cd"];
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        $query = knjp190qQuery::getName();
        $result = $db->query($query);
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["NAMECD2"].":".$row["NAME1"],
                           "value" => $row["NAMECD2"]);
        }
        $result->free();

        Query::dbCheckIn($db);

        //交付コード
        $objForm->ae( array("type"      => "select",
                            "name"      => "GRANTCD",
                            "size"      => "1",
                            "value"     => $Row["GRANTCD"],
                            "options"   => $opt));
        $arg["data"]["GRANTCD"] = $objForm->ge("GRANTCD");

        //交付年度
        $objForm->ae( array("type"      => "text",
                            "name"      => "GRANTYEAR",
                            "size"      => 4,
                            "maxlength" => 4,
                            "extrahtml" => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"     => $Row["GRANTYEAR"] ));
        $arg["data"]["GRANTYEAR"] = $objForm->ge("GRANTYEAR");

        //交付開始日
        $sdate_ymd = strtr($Row["GRANTSDATE"],"-","/");
        $arg["data"]["GRANTSDATE"] = View::popUpCalendar($objForm, "GRANTSDATE", $sdate_ymd);

        //交付終了日
        $edate_ymd = strtr($Row["GRANTEDATE"],"-","/");
        $arg["data"]["GRANTEDATE"] = View::popUpCalendar($objForm, "GRANTEDATE", $edate_ymd);

        //交付金額
        $objForm->ae( array("type"      => "text",
                            "name"      => "GRANT_MONEY",
                            "size"      => 9,
                            "maxlength" => 9,
                            "extrahtml" => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"     => $Row["GRANT_MONEY"] ));
        $arg["data"]["GRANT_MONEY"] = $objForm->ge("GRANT_MONEY");

        //備考
        $objForm->ae( array("type"      => "text",
                            "name"      => "REMARK",
                            "size"      => 50,
                            "maxlength" => 75,
                            "extrahtml" => "",
                            "value"     => $Row["REMARK"] ));
        $arg["data"]["REMARK"] = $objForm->ge("REMARK");

        //追加ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_add",
                            "value"     => "追 加",
                            "extrahtml" => "onclick=\"return btn_submit('add');\""));
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_update",
                            "value"     => "更 新",
                            "extrahtml" => "onclick=\"return btn_submit('update');\""));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_del",
                            "value"     => "削 除",
                            "extrahtml" => "onclick=\"return btn_submit('delete');\""));
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"return Btn_reset('clear');\""));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\""));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]));
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno));

        if($model->temp_cd=="") $model->temp_cd = $model->field["temp_cd"];

        $objForm->ae( array("type"      =>      "hidden",
                            "name"      =>      "temp_cd",
                            "value"     =>      $model->temp_cd));

#        $cd_change = false;
#        if ($temp_cd==$Row["SCHREGNO"] ) $cd_change = true;

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "clear" && ($cd_change==true || $model->isload != 1)){
                $arg["reload"]  = "window.open('knjp190qindex.php?cmd=list&SCHREGNO=$model->schregno','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp190qForm2.html", $arg);
    }
}        

?>
