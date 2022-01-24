<?php

require_once('for_php7.php');

class knjl016dForm1 {
    function main(&$model) {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl016dindex.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        $no_year        = 0;
        //年度設定
        $result = $db->query(knjl016dQuery::selectYearQuery($model));
        $opt    = array();

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["ENTEXAMYEAR"], 
                           "value" => $row["ENTEXAMYEAR"]);
            if ($row["ENTEXAMYEAR"] == $model->year)
                $no_year = 1;
        }
        if ($no_year == 0)
            $model->year = $opt[0]["value"];
        //年度コンボボックスを作成する
        $extra = "onchange=\"return btn_submit('');\"";
        $arg["year"]["VAL"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["year"]["VAL"] .= "&nbsp;&nbsp;".knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);

        $extra = "onclick=\"return add('');\"";
        $arg["year"]["VAL"] .= "&nbsp;".knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

/****ListToList****/
        //内部判定一覧取得
        $result      = $db->query(knjl016dQuery::selectQuery($model));
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[]    = array("label" => $row["LABEL"], 
                                   "value" => $row["VALUE"]);
            $opt_left_id[] = $row["VALUE"];
        }
        $opt_right = array();

        //一覧取得
        $result = $db->query(knjl016dQuery::selectJuniorQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array("label" => $row["LABEL"], 
                                 "value" => $row["VALUE"]);
        }
        $result->free();

        //内部判定年度
        $extra = "multiple STYLE=\"WIDTH:240px\" WIDTH=\"240px\" ondblclick=\"move('right','DECISIONCDYEAR','DECISIONCDMST',1)\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "DECISIONCDYEAR", "left", $opt_left, $extra, 20);

        //内部判定マスタ
        $extra = "multiple STYLE=\"WIDTH:240px\" WIDTH=\"240px\" ondblclick=\"move('left','DECISIONCDYEAR','DECISIONCDMST',1)\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "DECISIONCDMST", "right", $opt_right, $extra, 20);

        //追加ボタンを作成する(全て)
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('sel_add_all','DECISIONCDYEAR','DECISIONCDMST',1);\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        //追加ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('left','DECISIONCDYEAR','DECISIONCDMST',1);\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        //削除ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('right','DECISIONCDYEAR','DECISIONCDMST',1);\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        //削除ボタンを作成する(全て)
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('sel_del_all','DECISIONCDYEAR','DECISIONCDMST',1);\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);
/****ListToList(fin)****/

        //出身学校マスタボタンを作成する
        $link = REQUESTROOT."/L/KNJL016D_2/knjl016d_2index.php?mode=1";
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["BTN_MASTER"] = knjCreateBtn($objForm, "btn_master", " 内部判定マスタ ", $extra);

        //保存ボタンを作成する
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep", "更 新", $extra);

        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        $arg["info"]    = array("TOP"        => "対象年度",
                                "LEFT_LIST"  => "名称年度一覧",
                                "RIGHT_LIST" => "名称一覧");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl016dForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $opt[] = array('label' => '', 'value' => '');
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
