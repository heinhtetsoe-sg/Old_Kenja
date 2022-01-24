<?php

require_once('for_php7.php');

class knjh070form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh070index.php", "", "edit");

        $db = Query::dbCheckOut();
        //警告メッセージを表示しない場合
        if (isset($model->schregno) && isset($model->traindate) && !isset($model->warning)){
            $query = knjh070Query::selectQuery($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }else{
            $Row =& $model->field;
        }
        //指導日付
        $arg["data"]["TRAINDATE"] = View::popUpCalendar($objForm, "TRAINDATE", str_replace("-", "/" ,$Row["TRAINDATE"]));

        $query = knjh070Query::getName();
        $result = $db->query($query);
        $opt['H301'] = $opt['H302'] = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            $opt[$row["NAMECD1"]][]  = array("label" => $row["NAMECD2"]."&nbsp;".$row["NAME1"],
                                             "value" => $row["NAMECD2"]);
        }

        //対応者を取得
        $query = knjh070Query::selectStaffQuery($model);
        $result = $db->query($query);
        $opt_staff = array();
        list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            $ume = "" ;
            for ($umecnt = 1; $umecnt <= strlen($row["STAFFCD"]) - (int)$simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $SET_VALUE = $ume.substr($row["STAFFCD"], (strlen($row["STAFFCD"]) - (int)$simo), (int)$simo);
            } else {
                $SET_VALUE = $row["STAFFCD"];
            }
            $row["STAFFNAME_SHOW"] = str_replace($row["STAFFCD"], $SET_VALUE, $row["STAFFNAME_SHOW"]);
            $opt_staff[]  = array("label" => $row["STAFFNAME_SHOW"],
                                  "value" => $row["STAFFCD"]);
        }
        $result->free();
        Query::dbCheckIn($db);
        //相談者
        $objForm->ae( array("type"      =>      "select",
                            "name"      =>      "PATIENTCD",
                            "size"      =>      "1",
                            "value"     =>      $Row["PATIENTCD"],
                            "options"   =>      $opt["H301"]));
        $arg["data"]["PATIENTCD"] = $objForm->ge("PATIENTCD");

        //対応者
        $objForm->ae( array("type"      =>      "select",
                            "name"      =>      "STAFFCD",
                            "size"      =>      "1",
                            "value"     =>      ((isset($Row["STAFFCD"]))? $Row["STAFFCD"] : STAFFCD),
                            "options"   =>      $opt_staff));

        $arg["data"]["STAFFCD"] = $objForm->ge("STAFFCD");

        //指導方法
        $objForm->ae( array("type"      =>      "select",
                            "name"      =>      "HOWTOTRAINCD",
                            "size"      =>      "1",
                            "value"     =>      $Row["HOWTOTRAINCD"],
                            "options"   =>      $opt["H302"]));

        $arg["data"]["HOWTOTRAINCD"] = $objForm->ge("HOWTOTRAINCD");

        //指導内容
        $height = (int)$model->content_gyou * 13.5 + ((int)$model->content_gyou -1 ) * 3 + 5;
        $objForm->ae( array("type"      =>      "textarea",
                            "name"      =>      "CONTENT",
                            "cols"      =>      ((int)$model->content_moji * 2 + 1),
                            "rows"      =>      $model->content_gyou,
                            "extrahtml" =>      "style=\"height:{$height}px;\"",
                            "wrap"      =>      "soft",
                            "value"     =>      $Row["CONTENT"] ));
        $arg["data"]["CONTENT"] = $objForm->ge("CONTENT");


        //追加ボタンを作成する
        $objForm->ae( array("type"      =>      "button",
                            "name"      =>      "btn_add",
                            "value"     =>      "追 加",
                            "extrahtml" =>      "onclick=\"return btn_submit('add');\""));
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type"      =>      "button",
                            "name"      =>      "btn_update",
                            "value"     =>      "更 新",
                            "extrahtml" =>      "onclick=\"return btn_submit('update');\""));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成する
        $objForm->ae( array("type"      =>   "button",
                            "name"      =>      "btn_del",
                            "value"     =>      "削 除",
                            "extrahtml" =>      "onclick=\"return btn_submit('delete');\""));
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type"      =>      "button",
                            "name"      =>      "btn_reset",
                            "value"     =>      "取 消",
                            "extrahtml" =>      "onclick=\"return Btn_reset('edit');\""));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type"      =>      "button",
                            "name"      =>      "btn_end",
                            "value"     =>      "終了",
                            "extrahtml" =>      "onclick=\"closeWin();\""));
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      =>      "hidden",
                            "name"      =>      "cmd"));

        //hiddenを作成する
        $objForm->ae( array("type"      =>      "hidden",
                            "name"      =>      "UPDATED",
                            "value"     =>      $Row["UPDATED"]));

        $objForm->ae( array("type"      =>      "hidden",
                            "name"      =>      "SCHREGNO",
                            "value"     =>      $model->schregno));

        if ($temp_cd=="") $temp_cd = $model->field["temp_cd"];

        $objForm->ae( array("type"      =>      "hidden",
                            "name"      =>      "temp_cd",
                            "value"     =>      $temp_cd));

        $cd_change = false;
        if ($temp_cd==$Row["SCHREGNO"] ) $cd_change = true;

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "clear" && ($cd_change==true || $model->isload != 1)){
            $arg["reload"]  = "window.open('knjh070index.php?cmd=list&SCHREGNO=$model->schregno','top_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh070Form2.html", $arg);
    }
}        

?>
