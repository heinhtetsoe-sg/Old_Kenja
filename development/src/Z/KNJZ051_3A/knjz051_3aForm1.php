<?php

require_once('for_php7.php');

class knjz051_3aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz051_3aindex.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $opt = array();
        $value_flg = false;
        $query = knjz051_3aQuery::getYear($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);

            if ($model->field["YEAR"] == $row["VALUE"]) $value_flg = true;
        }
        $model->field["YEAR"] = ($model->field["YEAR"] && $value_flg) ? $model->field["YEAR"] : ($model->year ? $model->year : $model->send_year);
        $extra = "onchange=\"return btn_submit('chg_year');\"";
        $arg["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);

        //基本（学校マスタの学校区分）
        $school_div = $db->getOne(knjz051_3aQuery::getSchoolDiv($model));
        $arg["SCHOOLDIV"] = $school_div;

        //データ取得
        $query = knjz051_3aQuery::getData($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            //課程学科
            $row["COURSE_MAJORCD"] = $row["COURSECD"].$row["MAJORCD"]."　".$row["COURSENAME"]."　".$row["MAJORNAME"];
            //学校区分
            $sep = ($row["SCHOOLDIV"] != "" && $row["NAME1"] != "") ? ":" : "";
            $row["SCHOOLDIV_NAME"] = $row["SCHOOLDIV"].$sep.$row["NAME1"];

            $arg["data"][] = $row; 
        }
        $result->free();

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if ($model->cmd == "chg_year"){
            $arg["reload"] = "parent.right_frame.location.href='knjz051_3aindex.php?cmd=edit&mode=2';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz051_3aForm1.html", $arg); 
    }
}
?>
