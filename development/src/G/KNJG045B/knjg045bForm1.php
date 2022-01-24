<?php

require_once('for_php7.php');

class knjg045bForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("right_list", "POST", "knjg045bindex.php", "", "edit");

        //登録・更新・削除の際、履歴を再読込する
        if (VARS::get("cmd") == "from_edit"){
            $arg["reload"] = "window.open('knjg045bindex.php?cmd=right_list','right_frame');";
        }

        //DB接続
        $db = Query::dbCheckOut();
        
        $arg['DIARY_DATE'] = str_replace('-','/',$model->diarydate);

        //履歴表示
        if ($model->diarydate) {
            $result = $db->query(knjg045bQuery::getSEQDat($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $row["URL"] = View::alink("knjg045bindex.php", $row["SEQ"], "target=edit_frame",
                                            array("cmd"         => "edit",
                                                  "SCHOOLCD"    => $row["SCHOOLCD"],
                                                  "SCHOOL_KIND" => $row["SCHOOL_KIND"],
                                                  "DIARY_DATE"  => $row["DIARY_DATE"],
                                                  "SEQ"         => $row["SEQ"],
                                                  ));

                $row['REMARK1'] = preg_replace('/\n.+/','',$row['REMARK1']);
                $row['REMARK2'] = preg_replace('/\n.+/','',$row['REMARK2']);

                $arg["data"][] = $row;
            }
            $result->free();
        }

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "from_list"){
            $arg["reload"] = "window.open('knjg045bindex.php?cmd=from_right&SCHREGNO=$model->schregno','edit_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg045bForm1.html", $arg);
    }
}
?>
