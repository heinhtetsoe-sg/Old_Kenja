<?php

require_once('for_php7.php');

class knjl550aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //ajax
        //学校名取得
        if ($model->cmd == "ajaxGetName") {
            $query = knjl550aQuery::getFinschoolName($model->ajaxParam["AJAX_YEAR"], $model->ajaxParam["AJAX_REMARK"]);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            echo json_encode($row["FINSCHOOL_NAME"]);
            die();
        }

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);

        //入試区分
        $query = knjl550aQuery::getNameMst($model->ObjYear, "L004");
        $extra = "onchange=\"return btn_submit('main');\" tabindex=1";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //志望区分
        $query = knjl550aQuery::getHopeCourse($model->ObjYear);
        $extra = "onchange=\"return btn_submit('main');\" tabindex=2";
        makeCmb($objForm, $arg, $db, $query, "DESIREDIV", $model->field["DESIREDIV"], $extra, 1, "blank");

        //表示順 (1:受験番号順 2:名前順)
        $opt = array(1, 2);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "1" : $model->field["SORT"];
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["TOP"][$key] = $val;
        }

        if ($model->cmd == "main") {
            $model->field["S_EXAMNO"] = "";
            $model->field["E_EXAMNO"] = "";
        }

        //受験番号範囲
        $query = knjl550aQuery::selectFstExamno($model);
        $fstExamno =  $db->getOne($query);
        $query = knjl550aQuery::selectLstExamno($model);
        $lstExamno =  $db->getOne($query);
        $model->field["S_EXAMNO"] = ($model->field["S_EXAMNO"]) ? sprintf("%05d", $model->field["S_EXAMNO"]) : "";
        $model->field["E_EXAMNO"] = ($model->field["E_EXAMNO"]) ? sprintf("%05d", $model->field["E_EXAMNO"]) : "";

        if ($model->field["E_EXAMNO"] != "" && $model->field["S_EXAMNO"] > $model->field["E_EXAMNO"]) {
            $chgwk = $model->field["E_EXAMNO"];
            $model->field["E_EXAMNO"] = $model->field["S_EXAMNO"];
            $model->field["S_EXAMNO"] = $chgwk;
        }

        //入力教科ラジオボタン
        $opt   = array();
        $extra = array();
        $label = array();
        $arr_classCd = array();
        $scoreName = "";
        $idx = 1;
        $model->field["DIV"] = ($model->field["DIV"] == "") ? "1" : $model->field["DIV"];
        $query = knjl550aQuery::getNameMst($model->ObjYear, "L009");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_push($opt, $idx);
            array_push($extra, "id=\"DIV".$idx."\" onclick=\"return btn_submit('reload');\" ");
            array_push($label, $row["NAME1"]);
            $scoreName .= "<td nowrap>".$row["NAME1"]."</td>";
            $arr_classCd[] = $row["NAMECD2"];
            $idx++;
        }
        $classCnt = $idx - 1; //科目数を保持

        //併願校を追加
        array_push($opt, $idx);
        array_push($extra, "id=\"DIV".$idx."\" onclick=\"return btn_submit('reload');\" ");
        array_push($label, "併願校");

        $radioArray = knjCreateRadio($objForm, "DIV", $model->field["DIV"], $extra, $opt, get_count($opt));
        $setSpase = "";

        $idx = 0;
        foreach ($radioArray as $key => $val) {
            $setVal .= $setSpase.$val."<LABEL for=\"{$key}\">".$label[$idx]."</LABEL>";
            $setSpase = "　";
            $idx++;
        }
        $arg["TOP"]["DIV"] = $setVal; //入力教科ラジオボタン
        $arg["HEAD"]["SCORE_NAME"] = $scoreName; //ヘッダ 教科

        $schoolFlg = false;
        $arr_examno = array();
        if ($model->cmd == "search" || $model->cmd == "reload" || $model->cmd == "back" || $model->cmd == "next") {
            //データ一覧取得
            $query = knjl550aQuery::selectQuery($model, $arr_classCd);
            $result = $db->query($query);
            //データが1件もなかったらメッセージを返す
            if ($result->numRows() == 0) {
                $model->setWarning("MSG303");
            }
            //一覧表示
            $arr_examno = array();
            $examno = array();
            $dataflg = false;
            $counter = 0;
            $valueFlg = false;
            if ($model->cmd == "search" || $model->cmd == "reset" || $model->cmd == "back" || $model->cmd == "next") {
                $valueFlg = true;
            }
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                $model->field["S_EXAMNO"] = $counter == 0 ? $row["EXAMNO"] : $model->field["S_EXAMNO"];
                $model->field["E_EXAMNO"] = $row["EXAMNO"];

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];

                //SCORE
                $setVal = "";
                $idx = 1;
                foreach ($arr_classCd as $key => $cd) {
                    $val = ($valueFlg) ? $row["SCORE".$cd] : $model->data["SCORE".$cd][$row["EXAMNO"]];
                    $extra  = " id=\"SCORE".$cd."_".$row["EXAMNO"]."\" onKeyDown=\"keyChangeEntToTab(this);\" ";
                    if ($model->field["DIV"] != $idx || $row["JUDGEDIV"] == "4") {
                        $extra .= "disabled";
                        knjCreateHidden($objForm, "HID_SCORE".$cd."_".$row["EXAMNO"], $val);
                    }
                    $setVal .= "<td align=\"center\" >";
                    $setVal .= knjCreateTextBox($objForm, $val, "SCORE".$cd."_".$row["EXAMNO"], 3, 3, $extra);
                    $setVal .= "</td>";
                    $idx++;
                }
                $row["SCORE"] = $setVal;

                //公立1 コード
                $val = ($valueFlg) ? $row["REMARK1"] : $model->data["REMARK1"][$row["EXAMNO"]];
                $extra = " onblur=\"showName('1', '{$row["EXAMNO"]}')\"";
                if ($model->field["DIV"] != ($classCnt+1) || $row["JUDGEDIV"] == "4") {
                    $extra .= "disabled";
                    knjCreateHidden($objForm, "HID_REMARK1_".$row["EXAMNO"], $val);
                }
                $row["REMARK1"] = knjCreateTextBox($objForm, $val, "REMARK1_".$row["EXAMNO"], 3, 3, $extra);
                $row["FINSCHOOL_DISP1"] = $row["FINSCHOOL_NAME1"];
                $row["FINSCHOOL_NAME1"] = "FINSCHOOL_NAME1_{$row["EXAMNO"]}";

                //公立2 コード
                $val = ($valueFlg) ? $row["REMARK2"] : $model->data["REMARK2"][$row["EXAMNO"]];
                $extra = " onblur=\"showName('2', '{$row["EXAMNO"]}')\"";
                if ($model->field["DIV"] != ($classCnt+1) || $row["JUDGEDIV"] == "4") {
                    $extra .= "disabled";
                    knjCreateHidden($objForm, "HID_REMARK2_".$row["EXAMNO"], $val);
                }
                $row["REMARK2"] = knjCreateTextBox($objForm, $val, "REMARK2_".$row["EXAMNO"], 3, 3, $extra);
                $row["FINSCHOOL_DISP2"] = $row["FINSCHOOL_NAME2"];
                $row["FINSCHOOL_NAME2"] = "FINSCHOOL_NAME2_{$row["EXAMNO"]}";

                //私立1 コード
                $val = ($valueFlg) ? $row["REMARK3"] : $model->data["REMARK3"][$row["EXAMNO"]];
                $extra = " onblur=\"showName('3', '{$row["EXAMNO"]}')\"";
                if ($model->field["DIV"] != ($classCnt+1) || $row["JUDGEDIV"] == "4") {
                    $extra .= "disabled";
                    knjCreateHidden($objForm, "HID_REMARK3_".$row["EXAMNO"], $val);
                }
                $row["REMARK3"] = knjCreateTextBox($objForm, $val, "REMARK3_".$row["EXAMNO"], 3, 3, $extra);
                $row["FINSCHOOL_DISP3"] = $row["FINSCHOOL_NAME3"];
                $row["FINSCHOOL_NAME3"] = "FINSCHOOL_NAME3_{$row["EXAMNO"]}";

                //私立2 コード
                $val = ($valueFlg) ? $row["REMARK4"] : $model->data["REMARK4"][$row["EXAMNO"]];
                $extra = " onblur=\"showName('4', '{$row["EXAMNO"]}')\"";
                if ($model->field["DIV"] != ($classCnt+1) || $row["JUDGEDIV"] == "4") {
                    $extra .= "disabled";
                    knjCreateHidden($objForm, "HID_REMARK4_".$row["EXAMNO"], $val);
                }
                $row["REMARK4"] = knjCreateTextBox($objForm, $val, "REMARK4_".$row["EXAMNO"], 3, 3, $extra);
                $row["FINSCHOOL_DISP4"] = $row["FINSCHOOL_NAME4"];
                $row["FINSCHOOL_NAME4"] = "FINSCHOOL_NAME4_{$row["EXAMNO"]}";

                $dataflg = true;

                $arg["data"][] = $row;
                $counter++;
            }
        }

        if ($schoolFlg) {
            $model->setWarning("MSG303", "　　（学籍学校コード）");
        }

        //受験番号
        $extra = " onchange=\"this.value=toAlphaNumber(this.value);\" tabindex=2";
        $arg["TOP"]["S_EXAMNO"] = knjCreateTextBox($objForm, $model->field["S_EXAMNO"], "S_EXAMNO", 5, 5, $extra);
        $extra = " onchange=\"this.value=toAlphaNumber(this.value);\" tabindex=3";
        $model->field["E_EXAMNO"] = ($model->field["E_EXAMNO"]) ? sprintf("%05d", $model->field["E_EXAMNO"]) : "";
        $arg["TOP"]["E_EXAMNO"] = $model->field["E_EXAMNO"];
        knjCreateHidden($objForm, "E_EXAMNO", $model->field["E_EXAMNO"]);

        $fsthidden = $fstExamno == $model->field["S_EXAMNO"] ? " disabled " : "";
        $extra = "onClick=\"btn_submit('back');\" tabindex=5".$fsthidden;
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        $lsthidden = $lstExamno == $model->field["E_EXAMNO"] ? " disabled " : "";
        $extra = "onClick=\"btn_submit('next');\" tabindex=6".$lsthidden;
        $arg["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno, $arr_classCd);

        //CSVフォーム部品作成
        makeCsvForm($objForm, $arg, $model, $dataflg);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl550aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjl550aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "－全て－", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $dataflg)
{
    $disable  = ($dataflg) ? "" : " disabled";

    //読込ボタン
    $extra = "onclick=\"return btn_submit('search');\" tabindex=4";
    $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "読 込", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"".$disable;
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"".$disable;
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return btn_submit('end');\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno, $arr_classCd)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",", $arr_examno));
    knjCreateHidden($objForm, "HID_CLASSCD", implode(",", $arr_classCd));
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL550A");
    knjCreateHidden($objForm, "CLASS_COUNT", $classCnt);
}

//CSVフォーム部品作成
function makeCsvForm(&$objForm, &$arg, $model, $dataflg)
{
    $disable  = ($dataflg) ? "" : " disabled";

    //ファイル
    $extra = "";
    $arg["csv"]["FILE"] = knjCreateFile($objForm, "FILE", 1024000, $extra);

    //実行
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["csv"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実行", $extra);

    //ヘッダ有チェックボックス
    if ($model->csvField["HEADER"] == "on") {
        $check_header = " checked";
    } else {
        $check_header = ($model->cmd == "main") ? " checked" : "";
    }
    $extra = "id=\"HEADER\"".$check_header;
    $arg["csv"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

    //CSV取込書出種別ラジオボタン 1:取込 2:ヘッダー 3:エラー
    $opt_shubetsu = array(1, 2, 3);
    $model->csvField["OUTPUT"] = ($model->csvField["OUTPUT"] == "") ? "1" : $model->csvField["OUTPUT"];
    $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->csvField["OUTPUT"], $extra, $opt_shubetsu, count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["csv"][$key] = $val;
    }
}
