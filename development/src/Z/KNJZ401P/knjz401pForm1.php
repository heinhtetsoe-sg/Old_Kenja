<?php

require_once('for_php7.php');

class knjz401pForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz401pindex.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjz401pQuery::getYearList());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);

            if ($model->year == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
        $extra = "onchange=\"return btn_submit('sel');\"";
        $year = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        //追加年度テキストボックス
        $year_add = knjCreateTextBox($objForm, "", "year_add", 5, 4, "onblur=\"this.value=toInteger(this.value);\"");

        //年度追加ボタン
        $extra = "onclick=\"return add('');\"";
        $btn_year_add = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

        $arg["year"] = array("VAL" => $year."&nbsp;&nbsp;".$year_add."&nbsp;".$btn_year_add);

        //年度観点一覧取得
        $opt_left_id = $opt_left = array();
        $result = $db->query(knjz401pQuery::selectQuery($model));
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_left[] = array('label' => $row["GRADE"].'-'.$row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"].'-'.$row["VIEWCD"].'　'.$row["VIEWNAME"],
                                    'value' => $row["VALUE"]);
                $opt_left_id[] = $row["GRADE"]."-".$row["VIEWCD"];
            }
        } else {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_left[] = array('label' => $row["GRADE"].'-'.$row["SUBCLASSCD"].'-'.$row["VIEWCD"].'　'.$row["VIEWNAME"],
                                    'value' => $row["VALUE"]);
                $opt_left_id[] = $row["GRADE"]."-".$row["VIEWCD"];
            }
        }
        $result->free();

        //年度観点
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','yearList','masterList',1)\"";
        $yearList = knjCreateCombo($objForm, "yearList", "left", $opt_left, $extra, 20);

        //観点一覧取得
        $opt_right = array();
        $result = $db->query(knjz401pQuery::selectViewNameQuery($opt_left_id, $model));
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_right[] = array('label' => $row["GRADE"].'-'.$row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"].'-'.$row["VIEWCD"].'　'.$row["VIEWNAME"],
                                     'value' => $row["VALUE"]);
            }
        } else {
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_right[] = array('label' => $row["GRADE"].'-'.$row["SUBCLASSCD"].'-'.$row["VIEWCD"].'　'.$row["VIEWNAME"],
                                     'value' => $row["VALUE"]);
            }
        }
        $result->free();

        //観点マスタ
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','yearList','masterList',1)\"";
        $masterList = knjCreateCombo($objForm, "masterList", "right", $opt_right, $extra, 20);

        //追加ボタン（全部）
        $extra = "onclick=\"return move('sel_add_all','yearList','masterList',1);\"";
        $sel_add_all = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        //追加ボタン（一部）
        $extra = "onclick=\"return move('left','yearList','masterList',1);\"";
        $sel_add = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        //削除ボタン（一部）
        $extra = "onclick=\"return move('right','yearList','masterList',1);\"";
        $sel_del = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        //削除ボタン（全部）
        $extra = "onclick=\"return move('sel_del_all','yearList','masterList',1);\"";
        $sel_del_all = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        $arg["main_part"] = array( "LEFT_PART"   => $yearList,
                                   "RIGHT_PART"  => $masterList,
                                   "SEL_ADD_ALL" => $sel_add_all,
                                   "SEL_ADD"     => $sel_add,
                                   "SEL_DEL"     => $sel_del,
                                   "SEL_DEL_ALL" => $sel_del_all);

        //観点マスタボタン
        $link = REQUESTROOT."/Z/KNJZ401P_2/knjz401p_2index.php?year_code=".$model->year;
        $extra = "onclick=\"document.location.href='$link'\"";
        $btn_master = knjCreateBtn($objForm, "btn_master", "観点マスタ", $extra);

        //更新ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $btn_keep = knjCreateBtn($objForm, "btn_keep", "更新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $btn_clear = knjCreateBtn($objForm, "btn_clear", "取消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $btn_end = knjCreateBtn($objForm, "btn_end", "終了", $extra);

        $arg["button"] = array("BTN_MASTER" => $btn_master,
                               "BTN_OK"     => $btn_keep,
                               "BTN_CLEAR"  => $btn_clear,
                               "BTN_END"    => $btn_end );  

        $arg["info"] = array("TOP"          => "対象年度",
                             "LEFT_LIST"    => "年度観点一覧",
                             "RIGHT_LIST"   => "観点一覧");

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "temp_year");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        $arg["TITLE"] = "マスタメンテナンスー観点マスタ";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, TMPLDIRECTORY."/sel.html", $arg); 
    }
}
?>
