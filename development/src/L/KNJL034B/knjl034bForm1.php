<?php

require_once('for_php7.php');

class knjl034bForm1
{
    function main(&$model){

        $objForm = new form;
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl034bForm1", "POST", "knjl034bindex.php", "", "knjl034bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl034bQuery::getYear();
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->ObjYear, $extra, 1);
        //$arg["TOP"]["YEAR"] = $model->ObjYear;

        //履歴一覧
        $query = knjl034bQuery::getList($model);
        $result = $db->query($query);
        while ($rirekiRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rirekiRow["PROMISE_RECOMMEND_TEST_FLG"] = ($rirekiRow["PROMISE_RECOMMEND_TEST_FLG"] == "1") ? 'レ' : "";
            $rirekiRow["PROMISE_GENERAL_TEST_FLG"] = ($rirekiRow["PROMISE_GENERAL_TEST_FLG"] == "1") ? 'レ' : "";

            $rirekiRow["TAKE_RECOMMEND_TEST_FLG"] = ($rirekiRow["TAKE_RECOMMEND_TEST_FLG"] == "1") ? 'レ' : "";
            $rirekiRow["TAKE_GENERAL_TEST_FLG"] = ($rirekiRow["TAKE_GENERAL_TEST_FLG"] == "1") ? 'レ' : "";
            $rirekiRow["CHANGE_SINGLE_TEST_FLG"] = ($rirekiRow["CHANGE_SINGLE_TEST_FLG"] == "1") ? 'レ' : "";
            if ($rirekiRow["NORMAL_PASSCOURSECD_NAME_SET"]) {
                $rirekiRow["NORMAL_PASSCOURSECD_NAME"] = $db->getOne(knjl034bQuery::getNameMst($model->ObjYear, "SEIKI", $model, $rirekiRow["NORMAL_PASSCOURSECD_NAME_SET"]));
            }
            if ($rirekiRow["EARLY_PASSCOURSECD_NAME_SET"]) {
                $rirekiRow["EARLY_PASSCOURSECD_NAME"] = $db->getOne(knjl034bQuery::getNameMst($model->ObjYear, "TANGAN", $model, $rirekiRow["EARLY_PASSCOURSECD_NAME_SET"]));
            }
            if ($rirekiRow["PASSCOURSE_DIV"] == "1") $rirekiRow["PASSCOURSE_DIV"] = '特進';
            if ($rirekiRow["PASSCOURSE_DIV"] == "2") $rirekiRow["PASSCOURSE_DIV"] = '進学';
            if ($rirekiRow["PASSCOURSE_DIV"] == "3") $rirekiRow["PASSCOURSE_DIV"] = '特進選抜';
            if ($rirekiRow["SCHOOLWORK_DIV"] == "1") $rirekiRow["SCHOOLWORK_DIV"] = '学業';
            if ($rirekiRow["SCHOOLWORK_DIV"] == "2") $rirekiRow["SCHOOLWORK_DIV"] = '体育';
            if ($rirekiRow["SPECIAL_DIV"] == "1") $rirekiRow["SPECIAL_DIV"] = '特待';
            if ($rirekiRow["SPECIAL_DIV"] == "2") $rirekiRow["SPECIAL_DIV"] = '準特待';
            
            $arg["data2"][] = $rirekiRow;
        }
        $result->free();
        
        //履歴から選択時
        if (!isset($model->warning) && ($model->cmd == "select")) {
            $query = knjl034bQuery::getList($model, "select");
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }
        
        //権限チェック
        if (AUTHORITY == DEF_UPDATABLE) {
            $arg["DEF_UPDATABLE"] = "1";
            $arg["BUTTON_COLSPAN"] = "2";
        } else {
            $arg["BUTTON_COLSPAN"] = "4";
        }
        
        //確約・合否詳細区分
        $extra = "";
        $arg["data"]["JUDGMENT_DIV"] = knjCreateTextBox($objForm, $Row["JUDGMENT_DIV"], "JUDGMENT_DIV", 2, 2, $extra);

        //確約区分の名称
        $extra = "";
        $arg["data"]["PROMISE_COURSE_NAME"] = knjCreateTextBox($objForm, $Row["PROMISE_COURSE_NAME"], "PROMISE_COURSE_NAME", 30, 30, $extra);
        
        //確約区分の略称
        $extra = "";
        $arg["data"]["PROMISE_COURSE_ABBV"] = knjCreateTextBox($objForm, $Row["PROMISE_COURSE_ABBV"], "PROMISE_COURSE_ABBV", 20, 20, $extra);
        
        //推薦入試確約
        if ($Row["PROMISE_RECOMMEND_TEST_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["PROMISE_RECOMMEND_TEST_FLG"] = knjCreateCheckBox($objForm, "PROMISE_RECOMMEND_TEST_FLG", "1", $extra);

        //一般入試確約
        if ($Row["PROMISE_GENERAL_TEST_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["PROMISE_GENERAL_TEST_FLG"] = knjCreateCheckBox($objForm, "PROMISE_GENERAL_TEST_FLG", "1", $extra);
        
        //合否区分の名称
        $extra = "";
        $arg["data"]["JUDGMENT_COURSE_NAME"] = knjCreateTextBox($objForm, $Row["JUDGMENT_COURSE_NAME"], "JUDGMENT_COURSE_NAME", 40, 40, $extra);
        
        //合否区分の略称
        $extra = "";
        $arg["data"]["JUDGMENT_COURSE_ABBV"] = knjCreateTextBox($objForm, $Row["JUDGMENT_COURSE_ABBV"], "JUDGMENT_COURSE_ABBV", 20, 20, $extra);
        
        //推薦入試(正規)
        if ($Row["TAKE_RECOMMEND_TEST_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["TAKE_RECOMMEND_TEST_FLG"] = knjCreateCheckBox($objForm, "TAKE_RECOMMEND_TEST_FLG", "1", $extra);
        
        //一般入試(正規)
        if ($Row["TAKE_GENERAL_TEST_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["TAKE_GENERAL_TEST_FLG"] = knjCreateCheckBox($objForm, "TAKE_GENERAL_TEST_FLG", "1", $extra);
        
        //一般入試(単願切換)
        if ($Row["CHANGE_SINGLE_TEST_FLG"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["CHANGE_SINGLE_TEST_FLG"] = knjCreateCheckBox($objForm, "CHANGE_SINGLE_TEST_FLG", "1", $extra);
        
        
        //正規合格コース
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $value_flg = false;
        $result = $db->query(knjl034bQuery::getNameMst($model->ObjYear, "SEIKI", $model, ""));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["NORMAL_PASSCOURSECD_NAME_SET"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $Row["NORMAL_PASSCOURSECD_NAME_SET"] = ($Row["NORMAL_PASSCOURSECD_NAME_SET"] && $value_flg) ? $Row["NORMAL_PASSCOURSECD_NAME_SET"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["NORMAL_PASSCOURSECD_NAME_SET"] = knjCreateCombo($objForm, "NORMAL_PASSCOURSECD_NAME_SET", $Row["NORMAL_PASSCOURSECD_NAME_SET"], $opt, $extra, 1);

        //単願切換合格コース
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $value_flg = false;
        $result = $db->query(knjl034bQuery::getNameMst($model->ObjYear, "TANGAN", $model, ""));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["EARLY_PASSCOURSECD_NAME_SET"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $Row["EARLY_PASSCOURSECD_NAME_SET"] = ($Row["EARLY_PASSCOURSECD_NAME_SET"] && $value_flg) ? $Row["EARLY_PASSCOURSECD_NAME_SET"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["EARLY_PASSCOURSECD_NAME_SET"] = knjCreateCombo($objForm, "EARLY_PASSCOURSECD_NAME_SET", $Row["EARLY_PASSCOURSECD_NAME_SET"], $opt, $extra, 1);

        //コース区分
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $opt[] = array('label' => "1:特進", 'value' => "1");
        $opt[] = array('label' => "2:進学", 'value' => "2");
        $opt[] = array('label' => "3:特進選抜", 'value' => "3");
        $extra = "";
        $arg["data"]["PASSCOURSE_DIV"] = knjCreateCombo($objForm, "PASSCOURSE_DIV", $Row["PASSCOURSE_DIV"], $opt, $extra, 1);
        
        //学/体区分
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $opt[] = array('label' => "1:学業", 'value' => "1");
        $opt[] = array('label' => "2:体育", 'value' => "2");
        $extra = "";
        $arg["data"]["SCHOOLWORK_DIV"] = knjCreateCombo($objForm, "SCHOOLWORK_DIV", $Row["SCHOOLWORK_DIV"], $opt, $extra, 1);
        
        //特待生区分
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $opt[] = array('label' => "1:特待", 'value' => "1");
        $opt[] = array('label' => "2:準特待", 'value' => "2");
        $extra = "";
        $arg["data"]["SPECIAL_DIV"] = knjCreateCombo($objForm, "SPECIAL_DIV", $Row["SPECIAL_DIV"], $opt, $extra, 1);

        //次年度作成ボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_year_add"] = knjCreateBtn($objForm, 'btn_year_add', '次年度作成', $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        
        //終了ボタン
        $extra = " onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL034B");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl034bForm1.html", $arg); 
        
    }
}
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
    if (get_count($opt) == 0) {
        $opt[] = array('label' => CTRL_YEAR+1,
                       'value' => CTRL_YEAR+1);
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
