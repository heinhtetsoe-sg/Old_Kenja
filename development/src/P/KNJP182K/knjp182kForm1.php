<?php

require_once('for_php7.php');


class knjp182kForm1
{
    function main(&$model){

        $objForm = new form;

        $arg["start"]   = $objForm->get_start("knjp182kForm1", "POST", "knjp182kindex.php", "", "knjp182kForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //更新種別
        $opt = array(1, 2);
        $model->field["UPDATE_DIV"] = ($model->field["UPDATE_DIV"] == "") ? "2" : $model->field["UPDATE_DIV"];
        $extra = array("onclick =\" return btn_submit('knjp182k');\" id=\"UPDATE_DIV1\"", "onclick =\" return btn_submit('knjp182k');\" id=\"UPDATE_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "UPDATE_DIV", $model->field["UPDATE_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //開始日付
        $query = knjp182kQuery::getYMdata();
        $extra = "onChange=\"return btn_submit('knjp182k');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["KASAN_MONTH"], "KASAN_MONTH", $extra, 1, "");

        //1:加算(4月～6月),2:加算(7月～3月)
        list($ymd, $kasanDiv) = preg_split("/:/", $model->field["KASAN_MONTH"]);
        $model->field["KASAN_DIV"] = $kasanDiv;
        $model->field["KASAN_YMD"] = $ymd;

        //1:クラス,2:個人表示指定
        $opt = array(1, 2);
        $model->field["KUBUN"] = ($model->field["KUBUN"] == "") ? "1" : $model->field["KUBUN"];
        $extra = array("onclick =\" return btn_submit('knjp182k');\" id=\"KUBUN1\"", "onclick =\" return btn_submit('knjp182k');\" id=\"KUBUN2\"");
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        if ($model->field["KUBUN"] == 1) $arg["clsno"] = $model->field["KUBUN"];
        if ($model->field["KUBUN"] == 2) $arg["schno"] = $model->field["KUBUN"];

        //学期コンボ
        $query = knjp182kQuery::getSemesterMst();
        $extra = "onChange=\"return btn_submit('knjp182k');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //クラス一覧リスト
        $row1 = array();
        $query = knjp182kQuery::getHrClassAuth($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();

        //2:個人表示指定用
        $opt_left = array();
        if ($model->field["KUBUN"] == 2) {
            if ($model->field["GRADE_HR_CLASS"]=="") $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            $extra = "onChange=\"return btn_submit('change_class');\"";
            $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

            $row1 = array();
            //生徒単位
            $selectleft = explode(",", $model->selectleft);
            $query = knjp182kQuery::getSchno($model);//生徒一覧取得
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
            if ($model->cmd == 'change_class' ) {
                foreach ($model->select_opt as $key => $val) {
                    if (in_array($key, $selectleft)) {
                        $opt_left[] = $val;
                    }
                }
            }
        }
        $chdt = $model->field["KUBUN"];

        //出力対象一覧
        $extra = "multiple style=\"width=250px\" width=\"250px\" ondblclick=\"move1('left', $chdt)\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", $dummy, $row1, $extra, 20);

        //出力対象クラスリスト
        $extra = "multiple style=\"width=250px\" width=\"250px\" ondblclick=\"move1('right',$chdt)\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", $dummy, $opt_left, $extra, 20);

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

        //実行ボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "更新/CSV出力", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectleft");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp182kForm1.html", $arg); 

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
    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    $result->free();
}

?>
