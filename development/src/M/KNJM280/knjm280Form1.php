<?php

require_once('for_php7.php');

/********************************************************************/
/* レポート入力チェックリスト                       山城 2005/04/25 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm280Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm280Form1", "POST", "knjm280index.php", "", "knjm280Form1");

        //年度設定
        $arg["YEAR"] = CTRL_YEAR;

        //日付コンボを作成する
        if ($model->Date == ""){
            $model->Date = str_replace("-","/",CTRL_DATE);
            $model->semester = CTRL_SEMESTER;
        }
        $arg["data"]["DATE"] = View::popUpCalendar($objForm ,"DATE" ,str_replace("-","/",$model->Date),"reload=true");
        //チェック用hidden
        $objForm->ae( array("type"      => "hidden",
                            "value"     => $model->Date,
                            "name"      => "DEFOULTDATE") );

        //出力順ラジオボタンを作成
        $opt[0]=1;
        $opt[1]=2;
        $disable = "";
        if (!$model->field["OUTPUT"]) $disable = "disabled";
        if ($model->field["OUTPUT"] == 1) $disable = "disabled";
        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUT",
                            "value"      => isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:"1",
                            "extrahtml"  => " onclick =\" return checkr(this);\"",
                            "multiple"   => $opt));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

        //担当者コンボを作成する
        $opt_staff = array();
        $i = 1;

        $db = Query::dbCheckOut();
        $query = knjm280Query::Getstaff($model);
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
                            "extrahtml"  => "$disable",
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
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => $model->semester
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJM280"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm280Form1.html", $arg); 

    }
}
?>
