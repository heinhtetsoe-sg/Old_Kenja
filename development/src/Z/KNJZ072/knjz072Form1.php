<?php

require_once('for_php7.php');

class knjz072Form1
{
    public function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz072index.php", "", "edit");

        $db = Query::dbCheckOut();
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
        } else {
            $arg["NoCurriculumcd"] = "1";
        }
        $query = knjz072Query::getSubclassData($model);
        //$query = "select * from subclass_mst order by SUBCLASSCD";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($row["ELECTDIV"]=="1") {
                $row["ELECTDIV"] = "選";
            } elseif ($row["ELECTDIV"]=="0") {
                $row["ELECTDIV"] = "";
            }

            //更新後この行が画面の先頭に来るようにする
            if ($row["SUBCLASSCD"] == $model->subclasscd) {
                $row["SUBCLASSNAME"] = ($row["SUBCLASSNAME"]) ? $row["SUBCLASSNAME"] : "　";
                $row["SUBCLASSNAME"] = "<a name=\"target\">{$row["SUBCLASSNAME"]}</a><script>location.href='#target';</script>";
            }

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);
        $arg["year"] = $model->year_code;
        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"
                            ));

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz072Form1.html", $arg);
    }
}
