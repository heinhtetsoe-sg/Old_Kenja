<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjb3030SubFormSikenKaizyou
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subformSikenKaizyou", "POST", "knjb3030index.php", "", "subformSikenKaizyou");

        $arg["NAME_SHOW"] = substr($model->term, 0, 4)."年度";

        $db = Query::dbCheckOut();

        //SQL文発行
        //使用施設一覧取得
        $query = knjb3030Query::selectQuerySubFormFacility($model->term);
        $result = $db->query($query);
        $i = 0;
        $param = VARS::get("param");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            if (strstr($param, $row["FACCD"])) {
                $check = "checked";
            } else {
                $check = "";
            }
            $row["backcolor"] = (strstr($param, $row["FACCD"])) ? "#ccffcc" : "#ffffff";  //#ccffff
            $setId = " class=\"changeColor\" data-name=\"CHECK{$i}\" id=\"CHECK{$i}\" data-befColor=\"{$row["backcolor"]}\" ";
            //選択（チェック）
            $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $row["FACCD"].",".$row["FACILITYABBV"], $setId.$check, 1);
            $row["CHECK_NUM"] = $i;

            $arg["data"][] = $row;
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);

        //選択ボタンを作成する
        $extra = "onclick=\"return btn_submit('".$i."')\"";
        $arg["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb3030SubFormSikenKaizyou.html", $arg);
    }
}
