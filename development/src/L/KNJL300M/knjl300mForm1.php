<?php

require_once('for_php7.php');


class knjl300mForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl300mForm1", "POST", "knjl300mindex.php", "", "knjl300mForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックスを作成する
        $opt_app = array();
        $default_flg = false;
        $default     = 0 ;
        $result = $db->query(knjl300mQuery::getApplicantDiv("L003", $model->ObjYear));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_app[] = array("label" => $row["LABEL"], 
                               "value" => $row["VALUE"]);

            if ($row["NAMESPARE2"] == "" && !$default_flg){
                $default++;
            } else {
                $default_flg = true;
            }
        }
        $result->free();
        $model->field["APPLICANTDIV"] = (!isset($model->field["APPLICANTDIV"])) ? $opt_app[$default]["value"] : $model->field["APPLICANTDIV"];

        $extra = "onchange=\"return btn_submit('knjl300m'), AllClearList();\"";
        $arg["data"]["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"], $opt_app, $extra, 1);

        //試験会場選択コンボボックスを作成する
        $result = $db->query(knjl300mQuery::getHallData($model->field["APPLICANTDIV"], "1"));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $s_length = strlen((int)$row["S_RECEPTNO"]) + (3 - strlen((int)$row["S_RECEPTNO"])) * 6;
            $row["S_RECEPTNO"] = str_pad((int)$row["S_RECEPTNO"], $s_length, "&nbsp;", STR_PAD_LEFT);

            $e_length = strlen((int)$row["E_RECEPTNO"]) + (3 - strlen((int)$row["E_RECEPTNO"])) * 6;
            $row["E_RECEPTNO"] = str_pad((int)$row["E_RECEPTNO"], $e_length, "&nbsp;", STR_PAD_LEFT);

            $row["LABEL"] = $row["EXAMHALL_NAME"].' ( '.$row["S_RECEPTNO"].' ～ '.$row["E_RECEPTNO"].' )';

            $opt_list[]= array("label" => $row["LABEL"],
                               "value" => $row["VALUE"]);
        }
        $result->free();

        //対象会場リストを作成する
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move('right')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", array(), $extra, 20);

        //会場一覧リストを作成する
        $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move('left')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", isset($opt_list)?$opt_list:array(), $extra, 20);

        //対象選択ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('rightall');\"";
        $arg["button"]["btn_right2"] = knjCreateBtn($objForm, "btn_right2", ">>", $extra);
        //対象取消ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('leftall');\"";
        $arg["button"]["btn_left2"] = knjCreateBtn($objForm, "btn_left2", "<<", $extra);
        //対象選択ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        //対象取消ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //extra
        $extra = "onblur=\"this.value=toInteger(this.value)\"";

        //受付番号開始テキストボックスを作成する
        $model->field["NOINF_ST"] = (isset($model->field["NOINF_ST"])) ? $model->field["NOINF_ST"] : "";
        $arg["data"]["NOINF_ST"] = knjCreateTextBox($objForm, $model->field["NOINF_ST"], "noinf_st", 5, 3, $extra);

        //受付番号終了テキストボックスを作成する
        $model->field["NOINF_ED"] = (isset($model->field["NOINF_ED"])) ? $model->field["NOINF_ED"] : "";
        $arg["data"]["NOINF_ED"] = knjCreateTextBox($objForm, $model->field["NOINF_ST"], "noinf_ed", 5, 3, $extra);

        //開始位置（行）コンボボックスを作成する
        $row = array(array('label' => "１行",'value' => 1),
                     array('label' => "２行",'value' => 2),
                     array('label' => "３行",'value' => 3),
                     array('label' => "４行",'value' => 4),
                     array('label' => "５行",'value' => 5)
                    );
        $arg["data"]["POROW"] = knjCreateCombo($objForm, "POROW", $model->field["POROW"], isset($row)?$row:array(), "", 1);

        //開始位置（列）コンボボックスを作成する
        $col = array(array('label' => "１列",'value' => 1),
                     array('label' => "２列",'value' => 2),
                     array('label' => "３列",'value' => 3),
                     array('label' => "４列",'value' => 4)
                    );
        $arg["data"]["POCOL"] = knjCreateCombo($objForm, "POCOL", $model->field["POCOL"], isset($col)?$col:array(), "", 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl300mForm1.html", $arg); 
	}
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);
        knjCreateHidden($objForm, "TESTDV", "1");
        knjCreateHidden($objForm, "EXAM_TYPE", "1");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL300M");
        knjCreateHidden($objForm, "cmd");
}
?>
