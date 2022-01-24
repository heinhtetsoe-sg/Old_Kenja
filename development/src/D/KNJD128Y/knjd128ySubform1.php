<?php

require_once('for_php7.php');

class knjd128ySubform1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjd128yindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["ATTENDNO"] = $model->attendno;
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME_SHOW"] = $model->name_show;

        //顔写真
        $arg["FACE_IMG"] = REQUESTROOT."/".$model->control["LargePhotoPath"]."/P".$model->schregno.".".$model->control["Extension"];
        $arg["IMG_PATH"] = REQUESTROOT."/".$model->control["LargePhotoPath"]."/P".$model->schregno.".".$model->control["Extension"];

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd128ySubform1.html", $arg);
    }
}
