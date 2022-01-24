<?php

require_once('for_php7.php');

class knjz173Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz173index.php", "", "edit");

        $db = Query::dbCheckOut();

        // if ($model->isChgPwdUse) {
        //     $arg["chgPwd"] = 1;
        // }

        //リンク先設定
        $link = REQUESTROOT."/Z/KNJZ173_2/knjz173_2index.php";

        // //コピーボタン
        // $extra = "onclick=\"return btn_submit('copy');\"";
        // $arg["button"]["copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からデータをコピー", $extra);

        $arg["year"]["VAL"] = $model->year;

        //リスト内データ取得
        $query = knjz173Query::getList($model);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["KAIKIN_CD"] = View::alink(
                "knjz173index.php",
                $row["KAIKIN_CD"],
                "target=\"right_frame\"",
                array("cmd"     => "edit",
                      "KAIKIN_CD" => $row["KAIKIN_CD"])
            );
            $arg["data"][] = $row;
        }

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SEND_selectSchoolKind", $model->selectSchoolKind);

        // if ($model->cmd == "change_kind") {
        //     $arg["jscript"] = "window.open('knjz173index.php?cmd=edit','right_frame');";
        // }

        if ($model->cmd != "edit") {
            $arg["reload"]  = "window.open('knjz173index.php?cmd=list','left_frame');";
        }

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz173Form1.html", $arg);
    }
}
