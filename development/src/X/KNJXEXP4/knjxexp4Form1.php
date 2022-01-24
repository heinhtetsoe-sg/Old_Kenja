<?php

require_once('for_php7.php');

class knjxexp4Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");
        //DB接続
        $db     = Query::dbCheckOut();

        //extraセット
        $extraInt = "onblur=\"this.value=toInteger(this.value)\";";

        //出力種別ラジオボタン
        $opt_shubetsu[0] = 1;        //志願者
        $opt_shubetsu[1] = 2;        //在籍者

        if ($model->dispData["search_div"] != "" || $model->dispData["search_div2"] != "") {
            $model->search["SEARCH_DIV"] = ($model->search["SEARCH_DIV"] == "") ? "2" : $model->search["SEARCH_DIV"];
        } else {
            $model->search["SEARCH_DIV"] = ($model->dispData["serch2"] == "") ? "2" : $model->dispData["serch2"];
        }
        $extra = array("id=\"SEARCH_DIV1\"".$model->searchDivExtra, "id=\"SEARCH_DIV2\"".$model->searchDivExtra);
        $radioArray = knjCreateRadio($objForm, "SEARCH_DIV", $model->search["SEARCH_DIV"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //checkbox
        $extra = "id=\"SEARCH_TENHEN\"";
        if ($model->search["SEARCH_TENHEN"] == "1" || $model->cmd == "list") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["SEARCH_TENHEN"] = knjCreateCheckBox($objForm, "SEARCH_TENHEN", "1", $extra);

        //年組番号を表示するチェックボックス
        $extra = "id=\"HR_CLASS_HYOUJI_FLG\"";
        if ($model->search["HR_CLASS_HYOUJI_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["HR_CLASS_HYOUJI_FLG"] = knjCreateCheckBox($objForm, "HR_CLASS_HYOUJI_FLG", "1", $extra);

        //表示項目設定
        foreach ($model->dispData as $key => $val) {
            $arg[$key] = $val;
        }

        $hrclass_type = "";
        $disabledG = "";
        if ($model->Properties["useFi_Hrclass"] == "1") {
            //クラス方式選択 (1:法定クラス 2:複式クラス)
            $opt_type = array(1, 2);
            $opt_type_label = array("HR_CLASS_TYPE1" => "法定クラス", "HR_CLASS_TYPE2" => "複式クラス");
            $model->search["HR_CLASS_TYPE"] = ($model->search["HR_CLASS_TYPE"] == "") ? "1" : $model->search["HR_CLASS_TYPE"];
            $extra = array("id=\"HR_CLASS_TYPE1\"".$model->searchDivExtra, "id=\"HR_CLASS_TYPE2\"".$model->searchDivExtra);
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->search["HR_CLASS_TYPE"], $extra, $opt_type, get_count($opt_type));
            foreach($radioArray as $key => $val) {
                if ($hrclass_type != "") $hrclass_type .= " ";
                $hrclass_type .= $val."<LABEL for=\"{$key}\">".$opt_type_label[$key]."</LABEL>";
            }
        } else if ($model->Properties["useSpecial_Support_Hrclass"] == "1") {
            //クラス方式選択 (1:法定クラス 2:実クラス)
            $opt_type = array(1, 2);
            $opt_type_label = array("HR_CLASS_TYPE1" => "法定クラス", "HR_CLASS_TYPE2" => "実クラス");
            $model->search["HR_CLASS_TYPE"] = ($model->search["HR_CLASS_TYPE"] == "") ? "1" : $model->search["HR_CLASS_TYPE"];
            $extra = array("id=\"HR_CLASS_TYPE1\"".$model->searchDivExtra, "id=\"HR_CLASS_TYPE2\"".$model->searchDivExtra);
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->search["HR_CLASS_TYPE"], $extra, $opt_type, get_count($opt_type));
            foreach($radioArray as $key => $val) {
                if ($hrclass_type != "") $hrclass_type .= " ";
                $hrclass_type .= $val."<LABEL for=\"{$key}\">".$opt_type_label[$key]."</LABEL>";

                if ($key == "HR_CLASS_TYPE1") {
                    //学年混合チェックボックス
                    $extraG  = ($model->search["GAKUNEN_KONGOU"] == "1") ? " checked " : "";
                    $extraG .= ($model->search["HR_CLASS_TYPE"] == "1") ? "" : " disabled ";
                    $extraG .= " id=\"GAKUNEN_KONGOU\"".$model->searchDivExtra;
                    $hrclass_type .= "(".knjCreateCheckBox($objForm, "GAKUNEN_KONGOU", "1", $extraG)."<LABEL for=\"GAKUNEN_KONGOU\">学年混合</LABEL>)";
                }
            }
            $disabledG = ($model->search["HR_CLASS_TYPE"] == "2" || $model->search["GAKUNEN_KONGOU"] == "1") ? " disabled " : "";
        } else {
            //クラス方式選択 (1:法定クラス)
            $opt_type = array(1);
            $model->search["HR_CLASS_TYPE"] = ($model->search["HR_CLASS_TYPE"] == "") ? "1" : $model->search["HR_CLASS_TYPE"];
            $extra = array("id=\"HR_CLASS_TYPE1\"".$model->searchDivExtra);
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_TYPE", $model->search["HR_CLASS_TYPE"], $extra, $opt_type, get_count($opt_type));
            foreach($radioArray as $key => $val) {
                if ($hrclass_type != "") $hrclass_type .= " ";
                $hrclass_type .= $val."<LABEL for=\"{$key}\">法定クラス</LABEL>";
            }
        }
        $arg["HRCLASS_TYPE"] = $hrclass_type;

        //転退学
        $extra = " id=\"GRD_CHECK\" ";
        $checked = $model->search["GRD_CHECK"] == "1" ? " checked " : "";
        $arg["GRD_CHECK"] = knjCreateCheckBox($objForm, "GRD_CHECK", "1", $extra.$checked);

        //学年
        $query = knjxexp4Query::getGrade($model);
        makeCombo($objForm, $arg, $db, $query, $model->search["GRADE"], "GRADE", $disabled.$disabledG, 1, "BLANK");

        //年組
        $query = knjxexp4Query::getHrClass($model);
        makeCombo($objForm, $arg, $db, $query, $model->search["HR_CLASS"], "HR_CLASS", $disabled, 1, "BLANK");

        //入学年度
        $query = knjxexp4Query::getEntYear();
        makeCombo($objForm, $arg, $db, $query, $model->search["ENT_YEAR"], "ENT_YEAR", $disabled, 1, "BLANK");

        //受験区分A028
        $query = knjxexp4Query::getNameMst("A028");
        makeCombo($objForm, $arg, $db, $query, $model->search["A028"], "A028", $disabled, 1, "BLANK");

        //入金有無
        $query = knjxexp4Query::getUmu();
        makeCombo($objForm, $arg, $db, $query, $model->search["PAID_UMU"], "PAID_UMU", $disabled, 1, "BLANK");

        //卒業予定年度
        $query = knjxexp4Query::getGrdYear();
        makeCombo($objForm, $arg, $db, $query, $model->search["GRD_YEAR"], "GRD_YEAR", $disabled, 1, "BLANK");

        //課程・学科
        $query = knjxexp4Query::getCourseMajor();
        makeCombo($objForm, $arg, $db, $query, $model->search["COURSE_MAJOR"], "COURSE_MAJOR", $disabled, 1, "BLANK");

        //コース
        $query = knjxexp4Query::getCourseCode();
        makeCombo($objForm, $arg, $db, $query, $model->search["COURSECODE"], "COURSECODE", $disabled, 1, "BLANK");

        //学籍番号
        $arg["SCHREGNO"] = knjCreateTextBox($objForm, $model->search["SCHREGNO"], "SCHREGNO", 8, 8, $extraInt);

        //氏名
        $arg["NAME"] = knjCreateTextBox($objForm, $model->search["NAME"], "NAME", 40, 40, "");

        //かな氏名
        $arg["NAME_KANA"] = knjCreateTextBox($objForm, $model->search["NAME_KANA"], "NAME_KANA", 40, 40, "");

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //生徒リスト表示
        if ($model->cmd == "search") {
            makeStudentList($objForm, $arg, $db, $model);
            $model->firstFlg = false;
            $arg["search"] = 1;
        } else if ($model->cmd == "search2") {
            $arg["reload"] = "Link2('".REQUESTROOT."')";
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

        View::toHTML($model, "knjxexp4Form1.html", $arg);
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

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
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
    if ($model->search["HR_CLASS_HYOUJI_FLG"] == "1" || $model->dispData["sort"] == "1") {
        $arg["KOUMOKU1"] = 'クラス - 番';
        $arg["KOUMOKU2"] = '学籍番号';
    } else {
        $arg["KOUMOKU1"] = '学籍番号';
        $arg["KOUMOKU2"] = 'クラス - 番';
    }

    //テーブルカラムチェック
    $columnCnt = $db->getOne(knjxexp4Query::checkTableColumn("FRESHMAN_DAT","GRADE"));
    $model->isFreshmanGrade = "";
    if ($columnCnt > 0) {
        $model->isFreshmanGrade = "1";
    }

    $query = knjxexp4Query::GetStudents($model);
    $result = $db->query($query);
    $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
    $i = 0;
    list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $a = array("cmd"         => $cmd,
                   "SCHREGNO"    => $row["SCHREGNO"],
                   "SEARCH_DIV"  => $model->search["SEARCH_DIV"],
                   "HR_CLASS_HYOUJI_FLG" => $model->search["HR_CLASS_HYOUJI_FLG"],
                   "NAME"        => $row["NAME"]);
        $row["NAME"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME"]), "target=" .$model->target[$model->programid] ." onclick=\"Link(this)\"",$a);
        $row["INFONO"] = $row["SCHREGNO"];
        $row["IMAGE"] = $image[($row["SEX"]-1)];
        $row["GRD_DATE"] = str_replace("-", "/", $row["GRD_DATE"]);
        if ($model->search["HR_CLASS_HYOUJI_FLG"] == "1" || $model->dispData["sort"] == "1") {
            $row["KOUMOKU1_VALUE"] = $row["HR_ATTEND"];
            $row["KOUMOKU2_VALUE"] = $row["SCHREGNO"];
            $row["KOUMOKU1_align"] = "center";
            $row["KOUMOKU2_align"] = "right";
        } else {
            $row["KOUMOKU1_VALUE"] = $row["SCHREGNO"];
            $row["KOUMOKU2_VALUE"] = $row["HR_ATTEND"];
            $row["KOUMOKU1_align"] = "right";
            $row["KOUMOKU2_align"] = "center";
        }
        
        $arg["data"][] = $row;
        $i++;
    }
    $result->free();
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

?>
