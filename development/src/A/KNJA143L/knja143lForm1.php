<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knja143lForm1
{
    function main(&$model) {

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja143lForm1", "POST", "knja143lindex.php", "", "knja143lForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //学期マスタ
        $query = knja143lQuery::getSemeMst(CTRL_YEAR, CTRL_SEMESTER);
        $Row_Mst = $db->getRow($query,DB_FETCHMODE_ASSOC);

        //年度
        knjCreateHidden($objForm, "YEAR", $Row_Mst["YEAR"]);
        $arg["data"]["YEAR"] = $Row_Mst["YEAR"];

        //学期
        knjCreateHidden($objForm, "GAKKI", $Row_Mst["SEMESTER"]);
        $arg["data"]["GAKKI"] = $Row_Mst["SEMESTERNAME"];

        //発行日
        if (!isset($model->field["TERM_SDATE"])) {
            $model->field["TERM_SDATE"] = str_replace("-","/",CTRL_DATE);
        }
        $arg["data"]["TERM_SDATE"]=View::popUpCalendar($objForm,"TERM_SDATE",$model->field["TERM_SDATE"]);
        //クラス
        $query = knja143lQuery::getAuth(CTRL_YEAR, CTRL_SEMESTER, $model);
        $class_flg = false;
        $row1 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) {
                $class_flg = true;
            }
        }
        $result->free();

        if (!isset($model->field["GRADE_HR_CLASS"]) || !$class_flg) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $extra = "onchange=\"return btn_submit('knja143l');\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //生徒一覧リスト
        $opt_right = array();

        $query = knja143lQuery::getSchno($model, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["NAME"],
                                 'value' => $row["SCHREGNO"]);
        }
        $result->free();

        //生徒一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", array(), $extra, 20);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja143lForm1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //印刷ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA143L");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "knja143lExpireFlg" , $model->Properties["knja143lExpireFlg"]);
}

?>
