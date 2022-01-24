<?php

require_once('for_php7.php');


class knjz280_2form1{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjz280_2form1", "POST", "knjz280_2index.php", "", "knjz280_2form1");

        $db     = Query::dbCheckOut();
        $query  = "select * from job_mst order by JOBCD";
        $result = $db->query($query);
    
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");

            //更新後この行にスクロールバーを移動させる
            if ($row["JOBCD"] == $model->jobcd) {
                $row["JOBNAME"] = ($row["JOBNAME"]) ? $row["JOBNAME"] : "　";
                $row["JOBNAME"] = "<a name=\"target\">{$row["JOBNAME"]}</a><script>location.href='#target';</script>";
            }

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
        View::toHTML($model, "knjz280_2form1.html", $arg); 

    }
}
?>
