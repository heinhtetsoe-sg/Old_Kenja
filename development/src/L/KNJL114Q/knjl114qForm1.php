<?php

require_once('for_php7.php');

class knjl114qForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //一覧表示
        $arr_examno = array();
        $dataflg = false;

        //データ取得
        $query = knjl114qQuery::SelectQuery($model, "list");
        $result = $db->query($query);

        //データが1件もなかったらメッセージを返す
        if ($result->numRows() == 0 ) {
            $model->setWarning("MSG303");
        }

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            //HIDDENに保持する用
            $arr_examno[] = $row["EXAMNO"];
            
            //第一志望
            $intValArray = array(1=>"1", 2=>"2", 3=>"3", 4=>"4");
            $intLabelArray = array(1=>"音楽", 2=>"美術", 3=>"書道", 4=>"どれでも");
            $row["ART_SELECT_FST"] = makeCustomRadio($objForm, $row, "ART_SELECT_FST_VAL", "ART_SELECT_FST", $intValArray, $intLabelArray, "4");
            
            ////第二志望
            $intValArray = array(1=>"1", 2=>"2", 3=>"3", 4=>"4");
            $intLabelArray = array(1=>"音楽", 2=>"美術", 3=>"書道", 4=>"どれでも");
            $row["ART_SELECT_SND"] = makeCustomRadio($objForm, $row, "ART_SELECT_SND_VAL", "ART_SELECT_SND", $intValArray, $intLabelArray, "4");
            
            $dataflg = true;

            $arg["data"][] = $row;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $dataflg);

        //hidden作成
        makeHidden($objForm, $model, $arr_examno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl114qindex.php", "", "main");

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl114qForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
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
function makeBtn(&$objForm, &$arg, $model, $dataflg) {
    $disable  = $dataflg ? "" : " disabled";
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
function makeHidden(&$objForm, $model, $arr_examno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_EXAMNO", implode(",",$arr_examno));
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL114Q");
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
}

function makeCustomRadio($objForm, $row, $dataname, $fieldname, $intValArray, $intLabelArray, $defval="1", $click="") {
    //例：$click = "onClick=\"clickUpdate(this, '{$row["RECEPTNO"]}');\"";
    $extra = array();
    $value = strlen($row[$dataname]) ? $row[$dataname] : $defval;
    foreach ($intValArray as $akey => $aval) $extra[] = "id=\"".$fieldname."-".$row["EXAMNO"]."_".$akey."\"".$click;
    $radioArray = knjCreateRadio($objForm, $fieldname."-".$row["EXAMNO"], $value, $extra, $intValArray, get_count($intValArray));

    $counter = 1;
    $setData = $sep = "";
    foreach($radioArray as $rkey => $rval) {
        $setData .= $sep.$rval."<LABEL for=\"".$fieldname."-".$row["EXAMNO"]."_".$counter."\">".$intLabelArray[$counter]."</LABEL>";
        $counter++;
        $sep = "&nbsp;&nbsp;";
    }
    return $setData;
}

?>
