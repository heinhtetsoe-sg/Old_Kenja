<?php

require_once('for_php7.php');

class knjm240dForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjm240dindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["CHAIRCD"]      = $model->chairCd;
        $arg["NAME"]         = $model->chairName;

        //更新するフィールド
        $model->setFieldName = array("REPO_MAX_CNT",
                                     "REPO_LIMIT_CNT",
                                     "SCHOOLING_MAX_CNT",
                                     "SCHOOLING_LIMIT_CNT");

        //初期化
        $model->data = array();

        $query = knjm240dQuery::getSemesterCnt($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            //学期を配列で取得
            $model->data["SEMESTER"][] = $row["SEMESTER"];

            /**レポート**/
            //回数
            $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value);check(this, {$row["SEMESTER"]})\" onkeydown=\"changeEnterToTab(this)\"";
            $value = (!isset($model->warning)) ? $row["REPO_MAX_CNT"]: $model->fields["REPO_MAX_CNT"][$row["SEMESTER"]];
            $name  = "REPO_MAX_CNT-".$row["SEMESTER"];
            $row["REPO_MAX_CNT"] = knjCreateTextBox($objForm, $value, $name, 2, 2, $extra);
            //最低提出回数
            $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value);check(this, {$row["SEMESTER"]})\" onkeydown=\"changeEnterToTab(this)\"";
            $value = (!isset($model->warning)) ? $row["REPO_LIMIT_CNT"]: $model->fields["REPO_LIMIT_CNT"][$row["SEMESTER"]];
            $name  = "REPO_LIMIT_CNT-".$row["SEMESTER"];
            $row["REPO_LIMIT_CNT"] = knjCreateTextBox($objForm, $value, $name, 2, 2, $extra);

            /**スクーリング**/
            //回数
            $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value);check(this, {$row["SEMESTER"]})\" onkeydown=\"changeEnterToTab(this)\"";
            $value = (!isset($model->warning)) ? $row["SCHOOLING_MAX_CNT"]: $model->fields["SCHOOLING_MAX_CNT"][$row["SEMESTER"]];
            $name  = "SCHOOLING_MAX_CNT-".$row["SEMESTER"];
            $row["SCHOOLING_MAX_CNT"] = knjCreateTextBox($objForm, $value, $name, 2, 2, $extra);
            //最低出席回数
            $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value);check(this, {$row["SEMESTER"]})\" onkeydown=\"changeEnterToTab(this)\"";
            $value = (!isset($model->warning)) ? $row["SCHOOLING_LIMIT_CNT"]: $model->fields["SCHOOLING_LIMIT_CNT"][$row["SEMESTER"]];
            $name  = "SCHOOLING_LIMIT_CNT-".$row["SEMESTER"];
            $row["SCHOOLING_LIMIT_CNT"] = knjCreateTextBox($objForm, $value, $name, 2, 2, $extra);

            $arg["data"][] = $row;
        }

        /************/
        /** ボタン **/
        /************/
        //登録ボタン
        $extra = " onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "登 録", $extra);
        //取消ボタン
        $extra = " onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && !isset($model->warning)) {
            $arg["reload"] = "parent.left_frame.location.reload();";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm240dForm2.html", $arg);
    }
}
?>
