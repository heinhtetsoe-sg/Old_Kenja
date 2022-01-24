<?php

require_once('for_php7.php');

class knjx090aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx090aindex.php", "", "main");

        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        $securityCnt = $db->getOne(knjx090aQuery::getSecurityHigh());
        //セキュリティーチェック
        if (!$model->getPrgId && $model->Properties["useXLS"] && $securityCnt > 0) {
            $arg["jscript"] = "OnSecurityError();";
        }
        
        //タイトル
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."";

        //学年一覧コンボボックス
        $result     = $db->query(knjx090aquery::getSelectFieldSQL($model));
        $opt_grade  = array();
        $opt_grade[] = array("label" => "(全て出力)", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_grade[] = array("label" => $row["LABEL"],
                                 "value" => $row["VALUE"]);
        }
        $result->free();
        $extra = "";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $opt_grade, $extra, 1);

        // 「HRクラスの自動名簿生成」ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_cre",
                            "value"     => "HRクラスの自動名簿生成",
                            "extrahtml" => "onclick=\"return btn_submit('csv2');\"" ));

        $arg["btn_cre"] = $objForm->ge("btn_cre");


        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJX090A");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx090aForm1.html", $arg);
    }
}
