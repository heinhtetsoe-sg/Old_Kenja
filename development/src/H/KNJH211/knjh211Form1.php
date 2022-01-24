<?php

require_once('for_php7.php');


class knjh211Form1
{
    public function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh211index.php", "", "edit");

        $db = Query::dbCheckOut();

        //学籍基礎マスタより学籍番号と名前を取得
        $query = knjh211Query::getSchregno_name($model->schregno);
        $Row          = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $Row["SCHREGNO"];
        $arg["NAME"] = $Row["NAME"];

        //学籍住所データよりデータを取得
        if ($model->schregno) {
            $result = $db->query(knjh211Query::getAward($model, $model->control_data["年度"], $model->schregno));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["DOMI_ENTDAY"]    = str_replace("-", "/", $row["DOMI_ENTDAY"]);
                $row["DOMI_OUTDAY"]    = str_replace("-", "/", $row["DOMI_OUTDAY"]);

                //更新後この行にスクロールバーを移動させる
                if ($row["DOMI_CD"] == $model->clubcd) {
                    $row["DOMI_OUTDAY"] = ($row["DOMI_OUTDAY"]) ? $row["DOMI_OUTDAY"] : "　";
                    $row["DOMI_OUTDAY"] = "<a name=\"target\">{$row["DOMI_OUTDAY"]}</a><script>location.href='#target';</script>";
                }

                $arg["data"][] = $row;
            }
        }
        Query::dbCheckIn($db);

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                        "name"      => "cmd"
                        ));
        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                        "name"      => "clear",
                        "value"     => "0"
                        ));

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") == "right_list") {
            $arg["reload"]  = "window.open('knjh211index.php?cmd=edit&SCHREGNO=$model->schregno','edit_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh211Form1.html", $arg);
    }
}
