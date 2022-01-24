<?php

require_once('for_php7.php');

class knjp081kForm1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjp081kindex.php", "", "sel");
        //DB接続
        $db = Query::dbCheckOut();

        $arg["CTRL_YEAR"] = CTRL_YEAR."年度";

        //出力ファイル様式
        $opt = array();
        $opt[] = array("label" => "01:生徒基本情報(生徒・負担者の住所等)", "value" => "1");
        $opt[] = array("label" => "02:年度振り情報(ＱＷ-３２)", "value" => "2");
        if (!$model->outformat) {
            $model->outformat = $opt[0]["value"];
        }
        $extra = "onchange=\" return btn_submit('main');\"";
        $arg["OUTFORMAT"] = knjCreateCombo($objForm, "OUTFORMAT", $model->outformat, $opt, $extra, 1);

        //学年クラス一覧取得
        $query = knjp081kQuery::selectQuery($model);
        $result = $db->query($query);
        $opt_left = $opt_right = $opt_grade = $selectLeft = array();
        if ($model->selectdata) {
            $selectLeft = explode(",", $model->selectdata);
        }
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($row["GRADE"] ."-" .$row["HR_CLASS"], $selectLeft)) {
                $opt_left[] = array("label" => $row["HR_NAME"],
                                    "value" => $row["GRADE"] ."-" .$row["HR_CLASS"]);
            } else {
                $opt_right[] = array("label" => $row["HR_NAME"],
                                     "value" => $row["GRADE"] ."-" .$row["HR_CLASS"]);
            }
            $opt_grade[$row["GRADE"]] = array("label" => $row["GRADE"] ."年",
                                              "value" => $row["GRADE"]);
        }
        $result->free();
        //学年一覧
        $extra = "onchange=\"chgGrade()\"";
        $arg["GRADE"] = knjCreateCombo($objForm, "GRADE", "", $opt_grade, $extra, 1);

        //対象一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','class_sel','class_all',1)\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "class_sel", "", $opt_left, $extra, 10);
        //クラス一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','class_sel','class_all',1)\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "class_all", "", $opt_right, $extra, 10);

        //ボタンを作成する
        $extra = "onclick=\"return move('sel_add_all','class_sel','class_all',1);\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
        //ボタンを作成する
        $extra = "onclick=\"return move('left','class_sel','class_all',1);\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
        //ボタンを作成する
        $extra = "onclick=\"return move('right','class_sel','class_all',1);\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
        //ボタンを作成する
        $extra = "onclick=\"return move('sel_del_all','class_sel','class_all',1);\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //生徒一覧取得
        $query = knjp081kQuery::selectSchregQuery($model, $selectLeft);
        $result = $db->query($query);
        $opt_left = $opt_right = $opt_Sch = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array("label" => $row["HR_NAME"]."-".$row["ATTENDNO"]."-".$row["NAME"],
                                 "value" => $row["GRADE"]."-".$row["HR_CLASS"]."-".$row["ATTENDNO"]."-".$row["SCHREGNO"]);
            $opt_Sch[$row["GRADE"]."-".$row["HR_CLASS"]] = array("label" => $row["HR_NAME"],
                                                                 "value" => $row["GRADE"]."-".$row["HR_CLASS"]);
        }
        $result->free();
        //クラス一覧
        $extra = "onchange=\"chgSchreg()\"";
        $arg["GRADE_HR"] = knjCreateCombo($objForm, "GRADE_HR", "", $opt_Sch, $extra, 1);

        //対象一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','schreg_sel','schreg_all',1)\"";
        $arg["main_part"]["LEFTSCH_PART"] = knjCreateCombo($objForm, "schreg_sel", "", $opt_left, $extra, 10);
        //クラス一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','schreg_sel','schreg_all',1)\"";
        $arg["main_part"]["RIGHTSCH_PART"] = knjCreateCombo($objForm, "schreg_all", "", $opt_right, $extra, 10);

        //ボタンを作成する
        $extra = "onclick=\"return move('sel_add_all','schreg_sel','schreg_all',1);\"";
        $arg["main_part"]["SELSCH_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
        //ボタンを作成する
        $extra = "onclick=\"return move('left','schreg_sel','schreg_all',1);\"";
        $arg["main_part"]["SELSCH_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
        //ボタンを作成する
        $extra = "onclick=\"return move('right','schreg_sel','schreg_all',1);\"";
        $arg["main_part"]["SELSCH_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
        //ボタンを作成する
        $extra = "onclick=\"return move('sel_del_all','schreg_sel','schreg_all',1);\"";
        $arg["main_part"]["SELSCH_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //留年生ラジオボタン 1:省かない 2:省く
        $opt_shubetsu = array(1, 2);
        $model->radio = ($model->radio) ? $model->radio : "1";
        $radioArray = knjCreateRadio($objForm, "RADIO", $model->radio, "", $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) $arg[$key] = $val;

        makeBtn($objForm, $arg);

        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp081kForm1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //読込ボタンを作成する
    $extra = "onclick=\"return btn_submit('read');\"";
    $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);
    //実行ボタンを作成する
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["button"]["btn_ok"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataSch");
}

?>
