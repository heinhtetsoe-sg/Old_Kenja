<?php

require_once('for_php7.php');

class knjh536Form1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjh536index.php", "", "main");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");
        $extra = "style=\"width:60px;\"";
        $arg["data"]["SHORI_MEI"] = knjCreateCombo($objForm, "SHORI_MEI", $model->field["SHORI_MEI"], $opt_shori, $extra, 1);

        //出力取込種別ラジオボタン(1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            $check_header = ($model->cmd == "") ? "checked" : "";
        }
        $extra = "id=\"HEADER\"".$check_header;
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $extra);

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        //学期一覧コンボボックス
        $query = knjh536query::getSemester();
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        //学年一覧コンボボックス
        $query = knjh536query::getGrade($model);
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);

        //年組一覧コンボボックス
        $query = knjh536query::getGradeHrclass($model);
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE_HR_CLASS"], "GRADE_HR_CLASS", $extra, 1);

        /* データ種別コンボ */
        $query = knjh536query::getProfDiv();
        $extra = "onchange=\"btn_submit('');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCYDIV"], "PROFICIENCYDIV", $extra, 1);

        /* テスト名称コンボ */
        $query = knjh536query::getProfMst($model);
        $extra = "onchange=\"btn_submit('');\"";
        $testName = makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCYCD"], "PROFICIENCYCD", $extra, 1);

        /* 実力科目コンボ */
        $query = knjh536query::getProfSubclassMst($model);
        $extra = "";
        $subclassName = makeCmb($objForm, $arg, $db, $query, $model->field["PROFICIENCY_SUBCLASS_CD"], "PROFICIENCY_SUBCLASS_CD", $extra, 1, "BLANK");

        $query = knjh536query::getKwansei();
        $kwanseiFlg = $db->getOne($query);
        if ($kwanseiFlg) {
            $arg["KWANSEI"] = $kwanseiFlg;
            $opt = array(1, 2);
            $model->field["QUALIFIED"] = ($model->field["QUALIFIED"] == "") ? "1" : $model->field["QUALIFIED"];
            $extra = array("id=\"QUALIFIED1\"", "id=\"QUALIFIED2\"");
            $radioArray = knjCreateRadio($objForm, "QUALIFIED", $model->field["QUALIFIED"], $extra, $opt, get_count($opt));
            foreach ($radioArray as $key => $val) {
                $arg["data"][$key] = $val;
            }
        }
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh536Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "(全て出力)", "value" => "");
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

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
