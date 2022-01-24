<?php

require_once('for_php7.php');

/********************************************************************/
/* クラス編成事前一覧                               山城 2006/03/17 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：成績、フリガナの出力可否追加             山城 2006/03/20 */
/********************************************************************/

class knja081Form1 {
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja081Form1", "POST", "knja081index.php", "", "knja081Form1");

        $opt=array();

        //年度
        $arg["data"]["YEAR"] = $model->nextyear;

        /*------------*/
        /* 学年コンボ */
        /*------------*/
        $db = Query::dbCheckOut();
        $opt = array();
        $result = $db->query(knja081Query::GetGrade($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["NAME"],
                           'value' => $row["CD"]);
        }
        $result->free();
        $opt[] = array('label' => "全学年",
                       'value' => "99");
        $extra = "";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->grade, $opt, $extra, 1);

        Query::dbCheckIn($db);

        //成績出力チェックボックスを作成する NO001
        $extra = "checked id=\"OUTPUT1\"";
        $arg["data"]["OUTPUT1"] = knjCreateCheckBox($objForm, "OUTPUT1", "on", $extra);

        //ふりがな出力チェックボックスを作成する NO001
        $extra = "checked id=\"OUTPUT2\"";
        $arg["data"]["OUTPUT2"] = knjCreateCheckBox($objForm, "OUTPUT2", "on", $extra);

        //中高判定フラグを作成する
        $db = Query::dbCheckOut();
        $row = $db->getOne(knja081Query::GetJorH());
        if ($row == 1) {
            $jhflg = 1;
        } else {
            $jhflg = 2;
        }
        Query::dbCheckIn($db);
        knjCreateHidden($objForm, "JHFLG", $jhflg);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "YEAR", $model->nextyear);
        knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA081");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "useKNJA081_2.frm", $model->Properties["useKNJA081_2.frm"]);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja081Form1.html", $arg); 
    }
}
?>
