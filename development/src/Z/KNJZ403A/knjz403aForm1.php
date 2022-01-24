<?php

require_once('for_php7.php');


class knjz403aForm1{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjz403aForm1", "POST", "knjz403aindex.php", "", "knjz403aForm1");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //前年度からコピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["copy"] = knjCreateBtn($objForm, "copy", "前年度からコピー", $extra);

        //データ表示
        $dataArray = array();
        $query = knjz403aQuery::selectQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $dataArray[$row["SCHOOL_KIND"]][$row["GRADE"]][$row["L_CD"]][$row["M_CD"]] = $row;
        }
        $result->free();

        foreach ($dataArray as $schoolKind => $schoolVal) {
            foreach ($schoolVal as $grade => $gVal) {
                $setLData = "";
                $gSpan = 0;
                $setGname = "";
                $setLMData = "";
                foreach ($gVal as $lCd => $lVal) {
                    $mcnt = 0;
                    $setLData = "";
                    $setMData = "";
                    foreach ($lVal as $mCd => $mVal) {
                        $setGname = $mVal["GRADE_NAME1"];
                        if ($mcnt == 0) {
                            $lSpan = get_count($lVal) > 0 ?get_count($lVal) : "1";
                            $setLname = $lCd.":".$mVal["L_NAME"];
                            $setLData .= "<td nowrap width=\"40%\" bgcolor=\"#ffffff\" rowspan=\"{$lSpan}\"><a href=\"knjz403aindex.php?cmd=edit&SCHOOL_KIND={$mVal["SCHOOL_KIND"]}&GRADE={$mVal["GRADE"]}&L_CD={$lCd}&M_CD={$mCd}\" target=\"right_frame\">{$setLname}</a></td>";
                        } else {
                            $setMData .= "<tr>";
                        }
                        $setMData .= "<td nowrap width=\"40%\" bgcolor=\"#ffffff\">{$mVal["M_NAME"]}</td></tr>";
                        $mcnt++;
                        $gSpan++;
                    }
                    $setLMData .= $setLData.$setMData;
                }
                $gSpan = $gSpan > 0 ? $gSpan : "1";
                $setDataGrade = "<tr><td nowrap width=\"20%\" bgcolor=\"#ffffff\" rowspan=\"{$gSpan}\">{$setGname}</td>";
                $arg["data"][]["MEISAI"] = $setDataGrade.$setLMData;
            }
        }

        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz403aForm1.html", $arg); 

    }
}
?>
