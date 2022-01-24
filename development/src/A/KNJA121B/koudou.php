<?php

require_once('for_php7.php');

class koudou
{
    function main(&$model)
    {
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("detail", "POST", "knja121bindex.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //記録の取得
        $Row = array();
        $result = $db->query(knja121bQuery::getBehavior($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $scd = $row["DIV"] .$row["CODE"];

            $Row["RECORD"][$scd] = $row["RECORD"];  //記録
        }
        $result->free();

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if(isset($model->warning)){
            $Row =& $model->record;
        }

        //行動の記録
        for($i=1; $i<11; $i++)
        {
            $ival = "1" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $arg["RECORD".$ival] = knjCreateCheckBox($objForm, "RECORD".$ival, "1", $check1, "");
        }

        //特別活動の記録
        for($i=1; $i<4; $i++)
        {
            $ival = "2" . sprintf("%02d", $i);
            $check2 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $arg["RECORD".$ival] = knjCreateCheckBox($objForm, "RECORD".$ival, "1", $check2, "");
        }

        //署名チェック
        $query = knja121bQuery::getOpinionsWk($model);
        $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $opinion = ($check["CHAGE_OPI_SEQ"] || $check["LAST_OPI_SEQ"]) ? false : true;

        //更新ボタン
        if((AUTHORITY < DEF_UPDATE_RESTRICT) || !$opinion){
            $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "disabled");
        } else {
            $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('koudou_update');\"");
        }
        //取消ボタン
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('clear');\"");
        //終了ボタン
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る",  "onclick=\"return parent.closeit()\"");

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "koudou.html", $arg);
    }
}
?>