<?php

require_once('for_php7.php');

class knjp1218Form1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjp1218Form1", "POST", "knjp1218index.php", "", "knjp1218Form1");

        $db = Query::dbCheckOut();

        //年度、学期を画面表示する
        $model->field["YEAR"] = ($model->field["YEAR"] == "") ? CTRL_YEAR : $model->field["YEAR"];
        $opt = array(array('label' => (CTRL_YEAR + 1),'value' => (CTRL_YEAR + 1)),
                     array('label' => CTRL_YEAR,'value' => CTRL_YEAR));
        $ext = "onChange=\"return btn_submit('knjp1218')\"";
        $arg["data"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $ext, 1);

        $model->field["SEMESTER"] = ($model->field["SEMESTER"] == "") ? CTRL_SEMESTER : $model->field["SEMESTER"];
        $row1 = array();
        $query = knjp1218Query::getSemester($model->field["YEAR"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $ext = "onChange=\"return btn_submit('knjp1218')\"";
        $arg["data"]["SEMESTER"] = knjCreateCombo($objForm, "SEMESTER", $model->field["SEMESTER"], $row1, $ext, 1);

        //年度
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        //学期
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);

        //志願者・在学者ラジオボタン
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"onClick=\"return btn_submit('knjp1218')\"", "id=\"OUTPUT2\"onClick=\"return btn_submit('knjp1218')\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //クラス選択コンボボックスを作成する
        //生徒名チェックボックスを作成
        if ($model->field["OUTPUT"] === '2') {
            $disabled = "";
        } else {
            $disabled = "disabled";
        }
        $row1 = array();
        $semes = CTRL_YEAR == $model->field["YEAR"] ? CTRL_SEMESTER : "1";
        $query = knjp1218Query::getAuth($model, $model->field["YEAR"], $semes);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        if (!isset($model->field["GRADE_HR_CLASS"]) && $model->hr_set === '') {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        } else if (!isset($model->field["GRADE_HR_CLASS"]) && $model->hr_set !== '') {
            $model->field["GRADE_HR_CLASS"] = $model->hr_set;
        }

        $extra = " onChange=\"return btn_submit('change_class');\"".$disabled;
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], isset($row1)?$row1:array(), $extra, 1);

        //ラジオボタンを戻した時に同じクラスを表示するための変数
        $model->hr_set = $model->field["GRADE_HR_CLASS"];

        //日付
        $model->field["PRINTDATE"] = $model->field["PRINTDATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["PRINTDATE"];
        $arg["PRINTDATE"] = View::popUpCalendar2($objForm, "PRINTDATE", $model->field["PRINTDATE"], "reload=true", "btn_submit('knjp1218')","");

        //生徒選択コンボボックスを作成する
        $opt_left = array();
        $selectleft = array();
        $selectleft = explode(",", $model->selectleft);
        $selectleftval = array();
        $selectleftval = explode(",", $model->selectleftval);
        $query = knjp1218Query::getSchno($model, $model->field["YEAR"], $semes);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["SCHREGNO"]."　".$row["NAME_SHOW"],
                                                         'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
            if ($model->cmd == 'read') {
                if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)) {
                    $row2[] = array('label' => $row["SCHREGNO"]."　".$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                }
            } else {
                $row2[] = array('label' => $row["SCHREGNO"]."　".$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
            }
        }

        //左リストで選択されたものを再セット
        if ($model->cmd == 'read') {
            for ($i = 0; $i < get_count($selectleft); $i++) {
                $opt_left[] = array("label" => $selectleftval[$i],
                "value" => $selectleft[$i]);
            }
        }

        $result->free();

        //対象者リストを作成する
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", $value, $opt_left, $extra, 20);

        //生徒一覧リストを作成する
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", $value, isset($row2)?$row2:array(), $extra, 20);

        //対象取り消しボタンを作成する(個別)
        $extra = " onclick=\"move('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "　＞　", $extra);

        //対象取り消しボタンを作成する(全て)
        $extra = " onclick=\"move('rightall');\"";
        $arg["button"]["btn_right2"] = knjCreateBtn($objForm, "btn_right2", "　≫　", $extra);

        //対象選択ボタンを作成する(個別)
        $extra = " onclick=\"move('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "　＜　", $extra);

        //対象選択ボタンを作成する(全て)
        $extra = " onclick=\"move('leftall');\"";
        $arg["button"]["btn_left2"] = knjCreateBtn($objForm, "btn_left2", "　≪　", $extra);

        //マスタ取得
        $query = knjp1218Query::getDocumentPrg("01");
        $documentRow01 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $query = knjp1218Query::getDocumentPrg("02");
        $documentRow02 = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectleft");
        knjCreateHidden($objForm, "selectleftval");
        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);

        $arg["TOP"]["CTRL_YEAR"]            = knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        $arg["TOP"]["CTRL_SEMESTER"]        = knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        $arg["TOP"]["CTRL_DATE"]            = knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        $arg["TOP"]["DBNAME"]               = knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        $arg["TOP"]["PRGID"]                = knjCreateHidden($objForm, "PRGID", "KNJP1218");
        $arg["TOP"]["SCHOOLCD"]             = knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
        $arg["TOP"]["SCHOOLKIND"]           = knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        $arg["TOP"]["useAddrField2"]        = knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
        $arg["TOP"]["useSchool_KindField"]  = knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);

        //印刷
        //if (!isset($model->warning) && ($model->cmd == 'read')) {
        //    $arg["printgo"] = "newwin('" . SERVLET_URL . "')";
        //}

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjp1218Form1.html", $arg);
    }
}
?>
