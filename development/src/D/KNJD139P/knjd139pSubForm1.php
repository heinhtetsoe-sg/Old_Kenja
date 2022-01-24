<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd139pSubForm1 {
    function main(&$model) {
    
        $objForm = new form;
        $db = Query::dbCheckOut();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("SubForm1", "POST", "knjd139pindex.php", "", "SubForm1");
        
        //学籍番号・生徒氏名表示
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //年度・学期表示
        $arg["YEAR_SEMESTER"] = CTRL_YEAR."年度　".CTRL_SEMESTERNAME;

        //委員会リスト
        $query = knjd139pQuery::getCommittee($model, "1");
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row = str_replace("-","/",$row);
                $row["COMMITTEE"] = "　".$row["GRADE"]." ／ ".$row["SEMESTERNAME"]." ／ ".$row["COMMITTEENAME"]." ／ ".$row["NAME1"];
                $arg["data"][] = $row;
        }
        
        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"return top.main_frame.right_frame.closeit()\"" ));
        $arg["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
                            
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd139pSubForm1.html", $arg);
    }
}
?>
