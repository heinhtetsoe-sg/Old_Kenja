<?php
/********************************************************************/
/* 変更履歴 成績段階別人数一覧表                    山城 2005/07/11 */
/*                                                                  */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knje140Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knje140Form1", "POST", "knje140index.php", "", "knje140Form1");

        $arg["data"]["YEAR"] = CTRL_YEAR;
        //エラーコード取得
        $opt_grade = array();
        $db = Query::dbCheckOut();
        $query = knje140Query::getgrade($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_grade[] = array('label' => sprintf('%d',$row["GRADE"])."年",
                                 'value' => $row["GRADE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        if (!$model->field["GRADE"]) $model->field["GRADE"] = $opt_grade[0];

        $objForm->ae( array("type"        => "select",
                            "name"        => "GRADE",
                            "size"        => 1,
                            "value"       => $model->field["GRADE"],
                            "options"     => $opt_grade) );

        $arg["data"]["GRADE"] = $objForm->ge("GRADE");
        //印刷ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する(必須)
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR,
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"     => CTRL_SEMESTER,
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJE140"
                            ) );

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje140Form1.html", $arg); 
    }
}
?>
