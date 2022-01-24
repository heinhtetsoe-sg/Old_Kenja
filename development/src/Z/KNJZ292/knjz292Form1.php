<?php

require_once('for_php7.php');

class knjz292Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz292index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        
        //年度
        $arg["YEAR"] = CTRL_YEAR.'年度';

        //職員リスト
        $getRow = array();
        $query = knjz292Query::getList($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            if ($row["STAFFCD"] == $model->staffcd) {
                $row["STAFFNAME"] = ($row["STAFFNAME"]) ? $row["STAFFNAME"] : "　";
                $row["STAFFNAME"] = "<a name=\"target\">{$row["STAFFNAME"]}</a><script>location.href='#target';</script>";
            }
            //異動情報を取得(県側)
            $query = knjz292Query::getRow($model, $row["STAFFCD"]);
            $getRow = $db2->getRow($query, DB_FETCHMODE_ASSOC);
            if ($getRow["IDOU_DIV"] === '2') {
                $row["IDOU_DIV"] = $getRow["IDOU_DIV"].':転出';
            } else if ($getRow["IDOU_DIV"] === '3') {
                $row["IDOU_DIV"] = $getRow["IDOU_DIV"].':退職';
            }
            $row["IDOU_DATE"] = str_replace("-","/",$getRow["IDOU_DATE"]);
            $row["ASSIGNMENT_DATE"] = str_replace("-","/",$getRow["ASSIGNMENT_DATE"]);
            if ($getRow["TO_FINSCHOOLCD"] === '9999999-9999') {
                $row["TO_EDBOARD_SCHOOLNAME"] = 'その他';
            } else {
                $row["TO_EDBOARD_SCHOOLNAME"] = $getRow["TO_EDBOARD_SCHOOLNAME"];
            }
            
            $arg["data"][] = $row; 
        }
        $result->free();

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "list"){
            $arg["reload"] = "parent.right_frame.location.href='knjz292index.php?cmd=new';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz292Form1.html", $arg);
    }
}

?>
