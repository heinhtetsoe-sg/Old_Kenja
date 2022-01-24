<?php

require_once('for_php7.php');

class knjl503iForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl503iindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjl503iQuery::getYearQuery($model);
        $extra = "onchange=\"return btn_submit('chgYear');\"";
        makeCmb($objForm, $arg, $db, $query, "ENTEXAMYEAR", $model->leftYear, $extra, 1, "");

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "次年度作成", $extra);


        //リスト作成
        $result = $db->query(knjl503iQuery::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $hash = array(
                "cmd"              => "edit",
                "TESTSUBCLASSCD"   => $row["TESTSUBCLASSCD"],
                "ENTEXAMYEAR"      => $model->leftYear
             );
            $row["TESTSUBCLASSCD"] = View::alink("knjl503iindex.php", $row["TESTSUBCLASSNAME"], "target=\"right_frame\"", $hash);
            $arg["data"][] = $row;
        }

        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        $flg = in_array($model->cmd, array("list", "chgYear", "chgAppDiv", "chgTestDiv", "copy"));
        if (!isset($model->warning) && $flg) {
            $arg["reload"]= "parent.right_frame.location.href='knjl503iindex.php?cmd=edit"
                             . "&ENTEXAMYEAR=".$model->leftYear."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl503iForm1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($name == 'APPLICANTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') {
                $value = $row["VALUE"];
            }
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
//権限チェック
function authCheck(&$arg)
{
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}
?>

