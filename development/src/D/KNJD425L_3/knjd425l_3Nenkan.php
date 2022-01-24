<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425l_3Nenkan
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform", "POST", "knjd425l_3index.php", "", "subform");

        $db = Query::dbCheckOut();

        $year = $model->exp_year - 1;
        //年度
        $arg["YEAR"] = $year;
        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //テーブルタイトル
        $query = knjd425l_3Query::getHreportGuidanceKindNameHdat($model, $year);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["TABLE_TITLE"] = $row["KIND_NAME"];

        //項目内容取得
        $query = knjd425l_3Query::getHreportGuidanceSchregSubclassRemark($model, $year, "05", $model->subclasscd);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $arg["SUBCLASSNAME"] = $model->subclassname;

        $moji = 45;
        $gyou = 25;
        $extra = "id=\"REMARK_".$model->subclasscd."\" aria-label=\"".$model->subclassname."\" readonly ";
        $arg["REMARK"] = knjCreateTextArea($objForm, "REMARK_".$model->subclasscd, $gyou, ($moji * 2), "", $extra, $row["REMARK"]);
        $arg["REMARK_SIZE"] = "<font size=2, color=\"red\">(全角".$moji."文字X".$gyou."行まで)</font>";

        Query::dbCheckIn($db);

        //取込ボタンを作成
        $extra = "onclick=\"return btn_submit('".$model->subclasscd."');\"";
        $arg["btn_input"] = KnjCreateBtn($objForm, "btn_input", "取 込", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["IFRAME"] = VIEW::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd425l_3Nenkan.html", $arg);
    }
}
?>
