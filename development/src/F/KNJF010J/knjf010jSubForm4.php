<?php

require_once('for_php7.php');

class knjf010jSubForm4
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("sel", "POST", "knjf010jindex.php", "", "sel");

        //生徒一覧
        $opt_left = $opt_right = array();
        //置換処理選択時の生徒の情報
        $array = explode(",", $model->replace_data["selectdata"]);
        if ($array[0] == "") {
            $array[0] = $model->schregno;
        }
        //生徒情報
        $RowH = knjf010jQuery::getMedexamHdat($model);    //生徒健康診断ヘッダデータ取得
        $RowD = knjf010jQuery::getMedexamDetDat($model); //生徒健康診断詳細データ取得

        $db = Query::dbCheckOut();

        $result   = $db->query(knjf010jQuery::getStudent($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["SCHREGNO"], $array)) {
                $opt_right[]  = array("label" => $row["ATTENDNO"]." ".$row["SCHREGNO"]." ".$row["NAME_SHOW"],
                                      "value" => $row["SCHREGNO"]);
            } else {
                $opt_left[]   = array("label" => $row["ATTENDNO"]." ".$row["SCHREGNO"]." ".$row["NAME_SHOW"],
                                      "value" => $row["SCHREGNO"]);
            }
        }
        $result->free();

        //未検査項目設定
        $notFieldSet = $sep = "";
        $query = knjf010jQuery::getMedexamDetNotExaminedDat($model);
        $result = $db->query($query);
        while ($notExamined = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($notExamined as $field => $val) {
                if ($field == "YEAR" || $field == "GRADE") {
                    continue;
                }

                if ($val == "1") {
                    $notFieldSet .= $sep.$field;
                    $sep = ":";
                }
            }
        }
        if ($notFieldSet != "") {
            $arg["setNotExamined"] = "setNotExamined('{$notFieldSet}')";
        }

        /* 編集項目 */
        //チェックボックス
        for ($i = 0; $i < 6; $i++) {
            if ($i == 5) {
                $extra = "onClick=\"return check_all(this);\"";
                if ($model->replace_data["check"][$i] == "1") {
                    $extra .= "checked = 'checked'";
                } else {
                    $extra .= "";
                }
                $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra);
            } else {
                $extra = "";
                if ($model->replace_data["check"][$i] == "1") {
                    $extra .= "checked = 'checked'";
                } else {
                    $extra .= "";
                }
                $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra);
            }
        }
        //既往症
        $result     = $db->query(knjf010jQuery::getNameMst($model, "F143"));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["MEDICAL_HISTORY1"] = knjCreateCombo($objForm, "MEDICAL_HISTORY1", $RowD["MEDICAL_HISTORY1"], $opt, $extra.$entMove, 1);
        $arg["data"]["MEDICAL_HISTORY2"] = knjCreateCombo($objForm, "MEDICAL_HISTORY2", $RowD["MEDICAL_HISTORY2"], $opt, $extra.$entMove, 1);
        $arg["data"]["MEDICAL_HISTORY3"] = knjCreateCombo($objForm, "MEDICAL_HISTORY3", $RowD["MEDICAL_HISTORY3"], $opt, $extra.$entMove, 1);

        //診断名
        $extra = "";
        $arg["data"]["DIAGNOSIS_NAME"] = knjCreateTextBox($objForm, $RowD["DIAGNOSIS_NAME"], "DIAGNOSIS_NAME", 40, 50, $extra);

        Query::dbCheckIn($db);
        /* ボタン作成 */
        //更新ボタン
        $extra = "onclick=\"return doSubmit()\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //戻るボタン
        $link = REQUESTROOT."/F/KNJF010J/knjf010jindex.php?cmd=back&ini2=1";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //対象者一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\"";
        $arg["data"]["left_select"] = knjCreateCombo($objForm, "left_select", "left", $opt_left, $extra, "20");

        //生徒一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\"";
        $arg["data"]["right_select"] = knjCreateCombo($objForm, "right_select", "left", $opt_right, $extra, "20");

        //全て追加
        $extra = "onclick=\"return move('sel_add_all','left_select','right_select',1);\"";
        $arg["button"]["sel_add_all"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        //追加
        $extra = "onclick=\"return move('left','left_select','right_select',1);\"";
        $arg["button"]["sel_add"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        //削除
        $extra = "onclick=\"return move('right','left_select','right_select',1);\"";
        $arg["button"]["sel_del"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        //全て削除
        $extra = "onclick=\"return move('sel_del_all','left_select','right_select',1);\"";
        $arg["button"]["sel_del_all"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        /* ヘッダ */
        $arg["info"]    = array("TOP"        =>  $model->year."年度  "
                                                .$model->control_data["学期名"][$model->semester]
                                                ."  対象クラス  ".$model->Hrname,
                                "LEFT_LIST"  => "対象者一覧",
                                "RIGHT_LIST" => $model->sch_label."一覧");
        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "REPLACEHIDDENDATE", $RowH["DATE"]);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf010jSubForm4.html", $arg);
    }
}
