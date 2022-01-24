<?php

require_once('for_php7.php');

class knjm210m_2Form3
{   
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjm210m_2index.php", "", "main");
        $db = Query::dbCheckOut();

        //氏名
        $sch_name = $db->getOne(knjm210m_2Query::getSchName($model));
        $arg["data"]["NAME"] = "学籍番号：".$model->schregno."　　　"."氏名：".$sch_name;
        //科目名
        $sch_name = $db->getOne(knjm210m_2Query::getSubclassName($model));
        $arg["data"]["SUBCLASSNAME"] = "　　　　　　"."（　全科目　）";

        //スクーリング出席状況詳細（全科目）
        $rec_cnt = 0;   //行番号
        $result = $db->query(knjm210m_2Query::getSchDetailSubclassAll($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $rec_cnt++;

            array_walk($row, "htmlspecialchars_array");

            $row["REC_NO"] = $rec_cnt;
            $row["EXECUTEDATE"] = str_replace("-","/",$row["EXECUTEDATE"]);

            $arg["data1"][] = $row;
        }

        //レポート提出状況詳細（全科目）
        $rec_cnt = 0;   //行番号
        $result = $db->query(knjm210m_2Query::getRepDetailSubclassAll($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rec_cnt++;

            array_walk($row, "htmlspecialchars_array");

            $row["REC_NO"] = $rec_cnt;
            $row["RECEIPT_DATE"] = str_replace("-","/",$row["RECEIPT_DATE"]);
            $row["GRAD_DATE"] = str_replace("-","/",$row["GRAD_DATE"]);

            $arg["data2"][] = $row;
        }

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_cancel",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print1",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin1('" . SERVLET_URL . "');\"" ) );

        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_print2",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin2('" . SERVLET_URL . "');\"" ) );

        $arg["button"] = array("BTN_CLEAR"  => $objForm->ge("btn_cancel"),
                               "BTN_PRINT1"  => $objForm->ge("btn_print1"),
                               "BTN_PRINT2"  => $objForm->ge("btn_print2") );

        //HIDDEN
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJM210M"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER",
                            "value"     => CTRL_SEMESTER) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->schregno) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRINTKIND",
                            "value"     => "") );

        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useCreditTime", $model->Properties["useCreditTime"]);

        $result->free();
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm210m_2Form3.html", $arg); 
    }
}
?>
