<?php

require_once("for_php7.php");

require_once("AttendAccumulate.php");

class knja125jSubForm3
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3", "POST", "knja125jindex.php", "", "subform3");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //道徳の通知票参照
        for ($i = 0; $i < get_count($model->control["SEMESTER"]); $i++) {
            $semester = $model->control["SEMESTER"][$i];
            $remark1 = array();
            $remark1["SEM_NAME"] = $model->control["学期名"][$semester];

            $query = knja125jQuery::getHreportremarkDetailDatMoral($model, $semester, "08", "01");
            $result = $db->query($query);
            $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
            if ($row) {
                $remark1["REMARK1"] = $row["REMARK1"];
            }
            $arg["data"][] = $remark1;
            $result->free();
        }

        //戻るボタン
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja125jSubForm3.html", $arg);
    }
}
