<?php

require_once('for_php7.php');

class knjl502jForm1 {
    function main(&$model) {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjl502jindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('changeCmb');\"";
        $query = knjl502jQuery::getNameMst($model, "L003");
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");

        //リスト作成
        $examType = "";
        $examtypeName = "";
        $subClass = "";
        $wkExamType = "";
        $wkExamtypeName = "";
        $wkSubClass = "";
        $sep = ",";
        $idx = 0;
        $maxCnt = $db->getOne(knjl502jQuery::selectQuery($model, "ALL"));
        $query  = knjl502jQuery::selectQuery($model);
        $result = $db->query($query);
        while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            if($examType == $row["EXAM_TYPE"]){
                $subClass = $subClass.$sep.$row["SUBCLASS"];
            }else{
                $wkExamType     = $row["EXAM_TYPE"];
                $wkExamtypeName = $row["EXAMTYPE_NAME"];
                $wkSubClass     = $row["SUBCLASS"];

                if($examType != ""){
                    $hash = array("cmd"             => "edit2",
                                  "APPLICANTDIV"    => $row["APPLICANTDIV"],
                                  "EXAM_TYPE"       => $examType);

                    $row["ROWSPAN2"]      = 1;
                    $row["EXAM_TYPE"]     = View::alink("knjl502jindex.php", $examType , "target=\"right_frame\"", $hash);
                    $row["EXAMTYPE_NAME"] = $examtypeName;
                    $row["SUBCLASS"]      = $subClass;

                    $arg["data"][] = $row;
                }

                $examType     = $wkExamType;
                $examtypeName = $wkExamtypeName;
                $subClass     = $wkSubClass;

            }

            $idx++;

            if($idx == $maxCnt){
                $hash = array("cmd"             => "edit2",
                              "APPLICANTDIV"    => $row["APPLICANTDIV"],
                              "EXAM_TYPE"       => $examType);

                $row["ROWSPAN2"]      = 1;
                $row["EXAM_TYPE"]     = View::alink("knjl502jindex.php", $examType, "target=\"right_frame\"", $hash);
                $row["EXAMTYPE_NAME"] = $examtypeName;
                $row["SUBCLASS"]      = $subClass;

                $arg["data"][] = $row;
            }
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd", "");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (!isset($model->warning) && ( VARS::post("cmd") == "copy" || VARS::post("cmd") == "changeCmb" )) {
            $arg["reload"] = "parent.right_frame.location.href='knjl502jindex.php?cmd=edit"
                           . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl502jForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
