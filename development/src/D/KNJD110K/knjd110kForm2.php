<?php

require_once('for_php7.php');

class knjd110kForm2
{
    function main(&$model)
    {
        $objForm      = new form;
        $db           = Query::dbCheckOut();
        $arg["start"] = $objForm->get_start("list", "POST", "knjd110kindex.php", "", "edit");

        //生成済み一覧
        $result = $db->query(knjd110kQuery::GetList($model->year));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");

            $row["SEMESTER"] = $model->ctrl["学期名"][$row["SEMESTER"]];
            $row["MONTH"]   = $row["MONTH"]."月";
            $row["UPDATED"] = str_replace("-","/",$row["UPDATED"]);
            $arg["data"][]  = $row;

        }

        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd110kForm2.html", $arg);
    }
}
?>
