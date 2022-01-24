<?php

require_once('for_php7.php');

class knjz179Form1 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        $arg["start"] = $objForm->get_start("list", "POST", "knjz179index.php", "", "edit");
        $db = Query::dbCheckOut();

        //参照年度の取得
        $query = knjz179query::getGdatYear();
        $result = $db->query($query);
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($row["VALUE"] < CTRL_YEAR) {
                $shoki_nendo = $row["VALUE"];
            }
        }
        $model->term2 = $model->term2 ? $model->term2 : $shoki_nendo;
        //参照年度コンボボックス
        $objForm->ae( array("type"      => "select",
                            "name"      => "term2",
                            "size"      => "1",
                            "extrahtml" => "selectedindex=\"$selected\"",
                            "value"     => $model->term2,
                            "options"   => $opt));
        $arg["term2"] = $objForm->ge("term2");

        //対象年度の取得
        $query = knjz179query::getYear();
        $result = $db->query($query);
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        //対象年度コンボボックス
        $objForm->ae( array("type"      => "select",
                            "name"      => "term",
                            "size"      => "1",
                            "extrahtml" => "selectedindex=\"$selected\" onChange=\"btn_submit('list')\"",
                            "value"     => $model->term,
                            "options"   => $opt));
        $arg["term"] = $objForm->ge("term");

        //コピーのボタン
        $objForm->ae( array("type"      =>    "button",
                            "name"      =>    "btn_copy",
                            "value"     =>    "左の年度のデータをコピー",
                            "extrahtml" =>    "onClick=\"return btn_submit('copy');\""));
        $arg["btn_copy"] = $objForm->ge("btn_copy");

        /****************/
        /* リストの作成 */
        /****************/
        $result = $db->query(knjz179query::SelectList($model->term, $model));
        $i = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             array_walk($row, "htmlspecialchars_array");

             $row["URL"] = View::alink("knjz179index.php", $row["GRADE"], "target=right_frame",
                                         array("cmd"        => "edit",
                                              "GRADE"       => $row["GRADE"],
                                              "term"        => $model->term));
             $row["backcolor"] = ($i%2 == 0) ? "#ffffff" : "#ccffcc";
             $arg["data"][] = $row;
             $i++;
        }
        $result->free();

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"] = $objForm->get_finish();
        Query::dbCheckIn($db);
        if ($model->cmd == "list" && VARS::get("ed") != "1") {
            $arg["reload"] = "window.open('knjz179index.php?cmd=edit&init=1','right_frame');";
        }

        View::toHTML($model, "knjz179Form1.html", $arg);
    }
}
?>
