<?php

require_once('for_php7.php');

class knjl398qForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl398qindex.php", "", "edit");

        //DB接続
        $db     = Query::dbCheckOut();

        //校種コンボ
        $extra = "onChange=\"return btn_submit('changeType')\"";
        $query = knjl398qQuery::getNameMst('L019');
        makeCmb($objForm, $arg, $db, $query, "SELECT_FINSCHOOL_TYPE", $model->selectFinschoolType, $extra, 1);
        
        if($model->selectFinschoolType != ""){
            $arg["pref"] = 1;
            //県コンボ追加
            $extra = "onChange=\"return btn_submit('changeType')\"";
            $query = knjl398qQuery::getPrefMst();
            makeCmb($objForm, $arg, $db, $query, "SELECT_PREFCD", $model->selectPrefCd, $extra, 1);
        }
            
        //出身学校一覧取得
        $query  = knjl398qQuery::selectQuery($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            if ($row["FINSCHOOLCD"] == $model->finschoolcd) {
                $row["FINSCHOOL_TYPE"] = ($row["FINSCHOOL_TYPE"]) ? $row["FINSCHOOL_TYPE"] : "　";
                $row["FINSCHOOL_TYPE"] = "<a name=\"target\">{$row["FINSCHOOL_TYPE"]}</a><script>location.href='#target';</script>";
            }
            $arg["data"][] = $row; 
        }
        $result->free();

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl398qForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $opt[] = array('label' => '', 'value' => '');
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = (($value === '0' || $value) && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, &$model) {
    if($model->selectFinschoolType != ""){
        //追加ボタンを作成する
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
    }
}
?>
