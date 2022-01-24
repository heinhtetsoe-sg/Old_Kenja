<?php

require_once('for_php7.php');

class knjf010aSubForm4
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjf010aindex.php", "", "sel");

        //生徒一覧
        $opt_left = $opt_right = array();
        //置換処理選択時の生徒の情報
        $array = explode(",", $model->replace_data["selectdata"]);
        if ($array[0]=="") {
            $array[0] = $model->schregno;
        }
        //生徒情報
        $RowH = knjf010aQuery::getMedexamHdat($model);    //生徒健康診断ヘッダデータ取得
        $RowD = knjf010aQuery::getMedexamDetDat($model); //生徒健康診断詳細データ取得

        $db = Query::dbCheckOut();

        $result   = $db->query(knjf010aQuery::getStudent($model));
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
        $query = knjf010aQuery::getMedexamDetNotExaminedDat($model);
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
        for ($i=0; $i<8; $i++) {
            if ($i==7) {
                $objForm->ae(array("type"       => "checkbox",
                                    "name"      => "RCHECK".$i,
                                    "value"     => "1",
                                    "checked"   => (($model->replace_data["check"][$i] == "1") ? 1 : 0),
                                    "extrahtml" => "onClick=\"return check_all(this);\""));
                $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
            } else {
                $objForm->ae(array("type"       => "checkbox",
                                   "name"       => "RCHECK".$i,
                                   "value"      => "1",
                                   "checked"    => (($model->replace_data["check"][$i] == "1") ? 1 : 0)));
                $arg["data"]["RCHECK".$i] = $objForm->ge("RCHECK".$i);
            }
        }
        //既往症
        $result     = $db->query(knjf010aQuery::getMedicalHist($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "style=\"width:170px;\"";
        $arg["data"]["MEDICAL_HISTORY1"] = knjCreateCombo($objForm, "MEDICAL_HISTORY1", $RowD["MEDICAL_HISTORY1"], $opt, $extra, 1);
        $extra = "style=\"width:170px;\"";
        $arg["data"]["MEDICAL_HISTORY2"] = knjCreateCombo($objForm, "MEDICAL_HISTORY2", $RowD["MEDICAL_HISTORY2"], $opt, $extra, 1);
        $extra = "style=\"width:170px;\"";
        $arg["data"]["MEDICAL_HISTORY3"] = knjCreateCombo($objForm, "MEDICAL_HISTORY3", $RowD["MEDICAL_HISTORY3"], $opt, $extra, 1);

        //診断名
        $extra = "";
        $arg["data"]["DIAGNOSIS_NAME"] = knjCreateTextBox($objForm, $RowD["DIAGNOSIS_NAME"], "DIAGNOSIS_NAME", 40, 50, $extra);


        //運動/指導区分
        $result     = $db->query(knjf010aQuery::getGuideDiv($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "GUIDE_DIV",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $RowD["GUIDE_DIV"],
                            "options"     => $opt ));
        $arg["data"]["GUIDE_DIV"] = $objForm->ge("GUIDE_DIV");

        //運動/部活動
        $result     = $db->query(knjf010aQuery::getJoiningSportsClub($model));
        $opt        = array();
        $opt[]      = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();
        $objForm->ae(array("type"        => "select",
                            "name"        => "JOINING_SPORTS_CLUB",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:170px;\"",
                            "value"       => $RowD["JOINING_SPORTS_CLUB"],
                            "options"     => $opt ));
        $arg["data"]["JOINING_SPORTS_CLUB"] = $objForm->ge("JOINING_SPORTS_CLUB");

        Query::dbCheckIn($db);
        /* ボタン作成 */
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit()\"" ));
        //戻るボタン
        $link = REQUESTROOT."/F/KNJF010A/knjf010aindex.php?cmd=back&ini2=1";
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"window.open('$link','_self');\"" ));
        $arg["BUTTONS"] = $objForm->ge("btn_update")."    ".$objForm->ge("btn_back");
        //対象者一覧
        $objForm->ae(array("type"        => "select",
                            "name"        => "left_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\" ",
                            "options"     => $opt_left));
        //生徒一覧
        $objForm->ae(array("type"        => "select",
                            "name"        => "right_select",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\" ",
                            "options"     => $opt_right));
        //全て追加
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all','left_select','right_select',1);\"" ));
        //追加
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left','left_select','right_select',1);\"" ));
        //削除
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right','left_select','right_select',1);\"" ));
        //全て削除
        $objForm->ae(array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all','left_select','right_select',1);\"" ));
        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("left_select"),
                                   "RIGHT_PART"  => $objForm->ge("right_select"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));
        /* ヘッダ */
        $arg["info"]    = array("TOP"        =>  $model->year."年度  "
                                                .$model->control_data["学期名"][$model->semester]
                                                ."  対象クラス  ".$model->Hrname,
                                "LEFT_LIST"  => "対象者一覧",
                                "RIGHT_LIST" => $model->sch_label."一覧");
        //hidden
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "selectdata"));
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "REPLACEHIDDENDATE",
                            "value"     => $RowH["DATE"]));

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjf010aSubForm4.html", $arg);
    }
}
?>

