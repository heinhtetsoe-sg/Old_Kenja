<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");
class knja040aForm1
{
    public function main($model)
    {
        $objForm = new form();

        //DB接続
        $db  = Query::dbCheckOut();

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        //動作条件チェック
        } elseif ($db->getOne(knja040aQuery::checktoStart()) == "0") {
            $arg["check"] = "Show_ErrMsg(1);";
        }
        //最終学期チェック
        if (CTRL_SEMESTER != $model->controls["学期数"]) {
            $arg["max_semes_cl"] = " max_semes_cl(); ";
        }
    
        //対象年度
        $arg["data"]["YEAR"] = CTRL_YEAR;
        $arg["data"]["SEMESTER"] = $model->controls["学期名"][CTRL_SEMESTER];
        $arg["data"]["NEWYEAR"]  = $model->new_year;
        $arg["data"]["NOWDATE"]  = knja040aQuery::getMaxUpdate($db);

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力
        $opt_shubetsu = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"]=="") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //対象データラジオボタン 1:入学者 2:保護者
        $opt_div = array(1, 2);
        $model->field["DATADIV"] = ($model->field["DATADIV"]=="") ? "1" : $model->field["DATADIV"];
        $click = "onClick =\" shorimei_show(this)\"";
        $extra = array("id=\"DATADIV1\"".$click, "id=\"DATADIV2\"".$click);
        $radioArray = knjCreateRadio($objForm, "DATADIV", $model->field["DATADIV"], $extra, $opt_div, get_count($opt_div));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //学校取得
        $query = knja040aQuery::checkSchool();
        $schoolDiv = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //処理名コンボボックス
        $opt_shori   = array();
        $opt_shori[] = array("label" => "更新","value" => "1");
        if ($schoolDiv["NAMESPARE2"] == "1") {
            $opt_shori[] = array("label" => "削除","value" => "2");
        }
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, "style=\"width:60px;\"", 1);

        //学年コンボボックス
        $opt_grade   = array();
        $opt_grade[] = array("label" => "1学年","value" => "01");
        if ($schoolDiv["NAMESPARE2"] == "1") {
            $opt_grade[] = array("label" => "4学年","value" => "04");
        }
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->grade, $opt_grade, "", 1);

        //課程学科コンボ
        $query = knja040aQuery::getCourseMajor(($model->new_year));
        makeCombo($objForm, $arg, $db, $query, $model->coursemajor, "COURSEMAJOR", "", 1);

        //肩書き出力
        $extra  = $model->field["GUARD_ADDR_FLG"] ? " checked " : "";
        $extra .= " id=\"GUARD_ADDR_FLG\"";
        $arg["data"]["GUARD_ADDR_FLG"] = knjCreateCheckBox($objForm, "GUARD_ADDR_FLG", "1", $extra);

        //開始日
        $disabled = "";
        $sDate = preg_split("{-}", $model->controls["学校入学日"]);
        $value = $model->field["GUARD_ISSUEDATE"] ? $model->field["GUARD_ISSUEDATE"] : ($sDate[0] + 1)."/".$sDate[1]."/".$sDate[2];
        $arg["data"]["GUARD_ISSUEDATE"] = View::popUpCalendarAlp($objForm, "GUARD_ISSUEDATE", $value, $disabled);

        //終了日
        $disabled = "";
        $eDate = preg_split("/-/", $model->controls["学校卒業日"]);
        $value = $model->field["GUARD_EXPIREDATE"] ? $model->field["GUARD_EXPIREDATE"] : ($eDate[0] + 3)."/03/31";
        $arg["data"]["GUARD_EXPIREDATE"] = View::popUpCalendarAlp($objForm, "GUARD_EXPIREDATE", $value, $disabled);

        //ヘッダ有無
        $extra = "checked id=\"HEADERCHECK\"";
        $arg["data"]["HEADERCHECK"] = knjCreateCheckBox($objForm, "HEADERCHECK", "1", $extra, "");

        //ファイルからの取り込み
        $arg["data"]["FILE"] = knjCreateFile($objForm, "FILE", "", 2048000);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knja040aindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja040aForm1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size)
{
    $opt = array();
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_cancel", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
}
