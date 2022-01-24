<?php

require_once('for_php7.php');
/********************************************************************/
/* 変更履歴                                                         */
/* ･NO001：一覧表示がされるよう修正                 山城 2004/11/17 */
/********************************************************************/

class knjj010Form1 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjj010index.php", "", "sel");
        $db             = Query::dbCheckOut();
        $no_year        = 0;
        //年度設定
        $result    = $db->query(knjj010Query::selectYearQuery());   
        $opt       = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"], 
                           "value" => $row["YEAR"]);
            if ($row["YEAR"] == $model->year)
                $no_year = 1;
        }
        if ($no_year==0) $model->year = $opt[0]["value"];

        //校種コンボ
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $model->schKind = SCHOOLKIND;
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $arg["schkind"] = "1";
            $query = knjj010Query::getSchkind($model);
            $extra = "onchange=\"return btn_submit('sel');\"";
            makeCmb($objForm, $arg, $db, $query, "SCHKIND", $model->schKind, $extra, 1);
        }

        //部クラブ年度一覧取得
        $result      = $db->query(knjj010Query::selectQuery($model));   
        $opt_left_id = $opt_left = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[]    = array("label" => $row["CLUBCD"]."　".$row["CLUBNAME"], 
                                   "value" => $row["CLUBCD"]);
            $opt_left_id[] = $row["CLUBCD"];
        }
        $opt_right = array();

        //部クラブ一覧取得
        $result = $db->query(knjj010Query::selectClassQuery($opt_left_id,$model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array("label" => $row["CLUBCD"]."　".$row["CLUBNAME"], 
                                 "value" => $row["CLUBCD"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //年度コンボボックスを作成する
        $extra = "onchange=\"return btn_submit('');\"";
        $yearCmb = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        //年度
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $value = "";
        $yearText = knjCreateTextBox($objForm, $value, "year_add", 5, 4, $extra);

        //年度追加
        $extra = "onclick=\"return add('');\"";
        $yearBtn = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

        $arg["year"]["VAL"] = $yearCmb."&nbsp;&nbsp;".$yearText."&nbsp;".$yearBtn;

        //部クラブ年度
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','classyear','classmaster','value')\"";
        $value = "left";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "classyear", $value, $opt_left, $extra, 20);
                        
        //部クラブマスタ
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','classyear','classmaster','value')\"";
        $value = "right";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "classmaster", $value, $opt_right, $extra, 20);

        //追加ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('sel_add_all','classyear','classmaster','value');\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        //追加ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('left','classyear','classmaster','value');\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        //削除ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('right','classyear','classmaster','value');\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        //削除ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('sel_del_all','classyear','classmaster','value');\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //部クラブマスタボタンを作成する
        $link  = REQUESTROOT."/J/KNJJ010_2/knjj010_2index.php?mode=1";
        $link .= "&SEND_selectSchoolKind=".$model->selectSchoolKind;

        $extra = "onclick=\"document.location.href='$link'\"";
        $clubMst = knjCreateBtn($objForm, "btn_master", " 部クラブマスタ ", $extra);

        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            //部クラブ校種設定マスタボタンを作成する
            $link  = REQUESTROOT."/J/KNJJ010_3/knjj010_3index.php?mode=1";
            $link .= "&SEND_selectSchoolKind=".$model->selectSchoolKind;
            $extra = "onclick=\"document.location.href='$link'\"";
            $clubMst .= "&nbsp;".knjCreateBtn($objForm, "BTN_DETAIL", " 部クラブ校種 ", $extra);
        }

        $arg["button"]["BTN_MASTER"] = $clubMst;

        //保存ボタンを作成する
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep", "更 新", $extra);

        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        $arg["info"]    = array("TOP"        => "対象年度　",
                                "LEFT_LIST"  => "部クラブ年度一覧",
                                "RIGHT_LIST" => "部クラブ一覧");

        $arg["TITLE"]   = "マスタメンテナンス - 部クラブマスタ";
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "sel.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
