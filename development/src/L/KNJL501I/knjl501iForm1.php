<?php

require_once('for_php7.php');

class knjl501iForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl501iindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $yearsLst = array();
        $findFlg = false;
        $result = $db->query(knjl501iQuery::getYear(CTRL_YEAR + 1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $yearsLst[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            if (!$findFlg) {
                $findFlg = $model->leftYear == $row["VALUE"] ? true : false;
            }
        }
        $result->free();
        if (!$findFlg && get_count($yearsLst) > 0) {
            $model->leftYear = $yearsLst[0]["value"];
        }
        $arg["ENTEXAMYEAR"] = knjCreateCombo($objForm, "ENTEXAMYEAR", $model->leftYear, $yearsLst, "onchange=\"return btn_submit('list');\"", 1);

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "次年度作成", $extra);

        //リスト作成
        $result = $db->query(knjl501iQuery::getList($model->leftYear, $model->applicantdiv));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $hash = array(
                "cmd"            => "edit",
                "TESTDIV"        => $row["TESTDIV"]
             );
            $row["TESTDIV"] = View::alink("knjl501iindex.php", $row["TESTDIV"], "target=\"right_frame\"", $hash);
            $row["TEST_DATE"] = str_replace("-", "/", $row["TEST_DATE"]);
            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl501iForm1.html", $arg);
    }
}

//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}
