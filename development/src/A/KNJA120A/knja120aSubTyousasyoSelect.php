<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knja120aSubTyousasyoSelect
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subtyousasyoselect", "POST", "knja120aindex.php", "", "subtyousasyoselect");

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //DB接続
        $db = Query::dbCheckOut();

        $row = knja120aQuery::getHexamEntremark($model);

        //総合的な学習の記録
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] !== '1') {
            //活動内容
            makeCheckBox($objForm, $arg, $row["TOTALSTUDYACT"], "TOTALSTUDYACT", "総合的な学習の記録(活動内容)");
            //評価
            makeCheckBox($objForm, $arg, $row["TOTALSTUDYVAL"], "TOTALSTUDYVAL", "総合的な学習の記録(評価)");
        } else {
            //活動内容
            makeCheckBox($objForm, $arg, $row["TOTALSTUDYACT_YEAR"], "TOTALSTUDYACT", "総合的な学習の記録(活動内容)");
            //評価
            makeCheckBox($objForm, $arg, $row["TOTALSTUDYVAL_YEAR"], "TOTALSTUDYVAL", "総合的な学習の記録(評価)");
        }
        //特別活動の記録
        makeCheckBox($objForm, $arg, $row["SPECIALACTREC"], "SPECIALACTREMARK", "特別活動の記録");
        //総合所見
        makeCheckBox($objForm, $arg, $row["TRAIN_REF"], "TOTALREMARK", "指導上参考となる諸事項");
        //出欠の記録備考
        makeCheckBox($objForm, $arg, $row["ATTENDREC_REMARK"], "ATTENDREC_REMARK", "出欠の記録備考");

        //ボタン作成
        makeBtn($objForm, $arg);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knja120aSubTyousasyoSelect.html", $arg);
    }
}

//リスト作成
function makeCheckBox(&$objForm, &$arg, $value, $name, $item_name)
{
    //選択チェックボックス
    $check = "";
    $objForm->ae(
        array("type"       => "checkbox",
        "name"       => "CHECK_".$name,
        "value"      => $value,
        "extrahtml"  => $check,
        "multiple"   => "1" )
    );
    $row["CHECK"] = $objForm->ge("CHECK_".$name);

    $row["ITEM_NAME"] = $item_name;
    $row["TEXT"] = $value;

    $arg["data"][] = $row;
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //選択ボタン
    $extra = "onclick=\"return selectTyousasyo()\"";
    $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
