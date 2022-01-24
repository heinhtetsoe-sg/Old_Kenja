<?php

require_once('for_php7.php');

class knjh340aForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh340aindex.php", "", "edit");

        /********/
        /* 年度 */
        /********/
        $arg["YEAR"] = CTRL_YEAR;
        $db = Query::dbCheckOut();
        /********/
        /* 学期 */
        /********/
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;

        /**********/
        /* コピー */
        /**********/
        $extra = "style=\"width:130px\" onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        /**************/
        /* 模試コード */
        /**************/
        $opt = array();
        $value_flg = false;
        $query = knjh340aQuery::getMockcd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["MOCKCD"] == $row["VALUE"]) $value_flg = true;
        }
        $model->field["MOCKCD"] = ($model->field["MOCKCD"] && $value_flg) ? $model->field["MOCKCD"] : $opt[0]["value"];
        $extra = "onChange=\"btn_submit('list');\"";
        $arg["MOCKCD"] = knjCreateCombo($objForm, "MOCKCD", $model->field["MOCKCD"], $opt, $extra, 1);

        //コピー元
        $opt = array();
        $opt[] = array('label' => "",'value' => "");
        $value_flg = false;
        $query = knjh340aQuery::getMockcd();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["MOCKCD_FROMCOPY"] == $row["VALUE"]) $value_flg = true;
        }
        $model->field["MOCKCD_FROMCOPY"] = ($model->field["MOCKCD_FROMCOPY"] && $value_flg) ? $model->field["MOCKCD_FROMCOPY"] : $opt[0]["value"];
        $extra = "";
        $arg["MOCKCD_FROMCOPY"] = knjCreateCombo($objForm, "MOCKCD_FROMCOPY", $model->field["MOCKCD_FROMCOPY"], $opt, $extra, 1);
        
        $extra = "style=\"width:130px\" onclick=\"return btn_submit('fromcopy');\"";
        $arg["btn_fromcopy"] = knjCreateBtn($objForm, "btn_fromcopy", "左記からコピー", $extra);

        /****************/
        /* リスト内表示 */
        /****************/
        $query  = knjh340aQuery::getListdata($model);
        $result = $db->query($query);

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //権限チェック
            if($model->sec_competence == DEF_NOAUTH || $model->sec_competence == DEF_REFERABLE || $model->sec_competence == DEF_REFER_RESTRICT){
                break;
            }

            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            //リンク作成
            $row["SUBCLASS_NAME"] = View::alink("knjh340aindex.php", $row["SUBCLASS_NAME"], "target=\"right_frame\"",
                                              array("cmd"              => "edit",
                                                    "MOCKCD"           => $row["MOCKCD"],
                                                    "COURSE_DIV"       => $row["COURSE_DIV"],
                                                    "MOCK_SUBCLASS_CD" => $row["MOCK_SUBCLASS_CD"],
                                                    "DIV"              => $row["DIV"],
                                                    "GRADE"            => $row["GRADE"],
                                                    "COURSECD"         => $row["COURSECD"],
                                                    "MAJORCD"          => $row["MAJORCD"],
                                                    "COURSECODE"       => $row["COURSECODE"],
                                                    "PERFECT"          => $row["PERFECT"],
                                                    "PASS_SCORE"       => $row["PASS_SCORE"]
                                                    ));

            if ($row["GRADE"] == "00") {
                $row["GRADE"] = "";
            }
            if ($row["COURSECD"] == "0") {
                $row["COURSECD"] = "";
                $row["MAJORCD"] = "";
                $row["COURSECODE"] = "";
            } else {
                $row["COURSECD_MAJORCD"] = "({$row["COURSECD"]}{$row["MAJORCD"]})";
                $row["COURSECODE"] = "({$row["COURSECODE"]})";
            }

            $arg["data"][] = $row;
        }
        $result->free();

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //権限
        if($model->sec_competence == DEF_NOAUTH || $model->sec_competence == DEF_REFERABLE || $model->sec_competence == DEF_REFER_RESTRICT){
            $arg["Closing"]  = " closing_window(); " ;
        }
        if ($model->cmd == "change" || VARS::post("cmd") == "list"){
            $arg["reload"] = "window.open('knjh340aindex.php?cmd=edit2&SEMESTER={$model->semester}&TEST={$model->test}','right_frame');";
        }

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh340aForm1.html", $arg);
    }
}
?>
