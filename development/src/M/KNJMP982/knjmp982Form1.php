<?php

require_once('for_php7.php');

class knjmp982Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjmp982index.php", "", "edit");

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
        $result = $db->query(knjmp982Query::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($bifKey !== $row["LEVY_L_M_CD"]) {
                $cnt = $db->getOne(knjmp982Query::getLevyLMCnt($row["LEVY_L_M_CD"]));
                $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            $bifKey = $row["LEVY_L_M_CD"];

            $row["LEVY_S_CD"] = View::alink("knjmp982index.php", $row["LEVY_S_CD"], "target=\"right_frame\"",
                                                  array("cmd"             => "edit",
                                                        "LEVY_L_M_CD"  => $row["LEVY_L_M_CD"],
                                                        "LEVY_S_CD"    => $row["LEVY_S_CD"] ));
            //返金可・不可
            if ($row["REPAY_DIV"] == "1") {
                $row["REPAY_DIV"] = "可";
            } else {
                $row["REPAY_DIV"] = "不可";
            }

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"] = "parent.right_frame.location.href='knjmp982index.php?cmd=edit"
                           . "&year=".$model->year."';";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjmp982Form1.html", $arg);
    }
}
?>
