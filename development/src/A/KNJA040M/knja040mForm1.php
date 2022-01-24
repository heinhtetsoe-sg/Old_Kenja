<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");
class knja040mForm1
{
    public function main($model)
    {
        $objForm = new form();

        //権限チェック
        if (AUTHORITY < DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        if ($model->cmd == "change") {
            unset($model->field["GUARD_ISSUEDATE"]);
            unset($model->field["GUARD_EXPIREDATE"]);
        }

        //対象データラジオボタン 1:入学者 2:保護者
        $opt_div = array(1, 2);
        $model->field["DATADIV"] = ($model->field["DATADIV"] == "") ? "1" : $model->field["DATADIV"];
        $extra = array("id=\"DATADIV1\"", "id=\"DATADIV2\"");
        $radioArray = knjCreateRadio($objForm, "DATADIV", $model->field["DATADIV"], $extra, $opt_div, get_count($opt_div));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力
        $opt_shubetsu = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //処理年度コンボ
        $query = knja040mQuery::getYear($model);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["EXE_YEAR"], "EXE_YEAR", $extra, 1, "");

        //住所2出力チェックボックス
        $extra  = $model->field["GUARD_ADDR_FLG"] ? " checked " : "";
        $extra .= " id=\"GUARD_ADDR_FLG\"";
        $arg["data"]["GUARD_ADDR_FLG"] = knjCreateCheckBox($objForm, "GUARD_ADDR_FLG", "1", $extra);

        //学校マスタ取得
        $query = knja040mQuery::getSchoolMst($model);
        $schData = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //開始日
        $disabled = "";
        $sDate = str_replace("-", "/", $schData["ENTRANCE_DATE"]);
        $value = $model->field["GUARD_ISSUEDATE"] ? $model->field["GUARD_ISSUEDATE"] : $sDate;
        $arg["data"]["GUARD_ISSUEDATE"] = View::popUpCalendarAlp($objForm, "GUARD_ISSUEDATE", $value, $disabled);

        //終了日
        $disabled = "";
        if ($model->field["GUARD_EXPIREDATE"]) {
            $value = $model->field["GUARD_EXPIREDATE"];
        } else {
            $eDate = preg_split("/-/", $schData["GRADUATE_DATE"]);
            if ($model->Properties["useSchool_KindField"] == "1") {
                $addYear = $db->getOne(knja040mQuery::getA023GradeRange());
            } else {
                $addYear = 2;
            }
            $value = ((int)$eDate[0] + (int)$addYear)."/03/31";
        }
        $arg["data"]["GUARD_EXPIREDATE"] = View::popUpCalendarAlp($objForm, "GUARD_EXPIREDATE", $value, $disabled);

        //ファイルからの取り込み
        $extra = "";
        $arg["data"]["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

        //ヘッダ有無
        $extra  = $model->field["HEADERCHECK"] ? " checked " : "";
        $extra .= " id=\"HEADERCHECK\"";
        $arg["data"]["HEADERCHECK"] = knjCreateCheckBox($objForm, "HEADERCHECK", $model->field["HEADERCHECK"], $extra);

        //前回実行
        $arg["data"]["NOWDATE"] = knja040mQuery::getMaxUpdate($db);

        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knja040mindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja040mForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "EXE_YEAR") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
