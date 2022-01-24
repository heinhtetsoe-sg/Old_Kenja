<?php

require_once('for_php7.php');

class knjxcol_search {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjxcol_search", "POST", "knjxcol_searchindex.php", "", "knjxcol_search");
        $db = Query::dbCheckOut();

        $arg["jscript"] = "setF('".$model->frame."');";

        //東京都集計用
        knjCreateHidden($objForm, "useTokyotoShinroTyousasyo", $model->Properties["useTokyotoShinroTyousasyo"]);

        //学校名
        $arg["SCHOOL_NAME"] = knjCreateTextBox($objForm, $model->field["SCHOOL_NAME"], "SCHOOL_NAME", 32, 32, $extra);

        //学校系列
        $opt = array();
        $opt[] = array('label' => '', 'value' => '');
        $query = knjxcol_searchQuery::getSchoolGroup();
        $value = $model->field["SCHOOL_GROUP"];
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $extra = "onChange=\"btn_submit('search')\" ";
        $arg["SCHOOL_GROUP"] = knjCreateCombo($objForm, "SCHOOL_GROUP", $value, $opt, $extra, 1);

        //学科 降順出力チェックボックス
        $extra  = $model->field["SORT"] ? " checked " : "";
        //学校名取得
        $query = knjxcol_searchQuery::getSchoolName();
        $schoolName = $db->getOne($query);
        if($schoolName == 'KINDAI' || $schoolName == 'KINJUNIOR'){
            $extra = " checked ";
        }
        $extra .= "id=\"SORT\"";
        $arg["SORT"] = knjCreateCheckBox($objForm, "SORT", "on", $extra);

        //所在県
        $arg["AREA_NAME"] = knjCreateTextBox($objForm, $model->field["AREA_NAME"], "AREA_NAME", 20, 30, $extra);

        //学校名
        $extra = "onclick=\"btn_submit('search')\" style=\"width:600px;\"";
        $query = knjxcol_searchQuery::getLongName();
        $long_name = $db->getOne($query);
        if ($model->cmd == "search") {
            $query = knjxcol_searchQuery::getSchoolList($model);
            $opt = array();
            $opt[] = array("label" => "","value" => "");
            $value_flg = false;
            $cnt = 1;
            $school_list = $model->field["SCHOOL_LIST"];
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $spaces = make_space('ああああああああ', $row["SCHOOL_NAME"]);
                $label = "{$row["SCHOOL_CD"]} {$row["SCHOOL_NAME"]} {$spaces} {$row["SCHOOL_GROUP"]}";
                $value = "{$row["SCHOOL_CD"]}|{$row["SCHOOL_NAME"]}|{$row["SCHOOL_GROUP"]}|{$row["SCHOOL_GROUP_NAME"]}|{$row["ZIPCD"]}|{$row["ADDR1"]}|{$row["ADDR2"]}|{$row["TELNO"]}";

                $opt[] = array ('label' => $label,
                                "value" => $value);

                if ($school_list == $value) $value_flg = true;
            }
            $syokiti = ($school_list && $value_flg) ? $school_list : $opt[0]["value"];
            $result->free();

            $arg["SCHOOL_LIST"] = knjCreateCombo($objForm, "SCHOOL_LIST", $syokiti, $opt, $extra, 10);
            $model->field["SCHOOL_LIST"] = $school_list;
        } else {
            $arg["SCHOOL_LIST"] = knjCreateCombo($objForm, "SCHOOL_LIST", "", array(), $extra, 10);
            $model->field["SCHOOL_LIST"] = "";
        }

        //部名
        $extra = "onclick=\"btn_submit('search')\" style=\"width:600px;\"";
        $long_name = $db->getOne($query);
        if ($model->field["SCHOOL_LIST"]) {
            $query = knjxcol_searchQuery::getCollegeFacultyMst($model);
            $opt = array();
            $opt[] = array("label" => "","value" => "");
            $value = $model->field["FACULTY"];
            $value_flg = false;
            $cnt = 1;
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt[] = array ('label' => $row["LABEL"],
                                "value" => $row["VALUE"]);

                if ($value == $row["VALUE"]) $value_flg = true;
            }
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
            $result->free();

            $arg["FACULTY"] = knjCreateCombo($objForm, "FACULTY", $value, $opt, $extra, 10);
            $model->field["FACULTY"] = $value;
        } else {
            $arg["FACULTY"] = knjCreateCombo($objForm, "FACULTY", "", array(), $extra, 10);
            $model->field["FACULTY"] = "";
        }

        //科名
        $extra = "style=\"width:600px;\"";
        $long_name = $db->getOne($query);
        if ($model->field["FACULTY"]) {
            $query = knjxcol_searchQuery::getCollegeDepartmentMst($model);
            $opt = array();
            $opt[] = array("label" => "","value" => "");
            $value = $model->field["DEPARTMENT"];
            $value_flg = false;
            $cnt = 1;
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt[] = array ('label' => $row["LABEL"],
                                "value" => $row["VALUE"]);

                if ($value == $row["VALUE"]) $value_flg = true;
            }
            $value = ($value && $value_flg) ? $value : $opt[0]["value"];
            $result->free();

            $arg["DEPARTMENT"] = knjCreateCombo($objForm, "DEPARTMENT", $value, $opt, $extra, 10);
        } else {
            $arg["DEPARTMENT"] = knjCreateCombo($objForm, "DEPARTMENT", "", array(), $extra, 10);
        }

        /**********/
        /* ボタン */
        /**********/
        //検索
        $extra = "onclick=\"return btn_submit('search')\"";
        $arg["button"]["search"] = knjCreateBtn($objForm, "search", "検 索", $extra);
        //戻る
        $extra = "onClick=\"parent.closeit();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
        //選択
        $extra = "onclick=\"return apply_school()\"";
        $arg["button"]["exec_select"] = knjCreateBtn($objForm, "exec_select", "選 択", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "target_number", $model->target_number);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjxcol_search.html", $arg);
    }
}

/****************************/
/* ネスト用のスペースの生成 */
/****************************/
function make_space($longer_name, $name) {
    $mojisuu_no_sa = strlen($longer_name) - strlen($name);
    $spaces = '　';
    for ($i = 0; $i < $mojisuu_no_sa / 3; $i++) {
        $spaces .= '　';
    }

    return $spaces;
}
?>
