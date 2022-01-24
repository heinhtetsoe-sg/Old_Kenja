<?php

require_once('for_php7.php');

class knjz060Form1
{
    public function main(&$model)
    {
        $arg["jscript"] = "";

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz060index.php", "", "sel");
        $db             = Query::dbCheckOut();
        $no_year        = 0;

        //年度設定
        $query     = knjz060Query::selectYearQuery($model);
        $result    = $db->query($query);
        $opt       = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["YEAR"],
                           "value" => $row["YEAR"]);
            if ($row["YEAR"] == $model->year) {
                $no_year = 1;
            }
        }
        if ($no_year == 0) {
            $model->year = $opt[0]["value"];
        }

        //年度コンボボックス
        $extra = "onchange=\"return btn_submit('');\"";
        $arg["year"]["VAL"] = knjCreateCombo($objForm, "year", $model->year, $opt, $extra, 1);

        $extra1 = "onblur=\"this.value=toInteger(this.value);\"";
        $extra2 = "onclick=\"return add('');\"";
        $arg["year"]["BUTTON"] = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra1)."&nbsp;".knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra2);

        //年度教科一覧取得
        $query       = knjz060Query::selectQuery($model);
        $result      = $db->query($query);
        $opt_left_id = $opt_left = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array('label' => $row["LABEL"],
                                'value' => $row["VALUE"]);
            $opt_left_id[] = $row["VALUE"];
        }
        $opt_right = array();

        //教科一覧取得
        $query  = knjz060Query::selectClassQuery($model, $opt_left_id);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
        }
        $result->free();
        //教科年度
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "classyear", "left", $opt_left, $extra, 20);

        //教科マスタ
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "classmaster", "left", $opt_right, $extra, 20);

        //追加ボタン
        $extra = " onclick=\"moves('left');\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        //追加ボタン
        $extra = " onclick=\"move1('left');\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        //削除ボタン
        $extra = " onclick=\"move1('right');\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        //削除ボタン
        $extra = " onclick=\"moves('right');\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //教科マスタボタン
        $link = REQUESTROOT."/Z/KNJZ060_2/knjz060_2index.php?mode=1&SEND_selectSchoolKind={$model->selectSchoolKind}";
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["BTN_MASTER"] = knjCreateBtn($objForm, "btn_master", " 教科マスタ ", $extra);

        //教科科目名称マスタのマスタ化
        //詳細登録ボタン
        if ($model->Properties["useCurriculumcd"] == 1 &&
            $model->Properties["useClassDetailDat"] == 1 &&
            $model->Properties["hyoujiClassDetailDat"] == 1) {
            $arg["useClassDetailDat"] = '1';
        }
        $link = REQUESTROOT."/Z/KNJZ060_3/knjz060_3index.php?mode=1&SEND_YEAR=$model->year&SEND_selectSchoolKind={$model->selectSchoolKind}";
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

        $arg["info"]    = array("TOP"        => "対象年度",
                                "LEFT_LIST"  => "教科年度一覧",
                                "RIGHT_LIST" => "教科一覧");

        $arg["TITLE"]   = "マスタメンテナンス - 教科マスタ";

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        $arg["jscript"] = " setFirstData(); ";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "sel.html", $arg);
    }
}
