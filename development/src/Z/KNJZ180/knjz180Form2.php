<?php

require_once('for_php7.php');

class knjz180form2
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz180index.php", "", "edit");
        $temp_cd = "";
        //警告メッセージを表示しない場合
        if (isset($model->knjz180cd) && !isset($model->warning) && $model->knjz180cd !="") {
            $Row = knjz180Query::getRow($model->knjz180cd);
            $temp_cd = $Row["HOLIDAY"];
        } else {
            $Row =& $model->field;
        }

        //登録年度をlistに表示
        $year_select= common::DateConv1($model->knjz180cd, 12);

        //日付
        $arg["data"]["HOLIDAY"] =  View::popUpCalendar($objForm, "HOLIDAY", str_replace("-", "/", $Row["HOLIDAY"]), "");

        //備考
        $objForm->ae(array("type"        => "textarea",
                                "name"        => "REMARK",
                                "cols"        => 35,
                                "rows"        => 4,
                                "extrahtml"   => "onFocus=\"fcName=this.name\"",
                                "wrap"        => "soft",
                                "value"       => $Row["REMARK"] ));

        $arg["data"]["REMARK"] = $objForm->ge("REMARK");

        //追加ボタンを作成する
        $objForm->ae(array("type" => "button",
                                "name"        => "btn_add",
                                "value"       => "追 加",
                                "extrahtml"   => "onclick=\"return btn_submit('add');\"" ));

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae(array("type" => "button",
                                "name"        => "btn_update",
                                "value"       => "更 新",
                                "extrahtml"   => "onclick=\"return btn_submit('update');\"" ));

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成する
        $objForm->ae(array("type" => "button",
                                "name"        => "btn_del",
                                "value"       => "削 除",
                                "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ));

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae(array("type" => "button",
                                "name"        => "btn_reset",
                                "value"       => "取 消",
                                "extrahtml"   => "onclick=\"return Btn_reset('clear');\"" ));

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae(array("type"        => "button",
                                "name"        => "btn_end",
                                "value"       => "終了",
                                "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                                "name"      => "cmd"
                                ));

        $objForm->ae(array("type"      => "hidden",
                                "name"      => "org_knjz180cd",
                                "value"     => $model->org_knjz180cd));

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                                "name"      => "UPDATED",
                                "value"     => $Row["UPDATED"]
                                ));

        $objForm->ae(array("type"      => "hidden",
                                "name"      => "year_code",
                                "value"     => $model->year_code
                                ));
        if ($temp_cd=="") {
            $temp_cd = $model->field["temp_cd"];
        }

        $objForm->ae(array("type"      => "hidden",
                                "name"      => "temp_cd",
                                "value"     => $temp_cd
                                ));

        $cd_change = false;
        if ($temp_cd==$Row["HOLIDAY"]) {
            $cd_change = true;
        }

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && $model->cmd != "clear" && ($cd_change==true || $model->isload != 1)) {
            if (!isset($model->warning)) {
                $arg["reload"]  = "parent.left_frame.location.href='knjz180index.php?cmd=list&year_select=$year_select';";
            }
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz180Form2.html", $arg);
    }
}
