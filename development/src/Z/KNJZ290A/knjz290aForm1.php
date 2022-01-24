<?php

require_once('for_php7.php');

class knjz290aForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz290aindex.php", "", "sel");

        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $no_year = 0;
        //年度設定
        $result = $db->query(knjz290aQuery::selectYearQuery());
        $opt = array();

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"],
                           "value" => $row["YEAR"]);
            if($row["YEAR"] == $model->year) $no_year = 1;
        }
        if ($no_year == 0) $model->year = $opt[0]["value"];
        $result->free();

        //職員年度一覧取得
        $result = $db->query(knjz290aQuery::selectQuery($model));
        $opt_left_id = $opt_left = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[]    = array("label" => $row["STAFFCD"]."  ".$row["STAFFNAME"],
                                   "value" => $row["STAFFCD"]);
            $opt_left_id[] = $row["STAFFCD"];
        }
        $opt_right = array();
        $result->free();

        //職員一覧取得
        $result = $db->query(knjz290aQuery::selectStaffQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array("label" => $row["STAFFCD"]."  ".$row["STAFFNAME"],
                                 "value" => $row["STAFFCD"]);
        }
        $result->free();

        //年度
        $setNendo = "";

        //年度コンボボックス
        $extra = "onchange=\"return btn_submit('');\"";
        $setNendo = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        //年度テキスト
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $setNendo .= "&nbsp;&nbsp;".knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);

        //年度追加ボタン
        $extra = "onclick=\"return add('');\"";
        $setNendo .= "&nbsp;".knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

        $arg["year"] = array( "VAL" => $setNendo);

        //職員年度
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','staffyear','staffmaster',1)\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "staffyear", "left", $opt_left, $extra, 20);

        //職員マスタ
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','staffyear','staffmaster',1)\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "staffmaster", "left", $opt_right, $extra, 20);

        //追加ボタンを作成する
        $extra = "onclick=\"return move('sel_add_all','staffyear','staffmaster',1);\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        //追加ボタンを作成する
        $extra = "onclick=\"return move('left','staffyear','staffmaster',1);\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return move('right','staffyear','staffmaster',1);\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return move('sel_del_all','staffyear','staffmaster',1);\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //職員マスタボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ290A_2/knjz290a_2index.php?mode=1&SEND_PRGID=KNJZ290A&cmd=&SEND_SUBMIT=1&SEND_AUTH=".AUTHORITY;
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["BTN_MASTER"] = knjCreateBtn($objForm, "btn_master", " 職員マスタ ", $extra);

        //保存ボタンを作成する
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep", "更新", $extra);

        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終了", $extra)."&nbsp;&nbsp;".$syokuin;

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        $arg["info"]    = array("TOP"        => "対象年度",
                                "LEFT_LIST"  => "職員年度一覧",
                                "RIGHT_LIST" => "職員一覧");

        $arg["TITLE"]   = "マスタメンテナンス - 職員マスタメンテ";

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, TMPLDIRECTORY."/sel.html", $arg);
    }
}
?>
