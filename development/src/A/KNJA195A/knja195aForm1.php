<?php

require_once('for_php7.php');


class knja195aForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja195aForm1", "POST", "knja195aindex.php", "", "knja195aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期名
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;

        //リスト表示選択
        $opt = array(1, 2); //1:在校生 2:卒業生
        if (!$model->field["KUBUN"]) $model->field["KUBUN"] = 1;
        $onClick = " onclick =\" return btn_submit('knja195a');\"";
        $extra = array("id=\"KUBUN1\"".$onClick, "id=\"KUBUN2\"".$onClick);
        $radioArray = knjCreateRadio($objForm, "KUBUN", $model->field["KUBUN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        $opt_left = array();
        $row1 = array();
        if ($model->field["KUBUN"] == 1) {

            $arg["ZAIKOUSEI"] = $model->field["KUBUN"];

            //クラス
            $query = knja195aQuery::getHrClassList($model);
            $extra = "onChange=\" return btn_submit('change_class');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "");

            $schQuery = knja195aQuery::getSchno($model);
        } else {

            $arg["SOTUGYOUSEI"] = $model->field["KUBUN"];

            //卒業年度
            $query = knja195aQuery::GetYear($model);
            $extra = "onChange=\" return btn_submit('change_class');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["G_YEAR"], "G_YEAR", $extra, 1, "");

            //卒業クラス
            $query = knja195aQuery::getHrclass($model);
            $extra = "onChange=\" return btn_submit('change_class');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1, "BLANK");

            //textbox
            $extra = "";
            $arg["data"]["KANJI"] = knjCreateTextBox($objForm, $model->field["KANJI"], "KANJI", 20, 10, $extra);

            //textbox
            $extra = "";
            $arg["data"]["KANA"] = knjCreateTextBox($objForm, $model->field["KANA"], "KANA", 20, 20, $extra);

            //検索ボタンを作成する
            $extra = "onClick=\"return btn_submit('search');\"";
            $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);

            //学校区分抽出
            $query = knja195aQuery::GetSchoolDiv($model);
            $model->schooldiv = $db->getOne($query);

            $schQuery = knja195aQuery::getGrdSchno($model);
        }

        //封筒
        $opt = array(1, 2); //1:封筒1 2:封筒2
        if (!$model->field["OUTPUT"]) $model->field["OUTPUT"] = 1;
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //生徒単位
        $selectleft = explode(",", $model->selectleft);
        $result = $db->query($schQuery);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["MARK"].$row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"], 
                                                         "value" => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);

            if ($model->cmd == 'change_class' ) {
                if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)) {
                    $row1[] = array('label' => $row["MARK"].$row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                }
            } else {
                $row1[] = array('label' => $row["MARK"].$row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"],
                                'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
            }
        }

        $result->free();
        //左リストで選択されたものを再セット
        if ($model->cmd == 'change_class' || $model->cmd == 'search') {
            foreach ($model->select_opt as $key => $val) {
                if (in_array($key, $selectleft)) {
                    $opt_left[] = $val;
                }
            }
        }

        $chdt = $model->field["KUBUN"];

        $value = "";
        //出力対象一覧
        $extra = "multiple style=\"width:250px\" ondblclick=\"move1('left', $chdt)\"";
        $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", $value, $row1, $extra, 18);

        //生徒一覧
        $extra = "multiple style=\"width:250px\" ondblclick=\"move1('right', $chdt)\"";
        $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", $value, $opt_left, $extra, 18);

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

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "selectleft");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "PROGRAMID");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja195aForm1.html", $arg); 

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
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
