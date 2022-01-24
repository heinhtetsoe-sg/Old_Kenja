<?php

require_once('for_php7.php');

class knjz241Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz241index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning))
        {
            $Row = knjz241Query::getRow($model->questioncd);
        } else {
            $Row =& $model->field;
        }

        //コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "QUESTIONCD",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["QUESTIONCD"] ));
        $arg["data"]["QUESTIONCD"] = $objForm->ge("QUESTIONCD");

        //質問
        $objForm->ae( array("type"        => "text",
                            "name"        => "CONTENTS",
                            "size"        => 52,
                            "maxlength"   => 52,
                            "extrahtml"   => "",
                            "value"       => $Row["CONTENTS"] ));
        $arg["data"]["CONTENTS"] = $objForm->ge("CONTENTS");

        //表示順
        $objForm->ae( array("type"        => "text",
                            "name"        => "SORT",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $Row["SORT"] ));
        $arg["data"]["SORT"] = $objForm->ge("SORT");

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz241index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz241Form2.html", $arg); 
    }
}
?>
