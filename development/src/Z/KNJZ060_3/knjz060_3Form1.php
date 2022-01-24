<?php

require_once('for_php7.php');

class knjz060_3Form1
{
    function main(&$model)
    {
         $arg["jscript"] = "";
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz060_3index.php", "", "edit");
        //処理ごとに年度を再セット
        if ($model->leftyear !== "") {
            $model->year = $model->leftyear;
        }
        
        //DB接続
        $db     = Query::dbCheckOut();
        
        //前年度コピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度コピー", $extra);
        
        //年度コンボ
        $opt = array();
        $query = knjz060_3Query::getYear($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["YEAR"] = $model->field["YEAR"] ? $model->field["YEAR"] : $model->year;
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);

        //学校校種コンボ
        $opt = array();
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $opt[] = array('label' => '--全て--', 'value' => '');
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
        } else {
            $opt[] = array('label' => '--全て--', 'value' => '');
        }
        $query = knjz060_3Query::getNamecd($model, "A023");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["SCHOOL_KIND"] = $model->field["SCHOOL_KIND"] ? $model->field["SCHOOL_KIND"] : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["SCHOOL_KIND"] = knjCreateCombo($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $opt, $extra, 1);
        

        //データ取得
        $query = knjz060_3Query::getData($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            //年度データがない場合、対象年度の値をセットする(右画面にパラメータで渡す為)
            if (!$row["YEAR"]) {
                $row["YEAR"] = $model->field["YEAR"];
            }
            //更新後この行が画面の先頭に来るようにする
            if ($row["CLASSCD"].'-'.$row["SCHOOL_KIND"] == $model->classcd.'-'.$model->school_kind) {
                $row["CLASSNAME"] = ($row["CLASSNAME"]) ? $row["CLASSNAME"] : "　";
                $row["CLASSNAME"] = "<a name=\"target\">{$row["CLASSNAME"]}</a><script>location.href='#target';</script>";
            }
            if ($row["REMARK1_004"]) {
                $row["REMARK1_004"] = '順番：'.$row["REMARK1_004"];
            }
            if ($row["REMARK1_005"]) {
                $row["REMARK1_005"] = '順番：'.$row["REMARK1_005"];
            }
            if ($row["REMARK1_007"]) {
                $row["REMARK1_007"] = '順番：'.$row["REMARK1_007"];
            }
            if ($row["REMARK2_007"]) {
                $row["REMARK2_007"] = '出力教科名：'.$row["REMARK2_007"];
            }
            
            $row["CLASSCD_SET"] = $row["CLASSCD"].'-'.$row["SCHOOL_KIND"];
            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz060_3Form1.html", $arg); 
    }
}
?>
