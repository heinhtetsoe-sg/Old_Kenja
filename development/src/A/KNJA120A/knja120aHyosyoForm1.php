<?php

require_once('for_php7.php');

class knja120aHyosyoForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("HyosyoForm1", "POST", "knja120aindex.php", "", "HyosyoForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //学籍賞罰データより賞データを取得
        if ($model->schregno) {
            $result = $db->query(knja120aQuery::getHyosyo($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["DETAIL_SDATE"] = str_replace("-", "/", $row["DETAIL_SDATE"]);
                $arg["data"][] = $row;
            }
        }

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja120aHyosyoForm1.html", $arg);
    }
}
