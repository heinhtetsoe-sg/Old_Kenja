<?php

require_once('for_php7.php');

class knjl361cForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $db = Query::dbCheckOut();

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl361cindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $query = knjl361cQuery::getRow($model->year,$model->itemcd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }else{
            $Row =& $model->field;
        }

    /****************/
    /**テキスト作成**/
    /****************/
        //コースコードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["ITEMCD"] = knjCreateTextBox($objForm, $Row["ITEMCD"], "ITEMCD", 2, 2, $extra);

        //コース名テキストボックス
        $extra = "";
        $arg["data"]["ITEMNAME"] = knjCreateTextBox($objForm, $Row["ITEMNAME"], "ITEMNAME", 20, 30, $extra);

        //コース定員テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MONEY_BOY"] = knjCreateTextBox($objForm, $Row["MONEY_BOY"], "MONEY_BOY", 10, 8, $extra);

        //コース定員テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MONEY_GIRL"] = knjCreateTextBox($objForm, $Row["MONEY_GIRL"], "MONEY_GIRL", 10, 8, $extra);

    /**************/
    /**ボタン作成**/
    /**************/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '終 了', $extra);

        Query::dbCheckIn($db);
        
        //hidden
        knjCreateHidden($objForm, "cmd", "");

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjl361cindex.php?cmd=list"
                            . "&year=" .$model->year."';";
        }
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl361cForm2.html", $arg);
    }
}

?>
