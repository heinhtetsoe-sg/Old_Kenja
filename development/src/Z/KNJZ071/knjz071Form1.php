<?php

require_once('for_php7.php');

class knjz071Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz071index.php", "", "edit");

        $db = Query::dbCheckOut();

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            //校種コンボ
            $query = knjz071Query::getSchkind($model);
            $result = $db->query($query);
            $opt = array();
            $opt[] = array('label' => "--全て--",
                           'value' => "99");
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt[] = array('label' => $row["LABEL"],
                               'value' => $row["VALUE"]);
            }
            $model->leftField["SCHKIND"] = $model->leftField["SCHKIND"] ? $model->leftField["SCHKIND"] : $model->leftSField["SCHKIND"];
            $model->leftField["SCHKIND"] = $model->leftField["SCHKIND"] ? $model->leftField["SCHKIND"] : $opt[0]["value"];
            $extra = "onChange=\"btn_submit('changeCmb')\"";
            $arg["TOP"]["SCHKIND"] = knjCreateCombo($objForm, "SCHKIND", $model->leftField["SCHKIND"], $opt, $extra, 1);        
        }

        //教育課程コード
        $opt_kyouiku = array();
        $opt_kyouiku[] = array('label' => "--全て--",
                               'value' => "99");
        $query = knjz071Query::getNamecd('Z018');
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_kyouiku[] = array('label' => $row["LABEL"],
                                   'value' => $row["VALUE"]);
        }
        $model->leftField["S_CURRICULUM_CD"] = $model->leftField["S_CURRICULUM_CD"] || $model->leftField["S_CURRICULUM_CD"] === "0" ? $model->leftField["S_CURRICULUM_CD"] : $model->leftSField["S_CURRICULUM_CD"];
        $model->leftField["S_CURRICULUM_CD"] = $model->leftField["S_CURRICULUM_CD"] || $model->leftField["S_CURRICULUM_CD"] === "0" ? $model->leftField["S_CURRICULUM_CD"] : $opt_kyouiku[0]["value"];
        $extra = "onChange=\"btn_submit('changeCmb')\"";
        $arg["TOP"]["S_CURRICULUM_CD"] = knjCreateCombo($objForm, "S_CURRICULUM_CD", $model->leftField["S_CURRICULUM_CD"], $opt_kyouiku, $extra, 1);

        //教科取得
        $query = knjz071Query::getClassCd($model);
        $result = $db->query($query);
        $opt = array();
        $opt[] = array('label' => "--全て--",
                       'value' => "99");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->leftField["S_CLASSCD"] = $model->leftField["S_CLASSCD"] ? $model->leftField["S_CLASSCD"] : $model->leftSField["S_CLASSCD"];
        $model->leftField["S_CLASSCD"] = $model->leftField["S_CLASSCD"] ? $model->leftField["S_CLASSCD"] : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('changeCmb')\"";
        $arg["TOP"]["S_CLASSCD"] = knjCreateCombo($objForm, "S_CLASSCD", $model->leftField["S_CLASSCD"], $opt, $extra, 1);        

        $query = knjz071Query::getSubclassData($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
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
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "changeCmb"){ 
            $arg["reload"]  = "parent.right_frame.location.href='knjz071index.php?cmd=edit&CHANGE_SCHKIND={$model->leftField["SCHKIND"]}';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz071Form1.html", $arg); 
    }
}
?>
