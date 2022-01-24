<?php

require_once('for_php7.php');

class knjb3055Form1 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        $db         = Query::dbCheckOut();

        /**************/
        /* 学期コンボ */
        /**************/
        //学期数による判定処理
        $year_control = (trim(CTRL_SEMESTER) == trim($model->control["学期数"]))? true : false ;
        
        $query = knjb3055Query::getSemester();
        $result = $db->query($query);
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        
            /* 最終学期の場合、対象年度リストに次の年度の１学期（前期）を追加 */
            if ($row["YEAR"] == CTRL_YEAR){
               $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            }
             
            if ($year_control && $row["YEAR"] == CTRL_YEAR+1 && $row["SEMESTER"] == "1"){
               $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            }
        }

        //javascriptの処理、
        $extra = "onChange=\"btn_submit('list');\"";
        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_YEAR ."-". CTRL_SEMESTER;
        $arg["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $opt, $extra, 1);

        /**********/
        /* リスト */
        /**********/
        $query = knjb3055Query::getSubclassList($model);
        $result = $db->query($query);
        $firstFlg = true;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!$row["CLASSCD"]) {
                continue;
            }
            $tree = '';
            if ($classcd != $row["CLASSCD"] && !$firstFlg) {
                $tree .= "    </ul>\n";
                $tree .= "</ul>\n";
                $tree .= "<ul>\n";
                $tree .= "  <li>{$row["CLASSCD"]} {$row["CLASSNAME"]}</li>\n";
                $tree .= "    <ul>\n";
            } elseif ($firstFlg) {
                $tree .= "<ul>\n";
                $tree .= "  <li>{$row["CLASSCD"]} {$row["CLASSNAME"]}</li>\n";
                $tree .= "    <ul>\n";
            }

            $row["SUBCLASSNAME"] = View::alink("knjb3055index.php", htmlspecialchars($row["SUBCLASSNAME"]), "target='right_frame'",
                                array("cmd"        => "edit",
                                      "SEMESTER"   => $model->field["SEMESTER"],
                                      "CLASSCD"    => $row["CLASSCD"],
                                      "SUBCLASSCD" => $row["SUBCLASSCD"]
                                ));

            $tree .= "      <li>{$row["SUBCLASSCD"]} {$row["SUBCLASSNAME"]}</li>\n";

            $arg["TREE"] .= $tree;

            $classcd = $row["CLASSCD"];
            $firstFlg = false;
        }
        $tree  = "    </ul>\n";
        $tree .= "</ul>\n";
        $arg["TREE"] .= $tree;

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd", "");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJB3055");
        knjCreateHidden($objForm, "SUBCLASSCD", $model->field["SUBCLASSCD"]);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjb3055index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb3055Form1.html", $arg);
    }
}
?>
