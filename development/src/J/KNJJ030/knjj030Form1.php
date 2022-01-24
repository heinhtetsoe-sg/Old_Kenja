<?php

require_once('for_php7.php');


class knjj030Form1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("list", "POST", "knjj030index.php", "", "edit");

    $db = Query::dbCheckOut();

    //学籍基礎マスタより学籍番号と名前を取得
    $query = knjj030Query::getSchregno_name($model->schregno);
    $Row          = $db->getRow($query,DB_FETCHMODE_ASSOC);
    $arg["SCHREGNO"] = $Row["SCHREGNO"];
    $arg["NAME"] = $Row["NAME"];

    //学籍住所データよりデータを取得
    if($model->schregno)
    {
        $result = $db->query(knjj030Query::getAward($model, $model->control_data["年度"],$model->schregno));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
           if ($model->Properties["useClubMultiSchoolKind"] == "1") {
               if (strpos($row["REMARK1"], $model->schKind) !== false) {
                    $row["adlink"] = "1";
                } else {
                    $row["nolink"] = "1";
                }
            } else {
                $row["adlink"] = "1";
            }
            $row["SDATE"]    = str_replace("-","/",$row["SDATE"]);
            $row["EDATE"]    = str_replace("-","/",$row["EDATE"]);

            //更新後この行にスクロールバーを移動させる
            if ($row["CLUBCD"] == $model->clubcd) {
                $row["EDATE"] = ($row["EDATE"]) ? $row["EDATE"] : "　";
                $row["EDATE"] = "<a name=\"target\">{$row["EDATE"]}</a><script>location.href='#target';</script>";
            }

            $arg["data"][] = $row;
        }
    }
    Query::dbCheckIn($db);

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );
    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "clear",
                        "value"     => "0"
                        ) );

    $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") == "right_list"){
            $arg["reload"]  = "window.open('knjj030index.php?cmd=edit&SCHREGNO=$model->schregno','edit_frame');";
        }

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjj030Form1.html", $arg);
}
}
?>
