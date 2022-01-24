<?php

require_once('for_php7.php');

class knjm711Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjm711index.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = $model->year;

        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_pre_copy",
                            "value"     => "前年度からコピー",
                            "extrahtml" => "onclick=\"return btn_submit('copy');\"" ));
        $arg["pre_copy"] = $objForm->ge("btn_pre_copy");

        //リスト表示
        $bifKey = "";
        $result = $db->query(knjm711Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($bifKey !== $row["COLLECT_L_M_CD"]) {
                $cnt = $db->getOne(knjm711Query::getColectLMCnt($row["COLLECT_L_M_CD"]));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            $bifKey = $row["COLLECT_L_M_CD"];

            $row["COLLECT_S_CD"] = View::alink("knjm711index.php", $row["COLLECT_S_CD"], "target=\"right_frame\"",
                                                  array("cmd"             => "edit",
                                                        "COLLECT_L_M_CD"  => $row["COLLECT_L_M_CD"],
                                                        "COLLECT_S_CD"    => $row["COLLECT_S_CD"] ));

            //金額をカンマ区切りにする
            $row["COLLECT_S_MONEY"] = (strlen($row["COLLECT_S_MONEY"])) ? number_format($row["COLLECT_S_MONEY"]): "";

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"] = "parent.right_frame.location.href='knjm711index.php?cmd=edit"
                           . "&year=".$model->year."';";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm711Form1.html", $arg);
    }
}
?>
