<?php

require_once('for_php7.php');

class knja141aForm1 {
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja141aForm1", "POST", "knja141aindex.php", "", "knja141aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);

        //学期
        $semesterName = $db->getOne(knja141aQuery::getSemeMst());
        $arg["data"]["GAKKI"] = $semesterName;
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);

        //フォーム選択・・・1:仮身分証明書(新入生), 2:身分証明書(在籍), 3:仮身分証明書(在籍)
        $model->field["OUTPUT"] = isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "3";//---2005.06.10
        $ext = "onclick=\"return btn_submit('output');\"";
        $opt_output = array(1, 2, 3);
        $extra  = array("id=\"OUTPUT1\" ".$ext, "id=\"OUTPUT2\" ".$ext, "id=\"OUTPUT3\" ".$ext);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_output, get_count($opt_output));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //1:個人  固定
        knjCreateHidden($objForm, "DISP", "1");

        //クラス一覧リスト
        $row1 = array();
        $class_flg = false;
        $query = knja141aQuery::getHrClassAuth();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $class_flg = true;
        }

        //1:個人表示指定用
        $opt_left = array();
        if ($model->field["GRADE_HR_CLASS"]=="" || !$class_flg) $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];

        $extra = "onChange=\"return btn_submit('change_class');\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        $row1 = array();
        //生徒単位
        $selectleft = explode(",", $model->selectleft);
        $query = knja141aQuery::getSchno2($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"], 
                                                         "value" => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);

            if ($model->cmd == 'change_class' ) {
                if (!in_array($row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"], $selectleft)) {
                    $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"],
                                    'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
                }
            } else {
                $row1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME"],
                                'value' => $row["SCHREGNO"]."-".$row["GRADE"].$row["HR_CLASS"].$row["ATTENDNO"]);
            }
        }
        //左リストで選択されたものを再セット
        if ($model->cmd == 'change_class') {
            foreach ($model->select_opt as $key => $val){
                if (in_array($key, $selectleft)) {
                    $opt_left[] = $val;
                }
            }
        }

        $result->free();

        //1:個人表示指定用
        $chdt = "1";
        //生徒一覧リスト
        $extra = "multiple style=\"width:250px\" ondblclick=\"move1('left', $chdt)\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $row1, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px\" ondblclick=\"move1('right', $chdt)\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left', $chdt);\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left', $chdt);\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right', $chdt);\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right', $chdt);\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //GRADE_CDに応じて、年度をセットする
        $gradeFlg = $db->getOne(knja141aQuery::getGradeCdFlg($model->field["GRADE_HR_CLASS"]));
        $setYear  = CTRL_YEAR + (int)$gradeFlg;

        //有効期限
        if (!isset($model->field["TERM_SDATE"])) { 
            $model->field["TERM_SDATE"] = str_replace("-", "/", CTRL_DATE);
        }
        if ($model->cmd == "change_class" || $model->cmd == "output" || !isset($model->field["TERM_EDATE"])) {
            $model->field["TERM_EDATE"] = $setYear."/03/31";
        }
        if ($model->field["OUTPUT"] == "3") {
             $model->field["TERM_EDATE"] = (CTRL_YEAR + 1)."/03/31";
        }
        $arg["data"]["TERM_SDATE"] = View::popUpCalendar($objForm, "TERM_SDATE", $model->field["TERM_SDATE"]);//開始
        $arg["data"]["TERM_EDATE"] = View::popUpCalendar($objForm, "TERM_EDATE", $model->field["TERM_EDATE"]);//終了

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA141");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "SCHOOL_JUDGE", knja141aQuery::getSchoolJudge());
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectleft");
        knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja141aForm1.html", $arg); 
    }
}
?>
