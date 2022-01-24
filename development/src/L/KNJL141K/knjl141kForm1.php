<?php

require_once('for_php7.php');

/********************************************************************/
/* 入学者成績データ出力                             山城 2006/03/10 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 :                                          name yyyy/mm/dd */
/********************************************************************/

class knjl141kForm1
{
    function main(&$model)
    {
        $db = Query::dbCheckOut();
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl141kindex.php", "", "main");
        
        //年度学期表示
        $arg["YEAR"] = $model->examyear;

        //合格コース
        $opt = array();
        $model->examcourseall = array();
        $result = $db->query(knjl141kQuery::getExamCourse($model->examyear));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]."：".$row["EXAMCOURSE_NAME"],
                           "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
            $model->examcourseall[] = $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"];
        }
            $opt[] = array("label" => "全部","value" => "99999999");
        $objForm->ae( array("type"        => "select",
                            "name"        => "EXAMCOURSE",
                            "size"        => "1",
                            "value"       => $model->examcourse,
                            "options"     => $opt));
        $arg["EXAMCOURSE"] = $objForm->ge("EXAMCOURSE");

        Query::dbCheckIn($db);

        //実行
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_exec",
                            "value"       => "実 行",
                            "extrahtml"   => "onclick=\"return btn_submit('exec');\"" ));
        $arg["btn_exec"] = $objForm->ge("btn_exec");

        //終了
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjl141kForm1.html", $arg);
    }
}
?>
