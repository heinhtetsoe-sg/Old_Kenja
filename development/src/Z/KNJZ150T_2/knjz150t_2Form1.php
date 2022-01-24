<?php

require_once('for_php7.php');

/********************************************************************/
/* 学籍番号バーコードラベル                                         */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：テーブル変更に伴う修正                   山城 2005/04/14 */
/********************************************************************/

class knjz150t_2Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjz150t_2Form1", "POST", "knjz150t_2index.php", "", "knjz150t_2Form2");

		//NO001
        $db     = Query::dbCheckOut();
//        $query  = "SELECT * FROM TEXTBOOK_MST ORDER BY TEXTBOOKCD";NO001
		$query  = knjz150t_2Query::getData();
		$result = $db->query($query);
  		//NO001

        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            if ($row["TEXTBOOKCD"] == $model->textbookcd) {
                $row["TEXTBOOKDIV"] = ($row["TEXTBOOKDIV"]) ? $row["TEXTBOOKDIV"] : "　";
                $row["TEXTBOOKDIV"] = "<a name=\"target\">{$row["TEXTBOOKDIV"]}</a><script>location.href='#target';</script>";
            }
            $arg["data"][] = $row; 
        }
        $result->free();
        Query::dbCheckIn($db);
    
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz150t_2Form1.html", $arg); 
    }
}
?>
