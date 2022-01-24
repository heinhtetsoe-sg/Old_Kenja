<?php

require_once('for_php7.php');


class knjg100Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knjg100Form1", "POST", "knjg100index.php", "", "knjg100Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        //次年度セット
        $model->setYear = CTRL_YEAR + 1;

        //校種コンボ
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
        }
        $query = knjg100Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('knjg100');\"";
        makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->field["SCHKIND"], $extra, 1);

        //学期名
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //1:クラス,2:個人表示指定
        $opt_data = array(1, 2, 3);
        $model->field["KUBUN"] = ($model->field["KUBUN"] == "") ? "3" : $model->field["KUBUN"];
        $extra = array("id=\"KUBUN1\" onClick=\"btn_submit('knjg100')\"", "id=\"KUBUN2\" onClick=\"btn_submit('knjg100')\"", "id=\"KUBUN3\" onClick=\"btn_submit('knjg100')\"");
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt_data, get_count($opt_data));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["KUBUN"] == 1) {
            $arg["listtolist"] = $model->field["KUBUN"];
            $arg["gradeno"] = $model->field["KUBUN"];
            $arg["clsno"] = $model->field["KUBUN"];

            //学年コンボ
            $extra = "onChange=\"return btn_submit('knjg100');\"";
            $query = knjg100Query::getSelectGrade($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        } else if ($model->field["KUBUN"] == 2) {
            $arg["listtolist"] = $model->field["KUBUN"];
            $arg["schno"] = $model->field["KUBUN"];
        } else if ($model->field["KUBUN"] == 3) {
            $arg["gradeno"] = $model->field["KUBUN"];
            $arg["clsno"] = $model->field["KUBUN"];

            //学年コンボ
            $extra = "onChange=\"return btn_submit('knjg100');\"";
            $query = knjg100Query::getClassFormation($model);
            makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        }

        //記載日付
        if ($model->kidaiDate == "") $model->kidaiDate = str_replace("-","/",CTRL_DATE);
        $arg["data"]["KISAI_DATE"] = View::popUpCalendar($objForm  ,"KISAI_DATE" ,str_replace("-","/",$model->kidaiDate));

        //クラス一覧リスト
        makeClassItiran($objForm, $arg, $db, $model);

        //ボタンを作成する
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg100Form1.html", $arg); 

    }

}
/**************************************** 以下関数 **************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeClassItiran(&$objForm, &$arg, $db, &$model) {
        $row1 = array();
        $value_flg = false;
        if ($model->field["KUBUN"] == 3) {
            $query = knjg100Query::getClassFormation($model);
        } else if ($model->field["KUBUN"] == 2) {
            $query = knjg100Query::getHrClassAuth("", $model);
        } else {
            $query = knjg100Query::getHrClassAuth($model->field["GRADE"], $model);
        }
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $value_flg = true;
        }

        //2:個人表示指定用
        $opt_left = array();
        if ($model->field["KUBUN"] == 2 || $model->field["KUBUN"] == 3) {
            $model->field["GRADE_HR_CLASS"] = ($model->field["GRADE_HR_CLASS"] && $value_flg) ? $model->field["GRADE_HR_CLASS"] : $row1[0]["value"];
            $extra = "onChange=\"return btn_submit('change_class');\"";
            $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

            $row1 = array();
            //生徒単位
            $selectleft = explode(",", $model->selectleft);
            $query = knjg100Query::getSchno($model);//生徒一覧取得
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["SCHREGNO"]." ".$row["HR_NAME"]." ".$row["ATTENDNO"]."番 ".$row["NAME_SHOW"], 
                                                             "value" => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);

                if($model->cmd == 'change_class' ) {
                    if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)){
                        $row1[] = array('label' => $row["SCHREGNO"]." ".$row["HR_NAME"]." ".$row["ATTENDNO"]."番 ".$row["NAME_SHOW"],
                                        'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                    }
                } else {
                    $row1[] = array('label' => $row["SCHREGNO"]." ".$row["HR_NAME"]." ".$row["ATTENDNO"]."番 ".$row["NAME_SHOW"],
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
        $extra = "multiple style=\"width:250px\" ondblclick=\"move1('left', $chdt)\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $row1, $extra, 18);

        //出力クラスリスト
        $extra = "multiple style=\"width:250px\" ondblclick=\"move1('right', $chdt)\"";
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
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "PROGRAMID");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
    knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
}
?>
