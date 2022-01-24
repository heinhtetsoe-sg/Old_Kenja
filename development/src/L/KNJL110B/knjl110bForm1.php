<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl110bForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl110bindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl110bQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $query = knjl110bQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //単願切換チェックボックス
        if ($model->testdiv === '2') {
            $arg["tangan_kirikae"] = "1";
            $extra = "id=\"TANGAN_KIRIKAE\" onclick=\"btn_submit('main')\"";
            if ($model->tangan_kirikae == "1") {
                $extra .= "checked='checked' ";
            } else {
                $extra .= "";
            }
            $arg["TOP"]["TANGAN_KIRIKAE"] = knjCreateCheckBox($objForm, "TANGAN_KIRIKAE", "1", $extra);
        }

        //一覧表示
        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
                $result = $db->query(knjl110bQuery::SelectQuery($model));
                $count = 0;
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($row, "htmlspecialchars_array");
                    if ($row["REMARK1"] == "2") {
                        $row["REMARK2"] = $row["REMARK1"];
                    } else if ($row["REMARK1"] == "3") {
                        $row["REMARK3"] = $row["REMARK1"];
                    } else if ($row["REMARK1"] == "4") {
                        $row["REMARK4"] = $row["REMARK1"];
                    } else if ($row["REMARK1"] == "9") {
                        $row["REMARK5"] = $row["REMARK1"];
                    }
                    //HIDDENに保持する用
                    $arr_examno[] = $row["EXAMNO"].'-'.$count;
                    $extra = "onclick=\"check(this);\"";
                    $extra .= $row["REMARK1"] == "1" ? " checked " : "";
                    $row["REMARK1"] = knjCreateCheckBox($objForm, "REMARK1-".$count, "1", $extra);
                    $extra = "onclick=\"check(this);\"";
                    $extra .= $row["REMARK2"] == "2" ? " checked " : "";
                    $row["REMARK2"] = knjCreateCheckBox($objForm, "REMARK2-".$count, "2", $extra);
                    $extra = "onclick=\"check(this);\"";
                    $extra .= $row["REMARK3"] == "3" ? " checked " : "";
                    $row["REMARK3"] = knjCreateCheckBox($objForm, "REMARK3-".$count, "3", $extra);
                    $extra = "onclick=\"check(this);\"";
                    $extra .= $row["REMARK4"] == "4" ? " checked " : "";
                    $row["REMARK4"] = knjCreateCheckBox($objForm, "REMARK4-".$count, "4", $extra);
                    $extra = "onclick=\"check(this);\"";
                    $extra .= $row["REMARK5"] == "9" ? " checked " : "";
                    $row["REMARK5"] = knjCreateCheckBox($objForm, "REMARK5-".$count, "9", $extra);
                                        
                    $extra = $row["REMARK6"] == "1" ? " checked " : "";
                    $row["REMARK6"] = knjCreateCheckBox($objForm, "REMARK6-".$count, "1", $extra);
                    $extra = $row["REMARK7"] == "1" ? " checked " : "";
                    $row["REMARK7"] = knjCreateCheckBox($objForm, "REMARK7-".$count, "1", $extra);
                    $extra = $row["REMARK8"] == "1" ? " checked " : "";
                    $row["REMARK8"] = knjCreateCheckBox($objForm, "REMARK8-".$count, "1", $extra);
                    
                    //日付変換
                    $row["PROCEDUREDATE"] = str_replace("-", "/", $row["PROCEDUREDATE"]);
                    
                    $arg["data"][] = $row;
                    $count++;
                }
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl110bForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

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
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

}

//hidden作成
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL110B");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
