<?php

require_once('for_php7.php');

class knjz440Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz440index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //教育課程コンボ
        $query = knjz440Query::getCurriculumCD($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["CURRICULUM_CD"], "CURRICULUM_CD", $extra, 1, 'BLANK');

        //課程学科コンボ
        $query = knjz440Query::getCouseMajor($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["COURSE_MAJOR"], "COURSE_MAJOR", $extra, 1, 'BLANK');

        //教科コンボ
        $query = knjz440Query::getClassName($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["CLASSCD"], "CLASSCD", $extra, 1, 'BLANK');

        //リスト表示
        if ($model->field["CURRICULUM_CD"] && $model->field["CLASSCD"] && $model->cmd == 'edit' &&
            $model->field["COURSE_MAJOR"]
        ) {
            makeList($objForm, $arg, $db, $model);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjz440Form1.html", $arg);
    }
}

/**************************************** 以下メソッド **********************************************************/

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $flg = 0;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        
        if ($row["VALUE"] == $value) {
            $flg = 1;
        }
    }
    $result->free();
    
    $value = $flg == 1 ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//リスト作成
function makeList(&$objForm, &$arg, $db, $model) {
    /****************科目名の表示*******************/
    $query = knjz440Query::getListTitle($model);
    $result = $db->query($query);
    $subclas = null;
    $titleGyou = '<th width="12">削</th><th width="20">No</th>';
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (mb_strlen($row["SUBCLASSABBV"]) == 1) {
            $cellWidth = 20;    //基本的にセルの幅は文字数を元に決めるけど、1文字の場合はチェックボックスの幅が広いため"20"を入れる
        } else {
            $cellWidth = mb_strlen($row["SUBCLASSABBV"]) * 15;    //セルの幅を文字数を元に決める
        }
        $titleGyouArray[] = array('width' => $cellWidth,
                                  'subclassAbbv' => $row["SUBCLASSABBV"]);
        $subclassCd[$row["VALUE"]] = 0;                  //SUBCLASSCDを配列にしておく別に0じゃなくてもかまわない、
        $cellWidths[] = $cellWidth;                           //それぞれのセル幅を配列に入れる
    }

    $titleGyouArray[get_count($titleGyouArray)-1]["width"] = '*'; //最後のセルの幅を"*"にする

    foreach ($titleGyouArray as $key => $val) {
        $titleGyou .= "<th width='{$val["width"]}'>{$val["subclassAbbv"]}</th>";
    }

    $cellWidths[get_count($cellWidths)-1] = '*';                  //最後のセルの幅を"*"にする
    $arg["titleGyou"] = $titleGyou;

    /****************リストの行の分だけ配列を作る*******************/
    $query = knjz440Query::getSeq($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $itiGyouBunNoData[$row["SEQ"]] = $subclassCd;
    }

    /****************リストの作成*******************/
    $query = knjz440Query::getList($model);
    $result = $db->query($query);
    $seq = null;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $itiGyouBunNoData[$row["SEQ"]][$row["VALUE"]] = 1; //チェックボックスの表示が必要であるというフラグ
    }
    $listGyou = null;
    if (is_array($itiGyouBunNoData)) {
        foreach($itiGyouBunNoData as $seq => $val) {
            $extra = "onclick=\"return btn_delete('{$seq}');\"";
            $inputButton = knjCreateBtn($objForm, "DELETESEQ", "", $extra);

            $listGyou .= "<tr><td width='12'>$inputButton</td>";
            $listGyou .= "<input type='hidden' id='DELETESEQ{$seq}' name='DELETESEQ{$seq}' value=''>";
            $listGyou .= "<td width='20'>{$seq}</td>";
            $i = 0;
            $cellWidthCount = get_count($cellWidths);
            foreach($val as $subclasscd => $flag) {
                if ($flag == 1) {
                    $value = $seq . ':' . $subclasscd;
                    $extra = "checked=\"checked\"";
                    $checkBox = makeCheckBox($objForm, $value, $extra);
                    $listGyou .= "<td width='{$cellWidths[$i]}'>$checkBox</td>\n"; //$cellWidthsにはそれぞれのセル幅が格納されている
                } else {
                    $value = $seq . ':' . $subclasscd;
                    $extra = "";
                    $checkBox = makeCheckBox($objForm, $value, $extra);
                    $listGyou .= "<td width='{$cellWidths[$i]}'>$checkBox</td>\n"; //$cellWidthsにはそれぞれのセル幅が格納されている
                }
                $i++;
            }
            $listGyou .= "</tr>\n";
        }
    }
    /****************最後に入力用の行を追加*******************/
    if (is_array($subclassCd)) {
        $lastSeq = $seq+1;
        $extra = "onclick=\"return btn_delete('{$lastSeq}');\"";
        $inputButton = knjCreateBtn($objForm, "DELETESEQ", "", $extra);

        $listGyou .= "<tr><td width='12'>{$inputButton}</td>";
        $listGyou .= "<input type='hidden' id='DELETESEQ{$lastSeq}' name='DELETESEQ{$lastSeq}' value=''>";
        $listGyou .= "<td width='20'>{$lastSeq}</td>";
        $i = 0;
        foreach($subclassCd as $eachSubclassCd => $val) {
            $value = $lastSeq . ':' . $eachSubclassCd;
            $checkBox = makeCheckBox($objForm, $value);
            $listGyou .= "<td width='{$cellWidths[$i]}'>$checkBox</td>\n";
            $i++;
        }
        $listGyou .= "</tr>\n";
    }
    $arg["listGyou"] = $listGyou;
}

//チェックボックス作成
function makeCheckBox(&$objForm, $value, $extra = "") {
    $checkBox = knjCreateCheckBox($objForm, "CHECKSUBCLASS[]", $value, $extra, "", "");

    return $checkBox;
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
}

?>
