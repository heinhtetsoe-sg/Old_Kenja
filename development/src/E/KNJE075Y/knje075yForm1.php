<?php

require_once('for_php7.php');


class knje075yForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knje075yForm1", "POST", "knje075yindex.php", "", "knje075yForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = $model->control["年度"];
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //現在の学期コードを送る（hidden）
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        //学年コンボ
        $query = knje075yQuery::getGrade();
        $extra = "onchange=\"return btn_submit('knje075y');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "");

        //クラス選択コンボ
        $query = knje075yQuery::getAuth($model);
        $extra = "onchange=\"return btn_submit('knje075y');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "");

        //対象者リストを作成する
        $query = knje075yQuery::getList($model);

        $result = $db->query($query);

        $opt1 = array();
        $opt2 = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["SCHREGNO"],$model->select_data["selectdata"])) {
                $opt1[]= array('label' => $row["NAME"],
                               'value' => $row["SCHREGNO"]);
            }
        }

        if ($model->select_data["selectdata"][0]) {
            for ($i = 0; $i < get_count($model->select_data["selectdata"]); $i++) {
                $query = knje075yQuery::getList2($model->select_data["selectdata"][$i], $model);
                $seitoData = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $opt2[]= array('label' => $seitoData["NAME"],
                               'value' => $seitoData["SCHREGNO"]);
            }
        }

        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:250px;\" ondblclick=\"move1('left')\"",
                            "size"       => "25",
                            "options"    => isset($opt1)?$opt1:array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:250px;\" ondblclick=\"move1('right')\"",
                            "size"       => "25",
                            "options"    => isset($opt2)?$opt2:array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //指導上参考となる諸事欄、３分割フォーム
        $extra = ($model->field["useSyojikou3"] == "1" || !$model->cmd) ? "checked " : "";
        $arg["data"]["useSyojikou3"] = knjCreateCheckBox($objForm, "useSyojikou3", "1", $extra);

        //記載責任者コンボボックスを作成
        $query = "SELECT T1.STAFFCD AS VALUE, T1.STAFFCD || '　' || T1.STAFFNAME_SHOW AS LABEL ".
                 "FROM STAFF_MST AS T1,STAFF_YDAT AS T2 ".
                 "WHERE T1.STAFFCD = T2.STAFFCD AND".
                 "      T2.YEAR = '". $model->control["年度"]. "' ".
                 "ORDER BY T1.STAFFCD ";

        $result = $db->query($query);
        $row2[0] = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row2[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        $value = isset($model->field["SEKI"])?$model->field["SEKI"] : $model->staffcd;
        $arg["data"]["SEKI"] = knjCreateCombo($objForm, "SEKI", $value, $row2, $extra, 1);

        //ポップアップカレンダーを作成する
        $arg["el"]["DATE"]=View::popUpCalendar($objForm,"DATE",isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"]);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJE075Y");
        knjCreateHidden($objForm, "cmd");
        //CSV用
        knjCreateHidden($objForm, "selectdata");
        //ＯＳ選択(1:XP)
        knjCreateHidden($objForm, "OS", "1");
        //今年度
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        //教育課程コード
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
        
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
        
        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje075yForm1.html", $arg); 
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
