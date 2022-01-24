<?php

require_once('for_php7.php');

class knje063p_2form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knje063p_2index.php", "", "right_list");

        $db = Query::dbCheckOut();

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"] = $db->getOne(knje063p_2Query::getName($model->schregno));

        //観点履歴
        $setData = array();
        if ($model->schregno) {
            $befSubclassCd = "";
            $statusCnt = 0;
            $result = $db->query(knje063p_2Query::selectQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $diffCd = $row["YEAR"].$row["CLASSCD"].$row["SUBCLASSCD"];
                if ($befSubclassCd == $diffCd) {
                    $statusCnt++;
                    if ($statusCnt > 5) {
                        continue;
                    } else {
                        $setData["STATUS".$statusCnt] = $row["STATUS"];
                    }
                } else {
                    if ($befSubclassCd) {
                        $arg["data"][] = $setData;
                    }
                    $setData = array();
                    $statusCnt = 1;

                    $setData["YEAR"] = $row["YEAR"];
                    $setData["GRADE_NAME1"] = $row["GRADE_NAME1"];
                    $setData["SCHREGNO"] = $row["SCHREGNO"];
                    $setData["SUBCLASSCD"] = $row["SUBCLASSCD"];
                    $setData["CLASSNAME"] = $row["CLASSNAME"];
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $setData["SUBCLASSNAME"] = $row["SUBCLASSCD"].":".$row["SUBCLASSNAME"];
                    } else {
                        $setData["SUBCLASSNAME"] = $row["SUBCLASSNAME"];
                    }
                    $setData["STATUS".$statusCnt] = $row["STATUS"];
                    $setData["STATUS9"] = $row["VALUATION"];
                }
                $befSubclassCd = $diffCd;
            }
            if ($befSubclassCd) {
                $arg["data"][] = $setData;
            }
        }

        //終了ボタンを作成する
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje063p_2Form1.html", $arg);
    }

}
?>
