<?php

require_once('for_php7.php');

class knjz069_2Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz069_2index.php", "", "edit");

        $db = Query::dbCheckOut();

        $query = knjz069_2Query::getPypElementMst($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            if ($row["ELEMENT_DIV"] == "1") {
               $row["ELEMENT_DIV_NAME"] = "1：Outputs";
            } elseif ($row["ELEMENT_DIV"] == "2") {
               $row["ELEMENT_DIV_NAME"] = "2：Skills";
            }

            $row["ELEMENT_CD"] = View::alink("knjz069_2index.php", $row["ELEMENT_CD"], "target=right_frame",
                    array(
                        "cmd"            => "edit",
                        "ELEMENT_DIV"    => $row["ELEMENT_DIV"],
                        "ELEMENT_CD"     => $row["ELEMENT_CD"],
                    )
                );

            $arg["data"][] = $row; 
        }
        $result->free();

        Query::dbCheckIn($db);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz069_2Form1.html", $arg); 
    }
}
?>
