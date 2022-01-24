<?php

require_once('for_php7.php');


class knjl515aForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        $CONST_SELALL = "99999";

        //年度
        $arg["TOP"]["YEAR"] = $model->examyear;

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=1";
        $query = knjl515aQuery::getNameMst("L004", $model->examyear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //志望区分コンボボックス
        $model->hope_coursecode = $model->hope_coursecode ? $model->hope_coursecode : $model->CONST_SELALL;
        $query = knjl515aQuery::getHopeCourseCd($model);
        makeCmb($objForm, $arg, $db, $query, "HOPE_COURSECODE", $model->hope_coursecode, $extra, 1, "SELALL", $model->CONST_SELALL);
        
        if($model->cmd == "main"){
            $model->field["S_EXAMNO"] = "";
            $model->field["E_EXAMNO"] = "";
        }

        //受験番号範囲
        $query = knjl515aQuery::SelectFstExamno($model);
        $fstExamno =  $db->getOne($query);
        $query = knjl515aQuery::SelectLstExamno($model);
        $lstExamno =  $db->getOne($query);
        $model->field["S_EXAMNO"] = ($model->field["S_EXAMNO"]) ? sprintf("%05d",$model->field["S_EXAMNO"]) : "";
        $model->field["E_EXAMNO"] = ($model->field["E_EXAMNO"]) ? sprintf("%05d",$model->field["E_EXAMNO"]) : "";

        if ($model->field["E_EXAMNO"] != "" && $model->field["S_EXAMNO"] > $model->field["E_EXAMNO"]) {
            $chgwk = $model->field["E_EXAMNO"];
            $model->field["E_EXAMNO"] = $model->field["S_EXAMNO"];
            $model->field["S_EXAMNO"] = $chgwk;
        }

        //各項目の教科名称取得
        $kyouka_count = 0;
        $kyouka5 = array();
        $result = $db->query(knjl515aQuery::getNameMst("L008", $model->examyear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $kyouka_count++;
            $arg["hd"]["ABBV1_".sprintf("%02d", $row["VALUE"])] = $row["ABBV1"];
            if ($row["NAMESPARE1"] == '1') $kyouka5[] = intval($row["VALUE"]);
        }
        knjCreateHidden($objForm, "kyouka_count", $kyouka_count);
        knjCreateHidden($objForm, "kyouka5", implode(',', $kyouka5));

        //一覧表示
        $searchCnt = 0;
        $arr_examno = array();
        if ($model->testdiv != "" && ($model->cmd == "read" || $model->cmd == "back" || $model->cmd == "next" || $model->cmd == "kirikae")) {
            //データ取得
            $searchCnt = $db->getOne(knjl515aQuery::SelectQuery($model, "COUNT"));
            $checkCnt = 50;
            if ($searchCnt == 0) {
                $model->setMessage("MSG303");
            } else {
                $counter = 0;
                $result    = $db->query(knjl515aQuery::SelectQuery($model, ""));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($row, "htmlspecialchars_array");
                    $model->field["S_EXAMNO"] = $counter == 0 ? $row["EXAMNO"] : $model->field["S_EXAMNO"];
                    $model->field["E_EXAMNO"] = $row["EXAMNO"];
                    //HIDDENに保持する用
                    $arr_examno[] = $row["EXAMNO"];
                    //受験番号を配列で取得
                    for ($colcnt = 1;$colcnt <= 9;$colcnt++) {
                        $model->fields["CONFIDENTIAL_RPT"][$colcnt] = $row["CONFIDENTIAL_RPT0".$colcnt];
                        $idxStr = $row["EXAMNO"]."_".$colcnt;
                        $extra = "id=\"".$idxStr."\"onblur=\"this.value=toInteger(this.value); lineSummary(this);\" onchange=\"Setflg(this);\" onKeyDown=\"keyChangeEntToTab(this);\"";
                        $row["CONFIDENTIAL_RPT0".$colcnt] = knjCreateTextBox($objForm, $model->fields["CONFIDENTIAL_RPT"][$colcnt], $idxStr, 2, 2, $extra);
                    }
                    $row["TOTAL5"] = ($row["TOTAL5"] ? $row["TOTAL5"] : "0");
                    $setttlidx = $row["EXAMNO"]."_TOTAL5";
                    knjCreateHidden($objForm, "HID_".$setttlidx, $row["TOTAL5"]);
                    $row["TOTAL5"] = "<div id=\"{$setttlidx}\">".$row["TOTAL5"]."</div>";

                    $row["TOTAL_ALL"] = ($row["TOTAL_ALL"] ? $row["TOTAL_ALL"] : "0");
                    $setttlidx = $row["EXAMNO"]."_TOTAL_ALL";
                    knjCreateHidden($objForm, "HID_".$setttlidx, $row["TOTAL_ALL"]);
                    $row["TOTAL_ALL"] = "<div id=\"{$setttlidx}\">".$row["TOTAL_ALL"]."</div>";
                    
                    $arg["data"][] = $row;
                    $counter++;
                }
            }
        }

        // //座席番号至
        $extra = " onchange=\"this.value=toAlphaNumber(this.value);\" tabindex=2";
        $arg["TOP"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->field["S_EXAMNO"], "S_EXAMNO", 5, 5, $extra);
        $extra = " onchange=\"this.value=toAlphaNumber(this.value);\" tabindex=3";
        $model->field["E_EXAMNO"] = ($model->field["E_EXAMNO"]) ? sprintf("%05d",$model->field["E_EXAMNO"]) : "";
        $arg["TOP"]["E_EXAMNO"] = $model->field["E_EXAMNO"];
        knjCreateHidden($objForm, "E_EXAMNO", $model->field["E_EXAMNO"]);

        $extra = "onClick=\"btn_submit('read');\" tabindex=4";
        $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読込", $extra);

        $fsthidden = $fstExamno == $model->field["S_EXAMNO"] ? " disabled " : "";
        $extra = "onClick=\"btn_submit('back');\" tabindex=5".$fsthidden;
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        $lsthidden = $lstExamno == $model->field["E_EXAMNO"] ? " disabled " : "";
        $extra = "onClick=\"btn_submit('next');\" tabindex=6".$lsthidden;
        $arg["button"]["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $searchCnt);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl515aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl515aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "", $selallval="") {
    $value_flg = false;
    $opt = array();
    $default_flg = true;
    $force_setflg = false;
    $i = $default = 0;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    } else if ($blank == "SELALL") {
        //リストに全てを設定
        $opt[] = array("label" => "全て", "value" => $selallval);
        //先に"全て"が設定されていたら、下記のSQLデータ取得ループでは"見つかったもの"としてフラグ設定する
        $value_flg = ($value == $selallval ? true : $value_flg);
        //$default_flgも不要。
        $default_flg = ($value_flg ? false : $default_flg);
        //先に"全て"が選択されていたら、SQLには"全て"が無いので、強制フラグをセットする。
        $force_setflg = ($value_flg ? true : false);
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($force_setflg || (!$force_setflg && $value && $value_flg)) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg ,$searchCnt) {
    //更新ボタン
    $disflg = $searchCnt == 0 ? " disabled " : "";
    $extra = "onclick=\"return btn_submit('update');\" tabindex=7".$disflg;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\" tabindex=8".$disflg;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\" tabindex=9";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $arr_examno));
    knjCreateHidden($objForm, "HID_TESTDIV");  //非活性直前の値保持用
    knjCreateHidden($objForm, "HID_HOPE_COURSECODE");  //非活性直前の値保持用

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL515A");
    knjCreateHidden($objForm, "YEAR", $model->examyear);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
}
?>
