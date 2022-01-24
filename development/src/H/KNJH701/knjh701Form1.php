<?php

require_once('for_php7.php');

class knjh701form1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjh701index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //前年度からコピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //テスト区分・実施日付・テスト名称表示
        $query = knjh701Query::getTestKubunData();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["EXAM_DATE"] = str_replace("-", "/", $row["EXAM_DATE"]);
            $arg["data"][] = $row;
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh701Form1.html", $arg);
    }
}
