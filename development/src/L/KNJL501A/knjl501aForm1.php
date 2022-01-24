<?php

require_once('for_php7.php');

class knjl501aForm1
{
    function main(&$model)
    {
        $flg = "";

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjl501aindex.php", "", "sel");
        $db             = Query::dbCheckOut();

        //年度設定
        $result    = $db->query(knjl501aQuery::selectYearQuery());
        $opt       = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["YEAR"],
                           "value" => $row["YEAR"]);
            if ($model->year == $row["YEAR"]) $flg = true;
        }
        if (!$flg) $model->year = $opt[0]["value"];

        //年度コース一覧取得
        $result      = $db->query(knjl501aQuery::selectCourseQuery($model));
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_left[]    = array("label" => $row["HOPE_COURSECODE"]."  ".$row["HOPE_NAME"],
                                   "value" => $row["HOPE_COURSECODE"]);
            $opt_left_id[] = $row["HOPE_COURSECODE"];
        }
        $opt_right = array();

        //コース一覧取得
        $result = $db->query(knjl501aQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
             if (!in_array($row["HOPE_COURSECODE"], $opt_left_id)) {
                 $opt_right[] = array("label" => $row["HOPE_COURSECODE"]."  ".$row["HOPE_NAME"],
                                  "value" => $row["HOPE_COURSECODE"]);
             }
        }

        $result->free();
        Query::dbCheckIn($db);

        //年度コンボボックスを作成する
        $extra1 = "onchange=\"return btn_submit('');\"";
        $extra2 = "onblur=\"this.value=toInteger(this.value);\"";
        $extra3 = "onclick=\"return add('');\"" ;

        $arg["year"]["VAL"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra1, 1)."&nbsp;&nbsp;".
                              knjCreateTextBox($objForm, "" , "year_add", 5, 4, $extra2)."&nbsp;".knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra3);

        //年度
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','courseyear','coursemaster',1)\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "courseyear", "left", $opt_left, $extra, 20);

        //コースマスタ
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','courseyear','coursemaster',1)\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "coursemaster", "left", $opt_right, $extra, 20);

        //追加ボタンを作成する
        $extra = "onclick=\"return move('sel_add_all','courseyear','coursemaster',1);\"" ;
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        //追加ボタンを作成する
        $extra = "onclick=\"return move('left','courseyear','coursemaster',1);\"" ;
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return move('right','courseyear','coursemaster',1);\"" ;
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        //削除ボタンを作成する
        $extra = "onclick=\"return move('sel_del_all','courseyear','coursemaster',1);\"" ;
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //コースマスタボタンを作成する
        $link = REQUESTROOT."/L/KNJL501A_2/knjl501a_2index.php?mode=1&SEND_PRGID=KNJL501A&SEND_AUTH={$model->auth}";

        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["BTN_MASTER"] = knjCreateBtn($objForm, "btn_master", " コースマスタ ", $extra);

        //保存ボタンを作成する
        $extra = "onclick=\"return doSubmit();\"" ;
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep", "更新", $extra);

        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('clear');\"" ;
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"" ;
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);

        //hiddenを作成する
        $arg["data"]["cmd"] = knjCreateHidden($objForm, "cmd");
        $arg["data"]["selectdata"] = knjCreateHidden($objForm, "selectdata");


        $arg["info"]    = array("TOP"        => "対象年度",
                                "LEFT_LIST"  => "コース年度一覧",
                                "RIGHT_LIST" => "コース一覧");

        $arg["TITLE"]   = "マスタメンテナンス - コースマスタ";
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, TMPLDIRECTORY."/sel.html", $arg);
    }
}
?>
