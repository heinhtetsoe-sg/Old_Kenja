<?php

require_once('for_php7.php');

class knjz236_2Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz236_2index.php", "", "list");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //科目リスト
        makeSubclassList($arg, $db, $model);

        //対象年度
        $arg["year"] = CTRL_YEAR;

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz236_2Form1.html", $arg); 
    }
}

/***************************************** 以下関数 *********************************************/

//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//代替科目・学科リスト
function makeSubclassList(&$arg, $db, $model)
{
    //代替先科目を作成する
    $query = knjz236_2Query::selectQuery1_1($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $mainData = "<tr> ";
        //代替先科目
        $mainData .= "<td bgcolor=\"#ffffff\" nowrap align=\"center\" rowspan=".$row["CNT"].">".$row["VALUE_SUB"]."</td> ";
        $mainData .= "<td bgcolor=\"#ffffff\" nowrap rowspan=".$row["CNT"].">".$row["SUB_NAME"]."</td> ";

        //代替元科目を作成する
        $att_cnt = 0;
        $attendData = "";
        //教育課程対応
        $query = knjz236_2Query::selectQuery1_2($model, $row["VALUE_SUB"]);
        $attendsub = $db->query($query);
        while ($arow = $attendsub->fetchRow(DB_FETCHMODE_ASSOC)) {
            $attendData .= ($att_cnt == 0) ? "" : "<tr> ";
            $link = View::alink("knjz236_2index.php",
                                $arow["VALUE_ATT"],
                                "target=right_frame",
                                array("cmd"                     => "sel",
                                      "SUBSTITUTION_SUBCLASSCD" => $arow["VALUE_SUB"],
                                      "ATTEND_SUBCLASSCD"       => $arow["VALUE_ATT"] ));
            //代替元科目
            $bgcolor = ($arow["MAJOR_ATTEND_SUBCLASSCD"] == "") ? "#ffff00" : "#ffffff";
            $attendData .= "<td bgcolor=\"#ffffff\" nowrap align=\"center\" rowspan=".$arow["CNT"].">".$link."</td> ";
            $attendData .= "<td bgcolor=\"{$bgcolor}\" nowrap rowspan=".$arow["CNT"].">".$arow["ATTEND_NAME"]."</td> ";
            $att_cnt++;

            //学科を作成する
            $major_cnt = 0;
            $majorData = "";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query = knjz236_2Query::selectQuery1_3($model, substr($arow["VALUE_SUB"],7,13), substr($arow["VALUE_ATT"],7,13), substr($arow["VALUE_SUB"],0,2), substr($arow["VALUE_ATT"],0,2), substr($arow["VALUE_SUB"],3,1), substr($arow["VALUE_ATT"],3,1), substr($arow["VALUE_SUB"],5,1), substr($arow["VALUE_ATT"],5,1));
            } else {
                $query = knjz236_2Query::selectQuery1_3($model, $arow["VALUE_SUB"], $arow["VALUE_ATT"],"","","","","","");
            }
            $majorsub = $db->query($query);
            while ($mrow = $majorsub->fetchRow(DB_FETCHMODE_ASSOC)) {
                $majorData .= ($major_cnt == 0) ? "" : "<tr> ";
                if (strlen($mrow["GRADE"])) {
                    $majorData .= "<td bgcolor=\"#ffffff\" nowrap align=\"center\" >".$mrow["GRADE"]."-".$mrow["COURSECD"].$mrow["MAJORCD"].$mrow["COURSECODE"]."</td> ";
                    $majorData .= "<td bgcolor=\"#ffffff\" nowrap>{$mrow["GRADE_NAME1"]}{$mrow["COURSENAME"]}{$mrow["MAJORNAME"]}／{$mrow["COURSECODENAME"]}</td> </tr>";
                } else {
                    $majorData .= "<td bgcolor=\"#ffffff\" nowrap align=\"center\" ></td> ";
                    $majorData .= "<td bgcolor=\"#ffffff\" nowrap></td> </tr>";
                }
                $major_cnt++;
            }
            $majorsub->free();
            $attendData = $attendData.$majorData;
        }
        $attendsub->free();
        $arg["data"][]["MAINLIST"] = $mainData.$attendData;
    }
    $result->free();
}
?>
