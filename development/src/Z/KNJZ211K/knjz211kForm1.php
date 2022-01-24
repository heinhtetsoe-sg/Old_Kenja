<?php

class knjz211kForm1{

    function main(&$model){

        $objForm      = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz211kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //評定リスト
        $result = $db->query(knjz211kQuery::getAssesQuery($model->year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row["TYPE_ASSES_CD"] = View::alink("knjz211kindex.php", $row["TYPE_ASSES_CD"], "target=right_frame",
                                                array("cmd"      => "edit",
                                                      "ASSES_CD" => $row["TYPE_ASSES_CD"] ));
            $arg["data"][] = $row;
        }
        $result->free();
        $copy_flg = 1;
        if (isset($arg["data"])) {
            $copy_flg |= (1 << 1);
        }
        $row = $db->getRow(knjz211kQuery::getAssesQuery($model->year-1), DB_FETCHMODE_ASSOC);
        if (isset($row)) {
            $copy_flg |= (1 << 2);
        }
        Query::dbCheckIn($db);

        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_copy",
                            "value"         => "前年度からコピー",
                            "extrahtml"     => "style=\"width:200px\"onclick=\"return btn_submit('copy')\"" ));
        $arg["button"]["copy_year"] = $objForm->ge("btn_copy");

        //hidden
        $objForm->ae( array("type"          => "hidden",
                            "name"          => "cmd") );
        $objForm->ae( array("type"          => "hidden",
                            "name"          => "Cleaning") );
        $objForm->ae( array("type"          => "hidden",
                            "name"          => "copy_flg",
                            "value"         => $copy_flg) );

        if($model->sec_competence != DEF_UPDATABLE){
            $arg["Closing"] = " closing_window(); " ;
        }

        $arg["current_year"] = "対象年度：" . $model->year;

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjz211kForm1.html", $arg); 
    }
}
?>
