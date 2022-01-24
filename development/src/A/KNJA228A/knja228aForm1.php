<?php

require_once('for_php7.php');

class knja228aForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("knja228aForm1", "POST", "knja228aindex.php", "", "knja228aForm1");

        //リスト表示選択
        $opt = array(1, 2); //1:個人 2:クラス
        if (!$model->field["KUBUN"]) {
            $model->field["KUBUN"] = 1;
        }
        $onClick = " onclick =\" return btn_submit('knja228a');\"";
        $extra = array("id=\"KUBUN1\"".$onClick, "id=\"KUBUN2\"".$onClick);
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        if ($model->field["KUBUN"] == 1) {
            $arg["schno"] = $model->field["KUBUN"];
        }
        if ($model->field["KUBUN"] == 2) {
            $arg["clsno"] = $model->field["KUBUN"];
        }

        //学校名
        $db = Query::dbCheckOut();
        $query = knja228aQuery::getSchoolName();
        $result = $db->query($query);
        while ($rowf = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schoolName = $rowf["NAME1"];
        }
        $result->free();
        Query::dbCheckIn($db);

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学期名
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        //クラス一覧リスト
        $db = Query::dbCheckOut();
        $row1 = array();
        $query = knja228aQuery::getHrClassList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        //1:個人表示指定用
        $opt_left = array();
        if ($model->field["KUBUN"] == 1) {
            if ($model->field["GRADE_HR_CLASS"] == "") {
                $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            }

            $extra = "onChange=\"return btn_submit('change_class');\"";
            $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

            $row1 = array();
            //生徒単位
            $selectleft = explode(",", $model->selectleft);
            $query = knja228aQuery::getSchno($model);//生徒一覧取得
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                                             "value" => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);

                if ($model->cmd == 'change_class') {
                    if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)) {
                        $row1[] = array('label' => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                        'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                    }
                } else {
                    $row1[] = array('label' => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                }
            }
            //左リストで選択されたものを再セット
            if ($model->cmd == 'change_class') {
                foreach ($model->select_opt as $key => $val) {
                    if (in_array($key, $selectleft)) {
                        $opt_left[] = $val;
                    }
                }
            }
        }

        $result->free();
        Query::dbCheckIn($db);

        $chdt = $model->field["KUBUN"];

        $extra = "multiple style=\"width:100%;\" ondblclick=\"move1('left',$chdt)\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 18);

        //出力対象クラスリスト
        $extra = "multiple style=\"width:100%;\" ondblclick=\"move1('right',$chdt)\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", $opt_left, $extra, 18);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right',$chdt);\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left',$chdt);\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right',$chdt);\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left',$chdt);\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //CSVボタンを作成する
        $extra = "onclick =\" return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE); //hiddenを作成する(必須)
        knjCreateHidden($objForm, "PRGID", PROGRAMID);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectleft"); //左のリストを保持
        knjCreateHidden($objForm, "printKenkouSindanIppan", $model->Properties["printKenkouSindanIppan"]);
        knjCreateHidden($objForm, "useKnjf030jAHeartBiko", $model->Properties["useKnjf030jAHeartBiko"]);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "useParasite_J", $model->Properties["useParasite_J"]);
        knjCreateHidden($objForm, "useParasite_H", $model->Properties["useParasite_H"]);
        knjCreateHidden($objForm, "useParasite_P", $model->Properties["useParasite_P"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "SCHOOLNAME", $model->schoolName);
        knjCreateHidden($objForm, "useForm5_H_Ha", $model->Properties["useForm5_H_Ha"]);
        knjCreateHidden($objForm, "useForm5_H_Ippan", $model->Properties["useForm5_H_Ippan"]);
        knjCreateHidden($objForm, "kenkouSindanIppanNotPrintNameMstComboNamespare2Is1", $model->Properties["kenkouSindanIppanNotPrintNameMstComboNamespare2Is1"]);
        knjCreateHidden($objForm, "knja228aPrintVisionNumber", $model->Properties["knjf03j0PrintVisionNumber"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja228aForm1.html", $arg);
    }
}
