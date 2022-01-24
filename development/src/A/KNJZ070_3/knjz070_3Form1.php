<?php

require_once('for_php7.php');

class knjz070_3Form1
{
    function main($model)
    {
         $arg["jscript"] = "";
        //権限チェック
        if ($model->auth != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz070_3index.php", "", "edit");
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
        $query = knjz070_3Query::getYear($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["YEAR"] = $model->field["YEAR"] ? $model->field["YEAR"] : $model->year;
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);
        
        //学期数取得
        $model->semesterCount = $db->getOne(knjz070_3Query::getSemesterMst($model, ""));

        //教科コンボ
        $opt = array();
        $opt[] = array('label' => '--全て--', 'value' => '');
        $query = knjz070_3Query::getClasscd($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $model->field["CLASSCD_SET"] = $model->field["CLASSCD_SET"] ? $model->field["CLASSCD_SET"] : $opt[0]["value"];
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["CLASSCD_SET"] = knjCreateCombo($objForm, "CLASSCD_SET", $model->field["CLASSCD_SET"], $opt, $extra, 1);
        
        //宮城、常磐のSEQ=012のみを表示するプロパティ（見た目は逆）
        if ($model->Properties["kari_useMiyagiTokiwa"] !== '1') {
            $arg["kari_useMiyagiTokiwa"] = "1";
        }
        //データ取得
        $query = knjz070_3Query::getData($model);
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
            if ($row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"] == $model->classcd.'-'.$model->school_kind.'-'.$model->curriculum_cd.'-'.$model->subclasscd) {
                $row["SUBCLASSNAME"] = ($row["SUBCLASSNAME"]) ? $row["SUBCLASSNAME"] : "　";
                $row["SUBCLASSNAME"] = "<a name=\"target\">{$row["SUBCLASSNAME"]}</a><script>location.href='#target';</script>";
            }
            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz070_3Form1.html", $arg); 
    }
}
?>
