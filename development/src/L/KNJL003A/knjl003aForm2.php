<?php

require_once('for_php7.php');

class knjl003aForm2
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl003aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2" || $model->cmd == "reset") && $model->year && $model->applicantdiv && $model->honordiv) {
            $query = knjl003aQuery::getRow($model->year, $model->applicantdiv, $model->honordiv);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //初期値セット
        $model->year = ($model->year == "") ? CTRL_YEAR + 1: $model->year;
        if ($model->applicantdiv == "") {
            $extra = "onchange=\"return btn_submit('list');\"";
            $query = knjl003aQuery::getNameMst($model, "L003");
            makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1, "");
        }
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl003aQuery::getNameMst($model, "L003", $model->applicantdiv));

        /*****************/
        /** textbox作成 **/
        /*****************/
        //特待CD
        $extra = "STYLE=\"ime-mode: inactive; text-align: right;\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["HONORDIV"] = knjCreateTextBox($objForm, $Row["HONORDIV"], "HONORDIV", 2, 2, $extra);

        //特待名称
        $extra = "";
        $arg["data"]["HONORDIV_NAME"] = knjCreateTextBox($objForm, $Row["HONORDIV_NAME"], "HONORDIV_NAME", 41, 60, $extra);

        //特待略称
        $extra = "";
        $arg["data"]["HONORDIV_ABBV"] = knjCreateTextBox($objForm, $Row["HONORDIV_ABBV"], "HONORDIV_ABBV", 21, 30, $extra);

        //優先順位
        $extra = "STYLE=\"ime-mode: inactive; text-align: right;\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PRIORITY"] = knjCreateTextBox($objForm, $Row["PRIORITY"], "PRIORITY", 2, 2, $extra);

        //入学金
        $size = ($model->applicantdiv == '1') ? 6 : 20;
        $extra = ($model->applicantdiv == '1') ? "STYLE=\"ime-mode: inactive; text-align: right;\" onblur=\"this.value=toInteger(this.value)\"" : "";
        $unit = ($model->applicantdiv == '1') ? ' 円' : '';
        $arg["data"]["ENROLL_FEES"] = knjCreateTextBox($objForm, $Row["ENROLL_FEES"], "ENROLL_FEES", $size, $size, $extra).$unit;
        if ($model->applicantdiv == '2') {
            $arg["data"]["ENROLL_FEES_COMMENT"] = '　(全角で10文字)';
        }
        $arg["data"]["ENROLL_FEES_EXAMPLE"] = ($model->applicantdiv == '2')  ? '例：21万円免除' : '例：0円';

        if ($model->applicantdiv == '2') {
            $size = 6;
            $extra = "STYLE=\"ime-mode: inactive; text-align: right;\" onblur=\"this.value=toInteger(this.value)\"";
            $unit = ' 円';
            $arg["data"]["ENROLL_FEES2"] = knjCreateTextBox($objForm, $Row["ENROLL_FEES2"], "ENROLL_FEES2", $size, $size, $extra).$unit;
            $arg["data"]["ENROLL_FEES2_EXAMPLE"] = '例：0円';
        }

        if ($model->applicantdiv == '2') {
            $arg["showENROLL_FEES2"] = 1;
        }

        //表示切替
        if ($model->applicantdiv == '1') {
            $arg["showSCHOOL_FEES"] = 1;
        } else {
            $arg["showSCHOLARSHIP"] = 1;
        }

        //授業料
        $extra = "STYLE=\"ime-mode: inactive; text-align: right;\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SCHOOL_FEES"] = knjCreateTextBox($objForm, $Row["SCHOOL_FEES"], "SCHOOL_FEES", 6, 6, $extra);

        //奨学金（月額）
        $extra = "";
        $arg["data"]["SCHOLARSHIP1"] = knjCreateTextBox($objForm, $Row["SCHOLARSHIP1"], "SCHOLARSHIP1", 10, 10, $extra);

        //奨学金（年間）
        $extra = "";
        $arg["data"]["SCHOLARSHIP2"] = knjCreateTextBox($objForm, $Row["SCHOLARSHIP2"], "SCHOLARSHIP2", 10, 10, $extra);

        /******************/
        /** checkbox作成 **/
        /******************/
        //クラブ
        $extra  = "id=\"CLUB_FLG\"";
        $extra .= ($Row["CLUB_FLG"] == "1") ? " checked": "";
        $arg["data"]["CLUB_FLG"] = knjCreateCheckBox($objForm, "CLUB_FLG", "1", $extra);

        /******************/
        /** combobox作成 **/
        /******************/
        //通知書用区分1
        $extra = "";
        $query = knjl003aQuery::getNoticeClass();
        makeCmb($objForm, $arg, $db, $query, "NOTICE_CLASS", $Row["NOTICE_CLASS"], $extra, 1, "BLANK");
        //通知書用区分2
        $extra = "";
        $query = knjl003aQuery::getNoticeKind();
        makeCmb($objForm, $arg, $db, $query, "NOTICE_KIND", $Row["NOTICE_KIND"], $extra, 1, "BLANK");
        //特待区分
        $extra = "";
        makeCmb2($objForm, $arg, $model->honorTypeList, "HONOR_TYPE", $Row["HONOR_TYPE"], $extra, 1, "BLANK");

        /**************/
        /**ボタン作成**/
        /**************/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '終 了', $extra);

        /**************/
        /**hidden作成**/
        /**************/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl003aindex.php?cmd=list2"
                            . "&year=".$model->year."&applicantdiv=".$model->applicantdiv."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl003aForm2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//makeCmb
function makeCmb2(&$objForm, &$arg, $orgOpt, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    $default = 0;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    foreach ($orgOpt as $row) {
        $opt[] = array('label' => $row["VALUE"] . ":" . $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[$default]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
