<?php

class knjz041kForm1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjz041kForm1", "POST", "knjz041kindex.php", "", "knjz041kForm1");

        $opt=array();

        $arg["data"]["YEAR"] = $model->ObjYear;

        //中高判別フラグを作成する
        $jhflg = 0;
        $db = Query::dbCheckOut();
        $row = $db->getOne(knjz041kQuery::GetJorH());
        if ($row == 1){
            $jhflg = 1;
        }else {
            $jhflg = 2;
        }
        Query::dbCheckIn($db);

        //コース表示順
        $extra = "id=\"COURSE_ORDER\" checked";
        $arg["data"]["COURSE_ORDER"] = knjCreateCheckBox($objForm, "COURSE_ORDER", "1", $extra);

        $objForm->ae( array("type" => "hidden",
                            "name" => "JHFLG",
                            "value"=> $jhflg ) );

        //印刷ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->ObjYear
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJZ041K"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz041kForm1.html", $arg); 
    }
}
?>
