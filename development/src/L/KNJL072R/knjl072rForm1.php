<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl072rForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl072rindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl072rQuery::getNameMst("L003", $model->ObjYear, $model->fixApplicantDiv);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $query = knjl072rQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //ソート
        $opt = array(1, 2, 3);
        $model->sort = ($model->sort == "") ? "1" : $model->sort;
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SORT{$val}\" onClick=\"btn_submit('main')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SORT", $model->sort, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["TOP"][$key] = $val;

        //一覧表示

        //奨学生合格種別
        $query = knjl072rQuery::getNameMst("L031", $model->ObjYear);
        $result = $db->query($query);
        $setSyubetu = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setSyubetu[$row["VALUE"]] = $row["NAME1"];
            //hidden
            knjCreateHidden($objForm, "L031_".$row["VALUE"], $row["NAME1"]);
        }

        $arr_examno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $result = $db->query(knjl072rQuery::SelectQuery($model));

            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_examno[] = $row["EXAMNO"];
                //奨学生合格種別
                $row["JUDGE_KIND_NAME"] = $setSyubetu[$row["JUDGE_KIND"]];
                $row["JUDGE_KIND_ID"] = "ID_".$row["EXAMNO"];
                $extra = "style=\"text-align:right; ime-mode: disabled;\" onkeydown=\"keyChangeEntToTab(this);\" onblur=\"CheckScore(this, '".$row["EXAMNO"]."');\"";
                $row["JUDGE_KIND"] = knjCreateTextBox($objForm, $row["JUDGE_KIND"], "JUDGE_KIND-".$row["EXAMNO"], 1, 1, $extra);
                $arg["data"][] = $row;
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
        View::toHTML($model, "knjl072rForm1.html", $arg);
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
    knjCreateHidden($objForm, "HID_EXAMHALLCD");

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL072R");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
