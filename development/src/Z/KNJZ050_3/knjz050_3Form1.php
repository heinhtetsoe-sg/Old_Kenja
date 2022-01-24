<?php

require_once('for_php7.php');

class knjz050_3Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz050_3index.php", "", "edit");
        
        //DB接続
        $db     = Query::dbCheckOut();
        
        //年度コンボ
        $opt = array();
        $query = knjz050_3Query::getYear($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["YEAR"] = $model->field["YEAR"] ? $model->field["YEAR"] : $model->year;
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);

        //基本（学校マスタの学校区分）
        $school_div = $db->getOne(knjz050_3Query::getSchoolDiv($model, ""));
        $arg["SCHOOLDIV"] = $school_div;

        //データ取得
        $query = knjz050_3Query::getData($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
             //レコードを連想配列のまま配列$arg[data]に追加していく。 
             array_walk($row, "htmlspecialchars_array");
             $row["COURSE_MAJORCD_SET"] = $row["COURSE_MAJORCD"].'　 '.$row["COURSE_MAJORNAME"];
             $arg["data"][] = $row; 
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");
        
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz050_3Form1.html", $arg); 
    }
}
?>
