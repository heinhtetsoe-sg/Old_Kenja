<?php

require_once('for_php7.php');

class knjc052Form1
{
    function main(&$model){

        $objForm = new form;

        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjc052Form1", "POST", "knjc052index.php", "", "knjc052Form1");

        $opt=array();

        //学期リスト
        $db = Query::dbCheckOut();
        $query = knjc052Query::getSemester($model);
        $result = $db->query($query);
        $opt_seme = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
              $opt_seme[]= array('label'    => $row["SEMESTERNAME"],
                                 'value' => $row["SEMESTER"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        if ($model->field["GAKKI"]=="") $model->field["GAKKI"] = CTRL_SEMESTER;

        $objForm->ae( array("type"       => "select",
                            "name"       => "GAKKI",
                            "size"       => "1",
                            "value"      => $model->field["GAKKI"],
                            "extrahtml"  => "onChange=\"return btn_submit('knjc052');\"",
                            "options"    => $opt_seme));

        $arg["data"]["GAKKI"] = $objForm->ge("GAKKI");


        //印刷範囲
        $db = Query::dbCheckOut();
        $query = knjc052Query::getsdateedate($model->field["GAKKI"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
              $start_date = str_replace("-","/",$row["SDATE"]);
              $end_date   = str_replace("-","/",$row["EDATE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        $arg["data"]["DATE"] = View::popUpCalendar($objForm,"DATE",isset($start_date)?$start_date:$model->control["学籍処理日"]);
        $arg["data"]["DATE2"] = View::popUpCalendar($objForm,"DATE2",isset($end_date)?$end_date:$model->control["学籍処理日"]);

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "STARTDAY",
                            "value"     => $start_date
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ENDDAY",
                            "value"     => $end_date
                            ) );


        //欠課処理チェックボックスを作成する/////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "checkbox",
                            "name"       => "OUTPUT3",
                            "extrahtml"  => "checked",
                            "value"      => isset($model->field["OUTPUT3"])?$model->field["OUTPUT3"]:"1"));

        $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT3");

        //印刷ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return opener_submit('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE,
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJC052"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //年度データ
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->control["年度"]
                            ) );

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);


        $arg["data"]["YEAR"] = $model->control["年度"];


        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjc052Form1.html", $arg); 
    }

}
?>
