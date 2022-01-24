<?php

require_once('for_php7.php');

/********************************************************************/
/* スクーリング時間割登録・削除                     山城 2005/03/16 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm110Form1
{
    function main(&$model)
    {

        //セキュリティーチェック
        if(AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm      = new form;
        $db           = Query::dbCheckOut();
        $arg["start"] = $objForm->get_start("list", "POST", "knjm110index.php", "", "edit");

        //年コンボボックス 今年度・今年度+1
        $opt_year = array();

        $opt_year[0] = array("label" => CTRL_YEAR,
                             "value" => CTRL_YEAR);
        $opt_year[1] = array("label" => CTRL_YEAR+1,
                             "value" => CTRL_YEAR+1);

        if (!$model->Year)  $model->Year = CTRL_YEAR;

        $objForm->ae( array("type"    => "select",
                            "name"    => "GrYEAR",
                            "size"    => "1",
                            "value"   => $model->Year,
                            "extrahtml" => "onChange=\"btn_submit('init');\"",
                            "options" => $opt_year));

        $arg["GrYEAR"] = $objForm->ge("GrYEAR");

        //講座コンボ
        $opt_chair = array();
        $db = Query::dbCheckOut();
        $result = $db->query(knjm110Query::getAuth($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_chair[] = array("label" => $row["CHAIRNAME"],
                                 "value" => $row["CHAIRCD"]);
        }

        if (!$model->Chair){
            $model->Chair        = $opt_chair[0]["value"];
        }

        $objForm->ae( array("type"    => "select",
                            "name"    => "Chair",
                            "size"    => "1",
                            "value"   => $model->Chair,
                            "extrahtml" => "onChange=\"btn_submit('kch');\" ",
                            "options" => $opt_chair));

        $arg["Chair"] = $objForm->ge("Chair");

        $result->free();
        Query::dbCheckIn($db);

        //担当者表示
        $chair_name = "";
        $db = Query::dbCheckOut();
        $result = $db->query(knjm110Query::getStaffname($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($chair_name != "") $chair_name .= "、";
            $chair_name .= $row["STAFFNAME_SHOW"];
        }

        $arg["ChaiShow"] = $chair_name;

        $result->free();
        Query::dbCheckIn($db);

        //講座データ一覧
        $syoki = 0;
        $db = Query::dbCheckOut();
        $result = $db->query(knjm110Query::ReadQuery($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($model->cmd == "kch" && $syoki == 0){
                $model->Periodcd      = $row["PERIODCD"];
                $model->Schooling_seq = $row["SCHOOLING_SEQ"];
                $model->Remark        = $row["REMARK"];
                $model->Exedate       = $row["EXECUTEDATE"];
            }
            array_walk($row, "htmlspecialchars_array");
            if($row["EXECUTED"] == 1) {
                $row["EXEDATE"] = $row["EXECUTEDATE"];
            } else {
                $row["EXEDATE"] = View::alink("knjm110index.php",
                                            $row["EXECUTEDATE"],
                                            "target=\"right_frame\"",
                                            array("cmd"      => "edit",
                                                  "PERIODCD" => $row["PERIODCD"],
                                                  "SCHOOLING_SEQ" => $row["SCHOOLING_SEQ"],
                                                  "REMARK" => $row["REMARK"],
                                                  "EXEDATE"     => $row["EXECUTEDATE"]
                                           ));
            }
            $row["KOUJI"] = $row["NAME1"];
            $row["KAISUU"] = "第".$row["SCHOOLING_SEQ"]."回";
            $arg["data"][] = $row;
            $syoki++;
        }
        if ($model->cmd == "kch" && $syoki == 0){
            $model->Periodcd      = 0;
            $model->Schooling_seq = 0;
            $model->Remark        = "";
        }
        $result->free();
        Query::dbCheckIn($db);

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();

        if (($model->cmd == "list" || $model->cmd== "kch" || $model->cmd== "init") && VARS::get("ed") != "1")
            $arg["reload"] = "window.open('knjm110index.php?cmd=edit&init=1','right_frame');";

        View::toHTML($model, "knjm110Form1.html", $arg);

    }
}
?>
