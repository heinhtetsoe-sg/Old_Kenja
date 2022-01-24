<?php

require_once('for_php7.php');

/********************************************************************/
/* 時間割チェックリスト                             山城 2005/04/14 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm290Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm290Form1", "POST", "knjm290index.php", "", "knjm290Form1");

        //年度コンボを作成する

        $opt_year = array();
        $db = Query::dbCheckOut();
        $query = knjm290Query::Getyear();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_year[] = array('label' => $row["YEAR"],
                                'value' => $row["YEAR"]);
        }
        if ($model->field["YEAR"] == "") $model->field["YEAR"] = CTRL_YEAR;
        $result->free();
        Query::dbCheckIn($db);


        $objForm->ae( array("type"      => "select",
                            "name"      => "YEAR",
                            "size"      => "1",
                            "value"     => $model->field["YEAR"],
                            "extrahtml" => "onchange=\" return btn_submit('nenhenkou')\";",
                            "options"   => $opt_year));

        $arg["data"]["YEAR"] = $objForm->ge("YEAR");

        //日付データFROM
        if ($model->Datef == "") $model->Datef = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DATEF"] = View::popUpCalendar($objForm ,"DATEF"    ,str_replace("-","/",$model->Datef),"reload=true");

        //日付データTO
        if ($model->Datet == "") $model->Datet = str_replace("-","/",CTRL_DATE);
        $arg["sel"]["DATET"] = View::popUpCalendar($objForm ,"DATET"    ,str_replace("-","/",$model->Datet),"reload=true");

        //担当者コンボを作成する
        $opt_staff = array();
        $i = 1;

        $db = Query::dbCheckOut();
        $query = knjm290Query::Getstaff($model);
        $result = $db->query($query);
        $opt_staff[0] = array('label' => "",
                              'value' => 0);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_staff[$i] = array('label' => $row["STAFFNAME"],
                                   'value' => $row["STAFFCD"]);
            $i++;
        }
        if($model->field["STAFF"]=="") $model->field["STAFF"] = $opt_staff[0]["value"];
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"       => "select",
                            "name"       => "STAFF",
                            "size"       => "1",
                            "value"      => $model->field["STAFF"],
                            "extrahtml"  => "",
                            "options"    => $opt_staff));

        $arg["data"]["STAFF"] = $objForm->ge("STAFF");

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
                            "name"      => "PRGID",
                            "value"     => "KNJM290"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm290Form1.html", $arg); 

    }
}
?>
