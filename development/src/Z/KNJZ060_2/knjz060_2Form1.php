<?php

require_once('for_php7.php');

class knjz060_2Form1
{
    public function main(&$model)
    {
        $arg["jscript"] = "";
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz060_2index.php", "", "edit");

        $db     = Query::dbCheckOut();

        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1' || $model->Properties["use_prg_schoolkind"] == "1") {
            //校種コンボ
            $arg["schkind"] = "1";

            $opt = array();
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $opt[] = array('label' => "-- 全て --",
                               'value' => "99");
            }
            $value_flg = false;
            $query = knjz060_2Query::getNamecd($model, "A023");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
                if ($model->schkind === $row["VALUE"]) {
                    $value_flg = true;
                }
            }
            $result->free();

            $model->schkind = ($model->schkind && $value_flg) ? $model->schkind : $opt[0]["value"];

            $extra = "onchange=\"return btn_submit('list');\"";
            $arg["SCHKIND"] = knjCreateCombo($objForm, "SCHKIND", $model->schkind, $opt, $extra, 1);

            $arg["useCurriculumcd"] = "1";
        } else {
            $arg["NoCurriculumcd"] = "1";
        }
        $query = knjz060_2Query::getData($model);
        //$query  = "select * from class_mst order by CLASSCD";
        $result = $db->query($query);

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            if ($row["ELECTDIV"]=="1") {
                $row["ELECTDIV"] = "選";
            } elseif ($row["ELECTDIV"]=="0") {
                $row["ELECTDIV"] = "";
            }
            //専門･その他
            if ($row["SPECIALDIV"] == "1") {
                $row["SPECIALDIV"] = "専";
            } elseif ($row["SPECIALDIV"] == "2") {
                $row["SPECIALDIV"] = "他";
            } else {
                $row["SPECIALDIV"] = "";
            }

            //更新後この行が画面の先頭に来るようにする
            if ($row["CLASSCD"] == $model->classcd) {
                $row["CLASSNAME"] = ($row["CLASSNAME"]) ? $row["CLASSNAME"] : "　";
                $row["CLASSNAME"] = "<a name=\"target\">{$row["CLASSNAME"]}</a><script>location.href='#target';</script>";
            }

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        $objForm->ae(array("type"      => "hidden",
                           "name"      => "cmd"
                           ));
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz060_2Form1.html", $arg);
    }
}
