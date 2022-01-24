<?php

require_once('for_php7.php');

class knjf150bForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjf150bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $hr_name = $db->getOne(knjf150bQuery::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

        //データを取得
        $setval = array();
        $firstflg = true;   //初回フラグ
        $cnt = get_count($db->getcol(knjf150bQuery::selectQuery($model)));
        if($model->schregno && $cnt) {
            $result = $db->query(knjf150bQuery::selectQuery($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["VISIT_TIME"] = str_replace("-", "/", $row["VISIT_DATE"]).' '.$row["VISIT_HOUR"].':'.$row["VISIT_MINUTE"];

                $row["VISIT_TIME"] = View::alink("knjf150bindex.php", $row["VISIT_TIME"], "target=\"edit_frame\" tabindex=\"-1\"",
                                                array("SCHREGNO"        => $row["SCHREGNO"],
                                                      "VISIT_DATE"      => $row["VISIT_DATE"],
                                                      "VISIT_HOUR"      => $row["VISIT_HOUR"],
                                                      "VISIT_MINUTE"    => $row["VISIT_MINUTE"],
                                                      "cmd"             => "form2"));


                if ($firstflg) {
                    $setval = $row;
                    $firstflg = false;
                } else {
                    $visit = $setval["VISIT_DATE"].':'.$setval["VISIT_HOUR"].':'.$setval["VISIT_MINUTE"];
                    $arg["data"][] = $setval;
                    $setval = $row;
                }

            }
            $visit = $setval["VISIT_DATE"].':'.$setval["VISIT_HOUR"].':'.$setval["VISIT_MINUTE"];

            $arg["data"][] = $setval;
        }

        //hidden作成
        makeHidden($objForm, $model);


        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "edit"){
            //データを削除
            $model->visit_date = "";
            $model->visit_hour = "";
            $model->visit_minute = "";

            $arg["reload"]  = "window.open('knjf150bindex.php?cmd=form2&SCHREGNO=$model->schregno','edit_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf150bForm1.html", $arg);
    }
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}
?>
