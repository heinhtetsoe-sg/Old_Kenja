<?php

require_once('for_php7.php');

class knjh400_kyoukaForm3
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh400_kyoukaindex.php", "", "edit");

        $db = Query::dbCheckOut();
        
        //年度
        $arg["CTRLYEAR"] = $model->year."年度";
        $ctrlyear = $model->year;
        //学年
        //SCHREG_REGD_BASE_MSTから取得
        $schregQuery = knjh400_kyoukaQuery::getSchreg($model->year, $model->GAKUSEKI);
        $schregRow = $db->getRow($schregQuery, DB_FETCHMODE_ASSOC);
        
        $grade = $schregRow["GRADE_CD"];
        
        $model->gakunen = number_format((int)$grade);

        $arg["GAKUNEN"] = $model->gakunen."年生";
        //名前
        $arg["NAME"] = $schregRow["HR_CLASS_NAME1"]."組 ".number_format($schregRow["ATTENDNO"])."番 ".$schregRow["NAME_SHOW"];

        Query::dbCheckIn($db);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd",
                            "value"     => $model->cmd
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh400_kyoukaForm3.html", $arg);
    }
} 
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //終了
    $extra = "onclick=\"closecheck();return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
