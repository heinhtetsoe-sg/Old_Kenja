<?php

require_once('for_php7.php');

class knjz070Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("sel", "POST", "knjz070index.php", "", "sel");
        $db = Query::dbCheckOut();
        
        //年度設定
        $query = knjz070Query::selectYearQuery($model);
        $result = $db->query($query);
        $opt = array();
        $j = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"],
                           "value" => $row["YEAR"]);
            if ($model->year == $row["YEAR"]) {
                $j++;
            }
        }
        if ($j == 0) {
            $model->year = $opt[0]["value"];
        }
        
        //年度科目一覧取得
        $query = knjz070Query::selectQuery($model);
        $result = $db->query($query);
        $opt_left_id = $opt_left = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
            $opt_left_id[] = $row["VALUE"];
        }
        
        $opt_right = array();
        //科目一覧取得
        $query = knjz070Query::selectSubclassQuery($opt_left_id, $model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //年度コンボボックス
        $extra = "onchange=\"return btn_submit('');\"";
        $setYearCmb = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        //テキスト
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $setYearText = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);

        //ボタン
        $extra = "onclick=\"return add('');\"";
        $setYearBtn = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

        $arg["year"]["VAL"] = $setYearCmb."&nbsp;&nbsp;".$setYearText."&nbsp;".$setYearBtn;

        //年度科目
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "subclassyear", "left", $opt_left, $extra, 20);

        //科目マスタ
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "subclassmaster", "right", $opt_right, $extra, 20);

        //追加ボタン
        $extra = "onclick=\"moves('left');\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        //追加ボタン
        $extra = "onclick=\"move1('left');\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        //削除ボタン
        $extra = "onclick=\"move1('right');\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        //削除ボタン
        $extra = "onclick=\"moves('right');\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //科目マスタボタン
        $link = REQUESTROOT."/Z/KNJZ070_2/knjz070_2index.php?year_code={$model->year}&SEND_selectSchoolKind={$model->selectSchoolKind}";
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["BTN_MASTER"] = knjCreateBtn($objForm, "btn_master", " 科目マスタ ", $extra);

        //教科科目名称マスタのマスタ画面のボタン表示設定
        //詳細登録ボタン
        if ($model->Properties["useCurriculumcd"] == 1 &&
            $model->Properties["useClassDetailDat"] == 1 &&
            $model->Properties["hyoujiClassDetailDat"] == 1) {
            $arg["hyoujiClassDetailDat"] = '1';
        }
        $link = REQUESTROOT."/Z/KNJZ070_3/knjz070_3index.php?mode=1&SEND_YEAR=$model->year&SEND_PRGID=KNJZ070&SEND_AUTH=".AUTHORITY."&SEND_selectSchoolKind={$model->selectSchoolKind}";
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["BTN_SHOUSAI"] = knjCreateBtn($objForm, "btn_shousai", " 詳細登録 ", $extra);

        //保存ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_keep", "更新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_clear", "取消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "firstData");
        knjCreateHidden($objForm, "rightMoveData");
        knjCreateHidden($objForm, "temp_year");
        $arg["info"] = array("TOP"        => "対象年度",
                             "LEFT_LIST"  => "年度科目一覧",
                             "RIGHT_LIST" => "科目一覧");
        $arg["finish"]  = $objForm->get_finish();

        $arg["jscript"] = " setFirstData(); ";

        $arg["TITLE"] = "マスタメンテナンスー科目マスタ";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "sel.html", $arg);
    }
}
