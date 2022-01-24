<?php

require_once('for_php7.php');
class knjl605aForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl605aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "edit2") 
        && $model->examYear && $model->applicantDiv && $model->testDiv && $model->scholarCd) {
            $query = knjl605aQuery::getRow($model->examYear, $model->applicantDiv, $model->testDiv, $model->scholarCd);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $row =& $model->field;
        }

        // 入試年度
        if (!$model->examYear) {
            $model->examYear = CTRL_YEAR + 1;
        }

        /**************/
        /**コンボ作成**/
        /**************/
        //入試制度コンボ
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjl605aQuery::getNameMst($model->examYear, "L003");
        makeCombo($objForm, $arg, $db, $query, $row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "blank");

        //入試区分コンボ
        $namecd1 = ($row["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjl605aQuery::getNameMst($model->examYear, $namecd1);
        makeCombo($objForm, $arg, $db, $query, $row["TESTDIV"], "TESTDIV", $extra, 1, "blank");

        /****************/
        /**テキスト作成**/
        /****************/
        //特奨学生コードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SP_SCHOLAR_CD"] = knjCreateTextBox($objForm, $row["SP_SCHOLAR_CD"], "SP_SCHOLAR_CD", 4, 3, $extra);

        //特奨学生名称テキストボックス
        $extra = "";
        $arg["data"]["SP_SCHOLAR_NAME"] = knjCreateTextBox($objForm, $row["SP_SCHOLAR_NAME"], "SP_SCHOLAR_NAME", 41, 20, $extra);


        /**************/
        /**コンボ作成**/
        /**************/
        //対象コース(出力対象一覧)
        $extra = "multiple style=\"width:230px\" ondblclick=\"move('right');\"";
        $query = knjl605aQuery::selectCourseQuery($row["ENTEXAMYEAR"], $row['APPLICANTDIV'], $row["TESTDIV"], $row["SP_SCHOLAR_CD"]);
        makeCombo($objForm, $arg, $db, $query, $row["EXAMCOURSECD"], "SELECT_EXAMCOURSECD", $extra, 20, "");

        //対象コース(コース一覧)
        $extra = "multiple style=\"width:230px\" ondblclick=\"move('left');\"";
        $query = knjl605aQuery::selectEntexamCourseQuery($row["ENTEXAMYEAR"], $row["APPLICANTDIV"], $row["TESTDIV"], $row["SP_SCHOLAR_CD"]);
        makeCombo($objForm, $arg, $db, $query, $row["EXAMCOURSECD"], "EXAMCOURSECD", $extra, 20, "");


        /**************/
        /**ボタン作成**/
        /**************/

        // << ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('left', 'ALL');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
        // ＜ ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
        // ＞ ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        // >> ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"move('right', 'ALL');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);


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
        knjCreateHidden($objForm, "cmd", "");
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examYear);
        knjCreateHidden($objForm, "SELECT_COURSE");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (!isset($model->warning) && $model->cmd != "edit") {
            $reload  = "parent.left_frame.location.href='knjl605aindex.php?cmd=list";
            $reload .= "&YEAR=" .$model->examYear."';";
            $arg["reload"] = $reload;
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl605aForm2.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = false) {
    $opt = array();
    $value_flg = false;
    if ($blank == "blank") $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name != "SELECT_EXAMCOURSECD" && $name != "EXAMCOURSECD" ) {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
