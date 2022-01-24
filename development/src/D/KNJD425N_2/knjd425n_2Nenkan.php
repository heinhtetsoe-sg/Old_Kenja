<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425n_2Nenkan
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform", "POST", "knjd425n_2index.php", "", "subform");

        //学籍番号・生徒氏名表示
        $arg["data"]["YEAR"] = $model->exp_year - 1;
        $arg["data"]["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        $db = Query::dbCheckOut();

        //テーブルタイトル
        $arg["data"]["TABLE_TITLE"] = $db->getOne(knjd425n_2Query::getHreportGuidanceKindNameDat($model));

        //入力項目を設定
        $arg["REMARKTITLE"] = $model->subclassname;

        $row = $db->getRow(knjd425n_2Query::getNenkanDataList($model), DB_FETCHMODE_ASSOC);
        $extra = "id=\"REMARK_".$model->subclasscd."\" readonly ";
        $arg["REMARK"] = knjCreateTextArea($objForm, "REMARK_0_".$model->subclasscd, 10, 90, "", $extra, $row["REMARK"]);
        $arg["EXTFMT"] .= "<BR><font size=2, color=\"red\">(全角45文字X25行まで)</font>";
        knjCreateHidden($objForm, "REMARK_".$model->subclasscd."_KETA", 90);
        knjCreateHidden($objForm, "REMARK_".$model->subclasscd."_GYO", 25);
        KnjCreateHidden($objForm, "REMARK_".$model->subclasscd."_STAT", "statusarea_".$model->subclasscd);

        Query::dbCheckIn($db);

        //取込ボタンを作成
        $extra = "onclick=\"return btn_submit('".$model->subclasscd."');\"";
        $arg["btn_input"] = KnjCreateBtn($objForm, "btn_input", "取 込", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["IFRAME"] = VIEW::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd425n_2Nenkan.html", $arg);
    }
}
?>
