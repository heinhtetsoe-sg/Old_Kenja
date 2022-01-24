<?php

require_once('for_php7.php');

class knja240uForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knja240uForm1", "POST", "knja240uindex.php", "", "knja240uForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //処理日
        $model->field["DATE"] = ($model->field["DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $model->field["DATE"]);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA240U");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SEMESTER");
        knjCreateHidden($objForm, "useSchregRegdHdat", $model->Properties["useSchregRegdHdat"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);

        //学期データ取得
        $seme_data = $sep = "";
        $query = knja240uQuery::getSemesterData();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $seme_data .= $sep.$row["SEMESTER"].":".$row["SDATE"].":".$row["EDATE"];
            $sep = ",";
        }
        $result->free();
        knjCreateHidden($objForm, "SEME_DATE", $seme_data);

        //学年数取得
        $grade_cnt = $db->getOne(knja240uQuery::getGradeCnt($model));
        knjCreateHidden($objForm, "GRADE_HVAL", $grade_cnt);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja240uForm1.html", $arg); 
    }
}
?>
