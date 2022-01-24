<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja128hSubForm2
{

    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform2", "POST", "knja128hindex.php", "", "subform2");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //部活一覧
        $query = knja128hQuery::getClub($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("-", "/", $row);
            //和暦表示
            if ($model->Properties["useWarekiHyoji"] == "1") {
                $row["SDATE"] = common::DateConv1($row["SDATE"], 0);
                $row["EDATE"] = common::DateConv1($row["EDATE"], 0);
            }
            $row["CLUBNAME"] = "　".$row["CLUBNAME"]." ／ ".$row["SDATE"]."～".$row["EDATE"]." ／ ".$row["NAME1"];
            $arg["data"][] = $row;
        }

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja128hSubForm2.html", $arg);
    }
}
