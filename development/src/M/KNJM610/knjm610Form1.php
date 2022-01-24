<?php

require_once('for_php7.php');

/********************************************************************/
/* 受講料登録                                       山城 2005/03/03 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm610Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjm610Form1", "POST", "knjm610index.php", "", "knjm610Form1");

        //年度データ
        $opt_year = array();

        $opt_year[0] = $model->control["年度"];
        $opt_year[1] = $model->control["年度"]+1;

        if ($model->field["YEAR"] == "") $model->field["YEAR"] = $opt_year[0];

        $objForm->ae( array("type"      => "select",
                            "name"      => "YEAR",
                            "size"      => 1,
                            "value"     => $model->field["YEAR"],
                            "extrahtml" => "onChange=\"return btn_submit('knjm610');\"",
                            "options"   => $opt_year ) );

        $arg["data"]["YEAR"] = $objForm->ge("YEAR");

        //受講料
        $db = Query::dbCheckOut();

        $result = $db->query(knjm610Query::get_money($model));
        $Rowmny = $result->fetchRow(DB_FETCHMODE_ASSOC);

        $result->free();
        Query::dbCheckIn($db);

        $model->field["MONEY"] = $Rowmny["TUITION"] ;

        $objForm->ae( array("type"      => "text",
                            "name"      => "MONEY",
                            "size"      => 4,
                            "maxlength" => 4,
                            "value"     => $model->field["MONEY"],
                            "extrahtml" => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"" ) );

        $arg["data"]["MONEY"] = $objForm->ge("MONEY");

        //更新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_delete",
                            "value"       => "削除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_delete"] = $objForm->ge("btn_delete");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する//////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJM610"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //フォーム作成
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm610Form1.html", $arg); 
    }
}
?>
