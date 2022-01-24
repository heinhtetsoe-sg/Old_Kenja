<?php
class knjxexp_subclassForm1
{
    public function main(&$model)
    {

        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");
        //DB接続
        $db     = Query::dbCheckOut();

        //extraセット
        $extraInt = "onblur=\"this.value=toInteger(this.value)\";";

//        //出力種別ラジオボタン
//        $opt_shubetsu[0] = 1;        //志願者
//        $opt_shubetsu[1] = 2;        //在籍者

//★出力に応じて設定？無視して設定？

        //年度表示
//var_dump($model);
        $arg["YEAR"] = $model->search["YEAR"];
        if ($model->dispData["semester"] != "") {
            //学期表示
            $query = knjxexp_subclassQuery::getSemesterName($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $printsems = $row["SEMESTERNAME"];
            }
            $arg["SEMESTER"] = $printsems;
        }

        if ($model->dispData["subclass"] != "") {
            //科目プルダウンリスト
            $query = knjxexp_subclassQuery::getSubclassList($model);
            $extra = "onChange=\"btn_submit('search')\";";
            makeCombo($objForm, $arg, $db, $query, $model->search["SUBCLASS"], "SUBCLASS", $extra, 1, "");
        }

        if ($model->dispData["subclass"] != "" && $model->dispData["chair"] != "") {
            //講座プルダウンリスト(科目の選択に依存。未指定(空文字)可。
            $query = knjxexp_subclassQuery::getChairList($model);
            makeCombo($objForm, $arg, $db, $query, $model->search["CHAIR"], "CHAIR", $disabled, 1, "BLANK");
        }

        if ($model->dispData["testtype"] != "") {
            //テスト種別
            $query = knjxexp_subclassQuery::getTestcd($model);
            $extra = "";
            if ($model->dispData["testtype"] == "2") {
                $extra .= "onChange=\"btn_submit('search')\";";
            }
            makeCombo($objForm, $arg, $db, $query, $model->search["TESTTYPE"], "TESTTYPE", $extra, 1, "");
        }

        if ($model->dispData["req_flg"] != "") {
            //受験者資格チェックボックス
            $extra = "id=\"REQ_FLG\"";
            if ($model->search["REQ_FLG"] == "1" || $model->cmd == "list") {
                $extra .= "checked='checked' ";
            } else {
                $extra .= "";
            }
            $arg["REQ_FLG"] = knjCreateCheckBox($objForm, "REQ_FLG", "1", $extra);
        }

//        if ($model->dispData["search_div"] != "") {
//            $model->search["SEARCH_DIV"] = ($model->search["SEARCH_DIV"] == "") ? "2" : $model->search["SEARCH_DIV"];
//        } else {
//            $model->search["SEARCH_DIV"] = ($model->dispData["serch2"] == "") ? "2" : $model->dispData["serch2"];
//        }
//        $extra = array("id=\"SEARCH_DIV1\"".$model->searchDivExtra, "id=\"SEARCH_DIV2\"".$model->searchDivExtra);
//        $radioArray = knjCreateRadio($objForm, "SEARCH_DIV", $model->search["SEARCH_DIV"], $extra, $opt_shubetsu, count($opt_shubetsu));
//        foreach($radioArray as $key => $val) $arg[$key] = $val;
//
//        //checkbox
//        $extra = "id=\"SEARCH_TENHEN\"";
//        if ($model->search["SEARCH_TENHEN"] == "1" || $model->cmd == "list") {
//            $extra .= "checked='checked' ";
//        } else {
//            $extra .= "";
//        }
//        $arg["SEARCH_TENHEN"] = knjCreateCheckBox($objForm, "SEARCH_TENHEN", "1", $extra);
//
//        //年組番号を表示するチェックボックス
//        $extra = "id=\"HR_CLASS_HYOUJI_FLG\"";
//        if ($model->search["HR_CLASS_HYOUJI_FLG"] == "1") {
//            $extra .= "checked='checked' ";
//        } else {
//            $extra .= "";
//        }
//        $arg["HR_CLASS_HYOUJI_FLG"] = knjCreateCheckBox($objForm, "HR_CLASS_HYOUJI_FLG", "1", $extra);
//
//        //表示項目設定
//        foreach ($model->dispData as $key => $val) {
//            $arg[$key] = $val;
//        }
//
//        //学年
//        $query = knjxexp_subclassQuery::getGrade($model);
//        makeCombo($objForm, $arg, $db, $query, $model->search["GRADE"], "GRADE", $disabled, 1, "BLANK");
//
//        //年組
//        $query = knjxexp_subclassQuery::getHrClass($model);
//        makeCombo($objForm, $arg, $db, $query, $model->search["HR_CLASS"], "HR_CLASS", $disabled, 1, "BLANK");
//
//        //入学年度
//        $query = knjxexp_subclassQuery::getEntYear();
//        makeCombo($objForm, $arg, $db, $query, $model->search["ENT_YEAR"], "ENT_YEAR", $disabled, 1, "BLANK");
//
//        //受験区分A028
//        $query = knjxexp_subclassQuery::getNameMst("A028");
//        makeCombo($objForm, $arg, $db, $query, $model->search["A028"], "A028", $disabled, 1, "BLANK");
//
//        //入金有無
//        $query = knjxexp_subclassQuery::getUmu();
//        makeCombo($objForm, $arg, $db, $query, $model->search["PAID_UMU"], "PAID_UMU", $disabled, 1, "BLANK");
//
//        //卒業予定年度
//        $query = knjxexp_subclassQuery::getGrdYear();
//        makeCombo($objForm, $arg, $db, $query, $model->search["GRD_YEAR"], "GRD_YEAR", $disabled, 1, "BLANK");
//
//        //学籍番号
//        $arg["SCHREGNO"] = knjCreateTextBox($objForm, $model->search["SCHREGNO"], "SCHREGNO", 8, 8, $extraInt);
//
//        //氏名
//        $arg["NAME"] = knjCreateTextBox($objForm, $model->search["NAME"], "NAME", 40, 40, "");
//
//        //かな氏名
//        $arg["NAME_KANA"] = knjCreateTextBox($objForm, $model->search["NAME_KANA"], "NAME_KANA", 40, 40, "");

        //表示項目設定
        foreach ($model->dispData as $key => $val) {
            $arg[$key] = $val;
        }

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //生徒リスト表示
        $cnt = "";
        if ($model->cmd == "search") {
            $cnt = makeStudentList($objForm, $arg, $db, $model);
            $model->firstFlg = false;
            $arg["search"] = 1;
        } elseif ($model->cmd == "search2") {
            $arg["reload"] = "Link2('".REQUESTROOT."')";
        }

        if ($model->cmd == "search" && $model->dispData["resultcnt"] != "") {
            //結果数表示
            if ($cnt !== "" && $cnt > 0) {
                $arg["RESULTCNT"] = $cnt;
                $model->aearch["RESULTCNT"] = $cnt;
            }
        }


        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->searchBack == "back") {
            echo "aa";
            $arg["jscript"] = "btn_submit('search')";
        }

        View::toHTML($model, "knjxexp_subclassForm1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();
    $value = ($value != "") ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//生徒リスト表示
function makeStudentList(&$objForm, &$arg, $db, $model)
{
    $arg["KOUMOKU1"] = '年組番';
    $arg["KOUMOKU2"] = '氏名';
    $arg["KOUMOKU3"] = '氏名かな';
    $arg["KOUMOKU4"] = '学籍番号';

    $i = "";
    if ($model->cmd == "search") {
        $query = knjxexp_subclassQuery::GetStudents($model);
        $result = $db->query($query);
//        $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
        $i = 0;
        list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $a = array("cmd"         => $cmd,
                       "SEND_YEAR"    => $model->search["YEAR"],
                       "SEND_SEMESTER"    => $model->search["SEMESTER"],
                       "SEND_TESTTYPE"    =>$model->search["TESTTYPE"],
                       "SEND_SUBCLASS"    => $model->search["SUBCLASS"],
                       "SEND_SCHREGNO"    => $row["SCHREGNO"],
//                       "SEARCH_DIV"  => $model->search["SEARCH_DIV"],
                       "NAME"        => $row["NAME"]);
            $row["NAME"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME"]), "target=" .$model->target[$model->programid] ." onclick=\"Link(this)\"", $a);
//            $row["INFONO"] = $row["SCHREGNO"];
//            $row["IMAGE"] = $image[($row["SEX"]-1)];
            $row["KOUMOKU1_VALUE"] = $row["GRADE_CD"] + $row["HR_CLASS"] + $row["ATTENDNO"];
//            $row["KOUMOKU2_VALUE"] = $row["NAME"];
            $row["KOUMOKU2_VALUE"] = $row["NAME_KANA"];
            $row["KOUMOKU3_VALUE"] = $row["SCHREGNO"];
            $row["KOUMOKU1_align"] = "center";
            $row["KOUMOKU2_align"] = "left";
            $row["KOUMOKU3_align"] = "left";
            $row["KOUMOKU4_align"] = "left";

            $arg["data"][] = $row;
            $i++;
        }
        $result->free();
    }
    return $i;
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //検索ボタンを作成する
    if ($model->searchMode) {
        $extra = "onclick=\"btn_submit('search2')\"";
    } else {
        $extra = "onclick=\"btn_submit('search')\"";
    }
    $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "検　索", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "path", REQUESTROOT .$model->path[$model->programid]);
    knjCreateHidden($objForm, "PROGRAMID", $model->programid);
    knjCreateHidden($objForm, "searchMode", $model->searchMode);

    list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);
    knjCreateHidden($objForm, "right_path", $path);

    foreach ($model->dispData as $key => $val) {
        knjCreateHidden($objForm, $key, $val);
    }
}
