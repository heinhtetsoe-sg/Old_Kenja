<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd139mSanSyoButton {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        $arg = array();

        //フォーム作成
        $arg["start"] = $objForm->get_start("SanSyoButton", "POST", "knjd139mindex.php", "", "SanSyoButton");

        //DB接続
        $db = Query::dbCheckOut();

        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //年度・学期表示
        $arg["YEAR_SEMESTER"] = $model->exp_year."年度　".$model->control["学期名"][$model->exp_semester];

        //総合学習、出欠備考取得
        $query = knjd139mQuery::getHreportRemrkDatSansyo($model);
        $result = $db->query($query);
        $semsterCnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //checkbox
            $name = "CHECKED".$row["SEMESTER"];
            $extra = "id=\"{$name}\" onclick=\"OptionUse(this);\"";
            $row["CHECKED"] = knjCreateCheckBox($objForm, $name, "1", $extra);
            //hidden
            knjCreateHidden($objForm, "VALUE".$row["SEMESTER"], $row["VALUE"]);

            $arg["data2"][] = $row;
            $semsterCnt++;
        }
        knjCreateHidden($objForm, "SEMSETERCNT", $semsterCnt);

        $fieldName = ($model->cmd == 'totalStudy') ? 'TOTALSTUDYTIME': 'ATTENDREC_REMARK';

        //取込ボタン
        $extra = "disabled style=\"color:#1E90FF;font:bold\" onclick=\"return dataPositionSet('{$fieldName}');\"";
        $arg["button"]["btn_torikomi"] = knjCreateBtn($objForm, "btn_torikomi", "取 込", $extra);

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd139mSanSyoButton.html", $arg);
    }
}
?>
