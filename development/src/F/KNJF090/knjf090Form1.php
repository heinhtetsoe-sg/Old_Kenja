<?php

require_once('for_php7.php');

class knjf090Form1
{

    function main(&$model)
    {

        $db = Query::dbCheckOut();
        //SQL文発行
        $query = knjf090Query::selectQuery($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $row["DATE"] = View::alink("knjf090index.php", str_replace("-","/",$row["DATE"]) ."&nbsp;" .$row["TIME"],
                "target=\"edit_frame\"",
                array("cmd"           => "edit",
                      "SCHREGNO"      => $row["SCHREGNO"],
                      "TREATMENT_DIV" => $row["TREATMENT_DIV_CD"],
                      "DATE"          => str_replace("-","/",$row["DATE"]),
                      "TIME"          => $row["TIME"]));

                if ($row["BEDTIME_H"] != "") {
                        $row["BEDTIME"] = $row["BEDTIME_H"] .":". $row["BEDTIME_M"];
                }
                if ($row["RISINGTIME_H"] != "") {
                        $row["RISINGTIME"] = $row["RISINGTIME_H"] .":". $row["RISINGTIME_M"];
                }
                if ($row["OCCURTIME_H"] != "") {
                        $row["OCCURTIME"] = $row["OCCURTIME_H"] .":". $row["OCCURTIME_M"];
                }
                //レコードを連想配列のまま配列$arg[data]に追加していく。
                $arg["data"][] = $row;
        }

        $arg["SCHREGNO"]    = $model->schregno;
        $arg["NAME"]        = $model->name;

        Query::dbCheckIn($db);
        if (VARS::get("SCHREGNO") || VARS::get("init") == 1){
                $arg["reload"] = "window.open('knjf090index.php?cmd=edit','edit_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjf090Form1.html", $arg);
    }
}
?>
