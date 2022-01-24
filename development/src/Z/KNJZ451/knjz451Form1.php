<?php

require_once('for_php7.php');

class knjz451Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz451index.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $query = knjz451Query::getExeYear();
        $extra = "onchange=\"return btn_submit('combo');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1);

        //リスト作成
        makeList($arg, $db, $model);

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からデータをコピー", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        if ($model->cmd == "combo") {
            $arg["reload"] = "window.open('knjz451index.php?cmd=edit','right_frame')";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz451Form1.html", $arg); 
    }
}

//リスト作成
function makeList(&$arg, $db, $model) {
    $bifKey = "";
    $query = knjz451Query::getList($model);
    $result = $db->query($query);
    $listData  = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        if ($bifKey !== $row["QUALIFIED_CD"]) {
            if ($listData) {
                $arg["data"][]["MAINLIST"] = $listData."</tr>";
            }
            $query = knjz451Query::getGroupCnt($model, $row["QUALIFIED_CD"]);
            $cnt = $db->getOne($query);
            $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;

            $listData  = "<tr>";
            $listData .= "<td nowrap bgcolor=\"#ffffff\" rowspan={$cnt}><a href=\"knjz451index.php?cmd=edit&SEND_FLG=1&QUALIFIED_CD={$row["QUALIFIED_CD"]}\" target=\"right_frame\">{$row["QUALIFIED_NAME"]}</a></td>";
            $listData .= "<td nowrap align=\"center\" bgcolor=\"#ffffff\" rowspan={$cnt}>{$row["LIMIT_MONTH"]}</td>";
            $listData .= "<td nowrap align=\"center\" bgcolor=\"#ffffff\" rowspan={$cnt}>{$row["SETUP_CNT"]}</td>";
            $listData .= "<td nowrap bgcolor=\"#ffffff\">{$row["SETUP_QUALIFIED_NAME"]}</td>";
        } else {
            $listData .= "</tr><tr>";
            $listData .= "<td nowrap bgcolor=\"#ffffff\">{$row["SETUP_QUALIFIED_NAME"]}</td>";
        }
        $bifKey = $row["QUALIFIED_CD"];
    }
    if ($listData) {
        $arg["data"][]["MAINLIST"] = $listData."</tr>";
    }
    $result->free();
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

?>
