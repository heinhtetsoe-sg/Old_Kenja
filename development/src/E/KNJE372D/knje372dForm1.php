<?php

require_once('for_php7.php');

class knje372dForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //権限チェック
        authCheck($arg);

        //ＤＢ接続
        $db = Query::dbCheckOut();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR"] = CTRL_YEAR ."年度";
        /* 学期 */
        $query = knje372dQuery::getSemester(CTRL_YEAR, CTRL_SEMESTER);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($row) {
            $arg["data"]["SEMESTER"] = $row["LABEL"];
        }

        /* 学年コンボ */
        $query = knje372dQuery::getGrade($model);
        $extra = "onchange=\"btn_submit('main');\"";
        $hrName = makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1, "");

        /* 考査コンボ */
        $query = knje372dQuery::getTestItem($model);
        $extra = "onchange=\"btn_submit('main');\"";
        $hrName = makeCmb($objForm, $arg, $db, $query, $model->field["TESTITEM"], "TESTITEM", $extra, 1, "");

        //学年の最大取得
        $gradeMax = "";
        $query = knje372dQuery::getGrade($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($gradeMax < $row["VALUE"]) {
                $gradeMax = $row["VALUE"];
            }
        }

        //ボタン作成
        makeButton($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "STAFF_AUTH", AUTHORITY);
        knjCreateHidden($objForm, "PASS_AUTH", DEF_UPDATABLE);
        knjCreateHidden($objForm, "GRADE_MAX", $gradeMax);

        //ＤＢ切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]  = $objForm->get_start("main", "POST", "knje372dindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje372dForm1.html", $arg);
    }
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }
    if ($name == "SEMESTER") {
        $value = ($value && in_array($value, $serch)) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //実行
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", "onclick=\"return btn_submit('execute');\"");
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}
