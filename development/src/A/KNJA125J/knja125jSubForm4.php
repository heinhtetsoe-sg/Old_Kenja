<?php

require_once("for_php7.php");

require_once("AttendAccumulate.php");

class knja125jSubForm4
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform3", "POST", "knja125jindex.php", "", "subform4");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        $result = $db->query(knja125jQuery::selectSyoken($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $extra = "";
            $row['SPECIALACTREC'] = knjCreateTextArea($objForm, $name, $model->specialactrec_gyou, ($model->specialactrec_moji * 2) + 1, "soft", $extra,  $row['SPECIALACTREC']);
            $row['CLUBACT'] = knjCreateTextArea($objForm, $name, $model->clubact_gyou, ($model->clubact_moji * 2) + 1, "soft", $extra,  $row['CLUBACT']);
            $arg['data'][] = $row;
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
        View::toHTML($model, "knja125jSubForm4.html", $arg);
    }
}
