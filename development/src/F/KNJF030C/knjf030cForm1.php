<?php

require_once('for_php7.php');


class knjf030cForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjf030cForm1", "POST", "knjf030cindex.php", "", "knjf030cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期名
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //1:クラス,2:個人表示指定
        $opt_data = array(1, 2);
        $model->field["KUBUN"] = ($model->field["KUBUN"] == "") ? "1" : $model->field["KUBUN"];
        $extra = array("id=\"KUBUN1\" onClick=\"btn_submit('knjf030c')\"", "id=\"KUBUN2\" onClick=\"btn_submit('knjf030c')\"");
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["KUBUN"] == 1) $arg["clsno"] = $model->field["KUBUN"];
        if ($model->field["KUBUN"] == 2) $arg["schno"] = $model->field["KUBUN"];

        //クラス一覧リスト
        makeClassItiran($objForm, $arg, $db, $model);

        //眼科検診チェックボックス
        $extra  = " id=\"GANKA_KENSIN\"";
        $extra .= $model->field["GANKA_KENSIN"] == "on" ? " checked" : "";
        $arg["data"]["GANKA_KENSIN"] = knjCreateCheckBox($objForm, "GANKA_KENSIN", "on", $extra);

        //診断結果チェックボックス
        $extra  = " id=\"KEKKA_HA\"";
        $extra .= $model->field["KEKKA_HA"] == "on" ? "checked" : "";
        $arg["data"]["KEKKA_HA"] = knjCreateCheckBox($objForm, "KEKKA_HA", "on", $extra);

        //治療済票付きチェックボックス
        $extra  = " id=\"KEKKA_HA_CARD\"";
        $extra .= $model->field["KEKKA_HA_CARD"] == "on"? "checked" : "";
        $arg["data"]["KEKKA_HA_CARD"] = knjCreateCheckBox($objForm, "KEKKA_HA_CARD", "on", $extra);

        //歯・口の健康診断結果チェックボックス
        $extra  = " id=\"KEKKA_HA2\"";
        $extra .= $model->field["KEKKA_HA2"] == "on" ? "checked" : "";
        $arg["data"]["KEKKA_HA2"] = knjCreateCheckBox($objForm, "KEKKA_HA2", "on", $extra);

        //歯・口の健康診断結果（作成日付）
        $value = isset($model->field["KEKKA_HA2_DATE"]) ? $model->field["KEKKA_HA2_DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["KEKKA_HA2_DATE"] = View::popUpCalendar($objForm, "KEKKA_HA2_DATE", $value);

        //定期健康診断結果チェックボックス
        $extra  = " id=\"TEIKI_KENSIN\"";
        $extra .= $model->field["TEIKI_KENSIN"] == "on" ? "checked" : "";
        $arg["data"]["TEIKI_KENSIN"] = knjCreateCheckBox($objForm, "TEIKI_KENSIN", "on", $extra);

        //心電図検査出力チェックボックス
        $extra  = " id=\"HEART_MEDEXAM_PRINT\"";
        $extra .= $model->field["HEART_MEDEXAM_PRINT"] == "on" || $model->cmd == "" ? "checked" : "";
        $arg["data"]["HEART_MEDEXAM_PRINT"] = knjCreateCheckBox($objForm, "HEART_MEDEXAM_PRINT", "on", $extra);

        //結核検査(X線)出力チェックボックス
        $extra  = " id=\"TB_PRINT\"";
        $extra .= $model->field["TB_PRINT"] == "on" || $model->cmd == "" ? "checked" : "";
        $arg["data"]["TB_PRINT"] = knjCreateCheckBox($objForm, "TB_PRINT", "on", $extra);

        //定期健康診断（作成日付）
        $value = isset($model->field["TEIKI_KENSIN_DATE"]) ? $model->field["TEIKI_KENSIN_DATE"] : str_replace("-", "/", CTRL_DATE);
        $arg["data"]["TEIKI_KENSIN_DATE"] = View::popUpCalendar($objForm, "TEIKI_KENSIN_DATE", $value);

        //貧血出力チェックボックス
        $extra  = " id=\"ANEMIA_PRINT\"";
        $extra .= $model->field["ANEMIA_PRINT"] == "on" ? "checked" : "";
        $arg["data"]["ANEMIA_PRINT"] = knjCreateCheckBox($objForm, "ANEMIA_PRINT", "on", $extra);

        //定期健康診断結果一覧チェックボックス
        $extra  = " id=\"TEIKI_KENSIN_ITIRAN\"";
        $extra .= $model->field["TEIKI_KENSIN_ITIRAN"] == "on" ? "checked" : "";
        $arg["data"]["TEIKI_KENSIN_ITIRAN"] = knjCreateCheckBox($objForm, "TEIKI_KENSIN_ITIRAN", "on", $extra);

        //ボタンを作成する
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf030cForm1.html", $arg); 

    }

}

function makeClassItiran(&$objForm, &$arg, $db, &$model) {
        $row1 = array();
        $query = knjf030cQuery::getHrClassList($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        //2:個人表示指定用
        $opt_left = array();
        if ($model->field["KUBUN"] == 2) {
            if ($model->field["GRADE_HR_CLASS"] == "") $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];

            $extra = "onChange=\"return btn_submit('change_class');\"";
            $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

            $row1 = array();
            //生徒単位
            $selectleft = explode(",", $model->selectleft);
            $query = knjf030cQuery::getSchno($model);//生徒一覧取得
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"], 
                                                             "value" => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);

                if($model->cmd == 'change_class' ) {
                    if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)){
                        $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                        'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                    }
                } else {
                    $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                }
            }
            //左リストで選択されたものを再セット
            if($model->cmd == 'change_class' ) {
                foreach ($model->select_opt as $key => $val){
                    if (in_array($key, $selectleft)) {
                        $opt_left[] = $val;
                    }
                }
            }
        }

        $result->free();

        $chdt = $model->field["KUBUN"];

        //対象クラスリスト
        $extra = "multiple style=\"width:300px\" ondblclick=\"move1('left', $chdt)\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 18);

        //出力クラスリスト
        $extra = "multiple style=\"width:300px\" ondblclick=\"move1('right', $chdt)\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", $opt_left, $extra, 18);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', $chdt);\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', $chdt);\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', $chdt);\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', $chdt);\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "STAFFCD", STAFFCD);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", PROGRAMID);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "printKenkouSindanIppan", $model->printKenkouSindanIppan);
    knjCreateHidden($objForm, "useParasite_J", $model->Properties["useParasite_J"]);
    knjCreateHidden($objForm, "useParasite_H", $model->Properties["useParasite_H"]);
    knjCreateHidden($objForm, "kenkouSindanIppanNotPrintNameMstComboNamespare2Is1", $model->Properties["kenkouSindanIppanNotPrintNameMstComboNamespare2Is1"]);
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}

?>
