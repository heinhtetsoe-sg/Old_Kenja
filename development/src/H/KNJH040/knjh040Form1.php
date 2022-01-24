<?php

require_once('for_php7.php');

class knjh040Form1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成////////////////////////////////////////////////////////////////////////
        $arg["start"]   = $objForm->get_start("knjh040Form1", "POST", "knjh040index.php", "", "knjh040Form1");

        //年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["YEAR"] = $model->control["年度"];
        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);

        //学期コードをhiddenで送る/////////////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["GAKKI"] = CTRL_SEMESTERNAME;
        knjCreateHidden($objForm, "GAKKI", $model->control["学期"]);

        //学年リストボックスを作成する//////////////////////////////////////////////////////////////////////////////////////
        $db = Query::dbCheckOut();
        $opt_grade=array();
        $query = knjh040Query::getSelectGrade($model, $model->control["年度"]);
        $result = $db->query($query);
        $i=0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_grade[] = array('label' => $row["LABEL"],
                                 'value' => $row["VALUE"]);
            $i++;
        }
        if($model->field["GAKUNEN"]=="") $model->field["GAKUNEN"] = $opt_grade[0]["value"];
        $result->free();
        Query::dbCheckIn($db);

        $extra = "multiple";
        $arg["data"]["GAKUNEN"] = knjCreateCombo($objForm, "GAKUNEN", $model->field["GAKUNEN"], $opt_grade, $extra, $i);

        //印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJH040");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useSchregRegdHdat", $model->useSchregRegdHdat);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh040Form1.html", $arg); 
    }
}
?>
