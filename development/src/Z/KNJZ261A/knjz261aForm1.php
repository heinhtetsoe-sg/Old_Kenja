<?php

require_once('for_php7.php');

class knjz261aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("sel", "POST", "knjz261aindex.php", "", "sel");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        /**************/
        /*  年度設定  */
        /**************/
        $flg = "";
        $opt = array();
        $result = $db->query(knjz261aQuery::selectYearQuery());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"],
                           "value" => $row["YEAR"]);
            if ($model->year == $row["YEAR"]) $flg = true;
        }
        $result->free();
        $model->year = ($model->year && $flg) ? $model->year : CTRL_YEAR;

        //年度コンボ
        $extra = "onchange=\"return btn_submit('');\"";
        $year = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        //年度追加テキスト
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $year_add = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);

        //年度追加ボタン
        $extra = "onclick=\"return add('');\"";
        $btn_year_add = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

        $arg["year"]["VAL"] = $year."&nbsp;&nbsp;".$year_add."&nbsp;".$btn_year_add;


        /************************/
        /*  リストTOリスト作成  */
        /************************/
        //校務分掌年度一覧取得
        $opt_left = array();
        $result = $db->query(knjz261aQuery::selectQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);
        }
        $result->free();

        //校務分掌一覧取得
        $opt_right = array();
        $result = $db->query(knjz261aQuery::selectDutyshareQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
             $opt_right[] = array("label" => $row["LABEL"],
                                  "value" => $row["VALUE"]);
        }
        $result->free();

        //extra
        $extra_left     = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','CATEGORY_YEAR','CATEGORY_MASTER',1)\"";
        $extra_right    = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','CATEGORY_YEAR','CATEGORY_MASTER',1)\"";
        $extra_add_all  = "onclick=\"return move('sel_add_all','CATEGORY_YEAR','CATEGORY_MASTER',1);\"";
        $extra_add      = "onclick=\"return move('left','CATEGORY_YEAR','CATEGORY_MASTER',1);\"";
        $extra_del      = "onclick=\"return move('right','CATEGORY_YEAR','CATEGORY_MASTER',1);\"";
        $extra_del_all  = "onclick=\"return move('sel_del_all','CATEGORY_YEAR','CATEGORY_MASTER',1);\"";

        $arg["main_part"] = array("LEFT_PART"       => knjCreateCombo($objForm, "CATEGORY_YEAR", "left", $opt_left, $extra_left, 20),
                                  "RIGHT_PART"      => knjCreateCombo($objForm, "CATEGORY_MASTER", "left", $opt_right, $extra_right, 20),
                                  "SEL_ADD_ALL"     => knjCreateBtn($objForm, "sel_add_all", "≪", $extra_add_all),
                                  "SEL_ADD"         => knjCreateBtn($objForm, "sel_add", "＜", $extra_add),
                                  "SEL_DEL"         => knjCreateBtn($objForm, "sel_del", "＞", $extra_del),
                                  "SEL_DEL_ALL"     => knjCreateBtn($objForm, "sel_del_all", "≫", $extra_del_all));

        //ラベル作成
        $arg["info"]    = array("TOP"        => "対象年度",
                                "LEFT_LIST"  => "校務分掌年度一覧",
                                "RIGHT_LIST" => "校務分掌一覧");


        /****************/
        /*  ボタン作成  */
        /****************/
        //extra
        $link = REQUESTROOT."/Z/KNJZ261_2A/knjz261_2aindex.php?mode=1";
        $extra_mst      = "onclick=\"document.location.href='$link'\"";
        $extra_update   = "onclick=\"return doSubmit();\"";
        $extra_clear    = "onclick=\"return btn_submit('clear');\"";
        $extra_end      = "onclick=\"closeWin();\"";
        $link = REQUESTROOT."/Z/KNJZ261_DUTYSHARE_REFLECTION/knjz261_dutyshare_reflectionindex.php";
        $extra_torikomi = "onclick=\"document.location.href='$link'\"";

        $arg["button"] = array("BTN_MASTER" => knjCreateBtn($objForm, "btn_master", "校務分掌マスタ", $extra_mst),
                               "BTN_OK"     => knjCreateBtn($objForm, "btn_keep", "更 新", $extra_update),
                               "BTN_CLEAR"  => knjCreateBtn($objForm, "btn_clear", "取 消", $extra_clear),
                               "BTN_END"    => knjCreateBtn($objForm, "btn_end", "終 了", $extra_end)."&nbsp;&nbsp;".
                                               knjCreateBtn($objForm, "btn_torikomi", "教育委員会校務分掌取込", $extra_torikomi));

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, TMPLDIRECTORY."/sel.html", $arg);
    }
}
?>
