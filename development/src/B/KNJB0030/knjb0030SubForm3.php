<?php
//ビュー作成用クラス
class knjb0030SubForm3
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform3", "POST", "knjb0030index.php", "", "subform3");

        $arg["NAME_SHOW"] = substr($model->term, 0, 4)."年度";

        $db = Query::dbCheckOut();

        //SQL文発行
        //使用施設一覧取得
        $query = knjb0030Query::selectQuerySubForm3($model->term);
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
            //選択（チェック）
            $objForm->ae(array("type"       => "checkbox",
                               "name"       => "CHECK",
                               "value"      => $row["FACCD"].",".$row["FACILITYABBV"],
                               "extrahtml"  => $check,
                               "multiple"   => "1" ));

            $row["CHECK"] = $objForm->ge("CHECK");

            $row["backcolor"] = (strstr($param, $row["FACCD"])) ? "#ccffcc" : "#ffffff";  //#ccffff
//            $row["backcolor"] = "#ffffff";
            $arg["data"][] = $row;
            $i++;
        }
        $result->free();
        Query::dbCheckIn($db);


        //選択ボタンを作成する
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_sentaku",
                            "value"     => "選 択",
                            "extrahtml" => "onclick=\"return btn_submit('".$i."')\"" ));// 04//11/12Add $i

        $arg["btn_sentaku"] = $objForm->ge("btn_sentaku");

        //終了ボタンを作成する
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"return top.main_frame.right_frame.closeit()\"" ));

        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd" ));

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb0030SubForm3.html", $arg);
    }
}
