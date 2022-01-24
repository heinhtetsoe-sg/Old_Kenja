<?php

require_once('for_php7.php');

/********************************************************************/
/* レポート添削職員登録                             M.I 2005/09/XX  */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm540Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm540Form1", "POST", "knjm540index.php", "", "knjm540Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR."年度";

        //button
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["COPY"] = knjCreateBtn($objForm, "COPY", "前年度からコピー", $extra);

        //データセット
        $opt_sub = array();
        $result = $db->query(knjm540Query::ReadQuery($model));
        while ($RowR = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_sub[] = array('label' => $RowR["CHAIRNAME"],
                               'value' => $RowR["CHAIRCD"].$RowR["SUBCLASSCD"]);
        }
        $result->free();
        if ($model->sub == "") $model->sub = $opt_sub[0]["value"];

        $extra = "onChange=\"return btn_submit('change');\"";
        $arg["data"]["SELSUB"] = knjCreateCombo($objForm, "SELSUB", $model->sub, $opt_sub, $extra, 1);

        //科目添削職員一覧取得
        $opt_left = array();
        $result      = $db->query(knjm540Query::selectQuery($model));   
        $opt_left_id = $opt_left = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[]    = array("label" => $row["STAFFCD"]."  ".$row["STAFFNAME"], 
                                   "value" => $row["STAFFCD"]);
            $opt_left_id[] = $row["STAFFCD"];
        }
        $result->free();

        //職員年度一覧取得
        $opt_right = array();
        $result = $db->query(knjm540Query::selectCorrectionQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array("label" => $row["STAFFCD"]."  ".$row["STAFFNAME"], 
                                 "value" => $row["STAFFCD"]);
        }
        $result->free();

        $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["STAFF_NAME"] = knjCreateCombo($objForm, "staff_name", "", isset($opt_left) ? $opt_left : array(), $extra, 20);

        //科目添削職員一覧リストを作成する
        $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["STAFF_SELECTED"] = knjCreateCombo($objForm, "staff_selected", "", isset($opt_right) ? $opt_right : array(), $extra, 20);

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //更新ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJM540");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm540Form1.html", $arg); 
    }
}
?>
