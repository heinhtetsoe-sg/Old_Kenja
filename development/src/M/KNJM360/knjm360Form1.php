<?php

require_once('for_php7.php');

/********************************************************************/
/* 時間割チェックリスト                             山城 2005/04/14 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm360Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm360Form1", "POST", "knjm360index.php", "", "knjm360Form1");

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //受付経過期間
        if ($model->field["LASTDAY"] == "") $model->field["LASTDAY"] = 10;

        $objForm->ae( array("type"      => "text",
                            "name"      => "LASTDAY",
                            "size"      => "3",
                            "maxlength" => "3",
                            "value"     => $model->field["LASTDAY"],
                            "extrahtml" => "onblur =\" this.value=toInteger(this.value)\";"));

        $arg["data"]["LASTDAY"] = $objForm->ge("LASTDAY");

        //件数一覧チェックボックス
        $opt_check = "";
        if ($model->field["OUTPUT1"] == "on" || $model->cmd == ""){
            $opt_check = "checked";
        }

        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT1",
                            "value"     => "on",
                            "extrahtml" => $opt_check));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT1");

        //詳細リストチェックボックス
        $opt_check = "";
        if ($model->field["OUTPUT2"] == "on" || $model->cmd == ""){
            $opt_check = "checked";
        }
        
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "OUTPUT2",
                            "value"     => "on",
                            "extrahtml" => $opt_check));

        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT2");

        //科目コンボを作成する
        $opt_subclass = array();
        $i = 1;

        $db = Query::dbCheckOut();
        $query = knjm360Query::GetSubclass($model);
        $result = $db->query($query);
        $opt_subclass[0] = array('label' => "",
                              'value' => 0);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_subclass[$i] = array('label' => $row["SUBCLASSNAME"],
                                      'value' => $row["SUBCLASSCD"]);
            $i++;
        }
        if($model->field["SUBCLASS"]=="") $model->field["SUBCLASS"] = $opt_subclass[0]["value"];
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "SUBCLASS",
                            "size"       => "1",
                            "value"      => $model->field["SUBCLASS"],
                            "extrahtml"  => "",
                            "options"    => $opt_subclass));

        $arg["data"]["SUBCLASS"] = $objForm->ge("SUBCLASS");

        //印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJM360"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm360Form1.html", $arg); 

    }
}
?>
